package net.minecraft.client.renderer.tileentity;

import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelShulker;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderShulker;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.EnumFacing;

public class TileEntityShulkerBoxRenderer extends TileEntitySpecialRenderer<TileEntityShulkerBox> {

	private final ModelShulker model;

	public TileEntityShulkerBoxRenderer(ModelShulker modelIn) {

		model = modelIn;
	}

	public void render(TileEntityShulkerBox te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		EnumFacing enumfacing = EnumFacing.UP;

		if (te.hasWorld()) {
			IBlockState iblockstate = getWorld().getBlockState(te.getPos());

			if (iblockstate.getBlock() instanceof BlockShulkerBox) {
				enumfacing = iblockstate.getValue(BlockShulkerBox.FACING);
			}
		}

		GlStateManager.enableDepth();
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);
		GlStateManager.disableCull();

		if (destroyStage >= 0) {
			bindTexture(DESTROY_STAGES[destroyStage]);
			GlStateManager.matrixMode(5890);
			GlStateManager.pushMatrix();
			GlStateManager.scale(4F, 4F, 1F);
			GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
			GlStateManager.matrixMode(5888);
		} else {
			bindTexture(RenderShulker.SHULKER_ENDERGOLEM_TEXTURE[te.getColor().getMetadata()]);
		}

		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();

		if (destroyStage < 0) {
			GlStateManager.color(1F, 1F, 1F, alpha);
		}

		GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GlStateManager.scale(1F, -1F, -1F);
		GlStateManager.translate(0F, 1F, 0F);
		float f = 0.9995F;
		GlStateManager.scale(0.9995F, 0.9995F, 0.9995F);
		GlStateManager.translate(0F, -1F, 0F);

		switch (enumfacing) {
			case DOWN:
				GlStateManager.translate(0F, 2F, 0F);
				GlStateManager.rotate(180F, 1F, 0F, 0F);

			case UP:
			default:
				break;

			case NORTH:
				GlStateManager.translate(0F, 1F, 1F);
				GlStateManager.rotate(90F, 1F, 0F, 0F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				break;

			case SOUTH:
				GlStateManager.translate(0F, 1F, -1F);
				GlStateManager.rotate(90F, 1F, 0F, 0F);
				break;

			case WEST:
				GlStateManager.translate(-1F, 1F, 0F);
				GlStateManager.rotate(90F, 1F, 0F, 0F);
				GlStateManager.rotate(-90F, 0F, 0F, 1F);
				break;

			case EAST:
				GlStateManager.translate(1F, 1F, 0F);
				GlStateManager.rotate(90F, 1F, 0F, 0F);
				GlStateManager.rotate(90F, 0F, 0F, 1F);
		}

		model.base.render(0.0625F);
		GlStateManager.translate(0F, -te.getProgress(partialTicks) * 0.5F, 0F);
		GlStateManager.rotate(270F * te.getProgress(partialTicks), 0F, 1F, 0F);
		model.lid.render(0.0625F);
		GlStateManager.enableCull();
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
