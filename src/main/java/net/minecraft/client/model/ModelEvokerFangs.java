package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelEvokerFangs extends ModelBase {

	private final ModelRenderer base = new ModelRenderer(this, 0, 0);
	private final ModelRenderer upperJaw;
	private final ModelRenderer lowerJaw;

	public ModelEvokerFangs() {

		base.setRotationPoint(-5F, 22F, -5F);
		base.addBox(0F, 0F, 0F, 10, 12, 10);
		upperJaw = new ModelRenderer(this, 40, 0);
		upperJaw.setRotationPoint(1.5F, 22F, -4F);
		upperJaw.addBox(0F, 0F, 0F, 4, 14, 8);
		lowerJaw = new ModelRenderer(this, 40, 0);
		lowerJaw.setRotationPoint(-1.5F, 22F, 4F);
		lowerJaw.addBox(0F, 0F, 0F, 4, 14, 8);
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		float f = limbSwing * 2F;

		if (f > 1F) {
			f = 1F;
		}

		f = 1F - f * f * f;
		upperJaw.rotateAngleZ = (float) Math.PI - f * 0.35F * (float) Math.PI;
		lowerJaw.rotateAngleZ = (float) Math.PI + f * 0.35F * (float) Math.PI;
		lowerJaw.rotateAngleY = (float) Math.PI;
		float f1 = (limbSwing + MathHelper.sin(limbSwing * 2.7F)) * 0.6F * 12F;
		upperJaw.rotationPointY = 24F - f1;
		lowerJaw.rotationPointY = upperJaw.rotationPointY;
		base.rotationPointY = upperJaw.rotationPointY;
		base.render(scale);
		upperJaw.render(scale);
		lowerJaw.render(scale);
	}

}
