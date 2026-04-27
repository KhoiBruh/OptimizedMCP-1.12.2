package net.minecraft.client.renderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadDownloadImageData extends SimpleTexture
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final AtomicInteger TEXTURE_DOWNLOADER_THREAD_ID = new AtomicInteger(0);
    @Nullable
    private final File cacheFile;
    private final String imageUrl;
    @Nullable
    private final IImageBuffer imageBuffer;
    @Nullable
    private BufferedImage bufferedImage;
    @Nullable
    private Thread imageThread;
    private boolean textureUploaded;

    public ThreadDownloadImageData(@Nullable File cacheFileIn, String imageUrlIn, ResourceLocation textureResourceLocation, @Nullable IImageBuffer imageBufferIn)
    {
        super(textureResourceLocation);
        cacheFile = cacheFileIn;
        imageUrl = imageUrlIn;
        imageBuffer = imageBufferIn;
    }

    private void checkTextureUploaded()
    {
        if (!textureUploaded)
        {
            if (bufferedImage != null)
            {
                if (textureLocation != null)
                {
                    deleteGlTexture();
                }

                TextureUtil.uploadTextureImage(super.getGlTextureId(), bufferedImage);
                textureUploaded = true;
            }
        }
    }

    public int getGlTextureId()
    {
        checkTextureUploaded();
        return super.getGlTextureId();
    }

    public void setBufferedImage(BufferedImage bufferedImageIn)
    {
        bufferedImage = bufferedImageIn;

        if (imageBuffer != null)
        {
            imageBuffer.skinAvailable();
        }
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException
    {
        if (bufferedImage == null && textureLocation != null)
        {
            super.loadTexture(resourceManager);
        }

        if (imageThread == null)
        {
            if (cacheFile != null && cacheFile.isFile())
            {
                LOGGER.debug("Loading http texture from local cache ({})", (Object) cacheFile);

                try
                {
                    bufferedImage = ImageIO.read(cacheFile);

                    if (imageBuffer != null)
                    {
                        setBufferedImage(imageBuffer.parseUserSkin(bufferedImage));
                    }
                }
                catch (IOException ioexception)
                {
                    LOGGER.error("Couldn't load skin {}", cacheFile, ioexception);
                    loadTextureFromServer();
                }
            }
            else
            {
                loadTextureFromServer();
            }
        }
    }

    protected void loadTextureFromServer()
    {
        imageThread = new Thread("Texture Downloader #" + TEXTURE_DOWNLOADER_THREAD_ID.incrementAndGet())
        {
            public void run()
            {
                HttpURLConnection httpurlconnection = null;
                ThreadDownloadImageData.LOGGER.debug("Downloading http texture from {} to {}", imageUrl, cacheFile);

                try
                {
                    httpurlconnection = (HttpURLConnection)(new URL(imageUrl)).openConnection(Minecraft.getMinecraft().getProxy());
                    httpurlconnection.setDoInput(true);
                    httpurlconnection.setDoOutput(false);
                    httpurlconnection.connect();

                    if (httpurlconnection.getResponseCode() / 100 == 2)
                    {
                        BufferedImage bufferedimage;

                        if (cacheFile != null)
                        {
                            FileUtils.copyInputStreamToFile(httpurlconnection.getInputStream(), cacheFile);
                            bufferedimage = ImageIO.read(cacheFile);
                        }
                        else
                        {
                            bufferedimage = TextureUtil.readBufferedImage(httpurlconnection.getInputStream());
                        }

                        if (imageBuffer != null)
                        {
                            bufferedimage = imageBuffer.parseUserSkin(bufferedimage);
                        }

                        setBufferedImage(bufferedimage);
                        return;
                    }
                }
                catch (Exception exception)
                {
                    ThreadDownloadImageData.LOGGER.error("Couldn't download http texture", (Throwable)exception);
                    return;
                }
                finally
                {
                    if (httpurlconnection != null)
                    {
                        httpurlconnection.disconnect();
                    }
                }
            }
        };
        imageThread.setDaemon(true);
        imageThread.start();
    }
}
