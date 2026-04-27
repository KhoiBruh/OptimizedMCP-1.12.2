package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.StitcherException;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TextureMap extends AbstractTexture implements ITickableTextureObject {

	public static final ResourceLocation LOCATION_MISSING_TEXTURE = new ResourceLocation("missingno");
	public static final ResourceLocation LOCATION_BLOCKS_TEXTURE = new ResourceLocation("textures/atlas/blocks.png");
	private static final Logger LOGGER = LogManager.getLogger();
	private final List<TextureAtlasSprite> listAnimatedSprites;
	private final Map<String, TextureAtlasSprite> mapRegisteredSprites;
	private final Map<String, TextureAtlasSprite> mapUploadedSprites;
	private final String basePath;
	private final ITextureMapPopulator iconCreator;
	private final TextureAtlasSprite missingImage;
	private int mipmapLevels;

	public TextureMap(String basePathIn) {

		this(basePathIn, null);
	}

	public TextureMap(String basePathIn, @Nullable ITextureMapPopulator iconCreatorIn) {

		listAnimatedSprites = Lists.newArrayList();
		mapRegisteredSprites = Maps.newHashMap();
		mapUploadedSprites = Maps.newHashMap();
		missingImage = new TextureAtlasSprite("missingno");
		basePath = basePathIn;
		iconCreator = iconCreatorIn;
	}

	private void initMissingImage() {

		int[] aint = TextureUtil.MISSING_TEXTURE_DATA;
		missingImage.setIconWidth(16);
		missingImage.setIconHeight(16);
		int[][] aint1 = new int[mipmapLevels + 1][];
		aint1[0] = aint;
		missingImage.setFramesTextureData(Lists.<int[][]>newArrayList(aint1));
	}

	public void loadTexture(IResourceManager resourceManager) {

		if (iconCreator != null) {
			loadSprites(resourceManager, iconCreator);
		}
	}

	public void loadSprites(IResourceManager resourceManager, ITextureMapPopulator iconCreatorIn) {

		mapRegisteredSprites.clear();
		iconCreatorIn.registerSprites(this);
		initMissingImage();
		deleteGlTexture();
		loadTextureAtlas(resourceManager);
	}

	public void loadTextureAtlas(IResourceManager resourceManager) {

		int i = Minecraft.getGLMaximumTextureSize();
		Stitcher stitcher = new Stitcher(i, i, 0, mipmapLevels);
		mapUploadedSprites.clear();
		listAnimatedSprites.clear();
		int j = Integer.MAX_VALUE;
		int k = 1 << mipmapLevels;

		for (Entry<String, TextureAtlasSprite> entry : mapRegisteredSprites.entrySet()) {
			TextureAtlasSprite textureatlassprite = entry.getValue();
			ResourceLocation resourcelocation = getResourceLocation(textureatlassprite);
			IResource iresource = null;

			try {
				PngSizeInfo pngsizeinfo = PngSizeInfo.makeFromResource(resourceManager.getResource(resourcelocation));
				iresource = resourceManager.getResource(resourcelocation);
				boolean flag = iresource.getMetadata("animation") != null;
				textureatlassprite.loadSprite(pngsizeinfo, flag);
			} catch (RuntimeException runtimeexception) {
				LOGGER.error("Unable to parse metadata from {}", resourcelocation, runtimeexception);
				continue;
			} catch (IOException ioexception) {
				LOGGER.error("Using missing texture, unable to load {}", resourcelocation, ioexception);
				continue;
			} finally {
				IOUtils.closeQuietly(iresource);
			}

			j = Math.min(j, Math.min(textureatlassprite.getIconWidth(), textureatlassprite.getIconHeight()));
			int j1 = Math.min(Integer.lowestOneBit(textureatlassprite.getIconWidth()), Integer.lowestOneBit(textureatlassprite.getIconHeight()));

			if (j1 < k) {
				LOGGER.warn("Texture {} with size {}x{} limits mip level from {} to {}", resourcelocation, textureatlassprite.getIconWidth(), textureatlassprite.getIconHeight(), MathHelper.log2(k), MathHelper.log2(j1));
				k = j1;
			}

			stitcher.addSprite(textureatlassprite);
		}

		int l = Math.min(j, k);
		int i1 = MathHelper.log2(l);

		if (i1 < mipmapLevels) {
			LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", basePath, mipmapLevels, i1, l);
			mipmapLevels = i1;
		}

		missingImage.generateMipmaps(mipmapLevels);
		stitcher.addSprite(missingImage);

		try {
			stitcher.doStitch();
		} catch (StitcherException stitcherexception) {
			throw stitcherexception;
		}

		LOGGER.info("Created: {}x{} {}-atlas", stitcher.getCurrentWidth(), stitcher.getCurrentHeight(), basePath);
		TextureUtil.allocateTextureImpl(getGlTextureId(), mipmapLevels, stitcher.getCurrentWidth(), stitcher.getCurrentHeight());
		Map<String, TextureAtlasSprite> map = Maps.newHashMap(mapRegisteredSprites);

		for (TextureAtlasSprite textureatlassprite1 : stitcher.getStichSlots()) {
			if (textureatlassprite1 == missingImage || generateMipmaps(resourceManager, textureatlassprite1)) {
				String s = textureatlassprite1.getIconName();
				map.remove(s);
				mapUploadedSprites.put(s, textureatlassprite1);

				try {
					TextureUtil.uploadTextureMipmap(textureatlassprite1.getFrameTextureData(0), textureatlassprite1.getIconWidth(), textureatlassprite1.getIconHeight(), textureatlassprite1.getOriginX(), textureatlassprite1.getOriginY(), false, false);
				} catch (Throwable throwable) {
					CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Stitching texture atlas");
					CrashReportCategory crashreportcategory = crashreport.makeCategory("Texture being stitched together");
					crashreportcategory.addCrashSection("Atlas path", basePath);
					crashreportcategory.addCrashSection("Sprite", textureatlassprite1);
					throw new ReportedException(crashreport);
				}

				if (textureatlassprite1.hasAnimationMetadata()) {
					listAnimatedSprites.add(textureatlassprite1);
				}
			}
		}

		for (TextureAtlasSprite textureatlassprite2 : map.values()) {
			textureatlassprite2.copyFrom(missingImage);
		}
	}

	private boolean generateMipmaps(IResourceManager resourceManager, final TextureAtlasSprite texture) {

		ResourceLocation resourcelocation = getResourceLocation(texture);
		IResource iresource = null;
		label62:
		{
			boolean flag;

			try {
				iresource = resourceManager.getResource(resourcelocation);
				texture.loadSpriteFrames(iresource, mipmapLevels + 1);
				break label62;
			} catch (RuntimeException runtimeexception) {
				LOGGER.error("Unable to parse metadata from {}", resourcelocation, runtimeexception);
				flag = false;
			} catch (IOException ioexception) {
				LOGGER.error("Using missing texture, unable to load {}", resourcelocation, ioexception);
				flag = false;
				return flag;
			} finally {
				IOUtils.closeQuietly(iresource);
			}

			return flag;
		}

		try {
			texture.generateMipmaps(mipmapLevels);
			return true;
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Applying mipmap");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Sprite being mipmapped");
			crashreportcategory.addDetail("Sprite name", texture::getIconName);
			crashreportcategory.addDetail("Sprite size", () -> texture.getIconWidth() + " x " + texture.getIconHeight());
			crashreportcategory.addDetail("Sprite frames", () -> texture.getFrameCount() + " frames");
			crashreportcategory.addCrashSection("Mipmap levels", mipmapLevels);
			throw new ReportedException(crashreport);
		}
	}

	private ResourceLocation getResourceLocation(TextureAtlasSprite p_184396_1_) {

		ResourceLocation resourcelocation = new ResourceLocation(p_184396_1_.getIconName());
		return new ResourceLocation(resourcelocation.getResourceDomain(), String.format("%s/%s%s", basePath, resourcelocation.getResourcePath(), ".png"));
	}

	public TextureAtlasSprite getAtlasSprite(String iconName) {

		TextureAtlasSprite textureatlassprite = mapUploadedSprites.get(iconName);

		if (textureatlassprite == null) {
			textureatlassprite = missingImage;
		}

		return textureatlassprite;
	}

	public void updateAnimations() {

		TextureUtil.bindTexture(getGlTextureId());

		for (TextureAtlasSprite textureatlassprite : listAnimatedSprites) {
			textureatlassprite.updateAnimation();
		}
	}

	public TextureAtlasSprite registerSprite(ResourceLocation location) {

		if (location == null) {
			throw new IllegalArgumentException("Location cannot be null!");
		} else {
			TextureAtlasSprite textureatlassprite = mapRegisteredSprites.get(location);

			if (textureatlassprite == null) {
				textureatlassprite = TextureAtlasSprite.makeAtlasSprite(location);
				mapRegisteredSprites.put(location.toString(), textureatlassprite);
			}

			return textureatlassprite;
		}
	}

	public void tick() {

		updateAnimations();
	}

	public void setMipmapLevels(int mipmapLevelsIn) {

		mipmapLevels = mipmapLevelsIn;
	}

	public TextureAtlasSprite getMissingSprite() {

		return missingImage;
	}

}
