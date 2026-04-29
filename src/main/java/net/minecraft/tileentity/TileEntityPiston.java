package net.minecraft.tileentity;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.List;

public class TileEntityPiston extends TileEntity implements ITickable {

	private static final ThreadLocal<EnumFacing> MOVING_ENTITY = ThreadLocal.withInitial(() -> null);
	private IBlockState pistonState;
	private EnumFacing pistonFacing;
	/**
	 * if this piston is extending or not
	 */
	private boolean extending;
	private boolean shouldHeadBeRendered;
	private float progress;

	/**
	 * the progress in (de)extending
	 */
	private float lastProgress;

	public TileEntityPiston() {

	}

	public TileEntityPiston(IBlockState pistonStateIn, EnumFacing pistonFacingIn, boolean extendingIn, boolean shouldHeadBeRenderedIn) {

		pistonState = pistonStateIn;
		pistonFacing = pistonFacingIn;
		extending = extendingIn;
		shouldHeadBeRendered = shouldHeadBeRenderedIn;
	}

	private static double getDeltaX(AxisAlignedBB p_190611_0_, EnumFacing facing, AxisAlignedBB p_190611_2_) {

		return facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? p_190611_0_.maxX - p_190611_2_.minX : p_190611_2_.maxX - p_190611_0_.minX;
	}

	private static double getDeltaY(AxisAlignedBB p_190608_0_, EnumFacing facing, AxisAlignedBB p_190608_2_) {

		return facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? p_190608_0_.maxY - p_190608_2_.minY : p_190608_2_.maxY - p_190608_0_.minY;
	}

	private static double getDeltaZ(AxisAlignedBB p_190604_0_, EnumFacing facing, AxisAlignedBB p_190604_2_) {

		return facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? p_190604_0_.maxZ - p_190604_2_.minZ : p_190604_2_.maxZ - p_190604_0_.minZ;
	}

	public static void registerFixesPiston(DataFixer fixer) {

	}

	public IBlockState getPistonState() {

		return pistonState;
	}

	public NBTTagCompound getUpdateTag() {

		return writeToNBT(new NBTTagCompound());
	}

	public int getBlockMetadata() {

		return 0;
	}

	/**
	 * Returns true if a piston is extending
	 */
	public boolean isExtending() {

		return extending;
	}

	public EnumFacing getFacing() {

		return pistonFacing;
	}

	public boolean shouldPistonHeadBeRendered() {

		return shouldHeadBeRendered;
	}

	/**
	 * Get interpolated progress value (between lastProgress and progress) given the fractional time between ticks as an
	 * argument
	 */
	public float getProgress(float ticks) {

		if (ticks > 1.0F) {
			ticks = 1.0F;
		}

		return lastProgress + (progress - lastProgress) * ticks;
	}

	public float getOffsetX(float ticks) {

		return (float) pistonFacing.getFrontOffsetX() * getExtendedProgress(getProgress(ticks));
	}

	public float getOffsetY(float ticks) {

		return (float) pistonFacing.getFrontOffsetY() * getExtendedProgress(getProgress(ticks));
	}

	public float getOffsetZ(float ticks) {

		return (float) pistonFacing.getFrontOffsetZ() * getExtendedProgress(getProgress(ticks));
	}

	private float getExtendedProgress(float p_184320_1_) {

		return extending ? p_184320_1_ - 1.0F : 1.0F - p_184320_1_;
	}

	public AxisAlignedBB getAABB(IBlockAccess p_184321_1_, BlockPos p_184321_2_) {

		return getAABB(p_184321_1_, p_184321_2_, progress).union(getAABB(p_184321_1_, p_184321_2_, lastProgress));
	}

	public AxisAlignedBB getAABB(IBlockAccess p_184319_1_, BlockPos p_184319_2_, float p_184319_3_) {

		p_184319_3_ = getExtendedProgress(p_184319_3_);
		IBlockState iblockstate = getCollisionRelatedBlockState();
		return iblockstate.getBoundingBox(p_184319_1_, p_184319_2_).offset(p_184319_3_ * (float) pistonFacing.getFrontOffsetX(), p_184319_3_ * (float) pistonFacing.getFrontOffsetY(), p_184319_3_ * (float) pistonFacing.getFrontOffsetZ());
	}

