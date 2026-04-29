package net.minecraft.block;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import java.util.Random;

public class BlockIce extends BlockBreakable {

	public BlockIce() {

		super(Material.ICE, false);
		slipperiness = 0.98F;
		setTickRandomly(true);
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
	}

	/**
	 * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
	 * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
	 */
	public BlockRenderLayer getBlockLayer() {

		return BlockRenderLayer.TRANSLUCENT;
	}

	/**
	 * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
	 * Block.removedByPlayer
	 */
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {

		player.addStat(StatList.getBlockStats(this));
		player.addExhaustion(0.005F);

		if (canSilkHarvest() && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
			spawnAsEntity(worldIn, pos, getSilkTouchDrop(state));
		} else {
			if (worldIn.provider.doesWaterVaporize()) {
				worldIn.setBlockToAir(pos);
				return;
			}

			int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
			dropBlockAsItem(worldIn, pos, state, i);
			Material material = worldIn.getBlockState(pos.down()).getMaterial();

			if (material.blocksMovement() || material.isLiquid()) {
				worldIn.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState());
			}
		}
	}

	/**
	 * Returns the quantity of items to drop on block destruction.
	 */
	public int quantityDropped(Random random) {

		return 0;
	}

	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {

		if (worldIn.getLightFor(EnumSkyBlock.BLOCK, pos) > 11 - getDefaultState().getLightOpacity()) {
			turnIntoWater(worldIn, pos);
		}
	}

	protected void turnIntoWater(World worldIn, BlockPos pos) {

		if (worldIn.provider.doesWaterVaporize()) {
			worldIn.setBlockToAir(pos);
		} else {
			dropBlockAsItem(worldIn, pos, worldIn.getBlockState(pos), 0);
			worldIn.setBlockState(pos, Blocks.WATER.getDefaultState());
			worldIn.neighborChanged(pos, Blocks.WATER, pos);
		}
	}

	public EnumPushReaction getMobilityFlag(IBlockState state) {

		return EnumPushReaction.NORMAL;
	}

}
