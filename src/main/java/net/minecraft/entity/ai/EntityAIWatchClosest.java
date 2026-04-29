package net.minecraft.entity.ai;

import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.entity.EntitySelectors;

public class EntityAIWatchClosest extends EntityAIBase {

	private final float chance;
	protected EntityLiving entity;
	/**
	 * The closest entity which is being watched by this one.
	 */
	protected Entity closestEntity;
	/**
	 * This is the Maximum distance that the AI will look for the Entity
	 */
	protected float maxDistanceForPlayer;
	protected Class<? extends Entity> watchedClass;
	private int lookTime;

	public EntityAIWatchClosest(EntityLiving entityIn, Class<? extends Entity> watchTargetClass, float maxDistance) {

		entity = entityIn;
		watchedClass = watchTargetClass;
		maxDistanceForPlayer = maxDistance;
		chance = 0.02F;
		setMutexBits(2);
	}

	public EntityAIWatchClosest(EntityLiving entityIn, Class<? extends Entity> watchTargetClass, float maxDistance, float chanceIn) {

		entity = entityIn;
		watchedClass = watchTargetClass;
		maxDistanceForPlayer = maxDistance;
		chance = chanceIn;
		setMutexBits(2);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {

		if (entity.getRNG().nextFloat() >= chance) {
			return false;
		} else {
			if (entity.getAttackTarget() != null) {
				closestEntity = entity.getAttackTarget();
			}

			if (watchedClass == EntityPlayer.class) {
				closestEntity = entity.world.getClosestPlayer(entity.posX, entity.posY, entity.posZ, maxDistanceForPlayer, Predicates.and(EntitySelectors.NOT_SPECTATING, EntitySelectors.notRiding(entity)));
			} else {
				closestEntity = entity.world.findNearestEntityWithinAABB(watchedClass, entity.getEntityBoundingBox().grow(maxDistanceForPlayer, 3D, maxDistanceForPlayer), entity);
			}

			return closestEntity != null;
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {

		if (!closestEntity.isEntityAlive()) {
			return false;
		} else if (entity.getDistanceSq(closestEntity) > (double) (maxDistanceForPlayer * maxDistanceForPlayer)) {
			return false;
		} else {
			return lookTime > 0;
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {

		lookTime = 40 + entity.getRNG().nextInt(40);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {

		closestEntity = null;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void updateTask() {

		entity.getLookHelper().setLookPosition(closestEntity.posX, closestEntity.posY + (double) closestEntity.getEyeHeight(), closestEntity.posZ, (float) entity.getHorizontalFaceSpeed(), (float) entity.getVerticalFaceSpeed());
		--lookTime;
	}

}