	private IBlockState getCollisionRelatedBlockState() {

		return !isExtending() && shouldPistonHeadBeRendered() ? Blocks.PISTON_HEAD.getDefaultState().withProperty(BlockPistonExtension.TYPE, pistonState.getBlock() == Blocks.STICKY_PISTON ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT).withProperty(BlockPistonExtension.FACING, pistonState.getValue(BlockPistonBase.FACING)) : pistonState;
	}

	private void moveCollidedEntities(float p_184322_1_) {

		EnumFacing enumfacing = extending ? pistonFacing : pistonFacing.getOpposite();
		double d0 = p_184322_1_ - progress;
		List<AxisAlignedBB> list = Lists.newArrayList();
		getCollisionRelatedBlockState().addCollisionBoxToList(world, BlockPos.ORIGIN, new AxisAlignedBB(BlockPos.ORIGIN), list, null, true);

		if (!list.isEmpty()) {
			AxisAlignedBB axisalignedbb = moveByPositionAndProgress(getMinMaxPiecesAABB(list));
			List<Entity> list1 = world.getEntitiesWithinAABBExcludingEntity(null, getMovementArea(axisalignedbb, enumfacing, d0).union(axisalignedbb));

			if (!list1.isEmpty()) {
				boolean flag = pistonState.getBlock() == Blocks.SLIME_BLOCK;

				for (Entity entity : list1) {
					if (entity.getPushReaction() != EnumPushReaction.IGNORE) {
						if (flag) {
							switch (enumfacing.getAxis()) {
								case X:
									entity.motionX = enumfacing.getFrontOffsetX();
									break;

								case Y:
									entity.motionY = enumfacing.getFrontOffsetY();
									break;

								case Z:
									entity.motionZ = enumfacing.getFrontOffsetZ();
							}
						}

						double d1 = 0.0D;

						for (AxisAlignedBB axisAlignedBB : list) {
							AxisAlignedBB axisalignedbb1 = getMovementArea(moveByPositionAndProgress(axisAlignedBB), enumfacing, d0);
							AxisAlignedBB axisalignedbb2 = entity.getEntityBoundingBox();

							if (axisalignedbb1.intersects(axisalignedbb2)) {
								d1 = Math.max(d1, getMovement(axisalignedbb1, enumfacing, axisalignedbb2));

								if (d1 >= d0) {
									break;
								}
							}
						}

						if (d1 > 0.0D) {
							d1 = Math.min(d1, d0) + 0.01D;
							MOVING_ENTITY.set(enumfacing);
							entity.move(MoverType.PISTON, d1 * (double) enumfacing.getFrontOffsetX(), d1 * (double) enumfacing.getFrontOffsetY(), d1 * (double) enumfacing.getFrontOffsetZ());
							MOVING_ENTITY.remove();

							if (!extending && shouldHeadBeRendered) {
								fixEntityWithinPistonBase(entity, enumfacing, d0);
							}
						}
					}
				}
			}
		}
	}

	private AxisAlignedBB getMinMaxPiecesAABB(List<AxisAlignedBB> p_191515_1_) {

		double d0 = 0.0D;
		double d1 = 0.0D;
		double d2 = 0.0D;
		double d3 = 1.0D;
		double d4 = 1.0D;
		double d5 = 1.0D;

		for (AxisAlignedBB axisalignedbb : p_191515_1_) {
			d0 = Math.min(axisalignedbb.minX, d0);
			d1 = Math.min(axisalignedbb.minY, d1);
			d2 = Math.min(axisalignedbb.minZ, d2);
			d3 = Math.max(axisalignedbb.maxX, d3);
			d4 = Math.max(axisalignedbb.maxY, d4);
			d5 = Math.max(axisalignedbb.maxZ, d5);
		}

		return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
	}

