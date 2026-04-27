package net.minecraft.network.play.client;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.io.IOException;

public class CPacketKeepAlive implements Packet<INetHandlerPlayServer> {

	private long key;

	public CPacketKeepAlive() {

	}

	public CPacketKeepAlive(long idIn) {

		key = idIn;
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayServer handler) {

		handler.processKeepAlive(this);
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		key = buf.readLong();
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeLong(key);
	}

	public long getKey() {

		return key;
	}

}
