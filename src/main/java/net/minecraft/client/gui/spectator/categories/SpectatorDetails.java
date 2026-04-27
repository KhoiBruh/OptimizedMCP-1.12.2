package net.minecraft.client.gui.spectator.categories;

import com.google.common.base.MoreObjects;
import java.util.List;
import net.minecraft.client.gui.spectator.ISpectatorMenuObject;
import net.minecraft.client.gui.spectator.ISpectatorMenuView;
import net.minecraft.client.gui.spectator.SpectatorMenu;

public class SpectatorDetails
{
    private final ISpectatorMenuView category;
    private final List<ISpectatorMenuObject> items;
    private final int selectedSlot;

    public SpectatorDetails(ISpectatorMenuView categoryIn, List<ISpectatorMenuObject> itemsIn, int selectedIndex)
    {
        category = categoryIn;
        items = itemsIn;
        selectedSlot = selectedIndex;
    }

    public ISpectatorMenuObject getObject(int index)
    {
        return index >= 0 && index < items.size() ? (ISpectatorMenuObject)MoreObjects.firstNonNull(items.get(index), SpectatorMenu.EMPTY_SLOT) : SpectatorMenu.EMPTY_SLOT;
    }

    public int getSelectedSlot()
    {
        return selectedSlot;
    }
}
