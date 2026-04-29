package net.minecraft.client.renderer.block.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Facing;

public class BakedQuad {

	/**
	 * Joined 4 vertex records, each stores packed data according to the VertexFormat of the quad. Vanilla minecraft
	 * uses DefaultVertexFormats.BLOCK, Forge uses (usually) ITEM, use BakedQuad.getFormat() to get the correct format.
	 */
	protected final int[] vertexData;
	protected final int tintIndex;
	protected final Facing face;
	protected final TextureAtlasSprite sprite;

	public BakedQuad(int[] vertexDataIn, int tintIndexIn, Facing faceIn, TextureAtlasSprite spriteIn) {

		vertexData = vertexDataIn;
		tintIndex = tintIndexIn;
		face = faceIn;
		sprite = spriteIn;
	}

	public TextureAtlasSprite getSprite() {

		return sprite;
	}

	public int[] getVertexData() {

		return vertexData;
	}

	public boolean hasTintIndex() {

		return tintIndex != -1;
	}

	public int getTintIndex() {

		return tintIndex;
	}

	public Facing getFace() {

		return face;
	}

}
