package net.minecraft.entity.passive;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLlamaSpit;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;

public class EntityLlama extends AbstractChestHorse implements IRangedAttackMob {

	private static final DataParameter<Integer> DATA_STRENGTH_ID = EntityDataManager.createKey(EntityLlama.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> DATA_COLOR_ID = EntityDataManager.createKey(EntityLlama.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> DATA_VARIANT_ID = EntityDataManager.createKey(EntityLlama.class, DataSerializers.VARINT);
	private boolean didSpit;

	@Nullable
	private EntityLlama caravanHead;

	@Nullable
	private EntityLlama caravanTail;

	public EntityLlama(World worldIn) {

		super(worldIn);
		setSize(0.9F, 1.87F);
	}

	private void setRandomStrength() {

		int i = rand.nextFloat() < 0.04F ? 5 : 3;
		setStrength(1 + rand.nextInt(i));
	}

	public int getStrength() {

		return dataManager.get(DATA_STRENGTH_ID);
	}

	private void setStrength(int strengthIn) {

		dataManager.set(DATA_STRENGTH_ID, Math.max(1, Math.min(5, strengthIn)));
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound compound) {

		super.writeEntityToNBT(compound);
		compound.setInteger("Variant", getVariant());
		compound.setInteger("Strength", getStrength());

		if (!horseChest.getStackInSlot(1).isEmpty()) {
			compound.setTag("DecorItem", horseChest.getStackInSlot(1).writeToNBT(new NBTTagCompound()));
		}
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound compound) {

		setStrength(compound.getInteger("Strength"));
		super.readEntityFromNBT(compound);
		setVariant(compound.getInteger("Variant"));

		if (compound.hasKey("DecorItem", 10)) {
			horseChest.setInventorySlotContents(1, new ItemStack(compound.getCompoundTag("DecorItem")));
		}

		updateHorseSlots();
	}

	protected void initEntityAI() {

		tasks.addTask(0, new EntityAISwimming(this));
		tasks.addTask(1, new EntityAIRunAroundLikeCrazy(this, 1.2D));
		tasks.addTask(2, new EntityAILlamaFollowCaravan(this, 2.0999999046325684D));
		tasks.addTask(3, new EntityAIAttackRanged(this, 1.25D, 40, 20.0F));
		tasks.addTask(3, new EntityAIPanic(this, 1.2D));
		tasks.addTask(4, new EntityAIMate(this, 1.0D));
		tasks.addTask(5, new EntityAIFollowParent(this, 1.0D));
		tasks.addTask(6, new EntityAIWanderAvoidWater(this, 0.7D));
		tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		tasks.addTask(8, new EntityAILookIdle(this));
		targetTasks.addTask(1, new EntityLlama.AIHurtByTarget(this));
		targetTasks.addTask(2, new EntityLlama.AIDefendTarget(this));
	}

	protected void applyEntityAttributes() {

		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
	}

	protected void entityInit() {

		super.entityInit();
		dataManager.register(DATA_STRENGTH_ID, 0);
		dataManager.register(DATA_COLOR_ID, -1);
		dataManager.register(DATA_VARIANT_ID, 0);
	}

	public int getVariant() {

		return MathHelper.clamp(dataManager.get(DATA_VARIANT_ID), 0, 3);
	}

	public void setVariant(int variantIn) {

		dataManager.set(DATA_VARIANT_ID, variantIn);
	}

	protected int getInventorySize() {

		return hasChest() ? 2 + 3 * getInventoryColumns() : super.getInventorySize();
	}

	public void updatePassenger(Entity passenger) {

		if (isPassenger(passenger)) {
			float f = MathHelper.cos(renderYawOffset * 0.017453292F);
			float f1 = MathHelper.sin(renderYawOffset * 0.017453292F);
			float f2 = 0.3F;
			passenger.setPosition(posX + (double) (0.3F * f1), posY + getMountedYOffset() + passenger.getYOffset(), posZ - (double) (0.3F * f));
		}
	}

	/**
	 * Returns the Y offset from the entity's position for any entity riding this one.
	 */
	public double getMountedYOffset() {

		return (double) height * 0.67D;
	}

	/**
	 * returns true if all the conditions for steering the entity are met. For pigs, this is true if it is being ridden
	 * by a player and the player is holding a carrot-on-a-stick
	 */
	public boolean canBeSteered() {

		return false;
	}

	protected boolean handleEating(EntityPlayer player, ItemStack stack) {

		int i = 0;
		int j = 0;
		float f = 0.0F;
		boolean flag = false;
		Item item = stack.getItem();

		if (item == Items.WHEAT) {
			i = 10;
			j = 3;
			f = 2.0F;
		} else if (item == Item.getItemFromBlock(Blocks.HAY_BLOCK)) {
			i = 90;
			j = 6;
			f = 10.0F;

			if (isTame() && getGrowingAge() == 0) {
				flag = true;
				setInLove(player);
			}
		}

		if (getHealth() < getMaxHealth() && f > 0.0F) {
			heal(f);
			flag = true;
		}

		if (isChild() && i > 0) {
			world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, posX + (double) (rand.nextFloat() * width * 2.0F) - (double) width, posY + 0.5D + (double) (rand.nextFloat() * height), posZ + (double) (rand.nextFloat() * width * 2.0F) - (double) width, 0.0D, 0.0D, 0.0D);

			if (!world.isRemote) {
				addGrowth(i);
			}

			flag = true;
		}

		if (j > 0 && (flag || !isTame()) && getTemper() < getMaxTemper()) {
			flag = true;

			if (!world.isRemote) {
				increaseTemper(j);
			}
		}

		if (flag && !isSilent()) {
			world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_LLAMA_EAT, getSoundCategory(), 1.0F, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F);
		}

		return flag;
	}

