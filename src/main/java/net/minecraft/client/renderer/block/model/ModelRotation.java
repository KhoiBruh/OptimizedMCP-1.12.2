package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Maps;
import net.minecraft.util.Facing;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

import java.util.Map;

public enum ModelRotation {
	X0_Y0(0, 0),
	X0_Y90(0, 90),
	X0_Y180(0, 180),
	X0_Y270(0, 270),
	X90_Y0(90, 0),
	X90_Y90(90, 90),
	X90_Y180(90, 180),
	X90_Y270(90, 270),
	X180_Y0(180, 0),
	X180_Y90(180, 90),
	X180_Y180(180, 180),
	X180_Y270(180, 270),
	X270_Y0(270, 0),
	X270_Y90(270, 90),
	X270_Y180(270, 180),
	X270_Y270(270, 270);

	private static final Map<Integer, ModelRotation> MAP_ROTATIONS = Maps.newHashMap();

	static {
		for (ModelRotation modelrotation : values()) {
			MAP_ROTATIONS.put(modelrotation.combinedXY, modelrotation);
		}
	}

	private final int combinedXY;
	private final Matrix4f matrix4d;
	private final int quartersX;
	private final int quartersY;

	ModelRotation(int x, int y) {

		combinedXY = combineXY(x, y);
		matrix4d = JomlRotationMath.modelRotation(x, y);
		quartersX = MathHelper.abs(x / 90);
		quartersY = MathHelper.abs(y / 90);
	}

	private static int combineXY(int p_177521_0_, int p_177521_1_) {

		return p_177521_0_ * 360 + p_177521_1_;
	}

	public static ModelRotation getModelRotation(int x, int y) {

		return MAP_ROTATIONS.get(combineXY(MathHelper.normalizeAngle(x, 360), MathHelper.normalizeAngle(y, 360)));
	}

	public Matrix4f getMatrix4d() {

		return matrix4d;
	}

	public Facing rotateFace(Facing facing) {

		Facing enumfacing = facing;

		for (int i = 0; i < quartersX; ++i) {
			enumfacing = enumfacing.rotateAround(Facing.Axis.X);
		}

		if (enumfacing.getAxis() != Facing.Axis.Y) {
			for (int j = 0; j < quartersY; ++j) {
				enumfacing = enumfacing.rotateAround(Facing.Axis.Y);
			}
		}

		return enumfacing;
	}

	public int rotateVertex(Facing facing, int vertexIndex) {

		int i = vertexIndex;

		if (facing.getAxis() == Facing.Axis.X) {
			i = (vertexIndex + quartersX) % 4;
		}

		Facing enumfacing = facing;

		for (int j = 0; j < quartersX; ++j) {
			enumfacing = enumfacing.rotateAround(Facing.Axis.X);
		}

		if (enumfacing.getAxis() == Facing.Axis.Y) {
			i = (i + quartersY) % 4;
		}

		return i;
	}
}
