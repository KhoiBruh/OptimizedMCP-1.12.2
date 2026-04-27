package net.minecraft.client.player.inventory;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;

public class ContainerLocalMenu extends InventoryBasic implements ILockableContainer
{
    private final String guiID;
    private final Map<Integer, Integer> dataValues = Maps.<Integer, Integer>newHashMap();

    public ContainerLocalMenu(String id, ITextComponent title, int slotCount)
    {
        super(title, slotCount);
        guiID = id;
    }

    public int getField(int id)
    {
        return dataValues.containsKey(Integer.valueOf(id)) ? ((Integer) dataValues.get(Integer.valueOf(id))).intValue() : 0;
    }

    public void setField(int id, int value)
    {
        dataValues.put(Integer.valueOf(id), Integer.valueOf(value));
    }

    public int getFieldCount()
    {
        return dataValues.size();
    }

    public boolean isLocked()
    {
        return false;
    }

    public void setLockCode(LockCode code)
    {
    }

    public LockCode getLockCode()
    {
        return LockCode.EMPTY_CODE;
    }

    public String getGuiID()
    {
        return guiID;
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        throw new UnsupportedOperationException();
    }
}
