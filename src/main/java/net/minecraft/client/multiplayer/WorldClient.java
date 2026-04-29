package net.minecraft.client.multiplayer;

import com.google.common.collect.Sets;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSoundMinecart;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.ParticleFirework;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveDataMemoryStorage;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import java.util.Random;
import java.util.Set;

public class WorldClient extends World {

	/**
	 * The packets that need to be sent to the server.
	 */
	private final NetHandlerPlayClient connection;
	private final Set<Entity> entityList = Sets.newHashSet();
	private final Set<Entity> entitySpawnQueue = Sets.newHashSet();
	private final Minecraft mc = Minecraft.getMinecraft();
	private final Set<ChunkPos> previousActiveChunkSet = Sets.newHashSet();
	protected Set<ChunkPos> visibleChunks;
	/**
	 * The ChunkProviderClient instance
	 */
	private ChunkProviderClient clientChunkProvider;
	private int ambienceTicks;

	public WorldClient(NetHandlerPlayClient netHandler, WorldSettings settings, int dimension, Difficulty difficulty, Profiler profilerIn) {

		super(new SaveHandlerMP(), new WorldInfo(settings, "MpServer"), DimensionType.getById(dimension).createDimension(), profilerIn, true);
		ambienceTicks = rand.nextInt(12000);
		visibleChunks = Sets.newHashSet();
		connection = netHandler;
		getWorldInfo().setDifficulty(difficulty);
		setSpawnPoint(new BlockPos(8, 64, 8));
		provider.setWorld(this);
		chunkProvider = createChunkProvider();
		mapStorage = new SaveDataMemoryStorage();
		calculateInitialSkylight();
		calculateInitialWeather();
	}

	/**
	 * Runs a single tick for the world
	 */
	public void tick() {

		super.tick();
		setTotalWorldTime(getTotalWorldTime() + 1L);

		if (getGameRules().getBoolean("doDaylightCycle")) {
			setWorldTime(getWorldTime() + 1L);
		}

		profiler.startSection("reEntryProcessing");

		for (int i = 0; i < 10 && !entitySpawnQueue.isEmpty(); ++i) {
			Entity entity = entitySpawnQueue.iterator().next();
			entitySpawnQueue.remove(entity);

			if (!loadedEntityList.contains(entity)) {
				spawnEntity(entity);
			}
		}

		profiler.endStartSection("chunkCache");
		clientChunkProvider.tick();
		profiler.endStartSection("blocks");
		updateBlocks();
		profiler.endSection();
	}

	/**
	 * Invalidates an AABB region of blocks from the receive queue, in the event that the block has been modified
	 * client-side in the intervening 80 receive ticks.
	 */
	public void invalidateBlockReceiveRegion(int x1, int y1, int z1, int x2, int y2, int z2) {

	}

	/**
	 * Creates the chunk provider for this world. Called in the constructor. Retrieves provider from worldProvider?
	 */
	protected IChunkProvider createChunkProvider() {

		clientChunkProvider = new ChunkProviderClient(this);
		return clientChunkProvider;
	}

	protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {

		return allowEmpty || !getChunkProvider().provideChunk(x, z).isEmpty();
	}

	protected void refreshVisibleChunks() {

		visibleChunks.clear();
		int i = mc.gameSettings.renderDistanceChunks;
		profiler.startSection("buildList");
		int j = MathHelper.floor(mc.player.posX / 16D);
		int k = MathHelper.floor(mc.player.posZ / 16D);

		for (int l = -i; l <= i; ++l) {
			for (int i1 = -i; i1 <= i; ++i1) {
				visibleChunks.add(new ChunkPos(l + j, i1 + k));
			}
		}

		profiler.endSection();
	}

	protected void updateBlocks() {

		refreshVisibleChunks();

		if (ambienceTicks > 0) {
			--ambienceTicks;
		}

		previousActiveChunkSet.retainAll(visibleChunks);

		if (previousActiveChunkSet.size() == visibleChunks.size()) {
			previousActiveChunkSet.clear();
		}

		int i = 0;

		for (ChunkPos chunkpos : visibleChunks) {
			if (!previousActiveChunkSet.contains(chunkpos)) {
				int j = chunkpos.x * 16;
				int k = chunkpos.z * 16;
				profiler.startSection("getChunk");
				Chunk chunk = getChunkFromChunkCoords(chunkpos.x, chunkpos.z);
				playMoodSoundAndCheckLight(j, k, chunk);
				profiler.endSection();
				previousActiveChunkSet.add(chunkpos);
				++i;

				if (i >= 10) {
					return;
				}
			}
		}
	}

	public void doPreChunk(int chunkX, int chunkZ, boolean loadChunk) {

		if (loadChunk) {
			clientChunkProvider.loadChunk(chunkX, chunkZ);
		} else {
			clientChunkProvider.unloadChunk(chunkX, chunkZ);
			markBlockRangeForRenderUpdate(chunkX * 16, 0, chunkZ * 16, chunkX * 16 + 15, 256, chunkZ * 16 + 15);
		}
	}

