package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

public class GuiSlider extends GuiButton {

	private final String name;
	private final float min;
	private final float max;
	private final GuiPageButtonList.GuiResponder responder;
	private final GuiSlider.FormatHelper formatHelper;
	public boolean isMouseDown;
	private float sliderPosition = 1.0F;

	public GuiSlider(GuiPageButtonList.GuiResponder guiResponder, int idIn, int x, int y, String nameIn, float minIn, float maxIn, float defaultValue, GuiSlider.FormatHelper formatter) {

		super(idIn, x, y, 150, 20, "");
		name = nameIn;
		min = minIn;
		max = maxIn;
		sliderPosition = (defaultValue - minIn) / (maxIn - minIn);
		formatHelper = formatter;
		responder = guiResponder;
		displayString = getDisplayString();
	}

	/**
	 * Gets the value of the slider.
	 *
	 * @return A value that will under normal circumstances be between the slider's {@link #min} and {@link #max}
	 * values, unless it was manually set out of that range.
	 */
	public float getSliderValue() {

		return min + (max - min) * sliderPosition;
	}

	/**
	 * Sets the slider's value, optionally notifying the associated {@linkplain GuiPageButtonList.GuiResponder
	 * responder} of the change.
	 */
	public void setSliderValue(float value, boolean notifyResponder) {

		sliderPosition = (value - min) / (max - min);
		displayString = getDisplayString();

		if (notifyResponder) {
			responder.setEntryValue(id, getSliderValue());
		}
	}

	/**
	 * Gets the slider's position.
	 *
	 * @return The position of the slider, which will under normal circumstances be between 0 and 1, unless it was
	 * manually set out of that range.
	 */
	public float getSliderPosition() {

		return sliderPosition;
	}

	/**
	 * Sets the position of the slider and notifies the associated {@linkplain GuiPageButtonList.GuiResponder responder}
	 * of the change
	 */
	public void setSliderPosition(float position) {

		sliderPosition = position;
		displayString = getDisplayString();
		responder.setEntryValue(id, getSliderValue());
	}

	private String getDisplayString() {

		return formatHelper == null ? I18n.format(name) + ": " + getSliderValue() : formatHelper.getText(id, I18n.format(name), getSliderValue());
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
			if (isMouseDown) {
				sliderPosition = (float) (mouseX - (x + 4)) / (float) (width - 8);

				if (sliderPosition < 0.0F) {
					sliderPosition = 0.0F;
				}

				if (sliderPosition > 1.0F) {
					sliderPosition = 1.0F;
				}

				displayString = getDisplayString();
				responder.setEntryValue(id, getSliderValue());
			}

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			drawTexturedModalRect(x + (int) (sliderPosition * (float) (width - 8)), y, 0, 66, 4, 20);
			drawTexturedModalRect(x + (int) (sliderPosition * (float) (width - 8)) + 4, y, 196, 66, 4, 20);
		}
	}

	/**
	 * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
	 * e).
	 */
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {

		if (super.mousePressed(mc, mouseX, mouseY)) {
			sliderPosition = (float) (mouseX - (x + 4)) / (float) (width - 8);

			if (sliderPosition < 0.0F) {
				sliderPosition = 0.0F;
			}

			if (sliderPosition > 1.0F) {
				sliderPosition = 1.0F;
			}

			displayString = getDisplayString();
			responder.setEntryValue(id, getSliderValue());
			isMouseDown = true;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
	 */
	public void mouseReleased(int mouseX, int mouseY) {

		isMouseDown = false;
	}

	public interface FormatHelper {

		String getText(int id, String name, float value);

	}

}
