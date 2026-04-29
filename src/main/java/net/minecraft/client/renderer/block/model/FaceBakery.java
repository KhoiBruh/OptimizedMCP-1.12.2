package net.minecraft.client.renderer.block.model;

import net.minecraft.client.renderer.FaceDirection;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Facing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class FaceBakery {

	private static final float SCALE_ROTATION_22_5 = 1F / (float) Math.cos(0.39269909262657166D) - 1F;
	private static final float SCALE_ROTATION_GENERAL = 1F / (float) Math.cos((Math.PI / 4D)) - 1F;
	private static final FaceBakery.Rotation[] UV_ROTATIONS = new FaceBakery.Rotation[ModelRotation.values().length * Facing.values().length];
	private static final FaceBakery.Rotation UV_ROTATION_0 = new FaceBakery.Rotation() {
		BlockFaceUV makeRotatedUV(float p_188007_1_, float p_188007_2_, float p_188007_3_, float p_188007_4_) {

			return new BlockFaceUV(new float[]{p_188007_1_, p_188007_2_, p_188007_3_, p_188007_4_}, 0);
		}
	};
	private static final FaceBakery.Rotation UV_ROTATION_270 = new FaceBakery.Rotation() {
		BlockFaceUV makeRotatedUV(float p_188007_1_, float p_188007_2_, float p_188007_3_, float p_188007_4_) {

			return new BlockFaceUV(new float[]{p_188007_4_, 16F - p_188007_1_, p_188007_2_, 16F - p_188007_3_}, 270);
		}
	};
	private static final FaceBakery.Rotation UV_ROTATION_INVERSE = new FaceBakery.Rotation() {
		BlockFaceUV makeRotatedUV(float p_188007_1_, float p_188007_2_, float p_188007_3_, float p_188007_4_) {

			return new BlockFaceUV(new float[]{16F - p_188007_1_, 16F - p_188007_2_, 16F - p_188007_3_, 16F - p_188007_4_}, 0);
		}
	};
	private static final FaceBakery.Rotation UV_ROTATION_90 = new FaceBakery.Rotation() {
		BlockFaceUV makeRotatedUV(float p_188007_1_, float p_188007_2_, float p_188007_3_, float p_188007_4_) {

			return new BlockFaceUV(new float[]{16F - p_188007_2_, p_188007_3_, 16F - p_188007_4_, p_188007_1_}, 90);
		}
	};

	static {
		addUvRotation(ModelRotation.X0_Y0, Facing.DOWN, UV_ROTATION_0);
		addUvRotation(ModelRotation.X0_Y0, Facing.EAST, UV_ROTATION_0);
		addUvRotation(ModelRotation.X0_Y0, Facing.NORTH, UV_ROTATION_0);
		addUvRotation(ModelRotation.X0_Y0, Facing.SOUTH, UV_ROTATION_0);
		addUvRotation(ModelRotation.X0_Y0, Facing.UP, UV_ROTATION_0);
		addUvRotation(ModelRotation.X0_Y0, Facing.WEST, UV_ROTATION_0);
		addUvRotation(ModelRotation.X0_Y90, Facing.EAST, UV_ROTATION_0);
		addUvRotation(ModelRotation.X0_Y90, Facing.NORTH, UV_ROTATION_0);
		addUvRotation(ModelRotation.X0_Y90, Facing.SOUTH, UV_ROTATION_0);
		addUvRotation(ModelRotation.X0_Y90, Facing.WEST, UV_ROTATION_0);
		addUvRotation(ModelRotation.X0_Y180, Facing.EAST, UV_ROTATION_0);
		addUvRotation(ModelRotation.X0_Y180, Facing.NORTH, UV_ROTATION_0);
		addUvRotation(ModelRotation.X0_Y180, Facing.SOUTH, UV_ROTATION_0);
		addUvRotation(ModelRotation.X0_Y180, Facing.WEST, UV_ROTATION_0);
		addUvRotation(ModelRotation.X0_Y270, Facing.EAST, UV_ROTATION_0);
		addUvRotation(ModelRotation.X0_Y270, Facing.NORTH, UV_ROTATION_0);
		addUvRotation(ModelRotation.X0_Y270, Facing.SOUTH, UV_ROTATION_0);
		addUvRotation(ModelRotation.X0_Y270, Facing.WEST, UV_ROTATION_0);
		addUvRotation(ModelRotation.X90_Y0, Facing.DOWN, UV_ROTATION_0);
		addUvRotation(ModelRotation.X90_Y0, Facing.SOUTH, UV_ROTATION_0);
		addUvRotation(ModelRotation.X90_Y90, Facing.DOWN, UV_ROTATION_0);
		addUvRotation(ModelRotation.X90_Y180, Facing.DOWN, UV_ROTATION_0);
		addUvRotation(ModelRotation.X90_Y180, Facing.NORTH, UV_ROTATION_0);
		addUvRotation(ModelRotation.X90_Y270, Facing.DOWN, UV_ROTATION_0);
		addUvRotation(ModelRotation.X180_Y0, Facing.DOWN, UV_ROTATION_0);
		addUvRotation(ModelRotation.X180_Y0, Facing.UP, UV_ROTATION_0);
		addUvRotation(ModelRotation.X270_Y0, Facing.SOUTH, UV_ROTATION_0);
		addUvRotation(ModelRotation.X270_Y0, Facing.UP, UV_ROTATION_0);
		addUvRotation(ModelRotation.X270_Y90, Facing.UP, UV_ROTATION_0);
		addUvRotation(ModelRotation.X270_Y180, Facing.NORTH, UV_ROTATION_0);
		addUvRotation(ModelRotation.X270_Y180, Facing.UP, UV_ROTATION_0);
		addUvRotation(ModelRotation.X270_Y270, Facing.UP, UV_ROTATION_0);
		addUvRotation(ModelRotation.X0_Y270, Facing.UP, UV_ROTATION_270);
		addUvRotation(ModelRotation.X0_Y90, Facing.DOWN, UV_ROTATION_270);
		addUvRotation(ModelRotation.X90_Y0, Facing.WEST, UV_ROTATION_270);
		addUvRotation(ModelRotation.X90_Y90, Facing.WEST, UV_ROTATION_270);
		addUvRotation(ModelRotation.X90_Y180, Facing.WEST, UV_ROTATION_270);
		addUvRotation(ModelRotation.X90_Y270, Facing.NORTH, UV_ROTATION_270);
		addUvRotation(ModelRotation.X90_Y270, Facing.SOUTH, UV_ROTATION_270);
		addUvRotation(ModelRotation.X90_Y270, Facing.WEST, UV_ROTATION_270);
		addUvRotation(ModelRotation.X180_Y90, Facing.UP, UV_ROTATION_270);
		addUvRotation(ModelRotation.X180_Y270, Facing.DOWN, UV_ROTATION_270);
		addUvRotation(ModelRotation.X270_Y0, Facing.EAST, UV_ROTATION_270);
		addUvRotation(ModelRotation.X270_Y90, Facing.EAST, UV_ROTATION_270);
		addUvRotation(ModelRotation.X270_Y90, Facing.NORTH, UV_ROTATION_270);
		addUvRotation(ModelRotation.X270_Y90, Facing.SOUTH, UV_ROTATION_270);
		addUvRotation(ModelRotation.X270_Y180, Facing.EAST, UV_ROTATION_270);
		addUvRotation(ModelRotation.X270_Y270, Facing.EAST, UV_ROTATION_270);
		addUvRotation(ModelRotation.X0_Y180, Facing.DOWN, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X0_Y180, Facing.UP, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X90_Y0, Facing.NORTH, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X90_Y0, Facing.UP, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X90_Y90, Facing.UP, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X90_Y180, Facing.SOUTH, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X90_Y180, Facing.UP, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X90_Y270, Facing.UP, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X180_Y0, Facing.EAST, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X180_Y0, Facing.NORTH, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X180_Y0, Facing.SOUTH, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X180_Y0, Facing.WEST, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X180_Y90, Facing.EAST, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X180_Y90, Facing.NORTH, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X180_Y90, Facing.SOUTH, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X180_Y90, Facing.WEST, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X180_Y180, Facing.DOWN, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X180_Y180, Facing.EAST, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X180_Y180, Facing.NORTH, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X180_Y180, Facing.SOUTH, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X180_Y180, Facing.UP, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X180_Y180, Facing.WEST, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X180_Y270, Facing.EAST, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X180_Y270, Facing.NORTH, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X180_Y270, Facing.SOUTH, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X180_Y270, Facing.WEST, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X270_Y0, Facing.DOWN, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X270_Y0, Facing.NORTH, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X270_Y90, Facing.DOWN, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X270_Y180, Facing.DOWN, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X270_Y180, Facing.SOUTH, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X270_Y270, Facing.DOWN, UV_ROTATION_INVERSE);
		addUvRotation(ModelRotation.X0_Y90, Facing.UP, UV_ROTATION_90);
		addUvRotation(ModelRotation.X0_Y270, Facing.DOWN, UV_ROTATION_90);
		addUvRotation(ModelRotation.X90_Y0, Facing.EAST, UV_ROTATION_90);
		addUvRotation(ModelRotation.X90_Y90, Facing.EAST, UV_ROTATION_90);
		addUvRotation(ModelRotation.X90_Y90, Facing.NORTH, UV_ROTATION_90);
		addUvRotation(ModelRotation.X90_Y90, Facing.SOUTH, UV_ROTATION_90);
		addUvRotation(ModelRotation.X90_Y180, Facing.EAST, UV_ROTATION_90);
		addUvRotation(ModelRotation.X90_Y270, Facing.EAST, UV_ROTATION_90);
		addUvRotation(ModelRotation.X270_Y0, Facing.WEST, UV_ROTATION_90);
		addUvRotation(ModelRotation.X180_Y90, Facing.DOWN, UV_ROTATION_90);
		addUvRotation(ModelRotation.X180_Y270, Facing.UP, UV_ROTATION_90);
		addUvRotation(ModelRotation.X270_Y90, Facing.WEST, UV_ROTATION_90);
		addUvRotation(ModelRotation.X270_Y180, Facing.WEST, UV_ROTATION_90);
		addUvRotation(ModelRotation.X270_Y270, Facing.NORTH, UV_ROTATION_90);
		addUvRotation(ModelRotation.X270_Y270, Facing.SOUTH, UV_ROTATION_90);
		addUvRotation(ModelRotation.X270_Y270, Facing.WEST, UV_ROTATION_90);
	}

	public static Facing getFacingFromVertexData(int[] faceData) {

		Vector3f vector3f = new Vector3f(Float.intBitsToFloat(faceData[0]), Float.intBitsToFloat(faceData[1]), Float.intBitsToFloat(faceData[2]));
		Vector3f vector3f1 = new Vector3f(Float.intBitsToFloat(faceData[7]), Float.intBitsToFloat(faceData[8]), Float.intBitsToFloat(faceData[9]));
		Vector3f vector3f2 = new Vector3f(Float.intBitsToFloat(faceData[14]), Float.intBitsToFloat(faceData[15]), Float.intBitsToFloat(faceData[16]));
		Vector3f vector3f3 = new Vector3f(vector3f.x - vector3f1.x, vector3f.y - vector3f1.y, vector3f.z - vector3f1.z);
		Vector3f vector3f4 = new Vector3f(vector3f2.x - vector3f1.x, vector3f2.y - vector3f1.y, vector3f2.z - vector3f1.z);
		Vector3f vector3f5 = new Vector3f(
				vector3f4.y * vector3f3.z - vector3f4.z * vector3f3.y,
				vector3f4.z * vector3f3.x - vector3f4.x * vector3f3.z,
				vector3f4.x * vector3f3.y - vector3f4.y * vector3f3.x
		);
		float dist = vector3f5.lengthSquared();
		vector3f5.div(dist);
		Facing currentFacing = null;
		float f1 = 0F;

		for (Facing facing : Facing.values()) {
			Vec3i vec3i = facing.getDirectionVec();
			Vector3f vector3f6 = new Vector3f(vec3i.getX(), vec3i.getY(), vec3i.getZ());
			float f2 = vector3f5.x * vector3f6.x + vector3f5.y * vector3f6.y + vector3f5.z * vector3f6.z;

			if (f2 >= 0F && f2 > f1) {
				f1 = f2;
				currentFacing = facing;
			}
		}

		if (currentFacing == null) {
			return Facing.UP;
		} else {
			return currentFacing;
		}
	}

	private static void addUvRotation(ModelRotation p_188013_0_, Facing p_188013_1_, FaceBakery.Rotation p_188013_2_) {

		UV_ROTATIONS[getIndex(p_188013_0_, p_188013_1_)] = p_188013_2_;
	}

	private static int getIndex(ModelRotation p_188014_0_, Facing p_188014_1_) {

		return ModelRotation.values().length * p_188014_1_.ordinal() + p_188014_0_.ordinal();
	}

	public BakedQuad makeBakedQuad(Vector3f posFrom, Vector3f posTo, BlockPartFace face, TextureAtlasSprite sprite, Facing facing, ModelRotation modelRotationIn, BlockPartRotation partRotation, boolean uvLocked, boolean shade) {

		BlockFaceUV blockfaceuv = face.blockFaceUV();

		if (uvLocked) {
			blockfaceuv = applyUVLock(face.blockFaceUV(), facing, modelRotationIn);
		}

		int[] aint = makeQuadVertexData(blockfaceuv, sprite, facing, getPositionsDiv16(posFrom, posTo), modelRotationIn, partRotation, shade);
		Facing enumfacing = getFacingFromVertexData(aint);

		if (partRotation == null) {
			applyFacing(aint, enumfacing);
		}

		return new BakedQuad(aint, face.tintIndex(), enumfacing, sprite);
	}

	private BlockFaceUV applyUVLock(BlockFaceUV p_188010_1_, Facing p_188010_2_, ModelRotation p_188010_3_) {

		return UV_ROTATIONS[getIndex(p_188010_3_, p_188010_2_)].rotateUV(p_188010_1_);
	}

	private int[] makeQuadVertexData(BlockFaceUV uvs, TextureAtlasSprite sprite, Facing orientation, float[] p_188012_4_, ModelRotation rotationIn, BlockPartRotation partRotation, boolean shade) {

		int[] aint = new int[28];

		for (int i = 0; i < 4; ++i) {
			fillVertexData(aint, i, orientation, uvs, p_188012_4_, sprite, rotationIn, partRotation, shade);
		}

		return aint;
	}

	private int getFaceShadeColor(Facing facing) {

		float f = getFaceBrightness(facing);
		int i = MathHelper.clamp((int) (f * 255F), 0, 255);
		return -16777216 | i << 16 | i << 8 | i;
	}

	private float getFaceBrightness(Facing facing) {

		return switch (facing) {
			case DOWN -> 0.5F;
			case NORTH, SOUTH -> 0.8F;
			case WEST, EAST -> 0.6F;
			default -> 1F;
		};
	}

	private float[] getPositionsDiv16(Vector3f pos1, Vector3f pos2) {

		float[] afloat = new float[Facing.values().length];
		afloat[FaceDirection.Constants.WEST_INDEX] = pos1.x / 16F;
		afloat[FaceDirection.Constants.DOWN_INDEX] = pos1.y / 16F;
		afloat[FaceDirection.Constants.NORTH_INDEX] = pos1.z / 16F;
		afloat[FaceDirection.Constants.EAST_INDEX] = pos2.x / 16F;
		afloat[FaceDirection.Constants.UP_INDEX] = pos2.y / 16F;
		afloat[FaceDirection.Constants.SOUTH_INDEX] = pos2.z / 16F;
		return afloat;
	}

	private void fillVertexData(int[] p_188015_1_, int p_188015_2_, Facing p_188015_3_, BlockFaceUV p_188015_4_, float[] p_188015_5_, TextureAtlasSprite p_188015_6_, ModelRotation p_188015_7_, BlockPartRotation p_188015_8_, boolean p_188015_9_) {

		Facing enumfacing = p_188015_7_.rotateFace(p_188015_3_);
		int i = p_188015_9_ ? getFaceShadeColor(enumfacing) : -1;
		FaceDirection.VertexInformation enumfacedirection$vertexinformation = FaceDirection.getFacing(p_188015_3_).getVertexInformation(p_188015_2_);
		Vector3f vector3f = new Vector3f(p_188015_5_[enumfacedirection$vertexinformation.xIndex], p_188015_5_[enumfacedirection$vertexinformation.yIndex], p_188015_5_[enumfacedirection$vertexinformation.zIndex]);
		rotatePart(vector3f, p_188015_8_);
		int j = rotateVertex(vector3f, p_188015_3_, p_188015_2_, p_188015_7_);
		storeVertexData(p_188015_1_, j, p_188015_2_, vector3f, i, p_188015_6_, p_188015_4_);
	}

	private void storeVertexData(int[] faceData, int storeIndex, int vertexIndex, Vector3f position, int shadeColor, TextureAtlasSprite sprite, BlockFaceUV faceUV) {

		int i = storeIndex * 7;
		faceData[i] = Float.floatToRawIntBits(position.x);
		faceData[i + 1] = Float.floatToRawIntBits(position.y);
		faceData[i + 2] = Float.floatToRawIntBits(position.z);
		faceData[i + 3] = shadeColor;
		faceData[i + 4] = Float.floatToRawIntBits(sprite.getInterpolatedU(faceUV.getVertexU(vertexIndex)));
		faceData[i + 4 + 1] = Float.floatToRawIntBits(sprite.getInterpolatedV(faceUV.getVertexV(vertexIndex)));
	}

	private void rotatePart(Vector3f p_178407_1_, BlockPartRotation partRotation) {

		if (partRotation != null) {
			Matrix4f matrix4f = getMatrixIdentity();
			Vector3f vector3f = new Vector3f();

			switch (partRotation.axis()) {
				case X:
					matrix4f.rotateX((float) Math.toRadians(partRotation.angle()));
					vector3f.x = 0F;
					vector3f.y = 1F;
					vector3f.z = 1F;
					break;

				case Y:
					matrix4f.rotateY((float) Math.toRadians(partRotation.angle()));
					vector3f.x = 1F;
					vector3f.y = 0F;
					vector3f.z = 1F;
					break;

				case Z:
					matrix4f.rotateZ((float) Math.toRadians(partRotation.angle()));
					vector3f.x = 1F;
					vector3f.y = 1F;
					vector3f.z = 0F;
			}

			if (partRotation.rescale()) {
				if (Math.abs(partRotation.angle()) == 22.5F) {
					vector3f.mul(SCALE_ROTATION_22_5);
				} else {
					vector3f.mul(SCALE_ROTATION_GENERAL);
				}

				vector3f.x += 1F;
				vector3f.y += 1F;
				vector3f.z += 1F;
			} else {
				vector3f.x = 1F;
				vector3f.y = 1F;
				vector3f.z = 1F;
			}

			rotateScale(p_178407_1_, new Vector3f(partRotation.origin().x, partRotation.origin().y, partRotation.origin().z), matrix4f, vector3f);
		}
	}

	public int rotateVertex(Vector3f p_188011_1_, Facing p_188011_2_, int p_188011_3_, ModelRotation p_188011_4_) {

		if (p_188011_4_ == ModelRotation.X0_Y0) {
			return p_188011_3_;
		} else {
			rotateScale(p_188011_1_, new Vector3f(0.5F, 0.5F, 0.5F), p_188011_4_.getMatrix4d(), new Vector3f(1F, 1F, 1F));
			return p_188011_4_.rotateVertex(p_188011_2_, p_188011_3_);
		}
	}

	private void rotateScale(Vector3f position, Vector3f rotationOrigin, Matrix4f rotationMatrix, Vector3f scale) {

		Vector3f vector3f = new Vector3f(position.x - rotationOrigin.x, position.y - rotationOrigin.y, position.z - rotationOrigin.z);
		rotationMatrix.transformPosition(vector3f, vector3f);
		vector3f.mul(scale);
		position.x = vector3f.x + rotationOrigin.x;
		position.y = vector3f.y + rotationOrigin.y;
		position.z = vector3f.z + rotationOrigin.z;
	}

	private Matrix4f getMatrixIdentity() {

		return new Matrix4f().identity();
	}

	private void applyFacing(int[] p_178408_1_, Facing p_178408_2_) {

		int[] aint = new int[p_178408_1_.length];
		System.arraycopy(p_178408_1_, 0, aint, 0, p_178408_1_.length);
		float[] afloat = new float[Facing.values().length];
		afloat[FaceDirection.Constants.WEST_INDEX] = 999F;
		afloat[FaceDirection.Constants.DOWN_INDEX] = 999F;
		afloat[FaceDirection.Constants.NORTH_INDEX] = 999F;
		afloat[FaceDirection.Constants.EAST_INDEX] = -999F;
		afloat[FaceDirection.Constants.UP_INDEX] = -999F;
		afloat[FaceDirection.Constants.SOUTH_INDEX] = -999F;

		for (int i = 0; i < 4; ++i) {
			int j = 7 * i;
			float f = Float.intBitsToFloat(aint[j]);
			float f1 = Float.intBitsToFloat(aint[j + 1]);
			float f2 = Float.intBitsToFloat(aint[j + 2]);

			if (f < afloat[FaceDirection.Constants.WEST_INDEX]) {
				afloat[FaceDirection.Constants.WEST_INDEX] = f;
			}

			if (f1 < afloat[FaceDirection.Constants.DOWN_INDEX]) {
				afloat[FaceDirection.Constants.DOWN_INDEX] = f1;
			}

			if (f2 < afloat[FaceDirection.Constants.NORTH_INDEX]) {
				afloat[FaceDirection.Constants.NORTH_INDEX] = f2;
			}

			if (f > afloat[FaceDirection.Constants.EAST_INDEX]) {
				afloat[FaceDirection.Constants.EAST_INDEX] = f;
			}

			if (f1 > afloat[FaceDirection.Constants.UP_INDEX]) {
				afloat[FaceDirection.Constants.UP_INDEX] = f1;
			}

			if (f2 > afloat[FaceDirection.Constants.SOUTH_INDEX]) {
				afloat[FaceDirection.Constants.SOUTH_INDEX] = f2;
			}
		}

		FaceDirection enumfacedirection = FaceDirection.getFacing(p_178408_2_);

		for (int i1 = 0; i1 < 4; ++i1) {
			int j1 = 7 * i1;
			FaceDirection.VertexInformation enumfacedirection$vertexinformation = enumfacedirection.getVertexInformation(i1);
			float f8 = afloat[enumfacedirection$vertexinformation.xIndex];
			float f3 = afloat[enumfacedirection$vertexinformation.yIndex];
			float f4 = afloat[enumfacedirection$vertexinformation.zIndex];
			p_178408_1_[j1] = Float.floatToRawIntBits(f8);
			p_178408_1_[j1 + 1] = Float.floatToRawIntBits(f3);
			p_178408_1_[j1 + 2] = Float.floatToRawIntBits(f4);

			for (int k = 0; k < 4; ++k) {
				int l = 7 * k;
				float f5 = Float.intBitsToFloat(aint[l]);
				float f6 = Float.intBitsToFloat(aint[l + 1]);
				float f7 = Float.intBitsToFloat(aint[l + 2]);

				if (MathHelper.epsilonEquals(f8, f5) && MathHelper.epsilonEquals(f3, f6) && MathHelper.epsilonEquals(f4, f7)) {
					p_178408_1_[j1 + 4] = aint[l + 4];
					p_178408_1_[j1 + 4 + 1] = aint[l + 4 + 1];
				}
			}
		}
	}

	abstract static class Rotation {

		private Rotation() {

		}

		public BlockFaceUV rotateUV(BlockFaceUV p_188006_1_) {

			float f = p_188006_1_.getVertexU(p_188006_1_.getVertexRotatedRev(0));
			float f1 = p_188006_1_.getVertexV(p_188006_1_.getVertexRotatedRev(0));
			float f2 = p_188006_1_.getVertexU(p_188006_1_.getVertexRotatedRev(2));
			float f3 = p_188006_1_.getVertexV(p_188006_1_.getVertexRotatedRev(2));
			return makeRotatedUV(f, f1, f2, f3);
		}

		abstract BlockFaceUV makeRotatedUV(float p_188007_1_, float p_188007_2_, float p_188007_3_, float p_188007_4_);

	}

}
