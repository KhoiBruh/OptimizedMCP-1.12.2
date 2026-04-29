package net.minecraft.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ModelGuardian extends ModelBase {

	private final ModelRenderer guardianBody;
	private final ModelRenderer guardianEye;
	private final ModelRenderer[] guardianSpines;
	private final ModelRenderer[] guardianTail;

	public ModelGuardian() {

		textureWidth = 64;
		textureHeight = 64;
		guardianSpines = new ModelRenderer[12];
		guardianBody = new ModelRenderer(this);
		guardianBody.setTextureOffset(0, 0).addBox(-6F, 10F, -8F, 12, 12, 16);
		guardianBody.setTextureOffset(0, 28).addBox(-8F, 10F, -6F, 2, 12, 12);
		guardianBody.setTextureOffset(0, 28).addBox(6F, 10F, -6F, 2, 12, 12, true);
		guardianBody.setTextureOffset(16, 40).addBox(-6F, 8F, -6F, 12, 2, 12);
		guardianBody.setTextureOffset(16, 40).addBox(-6F, 22F, -6F, 12, 2, 12);

		for (int i = 0; i < guardianSpines.length; ++i) {
			guardianSpines[i] = new ModelRenderer(this, 0, 0);
			guardianSpines[i].addBox(-1F, -4.5F, -1F, 2, 9, 2);
			guardianBody.addChild(guardianSpines[i]);
		}

		guardianEye = new ModelRenderer(this, 8, 0);
		guardianEye.addBox(-1F, 15F, 0F, 2, 2, 1);
		guardianBody.addChild(guardianEye);
		guardianTail = new ModelRenderer[3];
		guardianTail[0] = new ModelRenderer(this, 40, 0);
		guardianTail[0].addBox(-2F, 14F, 7F, 4, 4, 8);
		guardianTail[1] = new ModelRenderer(this, 0, 54);
		guardianTail[1].addBox(0F, 14F, 0F, 3, 3, 7);
		guardianTail[2] = new ModelRenderer(this);
		guardianTail[2].setTextureOffset(41, 32).addBox(0F, 14F, 0F, 2, 2, 6);
		guardianTail[2].setTextureOffset(25, 19).addBox(1F, 10.5F, 3F, 1, 9, 9);
		guardianBody.addChild(guardianTail[0]);
		guardianTail[0].addChild(guardianTail[1]);
		guardianTail[1].addChild(guardianTail[2]);
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
		guardianBody.render(scale);
	}

	/**
	 * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
	 * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
	 * "far" arms and legs can swing at most.
	 */
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {

		EntityGuardian entityguardian = (EntityGuardian) entityIn;
		float f = ageInTicks - (float) entityguardian.ticksExisted;
		guardianBody.rotateAngleY = netHeadYaw * 0.017453292F;
		guardianBody.rotateAngleX = headPitch * 0.017453292F;
		float[] afloat = new float[]{1.75F, 0.25F, 0F, 0F, 0.5F, 0.5F, 0.5F, 0.5F, 1.25F, 0.75F, 0F, 0F};
		float[] afloat1 = new float[]{0F, 0F, 0F, 0F, 0.25F, 1.75F, 1.25F, 0.75F, 0F, 0F, 0F, 0F};
		float[] afloat2 = new float[]{0F, 0F, 0.25F, 1.75F, 0F, 0F, 0F, 0F, 0F, 0F, 0.75F, 1.25F};
		float[] afloat3 = new float[]{0F, 0F, 8F, -8F, -8F, 8F, 8F, -8F, 0F, 0F, 8F, -8F};
		float[] afloat4 = new float[]{-8F, -8F, -8F, -8F, 0F, 0F, 0F, 0F, 8F, 8F, 8F, 8F};
		float[] afloat5 = new float[]{8F, -8F, 0F, 0F, -8F, -8F, 8F, 8F, 8F, -8F, 0F, 0F};
		float f1 = (1F - entityguardian.getSpikesAnimation(f)) * 0.55F;

		for (int i = 0; i < 12; ++i) {
			guardianSpines[i].rotateAngleX = (float) Math.PI * afloat[i];
			guardianSpines[i].rotateAngleY = (float) Math.PI * afloat1[i];
			guardianSpines[i].rotateAngleZ = (float) Math.PI * afloat2[i];
			guardianSpines[i].rotationPointX = afloat3[i] * (1F + MathHelper.cos(ageInTicks * 1.5F + (float) i) * 0.01F - f1);
			guardianSpines[i].rotationPointY = 16F + afloat4[i] * (1F + MathHelper.cos(ageInTicks * 1.5F + (float) i) * 0.01F - f1);
			guardianSpines[i].rotationPointZ = afloat5[i] * (1F + MathHelper.cos(ageInTicks * 1.5F + (float) i) * 0.01F - f1);
		}

		guardianEye.rotationPointZ = -8.25F;
		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();

		if (entityguardian.hasTargetedEntity()) {
			entity = entityguardian.getTargetedEntity();
		}

		if (entity != null) {
			Vec3d vec3d = entity.getPositionEyes(0F);
			Vec3d vec3d1 = entityIn.getPositionEyes(0F);
			double d0 = vec3d.y() - vec3d1.y();

			if (d0 > 0D) {
				guardianEye.rotationPointY = 0F;
			} else {
				guardianEye.rotationPointY = 1F;
			}

			Vec3d vec3d2 = entityIn.getLook(0F);
			vec3d2 = new Vec3d(vec3d2.x(), 0D, vec3d2.z());
			Vec3d vec3d3 = (new Vec3d(vec3d1.x() - vec3d.x(), 0D, vec3d1.z() - vec3d.z())).normalize().rotateYaw(((float) Math.PI / 2F));
			double d1 = vec3d2.dotProduct(vec3d3);
			guardianEye.rotationPointX = MathHelper.sqrt((float) Math.abs(d1)) * 2F * (float) Math.signum(d1);
		}

		guardianEye.showModel = true;
		float f2 = entityguardian.getTailAnimation(f);
		guardianTail[0].rotateAngleY = MathHelper.sin(f2) * (float) Math.PI * 0.05F;
		guardianTail[1].rotateAngleY = MathHelper.sin(f2) * (float) Math.PI * 0.1F;
		guardianTail[1].rotationPointX = -1.5F;
		guardianTail[1].rotationPointY = 0.5F;
		guardianTail[1].rotationPointZ = 14F;
		guardianTail[2].rotateAngleY = MathHelper.sin(f2) * (float) Math.PI * 0.15F;
		guardianTail[2].rotationPointX = 0.5F;
		guardianTail[2].rotationPointY = 0.5F;
		guardianTail[2].rotationPointZ = 6F;
	}

}
