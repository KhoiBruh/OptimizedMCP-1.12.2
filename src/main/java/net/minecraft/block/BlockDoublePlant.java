package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.Random;

public class BlockDoublePlant extends BlockBush implements IGrowable {

	public static final PropertyEnum<BlockDoublePlant.PlantType> VARIANT = PropertyEnum.create("variant", BlockDoublePlant.PlantType.class);
	public static final PropertyEnum<BlockDoublePlant.BlockHalf> HALF = PropertyEnum.create("half", BlockDoublePlant.BlockHalf.class);
	public static final PropertyEnum<Facing> FACING = BlockHorizontal.FACING;

	public BlockDoublePlant() {

		super(Material.VINE);
		setDefaultState(blockState.getBaseState().withProperty(VARIANT, BlockDoublePlant.PlantType.SUNFLOWER).withProperty(HALF, BlockDoublePlant.BlockHalf.LOWER).withProperty(FACING, Facing.NORTH));
		setHardness(0F);
		setSoundType(SoundType.PLANT);
		setUnlocalizedName("doublePlant");
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {

		return FULL_BLOCK_AABB;
	}

	private BlockDoublePlant.PlantType getType(IBlockAccess blockAccess, BlockPos pos, IBlockState state) {

		if (state.getBlock() == this) {
			state = state.getActualState(blockAccess, pos);
			return state.getValue(VARIANT);
		} else {
			return BlockDoublePlant.PlantType.FERN;
		}
	}

	/**
	 * Checks if this block can be placed exactly at the given position.
	 */
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {

		return super.canPlaceBlockAt(worldIn, pos) && worldIn.isAirBlock(pos.up());
	}

	/**
	 * Whether this Block can be replaced directly by other blocks (true for e.g. tall grass)
	 */
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {

		IBlockState iblockstate = worldIn.getBlockState(pos);

		if (iblockstate.getBlock() != this) {
			return true;
		} else {
			BlockDoublePlant.PlantType blockdoubleplant$enumplanttype = iblockstate.getActualState(worldIn, pos).getValue(VARIANT);
			return blockdoubleplant$enumplanttype == BlockDoublePlant.PlantType.FERN || blockdoubleplant$enumplanttype == BlockDoublePlant.PlantType.GRASS;
		}
	}

	protected void checkAndDropBlock(World worldIn, BlockPos pos, IBlockState state) {

		if (!canBlockStay(worldIn, pos, state)) {
			boolean flag = state.getValue(HALF) == BlockDoublePlant.BlockHalf.UPPER;
			BlockPos blockpos = flag ? pos : pos.up();
			BlockPos blockpos1 = flag ? pos.down() : pos;
			Block block = flag ? this : worldIn.getBlockState(blockpos).getBlock();
			Block block1 = flag ? worldIn.getBlockState(blockpos1).getBlock() : this;

			if (block == this) {
				worldIn.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 2);
			}

			if (block1 == this) {
				worldIn.setBlockState(blockpos1, Blocks.AIR.getDefaultState(), 3);

				if (!flag) {
					dropBlockAsItem(worldIn, blockpos1, state, 0);
				}
			}
		}
	}

	public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {

		if (state.getValue(HALF) == BlockDoublePlant.BlockHalf.UPPER) {
			return worldIn.getBlockState(pos.down()).getBlock() == this;
		} else {
			IBlockState iblockstate = worldIn.getBlockState(pos.up());
			return iblockstate.getBlock() == this && super.canBlockStay(worldIn, pos, iblockstate);
		}
	}

	/**
	 * Get the Item that this Block should drop when harvested.
	 */
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {

		if (state.getValue(HALF) == BlockDoublePlant.BlockHalf.UPPER) {
			return Items.AIR;
		} else {
			BlockDoublePlant.PlantType blockdoubleplant$enumplanttype = state.getValue(VARIANT);

			if (blockdoubleplant$enumplanttype == BlockDoublePlant.PlantType.FERN) {
				return Items.AIR;
			} else if (blockdoubleplant$enumplanttype == BlockDoublePlant.PlantType.GRASS) {
				return rand.nextInt(8) == 0 ? Items.WHEAT_SEEDS : Items.AIR;
			} else {
				return super.getItemDropped(state, rand, fortune);
			}
		}
	}

