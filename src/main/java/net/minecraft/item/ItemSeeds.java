package net.minecraft.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Facing;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSeeds extends Item {

	private final Block crops;

	/**
	 * BlockID of the block the seeds can be planted on.
	 */
	private final Block soilBlockID;

	public ItemSeeds(Block crops, Block soil) {

		this.crops = crops;
		soilBlockID = soil;
		setCreativeTab(CreativeTabs.MATERIALS);
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	public ActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, Hand hand, Facing facing, float hitX, float hitY, float hitZ) {

		ItemStack itemstack = player.getHeldItem(hand);

		if (facing == Facing.UP && player.canPlayerEdit(pos.offset(facing), facing, itemstack) && worldIn.getBlockState(pos).getBlock() == soilBlockID && worldIn.isAirBlock(pos.up())) {
			worldIn.setBlockState(pos.up(), crops.getDefaultState());

			if (player instanceof EntityPlayerMP) {
				CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos.up(), itemstack);
			}

			itemstack.shrink(1);
			return ActionResult.SUCCESS;
		} else {
			return ActionResult.FAIL;
		}
	}

}
