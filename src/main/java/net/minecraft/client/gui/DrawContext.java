package net.minecraft.client.gui;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class DrawContext implements AutoCloseable {

	private static final int MAX_VERTICES = 4096;
	private static final int VERTEX_SIZE_FLOATS = 9; // x, y, z, r, g, b, a, u, v
	private static final int VERTEX_SIZE_BYTES = VERTEX_SIZE_FLOATS * 4;

	private final ByteBuffer byteBuffer;
	private final FloatBuffer floatBuffer;

	private int vbo;
	private int vertecies = 0;

	@Setter
	@Getter
	private float zLevel = 0;

	private enum BatchMode {
		NONE,
		COLOR,
		TEXTURE
	}

	private BatchMode currentMode = BatchMode.NONE;
	private int currentTextureId = -1;

	public DrawContext() {
		byteBuffer = BufferUtils.createByteBuffer(MAX_VERTICES * VERTEX_SIZE_BYTES);
		floatBuffer = byteBuffer.asFloatBuffer();

		vbo = GLS.genBuffers();
		GLS.bindBuffer(GLS.GL_ARRAY_BUFFER, vbo);
		GLS.bufferData(GLS.GL_ARRAY_BUFFER, (long) MAX_VERTICES * VERTEX_SIZE_BYTES, GL15.GL_DYNAMIC_DRAW);
		GLS.bindBuffer(GLS.GL_ARRAY_BUFFER, 0);
	}

	private void vertex(float x, float y, float z, float u, float v, float r, float g, float b, float a) {
		floatBuffer.put(x).put(y).put(z);
		floatBuffer.put(r).put(g).put(b).put(a);
		floatBuffer.put(u).put(v);
		vertecies++;
	}

	private void ensureMode(BatchMode mode, int textureId) {
		if (currentMode != mode || (mode == BatchMode.TEXTURE && currentTextureId != textureId) || vertecies + 4 > MAX_VERTICES) {
			flush();
			currentMode = mode;
			currentTextureId = textureId;
		}
	}

	public void flush() {
		if (vertecies == 0) return;

		boolean disabledTexture = false;
		if (currentMode == BatchMode.COLOR) {
			GLS.disableTexture2D();
			disabledTexture = true;
			GLS.enableBlend();
			GLS.blendFunc(GLS.SourceFactor.SRC_ALPHA, GLS.DestFactor.ONE_MINUS_SRC_ALPHA, GLS.SourceFactor.ONE, GLS.DestFactor.ZERO);
		} else if (currentMode == BatchMode.TEXTURE) {
			GLS.enableTexture2D();
			GLS.bindTexture(currentTextureId);
			GLS.enableBlend();
			GLS.blendFunc(GLS.SourceFactor.SRC_ALPHA, GLS.DestFactor.ONE_MINUS_SRC_ALPHA, GLS.SourceFactor.ONE, GLS.DestFactor.ZERO);
		}

		floatBuffer.flip();
		GLS.bindBuffer(GLS.GL_ARRAY_BUFFER, vbo);

		byteBuffer.limit(floatBuffer.limit() * 4);
		byteBuffer.position(0);
		GLS.bufferSubData(GLS.GL_ARRAY_BUFFER, 0, byteBuffer);

		GLS.enableClientState(GL11.GL_VERTEX_ARRAY);
		GLS.enableClientState(GL11.GL_COLOR_ARRAY);
		GLS.enableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		GLS.vertexPointer(3, GL11.GL_FLOAT, VERTEX_SIZE_BYTES, 0);
		GLS.colorPointer(4, GL11.GL_FLOAT, VERTEX_SIZE_BYTES, 12);
		GLS.texCoordPointer(2, GL11.GL_FLOAT, VERTEX_SIZE_BYTES, 28);

		GLS.drawArrays(GL11.GL_QUADS, 0, vertecies);

		GLS.disableClientState(GL11.GL_VERTEX_ARRAY);
		GLS.disableClientState(GL11.GL_COLOR_ARRAY);
		GLS.disableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		GLS.bindBuffer(GLS.GL_ARRAY_BUFFER, 0);

		if (disabledTexture) {
			GLS.enableTexture2D();
			GLS.disableBlend();
		}

		floatBuffer.clear();
		byteBuffer.clear();

		GLS.resetColor();

		vertecies = 0;
		currentMode = BatchMode.NONE;
		currentTextureId = -1;
	}

	// ==========================================
	// Shape Drawing Methods
	// ==========================================

	public void fill(int x, int y, int width, int height, int color) {
		if (x < width) {
			int i = x;
			x = width;
			width = i;
		}

		if (y < height) {
			int j = y;
			y = height;
			height = j;
		}

		float a = (float) (color >> 24 & 255) / 255F;
		float r = (float) (color >> 16 & 255) / 255F;
		float g = (float) (color >> 8 & 255) / 255F;
		float b = (float) (color & 255) / 255F;

		ensureMode(BatchMode.COLOR, -1);

		vertex(x, height, zLevel, 0, 0, r, g, b, a);
		vertex(width, height, zLevel, 0, 0, r, g, b, a);
		vertex(width, y, zLevel, 0, 0, r, g, b, a);
		vertex(x, y, zLevel, 0, 0, r, g, b, a);
	}

	public void fillGradient(int left, int top, int right, int bottom, int startColor, int endColor) {
		float f = (float) (startColor >> 24 & 255) / 255F;
		float f1 = (float) (startColor >> 16 & 255) / 255F;
		float f2 = (float) (startColor >> 8 & 255) / 255F;
		float f3 = (float) (startColor & 255) / 255F;
		float f4 = (float) (endColor >> 24 & 255) / 255F;
		float f5 = (float) (endColor >> 16 & 255) / 255F;
		float f6 = (float) (endColor >> 8 & 255) / 255F;
		float f7 = (float) (endColor & 255) / 255F;

		ensureMode(BatchMode.COLOR, -1);

		vertex(right, top, zLevel, 0, 0, f1, f2, f3, f);
		vertex(left, top, zLevel, 0, 0, f1, f2, f3, f);
		vertex(left, bottom, zLevel, 0, 0, f5, f6, f7, f4);
		vertex(right, bottom, zLevel, 0, 0, f5, f6, f7, f4);
	}

	public void hLine(int startX, int endX, int y, int color) {
		if (endX < startX) {
			int i = startX;
			startX = endX;
			endX = i;
		}
		fill(startX, y, endX + 1, y + 1, color);
	}

	public void vLine(int x, int startY, int endY, int color) {
		if (endY < startY) {
			int i = startY;
			startY = endY;
			endY = i;
		}
		fill(x, startY + 1, x + 1, endY, color);
	}

	public void outline(int x, int y, int w, int h, int color) {
		hLine(x, x + w - 1, y, color);
		hLine(x, x + w - 1, y + h - 1, color);
		vLine(x, y + 1, y + h - 2, color);
		vLine(x + w - 1, y + 1, y + h - 2, color);
	}

	// ==========================================
	// Texture Drawing Methods
	// ==========================================

	public void blit(int x, int y, int textureX, int textureY, int width, int height) {
		float f = 0.00390625F;
		float f1 = 0.00390625F;
		int textureId = GLS.getInteger(GL11.GL_TEXTURE_BINDING_2D);
		ensureMode(BatchMode.TEXTURE, textureId);

		float u0 = (float) textureX * f;
		float v0 = (float) textureY * f1;
		float u1 = (float) (textureX + width) * f;
		float v1 = (float) (textureY + height) * f1;

		vertex(x, y + height, zLevel, u0, v1, 1F, 1F, 1F, 1F);
		vertex(x + width, y + height, zLevel, u1, v1, 1F, 1F, 1F, 1F);
		vertex(x + width, y, zLevel, u1, v0, 1F, 1F, 1F, 1F);
		vertex(x, y, zLevel, u0, v0, 1F, 1F, 1F, 1F);
	}

	public void blit(float x, float y, int minU, int minV, int maxU, int maxV) {
		float f = 0.00390625F;
		float f1 = 0.00390625F;
		int textureId = GLS.getInteger(GL11.GL_TEXTURE_BINDING_2D);
		ensureMode(BatchMode.TEXTURE, textureId);

		float u0 = (float) minU * f;
		float v0 = (float) minV * f1;
		float u1 = (float) (minU + maxU) * f;
		float v1 = (float) (minV + maxV) * f1;

		vertex(x + 0F, y + (float) maxV, zLevel, u0, v1, 1F, 1F, 1F, 1F);
		vertex(x + (float) maxU, y + (float) maxV, zLevel, u1, v1, 1F, 1F, 1F, 1F);
		vertex(x + (float) maxU, y + 0F, zLevel, u1, v0, 1F, 1F, 1F, 1F);
		vertex(x + 0F, y + 0F, zLevel, u0, v0, 1F, 1F, 1F, 1F);
	}

	public void blit(int x, int y, TextureAtlasSprite sprite, int width, int height) {
		int textureId = GLS.getInteger(GL11.GL_TEXTURE_BINDING_2D);
		ensureMode(BatchMode.TEXTURE, textureId);

		vertex(x, y + height, zLevel, sprite.getMinU(), sprite.getMaxV(), 1F, 1F, 1F, 1F);
		vertex(x + width, y + height, zLevel, sprite.getMaxU(), sprite.getMaxV(), 1F, 1F, 1F, 1F);
		vertex(x + width, y, zLevel, sprite.getMaxU(), sprite.getMinV(), 1F, 1F, 1F, 1F);
		vertex(x, y, zLevel, sprite.getMinU(), sprite.getMinV(), 1F, 1F, 1F, 1F);
	}

	public void blit(int x, int y, float u, float v, int width, int height, float textureWidth, float textureHeight) {
		float f = 1F / textureWidth;
		float f1 = 1F / textureHeight;
		int textureId = GLS.getInteger(GL11.GL_TEXTURE_BINDING_2D);
		ensureMode(BatchMode.TEXTURE, textureId);

		float u0 = u * f;
		float v0 = v * f1;
		float u1 = (u + (float) width) * f;
		float v1 = (v + (float) height) * f1;

		vertex(x, y + height, zLevel, u0, v1, 1F, 1F, 1F, 1F);
		vertex(x + width, y + height, zLevel, u1, v1, 1F, 1F, 1F, 1F);
		vertex(x + width, y, zLevel, u1, v0, 1F, 1F, 1F, 1F);
		vertex(x, y, zLevel, u0, v0, 1F, 1F, 1F, 1F);
	}

	public void blit(int x, int y, int width, int height, float u0, float v0, float u1, float v1, int color) {
		float a = (float) (color >> 24 & 255) / 255F;
		float r = (float) (color >> 16 & 255) / 255F;
		float g = (float) (color >> 8 & 255) / 255F;
		float b = (float) (color & 255) / 255F;
		int textureId = GLS.getInteger(GL11.GL_TEXTURE_BINDING_2D);
		ensureMode(BatchMode.TEXTURE, textureId);

		vertex(x, y + height, zLevel, u0, v1, r, g, b, a);
		vertex(x + width, y + height, zLevel, u1, v1, r, g, b, a);
		vertex(x + width, y, zLevel, u1, v0, r, g, b, a);
		vertex(x, y, zLevel, u0, v0, r, g, b, a);
	}

	public void blitScaled(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
		float f = 1F / tileWidth;
		float f1 = 1F / tileHeight;
		int textureId = GLS.getInteger(GL11.GL_TEXTURE_BINDING_2D);
		ensureMode(BatchMode.TEXTURE, textureId);

		float u0 = u * f;
		float v0 = v * f1;
		float u1 = (u + (float) uWidth) * f;
		float v1 = (v + (float) vHeight) * f1;

		vertex(x, y + height, zLevel, u0, v1, 1F, 1F, 1F, 1F);
		vertex(x + width, y + height, zLevel, u1, v1, 1F, 1F, 1F, 1F);
		vertex(x + width, y, zLevel, u1, v0, 1F, 1F, 1F, 1F);
		vertex(x, y, zLevel, u0, v0, 1F, 1F, 1F, 1F);
	}

	// ==========================================
	// String / Text Drawing Methods
	// ==========================================

	public void drawString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
		flush();
		fontRendererIn.drawShadowText(text, (float) x, (float) y, color);
	}

	public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
		flush();
		fontRendererIn.drawShadowText(text, (float) (x - fontRendererIn.getWidth(text) / 2), (float) y, color);
	}

	// ==========================================
	// State / Matrix Methods
	// ==========================================

	public void push() {
		flush();
		GLS.pushMatrix();
	}

	public void pop() {
		flush();
		GLS.popMatrix();
	}

	public void translate(float x, float y, float z) {
		flush();
		GLS.translate(x, y, z);
	}

	public void scale(float x, float y, float z) {
		flush();
		GLS.scale(x, y, z);
	}

	public void enableScissor(int x, int y, int width, int height) {
		flush();
		GLS.enableScissor();
		GLS.scissor(x, y, width, height);
	}

	public void disableScissor() {
		flush();
		GLS.disableScissor();
	}

	@Override
	public void close() {
		flush();
		if (vbo != 0) {
			GLS.deleteBuffers(vbo);
			vbo = 0;
		}
	}
}
