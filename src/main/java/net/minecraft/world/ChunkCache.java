package net.minecraft.world;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public class ChunkCache implements IBlockAccess {

	protected int chunkX;
	protected int chunkZ;
	protected Chunk[][] chunkArray;

	/**
	 * set by !chunk.getAreLevelsEmpty
	 */
	protected boolean empty;

	/**
	 * Reference to the World object.
	 */
	protected World world;

	public ChunkCache(World worldIn, BlockPos posFromIn, BlockPos posToIn, int subIn) {

		world = worldIn;
		chunkX = posFromIn.getX() - subIn >> 4;
		chunkZ = posFromIn.getZ() - subIn >> 4;
		int i = posToIn.getX() + subIn >> 4;
		int j = posToIn.getZ() + subIn >> 4;
		chunkArray = new Chunk[i - chunkX + 1][j - chunkZ + 1];
		empty = true;

		for (int k = chunkX; k <= i; ++k) {
			for (int l = chunkZ; l <= j; ++l) {
				chunkArray[k - chunkX][l - chunkZ] = worldIn.getChunkFromChunkCoords(k, l);
			}
		}

		for (int i1 = posFromIn.getX() >> 4; i1 <= posToIn.getX() >> 4; ++i1) {
			for (int j1 = posFromIn.getZ() >> 4; j1 <= posToIn.getZ() >> 4; ++j1) {
				Chunk chunk = chunkArray[i1 - chunkX][j1 - chunkZ];

				if (chunk != null && !chunk.isEmptyBetween(posFromIn.getY(), posToIn.getY())) {
					empty = false;
				}
			}
		}
	}

	/**
	 * set by !chunk.getAreLevelsEmpty
	 */
	public boolean isEmpty() {

		return empty;
	}

	
	public TileEntity getTileEntity(BlockPos pos) {

		return getTileEntity(pos, Chunk.CreateEntityType.IMMEDIATE);
	}

	
	public TileEntity getTileEntity(BlockPos pos, Chunk.CreateEntityType p_190300_2_) {

		int i = (pos.getX() >> 4) - chunkX;
		int j = (pos.getZ() >> 4) - chunkZ;
		return chunkArray[i][j].getTileEntity(pos, p_190300_2_);
	}

	public int getCombinedLight(BlockPos pos, int lightValue) {

		int i = getLightForExt(SkyBlock.SKY, pos);
		int j = getLightForExt(SkyBlock.BLOCK, pos);

		if (j < lightValue) {
			j = lightValue;
		}

		return i << 20 | j << 4;
	}

	public IBlockState getBlockState(BlockPos pos) {

		if (pos.getY() >= 0 && pos.getY() < 256) {
			int i = (pos.getX() >> 4) - chunkX;
			int j = (pos.getZ() >> 4) - chunkZ;

			if (i >= 0 && i < chunkArray.length && j >= 0 && j < chunkArray[i].length) {
				Chunk chunk = chunkArray[i][j];

				if (chunk != null) {
					return chunk.getBlockState(pos);
				}
			}
		}

		return Blocks.AIR.getDefaultState();
	}

	public Biome getBiome(BlockPos pos) {

		int i = (pos.getX() >> 4) - chunkX;
		int j = (pos.getZ() >> 4) - chunkZ;
		return chunkArray[i][j].getBiome(pos, world.getBiomeProvider());
	}

	private int getLightForExt(SkyBlock type, BlockPos pos) {

		if (type == SkyBlock.SKY && !world.provider.hasSkyLight()) {
			return 0;
		} else if (pos.getY() >= 0 && pos.getY() < 256) {
			if (getBlockState(pos).useNeighborBrightness()) {
				int l = 0;

				for (Facing enumfacing : Facing.values()) {
					int k = getLightFor(type, pos.offset(enumfacing));

					if (k > l) {
						l = k;
					}

					if (l >= 15) {
						return l;
					}
				}

				return l;
			} else {
				int i = (pos.getX() >> 4) - chunkX;
				int j = (pos.getZ() >> 4) - chunkZ;
				return chunkArray[i][j].getLightFor(type, pos);
			}
		} else {
			return type.defaultLightValue;
		}
	}

	/**
	 * Checks to see if an air block exists at the provided location. Note that this only checks to see if the blocks
	 * material is set to air, meaning it is possible for non-vanilla blocks to still pass this check.
	 */
	public boolean isAirBlock(BlockPos pos) {

		return getBlockState(pos).getMaterial() == Material.AIR;
	}

	public int getLightFor(SkyBlock type, BlockPos pos) {

		if (pos.getY() >= 0 && pos.getY() < 256) {
			int i = (pos.getX() >> 4) - chunkX;
			int j = (pos.getZ() >> 4) - chunkZ;
			return chunkArray[i][j].getLightFor(type, pos);
		} else {
			return type.defaultLightValue;
		}
	}

	public int getStrongPower(BlockPos pos, Facing direction) {

		return getBlockState(pos).getStrongPower(this, pos, direction);
	}

	public WorldType getWorldType() {

		return world.getWorldType();
	}

}
