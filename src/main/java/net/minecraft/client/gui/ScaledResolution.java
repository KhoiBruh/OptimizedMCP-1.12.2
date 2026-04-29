package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;

public class ScaledResolution {

	private final double scaledWidthD;
	private final double scaledHeightD;
	private int scaledWidth;
	private int scaledHeight;
	private int scaleFactor;

	public ScaledResolution(Minecraft mc) {

		scaledWidth = mc.displayWidth;
		scaledHeight = mc.displayHeight;
		scaleFactor = 1;
		boolean flag = mc.isUnicode();
		int i = mc.gameSettings.guiScale;

		if (i == 0) {
			i = 1000;
		}

		while (scaleFactor < i && scaledWidth / (scaleFactor + 1) >= 320 && scaledHeight / (scaleFactor + 1) >= 240) {
			++scaleFactor;
		}

		if (flag && scaleFactor % 2 != 0 && scaleFactor != 1) {
			--scaleFactor;
		}

		scaledWidthD = (double) scaledWidth / (double) scaleFactor;
		scaledHeightD = (double) scaledHeight / (double) scaleFactor;
		scaledWidth = MathHelper.ceil(scaledWidthD);
		scaledHeight = MathHelper.ceil(scaledHeightD);
	}

	public int getScaledWidth() {

		return scaledWidth;
	}

	public int getScaledHeight() {

		return scaledHeight;
	}

	public double getScaledWidth_double() {

		return scaledWidthD;
	}

	public double getScaledHeight_double() {

		return scaledHeightD;
	}

	public int getScaleFactor() {

		return scaleFactor;
	}

}
