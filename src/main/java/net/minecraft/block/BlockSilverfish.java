package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockSilverfish extends Block {

	public static final PropertyEnum<BlockSilverfish.Type> VARIANT = PropertyEnum.create("variant", BlockSilverfish.Type.class);

	public BlockSilverfish() {

		super(Material.CLAY);
		setDefaultState(blockState.getBaseState().withProperty(VARIANT, BlockSilverfish.Type.STONE));
		setHardness(0F);
		setCreativeTab(CreativeTabs.DECORATIONS);
	}

	public static boolean canContainSilverfish(IBlockState blockState) {

		Block block = blockState.getBlock();
		return blockState == Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.Type.STONE) || block == Blocks.COBBLESTONE || block == Blocks.STONEBRICK;
	}

	/**
	 * Returns the quantity of items to drop on block destruction.
	 */
	public int quantityDropped(Random random) {

		return 0;
	}

	protected ItemStack getSilkTouchDrop(IBlockState state) {

		return switch (state.getValue(VARIANT)) {
			case COBBLESTONE -> new ItemStack(Blocks.COBBLESTONE);
			case STONEBRICK -> new ItemStack(Blocks.STONEBRICK);
			case MOSSY_STONEBRICK -> new ItemStack(Blocks.STONEBRICK, 1, BlockStoneBrick.Type.MOSSY.getMetadata());
			case CRACKED_STONEBRICK ->
					new ItemStack(Blocks.STONEBRICK, 1, BlockStoneBrick.Type.CRACKED.getMetadata());
			case CHISELED_STONEBRICK ->
					new ItemStack(Blocks.STONEBRICK, 1, BlockStoneBrick.Type.CHISELED.getMetadata());
			default -> new ItemStack(Blocks.STONE);
		};
	}

	/**
	 * Spawns this Block's drops into the World as EntityItems.
	 */
	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {

		if (!worldIn.isRemote && worldIn.getGameRules().getBoolean("doTileDrops")) {
			EntitySilverfish entitysilverfish = new EntitySilverfish(worldIn);
			entitysilverfish.setLocationAndAngles((double) pos.getX() + 0.5D, pos.getY(), (double) pos.getZ() + 0.5D, 0F, 0F);
			worldIn.spawnEntity(entitysilverfish);
			entitysilverfish.spawnExplosionParticle();
		}
	}

	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {

		return new ItemStack(this, 1, state.getBlock().getMetaFromState(state));
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {

		for (BlockSilverfish.Type blocksilverfish$enumtype : BlockSilverfish.Type.values()) {
			items.add(new ItemStack(this, 1, blocksilverfish$enumtype.getMetadata()));
		}
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(VARIANT, BlockSilverfish.Type.byMetadata(meta));
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {

		return state.getValue(VARIANT).getMetadata();
	}

	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, VARIANT);
	}

	public enum Type implements IStringSerializable {
		STONE(0, "stone") {
			public IBlockState getModelBlock() {

				return Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.Type.STONE);
			}
		},
		COBBLESTONE(1, "cobblestone", "cobble") {
			public IBlockState getModelBlock() {

				return Blocks.COBBLESTONE.getDefaultState();
			}
		},
		STONEBRICK(2, "stone_brick", "brick") {
			public IBlockState getModelBlock() {

				return Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.Type.DEFAULT);
			}
		},
		MOSSY_STONEBRICK(3, "mossy_brick", "mossybrick") {
			public IBlockState getModelBlock() {

				return Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.Type.MOSSY);
			}
		},
		CRACKED_STONEBRICK(4, "cracked_brick", "crackedbrick") {
			public IBlockState getModelBlock() {

				return Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.Type.CRACKED);
			}
		},
		CHISELED_STONEBRICK(5, "chiseled_brick", "chiseledbrick") {
			public IBlockState getModelBlock() {

				return Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.Type.CHISELED);
			}
		};

		private static final BlockSilverfish.Type[] META_LOOKUP = new BlockSilverfish.Type[values().length];

		static {
			for (BlockSilverfish.Type blocksilverfish$enumtype : values()) {
				META_LOOKUP[blocksilverfish$enumtype.getMetadata()] = blocksilverfish$enumtype;
			}
		}

		private final int meta;
		private final String name;
		private final String unlocalizedName;

		Type(int meta, String name) {

			this(meta, name, name);
		}

		Type(int meta, String name, String unlocalizedName) {

			this.meta = meta;
			this.name = name;
			this.unlocalizedName = unlocalizedName;
		}

		public static BlockSilverfish.Type byMetadata(int meta) {

			if (meta < 0 || meta >= META_LOOKUP.length) {
				meta = 0;
			}

			return META_LOOKUP[meta];
		}

		public static BlockSilverfish.Type forModelBlock(IBlockState model) {

			for (BlockSilverfish.Type blocksilverfish$enumtype : values()) {
				if (model == blocksilverfish$enumtype.getModelBlock()) {
					return blocksilverfish$enumtype;
				}
			}

			return STONE;
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

		public String getUnlocalizedName() {

			return unlocalizedName;
		}

		public abstract IBlockState getModelBlock();
	}

}
