package net.minecraft.entity.monster;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;

public class EntityVex extends EntityMob {

	protected static final DataParameter<Byte> VEX_FLAGS = EntityDataManager.createKey(EntityVex.class, DataSerializers.BYTE);
	private EntityLiving owner;

	@Nullable
	private BlockPos boundOrigin;
	private boolean limitedLifespan;
	private int limitedLifeTicks;

	public EntityVex(World worldIn) {

		super(worldIn);
		isImmuneToFire = true;
		moveHelper = new EntityVex.AIMoveControl(this);
		setSize(0.4F, 0.8F);
		experienceValue = 3;
	}

	public static void registerFixesVex(DataFixer fixer) {

		EntityLiving.registerFixesMob(fixer, EntityVex.class);
	}

	/**
	 * Tries to move the entity towards the specified location.
	 */
	public void move(MoverType type, double x, double y, double z) {

		super.move(type, x, y, z);
		doBlockCollisions();
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void onUpdate() {

		noClip = true;
		super.onUpdate();
		noClip = false;
		setNoGravity(true);

		if (limitedLifespan && --limitedLifeTicks <= 0) {
			limitedLifeTicks = 20;
			attackEntityFrom(DamageSource.STARVE, 1.0F);
		}
	}

	protected void initEntityAI() {

		super.initEntityAI();
		tasks.addTask(0, new EntityAISwimming(this));
		tasks.addTask(4, new EntityVex.AIChargeAttack());
		tasks.addTask(8, new EntityVex.AIMoveRandom());
		tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 3.0F, 1.0F));
		tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
		targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, EntityVex.class));
		targetTasks.addTask(2, new EntityVex.AICopyOwnerTarget(this));
		targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
	}

	protected void applyEntityAttributes() {

		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(14.0D);
		getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
	}

	protected void entityInit() {

		super.entityInit();
		dataManager.register(VEX_FLAGS, (byte) 0);
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound compound) {

		super.readEntityFromNBT(compound);

		if (compound.hasKey("BoundX")) {
			boundOrigin = new BlockPos(compound.getInteger("BoundX"), compound.getInteger("BoundY"), compound.getInteger("BoundZ"));
		}

		if (compound.hasKey("LifeTicks")) {
			setLimitedLife(compound.getInteger("LifeTicks"));
		}
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound compound) {

		super.writeEntityToNBT(compound);

		if (boundOrigin != null) {
			compound.setInteger("BoundX", boundOrigin.getX());
			compound.setInteger("BoundY", boundOrigin.getY());
			compound.setInteger("BoundZ", boundOrigin.getZ());
		}

		if (limitedLifespan) {
			compound.setInteger("LifeTicks", limitedLifeTicks);
		}
	}

	public EntityLiving getOwner() {

		return owner;
	}

	public void setOwner(EntityLiving ownerIn) {

		owner = ownerIn;
	}

	@Nullable
	public BlockPos getBoundOrigin() {

		return boundOrigin;
	}

	public void setBoundOrigin(@Nullable BlockPos boundOriginIn) {

		boundOrigin = boundOriginIn;
	}

	private boolean getVexFlag(int mask) {

		int i = dataManager.get(VEX_FLAGS);
		return (i & mask) != 0;
	}

	private void setVexFlag(int mask, boolean value) {

		int i = dataManager.get(VEX_FLAGS);

		if (value) {
			i = i | mask;
		} else {
			i = i & ~mask;
		}

		dataManager.set(VEX_FLAGS, (byte) (i & 255));
	}

	public boolean isCharging() {

		return getVexFlag(1);
	}

	public void setCharging(boolean charging) {

		setVexFlag(1, charging);
	}

	public void setLimitedLife(int limitedLifeTicksIn) {

		limitedLifespan = true;
		limitedLifeTicks = limitedLifeTicksIn;
	}

	protected SoundEvent getAmbientSound() {

		return SoundEvents.ENTITY_VEX_AMBIENT;
	}

	protected SoundEvent getDeathSound() {

		return SoundEvents.ENTITY_VEX_DEATH;
	}

	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {

		return SoundEvents.ENTITY_VEX_HURT;
	}

	@Nullable
	protected ResourceLocation getLootTable() {

		return LootTableList.ENTITIES_VEX;
	}

	public int getBrightnessForRender() {

		return 15728880;
	}

	/**
	 * Gets how bright this entity is.
	 */
	public float getBrightness() {

		return 1.0F;
	}

	@Nullable

	/**
	 * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
	 * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory.
	 *
	 * The livingdata parameter is used to pass data between all instances during a pack spawn. It will be null on the
	 * first call. Subclasses may check if it's null, and then create a new one and return it if so, initializing all
	 * entities in the pack with the contained data.
	 *
	 * @return The IEntityLivingData to pass to this method for other instances of this entity class within the same
	 * pack
	 *
	 * @param difficulty The current local difficulty
	 * @param livingdata Shared spawn data. Will usually be null. (See return value for more information)
	 */
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {

		setEquipmentBasedOnDifficulty(difficulty);
		setEnchantmentBasedOnDifficulty(difficulty);
		return super.onInitialSpawn(difficulty, livingdata);
	}

	/**
	 * Gives armor or weapon for entity based on given DifficultyInstance
	 */
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {

		setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
		setDropChance(EntityEquipmentSlot.MAINHAND, 0.0F);
	}

	class AIChargeAttack extends EntityAIBase {

		public AIChargeAttack() {

			setMutexBits(1);
		}

		public boolean shouldExecute() {

			if (getAttackTarget() != null && !getMoveHelper().isUpdating() && rand.nextInt(7) == 0) {
				return getDistanceSq(getAttackTarget()) > 4.0D;
			} else {
				return false;
			}
		}

		public boolean shouldContinueExecuting() {

			return getMoveHelper().isUpdating() && isCharging() && getAttackTarget() != null && getAttackTarget().isEntityAlive();
		}

		public void startExecuting() {

			EntityLivingBase entitylivingbase = getAttackTarget();
			Vec3d vec3d = entitylivingbase.getPositionEyes(1.0F);
			moveHelper.setMoveTo(vec3d.x(), vec3d.y(), vec3d.z(), 1.0D);
			setCharging(true);
			playSound(SoundEvents.ENTITY_VEX_CHARGE, 1.0F, 1.0F);
		}

		public void resetTask() {

			setCharging(false);
		}

		public void updateTask() {

			EntityLivingBase entitylivingbase = getAttackTarget();

			if (getEntityBoundingBox().intersects(entitylivingbase.getEntityBoundingBox())) {
				attackEntityAsMob(entitylivingbase);
				setCharging(false);
			} else {
				double d0 = getDistanceSq(entitylivingbase);

				if (d0 < 9.0D) {
					Vec3d vec3d = entitylivingbase.getPositionEyes(1.0F);
					moveHelper.setMoveTo(vec3d.x(), vec3d.y(), vec3d.z(), 1.0D);
				}
			}
		}

	}

	class AICopyOwnerTarget extends EntityAITarget {

		public AICopyOwnerTarget(EntityCreature creature) {

			super(creature, false);
		}

		public boolean shouldExecute() {

			return owner != null && owner.getAttackTarget() != null && isSuitableTarget(owner.getAttackTarget(), false);
		}

		public void startExecuting() {

			setAttackTarget(owner.getAttackTarget());
			super.startExecuting();
		}

	}

	class AIMoveControl extends EntityMoveHelper {

		public AIMoveControl(EntityVex vex) {

			super(vex);
		}

		public void onUpdateMoveHelper() {

			if (action == EntityMoveHelper.Action.MOVE_TO) {
				double d0 = posX - EntityVex.this.posX;
				double d1 = posY - EntityVex.this.posY;
				double d2 = posZ - EntityVex.this.posZ;
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;
				d3 = MathHelper.sqrt(d3);

				if (d3 < getEntityBoundingBox().getAverageEdgeLength()) {
					action = EntityMoveHelper.Action.WAIT;
					motionX *= 0.5D;
					motionY *= 0.5D;
					motionZ *= 0.5D;
				} else {
					motionX += d0 / d3 * 0.05D * speed;
					motionY += d1 / d3 * 0.05D * speed;
					motionZ += d2 / d3 * 0.05D * speed;

					if (getAttackTarget() == null) {
						rotationYaw = -((float) MathHelper.atan2(motionX, motionZ)) * (180F / (float) Math.PI);
						renderYawOffset = rotationYaw;
					} else {
						double d4 = getAttackTarget().posX - EntityVex.this.posX;
						double d5 = getAttackTarget().posZ - EntityVex.this.posZ;
						rotationYaw = -((float) MathHelper.atan2(d4, d5)) * (180F / (float) Math.PI);
						renderYawOffset = rotationYaw;
					}
				}
			}
		}

	}

	class AIMoveRandom extends EntityAIBase {

		public AIMoveRandom() {

			setMutexBits(1);
		}

		public boolean shouldExecute() {

			return !getMoveHelper().isUpdating() && rand.nextInt(7) == 0;
		}

		public boolean shouldContinueExecuting() {

			return false;
		}

		public void updateTask() {

			BlockPos blockpos = getBoundOrigin();

			if (blockpos == null) {
				blockpos = new BlockPos(EntityVex.this);
			}

			for (int i = 0; i < 3; ++i) {
				BlockPos blockpos1 = blockpos.add(rand.nextInt(15) - 7, rand.nextInt(11) - 5, rand.nextInt(15) - 7);

				if (world.isAirBlock(blockpos1)) {
					moveHelper.setMoveTo((double) blockpos1.getX() + 0.5D, (double) blockpos1.getY() + 0.5D, (double) blockpos1.getZ() + 0.5D, 0.25D);

					if (getAttackTarget() == null) {
						getLookHelper().setLookPosition((double) blockpos1.getX() + 0.5D, (double) blockpos1.getY() + 0.5D, (double) blockpos1.getZ() + 0.5D, 180.0F, 20.0F);
					}

					break;
				}
			}
		}

	}

}
