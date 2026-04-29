package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.WorldGenEndPodium;

public class PhaseDying extends PhaseBase {

	private Vec3d targetLocation;
	private int time;

	public PhaseDying(EntityDragon dragonIn) {

		super(dragonIn);
	}

	/**
	 * Generates particle effects appropriate to the phase (or sometimes sounds).
	 * Called by dragon's onLivingUpdate. Only used when worldObj.isRemote.
	 */
	public void doClientRenderEffects() {

		if (time++ % 10 == 0) {
			float f = (dragon.getRNG().nextFloat() - 0.5F) * 8F;
			float f1 = (dragon.getRNG().nextFloat() - 0.5F) * 4F;
			float f2 = (dragon.getRNG().nextFloat() - 0.5F) * 8F;
			dragon.world.spawnParticle(ParticleTypes.EXPLOSION_HUGE, dragon.posX + (double) f, dragon.posY + 2D + (double) f1, dragon.posZ + (double) f2, 0D, 0D, 0D);
		}
	}

	/**
	 * Gives the phase a chance to update its status.
	 * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
	 */
	public void doLocalUpdate() {

		++time;

		if (targetLocation == null) {
			BlockPos blockpos = dragon.world.getHeight(WorldGenEndPodium.END_PODIUM_LOCATION);
			targetLocation = new Vec3d(blockpos.getX(), blockpos.getY(), blockpos.getZ());
		}

		double d0 = targetLocation.squareDistanceTo(dragon.posX, dragon.posY, dragon.posZ);

		if (d0 >= 100D && d0 <= 22500D && !dragon.collidedHorizontally && !dragon.collidedVertically) {
			dragon.setHealth(1F);
		} else {
			dragon.setHealth(0F);
		}
	}

	/**
	 * Called when this phase is set to active
	 */
	public void initPhase() {

		targetLocation = null;
		time = 0;
	}

	/**
	 * Returns the maximum amount dragon may rise or fall during this phase
	 */
	public float getMaxRiseOrFall() {

		return 3F;
	}

	

	/**
	 * Returns the location the dragon is flying toward
	 */
	public Vec3d getTargetLocation() {

		return targetLocation;
	}

	public PhaseList<PhaseDying> getType() {

		return PhaseList.DYING;
	}

}
