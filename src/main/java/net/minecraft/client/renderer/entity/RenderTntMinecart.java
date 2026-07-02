package net.minecraft.client.renderer.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GLS;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Maths;

public class RenderTntMinecart extends RenderMinecart<EntityMinecartTNT> {

	public RenderTntMinecart(RenderManager renderManagerIn) {
		super(renderManagerIn);
	}

	protected void renderCartContents(EntityMinecartTNT p_188319_1_, float partialTicks, IBlockState p_188319_3_) {
		int i = p_188319_1_.getFuseTicks();

		if (i > -1 && (float) i - partialTicks + 1F < 10F) {
			float f = 1F - ((float) i - partialTicks + 1F) / 10F;
			f = Maths.clamp(f, 0F, 1F);
			f = f * f;
			f = f * f;
			float f1 = 1F + f * 0.3F;
			GLS.scale(f1, f1, f1);
		}

		super.renderCartContents(p_188319_1_, partialTicks, p_188319_3_);

		if (i > -1 && i / 5 % 2 == 0) {
			BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
			GLS.disableTexture2D();
			GLS.disableLighting();
			GLS.enableBlend();
			GLS.blendFunc(GLS.SourceFactor.SRC_ALPHA, GLS.DestFactor.DST_ALPHA);
			GLS.color(1F, 1F, 1F, (1F - ((float) i - partialTicks + 1F) / 100F) * 0.8F);
			GLS.pushMatrix();
			blockrendererdispatcher.renderBlockBrightness(Blocks.TNT.getDefaultState(), 1F);
			GLS.popMatrix();
			GLS.color(1F, 1F, 1F, 1F);
			GLS.disableBlend();
			GLS.enableLighting();
			GLS.enableTexture2D();
		}
	}

}
