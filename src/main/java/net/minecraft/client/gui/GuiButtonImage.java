package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiButtonImage extends GuiButton {

	private final ResourceLocation resourceLocation;
	private final int xTexStart;
	private final int yTexStart;
	private final int yDiffText;

	public GuiButtonImage(int p_i47392_1_, int p_i47392_2_, int p_i47392_3_, int p_i47392_4_, int p_i47392_5_, int p_i47392_6_, int p_i47392_7_, int p_i47392_8_, ResourceLocation p_i47392_9_) {

		super(p_i47392_1_, p_i47392_2_, p_i47392_3_, p_i47392_4_, p_i47392_5_, "");
		xTexStart = p_i47392_6_;
		yTexStart = p_i47392_7_;
		yDiffText = p_i47392_8_;
		resourceLocation = p_i47392_9_;
	}

	public void setPosition(int p_191746_1_, int p_191746_2_) {

		x = p_191746_1_;
		y = p_191746_2_;
	}

	/**
	 * Draws this button to the screen.
	 */
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {

		if (visible) {
			hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			mc.getTextureManager().bindTexture(resourceLocation);
			GlStateManager.disableDepth();
			int j = yTexStart;

			if (hovered) {
				j += yDiffText;
			}

			drawTexturedModalRect(x, y, xTexStart, j, width, height);
			GlStateManager.enableDepth();
		}
	}

}
