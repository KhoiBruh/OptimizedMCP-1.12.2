package net.minecraft.block;

import com.google.common.base.Predicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMaterialMatcher;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.block.state.pattern.FactoryBlockPattern;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockSkull extends BlockContainer {

	public static final PropertyDirection FACING = BlockDirectional.FACING;
	public static final PropertyBool NODROP = PropertyBool.create("nodrop");
	protected static final AxisAlignedBB DEFAULT_AABB = new AxisAlignedBB(0.25D, 0D, 0.25D, 0.75D, 0.5D, 0.75D);
	protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.25D, 0.25D, 0.5D, 0.75D, 0.75D, 1D);
	protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.25D, 0.25D, 0D, 0.75D, 0.75D, 0.5D);
	protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.5D, 0.25D, 0.25D, 1D, 0.75D, 0.75D);
	protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0D, 0.25D, 0.25D, 0.5D, 0.75D, 0.75D);
	private static final Predicate<BlockWorldState> IS_WITHER_SKELETON = p_apply_1_ -> p_apply_1_.getBlockState() != null && p_apply_1_.getBlockState().getBlock() == Blocks.SKULL && p_apply_1_.getTileEntity() instanceof TileEntitySkull && ((TileEntitySkull) p_apply_1_.getTileEntity()).getSkullType() == 1;
	private BlockPattern witherBasePattern;
	private BlockPattern witherPattern;

	protected BlockSkull() {

		super(Material.CIRCUITS);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(NODROP, false));
	}

	/**
	 * Gets the localized name of this block. Used for the statistics page.
	 */
	public String getLocalizedName() {

		return I18n.translateToLocal("tile.skull.skeleton.name");
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

	public boolean hasCustomBreakingProgress(IBlockState state) {

		return true;
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {

		return switch (state.getValue(FACING)) {
			case NORTH -> NORTH_AABB;
			case SOUTH -> SOUTH_AABB;
			case WEST -> WEST_AABB;
			case EAST -> EAST_AABB;
			default -> DEFAULT_AABB;
		};
	}

	/**
	 * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
	 * IBlockstate
	 */
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {

		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing()).withProperty(NODROP, false);
	}

	/**
	 * Returns a new instance of a block's tile entity class. Called on placing the block.
	 */
	public TileEntity createNewTileEntity(World worldIn, int meta) {

		return new TileEntitySkull();
	}

	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {

		int i = 0;
		TileEntity tileentity = worldIn.getTileEntity(pos);

		if (tileentity instanceof TileEntitySkull) {
			i = ((TileEntitySkull) tileentity).getSkullType();
		}

		return new ItemStack(Items.SKULL, 1, i);
	}

	/**
	 * Spawns this Block's drops into the World as EntityItems.
	 */
	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {

	}

	/**
	 * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually
	 * collect this block
	 */
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {

		if (player.capabilities.isCreativeMode) {
			state = state.withProperty(NODROP, true);
			worldIn.setBlockState(pos, state, 4);
		}

		super.onBlockHarvested(worldIn, pos, state, player);
	}

	/**
	 * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
	 */
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {

		if (!worldIn.isRemote) {
			if (!state.getValue(NODROP)) {
				TileEntity tileentity = worldIn.getTileEntity(pos);

				if (tileentity instanceof TileEntitySkull tileentityskull) {
					ItemStack itemstack = getItem(worldIn, pos, state);

					if (tileentityskull.getSkullType() == 3 && tileentityskull.getPlayerProfile() != null) {
						itemstack.setTagCompound(new NBTTagCompound());
						NBTTagCompound nbttagcompound = new NBTTagCompound();
						NBTUtil.writeGameProfile(nbttagcompound, tileentityskull.getPlayerProfile());
						itemstack.getTagCompound().setTag("SkullOwner", nbttagcompound);
					}

					spawnAsEntity(worldIn, pos, itemstack);
				}
			}

			super.breakBlock(worldIn, pos, state);
		}
	}

	/**
	 * Get the Item that this Block should drop when harvested.
	 */
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {

		return Items.SKULL;
	}

	public boolean canDispenserPlace(World worldIn, BlockPos pos, ItemStack stack) {

		if (stack.getMetadata() == 1 && pos.getY() >= 2 && worldIn.getDifficulty() != EnumDifficulty.PEACEFUL && !worldIn.isRemote) {
			return getWitherBasePattern().match(worldIn, pos) != null;
		} else {
			return false;
		}
	}

	public void checkWitherSpawn(World worldIn, BlockPos pos, TileEntitySkull te) {

		if (te.getSkullType() == 1 && pos.getY() >= 2 && worldIn.getDifficulty() != EnumDifficulty.PEACEFUL && !worldIn.isRemote) {
			BlockPattern blockpattern = getWitherPattern();
			BlockPattern.PatternHelper blockpattern$patternhelper = blockpattern.match(worldIn, pos);

			if (blockpattern$patternhelper != null) {
				for (int i = 0; i < 3; ++i) {
					BlockWorldState blockworldstate = blockpattern$patternhelper.translateOffset(i, 0, 0);
					worldIn.setBlockState(blockworldstate.getPos(), blockworldstate.getBlockState().withProperty(NODROP, true), 2);
				}

				for (int j = 0; j < blockpattern.getPalmLength(); ++j) {
					for (int k = 0; k < blockpattern.getThumbLength(); ++k) {
						BlockWorldState blockworldstate1 = blockpattern$patternhelper.translateOffset(j, k, 0);
						worldIn.setBlockState(blockworldstate1.getPos(), Blocks.AIR.getDefaultState(), 2);
					}
				}

				BlockPos blockpos = blockpattern$patternhelper.translateOffset(1, 0, 0).getPos();
				EntityWither entitywither = new EntityWither(worldIn);
				BlockPos blockpos1 = blockpattern$patternhelper.translateOffset(1, 2, 0).getPos();
				entitywither.setLocationAndAngles((double) blockpos1.getX() + 0.5D, (double) blockpos1.getY() + 0.55D, (double) blockpos1.getZ() + 0.5D, blockpattern$patternhelper.getForwards().getAxis() == EnumFacing.Axis.X ? 0F : 90F, 0F);
				entitywither.renderYawOffset = blockpattern$patternhelper.getForwards().getAxis() == EnumFacing.Axis.X ? 0F : 90F;
				entitywither.ignite();

				for (EntityPlayerMP entityplayermp : worldIn.getEntitiesWithinAABB(EntityPlayerMP.class, entitywither.getEntityBoundingBox().grow(50D))) {
					CriteriaTriggers.SUMMONED_ENTITY.trigger(entityplayermp, entitywither);
				}

				worldIn.spawnEntity(entitywither);

				for (int l = 0; l < 120; ++l) {
					worldIn.spawnParticle(EnumParticleTypes.SNOWBALL, (double) blockpos.getX() + worldIn.rand.nextDouble(), (double) (blockpos.getY() - 2) + worldIn.rand.nextDouble() * 3.9D, (double) blockpos.getZ() + worldIn.rand.nextDouble(), 0D, 0D, 0D);
				}

				for (int i1 = 0; i1 < blockpattern.getPalmLength(); ++i1) {
					for (int j1 = 0; j1 < blockpattern.getThumbLength(); ++j1) {
						BlockWorldState blockworldstate2 = blockpattern$patternhelper.translateOffset(i1, j1, 0);
						worldIn.notifyNeighborsRespectDebug(blockworldstate2.getPos(), Blocks.AIR, false);
					}
				}
			}
		}
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta & 7)).withProperty(NODROP, (meta & 8) > 0);
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		int i = 0;
		i = i | state.getValue(FACING).getIndex();

		if (state.getValue(NODROP)) {
			i |= 8;
		}

		return i;
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withRotation(IBlockState state, Rotation rot) {

		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {

		return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, FACING, NODROP);
	}

	protected BlockPattern getWitherBasePattern() {

		if (witherBasePattern == null) {
			witherBasePattern = FactoryBlockPattern.start().aisle("   ", "###", "~#~").where('#', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.SOUL_SAND))).where('~', BlockWorldState.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build();
		}

		return witherBasePattern;
	}

	protected BlockPattern getWitherPattern() {

		if (witherPattern == null) {
			witherPattern = FactoryBlockPattern.start().aisle("^^^", "###", "~#~").where('#', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.SOUL_SAND))).where('^', IS_WITHER_SKELETON).where('~', BlockWorldState.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build();
		}

		return witherPattern;
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

		return BlockFaceShape.UNDEFINED;
	}

}
