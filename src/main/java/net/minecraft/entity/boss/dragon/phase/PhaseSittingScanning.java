package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PhaseSittingScanning extends PhaseSittingBase {

	private int scanningTime;

	public PhaseSittingScanning(EntityDragon dragonIn) {

		super(dragonIn);
	}

	/**
	 * Gives the phase a chance to update its status.
	 * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
	 */
	public void doLocalUpdate() {

		++scanningTime;
		EntityLivingBase entitylivingbase = dragon.world.getNearestAttackablePlayer(dragon, 20D, 10D);

		if (entitylivingbase != null) {
			if (scanningTime > 25) {
				dragon.getPhaseManager().setPhase(PhaseList.SITTING_ATTACKING);
			} else {
				Vec3d vec3d = (new Vec3d(entitylivingbase.posX - dragon.posX, 0D, entitylivingbase.posZ - dragon.posZ)).normalize();
				Vec3d vec3d1 = (new Vec3d(MathHelper.sin(dragon.rotationYaw * 0.017453292F), 0D, -MathHelper.cos(dragon.rotationYaw * 0.017453292F))).normalize();
				float f = (float) vec3d1.dotProduct(vec3d);
				float f1 = (float) (Math.acos(f) * (180D / Math.PI)) + 0.5F;

				if (f1 < 0F || f1 > 10F) {
					double d0 = entitylivingbase.posX - dragon.dragonPartHead.posX;
					double d1 = entitylivingbase.posZ - dragon.dragonPartHead.posZ;
					double d2 = MathHelper.clamp(MathHelper.wrapDegrees(180D - MathHelper.atan2(d0, d1) * (180D / Math.PI) - (double) dragon.rotationYaw), -100D, 100D);
					dragon.randomYawVelocity *= 0.8F;
					float f2 = MathHelper.sqrt(d0 * d0 + d1 * d1) + 1F;
					float f3 = f2;

					if (f2 > 40F) {
						f2 = 40F;
					}

					dragon.randomYawVelocity = (float) ((double) dragon.randomYawVelocity + d2 * (double) (0.7F / f2 / f3));
					dragon.rotationYaw += dragon.randomYawVelocity;
				}
			}
		} else if (scanningTime >= 100) {
			entitylivingbase = dragon.world.getNearestAttackablePlayer(dragon, 150D, 150D);
			dragon.getPhaseManager().setPhase(PhaseList.TAKEOFF);

			if (entitylivingbase != null) {
				dragon.getPhaseManager().setPhase(PhaseList.CHARGING_PLAYER);
				dragon.getPhaseManager().getPhase(PhaseList.CHARGING_PLAYER).setTarget(new Vec3d(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ));
			}
		}
	}

	/**
	 * Called when this phase is set to active
	 */
	public void initPhase() {

		scanningTime = 0;
	}

	public PhaseList<PhaseSittingScanning> getType() {

		return PhaseList.SITTING_SCANNING;
	}

}
