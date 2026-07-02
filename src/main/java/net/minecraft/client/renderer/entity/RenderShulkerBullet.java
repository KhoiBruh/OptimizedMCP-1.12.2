package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelShulkerBullet;
import net.minecraft.client.renderer.GLS;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Maths;

public class RenderShulkerBullet extends Render<EntityShulkerBullet> {

	private static final ResourceLocation SHULKER_SPARK_TEXTURE = new ResourceLocation("textures/entity/shulker/spark.png");
	private final ModelShulkerBullet model = new ModelShulkerBullet();

	public RenderShulkerBullet(RenderManager manager) {
		super(manager);
	}

	private float rotLerp(float p_188347_1_, float p_188347_2_, float p_188347_3_) {
		float f;

		for (f = p_188347_2_ - p_188347_1_; f < -180F; f += 360F) {
		}

		while (f >= 180F) {
			f -= 360F;
		}

		return p_188347_1_ + p_188347_3_ * f;
	}

	/**
	 * Renders the desired {@code T} type Entity.
	 */
	public void doRender(EntityShulkerBullet entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GLS.pushMatrix();
		float f = rotLerp(entity.prevRotationYaw, entity.rotationYaw, partialTicks);
		float f1 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
		float f2 = (float) entity.ticksExisted + partialTicks;
		GLS.translate((float) x, (float) y + 0.15F, (float) z);
		GLS.rotate(Maths.sin(f2 * 0.1F) * 180F, 0F, 1F, 0F);
		GLS.rotate(Maths.cos(f2 * 0.1F) * 180F, 1F, 0F, 0F);
		GLS.rotate(Maths.sin(f2 * 0.15F) * 360F, 0F, 0F, 1F);
		float f3 = 0.03125F;
		GLS.enableRescaleNormal();
		GLS.scale(-1F, -1F, 1F);
		bindEntityTexture(entity);
		model.render(entity, 0F, 0F, 0F, f, f1, 0.03125F);
		GLS.enableBlend();
		GLS.color(1F, 1F, 1F, 0.5F);
		GLS.scale(1.5F, 1.5F, 1.5F);
		model.render(entity, 0F, 0F, 0F, f, f1, 0.03125F);
		GLS.disableBlend();
		GLS.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(EntityShulkerBullet entity) {
		return SHULKER_SPARK_TEXTURE;
	}

}
