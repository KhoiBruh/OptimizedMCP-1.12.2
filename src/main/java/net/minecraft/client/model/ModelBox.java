package net.minecraft.client.model;

import net.minecraft.client.renderer.BufferBuilder;

public class ModelBox {

	/**
	 * X vertex coordinate of lower box corner
	 */
	public final float posX1;
	/**
	 * Y vertex coordinate of lower box corner
	 */
	public final float posY1;
	/**
	 * Z vertex coordinate of lower box corner
	 */
	public final float posZ1;
	/**
	 * X vertex coordinate of upper box corner
	 */
	public final float posX2;
	/**
	 * Y vertex coordinate of upper box corner
	 */
	public final float posY2;
	/**
	 * Z vertex coordinate of upper box corner
	 */
	public final float posZ2;
	/**
	 * The (x,y,z) vertex positions and (u,v) texture coordinates for each of the 8 points on a cube
	 */
	private final PositionTextureVertex[] vertexPositions;
	/**
	 * An array of 6 TexturedQuads, one for each face of a cube
	 */
	private final TexturedQuad[] quadList;
	public String boxName;

	public ModelBox(ModelRenderer renderer, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta) {

		this(renderer, texU, texV, x, y, z, dx, dy, dz, delta, renderer.mirror);
	}

	public ModelBox(ModelRenderer renderer, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror) {

		posX1 = x;
		posY1 = y;
		posZ1 = z;
		posX2 = x + (float) dx;
		posY2 = y + (float) dy;
		posZ2 = z + (float) dz;
		vertexPositions = new PositionTextureVertex[8];
		quadList = new TexturedQuad[6];
		float f = x + (float) dx;
		float f1 = y + (float) dy;
		float f2 = z + (float) dz;
		x = x - delta;
		y = y - delta;
		z = z - delta;
		f = f + delta;
		f1 = f1 + delta;
		f2 = f2 + delta;

		if (mirror) {
			float f3 = f;
			f = x;
			x = f3;
		}

		PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0F, 0F);
		PositionTextureVertex positiontexturevertex = new PositionTextureVertex(f, y, z, 0F, 8F);
		PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(f, f1, z, 8F, 8F);
		PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(x, f1, z, 8F, 0F);
		PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(x, y, f2, 0F, 0F);
		PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(f, y, f2, 0F, 8F);
		PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(f, f1, f2, 8F, 8F);
		PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(x, f1, f2, 8F, 0F);
		vertexPositions[0] = positiontexturevertex7;
		vertexPositions[1] = positiontexturevertex;
		vertexPositions[2] = positiontexturevertex1;
		vertexPositions[3] = positiontexturevertex2;
		vertexPositions[4] = positiontexturevertex3;
		vertexPositions[5] = positiontexturevertex4;
		vertexPositions[6] = positiontexturevertex5;
		vertexPositions[7] = positiontexturevertex6;
		quadList[0] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, texU + dz + dx, texV + dz, texU + dz + dx + dz, texV + dz + dy, renderer.textureWidth, renderer.textureHeight);
		quadList[1] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, texU, texV + dz, texU + dz, texV + dz + dy, renderer.textureWidth, renderer.textureHeight);
		quadList[2] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, texU + dz, texV, texU + dz + dx, texV + dz, renderer.textureWidth, renderer.textureHeight);
		quadList[3] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, texU + dz + dx, texV + dz, texU + dz + dx + dx, texV, renderer.textureWidth, renderer.textureHeight);
		quadList[4] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, texU + dz, texV + dz, texU + dz + dx, texV + dz + dy, renderer.textureWidth, renderer.textureHeight);
		quadList[5] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex3, positiontexturevertex4, positiontexturevertex5, positiontexturevertex6}, texU + dz + dx + dz, texV + dz, texU + dz + dx + dz + dx, texV + dz + dy, renderer.textureWidth, renderer.textureHeight);

		if (mirror) {
			for (TexturedQuad texturedquad : quadList) {
				texturedquad.flipFace();
			}
		}
	}

	public void render(BufferBuilder renderer, float scale) {

		for (TexturedQuad texturedquad : quadList) {
			texturedquad.draw(renderer, scale);
		}
	}

	public ModelBox setBoxName(String name) {

		boxName = name;
		return this;
	}

}
