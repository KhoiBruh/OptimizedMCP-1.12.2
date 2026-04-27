package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;

public class GuiScreenRealmsProxy extends GuiScreen
{
    private final RealmsScreen proxy;

    public GuiScreenRealmsProxy(RealmsScreen proxyIn)
    {
        proxy = proxyIn;
        buttonList = Collections.<GuiButton>synchronizedList(Lists.newArrayList());
    }

    public RealmsScreen getProxy()
    {
        return proxy;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        proxy.init();
        super.initGui();
    }

    public void drawCenteredString(String text, int x, int y, int color)
    {
        super.drawCenteredString(fontRenderer, text, x, y, color);
    }

    public void drawString(String text, int x, int y, int color, boolean p_154322_5_)
    {
        if (p_154322_5_)
        {
            super.drawString(fontRenderer, text, x, y, color);
        }
        else
        {
            fontRenderer.drawString(text, x, y, color);
        }
    }

    /**
     * Draws a textured rectangle at the current z-value.
     */
    public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
    {
        proxy.blit(x, y, textureX, textureY, width, height);
        super.drawTexturedModalRect(x, y, textureX, textureY, width, height);
    }

    /**
     * Draws a rectangle with a vertical gradient between the specified colors (ARGB format). Args : x1, y1, x2, y2,
     * topColor, bottomColor
     */
    public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor)
    {
        super.drawGradientRect(left, top, right, bottom, startColor, endColor);
    }

    /**
     * Draws either a gradient over the background screen (when it exists) or a flat gradient over background.png
     */
    public void drawDefaultBackground()
    {
        super.drawDefaultBackground();
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame()
    {
        return super.doesGuiPauseGame();
    }

    public void drawWorldBackground(int tint)
    {
        super.drawWorldBackground(tint);
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        proxy.render(mouseX, mouseY, partialTicks);
    }

    public void renderToolTip(ItemStack stack, int x, int y)
    {
        super.renderToolTip(stack, x, y);
    }

    /**
     * Draws the given text as a tooltip.
     */
    public void drawHoveringText(String text, int x, int y)
    {
        super.drawHoveringText(text, x, y);
    }

    /**
     * Draws a List of strings as a tooltip. Every entry is drawn on a seperate line.
     */
    public void drawHoveringText(List<String> textLines, int x, int y)
    {
        super.drawHoveringText(textLines, x, y);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        proxy.tick();
        super.updateScreen();
    }

    public int getFontHeight()
    {
        return fontRenderer.FONT_HEIGHT;
    }

    public int getStringWidth(String text)
    {
        return fontRenderer.getStringWidth(text);
    }

    public void fontDrawShadow(String text, int x, int y, int color)
    {
        fontRenderer.drawStringWithShadow(text, (float)x, (float)y, color);
    }

    public List<String> fontSplit(String text, int wrapWidth)
    {
        return fontRenderer.listFormattedStringToWidth(text, wrapWidth);
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    public final void actionPerformed(GuiButton button) throws IOException
    {
        proxy.buttonClicked(((GuiButtonRealmsProxy)button).getRealmsButton());
    }

    public void buttonsClear()
    {
        buttonList.clear();
    }

    public void buttonsAdd(RealmsButton button)
    {
        buttonList.add(button.getProxy());
    }

    public List<RealmsButton> buttons()
    {
        List<RealmsButton> list = Lists.<RealmsButton>newArrayListWithExpectedSize(buttonList.size());

        for (GuiButton guibutton : buttonList)
        {
            list.add(((GuiButtonRealmsProxy)guibutton).getRealmsButton());
        }

        return list;
    }

    public void buttonsRemove(RealmsButton button)
    {
        buttonList.remove(button.getProxy());
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        proxy.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException
    {
        proxy.mouseEvent();
        super.handleMouseInput();
    }

    /**
     * Handles keyboard input.
     */
    public void handleKeyboardInput() throws IOException
    {
        proxy.keyboardEvent();
        super.handleKeyboardInput();
    }

    /**
     * Called when a mouse button is released.
     */
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        proxy.mouseReleased(mouseX, mouseY, state);
    }

    /**
     * Called when a mouse button is pressed and the mouse is moved around. Parameters are : mouseX, mouseY,
     * lastButtonClicked & timeSinceMouseClick.
     */
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        proxy.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    public void keyTyped(char typedChar, int keyCode) throws IOException
    {
        proxy.keyPressed(typedChar, keyCode);
    }

    public void confirmClicked(boolean result, int id)
    {
        proxy.confirmResult(result, id);
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
        proxy.removed();
        super.onGuiClosed();
    }
}
