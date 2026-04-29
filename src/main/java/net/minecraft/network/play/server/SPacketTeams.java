package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;

import java.util.Collection;

public class SPacketTeams implements Packet<INetHandlerPlayClient> {

	private final Collection<String> players;
	private String name = "";
	private String displayName = "";
	private String prefix = "";
	private String suffix = "";
	private String nameTagVisibility;
	private String collisionRule;
	private int color;
	private int action;
	private int friendlyFlags;

	public SPacketTeams() {

		nameTagVisibility = Team.Visible.ALWAYS.internalName;
		collisionRule = Team.CollisionRule.ALWAYS.name;
		color = -1;
		players = Lists.newArrayList();
	}

	public SPacketTeams(ScorePlayerTeam teamIn, int actionIn) {

		nameTagVisibility = Team.Visible.ALWAYS.internalName;
		collisionRule = Team.CollisionRule.ALWAYS.name;
		color = -1;
		players = Lists.newArrayList();
		name = teamIn.getName();
		action = actionIn;

		if (actionIn == 0 || actionIn == 2) {
			displayName = teamIn.getDisplayName();
			prefix = teamIn.getPrefix();
			suffix = teamIn.getSuffix();
			friendlyFlags = teamIn.getFriendlyFlags();
			nameTagVisibility = teamIn.getNameTagVisibility().internalName;
			collisionRule = teamIn.getCollisionRule().name;
			color = teamIn.getColor().getColorIndex();
		}

		if (actionIn == 0) {
			players.addAll(teamIn.getMembershipCollection());
		}
	}

	public SPacketTeams(ScorePlayerTeam teamIn, Collection<String> playersIn, int actionIn) {

		nameTagVisibility = Team.Visible.ALWAYS.internalName;
		collisionRule = Team.CollisionRule.ALWAYS.name;
		color = -1;
		players = Lists.newArrayList();

		if (actionIn != 3 && actionIn != 4) {
			throw new IllegalArgumentException("Method must be join or leave for player constructor");
		} else if (playersIn != null && !playersIn.isEmpty()) {
			action = actionIn;
			name = teamIn.getName();
			players.addAll(playersIn);
		} else {
			throw new IllegalArgumentException("Players cannot be null/empty");
		}
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		name = buf.readString(16);
		action = buf.readByte();

		if (action == 0 || action == 2) {
			displayName = buf.readString(32);
			prefix = buf.readString(16);
			suffix = buf.readString(16);
			friendlyFlags = buf.readByte();
			nameTagVisibility = buf.readString(32);
			collisionRule = buf.readString(32);
			color = buf.readByte();
		}

		if (action == 0 || action == 3 || action == 4) {
			int i = buf.readVarInt();

			for (int j = 0; j < i; ++j) {
				players.add(buf.readString(40));
			}
		}
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeString(name);
		buf.writeByte(action);

		if (action == 0 || action == 2) {
			buf.writeString(displayName);
			buf.writeString(prefix);
			buf.writeString(suffix);
			buf.writeByte(friendlyFlags);
			buf.writeString(nameTagVisibility);
			buf.writeString(collisionRule);
			buf.writeByte(color);
		}

		if (action == 0 || action == 3 || action == 4) {
			buf.writeVarInt(players.size());

			for (String s : players) {
				buf.writeString(s);
			}
		}
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handleTeams(this);
	}

	public String getName() {

		return name;
	}

	public String getDisplayName() {

		return displayName;
	}

	public String getPrefix() {

		return prefix;
	}

	public String getSuffix() {

		return suffix;
	}

	public Collection<String> getPlayers() {

		return players;
	}

	public int getAction() {

		return action;
	}

	public int getFriendlyFlags() {

		return friendlyFlags;
	}

	public int getColor() {

		return color;
	}

	public String getNameTagVisibility() {

		return nameTagVisibility;
	}

	public String getCollisionRule() {

		return collisionRule;
	}

}
