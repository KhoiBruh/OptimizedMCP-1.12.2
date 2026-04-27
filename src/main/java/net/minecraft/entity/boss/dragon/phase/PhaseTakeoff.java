package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.WorldGenEndPodium;

import javax.annotation.Nullable;

public class PhaseTakeoff extends PhaseBase {

	private boolean firstTick;
	private Path currentPath;
	private Vec3d targetLocation;

	public PhaseTakeoff(EntityDragon dragonIn) {

		super(dragonIn);
	}

	/**
	 * Gives the phase a chance to update its status.
	 * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
	 */
	public void doLocalUpdate() {

		if (!firstTick && currentPath != null) {
			BlockPos blockpos = dragon.world.getTopSolidOrLiquidBlock(WorldGenEndPodium.END_PODIUM_LOCATION);
			double d0 = dragon.getDistanceSqToCenter(blockpos);

			if (d0 > 100.0D) {
				dragon.getPhaseManager().setPhase(PhaseList.HOLDING_PATTERN);
			}
		} else {
			firstTick = false;
			findNewTarget();
		}
	}

	/**
	 * Called when this phase is set to active
	 */
	public void initPhase() {

		firstTick = true;
		currentPath = null;
		targetLocation = null;
	}

	private void findNewTarget() {

		int i = dragon.initPathPoints();
		Vec3d vec3d = dragon.getHeadLookVec(1.0F);
		int j = dragon.getNearestPpIdx(-vec3d.x() * 40.0D, 105.0D, -vec3d.z() * 40.0D);

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
			navigateToNextPathNode();
		}
	}

	private void navigateToNextPathNode() {

		Vec3d vec3d = currentPath.getCurrentPos();
		currentPath.incrementPathIndex();
		double d0;

		while (true) {
			d0 = vec3d.y() + (double) (dragon.getRNG().nextFloat() * 20.0F);

			if (d0 >= vec3d.y()) {
				break;
			}
		}

		targetLocation = new Vec3d(vec3d.x(), d0, vec3d.z());
	}

	@Nullable

	/**
	 * Returns the location the dragon is flying toward
	 */
	public Vec3d getTargetLocation() {

		return targetLocation;
	}

	public PhaseList<PhaseTakeoff> getType() {

		return PhaseList.TAKEOFF;
	}

}
