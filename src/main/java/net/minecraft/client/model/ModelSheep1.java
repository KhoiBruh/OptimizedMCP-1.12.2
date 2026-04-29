package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;

public class ModelSheep1 extends ModelQuadruped {

	private float headRotationAngleX;

	public ModelSheep1() {

		super(12, 0F);
		head = new ModelRenderer(this, 0, 0);
		head.addBox(-3F, -4F, -4F, 6, 6, 6, 0.6F);
		head.setRotationPoint(0F, 6F, -8F);
		body = new ModelRenderer(this, 28, 8);
		body.addBox(-4F, -10F, -7F, 8, 16, 6, 1.75F);
		body.setRotationPoint(0F, 5F, 2F);
		float f = 0.5F;
		leg1 = new ModelRenderer(this, 0, 16);
		leg1.addBox(-2F, 0F, -2F, 4, 6, 4, 0.5F);
		leg1.setRotationPoint(-3F, 12F, 7F);
		leg2 = new ModelRenderer(this, 0, 16);
		leg2.addBox(-2F, 0F, -2F, 4, 6, 4, 0.5F);
		leg2.setRotationPoint(3F, 12F, 7F);
		leg3 = new ModelRenderer(this, 0, 16);
		leg3.addBox(-2F, 0F, -2F, 4, 6, 4, 0.5F);
		leg3.setRotationPoint(-3F, 12F, -5F);
		leg4 = new ModelRenderer(this, 0, 16);
		leg4.addBox(-2F, 0F, -2F, 4, 6, 4, 0.5F);
		leg4.setRotationPoint(3F, 12F, -5F);
	}

	/**
	 * Used for easily adding entity-dependent animations. The second and third float params here are the same second
	 * and third as in the setRotationAngles method.
	 */
	public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTickTime) {

		super.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTickTime);
		head.rotationPointY = 6F + ((EntitySheep) entitylivingbaseIn).getHeadRotationPointY(partialTickTime) * 9F;
		headRotationAngleX = ((EntitySheep) entitylivingbaseIn).getHeadRotationAngleX(partialTickTime);
	}

	/**
	 * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
	 * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
	 * "far" arms and legs can swing at most.
	 */
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {

		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		head.rotateAngleX = headRotationAngleX;
	}

}
