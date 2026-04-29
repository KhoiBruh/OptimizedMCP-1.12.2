package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelChicken extends ModelBase {

	public ModelRenderer head;
	public ModelRenderer body;
	public ModelRenderer rightLeg;
	public ModelRenderer leftLeg;
	public ModelRenderer rightWing;
	public ModelRenderer leftWing;
	public ModelRenderer bill;
	public ModelRenderer chin;

	public ModelChicken() {

		int i = 16;
		head = new ModelRenderer(this, 0, 0);
		head.addBox(-2F, -6F, -2F, 4, 6, 3, 0F);
		head.setRotationPoint(0F, 15F, -4F);
		bill = new ModelRenderer(this, 14, 0);
		bill.addBox(-2F, -4F, -4F, 4, 2, 2, 0F);
		bill.setRotationPoint(0F, 15F, -4F);
		chin = new ModelRenderer(this, 14, 4);
		chin.addBox(-1F, -2F, -3F, 2, 2, 2, 0F);
		chin.setRotationPoint(0F, 15F, -4F);
		body = new ModelRenderer(this, 0, 9);
		body.addBox(-3F, -4F, -3F, 6, 8, 6, 0F);
		body.setRotationPoint(0F, 16F, 0F);
		rightLeg = new ModelRenderer(this, 26, 0);
		rightLeg.addBox(-1F, 0F, -3F, 3, 5, 3);
		rightLeg.setRotationPoint(-2F, 19F, 1F);
		leftLeg = new ModelRenderer(this, 26, 0);
		leftLeg.addBox(-1F, 0F, -3F, 3, 5, 3);
		leftLeg.setRotationPoint(1F, 19F, 1F);
		rightWing = new ModelRenderer(this, 24, 13);
		rightWing.addBox(0F, 0F, -3F, 1, 4, 6);
		rightWing.setRotationPoint(-4F, 13F, 0F);
		leftWing = new ModelRenderer(this, 24, 13);
		leftWing.addBox(-1F, 0F, -3F, 1, 4, 6);
		leftWing.setRotationPoint(4F, 13F, 0F);
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);

		if (isChild) {
			float f = 2F;
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, 5F * scale, 2F * scale);
			head.render(scale);
			bill.render(scale);
			chin.render(scale);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0F, 24F * scale, 0F);
			body.render(scale);
			rightLeg.render(scale);
			leftLeg.render(scale);
			rightWing.render(scale);
			leftWing.render(scale);
			GlStateManager.popMatrix();
		} else {
			head.render(scale);
			bill.render(scale);
			chin.render(scale);
			body.render(scale);
			rightLeg.render(scale);
			leftLeg.render(scale);
			rightWing.render(scale);
			leftWing.render(scale);
		}
	}

	/**
	 * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
	 * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
	 * "far" arms and legs can swing at most.
	 */
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {

		head.rotateAngleX = headPitch * 0.017453292F;
		head.rotateAngleY = netHeadYaw * 0.017453292F;
		bill.rotateAngleX = head.rotateAngleX;
		bill.rotateAngleY = head.rotateAngleY;
		chin.rotateAngleX = head.rotateAngleX;
		chin.rotateAngleY = head.rotateAngleY;
		body.rotateAngleX = ((float) Math.PI / 2F);
		rightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		leftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		rightWing.rotateAngleZ = ageInTicks;
		leftWing.rotateAngleZ = -ageInTicks;
	}

}
