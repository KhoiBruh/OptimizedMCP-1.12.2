package net.minecraft.client.renderer;

import net.minecraft.client.renderer.chunk.ListedRenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;

public class RenderList extends ChunkRenderContainer {

	public void renderChunkLayer(BlockRenderLayer layer) {

		if (initialized) {
			for (RenderChunk renderchunk : renderChunks) {
				ListedRenderChunk listedrenderchunk = (ListedRenderChunk) renderchunk;
				GLS.pushMatrix();
				preRenderChunk(renderchunk);
				GLS.callList(listedrenderchunk.getDisplayList(layer, listedrenderchunk.getCompiledChunk()));
				GLS.popMatrix();
			}

			GLS.resetColor();
			renderChunks.clear();
		}
	}

}
