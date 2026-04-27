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

import javax.annotation.Nullable;
import java.util.Map;

public enum EnumConnectionState {
	HANDSHAKING(-1) {
		{
			registerPacket(EnumPacketDirection.SERVERBOUND, C00Handshake.class);
		}
	},
	PLAY(0) {
		{
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketSpawnObject.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketSpawnExperienceOrb.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketSpawnGlobalEntity.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketSpawnMob.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketSpawnPainting.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketSpawnPlayer.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketAnimation.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketStatistics.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketBlockBreakAnim.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketUpdateTileEntity.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketBlockAction.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketBlockChange.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketUpdateBossInfo.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketServerDifficulty.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketTabComplete.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketChat.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketMultiBlockChange.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketConfirmTransaction.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketCloseWindow.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketOpenWindow.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketWindowItems.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketWindowProperty.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketSetSlot.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketCooldown.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketCustomPayload.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketCustomSound.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketDisconnect.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketEntityStatus.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketExplosion.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketUnloadChunk.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketChangeGameState.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketKeepAlive.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketChunkData.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketEffect.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketParticles.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketJoinGame.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketMaps.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketEntity.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketEntity.S15PacketEntityRelMove.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketEntity.S17PacketEntityLookMove.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketEntity.S16PacketEntityLook.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketMoveVehicle.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketSignEditorOpen.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketPlaceGhostRecipe.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketPlayerAbilities.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketCombatEvent.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketPlayerListItem.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketPlayerPosLook.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketUseBed.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketRecipeBook.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketDestroyEntities.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketRemoveEntityEffect.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketResourcePackSend.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketRespawn.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketEntityHeadLook.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketSelectAdvancementsTab.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketWorldBorder.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketCamera.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketHeldItemChange.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketDisplayObjective.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketEntityMetadata.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketEntityAttach.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketEntityVelocity.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketEntityEquipment.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketSetExperience.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketUpdateHealth.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketScoreboardObjective.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketSetPassengers.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketTeams.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketUpdateScore.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketSpawnPosition.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketTimeUpdate.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketTitle.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketSoundEffect.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketPlayerListHeaderFooter.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketCollectItem.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketEntityTeleport.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketAdvancementInfo.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketEntityProperties.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketEntityEffect.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketConfirmTeleport.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketTabComplete.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketChatMessage.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketClientStatus.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketClientSettings.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketConfirmTransaction.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketEnchantItem.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketClickWindow.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketCloseWindow.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketCustomPayload.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketUseEntity.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketKeepAlive.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketPlayer.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketPlayer.Position.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketPlayer.PositionRotation.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketPlayer.Rotation.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketVehicleMove.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketSteerBoat.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketPlaceRecipe.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketPlayerAbilities.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketPlayerDigging.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketEntityAction.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketInput.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketRecipeInfo.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketResourcePackStatus.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketSeenAdvancements.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketHeldItemChange.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketCreativeInventoryAction.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketUpdateSign.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketAnimation.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketSpectate.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketPlayerTryUseItemOnBlock.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketPlayerTryUseItem.class);
		}
	},
	STATUS(1) {
		{
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketServerQuery.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketServerInfo.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketPing.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketPong.class);
		}
	},
	LOGIN(2) {
		{
			registerPacket(EnumPacketDirection.CLIENTBOUND, net.minecraft.network.login.server.SPacketDisconnect.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketEncryptionRequest.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketLoginSuccess.class);
			registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketEnableCompression.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketLoginStart.class);
			registerPacket(EnumPacketDirection.SERVERBOUND, CPacketEncryptionResponse.class);
		}
	};

	private static final EnumConnectionState[] STATES_BY_ID = new EnumConnectionState[4];
	private static final Map<Class<? extends Packet<?>>, EnumConnectionState> STATES_BY_CLASS = Maps.newHashMap();
	private final int id;
	private final Map<EnumPacketDirection, BiMap<Integer, Class<? extends Packet<?>>>> directionMaps;

	EnumConnectionState(int protocolId) {

		directionMaps = Maps.newEnumMap(EnumPacketDirection.class);
		id = protocolId;
	}

	protected EnumConnectionState registerPacket(EnumPacketDirection direction, Class<? extends Packet<?>> packetClass) {

		BiMap<Integer, Class<? extends Packet<?>>> bimap = directionMaps.get(direction);

		if (bimap == null) {
			bimap = HashBiMap.create();
			directionMaps.put(direction, bimap);
		}

		if (bimap.containsValue(packetClass)) {
			String s = direction + " packet " + packetClass + " is already known to ID " + bimap.inverse().get(packetClass);
			LogManager.getLogger().fatal(s);
			throw new IllegalArgumentException(s);
		} else {
			bimap.put(Integer.valueOf(bimap.size()), packetClass);
			return this;
		}
	}

	public Integer getPacketId(EnumPacketDirection direction, Packet<?> packetIn) throws Exception {

		return (Integer) ((BiMap) directionMaps.get(direction)).inverse().get(packetIn.getClass());
	}

	@Nullable
	public Packet<?> getPacket(EnumPacketDirection direction, int packetId) throws InstantiationException, IllegalAccessException {

		Class<? extends Packet<?>> oclass = (Class) ((BiMap) directionMaps.get(direction)).get(Integer.valueOf(packetId));
		return oclass == null ? null : oclass.newInstance();
	}

	public int getId() {

		return id;
	}

	public static EnumConnectionState getById(int stateId) {

		return stateId >= -1 && stateId <= 2 ? STATES_BY_ID[stateId - -1] : null;
	}

	public static EnumConnectionState getFromPacket(Packet<?> packetIn) {

		return STATES_BY_CLASS.get(packetIn.getClass());
	}

	static {
		for (EnumConnectionState enumconnectionstate : values()) {
			int i = enumconnectionstate.getId();

			if (i < -1 || i > 2) {
				throw new Error("Invalid protocol ID " + i);
			}

			STATES_BY_ID[i - -1] = enumconnectionstate;

			for (EnumPacketDirection enumpacketdirection : enumconnectionstate.directionMaps.keySet()) {
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
}
