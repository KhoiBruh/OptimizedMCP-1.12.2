package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Facing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.Random;

public class BlockChorusFlower extends Block {

	public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 5);

	protected BlockChorusFlower() {

		super(Material.PLANTS, MapColor.PURPLE);
		setDefaultState(blockState.getBaseState().withProperty(AGE, 0));
		setCreativeTab(CreativeTabs.DECORATIONS);
		setTickRandomly(true);
	}

	private static boolean areAllNeighborsEmpty(World worldIn, BlockPos pos, Facing excludingSide) {

		for (Facing enumfacing : Facing.Plane.HORIZONTAL) {
			if (enumfacing != excludingSide && !worldIn.isAirBlock(pos.offset(enumfacing))) {
				return false;
			}
		}

		return true;
	}

	public static void generatePlant(World worldIn, BlockPos pos, Random rand, int p_185603_3_) {

		worldIn.setBlockState(pos, Blocks.CHORUS_PLANT.getDefaultState(), 2);
		growTreeRecursive(worldIn, pos, rand, pos, p_185603_3_, 0);
	}

	private static void growTreeRecursive(World worldIn, BlockPos p_185601_1_, Random rand, BlockPos p_185601_3_, int p_185601_4_, int p_185601_5_) {

		int i = rand.nextInt(4) + 1;

		if (p_185601_5_ == 0) {
			++i;
		}

		for (int j = 0; j < i; ++j) {
			BlockPos blockpos = p_185601_1_.up(j + 1);

			if (!areAllNeighborsEmpty(worldIn, blockpos, null)) {
				return;
			}

			worldIn.setBlockState(blockpos, Blocks.CHORUS_PLANT.getDefaultState(), 2);
		}

		boolean flag = false;

		if (p_185601_5_ < 4) {
			int l = rand.nextInt(4);

			if (p_185601_5_ == 0) {
				++l;
			}

			for (int k = 0; k < l; ++k) {
				Facing enumfacing = Facing.Plane.HORIZONTAL.random(rand);
				BlockPos blockpos1 = p_185601_1_.up(i).offset(enumfacing);

				if (Math.abs(blockpos1.getX() - p_185601_3_.getX()) < p_185601_4_ && Math.abs(blockpos1.getZ() - p_185601_3_.getZ()) < p_185601_4_ && worldIn.isAirBlock(blockpos1) && worldIn.isAirBlock(blockpos1.down()) && areAllNeighborsEmpty(worldIn, blockpos1, enumfacing.getOpposite())) {
					flag = true;
					worldIn.setBlockState(blockpos1, Blocks.CHORUS_PLANT.getDefaultState(), 2);
					growTreeRecursive(worldIn, blockpos1, rand, p_185601_3_, p_185601_4_, p_185601_5_ + 1);
				}
			}
		}

		if (!flag) {
			worldIn.setBlockState(p_185601_1_.up(i), Blocks.CHORUS_FLOWER.getDefaultState().withProperty(AGE, 5), 2);
		}
	}

	/**
	 * Get the Item that this Block should drop when harvested.
	 */
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {

		return Items.AIR;
	}

	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {

		if (!canSurvive(worldIn, pos)) {
			worldIn.destroyBlock(pos, true);
		} else {
			BlockPos blockpos = pos.up();

			if (worldIn.isAirBlock(blockpos) && blockpos.getY() < 256) {
				int i = state.getValue(AGE);

				if (i < 5 && rand.nextInt(1) == 0) {
					boolean flag = false;
					boolean flag1 = false;
					IBlockState iblockstate = worldIn.getBlockState(pos.down());
					Block block = iblockstate.getBlock();

					if (block == Blocks.END_STONE) {
						flag = true;
					} else if (block == Blocks.CHORUS_PLANT) {
						int j = 1;

						for (int k = 0; k < 4; ++k) {
							Block block1 = worldIn.getBlockState(pos.down(j + 1)).getBlock();

							if (block1 != Blocks.CHORUS_PLANT) {
								if (block1 == Blocks.END_STONE) {
									flag1 = true;
								}

								break;
							}

							++j;
						}

						int i1 = 4;

						if (flag1) {
							++i1;
						}

						if (j < 2 || rand.nextInt(i1) >= j) {
							flag = true;
						}
					} else if (iblockstate.getMaterial() == Material.AIR) {
						flag = true;
					}

					if (flag && areAllNeighborsEmpty(worldIn, blockpos, null) && worldIn.isAirBlock(pos.up(2))) {
						worldIn.setBlockState(pos, Blocks.CHORUS_PLANT.getDefaultState(), 2);
						placeGrownFlower(worldIn, blockpos, i);
					} else if (i < 4) {
						int l = rand.nextInt(4);
						boolean flag2 = false;

						if (flag1) {
							++l;
						}

						for (int j1 = 0; j1 < l; ++j1) {
							Facing enumfacing = Facing.Plane.HORIZONTAL.random(rand);
							BlockPos blockpos1 = pos.offset(enumfacing);

							if (worldIn.isAirBlock(blockpos1) && worldIn.isAirBlock(blockpos1.down()) && areAllNeighborsEmpty(worldIn, blockpos1, enumfacing.getOpposite())) {
								placeGrownFlower(worldIn, blockpos1, i + 1);
								flag2 = true;
							}
						}

						if (flag2) {
							worldIn.setBlockState(pos, Blocks.CHORUS_PLANT.getDefaultState(), 2);
						} else {
							placeDeadFlower(worldIn, pos);
						}
					} else if (i == 4) {
						placeDeadFlower(worldIn, pos);
					}
				}
			}
		}
	}

	private void placeGrownFlower(World worldIn, BlockPos pos, int age) {

		worldIn.setBlockState(pos, getDefaultState().withProperty(AGE, age), 2);
		worldIn.playEvent(1033, pos, 0);
	}

	private void placeDeadFlower(World worldIn, BlockPos pos) {

		worldIn.setBlockState(pos, getDefaultState().withProperty(AGE, 5), 2);
		worldIn.playEvent(1034, pos, 0);
	}

	public boolean isFullCube(IBlockState state) {

		return false;
	}

	/**
	 * Used to determine ambient occlusion and culling when rebuilding chunks for render
	 */
	public boolean isOpaqueCube(IBlockState state) {

		return false;
	}

	/**
	 * Checks if this block can be placed exactly at the given position.
	 */
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {

		return super.canPlaceBlockAt(worldIn, pos) && canSurvive(worldIn, pos);
	}

	/**
	 * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
	 * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
	 * block, etc.
	 */
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {

		if (!canSurvive(worldIn, pos)) {
			worldIn.scheduleUpdate(pos, this, 1);
		}
	}

	public boolean canSurvive(World worldIn, BlockPos pos) {

		IBlockState iblockstate = worldIn.getBlockState(pos.down());
		Block block = iblockstate.getBlock();

		if (block != Blocks.CHORUS_PLANT && block != Blocks.END_STONE) {
			if (iblockstate.getMaterial() == Material.AIR) {
				int i = 0;

				for (Facing enumfacing : Facing.Plane.HORIZONTAL) {
					IBlockState iblockstate1 = worldIn.getBlockState(pos.offset(enumfacing));
					Block block1 = iblockstate1.getBlock();

					if (block1 == Blocks.CHORUS_PLANT) {
						++i;
					} else if (iblockstate1.getMaterial() != Material.AIR) {
						return false;
					}
				}

				return i == 1;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
	 * Block.removedByPlayer
	 */
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {

		super.harvestBlock(worldIn, player, pos, state, te, stack);
		spawnAsEntity(worldIn, pos, new ItemStack(Item.getItemFromBlock(this)));
	}

	protected ItemStack getSilkTouchDrop(IBlockState state) {

		return ItemStack.EMPTY;
	}

	/**
	 * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
	 * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
	 */
	public BlockRenderLayer getBlockLayer() {

		return BlockRenderLayer.CUTOUT;
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(AGE, meta);
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		return state.getValue(AGE);
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, AGE);
	}

	/**
	 * Get the geometry of the queried face at the given position and state. This is used to decide whether things like
	 * buttons are allowed to be placed on the face, or how glass panes connect to the face, among other things.
	 * <p>
	 * Common values are {@code SOLID}, which is the default, and {@code UNDEFINED}, which represents something that
	 * does not fit the other descriptions and will generally cause other things not to connect to the face.
	 *
	 * @return an approximation of the form of the given face
	 */
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, Facing face) {

		return BlockFaceShape.UNDEFINED;
	}

}
