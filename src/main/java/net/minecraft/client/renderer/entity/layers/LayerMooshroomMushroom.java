package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderMooshroom;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.init.Blocks;

public class LayerMooshroomMushroom implements LayerRenderer<EntityMooshroom> {

	private final RenderMooshroom mooshroomRenderer;

	public LayerMooshroomMushroom(RenderMooshroom mooshroomRendererIn) {

		mooshroomRenderer = mooshroomRendererIn;
	}

	public void doRenderLayer(EntityMooshroom entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		if (!entitylivingbaseIn.isChild() && !entitylivingbaseIn.isInvisible()) {
			BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
			mooshroomRenderer.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.enableCull();
			GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
			GlStateManager.pushMatrix();
			GlStateManager.scale(1F, -1F, 1F);
			GlStateManager.translate(0.2F, 0.35F, 0.5F);
			GlStateManager.rotate(42F, 0F, 1F, 0F);
			GlStateManager.pushMatrix();
			GlStateManager.translate(-0.5F, -0.5F, 0.5F);
			blockrendererdispatcher.renderBlockBrightness(Blocks.RED_MUSHROOM.getDefaultState(), 1F);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.1F, 0F, -0.6F);
			GlStateManager.rotate(42F, 0F, 1F, 0F);
			GlStateManager.translate(-0.5F, -0.5F, 0.5F);
			blockrendererdispatcher.renderBlockBrightness(Blocks.RED_MUSHROOM.getDefaultState(), 1F);
			GlStateManager.popMatrix();
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			mooshroomRenderer.getMainModel().head.postRender(0.0625F);
			GlStateManager.scale(1F, -1F, 1F);
			GlStateManager.translate(0F, 0.7F, -0.2F);
			GlStateManager.rotate(12F, 0F, 1F, 0F);
			GlStateManager.translate(-0.5F, -0.5F, 0.5F);
			blockrendererdispatcher.renderBlockBrightness(Blocks.RED_MUSHROOM.getDefaultState(), 1F);
			GlStateManager.popMatrix();
			GlStateManager.cullFace(GlStateManager.CullFace.BACK);
			GlStateManager.disableCull();
		}
	}

	public boolean shouldCombineTextures() {

		return true;
	}

}
