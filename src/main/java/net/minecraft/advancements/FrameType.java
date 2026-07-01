package net.minecraft.advancements;

import net.minecraft.util.text.TextFormat;

public enum FrameType {
	TASK("task", 0, TextFormat.GREEN),
	CHALLENGE("challenge", 26, TextFormat.DARK_PURPLE),
	GOAL("goal", 52, TextFormat.GREEN);

	private final String name;
	private final int icon;
	private final TextFormat format;

	FrameType(String nameIn, int iconIn, TextFormat formatIn) {

		name = nameIn;
		icon = iconIn;
		format = formatIn;
	}

	public static FrameType byName(String nameIn) {

		for (FrameType frametype : values()) {
			if (frametype.name.equals(nameIn)) {
				return frametype;
			}
		}

		throw new IllegalArgumentException("Unknown frame type '" + nameIn + "'");
	}

	public String getName() {

		return name;
	}

	public int getIcon() {

		return icon;
	}

	public TextFormat getFormat() {

		return format;
	}
}
