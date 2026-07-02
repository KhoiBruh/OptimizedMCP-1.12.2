package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelLeashKnot;
import net.minecraft.client.renderer.GLS;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.util.ResourceLocation;

public class RenderLeashKnot extends Render<EntityLeashKnot> {

	private static final ResourceLocation LEASH_KNOT_TEXTURES = new ResourceLocation("textures/entity/lead_knot.png");
	private final ModelLeashKnot leashKnotModel = new ModelLeashKnot();

	public RenderLeashKnot(RenderManager renderManagerIn) {
		super(renderManagerIn);
	}

	/**
	 * Renders the desired {@code T} type Entity.
	 */
	public void doRender(EntityLeashKnot entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GLS.pushMatrix();
		GLS.disableCull();
		GLS.translate((float) x, (float) y, (float) z);
		float f = 0.0625F;
		GLS.enableRescaleNormal();
		GLS.scale(-1F, -1F, 1F);
		GLS.enableAlpha();
		bindEntityTexture(entity);

		if (renderOutlines) {
			GLS.enableColorMaterial();
			GLS.enableOutlineMode(getTeamColor(entity));
		}

		leashKnotModel.render(entity, 0F, 0F, 0F, 0F, 0F, 0.0625F);

		if (renderOutlines) {
			GLS.disableOutlineMode();
			GLS.disableColorMaterial();
		}

		GLS.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(EntityLeashKnot entity) {
		return LEASH_KNOT_TEXTURES;
	}

}
