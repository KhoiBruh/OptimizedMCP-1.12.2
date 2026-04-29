package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.util.math.MathHelper;

public class ModelShulker extends ModelBase {

	public final ModelRenderer base;
	public final ModelRenderer lid;
	public ModelRenderer head;

	public ModelShulker() {

		textureHeight = 64;
		textureWidth = 64;
		lid = new ModelRenderer(this);
		base = new ModelRenderer(this);
		head = new ModelRenderer(this);
		lid.setTextureOffset(0, 0).addBox(-8F, -16F, -8F, 16, 12, 16);
		lid.setRotationPoint(0F, 24F, 0F);
		base.setTextureOffset(0, 28).addBox(-8F, -8F, -8F, 16, 8, 16);
		base.setRotationPoint(0F, 24F, 0F);
		head.setTextureOffset(0, 52).addBox(-3F, 0F, -3F, 6, 6, 6);
		head.setRotationPoint(0F, 12F, 0F);
	}

	/**
	 * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
	 * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
	 * "far" arms and legs can swing at most.
	 */
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {

		EntityShulker entityshulker = (EntityShulker) entityIn;
		float f = ageInTicks - (float) entityshulker.ticksExisted;
		float f1 = (0.5F + entityshulker.getClientPeekAmount(f)) * (float) Math.PI;
		float f2 = -1F + MathHelper.sin(f1);
		float f3 = 0F;

		if (f1 > (float) Math.PI) {
			f3 = MathHelper.sin(ageInTicks * 0.1F) * 0.7F;
		}

		lid.setRotationPoint(0F, 16F + MathHelper.sin(f1) * 8F + f3, 0F);

		if (entityshulker.getClientPeekAmount(f) > 0.3F) {
			lid.rotateAngleY = f2 * f2 * f2 * f2 * (float) Math.PI * 0.125F;
		} else {
			lid.rotateAngleY = 0F;
		}

		head.rotateAngleX = headPitch * 0.017453292F;
		head.rotateAngleY = netHeadYaw * 0.017453292F;
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		base.render(scale);
		lid.render(scale);
	}

}
