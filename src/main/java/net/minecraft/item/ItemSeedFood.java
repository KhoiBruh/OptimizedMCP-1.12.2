package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Facing;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSeedFood extends ItemFood {

	private final Block crops;

	/**
	 * Block ID of the soil this seed food should be planted on.
	 */
	private final Block soilId;

	public ItemSeedFood(int healAmount, float saturation, Block crops, Block soil) {

		super(healAmount, saturation, false);
		this.crops = crops;
		soilId = soil;
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	public ActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, Hand hand, Facing facing, float hitX, float hitY, float hitZ) {

		ItemStack itemstack = player.getHeldItem(hand);

		if (facing == Facing.UP && player.canPlayerEdit(pos.offset(facing), facing, itemstack) && worldIn.getBlockState(pos).getBlock() == soilId && worldIn.isAirBlock(pos.up())) {
			worldIn.setBlockState(pos.up(), crops.getDefaultState(), 11);
			itemstack.shrink(1);
			return ActionResult.SUCCESS;
		} else {
			return ActionResult.FAIL;
		}
	}

}
