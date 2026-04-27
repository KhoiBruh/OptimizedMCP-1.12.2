package net.minecraft.network.play.client;

import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.io.IOException;

public class CPacketRecipeInfo implements Packet<INetHandlerPlayServer> {

	private CPacketRecipeInfo.Purpose purpose;
	private IRecipe recipe;
	private boolean isGuiOpen;
	private boolean filteringCraftable;

	public CPacketRecipeInfo() {

	}

	public CPacketRecipeInfo(IRecipe p_i47518_1_) {

		purpose = CPacketRecipeInfo.Purpose.SHOWN;
		recipe = p_i47518_1_;
	}

	public CPacketRecipeInfo(boolean p_i47424_1_, boolean p_i47424_2_) {

		purpose = CPacketRecipeInfo.Purpose.SETTINGS;
		isGuiOpen = p_i47424_1_;
		filteringCraftable = p_i47424_2_;
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) throws IOException {

		purpose = buf.readEnumValue(Purpose.class);

		if (purpose == CPacketRecipeInfo.Purpose.SHOWN) {
			recipe = CraftingManager.getRecipeById(buf.readInt());
		} else if (purpose == CPacketRecipeInfo.Purpose.SETTINGS) {
			isGuiOpen = buf.readBoolean();
			filteringCraftable = buf.readBoolean();
		}
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) throws IOException {

		buf.writeEnumValue(purpose);

		if (purpose == CPacketRecipeInfo.Purpose.SHOWN) {
			buf.writeInt(CraftingManager.getIDForRecipe(recipe));
		} else if (purpose == CPacketRecipeInfo.Purpose.SETTINGS) {
			buf.writeBoolean(isGuiOpen);
			buf.writeBoolean(filteringCraftable);
		}
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayServer handler) {

		handler.handleRecipeBookUpdate(this);
	}

	public CPacketRecipeInfo.Purpose getPurpose() {

		return purpose;
	}

	public IRecipe getRecipe() {

		return recipe;
	}

	public boolean isGuiOpen() {

		return isGuiOpen;
	}

	public boolean isFilteringCraftable() {

		return filteringCraftable;
	}

	public enum Purpose {
		SHOWN,
		SETTINGS
	}

}
