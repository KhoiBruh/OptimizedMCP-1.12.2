package net.minecraft.client.model;

public class ModelChest extends ModelBase {

	/**
	 * The chest lid in the chest's model.
	 */
	public ModelRenderer chestLid = (new ModelRenderer(this, 0, 0)).setTextureSize(64, 64);

	/**
	 * The model of the bottom of the chest.
	 */
	public ModelRenderer chestBelow;

	/**
	 * The chest's knob in the chest model.
	 */
	public ModelRenderer chestKnob;

	public ModelChest() {

		chestLid.addBox(0F, -5F, -14F, 14, 5, 14, 0F);
		chestLid.rotationPointX = 1F;
		chestLid.rotationPointY = 7F;
		chestLid.rotationPointZ = 15F;
		chestKnob = (new ModelRenderer(this, 0, 0)).setTextureSize(64, 64);
		chestKnob.addBox(-1F, -2F, -15F, 2, 4, 1, 0F);
		chestKnob.rotationPointX = 8F;
		chestKnob.rotationPointY = 7F;
		chestKnob.rotationPointZ = 15F;
		chestBelow = (new ModelRenderer(this, 0, 19)).setTextureSize(64, 64);
		chestBelow.addBox(0F, 0F, 0F, 14, 10, 14, 0F);
		chestBelow.rotationPointX = 1F;
		chestBelow.rotationPointY = 6F;
		chestBelow.rotationPointZ = 1F;
	}

	/**
	 * This method renders out all parts of the chest model.
	 */
	public void renderAll() {

		chestKnob.rotateAngleX = chestLid.rotateAngleX;
		chestLid.render(0.0625F);
		chestKnob.render(0.0625F);
		chestBelow.render(0.0625F);
	}

}
