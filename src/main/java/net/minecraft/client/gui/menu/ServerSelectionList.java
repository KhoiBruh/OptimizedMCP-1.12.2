package net.minecraft.client.gui.menu;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.component.GuiListExtended;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerInfo;

import java.util.List;

public class ServerSelectionList extends GuiListExtended {

	private final GuiMultiplayer owner;
	private final List<ServerListEntryNormal> serverListInternet = Lists.newArrayList();
	private final List<ServerListEntryLanDetected> serverListLan = Lists.newArrayList();
	private final GuiListExtended.IGuiListEntry lanScanEntry = new ServerListEntryLanScan();
	private int selectedSlotIndex = -1;

	public ServerSelectionList(GuiMultiplayer ownerIn, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {

		super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
		owner = ownerIn;
	}

	/**
	 * Gets the IGuiListEntry object for the given index
	 */
	public GuiListExtended.IGuiListEntry getListEntry(int index) {

		if (index < serverListInternet.size()) {
			return serverListInternet.get(index);
		} else {
			index = index - serverListInternet.size();

			if (index == 0) {
				return lanScanEntry;
			} else {
				--index;
				return serverListLan.get(index);
			}
		}
	}

	protected int getSize() {

		return serverListInternet.size() + 1 + serverListLan.size();
	}

	public void setSelectedSlotIndex(int selectedSlotIndexIn) {

		selectedSlotIndex = selectedSlotIndexIn;
	}

	/**
	 * Returns true if the element passed in is currently selected
	 */
	protected boolean isSelected(int slotIndex) {

		return slotIndex == selectedSlotIndex;
	}

	public int getSelected() {

		return selectedSlotIndex;
	}

	public void updateOnlineServers(ServerList p_148195_1_) {

		serverListInternet.clear();

		for (int i = 0; i < p_148195_1_.countServers(); ++i) {
			serverListInternet.add(new ServerListEntryNormal(owner, p_148195_1_.getServerData(i)));
		}
	}

	public void updateNetworkServers(List<LanServerInfo> p_148194_1_) {

		serverListLan.clear();

		for (LanServerInfo lanserverinfo : p_148194_1_) {
			serverListLan.add(new ServerListEntryLanDetected(owner, lanserverinfo));
		}
	}

	protected int getScrollBarX() {

		return super.getScrollBarX() + 30;
	}

	/**
	 * Gets the width of the list
	 */
	public int getListWidth() {

		return super.getListWidth() + 85;
	}

}
