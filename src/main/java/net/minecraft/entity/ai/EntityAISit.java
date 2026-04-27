package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;

public class EntityAISit extends EntityAIBase {

	private final EntityTameable tameable;

	/**
	 * If the EntityTameable is sitting.
	 */
	private boolean isSitting;

	public EntityAISit(EntityTameable entityIn) {

		tameable = entityIn;
		setMutexBits(5);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {

		if (!tameable.isTamed()) {
			return false;
		} else if (tameable.isInWater()) {
			return false;
		} else if (!tameable.onGround) {
			return false;
		} else {
			EntityLivingBase entitylivingbase = tameable.getOwner();

			if (entitylivingbase == null) {
				return true;
			} else {
				return (!(tameable.getDistanceSq(entitylivingbase) < 144.0D) || entitylivingbase.getRevengeTarget() == null) && isSitting;
			}
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {

		tameable.getNavigator().clearPath();
		tameable.setSitting(true);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {

		tameable.setSitting(false);
	}

	/**
	 * Sets the sitting flag.
	 */
	public void setSitting(boolean sitting) {

		isSitting = sitting;
	}

}
