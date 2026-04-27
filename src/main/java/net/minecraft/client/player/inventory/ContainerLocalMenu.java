package net.minecraft.client.player.inventory;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;

import java.util.Map;

public class ContainerLocalMenu extends InventoryBasic implements ILockableContainer {

	private final String guiID;
	private final Map<Integer, Integer> dataValues = Maps.newHashMap();

	public ContainerLocalMenu(String id, ITextComponent title, int slotCount) {

		super(title, slotCount);
		guiID = id;
	}

	public int getField(int id) {

		return dataValues.getOrDefault(id, 0);
	}

	public void setField(int id, int value) {

		dataValues.put(id, value);
	}

	public int getFieldCount() {

		return dataValues.size();
	}

	public boolean isLocked() {

		return false;
	}

	public LockCode getLockCode() {

		return LockCode.EMPTY_CODE;
	}

	public void setLockCode(LockCode code) {

	}

	public String guiID() {

		return guiID;
	}

	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {

		throw new UnsupportedOperationException();
	}

}
