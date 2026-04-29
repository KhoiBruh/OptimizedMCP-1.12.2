package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemEmptyMap extends ItemMapBase {

	protected ItemEmptyMap() {

		setCreativeTab(CreativeTabs.MISC);
	}

	public TypedActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, Hand handIn) {

		ItemStack itemstack = ItemMap.setupNewMap(worldIn, playerIn.posX, playerIn.posZ, (byte) 0, true, false);
		ItemStack itemstack1 = playerIn.getHeldItem(handIn);
		itemstack1.shrink(1);

		if (itemstack1.isEmpty()) {
			return new TypedActionResult<>(ActionResult.SUCCESS, itemstack);
		} else {
			if (!playerIn.inventory.addItemStackToInventory(itemstack.copy())) {
				playerIn.dropItem(itemstack, false);
			}

			playerIn.addStat(StatList.getObjectUseStats(this));
			return new TypedActionResult<>(ActionResult.SUCCESS, itemstack1);
		}
	}

}
