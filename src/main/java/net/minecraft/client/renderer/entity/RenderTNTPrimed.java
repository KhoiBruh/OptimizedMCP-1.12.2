package net.minecraft.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Maths;

public class RenderTNTPrimed extends Render<EntityTNTPrimed> {

	public RenderTNTPrimed(RenderManager renderManagerIn) {
		super(renderManagerIn);
		shadowSize = 0.5F;
	}

	/**
	 * Renders the desired {@code T} type Entity.
	 */
	public void doRender(EntityTNTPrimed entity, double x, double y, double z, float entityYaw, float partialTicks) {
		BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		GLS.pushMatrix();
		GLS.translate((float) x, (float) y + 0.5F, (float) z);

		if ((float) entity.getFuse() - partialTicks + 1F < 10F) {
			float f = 1F - ((float) entity.getFuse() - partialTicks + 1F) / 10F;
			f = Maths.clamp(f, 0F, 1F);
			f = f * f;
			f = f * f;
			float f1 = 1F + f * 0.3F;
			GLS.scale(f1, f1, f1);
		}

		float f2 = (1F - ((float) entity.getFuse() - partialTicks + 1F) / 100F) * 0.8F;
		bindEntityTexture(entity);
		GLS.rotate(-90F, 0F, 1F, 0F);
		GLS.translate(-0.5F, -0.5F, 0.5F);
		blockrendererdispatcher.renderBlockBrightness(Blocks.TNT.getDefaultState(), entity.getBrightness());
		GLS.translate(0F, 0F, 1F);

		if (renderOutlines) {
			GLS.enableColorMaterial();
			GLS.enableOutlineMode(getTeamColor(entity));
			blockrendererdispatcher.renderBlockBrightness(Blocks.TNT.getDefaultState(), 1F);
			GLS.disableOutlineMode();
			GLS.disableColorMaterial();
		} else if (entity.getFuse() / 5 % 2 == 0) {
			GLS.disableTexture2D();
			GLS.disableLighting();
			GLS.enableBlend();
			GLS.blendFunc(GLS.SourceFactor.SRC_ALPHA, GLS.DestFactor.DST_ALPHA);
			GLS.color(1F, 1F, 1F, f2);
			GLS.doPolygonOffset(-3F, -3F);
			GLS.enablePolygonOffset();
			blockrendererdispatcher.renderBlockBrightness(Blocks.TNT.getDefaultState(), 1F);
			GLS.doPolygonOffset(0F, 0F);
			GLS.disablePolygonOffset();
			GLS.color(1F, 1F, 1F, 1F);
			GLS.disableBlend();
			GLS.enableLighting();
			GLS.enableTexture2D();
		}

		GLS.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(EntityTNTPrimed entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

}
