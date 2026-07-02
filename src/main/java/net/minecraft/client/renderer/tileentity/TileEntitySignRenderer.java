package net.minecraft.client.renderer.tileentity;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GLS;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class TileEntitySignRenderer extends TileEntitySpecialRenderer<TileEntitySign> {

	private static final ResourceLocation SIGN_TEXTURE = new ResourceLocation("textures/entity/sign.png");

	/**
	 * The ModelSign instance for use in this renderer
	 */
	private final ModelSign model = new ModelSign();

	public void render(TileEntitySign te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		Block block = te.getBlockType();
		GLS.pushMatrix();
		float f = 0.6666667F;

		if (block == Blocks.STANDING_SIGN) {
			GLS.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
			float f1 = (float) (te.getBlockMetadata() * 360) / 16F;
			GLS.rotate(-f1, 0F, 1F, 0F);
			model.signStick.showModel = true;
		} else {
			int k = te.getBlockMetadata();
			float f2 = 0F;

			if (k == 2) {
				f2 = 180F;
			}

			if (k == 4) {
				f2 = 90F;
			}

			if (k == 5) {
				f2 = -90F;
			}

			GLS.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
			GLS.rotate(-f2, 0F, 1F, 0F);
			GLS.translate(0F, -0.3125F, -0.4375F);
			model.signStick.showModel = false;
		}

		if (destroyStage >= 0) {
			bindTexture(DESTROY_STAGES[destroyStage]);
			GLS.matrixMode(5890);
			GLS.pushMatrix();
			GLS.scale(4F, 2F, 1F);
			GLS.translate(0.0625F, 0.0625F, 0.0625F);
			GLS.matrixMode(5888);
		} else {
			bindTexture(SIGN_TEXTURE);
		}

		GLS.enableRescaleNormal();
		GLS.pushMatrix();
		GLS.scale(0.6666667F, -0.6666667F, -0.6666667F);
		model.renderSign();
		GLS.popMatrix();
		FontRenderer fontrenderer = getFontRenderer();
		float f3 = 0.010416667F;
		GLS.translate(0F, 0.33333334F, 0.046666667F);
		GLS.scale(0.010416667F, -0.010416667F, 0.010416667F);
		GLS.normal3f(0F, 0F, -0.010416667F);
		GLS.depthMask(false);
		int i = 0;

		if (destroyStage < 0) {
			for (int j = 0; j < te.signText.length; ++j) {
				if (te.signText[j] != null) {
					ITextComponent itextcomponent = te.signText[j];
					List<ITextComponent> list = GuiUtilRenderComponents.splitText(itextcomponent, 90, fontrenderer, false, true);
					String s = list != null && !list.isEmpty() ? list.getFirst().getFormattedText() : "";

					if (j == te.lineBeingEdited) {
						s = "> " + s + " <";
						fontrenderer.drawText(s, -fontrenderer.getWidth(s) / 2, j * 10 - te.signText.length * 5, 0);
					} else {
						fontrenderer.drawText(s, -fontrenderer.getWidth(s) / 2, j * 10 - te.signText.length * 5, 0);
					}
				}
			}
		}

		GLS.depthMask(true);
		GLS.color(1F, 1F, 1F, 1F);
		GLS.popMatrix();

		if (destroyStage >= 0) {
			GLS.matrixMode(5890);
			GLS.popMatrix();
			GLS.matrixMode(5888);
		}
	}

}