	/**
	 * Called when an entity is spawned in the world. This includes players.
	 */
	public boolean spawnEntity(Entity entityIn) {

		boolean flag = super.spawnEntity(entityIn);
		entityList.add(entityIn);

		if (flag) {
			if (entityIn instanceof EntityMinecart) {
				mc.getSoundHandler().playSound(new MovingSoundMinecart((EntityMinecart) entityIn));
			}
		} else {
			entitySpawnQueue.add(entityIn);
		}

		return flag;
	}

	/**
	 * Schedule the entity for removal during the next tick. Marks the entity dead in anticipation.
	 */
	public void removeEntity(Entity entityIn) {

		super.removeEntity(entityIn);
		entityList.remove(entityIn);
	}

	protected void onEntityAdded(Entity entityIn) {

		super.onEntityAdded(entityIn);

		entitySpawnQueue.remove(entityIn);
	}

	protected void onEntityRemoved(Entity entityIn) {

		super.onEntityRemoved(entityIn);

		if (entityList.contains(entityIn)) {
			if (entityIn.isEntityAlive()) {
				entitySpawnQueue.add(entityIn);
			} else {
				entityList.remove(entityIn);
			}
		}
	}

	/**
	 * Add an ID to Entity mapping to entityHashSet
	 */
	public void addEntityToWorld(int entityID, Entity entityToSpawn) {

		Entity entity = getEntityByID(entityID);

		if (entity != null) {
			removeEntity(entity);
		}

		entityList.add(entityToSpawn);
		entityToSpawn.setEntityId(entityID);

		if (!spawnEntity(entityToSpawn)) {
			entitySpawnQueue.add(entityToSpawn);
		}

		entitiesById.addKey(entityID, entityToSpawn);
	}

	

	/**
	 * Returns the Entity with the given ID, or null if it doesn't exist in this World.
	 */
	public Entity getEntityByID(int id) {

		return id == mc.player.getEntityId() ? mc.player : super.getEntityByID(id);
	}

	public Entity removeEntityFromWorld(int entityID) {

		Entity entity = entitiesById.removeObject(entityID);

		if (entity != null) {
			entityList.remove(entity);
			removeEntity(entity);
		}

		return entity;
	}

	@Deprecated
	public boolean invalidateRegionAndSetBlock(BlockPos pos, IBlockState state) {

		int i = pos.getX();
		int j = pos.getY();
		int k = pos.getZ();
		invalidateBlockReceiveRegion(i, j, k, i, j, k);
		return super.setBlockState(pos, state, 3);
	}

	/**
	 * If on MP, sends a quitting packet.
	 */
	public void sendQuittingDisconnectingPacket() {

		connection.getNetworkManager().closeChannel(new TextComponentString("Quitting"));
	}

	/**
	 * Updates all weather states.
	 */
	protected void updateWeather() {

	}

	protected void playMoodSoundAndCheckLight(int p_147467_1_, int p_147467_2_, Chunk chunkIn) {

		super.playMoodSoundAndCheckLight(p_147467_1_, p_147467_2_, chunkIn);

		if (ambienceTicks == 0) {
			updateLCG = updateLCG * 3 + 1013904223;
			int i = updateLCG >> 2;
			int j = i & 15;
			int k = i >> 8 & 15;
			int l = i >> 16 & 255;
			BlockPos blockpos = new BlockPos(j + p_147467_1_, l, k + p_147467_2_);
			IBlockState iblockstate = chunkIn.getBlockState(blockpos);
			j = j + p_147467_1_;
			k = k + p_147467_2_;

			if (iblockstate.getMaterial() == Material.AIR && getLight(blockpos) <= rand.nextInt(8) && getLightFor(SkyBlock.SKY, blockpos) <= 0) {
				double d0 = mc.player.getDistanceSq((double) j + 0.5D, (double) l + 0.5D, (double) k + 0.5D);

				if (mc.player != null && d0 > 4D && d0 < 256D) {
					playSound((double) j + 0.5D, (double) l + 0.5D, (double) k + 0.5D, SoundEvents.AMBIENT_CAVE, SoundCategory.AMBIENT, 0.7F, 0.8F + rand.nextFloat() * 0.2F, false);
					ambienceTicks = rand.nextInt(12000) + 6000;
				}
			}
		}
	}

	public void doVoidFogParticles(int posX, int posY, int posZ) {

		int i = 32;
		Random random = new Random();
		ItemStack itemstack = mc.player.getHeldItemMainhand();
		boolean flag = mc.playerController.getCurrentGameType() == GameType.CREATIVE && !itemstack.isEmpty() && itemstack.getItem() == Item.getItemFromBlock(Blocks.BARRIER);
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

		for (int j = 0; j < 667; ++j) {
			showBarrierParticles(posX, posY, posZ, 16, random, flag, blockpos$mutableblockpos);
			showBarrierParticles(posX, posY, posZ, 32, random, flag, blockpos$mutableblockpos);
		}
	}

