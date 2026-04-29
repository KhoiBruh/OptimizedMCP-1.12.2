package net.minecraft.client.model;

public class ModelBanner extends ModelBase {

	public ModelRenderer bannerSlate;
	public ModelRenderer bannerStand;
	public ModelRenderer bannerTop;

	public ModelBanner() {

		textureWidth = 64;
		textureHeight = 64;
		bannerSlate = new ModelRenderer(this, 0, 0);
		bannerSlate.addBox(-10F, 0F, -2F, 20, 40, 1, 0F);
		bannerStand = new ModelRenderer(this, 44, 0);
		bannerStand.addBox(-1F, -30F, -1F, 2, 42, 2, 0F);
		bannerTop = new ModelRenderer(this, 0, 42);
		bannerTop.addBox(-10F, -32F, -1F, 20, 2, 2, 0F);
	}

	/**
	 * Renders the banner model in.
	 */
	public void renderBanner() {

		bannerSlate.rotationPointY = -32F;
		bannerSlate.render(0.0625F);
		bannerStand.render(0.0625F);
		bannerTop.render(0.0625F);
	}

}
