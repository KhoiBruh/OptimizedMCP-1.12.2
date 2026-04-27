package net.minecraft.entity.player;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.*;
import net.minecraft.util.datafix.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.*;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("incomplete-switch")
public abstract class EntityPlayer extends EntityLivingBase {

	protected static final DataParameter<Byte> PLAYER_MODEL_FLAG = EntityDataManager.createKey(EntityPlayer.class, DataSerializers.BYTE);
	protected static final DataParameter<Byte> MAIN_HAND = EntityDataManager.createKey(EntityPlayer.class, DataSerializers.BYTE);
	protected static final DataParameter<NBTTagCompound> LEFT_SHOULDER_ENTITY = EntityDataManager.createKey(EntityPlayer.class, DataSerializers.COMPOUND_TAG);
	protected static final DataParameter<NBTTagCompound> RIGHT_SHOULDER_ENTITY = EntityDataManager.createKey(EntityPlayer.class, DataSerializers.COMPOUND_TAG);
	private static final DataParameter<Float> ABSORPTION = EntityDataManager.createKey(EntityPlayer.class, DataSerializers.FLOAT);
	private static final DataParameter<Integer> PLAYER_SCORE = EntityDataManager.createKey(EntityPlayer.class, DataSerializers.VARINT);
	/**
	 * The player's unique game profile
	 */
	private final GameProfile gameProfile;
	private final CooldownTracker cooldownTracker = createCooldownTracker();
	/**
	 * Inventory of the player
	 */
	public InventoryPlayer inventory = new InventoryPlayer(this);
	/**
	 * The Container for the player's inventory (which opens when they press E)
	 */
	public Container inventoryContainer;
	/**
	 * The Container the player has open.
	 */
	public Container openContainer;
	public float prevCameraYaw;
	public float cameraYaw;
	/**
	 * Used by EntityPlayer to prevent too many xp orbs from getting absorbed at once.
	 */
	public int xpCooldown;
	/**
	 * Previous X position of the player's cape
	 */
	public double prevChasingPosX;
	/**
	 * Previous Y position of the player's cape
	 */
	public double prevChasingPosY;
	/**
	 * Previous Z position of the player's cape
	 */
	public double prevChasingPosZ;
	/**
	 * Current X position of the player's cape
	 */
	public double chasingPosX;
	/**
	 * Current Y position of the player's cape
	 */
	public double chasingPosY;
	/**
	 * Current Z position of the player's cape
	 */
	public double chasingPosZ;
	/**
	 * The location of the bed the player is sleeping in, or {@code null} if they are not sleeping
	 */
	public BlockPos bedLocation;
	/**
	 * Offset in the X axis used for rendering. This field is {@linkplain #setRenderOffsetForSleep() used by beds}.
	 */
	public float renderOffsetX;
	/**
	 * Offset in the Y axis used for rendering. This field is not written to in vanilla (other than being set to 0 each
	 * tick by {@link net.minecraft.client.entity.EntityOtherPlayerMP#onUpdate()}).
	 */
	public float renderOffsetY;
	/**
	 * Offset in the Z axis used for rendering. This field is {@linkplain #setRenderOffsetForSleep() used by beds}.
	 */
	public float renderOffsetZ;
	/**
	 * The player's capabilities. (See class PlayerCapabilities)
	 */
	public PlayerCapabilities capabilities = new PlayerCapabilities();
	/**
	 * The current experience level the player is on.
	 */
	public int experienceLevel;
	/**
	 * The total amount of experience the player has. This also includes the amount of experience within their
	 * Experience Bar.
	 */
	public int experienceTotal;
	/**
	 * The current amount of experience the player has within their Experience Bar.
	 */
	public float experience;

	@Nullable

	/**
	 * An instance of a fishing rod's hook. If this isn't null, the icon image of the fishing rod is slightly different
	 */
	public EntityFishHook fishEntity;
	protected InventoryEnderChest enderChest = new InventoryEnderChest();
	/**
	 * The food object of the player, the general hunger logic.
	 */
	protected FoodStats foodStats = new FoodStats();
	/**
	 * Used to tell if the player pressed jump twice. If this is at 0 and it's pressed (And they are allowed to fly, as
	 * defined in the player's movementInput) it sets this to 7. If it's pressed and it's greater than 0 enable fly.
	 */
	protected int flyToggleTimer;
	/**
	 * Boolean value indicating weather a player is sleeping or not
	 */
	protected boolean sleeping;
	protected int xpSeed;
	protected float speedInAir = 0.02F;
	private int sleepTimer;
	/**
	 * holds the spawn chunk of the player
	 */
	private BlockPos spawnPos;
	/**
	 * Whether this player's spawn point is forced, preventing execution of bed checks.
	 */
	private boolean spawnForced;
	private int lastXPSound;
	private boolean hasReducedDebug;
	private ItemStack itemStackMainHand = ItemStack.EMPTY;

	public EntityPlayer(World worldIn, GameProfile gameProfileIn) {

		super(worldIn);
		setUniqueId(getUUID(gameProfileIn));
		gameProfile = gameProfileIn;
		inventoryContainer = new ContainerPlayer(inventory, !worldIn.isRemote, this);
		openContainer = inventoryContainer;
		BlockPos blockpos = worldIn.getSpawnPoint();
		setLocationAndAngles((double) blockpos.getX() + 0.5D, blockpos.getY() + 1, (double) blockpos.getZ() + 0.5D, 0.0F, 0.0F);
		unused180 = 180.0F;
	}

	public static void registerFixesPlayer(DataFixer fixer) {

		fixer.registerWalker(FixTypes.PLAYER, (fixer1, compound, versionIn) -> {

			DataFixesManager.processInventory(fixer1, compound, versionIn, "Inventory");
			DataFixesManager.processInventory(fixer1, compound, versionIn, "EnderItems");

			if (compound.hasKey("ShoulderEntityLeft", 10)) {
				compound.setTag("ShoulderEntityLeft", fixer1.process(FixTypes.ENTITY, compound.getCompoundTag("ShoulderEntityLeft"), versionIn));
			}

			if (compound.hasKey("ShoulderEntityRight", 10)) {
				compound.setTag("ShoulderEntityRight", fixer1.process(FixTypes.ENTITY, compound.getCompoundTag("ShoulderEntityRight"), versionIn));
			}

			return compound;
		});
	}

	@Nullable

	/**
	 * Return null if bed is invalid
	 */
	public static BlockPos getBedSpawnLocation(World worldIn, BlockPos bedLocation, boolean forceSpawn) {

		Block block = worldIn.getBlockState(bedLocation).getBlock();

		if (block != Blocks.BED) {
			if (!forceSpawn) {
				return null;
			} else {
				boolean flag = block.canSpawnInBlock();
				boolean flag1 = worldIn.getBlockState(bedLocation.up()).getBlock().canSpawnInBlock();
				return flag && flag1 ? bedLocation : null;
			}
		} else {
			return BlockBed.getSafeExitLocation(worldIn, bedLocation, 0);
		}
	}

	/**
	 * Gets a players UUID given their GameProfie
	 */
	public static UUID getUUID(GameProfile profile) {

		UUID uuid = profile.getId();

		if (uuid == null) {
			uuid = getOfflineUUID(profile.getName());
		}

		return uuid;
	}

	public static UUID getOfflineUUID(String username) {

		return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
	}

	protected CooldownTracker createCooldownTracker() {

		return new CooldownTracker();
	}

