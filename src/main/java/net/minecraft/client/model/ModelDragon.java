package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;

public class ModelDragon extends ModelBase {

	/**
	 * The head Model renderer of the dragon
	 */
	private final ModelRenderer head;

	/**
	 * The spine Model renderer of the dragon
	 */
	private final ModelRenderer spine;

	/**
	 * The jaw Model renderer of the dragon
	 */
	private final ModelRenderer jaw;

	/**
	 * The body Model renderer of the dragon
	 */
	private final ModelRenderer body;

	/**
	 * The rear leg Model renderer of the dragon
	 */
	private final ModelRenderer rearLeg;

	/**
	 * The front leg Model renderer of the dragon
	 */
	private final ModelRenderer frontLeg;

	/**
	 * The rear leg tip Model renderer of the dragon
	 */
	private final ModelRenderer rearLegTip;

	/**
	 * The front leg tip Model renderer of the dragon
	 */
	private final ModelRenderer frontLegTip;

	/**
	 * The rear foot Model renderer of the dragon
	 */
	private final ModelRenderer rearFoot;

	/**
	 * The front foot Model renderer of the dragon
	 */
	private final ModelRenderer frontFoot;

	/**
	 * The wing Model renderer of the dragon
	 */
	private final ModelRenderer wing;

	/**
	 * The wing tip Model renderer of the dragon
	 */
	private final ModelRenderer wingTip;
	private float partialTicks;

	public ModelDragon(float p_i46360_1_) {

		textureWidth = 256;
		textureHeight = 256;
		setTextureOffset("body.body", 0, 0);
		setTextureOffset("wing.skin", -56, 88);
		setTextureOffset("wingtip.skin", -56, 144);
		setTextureOffset("rearleg.main", 0, 0);
		setTextureOffset("rearfoot.main", 112, 0);
		setTextureOffset("rearlegtip.main", 196, 0);
		setTextureOffset("head.upperhead", 112, 30);
		setTextureOffset("wing.bone", 112, 88);
		setTextureOffset("head.upperlip", 176, 44);
		setTextureOffset("jaw.jaw", 176, 65);
		setTextureOffset("frontleg.main", 112, 104);
		setTextureOffset("wingtip.bone", 112, 136);
		setTextureOffset("frontfoot.main", 144, 104);
		setTextureOffset("neck.box", 192, 104);
		setTextureOffset("frontlegtip.main", 226, 138);
		setTextureOffset("body.scale", 220, 53);
		setTextureOffset("head.scale", 0, 0);
		setTextureOffset("neck.scale", 48, 0);
		setTextureOffset("head.nostril", 112, 0);
		float f = -16F;
		head = new ModelRenderer(this, "head");
		head.addBox("upperlip", -6F, -1F, -24F, 12, 5, 16);
		head.addBox("upperhead", -8F, -8F, -10F, 16, 16, 16);
		head.mirror = true;
		head.addBox("scale", -5F, -12F, -4F, 2, 4, 6);
		head.addBox("nostril", -5F, -3F, -22F, 2, 2, 4);
		head.mirror = false;
		head.addBox("scale", 3F, -12F, -4F, 2, 4, 6);
		head.addBox("nostril", 3F, -3F, -22F, 2, 2, 4);
		jaw = new ModelRenderer(this, "jaw");
		jaw.setRotationPoint(0F, 4F, -8F);
		jaw.addBox("jaw", -6F, 0F, -16F, 12, 4, 16);
		head.addChild(jaw);
		spine = new ModelRenderer(this, "neck");
		spine.addBox("box", -5F, -5F, -5F, 10, 10, 10);
		spine.addBox("scale", -1F, -9F, -3F, 2, 4, 6);
		body = new ModelRenderer(this, "body");
		body.setRotationPoint(0F, 4F, 8F);
		body.addBox("body", -12F, 0F, -16F, 24, 24, 64);
		body.addBox("scale", -1F, -6F, -10F, 2, 6, 12);
		body.addBox("scale", -1F, -6F, 10F, 2, 6, 12);
		body.addBox("scale", -1F, -6F, 30F, 2, 6, 12);
		wing = new ModelRenderer(this, "wing");
		wing.setRotationPoint(-12F, 5F, 2F);
		wing.addBox("bone", -56F, -4F, -4F, 56, 8, 8);
		wing.addBox("skin", -56F, 0F, 2F, 56, 0, 56);
		wingTip = new ModelRenderer(this, "wingtip");
		wingTip.setRotationPoint(-56F, 0F, 0F);
		wingTip.addBox("bone", -56F, -2F, -2F, 56, 4, 4);
		wingTip.addBox("skin", -56F, 0F, 2F, 56, 0, 56);
		wing.addChild(wingTip);
		frontLeg = new ModelRenderer(this, "frontleg");
		frontLeg.setRotationPoint(-12F, 20F, 2F);
		frontLeg.addBox("main", -4F, -4F, -4F, 8, 24, 8);
		frontLegTip = new ModelRenderer(this, "frontlegtip");
		frontLegTip.setRotationPoint(0F, 20F, -1F);
		frontLegTip.addBox("main", -3F, -1F, -3F, 6, 24, 6);
		frontLeg.addChild(frontLegTip);
		frontFoot = new ModelRenderer(this, "frontfoot");
		frontFoot.setRotationPoint(0F, 23F, 0F);
		frontFoot.addBox("main", -4F, 0F, -12F, 8, 4, 16);
		frontLegTip.addChild(frontFoot);
		rearLeg = new ModelRenderer(this, "rearleg");
		rearLeg.setRotationPoint(-16F, 16F, 42F);
		rearLeg.addBox("main", -8F, -4F, -8F, 16, 32, 16);
		rearLegTip = new ModelRenderer(this, "rearlegtip");
		rearLegTip.setRotationPoint(0F, 32F, -4F);
		rearLegTip.addBox("main", -6F, -2F, 0F, 12, 32, 12);
		rearLeg.addChild(rearLegTip);
		rearFoot = new ModelRenderer(this, "rearfoot");
		rearFoot.setRotationPoint(0F, 31F, 4F);
		rearFoot.addBox("main", -9F, 0F, -20F, 18, 6, 24);
		rearLegTip.addChild(rearFoot);
	}

