package net.minecraft.client.model;

import net.minecraft.entity.Entity;

public class ModelLlamaSpit extends ModelBase {

	private final ModelRenderer main;

	public ModelLlamaSpit() {

		this(0F);
	}

	public ModelLlamaSpit(float p_i47225_1_) {

		main = new ModelRenderer(this);
		int i = 2;
		main.setTextureOffset(0, 0).addBox(-4F, 0F, 0F, 2, 2, 2, p_i47225_1_);
		main.setTextureOffset(0, 0).addBox(0F, -4F, 0F, 2, 2, 2, p_i47225_1_);
		main.setTextureOffset(0, 0).addBox(0F, 0F, -4F, 2, 2, 2, p_i47225_1_);
		main.setTextureOffset(0, 0).addBox(0F, 0F, 0F, 2, 2, 2, p_i47225_1_);
		main.setTextureOffset(0, 0).addBox(2F, 0F, 0F, 2, 2, 2, p_i47225_1_);
		main.setTextureOffset(0, 0).addBox(0F, 2F, 0F, 2, 2, 2, p_i47225_1_);
		main.setTextureOffset(0, 0).addBox(0F, 0F, 2F, 2, 2, 2, p_i47225_1_);
		main.setRotationPoint(0F, 0F, 0F);
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
		main.render(scale);
	}

}
