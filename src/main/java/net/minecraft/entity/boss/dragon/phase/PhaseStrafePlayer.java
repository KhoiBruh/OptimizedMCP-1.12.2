package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PhaseStrafePlayer extends PhaseBase {

	private static final Logger LOGGER = LogManager.getLogger();
	private int fireballCharge;
	private Path currentPath;
	private Vec3d targetLocation;
	private EntityLivingBase attackTarget;
	private boolean holdingPatternClockwise;

	public PhaseStrafePlayer(EntityDragon dragonIn) {

		super(dragonIn);
	}

	/**
	 * Gives the phase a chance to update its status.
	 * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
	 */
	public void doLocalUpdate() {

		if (attackTarget == null) {
			LOGGER.warn("Skipping player strafe phase because no player was found");
			dragon.getPhaseManager().setPhase(PhaseList.HOLDING_PATTERN);
		} else {
			if (currentPath != null && currentPath.isFinished()) {
				double d0 = attackTarget.posX;
				double d1 = attackTarget.posZ;
				double d2 = d0 - dragon.posX;
				double d3 = d1 - dragon.posZ;
				double d4 = MathHelper.sqrt(d2 * d2 + d3 * d3);
				double d5 = Math.min(0.4000000059604645D + d4 / 80D - 1D, 10D);
				targetLocation = new Vec3d(d0, attackTarget.posY + d5, d1);
			}

			double d12 = targetLocation == null ? 0D : targetLocation.squareDistanceTo(dragon.posX, dragon.posY, dragon.posZ);

			if (d12 < 100D || d12 > 22500D) {
				findNewTarget();
			}

			double d13 = 64D;

			if (attackTarget.getDistanceSq(dragon) < 4096D) {
				if (dragon.canEntityBeSeen(attackTarget)) {
					++fireballCharge;
					Vec3d vec3d1 = (new Vec3d(attackTarget.posX - dragon.posX, 0D, attackTarget.posZ - dragon.posZ)).normalize();
					Vec3d vec3d = (new Vec3d(MathHelper.sin(dragon.rotationYaw * 0.017453292F), 0D, -MathHelper.cos(dragon.rotationYaw * 0.017453292F))).normalize();
					float f1 = (float) vec3d.dotProduct(vec3d1);
					float f = (float) (Math.acos(f1) * (180D / Math.PI));
					f = f + 0.5F;

					if (fireballCharge >= 5 && f >= 0F && f < 10F) {
						double d14 = 1D;
						Vec3d vec3d2 = dragon.getLook(1F);
						double d6 = dragon.dragonPartHead.posX - vec3d2.x();
						double d7 = dragon.dragonPartHead.posY + (double) (dragon.dragonPartHead.height / 2F) + 0.5D;
						double d8 = dragon.dragonPartHead.posZ - vec3d2.z();
						double d9 = attackTarget.posX - d6;
						double d10 = attackTarget.posY + (double) (attackTarget.height / 2F) - (d7 + (double) (dragon.dragonPartHead.height / 2F));
						double d11 = attackTarget.posZ - d8;
						dragon.world.playEvent(null, 1017, new BlockPos(dragon), 0);
						EntityDragonFireball entitydragonfireball = new EntityDragonFireball(dragon.world, dragon, d9, d10, d11);
						entitydragonfireball.setLocationAndAngles(d6, d7, d8, 0F, 0F);
						dragon.world.spawnEntity(entitydragonfireball);
						fireballCharge = 0;

						if (currentPath != null) {
							while (!currentPath.isFinished()) {
								currentPath.incrementPathIndex();
							}
						}

						dragon.getPhaseManager().setPhase(PhaseList.HOLDING_PATTERN);
					}
				} else if (fireballCharge > 0) {
					--fireballCharge;
				}
			} else if (fireballCharge > 0) {
				--fireballCharge;
			}
		}
	}

	private void findNewTarget() {

		if (currentPath == null || currentPath.isFinished()) {
			int i = dragon.initPathPoints();
			int j = i;

			if (dragon.getRNG().nextInt(8) == 0) {
				holdingPatternClockwise = !holdingPatternClockwise;
				j = i + 6;
			}

			if (holdingPatternClockwise) {
				++j;
			} else {
				--j;
			}

			if (dragon.getFightManager() != null && dragon.getFightManager().getNumAliveCrystals() > 0) {
				j = j % 12;

				if (j < 0) {
					j += 12;
				}
			} else {
				j = j - 12;
				j = j & 7;
				j = j + 12;
			}

			currentPath = dragon.findPath(i, j, null);

			if (currentPath != null) {
				currentPath.incrementPathIndex();
			}
		}

		navigateToNextPathNode();
	}

	private void navigateToNextPathNode() {

		if (currentPath != null && !currentPath.isFinished()) {
			Vec3d vec3d = currentPath.getCurrentPos();
			currentPath.incrementPathIndex();
			double d0 = vec3d.x();
			double d2 = vec3d.z();
			double d1;

			while (true) {
				d1 = vec3d.y() + (double) (dragon.getRNG().nextFloat() * 20F);

				if (d1 >= vec3d.y()) {
					break;
				}
			}

			targetLocation = new Vec3d(d0, d1, d2);
		}
	}

	/**
	 * Called when this phase is set to active
	 */
	public void initPhase() {

		fireballCharge = 0;
		targetLocation = null;
		currentPath = null;
		attackTarget = null;
	}

	public void setTarget(EntityLivingBase p_188686_1_) {

		attackTarget = p_188686_1_;
		int i = dragon.initPathPoints();
		int j = dragon.getNearestPpIdx(attackTarget.posX, attackTarget.posY, attackTarget.posZ);
		int k = MathHelper.floor(attackTarget.posX);
		int l = MathHelper.floor(attackTarget.posZ);
		double d0 = (double) k - dragon.posX;
		double d1 = (double) l - dragon.posZ;
		double d2 = MathHelper.sqrt(d0 * d0 + d1 * d1);
		double d3 = Math.min(0.4000000059604645D + d2 / 80D - 1D, 10D);
		int i1 = MathHelper.floor(attackTarget.posY + d3);
		PathPoint pathpoint = new PathPoint(k, i1, l);
		currentPath = dragon.findPath(i, j, pathpoint);

		if (currentPath != null) {
			currentPath.incrementPathIndex();
			navigateToNextPathNode();
		}
	}

	

	/**
	 * Returns the location the dragon is flying toward
	 */
	public Vec3d getTargetLocation() {

		return targetLocation;
	}

	public PhaseList<PhaseStrafePlayer> getType() {

		return PhaseList.STRAFE_PLAYER;
	}

}
