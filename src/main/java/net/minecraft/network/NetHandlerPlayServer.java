package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.util.concurrent.Futures;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecartCommandBlock;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.*;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class NetHandlerPlayServer implements INetHandlerPlayServer, ITickable {

	private static final Logger LOGGER = LogManager.getLogger();
	public final NetworkManager netManager;
	private final MinecraftServer serverController;
	private final IntHashMap<Short> pendingTransactions = new IntHashMap<>();
	private final ServerRecipeBookHelper field_194309_H = new ServerRecipeBookHelper();
	public EntityPlayerMP player;
	private int networkTickCount;
	private long field_194402_f;
	private boolean field_194403_g;
	private long field_194404_h;
	/**
	 * Incremented by 20 each time a user sends a chat message, decreased by one every tick. Non-ops kicked when over
	 * 200
	 */
	private int chatSpamThresholdCount;
	private int itemDropThreshold;
	private double firstGoodX;
	private double firstGoodY;
	private double firstGoodZ;
	private double lastGoodX;
	private double lastGoodY;
	private double lastGoodZ;
	private Entity lowestRiddenEnt;
	private double lowestRiddenX;
	private double lowestRiddenY;
	private double lowestRiddenZ;
	private double lowestRiddenX1;
	private double lowestRiddenY1;
	private double lowestRiddenZ1;
	private Vec3d targetPos;
	private int teleportId;
	private int lastPositionUpdate;
	private boolean floating;
	/**
	 * Used to keep track of how the player is floating while gamerules should prevent that. Surpassing 80 ticks means
	 * kick
	 */
	private int floatingTickCount;
	private boolean vehicleFloating;
	/**
	 * Used to keep track of how long the player is floating in a vehicle. Surpassing 80 means a kick
	 */
	private int vehicleFloatingTickCount;
	private int movePacketCounter;
	private int lastMovePacketCounter;

	public NetHandlerPlayServer(MinecraftServer server, NetworkManager networkManagerIn, EntityPlayerMP playerIn) {

		serverController = server;
		netManager = networkManagerIn;
		networkManagerIn.setNetHandler(this);
		player = playerIn;
		playerIn.connection = this;
	}

	private static boolean isMovePlayerPacketInvalid(CPacketPlayer packetIn) {

		if (Doubles.isFinite(packetIn.getX(0D)) && Doubles.isFinite(packetIn.getY(0D)) && Doubles.isFinite(packetIn.getZ(0D)) && Floats.isFinite(packetIn.getPitch(0F)) && Floats.isFinite(packetIn.getYaw(0F))) {
			return Math.abs(packetIn.getX(0D)) > 3.0E7D || Math.abs(packetIn.getY(0D)) > 3.0E7D || Math.abs(packetIn.getZ(0D)) > 3.0E7D;
		} else {
			return true;
		}
	}

	private static boolean isMoveVehiclePacketInvalid(CPacketVehicleMove packetIn) {

		return !Doubles.isFinite(packetIn.getX()) || !Doubles.isFinite(packetIn.getY()) || !Doubles.isFinite(packetIn.getZ()) || !Floats.isFinite(packetIn.getPitch()) || !Floats.isFinite(packetIn.getYaw());
	}

	/**
	 * Like the old updateEntity(), except more generic.
	 */
	public void update() {

		captureCurrentPosition();
		player.onUpdateEntity();
		player.setPositionAndRotation(firstGoodX, firstGoodY, firstGoodZ, player.rotationYaw, player.rotationPitch);
		++networkTickCount;
		lastMovePacketCounter = movePacketCounter;

		if (floating) {
			if (++floatingTickCount > 80) {
				LOGGER.warn("{} was kicked for floating too long!", player.getName());
				disconnect(new TextComponentTranslation("multiplayer.disconnect.flying"));
				return;
			}
		} else {
			floating = false;
			floatingTickCount = 0;
		}

		lowestRiddenEnt = player.getLowestRidingEntity();

		if (lowestRiddenEnt != player && lowestRiddenEnt.getControllingPassenger() == player) {
			lowestRiddenX = lowestRiddenEnt.posX;
			lowestRiddenY = lowestRiddenEnt.posY;
			lowestRiddenZ = lowestRiddenEnt.posZ;
			lowestRiddenX1 = lowestRiddenEnt.posX;
			lowestRiddenY1 = lowestRiddenEnt.posY;
			lowestRiddenZ1 = lowestRiddenEnt.posZ;

			if (vehicleFloating && player.getLowestRidingEntity().getControllingPassenger() == player) {
				if (++vehicleFloatingTickCount > 80) {
					LOGGER.warn("{} was kicked for floating a vehicle too long!", player.getName());
					disconnect(new TextComponentTranslation("multiplayer.disconnect.flying"));
					return;
				}
			} else {
				vehicleFloating = false;
				vehicleFloatingTickCount = 0;
			}
		} else {
			lowestRiddenEnt = null;
			vehicleFloating = false;
			vehicleFloatingTickCount = 0;
		}

		serverController.profiler.startSection("keepAlive");
		long i = currentTimeMillis();

		if (i - field_194402_f >= 15000L) {
			if (field_194403_g) {
				disconnect(new TextComponentTranslation("disconnect.timeout"));
			} else {
				field_194403_g = true;
				field_194402_f = i;
				field_194404_h = i;
				sendPacket(new SPacketKeepAlive(field_194404_h));
			}
		}

		serverController.profiler.endSection();

		if (chatSpamThresholdCount > 0) {
			--chatSpamThresholdCount;
		}

		if (itemDropThreshold > 0) {
			--itemDropThreshold;
		}

		if (player.getLastActiveTime() > 0L && serverController.getMaxPlayerIdleMinutes() > 0 && MinecraftServer.getCurrentTimeMillis() - player.getLastActiveTime() > ((long) serverController.getMaxPlayerIdleMinutes() * 1000 * 60)) {
			disconnect(new TextComponentTranslation("multiplayer.disconnect.idling"));
		}
	}

	private void captureCurrentPosition() {

		firstGoodX = player.posX;
		firstGoodY = player.posY;
		firstGoodZ = player.posZ;
		lastGoodX = player.posX;
		lastGoodY = player.posY;
		lastGoodZ = player.posZ;
	}

	public NetworkManager getNetworkManager() {

		return netManager;
	}

	/**
	 * Disconnect the player with a specified reason
	 */
	public void disconnect(final ITextComponent textComponent) {

		netManager.sendPacket(new SPacketDisconnect(textComponent), p_operationComplete_1_ -> netManager.closeChannel(textComponent));
		netManager.disableAutoRead();
		Futures.getUnchecked(serverController.addScheduledTask(netManager::checkDisconnected));
	}

	/**
	 * Processes player movement input. Includes walking, strafing, jumping, sneaking; excludes riding and toggling
	 * flying/sprinting
	 */
	public void processInput(CPacketInput packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
		player.setEntityActionState(packetIn.getStrafeSpeed(), packetIn.getForwardSpeed(), packetIn.isJumping(), packetIn.isSneaking());
	}

	public void processVehicleMove(CPacketVehicleMove packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());

		if (isMoveVehiclePacketInvalid(packetIn)) {
			disconnect(new TextComponentTranslation("multiplayer.disconnect.invalid_vehicle_movement"));
		} else {
			Entity entity = player.getLowestRidingEntity();

			if (entity != player && entity.getControllingPassenger() == player && entity == lowestRiddenEnt) {
				WorldServer worldserver = player.getServerWorld();
				double d0 = entity.posX;
				double d1 = entity.posY;
				double d2 = entity.posZ;
				double d3 = packetIn.getX();
				double d4 = packetIn.getY();
				double d5 = packetIn.getZ();
				float f = packetIn.getYaw();
				float f1 = packetIn.getPitch();
				double d6 = d3 - lowestRiddenX;
				double d7 = d4 - lowestRiddenY;
				double d8 = d5 - lowestRiddenZ;
				double d9 = entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ;
				double d10 = d6 * d6 + d7 * d7 + d8 * d8;

				if (d10 - d9 > 100D && (!serverController.isSinglePlayer() || !serverController.getServerOwner().equals(entity.getName()))) {
					LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", entity.getName(), player.getName(), d6, d7, d8);
					netManager.sendPacket(new SPacketMoveVehicle(entity));
					return;
				}

				boolean flag = worldserver.getCollisionBoxes(entity, entity.getEntityBoundingBox().shrink(0.0625D)).isEmpty();
				d6 = d3 - lowestRiddenX1;
				d7 = d4 - lowestRiddenY1 - 1.0E-6D;
				d8 = d5 - lowestRiddenZ1;
				entity.move(MoverType.PLAYER, d6, d7, d8);
				double d11 = d7;
				d6 = d3 - entity.posX;
				d7 = d4 - entity.posY;

				if (d7 > -0.5D || d7 < 0.5D) {
					d7 = 0D;
				}

				d8 = d5 - entity.posZ;
				d10 = d6 * d6 + d7 * d7 + d8 * d8;
				boolean flag1 = false;

				if (d10 > 0.0625D) {
					flag1 = true;
					LOGGER.warn("{} moved wrongly!", entity.getName());
				}

				entity.setPositionAndRotation(d3, d4, d5, f, f1);
				boolean flag2 = worldserver.getCollisionBoxes(entity, entity.getEntityBoundingBox().shrink(0.0625D)).isEmpty();

				if (flag && (flag1 || !flag2)) {
					entity.setPositionAndRotation(d0, d1, d2, f, f1);
					netManager.sendPacket(new SPacketMoveVehicle(entity));
					return;
				}

				serverController.getPlayerList().serverUpdateMovingPlayer(player);
				player.addMovementStat(player.posX - d0, player.posY - d1, player.posZ - d2);
				vehicleFloating = d11 >= -0.03125D && !serverController.isFlightAllowed() && !worldserver.checkBlockCollision(entity.getEntityBoundingBox().grow(0.0625D).expand(0D, -0.55D, 0D));
				lowestRiddenX1 = entity.posX;
				lowestRiddenY1 = entity.posY;
				lowestRiddenZ1 = entity.posZ;
			}
		}
	}

	public void processConfirmTeleport(CPacketConfirmTeleport packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());

		if (packetIn.getTeleportId() == teleportId) {
			player.setPositionAndRotation(targetPos.x(), targetPos.y(), targetPos.z(), player.rotationYaw, player.rotationPitch);

			if (player.isInvulnerableDimensionChange()) {
				lastGoodX = targetPos.x();
				lastGoodY = targetPos.y();
				lastGoodZ = targetPos.z();
				player.clearInvulnerableDimensionChange();
			}

			targetPos = null;
		}
	}

	public void handleRecipeBookUpdate(CPacketRecipeInfo p_191984_1_) {

		PacketThreadUtil.checkThreadAndEnqueue(p_191984_1_, this, player.getServerWorld());

		if (p_191984_1_.getPurpose() == CPacketRecipeInfo.Purpose.SHOWN) {
			player.getRecipeBook().markSeen(p_191984_1_.getRecipe());
		} else if (p_191984_1_.getPurpose() == CPacketRecipeInfo.Purpose.SETTINGS) {
			player.getRecipeBook().setGuiOpen(p_191984_1_.isGuiOpen());
			player.getRecipeBook().setFilteringCraftable(p_191984_1_.isFilteringCraftable());
		}
	}

	public void handleSeenAdvancements(CPacketSeenAdvancements p_194027_1_) {

		PacketThreadUtil.checkThreadAndEnqueue(p_194027_1_, this, player.getServerWorld());

		if (p_194027_1_.getAction() == CPacketSeenAdvancements.Action.OPENED_TAB) {
			ResourceLocation resourcelocation = p_194027_1_.getTab();
			Advancement advancement = serverController.getAdvancementManager().getAdvancement(resourcelocation);

			if (advancement != null) {
				player.getAdvancements().setSelectedTab(advancement);
			}
		}
	}

	/**
	 * Processes clients perspective on player positioning and/or orientation
	 */
	public void processPlayer(CPacketPlayer packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());

		if (isMovePlayerPacketInvalid(packetIn)) {
			disconnect(new TextComponentTranslation("multiplayer.disconnect.invalid_player_movement"));
		} else {
			WorldServer worldserver = serverController.getWorld(player.dimension);

			if (!player.queuedEndExit) {
				if (networkTickCount == 0) {
					captureCurrentPosition();
				}

				if (targetPos != null) {
					if (networkTickCount - lastPositionUpdate > 20) {
						lastPositionUpdate = networkTickCount;
						setPlayerLocation(targetPos.x(), targetPos.y(), targetPos.z(), player.rotationYaw, player.rotationPitch);
					}
				} else {
					lastPositionUpdate = networkTickCount;

					if (player.isRiding()) {
						player.setPositionAndRotation(player.posX, player.posY, player.posZ, packetIn.getYaw(player.rotationYaw), packetIn.getPitch(player.rotationPitch));
						serverController.getPlayerList().serverUpdateMovingPlayer(player);
					} else {
						double d0 = player.posX;
						double d1 = player.posY;
						double d2 = player.posZ;
						double d3 = player.posY;
						double d4 = packetIn.getX(player.posX);
						double d5 = packetIn.getY(player.posY);
						double d6 = packetIn.getZ(player.posZ);
						float f = packetIn.getYaw(player.rotationYaw);
						float f1 = packetIn.getPitch(player.rotationPitch);
						double d7 = d4 - firstGoodX;
						double d8 = d5 - firstGoodY;
						double d9 = d6 - firstGoodZ;
						double d10 = player.motionX * player.motionX + player.motionY * player.motionY + player.motionZ * player.motionZ;
						double d11 = d7 * d7 + d8 * d8 + d9 * d9;

						if (player.isPlayerSleeping()) {
							if (d11 > 1D) {
								setPlayerLocation(player.posX, player.posY, player.posZ, packetIn.getYaw(player.rotationYaw), packetIn.getPitch(player.rotationPitch));
							}
						} else {
							++movePacketCounter;
							int i = movePacketCounter - lastMovePacketCounter;

							if (i > 5) {
								LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", player.getName(), i);
								i = 1;
							}

							if (!player.isInvulnerableDimensionChange() && (!player.getServerWorld().getGameRules().getBoolean("disableElytraMovementCheck") || !player.isElytraFlying())) {
								float f2 = player.isElytraFlying() ? 300F : 100F;

								if (d11 - d10 > (double) (f2 * (float) i) && (!serverController.isSinglePlayer() || !serverController.getServerOwner().equals(player.getName()))) {
									LOGGER.warn("{} moved too quickly! {},{},{}", player.getName(), d7, d8, d9);
									setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
									return;
								}
							}

							boolean flag2 = worldserver.getCollisionBoxes(player, player.getEntityBoundingBox().shrink(0.0625D)).isEmpty();
							d7 = d4 - lastGoodX;
							d8 = d5 - lastGoodY;
							d9 = d6 - lastGoodZ;

							if (player.onGround && !packetIn.isOnGround() && d8 > 0D) {
								player.jump();
							}

							player.move(MoverType.PLAYER, d7, d8, d9);
							player.onGround = packetIn.isOnGround();
							double d12 = d8;
							d7 = d4 - player.posX;
							d8 = d5 - player.posY;

							if (d8 > -0.5D || d8 < 0.5D) {
								d8 = 0D;
							}

							d9 = d6 - player.posZ;
							d11 = d7 * d7 + d8 * d8 + d9 * d9;
							boolean flag = false;

							if (!player.isInvulnerableDimensionChange() && d11 > 0.0625D && !player.isPlayerSleeping() && !player.interactionManager.isCreative() && player.interactionManager.getGameType() != GameType.SPECTATOR) {
								flag = true;
								LOGGER.warn("{} moved wrongly!", player.getName());
							}

							player.setPositionAndRotation(d4, d5, d6, f, f1);
							player.addMovementStat(player.posX - d0, player.posY - d1, player.posZ - d2);

							if (!player.noClip && !player.isPlayerSleeping()) {
								boolean flag1 = worldserver.getCollisionBoxes(player, player.getEntityBoundingBox().shrink(0.0625D)).isEmpty();

								if (flag2 && (flag || !flag1)) {
									setPlayerLocation(d0, d1, d2, f, f1);
									return;
								}
							}

							floating = d12 >= -0.03125D;
							floating &= !serverController.isFlightAllowed() && !player.capabilities.allowFlying;
							floating &= !player.isPotionActive(MobEffects.LEVITATION) && !player.isElytraFlying() && !worldserver.checkBlockCollision(player.getEntityBoundingBox().grow(0.0625D).expand(0D, -0.55D, 0D));
							player.onGround = packetIn.isOnGround();
							serverController.getPlayerList().serverUpdateMovingPlayer(player);
							player.handleFalling(player.posY - d3, packetIn.isOnGround());
							lastGoodX = player.posX;
							lastGoodY = player.posY;
							lastGoodZ = player.posZ;
						}
					}
				}
			}
		}
	}

	public void setPlayerLocation(double x, double y, double z, float yaw, float pitch) {

		setPlayerLocation(x, y, z, yaw, pitch, Collections.emptySet());
	}

	/**
	 * Teleports the player position to the (relative) values specified, and syncs to the client
	 */
	public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, Set<SPacketPlayerPosLook.Flags> relativeSet) {

		double d0 = relativeSet.contains(SPacketPlayerPosLook.Flags.X) ? player.posX : 0D;
		double d1 = relativeSet.contains(SPacketPlayerPosLook.Flags.Y) ? player.posY : 0D;
		double d2 = relativeSet.contains(SPacketPlayerPosLook.Flags.Z) ? player.posZ : 0D;
		targetPos = new Vec3d(x + d0, y + d1, z + d2);
		float f = yaw;
		float f1 = pitch;

		if (relativeSet.contains(SPacketPlayerPosLook.Flags.Y_ROT)) {
			f = yaw + player.rotationYaw;
		}

		if (relativeSet.contains(SPacketPlayerPosLook.Flags.X_ROT)) {
			f1 = pitch + player.rotationPitch;
		}

		if (++teleportId == Integer.MAX_VALUE) {
			teleportId = 0;
		}

		lastPositionUpdate = networkTickCount;
		player.setPositionAndRotation(targetPos.x(), targetPos.y(), targetPos.z(), f, f1);
		player.connection.sendPacket(new SPacketPlayerPosLook(x, y, z, yaw, pitch, relativeSet, teleportId));
	}

	/**
	 * Processes the player initiating/stopping digging on a particular spot, as well as a player dropping items
	 */
	public void processPlayerDigging(CPacketPlayerDigging packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
		WorldServer worldserver = serverController.getWorld(player.dimension);
		BlockPos blockpos = packetIn.getPosition();
		player.markPlayerActive();

		switch (packetIn.getAction()) {
			case SWAP_HELD_ITEMS:
				if (!player.isSpectator()) {
					ItemStack itemstack = player.getHeldItem(Hand.OFF_HAND);
					player.setHeldItem(Hand.OFF_HAND, player.getHeldItem(Hand.MAIN_HAND));
					player.setHeldItem(Hand.MAIN_HAND, itemstack);
				}

				return;

			case DROP_ITEM:
				if (!player.isSpectator()) {
					player.dropItem(false);
				}

				return;

			case DROP_ALL_ITEMS:
				if (!player.isSpectator()) {
					player.dropItem(true);
				}

				return;

			case RELEASE_USE_ITEM:
				player.stopActiveHand();
				return;

			case START_DESTROY_BLOCK:
			case ABORT_DESTROY_BLOCK:
			case STOP_DESTROY_BLOCK:
				double d0 = player.posX - ((double) blockpos.getX() + 0.5D);
				double d1 = player.posY - ((double) blockpos.getY() + 0.5D) + 1.5D;
				double d2 = player.posZ - ((double) blockpos.getZ() + 0.5D);
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;

				if (d3 > 36D) {
					return;
				} else if (blockpos.getY() >= serverController.getBuildLimit()) {
					return;
				} else {
					if (packetIn.getAction() == CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
						if (!serverController.isBlockProtected(worldserver, blockpos, player) && worldserver.getWorldBorder().contains(blockpos)) {
							player.interactionManager.onBlockClicked(blockpos, packetIn.getFacing());
						} else {
							player.connection.sendPacket(new SPacketBlockChange(worldserver, blockpos));
						}
					} else {
						if (packetIn.getAction() == CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
							player.interactionManager.blockRemoving(blockpos);
						} else if (packetIn.getAction() == CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK) {
							player.interactionManager.cancelDestroyingBlock();
						}

						if (worldserver.getBlockState(blockpos).getMaterial() != Material.AIR) {
							player.connection.sendPacket(new SPacketBlockChange(worldserver, blockpos));
						}
					}

					return;
				}

			default:
				throw new IllegalArgumentException("Invalid player action");
		}
	}

	public void processTryUseItemOnBlock(CPacketPlayerTryUseItemOnBlock packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
		WorldServer worldserver = serverController.getWorld(player.dimension);
		Hand enumhand = packetIn.getHand();
		ItemStack itemstack = player.getHeldItem(enumhand);
		BlockPos blockpos = packetIn.getPos();
		Facing enumfacing = packetIn.getDirection();
		player.markPlayerActive();

		if (blockpos.getY() < serverController.getBuildLimit() - 1 || enumfacing != Facing.UP && blockpos.getY() < serverController.getBuildLimit()) {
			if (targetPos == null && player.getDistanceSq((double) blockpos.getX() + 0.5D, (double) blockpos.getY() + 0.5D, (double) blockpos.getZ() + 0.5D) < 64D && !serverController.isBlockProtected(worldserver, blockpos, player) && worldserver.getWorldBorder().contains(blockpos)) {
				player.interactionManager.processRightClickBlock(player, worldserver, itemstack, enumhand, blockpos, enumfacing, packetIn.getFacingX(), packetIn.getFacingY(), packetIn.getFacingZ());
			}
		} else {
			TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("build.tooHigh", serverController.getBuildLimit());
			textcomponenttranslation.getStyle().setColor(TextFormatting.RED);
			player.connection.sendPacket(new SPacketChat(textcomponenttranslation, ChatType.GAME_INFO));
		}

		player.connection.sendPacket(new SPacketBlockChange(worldserver, blockpos));
		player.connection.sendPacket(new SPacketBlockChange(worldserver, blockpos.offset(enumfacing)));
	}

	/**
	 * Called when a client is using an item while not pointing at a block, but simply using an item
	 */
	public void processTryUseItem(CPacketPlayerTryUseItem packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
		WorldServer worldserver = serverController.getWorld(player.dimension);
		Hand enumhand = packetIn.getHand();
		ItemStack itemstack = player.getHeldItem(enumhand);
		player.markPlayerActive();

		if (!itemstack.isEmpty()) {
			player.interactionManager.processRightClick(player, worldserver, itemstack, enumhand);
		}
	}

	public void handleSpectate(CPacketSpectate packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());

		if (player.isSpectator()) {
			Entity entity = null;

			for (WorldServer worldserver : serverController.worlds) {
				if (worldserver != null) {
					entity = packetIn.getEntity(worldserver);

					if (entity != null) {
						break;
					}
				}
			}

			if (entity != null) {
				player.setSpectatingEntity(player);
				player.dismountRidingEntity();

				if (entity.world == player.world) {
					player.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
				} else {
					WorldServer worldserver1 = player.getServerWorld();
					WorldServer worldserver2 = (WorldServer) entity.world;
					player.dimension = entity.dimension;
					sendPacket(new SPacketRespawn(player.dimension, worldserver1.getDifficulty(), worldserver1.getWorldInfo().getTerrainType(), player.interactionManager.getGameType()));
					serverController.getPlayerList().updatePermissionLevel(player);
					worldserver1.removeEntityDangerously(player);
					player.isDead = false;
					player.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);

					if (player.isEntityAlive()) {
						worldserver1.updateEntityWithOptionalForce(player, false);
						worldserver2.spawnEntity(player);
						worldserver2.updateEntityWithOptionalForce(player, false);
					}

					player.setWorld(worldserver2);
					serverController.getPlayerList().preparePlayer(player, worldserver1);
					player.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
					player.interactionManager.setWorld(worldserver2);
					serverController.getPlayerList().updateTimeAndWeatherForPlayer(player, worldserver2);
					serverController.getPlayerList().syncPlayerInventory(player);
				}
			}
		}
	}

	public void handleResourcePackStatus(CPacketResourcePackStatus packetIn) {

	}

	public void processSteerBoat(CPacketSteerBoat packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
		Entity entity = player.getRidingEntity();

		if (entity instanceof EntityBoat) {
			((EntityBoat) entity).setPaddleState(packetIn.getLeft(), packetIn.getRight());
		}
	}

	/**
	 * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
	 */
	public void onDisconnect(ITextComponent reason) {

		LOGGER.info("{} lost connection: {}", player.getName(), reason.getUnformattedText());
		serverController.refreshStatusNextTick();
		TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("multiplayer.player.left", player.getDisplayName());
		textcomponenttranslation.getStyle().setColor(TextFormatting.YELLOW);
		serverController.getPlayerList().sendMessage(textcomponenttranslation);
		player.mountEntityAndWakeUp();
		serverController.getPlayerList().playerLoggedOut(player);

		if (serverController.isSinglePlayer() && player.getName().equals(serverController.getServerOwner())) {
			LOGGER.info("Stopping singleplayer server as player logged out");
			serverController.initiateShutdown();
		}
	}

	public void sendPacket(final Packet<?> packetIn) {

		if (packetIn instanceof SPacketChat spacketchat) {
			EntityPlayer.ChatVisibility entityplayer$enumchatvisibility = player.getChatVisibility();

			if (entityplayer$enumchatvisibility == EntityPlayer.ChatVisibility.HIDDEN && spacketchat.getType() != ChatType.GAME_INFO) {
				return;
			}

			if (entityplayer$enumchatvisibility == EntityPlayer.ChatVisibility.SYSTEM && !spacketchat.isSystem()) {
				return;
			}
		}

		try {
			netManager.sendPacket(packetIn);
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Sending packet");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Packet being sent");
			crashreportcategory.addDetail("Packet class", () -> packetIn.getClass().getCanonicalName());
			throw new ReportedException(crashreport);
		}
	}

	/**
	 * Updates which quickbar slot is selected
	 */
	public void processHeldItemChange(CPacketHeldItemChange packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());

		if (packetIn.getSlotId() >= 0 && packetIn.getSlotId() < InventoryPlayer.getHotbarSize()) {
			player.inventory.currentItem = packetIn.getSlotId();
			player.markPlayerActive();
		} else {
			LOGGER.warn("{} tried to set an invalid carried item", player.getName());
		}
	}

	/**
	 * Process chat messages (broadcast back to clients) and commands (executes)
	 */
	public void processChatMessage(CPacketChatMessage packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());

		if (player.getChatVisibility() == EntityPlayer.ChatVisibility.HIDDEN) {
			TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("chat.cannotSend");
			textcomponenttranslation.getStyle().setColor(TextFormatting.RED);
			sendPacket(new SPacketChat(textcomponenttranslation));
		} else {
			player.markPlayerActive();
			String s = packetIn.getMessage();
			s = StringUtils.normalizeSpace(s);

			for (int i = 0; i < s.length(); ++i) {
				if (!ChatAllowedCharacters.isAllowedCharacter(s.charAt(i))) {
					disconnect(new TextComponentTranslation("multiplayer.disconnect.illegal_characters"));
					return;
				}
			}

			if (s.startsWith("/")) {
				handleSlashCommand(s);
			} else {
				ITextComponent itextcomponent = new TextComponentTranslation("chat.type.text", player.getDisplayName(), s);
				serverController.getPlayerList().sendMessage(itextcomponent, false);
			}

			chatSpamThresholdCount += 20;

			if (chatSpamThresholdCount > 200 && !serverController.getPlayerList().canSendCommands(player.getGameProfile())) {
				disconnect(new TextComponentTranslation("disconnect.spam"));
			}
		}
	}

	/**
	 * Handle commands that start with a /
	 */
	private void handleSlashCommand(String command) {

		serverController.getCommandManager().executeCommand(player, command);
	}

	public void handleAnimation(CPacketAnimation packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
		player.markPlayerActive();
		player.swingArm(packetIn.getHand());
	}

	/**
	 * Processes a range of action-types: sneaking, sprinting, waking from sleep, opening the inventory or setting jump
	 * height of the horse the player is riding
	 */
	public void processEntityAction(CPacketEntityAction packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
		player.markPlayerActive();

		switch (packetIn.getAction()) {
			case START_SNEAKING:
				player.setSneaking(true);
				break;

			case STOP_SNEAKING:
				player.setSneaking(false);
				break;

			case START_SPRINTING:
				player.setSprinting(true);
				break;

			case STOP_SPRINTING:
				player.setSprinting(false);
				break;

			case STOP_SLEEPING:
				if (player.isPlayerSleeping()) {
					player.wakeUpPlayer(false, true, true);
					targetPos = new Vec3d(player.posX, player.posY, player.posZ);
				}

				break;

			case START_RIDING_JUMP:
				if (player.getRidingEntity() instanceof IJumpingMount ijumpingmount1) {
					int i = packetIn.getAuxData();

					if (ijumpingmount1.canJump() && i > 0) {
						ijumpingmount1.handleStartJump(i);
					}
				}

				break;

			case STOP_RIDING_JUMP:
				if (player.getRidingEntity() instanceof IJumpingMount ijumpingmount) {
					ijumpingmount.handleStopJump();
				}

				break;

			case OPEN_INVENTORY:
				if (player.getRidingEntity() instanceof AbstractHorse) {
					((AbstractHorse) player.getRidingEntity()).openGUI(player);
				}

				break;

			case START_FALL_FLYING:
				if (!player.onGround && player.motionY < 0D && !player.isElytraFlying() && !player.isInWater()) {
					ItemStack itemstack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

					if (itemstack.getItem() == Items.ELYTRA && ItemElytra.isUsable(itemstack)) {
						player.setElytraFlying();
					}
				} else {
					player.clearElytraFlying();
				}

				break;

			default:
				throw new IllegalArgumentException("Invalid client command!");
		}
	}

	/**
	 * Processes left and right clicks on entities
	 */
	public void processUseEntity(CPacketUseEntity packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
		WorldServer worldserver = serverController.getWorld(player.dimension);
		Entity entity = packetIn.getEntityFromWorld(worldserver);
		player.markPlayerActive();

		if (entity != null) {
			boolean flag = player.canEntityBeSeen(entity);
			double d0 = 36D;

			if (!flag) {
				d0 = 9D;
			}

			if (player.getDistanceSq(entity) < d0) {
				if (packetIn.getAction() == CPacketUseEntity.Action.INTERACT) {
					Hand enumhand = packetIn.getHand();
					player.interactOn(entity, enumhand);
				} else if (packetIn.getAction() == CPacketUseEntity.Action.INTERACT_AT) {
					Hand enumhand1 = packetIn.getHand();
					entity.applyPlayerInteraction(player, packetIn.getHitVec(), enumhand1);
				} else if (packetIn.getAction() == CPacketUseEntity.Action.ATTACK) {
					if (entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityArrow || entity == player) {
						disconnect(new TextComponentTranslation("multiplayer.disconnect.invalid_entity_attacked"));
						serverController.logWarning("Player " + player.getName() + " tried to attack an invalid entity");
						return;
					}

					player.attackTargetEntityWithCurrentItem(entity);
				}
			}
		}
	}

	/**
	 * Processes the client status updates: respawn attempt from player, opening statistics or achievements, or
	 * acquiring 'open inventory' achievement
	 */
	public void processClientStatus(CPacketClientStatus packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
		player.markPlayerActive();
		CPacketClientStatus.State cpacketclientstatus$state = packetIn.getStatus();

		switch (cpacketclientstatus$state) {
			case PERFORM_RESPAWN:
				if (player.queuedEndExit) {
					player.queuedEndExit = false;
					player = serverController.getPlayerList().recreatePlayerEntity(player, 0, true);
					CriteriaTriggers.CHANGED_DIMENSION.trigger(player, DimensionType.THE_END, DimensionType.OVERWORLD);
				} else {
					if (player.getHealth() > 0F) {
						return;
					}

					player = serverController.getPlayerList().recreatePlayerEntity(player, 0, false);

					if (serverController.isHardcore()) {
						player.setGameType(GameType.SPECTATOR);
						player.getServerWorld().getGameRules().setOrCreateGameRule("spectatorsGenerateChunks", "false");
					}
				}

				break;

			case REQUEST_STATS:
				player.getStatFile().sendStats(player);
		}
	}

	/**
	 * Processes the client closing windows (container)
	 */
	public void processCloseWindow(CPacketCloseWindow packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
		player.closeContainer();
	}

	/**
	 * Executes a container/inventory slot manipulation as indicated by the packet. Sends the serverside result if they
	 * didn't match the indicated result and prevents further manipulation by the player until he confirms that it has
	 * the same open container/inventory
	 */
	public void processClickWindow(CPacketClickWindow packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
		player.markPlayerActive();

		if (player.openContainer.windowId == packetIn.getWindowId() && player.openContainer.getCanCraft(player)) {
			if (player.isSpectator()) {
				NonNullList<ItemStack> nonnulllist = NonNullList.create();

				for (int i = 0; i < player.openContainer.inventorySlots.size(); ++i) {
					nonnulllist.add(player.openContainer.inventorySlots.get(i).getStack());
				}

				player.sendAllContents(player.openContainer, nonnulllist);
			} else {
				ItemStack itemstack2 = player.openContainer.slotClick(packetIn.getSlotId(), packetIn.getUsedButton(), packetIn.getClickType(), player);

				if (ItemStack.areItemStacksEqual(packetIn.getClickedItem(), itemstack2)) {
					player.connection.sendPacket(new SPacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), true));
					player.isChangingQuantityOnly = true;
					player.openContainer.detectAndSendChanges();
					player.updateHeldItem();
					player.isChangingQuantityOnly = false;
				} else {
					pendingTransactions.addKey(player.openContainer.windowId, packetIn.getActionNumber());
					player.connection.sendPacket(new SPacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), false));
					player.openContainer.setCanCraft(player, false);
					NonNullList<ItemStack> nonnulllist1 = NonNullList.create();

					for (int j = 0; j < player.openContainer.inventorySlots.size(); ++j) {
						ItemStack itemstack = player.openContainer.inventorySlots.get(j).getStack();
						ItemStack itemstack1 = itemstack.isEmpty() ? ItemStack.EMPTY : itemstack;
						nonnulllist1.add(itemstack1);
					}

					player.sendAllContents(player.openContainer, nonnulllist1);
				}
			}
		}
	}

	public void func_194308_a(CPacketPlaceRecipe p_194308_1_) {

		PacketThreadUtil.checkThreadAndEnqueue(p_194308_1_, this, player.getServerWorld());
		player.markPlayerActive();

		if (!player.isSpectator() && player.openContainer.windowId == p_194308_1_.func_194318_a() && player.openContainer.getCanCraft(player)) {
			field_194309_H.func_194327_a(player, p_194308_1_.func_194317_b(), p_194308_1_.func_194319_c());
		}
	}

	/**
	 * Enchants the item identified by the packet given some convoluted conditions (matching window, which
	 * should/shouldn't be in use?)
	 */
	public void processEnchantItem(CPacketEnchantItem packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
		player.markPlayerActive();

		if (player.openContainer.windowId == packetIn.getWindowId() && player.openContainer.getCanCraft(player) && !player.isSpectator()) {
			player.openContainer.enchantItem(player, packetIn.getButton());
			player.openContainer.detectAndSendChanges();
		}
	}

	/**
	 * Update the server with an ItemStack in a slot.
	 */
	public void processCreativeInventoryAction(CPacketCreativeInventoryAction packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());

		if (player.interactionManager.isCreative()) {
			boolean flag = packetIn.getSlotId() < 0;
			ItemStack itemstack = packetIn.getStack();

			if (!itemstack.isEmpty() && itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("BlockEntityTag", 10)) {
				NBTTagCompound nbttagcompound = itemstack.getTagCompound().getCompoundTag("BlockEntityTag");

				if (nbttagcompound.hasKey("x") && nbttagcompound.hasKey("y") && nbttagcompound.hasKey("z")) {
					BlockPos blockpos = new BlockPos(nbttagcompound.getInteger("x"), nbttagcompound.getInteger("y"), nbttagcompound.getInteger("z"));
					TileEntity tileentity = player.world.getTileEntity(blockpos);

					if (tileentity != null) {
						NBTTagCompound nbttagcompound1 = tileentity.writeToNBT(new NBTTagCompound());
						nbttagcompound1.removeTag("x");
						nbttagcompound1.removeTag("y");
						nbttagcompound1.removeTag("z");
						itemstack.setTagInfo("BlockEntityTag", nbttagcompound1);
					}
				}
			}

			boolean flag1 = packetIn.getSlotId() >= 1 && packetIn.getSlotId() <= 45;
			boolean flag2 = itemstack.isEmpty() || itemstack.getMetadata() >= 0 && itemstack.getCount() <= 64 && !itemstack.isEmpty();

			if (flag1 && flag2) {
				if (itemstack.isEmpty()) {
					player.inventoryContainer.putStackInSlot(packetIn.getSlotId(), ItemStack.EMPTY);
				} else {
					player.inventoryContainer.putStackInSlot(packetIn.getSlotId(), itemstack);
				}

				player.inventoryContainer.setCanCraft(player, true);
			} else if (flag && flag2 && itemDropThreshold < 200) {
				itemDropThreshold += 20;
				EntityItem entityitem = player.dropItem(itemstack, true);

				if (entityitem != null) {
					entityitem.setAgeToCreativeDespawnTime();
				}
			}
		}
	}

	/**
	 * Received in response to the server requesting to confirm that the client-side open container matches the servers'
	 * after a mismatched container-slot manipulation. It will unlock the player's ability to manipulate the container
	 * contents
	 */
	public void processConfirmTransaction(CPacketConfirmTransaction packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
		Short oshort = pendingTransactions.lookup(player.openContainer.windowId);

		if (oshort != null && packetIn.getUid() == oshort && player.openContainer.windowId == packetIn.getWindowId() && !player.openContainer.getCanCraft(player) && !player.isSpectator()) {
			player.openContainer.setCanCraft(player, true);
		}
	}

	public void processUpdateSign(CPacketUpdateSign packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
		player.markPlayerActive();
		WorldServer worldserver = serverController.getWorld(player.dimension);
		BlockPos blockpos = packetIn.getPosition();

		if (worldserver.isBlockLoaded(blockpos)) {
			IBlockState iblockstate = worldserver.getBlockState(blockpos);
			TileEntity tileentity = worldserver.getTileEntity(blockpos);

			if (!(tileentity instanceof TileEntitySign tileentitysign)) {
				return;
			}

			if (!tileentitysign.getIsEditable() || tileentitysign.getPlayer() != player) {
				serverController.logWarning("Player " + player.getName() + " just tried to change non-editable sign");
				return;
			}

			String[] astring = packetIn.getLines();

			for (int i = 0; i < astring.length; ++i) {
				tileentitysign.signText[i] = new TextComponentString(TextFormatting.getTextWithoutFormattingCodes(astring[i]));
			}

			tileentitysign.markDirty();
			worldserver.notifyBlockUpdate(blockpos, iblockstate, iblockstate, 3);
		}
	}

	/**
	 * Updates a players' ping statistics
	 */
	public void processKeepAlive(CPacketKeepAlive packetIn) {

		if (field_194403_g && packetIn.getKey() == field_194404_h) {
			int i = (int) (currentTimeMillis() - field_194402_f);
			player.ping = (player.ping * 3 + i) / 4;
			field_194403_g = false;
		} else if (!player.getName().equals(serverController.getServerOwner())) {
			disconnect(new TextComponentTranslation("disconnect.timeout"));
		}
	}

	private long currentTimeMillis() {

		return System.nanoTime() / 1000000L;
	}

	/**
	 * Processes a player starting/stopping flying
	 */
	public void processPlayerAbilities(CPacketPlayerAbilities packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
		player.capabilities.isFlying = packetIn.isFlying() && player.capabilities.allowFlying;
	}

	/**
	 * Retrieves possible tab completions for the requested command string and sends them to the client
	 */
	public void processTabComplete(CPacketTabComplete packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
		List<String> list = Lists.newArrayList();

		list.addAll(serverController.getTabCompletions(player, packetIn.getMessage(), packetIn.getTargetBlock(), packetIn.hasTargetBlock()));

		player.connection.sendPacket(new SPacketTabComplete(list.toArray(new String[0])));
	}

	/**
	 * Updates serverside copy of client settings: language, render distance, chat visibility, chat colours, difficulty,
	 * and whether to show the cape
	 */
	public void processClientSettings(CPacketClientSettings packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
		player.handleClientSettings(packetIn);
	}

	/**
	 * Synchronizes serverside and clientside book contents and signing
	 */
	public void processCustomPayload(CPacketCustomPayload packetIn) {

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, player.getServerWorld());
		String s = packetIn.getChannelName();

		if ("MC|BEdit".equals(s)) {
			PacketBuffer packetbuffer = packetIn.getBufferData();

			try {
				ItemStack itemstack = packetbuffer.readItemStack();

				if (itemstack.isEmpty()) {
					return;
				}

				if (!ItemWritableBook.isNBTValid(itemstack.getTagCompound())) {
					throw new IOException("Invalid book tag!");
				}

				ItemStack itemstack1 = player.getHeldItemMainhand();

				if (itemstack1.isEmpty()) {
					return;
				}

				if (itemstack.getItem() == Items.WRITABLE_BOOK && itemstack.getItem() == itemstack1.getItem()) {
					itemstack1.setTagInfo("pages", itemstack.getTagCompound().getTagList("pages", 8));
				}
			} catch (Exception exception6) {
				LOGGER.error("Couldn't handle book info", exception6);
			}
		} else if ("MC|BSign".equals(s)) {
			PacketBuffer packetbuffer1 = packetIn.getBufferData();

			try {
				ItemStack itemstack3 = packetbuffer1.readItemStack();

				if (itemstack3.isEmpty()) {
					return;
				}

				if (!ItemWrittenBook.validBookTagContents(itemstack3.getTagCompound())) {
					throw new IOException("Invalid book tag!");
				}

				ItemStack itemstack4 = player.getHeldItemMainhand();

				if (itemstack4.isEmpty()) {
					return;
				}

				if (itemstack3.getItem() == Items.WRITABLE_BOOK && itemstack4.getItem() == Items.WRITABLE_BOOK) {
					ItemStack itemstack2 = new ItemStack(Items.WRITTEN_BOOK);
					itemstack2.setTagInfo("author", new NBTTagString(player.getName()));
					itemstack2.setTagInfo("title", new NBTTagString(itemstack3.getTagCompound().getString("title")));
					NBTTagList nbttaglist = itemstack3.getTagCompound().getTagList("pages", 8);

					for (int i = 0; i < nbttaglist.tagCount(); ++i) {
						String s1 = nbttaglist.getStringTagAt(i);
						ITextComponent itextcomponent = new TextComponentString(s1);
						s1 = ITextComponent.Serializer.componentToJson(itextcomponent);
						nbttaglist.set(i, new NBTTagString(s1));
					}

					itemstack2.setTagInfo("pages", nbttaglist);
					player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, itemstack2);
				}
			} catch (Exception exception7) {
				LOGGER.error("Couldn't sign book", exception7);
			}
		} else if ("MC|TrSel".equals(s)) {
			try {
				int k = packetIn.getBufferData().readInt();
				Container container = player.openContainer;

				if (container instanceof ContainerMerchant) {
					((ContainerMerchant) container).setCurrentRecipeIndex(k);
				}
			} catch (Exception exception5) {
				LOGGER.error("Couldn't select trade", exception5);
			}
		} else if ("MC|AdvCmd".equals(s)) {
			if (!serverController.isCommandBlockEnabled()) {
				player.sendMessage(new TextComponentTranslation("advMode.notEnabled"));
				return;
			}

			if (!player.canUseCommandBlock()) {
				player.sendMessage(new TextComponentTranslation("advMode.notAllowed"));
				return;
			}

			PacketBuffer packetbuffer2 = packetIn.getBufferData();

			try {
				int l = packetbuffer2.readByte();
				CommandBlockBaseLogic commandblockbaselogic1 = null;

				if (l == 0) {
					TileEntity tileentity = player.world.getTileEntity(new BlockPos(packetbuffer2.readInt(), packetbuffer2.readInt(), packetbuffer2.readInt()));

					if (tileentity instanceof TileEntityCommandBlock) {
						commandblockbaselogic1 = ((TileEntityCommandBlock) tileentity).getCommandBlockLogic();
					}
				} else if (l == 1) {
					Entity entity = player.world.getEntityByID(packetbuffer2.readInt());

					if (entity instanceof EntityMinecartCommandBlock) {
						commandblockbaselogic1 = ((EntityMinecartCommandBlock) entity).getCommandBlockLogic();
					}
				}

				String s6 = packetbuffer2.readString(packetbuffer2.readableBytes());
				boolean flag2 = packetbuffer2.readBoolean();

				if (commandblockbaselogic1 != null) {
					commandblockbaselogic1.setCommand(s6);
					commandblockbaselogic1.setTrackOutput(flag2);

					if (!flag2) {
						commandblockbaselogic1.setLastOutput(null);
					}

					commandblockbaselogic1.updateCommand();
					player.sendMessage(new TextComponentTranslation("advMode.setCommand.success", s6));
				}
			} catch (Exception exception4) {
				LOGGER.error("Couldn't set command block", exception4);
			}
		} else if ("MC|AutoCmd".equals(s)) {
			if (!serverController.isCommandBlockEnabled()) {
				player.sendMessage(new TextComponentTranslation("advMode.notEnabled"));
				return;
			}

			if (!player.canUseCommandBlock()) {
				player.sendMessage(new TextComponentTranslation("advMode.notAllowed"));
				return;
			}

			PacketBuffer packetbuffer3 = packetIn.getBufferData();

			try {
				CommandBlockBaseLogic commandblockbaselogic = null;
				TileEntityCommandBlock tileentitycommandblock = null;
				BlockPos blockpos1 = new BlockPos(packetbuffer3.readInt(), packetbuffer3.readInt(), packetbuffer3.readInt());
				TileEntity tileentity2 = player.world.getTileEntity(blockpos1);

				if (tileentity2 instanceof TileEntityCommandBlock) {
					tileentitycommandblock = (TileEntityCommandBlock) tileentity2;
					commandblockbaselogic = tileentitycommandblock.getCommandBlockLogic();
				}

				String s7 = packetbuffer3.readString(packetbuffer3.readableBytes());
				boolean flag3 = packetbuffer3.readBoolean();
				TileEntityCommandBlock.Mode tileentitycommandblock$mode = TileEntityCommandBlock.Mode.valueOf(packetbuffer3.readString(16));
				boolean flag = packetbuffer3.readBoolean();
				boolean flag1 = packetbuffer3.readBoolean();

				if (commandblockbaselogic != null) {
					Facing enumfacing = player.world.getBlockState(blockpos1).getValue(BlockCommandBlock.FACING);

					switch (tileentitycommandblock$mode) {
						case SEQUENCE:
							IBlockState iblockstate3 = Blocks.CHAIN_COMMAND_BLOCK.getDefaultState();
							player.world.setBlockState(blockpos1, iblockstate3.withProperty(BlockCommandBlock.FACING, enumfacing).withProperty(BlockCommandBlock.CONDITIONAL, flag), 2);
							break;

						case AUTO:
							IBlockState iblockstate2 = Blocks.REPEATING_COMMAND_BLOCK.getDefaultState();
							player.world.setBlockState(blockpos1, iblockstate2.withProperty(BlockCommandBlock.FACING, enumfacing).withProperty(BlockCommandBlock.CONDITIONAL, flag), 2);
							break;

						case REDSTONE:
							IBlockState iblockstate = Blocks.COMMAND_BLOCK.getDefaultState();
							player.world.setBlockState(blockpos1, iblockstate.withProperty(BlockCommandBlock.FACING, enumfacing).withProperty(BlockCommandBlock.CONDITIONAL, flag), 2);
					}

					tileentity2.validate();
					player.world.setTileEntity(blockpos1, tileentity2);
					commandblockbaselogic.setCommand(s7);
					commandblockbaselogic.setTrackOutput(flag3);

					if (!flag3) {
						commandblockbaselogic.setLastOutput(null);
					}

					tileentitycommandblock.setAuto(flag1);
					commandblockbaselogic.updateCommand();

					if (!net.minecraft.util.StringUtils.isNullOrEmpty(s7)) {
						player.sendMessage(new TextComponentTranslation("advMode.setCommand.success", s7));
					}
				}
			} catch (Exception exception3) {
				LOGGER.error("Couldn't set command block", exception3);
			}
		} else if ("MC|Beacon".equals(s)) {
			if (player.openContainer instanceof ContainerBeacon containerbeacon) {
				try {
					PacketBuffer packetbuffer4 = packetIn.getBufferData();
					int i1 = packetbuffer4.readInt();
					int k1 = packetbuffer4.readInt();
					Slot slot = containerbeacon.getSlot(0);

					if (slot.getHasStack()) {
						slot.decrStackSize(1);
						IInventory iinventory = containerbeacon.getTileEntity();
						iinventory.setField(1, i1);
						iinventory.setField(2, k1);
						iinventory.markDirty();
					}
				} catch (Exception exception2) {
					LOGGER.error("Couldn't set beacon", exception2);
				}
			}
		} else if ("MC|ItemName".equals(s)) {
			if (player.openContainer instanceof ContainerRepair containerrepair) {

				if (packetIn.getBufferData() != null && packetIn.getBufferData().readableBytes() >= 1) {
					String s5 = ChatAllowedCharacters.filterAllowedCharacters(packetIn.getBufferData().readString(32767));

					if (s5.length() <= 35) {
						containerrepair.updateItemName(s5);
					}
				} else {
					containerrepair.updateItemName("");
				}
			}
		} else if ("MC|Struct".equals(s)) {
			if (!player.canUseCommandBlock()) {
				return;
			}

			PacketBuffer packetbuffer5 = packetIn.getBufferData();

			try {
				BlockPos blockpos = new BlockPos(packetbuffer5.readInt(), packetbuffer5.readInt(), packetbuffer5.readInt());
				IBlockState iblockstate1 = player.world.getBlockState(blockpos);
				TileEntity tileentity1 = player.world.getTileEntity(blockpos);

				if (tileentity1 instanceof TileEntityStructure tileentitystructure) {
					int l1 = packetbuffer5.readByte();
					String s8 = packetbuffer5.readString(32);
					tileentitystructure.setMode(TileEntityStructure.Mode.valueOf(s8));
					tileentitystructure.setName(packetbuffer5.readString(64));
					int i2 = MathHelper.clamp(packetbuffer5.readInt(), -32, 32);
					int j2 = MathHelper.clamp(packetbuffer5.readInt(), -32, 32);
					int k2 = MathHelper.clamp(packetbuffer5.readInt(), -32, 32);
					tileentitystructure.setPosition(new BlockPos(i2, j2, k2));
					int l2 = MathHelper.clamp(packetbuffer5.readInt(), 0, 32);
					int i3 = MathHelper.clamp(packetbuffer5.readInt(), 0, 32);
					int j = MathHelper.clamp(packetbuffer5.readInt(), 0, 32);
					tileentitystructure.setSize(new BlockPos(l2, i3, j));
					String s2 = packetbuffer5.readString(32);
					tileentitystructure.setMirror(Mirror.valueOf(s2));
					String s3 = packetbuffer5.readString(32);
					tileentitystructure.setRotation(Rotation.valueOf(s3));
					tileentitystructure.setMetadata(packetbuffer5.readString(128));
					tileentitystructure.setIgnoresEntities(packetbuffer5.readBoolean());
					tileentitystructure.setShowAir(packetbuffer5.readBoolean());
					tileentitystructure.setShowBoundingBox(packetbuffer5.readBoolean());
					tileentitystructure.setIntegrity(MathHelper.clamp(packetbuffer5.readFloat(), 0F, 1F));
					tileentitystructure.setSeed(packetbuffer5.readVarLong());
					String s4 = tileentitystructure.getName();

					if (l1 == 2) {
						if (tileentitystructure.save()) {
							player.sendStatusMessage(new TextComponentTranslation("structure_block.save_success", s4), false);
						} else {
							player.sendStatusMessage(new TextComponentTranslation("structure_block.save_failure", s4), false);
						}
					} else if (l1 == 3) {
						if (!tileentitystructure.isStructureLoadable()) {
							player.sendStatusMessage(new TextComponentTranslation("structure_block.load_not_found", s4), false);
						} else if (tileentitystructure.load()) {
							player.sendStatusMessage(new TextComponentTranslation("structure_block.load_success", s4), false);
						} else {
							player.sendStatusMessage(new TextComponentTranslation("structure_block.load_prepare", s4), false);
						}
					} else if (l1 == 4) {
						if (tileentitystructure.detectSize()) {
							player.sendStatusMessage(new TextComponentTranslation("structure_block.size_success", s4), false);
						} else {
							player.sendStatusMessage(new TextComponentTranslation("structure_block.size_failure"), false);
						}
					}

					tileentitystructure.markDirty();
					player.world.notifyBlockUpdate(blockpos, iblockstate1, iblockstate1, 3);
				}
			} catch (Exception exception1) {
				LOGGER.error("Couldn't set structure block", exception1);
			}
		} else if ("MC|PickItem".equals(s)) {
			PacketBuffer packetbuffer6 = packetIn.getBufferData();

			try {
				int j1 = packetbuffer6.readVarInt();
				player.inventory.pickItem(j1);
				player.connection.sendPacket(new SPacketSetSlot(-2, player.inventory.currentItem, player.inventory.getStackInSlot(player.inventory.currentItem)));
				player.connection.sendPacket(new SPacketSetSlot(-2, j1, player.inventory.getStackInSlot(j1)));
				player.connection.sendPacket(new SPacketHeldItemChange(player.inventory.currentItem));
			} catch (Exception exception) {
				LOGGER.error("Couldn't pick item", exception);
			}
		}
	}

}
