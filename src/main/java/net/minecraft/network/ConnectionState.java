package net.minecraft.network;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.CPacketEncryptionResponse;
import net.minecraft.network.login.client.CPacketLoginStart;
import net.minecraft.network.login.server.SPacketEnableCompression;
import net.minecraft.network.login.server.SPacketEncryptionRequest;
import net.minecraft.network.login.server.SPacketLoginSuccess;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.network.status.client.CPacketPing;
import net.minecraft.network.status.client.CPacketServerQuery;
import net.minecraft.network.status.server.SPacketPong;
import net.minecraft.network.status.server.SPacketServerInfo;
import org.apache.logging.log4j.LogManager;
import java.util.Map;

public enum ConnectionState {
	HANDSHAKING(-1) {
		{
			registerPacket(PacketDirection.SERVERBOUND, C00Handshake.class);
		}
	},
	PLAY(0) {
		{
			registerPacket(PacketDirection.CLIENTBOUND, SPacketSpawnObject.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketSpawnExperienceOrb.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketSpawnGlobalEntity.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketSpawnMob.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketSpawnPainting.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketSpawnPlayer.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketAnimation.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketStatistics.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketBlockBreakAnim.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketUpdateTileEntity.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketBlockAction.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketBlockChange.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketUpdateBossInfo.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketServerDifficulty.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketTabComplete.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketChat.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketMultiBlockChange.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketConfirmTransaction.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketCloseWindow.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketOpenWindow.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketWindowItems.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketWindowProperty.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketSetSlot.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketCooldown.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketCustomPayload.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketCustomSound.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketDisconnect.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketEntityStatus.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketExplosion.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketUnloadChunk.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketChangeGameState.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketKeepAlive.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketChunkData.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketEffect.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketParticles.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketJoinGame.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketMaps.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketEntity.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketEntity.S15PacketEntityRelMove.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketEntity.S17PacketEntityLookMove.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketEntity.S16PacketEntityLook.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketMoveVehicle.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketSignEditorOpen.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketPlaceGhostRecipe.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketPlayerAbilities.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketCombatEvent.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketPlayerListItem.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketPlayerPosLook.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketUseBed.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketRecipeBook.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketDestroyEntities.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketRemoveEntityEffect.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketResourcePackSend.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketRespawn.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketEntityHeadLook.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketSelectAdvancementsTab.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketWorldBorder.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketCamera.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketHeldItemChange.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketDisplayObjective.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketEntityMetadata.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketEntityAttach.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketEntityVelocity.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketEntityEquipment.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketSetExperience.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketUpdateHealth.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketScoreboardObjective.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketSetPassengers.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketTeams.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketUpdateScore.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketSpawnPosition.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketTimeUpdate.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketTitle.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketSoundEffect.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketPlayerListHeaderFooter.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketCollectItem.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketEntityTeleport.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketAdvancementInfo.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketEntityProperties.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketEntityEffect.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketConfirmTeleport.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketTabComplete.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketChatMessage.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketClientStatus.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketClientSettings.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketConfirmTransaction.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketEnchantItem.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketClickWindow.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketCloseWindow.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketCustomPayload.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketUseEntity.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketKeepAlive.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketPlayer.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketPlayer.Position.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketPlayer.PositionRotation.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketPlayer.Rotation.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketVehicleMove.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketSteerBoat.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketPlaceRecipe.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketPlayerAbilities.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketPlayerDigging.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketEntityAction.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketInput.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketRecipeInfo.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketResourcePackStatus.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketSeenAdvancements.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketHeldItemChange.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketCreativeInventoryAction.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketUpdateSign.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketAnimation.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketSpectate.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketPlayerTryUseItemOnBlock.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketPlayerTryUseItem.class);
		}
	},
	STATUS(1) {
		{
			registerPacket(PacketDirection.SERVERBOUND, CPacketServerQuery.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketServerInfo.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketPing.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketPong.class);
		}
	},
	LOGIN(2) {
		{
			registerPacket(PacketDirection.CLIENTBOUND, net.minecraft.network.login.server.SPacketDisconnect.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketEncryptionRequest.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketLoginSuccess.class);
			registerPacket(PacketDirection.CLIENTBOUND, SPacketEnableCompression.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketLoginStart.class);
			registerPacket(PacketDirection.SERVERBOUND, CPacketEncryptionResponse.class);
		}
	};

	private static final ConnectionState[] STATES_BY_ID = new ConnectionState[4];
	private static final Map<Class<? extends Packet<?>>, ConnectionState> STATES_BY_CLASS = Maps.newHashMap();

	static {
		for (ConnectionState enumconnectionstate : values()) {
			int i = enumconnectionstate.getId();

			if (i < -1 || i > 2) {
				throw new Error("Invalid protocol ID " + i);
			}

			STATES_BY_ID[i + 1] = enumconnectionstate;

			for (PacketDirection enumpacketdirection : enumconnectionstate.directionMaps.keySet()) {
				for (Class<? extends Packet<?>> oclass : (enumconnectionstate.directionMaps.get(enumpacketdirection)).values()) {
					if (STATES_BY_CLASS.containsKey(oclass) && STATES_BY_CLASS.get(oclass) != enumconnectionstate) {
						throw new Error("Packet " + oclass + " is already assigned to protocol " + STATES_BY_CLASS.get(oclass) + " - can't reassign to " + enumconnectionstate);
					}

					try {
						oclass.newInstance();
					} catch (Throwable var10) {
						throw new Error("Packet " + oclass + " fails instantiation checks! " + oclass);
					}

					STATES_BY_CLASS.put(oclass, enumconnectionstate);
				}
			}
		}
	}

	private final int id;
	private final Map<PacketDirection, BiMap<Integer, Class<? extends Packet<?>>>> directionMaps;

	ConnectionState(int protocolId) {

		directionMaps = Maps.newEnumMap(PacketDirection.class);
		id = protocolId;
	}

	public static ConnectionState getById(int stateId) {

		return stateId >= -1 && stateId <= 2 ? STATES_BY_ID[stateId + 1] : null;
	}

	public static ConnectionState getFromPacket(Packet<?> packetIn) {

		return STATES_BY_CLASS.get(packetIn.getClass());
	}

	protected ConnectionState registerPacket(PacketDirection direction, Class<? extends Packet<?>> packetClass) {

		BiMap<Integer, Class<? extends Packet<?>>> bimap = directionMaps.computeIfAbsent(direction, k -> HashBiMap.create());

		if (bimap.containsValue(packetClass)) {
			String s = direction + " packet " + packetClass + " is already known to ID " + bimap.inverse().get(packetClass);
			LogManager.getLogger().fatal(s);
			throw new IllegalArgumentException(s);
		} else {
			bimap.put(bimap.size(), packetClass);
			return this;
		}
	}

	public Integer getPacketId(PacketDirection direction, Packet<?> packetIn) {

		return (Integer) ((BiMap) directionMaps.get(direction)).inverse().get(packetIn.getClass());
	}

	
	public Packet<?> getPacket(PacketDirection direction, int packetId) throws InstantiationException, IllegalAccessException {

		Class<? extends Packet<?>> oclass = (Class) ((BiMap) directionMaps.get(direction)).get(packetId);
		return oclass == null ? null : oclass.newInstance();
	}

	public int getId() {

		return id;
	}
}
