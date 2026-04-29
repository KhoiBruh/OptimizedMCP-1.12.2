package net.minecraft.item;

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Facing;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemMinecart extends Item {

	private static final IBehaviorDispenseItem MINECART_DISPENSER_BEHAVIOR = new BehaviorDefaultDispenseItem() {
		private final BehaviorDefaultDispenseItem behaviourDefaultDispenseItem = new BehaviorDefaultDispenseItem();

		public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {

			Facing enumfacing = source.getBlockState().getValue(BlockDispenser.FACING);
			World world = source.getWorld();
			double d0 = source.x() + (double) enumfacing.getFrontOffsetX() * 1.125D;
			double d1 = Math.floor(source.y()) + (double) enumfacing.getFrontOffsetY();
			double d2 = source.z() + (double) enumfacing.getFrontOffsetZ() * 1.125D;
			BlockPos blockpos = source.getBlockPos().offset(enumfacing);
			IBlockState iblockstate = world.getBlockState(blockpos);
			BlockRailBase.RailDirection blockrailbase$enumraildirection = iblockstate.getBlock() instanceof BlockRailBase ? iblockstate.getValue(((BlockRailBase) iblockstate.getBlock()).getShapeProperty()) : BlockRailBase.RailDirection.NORTH_SOUTH;
			double d3;

			if (BlockRailBase.isRailBlock(iblockstate)) {
				if (blockrailbase$enumraildirection.isAscending()) {
					d3 = 0.6D;
				} else {
					d3 = 0.1D;
				}
			} else {
				if (iblockstate.getMaterial() != Material.AIR || !BlockRailBase.isRailBlock(world.getBlockState(blockpos.down()))) {
					return behaviourDefaultDispenseItem.dispense(source, stack);
				}

				IBlockState iblockstate1 = world.getBlockState(blockpos.down());
				BlockRailBase.RailDirection blockrailbase$enumraildirection1 = iblockstate1.getBlock() instanceof BlockRailBase ? iblockstate1.getValue(((BlockRailBase) iblockstate1.getBlock()).getShapeProperty()) : BlockRailBase.RailDirection.NORTH_SOUTH;

				if (enumfacing != Facing.DOWN && blockrailbase$enumraildirection1.isAscending()) {
					d3 = -0.4D;
				} else {
					d3 = -0.9D;
				}
			}

			EntityMinecart entityminecart = EntityMinecart.create(world, d0, d1 + d3, d2, ((ItemMinecart) stack.getItem()).minecartType);

			if (stack.hasDisplayName()) {
				entityminecart.setCustomNameTag(stack.getDisplayName());
			}

			world.spawnEntity(entityminecart);
			stack.shrink(1);
			return stack;
		}

	};
	private final EntityMinecart.Type minecartType;

	public ItemMinecart(EntityMinecart.Type typeIn) {

		maxStackSize = 1;
		minecartType = typeIn;
		setCreativeTab(CreativeTabs.TRANSPORTATION);
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, MINECART_DISPENSER_BEHAVIOR);
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	public ActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, Hand hand, Facing facing, float hitX, float hitY, float hitZ) {

		IBlockState iblockstate = worldIn.getBlockState(pos);

		if (!BlockRailBase.isRailBlock(iblockstate)) {
			return ActionResult.FAIL;
		} else {
			ItemStack itemstack = player.getHeldItem(hand);

			if (!worldIn.isRemote) {
				BlockRailBase.RailDirection blockrailbase$enumraildirection = iblockstate.getBlock() instanceof BlockRailBase ? iblockstate.getValue(((BlockRailBase) iblockstate.getBlock()).getShapeProperty()) : BlockRailBase.RailDirection.NORTH_SOUTH;
				double d0 = 0D;

				if (blockrailbase$enumraildirection.isAscending()) {
					d0 = 0.5D;
				}

				EntityMinecart entityminecart = EntityMinecart.create(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.0625D + d0, (double) pos.getZ() + 0.5D, minecartType);

				if (itemstack.hasDisplayName()) {
					entityminecart.setCustomNameTag(itemstack.getDisplayName());
				}

				worldIn.spawnEntity(entityminecart);
			}

			itemstack.shrink(1);
			return ActionResult.SUCCESS;
		}
	}

}
