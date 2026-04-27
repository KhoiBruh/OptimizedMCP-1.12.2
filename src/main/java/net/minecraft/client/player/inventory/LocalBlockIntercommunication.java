package net.minecraft.client.player.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IInteractionObject;

public record LocalBlockIntercommunication(String guiID, ITextComponent displayName) implements IInteractionObject {

	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {

		throw new UnsupportedOperationException();
	}

	/**
	 * Get the name of this object. For players this returns their username
	 */
	public String getName() {

		return displayName.getUnformattedText();
	}

	/**
	 * Returns true if this thing is named
	 */
	public boolean hasCustomName() {

		return true;
	}

	/**
	 * Get the formatted ChatComponent that will be used for the sender's username in chat
	 */
	@Override
	public ITextComponent displayName() {

		return displayName;
	}

}
