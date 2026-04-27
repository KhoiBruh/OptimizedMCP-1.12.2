package net.minecraft.network.login.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.INetHandlerLoginClient;

import java.io.IOException;

public class SPacketEnableCompression implements Packet<INetHandlerLoginClient> {

	private int compressionThreshold;

	public SPacketEnableCompression() {

	}

	public SPacketEnableCompression(int thresholdIn) {

		compressionThreshold = thresholdIn;
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		compressionThreshold = buf.readVarInt();
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeVarInt(compressionThreshold);
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerLoginClient handler) {

		handler.handleEnableCompression(this);
	}

	public int getCompressionThreshold() {

		return compressionThreshold;
	}

}
