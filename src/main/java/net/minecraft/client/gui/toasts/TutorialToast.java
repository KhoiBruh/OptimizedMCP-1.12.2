package net.minecraft.client.gui.toasts;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class TutorialToast implements IToast {

	private final TutorialToast.Icons icon;
	private final String title;
	private final String subtitle;
	private final boolean hasProgressBar;
	private IToast.Visibility visibility = IToast.Visibility.SHOW;
	private long lastDelta;
	private float displayedProgress;
	private float currentProgress;

	public TutorialToast(TutorialToast.Icons iconIn, ITextComponent titleComponent, ITextComponent subtitleComponent, boolean drawProgressBar) {

		icon = iconIn;
		title = titleComponent.getFormattedText();
		subtitle = subtitleComponent == null ? null : subtitleComponent.getFormattedText();
		hasProgressBar = drawProgressBar;
	}

	public IToast.Visibility draw(GuiToast toastGui, long delta) {

		toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
		GlStateManager.color(1.0F, 1.0F, 1.0F);
		toastGui.drawTexturedModalRect(0, 0, 0, 96, 160, 32);
		icon.draw(toastGui, 6, 6);

		if (subtitle == null) {
			toastGui.getMinecraft().fontRenderer.drawString(title, 30, 12, -11534256);
		} else {
			toastGui.getMinecraft().fontRenderer.drawString(title, 30, 7, -11534256);
			toastGui.getMinecraft().fontRenderer.drawString(subtitle, 30, 18, -16777216);
		}

		if (hasProgressBar) {
			Gui.drawRect(3, 28, 157, 29, -1);
			float f = (float) MathHelper.clampedLerp(displayedProgress, currentProgress, (float) (delta - lastDelta) / 100.0F);
			int i;

			if (currentProgress >= displayedProgress) {
				i = -16755456;
			} else {
				i = -11206656;
			}

			Gui.drawRect(3, 28, (int) (3.0F + 154.0F * f), 29, i);
			displayedProgress = f;
			lastDelta = delta;
		}

		return visibility;
	}

	public void hide() {

		visibility = IToast.Visibility.HIDE;
	}

	public void setProgress(float progress) {

		currentProgress = progress;
	}

	public enum Icons {
		MOVEMENT_KEYS(0, 0),
		MOUSE(1, 0),
		TREE(2, 0),
		RECIPE_BOOK(0, 1),
		WOODEN_PLANKS(1, 1);

		private final int column;
		private final int row;

		Icons(int columnIn, int rowIn) {

			column = columnIn;
			row = rowIn;
		}

		public void draw(Gui guiIn, int x, int y) {

			GlStateManager.enableBlend();
			guiIn.drawTexturedModalRect(x, y, 176 + column * 20, row * 20, 20, 20);
			GlStateManager.enableBlend();
		}
	}

}
