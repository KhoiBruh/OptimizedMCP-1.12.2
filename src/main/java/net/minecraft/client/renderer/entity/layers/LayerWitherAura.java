package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelWither;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.entity.RenderWither;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Maths;

public class LayerWitherAura implements LayerRenderer<EntityWither> {

	private static final ResourceLocation WITHER_ARMOR = new ResourceLocation("textures/entity/wither/wither_armor.png");
	private final RenderWither witherRenderer;
	private final ModelWither witherModel = new ModelWither(0.5F);

	public LayerWitherAura(RenderWither witherRendererIn) {
		witherRenderer = witherRendererIn;
	}

	public void doRenderLayer(EntityWither entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (entitylivingbaseIn.isArmored()) {
			GLS.depthMask(!entitylivingbaseIn.isInvisible());
			witherRenderer.bindTexture(WITHER_ARMOR);
			GLS.matrixMode(5890);
			GLS.loadIdentity();
			float f = (float) entitylivingbaseIn.ticksExisted + partialTicks;
			float f1 = Maths.cos(f * 0.02F) * 3F;
			float f2 = f * 0.01F;
			GLS.translate(f1, f2, 0F);
			GLS.matrixMode(5888);
			GLS.enableBlend();
			float f3 = 0.5F;
			GLS.color(0.5F, 0.5F, 0.5F, 1F);
			GLS.disableLighting();
			GLS.blendFunc(GLS.SourceFactor.ONE, GLS.DestFactor.ONE);
			witherModel.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
			witherModel.setModelAttributes(witherRenderer.getMainModel());
			Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
			witherModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
			Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
			GLS.matrixMode(5890);
			GLS.loadIdentity();
			GLS.matrixMode(5888);
			GLS.enableLighting();
			GLS.disableBlend();
		}
	}

	public boolean shouldCombineTextures() {
		return false;
	}

}
