package net.minecraft.client.renderer;

import net.minecraft.client.renderer.chunk.ListedRenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;

public class RenderList extends ChunkRenderContainer {

	public void renderChunkLayer(BlockRenderLayer layer) {

		if (initialized) {
			for (RenderChunk renderchunk : renderChunks) {
				ListedRenderChunk listedrenderchunk = (ListedRenderChunk) renderchunk;
				GlStateManager.pushMatrix();
				preRenderChunk(renderchunk);
				GlStateManager.callList(listedrenderchunk.getDisplayList(layer, listedrenderchunk.getCompiledChunk()));
				GlStateManager.popMatrix();
			}

			GlStateManager.resetColor();
			renderChunks.clear();
		}
	}

}
