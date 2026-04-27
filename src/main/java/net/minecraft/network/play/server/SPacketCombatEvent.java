package net.minecraft.network.play.server;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;

public class SPacketCombatEvent implements Packet<INetHandlerPlayClient> {

	public SPacketCombatEvent.Event eventType;
	public int playerId;
	public int entityId;
	public int duration;
	public ITextComponent deathMessage;

	public SPacketCombatEvent() {

	}

	public SPacketCombatEvent(CombatTracker tracker, SPacketCombatEvent.Event eventIn) {

		this(tracker, eventIn, true);
	}

	@SuppressWarnings("incomplete-switch")
	public SPacketCombatEvent(CombatTracker tracker, SPacketCombatEvent.Event eventIn, boolean showDeathMessage) {

		eventType = eventIn;
		EntityLivingBase entitylivingbase = tracker.getBestAttacker();

		switch (eventIn) {
			case END_COMBAT:
				duration = tracker.getCombatDuration();
				entityId = entitylivingbase == null ? -1 : entitylivingbase.getEntityId();
				break;

			case ENTITY_DIED:
				playerId = tracker.getFighter().getEntityId();
				entityId = entitylivingbase == null ? -1 : entitylivingbase.getEntityId();

				if (showDeathMessage) {
					deathMessage = tracker.getDeathMessage();
				} else {
					deathMessage = new TextComponentString("");
				}
		}
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) throws IOException {

		eventType = buf.readEnumValue(Event.class);

		if (eventType == SPacketCombatEvent.Event.END_COMBAT) {
			duration = buf.readVarInt();
			entityId = buf.readInt();
		} else if (eventType == SPacketCombatEvent.Event.ENTITY_DIED) {
			playerId = buf.readVarInt();
			entityId = buf.readInt();
			deathMessage = buf.readTextComponent();
		}
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeEnumValue(eventType);

		if (eventType == SPacketCombatEvent.Event.END_COMBAT) {
			buf.writeVarInt(duration);
			buf.writeInt(entityId);
		} else if (eventType == SPacketCombatEvent.Event.ENTITY_DIED) {
			buf.writeVarInt(playerId);
			buf.writeInt(entityId);
			buf.writeTextComponent(deathMessage);
		}
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handleCombatEvent(this);
	}

	public enum Event {
		ENTER_COMBAT,
		END_COMBAT,
		ENTITY_DIED
	}

}
