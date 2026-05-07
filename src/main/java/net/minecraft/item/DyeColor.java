package net.minecraft.item;

import net.minecraft.util.IStringSerializable;

public enum DyeColor implements IStringSerializable {
	WHITE(0, 15, "white", "white", 16383998),
	ORANGE(1, 14, "orange", "orange", 16351261),
	MAGENTA(2, 13, "magenta", "magenta", 13061821),
	LIGHT_BLUE(3, 12, "light_blue", "lightBlue", 3847130),
	YELLOW(4, 11, "yellow", "yellow", 16701501),
	LIME(5, 10, "lime", "lime", 8439583),
	PINK(6, 9, "pink", "pink", 15961002),
	GRAY(7, 8, "gray", "gray", 4673362),
	SILVER(8, 7, "silver", "silver", 10329495),
	CYAN(9, 6, "cyan", "cyan", 1481884),
	PURPLE(10, 5, "purple", "purple", 8991416),
	BLUE(11, 4, "blue", "blue", 3949738),
	BROWN(12, 3, "brown", "brown", 8606770),
	GREEN(13, 2, "green", "green", 6192150),
	RED(14, 1, "red", "red", 11546150),
	BLACK(15, 0, "black", "black", 1908001);

	private static final DyeColor[] META_LOOKUP = new DyeColor[values().length];
	private static final DyeColor[] DYE_DMG_LOOKUP = new DyeColor[values().length];

	static {
		for (DyeColor enumdyecolor : values()) {
			META_LOOKUP[enumdyecolor.getMetadata()] = enumdyecolor;
			DYE_DMG_LOOKUP[enumdyecolor.getDyeDamage()] = enumdyecolor;
		}
	}

	private final int meta;
	private final int dyeDamage;
	private final String name;
	private final String unlocalizedName;
	/**
	 * An int containing the corresponding RGB color for this dye color.
	 */
	private final int colorValue;
	/**
	 * An array containing 3 floats ranging from 0.0 to 1.0: the red, green, and blue components of the corresponding
	 * color.
	 */
	private final float[] colorComponentValues;
	
	DyeColor(int metaIn, int dyeDamageIn, String nameIn, String unlocalizedNameIn, int colorValueIn) {

		meta = metaIn;
		dyeDamage = dyeDamageIn;
		name = nameIn;
		unlocalizedName = unlocalizedNameIn;
		colorValue = colorValueIn;
		int i = (colorValueIn & 16711680) >> 16;
		int j = (colorValueIn & 65280) >> 8;
		int k = (colorValueIn & 255);
		colorComponentValues = new float[]{(float) i / 255F, (float) j / 255F, (float) k / 255F};
	}

	public static DyeColor byDyeDamage(int damage) {

		if (damage < 0 || damage >= DYE_DMG_LOOKUP.length) {
			damage = 0;
		}

		return DYE_DMG_LOOKUP[damage];
	}

	public static DyeColor byMetadata(int meta) {

		if (meta < 0 || meta >= META_LOOKUP.length) {
			meta = 0;
		}

		return META_LOOKUP[meta];
	}

	public int getMetadata() {

		return meta;
	}

	public int getDyeDamage() {

		return dyeDamage;
	}

	public String getDyeColorName() {

		return name;
	}

	public String getUnlocalizedName() {

		return unlocalizedName;
	}

	/**
	 * Gets the RGB color corresponding to this dye color.
	 */
	public int getColorValue() {

		return colorValue;
	}

	/**
	 * Gets an array containing 3 floats ranging from 0.0 to 1.0: the red, green, and blue components of the
	 * corresponding color.
	 */
	public float[] getColorComponentValues() {

		return colorComponentValues;
	}

	public String toString() {

		return unlocalizedName;
	}

	public String getName() {

		return name;
	}
}
