package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractChestHorse;

public class ModelLlama extends ModelQuadruped {

	private final ModelRenderer chest1;
	private final ModelRenderer chest2;

	public ModelLlama(float p_i47226_1_) {

		super(15, p_i47226_1_);
		textureWidth = 128;
		textureHeight = 64;
		head = new ModelRenderer(this, 0, 0);
		head.addBox(-2.0F, -14.0F, -10.0F, 4, 4, 9, p_i47226_1_);
		head.setRotationPoint(0.0F, 7.0F, -6.0F);
		head.setTextureOffset(0, 14).addBox(-4.0F, -16.0F, -6.0F, 8, 18, 6, p_i47226_1_);
		head.setTextureOffset(17, 0).addBox(-4.0F, -19.0F, -4.0F, 3, 3, 2, p_i47226_1_);
		head.setTextureOffset(17, 0).addBox(1.0F, -19.0F, -4.0F, 3, 3, 2, p_i47226_1_);
		body = new ModelRenderer(this, 29, 0);
		body.addBox(-6.0F, -10.0F, -7.0F, 12, 18, 10, p_i47226_1_);
		body.setRotationPoint(0.0F, 5.0F, 2.0F);
		chest1 = new ModelRenderer(this, 45, 28);
		chest1.addBox(-3.0F, 0.0F, 0.0F, 8, 8, 3, p_i47226_1_);
		chest1.setRotationPoint(-8.5F, 3.0F, 3.0F);
		chest1.rotateAngleY = ((float) Math.PI / 2F);
		chest2 = new ModelRenderer(this, 45, 41);
		chest2.addBox(-3.0F, 0.0F, 0.0F, 8, 8, 3, p_i47226_1_);
		chest2.setRotationPoint(5.5F, 3.0F, 3.0F);
		chest2.rotateAngleY = ((float) Math.PI / 2F);
		int i = 4;
		int j = 14;
		leg1 = new ModelRenderer(this, 29, 29);
		leg1.addBox(-2.0F, 0.0F, -2.0F, 4, 14, 4, p_i47226_1_);
		leg1.setRotationPoint(-2.5F, 10.0F, 6.0F);
		leg2 = new ModelRenderer(this, 29, 29);
		leg2.addBox(-2.0F, 0.0F, -2.0F, 4, 14, 4, p_i47226_1_);
		leg2.setRotationPoint(2.5F, 10.0F, 6.0F);
		leg3 = new ModelRenderer(this, 29, 29);
		leg3.addBox(-2.0F, 0.0F, -2.0F, 4, 14, 4, p_i47226_1_);
		leg3.setRotationPoint(-2.5F, 10.0F, -4.0F);
		leg4 = new ModelRenderer(this, 29, 29);
		leg4.addBox(-2.0F, 0.0F, -2.0F, 4, 14, 4, p_i47226_1_);
		leg4.setRotationPoint(2.5F, 10.0F, -4.0F);
		--leg1.rotationPointX;
		++leg2.rotationPointX;
		leg1.rotationPointZ += 0.0F;
		leg2.rotationPointZ += 0.0F;
		--leg3.rotationPointX;
		++leg4.rotationPointX;
		--leg3.rotationPointZ;
		--leg4.rotationPointZ;
		childZOffset += 2.0F;
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		AbstractChestHorse abstractchesthorse = (AbstractChestHorse) entityIn;
		boolean flag = !abstractchesthorse.isChild() && abstractchesthorse.hasChest();
		setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);

		if (isChild) {
			float f = 2.0F;
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0F, childYOffset * scale, childZOffset * scale);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			float f1 = 0.7F;
			GlStateManager.scale(0.71428573F, 0.64935064F, 0.7936508F);
			GlStateManager.translate(0.0F, 21.0F * scale, 0.22F);
			head.render(scale);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			float f2 = 1.1F;
			GlStateManager.scale(0.625F, 0.45454544F, 0.45454544F);
			GlStateManager.translate(0.0F, 33.0F * scale, 0.0F);
			body.render(scale);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.45454544F, 0.41322312F, 0.45454544F);
			GlStateManager.translate(0.0F, 33.0F * scale, 0.0F);
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

		if (flag) {
			chest1.render(scale);
			chest2.render(scale);
		}
	}

}
