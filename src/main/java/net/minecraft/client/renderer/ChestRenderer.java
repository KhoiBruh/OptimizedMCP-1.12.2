package net.minecraft.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;

public class ChestRenderer {

	public void renderChestBrightness(Block blockIn, float color) {

		GlStateManager.color(color, color, color, 1F);
		GlStateManager.rotate(90F, 0F, 1F, 0F);
		TileEntityItemStackRenderer.instance.renderByItem(new ItemStack(blockIn));
	}

}
