package net.minecraft.network.handshake.client;

import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;

import java.io.IOException;

public class C00Handshake implements Packet<INetHandlerHandshakeServer> {

	private int protocolVersion;
	private String ip;
	private int port;
	private EnumConnectionState requestedState;

	public C00Handshake() {

	}

	public C00Handshake(String p_i47613_1_, int p_i47613_2_, EnumConnectionState p_i47613_3_) {

		protocolVersion = 340;
		ip = p_i47613_1_;
		port = p_i47613_2_;
		requestedState = p_i47613_3_;
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) throws IOException {

		protocolVersion = buf.readVarInt();
		ip = buf.readString(255);
		port = buf.readUnsignedShort();
		requestedState = EnumConnectionState.getById(buf.readVarInt());
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) throws IOException {

		buf.writeVarInt(protocolVersion);
		buf.writeString(ip);
		buf.writeShort(port);
		buf.writeVarInt(requestedState.getId());
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerHandshakeServer handler) {

		handler.processHandshake(this);
	}

	public EnumConnectionState getRequestedState() {

		return requestedState;
	}

	public int getProtocolVersion() {

		return protocolVersion;
	}

}
