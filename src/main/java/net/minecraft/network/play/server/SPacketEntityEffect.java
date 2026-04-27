package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.io.IOException;

public class SPacketEntityEffect implements Packet<INetHandlerPlayClient> {

	private int entityId;
	private byte effectId;
	private byte amplifier;
	private int duration;
	private byte flags;

	public SPacketEntityEffect() {

	}

	public SPacketEntityEffect(int entityIdIn, PotionEffect effect) {

		entityId = entityIdIn;
		effectId = (byte) (Potion.getIdFromPotion(effect.getPotion()) & 255);
		amplifier = (byte) (effect.getAmplifier() & 255);

		if (effect.getDuration() > 32767) {
			duration = 32767;
		} else {
			duration = effect.getDuration();
		}

		flags = 0;

		if (effect.getIsAmbient()) {
			flags = (byte) (flags | 1);
		}

		if (effect.doesShowParticles()) {
			flags = (byte) (flags | 2);
		}
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) throws IOException {

		entityId = buf.readVarInt();
		effectId = buf.readByte();
		amplifier = buf.readByte();
		duration = buf.readVarInt();
		flags = buf.readByte();
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) throws IOException {

		buf.writeVarInt(entityId);
		buf.writeByte(effectId);
		buf.writeByte(amplifier);
		buf.writeVarInt(duration);
		buf.writeByte(flags);
	}

	public boolean isMaxDuration() {

		return duration == 32767;
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handleEntityEffect(this);
	}

	public int getEntityId() {

		return entityId;
	}

	public byte getEffectId() {

		return effectId;
	}

	public byte getAmplifier() {

		return amplifier;
	}

	public int getDuration() {

		return duration;
	}

	public boolean doesShowParticles() {

		return (flags & 2) == 2;
	}

	public boolean getIsAmbient() {

		return (flags & 1) == 1;
	}

}
