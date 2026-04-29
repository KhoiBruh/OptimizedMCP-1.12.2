package net.minecraft.network.play.client;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CPacketUseEntity implements Packet<INetHandlerPlayServer> {

	private int entityId;
	private CPacketUseEntity.Action action;
	private Vec3d hitVec;
	private Hand hand;

	public CPacketUseEntity() {

	}

	public CPacketUseEntity(Entity entityIn) {

		entityId = entityIn.getEntityId();
		action = CPacketUseEntity.Action.ATTACK;
	}

	public CPacketUseEntity(Entity entityIn, Hand handIn) {

		entityId = entityIn.getEntityId();
		action = CPacketUseEntity.Action.INTERACT;
		hand = handIn;
	}

	public CPacketUseEntity(Entity entityIn, Hand handIn, Vec3d hitVecIn) {

		entityId = entityIn.getEntityId();
		action = CPacketUseEntity.Action.INTERACT_AT;
		hand = handIn;
		hitVec = hitVecIn;
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		entityId = buf.readVarInt();
		action = buf.readEnumValue(Action.class);

		if (action == CPacketUseEntity.Action.INTERACT_AT) {
			hitVec = new Vec3d(buf.readFloat(), buf.readFloat(), buf.readFloat());
		}

		if (action == CPacketUseEntity.Action.INTERACT || action == CPacketUseEntity.Action.INTERACT_AT) {
			hand = buf.readEnumValue(Hand.class);
		}
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeVarInt(entityId);
		buf.writeEnumValue(action);

		if (action == CPacketUseEntity.Action.INTERACT_AT) {
			buf.writeFloat((float) hitVec.x());
			buf.writeFloat((float) hitVec.y());
			buf.writeFloat((float) hitVec.z());
		}

		if (action == CPacketUseEntity.Action.INTERACT || action == CPacketUseEntity.Action.INTERACT_AT) {
			buf.writeEnumValue(hand);
		}
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayServer handler) {

		handler.processUseEntity(this);
	}

	
	public Entity getEntityFromWorld(World worldIn) {

		return worldIn.getEntityByID(entityId);
	}

	public CPacketUseEntity.Action getAction() {

		return action;
	}

	public Hand getHand() {

		return hand;
	}

	public Vec3d getHitVec() {

		return hitVec;
	}

	public enum Action {
		INTERACT,
		ATTACK,
		INTERACT_AT
	}

}
