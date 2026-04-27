package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;

public class SPacketSpawnPosition implements Packet<INetHandlerPlayClient> {

	private BlockPos spawnBlockPos;

	public SPacketSpawnPosition() {

	}

	public SPacketSpawnPosition(BlockPos posIn) {

		spawnBlockPos = posIn;
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		spawnBlockPos = buf.readBlockPos();
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeBlockPos(spawnBlockPos);
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handleSpawnPosition(this);
	}

	public BlockPos getSpawnPos() {

		return spawnBlockPos;
	}

}
