package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.ResourceLocation;

public class TileEntityEnderChestRenderer extends TileEntitySpecialRenderer<TileEntityEnderChest> {

	private static final ResourceLocation ENDER_CHEST_TEXTURE = new ResourceLocation("textures/entity/chest/ender.png");
	private final ModelChest modelChest = new ModelChest();

	public void render(TileEntityEnderChest te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		int i = 0;

		if (te.hasWorld()) {
			i = te.getBlockMetadata();
		}

		if (destroyStage >= 0) {
			bindTexture(DESTROY_STAGES[destroyStage]);
			GlStateManager.matrixMode(5890);
			GlStateManager.pushMatrix();
			GlStateManager.scale(4F, 4F, 1F);
			GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
			GlStateManager.matrixMode(5888);
		} else {
			bindTexture(ENDER_CHEST_TEXTURE);
		}

		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();
		GlStateManager.color(1F, 1F, 1F, alpha);
		GlStateManager.translate((float) x, (float) y + 1F, (float) z + 1F);
		GlStateManager.scale(1F, -1F, -1F);
		GlStateManager.translate(0.5F, 0.5F, 0.5F);
		int j = 0;

		if (i == 2) {
			j = 180;
		}

		if (i == 3) {
			j = 0;
		}

		if (i == 4) {
			j = 90;
		}

		if (i == 5) {
			j = -90;
		}

		GlStateManager.rotate((float) j, 0F, 1F, 0F);
		GlStateManager.translate(-0.5F, -0.5F, -0.5F);
		float f = te.prevLidAngle + (te.lidAngle - te.prevLidAngle) * partialTicks;
		f = 1F - f;
		f = 1F - f * f * f;
		modelChest.chestLid.rotateAngleX = -(f * ((float) Math.PI / 2F));
		modelChest.renderAll();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		GlStateManager.color(1F, 1F, 1F, 1F);

		if (destroyStage >= 0) {
			GlStateManager.matrixMode(5890);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(5888);
		}
	}

}
