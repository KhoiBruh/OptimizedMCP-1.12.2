package net.minecraft.client.gui.advancements;

import com.google.common.collect.Maps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ClientAdvancementManager;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.play.client.CPacketSeenAdvancements;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import java.io.IOException;
import java.util.Map;

public class GuiScreenAdvancements extends GuiScreen implements ClientAdvancementManager.IListener {

	private static final ResourceLocation WINDOW = new ResourceLocation("textures/gui/advancements/window.png");
	private static final ResourceLocation TABS = new ResourceLocation("textures/gui/advancements/tabs.png");
	private final ClientAdvancementManager clientAdvancementManager;
	private final Map<Advancement, GuiAdvancementTab> tabs = Maps.newLinkedHashMap();
	private GuiAdvancementTab selectedTab;
	private int scrollMouseX;
	private int scrollMouseY;
	private boolean isScrolling;

	public GuiScreenAdvancements(ClientAdvancementManager p_i47383_1_) {

		clientAdvancementManager = p_i47383_1_;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

		tabs.clear();
		selectedTab = null;
		clientAdvancementManager.setListener(this);

		if (selectedTab == null && !tabs.isEmpty()) {
			clientAdvancementManager.setSelectedTab(tabs.values().iterator().next().getAdvancement(), true);
		} else {
			clientAdvancementManager.setSelectedTab(selectedTab == null ? null : selectedTab.getAdvancement(), true);
		}
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void onGuiClosed() {

		clientAdvancementManager.setListener(null);
		NetHandlerPlayClient nethandlerplayclient = mc.getConnection();

		if (nethandlerplayclient != null) {
			nethandlerplayclient.sendPacket(CPacketSeenAdvancements.closedScreen());
		}
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

		if (mouseButton == 0) {
			int i = (width - 252) / 2;
			int j = (height - 140) / 2;

			for (GuiAdvancementTab guiadvancementtab : tabs.values()) {
				if (guiadvancementtab.isMouseOver(i, j, mouseX, mouseY)) {
					clientAdvancementManager.setSelectedTab(guiadvancementtab.getAdvancement(), true);
					break;
				}
			}
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) throws IOException {

		if (keyCode == mc.gameSettings.keyBindAdvancements.getKeyCode()) {
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		int i = (width - 252) / 2;
		int j = (height - 140) / 2;

		if (Mouse.isButtonDown(0)) {
			if (!isScrolling) {
				isScrolling = true;
			} else if (selectedTab != null) {
				selectedTab.scroll(mouseX - scrollMouseX, mouseY - scrollMouseY);
			}

			scrollMouseX = mouseX;
			scrollMouseY = mouseY;
		} else {
			isScrolling = false;
		}

		drawDefaultBackground();
		renderInside(mouseX, mouseY, i, j);
		renderWindow(i, j);
		renderToolTips(mouseX, mouseY, i, j);
	}

	private void renderInside(int p_191936_1_, int p_191936_2_, int p_191936_3_, int p_191936_4_) {

		GuiAdvancementTab guiadvancementtab = selectedTab;

		if (guiadvancementtab == null) {
			drawRect(p_191936_3_ + 9, p_191936_4_ + 18, p_191936_3_ + 9 + 234, p_191936_4_ + 18 + 113, -16777216);
			String s = I18n.format("advancements.empty");
			int i = fontRenderer.getStringWidth(s);
			fontRenderer.drawString(s, p_191936_3_ + 9 + 117 - i / 2, p_191936_4_ + 18 + 56 - fontRenderer.FONT_HEIGHT / 2, -1);
			fontRenderer.drawString(":(", p_191936_3_ + 9 + 117 - fontRenderer.getStringWidth(":(") / 2, p_191936_4_ + 18 + 113 - fontRenderer.FONT_HEIGHT, -1);
		} else {
			GlStateManager.pushMatrix();
			GlStateManager.translate((float) (p_191936_3_ + 9), (float) (p_191936_4_ + 18), -400.0F);
			GlStateManager.enableDepth();
			guiadvancementtab.drawContents();
			GlStateManager.popMatrix();
			GlStateManager.depthFunc(515);
			GlStateManager.disableDepth();
		}
	}

	public void renderWindow(int p_191934_1_, int p_191934_2_) {

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();
		RenderHelper.disableStandardItemLighting();
		mc.getTextureManager().bindTexture(WINDOW);
		drawTexturedModalRect(p_191934_1_, p_191934_2_, 0, 0, 252, 140);

		if (tabs.size() > 1) {
			mc.getTextureManager().bindTexture(TABS);

			for (GuiAdvancementTab guiadvancementtab : tabs.values()) {
				guiadvancementtab.drawTab(p_191934_1_, p_191934_2_, guiadvancementtab == selectedTab);
			}

			GlStateManager.enableRescaleNormal();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			RenderHelper.enableGUIStandardItemLighting();

			for (GuiAdvancementTab guiadvancementtab1 : tabs.values()) {
				guiadvancementtab1.drawIcon(p_191934_1_, p_191934_2_, itemRender);
			}

			GlStateManager.disableBlend();
		}

		fontRenderer.drawString(I18n.format("gui.advancements"), p_191934_1_ + 8, p_191934_2_ + 6, 4210752);
	}

	private void renderToolTips(int p_191937_1_, int p_191937_2_, int p_191937_3_, int p_191937_4_) {

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		if (selectedTab != null) {
			GlStateManager.pushMatrix();
			GlStateManager.enableDepth();
			GlStateManager.translate((float) (p_191937_3_ + 9), (float) (p_191937_4_ + 18), 400.0F);
			selectedTab.drawToolTips(p_191937_1_ - p_191937_3_ - 9, p_191937_2_ - p_191937_4_ - 18, p_191937_3_, p_191937_4_);
			GlStateManager.disableDepth();
			GlStateManager.popMatrix();
		}

		if (tabs.size() > 1) {
			for (GuiAdvancementTab guiadvancementtab : tabs.values()) {
				if (guiadvancementtab.isMouseOver(p_191937_3_, p_191937_4_, p_191937_1_, p_191937_2_)) {
					drawHoveringText(guiadvancementtab.getTitle(), p_191937_1_, p_191937_2_);
				}
			}
		}
	}

	public void rootAdvancementAdded(Advancement advancementIn) {

		GuiAdvancementTab guiadvancementtab = GuiAdvancementTab.create(mc, this, tabs.size(), advancementIn);

		if (guiadvancementtab != null) {
			tabs.put(advancementIn, guiadvancementtab);
		}
	}

	public void rootAdvancementRemoved(Advancement advancementIn) {

	}

	public void nonRootAdvancementAdded(Advancement advancementIn) {

		GuiAdvancementTab guiadvancementtab = getTab(advancementIn);

		if (guiadvancementtab != null) {
			guiadvancementtab.addAdvancement(advancementIn);
		}
	}

	public void nonRootAdvancementRemoved(Advancement advancementIn) {

	}

	public void onUpdateAdvancementProgress(Advancement p_191933_1_, AdvancementProgress p_191933_2_) {

		GuiAdvancement guiadvancement = getAdvancementGui(p_191933_1_);

		if (guiadvancement != null) {
			guiadvancement.getAdvancementProgress(p_191933_2_);
		}
	}

	public void setSelectedTab(Advancement p_193982_1_) {

		selectedTab = tabs.get(p_193982_1_);
	}

	public void advancementsCleared() {

		tabs.clear();
		selectedTab = null;
	}

	
	public GuiAdvancement getAdvancementGui(Advancement p_191938_1_) {

		GuiAdvancementTab guiadvancementtab = getTab(p_191938_1_);
		return guiadvancementtab == null ? null : guiadvancementtab.getAdvancementGui(p_191938_1_);
	}

	
	private GuiAdvancementTab getTab(Advancement p_191935_1_) {

		while (p_191935_1_.getParent() != null) {
			p_191935_1_ = p_191935_1_.getParent();
		}

		return tabs.get(p_191935_1_);
	}

}
