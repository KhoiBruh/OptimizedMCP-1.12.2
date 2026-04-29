package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.util.math.MathHelper;

public class ModelHorse extends ModelBase {

	private final ModelRenderer head;
	private final ModelRenderer upperMouth;
	private final ModelRenderer lowerMouth;
	private final ModelRenderer horseLeftEar;
	private final ModelRenderer horseRightEar;

	/**
	 * The left ear box for the mule model.
	 */
	private final ModelRenderer muleLeftEar;

	/**
	 * The right ear box for the mule model.
	 */
	private final ModelRenderer muleRightEar;
	private final ModelRenderer neck;

	/**
	 * The box for the horse's ropes on its face.
	 */
	private final ModelRenderer horseFaceRopes;
	private final ModelRenderer mane;
	private final ModelRenderer body;
	private final ModelRenderer tailBase;
	private final ModelRenderer tailMiddle;
	private final ModelRenderer tailTip;
	private final ModelRenderer backLeftLeg;
	private final ModelRenderer backLeftShin;
	private final ModelRenderer backLeftHoof;
	private final ModelRenderer backRightLeg;
	private final ModelRenderer backRightShin;
	private final ModelRenderer backRightHoof;
	private final ModelRenderer frontLeftLeg;
	private final ModelRenderer frontLeftShin;
	private final ModelRenderer frontLeftHoof;
	private final ModelRenderer frontRightLeg;
	private final ModelRenderer frontRightShin;
	private final ModelRenderer frontRightHoof;

	/**
	 * The left chest box on the mule model.
	 */
	private final ModelRenderer muleLeftChest;

	/**
	 * The right chest box on the mule model.
	 */
	private final ModelRenderer muleRightChest;
	private final ModelRenderer horseSaddleBottom;
	private final ModelRenderer horseSaddleFront;
	private final ModelRenderer horseSaddleBack;
	private final ModelRenderer horseLeftSaddleRope;
	private final ModelRenderer horseLeftSaddleMetal;
	private final ModelRenderer horseRightSaddleRope;
	private final ModelRenderer horseRightSaddleMetal;

	/**
	 * The left metal connected to the horse's face ropes.
	 */
	private final ModelRenderer horseLeftFaceMetal;

	/**
	 * The right metal connected to the horse's face ropes.
	 */
	private final ModelRenderer horseRightFaceMetal;
	private final ModelRenderer horseLeftRein;
	private final ModelRenderer horseRightRein;

