package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class RenderChunk {

	public static int renderChunksUpdated;
	private final RenderGlobal renderGlobal;
	private final ReentrantLock lockCompileTask = new ReentrantLock();
	private final ReentrantLock lockCompiledChunk = new ReentrantLock();
	private final Set<TileEntity> setTileEntities = Sets.newHashSet();
	private final int index;
	private final FloatBuffer modelviewMatrix = GLAllocation.createDirectFloatBuffer(16);
	private final VertexBuffer[] vertexBuffers = new VertexBuffer[BlockRenderLayer.values().length];
	private final BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos(-1, -1, -1);
	private final BlockPos.MutableBlockPos[] mapEnumFacing = new BlockPos.MutableBlockPos[6];
	public CompiledChunk compiledChunk = CompiledChunk.DUMMY;
	public AxisAlignedBB boundingBox;
	private World world;
	private ChunkCompileTaskGenerator compileTask;
	private int frameIndex = -1;
	private boolean needsUpdate = true;
	private boolean needsImmediateUpdate;
	private ChunkCache worldView;

	public RenderChunk(World worldIn, RenderGlobal renderGlobalIn, int indexIn) {

		for (int i = 0; i < mapEnumFacing.length; ++i) {
			mapEnumFacing[i] = new BlockPos.MutableBlockPos();
		}

		world = worldIn;
		renderGlobal = renderGlobalIn;
		index = indexIn;

		if (OpenGlHelper.useVbo()) {
			for (int j = 0; j < BlockRenderLayer.values().length; ++j) {
				vertexBuffers[j] = new VertexBuffer(DefaultVertexFormats.BLOCK);
			}
		}
	}

	public boolean setFrameIndex(int frameIndexIn) {

		if (frameIndex == frameIndexIn) {
			return false;
		} else {
			frameIndex = frameIndexIn;
			return true;
		}
	}

	public VertexBuffer getVertexBufferByLayer(int layer) {

		return vertexBuffers[layer];
	}

	/**
	 * Sets the RenderChunk base position
	 */
	public void setPosition(int x, int y, int z) {

		if (x != position.getX() || y != position.getY() || z != position.getZ()) {
			stopCompileTask();
			position.setPos(x, y, z);
			boundingBox = new AxisAlignedBB(x, y, z, x + 16, y + 16, z + 16);

			for (EnumFacing enumfacing : EnumFacing.values()) {
				mapEnumFacing[enumfacing.ordinal()].setPos(position).move(enumfacing, 16);
			}

			initModelviewMatrix();
		}
	}

	public void resortTransparency(float x, float y, float z, ChunkCompileTaskGenerator generator) {

		CompiledChunk compiledchunk = generator.getCompiledChunk();

		if (compiledchunk.getState() != null && !compiledchunk.isLayerEmpty(BlockRenderLayer.TRANSLUCENT)) {
			preRenderBlocks(generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT), position);
			generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT).setVertexState(compiledchunk.getState());
			postRenderBlocks(BlockRenderLayer.TRANSLUCENT, x, y, z, generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT), compiledchunk);
		}
	}

	public void rebuildChunk(float x, float y, float z, ChunkCompileTaskGenerator generator) {

		CompiledChunk compiledchunk = new CompiledChunk();
		int i = 1;
		BlockPos blockpos = position;
		BlockPos blockpos1 = blockpos.add(15, 15, 15);
		generator.getLock().lock();

		try {
			if (generator.getStatus() != ChunkCompileTaskGenerator.Status.COMPILING) {
				return;
			}

			generator.setCompiledChunk(compiledchunk);
		} finally {
			generator.getLock().unlock();
		}

		VisGraph lvt_9_1_ = new VisGraph();
		HashSet lvt_10_1_ = Sets.newHashSet();

		if (!worldView.isEmpty()) {
			++renderChunksUpdated;
			boolean[] aboolean = new boolean[BlockRenderLayer.values().length];
			BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

			for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(blockpos, blockpos1)) {
				IBlockState iblockstate = worldView.getBlockState(blockpos$mutableblockpos);
				Block block = iblockstate.getBlock();

				if (iblockstate.isOpaqueCube()) {
					lvt_9_1_.setOpaqueCube(blockpos$mutableblockpos);
				}

				if (block.hasTileEntity()) {
					TileEntity tileentity = worldView.getTileEntity(blockpos$mutableblockpos, Chunk.EnumCreateEntityType.CHECK);

					if (tileentity != null) {
						TileEntitySpecialRenderer<TileEntity> tileentityspecialrenderer = TileEntityRendererDispatcher.instance.getRenderer(tileentity);

						if (tileentityspecialrenderer != null) {
							compiledchunk.addTileEntity(tileentity);

							if (tileentityspecialrenderer.isGlobalRenderer(tileentity)) {
								lvt_10_1_.add(tileentity);
							}
						}
					}
				}

				BlockRenderLayer blockrenderlayer1 = block.getBlockLayer();
				int j = blockrenderlayer1.ordinal();

				if (block.getDefaultState().getRenderType() != EnumBlockRenderType.INVISIBLE) {
					BufferBuilder bufferbuilder = generator.getRegionRenderCacheBuilder().getWorldRendererByLayerId(j);

					if (!compiledchunk.isLayerStarted(blockrenderlayer1)) {
						compiledchunk.setLayerStarted(blockrenderlayer1);
						preRenderBlocks(bufferbuilder, blockpos);
					}

					aboolean[j] |= blockrendererdispatcher.renderBlock(iblockstate, blockpos$mutableblockpos, worldView, bufferbuilder);
				}
			}

			for (BlockRenderLayer blockrenderlayer : BlockRenderLayer.values()) {
				if (aboolean[blockrenderlayer.ordinal()]) {
					compiledchunk.setLayerUsed(blockrenderlayer);
				}

				if (compiledchunk.isLayerStarted(blockrenderlayer)) {
					postRenderBlocks(blockrenderlayer, x, y, z, generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(blockrenderlayer), compiledchunk);
				}
			}
		}

		compiledchunk.setVisibility(lvt_9_1_.computeVisibility());
		lockCompileTask.lock();

		try {
			Set<TileEntity> set = Sets.newHashSet(lvt_10_1_);
			Set<TileEntity> set1 = Sets.newHashSet(setTileEntities);
			set.removeAll(setTileEntities);
			set1.removeAll(lvt_10_1_);
			setTileEntities.clear();
			setTileEntities.addAll(lvt_10_1_);
			renderGlobal.updateTileEntities(set1, set);
		} finally {
			lockCompileTask.unlock();
		}
	}

	protected void finishCompileTask() {

		lockCompileTask.lock();

		try {
			if (compileTask != null && compileTask.getStatus() != ChunkCompileTaskGenerator.Status.DONE) {
				compileTask.finish();
				compileTask = null;
			}
		} finally {
			lockCompileTask.unlock();
		}
	}

	public ReentrantLock getLockCompileTask() {

		return lockCompileTask;
	}

	public ChunkCompileTaskGenerator makeCompileTaskChunk() {

		lockCompileTask.lock();
		ChunkCompileTaskGenerator chunkcompiletaskgenerator;

		try {
			finishCompileTask();
			compileTask = new ChunkCompileTaskGenerator(this, ChunkCompileTaskGenerator.Type.REBUILD_CHUNK, getDistanceSq());
			rebuildWorldView();
			chunkcompiletaskgenerator = compileTask;
		} finally {
			lockCompileTask.unlock();
		}

		return chunkcompiletaskgenerator;
	}

	private void rebuildWorldView() {

		int i = 1;
		worldView = new ChunkCache(world, position.add(-1, -1, -1), position.add(16, 16, 16), 1);
	}

	
	public ChunkCompileTaskGenerator makeCompileTaskTransparency() {

		lockCompileTask.lock();
		ChunkCompileTaskGenerator chunkcompiletaskgenerator;

		try {
			if (compileTask == null || compileTask.getStatus() != ChunkCompileTaskGenerator.Status.PENDING) {
				if (compileTask != null && compileTask.getStatus() != ChunkCompileTaskGenerator.Status.DONE) {
					compileTask.finish();
					compileTask = null;
				}

				compileTask = new ChunkCompileTaskGenerator(this, ChunkCompileTaskGenerator.Type.RESORT_TRANSPARENCY, getDistanceSq());
				compileTask.setCompiledChunk(compiledChunk);
				chunkcompiletaskgenerator = compileTask;
				return chunkcompiletaskgenerator;
			}

			chunkcompiletaskgenerator = null;
		} finally {
			lockCompileTask.unlock();
		}

		return chunkcompiletaskgenerator;
	}

	protected double getDistanceSq() {

		EntityPlayerSP entityplayersp = Minecraft.getMinecraft().player;
		double d0 = boundingBox.minX + 8D - entityplayersp.posX;
		double d1 = boundingBox.minY + 8D - entityplayersp.posY;
		double d2 = boundingBox.minZ + 8D - entityplayersp.posZ;
		return d0 * d0 + d1 * d1 + d2 * d2;
	}

	private void preRenderBlocks(BufferBuilder bufferBuilderIn, BlockPos pos) {

		bufferBuilderIn.begin(7, DefaultVertexFormats.BLOCK);
		bufferBuilderIn.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
	}

	private void postRenderBlocks(BlockRenderLayer layer, float x, float y, float z, BufferBuilder bufferBuilderIn, CompiledChunk compiledChunkIn) {

		if (layer == BlockRenderLayer.TRANSLUCENT && !compiledChunkIn.isLayerEmpty(layer)) {
			bufferBuilderIn.sortVertexData(x, y, z);
			compiledChunkIn.setState(bufferBuilderIn.getVertexState());
		}

		bufferBuilderIn.finishDrawing();
	}

	private void initModelviewMatrix() {

		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		float f = 1.000001F;
		GlStateManager.translate(-8F, -8F, -8F);
		GlStateManager.scale(1.000001F, 1.000001F, 1.000001F);
		GlStateManager.translate(8F, 8F, 8F);
		GlStateManager.getFloat(2982, modelviewMatrix);
		GlStateManager.popMatrix();
	}

	public void multModelviewMatrix() {

		GlStateManager.multMatrix(modelviewMatrix);
	}

	public CompiledChunk getCompiledChunk() {

		return compiledChunk;
	}

	public void setCompiledChunk(CompiledChunk compiledChunkIn) {

		lockCompiledChunk.lock();

		try {
			compiledChunk = compiledChunkIn;
		} finally {
			lockCompiledChunk.unlock();
		}
	}

	public void stopCompileTask() {

		finishCompileTask();
		compiledChunk = CompiledChunk.DUMMY;
	}

	public void deleteGlResources() {

		stopCompileTask();
		world = null;

		for (int i = 0; i < BlockRenderLayer.values().length; ++i) {
			if (vertexBuffers[i] != null) {
				vertexBuffers[i].deleteGlBuffers();
			}
		}
	}

	public BlockPos getPosition() {

		return position;
	}

	public void setNeedsUpdate(boolean immediate) {

		if (needsUpdate) {
			immediate |= needsImmediateUpdate;
		}

		needsUpdate = true;
		needsImmediateUpdate = immediate;
	}

	public void clearNeedsUpdate() {

		needsUpdate = false;
		needsImmediateUpdate = false;
	}

	public boolean needsUpdate() {

		return needsUpdate;
	}

	public boolean needsImmediateUpdate() {

		return needsUpdate && needsImmediateUpdate;
	}

	public BlockPos getBlockPosOffset16(EnumFacing facing) {

		return mapEnumFacing[facing.ordinal()];
	}

	public World getWorld() {

		return world;
	}

}