	private double getMovement(AxisAlignedBB p_190612_1_, EnumFacing facing, AxisAlignedBB p_190612_3_) {

		return switch (facing.getAxis()) {
			case X -> getDeltaX(p_190612_1_, facing, p_190612_3_);
			case Z -> getDeltaZ(p_190612_1_, facing, p_190612_3_);
			default -> getDeltaY(p_190612_1_, facing, p_190612_3_);
		};
	}

	private AxisAlignedBB moveByPositionAndProgress(AxisAlignedBB p_190607_1_) {

		double d0 = getExtendedProgress(progress);
		return p_190607_1_.offset((double) pos.getX() + d0 * (double) pistonFacing.getFrontOffsetX(), (double) pos.getY() + d0 * (double) pistonFacing.getFrontOffsetY(), (double) pos.getZ() + d0 * (double) pistonFacing.getFrontOffsetZ());
	}

	private AxisAlignedBB getMovementArea(AxisAlignedBB p_190610_1_, EnumFacing p_190610_2_, double p_190610_3_) {

		double d0 = p_190610_3_ * (double) p_190610_2_.getAxisDirection().getOffset();
		double d1 = Math.min(d0, 0.0D);
		double d2 = Math.max(d0, 0.0D);

		return switch (p_190610_2_) {
			case WEST ->
					new AxisAlignedBB(p_190610_1_.minX + d1, p_190610_1_.minY, p_190610_1_.minZ, p_190610_1_.minX + d2, p_190610_1_.maxY, p_190610_1_.maxZ);
			case EAST ->
					new AxisAlignedBB(p_190610_1_.maxX + d1, p_190610_1_.minY, p_190610_1_.minZ, p_190610_1_.maxX + d2, p_190610_1_.maxY, p_190610_1_.maxZ);
			case DOWN ->
					new AxisAlignedBB(p_190610_1_.minX, p_190610_1_.minY + d1, p_190610_1_.minZ, p_190610_1_.maxX, p_190610_1_.minY + d2, p_190610_1_.maxZ);
			case NORTH ->
					new AxisAlignedBB(p_190610_1_.minX, p_190610_1_.minY, p_190610_1_.minZ + d1, p_190610_1_.maxX, p_190610_1_.maxY, p_190610_1_.minZ + d2);
			case SOUTH ->
					new AxisAlignedBB(p_190610_1_.minX, p_190610_1_.minY, p_190610_1_.maxZ + d1, p_190610_1_.maxX, p_190610_1_.maxY, p_190610_1_.maxZ + d2);
			default ->
					new AxisAlignedBB(p_190610_1_.minX, p_190610_1_.maxY + d1, p_190610_1_.minZ, p_190610_1_.maxX, p_190610_1_.maxY + d2, p_190610_1_.maxZ);
		};
	}

	private void fixEntityWithinPistonBase(Entity p_190605_1_, EnumFacing p_190605_2_, double p_190605_3_) {

		AxisAlignedBB axisalignedbb = p_190605_1_.getEntityBoundingBox();
		AxisAlignedBB axisalignedbb1 = Block.FULL_BLOCK_AABB.offset(pos);

		if (axisalignedbb.intersects(axisalignedbb1)) {
			EnumFacing enumfacing = p_190605_2_.getOpposite();
			double d0 = getMovement(axisalignedbb1, enumfacing, axisalignedbb) + 0.01D;
			double d1 = getMovement(axisalignedbb1, enumfacing, axisalignedbb.intersect(axisalignedbb1)) + 0.01D;

			if (Math.abs(d0 - d1) < 0.01D) {
				d0 = Math.min(d0, p_190605_3_) + 0.01D;
				MOVING_ENTITY.set(p_190605_2_);
				p_190605_1_.move(MoverType.PISTON, d0 * (double) enumfacing.getFrontOffsetX(), d0 * (double) enumfacing.getFrontOffsetY(), d0 * (double) enumfacing.getFrontOffsetZ());
				MOVING_ENTITY.remove();
			}
		}
	}

