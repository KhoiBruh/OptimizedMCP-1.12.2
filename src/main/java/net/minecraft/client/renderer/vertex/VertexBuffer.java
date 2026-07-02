package net.minecraft.client.renderer.vertex;

import net.minecraft.client.renderer.GLS;

import java.nio.ByteBuffer;

public class VertexBuffer {

	private final VertexFormat vertexFormat;
	private int glBufferId;
	private int count;

	public VertexBuffer(VertexFormat vertexFormatIn) {
		vertexFormat = vertexFormatIn;
		glBufferId = GLS.genBuffers();
	}

	public void bindBuffer() {
		GLS.bindBuffer(GLS.GL_ARRAY_BUFFER, glBufferId);
	}

	public void bufferData(ByteBuffer data) {
		bindBuffer();
		GLS.bufferData(GLS.GL_ARRAY_BUFFER, data, GLS.GL_STATIC_DRAW);
		unbindBuffer();
		count = data.limit() / vertexFormat.getNextOffset();
	}

	public void drawArrays(int mode) {
		GLS.drawArrays(mode, 0, count);
	}

	public void unbindBuffer() {
		GLS.bindBuffer(GLS.GL_ARRAY_BUFFER, 0);
	}

	public void deleteGlBuffers() {
		if (glBufferId >= 0) {
			GLS.deleteBuffers(glBufferId);
			glBufferId = -1;
		}
	}

}
