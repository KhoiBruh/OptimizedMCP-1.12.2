package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.WorldGenEndPodium;

public class PhaseLanding extends PhaseBase {

	private Vec3d targetLocation;

	public PhaseLanding(EntityDragon dragonIn) {

		super(dragonIn);
	}

	/**
	 * Generates particle effects appropriate to the phase (or sometimes sounds).
	 * Called by dragon's onLivingUpdate. Only used when worldObj.isRemote.
	 */
	public void doClientRenderEffects() {

		Vec3d vec3d = dragon.getHeadLookVec(1F).normalize();
		vec3d.rotateYaw(-((float) Math.PI / 4F));
		double d0 = dragon.dragonPartHead.posX;
		double d1 = dragon.dragonPartHead.posY + (double) (dragon.dragonPartHead.height / 2F);
		double d2 = dragon.dragonPartHead.posZ;

		for (int i = 0; i < 8; ++i) {
			double d3 = d0 + dragon.getRNG().nextGaussian() / 2D;
			double d4 = d1 + dragon.getRNG().nextGaussian() / 2D;
			double d5 = d2 + dragon.getRNG().nextGaussian() / 2D;
			dragon.world.spawnParticle(ParticleTypes.DRAGON_BREATH, d3, d4, d5, -vec3d.x() * 0.07999999821186066D + dragon.motionX, -vec3d.y() * 0.30000001192092896D + dragon.motionY, -vec3d.z() * 0.07999999821186066D + dragon.motionZ);
			vec3d.rotateYaw(0.19634955F);
		}
	}

	/**
	 * Gives the phase a chance to update its status.
	 * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
	 */
	public void doLocalUpdate() {

		if (targetLocation == null) {
			targetLocation = new Vec3d(dragon.world.getTopSolidOrLiquidBlock(WorldGenEndPodium.END_PODIUM_LOCATION));
		}

		if (targetLocation.squareDistanceTo(dragon.posX, dragon.posY, dragon.posZ) < 1D) {
			dragon.getPhaseManager().getPhase(PhaseList.SITTING_FLAMING).resetFlameCount();
			dragon.getPhaseManager().setPhase(PhaseList.SITTING_SCANNING);
		}
	}

	/**
	 * Returns the maximum amount dragon may rise or fall during this phase
	 */
	public float getMaxRiseOrFall() {

		return 1.5F;
	}

	public float getYawFactor() {

		float f = MathHelper.sqrt(dragon.motionX * dragon.motionX + dragon.motionZ * dragon.motionZ) + 1F;
		float f1 = Math.min(f, 40F);
		return f1 / f;
	}

	/**
	 * Called when this phase is set to active
	 */
	public void initPhase() {

		targetLocation = null;
	}

	

	/**
	 * Returns the location the dragon is flying toward
	 */
	public Vec3d getTargetLocation() {

		return targetLocation;
	}

	public PhaseList<PhaseLanding> getType() {

		return PhaseList.LANDING;
	}

}
