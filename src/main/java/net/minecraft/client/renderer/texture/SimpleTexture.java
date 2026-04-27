package net.minecraft.client.renderer.texture;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class SimpleTexture extends AbstractTexture {

	private static final Logger LOGGER = LogManager.getLogger();
	protected final ResourceLocation textureLocation;

	public SimpleTexture(ResourceLocation textureResourceLocation) {

		textureLocation = textureResourceLocation;
	}

	public void loadTexture(IResourceManager resourceManager) throws IOException {

		deleteGlTexture();
		IResource iresource = null;

		try {
			iresource = resourceManager.getResource(textureLocation);
			BufferedImage bufferedimage = TextureUtil.readBufferedImage(iresource.getInputStream());
			boolean flag = false;
			boolean flag1 = false;

			if (iresource.hasMetadata()) {
				try {
					TextureMetadataSection texturemetadatasection = iresource.getMetadata("texture");

					if (texturemetadatasection != null) {
						flag = texturemetadatasection.textureBlur();
						flag1 = texturemetadatasection.textureClamp();
					}
				} catch (RuntimeException runtimeexception) {
					LOGGER.warn("Failed reading metadata of: {}", textureLocation, runtimeexception);
				}
			}

			TextureUtil.uploadTextureImageAllocate(getGlTextureId(), bufferedimage, flag, flag1);
		} finally {
			IOUtils.closeQuietly(iresource);
		}
	}

}
