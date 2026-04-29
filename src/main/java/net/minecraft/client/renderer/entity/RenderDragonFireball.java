package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.util.ResourceLocation;

public class RenderDragonFireball extends Render<EntityDragonFireball> {

	private static final ResourceLocation DRAGON_FIREBALL_TEXTURE = new ResourceLocation("textures/entity/enderdragon/dragon_fireball.png");

	public RenderDragonFireball(RenderManager renderManagerIn) {

		super(renderManagerIn);
	}

	/**
	 * Renders the desired {@code T} type Entity.
	 */
	public void doRender(EntityDragonFireball entity, double x, double y, double z, float entityYaw, float partialTicks) {

		GlStateManager.pushMatrix();
		bindEntityTexture(entity);
		GlStateManager.translate((float) x, (float) y, (float) z);
		GlStateManager.enableRescaleNormal();
		GlStateManager.scale(2F, 2F, 2F);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		float f = 1F;
		float f1 = 0.5F;
		float f2 = 0.25F;
		GlStateManager.rotate(180F - renderManager.playerViewY, 0F, 1F, 0F);
		GlStateManager.rotate((float) (renderManager.options.thirdPersonView == 2 ? -1 : 1) * -renderManager.playerViewX, 1F, 0F, 0F);

		if (renderOutlines) {
			GlStateManager.enableColorMaterial();
			GlStateManager.enableOutlineMode(getTeamColor(entity));
		}

		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
		bufferbuilder.pos(-0.5D, -0.25D, 0D).tex(0D, 1D).normal(0F, 1F, 0F).endVertex();
		bufferbuilder.pos(0.5D, -0.25D, 0D).tex(1D, 1D).normal(0F, 1F, 0F).endVertex();
		bufferbuilder.pos(0.5D, 0.75D, 0D).tex(1D, 0D).normal(0F, 1F, 0F).endVertex();
		bufferbuilder.pos(-0.5D, 0.75D, 0D).tex(0D, 0D).normal(0F, 1F, 0F).endVertex();
		tessellator.draw();

		if (renderOutlines) {
			GlStateManager.disableOutlineMode();
			GlStateManager.disableColorMaterial();
		}

		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(EntityDragonFireball entity) {

		return DRAGON_FIREBALL_TEXTURE;
	}

}