	/**
	 * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
	 * returns the metadata of the dropped item based on the old metadata of the block.
	 */
	public int damageDropped(IBlockState state) {

		return state.getValue(HALF) != BlockDoublePlant.BlockHalf.UPPER && state.getValue(VARIANT) != BlockDoublePlant.PlantType.GRASS ? state.getValue(VARIANT).getMeta() : 0;
	}

	public void placeAt(World worldIn, BlockPos lowerPos, BlockDoublePlant.PlantType variant, int flags) {

		worldIn.setBlockState(lowerPos, getDefaultState().withProperty(HALF, BlockDoublePlant.BlockHalf.LOWER).withProperty(VARIANT, variant), flags);
		worldIn.setBlockState(lowerPos.up(), getDefaultState().withProperty(HALF, BlockDoublePlant.BlockHalf.UPPER), flags);
	}

	/**
	 * Called by ItemBlocks after a block is set in the world, to allow post-place logic
	 */
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {

		worldIn.setBlockState(pos.up(), getDefaultState().withProperty(HALF, BlockDoublePlant.BlockHalf.UPPER), 2);
	}

	/**
	 * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
	 * Block.removedByPlayer
	 */
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {

		if (worldIn.isRemote || stack.getItem() != Items.SHEARS || state.getValue(HALF) != BlockDoublePlant.BlockHalf.LOWER || !onHarvest(worldIn, pos, state, player)) {
			super.harvestBlock(worldIn, player, pos, state, te, stack);
		}
	}

	/**
	 * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually
	 * collect this block
	 */
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {

		if (state.getValue(HALF) == BlockDoublePlant.BlockHalf.UPPER) {
			if (worldIn.getBlockState(pos.down()).getBlock() == this) {
				if (player.capabilities.isCreativeMode) {
					worldIn.setBlockToAir(pos.down());
				} else {
					IBlockState iblockstate = worldIn.getBlockState(pos.down());
					BlockDoublePlant.PlantType blockdoubleplant$enumplanttype = iblockstate.getValue(VARIANT);

					if (blockdoubleplant$enumplanttype != BlockDoublePlant.PlantType.FERN && blockdoubleplant$enumplanttype != BlockDoublePlant.PlantType.GRASS) {
						worldIn.destroyBlock(pos.down(), true);
					} else if (worldIn.isRemote) {
						worldIn.setBlockToAir(pos.down());
					} else if (!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() == Items.SHEARS) {
						onHarvest(worldIn, pos, iblockstate, player);
						worldIn.setBlockToAir(pos.down());
					} else {
						worldIn.destroyBlock(pos.down(), true);
					}
				}
			}
		} else if (worldIn.getBlockState(pos.up()).getBlock() == this) {
			worldIn.setBlockState(pos.up(), Blocks.AIR.getDefaultState(), 2);
		}

		super.onBlockHarvested(worldIn, pos, state, player);
	}

