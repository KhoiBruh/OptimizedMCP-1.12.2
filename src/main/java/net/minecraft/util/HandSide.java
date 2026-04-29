package net.minecraft.util;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public enum HandSide {
	LEFT(new TextComponentTranslation("options.mainHand.left")),
	RIGHT(new TextComponentTranslation("options.mainHand.right"));

	private final ITextComponent handName;

	HandSide(ITextComponent nameIn) {

		handName = nameIn;
	}

	public HandSide opposite() {

		return this == LEFT ? RIGHT : LEFT;
	}

	public String toString() {

		return handName.getUnformattedText();
	}
}
