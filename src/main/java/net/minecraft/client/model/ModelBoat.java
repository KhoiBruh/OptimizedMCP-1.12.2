package net.minecraft.client.model;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.math.MathHelper;

public class ModelBoat extends ModelBase implements IMultipassModel {

	private final int patchList = GLAllocation.generateDisplayLists(1);
	public ModelRenderer[] boatSides = new ModelRenderer[5];
	public ModelRenderer[] paddles = new ModelRenderer[2];
	/**
	 * An invisible layer that is rendered to make it seem like there's no water in the boat.
	 *
	 * @see https://redd.it/3qufgo
	 * @see https://bugs.mojang.com/browse/MC-47636
	 */
	public ModelRenderer noWater;

	public ModelBoat() {

		boatSides[0] = (new ModelRenderer(this, 0, 0)).setTextureSize(128, 64);
		boatSides[1] = (new ModelRenderer(this, 0, 19)).setTextureSize(128, 64);
		boatSides[2] = (new ModelRenderer(this, 0, 27)).setTextureSize(128, 64);
		boatSides[3] = (new ModelRenderer(this, 0, 35)).setTextureSize(128, 64);
		boatSides[4] = (new ModelRenderer(this, 0, 43)).setTextureSize(128, 64);
		int i = 32;
		int j = 6;
		int k = 20;
		int l = 4;
		int i1 = 28;
		boatSides[0].addBox(-14F, -9F, -3F, 28, 16, 3, 0F);
		boatSides[0].setRotationPoint(0F, 3F, 1F);
		boatSides[1].addBox(-13F, -7F, -1F, 18, 6, 2, 0F);
		boatSides[1].setRotationPoint(-15F, 4F, 4F);
		boatSides[2].addBox(-8F, -7F, -1F, 16, 6, 2, 0F);
		boatSides[2].setRotationPoint(15F, 4F, 0F);
		boatSides[3].addBox(-14F, -7F, -1F, 28, 6, 2, 0F);
		boatSides[3].setRotationPoint(0F, 4F, -9F);
		boatSides[4].addBox(-14F, -7F, -1F, 28, 6, 2, 0F);
		boatSides[4].setRotationPoint(0F, 4F, 9F);
		boatSides[0].rotateAngleX = ((float) Math.PI / 2F);
		boatSides[1].rotateAngleY = ((float) Math.PI * 3F / 2F);
		boatSides[2].rotateAngleY = ((float) Math.PI / 2F);
		boatSides[3].rotateAngleY = (float) Math.PI;
		paddles[0] = makePaddle(true);
		paddles[0].setRotationPoint(3F, -5F, 9F);
		paddles[1] = makePaddle(false);
		paddles[1].setRotationPoint(3F, -5F, -9F);
		paddles[1].rotateAngleY = (float) Math.PI;
		paddles[0].rotateAngleZ = 0.19634955F;
		paddles[1].rotateAngleZ = 0.19634955F;
		noWater = (new ModelRenderer(this, 0, 0)).setTextureSize(128, 64);
		noWater.addBox(-14F, -9F, -3F, 28, 16, 3, 0F);
		noWater.setRotationPoint(0F, -3F, 1F);
		noWater.rotateAngleX = ((float) Math.PI / 2F);
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		GlStateManager.rotate(90F, 0F, 1F, 0F);
		EntityBoat entityboat = (EntityBoat) entityIn;
		setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);

		for (int i = 0; i < 5; ++i) {
			boatSides[i].render(scale);
		}

		renderPaddle(entityboat, 0, scale, limbSwing);
		renderPaddle(entityboat, 1, scale, limbSwing);
	}

	public void renderMultipass(Entity p_187054_1_, float p_187054_2_, float p_187054_3_, float p_187054_4_, float p_187054_5_, float p_187054_6_, float scale) {

		GlStateManager.rotate(90F, 0F, 1F, 0F);
		GlStateManager.colorMask(false, false, false, false);
		noWater.render(scale);
		GlStateManager.colorMask(true, true, true, true);
	}

	protected ModelRenderer makePaddle(boolean p_187056_1_) {

		ModelRenderer modelrenderer = (new ModelRenderer(this, 62, p_187056_1_ ? 0 : 20)).setTextureSize(128, 64);
		int i = 20;
		int j = 7;
		int k = 6;
		float f = -5F;
		modelrenderer.addBox(-1F, 0F, -5F, 2, 2, 18);
		modelrenderer.addBox(p_187056_1_ ? -1.001F : 0.001F, -3F, 8F, 1, 6, 7);
		return modelrenderer;
	}

	protected void renderPaddle(EntityBoat boat, int paddle, float scale, float limbSwing) {

		float f = boat.getRowingTime(paddle, limbSwing);
		ModelRenderer modelrenderer = paddles[paddle];
		modelrenderer.rotateAngleX = (float) MathHelper.clampedLerp(-1.0471975803375244D, -0.2617993950843811D, (MathHelper.sin(-f) + 1F) / 2F);
		modelrenderer.rotateAngleY = (float) MathHelper.clampedLerp(-(Math.PI / 4D), (Math.PI / 4D), (MathHelper.sin(-f + 1F) + 1F) / 2F);

		if (paddle == 1) {
			modelrenderer.rotateAngleY = (float) Math.PI - modelrenderer.rotateAngleY;
		}

		modelrenderer.render(scale);
	}

}
