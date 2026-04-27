package net.minecraft.realms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonRealmsProxy;
import net.minecraft.util.ResourceLocation;

public class RealmsButton
{
    protected static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    private final GuiButtonRealmsProxy proxy;

    public RealmsButton(int buttonId, int x, int y, String text)
    {
        proxy = new GuiButtonRealmsProxy(this, buttonId, x, y, text);
    }

    public RealmsButton(int buttonId, int x, int y, int widthIn, int heightIn, String text)
    {
        proxy = new GuiButtonRealmsProxy(this, buttonId, x, y, text, widthIn, heightIn);
    }

    public GuiButton getProxy()
    {
        return proxy;
    }

    public int id()
    {
        return proxy.getId();
    }

    public boolean active()
    {
        return proxy.getEnabled();
    }

    public void active(boolean p_active_1_)
    {
        proxy.setEnabled(p_active_1_);
    }

    public void msg(String p_msg_1_)
    {
        proxy.setText(p_msg_1_);
    }

    public int getWidth()
    {
        return proxy.getButtonWidth();
    }

    public int getHeight()
    {
        return proxy.getHeight();
    }

    public int y()
    {
        return proxy.getPositionY();
    }

    public void render(int p_render_1_, int p_render_2_, float p_render_3_)
    {
        proxy.drawButton(Minecraft.getMinecraft(), p_render_1_, p_render_2_, p_render_3_);
    }

    public void clicked(int p_clicked_1_, int p_clicked_2_)
    {
    }

    public void released(int p_released_1_, int p_released_2_)
    {
    }

    public void blit(int p_blit_1_, int p_blit_2_, int p_blit_3_, int p_blit_4_, int p_blit_5_, int p_blit_6_)
    {
        proxy.drawTexturedModalRect(p_blit_1_, p_blit_2_, p_blit_3_, p_blit_4_, p_blit_5_, p_blit_6_);
    }

    public void renderBg(int p_renderBg_1_, int p_renderBg_2_)
    {
    }

    public int getYImage(boolean p_getYImage_1_)
    {
        return proxy.getYImage(p_getYImage_1_);
    }
}
