package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.boss.EntityDragon;

import java.util.Random;

public class LayerEnderDragonDeath implements LayerRenderer<EntityDragon> {

	public void doRenderLayer(EntityDragon entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (entitylivingbaseIn.deathTicks > 0) {
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			RenderHelper.disableStandardItemLighting();
			float f = ((float) entitylivingbaseIn.deathTicks + partialTicks) / 200F;
			float f1 = 0F;

			if (f > 0.8F) {
				f1 = (f - 0.8F) / 0.2F;
			}

			Random random = new Random(432L);
			GLS.disableTexture2D();
			GLS.shadeModel(7425);
			GLS.enableBlend();
			GLS.blendFunc(GLS.SourceFactor.SRC_ALPHA, GLS.DestFactor.ONE);
			GLS.disableAlpha();
			GLS.enableCull();
			GLS.depthMask(false);
			GLS.pushMatrix();
			GLS.translate(0F, -1F, -2F);

			for (int i = 0; (float) i < (f + f * f) / 2F * 60F; ++i) {
				GLS.rotate(random.nextFloat() * 360F, 1F, 0F, 0F);
				GLS.rotate(random.nextFloat() * 360F, 0F, 1F, 0F);
				GLS.rotate(random.nextFloat() * 360F, 0F, 0F, 1F);
				GLS.rotate(random.nextFloat() * 360F, 1F, 0F, 0F);
				GLS.rotate(random.nextFloat() * 360F, 0F, 1F, 0F);
				GLS.rotate(random.nextFloat() * 360F + f * 90F, 0F, 0F, 1F);
				float f2 = random.nextFloat() * 20F + 5F + f1 * 10F;
				float f3 = random.nextFloat() * 2F + 1F + f1 * 2F;
				bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
				bufferbuilder.pos(0D, 0D, 0D).color(255, 255, 255, (int) (255F * (1F - f1))).endVertex();
				bufferbuilder.pos(-0.866D * (double) f3, f2, -0.5F * f3).color(255, 0, 255, 0).endVertex();
				bufferbuilder.pos(0.866D * (double) f3, f2, -0.5F * f3).color(255, 0, 255, 0).endVertex();
				bufferbuilder.pos(0D, f2, f3).color(255, 0, 255, 0).endVertex();
				bufferbuilder.pos(-0.866D * (double) f3, f2, -0.5F * f3).color(255, 0, 255, 0).endVertex();
				tessellator.draw();
			}

			GLS.popMatrix();
			GLS.depthMask(true);
			GLS.disableCull();
			GLS.disableBlend();
			GLS.shadeModel(7424);
			GLS.color(1F, 1F, 1F, 1F);
			GLS.enableTexture2D();
			GLS.enableAlpha();
			RenderHelper.enableStandardItemLighting();
		}
	}

	public boolean shouldCombineTextures() {
		return false;
	}

}
