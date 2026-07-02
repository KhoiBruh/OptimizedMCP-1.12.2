package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderDragon;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.ResourceLocation;


public class LayerEnderDragonEyes implements LayerRenderer<EntityDragon> {

	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/enderdragon/dragon_eyes.png");
	private final RenderDragon dragonRenderer;

	public LayerEnderDragonEyes(RenderDragon dragonRendererIn) {
		dragonRenderer = dragonRendererIn;
	}

	public void doRenderLayer(EntityDragon entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		dragonRenderer.bindTexture(TEXTURE);
		GLS.enableBlend();
		GLS.disableAlpha();
		GLS.blendFunc(GLS.SourceFactor.ONE, GLS.DestFactor.ONE);
		GLS.disableLighting();
		GLS.depthFunc(514);
		int i = 61680;
		int j = 61680;
		int k = 0;
		GLS.multiTexCoord2f(OpenGlHelper.lightmapTexUnit, 61680F, 0F);
		GLS.enableLighting();
		GLS.color(1F, 1F, 1F, 1F);
		Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
		dragonRenderer.getMainModel()
		              .render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
		dragonRenderer.setLightmap(entitylivingbaseIn);
		GLS.disableBlend();
		GLS.enableAlpha();
		GLS.depthFunc(515);
	}

	public boolean shouldCombineTextures() {
		return false;
	}

}
