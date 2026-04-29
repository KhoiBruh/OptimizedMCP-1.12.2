package net.minecraft.client.network;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.game.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.ConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.INetHandlerLoginClient;
import net.minecraft.network.login.client.CPacketEncryptionResponse;
import net.minecraft.network.login.server.SPacketDisconnect;
import net.minecraft.network.login.server.SPacketEnableCompression;
import net.minecraft.network.login.server.SPacketEncryptionRequest;
import net.minecraft.network.login.server.SPacketLoginSuccess;
import net.minecraft.util.CryptManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;

public class NetHandlerLoginClient implements INetHandlerLoginClient {

	private static final Logger LOGGER = LogManager.getLogger();
	private final Minecraft mc;

	
	private final GuiScreen previousGuiScreen;
	private final NetworkManager networkManager;
	private GameProfile gameProfile;

	public NetHandlerLoginClient(NetworkManager networkManagerIn, Minecraft mcIn, GuiScreen previousScreenIn) {

		networkManager = networkManagerIn;
		mc = mcIn;
		previousGuiScreen = previousScreenIn;
	}

	public void handleEncryptionRequest(SPacketEncryptionRequest packetIn) {

		final SecretKey secretkey = CryptManager.createNewSharedKey();
		String s = packetIn.getServerId();
		PublicKey publickey = packetIn.getPublicKey();
		String s1 = (new BigInteger(CryptManager.getServerIdHash(s, publickey, secretkey))).toString(16);

		if (mc.getCurrentServerData() != null && mc.getCurrentServerData().isOnLAN()) {
			try {
				getSessionService().joinServer(mc.getSession().getProfile(), mc.getSession().getToken(), s1);
			} catch (AuthenticationException var10) {
				LOGGER.warn("Couldn't connect to auth servers but will continue to join LAN");
			}
		} else {
			try {
				getSessionService().joinServer(mc.getSession().getProfile(), mc.getSession().getToken(), s1);
			} catch (AuthenticationUnavailableException var7) {
				networkManager.closeChannel(new TextComponentTranslation("disconnect.loginFailedInfo", new TextComponentTranslation("disconnect.loginFailedInfo.serversUnavailable")));
				return;
			} catch (InvalidCredentialsException var8) {
				networkManager.closeChannel(new TextComponentTranslation("disconnect.loginFailedInfo", new TextComponentTranslation("disconnect.loginFailedInfo.invalidSession")));
				return;
			} catch (AuthenticationException authenticationexception) {
				networkManager.closeChannel(new TextComponentTranslation("disconnect.loginFailedInfo", authenticationexception.getMessage()));
				return;
			}
		}

		networkManager.sendPacket(new CPacketEncryptionResponse(secretkey, publickey, packetIn.getVerifyToken()), p_operationComplete_1_ -> networkManager.enableEncryption(secretkey));
	}

	private MinecraftSessionService getSessionService() {

		return mc.getSessionService();
	}

	public void handleLoginSuccess(SPacketLoginSuccess packetIn) {

		gameProfile = packetIn.getProfile();
		networkManager.setConnectionState(ConnectionState.PLAY);
		networkManager.setNetHandler(new NetHandlerPlayClient(mc, previousGuiScreen, networkManager, gameProfile));
	}

	/**
	 * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
	 */
	public void onDisconnect(ITextComponent reason) {
		if (previousGuiScreen == null) mc.displayGuiScreen(new GuiDisconnected(null, "connect.failed", reason));
	}

	public void handleDisconnect(SPacketDisconnect packetIn) {

		networkManager.closeChannel(packetIn.getReason());
	}

	public void handleEnableCompression(SPacketEnableCompression packetIn) {

		if (!networkManager.isLocalChannel()) {
			networkManager.setCompressionThreshold(packetIn.getCompressionThreshold());
		}
	}

}