	/**
	 * Dead and sleeping entities cannot move
	 */
	protected boolean isMovementBlocked() {

		return getHealth() <= 0.0F || isEatingHaystack();
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

		livingdata = super.onInitialSpawn(difficulty, livingdata);
		setRandomStrength();
		int i;

		if (livingdata instanceof EntityLlama.GroupData) {
			i = ((EntityLlama.GroupData) livingdata).variant;
		} else {
			i = rand.nextInt(4);
			livingdata = new EntityLlama.GroupData(i);
		}

		setVariant(i);
		return livingdata;
	}

	public boolean hasColor() {

		return getColor() != null;
	}

	protected SoundEvent getAngrySound() {

		return SoundEvents.ENTITY_LLAMA_ANGRY;
	}

	protected SoundEvent getAmbientSound() {

		return SoundEvents.ENTITY_LLAMA_AMBIENT;
	}

	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {

		return SoundEvents.ENTITY_LLAMA_HURT;
	}

	protected SoundEvent getDeathSound() {

		return SoundEvents.ENTITY_LLAMA_DEATH;
	}

	protected void playStepSound(BlockPos pos, Block blockIn) {

		playSound(SoundEvents.ENTITY_LLAMA_STEP, 0.15F, 1.0F);
	}

	protected void playChestEquipSound() {

		playSound(SoundEvents.ENTITY_LLAMA_CHEST, 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
	}

	public void makeMad() {

		SoundEvent soundevent = getAngrySound();

		if (soundevent != null) {
			playSound(soundevent, getSoundVolume(), getSoundPitch());
		}
	}

	@Nullable
	protected ResourceLocation getLootTable() {

		return LootTableList.ENTITIES_LLAMA;
	}

	public int getInventoryColumns() {

		return getStrength();
	}

	public boolean wearsArmor() {

		return true;
	}

	public boolean isArmor(ItemStack stack) {

		return stack.getItem() == Item.getItemFromBlock(Blocks.CARPET);
	}

	public boolean canBeSaddled() {

		return false;
	}

	/**
	 * Called by InventoryBasic.onInventoryChanged() on a array that is never filled.
	 */
	public void onInventoryChanged(IInventory invBasic) {

		EnumDyeColor enumdyecolor = getColor();
		super.onInventoryChanged(invBasic);
		EnumDyeColor enumdyecolor1 = getColor();

		if (ticksExisted > 20 && enumdyecolor1 != null && enumdyecolor1 != enumdyecolor) {
			playSound(SoundEvents.ENTITY_LLAMA_SWAG, 0.5F, 1.0F);
		}
	}

	/**
	 * Updates the items in the saddle and armor slots of the horse's inventory.
	 */
	protected void updateHorseSlots() {

		if (!world.isRemote) {
			super.updateHorseSlots();
			setColorByItem(horseChest.getStackInSlot(1));
		}
	}

	private void setColorByItem(ItemStack stack) {

		if (isArmor(stack)) {
			setColor(EnumDyeColor.byMetadata(stack.getMetadata()));
		} else {
			setColor(null);
		}
	}

	@Nullable
	public EnumDyeColor getColor() {

		int i = dataManager.get(DATA_COLOR_ID);
		return i == -1 ? null : EnumDyeColor.byMetadata(i);
	}

	private void setColor(@Nullable EnumDyeColor color) {

		dataManager.set(DATA_COLOR_ID, color == null ? -1 : color.getMetadata());
	}

	public int getMaxTemper() {

		return 30;
	}

	/**
	 * Returns true if the mob is currently able to mate with the specified mob.
	 */
	public boolean canMateWith(EntityAnimal otherAnimal) {

		return otherAnimal != this && otherAnimal instanceof EntityLlama && canMate() && ((EntityLlama) otherAnimal).canMate();
	}

	public EntityLlama createChild(EntityAgeable ageable) {

		EntityLlama entityllama = new EntityLlama(world);
		setOffspringAttributes(ageable, entityllama);
		EntityLlama entityllama1 = (EntityLlama) ageable;
		int i = rand.nextInt(Math.max(getStrength(), entityllama1.getStrength())) + 1;

		if (rand.nextFloat() < 0.03F) {
			++i;
		}

		entityllama.setStrength(i);
		entityllama.setVariant(rand.nextBoolean() ? getVariant() : entityllama1.getVariant());
		return entityllama;
	}

	private void spit(EntityLivingBase target) {

		EntityLlamaSpit entityllamaspit = new EntityLlamaSpit(world, this);
		double d0 = target.posX - posX;
		double d1 = target.getEntityBoundingBox().minY + (double) (target.height / 3.0F) - entityllamaspit.posY;
		double d2 = target.posZ - posZ;
		float f = MathHelper.sqrt(d0 * d0 + d2 * d2) * 0.2F;
		entityllamaspit.shoot(d0, d1 + (double) f, d2, 1.5F, 10.0F);
		world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_LLAMA_SPIT, getSoundCategory(), 1.0F, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F);
		world.spawnEntity(entityllamaspit);
		didSpit = true;
	}

