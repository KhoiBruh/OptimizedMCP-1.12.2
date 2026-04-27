package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public abstract class ChunkRenderContainer {

	protected List<RenderChunk> renderChunks = Lists.newArrayListWithCapacity(17424);
	protected boolean initialized;
	private double viewEntityX;
	private double viewEntityY;
	private double viewEntityZ;

	public void initialize(double viewEntityXIn, double viewEntityYIn, double viewEntityZIn) {

		initialized = true;
		renderChunks.clear();
		viewEntityX = viewEntityXIn;
		viewEntityY = viewEntityYIn;
		viewEntityZ = viewEntityZIn;
	}

	public void preRenderChunk(RenderChunk renderChunkIn) {

		BlockPos blockpos = renderChunkIn.getPosition();
		GlStateManager.translate((float) ((double) blockpos.getX() - viewEntityX), (float) ((double) blockpos.getY() - viewEntityY), (float) ((double) blockpos.getZ() - viewEntityZ));
	}

	public void addRenderChunk(RenderChunk renderChunkIn, BlockRenderLayer layer) {

		renderChunks.add(renderChunkIn);
	}

	public abstract void renderChunkLayer(BlockRenderLayer layer);

}
