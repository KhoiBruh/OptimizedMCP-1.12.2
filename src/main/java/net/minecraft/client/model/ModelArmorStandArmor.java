package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;

public class ModelArmorStandArmor extends ModelBiped {

	public ModelArmorStandArmor() {

		this(0F);
	}

	public ModelArmorStandArmor(float modelSize) {

		this(modelSize, 64, 32);
	}

	protected ModelArmorStandArmor(float modelSize, int textureWidthIn, int textureHeightIn) {

		super(modelSize, 0F, textureWidthIn, textureHeightIn);
	}

	/**
	 * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
	 * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
	 * "far" arms and legs can swing at most.
	 */
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {

		if (entityIn instanceof EntityArmorStand entityarmorstand) {
			bipedHead.rotateAngleX = 0.017453292F * entityarmorstand.getHeadRotation().x();
			bipedHead.rotateAngleY = 0.017453292F * entityarmorstand.getHeadRotation().y();
			bipedHead.rotateAngleZ = 0.017453292F * entityarmorstand.getHeadRotation().z();
			bipedHead.setRotationPoint(0F, 1F, 0F);
			bipedBody.rotateAngleX = 0.017453292F * entityarmorstand.getBodyRotation().x();
			bipedBody.rotateAngleY = 0.017453292F * entityarmorstand.getBodyRotation().y();
			bipedBody.rotateAngleZ = 0.017453292F * entityarmorstand.getBodyRotation().z();
			bipedLeftArm.rotateAngleX = 0.017453292F * entityarmorstand.getLeftArmRotation().x();
			bipedLeftArm.rotateAngleY = 0.017453292F * entityarmorstand.getLeftArmRotation().y();
			bipedLeftArm.rotateAngleZ = 0.017453292F * entityarmorstand.getLeftArmRotation().z();
			bipedRightArm.rotateAngleX = 0.017453292F * entityarmorstand.getRightArmRotation().x();
			bipedRightArm.rotateAngleY = 0.017453292F * entityarmorstand.getRightArmRotation().y();
			bipedRightArm.rotateAngleZ = 0.017453292F * entityarmorstand.getRightArmRotation().z();
			bipedLeftLeg.rotateAngleX = 0.017453292F * entityarmorstand.getLeftLegRotation().x();
			bipedLeftLeg.rotateAngleY = 0.017453292F * entityarmorstand.getLeftLegRotation().y();
			bipedLeftLeg.rotateAngleZ = 0.017453292F * entityarmorstand.getLeftLegRotation().z();
			bipedLeftLeg.setRotationPoint(1.9F, 11F, 0F);
			bipedRightLeg.rotateAngleX = 0.017453292F * entityarmorstand.getRightLegRotation().x();
			bipedRightLeg.rotateAngleY = 0.017453292F * entityarmorstand.getRightLegRotation().y();
			bipedRightLeg.rotateAngleZ = 0.017453292F * entityarmorstand.getRightLegRotation().z();
			bipedRightLeg.setRotationPoint(-1.9F, 11F, 0F);
			copyModelAngles(bipedHead, bipedHeadwear);
		}
	}

}
