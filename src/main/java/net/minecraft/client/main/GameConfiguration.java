package net.minecraft.client.main;

import com.mojang.authlib.properties.PropertyMap;
import java.io.File;
import java.net.Proxy;
import javax.annotation.Nullable;
import net.minecraft.client.resources.ResourceIndex;
import net.minecraft.client.resources.ResourceIndexFolder;
import net.minecraft.util.Session;

public class GameConfiguration
{
    public final GameConfiguration.UserInformation userInfo;
    public final GameConfiguration.DisplayInformation displayInfo;
    public final GameConfiguration.FolderInformation folderInfo;
    public final GameConfiguration.GameInformation gameInfo;
    public final GameConfiguration.ServerInformation serverInfo;

    public GameConfiguration(GameConfiguration.UserInformation userInfoIn, GameConfiguration.DisplayInformation displayInfoIn, GameConfiguration.FolderInformation folderInfoIn, GameConfiguration.GameInformation gameInfoIn, GameConfiguration.ServerInformation serverInfoIn)
    {
        userInfo = userInfoIn;
        displayInfo = displayInfoIn;
        folderInfo = folderInfoIn;
        gameInfo = gameInfoIn;
        serverInfo = serverInfoIn;
    }

    public static class DisplayInformation
    {
        public final int width;
        public final int height;
        public final boolean fullscreen;
        public final boolean checkGlErrors;

        public DisplayInformation(int widthIn, int heightIn, boolean fullscreenIn, boolean checkGlErrorsIn)
        {
            width = widthIn;
            height = heightIn;
            fullscreen = fullscreenIn;
            checkGlErrors = checkGlErrorsIn;
        }
    }

    public static class FolderInformation
    {
        public final File mcDataDir;
        public final File resourcePacksDir;
        public final File assetsDir;
        public final String assetIndex;

        public FolderInformation(File mcDataDirIn, File resourcePacksDirIn, File assetsDirIn, @Nullable String assetIndexIn)
        {
            mcDataDir = mcDataDirIn;
            resourcePacksDir = resourcePacksDirIn;
            assetsDir = assetsDirIn;
            assetIndex = assetIndexIn;
        }

        public ResourceIndex getAssetsIndex()
        {
            return (ResourceIndex)(assetIndex == null ? new ResourceIndexFolder(assetsDir) : new ResourceIndex(assetsDir, assetIndex));
        }
    }

    public static class GameInformation
    {
        public final boolean isDemo;
        public final String version;
        public final String versionType;

        public GameInformation(boolean demo, String versionIn, String versionTypeIn)
        {
            isDemo = demo;
            version = versionIn;
            versionType = versionTypeIn;
        }
    }

    public static class ServerInformation
    {
        public final String serverName;
        public final int serverPort;

        public ServerInformation(String serverNameIn, int serverPortIn)
        {
            serverName = serverNameIn;
            serverPort = serverPortIn;
        }
    }

    public static class UserInformation
    {
        public final Session session;
        public final PropertyMap userProperties;
        public final PropertyMap profileProperties;
        public final Proxy proxy;

        public UserInformation(Session sessionIn, PropertyMap userPropertiesIn, PropertyMap profilePropertiesIn, Proxy proxyIn)
        {
            session = sessionIn;
            userProperties = userPropertiesIn;
            profileProperties = profilePropertiesIn;
            proxy = proxyIn;
        }
    }
}
