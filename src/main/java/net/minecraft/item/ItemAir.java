package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemAir extends Item {

	private final Block block;

	public ItemAir(Block blockIn) {

		block = blockIn;
	}

	/**
	 * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
	 * different names based on their damage or NBT.
	 */
	public String getUnlocalizedName(ItemStack stack) {

		return block.getUnlocalizedName();
	}

	/**
	 * Returns the unlocalized name of this item.
	 */
	public String getUnlocalizedName() {

		return block.getUnlocalizedName();
	}

	/**
	 * allows items to add custom lines of information to the mouseover description
	 */
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

		super.addInformation(stack, worldIn, tooltip, flagIn);
		block.addInformation(stack, worldIn, tooltip, flagIn);
	}

}
