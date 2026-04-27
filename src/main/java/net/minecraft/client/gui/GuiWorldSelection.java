package net.minecraft.client.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.resources.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiWorldSelection extends GuiScreen
{
    private static final Logger LOGGER = LogManager.getLogger();

    /** The screen to return to when this closes (always Main Menu). */
    protected GuiScreen prevScreen;
    protected String title = "Select world";

    /**
     * Tooltip displayed a world whose version is different from this client's
     */
    private String worldVersTooltip;
    private GuiButton deleteButton;
    private GuiButton selectButton;
    private GuiButton renameButton;
    private GuiButton copyButton;
    private GuiListWorldSelection selectionList;

    public GuiWorldSelection(GuiScreen screenIn)
    {
        prevScreen = screenIn;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        title = I18n.format("selectWorld.title");
        selectionList = new GuiListWorldSelection(this, mc, width, height, 32, height - 64, 36);
        postInit();
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        selectionList.handleMouseInput();
    }

    public void postInit()
    {
        selectButton = addButton(new GuiButton(1, width / 2 - 154, height - 52, 150, 20, I18n.format("selectWorld.select")));
        addButton(new GuiButton(3, width / 2 + 4, height - 52, 150, 20, I18n.format("selectWorld.create")));
        renameButton = addButton(new GuiButton(4, width / 2 - 154, height - 28, 72, 20, I18n.format("selectWorld.edit")));
        deleteButton = addButton(new GuiButton(2, width / 2 - 76, height - 28, 72, 20, I18n.format("selectWorld.delete")));
        copyButton = addButton(new GuiButton(5, width / 2 + 4, height - 28, 72, 20, I18n.format("selectWorld.recreate")));
        addButton(new GuiButton(0, width / 2 + 82, height - 28, 72, 20, I18n.format("gui.cancel")));
        selectButton.enabled = false;
        deleteButton.enabled = false;
        renameButton.enabled = false;
        copyButton.enabled = false;
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            GuiListWorldSelectionEntry guilistworldselectionentry = selectionList.getSelectedWorld();

            if (button.id == 2)
            {
                if (guilistworldselectionentry != null)
                {
                    guilistworldselectionentry.deleteWorld();
                }
            }
            else if (button.id == 1)
            {
                if (guilistworldselectionentry != null)
                {
                    guilistworldselectionentry.joinWorld();
                }
            }
            else if (button.id == 3)
            {
                mc.displayGuiScreen(new GuiCreateWorld(this));
            }
            else if (button.id == 4)
            {
                if (guilistworldselectionentry != null)
                {
                    guilistworldselectionentry.editWorld();
                }
            }
            else if (button.id == 0)
            {
                mc.displayGuiScreen(prevScreen);
            }
            else if (button.id == 5 && guilistworldselectionentry != null)
            {
                guilistworldselectionentry.recreateWorld();
            }
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        worldVersTooltip = null;
        selectionList.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRenderer, title, width / 2, 20, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (worldVersTooltip != null)
        {
            drawHoveringText(Lists.newArrayList(Splitter.on("\n").split(worldVersTooltip)), mouseX, mouseY);
        }
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        selectionList.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Called when a mouse button is released.
     */
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        selectionList.mouseReleased(mouseX, mouseY, state);
    }

    /**
     * Called back by selectionList when we call its drawScreen method, from ours.
     */
    public void setVersionTooltip(String p_184861_1_)
    {
        worldVersTooltip = p_184861_1_;
    }

    public void selectWorld(@Nullable GuiListWorldSelectionEntry entry)
    {
        boolean flag = entry != null;
        selectButton.enabled = flag;
        deleteButton.enabled = flag;
        renameButton.enabled = flag;
        copyButton.enabled = flag;
    }
}
