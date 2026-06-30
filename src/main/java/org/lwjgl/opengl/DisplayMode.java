package org.lwjgl.opengl;

public record DisplayMode(int width, int height, int bitsPerPixel, int frequency) {
	
	public DisplayMode(int width, int height) {
		this(width, height, 32, 60);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DisplayMode(int width1, int height1, int perPixel, int frequency1))) return false;
		return width == width1 && height == height1 && bitsPerPixel == perPixel && frequency == frequency1;
	}
	
	@Override
	public int hashCode() {
		return width ^ height ^ bitsPerPixel ^ frequency;
	}
	
}
