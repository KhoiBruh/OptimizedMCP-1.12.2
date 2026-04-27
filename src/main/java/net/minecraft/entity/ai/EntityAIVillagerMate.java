package net.minecraft.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.world.World;

public class EntityAIVillagerMate extends EntityAIBase {

	private final EntityVillager villager;
	private EntityVillager mate;
	private final World world;
	private int matingTimeout;
	Village village;

	public EntityAIVillagerMate(EntityVillager villagerIn) {

		villager = villagerIn;
		world = villagerIn.world;
		setMutexBits(3);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {

		if (villager.getGrowingAge() != 0) {
			return false;
		} else if (villager.getRNG().nextInt(500) != 0) {
			return false;
		} else {
			village = world.getVillageCollection().getNearestVillage(new BlockPos(villager), 0);

			if (village == null) {
				return false;
			} else if (checkSufficientDoorsPresentForNewVillager() && villager.getIsWillingToMate(true)) {
				Entity entity = world.findNearestEntityWithinAABB(EntityVillager.class, villager.getEntityBoundingBox().grow(8.0D, 3.0D, 8.0D), villager);

				if (entity == null) {
					return false;
				} else {
					mate = (EntityVillager) entity;
					return mate.getGrowingAge() == 0 && mate.getIsWillingToMate(true);
				}
			} else {
				return false;
			}
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {

		matingTimeout = 300;
		villager.setMating(true);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {

		village = null;
		mate = null;
		villager.setMating(false);
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {

		return matingTimeout >= 0 && checkSufficientDoorsPresentForNewVillager() && villager.getGrowingAge() == 0 && villager.getIsWillingToMate(false);
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void updateTask() {

		--matingTimeout;
		villager.getLookHelper().setLookPositionWithEntity(mate, 10.0F, 30.0F);

		if (villager.getDistanceSq(mate) > 2.25D) {
			villager.getNavigator().tryMoveToEntityLiving(mate, 0.25D);
		} else if (matingTimeout == 0 && mate.isMating()) {
			giveBirth();
		}

		if (villager.getRNG().nextInt(35) == 0) {
			world.setEntityState(villager, (byte) 12);
		}
	}

	private boolean checkSufficientDoorsPresentForNewVillager() {

		if (!village.isMatingSeason()) {
			return false;
		} else {
			int i = (int) ((double) ((float) village.getNumVillageDoors()) * 0.35D);
			return village.getNumVillagers() < i;
		}
	}

	private void giveBirth() {

		EntityVillager entityvillager = villager.createChild(mate);
		mate.setGrowingAge(6000);
		villager.setGrowingAge(6000);
		mate.setIsWillingToMate(false);
		villager.setIsWillingToMate(false);
		entityvillager.setGrowingAge(-24000);
		entityvillager.setLocationAndAngles(villager.posX, villager.posY, villager.posZ, 0.0F, 0.0F);
		world.spawnEntity(entityvillager);
		world.setEntityState(entityvillager, (byte) 12);
	}

}
