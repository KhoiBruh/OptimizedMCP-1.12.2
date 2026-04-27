package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiButtonToggle extends GuiButton {

	protected ResourceLocation resourceLocation;
	protected boolean stateTriggered;
	protected int xTexStart;
	protected int yTexStart;
	protected int xDiffTex;
	protected int yDiffTex;

	public GuiButtonToggle(int buttonId, int xIn, int yIn, int widthIn, int heightIn, boolean buttonText) {

		super(buttonId, xIn, yIn, widthIn, heightIn, "");
		stateTriggered = buttonText;
	}

	public void initTextureValues(int xTexStartIn, int yTexStartIn, int xDiffTexIn, int yDiffTexIn, ResourceLocation resourceLocationIn) {

		xTexStart = xTexStartIn;
		yTexStart = yTexStartIn;
		xDiffTex = xDiffTexIn;
		yDiffTex = yDiffTexIn;
		resourceLocation = resourceLocationIn;
	}

	public boolean isStateTriggered() {

		return stateTriggered;
	}

	public void setStateTriggered(boolean p_191753_1_) {

		stateTriggered = p_191753_1_;
	}

	public void setPosition(int p_191752_1_, int p_191752_2_) {

		x = p_191752_1_;
		y = p_191752_2_;
	}

	/**
	 * Draws this button to the screen.
	 */
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {

		if (visible) {
			hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			mc.getTextureManager().bindTexture(resourceLocation);
			GlStateManager.disableDepth();
			int i = xTexStart;
			int j = yTexStart;

			if (stateTriggered) {
				i += xDiffTex;
			}

			if (hovered) {
				j += yDiffTex;
			}

			drawTexturedModalRect(x, y, i, j, width, height);
			GlStateManager.enableDepth();
		}
	}

}
