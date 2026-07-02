package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.model.ModelBanner;
import net.minecraft.client.renderer.BannerTextures;
import net.minecraft.client.renderer.GLS;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Maths;

public class TileEntityBannerRenderer extends TileEntitySpecialRenderer<TileEntityBanner> {

	private final ModelBanner bannerModel = new ModelBanner();

	public void render(TileEntityBanner te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		boolean flag = te.getWorld() != null;
		boolean flag1 = !flag || te.getBlockType() == Blocks.STANDING_BANNER;
		int i = flag ? te.getBlockMetadata() : 0;
		long j = flag ? te.getWorld().getTotalWorldTime() : 0L;
		GLS.pushMatrix();
		float f = 0.6666667F;

		if (flag1) {
			GLS.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
			float f1 = (float) (i * 360) / 16F;
			GLS.rotate(-f1, 0F, 1F, 0F);
			bannerModel.bannerStand.showModel = true;
		} else {
			float f2 = 0F;

			if (i == 2) {
				f2 = 180F;
			}

			if (i == 4) {
				f2 = 90F;
			}

			if (i == 5) {
				f2 = -90F;
			}

			GLS.translate((float) x + 0.5F, (float) y - 0.16666667F, (float) z + 0.5F);
			GLS.rotate(-f2, 0F, 1F, 0F);
			GLS.translate(0F, -0.3125F, -0.4375F);
			bannerModel.bannerStand.showModel = false;
		}

		BlockPos blockpos = te.getPos();
		float f3 = (float) (blockpos.getX() * 7 + blockpos.getY() * 9 + blockpos.getZ() * 13) + (float) j + partialTicks;
		bannerModel.bannerSlate.rotateAngleX = (-0.0125F + 0.01F * Maths.cos(f3 * (float) Math.PI * 0.02F)) * (float) Math.PI;
		GLS.enableRescaleNormal();
		ResourceLocation resourcelocation = getBannerResourceLocation(te);

		if (resourcelocation != null) {
			bindTexture(resourcelocation);
			GLS.pushMatrix();
			GLS.scale(0.6666667F, -0.6666667F, -0.6666667F);
			bannerModel.renderBanner();
			GLS.popMatrix();
		}

		GLS.color(1F, 1F, 1F, alpha);
		GLS.popMatrix();
	}

	private ResourceLocation getBannerResourceLocation(TileEntityBanner bannerObj) {
		return BannerTextures.BANNER_DESIGNS.getResourceLocation(bannerObj.getPatternResourceLocation(), bannerObj.getPatternList(), bannerObj.getColorList());
	}

}
