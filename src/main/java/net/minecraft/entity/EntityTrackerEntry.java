package net.minecraft.entity;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.*;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.*;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.MapData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class EntityTrackerEntry {

	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * The entity that this EntityTrackerEntry tracks.
	 */
	private final Entity trackedEntity;
	private final int range;
	/**
	 * check for sync when ticks % updateFrequency==0
	 */
	private final int updateFrequency;
	private final boolean sendVelocityUpdates;
	private final Set<EntityPlayerMP> trackingPlayers = Sets.newHashSet();
	public int updateCounter;
	public boolean playerEntitiesUpdated;
	private int maxRange;
	/**
	 * The encoded entity X position.
	 */
	private long encodedPosX;
	/**
	 * The encoded entity Y position.
	 */
	private long encodedPosY;
	/**
	 * The encoded entity Z position.
	 */
	private long encodedPosZ;
	/**
	 * The encoded entity yaw rotation.
	 */
	private int encodedRotationYaw;
	/**
	 * The encoded entity pitch rotation.
	 */
	private int encodedRotationPitch;
	private int lastHeadMotion;
	private double lastTrackedEntityMotionX;
	private double lastTrackedEntityMotionY;
	private double motionZ;
	private double lastTrackedEntityPosX;
	private double lastTrackedEntityPosY;
	private double lastTrackedEntityPosZ;
	private boolean updatedPlayerVisibility;
	/**
	 * every 400 ticks a  full teleport packet is sent, rather than just a "move me +x" command, so that position
	 * remains fully synced.
	 */
	private int ticksSinceLastForcedTeleport;
	private List<Entity> passengers = Collections.emptyList();
	private boolean ridingEntity;
	private boolean onGround;

	public EntityTrackerEntry(Entity entityIn, int rangeIn, int maxRangeIn, int updateFrequencyIn, boolean sendVelocityUpdatesIn) {

		trackedEntity = entityIn;
		range = rangeIn;
		maxRange = maxRangeIn;
		updateFrequency = updateFrequencyIn;
		sendVelocityUpdates = sendVelocityUpdatesIn;
		encodedPosX = EntityTracker.getPositionLong(entityIn.posX);
		encodedPosY = EntityTracker.getPositionLong(entityIn.posY);
		encodedPosZ = EntityTracker.getPositionLong(entityIn.posZ);
		encodedRotationYaw = MathHelper.floor(entityIn.rotationYaw * 256F / 360F);
		encodedRotationPitch = MathHelper.floor(entityIn.rotationPitch * 256F / 360F);
		lastHeadMotion = MathHelper.floor(entityIn.getRotationYawHead() * 256F / 360F);
		onGround = entityIn.onGround;
	}

	public boolean equals(Object p_equals_1_) {

		if (p_equals_1_ instanceof EntityTrackerEntry) {
			return ((EntityTrackerEntry) p_equals_1_).trackedEntity.getEntityId() == trackedEntity.getEntityId();
		} else {
			return false;
		}
	}

	public int hashCode() {

		return trackedEntity.getEntityId();
	}

	public void updatePlayerList(List<EntityPlayer> players) {

		playerEntitiesUpdated = false;

		if (!updatedPlayerVisibility || trackedEntity.getDistanceSq(lastTrackedEntityPosX, lastTrackedEntityPosY, lastTrackedEntityPosZ) > 16D) {
			lastTrackedEntityPosX = trackedEntity.posX;
			lastTrackedEntityPosY = trackedEntity.posY;
			lastTrackedEntityPosZ = trackedEntity.posZ;
			updatedPlayerVisibility = true;
			playerEntitiesUpdated = true;
			updatePlayerEntities(players);
		}

		List<Entity> list = trackedEntity.getPassengers();

		if (!list.equals(passengers)) {
			passengers = list;
			sendPacketToTrackedPlayers(new SPacketSetPassengers(trackedEntity));
		}

		if (trackedEntity instanceof EntityItemFrame entityitemframe && updateCounter % 10 == 0) {
			ItemStack itemstack = entityitemframe.getDisplayedItem();

			if (itemstack.getItem() instanceof ItemMap) {
				MapData mapdata = Items.FILLED_MAP.getMapData(itemstack, trackedEntity.world);

				for (EntityPlayer entityplayer : players) {
					EntityPlayerMP entityplayermp = (EntityPlayerMP) entityplayer;
					mapdata.updateVisiblePlayers(entityplayermp, itemstack);
					Packet<?> packet = Items.FILLED_MAP.createMapDataPacket(itemstack, trackedEntity.world, entityplayermp);

					if (packet != null) {
						entityplayermp.connection.sendPacket(packet);
					}
				}
			}

			sendMetadata();
		}

		if (updateCounter % updateFrequency == 0 || trackedEntity.isAirBorne || trackedEntity.getDataManager().isDirty()) {
			if (trackedEntity.isRiding()) {
				int j1 = MathHelper.floor(trackedEntity.rotationYaw * 256F / 360F);
				int l1 = MathHelper.floor(trackedEntity.rotationPitch * 256F / 360F);
				boolean flag3 = Math.abs(j1 - encodedRotationYaw) >= 1 || Math.abs(l1 - encodedRotationPitch) >= 1;

				if (flag3) {
					sendPacketToTrackedPlayers(new SPacketEntity.S16PacketEntityLook(trackedEntity.getEntityId(), (byte) j1, (byte) l1, trackedEntity.onGround));
					encodedRotationYaw = j1;
					encodedRotationPitch = l1;
				}

				encodedPosX = EntityTracker.getPositionLong(trackedEntity.posX);
				encodedPosY = EntityTracker.getPositionLong(trackedEntity.posY);
				encodedPosZ = EntityTracker.getPositionLong(trackedEntity.posZ);
				sendMetadata();
				ridingEntity = true;
			} else {
				++ticksSinceLastForcedTeleport;
				long i1 = EntityTracker.getPositionLong(trackedEntity.posX);
				long i2 = EntityTracker.getPositionLong(trackedEntity.posY);
				long j2 = EntityTracker.getPositionLong(trackedEntity.posZ);
				int k2 = MathHelper.floor(trackedEntity.rotationYaw * 256F / 360F);
				int i = MathHelper.floor(trackedEntity.rotationPitch * 256F / 360F);
				long j = i1 - encodedPosX;
				long k = i2 - encodedPosY;
				long l = j2 - encodedPosZ;
				Packet<?> packet1 = null;
				boolean flag = j * j + k * k + l * l >= 128L || updateCounter % 60 == 0;
				boolean flag1 = Math.abs(k2 - encodedRotationYaw) >= 1 || Math.abs(i - encodedRotationPitch) >= 1;

				if (updateCounter > 0 || trackedEntity instanceof EntityArrow) {
					if (j >= -32768L && j < 32768L && k >= -32768L && k < 32768L && l >= -32768L && l < 32768L && ticksSinceLastForcedTeleport <= 400 && !ridingEntity && onGround == trackedEntity.onGround) {
						if ((!flag || !flag1) && !(trackedEntity instanceof EntityArrow)) {
							if (flag) {
								packet1 = new SPacketEntity.S15PacketEntityRelMove(trackedEntity.getEntityId(), j, k, l, trackedEntity.onGround);
							} else if (flag1) {
								packet1 = new SPacketEntity.S16PacketEntityLook(trackedEntity.getEntityId(), (byte) k2, (byte) i, trackedEntity.onGround);
							}
						} else {
							packet1 = new SPacketEntity.S17PacketEntityLookMove(trackedEntity.getEntityId(), j, k, l, (byte) k2, (byte) i, trackedEntity.onGround);
						}
					} else {
						onGround = trackedEntity.onGround;
						ticksSinceLastForcedTeleport = 0;
						resetPlayerVisibility();
						packet1 = new SPacketEntityTeleport(trackedEntity);
					}
				}

				boolean flag2 = sendVelocityUpdates;

				if (trackedEntity instanceof EntityLivingBase && ((EntityLivingBase) trackedEntity).isElytraFlying()) {
					flag2 = true;
				}

				if (flag2 && updateCounter > 0) {
					double d0 = trackedEntity.motionX - lastTrackedEntityMotionX;
					double d1 = trackedEntity.motionY - lastTrackedEntityMotionY;
					double d2 = trackedEntity.motionZ - motionZ;
					double d3 = 0.02D;
					double d4 = d0 * d0 + d1 * d1 + d2 * d2;

					if (d4 > 4.0E-4D || d4 > 0D && trackedEntity.motionX == 0D && trackedEntity.motionY == 0D && trackedEntity.motionZ == 0D) {
						lastTrackedEntityMotionX = trackedEntity.motionX;
						lastTrackedEntityMotionY = trackedEntity.motionY;
						motionZ = trackedEntity.motionZ;
						sendPacketToTrackedPlayers(new SPacketEntityVelocity(trackedEntity.getEntityId(), lastTrackedEntityMotionX, lastTrackedEntityMotionY, motionZ));
					}
				}

				if (packet1 != null) {
					sendPacketToTrackedPlayers(packet1);
				}

				sendMetadata();

				if (flag) {
					encodedPosX = i1;
					encodedPosY = i2;
					encodedPosZ = j2;
				}

				if (flag1) {
					encodedRotationYaw = k2;
					encodedRotationPitch = i;
				}

				ridingEntity = false;
			}

			int k1 = MathHelper.floor(trackedEntity.getRotationYawHead() * 256F / 360F);

			if (Math.abs(k1 - lastHeadMotion) >= 1) {
				sendPacketToTrackedPlayers(new SPacketEntityHeadLook(trackedEntity, (byte) k1));
				lastHeadMotion = k1;
			}

			trackedEntity.isAirBorne = false;
		}

		++updateCounter;

		if (trackedEntity.velocityChanged) {
			sendToTrackingAndSelf(new SPacketEntityVelocity(trackedEntity));
			trackedEntity.velocityChanged = false;
		}
	}

	/**
	 * Sends the entity metadata (DataWatcher) and attributes to all players tracking this entity, including the entity
	 * itself if a player.
	 */
	private void sendMetadata() {

		EntityDataManager entitydatamanager = trackedEntity.getDataManager();

		if (entitydatamanager.isDirty()) {
			sendToTrackingAndSelf(new SPacketEntityMetadata(trackedEntity.getEntityId(), entitydatamanager, false));
		}

		if (trackedEntity instanceof EntityLivingBase) {
			AttributeMap attributemap = (AttributeMap) ((EntityLivingBase) trackedEntity).getAttributeMap();
			Set<IAttributeInstance> set = attributemap.getDirtyInstances();

			if (!set.isEmpty()) {
				sendToTrackingAndSelf(new SPacketEntityProperties(trackedEntity.getEntityId(), set));
			}

			set.clear();
		}
	}

	/**
	 * Send the given packet to all players tracking this entity.
	 */
	public void sendPacketToTrackedPlayers(Packet<?> packetIn) {

		for (EntityPlayerMP entityplayermp : trackingPlayers) {
			entityplayermp.connection.sendPacket(packetIn);
		}
	}

	public void sendToTrackingAndSelf(Packet<?> packetIn) {

		sendPacketToTrackedPlayers(packetIn);

		if (trackedEntity instanceof EntityPlayerMP) {
			((EntityPlayerMP) trackedEntity).connection.sendPacket(packetIn);
		}
	}

	public void sendDestroyEntityPacketToTrackedPlayers() {

		for (EntityPlayerMP entityplayermp : trackingPlayers) {
			trackedEntity.removeTrackingPlayer(entityplayermp);
			entityplayermp.removeEntity(trackedEntity);
		}
	}

	public void removeFromTrackedPlayers(EntityPlayerMP playerMP) {

		if (trackingPlayers.contains(playerMP)) {
			trackedEntity.removeTrackingPlayer(playerMP);
			playerMP.removeEntity(trackedEntity);
			trackingPlayers.remove(playerMP);
		}
	}

	public void updatePlayerEntity(EntityPlayerMP playerMP) {

		if (playerMP != trackedEntity) {
			if (isVisibleTo(playerMP)) {
				if (!trackingPlayers.contains(playerMP) && (isPlayerWatchingThisChunk(playerMP) || trackedEntity.forceSpawn)) {
					trackingPlayers.add(playerMP);
					Packet<?> packet = createSpawnPacket();
					playerMP.connection.sendPacket(packet);

					if (!trackedEntity.getDataManager().isEmpty()) {
						playerMP.connection.sendPacket(new SPacketEntityMetadata(trackedEntity.getEntityId(), trackedEntity.getDataManager(), true));
					}

					boolean flag = sendVelocityUpdates;

					if (trackedEntity instanceof EntityLivingBase) {
						AttributeMap attributemap = (AttributeMap) ((EntityLivingBase) trackedEntity).getAttributeMap();
						Collection<IAttributeInstance> collection = attributemap.getWatchedAttributes();

						if (!collection.isEmpty()) {
							playerMP.connection.sendPacket(new SPacketEntityProperties(trackedEntity.getEntityId(), collection));
						}

						if (((EntityLivingBase) trackedEntity).isElytraFlying()) {
							flag = true;
						}
					}

					lastTrackedEntityMotionX = trackedEntity.motionX;
					lastTrackedEntityMotionY = trackedEntity.motionY;
					motionZ = trackedEntity.motionZ;

					if (flag && !(packet instanceof SPacketSpawnMob)) {
						playerMP.connection.sendPacket(new SPacketEntityVelocity(trackedEntity.getEntityId(), trackedEntity.motionX, trackedEntity.motionY, trackedEntity.motionZ));
					}

					if (trackedEntity instanceof EntityLivingBase) {
						for (EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values()) {
							ItemStack itemstack = ((EntityLivingBase) trackedEntity).getItemStackFromSlot(entityequipmentslot);

							if (!itemstack.isEmpty()) {
								playerMP.connection.sendPacket(new SPacketEntityEquipment(trackedEntity.getEntityId(), entityequipmentslot, itemstack));
							}
						}
					}

					if (trackedEntity instanceof EntityPlayer entityplayer) {

						if (entityplayer.isPlayerSleeping()) {
							playerMP.connection.sendPacket(new SPacketUseBed(entityplayer, new BlockPos(trackedEntity)));
						}
					}

					if (trackedEntity instanceof EntityLivingBase entitylivingbase) {

						for (PotionEffect potioneffect : entitylivingbase.getActivePotionEffects()) {
							playerMP.connection.sendPacket(new SPacketEntityEffect(trackedEntity.getEntityId(), potioneffect));
						}
					}

					if (!trackedEntity.getPassengers().isEmpty()) {
						playerMP.connection.sendPacket(new SPacketSetPassengers(trackedEntity));
					}

					if (trackedEntity.isRiding()) {
						playerMP.connection.sendPacket(new SPacketSetPassengers(trackedEntity.getRidingEntity()));
					}

					trackedEntity.addTrackingPlayer(playerMP);
					playerMP.addEntity(trackedEntity);
				}
			} else if (trackingPlayers.contains(playerMP)) {
				trackingPlayers.remove(playerMP);
				trackedEntity.removeTrackingPlayer(playerMP);
				playerMP.removeEntity(trackedEntity);
			}
		}
	}

	public boolean isVisibleTo(EntityPlayerMP playerMP) {

		double d0 = playerMP.posX - (double) encodedPosX / 4096D;
		double d1 = playerMP.posZ - (double) encodedPosZ / 4096D;
		int i = Math.min(range, maxRange);
		return d0 >= (double) (-i) && d0 <= (double) i && d1 >= (double) (-i) && d1 <= (double) i && trackedEntity.isSpectatedByPlayer(playerMP);
	}

	private boolean isPlayerWatchingThisChunk(EntityPlayerMP playerMP) {

		return playerMP.getServerWorld().getPlayerChunkMap().isPlayerWatchingChunk(playerMP, trackedEntity.chunkCoordX, trackedEntity.chunkCoordZ);
	}

	public void updatePlayerEntities(List<EntityPlayer> players) {

		for (EntityPlayer player : players) {
			updatePlayerEntity((EntityPlayerMP) player);
		}
	}

	private Packet<?> createSpawnPacket() {

		if (trackedEntity.isDead) {
			LOGGER.warn("Fetching addPacket for removed entity");
		}

		switch (trackedEntity) {
			case EntityPlayerMP entityPlayerMP -> {
				return new SPacketSpawnPlayer((EntityPlayer) trackedEntity);
			}
			case IAnimals iAnimals -> {
				lastHeadMotion = MathHelper.floor(trackedEntity.getRotationYawHead() * 256F / 360F);
				return new SPacketSpawnMob((EntityLivingBase) trackedEntity);
			}
			case EntityPainting entityPainting -> {
				return new SPacketSpawnPainting(entityPainting);
			}
			case EntityItem entityItem -> {
				return new SPacketSpawnObject(trackedEntity, 2, 1);
			}
			case EntityMinecart entityminecart -> {
				return new SPacketSpawnObject(trackedEntity, 10, entityminecart.getType().getId());
			}
			case EntityBoat entityBoat -> {
				return new SPacketSpawnObject(trackedEntity, 1);
			}
			case EntityXPOrb entityXPOrb -> {
				return new SPacketSpawnExperienceOrb(entityXPOrb);
			}
			case EntityFishHook entityFishHook -> {
				Entity entity2 = entityFishHook.getAngler();
				return new SPacketSpawnObject(trackedEntity, 90, entity2 == null ? trackedEntity.getEntityId() : entity2.getEntityId());
			}
			case EntitySpectralArrow entitySpectralArrow -> {
				Entity entity1 = entitySpectralArrow.shootingEntity;
				return new SPacketSpawnObject(trackedEntity, 91, 1 + (entity1 == null ? trackedEntity.getEntityId() : entity1.getEntityId()));
			}
			case EntityTippedArrow entityTippedArrow -> {
				Entity entity = ((EntityArrow) trackedEntity).shootingEntity;
				return new SPacketSpawnObject(trackedEntity, 60, 1 + (entity == null ? trackedEntity.getEntityId() : entity.getEntityId()));
			}
			case EntitySnowball entitySnowball -> {
				return new SPacketSpawnObject(trackedEntity, 61);
			}
			case EntityLlamaSpit entityLlamaSpit -> {
				return new SPacketSpawnObject(trackedEntity, 68);
			}
			case EntityPotion entityPotion -> {
				return new SPacketSpawnObject(trackedEntity, 73);
			}
			case EntityExpBottle entityExpBottle -> {
				return new SPacketSpawnObject(trackedEntity, 75);
			}
			case EntityEnderPearl entityEnderPearl -> {
				return new SPacketSpawnObject(trackedEntity, 65);
			}
			case EntityEnderEye entityEnderEye -> {
				return new SPacketSpawnObject(trackedEntity, 72);
			}
			case EntityFireworkRocket entityFireworkRocket -> {
				return new SPacketSpawnObject(trackedEntity, 76);
			}
			case EntityFireball entityfireball -> {
				SPacketSpawnObject spacketspawnobject;
				int i = 63;

				switch (trackedEntity) {
					case EntitySmallFireball entitySmallFireball -> i = 64;
					case EntityDragonFireball entityDragonFireball -> i = 93;
					case EntityWitherSkull entityWitherSkull -> i = 66;
					default -> {
					}
				}

				if (entityfireball.shootingEntity != null) {
					spacketspawnobject = new SPacketSpawnObject(trackedEntity, i, entityfireball.shootingEntity.getEntityId());
				} else {
					spacketspawnobject = new SPacketSpawnObject(trackedEntity, i, 0);
				}

				spacketspawnobject.setSpeedX((int) (entityfireball.accelerationX * 8000D));
				spacketspawnobject.setSpeedY((int) (entityfireball.accelerationY * 8000D));
				spacketspawnobject.setSpeedZ((int) (entityfireball.accelerationZ * 8000D));
				return spacketspawnobject;
			}
			case EntityShulkerBullet entityShulkerBullet -> {
				SPacketSpawnObject spacketspawnobject1 = new SPacketSpawnObject(trackedEntity, 67, 0);
				spacketspawnobject1.setSpeedX((int) (trackedEntity.motionX * 8000D));
				spacketspawnobject1.setSpeedY((int) (trackedEntity.motionY * 8000D));
				spacketspawnobject1.setSpeedZ((int) (trackedEntity.motionZ * 8000D));
				return spacketspawnobject1;
			}
			case EntityEgg entityEgg -> {
				return new SPacketSpawnObject(trackedEntity, 62);
			}
			case EntityEvokerFangs entityEvokerFangs -> {
				return new SPacketSpawnObject(trackedEntity, 79);
			}
			case EntityTNTPrimed entityTNTPrimed -> {
				return new SPacketSpawnObject(trackedEntity, 50);
			}
			case EntityEnderCrystal entityEnderCrystal -> {
				return new SPacketSpawnObject(trackedEntity, 51);
			}
			case EntityFallingBlock entityfallingblock -> {
				return new SPacketSpawnObject(trackedEntity, 70, Block.getStateId(entityfallingblock.getBlock()));
			}
			case EntityArmorStand entityArmorStand -> {
				return new SPacketSpawnObject(trackedEntity, 78);
			}
			case EntityItemFrame entityitemframe -> {
				return new SPacketSpawnObject(trackedEntity, 71, entityitemframe.facingDirection.getHorizontalIndex(), entityitemframe.getHangingPosition());
			}
			case EntityLeashKnot entityleashknot -> {
				return new SPacketSpawnObject(trackedEntity, 77, 0, entityleashknot.getHangingPosition());
			}
			case EntityAreaEffectCloud entityAreaEffectCloud -> {
				return new SPacketSpawnObject(trackedEntity, 3);
			}
			default -> throw new IllegalArgumentException("Don't know how to add " + trackedEntity.getClass() + "!");
		}
	}

	/**
	 * Remove a tracked player from our list and tell the tracked player to destroy us from their world.
	 */
	public void removeTrackedPlayerSymmetric(EntityPlayerMP playerMP) {

		if (trackingPlayers.contains(playerMP)) {
			trackingPlayers.remove(playerMP);
			trackedEntity.removeTrackingPlayer(playerMP);
			playerMP.removeEntity(trackedEntity);
		}
	}

	public Entity getTrackedEntity() {

		return trackedEntity;
	}

	public void setMaxRange(int maxRangeIn) {

		maxRange = maxRangeIn;
	}

	public void resetPlayerVisibility() {

		updatedPlayerVisibility = false;
	}

}