	private boolean onHarvest(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {

		BlockDoublePlant.PlantType blockdoubleplant$enumplanttype = state.getValue(VARIANT);

		if (blockdoubleplant$enumplanttype != BlockDoublePlant.PlantType.FERN && blockdoubleplant$enumplanttype != BlockDoublePlant.PlantType.GRASS) {
			return false;
		} else {
			player.addStat(StatList.getBlockStats(this));
			int i = (blockdoubleplant$enumplanttype == BlockDoublePlant.PlantType.GRASS ? BlockTallGrass.Type.GRASS : BlockTallGrass.Type.FERN).getMeta();
			spawnAsEntity(worldIn, pos, new ItemStack(Blocks.TALLGRASS, 2, i));
			return true;
		}
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {

		for (BlockDoublePlant.PlantType blockdoubleplant$enumplanttype : BlockDoublePlant.PlantType.values()) {
			items.add(new ItemStack(this, 1, blockdoubleplant$enumplanttype.getMeta()));
		}
	}

	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {

		return new ItemStack(this, 1, getType(worldIn, pos, state).getMeta());
	}

	/**
	 * Whether this IGrowable can grow
	 */
	public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {

		BlockDoublePlant.PlantType blockdoubleplant$enumplanttype = getType(worldIn, pos, state);
		return blockdoubleplant$enumplanttype != BlockDoublePlant.PlantType.GRASS && blockdoubleplant$enumplanttype != BlockDoublePlant.PlantType.FERN;
	}

	public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {

		return true;
	}

	public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {

		spawnAsEntity(worldIn, pos, new ItemStack(this, 1, getType(worldIn, pos, state).getMeta()));
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return (meta & 8) > 0 ? getDefaultState().withProperty(HALF, BlockDoublePlant.BlockHalf.UPPER) : getDefaultState().withProperty(HALF, BlockDoublePlant.BlockHalf.LOWER).withProperty(VARIANT, BlockDoublePlant.PlantType.byMetadata(meta & 7));
	}

	/**
	 * Get the actual Block state of this Block at the given position. This applies properties not visible in the
	 * metadata, such as fence connections.
	 */
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {

		if (state.getValue(HALF) == BlockDoublePlant.BlockHalf.UPPER) {
			IBlockState iblockstate = worldIn.getBlockState(pos.down());

			if (iblockstate.getBlock() == this) {
				state = state.withProperty(VARIANT, iblockstate.getValue(VARIANT));
			}
		}

		return state;
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		return state.getValue(HALF) == BlockDoublePlant.BlockHalf.UPPER ? 8 | state.getValue(FACING).getHorizontalIndex() : state.getValue(VARIANT).getMeta();
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, HALF, VARIANT, FACING);
	}

	/**
	 * Get the OffsetType for this Block. Determines if the model is rendered slightly offset.
	 */
	public Block.OffsetType getOffsetType() {

		return Block.OffsetType.XZ;
	}

	public enum BlockHalf implements IStringSerializable {
		UPPER,
		LOWER;

		public String toString() {

			return getName();
		}

		public String getName() {

			return this == UPPER ? "upper" : "lower";
		}
	}

	public enum PlantType implements IStringSerializable {
		SUNFLOWER(0, "sunflower"),
		SYRINGA(1, "syringa"),
		GRASS(2, "double_grass", "grass"),
		FERN(3, "double_fern", "fern"),
		ROSE(4, "double_rose", "rose"),
		PAEONIA(5, "paeonia");

		private static final BlockDoublePlant.PlantType[] META_LOOKUP = new BlockDoublePlant.PlantType[values().length];

		static {
			for (BlockDoublePlant.PlantType blockdoubleplant$enumplanttype : values()) {
				META_LOOKUP[blockdoubleplant$enumplanttype.getMeta()] = blockdoubleplant$enumplanttype;
			}
		}

		private final int meta;
		private final String name;
		private final String unlocalizedName;

		PlantType(int meta, String name) {

			this(meta, name, name);
		}

		PlantType(int meta, String name, String unlocalizedName) {

			this.meta = meta;
			this.name = name;
			this.unlocalizedName = unlocalizedName;
		}

		public static BlockDoublePlant.PlantType byMetadata(int meta) {

			if (meta < 0 || meta >= META_LOOKUP.length) {
				meta = 0;
			}

			return META_LOOKUP[meta];
		}

		public int getMeta() {

			return meta;
		}

		public String toString() {

			return name;
		}

		public String getName() {

			return name;
		}

		public String getUnlocalizedName() {

			return unlocalizedName;
		}
	}

}
