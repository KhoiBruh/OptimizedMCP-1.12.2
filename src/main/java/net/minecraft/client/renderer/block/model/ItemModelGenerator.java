package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ItemModelGenerator {

	public static final List<String> LAYERS = Lists.newArrayList("layer0", "layer1", "layer2", "layer3", "layer4");

	@Nullable
	public ModelBlock makeItemModel(TextureMap textureMapIn, ModelBlock blockModel) {

		Map<String, String> map = Maps.newHashMap();
		List<BlockPart> list = Lists.newArrayList();

		for (int i = 0; i < LAYERS.size(); ++i) {
			String s = LAYERS.get(i);

			if (!blockModel.isTexturePresent(s)) {
				break;
			}

			String s1 = blockModel.resolveTextureName(s);
			map.put(s, s1);
			TextureAtlasSprite textureatlassprite = textureMapIn.getAtlasSprite((new ResourceLocation(s1)).toString());
			list.addAll(getBlockParts(i, s, textureatlassprite));
		}

		if (list.isEmpty()) {
			return null;
		} else {
			map.put("particle", blockModel.isTexturePresent("particle") ? blockModel.resolveTextureName("particle") : map.get("layer0"));
			return new ModelBlock(null, list, map, false, false, blockModel.getAllTransforms(), blockModel.getOverrides());
		}
	}

	private List<BlockPart> getBlockParts(int tintIndex, String p_178394_2_, TextureAtlasSprite p_178394_3_) {

		Map<EnumFacing, BlockPartFace> map = Maps.newHashMap();
		map.put(EnumFacing.SOUTH, new BlockPartFace(null, tintIndex, p_178394_2_, new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0)));
		map.put(EnumFacing.NORTH, new BlockPartFace(null, tintIndex, p_178394_2_, new BlockFaceUV(new float[]{16.0F, 0.0F, 0.0F, 16.0F}, 0)));
		List<BlockPart> list = Lists.newArrayList();
		list.add(new BlockPart(new Vector3f(0.0F, 0.0F, 7.5F), new Vector3f(16.0F, 16.0F, 8.5F), map, null, true));
		list.addAll(getBlockParts(p_178394_3_, p_178394_2_, tintIndex));
		return list;
	}

	private List<BlockPart> getBlockParts(TextureAtlasSprite sprite, String key, int layer) {
		List<BlockPart> elements = Lists.newArrayList(); // todo: maybe hoistable?
		int width = sprite.getIconWidth();
		int height = sprite.getIconHeight();

		float xRatio = width >> 4;
		float yRatio = height >> 4;

		int size = 0;

		for (int frameCount = 0; frameCount < sprite.getFrameCount(); ++frameCount) {
			int[] textureData = sprite.getFrameTextureData(frameCount)[0];

			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x) {
					boolean previous = x - 1 < 0 || isTransparent(textureData, x - 1, y, width, height);
					boolean current = isTransparent(textureData, x, y, width, height);

					if (!current) {
						++size;
					}

					if (!previous && current) {
						elements.add(horizontalElement(x, y, size, height, xRatio, yRatio, key, layer));
						size = 0;
					}
				}

				if (size != 0) {
					elements.add(horizontalElement(width, y, size, height, xRatio, yRatio, key, layer));
					size = 0;
				}
			}

			for (int x = 0; x < width; ++x) {
				for (int y = 0; y < height; ++y) {
					boolean previous = y - 1 < 0 || isTransparent(textureData, x, y - 1, width, height);
					boolean current = isTransparent(textureData, x, y, width, height);

					if (!current) {
						++size;
					}

					if (!previous && current) {
						elements.add(verticalElement(x, y, size, height, xRatio, yRatio, key, layer));
						size = 0;
					}
				}

				if (size != 0) {
					elements.add(verticalElement(x, height, size, height, xRatio, yRatio, key, layer));
					size = 0;
				}
			}
		}

		return elements;
	}

	private BlockPart verticalElement(int x, int y, int size, int height, float xRatio, float yRatio, String key, int layer) {
		Map<EnumFacing, BlockPartFace> map = Maps.newHashMap(); // todo: maybe hoistable?
		map.put(EnumFacing.UP, new BlockPartFace(null, layer, key, new BlockFaceUV(new float[]{x / xRatio, (y - size) / yRatio, (x + 1) / xRatio, (y - size + 1) / yRatio}, 0)));
		map.put(EnumFacing.DOWN, new BlockPartFace(null, layer, key, new BlockFaceUV(new float[]{x / xRatio, (y - 1) / yRatio, (x + 1) / xRatio, y / yRatio}, 0)));
		return new BlockPart(new Vector3f(x / xRatio, (height - y) / yRatio, 7.5f), new Vector3f((x + 1) / xRatio, (height - (y - size)) / yRatio, 8.5F), map, null, true);
	}

	private BlockPart horizontalElement(int x, int y, int size, int height, float xRatio, float yRatio, String key, int layer) {
		Map<EnumFacing, BlockPartFace> map = Maps.newHashMap(); // todo: maybe hoistable?
		map.put(EnumFacing.NORTH, new BlockPartFace(null, layer, key, new BlockFaceUV(new float[]{x / xRatio, y / yRatio, (x - size) / xRatio, (y + 1) / yRatio}, 0)));
		map.put(EnumFacing.SOUTH, new BlockPartFace(null, layer, key, new BlockFaceUV(new float[]{(x - size) / xRatio, y / yRatio, x / xRatio, (y + 1) / yRatio}, 0)));
		map.put(EnumFacing.WEST, new BlockPartFace(null, layer, key, new BlockFaceUV(new float[]{(x - size) / xRatio, y / yRatio, (x - size + 1) / xRatio, (y + 1) / yRatio}, 0)));
		map.put(EnumFacing.EAST, new BlockPartFace(null, layer, key, new BlockFaceUV(new float[]{(x - 1) / xRatio, y / yRatio, x / xRatio, (y + 1) / yRatio}, 0)));
		return new BlockPart(new Vector3f((x - size) / xRatio, (height - (y + 1)) / yRatio, 7.5f), new Vector3f(x / xRatio, (height - y) / yRatio, 8.5F), map, null, true);
	}

	/*private List<ItemModelGenerator.Span> getSpans(TextureAtlasSprite p_178393_1_) {

		int i = p_178393_1_.getIconWidth();
		int j = p_178393_1_.getIconHeight();
		List<ItemModelGenerator.Span> list = Lists.newArrayList();

		for (int k = 0; k < p_178393_1_.getFrameCount(); ++k) {
			int[] aint = p_178393_1_.getFrameTextureData(k)[0];

			for (int l = 0; l < j; ++l) {
				for (int i1 = 0; i1 < i; ++i1) {
					boolean flag = !isTransparent(aint, i1, l, i, j);
					checkTransition(ItemModelGenerator.SpanFacing.UP, list, aint, i1, l, i, j, flag);
					checkTransition(ItemModelGenerator.SpanFacing.DOWN, list, aint, i1, l, i, j, flag);
					checkTransition(ItemModelGenerator.SpanFacing.LEFT, list, aint, i1, l, i, j, flag);
					checkTransition(ItemModelGenerator.SpanFacing.RIGHT, list, aint, i1, l, i, j, flag);
				}
			}
		}

		return list;
	}

	private void checkTransition(ItemModelGenerator.SpanFacing p_178396_1_, List<ItemModelGenerator.Span> p_178396_2_, int[] p_178396_3_, int p_178396_4_, int p_178396_5_, int p_178396_6_, int p_178396_7_, boolean p_178396_8_) {

		boolean flag = isTransparent(p_178396_3_, p_178396_4_ + p_178396_1_.getXOffset(), p_178396_5_ + p_178396_1_.getYOffset(), p_178396_6_, p_178396_7_) && p_178396_8_;

		if (flag) {
			createOrExpandSpan(p_178396_2_, p_178396_1_, p_178396_4_, p_178396_5_);
		}
	}

	private void createOrExpandSpan(List<ItemModelGenerator.Span> p_178395_1_, ItemModelGenerator.SpanFacing p_178395_2_, int p_178395_3_, int p_178395_4_) {

		ItemModelGenerator.Span itemmodelgenerator$span = null;

		for (ItemModelGenerator.Span itemmodelgenerator$span1 : p_178395_1_) {
			if (itemmodelgenerator$span1.getFacing() == p_178395_2_) {
				int i = p_178395_2_.isHorizontal() ? p_178395_4_ : p_178395_3_;

				if (itemmodelgenerator$span1.getAnchor() == i) {
					itemmodelgenerator$span = itemmodelgenerator$span1;
					break;
				}
			}
		}

		int j = p_178395_2_.isHorizontal() ? p_178395_4_ : p_178395_3_;
		int k = p_178395_2_.isHorizontal() ? p_178395_3_ : p_178395_4_;

		if (itemmodelgenerator$span == null) {
			p_178395_1_.add(new ItemModelGenerator.Span(p_178395_2_, k, j));
		} else {
			itemmodelgenerator$span.expand(k);
		}
	}

	}*/

	private boolean isTransparent(int[] p_178391_1_, int p_178391_2_, int p_178391_3_, int p_178391_4_, int p_178391_5_) {

		if (p_178391_2_ >= 0 && p_178391_3_ >= 0 && p_178391_2_ < p_178391_4_ && p_178391_3_ < p_178391_5_) {
			return (p_178391_1_[p_178391_3_ * p_178391_4_ + p_178391_2_] >> 24 & 255) == 0;
		} else {
			return true;
		}
	}

	/*enum SpanFacing {
		UP(EnumFacing.UP, 0, -1),
		DOWN(EnumFacing.DOWN, 0, 1),
		LEFT(EnumFacing.EAST, -1, 0),
		RIGHT(EnumFacing.WEST, 1, 0);

		private final EnumFacing facing;
		private final int xOffset;
		private final int yOffset;

		SpanFacing(EnumFacing facing, int p_i46215_4_, int p_i46215_5_) {

			this.facing = facing;
			xOffset = p_i46215_4_;
			yOffset = p_i46215_5_;
		}

		public EnumFacing getFacing() {

			return facing;
		}

		public int getXOffset() {

			return xOffset;
		}

		public int getYOffset() {

			return yOffset;
		}

		private boolean isHorizontal() {

			return this == DOWN || this == UP;
		}
	}

	static class Span {

		private final ItemModelGenerator.SpanFacing spanFacing;
		private final int anchor;
		private int min;
		private int max;

		public Span(ItemModelGenerator.SpanFacing spanFacingIn, int p_i46216_2_, int p_i46216_3_) {

			spanFacing = spanFacingIn;
			min = p_i46216_2_;
			max = p_i46216_2_;
			anchor = p_i46216_3_;
		}

		public void expand(int p_178382_1_) {

			if (p_178382_1_ < min) {
				min = p_178382_1_;
			} else if (p_178382_1_ > max) {
				max = p_178382_1_;
			}
		}

		public ItemModelGenerator.SpanFacing getFacing() {

			return spanFacing;
		}

		public int getMin() {

			return min;
		}

		public int getMax() {

			return max;
		}

		public int getAnchor() {

			return anchor;
		}

	}*/

}
