package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;

public class GuiShareToLan extends GuiScreen
{
    private final GuiScreen lastScreen;
    private GuiButton allowCheatsButton;
    private GuiButton gameModeButton;
    private String gameMode = "survival";
    private boolean allowCheats;

    public GuiShareToLan(GuiScreen lastScreenIn)
    {
        lastScreen = lastScreenIn;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        buttonList.clear();
        buttonList.add(new GuiButton(101, width / 2 - 155, height - 28, 150, 20, I18n.format("lanServer.start")));
        buttonList.add(new GuiButton(102, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
        gameModeButton = addButton(new GuiButton(104, width / 2 - 155, 100, 150, 20, I18n.format("selectWorld.gameMode")));
        allowCheatsButton = addButton(new GuiButton(103, width / 2 + 5, 100, 150, 20, I18n.format("selectWorld.allowCommands")));
        updateDisplayNames();
    }

    private void updateDisplayNames()
    {
        gameModeButton.displayString = I18n.format("selectWorld.gameMode") + ": " + I18n.format("selectWorld.gameMode." + gameMode);
        allowCheatsButton.displayString = I18n.format("selectWorld.allowCommands") + " ";

        if (allowCheats)
        {
            allowCheatsButton.displayString = allowCheatsButton.displayString + I18n.format("options.on");
        }
        else
        {
            allowCheatsButton.displayString = allowCheatsButton.displayString + I18n.format("options.off");
        }
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 102)
        {
            mc.displayGuiScreen(lastScreen);
        }
        else if (button.id == 104)
        {
            if ("spectator".equals(gameMode))
            {
                gameMode = "creative";
            }
            else if ("creative".equals(gameMode))
            {
                gameMode = "adventure";
            }
            else if ("adventure".equals(gameMode))
            {
                gameMode = "survival";
            }
            else
            {
                gameMode = "spectator";
            }

            updateDisplayNames();
        }
        else if (button.id == 103)
        {
            allowCheats = !allowCheats;
            updateDisplayNames();
        }
        else if (button.id == 101)
        {
            mc.displayGuiScreen((GuiScreen)null);
            String s = mc.getIntegratedServer().shareToLAN(GameType.getByName(gameMode), allowCheats);
            ITextComponent itextcomponent;

            if (s != null)
            {
                itextcomponent = new TextComponentTranslation("commands.publish.started", new Object[] {s});
            }
            else
            {
                itextcomponent = new TextComponentString("commands.publish.failed");
            }

            mc.ingameGUI.getChatGUI().printChatMessage(itextcomponent);
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.format("lanServer.title"), width / 2, 50, 16777215);
        drawCenteredString(fontRenderer, I18n.format("lanServer.otherPlayers"), width / 2, 82, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
