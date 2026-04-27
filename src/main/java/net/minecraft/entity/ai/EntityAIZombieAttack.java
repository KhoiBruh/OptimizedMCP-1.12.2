package net.minecraft.entity.ai;

import net.minecraft.entity.monster.EntityZombie;

public class EntityAIZombieAttack extends EntityAIAttackMelee {

	private final EntityZombie zombie;
	private int raiseArmTicks;

	public EntityAIZombieAttack(EntityZombie zombieIn, double speedIn, boolean longMemoryIn) {

		super(zombieIn, speedIn, longMemoryIn);
		zombie = zombieIn;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {

		super.startExecuting();
		raiseArmTicks = 0;
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {

		super.resetTask();
		zombie.setArmsRaised(false);
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void updateTask() {

		super.updateTask();
		++raiseArmTicks;

		zombie.setArmsRaised(raiseArmTicks >= 5 && attackTick < 10);
	}

}
