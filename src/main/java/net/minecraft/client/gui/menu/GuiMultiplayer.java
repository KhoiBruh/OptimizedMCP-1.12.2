package net.minecraft.client.gui.menu;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.component.GuiButton;
import net.minecraft.client.gui.component.GuiListExtended;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.network.LanServerInfo;
import net.minecraft.client.network.ServerPinger;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.glu.Project;

import java.io.IOException;
import java.util.List;

public class GuiMultiplayer extends GuiScreen {

	private static final ResourceLocation[] TITLE_PANORAMA_PATHS = new ResourceLocation[]{
		new ResourceLocation("textures/gui/title/background/panorama_0.png"),
		new ResourceLocation("textures/gui/title/background/panorama_1.png"),
		new ResourceLocation("textures/gui/title/background/panorama_2.png"),
		new ResourceLocation("textures/gui/title/background/panorama_3.png"),
		new ResourceLocation("textures/gui/title/background/panorama_4.png"),
		new ResourceLocation("textures/gui/title/background/panorama_5.png")
	};
	private static final Logger LOGGER = LogManager.getLogger();
	private final ServerPinger oldServerPinger = new ServerPinger();
	private final GuiScreen parentScreen;
	private ServerSelectionList serverListSelector;
	private ServerList savedServerList;
	private GuiButton btnEditServer;
	private GuiButton btnSelectServer;
	private GuiButton btnDeleteServer;
	private boolean deletingServer;
	private boolean addingServer;
	private boolean editingServer;
	private boolean directConnect;

	/**
	 * The text to be displayed when the player's cursor hovers over a server listing.
	 */
	private String hoveringText;
	private ServerData selectedServer;
	private LanServerDetector.LanServerList lanServerList;
	private LanServerDetector.ThreadLanServerFind lanServerDetector;
	private boolean initialized;
	private float panoramaTimer;
	private DynamicTexture viewportTexture;
	private ResourceLocation backgroundTexture;

	public GuiMultiplayer(GuiScreen parentScreen) {

		this.parentScreen = parentScreen;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

		Keyboard.enableRepeatEvents(true);
		buttonList.clear();
		viewportTexture = new DynamicTexture(256, 256);
		backgroundTexture = mc.getTextureManager().getDynamicTextureLocation("multiplayer_background", viewportTexture);

		if (initialized) {
			serverListSelector.setDimensions(width, height, 32, height - 64);
		} else {
			initialized = true;
			savedServerList = new ServerList(mc);
			savedServerList.loadServerList();
			lanServerList = new LanServerDetector.LanServerList();

			try {
				lanServerDetector = new LanServerDetector.ThreadLanServerFind(lanServerList);
				lanServerDetector.start();
			} catch (Exception exception) {
				LOGGER.warn("Unable to start LAN server detection: {}", exception.getMessage());
			}

			serverListSelector = new ServerSelectionList(this, mc, width, height, 32, height - 64, 36);
			serverListSelector.updateOnlineServers(savedServerList);
		}

		createButtons();
	}

	/**
	 * Handles mouse input.
	 */
	public void handleMouseInput() throws IOException {

		super.handleMouseInput();
		serverListSelector.handleMouseInput();
	}

