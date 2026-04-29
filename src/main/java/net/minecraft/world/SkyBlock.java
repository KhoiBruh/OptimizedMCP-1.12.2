package net.minecraft.world;

public enum SkyBlock {
	SKY(15),
	BLOCK(0);

	public final int defaultLightValue;

	SkyBlock(int defaultLightValueIn) {

		defaultLightValue = defaultLightValueIn;
	}
}
