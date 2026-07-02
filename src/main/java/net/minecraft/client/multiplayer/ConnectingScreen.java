package net.minecraft.client.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.component.Button;
import net.minecraft.client.gui.game.DisconnectedScreen;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.ConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.CPacketLoginStart;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectingScreen extends Screen {

	private static final AtomicInteger CONNECTION_ID = new AtomicInteger(0);
	private static final Logger LOGGER = LogManager.getLogger();
	private final Screen lastScreen;
	private NetworkManager networkManager;
	private boolean cancel;

	public ConnectingScreen(Screen parent, Minecraft mcIn, ServerData serverDataIn) {
		mc = mcIn;
		lastScreen = parent;
		ServerAddress serveraddress = ServerAddress.fromString(serverDataIn.serverIP);
		mcIn.loadWorld(null);
		mcIn.setServerData(serverDataIn);
		connect(serveraddress.getIP(), serveraddress.getPort());
	}

	public ConnectingScreen(Screen parent, Minecraft mcIn, String hostName, int port) {
		mc = mcIn;
		lastScreen = parent;
		mcIn.loadWorld(null);
		connect(hostName, port);
	}

	private void connect(String ip, int port) {
		LOGGER.info("Connecting to {}, {}", ip, port);
		(new Thread("Server Connector #" + CONNECTION_ID.incrementAndGet()) {
			public void run() {
				InetAddress inetaddress = null;

				try {
					if (cancel) {
						return;
					}

					inetaddress = InetAddress.getByName(ip);
					networkManager = NetworkManager.createNetworkManagerAndConnect(inetaddress, port, mc.gameSettings.isUsingNativeTransport());
					networkManager.setNetHandler(new NetHandlerLoginClient(networkManager, mc, lastScreen));
					networkManager.sendPacket(new C00Handshake(ip, port, ConnectionState.LOGIN));
					networkManager.sendPacket(new CPacketLoginStart(mc.getSession().getProfile()));
				} catch (UnknownHostException unknownhostexception) {
					if (cancel) {
						return;
					}

					ConnectingScreen.LOGGER.error("Couldn't connect to server", unknownhostexception);
					mc.displayScreen(new DisconnectedScreen(lastScreen, "connect.failed", new TextComponentTranslation("disconnect.genericReason", "Unknown host")));
				} catch (Exception exception) {
					if (cancel) {
						return;
					}

					ConnectingScreen.LOGGER.error("Couldn't connect to server", exception);
					String s = exception.toString();

					if (inetaddress != null) {
						String s1 = inetaddress + ":" + port;
						s = s.replaceAll(s1, "");
					}

					mc.displayScreen(new DisconnectedScreen(lastScreen, "connect.failed", new TextComponentTranslation("disconnect.genericReason", s)));
				}
			}
		}).start();
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void update() {
		if (networkManager != null) {
			if (networkManager.isChannelOpen()) {
				networkManager.processReceivedPackets();
			} else {
				networkManager.checkDisconnected();
			}
		}
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) {
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {
		buttons.clear();
		buttons.add(new Button(0, width / 2 - 100, height / 4 + 120 + 12, I18n.format("gui.cancel")));
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void action(Button button) {
		if (button.id == 0) {
			cancel = true;

			if (networkManager != null) {
				networkManager.closeChannel(new TextComponentString("Aborted"));
			}

			mc.displayScreen(lastScreen);
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(DrawContext context, int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();

		if (networkManager == null) {
			drawCenteredString(fontRenderer, I18n.format("connect.connecting"), width / 2, height / 2 - 50, 16777215);
		} else {
			drawCenteredString(fontRenderer, I18n.format("connect.authorizing"), width / 2, height / 2 - 50, 16777215);
		}

		super.draw(context, mouseX, mouseY, partialTicks);
	}

}
