package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;

import java.io.IOException;

public class SPacketPlayerListHeaderFooter implements Packet<INetHandlerPlayClient> {

	private ITextComponent header;
	private ITextComponent footer;

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		header = buf.readTextComponent();
		footer = buf.readTextComponent();
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeTextComponent(header);
		buf.writeTextComponent(footer);
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handlePlayerListHeaderFooter(this);
	}

	public ITextComponent getHeader() {

		return header;
	}

	public ITextComponent getFooter() {

		return footer;
	}

}
