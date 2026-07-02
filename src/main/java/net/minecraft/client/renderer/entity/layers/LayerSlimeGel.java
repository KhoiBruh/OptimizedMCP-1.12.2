package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelSlime;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.entity.RenderSlime;
import net.minecraft.entity.monster.EntitySlime;

public class LayerSlimeGel implements LayerRenderer<EntitySlime> {

	private final RenderSlime slimeRenderer;
	private final ModelBase slimeModel = new ModelSlime(0);

	public LayerSlimeGel(RenderSlime slimeRendererIn) {
		slimeRenderer = slimeRendererIn;
	}

	public void doRenderLayer(EntitySlime entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (!entitylivingbaseIn.isInvisible()) {
			GLS.color(1F, 1F, 1F, 1F);
			GLS.enableNormalize();
			GLS.enableBlend();
			GLS.blendFunc(GLS.SourceFactor.SRC_ALPHA, GLS.DestFactor.ONE_MINUS_SRC_ALPHA);
			slimeModel.setModelAttributes(slimeRenderer.getMainModel());
			slimeModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
			GLS.disableBlend();
			GLS.disableNormalize();
		}
	}

	public boolean shouldCombineTextures() {
		return true;
	}

}
