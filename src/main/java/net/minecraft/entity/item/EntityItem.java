package net.minecraft.entity.item;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityItem extends Entity {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final DataParameter<ItemStack> ITEM = EntityDataManager.createKey(EntityItem.class, DataSerializers.ITEM_STACK);
	/**
	 * The EntityItem's random initial float height.
	 */
	public float hoverStart;
	/**
	 * The age of this EntityItem (used to animate it up and down as well as expire it)
	 */
	private int age;
	private int pickupDelay;
	/**
	 * The health of this EntityItem. (For example, damage for tools)
	 */
	private int health;
	private String thrower;
	private String owner;

	public EntityItem(World worldIn, double x, double y, double z) {

		super(worldIn);
		health = 5;
		hoverStart = (float) (Math.random() * Math.PI * 2.0D);
		setSize(0.25F, 0.25F);
		setPosition(x, y, z);
		rotationYaw = (float) (Math.random() * 360.0D);
		motionX = (float) (Math.random() * 0.20000000298023224D - 0.10000000149011612D);
		motionY = 0.20000000298023224D;
		motionZ = (float) (Math.random() * 0.20000000298023224D - 0.10000000149011612D);
	}

	public EntityItem(World worldIn, double x, double y, double z, ItemStack stack) {

		this(worldIn, x, y, z);
		setItem(stack);
	}

	public EntityItem(World worldIn) {

		super(worldIn);
		health = 5;
		hoverStart = (float) (Math.random() * Math.PI * 2.0D);
		setSize(0.25F, 0.25F);
		setItem(ItemStack.EMPTY);
	}

	public static void registerFixesItem(DataFixer fixer) {

		fixer.registerWalker(FixTypes.ENTITY, new ItemStackData(EntityItem.class, "Item"));
	}

	/**
	 * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
	 * prevent them from trampling crops
	 */
	protected boolean canTriggerWalking() {

		return false;
	}

	protected void entityInit() {

		getDataManager().register(ITEM, ItemStack.EMPTY);
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void onUpdate() {

		if (getItem().isEmpty()) {
			setDead();
		} else {
			super.onUpdate();

			if (pickupDelay > 0 && pickupDelay != 32767) {
				--pickupDelay;
			}

			prevPosX = posX;
			prevPosY = posY;
			prevPosZ = posZ;
			double d0 = motionX;
			double d1 = motionY;
			double d2 = motionZ;

			if (!hasNoGravity()) {
				motionY -= 0.03999999910593033D;
			}

			if (world.isRemote) {
				noClip = false;
			} else {
				noClip = pushOutOfBlocks(posX, (getEntityBoundingBox().minY + getEntityBoundingBox().maxY) / 2.0D, posZ);
			}

			move(MoverType.SELF, motionX, motionY, motionZ);
			boolean flag = (int) prevPosX != (int) posX || (int) prevPosY != (int) posY || (int) prevPosZ != (int) posZ;

			if (flag || ticksExisted % 25 == 0) {
				if (world.getBlockState(new BlockPos(this)).getMaterial() == Material.LAVA) {
					motionY = 0.20000000298023224D;
					motionX = (rand.nextFloat() - rand.nextFloat()) * 0.2F;
					motionZ = (rand.nextFloat() - rand.nextFloat()) * 0.2F;
					playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + rand.nextFloat() * 0.4F);
				}

				if (!world.isRemote) {
					searchForOtherItemsNearby();
				}
			}

			float f = 0.98F;

			if (onGround) {
				f = world.getBlockState(new BlockPos(MathHelper.floor(posX), MathHelper.floor(getEntityBoundingBox().minY) - 1, MathHelper.floor(posZ))).getBlock().slipperiness * 0.98F;
			}

			motionX *= f;
			motionY *= 0.9800000190734863D;
			motionZ *= f;

			if (onGround) {
				motionY *= -0.5D;
			}

			if (age != -32768) {
				++age;
			}

			handleWaterMovement();

			if (!world.isRemote) {
				double d3 = motionX - d0;
				double d4 = motionY - d1;
				double d5 = motionZ - d2;
				double d6 = d3 * d3 + d4 * d4 + d5 * d5;

				if (d6 > 0.01D) {
					isAirBorne = true;
				}
			}

			if (!world.isRemote && age >= 6000) {
				setDead();
			}
		}
	}

	/**
	 * Looks for other itemstacks nearby and tries to stack them together
	 */
	private void searchForOtherItemsNearby() {

		for (EntityItem entityitem : world.getEntitiesWithinAABB(EntityItem.class, getEntityBoundingBox().grow(0.5D, 0.0D, 0.5D))) {
			combineItems(entityitem);
		}
	}

	/**
	 * Tries to merge this item with the item passed as the parameter. Returns true if successful. Either this item or
	 * the other item will  be removed from the world.
	 */
	private boolean combineItems(EntityItem other) {

		if (other == this) {
			return false;
		} else if (other.isEntityAlive() && isEntityAlive()) {
			ItemStack itemstack = getItem();
			ItemStack itemstack1 = other.getItem();

			if (pickupDelay != 32767 && other.pickupDelay != 32767) {
				if (age != -32768 && other.age != -32768) {
					if (itemstack1.getItem() != itemstack.getItem()) {
						return false;
					} else if (itemstack1.hasTagCompound() ^ itemstack.hasTagCompound()) {
						return false;
					} else if (itemstack1.hasTagCompound() && !itemstack1.getTagCompound().equals(itemstack.getTagCompound())) {
						return false;
					} else if (itemstack1.getItem() == null) {
						return false;
					} else if (itemstack1.getItem().getHasSubtypes() && itemstack1.getMetadata() != itemstack.getMetadata()) {
						return false;
					} else if (itemstack1.getCount() < itemstack.getCount()) {
						return other.combineItems(this);
					} else if (itemstack1.getCount() + itemstack.getCount() > itemstack1.getMaxStackSize()) {
						return false;
					} else {
						itemstack1.grow(itemstack.getCount());
						other.pickupDelay = Math.max(other.pickupDelay, pickupDelay);
						other.age = Math.min(other.age, age);
						other.setItem(itemstack1);
						setDead();
						return true;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * sets the age of the item so that it'll despawn one minute after it has been dropped (instead of five). Used when
	 * items are dropped from players in creative mode
	 */
	public void setAgeToCreativeDespawnTime() {

		age = 4800;
	}

	/**
	 * Returns if this entity is in water and will end up adding the waters velocity to the entity
	 */
	public boolean handleWaterMovement() {

		if (world.handleMaterialAcceleration(getEntityBoundingBox(), Material.WATER, this)) {
			if (!inWater && !firstUpdate) {
				doWaterSplashEffect();
			}

			inWater = true;
		} else {
			inWater = false;
		}

		return inWater;
	}

	/**
	 * Will deal the specified amount of fire damage to the entity if the entity isn't immune to fire damage.
	 */
	protected void dealFireDamage(int amount) {

		attackEntityFrom(DamageSource.IN_FIRE, (float) amount);
	}

	/**
	 * Called when the entity is attacked.
	 */
	public boolean attackEntityFrom(DamageSource source, float amount) {

		if (isEntityInvulnerable(source)) {
			return false;
		} else if (!getItem().isEmpty() && getItem().getItem() == Items.NETHER_STAR && source.isExplosion()) {
			return false;
		} else {
			markVelocityChanged();
			health = (int) ((float) health - amount);

			if (health <= 0) {
				setDead();
			}

			return false;
		}
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound compound) {

		compound.setShort("Health", (short) health);
		compound.setShort("Age", (short) age);
		compound.setShort("PickupDelay", (short) pickupDelay);

		if (getThrower() != null) {
			compound.setString("Thrower", thrower);
		}

		if (getOwner() != null) {
			compound.setString("Owner", owner);
		}

		if (!getItem().isEmpty()) {
			compound.setTag("Item", getItem().writeToNBT(new NBTTagCompound()));
		}
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound compound) {

		health = compound.getShort("Health");
		age = compound.getShort("Age");

		if (compound.hasKey("PickupDelay")) {
			pickupDelay = compound.getShort("PickupDelay");
		}

		if (compound.hasKey("Owner")) {
			owner = compound.getString("Owner");
		}

		if (compound.hasKey("Thrower")) {
			thrower = compound.getString("Thrower");
		}

		NBTTagCompound nbttagcompound = compound.getCompoundTag("Item");
		setItem(new ItemStack(nbttagcompound));

		if (getItem().isEmpty()) {
			setDead();
		}
	}

	/**
	 * Called by a player entity when they collide with an entity
	 */
	public void onCollideWithPlayer(EntityPlayer entityIn) {

		if (!world.isRemote) {
			ItemStack itemstack = getItem();
			Item item = itemstack.getItem();
			int i = itemstack.getCount();

			if (pickupDelay == 0 && (owner == null || 6000 - age <= 200 || owner.equals(entityIn.getName())) && entityIn.inventory.addItemStackToInventory(itemstack)) {
				entityIn.onItemPickup(this, i);

				if (itemstack.isEmpty()) {
					setDead();
					itemstack.setCount(i);
				}

				entityIn.addStat(StatList.getObjectsPickedUpStats(item), i);
			}
		}
	}

	/**
	 * Get the name of this object. For players this returns their username
	 */
	public String getName() {

		return hasCustomName() ? getCustomNameTag() : I18n.translateToLocal("item." + getItem().getUnlocalizedName());
	}

	/**
	 * Returns true if it's possible to attack this entity with an item.
	 */
	public boolean canBeAttackedWithItem() {

		return false;
	}

	
	public Entity changeDimension(int dimensionIn) {

		Entity entity = super.changeDimension(dimensionIn);

		if (!world.isRemote && entity instanceof EntityItem) {
			((EntityItem) entity).searchForOtherItemsNearby();
		}

		return entity;
	}

	/**
	 * Gets the item that this entity represents.
	 */
	public ItemStack getItem() {

		return getDataManager().get(ITEM);
	}

	/**
	 * Sets the item that this entity represents.
	 */
	public void setItem(ItemStack stack) {

		getDataManager().set(ITEM, stack);
		getDataManager().setDirty(ITEM);
	}

	public String getOwner() {

		return owner;
	}

	public void setOwner(String owner) {

		this.owner = owner;
	}

	public String getThrower() {

		return thrower;
	}

	public void setThrower(String thrower) {

		this.thrower = thrower;
	}

	public int getAge() {

		return age;
	}

	public void setDefaultPickupDelay() {

		pickupDelay = 10;
	}

	public void setNoPickupDelay() {

		pickupDelay = 0;
	}

	public void setInfinitePickupDelay() {

		pickupDelay = 32767;
	}

	public void setPickupDelay(int ticks) {

		pickupDelay = ticks;
	}

	public boolean cannotPickup() {

		return pickupDelay > 0;
	}

	public void setNoDespawn() {

		age = -6000;
	}

	public void makeFakeItem() {

		setInfinitePickupDelay();
		age = 5999;
	}

}
