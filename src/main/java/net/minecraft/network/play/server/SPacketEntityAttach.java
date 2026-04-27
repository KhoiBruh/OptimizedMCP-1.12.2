package net.minecraft.network.play.server;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import javax.annotation.Nullable;
import java.io.IOException;

public class SPacketEntityAttach implements Packet<INetHandlerPlayClient> {

	private int entityId;
	private int vehicleEntityId;

	public SPacketEntityAttach() {

	}

	public SPacketEntityAttach(Entity entityIn, @Nullable Entity vehicleIn) {

		entityId = entityIn.getEntityId();
		vehicleEntityId = vehicleIn != null ? vehicleIn.getEntityId() : -1;
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		entityId = buf.readInt();
		vehicleEntityId = buf.readInt();
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeInt(entityId);
		buf.writeInt(vehicleEntityId);
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handleEntityAttach(this);
	}

	public int getEntityId() {

		return entityId;
	}

	public int getVehicleEntityId() {

		return vehicleEntityId;
	}

}
