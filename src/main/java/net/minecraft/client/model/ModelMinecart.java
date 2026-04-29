package net.minecraft.client.model;

import net.minecraft.entity.Entity;

public class ModelMinecart extends ModelBase {

	public ModelRenderer[] sideModels = new ModelRenderer[7];

	public ModelMinecart() {

		sideModels[0] = new ModelRenderer(this, 0, 10);
		sideModels[1] = new ModelRenderer(this, 0, 0);
		sideModels[2] = new ModelRenderer(this, 0, 0);
		sideModels[3] = new ModelRenderer(this, 0, 0);
		sideModels[4] = new ModelRenderer(this, 0, 0);
		sideModels[5] = new ModelRenderer(this, 44, 10);
		int i = 20;
		int j = 8;
		int k = 16;
		int l = 4;
		sideModels[0].addBox(-10F, -8F, -1F, 20, 16, 2, 0F);
		sideModels[0].setRotationPoint(0F, 4F, 0F);
		sideModels[5].addBox(-9F, -7F, -1F, 18, 14, 1, 0F);
		sideModels[5].setRotationPoint(0F, 4F, 0F);
		sideModels[1].addBox(-8F, -9F, -1F, 16, 8, 2, 0F);
		sideModels[1].setRotationPoint(-9F, 4F, 0F);
		sideModels[2].addBox(-8F, -9F, -1F, 16, 8, 2, 0F);
		sideModels[2].setRotationPoint(9F, 4F, 0F);
		sideModels[3].addBox(-8F, -9F, -1F, 16, 8, 2, 0F);
		sideModels[3].setRotationPoint(0F, 4F, -7F);
		sideModels[4].addBox(-8F, -9F, -1F, 16, 8, 2, 0F);
		sideModels[4].setRotationPoint(0F, 4F, 7F);
		sideModels[0].rotateAngleX = ((float) Math.PI / 2F);
		sideModels[1].rotateAngleY = ((float) Math.PI * 3F / 2F);
		sideModels[2].rotateAngleY = ((float) Math.PI / 2F);
		sideModels[3].rotateAngleY = (float) Math.PI;
		sideModels[5].rotateAngleX = -((float) Math.PI / 2F);
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		sideModels[5].rotationPointY = 4F - ageInTicks;

		for (int i = 0; i < 6; ++i) {
			sideModels[i].render(scale);
		}
	}

}