	public void createButtons() {

		btnEditServer = addButton(new GuiButton(7, width / 2 - 154, height - 28, 70, 20, I18n.format("selectServer.edit")));
		btnDeleteServer = addButton(new GuiButton(2, width / 2 - 74, height - 28, 70, 20, I18n.format("selectServer.delete")));
		btnSelectServer = addButton(new GuiButton(1, width / 2 - 154, height - 52, 100, 20, I18n.format("selectServer.select")));
		buttonList.add(new GuiButton(4, width / 2 - 50, height - 52, 100, 20, I18n.format("selectServer.direct")));
		buttonList.add(new GuiButton(3, width / 2 + 4 + 50, height - 52, 100, 20, I18n.format("selectServer.add")));
		buttonList.add(new GuiButton(8, width / 2 + 4, height - 28, 70, 20, I18n.format("selectServer.refresh")));
		buttonList.add(new GuiButton(0, width / 2 + 4 + 76, height - 28, 75, 20, I18n.format("gui.cancel")));
		selectServer(serverListSelector.getSelected());
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {

		super.updateScreen();

		if (lanServerList.getWasUpdated()) {
			List<LanServerInfo> list = lanServerList.getLanServers();
			lanServerList.setWasNotUpdated();
			serverListSelector.updateNetworkServers(list);
		}

		oldServerPinger.pingPendingNetworks();
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void onGuiClosed() {

		Keyboard.enableRepeatEvents(false);

		if (lanServerDetector != null) {
			lanServerDetector.interrupt();
			lanServerDetector = null;
		}

		oldServerPinger.clearPendingNetworks();
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void actionPerformed(GuiButton button) {

		if (button.enabled) {
			GuiListExtended.IGuiListEntry guilistextended$iguilistentry = serverListSelector.getSelected() < 0 ? null : serverListSelector.getListEntry(serverListSelector.getSelected());

			if (button.id == 2 && guilistextended$iguilistentry instanceof ServerListEntryNormal) {
				String s4 = ((ServerListEntryNormal) guilistextended$iguilistentry).getServerData().serverName;

				if (s4 != null) {
					deletingServer = true;
					String s = I18n.format("selectServer.deleteQuestion");
					String s1 = "'" + s4 + "' " + I18n.format("selectServer.deleteWarning");
					String s2 = I18n.format("selectServer.deleteButton");
					String s3 = I18n.format("gui.cancel");
					GuiYesNo guiyesno = new GuiYesNo(this, s, s1, s2, s3, serverListSelector.getSelected());
					mc.displayGuiScreen(guiyesno);
				}
			} else if (button.id == 1) {
				connectToSelected();
			} else if (button.id == 4) {
				directConnect = true;
				selectedServer = new ServerData(I18n.format("selectServer.defaultName"), "", false);
				mc.displayGuiScreen(new GuiScreenServerList(this, selectedServer));
			} else if (button.id == 3) {
				addingServer = true;
				selectedServer = new ServerData(I18n.format("selectServer.defaultName"), "", false);
				mc.displayGuiScreen(new GuiScreenAddServer(this, selectedServer));
			} else if (button.id == 7 && guilistextended$iguilistentry instanceof ServerListEntryNormal) {
				editingServer = true;
				ServerData serverdata = ((ServerListEntryNormal) guilistextended$iguilistentry).getServerData();
				selectedServer = new ServerData(serverdata.serverName, serverdata.serverIP, false);
				selectedServer.copyFrom(serverdata);
				mc.displayGuiScreen(new GuiScreenAddServer(this, selectedServer));
			} else if (button.id == 0) {
				mc.displayGuiScreen(parentScreen);
			} else if (button.id == 8) {
				refreshServerList();
			}
		}
	}

	private void refreshServerList() {

		mc.displayGuiScreen(new GuiMultiplayer(parentScreen));
	}

	public void confirmClicked(boolean result, int id) {

		GuiListExtended.IGuiListEntry guilistextended$iguilistentry = serverListSelector.getSelected() < 0 ? null : serverListSelector.getListEntry(serverListSelector.getSelected());

		if (deletingServer) {
			deletingServer = false;

			if (result && guilistextended$iguilistentry instanceof ServerListEntryNormal) {
				savedServerList.removeServerData(serverListSelector.getSelected());
				savedServerList.saveServerList();
				serverListSelector.setSelectedSlotIndex(-1);
				serverListSelector.updateOnlineServers(savedServerList);
			}

			mc.displayGuiScreen(this);
		} else if (directConnect) {
			directConnect = false;

			if (result) {
				connectToServer(selectedServer);
			} else {
				mc.displayGuiScreen(this);
			}
		} else if (addingServer) {
			addingServer = false;

			if (result) {
				savedServerList.addServerData(selectedServer);
				savedServerList.saveServerList();
				serverListSelector.setSelectedSlotIndex(-1);
				serverListSelector.updateOnlineServers(savedServerList);
			}

			mc.displayGuiScreen(this);
		} else if (editingServer) {
			editingServer = false;

			if (result && guilistextended$iguilistentry instanceof ServerListEntryNormal) {
				ServerData serverdata = ((ServerListEntryNormal) guilistextended$iguilistentry).getServerData();
				serverdata.serverName = selectedServer.serverName;
				serverdata.serverIP = selectedServer.serverIP;
				serverdata.copyFrom(selectedServer);
				savedServerList.saveServerList();
				serverListSelector.updateOnlineServers(savedServerList);
			}

			mc.displayGuiScreen(this);
		}
	}

	/**
	 * Draws the main menu panorama.
	 */
	private void drawPanorama(int mouseX, int mouseY, float partialTicks) {

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.matrixMode(5889);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		Project.gluPerspective(120F, 1F, 0.05F, 10F);
		GlStateManager.matrixMode(5888);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.rotate(180F, 1F, 0F, 0F);
		GlStateManager.rotate(90F, 0F, 0F, 1F);
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.disableCull();
		GlStateManager.depthMask(false);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

		for (int j = 0; j < 64; ++j) {
			GlStateManager.pushMatrix();
			float f = ((float) (j % 8) / 8F - 0.5F) / 64F;
			float f1 = ((float) (j / 8) / 8F - 0.5F) / 64F;
			GlStateManager.translate(f, f1, 0F);
			GlStateManager.rotate(MathHelper.sin(panoramaTimer / 400F) * 25F + 20F, 1F, 0F, 0F);
			GlStateManager.rotate(-panoramaTimer * 0.1F, 0F, 1F, 0F);

			for (int k = 0; k < 6; ++k) {
				GlStateManager.pushMatrix();

				if (k == 1) {
					GlStateManager.rotate(90F, 0F, 1F, 0F);
				}

				if (k == 2) {
					GlStateManager.rotate(180F, 0F, 1F, 0F);
				}

				if (k == 3) {
					GlStateManager.rotate(-90F, 0F, 1F, 0F);
				}

				if (k == 4) {
					GlStateManager.rotate(90F, 1F, 0F, 0F);
				}

				if (k == 5) {
					GlStateManager.rotate(-90F, 1F, 0F, 0F);
				}

				mc.getTextureManager().bindTexture(TITLE_PANORAMA_PATHS[k]);
				bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				int l = 255 / (j + 1);
				bufferbuilder.pos(-1D, -1D, 1D).tex(0D, 0D).color(255, 255, 255, l).endVertex();
				bufferbuilder.pos(1D, -1D, 1D).tex(1D, 0D).color(255, 255, 255, l).endVertex();
				bufferbuilder.pos(1D, 1D, 1D).tex(1D, 1D).color(255, 255, 255, l).endVertex();
				bufferbuilder.pos(-1D, 1D, 1D).tex(0D, 1D).color(255, 255, 255, l).endVertex();
				tessellator.draw();
				GlStateManager.popMatrix();
			}

			GlStateManager.popMatrix();
			GlStateManager.colorMask(true, true, true, false);
		}

		bufferbuilder.setTranslation(0D, 0D, 0D);
		GlStateManager.colorMask(true, true, true, true);
		GlStateManager.matrixMode(5889);
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(5888);
		GlStateManager.popMatrix();
		GlStateManager.depthMask(true);
		GlStateManager.enableCull();
		GlStateManager.enableDepth();
	}

	/**
	 * Rotate and blur the skybox view.
	 */
	private void rotateAndBlurSkybox() {

		mc.getTextureManager().bindTexture(backgroundTexture);
		GlStateManager.glTexParameteri(3553, 10241, 9729);
		GlStateManager.glTexParameteri(3553, 10240, 9729);
		GlStateManager.glCopyTexSubImage2D(3553, 0, 0, 0, 0, 0, 256, 256);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.colorMask(true, true, true, false);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		GlStateManager.disableAlpha();

		for (int j = 0; j < 3; ++j) {
			float f = 1F / (float) (j + 1);
			int k = width;
			int l = height;
			float f1 = (float) (j - 1) / 256F;
			bufferbuilder.pos(k, l, zLevel).tex(0F + f1, 1D).color(1F, 1F, 1F, f).endVertex();
			bufferbuilder.pos(k, 0D, zLevel).tex(1F + f1, 1D).color(1F, 1F, 1F, f).endVertex();
			bufferbuilder.pos(0D, 0D, zLevel).tex(1F + f1, 0D).color(1F, 1F, 1F, f).endVertex();
			bufferbuilder.pos(0D, l, zLevel).tex(0F + f1, 0D).color(1F, 1F, 1F, f).endVertex();
		}

		tessellator.draw();
		GlStateManager.enableAlpha();
		GlStateManager.colorMask(true, true, true, true);
	}

	/**
	 * Renders the animated skybox background.
	 */
	private void renderSkybox(int mouseX, int mouseY, float partialTicks) {

		mc.getFramebuffer().unbindFramebuffer();
		GlStateManager.viewport(0, 0, 256, 256);
		drawPanorama(mouseX, mouseY, partialTicks);
		rotateAndBlurSkybox();
		rotateAndBlurSkybox();
		rotateAndBlurSkybox();
		rotateAndBlurSkybox();
		rotateAndBlurSkybox();
		rotateAndBlurSkybox();
		rotateAndBlurSkybox();
		mc.getFramebuffer().bindFramebuffer(true);
		GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);
		float f = 120F / (float) (Math.max(width, height));
		float f1 = (float) height * f / 256F;
		float f2 = (float) width * f / 256F;
		int i = width;
		int j = height;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.pos(0D, j, zLevel).tex(0.5F - f1, 0.5F + f2).color(1F, 1F, 1F, 1F).endVertex();
		bufferbuilder.pos(i, j, zLevel).tex(0.5F - f1, 0.5F - f2).color(1F, 1F, 1F, 1F).endVertex();
		bufferbuilder.pos(i, 0D, zLevel).tex(0.5F + f1, 0.5F - f2).color(1F, 1F, 1F, 1F).endVertex();
		bufferbuilder.pos(0D, 0D, zLevel).tex(0.5F + f1, 0.5F + f2).color(1F, 1F, 1F, 1F).endVertex();
		tessellator.draw();
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) throws IOException {

		int i = serverListSelector.getSelected();
		GuiListExtended.IGuiListEntry guilistextended$iguilistentry = i < 0 ? null : serverListSelector.getListEntry(i);

		if (keyCode == 63) {
			refreshServerList();
		} else {
			if (i >= 0) {
				if (keyCode == 200) {
					if (isShiftKeyDown()) {
						if (i > 0 && guilistextended$iguilistentry instanceof ServerListEntryNormal) {
							savedServerList.swapServers(i, i - 1);
							selectServer(serverListSelector.getSelected() - 1);
							serverListSelector.scrollBy(-serverListSelector.getSlotHeight());
							serverListSelector.updateOnlineServers(savedServerList);
						}
					} else if (i > 0) {
						selectServer(serverListSelector.getSelected() - 1);
						serverListSelector.scrollBy(-serverListSelector.getSlotHeight());

						if (serverListSelector.getListEntry(serverListSelector.getSelected()) instanceof ServerListEntryLanScan) {
							if (serverListSelector.getSelected() > 0) {
								selectServer(serverListSelector.getSize() - 1);
								serverListSelector.scrollBy(-serverListSelector.getSlotHeight());
							} else {
								selectServer(-1);
							}
						}
					} else {
						selectServer(-1);
					}
				} else if (keyCode == 208) {
					if (isShiftKeyDown()) {
						if (i < savedServerList.countServers() - 1) {
							savedServerList.swapServers(i, i + 1);
							selectServer(i + 1);
							serverListSelector.scrollBy(serverListSelector.getSlotHeight());
							serverListSelector.updateOnlineServers(savedServerList);
						}
					} else if (i < serverListSelector.getSize()) {
						selectServer(serverListSelector.getSelected() + 1);
						serverListSelector.scrollBy(serverListSelector.getSlotHeight());

						if (serverListSelector.getListEntry(serverListSelector.getSelected()) instanceof ServerListEntryLanScan) {
							if (serverListSelector.getSelected() < serverListSelector.getSize() - 1) {
								selectServer(serverListSelector.getSize() + 1);
								serverListSelector.scrollBy(serverListSelector.getSlotHeight());
							} else {
								selectServer(-1);
							}
						}
					} else {
						selectServer(-1);
					}
				} else if (keyCode != 28 && keyCode != 156) {
					super.keyTyped(typedChar, keyCode);
				} else {
					actionPerformed(buttonList.get(2));
				}
			} else {
				super.keyTyped(typedChar, keyCode);
			}
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		hoveringText = null;
		serverListSelector.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, I18n.format("multiplayer.title"), width / 2, 20, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);

		if (hoveringText != null) {
			drawHoveringText(Lists.newArrayList(Splitter.on("\n").split(hoveringText)), mouseX, mouseY);
		}
	}

	public void connectToSelected() {

		GuiListExtended.IGuiListEntry guilistextended$iguilistentry = serverListSelector.getSelected() < 0 ? null : serverListSelector.getListEntry(serverListSelector.getSelected());

		if (guilistextended$iguilistentry instanceof ServerListEntryNormal) {
			connectToServer(((ServerListEntryNormal) guilistextended$iguilistentry).getServerData());
		} else if (guilistextended$iguilistentry instanceof ServerListEntryLanDetected) {
			LanServerInfo lanserverinfo = ((ServerListEntryLanDetected) guilistextended$iguilistentry).getServerData();
			connectToServer(new ServerData(lanserverinfo.getServerMotd(), lanserverinfo.getServerIpPort(), true));
		}
	}

	private void connectToServer(ServerData server) {

		mc.displayGuiScreen(new GuiConnecting(this, mc, server));
	}

	public void selectServer(int index) {

		serverListSelector.setSelectedSlotIndex(index);
		GuiListExtended.IGuiListEntry guilistextended$iguilistentry = index < 0 ? null : serverListSelector.getListEntry(index);
		btnSelectServer.enabled = false;
		btnEditServer.enabled = false;
		btnDeleteServer.enabled = false;

		if (guilistextended$iguilistentry != null && !(guilistextended$iguilistentry instanceof ServerListEntryLanScan)) {
			btnSelectServer.enabled = true;

			if (guilistextended$iguilistentry instanceof ServerListEntryNormal) {
				btnEditServer.enabled = true;
				btnDeleteServer.enabled = true;
			}
		}
	}

	public ServerPinger getOldServerPinger() {

		return oldServerPinger;
	}

	public void setHoveringText(String p_146793_1_) {

		hoveringText = p_146793_1_;
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

		super.mouseClicked(mouseX, mouseY, mouseButton);
		serverListSelector.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Called when a mouse button is released.
	 */
	protected void mouseReleased(int mouseX, int mouseY, int state) {

		super.mouseReleased(mouseX, mouseY, state);
		serverListSelector.mouseReleased(mouseX, mouseY, state);
	}

	public ServerList getServerList() {

		return savedServerList;
	}

	public boolean canMoveUp(ServerListEntryNormal p_175392_1_, int p_175392_2_) {

		return p_175392_2_ > 0;
	}

	public boolean canMoveDown(ServerListEntryNormal p_175394_1_, int p_175394_2_) {

		return p_175394_2_ < savedServerList.countServers() - 1;
	}

	public void moveServerUp(ServerListEntryNormal p_175391_1_, int p_175391_2_, boolean p_175391_3_) {

		int i = p_175391_3_ ? 0 : p_175391_2_ - 1;
		savedServerList.swapServers(p_175391_2_, i);

		if (serverListSelector.getSelected() == p_175391_2_) {
			selectServer(i);
		}

		serverListSelector.updateOnlineServers(savedServerList);
	}

	public void moveServerDown(ServerListEntryNormal p_175393_1_, int p_175393_2_, boolean p_175393_3_) {

		int i = p_175393_3_ ? savedServerList.countServers() - 1 : p_175393_2_ + 1;
		savedServerList.swapServers(p_175393_2_, i);

		if (serverListSelector.getSelected() == p_175393_2_) {
			selectServer(i);
		}

		serverListSelector.updateOnlineServers(savedServerList);
	}

}
