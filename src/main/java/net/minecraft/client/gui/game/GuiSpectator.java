package net.minecraft.client.gui.game;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.spectator.ISpectatorMenuObject;
import net.minecraft.client.gui.spectator.ISpectatorMenuRecipient;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.categories.SpectatorDetails;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class GuiSpectator extends Gui implements ISpectatorMenuRecipient {

	public static final ResourceLocation SPECTATOR_WIDGETS = new ResourceLocation("textures/gui/spectator_widgets.png");
	private static final ResourceLocation WIDGETS = new ResourceLocation("textures/gui/widgets.png");
	private final Minecraft mc;
	private long lastSelectionTime;
	private SpectatorMenu menu;

	public GuiSpectator(Minecraft mcIn) {

		mc = mcIn;
	}

	public void onHotbarSelected(int p_175260_1_) {

		lastSelectionTime = Minecraft.getSystemTime();

		if (menu != null) {
			menu.selectSlot(p_175260_1_);
		} else {
			menu = new SpectatorMenu(this);
		}
	}

	private float getHotbarAlpha() {

		long i = lastSelectionTime - Minecraft.getSystemTime() + 5000L;
		return MathHelper.clamp((float) i / 2000.0F, 0.0F, 1.0F);
	}

	public void renderTooltip(ScaledResolution p_175264_1_, float p_175264_2_) {

		if (menu != null) {
			float f = getHotbarAlpha();

			if (f <= 0.0F) {
				menu.exit();
			} else {
				int i = p_175264_1_.getScaledWidth() / 2;
				float f1 = zLevel;
				zLevel = -90.0F;
				float f2 = (float) p_175264_1_.getScaledHeight() - 22.0F * f;
				SpectatorDetails spectatordetails = menu.getCurrentPage();
				renderPage(p_175264_1_, f, i, f2, spectatordetails);
				zLevel = f1;
			}
		}
	}

	protected void renderPage(ScaledResolution p_175258_1_, float p_175258_2_, int p_175258_3_, float p_175258_4_, SpectatorDetails p_175258_5_) {

		GlStateManager.enableRescaleNormal();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color(1.0F, 1.0F, 1.0F, p_175258_2_);
		mc.getTextureManager().bindTexture(WIDGETS);
		drawTexturedModalRect((float) (p_175258_3_ - 91), p_175258_4_, 0, 0, 182, 22);

		if (p_175258_5_.getSelectedSlot() >= 0) {
			drawTexturedModalRect((float) (p_175258_3_ - 91 - 1 + p_175258_5_.getSelectedSlot() * 20), p_175258_4_ - 1.0F, 0, 22, 24, 22);
		}

		RenderHelper.enableGUIStandardItemLighting();

		for (int i = 0; i < 9; ++i) {
			renderSlot(i, p_175258_1_.getScaledWidth() / 2 - 90 + i * 20 + 2, p_175258_4_ + 3.0F, p_175258_2_, p_175258_5_.getObject(i));
		}

		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
	}

	private void renderSlot(int p_175266_1_, int p_175266_2_, float p_175266_3_, float p_175266_4_, ISpectatorMenuObject p_175266_5_) {

		mc.getTextureManager().bindTexture(SPECTATOR_WIDGETS);

		if (p_175266_5_ != SpectatorMenu.EMPTY_SLOT) {
			int i = (int) (p_175266_4_ * 255.0F);
			GlStateManager.pushMatrix();
			GlStateManager.translate((float) p_175266_2_, p_175266_3_, 0.0F);
			float f = p_175266_5_.isEnabled() ? 1.0F : 0.25F;
			GlStateManager.color(f, f, f, p_175266_4_);
			p_175266_5_.renderIcon(f, i);
			GlStateManager.popMatrix();
			String s = String.valueOf(GameSettings.getKeyDisplayString(mc.gameSettings.keyBindsHotbar[p_175266_1_].getKeyCode()));

			if (i > 3 && p_175266_5_.isEnabled()) {
				mc.fontRenderer.drawStringWithShadow(s, (float) (p_175266_2_ + 19 - 2 - mc.fontRenderer.getStringWidth(s)), p_175266_3_ + 6.0F + 3.0F, 16777215 + (i << 24));
			}
		}
	}

	public void renderSelectedItem(ScaledResolution p_175263_1_) {

		int i = (int) (getHotbarAlpha() * 255.0F);

		if (i > 3 && menu != null) {
			ISpectatorMenuObject ispectatormenuobject = menu.getSelectedItem();
			String s = ispectatormenuobject == SpectatorMenu.EMPTY_SLOT ? menu.getSelectedCategory().getPrompt().getFormattedText() : ispectatormenuobject.getSpectatorName().getFormattedText();

			if (s != null) {
				int j = (p_175263_1_.getScaledWidth() - mc.fontRenderer.getStringWidth(s)) / 2;
				int k = p_175263_1_.getScaledHeight() - 35;
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				mc.fontRenderer.drawStringWithShadow(s, (float) j, (float) k, 16777215 + (i << 24));
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
		}
	}

	public void onSpectatorMenuClosed(SpectatorMenu menu) {

		this.menu = null;
		lastSelectionTime = 0L;
	}

	public boolean isMenuActive() {

		return menu != null;
	}

	public void onMouseScroll(int p_175259_1_) {

		int i;

		for (i = menu.getSelectedSlot() + p_175259_1_; i >= 0 && i <= 8 && (menu.getItem(i) == SpectatorMenu.EMPTY_SLOT || !menu.getItem(i).isEnabled()); i += p_175259_1_) {
		}

		if (i >= 0 && i <= 8) {
			menu.selectSlot(i);
			lastSelectionTime = Minecraft.getSystemTime();
		}
	}

	public void onMiddleClick() {

		lastSelectionTime = Minecraft.getSystemTime();

		if (isMenuActive()) {
			int i = menu.getSelectedSlot();

			if (i != -1) {
				menu.selectSlot(i);
			}
		} else {
			menu = new SpectatorMenu(this);
		}
	}

}
