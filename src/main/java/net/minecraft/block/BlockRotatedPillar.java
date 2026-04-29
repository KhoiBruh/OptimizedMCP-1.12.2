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
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRotatedPillar extends Block {

	public static final PropertyEnum<Facing.Axis> AXIS = PropertyEnum.create("axis", Facing.Axis.class);

	protected BlockRotatedPillar(Material materialIn) {

		super(materialIn, materialIn.getMaterialMapColor());
	}

	protected BlockRotatedPillar(Material materialIn, MapColor color) {

		super(materialIn, color);
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withRotation(IBlockState state, Rotation rot) {

		return switch (rot) {
			case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch (state.getValue(AXIS)) {
				case X -> state.withProperty(AXIS, Facing.Axis.Z);
				case Z -> state.withProperty(AXIS, Facing.Axis.X);
				default -> state;
			};
			default -> state;
		};
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		Facing.Axis enumfacing$axis = Facing.Axis.Y;
		int i = meta & 12;

		if (i == 4) {
			enumfacing$axis = Facing.Axis.X;
		} else if (i == 8) {
			enumfacing$axis = Facing.Axis.Z;
		}

		return getDefaultState().withProperty(AXIS, enumfacing$axis);
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		int i = 0;
		Facing.Axis enumfacing$axis = state.getValue(AXIS);

		if (enumfacing$axis == Facing.Axis.X) {
			i |= 4;
		} else if (enumfacing$axis == Facing.Axis.Z) {
			i |= 8;
		}

		return i;
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, AXIS);
	}

	protected ItemStack getSilkTouchDrop(IBlockState state) {

		return new ItemStack(Item.getItemFromBlock(this));
	}

	/**
	 * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
	 * IBlockstate
	 */
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, Facing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {

		return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(AXIS, facing.getAxis());
	}

}
