package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.io.IOException;

public class SPacketUpdateHealth implements Packet<INetHandlerPlayClient> {

	private float health;
	private int foodLevel;
	private float saturationLevel;

	public SPacketUpdateHealth() {

	}

	public SPacketUpdateHealth(float healthIn, int foodLevelIn, float saturationLevelIn) {

		health = healthIn;
		foodLevel = foodLevelIn;
		saturationLevel = saturationLevelIn;
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		health = buf.readFloat();
		foodLevel = buf.readVarInt();
		saturationLevel = buf.readFloat();
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeFloat(health);
		buf.writeVarInt(foodLevel);
		buf.writeFloat(saturationLevel);
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handleUpdateHealth(this);
	}

	public float getHealth() {

		return health;
	}

	public int getFoodLevel() {

		return foodLevel;
	}

	public float getSaturationLevel() {

		return saturationLevel;
	}

}
