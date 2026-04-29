package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.Random;

public class EntityAIFleeSun extends EntityAIBase {

	private final EntityCreature creature;
	private final double movementSpeed;
	private final World world;
	private double shelterX;
	private double shelterY;
	private double shelterZ;

	public EntityAIFleeSun(EntityCreature theCreatureIn, double movementSpeedIn) {

		creature = theCreatureIn;
		movementSpeed = movementSpeedIn;
		world = theCreatureIn.world;
		setMutexBits(1);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {

		if (!world.isDaytime()) {
			return false;
		} else if (!creature.isBurning()) {
			return false;
		} else if (!world.canSeeSky(new BlockPos(creature.posX, creature.getEntityBoundingBox().minY, creature.posZ))) {
			return false;
		} else if (!creature.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()) {
			return false;
		} else {
			Vec3d vec3d = findPossibleShelter();

			if (vec3d == null) {
				return false;
			} else {
				shelterX = vec3d.x();
				shelterY = vec3d.y();
				shelterZ = vec3d.z();
				return true;
			}
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {

		return !creature.getNavigator().noPath();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {

		creature.getNavigator().tryMoveToXYZ(shelterX, shelterY, shelterZ, movementSpeed);
	}

	
	private Vec3d findPossibleShelter() {

		Random random = creature.getRNG();
		BlockPos blockpos = new BlockPos(creature.posX, creature.getEntityBoundingBox().minY, creature.posZ);

		for (int i = 0; i < 10; ++i) {
			BlockPos blockpos1 = blockpos.add(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);

			if (!world.canSeeSky(blockpos1) && creature.getBlockPathWeight(blockpos1) < 0.0F) {
				return new Vec3d(blockpos1.getX(), blockpos1.getY(), blockpos1.getZ());
			}
		}

		return null;
	}

}
