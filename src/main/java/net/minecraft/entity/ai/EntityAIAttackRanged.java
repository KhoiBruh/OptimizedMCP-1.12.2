package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.util.math.MathHelper;

public class EntityAIAttackRanged extends EntityAIBase {

	/**
	 * The entity the AI instance has been applied to
	 */
	private final EntityLiving entityHost;

	/**
	 * The entity (as a RangedAttackMob) the AI instance has been applied to.
	 */
	private final IRangedAttackMob rangedAttackEntityHost;
	private final double entityMoveSpeed;
	private final int attackIntervalMin;
	/**
	 * The maximum time the AI has to wait before peforming another ranged attack.
	 */
	private final int maxRangedAttackTime;
	private final float attackRadius;
	private final float maxAttackDistance;
	private EntityLivingBase attackTarget;
	/**
	 * A decrementing tick that spawns a ranged attack once this value reaches 0. It is then set back to the
	 * maxRangedAttackTime.
	 */
	private int rangedAttackTime;
	private int seeTime;

	public EntityAIAttackRanged(IRangedAttackMob attacker, double movespeed, int maxAttackTime, float maxAttackDistanceIn) {

		this(attacker, movespeed, maxAttackTime, maxAttackTime, maxAttackDistanceIn);
	}

	public EntityAIAttackRanged(IRangedAttackMob attacker, double movespeed, int p_i1650_4_, int maxAttackTime, float maxAttackDistanceIn) {

		rangedAttackTime = -1;

		if (!(attacker instanceof EntityLivingBase)) {
			throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
		} else {
			rangedAttackEntityHost = attacker;
			entityHost = (EntityLiving) attacker;
			entityMoveSpeed = movespeed;
			attackIntervalMin = p_i1650_4_;
			maxRangedAttackTime = maxAttackTime;
			attackRadius = maxAttackDistanceIn;
			maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
			setMutexBits(3);
		}
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {

		EntityLivingBase entitylivingbase = entityHost.getAttackTarget();

		if (entitylivingbase == null) {
			return false;
		} else {
			attackTarget = entitylivingbase;
			return true;
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {

		return shouldExecute() || !entityHost.getNavigator().noPath();
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {

		attackTarget = null;
		seeTime = 0;
		rangedAttackTime = -1;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void updateTask() {

		double d0 = entityHost.getDistanceSq(attackTarget.posX, attackTarget.getEntityBoundingBox().minY, attackTarget.posZ);
		boolean flag = entityHost.getEntitySenses().canSee(attackTarget);

		if (flag) {
			++seeTime;
		} else {
			seeTime = 0;
		}

		if (d0 <= (double) maxAttackDistance && seeTime >= 20) {
			entityHost.getNavigator().clearPath();
		} else {
			entityHost.getNavigator().tryMoveToEntityLiving(attackTarget, entityMoveSpeed);
		}

		entityHost.getLookHelper().setLookPositionWithEntity(attackTarget, 30F, 30F);

		if (--rangedAttackTime == 0) {
			if (!flag) {
				return;
			}

			float f = MathHelper.sqrt(d0) / attackRadius;
			float lvt_5_1_ = MathHelper.clamp(f, 0.1F, 1F);
			rangedAttackEntityHost.attackEntityWithRangedAttack(attackTarget, lvt_5_1_);
			rangedAttackTime = MathHelper.floor(f * (float) (maxRangedAttackTime - attackIntervalMin) + (float) attackIntervalMin);
		} else if (rangedAttackTime < 0) {
			float f2 = MathHelper.sqrt(d0) / attackRadius;
			rangedAttackTime = MathHelper.floor(f2 * (float) (maxRangedAttackTime - attackIntervalMin) + (float) attackIntervalMin);
		}
	}

}
