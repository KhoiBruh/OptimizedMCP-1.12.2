package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.ResourceLocation;

public class SPacketSelectAdvancementsTab implements Packet<INetHandlerPlayClient> {

	
	private ResourceLocation tab;

	public SPacketSelectAdvancementsTab() {

	}

	public SPacketSelectAdvancementsTab(ResourceLocation p_i47596_1_) {

		tab = p_i47596_1_;
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handleSelectAdvancementsTab(this);
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		if (buf.readBoolean()) {
			tab = buf.readResourceLocation();
		}
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeBoolean(tab != null);

		if (tab != null) {
			buf.writeResourceLocation(tab);
		}
	}

	
	public ResourceLocation getTab() {

		return tab;
	}

}
