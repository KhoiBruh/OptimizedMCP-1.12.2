package net.minecraft.client.gui.advancements;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;

enum AdvancementTabType {
	ABOVE(0, 0, 28, 32, 8),
	BELOW(84, 0, 28, 32, 8),
	LEFT(0, 64, 32, 28, 5),
	RIGHT(96, 64, 32, 28, 5);

	public static final int MAX_TABS;

	static {
		int i = 0;

		for (AdvancementTabType advancementtabtype : values()) {
			i += advancementtabtype.max;
		}

		MAX_TABS = i;
	}

	private final int textureX;
	private final int textureY;
	private final int width;
	private final int height;
	private final int max;

	AdvancementTabType(int p_i47386_3_, int p_i47386_4_, int widthIn, int heightIn, int p_i47386_7_) {

		textureX = p_i47386_3_;
		textureY = p_i47386_4_;
		width = widthIn;
		height = heightIn;
		max = p_i47386_7_;
	}

	public int getMax() {

		return max;
	}

	public void draw(Gui guiIn, int x, int y, boolean p_192651_4_, int p_192651_5_) {

		int i = textureX;

		if (p_192651_5_ > 0) {
			i += width;
		}

		if (p_192651_5_ == max - 1) {
			i += width;
		}

		int j = p_192651_4_ ? textureY + height : textureY;
		guiIn.drawTexturedModalRect(x + getX(p_192651_5_), y + getY(p_192651_5_), i, j, width, height);
	}

	public void drawIcon(int p_192652_1_, int p_192652_2_, int p_192652_3_, RenderItem renderItemIn, ItemStack stack) {

		int i = p_192652_1_ + getX(p_192652_3_);
		int j = p_192652_2_ + getY(p_192652_3_);

		switch (this) {
			case ABOVE:
				i += 6;
				j += 9;
				break;

			case BELOW:
				i += 6;
				j += 6;
				break;

			case LEFT:
				i += 10;
				j += 5;
				break;

			case RIGHT:
				i += 6;
				j += 5;
		}

		renderItemIn.renderItemAndEffectIntoGUI(null, stack, i, j);
	}

	public int getX(int p_192648_1_) {

		switch (this) {
			case ABOVE:
				return (width + 4) * p_192648_1_;

			case BELOW:
				return (width + 4) * p_192648_1_;

			case LEFT:
				return -width + 4;

			case RIGHT:
				return 248;

			default:
				throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
		}
	}

	public int getY(int p_192653_1_) {

		switch (this) {
			case ABOVE:
				return -height + 4;

			case BELOW:
				return 136;

			case LEFT:
				return height * p_192653_1_;

			case RIGHT:
				return height * p_192653_1_;

			default:
				throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
		}
	}

	public boolean isMouseOver(int p_192654_1_, int p_192654_2_, int p_192654_3_, int p_192654_4_, int p_192654_5_) {

		int i = p_192654_1_ + getX(p_192654_3_);
		int j = p_192654_2_ + getY(p_192654_3_);
		return p_192654_4_ > i && p_192654_4_ < i + width && p_192654_5_ > j && p_192654_5_ < j + height;
	}
}
