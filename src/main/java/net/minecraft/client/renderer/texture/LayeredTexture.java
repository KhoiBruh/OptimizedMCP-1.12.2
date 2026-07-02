package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.renderer.NativeImage;
import java.io.IOException;
import java.util.List;

public class LayeredTexture extends AbstractTexture {

	private static final Logger LOGGER = LogManager.getLogger();
	public final List<String> layeredTextureNames;

	public LayeredTexture(String... textureNames) {
		layeredTextureNames = Lists.newArrayList(textureNames);
	}

	public void loadTexture(IResourceManager resourceManager) {
		deleteGlTexture();
		NativeImage bufferedimage = null;

		for (String s : layeredTextureNames) {
			try {
				if (s != null) {
					try (IResource iresource = resourceManager.getResource(new ResourceLocation(s))) {
						NativeImage bufferedimage1 = TextureUtil.readImage(iresource.getInputStream());

						if (bufferedimage == null) {
							bufferedimage = new NativeImage(bufferedimage1.getWidth(), bufferedimage1.getHeight(), true);
						}

						bufferedimage.drawImage(bufferedimage1, 0, 0);
						bufferedimage1.close();
					}
				}

				continue;
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
