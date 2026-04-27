package net.minecraft.client.model;

public class ModelBed extends ModelBase {

	public ModelRenderer headPiece;
	public ModelRenderer footPiece;
	public ModelRenderer[] legs = new ModelRenderer[4];

	public ModelBed() {

		textureWidth = 64;
		textureHeight = 64;
		headPiece = new ModelRenderer(this, 0, 0);
		headPiece.addBox(0.0F, 0.0F, 0.0F, 16, 16, 6, 0.0F);
		footPiece = new ModelRenderer(this, 0, 22);
		footPiece.addBox(0.0F, 0.0F, 0.0F, 16, 16, 6, 0.0F);
		legs[0] = new ModelRenderer(this, 50, 0);
		legs[1] = new ModelRenderer(this, 50, 6);
		legs[2] = new ModelRenderer(this, 50, 12);
		legs[3] = new ModelRenderer(this, 50, 18);
		legs[0].addBox(0.0F, 6.0F, -16.0F, 3, 3, 3);
		legs[1].addBox(0.0F, 6.0F, 0.0F, 3, 3, 3);
		legs[2].addBox(-16.0F, 6.0F, -16.0F, 3, 3, 3);
		legs[3].addBox(-16.0F, 6.0F, 0.0F, 3, 3, 3);
		legs[0].rotateAngleX = ((float) Math.PI / 2F);
		legs[1].rotateAngleX = ((float) Math.PI / 2F);
		legs[2].rotateAngleX = ((float) Math.PI / 2F);
		legs[3].rotateAngleX = ((float) Math.PI / 2F);
		legs[0].rotateAngleZ = 0.0F;
		legs[1].rotateAngleZ = ((float) Math.PI / 2F);
		legs[2].rotateAngleZ = ((float) Math.PI * 3F / 2F);
		legs[3].rotateAngleZ = (float) Math.PI;
	}

	public int getModelVersion() {

		return 51;
	}

	public void render() {

		headPiece.render(0.0625F);
		footPiece.render(0.0625F);
		legs[0].render(0.0625F);
		legs[1].render(0.0625F);
		legs[2].render(0.0625F);
		legs[3].render(0.0625F);
	}

	public void preparePiece(boolean p_193769_1_) {

		headPiece.showModel = p_193769_1_;
		footPiece.showModel = !p_193769_1_;
		legs[0].showModel = !p_193769_1_;
		legs[1].showModel = p_193769_1_;
		legs[2].showModel = !p_193769_1_;
		legs[3].showModel = p_193769_1_;
	}

}
