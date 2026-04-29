package net.minecraft.tileentity;

import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionHelper;
import net.minecraft.util.Facing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

public class TileEntityBrewingStand extends TileEntityLockable implements ITickable, ISidedInventory {

	/**
	 * an array of the input slot indices
	 */
	private static final int[] SLOTS_FOR_UP = new int[]{3};
	private static final int[] SLOTS_FOR_DOWN = new int[]{0, 1, 2, 3};

	/**
	 * an array of the output slot indices
	 */
	private static final int[] OUTPUT_SLOTS = new int[]{0, 1, 2, 4};
	private NonNullList<ItemStack> brewingItemStacks = NonNullList.withSize(5, ItemStack.EMPTY);
	private int brewTime;

	/**
	 * an integer with each bit specifying whether that slot of the stand contains a potion
	 */
	private boolean[] filledSlots;

	/**
	 * used to check if the current ingredient has been removed from the brewing stand during brewing
	 */
	private Item ingredientID;
	private String customName;
	private int fuel;

	public static void registerFixesBrewingStand(DataFixer fixer) {

		fixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists(TileEntityBrewingStand.class, "Items"));
	}

	/**
	 * Get the name of this object. For players this returns their username
	 */
	public String getName() {

		return hasCustomName() ? customName : "container.brewing";
	}

	public void setName(String name) {

		customName = name;
	}

	/**
	 * Returns true if this thing is named
	 */
	public boolean hasCustomName() {

		return customName != null && !customName.isEmpty();
	}

	/**
	 * Returns the number of slots in the inventory.
	 */
	public int getSizeInventory() {

		return brewingItemStacks.size();
	}

	public boolean isEmpty() {

		for (ItemStack itemstack : brewingItemStacks) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Like the old updateEntity(), except more generic.
	 */
	public void update() {

		ItemStack itemstack = brewingItemStacks.get(4);

		if (fuel <= 0 && itemstack.getItem() == Items.BLAZE_POWDER) {
			fuel = 20;
			itemstack.shrink(1);
			markDirty();
		}

		boolean flag = canBrew();
		boolean flag1 = brewTime > 0;
		ItemStack itemstack1 = brewingItemStacks.get(3);

		if (flag1) {
			--brewTime;
			boolean flag2 = brewTime == 0;

			if (flag2 && flag) {
				brewPotions();
				markDirty();
			} else if (!flag) {
				brewTime = 0;
				markDirty();
			} else if (ingredientID != itemstack1.getItem()) {
				brewTime = 0;
				markDirty();
			}
		} else if (flag && fuel > 0) {
			--fuel;
			brewTime = 400;
			ingredientID = itemstack1.getItem();
			markDirty();
		}

		if (!world.isRemote) {
			boolean[] aboolean = createFilledSlotsArray();

			if (!Arrays.equals(aboolean, filledSlots)) {
				filledSlots = aboolean;
				IBlockState iblockstate = world.getBlockState(getPos());

				if (!(iblockstate.getBlock() instanceof BlockBrewingStand)) {
					return;
				}

				for (int i = 0; i < BlockBrewingStand.HAS_BOTTLE.length; ++i) {
					iblockstate = iblockstate.withProperty(BlockBrewingStand.HAS_BOTTLE[i], aboolean[i]);
				}

				world.setBlockState(pos, iblockstate, 2);
			}
		}
	}

	/**
	 * Creates an array of boolean values, each value represents a potion input slot, value is true if the slot is not
	 * null.
	 */
	public boolean[] createFilledSlotsArray() {

		boolean[] aboolean = new boolean[3];

		for (int i = 0; i < 3; ++i) {
			if (!brewingItemStacks.get(i).isEmpty()) {
				aboolean[i] = true;
			}
		}

		return aboolean;
	}

	private boolean canBrew() {

		ItemStack itemstack = brewingItemStacks.get(3);

		if (itemstack.isEmpty()) {
			return false;
		} else if (!PotionHelper.isReagent(itemstack)) {
			return false;
		} else {
			for (int i = 0; i < 3; ++i) {
				ItemStack itemstack1 = brewingItemStacks.get(i);

				if (!itemstack1.isEmpty() && PotionHelper.hasConversions(itemstack1, itemstack)) {
					return true;
				}
			}

			return false;
		}
	}

	private void brewPotions() {

		ItemStack itemstack = brewingItemStacks.get(3);

		for (int i = 0; i < 3; ++i) {
			brewingItemStacks.set(i, PotionHelper.doReaction(itemstack, brewingItemStacks.get(i)));
		}

		itemstack.shrink(1);
		BlockPos blockpos = getPos();

		if (itemstack.getItem().hasContainerItem()) {
			ItemStack itemstack1 = new ItemStack(itemstack.getItem().getContainerItem());

			if (itemstack.isEmpty()) {
				itemstack = itemstack1;
			} else {
				InventoryHelper.spawnItemStack(world, blockpos.getX(), blockpos.getY(), blockpos.getZ(), itemstack1);
			}
		}

		brewingItemStacks.set(3, itemstack);
		world.playEvent(1035, blockpos, 0);
	}

	public void readFromNBT(NBTTagCompound compound) {

		super.readFromNBT(compound);
		brewingItemStacks = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(compound, brewingItemStacks);
		brewTime = compound.getShort("BrewTime");

		if (compound.hasKey("CustomName", 8)) {
			customName = compound.getString("CustomName");
		}

		fuel = compound.getByte("Fuel");
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {

		super.writeToNBT(compound);
		compound.setShort("BrewTime", (short) brewTime);
		ItemStackHelper.saveAllItems(compound, brewingItemStacks);

		if (hasCustomName()) {
			compound.setString("CustomName", customName);
		}

		compound.setByte("Fuel", (byte) fuel);
		return compound;
	}

	/**
	 * Returns the stack in the given slot.
	 */
	public ItemStack getStackInSlot(int index) {

		return index >= 0 && index < brewingItemStacks.size() ? brewingItemStacks.get(index) : ItemStack.EMPTY;
	}

	/**
	 * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
	 */
	public ItemStack decrStackSize(int index, int count) {

		return ItemStackHelper.getAndSplit(brewingItemStacks, index, count);
	}

	/**
	 * Removes a stack from the given slot and returns it.
	 */
	public ItemStack removeStackFromSlot(int index) {

		return ItemStackHelper.getAndRemove(brewingItemStacks, index);
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
	 */
	public void setInventorySlotContents(int index, ItemStack stack) {

		if (index >= 0 && index < brewingItemStacks.size()) {
			brewingItemStacks.set(index, stack);
		}
	}

	/**
	 * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
	 */
	public int getInventoryStackLimit() {

		return 64;
	}

	/**
	 * Don't rename this method to canInteractWith due to conflicts with Container
	 */
	public boolean isUsableByPlayer(EntityPlayer player) {

		if (world.getTileEntity(pos) != this) {
			return false;
		} else {
			return player.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64D;
		}
	}

	public void openInventory(EntityPlayer player) {

	}

	public void closeInventory(EntityPlayer player) {

	}

	/**
	 * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
	 * guis use Slot.isItemValid
	 */
	public boolean isItemValidForSlot(int index, ItemStack stack) {

		if (index == 3) {
			return PotionHelper.isReagent(stack);
		} else {
			Item item = stack.getItem();

			if (index == 4) {
				return item == Items.BLAZE_POWDER;
			} else {
				return (item == Items.POTIONITEM || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || item == Items.GLASS_BOTTLE) && getStackInSlot(index).isEmpty();
			}
		}
	}

	public int[] getSlotsForFace(Facing side) {

		if (side == Facing.UP) {
			return SLOTS_FOR_UP;
		} else {
			return side == Facing.DOWN ? SLOTS_FOR_DOWN : OUTPUT_SLOTS;
		}
	}

	/**
	 * Returns true if automation can insert the given item in the given slot from the given side.
	 */
	public boolean canInsertItem(int index, ItemStack itemStackIn, Facing direction) {

		return isItemValidForSlot(index, itemStackIn);
	}

	/**
	 * Returns true if automation can extract the given item in the given slot from the given side.
	 */
	public boolean canExtractItem(int index, ItemStack stack, Facing direction) {

		if (index == 3) {
			return stack.getItem() == Items.GLASS_BOTTLE;
		} else {
			return true;
		}
	}

	public String guiID() {

		return "minecraft:brewing_stand";
	}

	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {

		return new ContainerBrewingStand(playerInventory, this);
	}

	public int getField(int id) {

		return switch (id) {
			case 0 -> brewTime;
			case 1 -> fuel;
			default -> 0;
		};
	}

	public void setField(int id, int value) {

		switch (id) {
			case 0:
				brewTime = value;
				break;

			case 1:
				fuel = value;
		}
	}

	public int getFieldCount() {

		return 2;
	}

	public void clear() {

		brewingItemStacks.clear();
	}

}
