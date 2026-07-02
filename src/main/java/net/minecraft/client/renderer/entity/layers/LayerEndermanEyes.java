package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderEnderman;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.util.ResourceLocation;


public class LayerEndermanEyes implements LayerRenderer<EntityEnderman> {

	private static final ResourceLocation RES_ENDERMAN_EYES = new ResourceLocation("textures/entity/enderman/enderman_eyes.png");
	private final RenderEnderman endermanRenderer;

	public LayerEndermanEyes(RenderEnderman endermanRendererIn) {
		endermanRenderer = endermanRendererIn;
	}

	public void doRenderLayer(EntityEnderman entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		endermanRenderer.bindTexture(RES_ENDERMAN_EYES);
		GLS.enableBlend();
		GLS.disableAlpha();
		GLS.blendFunc(GLS.SourceFactor.ONE, GLS.DestFactor.ONE);
		GLS.disableLighting();
		GLS.depthMask(!entitylivingbaseIn.isInvisible());
		int i = 61680;
		int j = 61680;
		int k = 0;
		GLS.multiTexCoord2f(OpenGlHelper.lightmapTexUnit, 61680F, 0F);
		GLS.enableLighting();
		GLS.color(1F, 1F, 1F, 1F);
		Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
		endermanRenderer.getMainModel()
		                .render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
		endermanRenderer.setLightmap(entitylivingbaseIn);
		GLS.depthMask(true);
		GLS.disableBlend();
		GLS.enableAlpha();
	}

	public boolean shouldCombineTextures() {
		return false;
	}

}
