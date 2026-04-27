package net.minecraft.client.gui;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerListEntryNormal implements GuiListExtended.IGuiListEntry {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final ThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).build());
	private static final ResourceLocation UNKNOWN_SERVER = new ResourceLocation("textures/misc/unknown_server.png");
	private static final ResourceLocation SERVER_SELECTION_BUTTONS = new ResourceLocation("textures/gui/server_selection.png");
	private final GuiMultiplayer owner;
	private final Minecraft mc;
	private final ServerData server;
	private final ResourceLocation serverIcon;
	private String lastIconB64;
	private DynamicTexture icon;
	private long lastClickTime;

	protected ServerListEntryNormal(GuiMultiplayer ownerIn, ServerData serverIn) {

		owner = ownerIn;
		server = serverIn;
		mc = Minecraft.getMinecraft();
		serverIcon = new ResourceLocation("servers/" + serverIn.serverIP + "/icon");
		icon = (DynamicTexture) mc.getTextureManager().getTexture(serverIcon);
	}

	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {

		if (!server.pinged) {
			server.pinged = true;
			server.pingToServer = -2L;
			server.serverMOTD = "";
			server.populationInfo = "";
			EXECUTOR.submit(new Runnable() {
				public void run() {

					try {
						owner.getOldServerPinger().ping(server);
					} catch (UnknownHostException var2) {
						server.pingToServer = -1L;
						server.serverMOTD = TextFormatting.DARK_RED + I18n.format("multiplayer.status.cannot_resolve");
					} catch (Exception var3) {
						server.pingToServer = -1L;
						server.serverMOTD = TextFormatting.DARK_RED + I18n.format("multiplayer.status.cannot_connect");
					}
				}
			});
		}

		boolean flag = server.version > 340;
		boolean flag1 = server.version < 340;
		boolean flag2 = flag || flag1;
		mc.fontRenderer.drawString(server.serverName, x + 32 + 3, y + 1, 16777215);
		List<String> list = mc.fontRenderer.listFormattedStringToWidth(server.serverMOTD, listWidth - 32 - 2);

		for (int i = 0; i < Math.min(list.size(), 2); ++i) {
			mc.fontRenderer.drawString(list.get(i), x + 32 + 3, y + 12 + mc.fontRenderer.FONT_HEIGHT * i, 8421504);
		}

		String s2 = flag2 ? TextFormatting.DARK_RED + server.gameVersion : server.populationInfo;
		int j = mc.fontRenderer.getStringWidth(s2);
		mc.fontRenderer.drawString(s2, x + listWidth - j - 15 - 2, y + 1, 8421504);
		int k = 0;
		String s = null;
		int l;
		String s1;

		if (flag2) {
			l = 5;
			s1 = I18n.format(flag ? "multiplayer.status.client_out_of_date" : "multiplayer.status.server_out_of_date");
			s = server.playerList;
		} else if (server.pinged && server.pingToServer != -2L) {
			if (server.pingToServer < 0L) {
				l = 5;
			} else if (server.pingToServer < 150L) {
				l = 0;
			} else if (server.pingToServer < 300L) {
				l = 1;
			} else if (server.pingToServer < 600L) {
				l = 2;
			} else if (server.pingToServer < 1000L) {
				l = 3;
			} else {
				l = 4;
			}

			if (server.pingToServer < 0L) {
				s1 = I18n.format("multiplayer.status.no_connection");
			} else {
				s1 = server.pingToServer + "ms";
				s = server.playerList;
			}
		} else {
			k = 1;
			l = (int) (Minecraft.getSystemTime() / 100L + (slotIndex * 2L) & 7L);

			if (l > 4) {
				l = 8 - l;
			}

			s1 = I18n.format("multiplayer.status.pinging");
		}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(Gui.ICONS);
		Gui.drawModalRectWithCustomSizedTexture(x + listWidth - 15, y, (float) (k * 10), (float) (176 + l * 8), 10, 8, 256.0F, 256.0F);

		if (server.getBase64EncodedIconData() != null && !server.getBase64EncodedIconData().equals(lastIconB64)) {
			lastIconB64 = server.getBase64EncodedIconData();
			prepareServerIcon();
			owner.getServerList().saveServerList();
		}

		if (icon != null) {
			drawTextureAt(x, y, serverIcon);
		} else {
			drawTextureAt(x, y, UNKNOWN_SERVER);
		}

		int i1 = mouseX - x;
		int j1 = mouseY - y;

		if (i1 >= listWidth - 15 && i1 <= listWidth - 5 && j1 >= 0 && j1 <= 8) {
			owner.setHoveringText(s1);
		} else if (i1 >= listWidth - j - 15 - 2 && i1 <= listWidth - 15 - 2 && j1 >= 0 && j1 <= 8) {
			owner.setHoveringText(s);
		}

		if (mc.gameSettings.touchscreen || isSelected) {
			mc.getTextureManager().bindTexture(SERVER_SELECTION_BUTTONS);
			Gui.drawRect(x, y, x + 32, y + 32, -1601138544);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			int k1 = mouseX - x;
			int l1 = mouseY - y;

			if (canJoin()) {
				if (k1 < 32 && k1 > 16) {
					Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 32.0F, 32, 32, 256.0F, 256.0F);
				} else {
					Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 256.0F, 256.0F);
				}
			}

			if (owner.canMoveUp(this, slotIndex)) {
				if (k1 < 16 && l1 < 16) {
					Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 32.0F, 32, 32, 256.0F, 256.0F);
				} else {
					Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 0.0F, 32, 32, 256.0F, 256.0F);
				}
			}

			if (owner.canMoveDown(this, slotIndex)) {
				if (k1 < 16 && l1 > 16) {
					Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 32.0F, 32, 32, 256.0F, 256.0F);
				} else {
					Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 0.0F, 32, 32, 256.0F, 256.0F);
				}
			}
		}
	}

	protected void drawTextureAt(int p_178012_1_, int p_178012_2_, ResourceLocation p_178012_3_) {

		mc.getTextureManager().bindTexture(p_178012_3_);
		GlStateManager.enableBlend();
		Gui.drawModalRectWithCustomSizedTexture(p_178012_1_, p_178012_2_, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
		GlStateManager.disableBlend();
	}

	private boolean canJoin() {

		return true;
	}

	private void prepareServerIcon() {

		if (server.getBase64EncodedIconData() == null) {
			mc.getTextureManager().deleteTexture(serverIcon);
			icon = null;
		} else {
			ByteBuf bytebuf = Unpooled.copiedBuffer(server.getBase64EncodedIconData(), StandardCharsets.UTF_8);
			ByteBuf bytebuf1 = null;
			BufferedImage bufferedimage;
			label99:
			{
				try {
					bytebuf1 = Base64.decode(bytebuf);
					bufferedimage = TextureUtil.readBufferedImage(new ByteBufInputStream(bytebuf1));
					Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
					Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
					break label99;
				} catch (Throwable throwable) {
					LOGGER.error("Invalid icon for server {} ({})", server.serverName, server.serverIP, throwable);
					server.setBase64EncodedIconData(null);
				} finally {
					bytebuf.release();

					if (bytebuf1 != null) {
						bytebuf1.release();
					}
				}

				return;
			}

			if (icon == null) {
				icon = new DynamicTexture(bufferedimage.getWidth(), bufferedimage.getHeight());
				mc.getTextureManager().loadTexture(serverIcon, icon);
			}

			bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), icon.getTextureData(), 0, bufferedimage.getWidth());
			icon.updateDynamicTexture();
		}
	}

	/**
	 * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
	 * clicked and the list should not be dragged.
	 */
	public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {

		if (relativeX <= 32) {
			if (relativeX < 32 && relativeX > 16 && canJoin()) {
				owner.selectServer(slotIndex);
				owner.connectToSelected();
				return true;
			}

			if (relativeX < 16 && relativeY < 16 && owner.canMoveUp(this, slotIndex)) {
				owner.moveServerUp(this, slotIndex, GuiScreen.isShiftKeyDown());
				return true;
			}

			if (relativeX < 16 && relativeY > 16 && owner.canMoveDown(this, slotIndex)) {
				owner.moveServerDown(this, slotIndex, GuiScreen.isShiftKeyDown());
				return true;
			}
		}

		owner.selectServer(slotIndex);

		if (Minecraft.getSystemTime() - lastClickTime < 250L) {
			owner.connectToSelected();
		}

		lastClickTime = Minecraft.getSystemTime();
		return false;
	}

	public void updatePosition(int slotIndex, int x, int y, float partialTicks) {

	}

	/**
	 * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
	 */
	public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {

	}

	public ServerData getServerData() {

		return server;
	}

}
