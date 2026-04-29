package net.minecraft.client.model;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;

import java.util.List;

public class ModelRenderer {

	public final String boxName;
	private final ModelBase baseModel;
	/**
	 * The size of the texture file's width in pixels.
	 */
	public float textureWidth;
	/**
	 * The size of the texture file's height in pixels.
	 */
	public float textureHeight;
	public float rotationPointX;
	public float rotationPointY;
	public float rotationPointZ;
	public float rotateAngleX;
	public float rotateAngleY;
	public float rotateAngleZ;
	public boolean mirror;
	public boolean showModel;
	/**
	 * Hides the model.
	 */
	public boolean isHidden;
	public List<ModelBox> cubeList;
	public List<ModelRenderer> childModels;
	public float offsetX;
	public float offsetY;
	public float offsetZ;
	/**
	 * The X offset into the texture used for displaying this model
	 */
	private int textureOffsetX;
	/**
	 * The Y offset into the texture used for displaying this model
	 */
	private int textureOffsetY;
	private boolean compiled;
	/**
	 * The GL display list rendered by the Tessellator for this model
	 */
	private int displayList;

	public ModelRenderer(ModelBase model, String boxNameIn) {

		textureWidth = 64F;
		textureHeight = 32F;
		showModel = true;
		cubeList = Lists.newArrayList();
		baseModel = model;
		model.boxList.add(this);
		boxName = boxNameIn;
		setTextureSize(model.textureWidth, model.textureHeight);
	}

	public ModelRenderer(ModelBase model) {

		this(model, null);
	}

	public ModelRenderer(ModelBase model, int texOffX, int texOffY) {

		this(model);
		setTextureOffset(texOffX, texOffY);
	}

	/**
	 * Sets the current box's rotation points and rotation angles to another box.
	 */
	public void addChild(ModelRenderer renderer) {

		if (childModels == null) {
			childModels = Lists.newArrayList();
		}

		childModels.add(renderer);
	}

	public ModelRenderer setTextureOffset(int x, int y) {

		textureOffsetX = x;
		textureOffsetY = y;
		return this;
	}

	public ModelRenderer addBox(String partName, float offX, float offY, float offZ, int width, int height, int depth) {

		partName = boxName + "." + partName;
		TextureOffset textureoffset = baseModel.getTextureOffset(partName);
		setTextureOffset(textureoffset.textureOffsetX(), textureoffset.textureOffsetY());
		cubeList.add((new ModelBox(this, textureOffsetX, textureOffsetY, offX, offY, offZ, width, height, depth, 0F)).setBoxName(partName));
		return this;
	}

	public ModelRenderer addBox(float offX, float offY, float offZ, int width, int height, int depth) {

		cubeList.add(new ModelBox(this, textureOffsetX, textureOffsetY, offX, offY, offZ, width, height, depth, 0F));
		return this;
	}

	public ModelRenderer addBox(float offX, float offY, float offZ, int width, int height, int depth, boolean mirrored) {

		cubeList.add(new ModelBox(this, textureOffsetX, textureOffsetY, offX, offY, offZ, width, height, depth, 0F, mirrored));
		return this;
	}

	/**
	 * Creates a textured box.
	 */
	public void addBox(float offX, float offY, float offZ, int width, int height, int depth, float scaleFactor) {

		cubeList.add(new ModelBox(this, textureOffsetX, textureOffsetY, offX, offY, offZ, width, height, depth, scaleFactor));
	}

	public void setRotationPoint(float rotationPointXIn, float rotationPointYIn, float rotationPointZIn) {

		rotationPointX = rotationPointXIn;
		rotationPointY = rotationPointYIn;
		rotationPointZ = rotationPointZIn;
	}

