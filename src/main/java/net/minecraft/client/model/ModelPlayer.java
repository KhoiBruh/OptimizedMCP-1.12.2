package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumHandSide;

public class ModelPlayer extends ModelBiped {

	private final ModelRenderer bipedCape;
	private final ModelRenderer bipedDeadmau5Head;
	private final boolean smallArms;
	public ModelRenderer bipedLeftArmwear;
	public ModelRenderer bipedRightArmwear;
	public ModelRenderer bipedLeftLegwear;
	public ModelRenderer bipedRightLegwear;
	public ModelRenderer bipedBodyWear;

	public ModelPlayer(float modelSize, boolean smallArmsIn) {

		super(modelSize, 0F, 64, 64);
		smallArms = smallArmsIn;
		bipedDeadmau5Head = new ModelRenderer(this, 24, 0);
		bipedDeadmau5Head.addBox(-3F, -6F, -1F, 6, 6, 1, modelSize);
		bipedCape = new ModelRenderer(this, 0, 0);
		bipedCape.setTextureSize(64, 32);
		bipedCape.addBox(-5F, 0F, -1F, 10, 16, 1, modelSize);

		if (smallArmsIn) {
			bipedLeftArm = new ModelRenderer(this, 32, 48);
			bipedLeftArm.addBox(-1F, -2F, -2F, 3, 12, 4, modelSize);
			bipedLeftArm.setRotationPoint(5F, 2.5F, 0F);
			bipedRightArm = new ModelRenderer(this, 40, 16);
			bipedRightArm.addBox(-2F, -2F, -2F, 3, 12, 4, modelSize);
			bipedRightArm.setRotationPoint(-5F, 2.5F, 0F);
			bipedLeftArmwear = new ModelRenderer(this, 48, 48);
			bipedLeftArmwear.addBox(-1F, -2F, -2F, 3, 12, 4, modelSize + 0.25F);
			bipedLeftArmwear.setRotationPoint(5F, 2.5F, 0F);
			bipedRightArmwear = new ModelRenderer(this, 40, 32);
			bipedRightArmwear.addBox(-2F, -2F, -2F, 3, 12, 4, modelSize + 0.25F);
			bipedRightArmwear.setRotationPoint(-5F, 2.5F, 10F);
		} else {
			bipedLeftArm = new ModelRenderer(this, 32, 48);
			bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4, modelSize);
			bipedLeftArm.setRotationPoint(5F, 2F, 0F);
			bipedLeftArmwear = new ModelRenderer(this, 48, 48);
			bipedLeftArmwear.addBox(-1F, -2F, -2F, 4, 12, 4, modelSize + 0.25F);
			bipedLeftArmwear.setRotationPoint(5F, 2F, 0F);
			bipedRightArmwear = new ModelRenderer(this, 40, 32);
			bipedRightArmwear.addBox(-3F, -2F, -2F, 4, 12, 4, modelSize + 0.25F);
			bipedRightArmwear.setRotationPoint(-5F, 2F, 10F);
		}

		bipedLeftLeg = new ModelRenderer(this, 16, 48);
		bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4, modelSize);
		bipedLeftLeg.setRotationPoint(1.9F, 12F, 0F);
		bipedLeftLegwear = new ModelRenderer(this, 0, 48);
		bipedLeftLegwear.addBox(-2F, 0F, -2F, 4, 12, 4, modelSize + 0.25F);
		bipedLeftLegwear.setRotationPoint(1.9F, 12F, 0F);
		bipedRightLegwear = new ModelRenderer(this, 0, 32);
		bipedRightLegwear.addBox(-2F, 0F, -2F, 4, 12, 4, modelSize + 0.25F);
		bipedRightLegwear.setRotationPoint(-1.9F, 12F, 0F);
		bipedBodyWear = new ModelRenderer(this, 16, 32);
		bipedBodyWear.addBox(-4F, 0F, -2F, 8, 12, 4, modelSize + 0.25F);
		bipedBodyWear.setRotationPoint(0F, 0F, 0F);
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		GlStateManager.pushMatrix();

		if (isChild) {
			float f = 2F;
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0F, 24F * scale, 0F);
			bipedLeftLegwear.render(scale);
			bipedRightLegwear.render(scale);
			bipedLeftArmwear.render(scale);
			bipedRightArmwear.render(scale);
			bipedBodyWear.render(scale);
		} else {
			if (entityIn.isSneaking()) {
				GlStateManager.translate(0F, 0.2F, 0F);
			}

			bipedLeftLegwear.render(scale);
			bipedRightLegwear.render(scale);
			bipedLeftArmwear.render(scale);
			bipedRightArmwear.render(scale);
			bipedBodyWear.render(scale);
		}

		GlStateManager.popMatrix();
	}

	public void renderDeadmau5Head(float scale) {

		copyModelAngles(bipedHead, bipedDeadmau5Head);
		bipedDeadmau5Head.rotationPointX = 0F;
		bipedDeadmau5Head.rotationPointY = 0F;
		bipedDeadmau5Head.render(scale);
	}

	public void renderCape(float scale) {

		bipedCape.render(scale);
	}

	/**
	 * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
	 * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
	 * "far" arms and legs can swing at most.
	 */
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {

		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		copyModelAngles(bipedLeftLeg, bipedLeftLegwear);
		copyModelAngles(bipedRightLeg, bipedRightLegwear);
		copyModelAngles(bipedLeftArm, bipedLeftArmwear);
		copyModelAngles(bipedRightArm, bipedRightArmwear);
		copyModelAngles(bipedBody, bipedBodyWear);

		if (entityIn.isSneaking()) {
			bipedCape.rotationPointY = 2F;
		} else {
			bipedCape.rotationPointY = 0F;
		}
	}

	public void setVisible(boolean visible) {

		super.setVisible(visible);
		bipedLeftArmwear.showModel = visible;
		bipedRightArmwear.showModel = visible;
		bipedLeftLegwear.showModel = visible;
		bipedRightLegwear.showModel = visible;
		bipedBodyWear.showModel = visible;
		bipedCape.showModel = visible;
		bipedDeadmau5Head.showModel = visible;
	}

	public void postRenderArm(float scale, EnumHandSide side) {

		ModelRenderer modelrenderer = getArmForSide(side);

		if (smallArms) {
			float f = 0.5F * (float) (side == EnumHandSide.RIGHT ? 1 : -1);
			modelrenderer.rotationPointX += f;
			modelrenderer.postRender(scale);
			modelrenderer.rotationPointX -= f;
		} else {
			modelrenderer.postRender(scale);
		}
	}

}
