package net.minecraft.world.gen.structure;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import java.util.Iterator;
import java.util.Random;

public abstract class MapGenStructure extends MapGenBase {

	protected Long2ObjectMap<StructureStart> structureMap = new Long2ObjectOpenHashMap<>(1024);
	private MapGenStructureData structureData;

	protected static BlockPos findNearestStructurePosBySpacing(World worldIn, MapGenStructure p_191069_1_, BlockPos p_191069_2_, int p_191069_3_, int p_191069_4_, int p_191069_5_, boolean p_191069_6_, int p_191069_7_, boolean findUnexplored) {

		int i = p_191069_2_.getX() >> 4;
		int j = p_191069_2_.getZ() >> 4;
		int k = 0;

		for (Random random = new Random(); k <= p_191069_7_; ++k) {
			for (int l = -k; l <= k; ++l) {
				boolean flag = l == -k || l == k;

				for (int i1 = -k; i1 <= k; ++i1) {
					boolean flag1 = i1 == -k || i1 == k;

					if (flag || flag1) {
						int j1 = i + p_191069_3_ * l;
						int k1 = j + p_191069_3_ * i1;

						if (j1 < 0) {
							j1 -= p_191069_3_ - 1;
						}

						if (k1 < 0) {
							k1 -= p_191069_3_ - 1;
						}

						int l1 = j1 / p_191069_3_;
						int i2 = k1 / p_191069_3_;
						Random random1 = worldIn.setRandomSeed(l1, i2, p_191069_5_);
						l1 = l1 * p_191069_3_;
						i2 = i2 * p_191069_3_;

						if (p_191069_6_) {
							l1 = l1 + (random1.nextInt(p_191069_3_ - p_191069_4_) + random1.nextInt(p_191069_3_ - p_191069_4_)) / 2;
							i2 = i2 + (random1.nextInt(p_191069_3_ - p_191069_4_) + random1.nextInt(p_191069_3_ - p_191069_4_)) / 2;
						} else {
							l1 = l1 + random1.nextInt(p_191069_3_ - p_191069_4_);
							i2 = i2 + random1.nextInt(p_191069_3_ - p_191069_4_);
						}

						MapGenBase.setupChunkSeed(worldIn.getSeed(), random, l1, i2);
						random.nextInt();

						if (p_191069_1_.canSpawnStructureAtCoords(l1, i2)) {
							if (!findUnexplored || !worldIn.isChunkGeneratedAt(l1, i2)) {
								return new BlockPos((l1 << 4) + 8, 64, (i2 << 4) + 8);
							}
						} else if (k == 0) {
							break;
						}
					}
				}

				if (k == 0) {
					break;
				}
			}
		}

		return null;
	}

	public abstract String getStructureName();