	/**
	 * removes a piston's tile entity (and if the piston is moving, stops it)
	 */
	public void clearPistonTileEntity() {

		if (lastProgress < 1.0F && world != null) {
			progress = 1.0F;
			lastProgress = progress;
			world.removeTileEntity(pos);
			invalidate();

			if (world.getBlockState(pos).getBlock() == Blocks.PISTON_EXTENSION) {
				world.setBlockState(pos, pistonState, 3);
				world.neighborChanged(pos, pistonState.getBlock(), pos);
			}
		}
	}

	/**
	 * Like the old updateEntity(), except more generic.
	 */
	public void update() {

		lastProgress = progress;

		if (lastProgress >= 1.0F) {
			world.removeTileEntity(pos);
			invalidate();

			if (world.getBlockState(pos).getBlock() == Blocks.PISTON_EXTENSION) {
				world.setBlockState(pos, pistonState, 3);
				world.neighborChanged(pos, pistonState.getBlock(), pos);
			}
		} else {
			float f = progress + 0.5F;
			moveCollidedEntities(f);
			progress = f;

			if (progress >= 1.0F) {
				progress = 1.0F;
			}
		}
	}

	public void readFromNBT(NBTTagCompound compound) {

		super.readFromNBT(compound);
		pistonState = Block.getBlockById(compound.getInteger("blockId")).getStateFromMeta(compound.getInteger("blockData"));
		pistonFacing = EnumFacing.getFront(compound.getInteger("facing"));
		progress = compound.getFloat("progress");
		lastProgress = progress;
		extending = compound.getBoolean("extending");
		shouldHeadBeRendered = compound.getBoolean("source");
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {

		super.writeToNBT(compound);
		compound.setInteger("blockId", Block.getIdFromBlock(pistonState.getBlock()));
		compound.setInteger("blockData", pistonState.getBlock().getMetaFromState(pistonState));
		compound.setInteger("facing", pistonFacing.getIndex());
		compound.setFloat("progress", lastProgress);
		compound.setBoolean("extending", extending);
		compound.setBoolean("source", shouldHeadBeRendered);
		return compound;
	}

	public void addCollissionAABBs(World p_190609_1_, BlockPos p_190609_2_, AxisAlignedBB p_190609_3_, List<AxisAlignedBB> p_190609_4_, Entity p_190609_5_) {

		if (!extending && shouldHeadBeRendered) {
			pistonState.withProperty(BlockPistonBase.EXTENDED, true).addCollisionBoxToList(p_190609_1_, p_190609_2_, p_190609_3_, p_190609_4_, p_190609_5_, false);
		}

		EnumFacing enumfacing = MOVING_ENTITY.get();

		if ((double) progress >= 1.0D || enumfacing != (extending ? pistonFacing : pistonFacing.getOpposite())) {
			int i = p_190609_4_.size();
			IBlockState iblockstate;

			if (shouldPistonHeadBeRendered()) {
				iblockstate = Blocks.PISTON_HEAD.getDefaultState().withProperty(BlockPistonExtension.FACING, pistonFacing).withProperty(BlockPistonExtension.SHORT, extending != 1.0F - progress < 0.25F);
			} else {
				iblockstate = pistonState;
			}

			float f = getExtendedProgress(progress);
			double d0 = (float) pistonFacing.getFrontOffsetX() * f;
			double d1 = (float) pistonFacing.getFrontOffsetY() * f;
			double d2 = (float) pistonFacing.getFrontOffsetZ() * f;
			iblockstate.addCollisionBoxToList(p_190609_1_, p_190609_2_, p_190609_3_.offset(-d0, -d1, -d2), p_190609_4_, p_190609_5_, true);

			for (int j = i; j < p_190609_4_.size(); ++j) {
				p_190609_4_.set(j, p_190609_4_.get(j).offset(d0, d1, d2));
			}
		}
	}

}
