package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.model.ModelBed;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.DyeColor;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.util.Facing;
import net.minecraft.util.ResourceLocation;

public class TileEntityBedRenderer extends TileEntitySpecialRenderer<TileEntityBed> {

	private static final ResourceLocation[] TEXTURES;

	static {
		DyeColor[] aenumdyecolor = DyeColor.values();
		TEXTURES = new ResourceLocation[aenumdyecolor.length];

		for (DyeColor enumdyecolor : aenumdyecolor) {
			TEXTURES[enumdyecolor.getMetadata()] = new ResourceLocation("textures/entity/bed/" + enumdyecolor.getDyeColorName() + ".png");
		}
	}

	private ModelBed model = new ModelBed();
	private int version;

	public TileEntityBedRenderer() {

		version = model.getModelVersion();
	}

	public void render(TileEntityBed te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		if (version != model.getModelVersion()) {
			model = new ModelBed();
			version = model.getModelVersion();
		}

		boolean flag = te.getWorld() != null;
		boolean flag1 = !flag || te.isHeadPiece();
		DyeColor enumdyecolor = te != null ? te.getColor() : DyeColor.RED;
		int i = flag ? te.getBlockMetadata() & 3 : 0;

		if (destroyStage >= 0) {
			bindTexture(DESTROY_STAGES[destroyStage]);
			GlStateManager.matrixMode(5890);
			GlStateManager.pushMatrix();
			GlStateManager.scale(4F, 4F, 1F);
			GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
			GlStateManager.matrixMode(5888);
		} else {
			ResourceLocation resourcelocation = TEXTURES[enumdyecolor.getMetadata()];

			if (resourcelocation != null) {
				bindTexture(resourcelocation);
			}
		}

		if (flag) {
			renderPiece(flag1, x, y, z, i, alpha);
		} else {
			GlStateManager.pushMatrix();
			renderPiece(true, x, y, z, i, alpha);
			renderPiece(false, x, y, z - 1D, i, alpha);
			GlStateManager.popMatrix();
		}

		if (destroyStage >= 0) {
			GlStateManager.matrixMode(5890);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(5888);
		}
	}

	private void renderPiece(boolean p_193847_1_, double x, double y, double z, int p_193847_8_, float alpha) {

		model.preparePiece(p_193847_1_);
		GlStateManager.pushMatrix();
		float f = 0F;
		float f1 = 0F;
		float f2 = 0F;

		if (p_193847_8_ == Facing.NORTH.getHorizontalIndex()) {
			f = 0F;
		} else if (p_193847_8_ == Facing.SOUTH.getHorizontalIndex()) {
			f = 180F;
			f1 = 1F;
			f2 = 1F;
		} else if (p_193847_8_ == Facing.WEST.getHorizontalIndex()) {
			f = -90F;
			f2 = 1F;
		} else if (p_193847_8_ == Facing.EAST.getHorizontalIndex()) {
			f = 90F;
			f1 = 1F;
		}

		GlStateManager.translate((float) x + f1, (float) y + 0.5625F, (float) z + f2);
		GlStateManager.rotate(90F, 1F, 0F, 0F);
		GlStateManager.rotate(f, 0F, 0F, 1F);
		GlStateManager.enableRescaleNormal();
		GlStateManager.pushMatrix();
		model.render();
		GlStateManager.popMatrix();
		GlStateManager.color(1F, 1F, 1F, alpha);
		GlStateManager.popMatrix();
	}
}
