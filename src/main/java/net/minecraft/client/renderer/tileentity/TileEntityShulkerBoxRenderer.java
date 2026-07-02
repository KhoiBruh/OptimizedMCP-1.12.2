package net.minecraft.client.renderer.tileentity;

import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelShulker;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.entity.RenderShulker;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.Facing;

public class TileEntityShulkerBoxRenderer extends TileEntitySpecialRenderer<TileEntityShulkerBox> {

	private final ModelShulker model;

	public TileEntityShulkerBoxRenderer(ModelShulker modelIn) {
		model = modelIn;
	}

	public void render(TileEntityShulkerBox te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		Facing enumfacing = Facing.UP;

		if (te.hasWorld()) {
			IBlockState iblockstate = getWorld().getBlockState(te.getPos());

			if (iblockstate.getBlock() instanceof BlockShulkerBox) {
				enumfacing = iblockstate.getValue(BlockShulkerBox.FACING);
			}
		}

		GLS.enableDepth();
		GLS.depthFunc(515);
		GLS.depthMask(true);
		GLS.disableCull();

		if (destroyStage >= 0) {
			bindTexture(DESTROY_STAGES[destroyStage]);
			GLS.matrixMode(5890);
			GLS.pushMatrix();
			GLS.scale(4F, 4F, 1F);
			GLS.translate(0.0625F, 0.0625F, 0.0625F);
			GLS.matrixMode(5888);
		} else {
			bindTexture(RenderShulker.SHULKER_ENDERGOLEM_TEXTURE[te.getColor().getMetadata()]);
		}

		GLS.pushMatrix();
		GLS.enableRescaleNormal();

		if (destroyStage < 0) {
			GLS.color(1F, 1F, 1F, alpha);
		}

		GLS.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GLS.scale(1F, -1F, -1F);
		GLS.translate(0F, 1F, 0F);
		float f = 0.9995F;
		GLS.scale(0.9995F, 0.9995F, 0.9995F);
		GLS.translate(0F, -1F, 0F);

		switch (enumfacing) {
			case DOWN:
				GLS.translate(0F, 2F, 0F);
				GLS.rotate(180F, 1F, 0F, 0F);

			case UP:
			default:
				break;

			case NORTH:
				GLS.translate(0F, 1F, 1F);
				GLS.rotate(90F, 1F, 0F, 0F);
				GLS.rotate(180F, 0F, 0F, 1F);
				break;

			case SOUTH:
				GLS.translate(0F, 1F, -1F);
				GLS.rotate(90F, 1F, 0F, 0F);
				break;

			case WEST:
				GLS.translate(-1F, 1F, 0F);
				GLS.rotate(90F, 1F, 0F, 0F);
				GLS.rotate(-90F, 0F, 0F, 1F);
				break;

			case EAST:
				GLS.translate(1F, 1F, 0F);
				GLS.rotate(90F, 1F, 0F, 0F);
				GLS.rotate(90F, 0F, 0F, 1F);
		}

		model.base.render(0.0625F);
		GLS.translate(0F, -te.getProgress(partialTicks) * 0.5F, 0F);
		GLS.rotate(270F * te.getProgress(partialTicks), 0F, 1F, 0F);
		model.lid.render(0.0625F);
		GLS.enableCull();
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
