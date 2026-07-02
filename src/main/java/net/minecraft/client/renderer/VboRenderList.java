package net.minecraft.client.renderer;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.BlockRenderLayer;

public class VboRenderList extends ChunkRenderContainer {

	public void renderChunkLayer(BlockRenderLayer layer) {
		if (initialized) {
			for (RenderChunk renderchunk : renderChunks) {
				VertexBuffer vertexbuffer = renderchunk.getVertexBufferByLayer(layer.ordinal());
				GLS.pushMatrix();
				preRenderChunk(renderchunk);
				renderchunk.multModelviewMatrix();
				vertexbuffer.bindBuffer();
				setupArrayPointers();
				vertexbuffer.drawArrays(7);
				GLS.popMatrix();
			}

			GLS.bindBuffer(GLS.GL_ARRAY_BUFFER, 0);
			GLS.resetColor();
			renderChunks.clear();
		}
	}

	private void setupArrayPointers() {
		GLS.vertexPointer(3, 5126, 28, 0);
		GLS.colorPointer(4, 5121, 28, 12);
		GLS.texCoordPointer(2, 5126, 28, 16);
		GLS.clientActiveTexture(OpenGlHelper.lightmapTexUnit);
		GLS.texCoordPointer(2, 5122, 28, 24);
		GLS.clientActiveTexture(OpenGlHelper.defaultTexUnit);
	}

}