	private void setDidSpit(boolean didSpitIn) {

		didSpit = didSpitIn;
	}

	public void fall(float distance, float damageMultiplier) {

		int i = MathHelper.ceil((distance * 0.5F - 3.0F) * damageMultiplier);

		if (i > 0) {
			if (distance >= 6.0F) {
				attackEntityFrom(DamageSource.FALL, (float) i);

				if (isBeingRidden()) {
					for (Entity entity : getRecursivePassengers()) {
						entity.attackEntityFrom(DamageSource.FALL, (float) i);
					}
				}
			}

			IBlockState iblockstate = world.getBlockState(new BlockPos(posX, posY - 0.2D - (double) prevRotationYaw, posZ));
			Block block = iblockstate.getBlock();

			if (iblockstate.getMaterial() != Material.AIR && !isSilent()) {
				SoundType soundtype = block.getSoundType();
				world.playSound(null, posX, posY, posZ, soundtype.stepSound(), getSoundCategory(), soundtype.volume() * 0.5F, soundtype.pitch() * 0.75F);
			}
		}
	}

	public void leaveCaravan() {

		if (caravanHead != null) {
			caravanHead.caravanTail = null;
		}

		caravanHead = null;
	}

	public void joinCaravan(EntityLlama caravanHeadIn) {

		caravanHead = caravanHeadIn;
		caravanHead.caravanTail = this;
	}

	public boolean hasCaravanTrail() {

		return caravanTail != null;
	}

	public boolean inCaravan() {

		return caravanHead != null;
	}

	@Nullable
	public EntityLlama getCaravanHead() {

		return caravanHead;
	}

	protected double followLeashSpeed() {

		return 2.0D;
	}

	protected void followMother() {

		if (!inCaravan() && isChild()) {
			super.followMother();
		}
	}

	public boolean canEatGrass() {

		return false;
	}

	/**
	 * Attack the specified entity using a ranged attack.
	 */
	public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {

		spit(target);
	}

	public void setSwingingArms(boolean swingingArms) {

	}

	static class AIDefendTarget extends EntityAINearestAttackableTarget<EntityWolf> {

		public AIDefendTarget(EntityLlama llama) {

			super(llama, EntityWolf.class, 16, false, true, null);
		}

		public boolean shouldExecute() {

			if (super.shouldExecute() && targetEntity != null && !targetEntity.isTamed()) {
				return true;
			} else {
				taskOwner.setAttackTarget(null);
				return false;
			}
		}

		protected double getTargetDistance() {

			return super.getTargetDistance() * 0.25D;
		}

	}

	static class AIHurtByTarget extends EntityAIHurtByTarget {

		public AIHurtByTarget(EntityLlama llama) {

			super(llama, false);
		}

		public boolean shouldContinueExecuting() {

			if (taskOwner instanceof EntityLlama entityllama) {

				if (entityllama.didSpit) {
					entityllama.setDidSpit(false);
					return false;
				}
			}

			return super.shouldContinueExecuting();
		}

	}

	static class GroupData implements IEntityLivingData {

		public int variant;

		private GroupData(int variantIn) {

			variant = variantIn;
		}

	}

}
