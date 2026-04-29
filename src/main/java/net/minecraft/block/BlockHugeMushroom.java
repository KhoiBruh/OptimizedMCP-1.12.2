package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockHugeMushroom extends Block {

	public static final PropertyEnum<BlockHugeMushroom.Type> VARIANT = PropertyEnum.create("variant", BlockHugeMushroom.Type.class);
	private final Block smallBlock;

	public BlockHugeMushroom(Material materialIn, MapColor color, Block smallBlockIn) {

		super(materialIn, color);
		setDefaultState(blockState.getBaseState().withProperty(VARIANT, BlockHugeMushroom.Type.ALL_OUTSIDE));
		smallBlock = smallBlockIn;
	}

	/**
	 * Returns the quantity of items to drop on block destruction.
	 */
	public int quantityDropped(Random random) {

		return Math.max(0, random.nextInt(10) - 7);
	}

	/**
	 * Get the MapColor for this Block and the given BlockState
	 */
	public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {

		return switch (state.getValue(VARIANT)) {
			case ALL_STEM -> MapColor.CLOTH;
			case ALL_INSIDE, STEM -> MapColor.SAND;
			default -> super.getMapColor(state, worldIn, pos);
		};
	}

	/**
	 * Get the Item that this Block should drop when harvested.
	 */
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {

		return Item.getItemFromBlock(smallBlock);
	}

	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {

		return new ItemStack(smallBlock);
	}

	/**
	 * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
	 * IBlockstate
	 */
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, Facing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {

		return getDefaultState();
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(VARIANT, BlockHugeMushroom.Type.byMetadata(meta));
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		return state.getValue(VARIANT).getMetadata();
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withRotation(IBlockState state, Rotation rot) {

		switch (rot) {
			case CLOCKWISE_180:
				switch (state.getValue(VARIANT)) {
					case STEM:
						break;

					case NORTH_WEST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.SOUTH_EAST);

					case NORTH:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.SOUTH);

					case NORTH_EAST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.SOUTH_WEST);

					case WEST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.EAST);

					case EAST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.WEST);

					case SOUTH_WEST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.NORTH_EAST);

					case SOUTH:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.NORTH);

					case SOUTH_EAST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.NORTH_WEST);

					default:
						return state;
				}

			case COUNTERCLOCKWISE_90:
				switch (state.getValue(VARIANT)) {
					case STEM:
						break;

					case NORTH_WEST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.SOUTH_WEST);

					case NORTH:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.WEST);

					case NORTH_EAST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.NORTH_WEST);

					case WEST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.SOUTH);

					case EAST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.NORTH);

					case SOUTH_WEST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.SOUTH_EAST);

					case SOUTH:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.EAST);

					case SOUTH_EAST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.NORTH_EAST);

					default:
						return state;
				}

			case CLOCKWISE_90:
				switch (state.getValue(VARIANT)) {
					case STEM:
						break;

					case NORTH_WEST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.NORTH_EAST);

					case NORTH:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.EAST);

					case NORTH_EAST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.SOUTH_EAST);

					case WEST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.NORTH);

					case EAST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.SOUTH);

					case SOUTH_WEST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.NORTH_WEST);

					case SOUTH:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.WEST);

					case SOUTH_EAST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.SOUTH_WEST);

					default:
						return state;
				}

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

		BlockHugeMushroom.Type blockhugemushroom$enumtype = state.getValue(VARIANT);

		switch (mirrorIn) {
			case LEFT_RIGHT:
				return switch (blockhugemushroom$enumtype) {
					case NORTH_WEST -> state.withProperty(VARIANT, Type.SOUTH_WEST);
					case NORTH -> state.withProperty(VARIANT, Type.SOUTH);
					case NORTH_EAST -> state.withProperty(VARIANT, Type.SOUTH_EAST);
					case SOUTH_WEST -> state.withProperty(VARIANT, Type.NORTH_WEST);
					case SOUTH -> state.withProperty(VARIANT, Type.NORTH);
					case SOUTH_EAST -> state.withProperty(VARIANT, Type.NORTH_EAST);
					default -> super.withMirror(state, mirrorIn);
				};

			case FRONT_BACK:
				switch (blockhugemushroom$enumtype) {
					case NORTH_WEST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.NORTH_EAST);

					case NORTH:
					case SOUTH:
					default:
						break;

					case NORTH_EAST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.NORTH_WEST);

					case WEST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.EAST);

					case EAST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.WEST);

					case SOUTH_WEST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.SOUTH_EAST);

					case SOUTH_EAST:
						return state.withProperty(VARIANT, BlockHugeMushroom.Type.SOUTH_WEST);
				}
		}

		return super.withMirror(state, mirrorIn);
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, VARIANT);
	}

	public enum Type implements IStringSerializable {
		NORTH_WEST(1, "north_west"),
		NORTH(2, "north"),
		NORTH_EAST(3, "north_east"),
		WEST(4, "west"),
		CENTER(5, "center"),
		EAST(6, "east"),
		SOUTH_WEST(7, "south_west"),
		SOUTH(8, "south"),
		SOUTH_EAST(9, "south_east"),
		STEM(10, "stem"),
		ALL_INSIDE(0, "all_inside"),
		ALL_OUTSIDE(14, "all_outside"),
		ALL_STEM(15, "all_stem");

		private static final BlockHugeMushroom.Type[] META_LOOKUP = new BlockHugeMushroom.Type[16];

		static {
			for (BlockHugeMushroom.Type blockhugemushroom$enumtype : values()) {
				META_LOOKUP[blockhugemushroom$enumtype.getMetadata()] = blockhugemushroom$enumtype;
			}
		}

		private final int meta;
		private final String name;

		Type(int meta, String name) {

			this.meta = meta;
			this.name = name;
		}

		public static BlockHugeMushroom.Type byMetadata(int meta) {

			if (meta < 0 || meta >= META_LOOKUP.length) {
				meta = 0;
			}

			BlockHugeMushroom.Type blockhugemushroom$enumtype = META_LOOKUP[meta];
			return blockhugemushroom$enumtype == null ? META_LOOKUP[0] : blockhugemushroom$enumtype;
		}

		public int getMetadata() {

			return meta;
		}

		public String toString() {

			return name;
		}

		public String getName() {

			return name;
		}
	}

}
