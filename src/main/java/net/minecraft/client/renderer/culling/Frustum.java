package net.minecraft.client.renderer.culling;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.AxisAlignedBB;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;

public class Frustum implements ICamera {

	private static final Frustum instance = new Frustum();
	private final Matrix4f projectionMatrix = new Matrix4f();
	private final Matrix4f modelviewMatrix = new Matrix4f();
	private final Matrix4f clipMatrix = new Matrix4f();
	private final FrustumIntersection frustum = new FrustumIntersection();
	private final FloatBuffer projectionMatrixBuffer = GLAllocation.createDirectFloatBuffer(16);
	private final FloatBuffer modelviewMatrixBuffer = GLAllocation.createDirectFloatBuffer(16);
	private double x;
	private double y;
	private double z;

	public static Frustum getInstance() {

		instance.init();
		return instance;
	}

	public void init() {

		projectionMatrixBuffer.clear();
		modelviewMatrixBuffer.clear();
		GlStateManager.getFloat(2983, projectionMatrixBuffer);
		GlStateManager.getFloat(2982, modelviewMatrixBuffer);
		projectionMatrixBuffer.rewind();
		modelviewMatrixBuffer.rewind();
		projectionMatrix.set(projectionMatrixBuffer);
		modelviewMatrix.set(modelviewMatrixBuffer);
		setClipMatrix(new Matrix4f(projectionMatrix).mul(modelviewMatrix));
	}

	public void setPosition(double xIn, double yIn, double zIn) {

		x = xIn;
		y = yIn;
		z = zIn;
	}

	/**
	 * Calls the clipping helper. Returns true if the box is inside all 6 clipping planes, otherwise returns false.
	 */
	public boolean isBoxInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {

		return frustum.testAab((float) (minX - x), (float) (minY - y), (float) (minZ - z), (float) (maxX - x), (float) (maxY - y), (float) (maxZ - z));
	}

	/**
	 * Returns true if the bounding box is inside all 6 clipping planes, otherwise returns false.
	 */
	public boolean isBoundingBoxInFrustum(AxisAlignedBB bb) {

		return isBoxInFrustum(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
	}

	void setClipMatrix(Matrix4f matrix) {

		clipMatrix.set(matrix);
		frustum.set(clipMatrix);
	}

}
