package net.minecraft.block;

import com.google.common.collect.Lists;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.List;
import java.util.Random;

public class BlockStairs extends Block {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyEnum<BlockStairs.EnumHalf> HALF = PropertyEnum.create("half", BlockStairs.EnumHalf.class);
	public static final PropertyEnum<BlockStairs.EnumShape> SHAPE = PropertyEnum.create("shape", BlockStairs.EnumShape.class);

	/**
	 * B: .. T: xx
	 * B: .. T: xx
	 */
	protected static final AxisAlignedBB AABB_SLAB_TOP = new AxisAlignedBB(0D, 0.5D, 0D, 1D, 1D, 1D);

	/**
	 * B: .. T: x.
	 * B: .. T: x.
	 */
	protected static final AxisAlignedBB AABB_QTR_TOP_WEST = new AxisAlignedBB(0D, 0.5D, 0D, 0.5D, 1D, 1D);

	/**
	 * B: .. T: .x
	 * B: .. T: .x
	 */
	protected static final AxisAlignedBB AABB_QTR_TOP_EAST = new AxisAlignedBB(0.5D, 0.5D, 0D, 1D, 1D, 1D);

	/**
	 * B: .. T: xx
	 * B: .. T: ..
	 */
	protected static final AxisAlignedBB AABB_QTR_TOP_NORTH = new AxisAlignedBB(0D, 0.5D, 0D, 1D, 1D, 0.5D);

	/**
	 * B: .. T: ..
	 * B: .. T: xx
	 */
	protected static final AxisAlignedBB AABB_QTR_TOP_SOUTH = new AxisAlignedBB(0D, 0.5D, 0.5D, 1D, 1D, 1D);

	/**
	 * B: .. T: x.
	 * B: .. T: ..
	 */
	protected static final AxisAlignedBB AABB_OCT_TOP_NW = new AxisAlignedBB(0D, 0.5D, 0D, 0.5D, 1D, 0.5D);

	/**
	 * B: .. T: .x
	 * B: .. T: ..
	 */
	protected static final AxisAlignedBB AABB_OCT_TOP_NE = new AxisAlignedBB(0.5D, 0.5D, 0D, 1D, 1D, 0.5D);

	/**
	 * B: .. T: ..
	 * B: .. T: x.
	 */
	protected static final AxisAlignedBB AABB_OCT_TOP_SW = new AxisAlignedBB(0D, 0.5D, 0.5D, 0.5D, 1D, 1D);

	/**
	 * B: .. T: ..
	 * B: .. T: .x
	 */
	protected static final AxisAlignedBB AABB_OCT_TOP_SE = new AxisAlignedBB(0.5D, 0.5D, 0.5D, 1D, 1D, 1D);

	/**
	 * B: xx T: ..
	 * B: xx T: ..
	 */
	protected static final AxisAlignedBB AABB_SLAB_BOTTOM = new AxisAlignedBB(0D, 0D, 0D, 1D, 0.5D, 1D);

	/**
	 * B: x. T: ..
	 * B: x. T: ..
	 */
	protected static final AxisAlignedBB AABB_QTR_BOT_WEST = new AxisAlignedBB(0D, 0D, 0D, 0.5D, 0.5D, 1D);

	/**
	 * B: .x T: ..
	 * B: .x T: ..
	 */
	protected static final AxisAlignedBB AABB_QTR_BOT_EAST = new AxisAlignedBB(0.5D, 0D, 0D, 1D, 0.5D, 1D);

	/**
	 * B: xx T: ..
	 * B: .. T: ..
	 */
	protected static final AxisAlignedBB AABB_QTR_BOT_NORTH = new AxisAlignedBB(0D, 0D, 0D, 1D, 0.5D, 0.5D);

	/**
	 * B: .. T: ..
	 * B: xx T: ..
	 */
	protected static final AxisAlignedBB AABB_QTR_BOT_SOUTH = new AxisAlignedBB(0D, 0D, 0.5D, 1D, 0.5D, 1D);

