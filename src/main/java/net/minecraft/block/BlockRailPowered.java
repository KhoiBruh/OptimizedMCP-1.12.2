package net.minecraft.block;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRailPowered extends BlockRailBase {

	public static final PropertyEnum<BlockRailBase.RailDirection> SHAPE = PropertyEnum.create("shape", BlockRailBase.RailDirection.class, p_apply_1_ -> p_apply_1_ != RailDirection.NORTH_EAST && p_apply_1_ != RailDirection.NORTH_WEST && p_apply_1_ != RailDirection.SOUTH_EAST && p_apply_1_ != RailDirection.SOUTH_WEST);
	public static final PropertyBool POWERED = PropertyBool.create("powered");

	protected BlockRailPowered() {

		super(true);
		setDefaultState(blockState.getBaseState().withProperty(SHAPE, BlockRailBase.RailDirection.NORTH_SOUTH).withProperty(POWERED, false));
	}

	@SuppressWarnings("incomplete-switch")
	protected boolean findPoweredRailSignal(World worldIn, BlockPos pos, IBlockState state, boolean p_176566_4_, int p_176566_5_) {

		if (p_176566_5_ >= 8) {
			return false;
		} else {
			int i = pos.getX();
			int j = pos.getY();
			int k = pos.getZ();
			boolean flag = true;
			BlockRailBase.RailDirection blockrailbase$enumraildirection = state.getValue(SHAPE);

			switch (blockrailbase$enumraildirection) {
				case NORTH_SOUTH:
					if (p_176566_4_) {
						++k;
					} else {
						--k;
					}

					break;

				case EAST_WEST:
					if (p_176566_4_) {
						--i;
					} else {
						++i;
					}

					break;

				case ASCENDING_EAST:
					if (p_176566_4_) {
						--i;
					} else {
						++i;
						++j;
						flag = false;
					}

					blockrailbase$enumraildirection = BlockRailBase.RailDirection.EAST_WEST;
					break;

				case ASCENDING_WEST:
					if (p_176566_4_) {
						--i;
						++j;
						flag = false;
					} else {
						++i;
					}

					blockrailbase$enumraildirection = BlockRailBase.RailDirection.EAST_WEST;
					break;

				case ASCENDING_NORTH:
					if (p_176566_4_) {
						++k;
					} else {
						--k;
						++j;
						flag = false;
					}

					blockrailbase$enumraildirection = BlockRailBase.RailDirection.NORTH_SOUTH;
					break;

				case ASCENDING_SOUTH:
					if (p_176566_4_) {
						++k;
						++j;
						flag = false;
					} else {
						--k;
					}

					blockrailbase$enumraildirection = BlockRailBase.RailDirection.NORTH_SOUTH;
			}

			if (isSameRailWithPower(worldIn, new BlockPos(i, j, k), p_176566_4_, p_176566_5_, blockrailbase$enumraildirection)) {
				return true;
			} else {
				return flag && isSameRailWithPower(worldIn, new BlockPos(i, j - 1, k), p_176566_4_, p_176566_5_, blockrailbase$enumraildirection);
			}
		}
	}

	protected boolean isSameRailWithPower(World worldIn, BlockPos pos, boolean p_176567_3_, int distance, BlockRailBase.RailDirection p_176567_5_) {

		IBlockState iblockstate = worldIn.getBlockState(pos);

		if (iblockstate.getBlock() != this) {
			return false;
		} else {
			BlockRailBase.RailDirection blockrailbase$enumraildirection = iblockstate.getValue(SHAPE);

			if (p_176567_5_ != BlockRailBase.RailDirection.EAST_WEST || blockrailbase$enumraildirection != BlockRailBase.RailDirection.NORTH_SOUTH && blockrailbase$enumraildirection != BlockRailBase.RailDirection.ASCENDING_NORTH && blockrailbase$enumraildirection != BlockRailBase.RailDirection.ASCENDING_SOUTH) {
				if (p_176567_5_ != BlockRailBase.RailDirection.NORTH_SOUTH || blockrailbase$enumraildirection != BlockRailBase.RailDirection.EAST_WEST && blockrailbase$enumraildirection != BlockRailBase.RailDirection.ASCENDING_EAST && blockrailbase$enumraildirection != BlockRailBase.RailDirection.ASCENDING_WEST) {
					if (iblockstate.getValue(POWERED)) {
						return worldIn.isBlockPowered(pos) || findPoweredRailSignal(worldIn, pos, iblockstate, p_176567_3_, distance + 1);
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	protected void updateState(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {

		boolean flag = state.getValue(POWERED);
		boolean flag1 = worldIn.isBlockPowered(pos) || findPoweredRailSignal(worldIn, pos, state, true, 0) || findPoweredRailSignal(worldIn, pos, state, false, 0);

		if (flag1 != flag) {
			worldIn.setBlockState(pos, state.withProperty(POWERED, flag1), 3);
			worldIn.notifyNeighborsOfStateChange(pos.down(), this, false);

			if (state.getValue(SHAPE).isAscending()) {
				worldIn.notifyNeighborsOfStateChange(pos.up(), this, false);
			}
		}
	}

	public IProperty<BlockRailBase.RailDirection> getShapeProperty() {

		return SHAPE;
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(SHAPE, BlockRailBase.RailDirection.byMetadata(meta & 7)).withProperty(POWERED, (meta & 8) > 0);
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		int i = 0;
		i = i | state.getValue(SHAPE).getMetadata();

		if (state.getValue(POWERED)) {
			i |= 8;
		}

		return i;
	}

	@SuppressWarnings("incomplete-switch")

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withRotation(IBlockState state, Rotation rot) {

		switch (rot) {
			case CLOCKWISE_180:
				switch (state.getValue(SHAPE)) {
					case ASCENDING_EAST:
						return state.withProperty(SHAPE, BlockRailBase.RailDirection.ASCENDING_WEST);

					case ASCENDING_WEST:
						return state.withProperty(SHAPE, BlockRailBase.RailDirection.ASCENDING_EAST);

					case ASCENDING_NORTH:
						return state.withProperty(SHAPE, BlockRailBase.RailDirection.ASCENDING_SOUTH);

					case ASCENDING_SOUTH:
						return state.withProperty(SHAPE, BlockRailBase.RailDirection.ASCENDING_NORTH);

					case SOUTH_EAST:
						return state.withProperty(SHAPE, BlockRailBase.RailDirection.NORTH_WEST);

					case SOUTH_WEST:
						return state.withProperty(SHAPE, BlockRailBase.RailDirection.NORTH_EAST);

					case NORTH_WEST:
						return state.withProperty(SHAPE, BlockRailBase.RailDirection.SOUTH_EAST);

					case NORTH_EAST:
						return state.withProperty(SHAPE, BlockRailBase.RailDirection.SOUTH_WEST);
				}

			case COUNTERCLOCKWISE_90:
				return switch (state.getValue(SHAPE)) {
					case NORTH_SOUTH -> state.withProperty(SHAPE, RailDirection.EAST_WEST);
					case EAST_WEST -> state.withProperty(SHAPE, RailDirection.NORTH_SOUTH);
					case ASCENDING_EAST -> state.withProperty(SHAPE, RailDirection.ASCENDING_NORTH);
					case ASCENDING_WEST -> state.withProperty(SHAPE, RailDirection.ASCENDING_SOUTH);
					case ASCENDING_NORTH -> state.withProperty(SHAPE, RailDirection.ASCENDING_WEST);
					case ASCENDING_SOUTH -> state.withProperty(SHAPE, RailDirection.ASCENDING_EAST);
					case SOUTH_EAST -> state.withProperty(SHAPE, RailDirection.NORTH_EAST);
					case SOUTH_WEST -> state.withProperty(SHAPE, RailDirection.SOUTH_EAST);
					case NORTH_WEST -> state.withProperty(SHAPE, RailDirection.SOUTH_WEST);
					case NORTH_EAST -> state.withProperty(SHAPE, RailDirection.NORTH_WEST);
				};

			case CLOCKWISE_90:
				return switch (state.getValue(SHAPE)) {
					case NORTH_SOUTH -> state.withProperty(SHAPE, RailDirection.EAST_WEST);
					case EAST_WEST -> state.withProperty(SHAPE, RailDirection.NORTH_SOUTH);
					case ASCENDING_EAST -> state.withProperty(SHAPE, RailDirection.ASCENDING_SOUTH);
					case ASCENDING_WEST -> state.withProperty(SHAPE, RailDirection.ASCENDING_NORTH);
					case ASCENDING_NORTH -> state.withProperty(SHAPE, RailDirection.ASCENDING_EAST);
					case ASCENDING_SOUTH -> state.withProperty(SHAPE, RailDirection.ASCENDING_WEST);
					case SOUTH_EAST -> state.withProperty(SHAPE, RailDirection.SOUTH_WEST);
					case SOUTH_WEST -> state.withProperty(SHAPE, RailDirection.NORTH_WEST);
					case NORTH_WEST -> state.withProperty(SHAPE, RailDirection.NORTH_EAST);
					case NORTH_EAST -> state.withProperty(SHAPE, RailDirection.SOUTH_EAST);
				};

			default:
				return state;
		}
	}

	@SuppressWarnings("incomplete-switch")

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {

		BlockRailBase.RailDirection blockrailbase$enumraildirection = state.getValue(SHAPE);

		switch (mirrorIn) {
			case LEFT_RIGHT:
				return switch (blockrailbase$enumraildirection) {
					case ASCENDING_NORTH -> state.withProperty(SHAPE, RailDirection.ASCENDING_SOUTH);
					case ASCENDING_SOUTH -> state.withProperty(SHAPE, RailDirection.ASCENDING_NORTH);
					case SOUTH_EAST -> state.withProperty(SHAPE, RailDirection.NORTH_EAST);
					case SOUTH_WEST -> state.withProperty(SHAPE, RailDirection.NORTH_WEST);
					case NORTH_WEST -> state.withProperty(SHAPE, RailDirection.SOUTH_WEST);
					case NORTH_EAST -> state.withProperty(SHAPE, RailDirection.SOUTH_EAST);
					default -> super.withMirror(state, mirrorIn);
				};

			case FRONT_BACK:
				switch (blockrailbase$enumraildirection) {
					case ASCENDING_EAST:
						return state.withProperty(SHAPE, BlockRailBase.RailDirection.ASCENDING_WEST);

					case ASCENDING_WEST:
						return state.withProperty(SHAPE, BlockRailBase.RailDirection.ASCENDING_EAST);

					case ASCENDING_NORTH:
					case ASCENDING_SOUTH:
					default:
						break;

					case SOUTH_EAST:
						return state.withProperty(SHAPE, BlockRailBase.RailDirection.SOUTH_WEST);

					case SOUTH_WEST:
						return state.withProperty(SHAPE, BlockRailBase.RailDirection.SOUTH_EAST);

					case NORTH_WEST:
						return state.withProperty(SHAPE, BlockRailBase.RailDirection.NORTH_EAST);

					case NORTH_EAST:
						return state.withProperty(SHAPE, BlockRailBase.RailDirection.NORTH_WEST);
				}
		}

		return super.withMirror(state, mirrorIn);
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, SHAPE, POWERED);
	}

}
