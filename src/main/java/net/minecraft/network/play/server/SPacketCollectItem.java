package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.io.IOException;

public class SPacketCollectItem implements Packet<INetHandlerPlayClient> {

	private int collectedItemEntityId;
	private int entityId;
	private int collectedQuantity;

	public SPacketCollectItem() {

	}

	public SPacketCollectItem(int p_i47316_1_, int p_i47316_2_, int p_i47316_3_) {

		collectedItemEntityId = p_i47316_1_;
		entityId = p_i47316_2_;
		collectedQuantity = p_i47316_3_;
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) throws IOException {

		collectedItemEntityId = buf.readVarInt();
		entityId = buf.readVarInt();
		collectedQuantity = buf.readVarInt();
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) throws IOException {

		buf.writeVarInt(collectedItemEntityId);
		buf.writeVarInt(entityId);
		buf.writeVarInt(collectedQuantity);
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handleCollectItem(this);
	}

	public int getCollectedItemEntityID() {

		return collectedItemEntityId;
	}

	public int getEntityID() {

		return entityId;
	}

	public int getAmount() {

		return collectedQuantity;
	}

}
