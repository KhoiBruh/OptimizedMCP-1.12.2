package net.minecraft.client.renderer.block.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Facing;

import java.util.List;

public interface IBakedModel {

	List<BakedQuad> getQuads(IBlockState state, Facing side, long rand);

	boolean isAmbientOcclusion();

	boolean isGui3d();

	boolean isBuiltInRenderer();

	TextureAtlasSprite getParticleTexture();

	ItemCameraTransforms getItemCameraTransforms();

	ItemOverrideList getOverrides();

}
