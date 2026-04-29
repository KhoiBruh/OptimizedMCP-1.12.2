package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ParticleTypes;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.List;

public class EntityDragonFireball extends EntityFireball {

	public EntityDragonFireball(World worldIn) {

		super(worldIn);
		setSize(1F, 1F);
	}

	public EntityDragonFireball(World worldIn, double x, double y, double z, double accelX, double accelY, double accelZ) {

		super(worldIn, x, y, z, accelX, accelY, accelZ);
		setSize(1F, 1F);
	}

	public EntityDragonFireball(World worldIn, EntityLivingBase shooter, double accelX, double accelY, double accelZ) {

		super(worldIn, shooter, accelX, accelY, accelZ);
		setSize(1F, 1F);
	}

	public static void registerFixesDragonFireball(DataFixer fixer) {

		EntityFireball.registerFixesFireball(fixer, "DragonFireball");
	}

	/**
	 * Called when this EntityFireball hits a block or entity.
	 */
	protected void onImpact(RayTraceResult result) {

		if (result.entityHit == null || !result.entityHit.isEntityEqual(shootingEntity)) {
			if (!world.isRemote) {
				List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().grow(4D, 2D, 4D));
				EntityAreaEffectCloud entityareaeffectcloud = new EntityAreaEffectCloud(world, posX, posY, posZ);
				entityareaeffectcloud.setOwner(shootingEntity);
				entityareaeffectcloud.setParticle(ParticleTypes.DRAGON_BREATH);
				entityareaeffectcloud.setRadius(3F);
				entityareaeffectcloud.setDuration(600);
				entityareaeffectcloud.setRadiusPerTick((7F - entityareaeffectcloud.getRadius()) / (float) entityareaeffectcloud.getDuration());
				entityareaeffectcloud.addEffect(new PotionEffect(MobEffects.INSTANT_DAMAGE, 1, 1));

				if (!list.isEmpty()) {
					for (EntityLivingBase entitylivingbase : list) {
						double d0 = getDistanceSq(entitylivingbase);

						if (d0 < 16D) {
							entityareaeffectcloud.setPosition(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ);
							break;
						}
					}
				}

				world.playEvent(2006, new BlockPos(posX, posY, posZ), 0);
				world.spawnEntity(entityareaeffectcloud);
				setDead();
			}
		}
	}

	/**
	 * Returns true if other Entities should be prevented from moving through this Entity.
	 */
	public boolean canBeCollidedWith() {

		return false;
	}

	/**
	 * Called when the entity is attacked.
	 */
	public boolean attackEntityFrom(DamageSource source, float amount) {

		return false;
	}

	protected ParticleTypes getParticleType() {

		return ParticleTypes.DRAGON_BREATH;
	}

	protected boolean isFireballFiery() {

		return false;
	}

}
