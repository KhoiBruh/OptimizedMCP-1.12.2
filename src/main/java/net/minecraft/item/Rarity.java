package net.minecraft.item;

import net.minecraft.util.text.TextFormat;

public enum Rarity {
	COMMON(TextFormat.WHITE, "Common"),
	UNCOMMON(TextFormat.YELLOW, "Uncommon"),
	RARE(TextFormat.AQUA, "Rare"),
	EPIC(TextFormat.LIGHT_PURPLE, "Epic");

	/**
	 * A decimal representation of the hex color codes of a the color assigned to this rarity type. (13 becomes d as in
	 * \247d which is light purple)
	 */
	public final TextFormat rarityColor;

	/**
	 * Rarity name.
	 */
	public final String rarityName;

	Rarity(TextFormat color, String name) {
		rarityColor = color;
		rarityName = name;
	}
}
