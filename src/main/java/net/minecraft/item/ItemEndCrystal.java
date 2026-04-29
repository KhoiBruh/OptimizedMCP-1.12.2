package net.minecraft.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Facing;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.end.DragonFightManager;

import java.util.List;

public class ItemEndCrystal extends Item {

	public ItemEndCrystal() {

		setUnlocalizedName("end_crystal");
		setCreativeTab(CreativeTabs.DECORATIONS);
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	public ActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, Hand hand, Facing facing, float hitX, float hitY, float hitZ) {

		IBlockState iblockstate = worldIn.getBlockState(pos);

		if (iblockstate.getBlock() != Blocks.OBSIDIAN && iblockstate.getBlock() != Blocks.BEDROCK) {
			return ActionResult.FAIL;
		} else {
			BlockPos blockpos = pos.up();
			ItemStack itemstack = player.getHeldItem(hand);

			if (!player.canPlayerEdit(blockpos, facing, itemstack)) {
				return ActionResult.FAIL;
			} else {
				BlockPos blockpos1 = blockpos.up();
				boolean flag = !worldIn.isAirBlock(blockpos) && !worldIn.getBlockState(blockpos).getBlock().isReplaceable(worldIn, blockpos);
				flag = flag | (!worldIn.isAirBlock(blockpos1) && !worldIn.getBlockState(blockpos1).getBlock().isReplaceable(worldIn, blockpos1));

				if (flag) {
					return ActionResult.FAIL;
				} else {
					double d0 = blockpos.getX();
					double d1 = blockpos.getY();
					double d2 = blockpos.getZ();
					List<Entity> list = worldIn.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(d0, d1, d2, d0 + 1D, d1 + 2D, d2 + 1D));

					if (!list.isEmpty()) {
						return ActionResult.FAIL;
					} else {
						if (!worldIn.isRemote) {
							EntityEnderCrystal entityendercrystal = new EntityEnderCrystal(worldIn, (float) pos.getX() + 0.5F, pos.getY() + 1, (float) pos.getZ() + 0.5F);
							entityendercrystal.setShowBottom(false);
							worldIn.spawnEntity(entityendercrystal);

							if (worldIn.provider instanceof WorldProviderEnd) {
								DragonFightManager dragonfightmanager = ((WorldProviderEnd) worldIn.provider).getDragonFightManager();
								dragonfightmanager.respawnDragon();
							}
						}

						itemstack.shrink(1);
						return ActionResult.SUCCESS;
					}
				}
			}
		}
	}

	/**
	 * Returns true if this item has an enchantment glint. By default, this returns
	 * <code>stack.isItemEnchanted()</code>, but other items can override it (for instance, written books always return
	 * true).
	 * <p>
	 * Note that if you override this method, you generally want to also call the super version (on {@link Item}) to get
	 * the glint for enchanted items. Of course, that is unnecessary if the overwritten version always returns true.
	 */
	public boolean hasEffect(ItemStack stack) {

		return true;
	}

}
