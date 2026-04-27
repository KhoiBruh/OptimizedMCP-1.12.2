package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityShoulderRiding;
import net.minecraft.entity.player.EntityPlayer;

public class EntityAILandOnOwnersShoulder extends EntityAIBase {

	private final EntityShoulderRiding entity;
	private EntityPlayer owner;
	private boolean isSittingOnShoulder;

	public EntityAILandOnOwnersShoulder(EntityShoulderRiding entityIn) {

		entity = entityIn;
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {

		EntityLivingBase entitylivingbase = entity.getOwner();
		boolean flag = entitylivingbase != null && !((EntityPlayer) entitylivingbase).isSpectator() && !((EntityPlayer) entitylivingbase).capabilities.isFlying && !entitylivingbase.isInWater();
		return !entity.isSitting() && flag && entity.canSitOnShoulder();
	}

	/**
	 * Determine if this AI Task is interruptible by a higher (= lower value) priority task. All vanilla AITask have
	 * this value set to true.
	 */
	public boolean isInterruptible() {

		return !isSittingOnShoulder;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {

		owner = (EntityPlayer) entity.getOwner();
		isSittingOnShoulder = false;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void updateTask() {

		if (!isSittingOnShoulder && !entity.isSitting() && !entity.getLeashed()) {
			if (entity.getEntityBoundingBox().intersects(owner.getEntityBoundingBox())) {
				isSittingOnShoulder = entity.setEntityOnShoulder(owner);
			}
		}
	}

}
