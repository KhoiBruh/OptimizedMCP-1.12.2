package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

public class ModelBiped extends ModelBase {

	public ModelRenderer bipedHead;

	/**
	 * The Biped's Headwear. Used for the outer layer of player skins.
	 */
	public ModelRenderer bipedHeadwear;
	public ModelRenderer bipedBody;

	/**
	 * The Biped's Right Arm
	 */
	public ModelRenderer bipedRightArm;

	/**
	 * The Biped's Left Arm
	 */
	public ModelRenderer bipedLeftArm;

	/**
	 * The Biped's Right Leg
	 */
	public ModelRenderer bipedRightLeg;

	/**
	 * The Biped's Left Leg
	 */
	public ModelRenderer bipedLeftLeg;
	public ModelBiped.ArmPose leftArmPose;
	public ModelBiped.ArmPose rightArmPose;
	public boolean isSneak;

	public ModelBiped() {

		this(0F);
	}

	public ModelBiped(float modelSize) {

		this(modelSize, 0F, 64, 32);
	}

	public ModelBiped(float modelSize, float p_i1149_2_, int textureWidthIn, int textureHeightIn) {

		leftArmPose = ModelBiped.ArmPose.EMPTY;
		rightArmPose = ModelBiped.ArmPose.EMPTY;
		textureWidth = textureWidthIn;
		textureHeight = textureHeightIn;
		bipedHead = new ModelRenderer(this, 0, 0);
		bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8, modelSize);
		bipedHead.setRotationPoint(0F, 0F + p_i1149_2_, 0F);
		bipedHeadwear = new ModelRenderer(this, 32, 0);
		bipedHeadwear.addBox(-4F, -8F, -4F, 8, 8, 8, modelSize + 0.5F);
		bipedHeadwear.setRotationPoint(0F, 0F + p_i1149_2_, 0F);
		bipedBody = new ModelRenderer(this, 16, 16);
		bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4, modelSize);
		bipedBody.setRotationPoint(0F, 0F + p_i1149_2_, 0F);
		bipedRightArm = new ModelRenderer(this, 40, 16);
		bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4, modelSize);
		bipedRightArm.setRotationPoint(-5F, 2F + p_i1149_2_, 0F);
		bipedLeftArm = new ModelRenderer(this, 40, 16);
		bipedLeftArm.mirror = true;
		bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4, modelSize);
		bipedLeftArm.setRotationPoint(5F, 2F + p_i1149_2_, 0F);
		bipedRightLeg = new ModelRenderer(this, 0, 16);
		bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4, modelSize);
		bipedRightLeg.setRotationPoint(-1.9F, 12F + p_i1149_2_, 0F);
		bipedLeftLeg = new ModelRenderer(this, 0, 16);
		bipedLeftLeg.mirror = true;
		bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4, modelSize);
		bipedLeftLeg.setRotationPoint(1.9F, 12F + p_i1149_2_, 0F);
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
		GlStateManager.pushMatrix();

		if (isChild) {
			float f = 2F;
			GlStateManager.scale(0.75F, 0.75F, 0.75F);
			GlStateManager.translate(0F, 16F * scale, 0F);
			bipedHead.render(scale);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0F, 24F * scale, 0F);
			bipedBody.render(scale);
			bipedRightArm.render(scale);
			bipedLeftArm.render(scale);
			bipedRightLeg.render(scale);
			bipedLeftLeg.render(scale);
			bipedHeadwear.render(scale);
		} else {
			if (entityIn.isSneaking()) {
				GlStateManager.translate(0F, 0.2F, 0F);
			}

			bipedHead.render(scale);
			bipedBody.render(scale);
			bipedRightArm.render(scale);
			bipedLeftArm.render(scale);
			bipedRightLeg.render(scale);
			bipedLeftLeg.render(scale);
			bipedHeadwear.render(scale);
		}

		GlStateManager.popMatrix();
	}

	@SuppressWarnings("incomplete-switch")

	/**
	 * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
	 * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
	 * "far" arms and legs can swing at most.
	 */
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {

		boolean flag = entityIn instanceof EntityLivingBase && ((EntityLivingBase) entityIn).getTicksElytraFlying() > 4;
		bipedHead.rotateAngleY = netHeadYaw * 0.017453292F;

		if (flag) {
			bipedHead.rotateAngleX = -((float) Math.PI / 4F);
		} else {
			bipedHead.rotateAngleX = headPitch * 0.017453292F;
		}

		bipedBody.rotateAngleY = 0F;
		bipedRightArm.rotationPointZ = 0F;
		bipedRightArm.rotationPointX = -5F;
		bipedLeftArm.rotationPointZ = 0F;
		bipedLeftArm.rotationPointX = 5F;
		float f = 1F;

		if (flag) {
			f = (float) (entityIn.motionX * entityIn.motionX + entityIn.motionY * entityIn.motionY + entityIn.motionZ * entityIn.motionZ);
			f = f / 0.2F;
			f = f * f * f;
		}

		if (f < 1F) {
			f = 1F;
		}

		bipedRightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 2F * limbSwingAmount * 0.5F / f;
		bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2F * limbSwingAmount * 0.5F / f;
		bipedRightArm.rotateAngleZ = 0F;
		bipedLeftArm.rotateAngleZ = 0F;
		bipedRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
		bipedLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount / f;
		bipedRightLeg.rotateAngleY = 0F;
		bipedLeftLeg.rotateAngleY = 0F;
		bipedRightLeg.rotateAngleZ = 0F;
		bipedLeftLeg.rotateAngleZ = 0F;

		if (isRiding) {
			bipedRightArm.rotateAngleX -= ((float) Math.PI / 5F);
			bipedLeftArm.rotateAngleX -= ((float) Math.PI / 5F);
			bipedRightLeg.rotateAngleX = -1.4137167F;
			bipedRightLeg.rotateAngleY = ((float) Math.PI / 10F);
			bipedRightLeg.rotateAngleZ = 0.07853982F;
			bipedLeftLeg.rotateAngleX = -1.4137167F;
			bipedLeftLeg.rotateAngleY = -((float) Math.PI / 10F);
			bipedLeftLeg.rotateAngleZ = -0.07853982F;
		}

		bipedRightArm.rotateAngleY = 0F;
		bipedRightArm.rotateAngleZ = 0F;

		switch (leftArmPose) {
			case EMPTY:
				bipedLeftArm.rotateAngleY = 0F;
				break;

			case BLOCK:
				bipedLeftArm.rotateAngleX = bipedLeftArm.rotateAngleX * 0.5F - 0.9424779F;
				bipedLeftArm.rotateAngleY = 0.5235988F;
				break;

			case ITEM:
				bipedLeftArm.rotateAngleX = bipedLeftArm.rotateAngleX * 0.5F - ((float) Math.PI / 10F);
				bipedLeftArm.rotateAngleY = 0F;
		}

		switch (rightArmPose) {
			case EMPTY:
				bipedRightArm.rotateAngleY = 0F;
				break;

			case BLOCK:
				bipedRightArm.rotateAngleX = bipedRightArm.rotateAngleX * 0.5F - 0.9424779F;
				bipedRightArm.rotateAngleY = -0.5235988F;
				break;

			case ITEM:
				bipedRightArm.rotateAngleX = bipedRightArm.rotateAngleX * 0.5F - ((float) Math.PI / 10F);
				bipedRightArm.rotateAngleY = 0F;
		}

		if (swingProgress > 0F) {
			EnumHandSide enumhandside = getMainHand(entityIn);
			ModelRenderer modelrenderer = getArmForSide(enumhandside);
			float f1 = swingProgress;
			bipedBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt(f1) * ((float) Math.PI * 2F)) * 0.2F;

			if (enumhandside == EnumHandSide.LEFT) {
				bipedBody.rotateAngleY *= -1F;
			}

			bipedRightArm.rotationPointZ = MathHelper.sin(bipedBody.rotateAngleY) * 5F;
			bipedRightArm.rotationPointX = -MathHelper.cos(bipedBody.rotateAngleY) * 5F;
			bipedLeftArm.rotationPointZ = -MathHelper.sin(bipedBody.rotateAngleY) * 5F;
			bipedLeftArm.rotationPointX = MathHelper.cos(bipedBody.rotateAngleY) * 5F;
			bipedRightArm.rotateAngleY += bipedBody.rotateAngleY;
			bipedLeftArm.rotateAngleY += bipedBody.rotateAngleY;
			bipedLeftArm.rotateAngleX += bipedBody.rotateAngleY;
			f1 = 1F - swingProgress;
			f1 = f1 * f1;
			f1 = f1 * f1;
			f1 = 1F - f1;
			float f2 = MathHelper.sin(f1 * (float) Math.PI);
			float f3 = MathHelper.sin(swingProgress * (float) Math.PI) * -(bipedHead.rotateAngleX - 0.7F) * 0.75F;
			modelrenderer.rotateAngleX = (float) ((double) modelrenderer.rotateAngleX - ((double) f2 * 1.2D + (double) f3));
			modelrenderer.rotateAngleY += bipedBody.rotateAngleY * 2F;
			modelrenderer.rotateAngleZ += MathHelper.sin(swingProgress * (float) Math.PI) * -0.4F;
		}

		if (isSneak) {
			bipedBody.rotateAngleX = 0.5F;
			bipedRightArm.rotateAngleX += 0.4F;
			bipedLeftArm.rotateAngleX += 0.4F;
			bipedRightLeg.rotationPointZ = 4F;
			bipedLeftLeg.rotationPointZ = 4F;
			bipedRightLeg.rotationPointY = 9F;
			bipedLeftLeg.rotationPointY = 9F;
			bipedHead.rotationPointY = 1F;
		} else {
			bipedBody.rotateAngleX = 0F;
			bipedRightLeg.rotationPointZ = 0.1F;
			bipedLeftLeg.rotationPointZ = 0.1F;
			bipedRightLeg.rotationPointY = 12F;
			bipedLeftLeg.rotationPointY = 12F;
			bipedHead.rotationPointY = 0F;
		}

		bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
		bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
		bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
		bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;

		if (rightArmPose == ModelBiped.ArmPose.BOW_AND_ARROW) {
			bipedRightArm.rotateAngleY = -0.1F + bipedHead.rotateAngleY;
			bipedLeftArm.rotateAngleY = 0.1F + bipedHead.rotateAngleY + 0.4F;
			bipedRightArm.rotateAngleX = -((float) Math.PI / 2F) + bipedHead.rotateAngleX;
			bipedLeftArm.rotateAngleX = -((float) Math.PI / 2F) + bipedHead.rotateAngleX;
		} else if (leftArmPose == ModelBiped.ArmPose.BOW_AND_ARROW) {
			bipedRightArm.rotateAngleY = -0.1F + bipedHead.rotateAngleY - 0.4F;
			bipedLeftArm.rotateAngleY = 0.1F + bipedHead.rotateAngleY;
			bipedRightArm.rotateAngleX = -((float) Math.PI / 2F) + bipedHead.rotateAngleX;
			bipedLeftArm.rotateAngleX = -((float) Math.PI / 2F) + bipedHead.rotateAngleX;
		}

		copyModelAngles(bipedHead, bipedHeadwear);
	}

	public void setModelAttributes(ModelBase model) {

		super.setModelAttributes(model);

		if (model instanceof ModelBiped modelbiped) {
			leftArmPose = modelbiped.leftArmPose;
			rightArmPose = modelbiped.rightArmPose;
			isSneak = modelbiped.isSneak;
		}
	}

	public void setVisible(boolean visible) {

		bipedHead.showModel = visible;
		bipedHeadwear.showModel = visible;
		bipedBody.showModel = visible;
		bipedRightArm.showModel = visible;
		bipedLeftArm.showModel = visible;
		bipedRightLeg.showModel = visible;
		bipedLeftLeg.showModel = visible;
	}

	public void postRenderArm(float scale, EnumHandSide side) {

		getArmForSide(side).postRender(scale);
	}

	protected ModelRenderer getArmForSide(EnumHandSide side) {

		return side == EnumHandSide.LEFT ? bipedLeftArm : bipedRightArm;
	}

	protected EnumHandSide getMainHand(Entity entityIn) {

		if (entityIn instanceof EntityLivingBase entitylivingbase) {
			EnumHandSide enumhandside = entitylivingbase.getPrimaryHand();
			return entitylivingbase.swingingHand == EnumHand.MAIN_HAND ? enumhandside : enumhandside.opposite();
		} else {
			return EnumHandSide.RIGHT;
		}
	}

	public enum ArmPose {
		EMPTY,
		ITEM,
		BLOCK,
		BOW_AND_ARROW
	}

}
