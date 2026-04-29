package net.minecraft.client.model;

public class ModelSign extends ModelBase {

	/**
	 * The board on a sign that has the writing on it.
	 */
	public ModelRenderer signBoard = new ModelRenderer(this, 0, 0);

	/**
	 * The stick a sign stands on.
	 */
	public ModelRenderer signStick;

	public ModelSign() {

		signBoard.addBox(-12F, -14F, -1F, 24, 12, 2, 0F);
		signStick = new ModelRenderer(this, 0, 14);
		signStick.addBox(-1F, -2F, -1F, 2, 14, 2, 0F);
	}

	/**
	 * Renders the sign model through TileEntitySignRenderer
	 */
	public void renderSign() {

		signBoard.render(0.0625F);
		signStick.render(0.0625F);
	}

}
