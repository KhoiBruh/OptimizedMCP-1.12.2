package net.minecraft.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.ILootContainer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import java.util.Random;

public abstract class TileEntityLockableLoot extends TileEntityLockable implements ILootContainer {

	protected ResourceLocation lootTable;
	protected long lootTableSeed;
	protected String customName;

	protected boolean checkLootAndRead(NBTTagCompound compound) {

		if (compound.hasKey("LootTable", 8)) {
			lootTable = new ResourceLocation(compound.getString("LootTable"));
			lootTableSeed = compound.getLong("LootTableSeed");
			return true;
		} else {
			return false;
		}
	}

	protected boolean checkLootAndWrite(NBTTagCompound compound) {

		if (lootTable != null) {
			compound.setString("LootTable", lootTable.toString());

			if (lootTableSeed != 0L) {
				compound.setLong("LootTableSeed", lootTableSeed);
			}

			return true;
		} else {
			return false;
		}
	}

	public void fillWithLoot(EntityPlayer player) {

		if (lootTable != null) {
			LootTable loottable = world.getLootTableManager().getLootTableFromLocation(lootTable);
			lootTable = null;
			Random random;

			if (lootTableSeed == 0L) {
				random = new Random();
			} else {
				random = new Random(lootTableSeed);
			}

			LootContext.Builder lootcontext$builder = new LootContext.Builder((WorldServer) world);

			if (player != null) {
				lootcontext$builder.withLuck(player.getLuck());
			}

			loottable.fillInventory(this, random, lootcontext$builder.build());
		}
	}

	public ResourceLocation getLootTable() {

		return lootTable;
	}

	public void setLootTable(ResourceLocation p_189404_1_, long p_189404_2_) {

		lootTable = p_189404_1_;
		lootTableSeed = p_189404_2_;
	}

	/**
	 * Returns true if this thing is named
	 */
	public boolean hasCustomName() {

		return customName != null && !customName.isEmpty();
	}

	public void setCustomName(String p_190575_1_) {

		customName = p_190575_1_;
	}

	/**
	 * Returns the stack in the given slot.
	 */
	public ItemStack getStackInSlot(int index) {

		fillWithLoot(null);
		return getItems().get(index);
	}

	/**
	 * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
	 */
	public ItemStack decrStackSize(int index, int count) {

		fillWithLoot(null);
		ItemStack itemstack = ItemStackHelper.getAndSplit(getItems(), index, count);

		if (!itemstack.isEmpty()) {
			markDirty();
		}

		return itemstack;
	}

	/**
	 * Removes a stack from the given slot and returns it.
	 */
	public ItemStack removeStackFromSlot(int index) {

		fillWithLoot(null);
		return ItemStackHelper.getAndRemove(getItems(), index);
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
	 */
	public void setInventorySlotContents(int index, ItemStack stack) {

		fillWithLoot(null);
		getItems().set(index, stack);

		if (stack.getCount() > getInventoryStackLimit()) {
			stack.setCount(getInventoryStackLimit());
		}

		markDirty();
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

		return true;
	}

	public int getField(int id) {

		return 0;
	}

	public void setField(int id, int value) {

	}

	public int getFieldCount() {

		return 0;
	}

	public void clear() {

		fillWithLoot(null);
		getItems().clear();
	}

	protected abstract NonNullList<ItemStack> getItems();

}
