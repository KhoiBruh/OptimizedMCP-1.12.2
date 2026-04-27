package net.minecraft.network.play.server;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.io.IOException;

public class SPacketAnimation implements Packet<INetHandlerPlayClient> {

	private int entityId;
	private int type;

	public SPacketAnimation() {

	}

	public SPacketAnimation(Entity entityIn, int typeIn) {

		entityId = entityIn.getEntityId();
		type = typeIn;
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		entityId = buf.readVarInt();
		type = buf.readUnsignedByte();
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeVarInt(entityId);
		buf.writeByte(type);
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handleAnimation(this);
	}

	public int getEntityID() {

		return entityId;
	}

	public int getAnimationType() {

		return type;
	}

}
