package net.minecraft.client.model;

public class ModelPig extends ModelQuadruped {

	public ModelPig() {

		this(0F);
	}

	public ModelPig(float scale) {

		super(6, scale);
		head.setTextureOffset(16, 16).addBox(-2F, 0F, -9F, 4, 3, 1, scale);
		childYOffset = 4F;
	}

}
