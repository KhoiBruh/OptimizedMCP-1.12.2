package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class EntityAIAvoidEntity<T extends Entity> extends EntityAIBase {

	private final Predicate<Entity> canBeSeenSelector;
	private final double farSpeed;
	private final double nearSpeed;
	private final float avoidDistance;
	/**
	 * The PathNavigate of our entity
	 */
	private final PathNavigate navigation;
	private final Class<T> classToAvoid;
	private final Predicate<? super T> avoidTargetSelector;
	/**
	 * The entity we are attached to
	 */
	protected EntityCreature entity;
	protected T closestLivingEntity;
	/**
	 * The PathEntity of our entity
	 */
	private Path path;

	public EntityAIAvoidEntity(EntityCreature entityIn, Class<T> classToAvoidIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn) {

		this(entityIn, classToAvoidIn, Predicates.alwaysTrue(), avoidDistanceIn, farSpeedIn, nearSpeedIn);
	}

	public EntityAIAvoidEntity(EntityCreature entityIn, Class<T> classToAvoidIn, Predicate<? super T> avoidTargetSelectorIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn) {

		canBeSeenSelector = p_apply_1_ -> p_apply_1_.isEntityAlive() && entity.getEntitySenses().canSee(p_apply_1_) && !entity.isOnSameTeam(p_apply_1_);
		entity = entityIn;
		classToAvoid = classToAvoidIn;
		avoidTargetSelector = avoidTargetSelectorIn;
		avoidDistance = avoidDistanceIn;
		farSpeed = farSpeedIn;
		nearSpeed = nearSpeedIn;
		navigation = entityIn.getNavigator();
		setMutexBits(1);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {

		List<T> list = entity.world.getEntitiesWithinAABB(classToAvoid, entity.getEntityBoundingBox().grow(avoidDistance, 3.0D, avoidDistance), Predicates.and(EntitySelectors.CAN_AI_TARGET, canBeSeenSelector, avoidTargetSelector));

		if (list.isEmpty()) {
			return false;
		} else {
			closestLivingEntity = list.getFirst();
			Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(entity, 16, 7, new Vec3d(closestLivingEntity.posX, closestLivingEntity.posY, closestLivingEntity.posZ));

			if (vec3d == null) {
				return false;
			} else if (closestLivingEntity.getDistanceSq(vec3d.x(), vec3d.y(), vec3d.z()) < closestLivingEntity.getDistanceSq(entity)) {
				return false;
			} else {
				path = navigation.getPathToXYZ(vec3d.x(), vec3d.y(), vec3d.z());
				return path != null;
			}
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {

		return !navigation.noPath();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {

		navigation.setPath(path, farSpeed);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {

		closestLivingEntity = null;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void updateTask() {

		if (entity.getDistanceSq(closestLivingEntity) < 49.0D) {
			entity.getNavigator().setSpeed(nearSpeed);
		} else {
			entity.getNavigator().setSpeed(farSpeed);
		}
	}

}
