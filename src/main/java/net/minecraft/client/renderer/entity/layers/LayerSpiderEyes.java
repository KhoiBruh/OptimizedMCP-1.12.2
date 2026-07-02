package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderSpider;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.ResourceLocation;


public class LayerSpiderEyes<T extends EntitySpider> implements LayerRenderer<T> {

	private static final ResourceLocation SPIDER_EYES = new ResourceLocation("textures/entity/spider_eyes.png");
	private final RenderSpider<T> spiderRenderer;

	public LayerSpiderEyes(RenderSpider<T> spiderRendererIn) {
		spiderRenderer = spiderRendererIn;
	}

	public void doRenderLayer(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		spiderRenderer.bindTexture(SPIDER_EYES);
		GLS.enableBlend();
		GLS.disableAlpha();
		GLS.blendFunc(GLS.SourceFactor.ONE, GLS.DestFactor.ONE);

		GLS.depthMask(!entitylivingbaseIn.isInvisible());

		int i = 61680;
		int j = i % 65536;
		int k = i / 65536;
		GLS.multiTexCoord2f(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
		GLS.color(1F, 1F, 1F, 1F);
		Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
		spiderRenderer.getMainModel()
		              .render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
		i = entitylivingbaseIn.getBrightnessForRender();
		j = i % 65536;
		k = i / 65536;
		GLS.multiTexCoord2f(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
		spiderRenderer.setLightmap(entitylivingbaseIn);
		GLS.disableBlend();
		GLS.enableAlpha();
	}

	public boolean shouldCombineTextures() {
		return false;
	}

}
