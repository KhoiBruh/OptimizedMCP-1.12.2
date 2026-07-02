package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GLS;
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
			GLS.matrixMode(5890);
			GLS.pushMatrix();
			GLS.scale(4F, 4F, 1F);
			GLS.translate(0.0625F, 0.0625F, 0.0625F);
			GLS.matrixMode(5888);
		} else {
			bindTexture(ENDER_CHEST_TEXTURE);
		}

		GLS.pushMatrix();
		GLS.enableRescaleNormal();
		GLS.color(1F, 1F, 1F, alpha);
		GLS.translate((float) x, (float) y + 1F, (float) z + 1F);
		GLS.scale(1F, -1F, -1F);
		GLS.translate(0.5F, 0.5F, 0.5F);
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

		GLS.rotate((float) j, 0F, 1F, 0F);
		GLS.translate(-0.5F, -0.5F, -0.5F);
		float f = te.prevLidAngle + (te.lidAngle - te.prevLidAngle) * partialTicks;
		f = 1F - f;
		f = 1F - f * f * f;
		modelChest.chestLid.rotateAngleX = -(f * ((float) Math.PI / 2F));
		modelChest.renderAll();
		GLS.disableRescaleNormal();
		GLS.popMatrix();
		GLS.color(1F, 1F, 1F, 1F);

		if (destroyStage >= 0) {
			GLS.matrixMode(5890);
			GLS.popMatrix();
			GLS.matrixMode(5888);
		}
	}

}
