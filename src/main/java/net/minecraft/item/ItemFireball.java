package net.minecraft.item;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Facing;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemFireball extends Item {

	public ItemFireball() {

		setCreativeTab(CreativeTabs.MISC);
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	public ActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, Hand hand, Facing facing, float hitX, float hitY, float hitZ) {

		if (worldIn.isRemote) {
			return ActionResult.SUCCESS;
		} else {
			pos = pos.offset(facing);
			ItemStack itemstack = player.getHeldItem(hand);

			if (!player.canPlayerEdit(pos, facing, itemstack)) {
				return ActionResult.FAIL;
			} else {
				if (worldIn.getBlockState(pos).getMaterial() == Material.AIR) {
					worldIn.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1F, (itemRand.nextFloat() - itemRand.nextFloat()) * 0.2F + 1F);
					worldIn.setBlockState(pos, Blocks.FIRE.getDefaultState());
				}

				if (!player.capabilities.isCreativeMode) {
					itemstack.shrink(1);
				}

				return ActionResult.SUCCESS;
			}
		}
	}

}