	public ModelHorse() {

		textureWidth = 128;
		textureHeight = 128;
		body = new ModelRenderer(this, 0, 34);
		body.addBox(-5F, -8F, -19F, 10, 10, 24);
		body.setRotationPoint(0F, 11F, 9F);
		tailBase = new ModelRenderer(this, 44, 0);
		tailBase.addBox(-1F, -1F, 0F, 2, 2, 3);
		tailBase.setRotationPoint(0F, 3F, 14F);
		tailBase.rotateAngleX = -1.134464F;
		tailMiddle = new ModelRenderer(this, 38, 7);
		tailMiddle.addBox(-1.5F, -2F, 3F, 3, 4, 7);
		tailMiddle.setRotationPoint(0F, 3F, 14F);
		tailMiddle.rotateAngleX = -1.134464F;
		tailTip = new ModelRenderer(this, 24, 3);
		tailTip.addBox(-1.5F, -4.5F, 9F, 3, 4, 7);
		tailTip.setRotationPoint(0F, 3F, 14F);
		tailTip.rotateAngleX = -1.3962634F;
		backLeftLeg = new ModelRenderer(this, 78, 29);
		backLeftLeg.addBox(-2.5F, -2F, -2.5F, 4, 9, 5);
		backLeftLeg.setRotationPoint(4F, 9F, 11F);
		backLeftShin = new ModelRenderer(this, 78, 43);
		backLeftShin.addBox(-2F, 0F, -1.5F, 3, 5, 3);
		backLeftShin.setRotationPoint(4F, 16F, 11F);
		backLeftHoof = new ModelRenderer(this, 78, 51);
		backLeftHoof.addBox(-2.5F, 5.1F, -2F, 4, 3, 4);
		backLeftHoof.setRotationPoint(4F, 16F, 11F);
		backRightLeg = new ModelRenderer(this, 96, 29);
		backRightLeg.addBox(-1.5F, -2F, -2.5F, 4, 9, 5);
		backRightLeg.setRotationPoint(-4F, 9F, 11F);
		backRightShin = new ModelRenderer(this, 96, 43);
		backRightShin.addBox(-1F, 0F, -1.5F, 3, 5, 3);
		backRightShin.setRotationPoint(-4F, 16F, 11F);
		backRightHoof = new ModelRenderer(this, 96, 51);
		backRightHoof.addBox(-1.5F, 5.1F, -2F, 4, 3, 4);
		backRightHoof.setRotationPoint(-4F, 16F, 11F);
		frontLeftLeg = new ModelRenderer(this, 44, 29);
		frontLeftLeg.addBox(-1.9F, -1F, -2.1F, 3, 8, 4);
		frontLeftLeg.setRotationPoint(4F, 9F, -8F);
		frontLeftShin = new ModelRenderer(this, 44, 41);
		frontLeftShin.addBox(-1.9F, 0F, -1.6F, 3, 5, 3);
		frontLeftShin.setRotationPoint(4F, 16F, -8F);
		frontLeftHoof = new ModelRenderer(this, 44, 51);
		frontLeftHoof.addBox(-2.4F, 5.1F, -2.1F, 4, 3, 4);
		frontLeftHoof.setRotationPoint(4F, 16F, -8F);
		frontRightLeg = new ModelRenderer(this, 60, 29);
		frontRightLeg.addBox(-1.1F, -1F, -2.1F, 3, 8, 4);
		frontRightLeg.setRotationPoint(-4F, 9F, -8F);
		frontRightShin = new ModelRenderer(this, 60, 41);
		frontRightShin.addBox(-1.1F, 0F, -1.6F, 3, 5, 3);
		frontRightShin.setRotationPoint(-4F, 16F, -8F);
		frontRightHoof = new ModelRenderer(this, 60, 51);
		frontRightHoof.addBox(-1.6F, 5.1F, -2.1F, 4, 3, 4);
		frontRightHoof.setRotationPoint(-4F, 16F, -8F);
		head = new ModelRenderer(this, 0, 0);
		head.addBox(-2.5F, -10F, -1.5F, 5, 5, 7);
		head.setRotationPoint(0F, 4F, -10F);
		head.rotateAngleX = 0.5235988F;
		upperMouth = new ModelRenderer(this, 24, 18);
		upperMouth.addBox(-2F, -10F, -7F, 4, 3, 6);
		upperMouth.setRotationPoint(0F, 3.95F, -10F);
		upperMouth.rotateAngleX = 0.5235988F;
		lowerMouth = new ModelRenderer(this, 24, 27);
		lowerMouth.addBox(-2F, -7F, -6.5F, 4, 2, 5);
		lowerMouth.setRotationPoint(0F, 4F, -10F);
		lowerMouth.rotateAngleX = 0.5235988F;
		head.addChild(upperMouth);
		head.addChild(lowerMouth);
		horseLeftEar = new ModelRenderer(this, 0, 0);
		horseLeftEar.addBox(0.45F, -12F, 4F, 2, 3, 1);
		horseLeftEar.setRotationPoint(0F, 4F, -10F);
		horseLeftEar.rotateAngleX = 0.5235988F;
		horseRightEar = new ModelRenderer(this, 0, 0);
		horseRightEar.addBox(-2.45F, -12F, 4F, 2, 3, 1);
		horseRightEar.setRotationPoint(0F, 4F, -10F);
		horseRightEar.rotateAngleX = 0.5235988F;
		muleLeftEar = new ModelRenderer(this, 0, 12);
		muleLeftEar.addBox(-2F, -16F, 4F, 2, 7, 1);
		muleLeftEar.setRotationPoint(0F, 4F, -10F);
		muleLeftEar.rotateAngleX = 0.5235988F;
		muleLeftEar.rotateAngleZ = 0.2617994F;
		muleRightEar = new ModelRenderer(this, 0, 12);
		muleRightEar.addBox(0F, -16F, 4F, 2, 7, 1);
		muleRightEar.setRotationPoint(0F, 4F, -10F);
		muleRightEar.rotateAngleX = 0.5235988F;
		muleRightEar.rotateAngleZ = -0.2617994F;
		neck = new ModelRenderer(this, 0, 12);
		neck.addBox(-2.05F, -9.8F, -2F, 4, 14, 8);
		neck.setRotationPoint(0F, 4F, -10F);
		neck.rotateAngleX = 0.5235988F;
		muleLeftChest = new ModelRenderer(this, 0, 34);
		muleLeftChest.addBox(-3F, 0F, 0F, 8, 8, 3);
		muleLeftChest.setRotationPoint(-7.5F, 3F, 10F);
		muleLeftChest.rotateAngleY = ((float) Math.PI / 2F);
		muleRightChest = new ModelRenderer(this, 0, 47);
		muleRightChest.addBox(-3F, 0F, 0F, 8, 8, 3);
		muleRightChest.setRotationPoint(4.5F, 3F, 10F);
		muleRightChest.rotateAngleY = ((float) Math.PI / 2F);
		horseSaddleBottom = new ModelRenderer(this, 80, 0);
		horseSaddleBottom.addBox(-5F, 0F, -3F, 10, 1, 8);
		horseSaddleBottom.setRotationPoint(0F, 2F, 2F);
		horseSaddleFront = new ModelRenderer(this, 106, 9);
		horseSaddleFront.addBox(-1.5F, -1F, -3F, 3, 1, 2);
		horseSaddleFront.setRotationPoint(0F, 2F, 2F);
		horseSaddleBack = new ModelRenderer(this, 80, 9);
		horseSaddleBack.addBox(-4F, -1F, 3F, 8, 1, 2);
		horseSaddleBack.setRotationPoint(0F, 2F, 2F);
		horseLeftSaddleMetal = new ModelRenderer(this, 74, 0);
		horseLeftSaddleMetal.addBox(-0.5F, 6F, -1F, 1, 2, 2);
		horseLeftSaddleMetal.setRotationPoint(5F, 3F, 2F);
		horseLeftSaddleRope = new ModelRenderer(this, 70, 0);
		horseLeftSaddleRope.addBox(-0.5F, 0F, -0.5F, 1, 6, 1);
		horseLeftSaddleRope.setRotationPoint(5F, 3F, 2F);
		horseRightSaddleMetal = new ModelRenderer(this, 74, 4);
		horseRightSaddleMetal.addBox(-0.5F, 6F, -1F, 1, 2, 2);
		horseRightSaddleMetal.setRotationPoint(-5F, 3F, 2F);
		horseRightSaddleRope = new ModelRenderer(this, 80, 0);
		horseRightSaddleRope.addBox(-0.5F, 0F, -0.5F, 1, 6, 1);
		horseRightSaddleRope.setRotationPoint(-5F, 3F, 2F);
		horseLeftFaceMetal = new ModelRenderer(this, 74, 13);
		horseLeftFaceMetal.addBox(1.5F, -8F, -4F, 1, 2, 2);
		horseLeftFaceMetal.setRotationPoint(0F, 4F, -10F);
		horseLeftFaceMetal.rotateAngleX = 0.5235988F;
		horseRightFaceMetal = new ModelRenderer(this, 74, 13);
		horseRightFaceMetal.addBox(-2.5F, -8F, -4F, 1, 2, 2);
		horseRightFaceMetal.setRotationPoint(0F, 4F, -10F);
		horseRightFaceMetal.rotateAngleX = 0.5235988F;
		horseLeftRein = new ModelRenderer(this, 44, 10);
		horseLeftRein.addBox(2.6F, -6F, -6F, 0, 3, 16);
		horseLeftRein.setRotationPoint(0F, 4F, -10F);
		horseRightRein = new ModelRenderer(this, 44, 5);
		horseRightRein.addBox(-2.6F, -6F, -6F, 0, 3, 16);
		horseRightRein.setRotationPoint(0F, 4F, -10F);
		mane = new ModelRenderer(this, 58, 0);
		mane.addBox(-1F, -11.5F, 5F, 2, 16, 4);
		mane.setRotationPoint(0F, 4F, -10F);
		mane.rotateAngleX = 0.5235988F;
		horseFaceRopes = new ModelRenderer(this, 80, 12);
		horseFaceRopes.addBox(-2.5F, -10.1F, -7F, 5, 5, 12, 0.2F);
		horseFaceRopes.setRotationPoint(0F, 4F, -10F);
		horseFaceRopes.rotateAngleX = 0.5235988F;
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		AbstractHorse abstracthorse = (AbstractHorse) entityIn;
		float f = abstracthorse.getGrassEatingAmount(0F);
		boolean flag = abstracthorse.isChild();
		boolean flag1 = !flag && abstracthorse.isHorseSaddled();
		boolean flag2 = abstracthorse instanceof AbstractChestHorse;
		boolean flag3 = !flag && flag2 && ((AbstractChestHorse) abstracthorse).hasChest();
		float f1 = abstracthorse.getHorseSize();
		boolean flag4 = abstracthorse.isBeingRidden();

		if (flag1) {
			horseFaceRopes.render(scale);
			horseSaddleBottom.render(scale);
			horseSaddleFront.render(scale);
			horseSaddleBack.render(scale);
			horseLeftSaddleRope.render(scale);
			horseLeftSaddleMetal.render(scale);
			horseRightSaddleRope.render(scale);
			horseRightSaddleMetal.render(scale);
			horseLeftFaceMetal.render(scale);
			horseRightFaceMetal.render(scale);

			if (flag4) {
				horseLeftRein.render(scale);
				horseRightRein.render(scale);
			}
		}

		if (flag) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(f1, 0.5F + f1 * 0.5F, f1);
			GlStateManager.translate(0F, 0.95F * (1F - f1), 0F);
		}

