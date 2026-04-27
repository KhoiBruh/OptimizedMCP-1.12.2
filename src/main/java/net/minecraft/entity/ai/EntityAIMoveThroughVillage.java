package net.minecraft.entity.ai;

import com.google.common.collect.Lists;
import net.minecraft.entity.EntityCreature;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;

import java.util.List;

public class EntityAIMoveThroughVillage extends EntityAIBase {

	private final EntityCreature entity;
	private final double movementSpeed;

	/**
	 * The PathNavigate of our entity.
	 */
	private Path path;
	private VillageDoorInfo doorInfo;
	private final boolean isNocturnal;
	private final List<VillageDoorInfo> doorList = Lists.newArrayList();

	public EntityAIMoveThroughVillage(EntityCreature entityIn, double movementSpeedIn, boolean isNocturnalIn) {

		entity = entityIn;
		movementSpeed = movementSpeedIn;
		isNocturnal = isNocturnalIn;
		setMutexBits(1);

		if (!(entityIn.getNavigator() instanceof PathNavigateGround)) {
			throw new IllegalArgumentException("Unsupported mob for MoveThroughVillageGoal");
		}
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {

		resizeDoorList();

		if (isNocturnal && entity.world.isDaytime()) {
			return false;
		} else {
			Village village = entity.world.getVillageCollection().getNearestVillage(new BlockPos(entity), 0);

			if (village == null) {
				return false;
			} else {
				doorInfo = findNearestDoor(village);

				if (doorInfo == null) {
					return false;
				} else {
					PathNavigateGround pathnavigateground = (PathNavigateGround) entity.getNavigator();
					boolean flag = pathnavigateground.getEnterDoors();
					pathnavigateground.setBreakDoors(false);
					path = pathnavigateground.getPathToPos(doorInfo.getDoorBlockPos());
					pathnavigateground.setBreakDoors(flag);

					if (path != null) {
						return true;
					} else {
						Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(entity, 10, 7, new Vec3d(doorInfo.getDoorBlockPos().getX(), doorInfo.getDoorBlockPos().getY(), doorInfo.getDoorBlockPos().getZ()));

						if (vec3d == null) {
							return false;
						} else {
							pathnavigateground.setBreakDoors(false);
							path = entity.getNavigator().getPathToXYZ(vec3d.x(), vec3d.y(), vec3d.z());
							pathnavigateground.setBreakDoors(flag);
							return path != null;
						}
					}
				}
			}
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {

		if (entity.getNavigator().noPath()) {
			return false;
		} else {
			float f = entity.width + 4.0F;
			return entity.getDistanceSq(doorInfo.getDoorBlockPos()) > (double) (f * f);
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {

		entity.getNavigator().setPath(path, movementSpeed);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {

		if (entity.getNavigator().noPath() || entity.getDistanceSq(doorInfo.getDoorBlockPos()) < 16.0D) {
			doorList.add(doorInfo);
		}
	}

	private VillageDoorInfo findNearestDoor(Village villageIn) {

		VillageDoorInfo villagedoorinfo = null;
		int i = Integer.MAX_VALUE;

		for (VillageDoorInfo villagedoorinfo1 : villageIn.getVillageDoorInfoList()) {
			int j = villagedoorinfo1.getDistanceSquared(MathHelper.floor(entity.posX), MathHelper.floor(entity.posY), MathHelper.floor(entity.posZ));

			if (j < i && !doesDoorListContain(villagedoorinfo1)) {
				villagedoorinfo = villagedoorinfo1;
				i = j;
			}
		}

		return villagedoorinfo;
	}

	private boolean doesDoorListContain(VillageDoorInfo doorInfoIn) {

		for (VillageDoorInfo villagedoorinfo : doorList) {
			if (doorInfoIn.getDoorBlockPos().equals(villagedoorinfo.getDoorBlockPos())) {
				return true;
			}
		}

		return false;
	}

	private void resizeDoorList() {

		if (doorList.size() > 15) {
			doorList.remove(0);
		}
	}

}
