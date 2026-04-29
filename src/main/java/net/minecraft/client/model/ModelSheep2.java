package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;

public class ModelSheep2 extends ModelQuadruped {

	private float headRotationAngleX;

	public ModelSheep2() {

		super(12, 0F);
		head = new ModelRenderer(this, 0, 0);
		head.addBox(-3F, -4F, -6F, 6, 6, 8, 0F);
		head.setRotationPoint(0F, 6F, -8F);
		body = new ModelRenderer(this, 28, 8);
		body.addBox(-4F, -10F, -7F, 8, 16, 6, 0F);
		body.setRotationPoint(0F, 5F, 2F);
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
