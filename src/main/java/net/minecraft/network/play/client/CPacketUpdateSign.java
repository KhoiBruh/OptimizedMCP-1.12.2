package net.minecraft.network.play.client;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import java.io.IOException;

public class CPacketUpdateSign implements Packet<INetHandlerPlayServer> {

	private BlockPos pos;
	private String[] lines;

	public CPacketUpdateSign() {

	}

	public CPacketUpdateSign(BlockPos posIn, ITextComponent[] linesIn) {

		pos = posIn;
		lines = new String[]{linesIn[0].getUnformattedText(), linesIn[1].getUnformattedText(), linesIn[2].getUnformattedText(), linesIn[3].getUnformattedText()};
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) throws IOException {

		pos = buf.readBlockPos();
		lines = new String[4];

		for (int i = 0; i < 4; ++i) {
			lines[i] = buf.readString(384);
		}
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) throws IOException {

		buf.writeBlockPos(pos);

		for (int i = 0; i < 4; ++i) {
			buf.writeString(lines[i]);
		}
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayServer handler) {

		handler.processUpdateSign(this);
	}

	public BlockPos getPosition() {

		return pos;
	}

	public String[] getLines() {

		return lines;
	}

}
