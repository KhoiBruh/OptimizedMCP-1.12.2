package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.entity.RenderPlayer;

public class LayerDeadmau5Head implements LayerRenderer<AbstractClientPlayer> {

	private final RenderPlayer playerRenderer;

	public LayerDeadmau5Head(RenderPlayer playerRendererIn) {
		playerRenderer = playerRendererIn;
	}

	public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if ("deadmau5".equals(entitylivingbaseIn.getName()) && entitylivingbaseIn.hasSkin() && !entitylivingbaseIn.isInvisible()) {
			playerRenderer.bindTexture(entitylivingbaseIn.getLocationSkin());

			for (int i = 0; i < 2; ++i) {
				float f = entitylivingbaseIn.prevRotationYaw + (entitylivingbaseIn.rotationYaw - entitylivingbaseIn.prevRotationYaw) * partialTicks - (entitylivingbaseIn.prevRenderYawOffset + (entitylivingbaseIn.renderYawOffset - entitylivingbaseIn.prevRenderYawOffset) * partialTicks);
				float f1 = entitylivingbaseIn.prevRotationPitch + (entitylivingbaseIn.rotationPitch - entitylivingbaseIn.prevRotationPitch) * partialTicks;
				GLS.pushMatrix();
				GLS.rotate(f, 0F, 1F, 0F);
				GLS.rotate(f1, 1F, 0F, 0F);
				GLS.translate(0.375F * (float) (i * 2 - 1), 0F, 0F);
				GLS.translate(0F, -0.375F, 0F);
				GLS.rotate(-f1, 1F, 0F, 0F);
				GLS.rotate(-f, 0F, 1F, 0F);
				float f2 = 1.3333334F;
				GLS.scale(1.3333334F, 1.3333334F, 1.3333334F);
				playerRenderer.getMainModel().renderDeadmau5Head(0.0625F);
				GLS.popMatrix();
			}
		}
	}

	public boolean shouldCombineTextures() {
		return true;
	}

}
