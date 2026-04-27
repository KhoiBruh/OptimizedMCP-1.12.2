package net.minecraft.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class TileEntityMobSpawner extends TileEntity implements ITickable {

	private final MobSpawnerBaseLogic spawnerLogic = new MobSpawnerBaseLogic() {
		public void broadcastEvent(int id) {

			world.addBlockEvent(pos, Blocks.MOB_SPAWNER, id, 0);
		}

		public World getSpawnerWorld() {

			return world;
		}

		public BlockPos getSpawnerPosition() {

			return pos;
		}

		public void setNextSpawnData(WeightedSpawnerEntity p_184993_1_) {

			super.setNextSpawnData(p_184993_1_);

			if (getSpawnerWorld() != null) {
				IBlockState iblockstate = getSpawnerWorld().getBlockState(getSpawnerPosition());
				getSpawnerWorld().notifyBlockUpdate(pos, iblockstate, iblockstate, 4);
			}
		}
	};

	public static void registerFixesMobSpawner(DataFixer fixer) {

		fixer.registerWalker(FixTypes.BLOCK_ENTITY, (fixer1, compound, versionIn) -> {

			if (TileEntity.getKey(TileEntityMobSpawner.class).equals(new ResourceLocation(compound.getString("id")))) {
				if (compound.hasKey("SpawnPotentials", 9)) {
					NBTTagList nbttaglist = compound.getTagList("SpawnPotentials", 10);

					for (int i = 0; i < nbttaglist.tagCount(); ++i) {
						NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
						nbttagcompound.setTag("Entity", fixer1.process(FixTypes.ENTITY, nbttagcompound.getCompoundTag("Entity"), versionIn));
					}
				}

				compound.setTag("SpawnData", fixer1.process(FixTypes.ENTITY, compound.getCompoundTag("SpawnData"), versionIn));
			}

			return compound;
		});
	}

	public void readFromNBT(NBTTagCompound compound) {

		super.readFromNBT(compound);
		spawnerLogic.readFromNBT(compound);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {

		super.writeToNBT(compound);
		spawnerLogic.writeToNBT(compound);
		return compound;
	}

	/**
	 * Like the old updateEntity(), except more generic.
	 */
	public void update() {

		spawnerLogic.updateSpawner();
	}

	@Nullable
	public SPacketUpdateTileEntity getUpdatePacket() {

		return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
	}

	public NBTTagCompound getUpdateTag() {

		NBTTagCompound nbttagcompound = writeToNBT(new NBTTagCompound());
		nbttagcompound.removeTag("SpawnPotentials");
		return nbttagcompound;
	}

	public boolean receiveClientEvent(int id, int type) {

		return spawnerLogic.setDelayToMin(id) || super.receiveClientEvent(id, type);
	}

	public boolean onlyOpsCanSetNbt() {

		return true;
	}

	public MobSpawnerBaseLogic getSpawnerBaseLogic() {

		return spawnerLogic;
	}

}
