package net.minecraft.client.renderer;

import org.joml.Quaternionf;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class GlStateManager {

	private static final FloatBuffer BUF_FLOAT_16 = BufferUtils.createFloatBuffer(16);
	private static final FloatBuffer BUF_FLOAT_4 = BufferUtils.createFloatBuffer(4);

	private static final boolean[] light = new boolean[8];

	// Texture State
	private static final boolean[] texture2D = new boolean[8];
	private static final int[] texture = new int[8];

	// Alpha State
	private static boolean alphaTest;
	private static int alphaTestFunc = GL11.GL_ALWAYS;
	private static float alphaTestRef = -1F;

	// Lighting State
	private static boolean lightingEnabled;

	// ColorMaterial State
	private static boolean colorMaterial;
	private static int colorMaterialFace = GL11.GL_FRONT_AND_BACK;
	private static int colorMaterialMode = GL11.GL_AMBIENT_AND_DIFFUSE;

	// Blend State
	private static boolean blend;
	private static int blendSrcFactor = GL11.GL_ONE;
	private static int blendDstFactor = GL11.GL_ZERO;
	private static int blendSrcFactorAlpha = GL11.GL_ONE;
	private static int blendDstFactorAlpha = GL11.GL_ZERO;

	// Depth State
	private static boolean depthTest;
	private static boolean depthMask = true;
	private static int depthFunc = GL11.GL_LESS;

	// Fog State
	private static boolean fog;
	private static int fogMode = GL11.GL_EXP;
	private static float fogDensity = 1F;
	private static float fogStart;
	private static float fogEnd = 1F;

	// Cull State
	private static boolean cullFace;
	private static int cullFaceMode = GL11.GL_BACK;

	// PolygonOffset State
	private static boolean polygonOffsetFill;
	private static float polygonOffsetFactor;
	private static float polygonOffsetUnits;

	// ColorLogic State
	private static boolean colorLogicOp;
	private static int colorLogicOpcode = GL11.GL_COPY;

	// TexGen State
	private static boolean texGenS;
	private static boolean texGenT;
	private static boolean texGenR;
	private static boolean texGenQ;
	private static int texGenSMode = -1;
	private static int texGenTMode = -1;
	private static int texGenRMode = -1;
	private static int texGenQMode = -1;

	// Clear State
	private static double clearDepth = 1D;
	private static float clearRed;
	private static float clearGreen;
	private static float clearBlue;
	private static float clearColorAlpha;

	// Normalize State
	private static boolean normalize;
	private static int activeTextureUnit;
	private static int activeShadeModel = GL11.GL_SMOOTH;
	private static boolean rescaleNormal;

	// ColorMask State
	private static boolean colorMaskRed = true;
	private static boolean colorMaskGreen = true;
	private static boolean colorMaskBlue = true;
	private static boolean colorMaskAlpha = true;

	// Color State
	private static float colorStateRed = -1F;
	private static float colorStateGreen = -1F;
	private static float colorStateBlue = -1F;
	private static float colorStateAlpha = -1F;

	public static void pushAttrib() {
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
	}

	public static void popAttrib() {
		GL11.glPopAttrib();
	}

	public static void disableAlpha() {
		if (alphaTest) {
			alphaTest = false;
			GL11.glDisable(GL11.GL_ALPHA_TEST);
		}
	}

	public static void enableAlpha() {
		if (!alphaTest) {
			alphaTest = true;
			GL11.glEnable(GL11.GL_ALPHA_TEST);
		}
	}

	public static void alphaFunc(int func, float ref) {
		if (func != alphaTestFunc || ref != alphaTestRef) {
			alphaTestFunc = func;
			alphaTestRef = ref;
			GL11.glAlphaFunc(func, ref);
		}
	}

	public static void enableLighting() {
		if (!lightingEnabled) {
			lightingEnabled = true;
			GL11.glEnable(GL11.GL_LIGHTING);
		}
	}

	public static void disableLighting() {
		if (lightingEnabled) {
			lightingEnabled = false;
			GL11.glDisable(GL11.GL_LIGHTING);
		}
	}

	public static void enableLight(int light) {
		if (!GlStateManager.light[light]) {
			GlStateManager.light[light] = true;
			GL11.glEnable(GL11.GL_LIGHT0 + light);
		}
	}

	public static void disableLight(int light) {
		if (GlStateManager.light[light]) {
			GlStateManager.light[light] = false;
			GL11.glDisable(GL11.GL_LIGHT0 + light);
		}
	}

	public static void enableColorMaterial() {
		if (!colorMaterial) {
			colorMaterial = true;
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		}
	}

	public static void disableColorMaterial() {
		if (colorMaterial) {
			colorMaterial = false;
			GL11.glDisable(GL11.GL_COLOR_MATERIAL);
		}
	}

	public static void colorMaterial(int face, int mode) {
		if (face != colorMaterialFace || mode != colorMaterialMode) {
			colorMaterialFace = face;
			colorMaterialMode = mode;
			GL11.glColorMaterial(face, mode);
		}
	}

	public static void glLight(int light, int pname, FloatBuffer params) {
		GL11.glLightfv(light, pname, params);
	}

	public static void glLightModel(int pname, FloatBuffer params) {
		GL11.glLightModelfv(pname, params);
	}

	public static void glNormal3f(float nx, float ny, float nz) {
		GL11.glNormal3f(nx, ny, nz);
	}

	public static void disableDepth() {
		if (depthTest) {
			depthTest = false;
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		}
	}

	public static void enableDepth() {
		if (!depthTest) {
			depthTest = true;
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
	}

	public static void depthFunc(int depthFuncIn) {
		if (depthFuncIn != depthFunc) {
			depthFunc = depthFuncIn;
			GL11.glDepthFunc(depthFuncIn);
		}
	}

	public static void depthMask(boolean flagIn) {
		if (flagIn != depthMask) {
			depthMask = flagIn;
			GL11.glDepthMask(flagIn);
		}
	}

	public static void disableBlend() {
		if (blend) {
			blend = false;
			GL11.glDisable(GL11.GL_BLEND);
		}
	}

	public static void enableBlend() {
		if (!blend) {
			blend = true;
			GL11.glEnable(GL11.GL_BLEND);
		}
	}

	public static void blendFunc(GlStateManager.SourceFactor srcFactor, GlStateManager.DestFactor dstFactor) {
		blendFunc(srcFactor.factor, dstFactor.factor);
	}

	public static void blendFunc(int srcFactor, int dstFactor) {
		if (srcFactor != blendSrcFactor || dstFactor != blendDstFactor) {
			blendSrcFactor = srcFactor;
			blendDstFactor = dstFactor;
			GL11.glBlendFunc(srcFactor, dstFactor);
		}
	}

	public static void tryBlendFuncSeparate(GlStateManager.SourceFactor srcFactor, GlStateManager.DestFactor dstFactor, GlStateManager.SourceFactor srcFactorAlpha, GlStateManager.DestFactor dstFactorAlpha) {
		tryBlendFuncSeparate(srcFactor.factor, dstFactor.factor, srcFactorAlpha.factor, dstFactorAlpha.factor);
	}

	public static void tryBlendFuncSeparate(int srcFactor, int dstFactor, int srcFactorAlpha, int dstFactorAlpha) {
		if (srcFactor != blendSrcFactor || dstFactor != blendDstFactor || srcFactorAlpha != blendSrcFactorAlpha || dstFactorAlpha != blendDstFactorAlpha) {
			blendSrcFactor = srcFactor;
			blendDstFactor = dstFactor;
			blendSrcFactorAlpha = srcFactorAlpha;
			blendDstFactorAlpha = dstFactorAlpha;
			OpenGlHelper.glBlendFunc(srcFactor, dstFactor, srcFactorAlpha, dstFactorAlpha);
		}
	}

	public static void glBlendEquation(int blendEquation) {
		GL14.glBlendEquation(blendEquation);
	}

	public static void enableOutlineMode(int color) {
		BUF_FLOAT_4.put(0, (float) (color >> 16 & 255) / 255F);
		BUF_FLOAT_4.put(1, (float) (color >> 8 & 255) / 255F);
		BUF_FLOAT_4.put(2, (float) (color & 255) / 255F);
		BUF_FLOAT_4.put(3, (float) (color >> 24 & 255) / 255F);
		glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, BUF_FLOAT_4);
		glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL13.GL_COMBINE);
		glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_REPLACE);
		glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL13.GL_CONSTANT);
		glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
		glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_REPLACE);
		glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
		glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
	}

	public static void disableOutlineMode() {
		glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_MODULATE);
		glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
		glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL11.GL_TEXTURE);
		glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE1_RGB, GL13.GL_PREVIOUS);
		glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
		glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
	}

	public static void enableFog() {
		if (!fog) {
			fog = true;
			GL11.glEnable(GL11.GL_FOG);
		}
	}

	public static void disableFog() {
		if (fog) {
			fog = false;
			GL11.glDisable(GL11.GL_FOG);
		}
	}

	public static void setFog(GlStateManager.FogMode p_fogMode) {
		setFog(p_fogMode.capabilityId);
	}

	private static void setFog(int param) {
		if (param != fogMode) {
			fogMode = param;
			GL11.glFogi(GL11.GL_FOG_MODE, param);
		}
	}

	public static void setFogDensity(float param) {
		if (param != fogDensity) {
			fogDensity = param;
			GL11.glFogf(GL11.GL_FOG_DENSITY, param);
		}
	}

	public static void setFogStart(float param) {
		if (param != fogStart) {
			fogStart = param;
			GL11.glFogf(GL11.GL_FOG_START, param);
		}
	}

	public static void setFogEnd(float param) {
		if (param != fogEnd) {
			fogEnd = param;
			GL11.glFogf(GL11.GL_FOG_END, param);
		}
	}

	public static void glFog(int pname, FloatBuffer param) {
		GL11.glFogfv(pname, param);
	}

	public static void glFogi(int pname, int param) {
		GL11.glFogi(pname, param);
	}

	public static void enableCull() {
		if (!cullFace) {
			cullFace = true;
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
	}

	public static void disableCull() {
		if (cullFace) {
			cullFace = false;
			GL11.glDisable(GL11.GL_CULL_FACE);
		}
	}

	public static void cullFace(GlStateManager.CullFace p_cullFace) {
		cullFace(p_cullFace.mode);
	}

	private static void cullFace(int mode) {
		if (mode != cullFaceMode) {
			cullFaceMode = mode;
			GL11.glCullFace(mode);
		}
	}

	public static void enablePolygonOffset() {
		if (!polygonOffsetFill) {
			polygonOffsetFill = true;
			GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
		}
	}

	public static void disablePolygonOffset() {
		if (polygonOffsetFill) {
			polygonOffsetFill = false;
			GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
		}
	}

	public static void doPolygonOffset(float factor, float units) {
		if (factor != polygonOffsetFactor || units != polygonOffsetUnits) {
			polygonOffsetFactor = factor;
			polygonOffsetUnits = units;
			GL11.glPolygonOffset(factor, units);
		}
	}

	public static void enableColorLogic() {
		if (!colorLogicOp) {
			colorLogicOp = true;
			GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
		}
	}

	public static void disableColorLogic() {
		if (colorLogicOp) {
			colorLogicOp = false;
			GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
		}
	}

	public static void colorLogicOp(GlStateManager.LogicOp logicOperation) {
		colorLogicOp(logicOperation.opcode);
	}

	public static void colorLogicOp(int opcode) {
		if (opcode != colorLogicOpcode) {
			colorLogicOpcode = opcode;
			GL11.glLogicOp(opcode);
		}
	}

	public static void enableTexGenCoord(GlStateManager.TexGen texGen) {
		switch (texGen) {
			case S:
				if (!texGenS) {
					texGenS = true;
					GL11.glEnable(GL11.GL_TEXTURE_GEN_S);
				}
				break;
			case T:
				if (!texGenT) {
					texGenT = true;
					GL11.glEnable(GL11.GL_TEXTURE_GEN_T);
				}
				break;
			case R:
				if (!texGenR) {
					texGenR = true;
					GL11.glEnable(GL11.GL_TEXTURE_GEN_R);
				}
				break;
			case Q:
				if (!texGenQ) {
					texGenQ = true;
					GL11.glEnable(GL11.GL_TEXTURE_GEN_Q);
				}
				break;
		}
	}

	public static void disableTexGenCoord(GlStateManager.TexGen texGen) {
		switch (texGen) {
			case S:
				if (texGenS) {
					texGenS = false;
					GL11.glDisable(GL11.GL_TEXTURE_GEN_S);
				}
				break;
			case T:
				if (texGenT) {
					texGenT = false;
					GL11.glDisable(GL11.GL_TEXTURE_GEN_T);
				}
				break;
			case R:
				if (texGenR) {
					texGenR = false;
					GL11.glDisable(GL11.GL_TEXTURE_GEN_R);
				}
				break;
			case Q:
				if (texGenQ) {
					texGenQ = false;
					GL11.glDisable(GL11.GL_TEXTURE_GEN_Q);
				}
				break;
		}
	}

	public static void texGen(GlStateManager.TexGen texGen, int param) {
		switch (texGen) {
			case S:
				if (param != texGenSMode) {
					texGenSMode = param;
					GL11.glTexGeni(GL11.GL_TEXTURE_GEN_S, GL11.GL_TEXTURE_GEN_MODE, param);
				}
				break;
			case T:
				if (param != texGenTMode) {
					texGenTMode = param;
					GL11.glTexGeni(GL11.GL_TEXTURE_GEN_T, GL11.GL_TEXTURE_GEN_MODE, param);
				}
				break;
			case R:
				if (param != texGenRMode) {
					texGenRMode = param;
					GL11.glTexGeni(GL11.GL_TEXTURE_GEN_R, GL11.GL_TEXTURE_GEN_MODE, param);
				}
				break;
			case Q:
				if (param != texGenQMode) {
					texGenQMode = param;
					GL11.glTexGeni(GL11.GL_TEXTURE_GEN_Q, GL11.GL_TEXTURE_GEN_MODE, param);
				}
				break;
		}
	}

	public static void texGen(GlStateManager.TexGen texGen, int pname, FloatBuffer params) {
		int coord = switch (texGen) {
			case S -> GL11.GL_TEXTURE_GEN_S;
			case T -> GL11.GL_TEXTURE_GEN_T;
			case R -> GL11.GL_TEXTURE_GEN_R;
			case Q -> GL11.GL_TEXTURE_GEN_Q;
		};
		GL11.glTexGenfv(coord, pname, params);
	}

	public static void setActiveTexture(int texture) {
		if (activeTextureUnit != texture - OpenGlHelper.defaultTexUnit) {
			activeTextureUnit = texture - OpenGlHelper.defaultTexUnit;
			OpenGlHelper.setActiveTexture(texture);
		}
	}

	public static void enableTexture2D() {
		if (!texture2D[activeTextureUnit]) {
			texture2D[activeTextureUnit] = true;
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
	}

	public static void disableTexture2D() {
		if (texture2D[activeTextureUnit]) {
			texture2D[activeTextureUnit] = false;
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
	}

	public static void glTexEnv(int target, int parameterName, FloatBuffer parameters) {
		GL11.glTexEnvfv(target, parameterName, parameters);
	}

	public static void glTexEnvi(int target, int parameterName, int parameter) {
		GL11.glTexEnvi(target, parameterName, parameter);
	}

	public static void glTexParameterf(int target, int parameterName, float parameter) {
		GL11.glTexParameterf(target, parameterName, parameter);
	}

	public static void glTexParameteri(int target, int parameterName, int parameter) {
		GL11.glTexParameteri(target, parameterName, parameter);
	}

	public static int glGetTexLevelParameteri(int target, int level, int parameterName) {
		return GL11.glGetTexLevelParameteri(target, level, parameterName);
	}

	public static int generateTexture() {
		return GL11.glGenTextures();
	}

	public static void deleteTexture(int texture) {
		GL11.glDeleteTextures(texture);
		for (int i = 0; i < GlStateManager.texture.length; ++i) {
			if (GlStateManager.texture[i] == texture) {
				GlStateManager.texture[i] = -1;
			}
		}
	}

	public static void bindTexture(int texture) {
		if (texture != GlStateManager.texture[activeTextureUnit]) {
			GlStateManager.texture[activeTextureUnit] = texture;
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		}
	}

	public static void glTexImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, IntBuffer pixels) {
		GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
	}

	public static void glTexSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int type, IntBuffer pixels) {
		GL11.glTexSubImage2D(target, level, xOffset, yOffset, width, height, format, type, pixels);
	}

	public static void glCopyTexSubImage2D(int target, int level, int xOffset, int yOffset, int x, int y, int width, int height) {
		GL11.glCopyTexSubImage2D(target, level, xOffset, yOffset, x, y, width, height);
	}

	public static void glGetTexImage(int target, int level, int format, int type, IntBuffer pixels) {
		GL11.glGetTexImage(target, level, format, type, pixels);
	}

	public static void enableNormalize() {
		if (!normalize) {
			normalize = true;
			GL11.glEnable(GL11.GL_NORMALIZE);
		}
	}

	public static void disableNormalize() {
		if (normalize) {
			normalize = false;
			GL11.glDisable(GL11.GL_NORMALIZE);
		}
	}

	public static void shadeModel(int mode) {
		if (mode != activeShadeModel) {
			activeShadeModel = mode;
			GL11.glShadeModel(mode);
		}
	}

	public static void enableRescaleNormal() {
		if (!rescaleNormal) {
			rescaleNormal = true;
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		}
	}

	public static void disableRescaleNormal() {
		if (rescaleNormal) {
			rescaleNormal = false;
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		}
	}

	public static void viewport(int x, int y, int width, int height) {
		GL11.glViewport(x, y, width, height);
	}

	public static void colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
		if (red != colorMaskRed || green != colorMaskGreen || blue != colorMaskBlue || alpha != colorMaskAlpha) {
			colorMaskRed = red;
			colorMaskGreen = green;
			colorMaskBlue = blue;
			colorMaskAlpha = alpha;
			GL11.glColorMask(red, green, blue, alpha);
		}
	}

	public static void clearDepth(double depth) {
		if (depth != clearDepth) {
			clearDepth = depth;
			GL11.glClearDepth(depth);
		}
	}

	public static void clearColor(float red, float green, float blue, float alpha) {
		if (red != clearRed || green != clearGreen || blue != clearBlue || alpha != clearColorAlpha) {
			clearRed = red;
			clearGreen = green;
			clearBlue = blue;
			clearColorAlpha = alpha;
			GL11.glClearColor(red, green, blue, alpha);
		}
	}

	public static void clear(int mask) {
		GL11.glClear(mask);
	}

	public static void matrixMode(int mode) {
		GL11.glMatrixMode(mode);
	}

	public static void loadIdentity() {
		GL11.glLoadIdentity();
	}

	public static void pushMatrix() {
		GL11.glPushMatrix();
	}

	public static void popMatrix() {
		GL11.glPopMatrix();
	}

	public static void getFloat(int pname, FloatBuffer params) {
		params.clear();
		GL11.glGetFloatv(pname, params);
	}

	public static void ortho(double left, double right, double bottom, double top, double zNear, double zFar) {
		GL11.glOrtho(left, right, bottom, top, zNear, zFar);
	}

	public static void rotate(float angle, float x, float y, float z) {
		GL11.glRotatef(angle, x, y, z);
	}

	public static void scale(float x, float y, float z) {
		GL11.glScalef(x, y, z);
	}

	public static void scale(double x, double y, double z) {
		GL11.glScaled(x, y, z);
	}

	public static void translate(float x, float y, float z) {
		GL11.glTranslatef(x, y, z);
	}

	public static void translate(double x, double y, double z) {
		GL11.glTranslated(x, y, z);
	}

	public static void multMatrix(FloatBuffer matrix) {
		GL11.glMultMatrixf(matrix);
	}

	public static void rotate(Quaternionf quaternionIn) {
		multMatrix(quatToGlMatrix(BUF_FLOAT_16, quaternionIn));
	}

	public static FloatBuffer quatToGlMatrix(FloatBuffer buffer, Quaternionf quaternionIn) {
		buffer.clear();
		float f = quaternionIn.x * quaternionIn.x;
		float f1 = quaternionIn.x * quaternionIn.y;
		float f2 = quaternionIn.x * quaternionIn.z;
		float f3 = quaternionIn.x * quaternionIn.w;
		float f4 = quaternionIn.y * quaternionIn.y;
		float f5 = quaternionIn.y * quaternionIn.z;
		float f6 = quaternionIn.y * quaternionIn.w;
		float f7 = quaternionIn.z * quaternionIn.z;
		float f8 = quaternionIn.z * quaternionIn.w;
		buffer.put(1F - 2F * (f4 + f7));
		buffer.put(2F * (f1 + f8));
		buffer.put(2F * (f2 - f6));
		buffer.put(0F);
		buffer.put(2F * (f1 - f8));
		buffer.put(1F - 2F * (f + f7));
		buffer.put(2F * (f5 + f3));
		buffer.put(0F);
		buffer.put(2F * (f2 + f6));
		buffer.put(2F * (f5 - f3));
		buffer.put(1F - 2F * (f + f4));
		buffer.put(0F);
		buffer.put(0F);
		buffer.put(0F);
		buffer.put(0F);
		buffer.put(1F);
		buffer.rewind();
		return buffer;
	}

	public static void color(float red, float green, float blue, float colorAlpha) {
		if (red != colorStateRed || green != colorStateGreen || blue != colorStateBlue || colorAlpha != colorStateAlpha) {
			colorStateRed = red;
			colorStateGreen = green;
			colorStateBlue = blue;
			colorStateAlpha = colorAlpha;
			GL11.glColor4f(red, green, blue, colorAlpha);
		}
	}

	public static void color(float red, float green, float blue) {
		color(red, green, blue, 1F);
	}

	public static void glTexCoord2f(float sCoord, float tCoord) {
		GL11.glTexCoord2f(sCoord, tCoord);
	}

	public static void glVertex3f(float x, float y, float z) {
		GL11.glVertex3f(x, y, z);
	}

	public static void resetColor() {
		colorStateRed = -1F;
		colorStateGreen = -1F;
		colorStateBlue = -1F;
		colorStateAlpha = -1F;
	}

	public static void glNormalPointer(int type, int stride, ByteBuffer buffer) {
		GL11.glNormalPointer(type, stride, buffer);
	}

	public static void glTexCoordPointer(int size, int type, int stride, int offset) {
		GL11.glTexCoordPointer(size, type, stride, offset);
	}

	public static void glTexCoordPointer(int size, int type, int stride, ByteBuffer buffer) {
		GL11.glTexCoordPointer(size, type, stride, buffer);
	}

	public static void glVertexPointer(int size, int type, int stride, int offset) {
		GL11.glVertexPointer(size, type, stride, offset);
	}

	public static void glVertexPointer(int size, int type, int stride, ByteBuffer buffer) {
		GL11.glVertexPointer(size, type, stride, buffer);
	}

	public static void glColorPointer(int size, int type, int stride, int offset) {
		GL11.glColorPointer(size, type, stride, offset);
	}

	public static void glColorPointer(int size, int type, int stride, ByteBuffer buffer) {
		GL11.glColorPointer(size, type, stride, buffer);
	}

	public static void glDisableClientState(int cap) {
		GL11.glDisableClientState(cap);
	}

	public static void glEnableClientState(int cap) {
		GL11.glEnableClientState(cap);
	}

	public static void glBegin(int mode) {
		GL11.glBegin(mode);
	}

	public static void glEnd() {
		GL11.glEnd();
	}

	public static void glDrawArrays(int mode, int first, int count) {
		GL11.glDrawArrays(mode, first, count);
	}

	public static void glLineWidth(float width) {
		GL11.glLineWidth(width);
	}

	public static void callList(int list) {
		GL11.glCallList(list);
	}

	public static void glDeleteLists(int list, int range) {
		GL11.glDeleteLists(list, range);
	}

	public static void glNewList(int list, int mode) {
		GL11.glNewList(list, mode);
	}

	public static void glEndList() {
		GL11.glEndList();
	}

	public static int glGenLists(int range) {
		return GL11.glGenLists(range);
	}

	public static void glPixelStorei(int parameterName, int param) {
		GL11.glPixelStorei(parameterName, param);
	}

	public static void glReadPixels(int x, int y, int width, int height, int format, int type, IntBuffer pixels) {
		GL11.glReadPixels(x, y, width, height, format, type, pixels);
	}

	public static int glGetError() {
		return GL11.glGetError();
	}

	public static String glGetString(int name) {
		return GL11.glGetString(name);
	}

	public static void glGetInteger(int parameterName, IntBuffer parameters) {
		parameters.clear();
		GL11.glGetIntegerv(parameterName, parameters);
	}

	public static int glGetInteger(int parameterName) {
		return GL11.glGetInteger(parameterName);
	}

	public static void enableBlendProfile(GlStateManager.Profile profile) {
		profile.apply();
	}

	public static void disableBlendProfile(GlStateManager.Profile profile) {
		profile.clean();
	}

	public enum CullFace {
		FRONT(GL11.GL_FRONT),
		BACK(GL11.GL_BACK),
		FRONT_AND_BACK(GL11.GL_FRONT_AND_BACK);
		
		public final int mode;

		CullFace(int modeIn) {
			mode = modeIn;
		}
	}

	public enum DestFactor {
		CONSTANT_ALPHA(GL14.GL_CONSTANT_ALPHA),
		CONSTANT_COLOR(GL14.GL_CONSTANT_COLOR),
		DST_ALPHA(GL11.GL_DST_ALPHA),
		DST_COLOR(GL11.GL_DST_COLOR),
		ONE(GL11.GL_ONE),
		ONE_MINUS_CONSTANT_ALPHA(GL14.GL_ONE_MINUS_CONSTANT_ALPHA),
		ONE_MINUS_CONSTANT_COLOR(GL14.GL_ONE_MINUS_CONSTANT_COLOR),
		ONE_MINUS_DST_ALPHA(GL11.GL_ONE_MINUS_DST_ALPHA),
		ONE_MINUS_DST_COLOR(GL11.GL_ONE_MINUS_DST_COLOR),
		ONE_MINUS_SRC_ALPHA(GL11.GL_ONE_MINUS_SRC_ALPHA),
		ONE_MINUS_SRC_COLOR(GL11.GL_ONE_MINUS_SRC_COLOR),
		SRC_ALPHA(GL11.GL_SRC_ALPHA),
		SRC_COLOR(GL11.GL_SRC_COLOR),
		ZERO(GL11.GL_ZERO);
		public final int factor;

		DestFactor(int factorIn) {
			factor = factorIn;
		}
	}

	public enum FogMode {
		LINEAR(GL11.GL_LINEAR),
		EXP(GL11.GL_EXP),
		EXP2(GL11.GL_EXP2);
		public final int capabilityId;

		FogMode(int capabilityIn) {
			capabilityId = capabilityIn;
		}
	}

	public enum LogicOp {
		AND(GL11.GL_AND),
		AND_INVERTED(GL11.GL_AND_INVERTED),
		AND_REVERSE(GL11.GL_AND_REVERSE),
		CLEAR(GL11.GL_CLEAR),
		COPY(GL11.GL_COPY),
		COPY_INVERTED(GL11.GL_COPY_INVERTED),
		EQUIV(GL11.GL_EQUIV),
		INVERT(GL11.GL_INVERT),
		NAND(GL11.GL_NAND),
		NOOP(GL11.GL_NOOP),
		NOR(GL11.GL_NOR),
		OR(GL11.GL_OR),
		OR_INVERTED(GL11.GL_OR_INVERTED),
		OR_REVERSE(GL11.GL_OR_REVERSE),
		SET(GL11.GL_SET),
		XOR(GL11.GL_XOR);
		public final int opcode;

		LogicOp(int opcodeIn) {
			opcode = opcodeIn;
		}
	}

	public enum Profile {
		DEFAULT {
			public void apply() {
				disableAlpha();
				alphaFunc(GL11.GL_ALWAYS, 0F);
				disableLighting();
				GL11.glLightModelfv(GL11.GL_LIGHT_MODEL_AMBIENT, RenderHelper.setColorBuffer(0.2F, 0.2F, 0.2F, 1F));
				for (int i = 0; i < 8; ++i) {
					disableLight(i);
					GL11.glLightfv(GL11.GL_LIGHT0 + i, GL11.GL_AMBIENT, RenderHelper.setColorBuffer(0F, 0F, 0F, 1F));
					GL11.glLightfv(GL11.GL_LIGHT0 + i, GL11.GL_POSITION, RenderHelper.setColorBuffer(0F, 0F, 1F, 0F));
					if (i == 0) {
						GL11.glLightfv(GL11.GL_LIGHT0 + i, GL11.GL_DIFFUSE, RenderHelper.setColorBuffer(1F, 1F, 1F, 1F));
						GL11.glLightfv(GL11.GL_LIGHT0 + i, GL11.GL_SPECULAR, RenderHelper.setColorBuffer(1F, 1F, 1F, 1F));
					} else {
						GL11.glLightfv(GL11.GL_LIGHT0 + i, GL11.GL_DIFFUSE, RenderHelper.setColorBuffer(0F, 0F, 0F, 1F));
						GL11.glLightfv(GL11.GL_LIGHT0 + i, GL11.GL_SPECULAR, RenderHelper.setColorBuffer(0F, 0F, 0F, 1F));
					}
				}
				disableColorMaterial();
				colorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
				disableDepth();
				depthFunc(GL11.GL_LESS);
				depthMask(true);
				disableBlend();
				blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GL14.glBlendEquation(GL14.GL_FUNC_ADD);
				disableFog();
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
				setFogDensity(1F);
				setFogStart(0F);
				setFogEnd(1F);
				GL11.glFogfv(GL11.GL_FOG_COLOR, RenderHelper.setColorBuffer(0F, 0F, 0F, 0F));
				if (GL.getCapabilities().GL_NV_fog_distance) {
					GL11.glFogi(GL11.GL_FOG_MODE, 34140);
				}
				doPolygonOffset(0F, 0F);
				disableColorLogic();
				colorLogicOp(GL11.GL_COPY);
				disableTexGenCoord(GlStateManager.TexGen.S);
				texGen(GlStateManager.TexGen.S, GL11.GL_TEXTURE_GEN_MODE);
				texGen(GlStateManager.TexGen.S, GL11.GL_OBJECT_PLANE, RenderHelper.setColorBuffer(1F, 0F, 0F, 0F));
				texGen(GlStateManager.TexGen.S, GL11.GL_EYE_PLANE, RenderHelper.setColorBuffer(1F, 0F, 0F, 0F));
				disableTexGenCoord(GlStateManager.TexGen.T);
				texGen(GlStateManager.TexGen.T, GL11.GL_TEXTURE_GEN_MODE);
				texGen(GlStateManager.TexGen.T, GL11.GL_OBJECT_PLANE, RenderHelper.setColorBuffer(0F, 1F, 0F, 0F));
				texGen(GlStateManager.TexGen.T, GL11.GL_EYE_PLANE, RenderHelper.setColorBuffer(0F, 1F, 0F, 0F));
				disableTexGenCoord(GlStateManager.TexGen.R);
				texGen(GlStateManager.TexGen.R, GL11.GL_TEXTURE_GEN_MODE);
				texGen(GlStateManager.TexGen.R, GL11.GL_OBJECT_PLANE, RenderHelper.setColorBuffer(0F, 0F, 0F, 0F));
				texGen(GlStateManager.TexGen.R, GL11.GL_EYE_PLANE, RenderHelper.setColorBuffer(0F, 0F, 0F, 0F));
				disableTexGenCoord(GlStateManager.TexGen.Q);
				texGen(GlStateManager.TexGen.Q, GL11.GL_TEXTURE_GEN_MODE);
				texGen(GlStateManager.TexGen.Q, GL11.GL_OBJECT_PLANE, RenderHelper.setColorBuffer(0F, 0F, 0F, 0F));
				texGen(GlStateManager.TexGen.Q, GL11.GL_EYE_PLANE, RenderHelper.setColorBuffer(0F, 0F, 0F, 0F));
				setActiveTexture(0);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 1000);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, 1000);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, -1000);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0F);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				GL11.glTexEnvfv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, RenderHelper.setColorBuffer(0F, 0F, 0F, 0F));
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_MODULATE);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL11.GL_TEXTURE);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE1_RGB, GL13.GL_PREVIOUS);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE2_RGB, GL13.GL_CONSTANT);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE1_ALPHA, GL13.GL_PREVIOUS);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE2_ALPHA, GL13.GL_CONSTANT);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND2_RGB, GL11.GL_SRC_ALPHA);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND1_ALPHA, GL11.GL_SRC_ALPHA);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND2_ALPHA, GL11.GL_SRC_ALPHA);
				GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL13.GL_RGB_SCALE, 1F);
				GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_ALPHA_SCALE, 1F);
				disableNormalize();
				shadeModel(GL11.GL_SMOOTH);
				disableRescaleNormal();
				colorMask(true, true, true, true);
				clearDepth(1D);
				GL11.glLineWidth(1F);
				GL11.glNormal3f(0F, 0F, 1F);
				GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
				GL11.glPolygonMode(GL11.GL_BACK, GL11.GL_FILL);
			}

			public void clean() {
			}
		},
		PLAYER_SKIN {
			public void apply() {
				enableBlend();
				tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
			}

			public void clean() {
				disableBlend();
			}
		},
		TRANSPARENT_MODEL {
			public void apply() {
				color(1F, 1F, 1F, 0.15F);
				depthMask(false);
				enableBlend();
				blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				alphaFunc(GL11.GL_GREATER, 0.003921569F);
			}

			public void clean() {
				disableBlend();
				alphaFunc(GL11.GL_GREATER, 0.1F);
				depthMask(true);
			}
		};

		Profile() {
		}

		public abstract void apply();

		public abstract void clean();
	}

	public enum SourceFactor {
		CONSTANT_ALPHA(GL14.GL_CONSTANT_ALPHA),
		CONSTANT_COLOR(GL14.GL_CONSTANT_COLOR),
		DST_ALPHA(GL11.GL_DST_ALPHA),
		DST_COLOR(GL11.GL_DST_COLOR),
		ONE(GL11.GL_ONE),
		ONE_MINUS_CONSTANT_ALPHA(GL14.GL_ONE_MINUS_CONSTANT_ALPHA),
		ONE_MINUS_CONSTANT_COLOR(GL14.GL_ONE_MINUS_CONSTANT_COLOR),
		ONE_MINUS_DST_ALPHA(GL11.GL_ONE_MINUS_DST_ALPHA),
		ONE_MINUS_DST_COLOR(GL11.GL_ONE_MINUS_DST_COLOR),
		ONE_MINUS_SRC_ALPHA(GL11.GL_ONE_MINUS_SRC_ALPHA),
		ONE_MINUS_SRC_COLOR(GL11.GL_ONE_MINUS_SRC_COLOR),
		SRC_ALPHA(GL11.GL_SRC_ALPHA),
		SRC_ALPHA_SATURATE(GL11.GL_SRC_ALPHA_SATURATE),
		SRC_COLOR(GL11.GL_SRC_COLOR),
		ZERO(GL11.GL_ZERO);
		public final int factor;

		SourceFactor(int factorIn) {
			factor = factorIn;
		}
	}

	public enum TexGen {
		S, T, R, Q
	}

}
