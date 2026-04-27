package net.minecraft.client.resources;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Set;

public class DefaultResourcePack implements IResourcePack {

	public static final Set<String> DEFAULT_RESOURCE_DOMAINS = ImmutableSet.of("minecraft", "realms");
	private final ResourceIndex resourceIndex;

	public DefaultResourcePack(ResourceIndex resourceIndexIn) {

		resourceIndex = resourceIndexIn;
	}

	public InputStream getInputStream(ResourceLocation location) throws IOException {

		InputStream inputstream = getInputStreamAssets(location);

		if (inputstream != null) {
			return inputstream;
		} else {
			InputStream inputstream1 = getResourceStream(location);

			if (inputstream1 != null) {
				return inputstream1;
			} else {
				throw new FileNotFoundException(location.getResourcePath());
			}
		}
	}

	@Nullable
	public InputStream getInputStreamAssets(ResourceLocation location) throws IOException {

		File file1 = resourceIndex.getFile(location);
		return file1 != null && file1.isFile() ? new FileInputStream(file1) : null;
	}

	@Nullable
	private InputStream getResourceStream(ResourceLocation location) {

		String s = "/assets/" + location.getResourceDomain() + "/" + location.getResourcePath();

		try {
			URL url = DefaultResourcePack.class.getResource(s);
			return url != null && FolderResourcePack.validatePath(new File(url.getFile()), s) ? DefaultResourcePack.class.getResourceAsStream(s) : null;
		} catch (IOException var4) {
			return DefaultResourcePack.class.getResourceAsStream(s);
		}
	}

	public boolean resourceExists(ResourceLocation location) {

		return getResourceStream(location) != null || resourceIndex.isFileExisting(location);
	}

	public Set<String> getResourceDomains() {

		return DEFAULT_RESOURCE_DOMAINS;
	}

	@Nullable
	public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) {

		try {
			InputStream inputstream = new FileInputStream(resourceIndex.getPackMcmeta());
			return AbstractResourcePack.readMetadata(metadataSerializer, inputstream, metadataSectionName);
		} catch (RuntimeException | FileNotFoundException var4) {
			return null;
		}
	}

	public BufferedImage getPackImage() throws IOException {

		return TextureUtil.readBufferedImage(DefaultResourcePack.class.getResourceAsStream("/" + (new ResourceLocation("pack.png")).getResourcePath()));
	}

	public String getPackName() {

		return "Default";
	}

}
