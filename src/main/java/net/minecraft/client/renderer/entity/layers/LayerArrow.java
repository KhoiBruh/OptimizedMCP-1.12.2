package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class LayerArrow implements LayerRenderer<EntityLivingBase> {

	private final RenderLivingBase<?> renderer;

	public LayerArrow(RenderLivingBase<?> rendererIn) {

		renderer = rendererIn;
	}

	public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		int i = entitylivingbaseIn.getArrowCountInEntity();

		if (i > 0) {
			Entity entity = new EntityTippedArrow(entitylivingbaseIn.world, entitylivingbaseIn.posX, entitylivingbaseIn.posY, entitylivingbaseIn.posZ);
			Random random = new Random(entitylivingbaseIn.getEntityId());
			RenderHelper.disableStandardItemLighting();

			for (int j = 0; j < i; ++j) {
				GlStateManager.pushMatrix();
				ModelRenderer modelrenderer = renderer.getMainModel().getRandomModelBox(random);
				ModelBox modelbox = modelrenderer.cubeList.get(random.nextInt(modelrenderer.cubeList.size()));
				modelrenderer.postRender(0.0625F);
				float f = random.nextFloat();
				float f1 = random.nextFloat();
				float f2 = random.nextFloat();
				float f3 = (modelbox.posX1 + (modelbox.posX2 - modelbox.posX1) * f) / 16F;
				float f4 = (modelbox.posY1 + (modelbox.posY2 - modelbox.posY1) * f1) / 16F;
				float f5 = (modelbox.posZ1 + (modelbox.posZ2 - modelbox.posZ1) * f2) / 16F;
				GlStateManager.translate(f3, f4, f5);
				f = f * 2F - 1F;
				f1 = f1 * 2F - 1F;
				f2 = f2 * 2F - 1F;
				f = f * -1F;
				f1 = f1 * -1F;
				f2 = f2 * -1F;
				float f6 = MathHelper.sqrt(f * f + f2 * f2);
				entity.rotationYaw = (float) (Math.atan2(f, f2) * (180D / Math.PI));
				entity.rotationPitch = (float) (Math.atan2(f1, f6) * (180D / Math.PI));
				entity.prevRotationYaw = entity.rotationYaw;
				entity.prevRotationPitch = entity.rotationPitch;
				double d0 = 0D;
				double d1 = 0D;
				double d2 = 0D;
				renderer.getRenderManager().renderEntity(entity, 0D, 0D, 0D, 0F, partialTicks, false);
				GlStateManager.popMatrix();
			}

			RenderHelper.enableStandardItemLighting();
		}
	}

	public boolean shouldCombineTextures() {

		return false;
	}

}
