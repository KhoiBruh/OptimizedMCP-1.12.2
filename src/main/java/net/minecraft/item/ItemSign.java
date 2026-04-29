package net.minecraft.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Facing;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemSign extends Item {

	public ItemSign() {

		maxStackSize = 16;
		setCreativeTab(CreativeTabs.DECORATIONS);
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	public ActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, Hand hand, Facing facing, float hitX, float hitY, float hitZ) {

		IBlockState iblockstate = worldIn.getBlockState(pos);
		boolean flag = iblockstate.getBlock().isReplaceable(worldIn, pos);

		if (facing != Facing.DOWN && (iblockstate.getMaterial().isSolid() || flag) && (!flag || facing == Facing.UP)) {
			pos = pos.offset(facing);
			ItemStack itemstack = player.getHeldItem(hand);

			if (player.canPlayerEdit(pos, facing, itemstack) && Blocks.STANDING_SIGN.canPlaceBlockAt(worldIn, pos)) {
				if (worldIn.isRemote) {
					return ActionResult.SUCCESS;
				} else {
					pos = flag ? pos.down() : pos;

					if (facing == Facing.UP) {
						int i = MathHelper.floor((double) ((player.rotationYaw + 180F) * 16F / 360F) + 0.5D) & 15;
						worldIn.setBlockState(pos, Blocks.STANDING_SIGN.getDefaultState().withProperty(BlockStandingSign.ROTATION, i), 11);
					} else {
						worldIn.setBlockState(pos, Blocks.WALL_SIGN.getDefaultState().withProperty(BlockWallSign.FACING, facing), 11);
					}

					TileEntity tileentity = worldIn.getTileEntity(pos);

					if (tileentity instanceof TileEntitySign && !ItemBlock.setTileEntityNBT(worldIn, player, pos, itemstack)) {
						player.openEditSign((TileEntitySign) tileentity);
					}

					if (player instanceof EntityPlayerMP) {
						CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, itemstack);
					}

					itemstack.shrink(1);
					return ActionResult.SUCCESS;
				}
			} else {
				return ActionResult.FAIL;
			}
		} else {
			return ActionResult.FAIL;
		}
	}

}
