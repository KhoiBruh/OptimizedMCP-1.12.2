package net.minecraft.client.main;

import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.client.resources.ResourceIndex;
import net.minecraft.client.resources.ResourceIndexFolder;
import net.minecraft.util.Session;

import javax.annotation.Nullable;
import java.io.File;
import java.net.Proxy;

public record GameConfiguration(
		UserInformation userInfo,
		DisplayInformation displayInfo,
		FolderInformation folderInfo,
		GameInformation gameInfo,
		ServerInformation serverInfo
) {

	public record DisplayInformation(int width, int height, boolean fullscreen) {

	}

	public record FolderInformation(File mcDataDir, File resourcePacksDir, File assetsDir, String assetIndex) {

		public FolderInformation(File mcDataDir, File resourcePacksDir, File assetsDir, @Nullable String assetIndex) {

			this.mcDataDir = mcDataDir;
			this.resourcePacksDir = resourcePacksDir;
			this.assetsDir = assetsDir;
			this.assetIndex = assetIndex;
		}

		public ResourceIndex getAssetsIndex() {

			return assetIndex == null ? new ResourceIndexFolder(assetsDir) : new ResourceIndex(assetsDir, assetIndex);
		}

	}

	public record GameInformation(String version, String versionType) {

	}

	public record ServerInformation(String serverName, int serverPort) {

	}

	public record UserInformation(Session session, PropertyMap userProperties, PropertyMap profileProperties,
	                              Proxy proxy) {

	}

}
