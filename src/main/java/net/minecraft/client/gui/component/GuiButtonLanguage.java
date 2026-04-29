package net.minecraft.client.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class GuiButtonLanguage extends GuiButton {

	public GuiButtonLanguage(int buttonID, int xPos, int yPos) {

		super(buttonID, xPos, yPos, 20, 20, "");
	}

	/**
	 * Draws this button to the screen.
	 */
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {

		if (visible) {
			mc.getTextureManager().bindTexture(GuiButton.BUTTON_TEXTURES);
			GlStateManager.color(1F, 1F, 1F, 1F);
			boolean flag = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			int i = 106;

			if (flag) {
				i += height;
			}

			drawTexturedModalRect(x, y, 0, i, width, height);
		}
	}

}
