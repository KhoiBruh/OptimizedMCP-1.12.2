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

		for (int i = 0; i < LAYERS.size(); i++) {
			String s = LAYERS.get(i);

			if (!blockModel.isTexturePresent(s)) break;

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

	private List<BlockPart> getBlockParts(int tintIndex, String name, TextureAtlasSprite atlasSprite) {

		Map<EnumFacing, BlockPartFace> map = Maps.newHashMap();
		map.put(EnumFacing.SOUTH, new BlockPartFace(null, tintIndex, name, new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0)));
		map.put(EnumFacing.NORTH, new BlockPartFace(null, tintIndex, name, new BlockFaceUV(new float[]{16.0F, 0.0F, 0.0F, 16.0F}, 0)));
		List<BlockPart> list = Lists.newArrayList();
		list.add(new BlockPart(new Vector3f(0.0F, 0.0F, 7.5F), new Vector3f(16.0F, 16.0F, 8.5F), map, null, true));
		list.addAll(getBlockParts(atlasSprite, name, tintIndex));

		return list;
	}

	private List<BlockPart> getBlockParts(TextureAtlasSprite sprite, String key, int layer) {

		List<BlockPart> elements = Lists.newArrayList(); // todo: maybe hoistable?
		int width = sprite.getIconWidth();
		int height = sprite.getIconHeight();

		float xRatio = width >> 4;
		float yRatio = height >> 4;

		int size = 0;

		for (int frame = 0; frame < sprite.getFrameCount(); frame++) {
			int[] textureData = sprite.getFrameTextureData(frame)[0];

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					boolean previous = x - 1 < 0 || isTransparent(textureData, x - 1, y, width, height);
					boolean current = isTransparent(textureData, x, y, width, height);

					if (!current) size++;

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

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					boolean previous = y - 1 < 0 || isTransparent(textureData, x, y - 1, width, height);
					boolean current = isTransparent(textureData, x, y, width, height);

					if (!current) size++;

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

		return new BlockPart(
				new Vector3f(x / xRatio, (height - y) / yRatio, 7.5f),
				new Vector3f((x + 1) / xRatio, (height - (y - size)) / yRatio, 8.5F),
				map, null, true
		);
	}

	private BlockPart horizontalElement(int x, int y, int size, int height, float xRatio, float yRatio, String key, int layer) {

		Map<EnumFacing, BlockPartFace> map = Maps.newHashMap(); // todo: maybe hoistable?
		map.put(EnumFacing.NORTH, new BlockPartFace(null, layer, key, new BlockFaceUV(new float[]{x / xRatio, y / yRatio, (x - size) / xRatio, (y + 1) / yRatio}, 0)));
		map.put(EnumFacing.SOUTH, new BlockPartFace(null, layer, key, new BlockFaceUV(new float[]{(x - size) / xRatio, y / yRatio, x / xRatio, (y + 1) / yRatio}, 0)));
		map.put(EnumFacing.WEST, new BlockPartFace(null, layer, key, new BlockFaceUV(new float[]{(x - size) / xRatio, y / yRatio, (x - size + 1) / xRatio, (y + 1) / yRatio}, 0)));
		map.put(EnumFacing.EAST, new BlockPartFace(null, layer, key, new BlockFaceUV(new float[]{(x - 1) / xRatio, y / yRatio, x / xRatio, (y + 1) / yRatio}, 0)));

		return new BlockPart(
				new Vector3f((x - size) / xRatio, (height - (y + 1)) / yRatio, 7.5f),
				new Vector3f(x / xRatio, (height - y) / yRatio, 8.5F),
				map, null, true
		);
	}

	private boolean isTransparent(int[] textureData, int x, int y, int width, int height) {

		if (x >= 0 && y >= 0 && x < width && y < height) {
			return (textureData[y * width + x] >> 24 & 255) == 0;
		} else {
			return true;
		}
	}

}
