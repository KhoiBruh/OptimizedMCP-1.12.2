package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.AbstractIllager;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

public class ModelIllager extends ModelBase {

	public ModelRenderer head;
	public ModelRenderer hat;
	public ModelRenderer body;
	public ModelRenderer arms;
	public ModelRenderer leg0;
	public ModelRenderer leg1;
	public ModelRenderer nose;
	public ModelRenderer rightArm;
	public ModelRenderer leftArm;

	public ModelIllager(float scaleFactor, float p_i47227_2_, int textureWidthIn, int textureHeightIn) {

		head = (new ModelRenderer(this)).setTextureSize(textureWidthIn, textureHeightIn);
		head.setRotationPoint(0F, 0F + p_i47227_2_, 0F);
		head.setTextureOffset(0, 0).addBox(-4F, -10F, -4F, 8, 10, 8, scaleFactor);
		hat = (new ModelRenderer(this, 32, 0)).setTextureSize(textureWidthIn, textureHeightIn);
		hat.addBox(-4F, -10F, -4F, 8, 12, 8, scaleFactor + 0.45F);
		head.addChild(hat);
		hat.showModel = false;
		nose = (new ModelRenderer(this)).setTextureSize(textureWidthIn, textureHeightIn);
		nose.setRotationPoint(0F, p_i47227_2_ - 2F, 0F);
		nose.setTextureOffset(24, 0).addBox(-1F, -1F, -6F, 2, 4, 2, scaleFactor);
		head.addChild(nose);
		body = (new ModelRenderer(this)).setTextureSize(textureWidthIn, textureHeightIn);
		body.setRotationPoint(0F, 0F + p_i47227_2_, 0F);
		body.setTextureOffset(16, 20).addBox(-4F, 0F, -3F, 8, 12, 6, scaleFactor);
		body.setTextureOffset(0, 38).addBox(-4F, 0F, -3F, 8, 18, 6, scaleFactor + 0.5F);
		arms = (new ModelRenderer(this)).setTextureSize(textureWidthIn, textureHeightIn);
		arms.setRotationPoint(0F, 0F + p_i47227_2_ + 2F, 0F);
		arms.setTextureOffset(44, 22).addBox(-8F, -2F, -2F, 4, 8, 4, scaleFactor);
		ModelRenderer modelrenderer = (new ModelRenderer(this, 44, 22)).setTextureSize(textureWidthIn, textureHeightIn);
		modelrenderer.mirror = true;
		modelrenderer.addBox(4F, -2F, -2F, 4, 8, 4, scaleFactor);
		arms.addChild(modelrenderer);
		arms.setTextureOffset(40, 38).addBox(-4F, 2F, -2F, 8, 4, 4, scaleFactor);
		leg0 = (new ModelRenderer(this, 0, 22)).setTextureSize(textureWidthIn, textureHeightIn);
		leg0.setRotationPoint(-2F, 12F + p_i47227_2_, 0F);
		leg0.addBox(-2F, 0F, -2F, 4, 12, 4, scaleFactor);
		leg1 = (new ModelRenderer(this, 0, 22)).setTextureSize(textureWidthIn, textureHeightIn);
		leg1.mirror = true;
		leg1.setRotationPoint(2F, 12F + p_i47227_2_, 0F);
		leg1.addBox(-2F, 0F, -2F, 4, 12, 4, scaleFactor);
		rightArm = (new ModelRenderer(this, 40, 46)).setTextureSize(textureWidthIn, textureHeightIn);
		rightArm.addBox(-3F, -2F, -2F, 4, 12, 4, scaleFactor);
		rightArm.setRotationPoint(-5F, 2F + p_i47227_2_, 0F);
		leftArm = (new ModelRenderer(this, 40, 46)).setTextureSize(textureWidthIn, textureHeightIn);
		leftArm.mirror = true;
		leftArm.addBox(-1F, -2F, -2F, 4, 12, 4, scaleFactor);
		leftArm.setRotationPoint(5F, 2F + p_i47227_2_, 0F);
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
		head.render(scale);
		body.render(scale);
		leg0.render(scale);
		leg1.render(scale);
		AbstractIllager abstractillager = (AbstractIllager) entityIn;

		if (abstractillager.getArmPose() == AbstractIllager.IllagerArmPose.CROSSED) {
			arms.render(scale);
		} else {
			rightArm.render(scale);
			leftArm.render(scale);
		}
	}

