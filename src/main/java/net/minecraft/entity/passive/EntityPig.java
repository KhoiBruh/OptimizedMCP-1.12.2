package net.minecraft.entity.passive;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;
import java.util.Set;

public class EntityPig extends EntityAnimal {

	private static final DataParameter<Boolean> SADDLED = EntityDataManager.createKey(EntityPig.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> BOOST_TIME = EntityDataManager.createKey(EntityPig.class, DataSerializers.VARINT);
	private static final Set<Item> TEMPTATION_ITEMS = Sets.newHashSet(Items.CARROT, Items.POTATO, Items.BEETROOT);
	private boolean boosting;
	private int boostTime;
	private int totalBoostTime;

	public EntityPig(World worldIn) {

		super(worldIn);
		setSize(0.9F, 0.9F);
	}

	public static void registerFixesPig(DataFixer fixer) {

		EntityLiving.registerFixesMob(fixer, EntityPig.class);
	}

	protected void initEntityAI() {

		tasks.addTask(0, new EntityAISwimming(this));
		tasks.addTask(1, new EntityAIPanic(this, 1.25D));
		tasks.addTask(3, new EntityAIMate(this, 1.0D));
		tasks.addTask(4, new EntityAITempt(this, 1.2D, Items.CARROT_ON_A_STICK, false));
		tasks.addTask(4, new EntityAITempt(this, 1.2D, false, TEMPTATION_ITEMS));
		tasks.addTask(5, new EntityAIFollowParent(this, 1.1D));
		tasks.addTask(6, new EntityAIWanderAvoidWater(this, 1.0D));
		tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		tasks.addTask(8, new EntityAILookIdle(this));
	}

	protected void applyEntityAttributes() {

		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
		getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
	}

	@Nullable

	/**
	 * For vehicles, the first passenger is generally considered the controller and "drives" the vehicle. For example,
	 * Pigs, Horses, and Boats are generally "steered" by the controlling passenger.
	 */
	public Entity getControllingPassenger() {

		return getPassengers().isEmpty() ? null : getPassengers().get(0);
	}

	/**
	 * returns true if all the conditions for steering the entity are met. For pigs, this is true if it is being ridden
	 * by a player and the player is holding a carrot-on-a-stick
	 */
	public boolean canBeSteered() {

		Entity entity = getControllingPassenger();

		if (!(entity instanceof EntityPlayer entityplayer)) {
			return false;
		} else {
			return entityplayer.getHeldItemMainhand().getItem() == Items.CARROT_ON_A_STICK || entityplayer.getHeldItemOffhand().getItem() == Items.CARROT_ON_A_STICK;
		}
	}

	public void notifyDataManagerChange(DataParameter<?> key) {

		if (BOOST_TIME.equals(key) && world.isRemote) {
			boosting = true;
			boostTime = 0;
			totalBoostTime = dataManager.get(BOOST_TIME).intValue();
		}

		super.notifyDataManagerChange(key);
	}

	protected void entityInit() {

		super.entityInit();
		dataManager.register(SADDLED, Boolean.valueOf(false));
		dataManager.register(BOOST_TIME, Integer.valueOf(0));
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound compound) {

		super.writeEntityToNBT(compound);
		compound.setBoolean("Saddle", getSaddled());
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound compound) {

		super.readEntityFromNBT(compound);
		setSaddled(compound.getBoolean("Saddle"));
	}

	protected SoundEvent getAmbientSound() {

		return SoundEvents.ENTITY_PIG_AMBIENT;
	}

	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {

		return SoundEvents.ENTITY_PIG_HURT;
	}

	protected SoundEvent getDeathSound() {

		return SoundEvents.ENTITY_PIG_DEATH;
	}

	protected void playStepSound(BlockPos pos, Block blockIn) {

		playSound(SoundEvents.ENTITY_PIG_STEP, 0.15F, 1.0F);
	}

	public boolean processInteract(EntityPlayer player, EnumHand hand) {

		if (!super.processInteract(player, hand)) {
			ItemStack itemstack = player.getHeldItem(hand);

			if (itemstack.getItem() == Items.NAME_TAG) {
				itemstack.interactWithEntity(player, this, hand);
				return true;
			} else if (getSaddled() && !isBeingRidden()) {
				if (!world.isRemote) {
					player.startRiding(this);
				}

				return true;
			} else if (itemstack.getItem() == Items.SADDLE) {
				itemstack.interactWithEntity(player, this, hand);
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * Called when the mob's health reaches 0.
	 */
	public void onDeath(DamageSource cause) {

		super.onDeath(cause);

		if (!world.isRemote) {
			if (getSaddled()) {
				dropItem(Items.SADDLE, 1);
			}
		}
	}

	@Nullable
	protected ResourceLocation getLootTable() {

		return LootTableList.ENTITIES_PIG;
	}

	/**
	 * Returns true if the pig is saddled.
	 */
	public boolean getSaddled() {

		return dataManager.get(SADDLED).booleanValue();
	}

	/**
	 * Set or remove the saddle of the pig.
	 */
	public void setSaddled(boolean saddled) {

		if (saddled) {
			dataManager.set(SADDLED, Boolean.valueOf(true));
		} else {
			dataManager.set(SADDLED, Boolean.valueOf(false));
		}
	}

	/**
	 * Called when a lightning bolt hits the entity.
	 */
	public void onStruckByLightning(EntityLightningBolt lightningBolt) {

		if (!world.isRemote && !isDead) {
			EntityPigZombie entitypigzombie = new EntityPigZombie(world);
			entitypigzombie.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
			entitypigzombie.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
			entitypigzombie.setNoAI(isAIDisabled());

			if (hasCustomName()) {
				entitypigzombie.setCustomNameTag(getCustomNameTag());
				entitypigzombie.setAlwaysRenderNameTag(getAlwaysRenderNameTag());
			}

			world.spawnEntity(entitypigzombie);
			setDead();
		}
	}

	public void travel(float strafe, float vertical, float forward) {

		Entity entity = getPassengers().isEmpty() ? null : getPassengers().get(0);

		if (isBeingRidden() && canBeSteered()) {
			rotationYaw = entity.rotationYaw;
			prevRotationYaw = rotationYaw;
			rotationPitch = entity.rotationPitch * 0.5F;
			setRotation(rotationYaw, rotationPitch);
			renderYawOffset = rotationYaw;
			rotationYawHead = rotationYaw;
			stepHeight = 1.0F;
			jumpMovementFactor = getAIMoveSpeed() * 0.1F;

			if (boosting && boostTime++ > totalBoostTime) {
				boosting = false;
			}

			if (canPassengerSteer()) {
				float f = (float) getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * 0.225F;

				if (boosting) {
					f += f * 1.15F * MathHelper.sin((float) boostTime / (float) totalBoostTime * (float) Math.PI);
				}

				setAIMoveSpeed(f);
				super.travel(0.0F, 0.0F, 1.0F);
			} else {
				motionX = 0.0D;
				motionY = 0.0D;
				motionZ = 0.0D;
			}

			prevLimbSwingAmount = limbSwingAmount;
			double d1 = posX - prevPosX;
			double d0 = posZ - prevPosZ;
			float f1 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;

			if (f1 > 1.0F) {
				f1 = 1.0F;
			}

			limbSwingAmount += (f1 - limbSwingAmount) * 0.4F;
			limbSwing += limbSwingAmount;
		} else {
			stepHeight = 0.5F;
			jumpMovementFactor = 0.02F;
			super.travel(strafe, vertical, forward);
		}
	}

	public boolean boost() {

		if (boosting) {
			return false;
		} else {
			boosting = true;
			boostTime = 0;
			totalBoostTime = getRNG().nextInt(841) + 140;
			getDataManager().set(BOOST_TIME, Integer.valueOf(totalBoostTime));
			return true;
		}
	}

	public EntityPig createChild(EntityAgeable ageable) {

		return new EntityPig(world);
	}

	/**
	 * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
	 * the animal type)
	 */
	public boolean isBreedingItem(ItemStack stack) {

		return TEMPTATION_ITEMS.contains(stack.getItem());
	}

}
