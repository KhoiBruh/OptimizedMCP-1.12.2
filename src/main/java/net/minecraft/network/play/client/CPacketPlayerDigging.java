package net.minecraft.network.play.client;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.Facing;
import net.minecraft.util.math.BlockPos;

public class CPacketPlayerDigging implements Packet<INetHandlerPlayServer> {

	private BlockPos position;
	private Facing facing;

	/**
	 * Status of the digging (started, ongoing, broken).
	 */
	private CPacketPlayerDigging.Action action;

	public CPacketPlayerDigging() {

	}

	public CPacketPlayerDigging(CPacketPlayerDigging.Action actionIn, BlockPos posIn, Facing facingIn) {

		action = actionIn;
		position = posIn;
		facing = facingIn;
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		action = buf.readEnumValue(Action.class);
		position = buf.readBlockPos();
		facing = Facing.getFront(buf.readUnsignedByte());
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeEnumValue(action);
		buf.writeBlockPos(position);
		buf.writeByte(facing.getIndex());
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayServer handler) {

		handler.processPlayerDigging(this);
	}

	public BlockPos getPosition() {

		return position;
	}

	public Facing getFacing() {

		return facing;
	}

	public CPacketPlayerDigging.Action getAction() {

		return action;
	}

	public enum Action {
		START_DESTROY_BLOCK,
		ABORT_DESTROY_BLOCK,
		STOP_DESTROY_BLOCK,
		DROP_ALL_ITEMS,
		DROP_ITEM,
		RELEASE_USE_ITEM,
		SWAP_HELD_ITEMS
	}

}
