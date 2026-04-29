package net.minecraft.entity.passive;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.world.World;

public abstract class AbstractChestHorse extends AbstractHorse {

	private static final DataParameter<Boolean> DATA_ID_CHEST = EntityDataManager.createKey(AbstractChestHorse.class, DataSerializers.BOOLEAN);

	public AbstractChestHorse(World worldIn) {

		super(worldIn);
		canGallop = false;
	}

	public static void registerFixesAbstractChestHorse(DataFixer fixer, Class<?> entityClass) {

		AbstractHorse.registerFixesAbstractHorse(fixer, entityClass);
		fixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists(entityClass, "Items"));
	}

	protected void entityInit() {

		super.entityInit();
		dataManager.register(DATA_ID_CHEST, false);
	}

	protected void applyEntityAttributes() {

		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(getModifiedMaxHealth());
		getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.17499999701976776D);
		getEntityAttribute(JUMP_STRENGTH).setBaseValue(0.5D);
	}

	public boolean hasChest() {

		return dataManager.get(DATA_ID_CHEST);
	}

	public void setChested(boolean chested) {

		dataManager.set(DATA_ID_CHEST, chested);
	}

	protected int getInventorySize() {

		return hasChest() ? 17 : super.getInventorySize();
	}

	/**
	 * Returns the Y offset from the entity's position for any entity riding this one.
	 */
	public double getMountedYOffset() {

		return super.getMountedYOffset() - 0.25D;
	}

	protected SoundEvent getAngrySound() {

		super.getAngrySound();
		return SoundEvents.ENTITY_DONKEY_ANGRY;
	}

	/**
	 * Called when the mob's health reaches 0.
	 */
	public void onDeath(DamageSource cause) {

		super.onDeath(cause);

		if (hasChest()) {
			if (!world.isRemote) {
				dropItem(Item.getItemFromBlock(Blocks.CHEST), 1);
			}

			setChested(false);
		}
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound compound) {

		super.writeEntityToNBT(compound);
		compound.setBoolean("ChestedHorse", hasChest());

		if (hasChest()) {
			NBTTagList nbttaglist = new NBTTagList();

			for (int i = 2; i < horseChest.getSizeInventory(); ++i) {
				ItemStack itemstack = horseChest.getStackInSlot(i);

				if (!itemstack.isEmpty()) {
					NBTTagCompound nbttagcompound = new NBTTagCompound();
					nbttagcompound.setByte("Slot", (byte) i);
					itemstack.writeToNBT(nbttagcompound);
					nbttaglist.appendTag(nbttagcompound);
				}
			}

			compound.setTag("Items", nbttaglist);
		}
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound compound) {

		super.readEntityFromNBT(compound);
		setChested(compound.getBoolean("ChestedHorse"));

		if (hasChest()) {
			NBTTagList nbttaglist = compound.getTagList("Items", 10);
			initHorseChest();

			for (int i = 0; i < nbttaglist.tagCount(); ++i) {
				NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
				int j = nbttagcompound.getByte("Slot") & 255;

				if (j >= 2 && j < horseChest.getSizeInventory()) {
					horseChest.setInventorySlotContents(j, new ItemStack(nbttagcompound));
				}
			}
		}

		updateHorseSlots();
	}

	public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn) {

		if (inventorySlot == 499) {
			if (hasChest() && itemStackIn.isEmpty()) {
				setChested(false);
				initHorseChest();
				return true;
			}

			if (!hasChest() && itemStackIn.getItem() == Item.getItemFromBlock(Blocks.CHEST)) {
				setChested(true);
				initHorseChest();
				return true;
			}
		}

		return super.replaceItemInInventory(inventorySlot, itemStackIn);
	}

	public boolean processInteract(EntityPlayer player, Hand hand) {

		ItemStack itemstack = player.getHeldItem(hand);

		if (itemstack.getItem() == Items.SPAWN_EGG) {
			return super.processInteract(player, hand);
		} else {
			if (!isChild()) {
				if (isTame() && player.isSneaking()) {
					openGUI(player);
					return true;
				}

				if (isBeingRidden()) {
					return super.processInteract(player, hand);
				}
			}

			if (!itemstack.isEmpty()) {
				boolean flag = handleEating(player, itemstack);

				if (!flag && !isTame()) {
					if (itemstack.interactWithEntity(player, this, hand)) {
						return true;
					}

					makeMad();
					return true;
				}

				if (!flag && !hasChest() && itemstack.getItem() == Item.getItemFromBlock(Blocks.CHEST)) {
					setChested(true);
					playChestEquipSound();
					flag = true;
					initHorseChest();
				}

				if (!flag && !isChild() && !isHorseSaddled() && itemstack.getItem() == Items.SADDLE) {
					openGUI(player);
					return true;
				}

				if (flag) {
					if (!player.capabilities.isCreativeMode) {
						itemstack.shrink(1);
					}

					return true;
				}
			}

			if (isChild()) {
				return super.processInteract(player, hand);
			} else if (itemstack.interactWithEntity(player, this, hand)) {
				return true;
			} else {
				mountTo(player);
				return true;
			}
		}
	}

	protected void playChestEquipSound() {

		playSound(SoundEvents.ENTITY_DONKEY_CHEST, 1F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1F);
	}

	public int getInventoryColumns() {

		return 5;
	}

}
