package net.minecraft.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemBucketMilk extends Item {

	public ItemBucketMilk() {

		setMaxStackSize(1);
		setCreativeTab(CreativeTabs.MISC);
	}

	/**
	 * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
	 * the Item before the action is complete.
	 */
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {

		if (entityLiving instanceof EntityPlayerMP entityplayermp) {
			CriteriaTriggers.CONSUME_ITEM.trigger(entityplayermp, stack);
			entityplayermp.addStat(StatList.getObjectUseStats(this));
		}

		if (entityLiving instanceof EntityPlayer && !((EntityPlayer) entityLiving).capabilities.isCreativeMode) {
			stack.shrink(1);
		}

		if (!worldIn.isRemote) {
			entityLiving.clearActivePotions();
		}

		return stack.isEmpty() ? new ItemStack(Items.BUCKET) : stack;
	}

	/**
	 * How long it takes to use or consume an item
	 */
	public int getMaxItemUseDuration(ItemStack stack) {

		return 32;
	}

	/**
	 * returns the action that specifies what animation to play when the items is being used
	 */
	public Action getItemUseAction(ItemStack stack) {

		return Action.DRINK;
	}

	public TypedActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, Hand handIn) {

		playerIn.setActiveHand(handIn);
		return new TypedActionResult<>(ActionResult.SUCCESS, playerIn.getHeldItem(handIn));
	}

}