	/**
	 * Recursively called by generate()
	 */
	protected final synchronized void recursiveGenerate(World worldIn, final int chunkX, final int chunkZ, int originalX, int originalZ, ChunkPrimer chunkPrimerIn) {

		initializeStructureData(worldIn);

		if (!structureMap.containsKey(ChunkPos.asLong(chunkX, chunkZ))) {
			rand.nextInt();

			try {
				if (canSpawnStructureAtCoords(chunkX, chunkZ)) {
					StructureStart structurestart = getStructureStart(chunkX, chunkZ);
					structureMap.put(ChunkPos.asLong(chunkX, chunkZ), structurestart);

					if (structurestart.isSizeableStructure()) {
						setStructureStart(chunkX, chunkZ, structurestart);
					}
				}
			} catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception preparing structure feature");
				CrashReportCategory crashreportcategory = crashreport.makeCategory("Feature being prepared");
				crashreportcategory.addDetail("Is feature chunk", () -> canSpawnStructureAtCoords(chunkX, chunkZ) ? "True" : "False");
				crashreportcategory.addCrashSection("Chunk location", String.format("%d,%d", chunkX, chunkZ));
				crashreportcategory.addDetail("Chunk pos hash", () -> String.valueOf(ChunkPos.asLong(chunkX, chunkZ)));
				crashreportcategory.addDetail("Structure type", () -> getClass().getCanonicalName());
				throw new ReportedException(crashreport);
			}
		}
	}

	public synchronized boolean generateStructure(World worldIn, Random randomIn, ChunkPos chunkCoord) {

		initializeStructureData(worldIn);
		int i = (chunkCoord.x << 4) + 8;
		int j = (chunkCoord.z << 4) + 8;
		boolean flag = false;

		for (StructureStart structurestart : structureMap.values()) {
			if (structurestart.isSizeableStructure() && structurestart.isValidForPostProcess(chunkCoord) && structurestart.getBoundingBox().intersectsWith(i, j, i + 15, j + 15)) {
				structurestart.generateStructure(worldIn, randomIn, new StructureBoundingBox(i, j, i + 15, j + 15));
				structurestart.notifyPostProcessAt(chunkCoord);
				flag = true;
				setStructureStart(structurestart.getChunkPosX(), structurestart.getChunkPosZ(), structurestart);
			}
		}

		return flag;
	}

	public boolean isInsideStructure(BlockPos pos) {

		if (world == null) {
			return false;
		} else {
			initializeStructureData(world);
			return getStructureAt(pos) != null;
		}
	}

	
	protected StructureStart getStructureAt(BlockPos pos) {

		ObjectIterator<StructureStart> objectiterator = structureMap.values().iterator();
		label31:

		while (objectiterator.hasNext()) {
			StructureStart structurestart = (StructureStart) objectiterator.next();

			if (structurestart.isSizeableStructure() && structurestart.getBoundingBox().isVecInside(pos)) {
				Iterator<StructureComponent> iterator = structurestart.getComponents().iterator();

				while (true) {
					if (!iterator.hasNext()) {
						continue label31;
					}

					StructureComponent structurecomponent = iterator.next();

					if (structurecomponent.getBoundingBox().isVecInside(pos)) {
						break;
					}
				}

				return structurestart;
			}
		}

		return null;
	}

	public boolean isPositionInStructure(World worldIn, BlockPos pos) {

		initializeStructureData(worldIn);

		for (StructureStart structurestart : structureMap.values()) {
			if (structurestart.isSizeableStructure() && structurestart.getBoundingBox().isVecInside(pos)) {
				return true;
			}
		}

		return false;
	}

	
	public abstract BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored);

	protected void initializeStructureData(World worldIn) {

		if (structureData == null && worldIn != null) {
			structureData = (MapGenStructureData) worldIn.loadData(MapGenStructureData.class, getStructureName());

			if (structureData == null) {
				structureData = new MapGenStructureData(getStructureName());
				worldIn.setData(getStructureName(), structureData);
			} else {
				NBTTagCompound nbttagcompound = structureData.getTagCompound();

				for (String s : nbttagcompound.getKeySet()) {
					NBTBase nbtbase = nbttagcompound.getTag(s);

					if (nbtbase.getId() == 10) {
						NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbtbase;

						if (nbttagcompound1.hasKey("ChunkX") && nbttagcompound1.hasKey("ChunkZ")) {
							int i = nbttagcompound1.getInteger("ChunkX");
							int j = nbttagcompound1.getInteger("ChunkZ");
							StructureStart structurestart = MapGenStructureIO.getStructureStart(nbttagcompound1, worldIn);

							if (structurestart != null) {
								structureMap.put(ChunkPos.asLong(i, j), structurestart);
							}
						}
					}
				}
			}
		}
	}

	private void setStructureStart(int chunkX, int chunkZ, StructureStart start) {

		structureData.writeInstance(start.writeStructureComponentsToNBT(chunkX, chunkZ), chunkX, chunkZ);
		structureData.markDirty();
	}

	protected abstract boolean canSpawnStructureAtCoords(int chunkX, int chunkZ);

	protected abstract StructureStart getStructureStart(int chunkX, int chunkZ);

}
