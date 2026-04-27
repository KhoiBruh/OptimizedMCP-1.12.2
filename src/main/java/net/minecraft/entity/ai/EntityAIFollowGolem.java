package net.minecraft.entity.ai;

import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityVillager;

import java.util.List;

public class EntityAIFollowGolem extends EntityAIBase {

	private final EntityVillager villager;
	private EntityIronGolem ironGolem;
	private int takeGolemRoseTick;
	private boolean tookGolemRose;

	public EntityAIFollowGolem(EntityVillager villagerIn) {

		villager = villagerIn;
		setMutexBits(3);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {

		if (villager.getGrowingAge() >= 0) {
			return false;
		} else if (!villager.world.isDaytime()) {
			return false;
		} else {
			List<EntityIronGolem> list = villager.world.getEntitiesWithinAABB(EntityIronGolem.class, villager.getEntityBoundingBox().grow(6.0D, 2.0D, 6.0D));

			if (list.isEmpty()) {
				return false;
			} else {
				for (EntityIronGolem entityirongolem : list) {
					if (entityirongolem.getHoldRoseTick() > 0) {
						ironGolem = entityirongolem;
						break;
					}
				}

				return ironGolem != null;
			}
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {

		return ironGolem.getHoldRoseTick() > 0;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {

		takeGolemRoseTick = villager.getRNG().nextInt(320);
		tookGolemRose = false;
		ironGolem.getNavigator().clearPath();
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {

		ironGolem = null;
		villager.getNavigator().clearPath();
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void updateTask() {

		villager.getLookHelper().setLookPositionWithEntity(ironGolem, 30.0F, 30.0F);

		if (ironGolem.getHoldRoseTick() == takeGolemRoseTick) {
			villager.getNavigator().tryMoveToEntityLiving(ironGolem, 0.5D);
			tookGolemRose = true;
		}

		if (tookGolemRose && villager.getDistanceSq(ironGolem) < 4.0D) {
			ironGolem.setHoldingRose(false);
			villager.getNavigator().clearPath();
		}
	}

}
