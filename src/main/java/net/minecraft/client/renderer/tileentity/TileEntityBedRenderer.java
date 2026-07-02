package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.model.ModelBed;
import net.minecraft.client.renderer.GLS;
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
			GLS.matrixMode(5890);
			GLS.pushMatrix();
			GLS.scale(4F, 4F, 1F);
			GLS.translate(0.0625F, 0.0625F, 0.0625F);
			GLS.matrixMode(5888);
		} else {
			ResourceLocation resourcelocation = TEXTURES[enumdyecolor.getMetadata()];

			if (resourcelocation != null) {
				bindTexture(resourcelocation);
			}
		}

		if (flag) {
			renderPiece(flag1, x, y, z, i, alpha);
		} else {
			GLS.pushMatrix();
			renderPiece(true, x, y, z, i, alpha);
			renderPiece(false, x, y, z - 1D, i, alpha);
			GLS.popMatrix();
		}

		if (destroyStage >= 0) {
			GLS.matrixMode(5890);
			GLS.popMatrix();
			GLS.matrixMode(5888);
		}
	}

	private void renderPiece(boolean p_193847_1_, double x, double y, double z, int p_193847_8_, float alpha) {
		model.preparePiece(p_193847_1_);
		GLS.pushMatrix();
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

		GLS.translate((float) x + f1, (float) y + 0.5625F, (float) z + f2);
		GLS.rotate(90F, 1F, 0F, 0F);
		GLS.rotate(f, 0F, 0F, 1F);
		GLS.enableRescaleNormal();
		GLS.pushMatrix();
		model.render();
		GLS.popMatrix();
		GLS.color(1F, 1F, 1F, alpha);
		GLS.popMatrix();
	}

}
