package net.minecraft.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

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
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y + 0.5F, (float) z);

		if ((float) entity.getFuse() - partialTicks + 1F < 10F) {
			float f = 1F - ((float) entity.getFuse() - partialTicks + 1F) / 10F;
			f = MathHelper.clamp(f, 0F, 1F);
			f = f * f;
			f = f * f;
			float f1 = 1F + f * 0.3F;
			GlStateManager.scale(f1, f1, f1);
		}

		float f2 = (1F - ((float) entity.getFuse() - partialTicks + 1F) / 100F) * 0.8F;
		bindEntityTexture(entity);
		GlStateManager.rotate(-90F, 0F, 1F, 0F);
		GlStateManager.translate(-0.5F, -0.5F, 0.5F);
		blockrendererdispatcher.renderBlockBrightness(Blocks.TNT.getDefaultState(), entity.getBrightness());
		GlStateManager.translate(0F, 0F, 1F);

		if (renderOutlines) {
			GlStateManager.enableColorMaterial();
			GlStateManager.enableOutlineMode(getTeamColor(entity));
			blockrendererdispatcher.renderBlockBrightness(Blocks.TNT.getDefaultState(), 1F);
			GlStateManager.disableOutlineMode();
			GlStateManager.disableColorMaterial();
		} else if (entity.getFuse() / 5 % 2 == 0) {
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
			GlStateManager.color(1F, 1F, 1F, f2);
			GlStateManager.doPolygonOffset(-3F, -3F);
			GlStateManager.enablePolygonOffset();
			blockrendererdispatcher.renderBlockBrightness(Blocks.TNT.getDefaultState(), 1F);
			GlStateManager.doPolygonOffset(0F, 0F);
			GlStateManager.disablePolygonOffset();
			GlStateManager.color(1F, 1F, 1F, 1F);
			GlStateManager.disableBlend();
			GlStateManager.enableLighting();
			GlStateManager.enableTexture2D();
		}

		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(EntityTNTPrimed entity) {

		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

}
