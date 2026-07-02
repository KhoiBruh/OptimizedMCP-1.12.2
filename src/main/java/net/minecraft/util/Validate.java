package net.minecraft.util;

public class Validate {

	public static <T> T notNull(T object) {
		if (object == null) {
			throw new NullPointerException();
		}
		return object;
	}

	public static <T> T notNull(T object, String message) {
		if (object == null) {
			throw new NullPointerException(message);
		}
		return object;
	}

	public static void notEmpty(String string, String message) {
		if (string == null || string.isEmpty()) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void isTrue(boolean expression) {
		if (!expression) {
			throw new IllegalArgumentException();
		}
	}

	public static void isTrue(boolean expression, String message) {
		if (!expression) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void validState(boolean expression, String message) {
		if (!expression) {
			throw new IllegalStateException(message);
		}
	}

	public static void inclusiveBetween(long start, long end, long value) {
		if (value < start || value > end) {
			throw new IllegalArgumentException(String.format("%d not between %d and %d", value, start, end));
		}
	}

	public static void inclusiveBetween(long start, long end, long value, String message) {
		if (value < start || value > end) {
			throw new IllegalArgumentException(message);
		}
	}

}
