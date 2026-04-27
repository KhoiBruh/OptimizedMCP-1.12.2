package net.minecraft.client.gui.toasts;

import com.google.common.collect.Queues;
import java.util.Arrays;
import java.util.Deque;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.math.MathHelper;

public class GuiToast extends Gui
{
    private final Minecraft mc;
    private final GuiToast.ToastInstance<?>[] visible = new GuiToast.ToastInstance[5];
    private final Deque<IToast> toastsQueue = Queues.<IToast>newArrayDeque();

    public GuiToast(Minecraft mcIn)
    {
        mc = mcIn;
    }

    public void drawToast(ScaledResolution resolution)
    {
        if (!mc.gameSettings.hideGUI)
        {
            RenderHelper.disableStandardItemLighting();

            for (int i = 0; i < visible.length; ++i)
            {
                GuiToast.ToastInstance<?> toastinstance = visible[i];

                if (toastinstance != null && toastinstance.render(resolution.getScaledWidth(), i))
                {
                    visible[i] = null;
                }

                if (visible[i] == null && !toastsQueue.isEmpty())
                {
                    visible[i] = new GuiToast.ToastInstance(toastsQueue.removeFirst());
                }
            }
        }
    }

    @Nullable
    public <T extends IToast> T getToast(Class <? extends T > p_192990_1_, Object p_192990_2_)
    {
        for (GuiToast.ToastInstance<?> toastinstance : visible)
        {
            if (toastinstance != null && p_192990_1_.isAssignableFrom(toastinstance.getToast().getClass()) && toastinstance.getToast().getType().equals(p_192990_2_))
            {
                return (T)toastinstance.getToast();
            }
        }

        for (IToast itoast : toastsQueue)
        {
            if (p_192990_1_.isAssignableFrom(itoast.getClass()) && itoast.getType().equals(p_192990_2_))
            {
                return (T)itoast;
            }
        }

        return (T)null;
    }

    public void clear()
    {
        Arrays.fill(visible, (Object)null);
        toastsQueue.clear();
    }

    public void add(IToast toastIn)
    {
        toastsQueue.add(toastIn);
    }

    public Minecraft getMinecraft()
    {
        return mc;
    }

    class ToastInstance<T extends IToast>
    {
        private final T toast;
        private long animationTime;
        private long visibleTime;
        private IToast.Visibility visibility;

        private ToastInstance(T toastIn)
        {
            animationTime = -1L;
            visibleTime = -1L;
            visibility = IToast.Visibility.SHOW;
            toast = toastIn;
        }

        public T getToast()
        {
            return toast;
        }

        private float getVisibility(long p_193686_1_)
        {
            float f = MathHelper.clamp((float)(p_193686_1_ - animationTime) / 600.0F, 0.0F, 1.0F);
            f = f * f;
            return visibility == IToast.Visibility.HIDE ? 1.0F - f : f;
        }

        public boolean render(int p_193684_1_, int p_193684_2_)
        {
            long i = Minecraft.getSystemTime();

            if (animationTime == -1L)
            {
                animationTime = i;
                visibility.playSound(mc.getSoundHandler());
            }

            if (visibility == IToast.Visibility.SHOW && i - animationTime <= 600L)
            {
                visibleTime = i;
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate((float)p_193684_1_ - 160.0F * getVisibility(i), (float)(p_193684_2_ * 32), (float)(500 + p_193684_2_));
            IToast.Visibility itoast$visibility = toast.draw(GuiToast.this, i - visibleTime);
            GlStateManager.popMatrix();

            if (itoast$visibility != visibility)
            {
                animationTime = i - (long)((int)((1.0F - getVisibility(i)) * 600.0F));
                visibility = itoast$visibility;
                visibility.playSound(mc.getSoundHandler());
            }

            return visibility == IToast.Visibility.HIDE && i - animationTime > 600L;
        }
    }
}
