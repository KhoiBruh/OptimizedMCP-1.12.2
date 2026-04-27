package net.minecraft.client.tutorial;

import net.minecraft.client.gui.toasts.TutorialToast;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;

public class OpenInventoryStep implements ITutorialStep
{
    private static final ITextComponent TITLE = new TextComponentTranslation("tutorial.open_inventory.title", new Object[0]);
    private static final ITextComponent DESCRIPTION = new TextComponentTranslation("tutorial.open_inventory.description", new Object[] {Tutorial.createKeybindComponent("inventory")});
    private final Tutorial tutorial;
    private TutorialToast toast;
    private int timeWaiting;

    public OpenInventoryStep(Tutorial tutorial)
    {
        this.tutorial = tutorial;
    }

    public void update()
    {
        ++timeWaiting;

        if (tutorial.getGameType() != GameType.SURVIVAL)
        {
            tutorial.setStep(TutorialSteps.NONE);
        }
        else
        {
            if (timeWaiting >= 600 && toast == null)
            {
                toast = new TutorialToast(TutorialToast.Icons.RECIPE_BOOK, TITLE, DESCRIPTION, false);
                tutorial.getMinecraft().getToastGui().add(toast);
            }
        }
    }

    public void onStop()
    {
        if (toast != null)
        {
            toast.hide();
            toast = null;
        }
    }

    /**
     * Called when the player opens his inventory
     */
    public void openInventory()
    {
        tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
    }
}
