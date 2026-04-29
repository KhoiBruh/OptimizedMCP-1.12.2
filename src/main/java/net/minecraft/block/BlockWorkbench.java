package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.stats.StatList;
import net.minecraft.util.Facing;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;

public class BlockWorkbench extends Block {

	protected BlockWorkbench() {

		super(Material.WOOD);
		setCreativeTab(CreativeTabs.DECORATIONS);
	}

	/**
	 * Called when the block is right clicked by a player.
	 */
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, Hand hand, Facing facing, float hitX, float hitY, float hitZ) {

		if (worldIn.isRemote) {
			return true;
		} else {
			playerIn.displayGui(new BlockWorkbench.InterfaceCraftingTable(worldIn, pos));
			playerIn.addStat(StatList.CRAFTING_TABLE_INTERACTION);
			return true;
		}
	}

	public static class InterfaceCraftingTable implements IInteractionObject {

		private final World world;
		private final BlockPos position;

		public InterfaceCraftingTable(World worldIn, BlockPos pos) {

			world = worldIn;
			position = pos;
		}

		public String getName() {

			return "crafting_table";
		}

		public boolean hasCustomName() {

			return false;
		}

		public ITextComponent displayName() {

			return new TextComponentTranslation(Blocks.CRAFTING_TABLE.getUnlocalizedName() + ".name");
		}

		public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {

			return new ContainerWorkbench(playerInventory, world, position);
		}

		public String guiID() {

			return "minecraft:crafting_table";
		}

	}

}
