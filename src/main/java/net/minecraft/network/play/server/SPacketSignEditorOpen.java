package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;

public class SPacketSignEditorOpen implements Packet<INetHandlerPlayClient> {

	private BlockPos signPosition;

	public SPacketSignEditorOpen() {

	}

	public SPacketSignEditorOpen(BlockPos posIn) {

		signPosition = posIn;
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handleSignEditorOpen(this);
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		signPosition = buf.readBlockPos();
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeBlockPos(signPosition);
	}

	public BlockPos getSignPosition() {

		return signPosition;
	}

}