		backLeftLeg.render(scale);
		backLeftShin.render(scale);
		backLeftHoof.render(scale);
		backRightLeg.render(scale);
		backRightShin.render(scale);
		backRightHoof.render(scale);
		frontLeftLeg.render(scale);
		frontLeftShin.render(scale);
		frontLeftHoof.render(scale);
		frontRightLeg.render(scale);
		frontRightShin.render(scale);
		frontRightHoof.render(scale);

		if (flag) {
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.scale(f1, f1, f1);
			GlStateManager.translate(0F, 1.35F * (1F - f1), 0F);
		}

		body.render(scale);
		tailBase.render(scale);
		tailMiddle.render(scale);
		tailTip.render(scale);
		neck.render(scale);
		mane.render(scale);

		if (flag) {
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			float f2 = 0.5F + f1 * f1 * 0.5F;
			GlStateManager.scale(f2, f2, f2);

			if (f <= 0F) {
				GlStateManager.translate(0F, 1.35F * (1F - f1), 0F);
			} else {
				GlStateManager.translate(0F, 0.9F * (1F - f1) * f + 1.35F * (1F - f1) * (1F - f), 0.15F * (1F - f1) * f);
			}
		}

		if (flag2) {
			muleLeftEar.render(scale);
			muleRightEar.render(scale);
		} else {
			horseLeftEar.render(scale);
			horseRightEar.render(scale);
		}

