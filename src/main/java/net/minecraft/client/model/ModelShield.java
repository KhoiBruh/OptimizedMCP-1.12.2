package net.minecraft.client.model;

public class ModelShield extends ModelBase {

	public ModelRenderer plate;
	public ModelRenderer handle;

	public ModelShield() {

		textureWidth = 64;
		textureHeight = 64;
		plate = new ModelRenderer(this, 0, 0);
		plate.addBox(-6F, -11F, -2F, 12, 22, 1, 0F);
		handle = new ModelRenderer(this, 26, 0);
		handle.addBox(-1F, -3F, -1F, 2, 6, 6, 0F);
	}

	public void render() {

		plate.render(0.0625F);
		handle.render(0.0625F);
	}

}
