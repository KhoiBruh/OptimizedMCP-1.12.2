package net.minecraft.client.resources;

import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.util.ResourceLocation;
import java.io.Closeable;
import java.io.InputStream;

public interface IResource extends Closeable {

	ResourceLocation getResourceLocation();

	InputStream getInputStream();

	boolean hasMetadata();

	
	<T extends IMetadataSection> T getMetadata(String sectionName);

	String getResourcePackName();

}
