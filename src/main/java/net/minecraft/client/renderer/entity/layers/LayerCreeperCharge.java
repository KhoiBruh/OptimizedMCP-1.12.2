package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelCreeper;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.entity.RenderCreeper;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.ResourceLocation;

public class LayerCreeperCharge implements LayerRenderer<EntityCreeper> {

	private static final ResourceLocation LIGHTNING_TEXTURE = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
	private final RenderCreeper creeperRenderer;
	private final ModelCreeper creeperModel = new ModelCreeper(2F);

	public LayerCreeperCharge(RenderCreeper creeperRendererIn) {
		creeperRenderer = creeperRendererIn;
	}

	public void doRenderLayer(EntityCreeper entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (entitylivingbaseIn.getPowered()) {
			boolean flag = entitylivingbaseIn.isInvisible();
			GLS.depthMask(!flag);
			creeperRenderer.bindTexture(LIGHTNING_TEXTURE);
			GLS.matrixMode(5890);
			GLS.loadIdentity();
			float f = (float) entitylivingbaseIn.ticksExisted + partialTicks;
			GLS.translate(f * 0.01F, f * 0.01F, 0F);
			GLS.matrixMode(5888);
			GLS.enableBlend();
			float f1 = 0.5F;
			GLS.color(0.5F, 0.5F, 0.5F, 1F);
			GLS.disableLighting();
			GLS.blendFunc(GLS.SourceFactor.ONE, GLS.DestFactor.ONE);
			creeperModel.setModelAttributes(creeperRenderer.getMainModel());
			Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
			creeperModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
			Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
			GLS.matrixMode(5890);
			GLS.loadIdentity();
			GLS.matrixMode(5888);
			GLS.enableLighting();
			GLS.disableBlend();
			GLS.depthMask(flag);
		}
	}

	public boolean shouldCombineTextures() {
		return false;
	}

}
