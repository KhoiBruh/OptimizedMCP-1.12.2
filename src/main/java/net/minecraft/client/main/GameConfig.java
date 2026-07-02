package net.minecraft.client.main;

import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.client.resources.ResourceIndex;
import net.minecraft.client.resources.ResourceIndexFolder;
import net.minecraft.util.Session;

import java.io.File;
import java.net.Proxy;

public record GameConfig(
	User user,
	Display display,
	Folder folder,
	Game game
) {

	public record Display(int width, int height, boolean fullscreen) {

	}

	public record Folder(File dataDir, File resourcePacksDir, File assetsDir, String assetIndex) {

		public ResourceIndex getAssetsIndex() {
			return assetIndex == null ? new ResourceIndexFolder(assetsDir) : new ResourceIndex(assetsDir, assetIndex);
		}

	}

	public record Game(String version, String type) {

	}

	public record User(Session session, PropertyMap userProperties, PropertyMap profileProperties, Proxy proxy) {

	}

}
