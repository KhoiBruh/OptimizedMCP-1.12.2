package net.minecraft.client.renderer.texture;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Maths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.renderer.NativeImage;
import java.io.IOException;
import java.util.List;

public class LayeredColorMaskTexture extends AbstractTexture {

	/**
	 * Access to the Logger, for all your logging needs.
	 */
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * The location of the texture.
	 */
	private final ResourceLocation textureLocation;
	private final List<String> listTextures;
	private final List<DyeColor> listDyeColors;

	public LayeredColorMaskTexture(ResourceLocation textureLocationIn, List<String> p_i46101_2_, List<DyeColor> p_i46101_3_) {
		textureLocation = textureLocationIn;
		listTextures = p_i46101_2_;
		listDyeColors = p_i46101_3_;
	}

	public void loadTexture(IResourceManager resourceManager) {
		deleteGlTexture();
		NativeImage bufferedimage;
		label255:
		{
			try (IResource iresource = resourceManager.getResource(textureLocation)) {
				NativeImage bufferedimage1 = TextureUtil.readImage(iresource.getInputStream());
				int i = bufferedimage1.getFormat();

				bufferedimage = new NativeImage(bufferedimage1.getWidth(), bufferedimage1.getHeight(), i == NativeImage.FORMAT_RGBA);
				bufferedimage.drawImage(bufferedimage1, 0, 0);
				// Need bufferedimage1 later for l1 = bufferedimage1.getPixel(i1, l);
				int j = 0;

				while (true) {
					if (j >= 17 || j >= listTextures.size() || j >= listDyeColors.size()) {
						bufferedimage1.close();
						break label255;
					}

					try (IResource iresource1 = resourceManager.getResource(new ResourceLocation(listTextures.get(j)))) {
						String s = listTextures.get(j);
						int k = listDyeColors.get(j).getColorValue();

						if (s != null) {
							NativeImage bufferedimage2 = TextureUtil.readImage(iresource1.getInputStream());

							if (bufferedimage2.getWidth() == bufferedimage.getWidth() && bufferedimage2.getHeight() == bufferedimage.getHeight() && bufferedimage2.getFormat() == NativeImage.FORMAT_RGBA) {
								for (int l = 0; l < bufferedimage2.getHeight(); ++l) {
									for (int i1 = 0; i1 < bufferedimage2.getWidth(); ++i1) {
										int j1 = bufferedimage2.getPixel(i1, l);

										if ((j1 & -16777216) != 0) {
											int k1 = (j1 & 16711680) << 8 & -16777216;
											int l1 = bufferedimage1.getPixel(i1, l);
											int i2 = Maths.multiplyColor(l1, k) & 16777215;
											bufferedimage2.setPixel(i1, l, k1 | i2);
										}
									}
								}

								bufferedimage.drawImage(bufferedimage2, 0, 0);
								bufferedimage2.close();
							}
						}
					}

					++j;
				}
			} catch (IOException ioexception) {
				LOGGER.error("Couldn't load layered image", ioexception);
			}

			return;
		}
		if (bufferedimage != null) {
			TextureUtil.uploadTextureImage(getGlTextureId(), bufferedimage);
			bufferedimage.close();
		}
		
	}

}
