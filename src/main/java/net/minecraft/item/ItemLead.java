package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Facing;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemLead extends Item {

	public ItemLead() {

		setCreativeTab(CreativeTabs.TOOLS);
	}

	public static boolean attachToFence(EntityPlayer player, World worldIn, BlockPos fence) {

		EntityLeashKnot entityleashknot = EntityLeashKnot.getKnotForPosition(worldIn, fence);
		boolean flag = false;
		double d0 = 7D;
		int i = fence.getX();
		int j = fence.getY();
		int k = fence.getZ();

		for (EntityLiving entityliving : worldIn.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB((double) i - 7D, (double) j - 7D, (double) k - 7D, (double) i + 7D, (double) j + 7D, (double) k + 7D))) {
			if (entityliving.getLeashed() && entityliving.getLeashHolder() == player) {
				if (entityleashknot == null) {
					entityleashknot = EntityLeashKnot.createKnot(worldIn, fence);
				}

				entityliving.setLeashHolder(entityleashknot, true);
				flag = true;
			}
		}

		return flag;
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	public ActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, Hand hand, Facing facing, float hitX, float hitY, float hitZ) {

		Block block = worldIn.getBlockState(pos).getBlock();

		if (!(block instanceof BlockFence)) {
			return ActionResult.PASS;
		} else {
			if (!worldIn.isRemote) {
				attachToFence(player, worldIn, pos);
			}

			return ActionResult.SUCCESS;
		}
	}

}
