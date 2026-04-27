package net.minecraft.network.play.client;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class CPacketInput implements Packet<INetHandlerPlayServer> {

	/**
	 * Positive for left strafe, negative for right
	 */
	private float strafeSpeed;
	private float forwardSpeed;
	private boolean jumping;
	private boolean sneaking;

	public CPacketInput() {

	}

	public CPacketInput(float strafeSpeedIn, float forwardSpeedIn, boolean jumpingIn, boolean sneakingIn) {

		strafeSpeed = strafeSpeedIn;
		forwardSpeed = forwardSpeedIn;
		jumping = jumpingIn;
		sneaking = sneakingIn;
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		strafeSpeed = buf.readFloat();
		forwardSpeed = buf.readFloat();
		byte b0 = buf.readByte();
		jumping = (b0 & 1) > 0;
		sneaking = (b0 & 2) > 0;
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeFloat(strafeSpeed);
		buf.writeFloat(forwardSpeed);
		byte b0 = 0;

		if (jumping) {
			b0 = (byte) (b0 | 1);
		}

		if (sneaking) {
			b0 = (byte) (b0 | 2);
		}

		buf.writeByte(b0);
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayServer handler) {

		handler.processInput(this);
	}

	public float getStrafeSpeed() {

		return strafeSpeed;
	}

	public float getForwardSpeed() {

		return forwardSpeed;
	}

	public boolean isJumping() {

		return jumping;
	}

	public boolean isSneaking() {

		return sneaking;
	}

}
