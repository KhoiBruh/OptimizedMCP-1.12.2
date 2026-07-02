package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GLS;
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
			GLS.enableCull();
			GLS.cullFace(GLS.CullFace.FRONT);
			GLS.pushMatrix();
			GLS.scale(1F, -1F, 1F);
			GLS.translate(0.2F, 0.35F, 0.5F);
			GLS.rotate(42F, 0F, 1F, 0F);
			GLS.pushMatrix();
			GLS.translate(-0.5F, -0.5F, 0.5F);
			blockrendererdispatcher.renderBlockBrightness(Blocks.RED_MUSHROOM.getDefaultState(), 1F);
			GLS.popMatrix();
			GLS.pushMatrix();
			GLS.translate(0.1F, 0F, -0.6F);
			GLS.rotate(42F, 0F, 1F, 0F);
			GLS.translate(-0.5F, -0.5F, 0.5F);
			blockrendererdispatcher.renderBlockBrightness(Blocks.RED_MUSHROOM.getDefaultState(), 1F);
			GLS.popMatrix();
			GLS.popMatrix();
			GLS.pushMatrix();
			mooshroomRenderer.getMainModel().head.postRender(0.0625F);
			GLS.scale(1F, -1F, 1F);
			GLS.translate(0F, 0.7F, -0.2F);
			GLS.rotate(12F, 0F, 1F, 0F);
			GLS.translate(-0.5F, -0.5F, 0.5F);
			blockrendererdispatcher.renderBlockBrightness(Blocks.RED_MUSHROOM.getDefaultState(), 1F);
			GLS.popMatrix();
			GLS.cullFace(GLS.CullFace.BACK);
			GLS.disableCull();
		}
	}

	public boolean shouldCombineTextures() {
		return true;
	}

}
