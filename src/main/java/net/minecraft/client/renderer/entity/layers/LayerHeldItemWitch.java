package net.minecraft.client.renderer.entity.layers;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderWitch;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderType;

public class LayerHeldItemWitch implements LayerRenderer<EntityWitch> {

	private final RenderWitch witchRenderer;

	public LayerHeldItemWitch(RenderWitch witchRendererIn) {

		witchRenderer = witchRendererIn;
	}

	public void doRenderLayer(EntityWitch entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		ItemStack itemstack = entitylivingbaseIn.getHeldItemMainhand();

		if (!itemstack.isEmpty()) {
			GLS.color(1F, 1F, 1F);
			GLS.pushMatrix();

			if (witchRenderer.getMainModel().isChild) {
				GLS.translate(0F, 0.625F, 0F);
				GLS.rotate(-20F, -1F, 0F, 0F);
				float f = 0.5F;
				GLS.scale(0.5F, 0.5F, 0.5F);
			}

			witchRenderer.getMainModel().villagerNose.postRender(0.0625F);
			GLS.translate(-0.0625F, 0.53125F, 0.21875F);
			Item item = itemstack.getItem();
			Minecraft minecraft = Minecraft.getMinecraft();

			if (Block.getBlockFromItem(item).getDefaultState().getRenderType() == BlockRenderType.ENTITYBLOCK_ANIMATED) {
				GLS.translate(0F, 0.0625F, -0.25F);
				GLS.rotate(30F, 1F, 0F, 0F);
				GLS.rotate(-5F, 0F, 1F, 0F);
				float f1 = 0.375F;
				GLS.scale(0.375F, -0.375F, 0.375F);
			} else if (item == Items.BOW) {
				GLS.translate(0F, 0.125F, -0.125F);
				GLS.rotate(-45F, 0F, 1F, 0F);
				float f2 = 0.625F;
				GLS.scale(0.625F, -0.625F, 0.625F);
				GLS.rotate(-100F, 1F, 0F, 0F);
				GLS.rotate(-20F, 0F, 1F, 0F);
			} else if (item.isFull3D()) {
				if (item.shouldRotateAroundWhenRendering()) {
					GLS.rotate(180F, 0F, 0F, 1F);
					GLS.translate(0F, -0.0625F, 0F);
				}

				witchRenderer.transformHeldFull3DItemLayer();
				GLS.translate(0.0625F, -0.125F, 0F);
				float f3 = 0.625F;
				GLS.scale(0.625F, -0.625F, 0.625F);
				GLS.rotate(0F, 1F, 0F, 0F);
				GLS.rotate(0F, 0F, 1F, 0F);
			} else {
				GLS.translate(0.1875F, 0.1875F, 0F);
				float f4 = 0.875F;
				GLS.scale(0.875F, 0.875F, 0.875F);
				GLS.rotate(-20F, 0F, 0F, 1F);
				GLS.rotate(-60F, 1F, 0F, 0F);
				GLS.rotate(-30F, 0F, 0F, 1F);
			}

			GLS.rotate(-15F, 1F, 0F, 0F);
			GLS.rotate(40F, 0F, 0F, 1F);
			minecraft.getItemRenderer().renderItem(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
			GLS.popMatrix();
		}
	}

	public boolean shouldCombineTextures() {

		return false;
	}

}
