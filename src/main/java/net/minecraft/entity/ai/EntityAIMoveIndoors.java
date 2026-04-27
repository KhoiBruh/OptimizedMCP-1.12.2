package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;

public class EntityAIMoveIndoors extends EntityAIBase {

	private final EntityCreature entity;
	private VillageDoorInfo doorInfo;
	private int insidePosX = -1;
	private int insidePosZ = -1;

	public EntityAIMoveIndoors(EntityCreature entityIn) {

		entity = entityIn;
		setMutexBits(1);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {

		BlockPos blockpos = new BlockPos(entity);

		if ((!entity.world.isDaytime() || entity.world.isRaining() && !entity.world.getBiome(blockpos).canRain()) && entity.world.provider.hasSkyLight()) {
			if (entity.getRNG().nextInt(50) != 0) {
				return false;
			} else if (insidePosX != -1 && entity.getDistanceSq(insidePosX, entity.posY, insidePosZ) < 4.0D) {
				return false;
			} else {
				Village village = entity.world.getVillageCollection().getNearestVillage(blockpos, 14);

				if (village == null) {
					return false;
				} else {
					doorInfo = village.getDoorInfo(blockpos);
					return doorInfo != null;
				}
			}
		} else {
			return false;
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {

		return !entity.getNavigator().noPath();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {

		insidePosX = -1;
		BlockPos blockpos = doorInfo.getInsideBlockPos();
		int i = blockpos.getX();
		int j = blockpos.getY();
		int k = blockpos.getZ();

		if (entity.getDistanceSq(blockpos) > 256.0D) {
			Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(entity, 14, 3, new Vec3d((double) i + 0.5D, j, (double) k + 0.5D));

			if (vec3d != null) {
				entity.getNavigator().tryMoveToXYZ(vec3d.x(), vec3d.y(), vec3d.z(), 1.0D);
			}
		} else {
			entity.getNavigator().tryMoveToXYZ((double) i + 0.5D, j, (double) k + 0.5D, 1.0D);
		}
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {

		insidePosX = doorInfo.getInsideBlockPos().getX();
		insidePosZ = doorInfo.getInsideBlockPos().getZ();
		doorInfo = null;
	}

}
