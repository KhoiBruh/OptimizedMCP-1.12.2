package net.minecraft.network.play.client;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.Hand;

public class CPacketAnimation implements Packet<INetHandlerPlayServer> {

	private Hand hand;

	public CPacketAnimation() {

	}

	public CPacketAnimation(Hand handIn) {

		hand = handIn;
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		hand = buf.readEnumValue(Hand.class);
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeEnumValue(hand);
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayServer handler) {

		handler.handleAnimation(this);
	}

	public Hand getHand() {

		return hand;
	}

}
