package net.minecraft.block;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRail extends BlockRailBase {

	public static final PropertyEnum<BlockRailBase.RailDirection> SHAPE = PropertyEnum.create("shape", BlockRailBase.RailDirection.class);

	protected BlockRail() {

		super(false);
		setDefaultState(blockState.getBaseState().withProperty(SHAPE, BlockRailBase.RailDirection.NORTH_SOUTH));
	}

	protected void updateState(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {

		if (blockIn.getDefaultState().canProvidePower() && (new BlockRailBase.Rail(worldIn, pos, state)).countAdjacentRails() == 3) {
			updateDir(worldIn, pos, state, false);
		}
	}

	public IProperty<BlockRailBase.RailDirection> getShapeProperty() {

		return SHAPE;
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(SHAPE, BlockRailBase.RailDirection.byMetadata(meta));
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		return state.getValue(SHAPE).getMetadata();
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
					case ASCENDING_EAST -> state.withProperty(SHAPE, RailDirection.ASCENDING_NORTH);
					case ASCENDING_WEST -> state.withProperty(SHAPE, RailDirection.ASCENDING_SOUTH);
					case ASCENDING_NORTH -> state.withProperty(SHAPE, RailDirection.ASCENDING_WEST);
					case ASCENDING_SOUTH -> state.withProperty(SHAPE, RailDirection.ASCENDING_EAST);
					case SOUTH_EAST -> state.withProperty(SHAPE, RailDirection.NORTH_EAST);
					case SOUTH_WEST -> state.withProperty(SHAPE, RailDirection.SOUTH_EAST);
					case NORTH_WEST -> state.withProperty(SHAPE, RailDirection.SOUTH_WEST);
					case NORTH_EAST -> state.withProperty(SHAPE, RailDirection.NORTH_WEST);
					case NORTH_SOUTH -> state.withProperty(SHAPE, RailDirection.EAST_WEST);
					case EAST_WEST -> state.withProperty(SHAPE, RailDirection.NORTH_SOUTH);
				};

			case CLOCKWISE_90:
				return switch (state.getValue(SHAPE)) {
					case ASCENDING_EAST -> state.withProperty(SHAPE, RailDirection.ASCENDING_SOUTH);
					case ASCENDING_WEST -> state.withProperty(SHAPE, RailDirection.ASCENDING_NORTH);
					case ASCENDING_NORTH -> state.withProperty(SHAPE, RailDirection.ASCENDING_EAST);
					case ASCENDING_SOUTH -> state.withProperty(SHAPE, RailDirection.ASCENDING_WEST);
					case SOUTH_EAST -> state.withProperty(SHAPE, RailDirection.SOUTH_WEST);
					case SOUTH_WEST -> state.withProperty(SHAPE, RailDirection.NORTH_WEST);
					case NORTH_WEST -> state.withProperty(SHAPE, RailDirection.NORTH_EAST);
					case NORTH_EAST -> state.withProperty(SHAPE, RailDirection.SOUTH_EAST);
					case NORTH_SOUTH -> state.withProperty(SHAPE, RailDirection.EAST_WEST);
					case EAST_WEST -> state.withProperty(SHAPE, RailDirection.NORTH_SOUTH);
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

		return new BlockStateContainer(this, SHAPE);
	}

}
