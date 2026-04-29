package net.minecraft.inventory;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.math.MathHelper;

public class SlotFurnaceOutput extends Slot {

	/**
	 * The player that is using the GUI where this slot resides.
	 */
	private final EntityPlayer player;
	private int removeCount;

	public SlotFurnaceOutput(EntityPlayer player, IInventory inventoryIn, int slotIndex, int xPosition, int yPosition) {

		super(inventoryIn, slotIndex, xPosition, yPosition);
		this.player = player;
	}

	/**
	 * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
	 */
	public boolean isItemValid(ItemStack stack) {

		return false;
	}

	/**
	 * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
	 * stack.
	 */
	public ItemStack decrStackSize(int amount) {

		if (getHasStack()) {
			removeCount += Math.min(amount, getStack().getCount());
		}

		return super.decrStackSize(amount);
	}

	public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {

		onCrafting(stack);
		super.onTake(thePlayer, stack);
		return stack;
	}

	/**
	 * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
	 * internal count then calls onCrafting(item).
	 */
	protected void onCrafting(ItemStack stack, int amount) {

		removeCount += amount;
		onCrafting(stack);
	}

	/**
	 * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
	 */
	protected void onCrafting(ItemStack stack) {

		stack.onCrafting(player.world, player, removeCount);

		if (!player.world.isRemote) {
			int i = removeCount;
			float f = FurnaceRecipes.instance().getSmeltingExperience(stack);

			if (f == 0F) {
				i = 0;
			} else if (f < 1F) {
				int j = MathHelper.floor((float) i * f);

				if (j < MathHelper.ceil((float) i * f) && Math.random() < (double) ((float) i * f - (float) j)) {
					++j;
				}

				i = j;
			}

			while (i > 0) {
				int k = EntityXPOrb.getXPSplit(i);
				i -= k;
				player.world.spawnEntity(new EntityXPOrb(player.world, player.posX, player.posY + 0.5D, player.posZ + 0.5D, k));
			}
		}

		removeCount = 0;
	}

}