	protected void applyEntityAttributes() {

		super.applyEntityAttributes();
		getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
		getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.10000000149011612D);
		getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_SPEED);
		getAttributeMap().registerAttribute(SharedMonsterAttributes.LUCK);
	}

	protected void entityInit() {

		super.entityInit();
		dataManager.register(ABSORPTION, 0.0F);
		dataManager.register(PLAYER_SCORE, 0);
		dataManager.register(PLAYER_MODEL_FLAG, (byte) 0);
		dataManager.register(MAIN_HAND, (byte) 1);
		dataManager.register(LEFT_SHOULDER_ENTITY, new NBTTagCompound());
		dataManager.register(RIGHT_SHOULDER_ENTITY, new NBTTagCompound());
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void onUpdate() {

		noClip = isSpectator();

		if (isSpectator()) {
			onGround = false;
		}

		if (xpCooldown > 0) {
			--xpCooldown;
		}

		if (isPlayerSleeping()) {
			++sleepTimer;

			if (sleepTimer > 100) {
				sleepTimer = 100;
			}

			if (!world.isRemote) {
				if (!isInBed()) {
					wakeUpPlayer(true, true, false);
				} else if (world.isDaytime()) {
					wakeUpPlayer(false, true, true);
				}
			}
		} else if (sleepTimer > 0) {
			++sleepTimer;

			if (sleepTimer >= 110) {
				sleepTimer = 0;
			}
		}

		super.onUpdate();

		if (!world.isRemote && openContainer != null && !openContainer.canInteractWith(this)) {
			closeScreen();
			openContainer = inventoryContainer;
		}

		if (isBurning() && capabilities.disableDamage) {
			extinguish();
		}

		updateCape();

		if (!world.isRemote) {
			foodStats.onUpdate(this);
			addStat(StatList.PLAY_ONE_MINUTE);

			if (isEntityAlive()) {
				addStat(StatList.TIME_SINCE_DEATH);
			}

			if (isSneaking()) {
				addStat(StatList.SNEAK_TIME);
			}
		}

		int i = 29999999;
		double d0 = MathHelper.clamp(posX, -2.9999999E7D, 2.9999999E7D);
		double d1 = MathHelper.clamp(posZ, -2.9999999E7D, 2.9999999E7D);

		if (d0 != posX || d1 != posZ) {
			setPosition(d0, posY, d1);
		}

		++ticksSinceLastSwing;
		ItemStack itemstack = getHeldItemMainhand();

		if (!ItemStack.areItemStacksEqual(itemStackMainHand, itemstack)) {
			if (!ItemStack.areItemsEqualIgnoreDurability(itemStackMainHand, itemstack)) {
				resetCooldown();
			}

			itemStackMainHand = itemstack.isEmpty() ? ItemStack.EMPTY : itemstack.copy();
		}

		cooldownTracker.tick();
		updateSize();
	}

	private void updateCape() {

		prevChasingPosX = chasingPosX;
		prevChasingPosY = chasingPosY;
		prevChasingPosZ = chasingPosZ;
		double d0 = posX - chasingPosX;
		double d1 = posY - chasingPosY;
		double d2 = posZ - chasingPosZ;
		double d3 = 10.0D;

		if (d0 > 10.0D) {
			chasingPosX = posX;
			prevChasingPosX = chasingPosX;
		}

		if (d2 > 10.0D) {
			chasingPosZ = posZ;
			prevChasingPosZ = chasingPosZ;
		}

		if (d1 > 10.0D) {
			chasingPosY = posY;
			prevChasingPosY = chasingPosY;
		}

		if (d0 < -10.0D) {
			chasingPosX = posX;
			prevChasingPosX = chasingPosX;
		}

		if (d2 < -10.0D) {
			chasingPosZ = posZ;
			prevChasingPosZ = chasingPosZ;
		}

		if (d1 < -10.0D) {
			chasingPosY = posY;
			prevChasingPosY = chasingPosY;
		}

		chasingPosX += d0 * 0.25D;
		chasingPosZ += d2 * 0.25D;
		chasingPosY += d1 * 0.25D;
	}

	protected void updateSize() {

		float f;
		float f1;

		if (isElytraFlying()) {
			f = 0.6F;
			f1 = 0.6F;
		} else if (isPlayerSleeping()) {
			f = 0.2F;
			f1 = 0.2F;
		} else if (isSneaking()) {
			f = 0.6F;
			f1 = 1.65F;
		} else {
			f = 0.6F;
			f1 = 1.8F;
		}

		if (f != width || f1 != height) {
			AxisAlignedBB axisalignedbb = getEntityBoundingBox();
			axisalignedbb = new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double) f, axisalignedbb.minY + (double) f1, axisalignedbb.minZ + (double) f);

			if (!world.collidesWithAnyBlock(axisalignedbb)) {
				setSize(f, f1);
			}
		}
	}

	/**
	 * Return the amount of time this entity should stay in a portal before being transported.
	 */
	public int getMaxInPortalTime() {

		return capabilities.disableDamage ? 1 : 80;
	}

	protected SoundEvent getSwimSound() {

		return SoundEvents.ENTITY_PLAYER_SWIM;
	}

	protected SoundEvent getSplashSound() {

		return SoundEvents.ENTITY_PLAYER_SPLASH;
	}

	/**
	 * Return the amount of cooldown before this entity can use a portal again.
	 */
	public int getPortalCooldown() {

		return 10;
	}

	public void playSound(SoundEvent soundIn, float volume, float pitch) {

		world.playSound(this, posX, posY, posZ, soundIn, getSoundCategory(), volume, pitch);
	}

	public SoundCategory getSoundCategory() {

		return SoundCategory.PLAYERS;
	}

	protected int getFireImmuneTicks() {

		return 20;
	}

	/**
	 * Handler for {@link World#setEntityState}
	 */
	public void handleStatusUpdate(byte id) {

		if (id == 9) {
			onItemUseFinish();
		} else if (id == 23) {
			hasReducedDebug = false;
		} else if (id == 22) {
			hasReducedDebug = true;
		} else {
			super.handleStatusUpdate(id);
		}
	}

	/**
	 * Dead and sleeping entities cannot move
	 */
	protected boolean isMovementBlocked() {

		return getHealth() <= 0.0F || isPlayerSleeping();
	}

	/**
	 * set current crafting inventory back to the 2x2 square
	 */
	protected void closeScreen() {

		openContainer = inventoryContainer;
	}

	/**
	 * Handles updating while riding another entity
	 */
	public void updateRidden() {

		if (!world.isRemote && isSneaking() && isRiding()) {
			dismountRidingEntity();
			setSneaking(false);
		} else {
			double d0 = posX;
			double d1 = posY;
			double d2 = posZ;
			float f = rotationYaw;
			float f1 = rotationPitch;
			super.updateRidden();
			prevCameraYaw = cameraYaw;
			cameraYaw = 0.0F;
			addMountedMovementStat(posX - d0, posY - d1, posZ - d2);

			if (getRidingEntity() instanceof EntityPig) {
				rotationPitch = f1;
				rotationYaw = f;
				renderYawOffset = ((EntityPig) getRidingEntity()).renderYawOffset;
			}
		}
	}

	/**
	 * Keeps moving the entity up so it isn't colliding with blocks and other requirements for this entity to be spawned
	 * (only actually used on players though its also on Entity)
	 */
	public void preparePlayerToSpawn() {

		setSize(0.6F, 1.8F);
		super.preparePlayerToSpawn();
		setHealth(getMaxHealth());
		deathTime = 0;
	}

	protected void updateEntityActionState() {

		super.updateEntityActionState();
		updateArmSwingProgress();
		rotationYawHead = rotationYaw;
	}

	/**
	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
	 * use this to react to sunlight and start to burn.
	 */
	public void onLivingUpdate() {

		if (flyToggleTimer > 0) {
			--flyToggleTimer;
		}

		if (world.getDifficulty() == EnumDifficulty.PEACEFUL && world.getGameRules().getBoolean("naturalRegeneration")) {
			if (getHealth() < getMaxHealth() && ticksExisted % 20 == 0) {
				heal(1.0F);
			}

			if (foodStats.needFood() && ticksExisted % 10 == 0) {
				foodStats.setFoodLevel(foodStats.getFoodLevel() + 1);
			}
		}

		inventory.decrementAnimations();
		prevCameraYaw = cameraYaw;
		super.onLivingUpdate();
		IAttributeInstance iattributeinstance = getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);

		if (!world.isRemote) {
			iattributeinstance.setBaseValue(capabilities.getWalkSpeed());
		}

		jumpMovementFactor = speedInAir;

		if (isSprinting()) {
			jumpMovementFactor = (float) ((double) jumpMovementFactor + (double) speedInAir * 0.3D);
		}

		setAIMoveSpeed((float) iattributeinstance.getAttributeValue());
		float f = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
		float f1 = (float) (Math.atan(-motionY * 0.20000000298023224D) * 15.0D);

		if (f > 0.1F) {
			f = 0.1F;
		}

		if (!onGround || getHealth() <= 0.0F) {
			f = 0.0F;
		}

		if (onGround || getHealth() <= 0.0F) {
			f1 = 0.0F;
		}

		cameraYaw += (f - cameraYaw) * 0.4F;
		cameraPitch += (f1 - cameraPitch) * 0.8F;

		if (getHealth() > 0.0F && !isSpectator()) {
			AxisAlignedBB axisalignedbb;

			if (isRiding() && !getRidingEntity().isDead) {
				axisalignedbb = getEntityBoundingBox().union(getRidingEntity().getEntityBoundingBox()).grow(1.0D, 0.0D, 1.0D);
			} else {
				axisalignedbb = getEntityBoundingBox().grow(1.0D, 0.5D, 1.0D);
			}

			List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this, axisalignedbb);

			for (Entity entity : list) {
				if (!entity.isDead) {
					collideWithPlayer(entity);
				}
			}
		}

		playShoulderEntityAmbientSound(getLeftShoulderEntity());
		playShoulderEntityAmbientSound(getRightShoulderEntity());

		if (!world.isRemote && (fallDistance > 0.5F || isInWater() || isRiding()) || capabilities.isFlying) {
			spawnShoulderEntities();
		}
	}

	private void playShoulderEntityAmbientSound(@Nullable NBTTagCompound p_192028_1_) {

		if (p_192028_1_ != null && !p_192028_1_.hasKey("Silent") || !p_192028_1_.getBoolean("Silent")) {
			String s = p_192028_1_.getString("id");

			if (s.equals(EntityList.getKey(EntityParrot.class).toString())) {
				EntityParrot.playAmbientSound(world, this);
			}
		}
	}

	private void collideWithPlayer(Entity entityIn) {

		entityIn.onCollideWithPlayer(this);
	}

	public int getScore() {

		return dataManager.get(PLAYER_SCORE);
	}

	/**
	 * Set player's score
	 */
	public void setScore(int scoreIn) {

		dataManager.set(PLAYER_SCORE, scoreIn);
	}

	/**
	 * Add to player's score
	 */
	public void addScore(int scoreIn) {

		int i = getScore();
		dataManager.set(PLAYER_SCORE, i + scoreIn);
	}

	/**
	 * Called when the mob's health reaches 0.
	 */
	public void onDeath(DamageSource cause) {

		super.onDeath(cause);
		setSize(0.2F, 0.2F);
		setPosition(posX, posY, posZ);
		motionY = 0.10000000149011612D;

		if ("Notch".equals(getName())) {
			dropItem(new ItemStack(Items.APPLE, 1), true, false);
		}

		if (!world.getGameRules().getBoolean("keepInventory") && !isSpectator()) {
			destroyVanishingCursedItems();
			inventory.dropAllItems();
		}

		if (cause != null) {
			motionX = -MathHelper.cos((attackedAtYaw + rotationYaw) * 0.017453292F) * 0.1F;
			motionZ = -MathHelper.sin((attackedAtYaw + rotationYaw) * 0.017453292F) * 0.1F;
		} else {
			motionX = 0.0D;
			motionZ = 0.0D;
		}

		addStat(StatList.DEATHS);
		takeStat(StatList.TIME_SINCE_DEATH);
		extinguish();
		setFlag(0, false);
	}

	protected void destroyVanishingCursedItems() {

		for (int i = 0; i < inventory.getSizeInventory(); ++i) {
			ItemStack itemstack = inventory.getStackInSlot(i);

			if (!itemstack.isEmpty() && EnchantmentHelper.hasVanishingCurse(itemstack)) {
				inventory.removeStackFromSlot(i);
			}
		}
	}

	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {

		if (damageSourceIn == DamageSource.ON_FIRE) {
			return SoundEvents.ENTITY_PLAYER_HURT_ON_FIRE;
		} else {
			return damageSourceIn == DamageSource.DROWN ? SoundEvents.ENTITY_PLAYER_HURT_DROWN : SoundEvents.ENTITY_PLAYER_HURT;
		}
	}

	protected SoundEvent getDeathSound() {

		return SoundEvents.ENTITY_PLAYER_DEATH;
	}

	@Nullable

	/**
	 * Drop one item out of the currently selected stack if {@code dropAll} is false. If {@code dropItem} is true the
	 * entire stack is dropped.
	 */
	public EntityItem dropItem(boolean dropAll) {

		return dropItem(inventory.decrStackSize(inventory.currentItem, dropAll && !inventory.getCurrentItem().isEmpty() ? inventory.getCurrentItem().getCount() : 1), false, true);
	}

	@Nullable

	/**
	 * Drops an item into the world.
	 */
	public EntityItem dropItem(ItemStack itemStackIn, boolean unused) {

		return dropItem(itemStackIn, false, unused);
	}

	@Nullable
	public EntityItem dropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem) {

		if (droppedItem.isEmpty()) {
			return null;
		} else {
			double d0 = posY - 0.30000001192092896D + (double) getEyeHeight();
			EntityItem entityitem = new EntityItem(world, posX, d0, posZ, droppedItem);
			entityitem.setPickupDelay(40);

			if (traceItem) {
				entityitem.setThrower(getName());
			}

			if (dropAround) {
				float f = rand.nextFloat() * 0.5F;
				float f1 = rand.nextFloat() * ((float) Math.PI * 2F);
				entityitem.motionX = -MathHelper.sin(f1) * f;
				entityitem.motionZ = MathHelper.cos(f1) * f;
				entityitem.motionY = 0.20000000298023224D;
			} else {
				float f2 = 0.3F;
				entityitem.motionX = -MathHelper.sin(rotationYaw * 0.017453292F) * MathHelper.cos(rotationPitch * 0.017453292F) * f2;
				entityitem.motionZ = MathHelper.cos(rotationYaw * 0.017453292F) * MathHelper.cos(rotationPitch * 0.017453292F) * f2;
				entityitem.motionY = -MathHelper.sin(rotationPitch * 0.017453292F) * f2 + 0.1F;
				float f3 = rand.nextFloat() * ((float) Math.PI * 2F);
				f2 = 0.02F * rand.nextFloat();
				entityitem.motionX += Math.cos(f3) * (double) f2;
				entityitem.motionY += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
				entityitem.motionZ += Math.sin(f3) * (double) f2;
			}

			ItemStack itemstack = dropItemAndGetStack(entityitem);

			if (traceItem) {
				if (!itemstack.isEmpty()) {
					addStat(StatList.getDroppedObjectStats(itemstack.getItem()), droppedItem.getCount());
				}

				addStat(StatList.DROP);
			}

			return entityitem;
		}
	}

	protected ItemStack dropItemAndGetStack(EntityItem p_184816_1_) {

		world.spawnEntity(p_184816_1_);
		return p_184816_1_.getItem();
	}

	public float getDigSpeed(IBlockState state) {

		float f = inventory.getDestroySpeed(state);

		if (f > 1.0F) {
			int i = EnchantmentHelper.getEfficiencyModifier(this);
			ItemStack itemstack = getHeldItemMainhand();

			if (i > 0 && !itemstack.isEmpty()) {
				f += (float) (i * i + 1);
			}
		}

		if (isPotionActive(MobEffects.HASTE)) {
			f *= 1.0F + (float) (getActivePotionEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2F;
		}

		if (isPotionActive(MobEffects.MINING_FATIGUE)) {
			float f1 = switch (getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
				case 0 -> 0.3F;
				case 1 -> 0.09F;
				case 2 -> 0.0027F;
				default -> 8.1E-4F;
			};

			f *= f1;
		}

		if (isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(this)) {
			f /= 5.0F;
		}

		if (!onGround) {
			f /= 5.0F;
		}

		return f;
	}

	public boolean canHarvestBlock(IBlockState state) {

		return inventory.canHarvestBlock(state);
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound compound) {

		super.readEntityFromNBT(compound);
		setUniqueId(getUUID(gameProfile));
		NBTTagList nbttaglist = compound.getTagList("Inventory", 10);
		inventory.readFromNBT(nbttaglist);
		inventory.currentItem = compound.getInteger("SelectedItemSlot");
		sleeping = compound.getBoolean("Sleeping");
		sleepTimer = compound.getShort("SleepTimer");
		experience = compound.getFloat("XpP");
		experienceLevel = compound.getInteger("XpLevel");
		experienceTotal = compound.getInteger("XpTotal");
		xpSeed = compound.getInteger("XpSeed");

		if (xpSeed == 0) {
			xpSeed = rand.nextInt();
		}

		setScore(compound.getInteger("Score"));

		if (sleeping) {
			bedLocation = new BlockPos(this);
			wakeUpPlayer(true, true, false);
		}

		if (compound.hasKey("SpawnX", 99) && compound.hasKey("SpawnY", 99) && compound.hasKey("SpawnZ", 99)) {
			spawnPos = new BlockPos(compound.getInteger("SpawnX"), compound.getInteger("SpawnY"), compound.getInteger("SpawnZ"));
			spawnForced = compound.getBoolean("SpawnForced");
		}

		foodStats.readNBT(compound);
		capabilities.readCapabilitiesFromNBT(compound);

		if (compound.hasKey("EnderItems", 9)) {
			NBTTagList nbttaglist1 = compound.getTagList("EnderItems", 10);
			enderChest.loadInventoryFromNBT(nbttaglist1);
		}

		if (compound.hasKey("ShoulderEntityLeft", 10)) {
			setLeftShoulderEntity(compound.getCompoundTag("ShoulderEntityLeft"));
		}

		if (compound.hasKey("ShoulderEntityRight", 10)) {
			setRightShoulderEntity(compound.getCompoundTag("ShoulderEntityRight"));
		}
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound compound) {

		super.writeEntityToNBT(compound);
		compound.setInteger("DataVersion", 1343);
		compound.setTag("Inventory", inventory.writeToNBT(new NBTTagList()));
		compound.setInteger("SelectedItemSlot", inventory.currentItem);
		compound.setBoolean("Sleeping", sleeping);
		compound.setShort("SleepTimer", (short) sleepTimer);
		compound.setFloat("XpP", experience);
		compound.setInteger("XpLevel", experienceLevel);
		compound.setInteger("XpTotal", experienceTotal);
		compound.setInteger("XpSeed", xpSeed);
		compound.setInteger("Score", getScore());

		if (spawnPos != null) {
			compound.setInteger("SpawnX", spawnPos.getX());
			compound.setInteger("SpawnY", spawnPos.getY());
			compound.setInteger("SpawnZ", spawnPos.getZ());
			compound.setBoolean("SpawnForced", spawnForced);
		}

		foodStats.writeNBT(compound);
		capabilities.writeCapabilitiesToNBT(compound);
		compound.setTag("EnderItems", enderChest.saveInventoryToNBT());

		if (!getLeftShoulderEntity().hasNoTags()) {
			compound.setTag("ShoulderEntityLeft", getLeftShoulderEntity());
		}

		if (!getRightShoulderEntity().hasNoTags()) {
			compound.setTag("ShoulderEntityRight", getRightShoulderEntity());
		}
	}

	/**
	 * Called when the entity is attacked.
	 */
	public boolean attackEntityFrom(DamageSource source, float amount) {

		if (isEntityInvulnerable(source)) {
			return false;
		} else if (capabilities.disableDamage && !source.canHarmInCreative()) {
			return false;
		} else {
			idleTime = 0;

			if (getHealth() <= 0.0F) {
				return false;
			} else {
				if (isPlayerSleeping() && !world.isRemote) {
					wakeUpPlayer(true, true, false);
				}

				spawnShoulderEntities();

				if (source.isDifficultyScaled()) {
					if (world.getDifficulty() == EnumDifficulty.PEACEFUL) {
						amount = 0.0F;
					}

					if (world.getDifficulty() == EnumDifficulty.EASY) {
						amount = Math.min(amount / 2.0F + 1.0F, amount);
					}

					if (world.getDifficulty() == EnumDifficulty.HARD) {
						amount = amount * 3.0F / 2.0F;
					}
				}

				return amount != 0.0F && super.attackEntityFrom(source, amount);
			}
		}
	}

	protected void blockUsingShield(EntityLivingBase p_190629_1_) {

		super.blockUsingShield(p_190629_1_);

		if (p_190629_1_.getHeldItemMainhand().getItem() instanceof ItemAxe) {
			disableShield(true);
		}
	}

	public boolean canAttackPlayer(EntityPlayer other) {

		Team team = getTeam();
		Team team1 = other.getTeam();

		if (team == null) {
			return true;
		} else {
			return !team.isSameTeam(team1) || team.getAllowFriendlyFire();
		}
	}

	protected void damageArmor(float damage) {

		inventory.damageArmor(damage);
	}

	protected void damageShield(float damage) {

		if (damage >= 3.0F && activeItemStack.getItem() == Items.SHIELD) {
			int i = 1 + MathHelper.floor(damage);
			activeItemStack.damageItem(i, this);

			if (activeItemStack.isEmpty()) {
				EnumHand enumhand = getActiveHand();

				if (enumhand == EnumHand.MAIN_HAND) {
					setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
				} else {
					setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY);
				}

				activeItemStack = ItemStack.EMPTY;
				playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + world.rand.nextFloat() * 0.4F);
			}
		}
	}

	/**
	 * When searching for vulnerable players, if a player is invisible, the return value of this is the chance of seeing
	 * them anyway.
	 */
	public float getArmorVisibility() {

		int i = 0;

		for (ItemStack itemstack : inventory.armorInventory) {
			if (!itemstack.isEmpty()) {
				++i;
			}
		}

		return (float) i / (float) inventory.armorInventory.size();
	}

	/**
	 * Deals damage to the entity. This will take the armor of the entity into consideration before damaging the health
	 * bar.
	 */
	protected void damageEntity(DamageSource damageSrc, float damageAmount) {

		if (!isEntityInvulnerable(damageSrc)) {
			damageAmount = applyArmorCalculations(damageSrc, damageAmount);
			damageAmount = applyPotionDamageCalculations(damageSrc, damageAmount);
			float f = damageAmount;
			damageAmount = Math.max(damageAmount - getAbsorptionAmount(), 0.0F);
			setAbsorptionAmount(getAbsorptionAmount() - (f - damageAmount));

			if (damageAmount != 0.0F) {
				addExhaustion(damageSrc.getHungerDamage());
				float f1 = getHealth();
				setHealth(getHealth() - damageAmount);
				getCombatTracker().trackDamage(damageSrc, f1, damageAmount);

				if (damageAmount < 3.4028235E37F) {
					addStat(StatList.DAMAGE_TAKEN, Math.round(damageAmount * 10.0F));
				}
			}
		}
	}

	public void openEditSign(TileEntitySign signTile) {

	}

	public void displayGuiEditCommandCart(CommandBlockBaseLogic commandBlock) {

	}

	public void displayGuiCommandBlock(TileEntityCommandBlock commandBlock) {

	}

	public void openEditStructure(TileEntityStructure structure) {

	}

	public void displayVillagerTradeGui(IMerchant villager) {

	}

	/**
	 * Displays the GUI for interacting with a chest inventory.
	 */
	public void displayGUIChest(IInventory chestInventory) {

	}

	public void openGuiHorseInventory(AbstractHorse horse, IInventory inventoryIn) {

	}

	public void displayGui(IInteractionObject guiOwner) {

	}

	public void openBook(ItemStack stack, EnumHand hand) {

	}

	public EnumActionResult interactOn(Entity p_190775_1_, EnumHand p_190775_2_) {

		if (isSpectator()) {
			if (p_190775_1_ instanceof IInventory) {
				displayGUIChest((IInventory) p_190775_1_);
			}

			return EnumActionResult.PASS;
		} else {
			ItemStack itemstack = getHeldItem(p_190775_2_);
			ItemStack itemstack1 = itemstack.isEmpty() ? ItemStack.EMPTY : itemstack.copy();

			if (p_190775_1_.processInitialInteract(this, p_190775_2_)) {
				if (capabilities.isCreativeMode && itemstack == getHeldItem(p_190775_2_) && itemstack.getCount() < itemstack1.getCount()) {
					itemstack.setCount(itemstack1.getCount());
				}

				return EnumActionResult.SUCCESS;
			} else {
				if (!itemstack.isEmpty() && p_190775_1_ instanceof EntityLivingBase) {
					if (capabilities.isCreativeMode) {
						itemstack = itemstack1;
					}

					if (itemstack.interactWithEntity(this, (EntityLivingBase) p_190775_1_, p_190775_2_)) {
						if (itemstack.isEmpty() && !capabilities.isCreativeMode) {
							setHeldItem(p_190775_2_, ItemStack.EMPTY);
						}

						return EnumActionResult.SUCCESS;
					}
				}

				return EnumActionResult.PASS;
			}
		}
	}

	/**
	 * Returns the Y Offset of this entity.
	 */
	public double getYOffset() {

		return -0.35D;
	}

	/**
	 * Dismounts this entity from the entity it is riding.
	 */
	public void dismountRidingEntity() {

		super.dismountRidingEntity();
		rideCooldown = 0;
	}

	/**
	 * Attacks for the player the targeted entity with the currently equipped item.  The equipped item has hitEntity
	 * called on it. Args: targetEntity
	 */
	public void attackTargetEntityWithCurrentItem(Entity targetEntity) {

		if (targetEntity.canBeAttackedWithItem()) {
			if (!targetEntity.hitByEntity(this)) {
				float f = (float) getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
				float f1;

				if (targetEntity instanceof EntityLivingBase) {
					f1 = EnchantmentHelper.getModifierForCreature(getHeldItemMainhand(), ((EntityLivingBase) targetEntity).getCreatureAttribute());
				} else {
					f1 = EnchantmentHelper.getModifierForCreature(getHeldItemMainhand(), EnumCreatureAttribute.UNDEFINED);
				}

				float f2 = getCooledAttackStrength(0.5F);
				f = f * (0.2F + f2 * f2 * 0.8F);
				f1 = f1 * f2;
				resetCooldown();

				if (f > 0.0F || f1 > 0.0F) {
					boolean flag = f2 > 0.9F;
					boolean flag1 = false;
					int i = 0;
					i = i + EnchantmentHelper.getKnockbackModifier(this);

					if (isSprinting() && flag) {
						world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, getSoundCategory(), 1.0F, 1.0F);
						++i;
						flag1 = true;
					}

					boolean flag2 = flag && fallDistance > 0.0F && !onGround && !isOnLadder() && !isInWater() && !isPotionActive(MobEffects.BLINDNESS) && !isRiding() && targetEntity instanceof EntityLivingBase;
					flag2 = flag2 && !isSprinting();

					if (flag2) {
						f *= 1.5F;
					}

					f = f + f1;
					boolean flag3 = false;
					double d0 = distanceWalkedModified - prevDistanceWalkedModified;

					if (flag && !flag2 && !flag1 && onGround && d0 < (double) getAIMoveSpeed()) {
						ItemStack itemstack = getHeldItem(EnumHand.MAIN_HAND);

						if (itemstack.getItem() instanceof ItemSword) {
							flag3 = true;
						}
					}

					float f4 = 0.0F;
					boolean flag4 = false;
					int j = EnchantmentHelper.getFireAspectModifier(this);

					if (targetEntity instanceof EntityLivingBase) {
						f4 = ((EntityLivingBase) targetEntity).getHealth();

						if (j > 0 && !targetEntity.isBurning()) {
							flag4 = true;
							targetEntity.setFire(1);
						}
					}

					double d1 = targetEntity.motionX;
					double d2 = targetEntity.motionY;
					double d3 = targetEntity.motionZ;
					boolean flag5 = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage(this), f);

					if (flag5) {
						if (i > 0) {
							if (targetEntity instanceof EntityLivingBase) {
								((EntityLivingBase) targetEntity).knockBack(this, (float) i * 0.5F, MathHelper.sin(rotationYaw * 0.017453292F), -MathHelper.cos(rotationYaw * 0.017453292F));
							} else {
								targetEntity.addVelocity(-MathHelper.sin(rotationYaw * 0.017453292F) * (float) i * 0.5F, 0.1D, MathHelper.cos(rotationYaw * 0.017453292F) * (float) i * 0.5F);
							}

							motionX *= 0.6D;
							motionZ *= 0.6D;
							setSprinting(false);
						}

						if (flag3) {
							float f3 = 1.0F + EnchantmentHelper.getSweepingDamageRatio(this) * f;

							for (EntityLivingBase entitylivingbase : world.getEntitiesWithinAABB(EntityLivingBase.class, targetEntity.getEntityBoundingBox().grow(1.0D, 0.25D, 1.0D))) {
								if (entitylivingbase != this && entitylivingbase != targetEntity && !isOnSameTeam(entitylivingbase) && getDistanceSq(entitylivingbase) < 9.0D) {
									entitylivingbase.knockBack(this, 0.4F, MathHelper.sin(rotationYaw * 0.017453292F), -MathHelper.cos(rotationYaw * 0.017453292F));
									entitylivingbase.attackEntityFrom(DamageSource.causePlayerDamage(this), f3);
								}
							}

							world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, getSoundCategory(), 1.0F, 1.0F);
							spawnSweepParticles();
						}

						if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged) {
							((EntityPlayerMP) targetEntity).connection.sendPacket(new SPacketEntityVelocity(targetEntity));
							targetEntity.velocityChanged = false;
							targetEntity.motionX = d1;
							targetEntity.motionY = d2;
							targetEntity.motionZ = d3;
						}

						if (flag2) {
							world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, getSoundCategory(), 1.0F, 1.0F);
							onCriticalHit(targetEntity);
						}

						if (!flag2 && !flag3) {
							if (flag) {
								world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, getSoundCategory(), 1.0F, 1.0F);
							} else {
								world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, getSoundCategory(), 1.0F, 1.0F);
							}
						}

						if (f1 > 0.0F) {
							onEnchantmentCritical(targetEntity);
						}

						setLastAttackedEntity(targetEntity);

						if (targetEntity instanceof EntityLivingBase) {
							EnchantmentHelper.applyThornEnchantments((EntityLivingBase) targetEntity, this);
						}

						EnchantmentHelper.applyArthropodEnchantments(this, targetEntity);
						ItemStack itemstack1 = getHeldItemMainhand();
						Entity entity = targetEntity;

						if (targetEntity instanceof MultiPartEntityPart) {
							IEntityMultiPart ientitymultipart = ((MultiPartEntityPart) targetEntity).parent;

							if (ientitymultipart instanceof EntityLivingBase) {
								entity = (EntityLivingBase) ientitymultipart;
							}
						}

						if (!itemstack1.isEmpty() && entity instanceof EntityLivingBase) {
							itemstack1.hitEntity((EntityLivingBase) entity, this);

							if (itemstack1.isEmpty()) {
								setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
							}
						}

						if (targetEntity instanceof EntityLivingBase) {
							float f5 = f4 - ((EntityLivingBase) targetEntity).getHealth();
							addStat(StatList.DAMAGE_DEALT, Math.round(f5 * 10.0F));

							if (j > 0) {
								targetEntity.setFire(j * 4);
							}

							if (world instanceof WorldServer && f5 > 2.0F) {
								int k = (int) ((double) f5 * 0.5D);
								((WorldServer) world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, targetEntity.posX, targetEntity.posY + (double) (targetEntity.height * 0.5F), targetEntity.posZ, k, 0.1D, 0.0D, 0.1D, 0.2D);
							}
						}

						addExhaustion(0.1F);
					} else {
						world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, getSoundCategory(), 1.0F, 1.0F);

						if (flag4) {
							targetEntity.extinguish();
						}
					}
				}
			}
		}
	}

	public void disableShield(boolean p_190777_1_) {

		float f = 0.25F + (float) EnchantmentHelper.getEfficiencyModifier(this) * 0.05F;

		if (p_190777_1_) {
			f += 0.75F;
		}

		if (rand.nextFloat() < f) {
			getCooldownTracker().setCooldown(Items.SHIELD, 100);
			resetActiveHand();
			world.setEntityState(this, (byte) 30);
		}
	}

	/**
	 * Called when the entity is dealt a critical hit.
	 */
	public void onCriticalHit(Entity entityHit) {

	}

	public void onEnchantmentCritical(Entity entityHit) {

	}

	public void spawnSweepParticles() {

		double d0 = -MathHelper.sin(rotationYaw * 0.017453292F);
		double d1 = MathHelper.cos(rotationYaw * 0.017453292F);

		if (world instanceof WorldServer) {
			((WorldServer) world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, posX + d0, posY + (double) height * 0.5D, posZ + d1, 0, d0, 0.0D, d1, 0.0D);
		}
	}

	public void respawnPlayer() {

	}

	/**
	 * Will get destroyed next tick.
	 */
	public void setDead() {

		super.setDead();
		inventoryContainer.onContainerClosed(this);

		if (openContainer != null) {
			openContainer.onContainerClosed(this);
		}
	}

	/**
	 * Checks if this entity is inside of an opaque block
	 */
	public boolean isEntityInsideOpaqueBlock() {

		return !sleeping && super.isEntityInsideOpaqueBlock();
	}

	/**
	 * returns true if this is an EntityPlayerSP, or the logged in player.
	 */
	public boolean isUser() {

		return false;
	}

	/**
	 * Returns the GameProfile for this player
	 */
	public GameProfile getGameProfile() {

		return gameProfile;
	}

	public EntityPlayer.SleepResult trySleep(BlockPos bedLocation) {

		EnumFacing enumfacing = world.getBlockState(bedLocation).getValue(BlockHorizontal.FACING);

		if (!world.isRemote) {
			if (isPlayerSleeping() || !isEntityAlive()) {
				return EntityPlayer.SleepResult.OTHER_PROBLEM;
			}

			if (!world.provider.isSurfaceWorld()) {
				return EntityPlayer.SleepResult.NOT_POSSIBLE_HERE;
			}

			if (world.isDaytime()) {
				return EntityPlayer.SleepResult.NOT_POSSIBLE_NOW;
			}

			if (!bedInRange(bedLocation, enumfacing)) {
				return EntityPlayer.SleepResult.TOO_FAR_AWAY;
			}

			double d0 = 8.0D;
			double d1 = 5.0D;
			List<EntityMob> list = world.getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB((double) bedLocation.getX() - 8.0D, (double) bedLocation.getY() - 5.0D, (double) bedLocation.getZ() - 8.0D, (double) bedLocation.getX() + 8.0D, (double) bedLocation.getY() + 5.0D, (double) bedLocation.getZ() + 8.0D), new EntityPlayer.SleepEnemyPredicate(this));

			if (!list.isEmpty()) {
				return EntityPlayer.SleepResult.NOT_SAFE;
			}
		}

		if (isRiding()) {
			dismountRidingEntity();
		}

		spawnShoulderEntities();
		setSize(0.2F, 0.2F);

		if (world.isBlockLoaded(bedLocation)) {
			float f1 = 0.5F + (float) enumfacing.getFrontOffsetX() * 0.4F;
			float f = 0.5F + (float) enumfacing.getFrontOffsetZ() * 0.4F;
			setRenderOffsetForSleep(enumfacing);
			setPosition((float) bedLocation.getX() + f1, (float) bedLocation.getY() + 0.6875F, (float) bedLocation.getZ() + f);
		} else {
			setPosition((float) bedLocation.getX() + 0.5F, (float) bedLocation.getY() + 0.6875F, (float) bedLocation.getZ() + 0.5F);
		}

		sleeping = true;
		sleepTimer = 0;
		this.bedLocation = bedLocation;
		motionX = 0.0D;
		motionY = 0.0D;
		motionZ = 0.0D;

		if (!world.isRemote) {
			world.updateAllPlayersSleepingFlag();
		}

		return EntityPlayer.SleepResult.OK;
	}

	private boolean bedInRange(BlockPos p_190774_1_, EnumFacing p_190774_2_) {

		if (Math.abs(posX - (double) p_190774_1_.getX()) <= 3.0D && Math.abs(posY - (double) p_190774_1_.getY()) <= 2.0D && Math.abs(posZ - (double) p_190774_1_.getZ()) <= 3.0D) {
			return true;
		} else {
			BlockPos blockpos = p_190774_1_.offset(p_190774_2_.getOpposite());
			return Math.abs(posX - (double) blockpos.getX()) <= 3.0D && Math.abs(posY - (double) blockpos.getY()) <= 2.0D && Math.abs(posZ - (double) blockpos.getZ()) <= 3.0D;
		}
	}

	private void setRenderOffsetForSleep(EnumFacing bedDirection) {

		renderOffsetX = -1.8F * (float) bedDirection.getFrontOffsetX();
		renderOffsetZ = -1.8F * (float) bedDirection.getFrontOffsetZ();
	}

	/**
	 * Wake up the player if they're sleeping.
	 */
	public void wakeUpPlayer(boolean immediately, boolean updateWorldFlag, boolean setSpawn) {

		setSize(0.6F, 1.8F);
		IBlockState iblockstate = world.getBlockState(bedLocation);

		if (bedLocation != null && iblockstate.getBlock() == Blocks.BED) {
			world.setBlockState(bedLocation, iblockstate.withProperty(BlockBed.OCCUPIED, false), 4);
			BlockPos blockpos = BlockBed.getSafeExitLocation(world, bedLocation, 0);

			if (blockpos == null) {
				blockpos = bedLocation.up();
			}

			setPosition((float) blockpos.getX() + 0.5F, (float) blockpos.getY() + 0.1F, (float) blockpos.getZ() + 0.5F);
		}

		sleeping = false;

		if (!world.isRemote && updateWorldFlag) {
			world.updateAllPlayersSleepingFlag();
		}

		sleepTimer = immediately ? 0 : 100;

		if (setSpawn) {
			setSpawnPoint(bedLocation, false);
		}
	}

	private boolean isInBed() {

		return world.getBlockState(bedLocation).getBlock() == Blocks.BED;
	}

	/**
	 * Returns the orientation of the bed in degrees.
	 */
	public float getBedOrientationInDegrees() {

		if (bedLocation != null) {
			EnumFacing enumfacing = world.getBlockState(bedLocation).getValue(BlockHorizontal.FACING);

			switch (enumfacing) {
				case SOUTH:
					return 90.0F;

				case WEST:
					return 0.0F;

				case NORTH:
					return 270.0F;

				case EAST:
					return 180.0F;
			}
		}

		return 0.0F;
	}

	/**
	 * Returns whether player is sleeping or not
	 */
	public boolean isPlayerSleeping() {

		return sleeping;
	}

	/**
	 * Returns whether or not the player is asleep and the screen has fully faded.
	 */
	public boolean isPlayerFullyAsleep() {

		return sleeping && sleepTimer >= 100;
	}

	public int getSleepTimer() {

		return sleepTimer;
	}

	public void sendStatusMessage(ITextComponent chatComponent, boolean actionBar) {

	}

	public BlockPos getBedLocation() {

		return spawnPos;
	}

	public boolean isSpawnForced() {

		return spawnForced;
	}

	public void setSpawnPoint(BlockPos pos, boolean forced) {

		if (pos != null) {
			spawnPos = pos;
			spawnForced = forced;
		} else {
			spawnPos = null;
			spawnForced = false;
		}
	}

	/**
	 * Add a stat once
	 */
	public void addStat(StatBase stat) {

		addStat(stat, 1);
	}

	/**
	 * Adds a value to a statistic field.
	 */
	public void addStat(StatBase stat, int amount) {

	}

	public void takeStat(StatBase stat) {

	}

	public void unlockRecipes(List<IRecipe> p_192021_1_) {

	}

	public void unlockRecipes(ResourceLocation[] p_193102_1_) {

	}

	public void resetRecipes(List<IRecipe> p_192022_1_) {

	}

	/**
	 * Causes this entity to do an upwards motion (jumping).
	 */
	public void jump() {

		super.jump();
		addStat(StatList.JUMP);

		if (isSprinting()) {
			addExhaustion(0.2F);
		} else {
			addExhaustion(0.05F);
		}
	}

	public void travel(float strafe, float vertical, float forward) {

		double d0 = posX;
		double d1 = posY;
		double d2 = posZ;

		if (capabilities.isFlying && !isRiding()) {
			double d3 = motionY;
			float f = jumpMovementFactor;
			jumpMovementFactor = capabilities.getFlySpeed() * (float) (isSprinting() ? 2 : 1);
			super.travel(strafe, vertical, forward);
			motionY = d3 * 0.6D;
			jumpMovementFactor = f;
			fallDistance = 0.0F;
			setFlag(7, false);
		} else {
			super.travel(strafe, vertical, forward);
		}

		addMovementStat(posX - d0, posY - d1, posZ - d2);
	}

	/**
	 * the movespeed used for the new AI system
	 */
	public float getAIMoveSpeed() {

		return (float) getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
	}

	/**
	 * Adds a value to a movement statistic field - like run, walk, swin or climb.
	 */
	public void addMovementStat(double p_71000_1_, double p_71000_3_, double p_71000_5_) {

		if (!isRiding()) {
			if (isInsideOfMaterial(Material.WATER)) {
				int i = Math.round(MathHelper.sqrt(p_71000_1_ * p_71000_1_ + p_71000_3_ * p_71000_3_ + p_71000_5_ * p_71000_5_) * 100.0F);

				if (i > 0) {
					addStat(StatList.DIVE_ONE_CM, i);
					addExhaustion(0.01F * (float) i * 0.01F);
				}
			} else if (isInWater()) {
				int j = Math.round(MathHelper.sqrt(p_71000_1_ * p_71000_1_ + p_71000_5_ * p_71000_5_) * 100.0F);

				if (j > 0) {
					addStat(StatList.SWIM_ONE_CM, j);
					addExhaustion(0.01F * (float) j * 0.01F);
				}
			} else if (isOnLadder()) {
				if (p_71000_3_ > 0.0D) {
					addStat(StatList.CLIMB_ONE_CM, (int) Math.round(p_71000_3_ * 100.0D));
				}
			} else if (onGround) {
				int k = Math.round(MathHelper.sqrt(p_71000_1_ * p_71000_1_ + p_71000_5_ * p_71000_5_) * 100.0F);

				if (k > 0) {
					if (isSprinting()) {
						addStat(StatList.SPRINT_ONE_CM, k);
						addExhaustion(0.1F * (float) k * 0.01F);
					} else if (isSneaking()) {
						addStat(StatList.CROUCH_ONE_CM, k);
						addExhaustion(0.0F * (float) k * 0.01F);
					} else {
						addStat(StatList.WALK_ONE_CM, k);
						addExhaustion(0.0F * (float) k * 0.01F);
					}
				}
			} else if (isElytraFlying()) {
				int l = Math.round(MathHelper.sqrt(p_71000_1_ * p_71000_1_ + p_71000_3_ * p_71000_3_ + p_71000_5_ * p_71000_5_) * 100.0F);
				addStat(StatList.AVIATE_ONE_CM, l);
			} else {
				int i1 = Math.round(MathHelper.sqrt(p_71000_1_ * p_71000_1_ + p_71000_5_ * p_71000_5_) * 100.0F);

				if (i1 > 25) {
					addStat(StatList.FLY_ONE_CM, i1);
				}
			}
		}
	}

	/**
	 * Adds a value to a mounted movement statistic field - by minecart, boat, or pig.
	 */
	private void addMountedMovementStat(double p_71015_1_, double p_71015_3_, double p_71015_5_) {

		if (isRiding()) {
			int i = Math.round(MathHelper.sqrt(p_71015_1_ * p_71015_1_ + p_71015_3_ * p_71015_3_ + p_71015_5_ * p_71015_5_) * 100.0F);

			if (i > 0) {
				if (getRidingEntity() instanceof EntityMinecart) {
					addStat(StatList.MINECART_ONE_CM, i);
				} else if (getRidingEntity() instanceof EntityBoat) {
					addStat(StatList.BOAT_ONE_CM, i);
				} else if (getRidingEntity() instanceof EntityPig) {
					addStat(StatList.PIG_ONE_CM, i);
				} else if (getRidingEntity() instanceof AbstractHorse) {
					addStat(StatList.HORSE_ONE_CM, i);
				}
			}
		}
	}

	public void fall(float distance, float damageMultiplier) {

		if (!capabilities.allowFlying) {
			if (distance >= 2.0F) {
				addStat(StatList.FALL_ONE_CM, (int) Math.round((double) distance * 100.0D));
			}

			super.fall(distance, damageMultiplier);
		}
	}

	/**
	 * Plays the {@link #getSplashSound() splash sound}, and the {@link ParticleType#WATER_BUBBLE} and {@link
	 * ParticleType#WATER_SPLASH} particles.
	 */
	protected void doWaterSplashEffect() {

		if (!isSpectator()) {
			super.doWaterSplashEffect();
		}
	}

	protected SoundEvent getFallSound(int heightIn) {

		return heightIn > 4 ? SoundEvents.ENTITY_PLAYER_BIG_FALL : SoundEvents.ENTITY_PLAYER_SMALL_FALL;
	}

	/**
	 * This method gets called when the entity kills another one.
	 */
	public void onKillEntity(EntityLivingBase entityLivingIn) {

		EntityList.EntityEggInfo entitylist$entityegginfo = EntityList.ENTITY_EGGS.get(EntityList.getKey(entityLivingIn));

		if (entitylist$entityegginfo != null) {
			addStat(entitylist$entityegginfo.killEntityStat);
		}
	}

	/**
	 * Sets the Entity inside a web block.
	 */
	public void setInWeb() {

		if (!capabilities.isFlying) {
			super.setInWeb();
		}
	}

	/**
	 * Add experience points to player.
	 */
	public void addExperience(int amount) {

		addScore(amount);
		int i = Integer.MAX_VALUE - experienceTotal;

		if (amount > i) {
			amount = i;
		}

		experience += (float) amount / (float) xpBarCap();

		for (experienceTotal += amount; experience >= 1.0F; experience /= (float) xpBarCap()) {
			experience = (experience - 1.0F) * (float) xpBarCap();
			addExperienceLevel(1);
		}
	}

	public int getXPSeed() {

		return xpSeed;
	}

	public void onEnchant(ItemStack enchantedItem, int cost) {

		experienceLevel -= cost;

		if (experienceLevel < 0) {
			experienceLevel = 0;
			experience = 0.0F;
			experienceTotal = 0;
		}

		xpSeed = rand.nextInt();
	}

	/**
	 * Add experience levels to this player.
	 */
	public void addExperienceLevel(int levels) {

		experienceLevel += levels;

		if (experienceLevel < 0) {
			experienceLevel = 0;
			experience = 0.0F;
			experienceTotal = 0;
		}

		if (levels > 0 && experienceLevel % 5 == 0 && (float) lastXPSound < (float) ticksExisted - 100.0F) {
			float f = experienceLevel > 30 ? 1.0F : (float) experienceLevel / 30.0F;
			world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, getSoundCategory(), f * 0.75F, 1.0F);
			lastXPSound = ticksExisted;
		}
	}

	/**
	 * This method returns the cap amount of experience that the experience bar can hold. With each level, the
	 * experience cap on the player's experience bar is raised by 10.
	 */
	public int xpBarCap() {

		if (experienceLevel >= 30) {
			return 112 + (experienceLevel - 30) * 9;
		} else {
			return experienceLevel >= 15 ? 37 + (experienceLevel - 15) * 5 : 7 + experienceLevel * 2;
		}
	}

	/**
	 * increases exhaustion level by supplied amount
	 */
	public void addExhaustion(float exhaustion) {

		if (!capabilities.disableDamage) {
			if (!world.isRemote) {
				foodStats.addExhaustion(exhaustion);
			}
		}
	}

	/**
	 * Returns the player's FoodStats object.
	 */
	public FoodStats getFoodStats() {

		return foodStats;
	}

	public boolean canEat(boolean ignoreHunger) {

		return (ignoreHunger || foodStats.needFood()) && !capabilities.disableDamage;
	}

	/**
	 * Checks if the player's health is not full and not zero.
	 */
	public boolean shouldHeal() {

		return getHealth() > 0.0F && getHealth() < getMaxHealth();
	}

	public boolean isAllowEdit() {

		return capabilities.allowEdit;
	}

	/**
	 * Returns whether this player can modify the block at a certain location with the given stack.
	 * <p>
	 * The position being queried is {@code pos.offset(facing.getOpposite()))}.
	 *
	 * @param pos    a position adjacent to the queried position
	 * @param facing the direction from the queried location to (that is, pointing away from the location queried)
	 *               {@code pos}
	 * @param stack  the {@code ItemStack} that would be used to edit the world
	 * @return Whether this player may modify the queried location in the current world
	 * @see ItemStack#canPlaceOn(Block)
	 * @see ItemStack#canEditBlocks()
	 * @see PlayerCapabilities#allowEdit
	 */
	public boolean canPlayerEdit(BlockPos pos, EnumFacing facing, ItemStack stack) {

		if (capabilities.allowEdit) {
			return true;
		} else if (stack.isEmpty()) {
			return false;
		} else {
			BlockPos blockpos = pos.offset(facing.getOpposite());
			Block block = world.getBlockState(blockpos).getBlock();
			return stack.canPlaceOn(block) || stack.canEditBlocks();
		}
	}

	/**
	 * Get the experience points the entity currently has.
	 */
	protected int getExperiencePoints(EntityPlayer player) {

		if (!world.getGameRules().getBoolean("keepInventory") && !isSpectator()) {
			int i = experienceLevel * 7;
			return Math.min(i, 100);
		} else {
			return 0;
		}
	}

	/**
	 * Only use is to identify if class is an instance of player for experience dropping
	 */
	protected boolean isPlayer() {

		return true;
	}

	public boolean getAlwaysRenderNameTagForRender() {

		return true;
	}

	/**
	 * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
	 * prevent them from trampling crops
	 */
	protected boolean canTriggerWalking() {

		return !capabilities.isFlying;
	}

	/**
	 * Sends the player's abilities to the server (if there is one).
	 */
	public void sendPlayerAbilities() {

	}

	/**
	 * Sets the player's game mode and sends it to them.
	 */
	public void setGameType(GameType gameType) {

	}

	/**
	 * Get the name of this object. For players this returns their username
	 */
	public String getName() {

		return gameProfile.getName();
	}

	/**
	 * Returns the InventoryEnderChest of this player.
	 */
	public InventoryEnderChest getInventoryEnderChest() {

		return enderChest;
	}

	public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {

		if (slotIn == EntityEquipmentSlot.MAINHAND) {
			return inventory.getCurrentItem();
		} else if (slotIn == EntityEquipmentSlot.OFFHAND) {
			return inventory.offHandInventory.getFirst();
		} else {
			return slotIn.getSlotType() == EntityEquipmentSlot.Type.ARMOR ? inventory.armorInventory.get(slotIn.getIndex()) : ItemStack.EMPTY;
		}
	}

	public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {

		if (slotIn == EntityEquipmentSlot.MAINHAND) {
			playEquipSound(stack);
			inventory.mainInventory.set(inventory.currentItem, stack);
		} else if (slotIn == EntityEquipmentSlot.OFFHAND) {
			playEquipSound(stack);
			inventory.offHandInventory.set(0, stack);
		} else if (slotIn.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
			playEquipSound(stack);
			inventory.armorInventory.set(slotIn.getIndex(), stack);
		}
	}

	public boolean addItemStackToInventory(ItemStack p_191521_1_) {

		playEquipSound(p_191521_1_);
		return inventory.addItemStackToInventory(p_191521_1_);
	}

	public Iterable<ItemStack> getHeldEquipment() {

		return Lists.newArrayList(getHeldItemMainhand(), getHeldItemOffhand());
	}

	public Iterable<ItemStack> getArmorInventoryList() {

		return inventory.armorInventory;
	}

	public boolean addShoulderEntity(NBTTagCompound p_192027_1_) {

		if (!isRiding() && onGround && !isInWater()) {
			if (getLeftShoulderEntity().hasNoTags()) {
				setLeftShoulderEntity(p_192027_1_);
				return true;
			} else if (getRightShoulderEntity().hasNoTags()) {
				setRightShoulderEntity(p_192027_1_);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	protected void spawnShoulderEntities() {

		spawnShoulderEntity(getLeftShoulderEntity());
		setLeftShoulderEntity(new NBTTagCompound());
		spawnShoulderEntity(getRightShoulderEntity());
		setRightShoulderEntity(new NBTTagCompound());
	}

	private void spawnShoulderEntity(@Nullable NBTTagCompound p_192026_1_) {

		if (!world.isRemote && !p_192026_1_.hasNoTags()) {
			Entity entity = EntityList.createEntityFromNBT(p_192026_1_, world);

			if (entity instanceof EntityTameable) {
				((EntityTameable) entity).setOwnerId(entityUniqueID);
			}

			entity.setPosition(posX, posY + 0.699999988079071D, posZ);
			world.spawnEntity(entity);
		}
	}

	/**
	 * Only used by renderer in EntityLivingBase subclasses.
	 * Determines if an entity is visible or not to a specfic player, if the entity is normally invisible.
	 * For EntityLivingBase subclasses, returning false when invisible will render the entity semitransparent.
	 */
	public boolean isInvisibleToPlayer(EntityPlayer player) {

		if (!isInvisible()) {
			return false;
		} else if (player.isSpectator()) {
			return false;
		} else {
			Team team = getTeam();
			return team == null || player == null || player.getTeam() != team || !team.getSeeFriendlyInvisiblesEnabled();
		}
	}

	/**
	 * Returns true if the player is in spectator mode.
	 */
	public abstract boolean isSpectator();

	public abstract boolean isCreative();

	public boolean isPushedByWater() {

		return !capabilities.isFlying;
	}

	public Scoreboard getWorldScoreboard() {

		return world.getScoreboard();
	}

	public Team getTeam() {

		return getWorldScoreboard().getPlayersTeam(getName());
	}

	/**
	 * Get the formatted ChatComponent that will be used for the sender's username in chat
	 */
	public ITextComponent getDisplayName() {

		ITextComponent itextcomponent = new TextComponentString(ScorePlayerTeam.formatPlayerName(getTeam(), getName()));
		itextcomponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + getName() + " "));
		itextcomponent.getStyle().setHoverEvent(getHoverEvent());
		itextcomponent.getStyle().setInsertion(getName());
		return itextcomponent;
	}

	public float getEyeHeight() {

		float f = 1.62F;

		if (isPlayerSleeping()) {
			f = 0.2F;
		} else if (!isSneaking() && height != 1.65F) {
			if (isElytraFlying() || height == 0.6F) {
				f = 0.4F;
			}
		} else {
			f -= 0.08F;
		}

		return f;
	}

	/**
	 * Returns the amount of health added by the Absorption effect.
	 */
	public float getAbsorptionAmount() {

		return getDataManager().get(ABSORPTION);
	}

	public void setAbsorptionAmount(float amount) {

		if (amount < 0.0F) {
			amount = 0.0F;
		}

		getDataManager().set(ABSORPTION, amount);
	}

	/**
	 * Check whether this player can open an inventory locked with the given LockCode.
	 */
	public boolean canOpen(LockCode code) {

		if (code.isEmpty()) {
			return true;
		} else {
			ItemStack itemstack = getHeldItemMainhand();
			return !itemstack.isEmpty() && itemstack.hasDisplayName() && itemstack.getDisplayName().equals(code.getLock());
		}
	}

	public boolean isWearing(EnumPlayerModelParts part) {

		return (getDataManager().get(PLAYER_MODEL_FLAG) & part.getPartMask()) == part.getPartMask();
	}

	/**
	 * Returns true if the command sender should be sent feedback about executed commands
	 */
	public boolean sendCommandFeedback() {

		return getServer().worlds[0].getGameRules().getBoolean("sendCommandFeedback");
	}

	public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn) {

		if (inventorySlot >= 0 && inventorySlot < inventory.mainInventory.size()) {
			inventory.setInventorySlotContents(inventorySlot, itemStackIn);
			return true;
		} else {
			EntityEquipmentSlot entityequipmentslot;

			if (inventorySlot == 100 + EntityEquipmentSlot.HEAD.getIndex()) {
				entityequipmentslot = EntityEquipmentSlot.HEAD;
			} else if (inventorySlot == 100 + EntityEquipmentSlot.CHEST.getIndex()) {
				entityequipmentslot = EntityEquipmentSlot.CHEST;
			} else if (inventorySlot == 100 + EntityEquipmentSlot.LEGS.getIndex()) {
				entityequipmentslot = EntityEquipmentSlot.LEGS;
			} else if (inventorySlot == 100 + EntityEquipmentSlot.FEET.getIndex()) {
				entityequipmentslot = EntityEquipmentSlot.FEET;
			} else {
				entityequipmentslot = null;
			}

			if (inventorySlot == 98) {
				setItemStackToSlot(EntityEquipmentSlot.MAINHAND, itemStackIn);
				return true;
			} else if (inventorySlot == 99) {
				setItemStackToSlot(EntityEquipmentSlot.OFFHAND, itemStackIn);
				return true;
			} else if (entityequipmentslot == null) {
				int i = inventorySlot - 200;

				if (i >= 0 && i < enderChest.getSizeInventory()) {
					enderChest.setInventorySlotContents(i, itemStackIn);
					return true;
				} else {
					return false;
				}
			} else {
				if (!itemStackIn.isEmpty()) {
					if (!(itemStackIn.getItem() instanceof ItemArmor) && !(itemStackIn.getItem() instanceof ItemElytra)) {
						if (entityequipmentslot != EntityEquipmentSlot.HEAD) {
							return false;
						}
					} else if (EntityLiving.getSlotForItemStack(itemStackIn) != entityequipmentslot) {
						return false;
					}
				}

				inventory.setInventorySlotContents(entityequipmentslot.getIndex() + inventory.mainInventory.size(), itemStackIn);
				return true;
			}
		}
	}

	/**
	 * Whether the "reducedDebugInfo" option is active for this player.
	 */
	public boolean hasReducedDebug() {

		return hasReducedDebug;
	}

	public void setReducedDebug(boolean reducedDebug) {

		hasReducedDebug = reducedDebug;
	}

	public EnumHandSide getPrimaryHand() {

		return dataManager.get(MAIN_HAND) == 0 ? EnumHandSide.LEFT : EnumHandSide.RIGHT;
	}

	public void setPrimaryHand(EnumHandSide hand) {

		dataManager.set(MAIN_HAND, (byte) (hand == EnumHandSide.LEFT ? 0 : 1));
	}

	public NBTTagCompound getLeftShoulderEntity() {

		return dataManager.get(LEFT_SHOULDER_ENTITY);
	}

	protected void setLeftShoulderEntity(NBTTagCompound tag) {

		dataManager.set(LEFT_SHOULDER_ENTITY, tag);
	}

	public NBTTagCompound getRightShoulderEntity() {

		return dataManager.get(RIGHT_SHOULDER_ENTITY);
	}

	protected void setRightShoulderEntity(NBTTagCompound tag) {

		dataManager.set(RIGHT_SHOULDER_ENTITY, tag);
	}

	public float getCooldownPeriod() {

		return (float) (1.0D / getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue() * 20.0D);
	}

	/**
	 * Returns the percentage of attack power available based on the cooldown (zero to one).
	 */
	public float getCooledAttackStrength(float adjustTicks) {

		return MathHelper.clamp(((float) ticksSinceLastSwing + adjustTicks) / getCooldownPeriod(), 0.0F, 1.0F);
	}

	public void resetCooldown() {

		ticksSinceLastSwing = 0;
	}

	public CooldownTracker getCooldownTracker() {

		return cooldownTracker;
	}

	/**
	 * Applies a velocity to the entities, to push them away from eachother.
	 */
	public void applyEntityCollision(Entity entityIn) {

		if (!isPlayerSleeping()) {
			super.applyEntityCollision(entityIn);
		}
	}

	public float getLuck() {

		return (float) getEntityAttribute(SharedMonsterAttributes.LUCK).getAttributeValue();
	}

	/**
	 * Can the player use command blocks. It checks if the player is on Creative mode and has permissions (is he OP)
	 */
	public boolean canUseCommandBlock() {

		return capabilities.isCreativeMode && canUseCommand(2, "");
	}

	public enum EnumChatVisibility {
		FULL(0, "options.chat.visibility.full"),
		SYSTEM(1, "options.chat.visibility.system"),
		HIDDEN(2, "options.chat.visibility.hidden");

		private static final EntityPlayer.EnumChatVisibility[] ID_LOOKUP = new EntityPlayer.EnumChatVisibility[values().length];

		static {
			for (EntityPlayer.EnumChatVisibility entityplayer$enumchatvisibility : values()) {
				ID_LOOKUP[entityplayer$enumchatvisibility.chatVisibility] = entityplayer$enumchatvisibility;
			}
		}

		private final int chatVisibility;
		private final String resourceKey;

		EnumChatVisibility(int id, String resourceKey) {

			chatVisibility = id;
			this.resourceKey = resourceKey;
		}

		public static EntityPlayer.EnumChatVisibility getEnumChatVisibility(int id) {

			return ID_LOOKUP[id % ID_LOOKUP.length];
		}

		public int getChatVisibility() {

			return chatVisibility;
		}

		public String getResourceKey() {

			return resourceKey;
		}
	}

	public enum SleepResult {
		OK,
		NOT_POSSIBLE_HERE,
		NOT_POSSIBLE_NOW,
		TOO_FAR_AWAY,
		OTHER_PROBLEM,
		NOT_SAFE
	}

	static class SleepEnemyPredicate implements Predicate<EntityMob> {

		private final EntityPlayer player;

		private SleepEnemyPredicate(EntityPlayer playerIn) {

			player = playerIn;
		}

		public boolean apply(@Nullable EntityMob p_apply_1_) {

			return p_apply_1_.isPreventingPlayerRest(player);
		}

	}

}
