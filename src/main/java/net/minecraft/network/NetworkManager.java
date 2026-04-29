package net.minecraft.network;

import com.google.common.collect.Queues;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalIoHandler;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.util.CryptManager;
import net.minecraft.util.ITickable;
import net.minecraft.util.LazyLoadBase;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import javax.crypto.SecretKey;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NetworkManager extends SimpleChannelInboundHandler<Packet<?>> {

	public static final Marker NETWORK_MARKER = MarkerManager.getMarker("NETWORK");
	public static final Marker NETWORK_PACKETS_MARKER = MarkerManager.getMarker("NETWORK_PACKETS", NETWORK_MARKER);
	public static final AttributeKey<EnumConnectionState> PROTOCOL_ATTRIBUTE_KEY = AttributeKey.valueOf("protocol");

	public static final ThreadFactory nettyIOFactory = Thread.ofVirtual().name("Netty IO #%d", 0).factory();
	public static final LazyLoadBase<MultiThreadIoEventLoopGroup> CLIENT_NIO_EVENT_LOOP = new LazyLoadBase<>() {
		protected MultiThreadIoEventLoopGroup load() {

			return new MultiThreadIoEventLoopGroup(0, nettyIOFactory, NioIoHandler.newFactory());
		}
	};
	public static final LazyLoadBase<MultiThreadIoEventLoopGroup> CLIENT_LOCAL_EVENT_LOOP = new LazyLoadBase<>() {
		protected MultiThreadIoEventLoopGroup load() {

			return new MultiThreadIoEventLoopGroup(0, nettyIOFactory, LocalIoHandler.newFactory());
		}
	};
	public static final LazyLoadBase<MultiThreadIoEventLoopGroup> CLIENT_EPOLL_EVENT_LOOP = new LazyLoadBase<>() {
		protected MultiThreadIoEventLoopGroup load() {

			return new MultiThreadIoEventLoopGroup(0, nettyIOFactory, EpollIoHandler.newFactory());
		}
	};

	private static final Logger LOGGER = LogManager.getLogger();
	private final EnumPacketDirection direction;
	private final Queue<NetworkManager.InboundHandlerTuplePacketListener> outboundPacketsQueue = Queues.newConcurrentLinkedQueue();
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	/**
	 * The active channel
	 */
	private Channel channel;

	/**
	 * The address of the remote party
	 */
	private SocketAddress socketAddress;

	/**
	 * The INetHandler instance responsible for processing received packets
	 */
	private INetHandler packetListener;

	/**
	 * A String indicating why the network has shutdown.
	 */
	private ITextComponent terminationReason;
	private boolean isEncrypted;
	private boolean disconnected;

	public NetworkManager(EnumPacketDirection packetDirection) {

		direction = packetDirection;
	}

	/**
	 * Create a new NetworkManager from the server host and connect it to the server
	 */
	public static NetworkManager createNetworkManagerAndConnect(InetAddress address, int serverPort, boolean useNativeTransport) {

		final NetworkManager networkmanager = new NetworkManager(EnumPacketDirection.CLIENTBOUND);
		Class<? extends SocketChannel> oclass;
		LazyLoadBase<? extends EventLoopGroup> lazyloadbase;

		if (Epoll.isAvailable() && useNativeTransport) {
			oclass = EpollSocketChannel.class;
			lazyloadbase = CLIENT_EPOLL_EVENT_LOOP;
		} else {
			oclass = NioSocketChannel.class;
			lazyloadbase = CLIENT_NIO_EVENT_LOOP;
		}

		new Bootstrap()
				.group(lazyloadbase.getValue())
				.handler(
						new ChannelInitializer<>() {
							protected void initChannel(Channel channel) {

								channel.config().setOption(ChannelOption.TCP_NODELAY, true);

								channel.pipeline()
										.addLast("timeout", new ReadTimeoutHandler(30))
										.addLast("splitter", new NettyVarint21FrameDecoder())
										.addLast("decoder", new NettyPacketDecoder(EnumPacketDirection.CLIENTBOUND))
										.addLast("prepender", new NettyVarint21FrameEncoder())
										.addLast("encoder", new NettyPacketEncoder(EnumPacketDirection.SERVERBOUND))
										.addLast("packet_handler", networkmanager);
							}
						}
				)
				.channel(oclass)
				.connect(address, serverPort)
				.syncUninterruptibly();
		return networkmanager;
	}

	/**
	 * Prepares a clientside NetworkManager: establishes a connection to the socket supplied and configures the channel
	 * pipeline. Returns the newly created instance.
	 */
	public static NetworkManager provideLocalClient(SocketAddress address) {

		final NetworkManager manager = new NetworkManager(EnumPacketDirection.CLIENTBOUND);

		new Bootstrap()
				.group(CLIENT_LOCAL_EVENT_LOOP.getValue())
				.handler(new ChannelInitializer<>() {
					         protected void initChannel(Channel channel) {

						         channel.pipeline().addLast("packet_handler", manager);
					         }
				         }
				)
				.channel(LocalChannel.class)
				.connect(address)
				.syncUninterruptibly();

		return manager;
	}

	public void channelActive(ChannelHandlerContext context) throws Exception {

		super.channelActive(context);
		channel = context.channel();
		socketAddress = channel.remoteAddress();

		try {
			setConnectionState(EnumConnectionState.HANDSHAKING);
		} catch (Throwable throwable) {
			LOGGER.fatal(throwable);
		}
	}

	/**
	 * Sets the new connection state and registers which packets this channel may send and receive
	 */
	public void setConnectionState(EnumConnectionState newState) {

		channel.attr(PROTOCOL_ATTRIBUTE_KEY).set(newState);
		channel.config().setAutoRead(true);
		LOGGER.debug("Enabled auto read");
	}

	public void channelInactive(ChannelHandlerContext context) {

		closeChannel(new TextComponentTranslation("disconnect.endOfStream"));
	}

	public void exceptionCaught(ChannelHandlerContext context, Throwable throwable) {

		TextComponentTranslation translation;

		if (throwable instanceof TimeoutException) {
			translation = new TextComponentTranslation("disconnect.timeout");
		} else {
			translation = new TextComponentTranslation("disconnect.genericReason", "Internal Exception: " + throwable);
		}

		LOGGER.debug(translation.getUnformattedText(), throwable);
		closeChannel(translation);
	}

	protected void channelRead0(ChannelHandlerContext context, Packet<?> packet) {

		if (channel.isOpen()) {
			try {
				((Packet<INetHandler>) packet).processPacket(packetListener);
			} catch (ThreadQuickExitException ignored) {
			}
		}
	}

	public void sendPacket(Packet<?> packetIn) {

		if (isChannelOpen()) {
			flushOutboundQueue();
			dispatchPacket(packetIn, null);
		} else {
			readWriteLock.writeLock().lock();

			try {
				outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packetIn));
			} finally {
				readWriteLock.writeLock().unlock();
			}
		}
	}

	@SafeVarargs
	public final void sendPacket(Packet<?> packetIn, GenericFutureListener<? extends Future<? super Void>> listener, GenericFutureListener<? extends Future<? super Void>>... listeners) {

		if (isChannelOpen()) {
			flushOutboundQueue();
			dispatchPacket(packetIn, ArrayUtils.add(listeners, 0, listener));
		} else {
			readWriteLock.writeLock().lock();

			try {
				outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packetIn, ArrayUtils.add(listeners, 0, listener)));
			} finally {
				readWriteLock.writeLock().unlock();
			}
		}
	}

	/**
	 * Will commit the packet to the channel. If the current thread 'owns' the channel it will write and flush the
	 * packet, otherwise it will add a task for the channel eventloop thread to do that.
	 */
	private void dispatchPacket(final Packet<?> inPacket, final GenericFutureListener<? extends Future<? super Void>>[] futureListeners) {

		final EnumConnectionState enumconnectionstate = EnumConnectionState.getFromPacket(inPacket);
		final EnumConnectionState enumconnectionstate1 = channel.attr(PROTOCOL_ATTRIBUTE_KEY).get();

		if (enumconnectionstate1 != enumconnectionstate) {
			LOGGER.debug("Disabled auto read");
			channel.config().setAutoRead(false);
		}

		if (channel.eventLoop().inEventLoop()) {
			if (enumconnectionstate != enumconnectionstate1) {
				setConnectionState(enumconnectionstate);
			}

			ChannelFuture channelfuture = channel.writeAndFlush(inPacket);

			if (futureListeners != null) {
				channelfuture.addListeners(futureListeners);
			}

			channelfuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
		} else {
			channel.eventLoop().execute(() -> {

				if (enumconnectionstate != enumconnectionstate1) {
					setConnectionState(enumconnectionstate);
				}

				ChannelFuture channelfuture1 = channel.writeAndFlush(inPacket);

				if (futureListeners != null) {
					channelfuture1.addListeners(futureListeners);
				}

				channelfuture1.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
			});
		}
	}

	/**
	 * Will iterate through the outboundPacketQueue and dispatch all Packets
	 */
	private void flushOutboundQueue() {

		if (channel != null && channel.isOpen()) {
			readWriteLock.readLock().lock();

			try {
				while (!outboundPacketsQueue.isEmpty()) {
					NetworkManager.InboundHandlerTuplePacketListener poll = outboundPacketsQueue.poll();
					dispatchPacket(poll.packet, poll.futureListeners);
				}
			} finally {
				readWriteLock.readLock().unlock();
			}
		}
	}

	/**
	 * Checks timeouts and processes all packets received
	 */
	public void processReceivedPackets() {

		flushOutboundQueue();

		if (packetListener instanceof ITickable) {
			((ITickable) packetListener).update();
		}

		if (channel != null) {
			channel.flush();
		}
	}

	/**
	 * Returns the socket address of the remote side. Server-only.
	 */
	public SocketAddress getRemoteAddress() {

		return socketAddress;
	}

	/**
	 * Closes the channel, the parameter can be used for an exit message (not certain how it gets sent)
	 */
	public void closeChannel(ITextComponent message) {

		if (channel.isOpen()) {
			channel.close().awaitUninterruptibly();
			terminationReason = message;
		}
	}

	/**
	 * True if this NetworkManager uses a memory connection (single player game). False may imply both an active TCP
	 * connection or simply no active connection at all
	 */
	public boolean isLocalChannel() {

		return channel instanceof LocalChannel || channel instanceof LocalServerChannel;
	}

	/**
	 * Adds an encoder+decoder to the channel pipeline. The parameter is the secret key used for encrypted communication
	 */
	public void enableEncryption(SecretKey key) {

		isEncrypted = true;
		channel.pipeline().addBefore("splitter", "decrypt", new NettyEncryptingDecoder(CryptManager.createNetCipherInstance(2, key)));
		channel.pipeline().addBefore("prepender", "encrypt", new NettyEncryptingEncoder(CryptManager.createNetCipherInstance(1, key)));
	}

	public boolean isEncrypted() {

		return isEncrypted;
	}

	/**
	 * Returns true if this NetworkManager has an active channel, false otherwise
	 */
	public boolean isChannelOpen() {

		return channel != null && channel.isOpen();
	}

	public boolean hasNoChannel() {

		return channel == null;
	}

	/**
	 * Gets the current handler for processing packets
	 */
	public INetHandler getNetHandler() {

		return packetListener;
	}

	/**
	 * Sets the NetHandler for this NetworkManager, no checks are made if this handler is suitable for the particular
	 * connection state (protocol)
	 */
	public void setNetHandler(INetHandler handler) {

		Validate.notNull(handler, "packetListener");
		LOGGER.debug("Set listener of {} to {}", this, handler);
		packetListener = handler;
	}

	/**
	 * If this channel is closed, returns the exit message, null otherwise.
	 */
	public ITextComponent getExitMessage() {

		return terminationReason;
	}

	/**
	 * Switches the channel to manual reading modus
	 */
	public void disableAutoRead() {

		channel.config().setAutoRead(false);
	}

	public void setCompressionThreshold(int threshold) {

		if (threshold >= 0) {
			if (channel.pipeline().get("decompress") instanceof NettyCompressionDecoder) {
				((NettyCompressionDecoder) channel.pipeline().get("decompress")).setCompressionThreshold(threshold);
			} else {
				channel.pipeline().addBefore("decoder", "decompress", new NettyCompressionDecoder(threshold));
			}

			if (channel.pipeline().get("compress") instanceof NettyCompressionEncoder) {
				((NettyCompressionEncoder) channel.pipeline().get("compress")).setCompressionThreshold(threshold);
			} else {
				channel.pipeline().addBefore("encoder", "compress", new NettyCompressionEncoder(threshold));
			}
		} else {
			if (channel.pipeline().get("decompress") instanceof NettyCompressionDecoder) {
				channel.pipeline().remove("decompress");
			}

			if (channel.pipeline().get("compress") instanceof NettyCompressionEncoder) {
				channel.pipeline().remove("compress");
			}
		}
	}

	public void checkDisconnected() {

		if (channel != null && !channel.isOpen()) {
			if (disconnected) {
				LOGGER.warn("handleDisconnection() called twice");
			} else {
				disconnected = true;

				if (getExitMessage() != null) {
					getNetHandler().onDisconnect(getExitMessage());
				} else if (getNetHandler() != null) {
					getNetHandler().onDisconnect(new TextComponentTranslation("multiplayer.disconnect.generic"));
				}
			}
		}
	}

	static class InboundHandlerTuplePacketListener {

		private final Packet<?> packet;
		private final GenericFutureListener<? extends Future<? super Void>>[] futureListeners;

		@SafeVarargs
		public InboundHandlerTuplePacketListener(Packet<?> inPacket, GenericFutureListener<? extends Future<? super Void>>... inFutureListeners) {

			packet = inPacket;
			futureListeners = inFutureListeners;
		}

	}

}
