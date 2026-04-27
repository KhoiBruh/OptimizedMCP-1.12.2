package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class EntityAIWander extends EntityAIBase {

	protected final EntityCreature entity;
	protected final double speed;
	protected double x;
	protected double y;
	protected double z;
	protected int executionChance;
	protected boolean mustUpdate;

	public EntityAIWander(EntityCreature creatureIn, double speedIn) {

		this(creatureIn, speedIn, 120);
	}

	public EntityAIWander(EntityCreature creatureIn, double speedIn, int chance) {

		entity = creatureIn;
		speed = speedIn;
		executionChance = chance;
		setMutexBits(1);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {

		if (!mustUpdate) {
			if (entity.getIdleTime() >= 100) {
				return false;
			}

			if (entity.getRNG().nextInt(executionChance) != 0) {
				return false;
			}
		}

		Vec3d vec3d = getPosition();

		if (vec3d == null) {
			return false;
		} else {
			x = vec3d.x();
			y = vec3d.y();
			z = vec3d.z();
			mustUpdate = false;
			return true;
		}
	}

	@Nullable
	protected Vec3d getPosition() {

		return RandomPositionGenerator.findRandomTarget(entity, 10, 7);
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {

		return !entity.getNavigator().noPath();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {

		entity.getNavigator().tryMoveToXYZ(x, y, z, speed);
	}

	/**
	 * Makes task to bypass chance
	 */
	public void makeUpdate() {

		mustUpdate = true;
	}

	/**
	 * Changes task random possibility for execution
	 */
	public void setExecutionChance(int newchance) {

		executionChance = newchance;
	}

}
