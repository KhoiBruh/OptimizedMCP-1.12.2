package net.minecraft.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Facing;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemRedstone extends Item {

	public ItemRedstone() {

		setCreativeTab(CreativeTabs.REDSTONE);
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	public ActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, Hand hand, Facing facing, float hitX, float hitY, float hitZ) {

		boolean flag = worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
		BlockPos blockpos = flag ? pos : pos.offset(facing);
		ItemStack itemstack = player.getHeldItem(hand);

		if (player.canPlayerEdit(blockpos, facing, itemstack) && worldIn.mayPlace(worldIn.getBlockState(blockpos).getBlock(), blockpos, false, facing, null) && Blocks.REDSTONE_WIRE.canPlaceBlockAt(worldIn, blockpos)) {
			worldIn.setBlockState(blockpos, Blocks.REDSTONE_WIRE.getDefaultState());

			if (player instanceof EntityPlayerMP) {
				CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, blockpos, itemstack);
			}

			itemstack.shrink(1);
			return ActionResult.SUCCESS;
		} else {
			return ActionResult.FAIL;
		}
	}

}