	/**
	 * Used for easily adding entity-dependent animations. The second and third float params here are the same second
	 * and third as in the setRotationAngles method.
	 */
	public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTickTime) {

		partialTicks = partialTickTime;
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		GlStateManager.pushMatrix();
		EntityDragon entitydragon = (EntityDragon) entityIn;
		float f = entitydragon.prevAnimTime + (entitydragon.animTime - entitydragon.prevAnimTime) * partialTicks;
		jaw.rotateAngleX = (float) (Math.sin(f * ((float) Math.PI * 2F)) + 1D) * 0.2F;
		float f1 = (float) (Math.sin(f * ((float) Math.PI * 2F) - 1F) + 1D);
		f1 = (f1 * f1 + f1 * 2F) * 0.05F;
		GlStateManager.translate(0F, f1 - 2F, -3F);
		GlStateManager.rotate(f1 * 2F, 1F, 0F, 0F);
		float f2;
		float f4 = 0F;
		float f5 = 1.5F;
		double[] adouble = entitydragon.getMovementOffsets(6, partialTicks);
		float f6 = updateRotations(entitydragon.getMovementOffsets(5, partialTicks)[0] - entitydragon.getMovementOffsets(10, partialTicks)[0]);
		float f7 = updateRotations(entitydragon.getMovementOffsets(5, partialTicks)[0] + (double) (f6 / 2F));
		float f8 = f * ((float) Math.PI * 2F);
		f2 = 20F;
		float f3 = -12F;

		for (int i = 0; i < 5; ++i) {
			double[] adouble1 = entitydragon.getMovementOffsets(5 - i, partialTicks);
			float f9 = (float) Math.cos((float) i * 0.45F + f8) * 0.15F;
			spine.rotateAngleY = updateRotations(adouble1[0] - adouble[0]) * 0.017453292F * 1.5F;
			spine.rotateAngleX = f9 + entitydragon.getHeadPartYOffset(i, adouble, adouble1) * 0.017453292F * 1.5F * 5F;
			spine.rotateAngleZ = -updateRotations(adouble1[0] - (double) f7) * 0.017453292F * 1.5F;
			spine.rotationPointY = f2;
			spine.rotationPointZ = f3;
			spine.rotationPointX = f4;
			f2 = (float) ((double) f2 + Math.sin(spine.rotateAngleX) * 10D);
			f3 = (float) ((double) f3 - Math.cos(spine.rotateAngleY) * Math.cos(spine.rotateAngleX) * 10D);
			f4 = (float) ((double) f4 - Math.sin(spine.rotateAngleY) * Math.cos(spine.rotateAngleX) * 10D);
			spine.render(scale);
		}

		head.rotationPointY = f2;
		head.rotationPointZ = f3;
		head.rotationPointX = f4;
		double[] adouble2 = entitydragon.getMovementOffsets(0, partialTicks);
		head.rotateAngleY = updateRotations(adouble2[0] - adouble[0]) * 0.017453292F;
		head.rotateAngleX = updateRotations(entitydragon.getHeadPartYOffset(6, adouble, adouble2)) * 0.017453292F * 1.5F * 5F;
		head.rotateAngleZ = -updateRotations(adouble2[0] - (double) f7) * 0.017453292F;
		head.render(scale);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0F, 1F, 0F);
		GlStateManager.rotate(-f6 * 1.5F, 0F, 0F, 1F);
		GlStateManager.translate(0F, -1F, 0F);
		body.rotateAngleZ = 0F;
		body.render(scale);

		for (int j = 0; j < 2; ++j) {
			GlStateManager.enableCull();
			float f11 = f * ((float) Math.PI * 2F);
			wing.rotateAngleX = 0.125F - (float) Math.cos(f11) * 0.2F;
			wing.rotateAngleY = 0.25F;
			wing.rotateAngleZ = (float) (Math.sin(f11) + 0.125D) * 0.8F;
			wingTip.rotateAngleZ = -((float) (Math.sin(f11 + 2F) + 0.5D)) * 0.75F;
			rearLeg.rotateAngleX = 1F + f1 * 0.1F;
			rearLegTip.rotateAngleX = 0.5F + f1 * 0.1F;
			rearFoot.rotateAngleX = 0.75F + f1 * 0.1F;
			frontLeg.rotateAngleX = 1.3F + f1 * 0.1F;
			frontLegTip.rotateAngleX = -0.5F - f1 * 0.1F;
			frontFoot.rotateAngleX = 0.75F + f1 * 0.1F;
			wing.render(scale);
			frontLeg.render(scale);
			rearLeg.render(scale);
			GlStateManager.scale(-1F, 1F, 1F);

			if (j == 0) {
				GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
			}
		}

		GlStateManager.popMatrix();
		GlStateManager.cullFace(GlStateManager.CullFace.BACK);
		GlStateManager.disableCull();
		float f10 = -((float) Math.sin(f * ((float) Math.PI * 2F))) * 0F;
		f8 = f * ((float) Math.PI * 2F);
		f2 = 10F;
		f3 = 60F;
		f4 = 0F;
		adouble = entitydragon.getMovementOffsets(11, partialTicks);

		for (int k = 0; k < 12; ++k) {
			adouble2 = entitydragon.getMovementOffsets(12 + k, partialTicks);
			f10 = (float) ((double) f10 + Math.sin((float) k * 0.45F + f8) * 0.05000000074505806D);
			spine.rotateAngleY = (updateRotations(adouble2[0] - adouble[0]) * 1.5F + 180F) * 0.017453292F;
			spine.rotateAngleX = f10 + (float) (adouble2[1] - adouble[1]) * 0.017453292F * 1.5F * 5F;
			spine.rotateAngleZ = updateRotations(adouble2[0] - (double) f7) * 0.017453292F * 1.5F;
			spine.rotationPointY = f2;
			spine.rotationPointZ = f3;
			spine.rotationPointX = f4;
			f2 = (float) ((double) f2 + Math.sin(spine.rotateAngleX) * 10D);
			f3 = (float) ((double) f3 - Math.cos(spine.rotateAngleY) * Math.cos(spine.rotateAngleX) * 10D);
			f4 = (float) ((double) f4 - Math.sin(spine.rotateAngleY) * Math.cos(spine.rotateAngleX) * 10D);
			spine.render(scale);
		}

		GlStateManager.popMatrix();
	}

	/**
	 * Updates the rotations in the parameters for rotations greater than 180 degrees or less than -180 degrees. It adds
	 * or subtracts 360 degrees, so that the appearance is the same, although the numbers are then simplified to range
	 * -180 to 180
	 */
	private float updateRotations(double p_78214_1_) {

		while (p_78214_1_ >= 180D) {
			p_78214_1_ -= 360D;
		}

		while (p_78214_1_ < -180D) {
			p_78214_1_ += 360D;
		}

		return (float) p_78214_1_;
	}

}
