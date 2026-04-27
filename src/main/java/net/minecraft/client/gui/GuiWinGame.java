package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiWinGame extends GuiScreen
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation MINECRAFT_LOGO = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation field_194401_g = new ResourceLocation("textures/gui/title/edition.png");
    private static final ResourceLocation VIGNETTE_TEXTURE = new ResourceLocation("textures/misc/vignette.png");
    private final boolean poem;
    private final Runnable onFinished;
    private float time;
    private List<String> lines;
    private int totalScrollLength;
    private float scrollSpeed = 0.5F;

    public GuiWinGame(boolean poemIn, Runnable onFinishedIn)
    {
        poem = poemIn;
        onFinished = onFinishedIn;

        if (!poemIn)
        {
            scrollSpeed = 0.75F;
        }
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        mc.getMusicTicker().update();
        mc.getSoundHandler().update();
        float f = (float)(totalScrollLength + height + height + 24) / scrollSpeed;

        if (time > f)
        {
            sendRespawnPacket();
        }
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == 1)
        {
            sendRespawnPacket();
        }
    }

    private void sendRespawnPacket()
    {
        onFinished.run();
        mc.displayGuiScreen((GuiScreen)null);
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame()
    {
        return true;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        if (lines == null)
        {
            lines = Lists.<String>newArrayList();
            IResource iresource = null;

            try
            {
                String s = "" + TextFormatting.WHITE + TextFormatting.OBFUSCATED + TextFormatting.GREEN + TextFormatting.AQUA;
                int i = 274;

                if (poem)
                {
                    iresource = mc.getResourceManager().getResource(new ResourceLocation("texts/end.txt"));
                    InputStream inputstream = iresource.getInputStream();
                    BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
                    Random random = new Random(8124371L);
                    String s1;

                    while ((s1 = bufferedreader.readLine()) != null)
                    {
                        String s2;
                        String s3;

                        for (s1 = s1.replaceAll("PLAYERNAME", mc.getSession().getUsername()); s1.contains(s); s1 = s2 + TextFormatting.WHITE + TextFormatting.OBFUSCATED + "XXXXXXXX".substring(0, random.nextInt(4) + 3) + s3)
                        {
                            int j = s1.indexOf(s);
                            s2 = s1.substring(0, j);
                            s3 = s1.substring(j + s.length());
                        }

                        lines.addAll(mc.fontRenderer.listFormattedStringToWidth(s1, 274));
                        lines.add("");
                    }

                    inputstream.close();

                    for (int k = 0; k < 8; ++k)
                    {
                        lines.add("");
                    }
                }

                InputStream inputstream1 = mc.getResourceManager().getResource(new ResourceLocation("texts/credits.txt")).getInputStream();
                BufferedReader bufferedreader1 = new BufferedReader(new InputStreamReader(inputstream1, StandardCharsets.UTF_8));
                String s4;

                while ((s4 = bufferedreader1.readLine()) != null)
                {
                    s4 = s4.replaceAll("PLAYERNAME", mc.getSession().getUsername());
                    s4 = s4.replaceAll("\t", "    ");
                    lines.addAll(mc.fontRenderer.listFormattedStringToWidth(s4, 274));
                    lines.add("");
                }

                inputstream1.close();
                totalScrollLength = lines.size() * 12;
            }
            catch (Exception exception)
            {
                LOGGER.error("Couldn't load credits", (Throwable)exception);
            }
            finally
            {
                IOUtils.closeQuietly((Closeable)iresource);
            }
        }
    }

    private void drawWinGameScreen(int p_146575_1_, int p_146575_2_, float p_146575_3_)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        int i = width;
        float f = -time * 0.5F * scrollSpeed;
        float f1 = (float) height - time * 0.5F * scrollSpeed;
        float f2 = 0.015625F;
        float f3 = time * 0.02F;
        float f4 = (float)(totalScrollLength + height + height + 24) / scrollSpeed;
        float f5 = (f4 - 20.0F - time) * 0.005F;

        if (f5 < f3)
        {
            f3 = f5;
        }

        if (f3 > 1.0F)
        {
            f3 = 1.0F;
        }

        f3 = f3 * f3;
        f3 = f3 * 96.0F / 255.0F;
        bufferbuilder.pos(0.0D, (double) height, (double) zLevel).tex(0.0D, (double)(f * 0.015625F)).color(f3, f3, f3, 1.0F).endVertex();
        bufferbuilder.pos((double)i, (double) height, (double) zLevel).tex((double)((float)i * 0.015625F), (double)(f * 0.015625F)).color(f3, f3, f3, 1.0F).endVertex();
        bufferbuilder.pos((double)i, 0.0D, (double) zLevel).tex((double)((float)i * 0.015625F), (double)(f1 * 0.015625F)).color(f3, f3, f3, 1.0F).endVertex();
        bufferbuilder.pos(0.0D, 0.0D, (double) zLevel).tex(0.0D, (double)(f1 * 0.015625F)).color(f3, f3, f3, 1.0F).endVertex();
        tessellator.draw();
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawWinGameScreen(mouseX, mouseY, partialTicks);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        int i = 274;
        int j = width / 2 - 137;
        int k = height + 50;
        time += partialTicks;
        float f = -time * scrollSpeed;
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, f, 0.0F);
        mc.getTextureManager().bindTexture(MINECRAFT_LOGO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableAlpha();
        drawTexturedModalRect(j, k, 0, 0, 155, 44);
        drawTexturedModalRect(j + 155, k, 0, 45, 155, 44);
        mc.getTextureManager().bindTexture(field_194401_g);
        drawModalRectWithCustomSizedTexture(j + 88, k + 37, 0.0F, 0.0F, 98, 14, 128.0F, 16.0F);
        GlStateManager.disableAlpha();
        int l = k + 100;

        for (int i1 = 0; i1 < lines.size(); ++i1)
        {
            if (i1 == lines.size() - 1)
            {
                float f1 = (float)l + f - (float)(height / 2 - 6);

                if (f1 < 0.0F)
                {
                    GlStateManager.translate(0.0F, -f1, 0.0F);
                }
            }

            if ((float)l + f + 12.0F + 8.0F > 0.0F && (float)l + f < (float) height)
            {
                String s = lines.get(i1);

                if (s.startsWith("[C]"))
                {
                    fontRenderer.drawStringWithShadow(s.substring(3), (float)(j + (274 - fontRenderer.getStringWidth(s.substring(3))) / 2), (float)l, 16777215);
                }
                else
                {
                    fontRenderer.fontRandom.setSeed((long)((float)((long)i1 * 4238972211L) + time / 4.0F));
                    fontRenderer.drawStringWithShadow(s, (float)j, (float)l, 16777215);
                }
            }

            l += 12;
        }

        GlStateManager.popMatrix();
        mc.getTextureManager().bindTexture(VIGNETTE_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
        int j1 = width;
        int k1 = height;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(0.0D, (double)k1, (double) zLevel).tex(0.0D, 1.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        bufferbuilder.pos((double)j1, (double)k1, (double) zLevel).tex(1.0D, 1.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        bufferbuilder.pos((double)j1, 0.0D, (double) zLevel).tex(1.0D, 0.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        bufferbuilder.pos(0.0D, 0.0D, (double) zLevel).tex(0.0D, 0.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        tessellator.draw();
        GlStateManager.disableBlend();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
