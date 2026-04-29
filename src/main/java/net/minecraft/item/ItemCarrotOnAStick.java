package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemCarrotOnAStick extends Item {

	public ItemCarrotOnAStick() {

		setCreativeTab(CreativeTabs.TRANSPORTATION);
		setMaxStackSize(1);
		setMaxDamage(25);
	}

	/**
	 * Returns True is the item is renderer in full 3D when hold.
	 */
	public boolean isFull3D() {

		return true;
	}

	/**
	 * Returns true if this item should be rotated by 180 degrees around the Y axis when being held in an entities
	 * hands.
	 */
	public boolean shouldRotateAroundWhenRendering() {

		return true;
	}

	public TypedActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, Hand handIn) {

		ItemStack itemstack = playerIn.getHeldItem(handIn);

		if (worldIn.isRemote) {
			return new TypedActionResult<>(ActionResult.PASS, itemstack);
		} else {
			if (playerIn.isRiding() && playerIn.getRidingEntity() instanceof EntityPig entitypig) {

				if (itemstack.getMaxDamage() - itemstack.getMetadata() >= 7 && entitypig.boost()) {
					itemstack.damageItem(7, playerIn);

					if (itemstack.isEmpty()) {
						ItemStack itemstack1 = new ItemStack(Items.FISHING_ROD);
						itemstack1.setTagCompound(itemstack.getTagCompound());
						return new TypedActionResult<>(ActionResult.SUCCESS, itemstack1);
					}

					return new TypedActionResult<>(ActionResult.SUCCESS, itemstack);
				}
			}

			playerIn.addStat(StatList.getObjectUseStats(this));
			return new TypedActionResult<>(ActionResult.PASS, itemstack);
		}
	}

}
