package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.pathfinding.PathNavigateGround;

public class EntityAIRestrictSun extends EntityAIBase {

	private final EntityCreature entity;

	public EntityAIRestrictSun(EntityCreature creature) {

		entity = creature;
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {

		return entity.world.isDaytime() && entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {

		((PathNavigateGround) entity.getNavigator()).setAvoidSun(true);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {

		((PathNavigateGround) entity.getNavigator()).setAvoidSun(false);
	}

}
