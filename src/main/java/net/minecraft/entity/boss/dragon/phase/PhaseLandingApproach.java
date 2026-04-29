package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.WorldGenEndPodium;

public class PhaseLandingApproach extends PhaseBase {

	private Path currentPath;
	private Vec3d targetLocation;

	public PhaseLandingApproach(EntityDragon dragonIn) {

		super(dragonIn);
	}

	public PhaseList<PhaseLandingApproach> getType() {

		return PhaseList.LANDING_APPROACH;
	}

	/**
	 * Called when this phase is set to active
	 */
	public void initPhase() {

		currentPath = null;
		targetLocation = null;
	}

	/**
	 * Gives the phase a chance to update its status.
	 * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
	 */
	public void doLocalUpdate() {

		double d0 = targetLocation == null ? 0.0D : targetLocation.squareDistanceTo(dragon.posX, dragon.posY, dragon.posZ);

		if (d0 < 100.0D || d0 > 22500.0D || dragon.collidedHorizontally || dragon.collidedVertically) {
			findNewTarget();
		}
	}

	

	/**
	 * Returns the location the dragon is flying toward
	 */
	public Vec3d getTargetLocation() {

		return targetLocation;
	}

	private void findNewTarget() {

		if (currentPath == null || currentPath.isFinished()) {
			int i = dragon.initPathPoints();
			BlockPos blockpos = dragon.world.getTopSolidOrLiquidBlock(WorldGenEndPodium.END_PODIUM_LOCATION);
			EntityPlayer entityplayer = dragon.world.getNearestAttackablePlayer(blockpos, 128.0D, 128.0D);
			int j;

			if (entityplayer != null) {
				Vec3d vec3d = (new Vec3d(entityplayer.posX, 0.0D, entityplayer.posZ)).normalize();
				j = dragon.getNearestPpIdx(-vec3d.x() * 40.0D, 105.0D, -vec3d.z() * 40.0D);
			} else {
				j = dragon.getNearestPpIdx(40.0D, blockpos.getY(), 0.0D);
			}

			PathPoint pathpoint = new PathPoint(blockpos.getX(), blockpos.getY(), blockpos.getZ());
			currentPath = dragon.findPath(i, j, pathpoint);

			if (currentPath != null) {
				currentPath.incrementPathIndex();
			}
		}

		navigateToNextPathNode();

		if (currentPath != null && currentPath.isFinished()) {
			dragon.getPhaseManager().setPhase(PhaseList.LANDING);
		}
	}

	private void navigateToNextPathNode() {

		if (currentPath != null && !currentPath.isFinished()) {
			Vec3d vec3d = currentPath.getCurrentPos();
			currentPath.incrementPathIndex();
			double d0 = vec3d.x();
			double d1 = vec3d.z();
			double d2;

			while (true) {
				d2 = vec3d.y() + (double) (dragon.getRNG().nextFloat() * 20.0F);

				if (d2 >= vec3d.y()) {
					break;
				}
			}

			targetLocation = new Vec3d(d0, d2, d1);
		}
	}

}
