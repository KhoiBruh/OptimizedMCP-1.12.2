package net.minecraft.entity.ai;

import net.minecraft.entity.passive.EntityAnimal;

import java.util.List;

public class EntityAIFollowParent extends EntityAIBase {

	/**
	 * The child that is following its parent.
	 */
	EntityAnimal childAnimal;
	EntityAnimal parentAnimal;
	double moveSpeed;
	private int delayCounter;

	public EntityAIFollowParent(EntityAnimal animal, double speed) {

		childAnimal = animal;
		moveSpeed = speed;
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {

		if (childAnimal.getGrowingAge() >= 0) {
			return false;
		} else {
			List<EntityAnimal> list = childAnimal.world.getEntitiesWithinAABB(childAnimal.getClass(), childAnimal.getEntityBoundingBox().grow(8D, 4D, 8D));
			EntityAnimal entityanimal = null;
			double d0 = Double.MAX_VALUE;

			for (EntityAnimal entityanimal1 : list) {
				if (entityanimal1.getGrowingAge() >= 0) {
					double d1 = childAnimal.getDistanceSq(entityanimal1);

					if (d1 <= d0) {
						d0 = d1;
						entityanimal = entityanimal1;
					}
				}
			}

			if (entityanimal == null) {
				return false;
			} else if (d0 < 9D) {
				return false;
			} else {
				parentAnimal = entityanimal;
				return true;
			}
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {

		if (childAnimal.getGrowingAge() >= 0) {
			return false;
		} else if (!parentAnimal.isEntityAlive()) {
			return false;
		} else {
			double d0 = childAnimal.getDistanceSq(parentAnimal);
			return d0 >= 9D && d0 <= 256D;
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {

		delayCounter = 0;
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {

		parentAnimal = null;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void updateTask() {

		if (--delayCounter <= 0) {
			delayCounter = 10;
			childAnimal.getNavigator().tryMoveToEntityLiving(parentAnimal, moveSpeed);
		}
	}

}
