package net.minecraft.client.renderer.texture;

import net.minecraft.client.renderer.GlStateManager;

public abstract class AbstractTexture implements ITextureObject
{
    protected int glTextureId = -1;
    protected boolean blur;
    protected boolean mipmap;
    protected boolean blurLast;
    protected boolean mipmapLast;

    public void setBlurMipmapDirect(boolean blurIn, boolean mipmapIn)
    {
        blur = blurIn;
        mipmap = mipmapIn;
        int i;
        int j;

        if (blurIn)
        {
            i = mipmapIn ? 9987 : 9729;
            j = 9729;
        }
        else
        {
            i = mipmapIn ? 9986 : 9728;
            j = 9728;
        }

        GlStateManager.glTexParameteri(3553, 10241, i);
        GlStateManager.glTexParameteri(3553, 10240, j);
    }

    public void setBlurMipmap(boolean blurIn, boolean mipmapIn)
    {
        blurLast = blur;
        mipmapLast = mipmap;
        setBlurMipmapDirect(blurIn, mipmapIn);
    }

    public void restoreLastBlurMipmap()
    {
        setBlurMipmapDirect(blurLast, mipmapLast);
    }

    public int getGlTextureId()
    {
        if (glTextureId == -1)
        {
            glTextureId = TextureUtil.glGenTextures();
        }

        return glTextureId;
    }

    public void deleteGlTexture()
    {
        if (glTextureId != -1)
        {
            TextureUtil.deleteTexture(glTextureId);
            glTextureId = -1;
        }
    }
}
