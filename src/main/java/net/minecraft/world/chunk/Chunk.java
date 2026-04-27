package net.minecraft.world.chunk;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkGeneratorDebug;
import net.minecraft.world.gen.IChunkGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Chunk {

	public static final ExtendedBlockStorage NULL_BLOCK_STORAGE = null;
	private static final Logger LOGGER = LogManager.getLogger();
	/**
	 * The x coordinate of the chunk.
	 */
	public final int x;
	/**
	 * The z coordinate of the chunk.
	 */
	public final int z;
	/**
	 * Used to store block IDs, block MSBs, Sky-light maps, Block-light maps, and metadata. Each entry corresponds to a
	 * logical segment of 16x16x16 blocks, stacked vertically.
	 */
	private final ExtendedBlockStorage[] storageArrays;
	/**
	 * Contains a 16x16 mapping on the X/Z plane of the biome ID to which each colum belongs.
	 */
	private final byte[] blockBiomeArray;
	/**
	 * A map, similar to heightMap, that tracks how far down precipitation can fall.
	 */
	private final int[] precipitationHeightMap;
	/**
	 * Which columns need their skylightMaps updated.
	 */
	private final boolean[] updateSkylightColumns;
	/**
	 * Reference to the World object.
	 */
	private final World world;
	private final int[] heightMap;
	private final Map<BlockPos, TileEntity> tileEntities;
	private final ClassInheritanceMultiMap<Entity>[] entityLists;
	private final ConcurrentLinkedQueue<BlockPos> tileEntityPosQueue;
	public boolean unloadQueued;
	/**
	 * Whether or not this Chunk is currently loaded into the World
	 */
	private boolean loaded;
	private boolean isGapLightingUpdated;
	/**
	 * Boolean value indicating if the terrain is populated.
	 */
	private boolean isTerrainPopulated;
	private boolean isLightPopulated;
	private boolean ticked;
	/**
	 * Set to true if the chunk has been modified and needs to be updated internally.
	 */
	private boolean dirty;
	/**
	 * Whether this Chunk has any Entities and thus requires saving on every tick
	 */
	private boolean hasEntities;
	/**
	 * The time according to World.worldTime when this chunk was last saved
	 */
	private long lastSaveTime;
	/**
	 * Lowest value in the heightmap.
	 */
	private int heightMapMinimum;
	/**
	 * the cumulative number of ticks players have been in this chunk
	 */
	private long inhabitedTime;
	/**
	 * Contains the current round-robin relight check index, and is implied as the relight check location as well.
	 */
	private int queuedLightChecks;

	public Chunk(World worldIn, int x, int z) {

		storageArrays = new ExtendedBlockStorage[16];
		blockBiomeArray = new byte[256];
		precipitationHeightMap = new int[256];
		updateSkylightColumns = new boolean[256];
		tileEntities = Maps.newHashMap();
		queuedLightChecks = 4096;
		tileEntityPosQueue = Queues.newConcurrentLinkedQueue();
		entityLists = (ClassInheritanceMultiMap[]) (new ClassInheritanceMultiMap[16]);
		world = worldIn;
		this.x = x;
		this.z = z;
		heightMap = new int[256];

		for (int i = 0; i < entityLists.length; ++i) {
			entityLists[i] = new ClassInheritanceMultiMap(Entity.class);
		}

		Arrays.fill(precipitationHeightMap, -999);
		Arrays.fill(blockBiomeArray, (byte) -1);
	}

	public Chunk(World worldIn, ChunkPrimer primer, int x, int z) {

		this(worldIn, x, z);
		int i = 256;
		boolean flag = worldIn.provider.hasSkyLight();

		for (int j = 0; j < 16; ++j) {
			for (int k = 0; k < 16; ++k) {
				for (int l = 0; l < 256; ++l) {
					IBlockState iblockstate = primer.getBlockState(j, l, k);

					if (iblockstate.getMaterial() != Material.AIR) {
						int i1 = l >> 4;

						if (storageArrays[i1] == NULL_BLOCK_STORAGE) {
							storageArrays[i1] = new ExtendedBlockStorage(i1 << 4, flag);
						}

						storageArrays[i1].set(j, l & 15, k, iblockstate);
					}
				}
			}
		}
	}

	/**
	 * Checks whether the chunk is at the X/Z location specified
	 */
	public boolean isAtLocation(int x, int z) {

		return x == this.x && z == this.z;
	}

	public int getHeight(BlockPos pos) {

		return getHeightValue(pos.getX() & 15, pos.getZ() & 15);
	}

	/**
	 * Returns the value in the height map at this x, z coordinate in the chunk
	 */
	public int getHeightValue(int x, int z) {

		return heightMap[z << 4 | x];
	}

	@Nullable
	private ExtendedBlockStorage getLastExtendedBlockStorage() {

		for (int i = storageArrays.length - 1; i >= 0; --i) {
			if (storageArrays[i] != NULL_BLOCK_STORAGE) {
				return storageArrays[i];
			}
		}

		return null;
	}

	/**
	 * Returns the topmost ExtendedBlockStorage instance for this Chunk that actually contains a block.
	 */
	public int getTopFilledSegment() {

		ExtendedBlockStorage extendedblockstorage = getLastExtendedBlockStorage();
		return extendedblockstorage == null ? 0 : extendedblockstorage.getYLocation();
	}

	/**
	 * Returns the ExtendedBlockStorage array for this Chunk.
	 */
	public ExtendedBlockStorage[] getBlockStorageArray() {

		return storageArrays;
	}

	/**
	 * Generates the height map for a chunk from scratch
	 */
	protected void generateHeightMap() {

		int i = getTopFilledSegment();
		heightMapMinimum = Integer.MAX_VALUE;

		for (int j = 0; j < 16; ++j) {
			for (int k = 0; k < 16; ++k) {
				precipitationHeightMap[j + (k << 4)] = -999;

				for (int l = i + 16; l > 0; --l) {
					IBlockState iblockstate = getBlockState(j, l - 1, k);

					if (iblockstate.getLightOpacity() != 0) {
						heightMap[k << 4 | j] = l;

						if (l < heightMapMinimum) {
							heightMapMinimum = l;
						}

						break;
					}
				}
			}
		}

		dirty = true;
	}

	/**
	 * Generates the initial skylight map for the chunk upon generation or load.
	 */
	public void generateSkylightMap() {

		int i = getTopFilledSegment();
		heightMapMinimum = Integer.MAX_VALUE;

		for (int j = 0; j < 16; ++j) {
			for (int k = 0; k < 16; ++k) {
				precipitationHeightMap[j + (k << 4)] = -999;

				for (int l = i + 16; l > 0; --l) {
					if (getBlockLightOpacity(j, l - 1, k) != 0) {
						heightMap[k << 4 | j] = l;

						if (l < heightMapMinimum) {
							heightMapMinimum = l;
						}

						break;
					}
				}

				if (world.provider.hasSkyLight()) {
					int k1 = 15;
					int i1 = i + 16 - 1;

					while (true) {
						int j1 = getBlockLightOpacity(j, i1, k);

						if (j1 == 0 && k1 != 15) {
							j1 = 1;
						}

						k1 -= j1;

						if (k1 > 0) {
							ExtendedBlockStorage extendedblockstorage = storageArrays[i1 >> 4];

							if (extendedblockstorage != NULL_BLOCK_STORAGE) {
								extendedblockstorage.setSkyLight(j, i1 & 15, k, k1);
								world.notifyLightSet(new BlockPos((x << 4) + j, i1, (z << 4) + k));
							}
						}

						--i1;

						if (i1 <= 0 || k1 <= 0) {
							break;
						}
					}
				}
			}
		}

		dirty = true;
	}

	/**
	 * Propagates a given sky-visible block's light value downward and upward to neighboring blocks as necessary.
	 */
	private void propagateSkylightOcclusion(int x, int z) {

		updateSkylightColumns[x + z * 16] = true;
		isGapLightingUpdated = true;
	}

	private void recheckGaps(boolean onlyOne) {

		world.profiler.startSection("recheckGaps");

		if (world.isAreaLoaded(new BlockPos(x * 16 + 8, 0, z * 16 + 8), 16)) {
			for (int i = 0; i < 16; ++i) {
				for (int j = 0; j < 16; ++j) {
					if (updateSkylightColumns[i + j * 16]) {
						updateSkylightColumns[i + j * 16] = false;
						int k = getHeightValue(i, j);
						int l = x * 16 + i;
						int i1 = z * 16 + j;
						int j1 = Integer.MAX_VALUE;

						for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
							j1 = Math.min(j1, world.getChunksLowestHorizon(l + enumfacing.getFrontOffsetX(), i1 + enumfacing.getFrontOffsetZ()));
						}

						checkSkylightNeighborHeight(l, i1, j1);

						for (EnumFacing enumfacing1 : EnumFacing.Plane.HORIZONTAL) {
							checkSkylightNeighborHeight(l + enumfacing1.getFrontOffsetX(), i1 + enumfacing1.getFrontOffsetZ(), k);
						}

						if (onlyOne) {
							world.profiler.endSection();
							return;
						}
					}
				}
			}

			isGapLightingUpdated = false;
		}

		world.profiler.endSection();
	}

	/**
	 * Checks the height of a block next to a sky-visible block and schedules a lighting update as necessary.
	 */
	private void checkSkylightNeighborHeight(int x, int z, int maxValue) {

		int i = world.getHeight(new BlockPos(x, 0, z)).getY();

		if (i > maxValue) {
			updateSkylightNeighborHeight(x, z, maxValue, i + 1);
		} else if (i < maxValue) {
			updateSkylightNeighborHeight(x, z, i, maxValue + 1);
		}
	}

	private void updateSkylightNeighborHeight(int x, int z, int startY, int endY) {

		if (endY > startY && world.isAreaLoaded(new BlockPos(x, 0, z), 16)) {
			for (int i = startY; i < endY; ++i) {
				world.checkLightFor(EnumSkyBlock.SKY, new BlockPos(x, i, z));
			}

			dirty = true;
		}
	}

	/**
	 * Initiates the recalculation of both the block-light and sky-light for a given block inside a chunk.
	 */
	private void relightBlock(int x, int y, int z) {

		int i = heightMap[z << 4 | x] & 255;

		int j = Math.max(y, i);

		while (j > 0 && getBlockLightOpacity(x, j - 1, z) == 0) {
			--j;
		}

		if (j != i) {
			world.markBlocksDirtyVertical(x + this.x * 16, z + this.z * 16, j, i);
			heightMap[z << 4 | x] = j;
			int k = this.x * 16 + x;
			int l = this.z * 16 + z;

			if (world.provider.hasSkyLight()) {
				if (j < i) {
					for (int j1 = j; j1 < i; ++j1) {
						ExtendedBlockStorage extendedblockstorage2 = storageArrays[j1 >> 4];

						if (extendedblockstorage2 != NULL_BLOCK_STORAGE) {
							extendedblockstorage2.setSkyLight(x, j1 & 15, z, 15);
							world.notifyLightSet(new BlockPos((this.x << 4) + x, j1, (this.z << 4) + z));
						}
					}
				} else {
					for (int i1 = i; i1 < j; ++i1) {
						ExtendedBlockStorage extendedblockstorage = storageArrays[i1 >> 4];

						if (extendedblockstorage != NULL_BLOCK_STORAGE) {
							extendedblockstorage.setSkyLight(x, i1 & 15, z, 0);
							world.notifyLightSet(new BlockPos((this.x << 4) + x, i1, (this.z << 4) + z));
						}
					}
				}

				int k1 = 15;

				while (j > 0 && k1 > 0) {
					--j;
					int i2 = getBlockLightOpacity(x, j, z);

					if (i2 == 0) {
						i2 = 1;
					}

					k1 -= i2;

					if (k1 < 0) {
						k1 = 0;
					}

					ExtendedBlockStorage extendedblockstorage1 = storageArrays[j >> 4];

					if (extendedblockstorage1 != NULL_BLOCK_STORAGE) {
						extendedblockstorage1.setSkyLight(x, j & 15, z, k1);
					}
				}
			}

			int l1 = heightMap[z << 4 | x];
			int j2 = i;
			int k2 = l1;

			if (l1 < i) {
				j2 = l1;
				k2 = i;
			}

			if (l1 < heightMapMinimum) {
				heightMapMinimum = l1;
			}

			if (world.provider.hasSkyLight()) {
				for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
					updateSkylightNeighborHeight(k + enumfacing.getFrontOffsetX(), l + enumfacing.getFrontOffsetZ(), j2, k2);
				}

				updateSkylightNeighborHeight(k, l, j2, k2);
			}

			dirty = true;
		}
	}

	public int getBlockLightOpacity(BlockPos pos) {

		return getBlockState(pos).getLightOpacity();
	}

	private int getBlockLightOpacity(int x, int y, int z) {

		return getBlockState(x, y, z).getLightOpacity();
	}

	public IBlockState getBlockState(BlockPos pos) {

		return getBlockState(pos.getX(), pos.getY(), pos.getZ());
	}

	public IBlockState getBlockState(final int x, final int y, final int z) {

		if (world.getWorldType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
			IBlockState iblockstate = null;

			if (y == 60) {
				iblockstate = Blocks.BARRIER.getDefaultState();
			}

			if (y == 70) {
				iblockstate = ChunkGeneratorDebug.getBlockStateFor(x, z);
			}

			return iblockstate == null ? Blocks.AIR.getDefaultState() : iblockstate;
		} else {
			try {
				if (y >= 0 && y >> 4 < storageArrays.length) {
					ExtendedBlockStorage extendedblockstorage = storageArrays[y >> 4];

					if (extendedblockstorage != NULL_BLOCK_STORAGE) {
						return extendedblockstorage.get(x & 15, y & 15, z & 15);
					}
				}

				return Blocks.AIR.getDefaultState();
			} catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Getting block state");
				CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being got");
				crashreportcategory.addDetail("Location", () -> CrashReportCategory.getCoordinateInfo(x, y, z));
				throw new ReportedException(crashreport);
			}
		}
	}

	@Nullable
	public IBlockState setBlockState(BlockPos pos, IBlockState state) {

		int i = pos.getX() & 15;
		int j = pos.getY();
		int k = pos.getZ() & 15;
		int l = k << 4 | i;

		if (j >= precipitationHeightMap[l] - 1) {
			precipitationHeightMap[l] = -999;
		}

		int i1 = heightMap[l];
		IBlockState iblockstate = getBlockState(pos);

		if (iblockstate == state) {
			return null;
		} else {
			Block block = state.getBlock();
			Block block1 = iblockstate.getBlock();
			ExtendedBlockStorage extendedblockstorage = storageArrays[j >> 4];
			boolean flag = false;

			if (extendedblockstorage == NULL_BLOCK_STORAGE) {
				if (block == Blocks.AIR) {
					return null;
				}

				extendedblockstorage = new ExtendedBlockStorage(j >> 4 << 4, world.provider.hasSkyLight());
				storageArrays[j >> 4] = extendedblockstorage;
				flag = j >= i1;
			}

			extendedblockstorage.set(i, j & 15, k, state);

			if (block1 != block) {
				if (!world.isRemote) {
					block1.breakBlock(world, pos, iblockstate);
				} else if (block1 instanceof ITileEntityProvider) {
					world.removeTileEntity(pos);
				}
			}

			if (extendedblockstorage.get(i, j & 15, k).getBlock() != block) {
				return null;
			} else {
				if (flag) {
					generateSkylightMap();
				} else {
					int j1 = state.getLightOpacity();
					int k1 = iblockstate.getLightOpacity();

					if (j1 > 0) {
						if (j >= i1) {
							relightBlock(i, j + 1, k);
						}
					} else if (j == i1 - 1) {
						relightBlock(i, j, k);
					}

					if (j1 != k1 && (j1 < k1 || getLightFor(EnumSkyBlock.SKY, pos) > 0 || getLightFor(EnumSkyBlock.BLOCK, pos) > 0)) {
						propagateSkylightOcclusion(i, k);
					}
				}

				if (block1 instanceof ITileEntityProvider) {
					TileEntity tileentity = getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);

					if (tileentity != null) {
						tileentity.updateContainingBlockInfo();
					}
				}

				if (!world.isRemote && block1 != block) {
					block.onBlockAdded(world, pos, state);
				}

				if (block instanceof ITileEntityProvider) {
					TileEntity tileentity1 = getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);

					if (tileentity1 == null) {
						tileentity1 = ((ITileEntityProvider) block).createNewTileEntity(world, block.getMetaFromState(state));
						world.setTileEntity(pos, tileentity1);
					}

					if (tileentity1 != null) {
						tileentity1.updateContainingBlockInfo();
					}
				}

				dirty = true;
				return iblockstate;
			}
		}
	}

	public int getLightFor(EnumSkyBlock type, BlockPos pos) {

		int i = pos.getX() & 15;
		int j = pos.getY();
		int k = pos.getZ() & 15;
		ExtendedBlockStorage extendedblockstorage = storageArrays[j >> 4];

		if (extendedblockstorage == NULL_BLOCK_STORAGE) {
			return canSeeSky(pos) ? type.defaultLightValue : 0;
		} else if (type == EnumSkyBlock.SKY) {
			return !world.provider.hasSkyLight() ? 0 : extendedblockstorage.getSkyLight(i, j & 15, k);
		} else {
			return type == EnumSkyBlock.BLOCK ? extendedblockstorage.getBlockLight(i, j & 15, k) : type.defaultLightValue;
		}
	}

	public void setLightFor(EnumSkyBlock type, BlockPos pos, int value) {

		int i = pos.getX() & 15;
		int j = pos.getY();
		int k = pos.getZ() & 15;
		ExtendedBlockStorage extendedblockstorage = storageArrays[j >> 4];

		if (extendedblockstorage == NULL_BLOCK_STORAGE) {
			extendedblockstorage = new ExtendedBlockStorage(j >> 4 << 4, world.provider.hasSkyLight());
			storageArrays[j >> 4] = extendedblockstorage;
			generateSkylightMap();
		}

		dirty = true;

		if (type == EnumSkyBlock.SKY) {
			if (world.provider.hasSkyLight()) {
				extendedblockstorage.setSkyLight(i, j & 15, k, value);
			}
		} else if (type == EnumSkyBlock.BLOCK) {
			extendedblockstorage.setBlockLight(i, j & 15, k, value);
		}
	}

	public int getLightSubtracted(BlockPos pos, int amount) {

		int i = pos.getX() & 15;
		int j = pos.getY();
		int k = pos.getZ() & 15;
		ExtendedBlockStorage extendedblockstorage = storageArrays[j >> 4];

		if (extendedblockstorage == NULL_BLOCK_STORAGE) {
			return world.provider.hasSkyLight() && amount < EnumSkyBlock.SKY.defaultLightValue ? EnumSkyBlock.SKY.defaultLightValue - amount : 0;
		} else {
			int l = !world.provider.hasSkyLight() ? 0 : extendedblockstorage.getSkyLight(i, j & 15, k);
			l = l - amount;
			int i1 = extendedblockstorage.getBlockLight(i, j & 15, k);

			if (i1 > l) {
				l = i1;
			}

			return l;
		}
	}

	/**
	 * Adds an entity to the chunk.
	 */
	public void addEntity(Entity entityIn) {

		hasEntities = true;
		int i = MathHelper.floor(entityIn.posX / 16.0D);
		int j = MathHelper.floor(entityIn.posZ / 16.0D);

		if (i != x || j != z) {
			LOGGER.warn("Wrong location! ({}, {}) should be ({}, {}), {}", i, j, x, z, entityIn);
			entityIn.setDead();
		}

		int k = MathHelper.floor(entityIn.posY / 16.0D);

		if (k < 0) {
			k = 0;
		}

		if (k >= entityLists.length) {
			k = entityLists.length - 1;
		}

		entityIn.addedToChunk = true;
		entityIn.chunkCoordX = x;
		entityIn.chunkCoordY = k;
		entityIn.chunkCoordZ = z;
		entityLists[k].add(entityIn);
	}

	/**
	 * removes entity using its y chunk coordinate as its index
	 */
	public void removeEntity(Entity entityIn) {

		removeEntityAtIndex(entityIn, entityIn.chunkCoordY);
	}

	/**
	 * Removes entity at the specified index from the entity array.
	 */
	public void removeEntityAtIndex(Entity entityIn, int index) {

		if (index < 0) {
			index = 0;
		}

		if (index >= entityLists.length) {
			index = entityLists.length - 1;
		}

		entityLists[index].remove(entityIn);
	}

	public boolean canSeeSky(BlockPos pos) {

		int i = pos.getX() & 15;
		int j = pos.getY();
		int k = pos.getZ() & 15;
		return j >= heightMap[k << 4 | i];
	}

	@Nullable
	private TileEntity createNewTileEntity(BlockPos pos) {

		IBlockState iblockstate = getBlockState(pos);
		Block block = iblockstate.getBlock();
		return !block.hasTileEntity() ? null : ((ITileEntityProvider) block).createNewTileEntity(world, iblockstate.getBlock().getMetaFromState(iblockstate));
	}

	@Nullable
	public TileEntity getTileEntity(BlockPos pos, Chunk.EnumCreateEntityType creationMode) {

		TileEntity tileentity = tileEntities.get(pos);

		if (tileentity == null) {
			if (creationMode == Chunk.EnumCreateEntityType.IMMEDIATE) {
				tileentity = createNewTileEntity(pos);
				world.setTileEntity(pos, tileentity);
			} else if (creationMode == Chunk.EnumCreateEntityType.QUEUED) {
				tileEntityPosQueue.add(pos);
			}
		} else if (tileentity.isInvalid()) {
			tileEntities.remove(pos);
			return null;
		}

		return tileentity;
	}

	public void addTileEntity(TileEntity tileEntityIn) {

		addTileEntity(tileEntityIn.getPos(), tileEntityIn);

		if (loaded) {
			world.addTileEntity(tileEntityIn);
		}
	}

	public void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {

		tileEntityIn.setWorld(world);
		tileEntityIn.setPos(pos);

		if (getBlockState(pos).getBlock() instanceof ITileEntityProvider) {
			if (tileEntities.containsKey(pos)) {
				tileEntities.get(pos).invalidate();
			}

			tileEntityIn.validate();
			tileEntities.put(pos, tileEntityIn);
		}
	}

	public void removeTileEntity(BlockPos pos) {

		if (loaded) {
			TileEntity tileentity = tileEntities.remove(pos);

			if (tileentity != null) {
				tileentity.invalidate();
			}
		}
	}

	/**
	 * Called when this Chunk is loaded by the ChunkProvider
	 */
	public void onLoad() {

		loaded = true;
		world.addTileEntities(tileEntities.values());

		for (ClassInheritanceMultiMap<Entity> classinheritancemultimap : entityLists) {
			world.loadEntities(classinheritancemultimap);
		}
	}

	/**
	 * Called when this Chunk is unloaded by the ChunkProvider
	 */
	public void onUnload() {

		loaded = false;

		for (TileEntity tileentity : tileEntities.values()) {
			world.markTileEntityForRemoval(tileentity);
		}

		for (ClassInheritanceMultiMap<Entity> classinheritancemultimap : entityLists) {
			world.unloadEntities(classinheritancemultimap);
		}
	}

	/**
	 * Sets the isModified flag for this Chunk
	 */
	public void markDirty() {

		dirty = true;
	}

	/**
	 * Fills the given list of all entities that intersect within the given bounding box that aren't the passed entity.
	 */
	public void getEntitiesWithinAABBForEntity(@Nullable Entity entityIn, AxisAlignedBB aabb, List<Entity> listToFill, Predicate<? super Entity> filter) {

		int i = MathHelper.floor((aabb.minY - 2.0D) / 16.0D);
		int j = MathHelper.floor((aabb.maxY + 2.0D) / 16.0D);
		i = MathHelper.clamp(i, 0, entityLists.length - 1);
		j = MathHelper.clamp(j, 0, entityLists.length - 1);

		for (int k = i; k <= j; ++k) {
			if (!entityLists[k].isEmpty()) {
				for (Entity entity : entityLists[k]) {
					if (entity.getEntityBoundingBox().intersects(aabb) && entity != entityIn) {
						if (filter == null || filter.apply(entity)) {
							listToFill.add(entity);
						}

						Entity[] aentity = entity.getParts();

						if (aentity != null) {
							for (Entity entity1 : aentity) {
								if (entity1 != entityIn && entity1.getEntityBoundingBox().intersects(aabb) && (filter == null || filter.apply(entity1))) {
									listToFill.add(entity1);
								}
							}
						}
					}
				}
			}
		}
	}

	public <T extends Entity> void getEntitiesOfTypeWithinAABB(Class<? extends T> entityClass, AxisAlignedBB aabb, List<T> listToFill, Predicate<? super T> filter) {

		int i = MathHelper.floor((aabb.minY - 2.0D) / 16.0D);
		int j = MathHelper.floor((aabb.maxY + 2.0D) / 16.0D);
		i = MathHelper.clamp(i, 0, entityLists.length - 1);
		j = MathHelper.clamp(j, 0, entityLists.length - 1);

		for (int k = i; k <= j; ++k) {
			for (T t : entityLists[k].getByClass(entityClass)) {
				if (t.getEntityBoundingBox().intersects(aabb) && (filter == null || filter.apply(t))) {
					listToFill.add(t);
				}
			}
		}
	}

	/**
	 * Returns true if this Chunk needs to be saved
	 */
	public boolean needsSaving(boolean p_76601_1_) {

		if (p_76601_1_) {
			if (hasEntities && world.getTotalWorldTime() != lastSaveTime || dirty) {
				return true;
			}
		} else if (hasEntities && world.getTotalWorldTime() >= lastSaveTime + 600L) {
			return true;
		}

		return dirty;
	}

	public Random getRandomWithSeed(long seed) {

		return new Random(world.getSeed() + ((long) x * x * 4987142) + (x * 5947611L) + (long) z * z * 4392871L + (z * 389711L) ^ seed);
	}

	public boolean isEmpty() {

		return false;
	}

	public void populate(IChunkProvider chunkProvider, IChunkGenerator chunkGenrator) {

		Chunk chunk = chunkProvider.getLoadedChunk(x, z - 1);
		Chunk chunk1 = chunkProvider.getLoadedChunk(x + 1, z);
		Chunk chunk2 = chunkProvider.getLoadedChunk(x, z + 1);
		Chunk chunk3 = chunkProvider.getLoadedChunk(x - 1, z);

		if (chunk1 != null && chunk2 != null && chunkProvider.getLoadedChunk(x + 1, z + 1) != null) {
			populate(chunkGenrator);
		}

		if (chunk3 != null && chunk2 != null && chunkProvider.getLoadedChunk(x - 1, z + 1) != null) {
			chunk3.populate(chunkGenrator);
		}

		if (chunk != null && chunk1 != null && chunkProvider.getLoadedChunk(x + 1, z - 1) != null) {
			chunk.populate(chunkGenrator);
		}

		if (chunk != null && chunk3 != null) {
			Chunk chunk4 = chunkProvider.getLoadedChunk(x - 1, z - 1);

			if (chunk4 != null) {
				chunk4.populate(chunkGenrator);
			}
		}
	}

	protected void populate(IChunkGenerator generator) {

		if (isTerrainPopulated()) {
			if (generator.generateStructures(this, x, z)) {
				markDirty();
			}
		} else {
			checkLight();
			generator.populate(x, z);
			markDirty();
		}
	}

	public BlockPos getPrecipitationHeight(BlockPos pos) {

		int i = pos.getX() & 15;
		int j = pos.getZ() & 15;
		int k = i | j << 4;
		BlockPos blockpos = new BlockPos(pos.getX(), precipitationHeightMap[k], pos.getZ());

		if (blockpos.getY() == -999) {
			int l = getTopFilledSegment() + 15;
			blockpos = new BlockPos(pos.getX(), l, pos.getZ());
			int i1 = -1;

			while (blockpos.getY() > 0 && i1 == -1) {
				IBlockState iblockstate = getBlockState(blockpos);
				Material material = iblockstate.getMaterial();

				if (!material.blocksMovement() && !material.isLiquid()) {
					blockpos = blockpos.down();
				} else {
					i1 = blockpos.getY() + 1;
				}
			}

			precipitationHeightMap[k] = i1;
		}

		return new BlockPos(pos.getX(), precipitationHeightMap[k], pos.getZ());
	}

	public void onTick(boolean skipRecheckGaps) {

		if (isGapLightingUpdated && world.provider.hasSkyLight() && !skipRecheckGaps) {
			recheckGaps(world.isRemote);
		}

		ticked = true;

		if (!isLightPopulated && isTerrainPopulated) {
			checkLight();
		}

		while (!tileEntityPosQueue.isEmpty()) {
			BlockPos blockpos = tileEntityPosQueue.poll();

			if (getTileEntity(blockpos, Chunk.EnumCreateEntityType.CHECK) == null && getBlockState(blockpos).getBlock().hasTileEntity()) {
				TileEntity tileentity = createNewTileEntity(blockpos);
				world.setTileEntity(blockpos, tileentity);
				world.markBlockRangeForRenderUpdate(blockpos, blockpos);
			}
		}
	}

	public boolean isPopulated() {

		return ticked && isTerrainPopulated && isLightPopulated;
	}

	public boolean wasTicked() {

		return ticked;
	}

	/**
	 * Gets a {@link ChunkPos} representing the x and z coordinates of this chunk.
	 */
	public ChunkPos getPos() {

		return new ChunkPos(x, z);
	}

	/**
	 * Returns whether the ExtendedBlockStorages containing levels (in blocks) from arg 1 to arg 2 are fully empty
	 * (true) or not (false).
	 */
	public boolean isEmptyBetween(int startY, int endY) {

		if (startY < 0) {
			startY = 0;
		}

		if (endY >= 256) {
			endY = 255;
		}

		for (int i = startY; i <= endY; i += 16) {
			ExtendedBlockStorage extendedblockstorage = storageArrays[i >> 4];

			if (extendedblockstorage != NULL_BLOCK_STORAGE && !extendedblockstorage.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	public void setStorageArrays(ExtendedBlockStorage[] newStorageArrays) {

		if (storageArrays.length != newStorageArrays.length) {
			LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", newStorageArrays.length, storageArrays.length);
		} else {
			System.arraycopy(newStorageArrays, 0, storageArrays, 0, storageArrays.length);
		}
	}

	/**
	 * Loads this chunk from the given buffer.
	 *
	 * @see net.minecraft.network.play.server.SPacketChunkData#getReadBuffer()
	 */
	public void read(PacketBuffer buf, int availableSections, boolean groundUpContinuous) {

		boolean flag = world.provider.hasSkyLight();

		for (int i = 0; i < storageArrays.length; ++i) {
			ExtendedBlockStorage extendedblockstorage = storageArrays[i];

			if ((availableSections & 1 << i) == 0) {
				if (groundUpContinuous && extendedblockstorage != NULL_BLOCK_STORAGE) {
					storageArrays[i] = NULL_BLOCK_STORAGE;
				}
			} else {
				if (extendedblockstorage == NULL_BLOCK_STORAGE) {
					extendedblockstorage = new ExtendedBlockStorage(i << 4, flag);
					storageArrays[i] = extendedblockstorage;
				}

				extendedblockstorage.getData().read(buf);
				buf.readBytes(extendedblockstorage.getBlockLight().data());

				if (flag) {
					buf.readBytes(extendedblockstorage.getSkyLight().data());
				}
			}
		}

		if (groundUpContinuous) {
			buf.readBytes(blockBiomeArray);
		}

		for (int j = 0; j < storageArrays.length; ++j) {
			if (storageArrays[j] != NULL_BLOCK_STORAGE && (availableSections & 1 << j) != 0) {
				storageArrays[j].recalculateRefCounts();
			}
		}

		isLightPopulated = true;
		isTerrainPopulated = true;
		generateHeightMap();

		for (TileEntity tileentity : tileEntities.values()) {
			tileentity.updateContainingBlockInfo();
		}
	}

	public Biome getBiome(BlockPos pos, BiomeProvider provider) {

		int i = pos.getX() & 15;
		int j = pos.getZ() & 15;
		int k = blockBiomeArray[j << 4 | i] & 255;

		if (k == 255) {
			Biome biome = provider.getBiome(pos, Biomes.PLAINS);
			k = Biome.getIdForBiome(biome);
			blockBiomeArray[j << 4 | i] = (byte) (k & 255);
		}

		Biome biome1 = Biome.getBiome(k);
		return biome1 == null ? Biomes.PLAINS : biome1;
	}

	/**
	 * Returns an array containing a 16x16 mapping on the X/Z of block positions in this Chunk to biome IDs.
	 */
	public byte[] getBiomeArray() {

		return blockBiomeArray;
	}

	/**
	 * Accepts a 256-entry array that contains a 16x16 mapping on the X/Z plane of block positions in this Chunk to
	 * biome IDs.
	 */
	public void setBiomeArray(byte[] biomeArray) {

		if (blockBiomeArray.length != biomeArray.length) {
			LOGGER.warn("Could not set level chunk biomes, array length is {} instead of {}", biomeArray.length, blockBiomeArray.length);
		} else {
			System.arraycopy(biomeArray, 0, blockBiomeArray, 0, blockBiomeArray.length);
		}
	}

	/**
	 * Resets the relight check index to 0 for this Chunk.
	 */
	public void resetRelightChecks() {

		queuedLightChecks = 0;
	}

	/**
	 * Called once-per-chunk-per-tick, and advances the round-robin relight check index by up to 8 blocks at a time. In
	 * a worst-case scenario, can potentially take up to 25.6 seconds, calculated via (4096/8)/20, to re-check all
	 * blocks in a chunk, which may explain lagging light updates on initial world generation.
	 */
	public void enqueueRelightChecks() {

		if (queuedLightChecks < 4096) {
			BlockPos blockpos = new BlockPos(x << 4, 0, z << 4);

			for (int i = 0; i < 8; ++i) {
				if (queuedLightChecks >= 4096) {
					return;
				}

				int j = queuedLightChecks % 16;
				int k = queuedLightChecks / 16 % 16;
				int l = queuedLightChecks / 256;
				++queuedLightChecks;

				for (int i1 = 0; i1 < 16; ++i1) {
					BlockPos blockpos1 = blockpos.add(k, (j << 4) + i1, l);
					boolean flag = i1 == 0 || i1 == 15 || k == 0 || k == 15 || l == 0 || l == 15;

					if (storageArrays[j] == NULL_BLOCK_STORAGE && flag || storageArrays[j] != NULL_BLOCK_STORAGE && storageArrays[j].get(k, i1, l).getMaterial() == Material.AIR) {
						for (EnumFacing enumfacing : EnumFacing.values()) {
							BlockPos blockpos2 = blockpos1.offset(enumfacing);

							if (world.getBlockState(blockpos2).getLightValue() > 0) {
								world.checkLight(blockpos2);
							}
						}

						world.checkLight(blockpos1);
					}
				}
			}
		}
	}

	public void checkLight() {

		isTerrainPopulated = true;
		isLightPopulated = true;
		BlockPos blockpos = new BlockPos(x << 4, 0, z << 4);

		if (world.provider.hasSkyLight()) {
			if (world.isAreaLoaded(blockpos.add(-1, 0, -1), blockpos.add(16, world.getSeaLevel(), 16))) {
				label44:

				for (int i = 0; i < 16; ++i) {
					for (int j = 0; j < 16; ++j) {
						if (!checkLight(i, j)) {
							isLightPopulated = false;
							break label44;
						}
					}
				}

				if (isLightPopulated) {
					for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
						int k = enumfacing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 16 : 1;
						world.getChunkFromBlockCoords(blockpos.offset(enumfacing, k)).checkLightSide(enumfacing.getOpposite());
					}

					setSkylightUpdated();
				}
			} else {
				isLightPopulated = false;
			}
		}
	}

	private void setSkylightUpdated() {

		Arrays.fill(updateSkylightColumns, true);

		recheckGaps(false);
	}

	private void checkLightSide(EnumFacing facing) {

		if (isTerrainPopulated) {
			if (facing == EnumFacing.EAST) {
				for (int i = 0; i < 16; ++i) {
					checkLight(15, i);
				}
			} else if (facing == EnumFacing.WEST) {
				for (int j = 0; j < 16; ++j) {
					checkLight(0, j);
				}
			} else if (facing == EnumFacing.SOUTH) {
				for (int k = 0; k < 16; ++k) {
					checkLight(k, 15);
				}
			} else if (facing == EnumFacing.NORTH) {
				for (int l = 0; l < 16; ++l) {
					checkLight(l, 0);
				}
			}
		}
	}

	private boolean checkLight(int x, int z) {

		int i = getTopFilledSegment();
		boolean flag = false;
		boolean flag1 = false;
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos((this.x << 4) + x, 0, (this.z << 4) + z);

		for (int j = i + 16 - 1; j > world.getSeaLevel() || j > 0 && !flag1; --j) {
			blockpos$mutableblockpos.setPos(blockpos$mutableblockpos.getX(), j, blockpos$mutableblockpos.getZ());
			int k = getBlockLightOpacity(blockpos$mutableblockpos);

			if (k == 255 && blockpos$mutableblockpos.getY() < world.getSeaLevel()) {
				flag1 = true;
			}

			if (!flag && k > 0) {
				flag = true;
			} else if (flag && k == 0 && !world.checkLight(blockpos$mutableblockpos)) {
				return false;
			}
		}

		for (int l = blockpos$mutableblockpos.getY(); l > 0; --l) {
			blockpos$mutableblockpos.setPos(blockpos$mutableblockpos.getX(), l, blockpos$mutableblockpos.getZ());

			if (getBlockState(blockpos$mutableblockpos).getLightValue() > 0) {
				world.checkLight(blockpos$mutableblockpos);
			}
		}

		return true;
	}

	public boolean isLoaded() {

		return loaded;
	}

	public void markLoaded(boolean loaded) {

		this.loaded = loaded;
	}

	public World getWorld() {

		return world;
	}

	public int[] getHeightMap() {

		return heightMap;
	}

	public void setHeightMap(int[] newHeightMap) {

		if (heightMap.length != newHeightMap.length) {
			LOGGER.warn("Could not set level chunk heightmap, array length is {} instead of {}", newHeightMap.length, heightMap.length);
		} else {
			System.arraycopy(newHeightMap, 0, heightMap, 0, heightMap.length);
		}
	}

	public Map<BlockPos, TileEntity> getTileEntityMap() {

		return tileEntities;
	}

	public ClassInheritanceMultiMap<Entity>[] getEntityLists() {

		return entityLists;
	}

	public boolean isTerrainPopulated() {

		return isTerrainPopulated;
	}

	public void setTerrainPopulated(boolean terrainPopulated) {

		isTerrainPopulated = terrainPopulated;
	}

	public boolean isLightPopulated() {

		return isLightPopulated;
	}

	public void setLightPopulated(boolean lightPopulated) {

		isLightPopulated = lightPopulated;
	}

	public void setModified(boolean modified) {

		dirty = modified;
	}

	public void setHasEntities(boolean hasEntitiesIn) {

		hasEntities = hasEntitiesIn;
	}

	public void setLastSaveTime(long saveTime) {

		lastSaveTime = saveTime;
	}

	public int getLowestHeight() {

		return heightMapMinimum;
	}

	public long getInhabitedTime() {

		return inhabitedTime;
	}

	public void setInhabitedTime(long newInhabitedTime) {

		inhabitedTime = newInhabitedTime;
	}

	public enum EnumCreateEntityType {
		IMMEDIATE,
		QUEUED,
		CHECK
	}

}
