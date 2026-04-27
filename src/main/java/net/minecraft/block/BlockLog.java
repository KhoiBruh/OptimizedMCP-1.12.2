package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockLog extends BlockRotatedPillar {

	public static final PropertyEnum<BlockLog.EnumAxis> LOG_AXIS = PropertyEnum.create("axis", BlockLog.EnumAxis.class);

	public BlockLog() {

		super(Material.WOOD);
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
		setHardness(2.0F);
		setSoundType(SoundType.WOOD);
	}

	/**
	 * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
	 */
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {

		int i = 4;
		int j = 5;

		if (worldIn.isAreaLoaded(pos.add(-5, -5, -5), pos.add(5, 5, 5))) {
			for (BlockPos blockpos : BlockPos.getAllInBox(pos.add(-4, -4, -4), pos.add(4, 4, 4))) {
				IBlockState iblockstate = worldIn.getBlockState(blockpos);

				if (iblockstate.getMaterial() == Material.LEAVES && !iblockstate.getValue(BlockLeaves.CHECK_DECAY)) {
					worldIn.setBlockState(blockpos, iblockstate.withProperty(BlockLeaves.CHECK_DECAY, true), 4);
				}
			}
		}
	}

	/**
	 * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
	 * IBlockstate
	 */
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {

		return getStateFromMeta(meta).withProperty(LOG_AXIS, BlockLog.EnumAxis.fromFacingAxis(facing.getAxis()));
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withRotation(IBlockState state, Rotation rot) {

		return switch (rot) {
			case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch (state.getValue(LOG_AXIS)) {
				case X -> state.withProperty(LOG_AXIS, EnumAxis.Z);
				case Z -> state.withProperty(LOG_AXIS, EnumAxis.X);
				default -> state;
			};
			default -> state;
		};
	}

	public enum EnumAxis implements IStringSerializable {
		X("x"),
		Y("y"),
		Z("z"),
		NONE("none");

		private final String name;

		EnumAxis(String name) {

			this.name = name;
		}

		public static BlockLog.EnumAxis fromFacingAxis(EnumFacing.Axis axis) {

			return switch (axis) {
				case X -> X;
				case Y -> Y;
				case Z -> Z;
			};
		}

		public String toString() {

			return name;
		}

		public String getName() {

			return name;
		}
	}

}