	public void render(float scale) {

		if (!isHidden) {
			if (showModel) {
				if (!compiled) {
					compileDisplayList(scale);
				}

				GlStateManager.translate(offsetX, offsetY, offsetZ);

				if (rotateAngleX == 0F && rotateAngleY == 0F && rotateAngleZ == 0F) {
					if (rotationPointX == 0F && rotationPointY == 0F && rotationPointZ == 0F) {
						GlStateManager.callList(displayList);

						if (childModels != null) {
							for (ModelRenderer childModel : childModels) {
								childModel.render(scale);
							}
						}
					} else {
						GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);
						GlStateManager.callList(displayList);

						if (childModels != null) {
							for (ModelRenderer childModel : childModels) {
								childModel.render(scale);
							}
						}

						GlStateManager.translate(-rotationPointX * scale, -rotationPointY * scale, -rotationPointZ * scale);
					}
				} else {
					GlStateManager.pushMatrix();
					GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);

					if (rotateAngleZ != 0F) {
						GlStateManager.rotate(rotateAngleZ * (180F / (float) Math.PI), 0F, 0F, 1F);
					}

					if (rotateAngleY != 0F) {
						GlStateManager.rotate(rotateAngleY * (180F / (float) Math.PI), 0F, 1F, 0F);
					}

					if (rotateAngleX != 0F) {
						GlStateManager.rotate(rotateAngleX * (180F / (float) Math.PI), 1F, 0F, 0F);
					}

					GlStateManager.callList(displayList);

					if (childModels != null) {
						for (ModelRenderer childModel : childModels) {
							childModel.render(scale);
						}
					}

					GlStateManager.popMatrix();
				}

				GlStateManager.translate(-offsetX, -offsetY, -offsetZ);
			}
		}
	}

	public void renderWithRotation(float scale) {

		if (!isHidden) {
			if (showModel) {
				if (!compiled) {
					compileDisplayList(scale);
				}

				GlStateManager.pushMatrix();
				GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);

				if (rotateAngleY != 0F) {
					GlStateManager.rotate(rotateAngleY * (180F / (float) Math.PI), 0F, 1F, 0F);
				}

				if (rotateAngleX != 0F) {
					GlStateManager.rotate(rotateAngleX * (180F / (float) Math.PI), 1F, 0F, 0F);
				}

				if (rotateAngleZ != 0F) {
					GlStateManager.rotate(rotateAngleZ * (180F / (float) Math.PI), 0F, 0F, 1F);
				}

				GlStateManager.callList(displayList);
				GlStateManager.popMatrix();
			}
		}
	}

	/**
	 * Allows the changing of Angles after a box has been rendered
	 */
	public void postRender(float scale) {

		if (!isHidden) {
			if (showModel) {
				if (!compiled) {
					compileDisplayList(scale);
				}

				if (rotateAngleX == 0F && rotateAngleY == 0F && rotateAngleZ == 0F) {
					if (rotationPointX != 0F || rotationPointY != 0F || rotationPointZ != 0F) {
						GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);
					}
				} else {
					GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);

					if (rotateAngleZ != 0F) {
						GlStateManager.rotate(rotateAngleZ * (180F / (float) Math.PI), 0F, 0F, 1F);
					}

					if (rotateAngleY != 0F) {
						GlStateManager.rotate(rotateAngleY * (180F / (float) Math.PI), 0F, 1F, 0F);
					}

					if (rotateAngleX != 0F) {
						GlStateManager.rotate(rotateAngleX * (180F / (float) Math.PI), 1F, 0F, 0F);
					}
				}
			}
		}
	}

	/**
	 * Compiles a GL display list for this model
	 */
	private void compileDisplayList(float scale) {

		displayList = GLAllocation.generateDisplayLists(1);
		GlStateManager.glNewList(displayList, 4864);
		BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

		for (ModelBox modelBox : cubeList) {
			modelBox.render(bufferbuilder, scale);
		}

		GlStateManager.glEndList();
		compiled = true;
	}

	/**
	 * Returns the model renderer with the new texture parameters.
	 */
	public ModelRenderer setTextureSize(int textureWidthIn, int textureHeightIn) {

		textureWidth = (float) textureWidthIn;
		textureHeight = (float) textureHeightIn;
		return this;
	}

}
