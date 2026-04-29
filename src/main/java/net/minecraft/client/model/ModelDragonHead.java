package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

public class ModelDragonHead extends ModelBase {

	private final ModelRenderer head;
	private final ModelRenderer jaw;

	public ModelDragonHead(float p_i46588_1_) {

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
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		jaw.rotateAngleX = (float) (Math.sin(limbSwing * (float) Math.PI * 0.2F) + 1D) * 0.2F;
		head.rotateAngleY = netHeadYaw * 0.017453292F;
		head.rotateAngleX = headPitch * 0.017453292F;
		GlStateManager.translate(0F, -0.374375F, 0F);
		GlStateManager.scale(0.75F, 0.75F, 0.75F);
		head.render(scale);
	}

}
