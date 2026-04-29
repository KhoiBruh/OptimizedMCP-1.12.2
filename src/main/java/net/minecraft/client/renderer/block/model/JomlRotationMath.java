package net.minecraft.client.renderer.block.model;

import org.joml.Matrix4f;

final class JomlRotationMath {

	private JomlRotationMath() {

	}

	static Matrix4f modelRotation(int xDegrees, int yDegrees) {

		return new Matrix4f().identity().rotateY((float) Math.toRadians(-yDegrees)).rotateX((float) Math.toRadians(-xDegrees));
	}

}
