package net.minecraft.util;

public class MouseFilter {

	private float targetValue;
	private float remainingValue;
	private float lastAmount;

	/**
	 * Smooths mouse input
	 */
	public float smooth(float value, float factor) {
		targetValue += value;
		value = (targetValue - remainingValue) * factor;
		lastAmount += (value - lastAmount) * 0.5F;

		if (value > 0F && value > lastAmount || value < 0F && value < lastAmount) {
			value = lastAmount;
		}

		remainingValue += value;
		return value;
	}

	public void reset() {
		targetValue = 0F;
		remainingValue = 0F;
		lastAmount = 0F;
	}

}