	/**
	 * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
	 * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
	 * "far" arms and legs can swing at most.
	 */
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {

		head.rotateAngleY = netHeadYaw * 0.017453292F;
		head.rotateAngleX = headPitch * 0.017453292F;
		arms.rotationPointY = 3F;
		arms.rotationPointZ = -1F;
		arms.rotateAngleX = -0.75F;
		leg0.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * 0.5F;
		leg1.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount * 0.5F;
		leg0.rotateAngleY = 0F;
		leg1.rotateAngleY = 0F;
		AbstractIllager.IllagerArmPose abstractillager$illagerarmpose = ((AbstractIllager) entityIn).getArmPose();

		if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.ATTACKING) {
			float f = MathHelper.sin(swingProgress * (float) Math.PI);
			float f1 = MathHelper.sin((1F - (1F - swingProgress) * (1F - swingProgress)) * (float) Math.PI);
			rightArm.rotateAngleZ = 0F;
			leftArm.rotateAngleZ = 0F;
			rightArm.rotateAngleY = 0.15707964F;
			leftArm.rotateAngleY = -0.15707964F;

			if (((EntityLivingBase) entityIn).getPrimaryHand() == EnumHandSide.RIGHT) {
				rightArm.rotateAngleX = -1.8849558F + MathHelper.cos(ageInTicks * 0.09F) * 0.15F;
				leftArm.rotateAngleX = -0F + MathHelper.cos(ageInTicks * 0.19F) * 0.5F;
				rightArm.rotateAngleX += f * 2.2F - f1 * 0.4F;
				leftArm.rotateAngleX += f * 1.2F - f1 * 0.4F;
			} else {
				rightArm.rotateAngleX = -0F + MathHelper.cos(ageInTicks * 0.19F) * 0.5F;
				leftArm.rotateAngleX = -1.8849558F + MathHelper.cos(ageInTicks * 0.09F) * 0.15F;
				rightArm.rotateAngleX += f * 1.2F - f1 * 0.4F;
				leftArm.rotateAngleX += f * 2.2F - f1 * 0.4F;
			}

			rightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
			leftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
			rightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
			leftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
		} else if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.SPELLCASTING) {
			rightArm.rotationPointZ = 0F;
			rightArm.rotationPointX = -5F;
			leftArm.rotationPointZ = 0F;
			leftArm.rotationPointX = 5F;
			rightArm.rotateAngleX = MathHelper.cos(ageInTicks * 0.6662F) * 0.25F;
			leftArm.rotateAngleX = MathHelper.cos(ageInTicks * 0.6662F) * 0.25F;
			rightArm.rotateAngleZ = 2.3561945F;
			leftArm.rotateAngleZ = -2.3561945F;
			rightArm.rotateAngleY = 0F;
			leftArm.rotateAngleY = 0F;
		} else if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.BOW_AND_ARROW) {
			rightArm.rotateAngleY = -0.1F + head.rotateAngleY;
			rightArm.rotateAngleX = -((float) Math.PI / 2F) + head.rotateAngleX;
			leftArm.rotateAngleX = -0.9424779F + head.rotateAngleX;
			leftArm.rotateAngleY = head.rotateAngleY - 0.4F;
			leftArm.rotateAngleZ = ((float) Math.PI / 2F);
		}
	}

	public ModelRenderer getArm(EnumHandSide p_191216_1_) {

		return p_191216_1_ == EnumHandSide.LEFT ? leftArm : rightArm;
	}

}
