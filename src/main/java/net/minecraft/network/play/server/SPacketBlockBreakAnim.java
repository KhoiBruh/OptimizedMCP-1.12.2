package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;

public class SPacketBlockBreakAnim implements Packet<INetHandlerPlayClient> {

	private int breakerId;
	private BlockPos position;
	private int progress;

	public SPacketBlockBreakAnim() {

	}

	public SPacketBlockBreakAnim(int breakerIdIn, BlockPos positionIn, int progressIn) {

		breakerId = breakerIdIn;
		position = positionIn;
		progress = progressIn;
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		breakerId = buf.readVarInt();
		position = buf.readBlockPos();
		progress = buf.readUnsignedByte();
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeVarInt(breakerId);
		buf.writeBlockPos(position);
		buf.writeByte(progress);
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handleBlockBreakAnim(this);
	}

	public int getBreakerId() {

		return breakerId;
	}

	public BlockPos getPosition() {

		return position;
	}

	public int getProgress() {

		return progress;
	}

}