	public void showBarrierParticles(int x, int y, int z, int offset, Random random, boolean holdingBarrier, BlockPos.MutableBlockPos pos) {

		int i = x + rand.nextInt(offset) - rand.nextInt(offset);
		int j = y + rand.nextInt(offset) - rand.nextInt(offset);
		int k = z + rand.nextInt(offset) - rand.nextInt(offset);
		pos.setPos(i, j, k);
		IBlockState iblockstate = getBlockState(pos);
		iblockstate.getBlock().randomDisplayTick(iblockstate, this, pos, random);

		if (holdingBarrier && iblockstate.getBlock() == Blocks.BARRIER) {
			spawnParticle(ParticleTypes.BARRIER, (float) i + 0.5F, (float) j + 0.5F, (float) k + 0.5F, 0D, 0D, 0D);
		}
	}

	/**
	 * also releases skins.
	 */
	public void removeAllEntities() {

		loadedEntityList.removeAll(unloadedEntityList);

		for (Entity entity : unloadedEntityList) {
			int j = entity.chunkCoordX;
			int k = entity.chunkCoordZ;

			if (entity.addedToChunk && isChunkLoaded(j, k, true)) {
				getChunkFromChunkCoords(j, k).removeEntity(entity);
			}
		}

		for (Entity entity : unloadedEntityList) {
			onEntityRemoved(entity);
		}

		unloadedEntityList.clear();

		for (int j1 = 0; j1 < loadedEntityList.size(); ++j1) {
			Entity entity1 = loadedEntityList.get(j1);
			Entity entity2 = entity1.getRidingEntity();

			if (entity2 != null) {
				if (!entity2.isDead && entity2.isPassenger(entity1)) {
					continue;
				}

				entity1.dismountRidingEntity();
			}

			if (entity1.isDead) {
				int k1 = entity1.chunkCoordX;
				int l = entity1.chunkCoordZ;

				if (entity1.addedToChunk && isChunkLoaded(k1, l, true)) {
					getChunkFromChunkCoords(k1, l).removeEntity(entity1);
				}

				loadedEntityList.remove(j1--);
				onEntityRemoved(entity1);
			}
		}
	}

	/**
	 * Adds some basic stats of the world to the given crash report.
	 */
	public CrashReportCategory addWorldInfoToCrashReport(CrashReport report) {

		CrashReportCategory crashreportcategory = super.addWorldInfoToCrashReport(report);
		crashreportcategory.addDetail("Forced entities", () -> entityList.size() + " total; " + entityList);
		crashreportcategory.addDetail("Retry entities", () -> entitySpawnQueue.size() + " total; " + entitySpawnQueue);
		crashreportcategory.addDetail("Server brand", () -> mc.player.getServerBrand());
		crashreportcategory.addDetail("Server type", () -> mc.getIntegratedServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server");
		return crashreportcategory;
	}

	public void playSound(EntityPlayer player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {

		if (player == mc.player) {
			playSound(x, y, z, soundIn, category, volume, pitch, false);
		}
	}

	public void playSound(BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {

		playSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, soundIn, category, volume, pitch, distanceDelay);
	}

	public void playSound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {

		double d0 = mc.getRenderViewEntity().getDistanceSq(x, y, z);
		PositionedSoundRecord positionedsoundrecord = new PositionedSoundRecord(soundIn, category, volume, pitch, (float) x, (float) y, (float) z);

		if (distanceDelay && d0 > 100D) {
			double d1 = Math.sqrt(d0) / 40D;
			mc.getSoundHandler().playDelayedSound(positionedsoundrecord, (int) (d1 * 20D));
		} else {
			mc.getSoundHandler().playSound(positionedsoundrecord);
		}
	}

	public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, NBTTagCompound compound) {

		mc.effectRenderer.addEffect(new ParticleFirework.Starter(this, x, y, z, motionX, motionY, motionZ, mc.effectRenderer, compound));
	}

	public void sendPacketToServer(Packet<?> packetIn) {

		connection.sendPacket(packetIn);
	}

	public void setWorldScoreboard(Scoreboard scoreboardIn) {

		worldScoreboard = scoreboardIn;
	}

	/**
	 * Sets the world time.
	 */
	public void setWorldTime(long time) {

		if (time < 0L) {
			time = -time;
			getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
		} else {
			getGameRules().setOrCreateGameRule("doDaylightCycle", "true");
		}

		super.setWorldTime(time);
	}

	/**
	 * gets the world's chunk provider
	 */
	public ChunkProviderClient getChunkProvider() {

		return (ChunkProviderClient) super.getChunkProvider();
	}

}
