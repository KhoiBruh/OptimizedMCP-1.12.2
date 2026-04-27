package net.minecraft.client.resources.data;

public record AnimationFrame(int frameIndex, int frameTime) {

	public AnimationFrame(int frameIndexIn) {

		this(frameIndexIn, -1);
	}

	public boolean hasNoTime() {

		return frameTime == -1;
	}

}
