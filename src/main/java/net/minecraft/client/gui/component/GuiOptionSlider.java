package net.minecraft.client.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.math.MathHelper;

public class GuiOptionSlider extends GuiButton {

	private final GameSettings.Options options;
	private final float minValue;
	private final float maxValue;
	public boolean dragging;
	private float sliderValue;

	public GuiOptionSlider(int buttonId, int x, int y, GameSettings.Options optionIn) {

		this(buttonId, x, y, optionIn, 0.0F, 1.0F);
	}

	public GuiOptionSlider(int buttonId, int x, int y, GameSettings.Options optionIn, float minValueIn, float maxValue) {

		super(buttonId, x, y, 150, 20, "");
		sliderValue = 1.0F;
		options = optionIn;
		minValue = minValueIn;
		this.maxValue = maxValue;
		Minecraft minecraft = Minecraft.getMinecraft();
		sliderValue = optionIn.normalizeValue(minecraft.gameSettings.getOptionFloatValue(optionIn));
		displayString = minecraft.gameSettings.getKeyBinding(optionIn);
	}

	/**
	 * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
	 * this button.
	 */
	protected int getHoverState(boolean mouseOver) {

		return 0;
	}

	/**
	 * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
	 */
	protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {

		if (visible) {
			if (dragging) {
				sliderValue = (float) (mouseX - (x + 4)) / (float) (width - 8);
				sliderValue = MathHelper.clamp(sliderValue, 0.0F, 1.0F);
				float f = options.denormalizeValue(sliderValue);
				mc.gameSettings.setOptionFloatValue(options, f);
				sliderValue = options.normalizeValue(f);
				displayString = mc.gameSettings.getKeyBinding(options);
			}

			mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			drawTexturedModalRect(x + (int) (sliderValue * (float) (width - 8)), y, 0, 66, 4, 20);
			drawTexturedModalRect(x + (int) (sliderValue * (float) (width - 8)) + 4, y, 196, 66, 4, 20);
		}
	}

	/**
	 * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
	 * e).
	 */
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {

		if (super.mousePressed(mc, mouseX, mouseY)) {
			sliderValue = (float) (mouseX - (x + 4)) / (float) (width - 8);
			sliderValue = MathHelper.clamp(sliderValue, 0.0F, 1.0F);
			mc.gameSettings.setOptionFloatValue(options, options.denormalizeValue(sliderValue));
			displayString = mc.gameSettings.getKeyBinding(options);
			dragging = true;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
	 */
	public void mouseReleased(int mouseX, int mouseY) {

		dragging = false;
	}

}
