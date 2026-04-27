package net.minecraft.client.gui;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.ArrayUtils;

public class GuiKeyBindingList extends GuiListExtended
{
    private final GuiControls controlsScreen;
    private final Minecraft mc;
    private final GuiListExtended.IGuiListEntry[] listEntries;
    private int maxListLabelWidth;

    public GuiKeyBindingList(GuiControls controls, Minecraft mcIn)
    {
        super(mcIn, controls.width + 45, controls.height, 63, controls.height - 32, 20);
        controlsScreen = controls;
        mc = mcIn;
        KeyBinding[] akeybinding = (KeyBinding[])ArrayUtils.clone(mcIn.gameSettings.keyBindings);
        listEntries = new GuiListExtended.IGuiListEntry[akeybinding.length + KeyBinding.getKeybinds().size()];
        Arrays.sort((Object[])akeybinding);
        int i = 0;
        String s = null;

        for (KeyBinding keybinding : akeybinding)
        {
            String s1 = keybinding.getKeyCategory();

            if (!s1.equals(s))
            {
                s = s1;
                listEntries[i++] = new GuiKeyBindingList.CategoryEntry(s1);
            }

            int j = mcIn.fontRenderer.getStringWidth(I18n.format(keybinding.getKeyDescription()));

            if (j > maxListLabelWidth)
            {
                maxListLabelWidth = j;
            }

            listEntries[i++] = new GuiKeyBindingList.KeyEntry(keybinding);
        }
    }

    protected int getSize()
    {
        return listEntries.length;
    }

    /**
     * Gets the IGuiListEntry object for the given index
     */
    public GuiListExtended.IGuiListEntry getListEntry(int index)
    {
        return listEntries[index];
    }

    protected int getScrollBarX()
    {
        return super.getScrollBarX() + 15;
    }

    /**
     * Gets the width of the list
     */
    public int getListWidth()
    {
        return super.getListWidth() + 32;
    }

    public class CategoryEntry implements GuiListExtended.IGuiListEntry
    {
        private final String labelText;
        private final int labelWidth;

        public CategoryEntry(String name)
        {
            labelText = I18n.format(name);
            labelWidth = mc.fontRenderer.getStringWidth(labelText);
        }

        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
        {
            mc.fontRenderer.drawString(labelText, mc.currentScreen.width / 2 - labelWidth / 2, y + slotHeight - mc.fontRenderer.FONT_HEIGHT - 1, 16777215);
        }

        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            return false;
        }

        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
        }

        public void updatePosition(int slotIndex, int x, int y, float partialTicks)
        {
        }
    }

    public class KeyEntry implements GuiListExtended.IGuiListEntry
    {
        private final KeyBinding keybinding;
        private final String keyDesc;
        private final GuiButton btnChangeKeyBinding;
        private final GuiButton btnReset;

        private KeyEntry(KeyBinding name)
        {
            keybinding = name;
            keyDesc = I18n.format(name.getKeyDescription());
            btnChangeKeyBinding = new GuiButton(0, 0, 0, 75, 20, I18n.format(name.getKeyDescription()));
            btnReset = new GuiButton(0, 0, 0, 50, 20, I18n.format("controls.reset"));
        }

        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
        {
            boolean flag = controlsScreen.buttonId == keybinding;
            mc.fontRenderer.drawString(keyDesc, x + 90 - maxListLabelWidth, y + slotHeight / 2 - mc.fontRenderer.FONT_HEIGHT / 2, 16777215);
            btnReset.x = x + 190;
            btnReset.y = y;
            btnReset.enabled = keybinding.getKeyCode() != keybinding.getKeyCodeDefault();
            btnReset.drawButton(mc, mouseX, mouseY, partialTicks);
            btnChangeKeyBinding.x = x + 105;
            btnChangeKeyBinding.y = y;
            btnChangeKeyBinding.displayString = GameSettings.getKeyDisplayString(keybinding.getKeyCode());
            boolean flag1 = false;

            if (keybinding.getKeyCode() != 0)
            {
                for (KeyBinding keybinding : mc.gameSettings.keyBindings)
                {
                    if (keybinding != this.keybinding && keybinding.getKeyCode() == this.keybinding.getKeyCode())
                    {
                        flag1 = true;
                        break;
                    }
                }
            }

            if (flag)
            {
                btnChangeKeyBinding.displayString = TextFormatting.WHITE + "> " + TextFormatting.YELLOW + btnChangeKeyBinding.displayString + TextFormatting.WHITE + " <";
            }
            else if (flag1)
            {
                btnChangeKeyBinding.displayString = TextFormatting.RED + btnChangeKeyBinding.displayString;
            }

            btnChangeKeyBinding.drawButton(mc, mouseX, mouseY, partialTicks);
        }

        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            if (btnChangeKeyBinding.mousePressed(mc, mouseX, mouseY))
            {
                controlsScreen.buttonId = keybinding;
                return true;
            }
            else if (btnReset.mousePressed(mc, mouseX, mouseY))
            {
                mc.gameSettings.setOptionKeyBinding(keybinding, keybinding.getKeyCodeDefault());
                KeyBinding.resetKeyBindingArrayAndHash();
                return true;
            }
            else
            {
                return false;
            }
        }

        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
            btnChangeKeyBinding.mouseReleased(x, y);
            btnReset.mouseReleased(x, y);
        }

        public void updatePosition(int slotIndex, int x, int y, float partialTicks)
        {
        }
    }
}
