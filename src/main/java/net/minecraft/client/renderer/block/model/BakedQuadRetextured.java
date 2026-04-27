package net.minecraft.client.renderer.block.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.Arrays;

public class BakedQuadRetextured extends BakedQuad {

	private final TextureAtlasSprite texture;

	public BakedQuadRetextured(BakedQuad quad, TextureAtlasSprite textureIn) {

		super(Arrays.copyOf(quad.getVertexData(), quad.getVertexData().length), quad.tintIndex, FaceBakery.getFacingFromVertexData(quad.getVertexData()), quad.getSprite());
		texture = textureIn;
		remapQuad();
	}

	private void remapQuad() {

		for (int i = 0; i < 4; ++i) {
			int j = 7 * i;
			vertexData[j + 4] = Float.floatToRawIntBits(texture.getInterpolatedU(sprite.getUnInterpolatedU(Float.intBitsToFloat(vertexData[j + 4]))));
			vertexData[j + 4 + 1] = Float.floatToRawIntBits(texture.getInterpolatedV(sprite.getUnInterpolatedV(Float.intBitsToFloat(vertexData[j + 4 + 1]))));
		}
	}

}
