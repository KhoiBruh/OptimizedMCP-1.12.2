package net.minecraft.entity.monster;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityBlaze extends EntityMob {

	private static final DataParameter<Byte> ON_FIRE = EntityDataManager.createKey(EntityBlaze.class, DataSerializers.BYTE);
	/**
	 * Random offset used in floating behaviour
	 */
	private float heightOffset = 0.5F;
	/**
	 * ticks until heightOffset is randomized
	 */
	private int heightOffsetUpdateTime;

	public EntityBlaze(World worldIn) {

		super(worldIn);
		setPathPriority(PathNodeType.WATER, -1F);
		setPathPriority(PathNodeType.LAVA, 8F);
		setPathPriority(PathNodeType.DANGER_FIRE, 0F);
		setPathPriority(PathNodeType.DAMAGE_FIRE, 0F);
		isImmuneToFire = true;
		experienceValue = 10;
	}

	public static void registerFixesBlaze(DataFixer fixer) {

		EntityLiving.registerFixesMob(fixer, EntityBlaze.class);
	}

	protected void initEntityAI() {

		tasks.addTask(4, new EntityBlaze.AIFireballAttack(this));
		tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1D));
		tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1D, 0F));
		tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8F));
		tasks.addTask(8, new EntityAILookIdle(this));
		targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
	}

	protected void applyEntityAttributes() {

		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6D);
		getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23000000417232513D);
		getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(48D);
	}

	protected void entityInit() {

		super.entityInit();
		dataManager.register(ON_FIRE, (byte) 0);
	}

	protected SoundEvent getAmbientSound() {

		return SoundEvents.ENTITY_BLAZE_AMBIENT;
	}

	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {

		return SoundEvents.ENTITY_BLAZE_HURT;
	}

	protected SoundEvent getDeathSound() {

		return SoundEvents.ENTITY_BLAZE_DEATH;
	}

	public int getBrightnessForRender() {

		return 15728880;
	}

	/**
	 * Gets how bright this entity is.
	 */
	public float getBrightness() {

		return 1F;
	}

	/**
	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
	 * use this to react to sunlight and start to burn.
	 */
	public void onLivingUpdate() {

		if (!onGround && motionY < 0D) {
			motionY *= 0.6D;
		}

		if (world.isRemote) {
			if (rand.nextInt(24) == 0 && !isSilent()) {
				world.playSound(posX + 0.5D, posY + 0.5D, posZ + 0.5D, SoundEvents.ENTITY_BLAZE_BURN, getSoundCategory(), 1F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F, false);
			}

			for (int i = 0; i < 2; ++i) {
				world.spawnParticle(ParticleTypes.SMOKE_LARGE, posX + (rand.nextDouble() - 0.5D) * (double) width, posY + rand.nextDouble() * (double) height, posZ + (rand.nextDouble() - 0.5D) * (double) width, 0D, 0D, 0D);
			}
		}

		super.onLivingUpdate();
	}

	protected void updateAITasks() {

		if (isWet()) {
			attackEntityFrom(DamageSource.DROWN, 1F);
		}

		--heightOffsetUpdateTime;

		if (heightOffsetUpdateTime <= 0) {
			heightOffsetUpdateTime = 100;
			heightOffset = 0.5F + (float) rand.nextGaussian() * 3F;
		}

		EntityLivingBase entitylivingbase = getAttackTarget();

		if (entitylivingbase != null && entitylivingbase.posY + (double) entitylivingbase.getEyeHeight() > posY + (double) getEyeHeight() + (double) heightOffset) {
			motionY += (0.30000001192092896D - motionY) * 0.30000001192092896D;
			isAirBorne = true;
		}

		super.updateAITasks();
	}

	public void fall(float distance, float damageMultiplier) {

	}

	/**
	 * Returns true if the entity is on fire. Used by render to add the fire effect on rendering.
	 */
	public boolean isBurning() {

		return isCharged();
	}

	
	protected ResourceLocation getLootTable() {

		return LootTableList.ENTITIES_BLAZE;
	}

	public boolean isCharged() {

		return (dataManager.get(ON_FIRE) & 1) != 0;
	}

	public void setOnFire(boolean onFire) {

		byte b0 = dataManager.get(ON_FIRE);

		if (onFire) {
			b0 = (byte) (b0 | 1);
		} else {
			b0 = (byte) (b0 & -2);
		}

		dataManager.set(ON_FIRE, b0);
	}

	/**
	 * Checks to make sure the light is not too bright where the mob is spawning
	 */
	protected boolean isValidLightLevel() {

		return true;
	}

	static class AIFireballAttack extends EntityAIBase {

		private final EntityBlaze blaze;
		private int attackStep;
		private int attackTime;

		public AIFireballAttack(EntityBlaze blazeIn) {

			blaze = blazeIn;
			setMutexBits(3);
		}

		public boolean shouldExecute() {

			EntityLivingBase entitylivingbase = blaze.getAttackTarget();
			return entitylivingbase != null && entitylivingbase.isEntityAlive();
		}

		public void startExecuting() {

			attackStep = 0;
		}

		public void resetTask() {

			blaze.setOnFire(false);
		}

		public void updateTask() {

			--attackTime;
			EntityLivingBase entitylivingbase = blaze.getAttackTarget();
			double d0 = blaze.getDistanceSq(entitylivingbase);

			if (d0 < 4D) {
				if (attackTime <= 0) {
					attackTime = 20;
					blaze.attackEntityAsMob(entitylivingbase);
				}

				blaze.getMoveHelper().setMoveTo(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, 1D);
			} else if (d0 < getFollowDistance() * getFollowDistance()) {
				double d1 = entitylivingbase.posX - blaze.posX;
				double d2 = entitylivingbase.getEntityBoundingBox().minY + (double) (entitylivingbase.height / 2F) - (blaze.posY + (double) (blaze.height / 2F));
				double d3 = entitylivingbase.posZ - blaze.posZ;

				if (attackTime <= 0) {
					++attackStep;

					if (attackStep == 1) {
						attackTime = 60;
						blaze.setOnFire(true);
					} else if (attackStep <= 4) {
						attackTime = 6;
					} else {
						attackTime = 100;
						attackStep = 0;
						blaze.setOnFire(false);
					}

					if (attackStep > 1) {
						float f = MathHelper.sqrt(MathHelper.sqrt(d0)) * 0.5F;
						blaze.world.playEvent(null, 1018, new BlockPos((int) blaze.posX, (int) blaze.posY, (int) blaze.posZ), 0);

						for (int i = 0; i < 1; ++i) {
							EntitySmallFireball entitysmallfireball = new EntitySmallFireball(blaze.world, blaze, d1 + blaze.getRNG().nextGaussian() * (double) f, d2, d3 + blaze.getRNG().nextGaussian() * (double) f);
							entitysmallfireball.posY = blaze.posY + (double) (blaze.height / 2F) + 0.5D;
							blaze.world.spawnEntity(entitysmallfireball);
						}
					}
				}

				blaze.getLookHelper().setLookPositionWithEntity(entitylivingbase, 10F, 10F);
			} else {
				blaze.getNavigator().clearPath();
				blaze.getMoveHelper().setMoveTo(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, 1D);
			}

			super.updateTask();
		}

		private double getFollowDistance() {

			IAttributeInstance iattributeinstance = blaze.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
			return iattributeinstance == null ? 16D : iattributeinstance.getAttributeValue();
		}

	}

}
