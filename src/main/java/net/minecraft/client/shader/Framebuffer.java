package net.minecraft.client.shader;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

public class Framebuffer {

	public int width;
	public int height;

	public int textureWidth;
	public int textureHeight;

	public boolean depth;

	public int id;
	public int texture;
	public int filter;
	public int depthBuffer;

	public float[] color;

	public Framebuffer(int width, int height, boolean useDepth) {
		id = -1;
		texture = -1;
		depth = useDepth;
		depthBuffer = -1;
		color = new float[] { 1F, 1F, 1F, 0F };

		create(width, height);
	}

	public void create(int width, int height) {
		GLS.enableDepth();

		if (id >= 0) delete();

		this.width = width;
		this.height = height;
		textureWidth = width;
		textureHeight = height;

		id = GL30.glGenFramebuffers();
		texture = GLS.genTextures();

		if (depth) depthBuffer = GL30.glGenRenderbuffers();

		setFilter(9728);
		GLS.bindTexture(texture);
		GLS.texImage2D(3553, 0, 32856, textureWidth, textureHeight, 0, 6408, 5121, null);

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, 3553, texture, 0);

		if (depth) {
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
			GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, 33190, textureWidth, textureHeight);
			GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
		}

		clear();
		unbindTexture();

		check();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}

	public void delete() {
		unbindTexture();
		unbind();

		if (depthBuffer > -1) {
			GL30.glDeleteRenderbuffers(depthBuffer);
			depthBuffer = -1;
		}

		if (texture > -1) {
			GLS.deleteTexture(texture);
			texture = -1;
		}

		if (id > -1) {
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
			GL30.glDeleteFramebuffers(id);
			id = -1;
		}
	}

	public void setFilter(int filter) {
		this.filter = filter;
		GLS.bindTexture(texture);
		GLS.texParameteri(3553, 10241, filter);
		GLS.texParameteri(3553, 10240, filter);
		GLS.texParameteri(3553, 10242, 10496);
		GLS.texParameteri(3553, 10243, 10496);
		GLS.bindTexture(0);
	}

	public void check() {
		int i = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);

		if (i != GL30.GL_FRAMEBUFFER_COMPLETE) {
			if (i == GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT) {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
			} else if (i == GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT) {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
			} else if (i == GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER) {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
			} else if (i == GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER) {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
			} else {
				throw new RuntimeException("glCheckFramebufferStatus returned unknown status:" + i);
			}
		}
	}

	public void bindTexture() {
		GLS.bindTexture(texture);
	}

	public void unbindTexture() {
		GLS.bindTexture(0);
	}

	public void bind(boolean viewport) {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);
		if (viewport) GLS.viewport(0, 0, width, height);
	}

	public void unbind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}

	public void setColor(float red, float green, float blue, float alpha) {
		color[0] = red;
		color[1] = green;
		color[2] = blue;
		color[3] = alpha;
	}

	public void render(int width, int height) {
		render(width, height, true);
	}

	public void render(int width, int height, boolean mask) {
		GLS.colorMask(true, true, true, false);
		GLS.disableDepth();
		GLS.depthMask(false);
		GLS.matrixMode(5889);
		GLS.loadIdentity();
		GLS.ortho(0D, width, height, 0D, 1000D, 3000D);
		GLS.matrixMode(5888);
		GLS.loadIdentity();
		GLS.translate(0F, 0F, -2000F);
		GLS.viewport(0, 0, width, height);
		GLS.enableTexture2D();
		GLS.disableLighting();
		GLS.disableAlpha();

		if (mask) {
			GLS.disableBlend();
			GLS.enableColorMaterial();
		}

		GLS.color(1F, 1F, 1F, 1F);
		bindTexture();

		float f = (float) width;
		float f1 = (float) height;
		float f2 = (float) this.width / (float) textureWidth;
		float f3 = (float) this.height / (float) textureHeight;

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.pos(0D, f1, 0D).tex(0D, 0D).color(255, 255, 255, 255).endVertex();
		bufferbuilder.pos(f, f1, 0D).tex(f2, 0D).color(255, 255, 255, 255).endVertex();
		bufferbuilder.pos(f, 0D, 0D).tex(f2, f3).color(255, 255, 255, 255).endVertex();
		bufferbuilder.pos(0D, 0D, 0D).tex(0D, f3).color(255, 255, 255, 255).endVertex();
		tessellator.draw();
		unbindTexture();
		GLS.depthMask(true);
		GLS.colorMask(true, true, true, true);
	}

	public void clear() {
		bind(true);
		GLS.clearColor(color[0], color[1], color[2], color[3]);
		int i = 16384;

		if (depth) {
			GLS.clearDepth(1D);
			i |= 256;
		}

		GLS.clear(i);
		unbind();
	}

}
