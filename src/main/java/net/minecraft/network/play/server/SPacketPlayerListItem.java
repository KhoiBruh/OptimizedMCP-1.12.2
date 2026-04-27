package net.minecraft.network.play.server;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public class SPacketPlayerListItem implements Packet<INetHandlerPlayClient> {

	private SPacketPlayerListItem.Action action;
	private final List<SPacketPlayerListItem.AddPlayerData> players = Lists.newArrayList();

	public SPacketPlayerListItem() {

	}

	public SPacketPlayerListItem(SPacketPlayerListItem.Action actionIn, EntityPlayerMP... playersIn) {

		action = actionIn;

		for (EntityPlayerMP entityplayermp : playersIn) {
			players.add(new SPacketPlayerListItem.AddPlayerData(entityplayermp.getGameProfile(), entityplayermp.ping, entityplayermp.interactionManager.getGameType(), entityplayermp.getTabListDisplayName()));
		}
	}

	public SPacketPlayerListItem(SPacketPlayerListItem.Action actionIn, Iterable<EntityPlayerMP> playersIn) {

		action = actionIn;

		for (EntityPlayerMP entityplayermp : playersIn) {
			players.add(new SPacketPlayerListItem.AddPlayerData(entityplayermp.getGameProfile(), entityplayermp.ping, entityplayermp.interactionManager.getGameType(), entityplayermp.getTabListDisplayName()));
		}
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) throws IOException {

		action = buf.readEnumValue(Action.class);
		int i = buf.readVarInt();

		for (int j = 0; j < i; ++j) {
			GameProfile gameprofile = null;
			int k = 0;
			GameType gametype = null;
			ITextComponent itextcomponent = null;

			switch (action) {
				case ADD_PLAYER:
					gameprofile = new GameProfile(buf.readUniqueId(), buf.readString(16));
					int l = buf.readVarInt();
					int i1 = 0;

					for (; i1 < l; ++i1) {
						String s = buf.readString(32767);
						String s1 = buf.readString(32767);

						if (buf.readBoolean()) {
							gameprofile.getProperties().put(s, new Property(s, s1, buf.readString(32767)));
						} else {
							gameprofile.getProperties().put(s, new Property(s, s1));
						}
					}

					gametype = GameType.getByID(buf.readVarInt());
					k = buf.readVarInt();

					if (buf.readBoolean()) {
						itextcomponent = buf.readTextComponent();
					}

					break;

				case UPDATE_GAME_MODE:
					gameprofile = new GameProfile(buf.readUniqueId(), null);
					gametype = GameType.getByID(buf.readVarInt());
					break;

				case UPDATE_LATENCY:
					gameprofile = new GameProfile(buf.readUniqueId(), null);
					k = buf.readVarInt();
					break;

				case UPDATE_DISPLAY_NAME:
					gameprofile = new GameProfile(buf.readUniqueId(), null);

					if (buf.readBoolean()) {
						itextcomponent = buf.readTextComponent();
					}

					break;

				case REMOVE_PLAYER:
					gameprofile = new GameProfile(buf.readUniqueId(), null);
			}

			players.add(new SPacketPlayerListItem.AddPlayerData(gameprofile, k, gametype, itextcomponent));
		}
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) throws IOException {

		buf.writeEnumValue(action);
		buf.writeVarInt(players.size());

		for (SPacketPlayerListItem.AddPlayerData spacketplayerlistitem$addplayerdata : players) {
			switch (action) {
				case ADD_PLAYER:
					buf.writeUniqueId(spacketplayerlistitem$addplayerdata.getProfile().getId());
					buf.writeString(spacketplayerlistitem$addplayerdata.getProfile().getName());
					buf.writeVarInt(spacketplayerlistitem$addplayerdata.getProfile().getProperties().size());

					for (Property property : spacketplayerlistitem$addplayerdata.getProfile().getProperties().values()) {
						buf.writeString(property.getName());
						buf.writeString(property.getValue());

						if (property.hasSignature()) {
							buf.writeBoolean(true);
							buf.writeString(property.getSignature());
						} else {
							buf.writeBoolean(false);
						}
					}

					buf.writeVarInt(spacketplayerlistitem$addplayerdata.getGameMode().getID());
					buf.writeVarInt(spacketplayerlistitem$addplayerdata.getPing());

					if (spacketplayerlistitem$addplayerdata.getDisplayName() == null) {
						buf.writeBoolean(false);
					} else {
						buf.writeBoolean(true);
						buf.writeTextComponent(spacketplayerlistitem$addplayerdata.getDisplayName());
					}

					break;

				case UPDATE_GAME_MODE:
					buf.writeUniqueId(spacketplayerlistitem$addplayerdata.getProfile().getId());
					buf.writeVarInt(spacketplayerlistitem$addplayerdata.getGameMode().getID());
					break;

				case UPDATE_LATENCY:
					buf.writeUniqueId(spacketplayerlistitem$addplayerdata.getProfile().getId());
					buf.writeVarInt(spacketplayerlistitem$addplayerdata.getPing());
					break;

				case UPDATE_DISPLAY_NAME:
					buf.writeUniqueId(spacketplayerlistitem$addplayerdata.getProfile().getId());

					if (spacketplayerlistitem$addplayerdata.getDisplayName() == null) {
						buf.writeBoolean(false);
					} else {
						buf.writeBoolean(true);
						buf.writeTextComponent(spacketplayerlistitem$addplayerdata.getDisplayName());
					}

					break;

				case REMOVE_PLAYER:
					buf.writeUniqueId(spacketplayerlistitem$addplayerdata.getProfile().getId());
			}
		}
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handlePlayerListItem(this);
	}

	public List<SPacketPlayerListItem.AddPlayerData> getEntries() {

		return players;
	}

	public SPacketPlayerListItem.Action getAction() {

		return action;
	}

	public String toString() {

		return MoreObjects.toStringHelper(this).add("action", action).add("entries", players).toString();
	}

	public enum Action {
		ADD_PLAYER,
		UPDATE_GAME_MODE,
		UPDATE_LATENCY,
		UPDATE_DISPLAY_NAME,
		REMOVE_PLAYER
	}

	public class AddPlayerData {

		private final int ping;
		private final GameType gamemode;
		private final GameProfile profile;
		private final ITextComponent displayName;

		public AddPlayerData(GameProfile profileIn, int latencyIn, GameType gameModeIn, @Nullable ITextComponent displayNameIn) {

			profile = profileIn;
			ping = latencyIn;
			gamemode = gameModeIn;
			displayName = displayNameIn;
		}

		public GameProfile getProfile() {

			return profile;
		}

		public int getPing() {

			return ping;
		}

		public GameType getGameMode() {

			return gamemode;
		}

		@Nullable
		public ITextComponent getDisplayName() {

			return displayName;
		}

		public String toString() {

			return MoreObjects.toStringHelper(this).add("latency", ping).add("gameMode", gamemode).add("profile", profile).add("displayName", displayName == null ? null : ITextComponent.Serializer.componentToJson(displayName)).toString();
		}

	}

}