	/**
	 * B: x. T: ..
	 * B: .. T: ..
	 */
	protected static final AxisAlignedBB AABB_OCT_BOT_NW = new AxisAlignedBB(0D, 0D, 0D, 0.5D, 0.5D, 0.5D);

	/**
	 * B: .x T: ..
	 * B: .. T: ..
	 */
	protected static final AxisAlignedBB AABB_OCT_BOT_NE = new AxisAlignedBB(0.5D, 0D, 0D, 1D, 0.5D, 0.5D);

	/**
	 * B: .. T: ..
	 * B: x. T: ..
	 */
	protected static final AxisAlignedBB AABB_OCT_BOT_SW = new AxisAlignedBB(0D, 0D, 0.5D, 0.5D, 0.5D, 1D);

	/**
	 * B: .. T: ..
	 * B: .x T: ..
	 */
	protected static final AxisAlignedBB AABB_OCT_BOT_SE = new AxisAlignedBB(0.5D, 0D, 0.5D, 1D, 0.5D, 1D);
	private final Block modelBlock;
	private final IBlockState modelState;

	protected BlockStairs(IBlockState modelState) {

		super(modelState.getBlock().blockMaterial);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(HALF, BlockStairs.EnumHalf.BOTTOM).withProperty(SHAPE, BlockStairs.EnumShape.STRAIGHT));
		modelBlock = modelState.getBlock();
		this.modelState = modelState;
		setHardness(modelBlock.blockHardness);
		setResistance(modelBlock.blockResistance / 3F);
		setSoundType(modelBlock.blockSoundType);
		setLightOpacity(255);
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
	}

	private static List<AxisAlignedBB> getCollisionBoxList(IBlockState bstate) {

		List<AxisAlignedBB> list = Lists.newArrayList();
		boolean flag = bstate.getValue(HALF) == BlockStairs.EnumHalf.TOP;
		list.add(flag ? AABB_SLAB_TOP : AABB_SLAB_BOTTOM);
		BlockStairs.EnumShape blockstairs$enumshape = bstate.getValue(SHAPE);

		if (blockstairs$enumshape == BlockStairs.EnumShape.STRAIGHT || blockstairs$enumshape == BlockStairs.EnumShape.INNER_LEFT || blockstairs$enumshape == BlockStairs.EnumShape.INNER_RIGHT) {
			list.add(getCollQuarterBlock(bstate));
		}

		if (blockstairs$enumshape != BlockStairs.EnumShape.STRAIGHT) {
			list.add(getCollEighthBlock(bstate));
		}

		return list;
	}

	/**
	 * Returns a bounding box representing a quarter of a block (two eight-size cubes back to back).
	 * Used in all stair shapes except OUTER.
	 */
	private static AxisAlignedBB getCollQuarterBlock(IBlockState bstate) {

		boolean flag = bstate.getValue(HALF) == BlockStairs.EnumHalf.TOP;

		return switch (bstate.getValue(FACING)) {
			case SOUTH -> flag ? AABB_QTR_BOT_SOUTH : AABB_QTR_TOP_SOUTH;
			case WEST -> flag ? AABB_QTR_BOT_WEST : AABB_QTR_TOP_WEST;
			case EAST -> flag ? AABB_QTR_BOT_EAST : AABB_QTR_TOP_EAST;
			default -> flag ? AABB_QTR_BOT_NORTH : AABB_QTR_TOP_NORTH;
		};
	}

	/**
	 * Returns a bounding box representing an eighth of a block (a block whose three dimensions are halved).
	 * Used in all stair shapes except STRAIGHT (gets added alone in the case of OUTER; alone with a quarter block in
	 * case of INSIDE).
	 */
	private static AxisAlignedBB getCollEighthBlock(IBlockState bstate) {

		EnumFacing enumfacing = bstate.getValue(FACING);
		EnumFacing enumfacing1 = switch (bstate.getValue(SHAPE)) {
			case OUTER_RIGHT -> enumfacing.rotateY();
			case INNER_RIGHT -> enumfacing.getOpposite();
			case INNER_LEFT -> enumfacing.rotateYCCW();
			default -> enumfacing;
		};

		boolean flag = bstate.getValue(HALF) == BlockStairs.EnumHalf.TOP;

		return switch (enumfacing1) {
			case SOUTH -> flag ? AABB_OCT_BOT_SE : AABB_OCT_TOP_SE;
			case WEST -> flag ? AABB_OCT_BOT_SW : AABB_OCT_TOP_SW;
			case EAST -> flag ? AABB_OCT_BOT_NE : AABB_OCT_TOP_NE;
			default -> flag ? AABB_OCT_BOT_NW : AABB_OCT_TOP_NW;
		};
	}

	private static BlockStairs.EnumShape getStairsShape(IBlockState p_185706_0_, IBlockAccess p_185706_1_, BlockPos p_185706_2_) {

		EnumFacing enumfacing = p_185706_0_.getValue(FACING);
		IBlockState iblockstate = p_185706_1_.getBlockState(p_185706_2_.offset(enumfacing));

		if (isBlockStairs(iblockstate) && p_185706_0_.getValue(HALF) == iblockstate.getValue(HALF)) {
			EnumFacing enumfacing1 = iblockstate.getValue(FACING);

			if (enumfacing1.getAxis() != p_185706_0_.getValue(FACING).getAxis() && isDifferentStairs(p_185706_0_, p_185706_1_, p_185706_2_, enumfacing1.getOpposite())) {
				if (enumfacing1 == enumfacing.rotateYCCW()) {
					return BlockStairs.EnumShape.OUTER_LEFT;
				}

				return BlockStairs.EnumShape.OUTER_RIGHT;
			}
		}

		IBlockState iblockstate1 = p_185706_1_.getBlockState(p_185706_2_.offset(enumfacing.getOpposite()));

		if (isBlockStairs(iblockstate1) && p_185706_0_.getValue(HALF) == iblockstate1.getValue(HALF)) {
			EnumFacing enumfacing2 = iblockstate1.getValue(FACING);

			if (enumfacing2.getAxis() != p_185706_0_.getValue(FACING).getAxis() && isDifferentStairs(p_185706_0_, p_185706_1_, p_185706_2_, enumfacing2)) {
				if (enumfacing2 == enumfacing.rotateYCCW()) {
					return BlockStairs.EnumShape.INNER_LEFT;
				}

				return BlockStairs.EnumShape.INNER_RIGHT;
			}
		}

		return BlockStairs.EnumShape.STRAIGHT;
	}

	private static boolean isDifferentStairs(IBlockState p_185704_0_, IBlockAccess p_185704_1_, BlockPos p_185704_2_, EnumFacing p_185704_3_) {

		IBlockState iblockstate = p_185704_1_.getBlockState(p_185704_2_.offset(p_185704_3_));
		return !isBlockStairs(iblockstate) || iblockstate.getValue(FACING) != p_185704_0_.getValue(FACING) || iblockstate.getValue(HALF) != p_185704_0_.getValue(HALF);
	}

	public static boolean isBlockStairs(IBlockState state) {

		return state.getBlock() instanceof BlockStairs;
	}

	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState) {

		if (!isActualState) {
			state = getActualState(state, worldIn, pos);
		}

		for (AxisAlignedBB axisalignedbb : getCollisionBoxList(state)) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, axisalignedbb);
		}
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
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {

		state = getActualState(state, worldIn, pos);

		if (face.getAxis() == EnumFacing.Axis.Y) {
			return face == EnumFacing.UP == (state.getValue(HALF) == BlockStairs.EnumHalf.TOP) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
		} else {
			BlockStairs.EnumShape blockstairs$enumshape = state.getValue(SHAPE);

			if (blockstairs$enumshape != BlockStairs.EnumShape.OUTER_LEFT && blockstairs$enumshape != BlockStairs.EnumShape.OUTER_RIGHT) {
				EnumFacing enumfacing = state.getValue(FACING);

				return switch (blockstairs$enumshape) {
					case INNER_RIGHT ->
							enumfacing != face && enumfacing != face.rotateYCCW() ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
					case INNER_LEFT ->
							enumfacing != face && enumfacing != face.rotateY() ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
					case STRAIGHT -> enumfacing == face ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
					default -> BlockFaceShape.UNDEFINED;
				};
			} else {
				return BlockFaceShape.UNDEFINED;
			}
		}
	}

	/**
	 * Used to determine ambient occlusion and culling when rebuilding chunks for render
	 */
	public boolean isOpaqueCube(IBlockState state) {

		return false;
	}

	public boolean isFullCube(IBlockState state) {

		return false;
	}

	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {

		modelBlock.randomDisplayTick(stateIn, worldIn, pos, rand);
	}

	public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {

		modelBlock.onBlockClicked(worldIn, pos, playerIn);
	}

	/**
	 * Called after a player destroys this Block - the posiiton pos may no longer hold the state indicated.
	 */
	public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {

		modelBlock.onBlockDestroyedByPlayer(worldIn, pos, state);
	}

	public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos) {

		return modelState.getPackedLightmapCoords(source, pos);
	}

	/**
	 * Returns how much this block can resist explosions from the passed in entity.
	 */
	public float getExplosionResistance(Entity exploder) {

		return modelBlock.getExplosionResistance(exploder);
	}

	/**
	 * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
	 * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
	 */
	public BlockRenderLayer getBlockLayer() {

		return modelBlock.getBlockLayer();
	}

	/**
	 * How many world ticks before ticking
	 */
	public int tickRate(World worldIn) {

		return modelBlock.tickRate(worldIn);
	}

	/**
	 * Return an AABB (in world coords!) that should be highlighted when the player is targeting this Block
	 */
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {

		return modelState.getSelectedBoundingBox(worldIn, pos);
	}

	public Vec3d modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion) {

		return modelBlock.modifyAcceleration(worldIn, pos, entityIn, motion);
	}

	/**
	 * Returns if this block is collidable. Only used by fire, although stairs return that of the block that the stair
	 * is made of (though nobody's going to make fire stairs, right?)
	 */
	public boolean isCollidable() {

		return modelBlock.isCollidable();
	}

	public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {

		return modelBlock.canCollideCheck(state, hitIfLiquid);
	}

	/**
	 * Checks if this block can be placed exactly at the given position.
	 */
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {

		return modelBlock.canPlaceBlockAt(worldIn, pos);
	}

	/**
	 * Called after the block is set in the Chunk data, but before the Tile Entity is set
	 */
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {

		modelState.neighborChanged(worldIn, pos, Blocks.AIR, pos);
		modelBlock.onBlockAdded(worldIn, pos, modelState);
	}

	/**
	 * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
	 */
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {

		modelBlock.breakBlock(worldIn, pos, modelState);
	}

	/**
	 * Called when the given entity walks on this Block
	 */
	public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {

		modelBlock.onEntityWalk(worldIn, pos, entityIn);
	}

	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {

		modelBlock.updateTick(worldIn, pos, state, rand);
	}

	/**
	 * Called when the block is right clicked by a player.
	 */
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		return modelBlock.onBlockActivated(worldIn, pos, modelState, playerIn, hand, EnumFacing.DOWN, 0F, 0F, 0F);
	}

	/**
	 * Called when this Block is destroyed by an Explosion
	 */
	public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn) {

		modelBlock.onBlockDestroyedByExplosion(worldIn, pos, explosionIn);
	}

	/**
	 * Determines if the block is solid enough on the top side to support other blocks, like redstone components.
	 */
	public boolean isTopSolid(IBlockState state) {

		return state.getValue(HALF) == BlockStairs.EnumHalf.TOP;
	}

	/**
	 * Get the MapColor for this Block and the given BlockState
	 */
	public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {

		return modelBlock.getMapColor(modelState, worldIn, pos);
	}

	/**
	 * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
	 * IBlockstate
	 */
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {

		IBlockState iblockstate = super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
		iblockstate = iblockstate.withProperty(FACING, placer.getHorizontalFacing()).withProperty(SHAPE, BlockStairs.EnumShape.STRAIGHT);
		return facing != EnumFacing.DOWN && (facing == EnumFacing.UP || (double) hitY <= 0.5D) ? iblockstate.withProperty(HALF, BlockStairs.EnumHalf.BOTTOM) : iblockstate.withProperty(HALF, BlockStairs.EnumHalf.TOP);
	}

	

	/**
	 * Ray traces through the blocks collision from start vector to end vector returning a ray trace hit.
	 */
	public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {

		List<RayTraceResult> list = Lists.newArrayList();

		for (AxisAlignedBB axisalignedbb : getCollisionBoxList(getActualState(blockState, worldIn, pos))) {
			list.add(rayTrace(pos, start, end, axisalignedbb));
		}

		RayTraceResult raytraceresult1 = null;
		double d1 = 0D;

		for (RayTraceResult raytraceresult : list) {
			if (raytraceresult != null) {
				double d0 = raytraceresult.hitVec.squareDistanceTo(end);

				if (d0 > d1) {
					raytraceresult1 = raytraceresult;
					d1 = d0;
				}
			}
		}

		return raytraceresult1;
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		IBlockState iblockstate = getDefaultState().withProperty(HALF, (meta & 4) > 0 ? BlockStairs.EnumHalf.TOP : BlockStairs.EnumHalf.BOTTOM);
		iblockstate = iblockstate.withProperty(FACING, EnumFacing.getFront(5 - (meta & 3)));
		return iblockstate;
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		int i = 0;

		if (state.getValue(HALF) == BlockStairs.EnumHalf.TOP) {
			i |= 4;
		}

		i = i | 5 - state.getValue(FACING).getIndex();
		return i;
	}

	/**
	 * Get the actual Block state of this Block at the given position. This applies properties not visible in the
	 * metadata, such as fence connections.
	 */
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {

		return state.withProperty(SHAPE, getStairsShape(state, worldIn, pos));
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withRotation(IBlockState state, Rotation rot) {

		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@SuppressWarnings("incomplete-switch")

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {

		EnumFacing enumfacing = state.getValue(FACING);
		BlockStairs.EnumShape blockstairs$enumshape = state.getValue(SHAPE);

		switch (mirrorIn) {
			case LEFT_RIGHT:
				if (enumfacing.getAxis() == EnumFacing.Axis.Z) {
					return switch (blockstairs$enumshape) {
						case OUTER_LEFT ->
								state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, EnumShape.OUTER_RIGHT);
						case OUTER_RIGHT ->
								state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, EnumShape.OUTER_LEFT);
						case INNER_RIGHT ->
								state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, EnumShape.INNER_LEFT);
						case INNER_LEFT ->
								state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, EnumShape.INNER_RIGHT);
						default -> state.withRotation(Rotation.CLOCKWISE_180);
					};
				}

				break;

			case FRONT_BACK:
				if (enumfacing.getAxis() == EnumFacing.Axis.X) {
					return switch (blockstairs$enumshape) {
						case OUTER_LEFT ->
								state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, EnumShape.OUTER_RIGHT);
						case OUTER_RIGHT ->
								state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, EnumShape.OUTER_LEFT);
						case INNER_RIGHT ->
								state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, EnumShape.INNER_RIGHT);
						case INNER_LEFT ->
								state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, EnumShape.INNER_LEFT);
						case STRAIGHT -> state.withRotation(Rotation.CLOCKWISE_180);
					};
				}
		}

		return super.withMirror(state, mirrorIn);
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, FACING, HALF, SHAPE);
	}

	public enum EnumHalf implements IStringSerializable {
		TOP("top"),
		BOTTOM("bottom");

		private final String name;

		EnumHalf(String name) {

			this.name = name;
		}

		public String toString() {

			return name;
		}

		public String getName() {

			return name;
		}
	}

	public enum EnumShape implements IStringSerializable {
		STRAIGHT("straight"),
		INNER_LEFT("inner_left"),
		INNER_RIGHT("inner_right"),
		OUTER_LEFT("outer_left"),
		OUTER_RIGHT("outer_right");

		private final String name;

		EnumShape(String name) {

			this.name = name;
		}

		public String toString() {

			return name;
		}

		public String getName() {

			return name;
		}
	}

}
