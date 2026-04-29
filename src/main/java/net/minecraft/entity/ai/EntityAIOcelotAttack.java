package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class EntityAIOcelotAttack extends EntityAIBase {

	World world;
	EntityLiving entity;
	EntityLivingBase target;
	int attackCountdown;

	public EntityAIOcelotAttack(EntityLiving theEntityIn) {

		entity = theEntityIn;
		world = theEntityIn.world;
		setMutexBits(3);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {

		EntityLivingBase entitylivingbase = entity.getAttackTarget();

		if (entitylivingbase == null) {
			return false;
		} else {
			target = entitylivingbase;
			return true;
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {

		if (!target.isEntityAlive()) {
			return false;
		} else if (entity.getDistanceSq(target) > 225D) {
			return false;
		} else {
			return !entity.getNavigator().noPath() || shouldExecute();
		}
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {

		target = null;
		entity.getNavigator().clearPath();
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void updateTask() {

		entity.getLookHelper().setLookPositionWithEntity(target, 30F, 30F);
		double d0 = entity.width * 2F * entity.width * 2F;
		double d1 = entity.getDistanceSq(target.posX, target.getEntityBoundingBox().minY, target.posZ);
		double d2 = 0.8D;

		if (d1 > d0 && d1 < 16D) {
			d2 = 1.33D;
		} else if (d1 < 225D) {
			d2 = 0.6D;
		}

		entity.getNavigator().tryMoveToEntityLiving(target, d2);
		attackCountdown = Math.max(attackCountdown - 1, 0);

		if (d1 <= d0) {
			if (attackCountdown == 0) {
				attackCountdown = 20;
				entity.attackEntityAsMob(target);
			}
		}
	}

}
