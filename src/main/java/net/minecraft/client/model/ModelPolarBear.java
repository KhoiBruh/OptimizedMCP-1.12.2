package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityPolarBear;

public class ModelPolarBear extends ModelQuadruped {

	public ModelPolarBear() {

		super(12, 0F);
		textureWidth = 128;
		textureHeight = 64;
		head = new ModelRenderer(this, 0, 0);
		head.addBox(-3.5F, -3F, -3F, 7, 7, 7, 0F);
		head.setRotationPoint(0F, 10F, -16F);
		head.setTextureOffset(0, 44).addBox(-2.5F, 1F, -6F, 5, 3, 3, 0F);
		head.setTextureOffset(26, 0).addBox(-4.5F, -4F, -1F, 2, 2, 1, 0F);
		ModelRenderer modelrenderer = head.setTextureOffset(26, 0);
		modelrenderer.mirror = true;
		modelrenderer.addBox(2.5F, -4F, -1F, 2, 2, 1, 0F);
		body = new ModelRenderer(this);
		body.setTextureOffset(0, 19).addBox(-5F, -13F, -7F, 14, 14, 11, 0F);
		body.setTextureOffset(39, 0).addBox(-4F, -25F, -7F, 12, 12, 10, 0F);
		body.setRotationPoint(-2F, 9F, 12F);
		int i = 10;
		leg1 = new ModelRenderer(this, 50, 22);
		leg1.addBox(-2F, 0F, -2F, 4, 10, 8, 0F);
		leg1.setRotationPoint(-3.5F, 14F, 6F);
		leg2 = new ModelRenderer(this, 50, 22);
		leg2.addBox(-2F, 0F, -2F, 4, 10, 8, 0F);
		leg2.setRotationPoint(3.5F, 14F, 6F);
		leg3 = new ModelRenderer(this, 50, 40);
		leg3.addBox(-2F, 0F, -2F, 4, 10, 6, 0F);
		leg3.setRotationPoint(-2.5F, 14F, -7F);
		leg4 = new ModelRenderer(this, 50, 40);
		leg4.addBox(-2F, 0F, -2F, 4, 10, 6, 0F);
		leg4.setRotationPoint(2.5F, 14F, -7F);
		--leg1.rotationPointX;
		++leg2.rotationPointX;
		leg1.rotationPointZ += 0F;
		leg2.rotationPointZ += 0F;
		--leg3.rotationPointX;
		++leg4.rotationPointX;
		--leg3.rotationPointZ;
		--leg4.rotationPointZ;
		childZOffset += 2F;
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);

		if (isChild) {
			float f = 2F;
			childYOffset = 16F;
			childZOffset = 4F;
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.6666667F, 0.6666667F, 0.6666667F);
			GlStateManager.translate(0F, childYOffset * scale, childZOffset * scale);
			head.render(scale);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0F, 24F * scale, 0F);
			body.render(scale);
			leg1.render(scale);
			leg2.render(scale);
			leg3.render(scale);
			leg4.render(scale);
			GlStateManager.popMatrix();
		} else {
			head.render(scale);
			body.render(scale);
			leg1.render(scale);
			leg2.render(scale);
			leg3.render(scale);
			leg4.render(scale);
		}
	}

	/**
	 * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
	 * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
	 * "far" arms and legs can swing at most.
	 */
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {

		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		float f = ageInTicks - (float) entityIn.ticksExisted;
		float f1 = ((EntityPolarBear) entityIn).getStandingAnimationScale(f);
		f1 = f1 * f1;
		float f2 = 1F - f1;
		body.rotateAngleX = ((float) Math.PI / 2F) - f1 * (float) Math.PI * 0.35F;
		body.rotationPointY = 9F * f2 + 11F * f1;
		leg3.rotationPointY = 14F * f2 + -6F * f1;
		leg3.rotationPointZ = -8F * f2 + -4F * f1;
		leg3.rotateAngleX -= f1 * (float) Math.PI * 0.45F;
		leg4.rotationPointY = leg3.rotationPointY;
		leg4.rotationPointZ = leg3.rotationPointZ;
		leg4.rotateAngleX -= f1 * (float) Math.PI * 0.45F;
		head.rotationPointY = 10F * f2 + -12F * f1;
		head.rotationPointZ = -16F * f2 + -3F * f1;
		head.rotateAngleX += f1 * (float) Math.PI * 0.15F;
	}

}
