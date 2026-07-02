package net.minecraft.client.renderer.vertex;

import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;

public class VertexBuffer {

	private final VertexFormat vertexFormat;
	private int glBufferId;
	private int count;

	public VertexBuffer(VertexFormat vertexFormatIn) {
		vertexFormat = vertexFormatIn;
		glBufferId = GL15.glGenBuffers();
	}

	public void bindBuffer() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferId);
	}

	public void bufferData(ByteBuffer data) {
		bindBuffer();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, 35044);
		unbindBuffer();
		count = data.limit() / vertexFormat.getNextOffset();
	}

	public void drawArrays(int mode) {
		GLS.drawArrays(mode, 0, count);
	}

	public void unbindBuffer() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	public void deleteGlBuffers() {
		if (glBufferId >= 0) {
			GL15.glDeleteBuffers(glBufferId);
			glBufferId = -1;
		}
	}

}