		head.render(scale);

		if (flag) {
			GlStateManager.popMatrix();
		}

		if (flag3) {
			muleLeftChest.render(scale);
			muleRightChest.render(scale);
		}
	}

	/**
	 * Fixes and offsets a rotation in the ModelHorse class.
	 */
	private float updateHorseRotation(float p_110683_1_, float p_110683_2_, float p_110683_3_) {

		float f;

		for (f = p_110683_2_ - p_110683_1_; f < -180F; f += 360F) {
		}

		while (f >= 180F) {
			f -= 360F;
		}

		return p_110683_1_ + p_110683_3_ * f;
	}

	/**
	 * Used for easily adding entity-dependent animations. The second and third float params here are the same second
	 * and third as in the setRotationAngles method.
	 */
	public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTickTime) {

		super.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTickTime);
		float f = updateHorseRotation(entitylivingbaseIn.prevRenderYawOffset, entitylivingbaseIn.renderYawOffset, partialTickTime);
		float f1 = updateHorseRotation(entitylivingbaseIn.prevRotationYawHead, entitylivingbaseIn.rotationYawHead, partialTickTime);
		float f2 = entitylivingbaseIn.prevRotationPitch + (entitylivingbaseIn.rotationPitch - entitylivingbaseIn.prevRotationPitch) * partialTickTime;
		float f3 = f1 - f;
		float f4 = f2 * 0.017453292F;

		if (f3 > 20F) {
			f3 = 20F;
		}

		if (f3 < -20F) {
			f3 = -20F;
		}

		if (limbSwingAmount > 0.2F) {
			f4 += MathHelper.cos(limbSwing * 0.4F) * 0.15F * limbSwingAmount;
		}

		AbstractHorse abstracthorse = (AbstractHorse) entitylivingbaseIn;
		float f5 = abstracthorse.getGrassEatingAmount(partialTickTime);
		float f6 = abstracthorse.getRearingAmount(partialTickTime);
		float f7 = 1F - f6;
		float f8 = abstracthorse.getMouthOpennessAngle(partialTickTime);
		boolean flag = abstracthorse.tailCounter != 0;
		boolean flag1 = abstracthorse.isHorseSaddled();
		boolean flag2 = abstracthorse.isBeingRidden();
		float f9 = (float) entitylivingbaseIn.ticksExisted + partialTickTime;
		float f10 = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI);
		float f11 = f10 * 0.8F * limbSwingAmount;
		head.rotationPointY = 4F;
		head.rotationPointZ = -10F;
		tailBase.rotationPointY = 3F;
		tailMiddle.rotationPointZ = 14F;
		muleRightChest.rotationPointY = 3F;
		muleRightChest.rotationPointZ = 10F;
		body.rotateAngleX = 0F;
		head.rotateAngleX = 0.5235988F + f4;
		head.rotateAngleY = f3 * 0.017453292F;
		head.rotateAngleX = f6 * (0.2617994F + f4) + f5 * 2.1816616F + (1F - Math.max(f6, f5)) * head.rotateAngleX;
		head.rotateAngleY = f6 * f3 * 0.017453292F + (1F - Math.max(f6, f5)) * head.rotateAngleY;
		head.rotationPointY = f6 * -6F + f5 * 11F + (1F - Math.max(f6, f5)) * head.rotationPointY;
		head.rotationPointZ = f6 * -1F + f5 * -10F + (1F - Math.max(f6, f5)) * head.rotationPointZ;
		tailBase.rotationPointY = f6 * 9F + f7 * tailBase.rotationPointY;
		tailMiddle.rotationPointZ = f6 * 18F + f7 * tailMiddle.rotationPointZ;
		muleRightChest.rotationPointY = f6 * 5.5F + f7 * muleRightChest.rotationPointY;
		muleRightChest.rotationPointZ = f6 * 15F + f7 * muleRightChest.rotationPointZ;
		body.rotateAngleX = f6 * -((float) Math.PI / 4F) + f7 * body.rotateAngleX;
		horseLeftEar.rotationPointY = head.rotationPointY;
		horseRightEar.rotationPointY = head.rotationPointY;
		muleLeftEar.rotationPointY = head.rotationPointY;
		muleRightEar.rotationPointY = head.rotationPointY;
		neck.rotationPointY = head.rotationPointY;
		upperMouth.rotationPointY = 0.02F;
		lowerMouth.rotationPointY = 0F;
		mane.rotationPointY = head.rotationPointY;
		horseLeftEar.rotationPointZ = head.rotationPointZ;
		horseRightEar.rotationPointZ = head.rotationPointZ;
		muleLeftEar.rotationPointZ = head.rotationPointZ;
		muleRightEar.rotationPointZ = head.rotationPointZ;
		neck.rotationPointZ = head.rotationPointZ;
		upperMouth.rotationPointZ = 0.02F - f8;
		lowerMouth.rotationPointZ = f8;
		mane.rotationPointZ = head.rotationPointZ;
		horseLeftEar.rotateAngleX = head.rotateAngleX;
		horseRightEar.rotateAngleX = head.rotateAngleX;
		muleLeftEar.rotateAngleX = head.rotateAngleX;
		muleRightEar.rotateAngleX = head.rotateAngleX;
		neck.rotateAngleX = head.rotateAngleX;
		upperMouth.rotateAngleX = -0.09424778F * f8;
		lowerMouth.rotateAngleX = 0.15707964F * f8;
		mane.rotateAngleX = head.rotateAngleX;
		horseLeftEar.rotateAngleY = head.rotateAngleY;
		horseRightEar.rotateAngleY = head.rotateAngleY;
		muleLeftEar.rotateAngleY = head.rotateAngleY;
		muleRightEar.rotateAngleY = head.rotateAngleY;
		neck.rotateAngleY = head.rotateAngleY;
		upperMouth.rotateAngleY = 0F;
		lowerMouth.rotateAngleY = 0F;
		mane.rotateAngleY = head.rotateAngleY;
		muleLeftChest.rotateAngleX = f11 / 5F;
		muleRightChest.rotateAngleX = -f11 / 5F;
		float f12 = 0.2617994F * f6;
		float f13 = MathHelper.cos(f9 * 0.6F + (float) Math.PI);
		frontLeftLeg.rotationPointY = -2F * f6 + 9F * f7;
		frontLeftLeg.rotationPointZ = -2F * f6 + -8F * f7;
		frontRightLeg.rotationPointY = frontLeftLeg.rotationPointY;
		frontRightLeg.rotationPointZ = frontLeftLeg.rotationPointZ;
		backLeftShin.rotationPointY = backLeftLeg.rotationPointY + MathHelper.sin(((float) Math.PI / 2F) + f12 + f7 * -f10 * 0.5F * limbSwingAmount) * 7F;
		backLeftShin.rotationPointZ = backLeftLeg.rotationPointZ + MathHelper.cos(-((float) Math.PI / 2F) + f12 + f7 * -f10 * 0.5F * limbSwingAmount) * 7F;
		backRightShin.rotationPointY = backRightLeg.rotationPointY + MathHelper.sin(((float) Math.PI / 2F) + f12 + f7 * f10 * 0.5F * limbSwingAmount) * 7F;
		backRightShin.rotationPointZ = backRightLeg.rotationPointZ + MathHelper.cos(-((float) Math.PI / 2F) + f12 + f7 * f10 * 0.5F * limbSwingAmount) * 7F;
		float f14 = (-1.0471976F + f13) * f6 + f11 * f7;
		float f15 = (-1.0471976F - f13) * f6 + -f11 * f7;
		frontLeftShin.rotationPointY = frontLeftLeg.rotationPointY + MathHelper.sin(((float) Math.PI / 2F) + f14) * 7F;
		frontLeftShin.rotationPointZ = frontLeftLeg.rotationPointZ + MathHelper.cos(-((float) Math.PI / 2F) + f14) * 7F;
		frontRightShin.rotationPointY = frontRightLeg.rotationPointY + MathHelper.sin(((float) Math.PI / 2F) + f15) * 7F;
		frontRightShin.rotationPointZ = frontRightLeg.rotationPointZ + MathHelper.cos(-((float) Math.PI / 2F) + f15) * 7F;
		backLeftLeg.rotateAngleX = f12 + -f10 * 0.5F * limbSwingAmount * f7;
		backLeftShin.rotateAngleX = -0.08726646F * f6 + (-f10 * 0.5F * limbSwingAmount - Math.max(0F, f10 * 0.5F * limbSwingAmount)) * f7;
		backLeftHoof.rotateAngleX = backLeftShin.rotateAngleX;
		backRightLeg.rotateAngleX = f12 + f10 * 0.5F * limbSwingAmount * f7;
		backRightShin.rotateAngleX = -0.08726646F * f6 + (f10 * 0.5F * limbSwingAmount - Math.max(0F, -f10 * 0.5F * limbSwingAmount)) * f7;
		backRightHoof.rotateAngleX = backRightShin.rotateAngleX;
		frontLeftLeg.rotateAngleX = f14;
		frontLeftShin.rotateAngleX = (frontLeftLeg.rotateAngleX + (float) Math.PI * Math.max(0F, 0.2F + f13 * 0.2F)) * f6 + (f11 + Math.max(0F, f10 * 0.5F * limbSwingAmount)) * f7;
		frontLeftHoof.rotateAngleX = frontLeftShin.rotateAngleX;
		frontRightLeg.rotateAngleX = f15;
		frontRightShin.rotateAngleX = (frontRightLeg.rotateAngleX + (float) Math.PI * Math.max(0F, 0.2F - f13 * 0.2F)) * f6 + (-f11 + Math.max(0F, -f10 * 0.5F * limbSwingAmount)) * f7;
		frontRightHoof.rotateAngleX = frontRightShin.rotateAngleX;
		backLeftHoof.rotationPointY = backLeftShin.rotationPointY;
		backLeftHoof.rotationPointZ = backLeftShin.rotationPointZ;
		backRightHoof.rotationPointY = backRightShin.rotationPointY;
		backRightHoof.rotationPointZ = backRightShin.rotationPointZ;
		frontLeftHoof.rotationPointY = frontLeftShin.rotationPointY;
		frontLeftHoof.rotationPointZ = frontLeftShin.rotationPointZ;
		frontRightHoof.rotationPointY = frontRightShin.rotationPointY;
		frontRightHoof.rotationPointZ = frontRightShin.rotationPointZ;

		if (flag1) {
			horseSaddleBottom.rotationPointY = f6 * 0.5F + f7 * 2F;
			horseSaddleBottom.rotationPointZ = f6 * 11F + f7 * 2F;
			horseSaddleFront.rotationPointY = horseSaddleBottom.rotationPointY;
			horseSaddleBack.rotationPointY = horseSaddleBottom.rotationPointY;
			horseLeftSaddleRope.rotationPointY = horseSaddleBottom.rotationPointY;
			horseRightSaddleRope.rotationPointY = horseSaddleBottom.rotationPointY;
			horseLeftSaddleMetal.rotationPointY = horseSaddleBottom.rotationPointY;
			horseRightSaddleMetal.rotationPointY = horseSaddleBottom.rotationPointY;
			muleLeftChest.rotationPointY = muleRightChest.rotationPointY;
			horseSaddleFront.rotationPointZ = horseSaddleBottom.rotationPointZ;
			horseSaddleBack.rotationPointZ = horseSaddleBottom.rotationPointZ;
			horseLeftSaddleRope.rotationPointZ = horseSaddleBottom.rotationPointZ;
			horseRightSaddleRope.rotationPointZ = horseSaddleBottom.rotationPointZ;
			horseLeftSaddleMetal.rotationPointZ = horseSaddleBottom.rotationPointZ;
			horseRightSaddleMetal.rotationPointZ = horseSaddleBottom.rotationPointZ;
			muleLeftChest.rotationPointZ = muleRightChest.rotationPointZ;
			horseSaddleBottom.rotateAngleX = body.rotateAngleX;
			horseSaddleFront.rotateAngleX = body.rotateAngleX;
			horseSaddleBack.rotateAngleX = body.rotateAngleX;
			horseLeftRein.rotationPointY = head.rotationPointY;
			horseRightRein.rotationPointY = head.rotationPointY;
			horseFaceRopes.rotationPointY = head.rotationPointY;
			horseLeftFaceMetal.rotationPointY = head.rotationPointY;
			horseRightFaceMetal.rotationPointY = head.rotationPointY;
			horseLeftRein.rotationPointZ = head.rotationPointZ;
			horseRightRein.rotationPointZ = head.rotationPointZ;
			horseFaceRopes.rotationPointZ = head.rotationPointZ;
			horseLeftFaceMetal.rotationPointZ = head.rotationPointZ;
			horseRightFaceMetal.rotationPointZ = head.rotationPointZ;
			horseLeftRein.rotateAngleX = f4;
			horseRightRein.rotateAngleX = f4;
			horseFaceRopes.rotateAngleX = head.rotateAngleX;
			horseLeftFaceMetal.rotateAngleX = head.rotateAngleX;
			horseRightFaceMetal.rotateAngleX = head.rotateAngleX;
			horseFaceRopes.rotateAngleY = head.rotateAngleY;
			horseLeftFaceMetal.rotateAngleY = head.rotateAngleY;
			horseLeftRein.rotateAngleY = head.rotateAngleY;
			horseRightFaceMetal.rotateAngleY = head.rotateAngleY;
			horseRightRein.rotateAngleY = head.rotateAngleY;

			if (flag2) {
				horseLeftSaddleRope.rotateAngleX = -1.0471976F;
				horseLeftSaddleMetal.rotateAngleX = -1.0471976F;
				horseRightSaddleRope.rotateAngleX = -1.0471976F;
				horseRightSaddleMetal.rotateAngleX = -1.0471976F;
				horseLeftSaddleRope.rotateAngleZ = 0F;
				horseLeftSaddleMetal.rotateAngleZ = 0F;
				horseRightSaddleRope.rotateAngleZ = 0F;
				horseRightSaddleMetal.rotateAngleZ = 0F;
			} else {
				horseLeftSaddleRope.rotateAngleX = f11 / 3F;
				horseLeftSaddleMetal.rotateAngleX = f11 / 3F;
				horseRightSaddleRope.rotateAngleX = f11 / 3F;
				horseRightSaddleMetal.rotateAngleX = f11 / 3F;
				horseLeftSaddleRope.rotateAngleZ = f11 / 5F;
				horseLeftSaddleMetal.rotateAngleZ = f11 / 5F;
				horseRightSaddleRope.rotateAngleZ = -f11 / 5F;
				horseRightSaddleMetal.rotateAngleZ = -f11 / 5F;
			}
		}

		f12 = -1.3089969F + limbSwingAmount * 1.5F;

		if (f12 > 0F) {
			f12 = 0F;
		}

		if (flag) {
			tailBase.rotateAngleY = MathHelper.cos(f9 * 0.7F);
			f12 = 0F;
		} else {
			tailBase.rotateAngleY = 0F;
		}

		tailMiddle.rotateAngleY = tailBase.rotateAngleY;
		tailTip.rotateAngleY = tailBase.rotateAngleY;
		tailMiddle.rotationPointY = tailBase.rotationPointY;
		tailTip.rotationPointY = tailBase.rotationPointY;
		tailMiddle.rotationPointZ = tailBase.rotationPointZ;
		tailTip.rotationPointZ = tailBase.rotationPointZ;
		tailBase.rotateAngleX = f12;
		tailMiddle.rotateAngleX = f12;
		tailTip.rotateAngleX = -0.2617994F + f12;
	}

}
