package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;

public class SPacketScoreboardObjective implements Packet<INetHandlerPlayClient> {

	private String objectiveName;
	private String objectiveValue;
	private IScoreCriteria.RenderType type;
	private int action;

	public SPacketScoreboardObjective() {

	}

	public SPacketScoreboardObjective(ScoreObjective objective, int actionIn) {

		objectiveName = objective.getName();
		objectiveValue = objective.getDisplayName();
		type = objective.getCriteria().getRenderType();
		action = actionIn;
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		objectiveName = buf.readString(16);
		action = buf.readByte();

		if (action == 0 || action == 2) {
			objectiveValue = buf.readString(32);
			type = IScoreCriteria.RenderType.getByName(buf.readString(16));
		}
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeString(objectiveName);
		buf.writeByte(action);

		if (action == 0 || action == 2) {
			buf.writeString(objectiveValue);
			buf.writeString(type.getRenderType());
		}
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handleScoreboardObjective(this);
	}

	public String getObjectiveName() {

		return objectiveName;
	}

	public String getObjectiveValue() {

		return objectiveValue;
	}

	public int getAction() {

		return action;
	}

	public IScoreCriteria.RenderType getRenderType() {

		return type;
	}

}
