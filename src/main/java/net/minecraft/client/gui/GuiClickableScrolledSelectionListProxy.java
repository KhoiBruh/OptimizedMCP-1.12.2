package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.realms.RealmsClickableScrolledSelectionList;
import net.minecraft.realms.Tezzelator;
import org.lwjgl.input.Mouse;

public class GuiClickableScrolledSelectionListProxy extends GuiSlot {

	private final RealmsClickableScrolledSelectionList proxy;

	public GuiClickableScrolledSelectionListProxy(RealmsClickableScrolledSelectionList selectionList, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {

		super(Minecraft.getMinecraft(), widthIn, heightIn, topIn, bottomIn, slotHeightIn);
		proxy = selectionList;
	}

	protected int getSize() {

		return proxy.getItemCount();
	}

	/**
	 * The element in the slot that was clicked, boolean for whether it was double clicked or not
	 */
	protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {

		proxy.selectItem(slotIndex, isDoubleClick, mouseX, mouseY);
	}

	/**
	 * Returns true if the element passed in is currently selected
	 */
	protected boolean isSelected(int slotIndex) {

		return proxy.isSelectedItem(slotIndex);
	}

	protected void drawBackground() {

		proxy.renderBackground();
	}

	protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {

		proxy.renderItem(slotIndex, xPos, yPos, heightIn, mouseXIn, mouseYIn);
	}

	public int width() {

		return width;
	}

	public int mouseY() {

		return mouseY;
	}

	public int mouseX() {

		return mouseX;
	}

	/**
	 * Return the height of the content being scrolled
	 */
	protected int getContentHeight() {

		return proxy.getMaxPosition();
	}

	protected int getScrollBarX() {

		return proxy.getScrollbarPosition();
	}

	public void handleMouseInput() {

		super.handleMouseInput();

		if (scrollMultiplier > 0.0F && Mouse.getEventButtonState()) {
			proxy.customMouseEvent(top, bottom, headerPadding, amountScrolled, slotHeight);
		}
	}

	public void renderSelected(int p_178043_1_, int p_178043_2_, int p_178043_3_, Tezzelator p_178043_4_) {

		proxy.renderSelected(p_178043_1_, p_178043_2_, p_178043_3_, p_178043_4_);
	}

	/**
	 * Draws the selection box around the selected slot element.
	 */
	protected void drawSelectionBox(int insideLeft, int insideTop, int mouseXIn, int mouseYIn, float partialTicks) {

		int i = getSize();

		for (int j = 0; j < i; ++j) {
			int k = insideTop + j * slotHeight + headerPadding;
			int l = slotHeight - 4;

			if (k > bottom || k + l < top) {
				updateItemPos(j, insideLeft, k, partialTicks);
			}

			if (showSelectionBox && isSelected(j)) {
				renderSelected(width, k, l, Tezzelator.instance);
			}

			drawSlot(j, insideLeft, k, l, mouseXIn, mouseYIn, partialTicks);
		}
	}

}
