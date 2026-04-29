package net.minecraft.network.play.client;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.Facing;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class CPacketPlayerTryUseItemOnBlock implements Packet<INetHandlerPlayServer> {

	private BlockPos position;
	private Facing placedBlockDirection;
	private Hand hand;
	private float facingX;
	private float facingY;
	private float facingZ;

	public CPacketPlayerTryUseItemOnBlock() {

	}

	public CPacketPlayerTryUseItemOnBlock(BlockPos posIn, Facing placedBlockDirectionIn, Hand handIn, float facingXIn, float facingYIn, float facingZIn) {

		position = posIn;
		placedBlockDirection = placedBlockDirectionIn;
		hand = handIn;
		facingX = facingXIn;
		facingY = facingYIn;
		facingZ = facingZIn;
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		position = buf.readBlockPos();
		placedBlockDirection = buf.readEnumValue(Facing.class);
		hand = buf.readEnumValue(Hand.class);
		facingX = buf.readFloat();
		facingY = buf.readFloat();
		facingZ = buf.readFloat();
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeBlockPos(position);
		buf.writeEnumValue(placedBlockDirection);
		buf.writeEnumValue(hand);
		buf.writeFloat(facingX);
		buf.writeFloat(facingY);
		buf.writeFloat(facingZ);
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayServer handler) {

		handler.processTryUseItemOnBlock(this);
	}

	public BlockPos getPos() {

		return position;
	}

	public Facing getDirection() {

		return placedBlockDirection;
	}

	public Hand getHand() {

		return hand;
	}

	public float getFacingX() {

		return facingX;
	}

	public float getFacingY() {

		return facingY;
	}

	public float getFacingZ() {

		return facingZ;
	}

}
