package net.minecraft.network;

import com.google.common.collect.Lists;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalIoHandler;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.client.network.NetHandlerHandshakeMemory;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerHandshakeTCP;
import net.minecraft.util.LazyLoadBase;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.TextComponentString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadFactory;

public class NetworkSystem {

	public static final ThreadFactory nettyIOFactory = Thread.ofVirtual().name("Netty Server IO #%d", 0).factory();
	public static final LazyLoadBase<MultiThreadIoEventLoopGroup> SERVER_NIO_EVENT_LOOP = new LazyLoadBase<>() {
		protected MultiThreadIoEventLoopGroup load() {

			return new MultiThreadIoEventLoopGroup(0, nettyIOFactory, NioIoHandler.newFactory());
		}
	};
	public static final LazyLoadBase<MultiThreadIoEventLoopGroup> SERVER_LOCAL_EVENT_LOOP = new LazyLoadBase<>() {
		protected MultiThreadIoEventLoopGroup load() {

			return new MultiThreadIoEventLoopGroup(0, nettyIOFactory, LocalIoHandler.newFactory());
		}
	};
	public static final LazyLoadBase<MultiThreadIoEventLoopGroup> SERVER_EPOLL_EVENT_LOOP = new LazyLoadBase<>() {
		protected MultiThreadIoEventLoopGroup load() {

			return new MultiThreadIoEventLoopGroup(0, nettyIOFactory, EpollIoHandler.newFactory());
		}
	};

	private static final Logger LOGGER = LogManager.getLogger();
	/**
	 * Reference to the MinecraftServer object.
	 */
	private final MinecraftServer mcServer;
	private final List<ChannelFuture> endpoints = Collections.synchronizedList(Lists.newArrayList());
	private final List<NetworkManager> networkManagers = Collections.synchronizedList(Lists.newArrayList());
	/**
	 * True if this NetworkSystem has never had his endpoints terminated
	 */
	public volatile boolean isAlive;

	public NetworkSystem(MinecraftServer server) {

		mcServer = server;
		isAlive = true;
	}

	/**
	 * Adds a channel that listens on publicly accessible network ports
	 */
	public void addLanEndpoint(InetAddress address, int port) {

		synchronized (endpoints) {
			Class<? extends ServerSocketChannel> oclass;
			LazyLoadBase<? extends EventLoopGroup> base;

			if (Epoll.isAvailable() && mcServer.shouldUseNativeTransport()) {
				oclass = EpollServerSocketChannel.class;
				base = SERVER_EPOLL_EVENT_LOOP;
				LOGGER.info("Using epoll channel type");
			} else {
				oclass = NioServerSocketChannel.class;
				base = SERVER_NIO_EVENT_LOOP;
				LOGGER.info("Using default channel type");
			}

			endpoints.add(
					new ServerBootstrap()
							.channel(oclass)
							.childHandler(
									new ChannelInitializer<>() {
										protected void initChannel(Channel channel) {

											channel.config().setOption(ChannelOption.TCP_NODELAY, true);

											NetworkManager manager = new NetworkManager(EnumPacketDirection.SERVERBOUND);
											networkManagers.add(manager);

											channel.pipeline()
													.addLast("timeout", new ReadTimeoutHandler(30))
													.addLast("legacy_query", new LegacyPingHandler(NetworkSystem.this))
													.addLast("splitter", new NettyVarint21FrameDecoder())
													.addLast("decoder", new NettyPacketDecoder(EnumPacketDirection.SERVERBOUND))
													.addLast("prepender", new NettyVarint21FrameEncoder())
													.addLast("encoder", new NettyPacketEncoder(EnumPacketDirection.CLIENTBOUND))
													.addLast("packet_handler", manager);

											manager.setNetHandler(new NetHandlerHandshakeTCP(mcServer, manager));
										}
									}
							)
							.group(base.getValue())
							.localAddress(address, port)
							.bind()
							.syncUninterruptibly()
			);
		}
	}

	/**
	 * Adds a channel that listens locally
	 */
	public SocketAddress addLocalEndpoint() {

		ChannelFuture future;

		synchronized (endpoints) {
			future = new ServerBootstrap()
					.channel(LocalServerChannel.class)
					.childHandler(new ChannelInitializer<>() {
						              protected void initChannel(Channel channel) {

							              NetworkManager manager = new NetworkManager(EnumPacketDirection.SERVERBOUND);
							              manager.setNetHandler(new NetHandlerHandshakeMemory(mcServer, manager));
							              networkManagers.add(manager);
							              channel.pipeline().addLast("packet_handler", manager);
						              }
					              }
					)
					.group(SERVER_LOCAL_EVENT_LOOP.getValue())
					.localAddress(LocalAddress.ANY)
					.bind()
					.syncUninterruptibly();

			endpoints.add(future);
		}

		return future.channel().localAddress();
	}

	/**
	 * Shuts down all open endpoints (with immediate effect?)
	 */
	public void terminateEndpoints() {

		isAlive = false;

		for (ChannelFuture future : endpoints) {
			try {
				future.channel().close().sync();
			} catch (InterruptedException var4) {
				LOGGER.error("Interrupted whilst closing channel");
			}
		}
	}

	/**
	 * Will try to process the packets received by each NetworkManager, gracefully manage processing failures and cleans
	 * up dead connections
	 */
	public void networkTick() {

		synchronized (networkManagers) {
			Iterator<NetworkManager> iterator = networkManagers.iterator();

			while (iterator.hasNext()) {
				final NetworkManager manager = iterator.next();

				if (!manager.hasNoChannel()) {
					if (manager.isChannelOpen()) {
						try {
							manager.processReceivedPackets();
						} catch (Exception exception) {
							if (manager.isLocalChannel()) {
								CrashReport crashreport = CrashReport.makeCrashReport(exception, "Ticking memory connection");
								CrashReportCategory crashreportcategory = crashreport.makeCategory("Ticking connection");
								crashreportcategory.addDetail("Connection", manager::toString);
								throw new ReportedException(crashreport);
							}

							LOGGER.warn("Failed to handle packet for {}", manager.getRemoteAddress(), exception);
							final TextComponentString textcomponentstring = new TextComponentString("Internal server error");
							manager.sendPacket(new SPacketDisconnect(textcomponentstring), p_operationComplete_1_ -> manager.closeChannel(textcomponentstring));
							manager.disableAutoRead();
						}
					} else {
						iterator.remove();
						manager.checkDisconnected();
					}
				}
			}
		}
	}

	public MinecraftServer getServer() {

		return mcServer;
	}

}
