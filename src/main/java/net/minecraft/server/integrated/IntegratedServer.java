package net.minecraft.server.integrated;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ThreadLanServerPing;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.CryptManager;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.Util;
import net.minecraft.world.*;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

public class IntegratedServer extends MinecraftServer {

	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * The Minecraft instance.
	 */
	private final Minecraft mc;
	private final WorldSettings worldSettings;
	private boolean isGamePaused;
	private boolean isPublic;
	private ThreadLanServerPing lanServerPing;

	public IntegratedServer(Minecraft clientIn, String folderNameIn, String worldNameIn, WorldSettings worldSettingsIn, YggdrasilAuthenticationService authServiceIn, MinecraftSessionService sessionServiceIn, GameProfileRepository profileRepoIn, PlayerProfileCache profileCacheIn) {

		super(new File(clientIn.mcDataDir, "saves"), clientIn.getProxy(), clientIn.getDataFixer(), authServiceIn, sessionServiceIn, profileRepoIn, profileCacheIn);
		setServerOwner(clientIn.getSession().getUsername());
		setFolderName(folderNameIn);
		setWorldName(worldNameIn);
		canCreateBonusChest(worldSettingsIn.isBonusChestEnabled());
		setBuildLimit(256);
		setPlayerList(new IntegratedPlayerList(this));
		mc = clientIn;
		worldSettings = worldSettingsIn;
	}

	public ServerCommandManager createCommandManager() {

		return new IntegratedServerCommandManager(this);
	}

	public void loadAllWorlds(String saveName, String worldNameIn, long seed, WorldType type, String generatorOptions) {

		convertMapIfNeeded(saveName);
		worlds = new WorldServer[3];
		timeOfLastDimensionTick = new long[worlds.length][100];
		ISaveHandler isavehandler = getActiveAnvilConverter().getSaveLoader(saveName, true);
		setResourcePackFromWorld(getFolderName(), isavehandler);
		WorldInfo worldinfo = isavehandler.loadWorldInfo();

		if (worldinfo == null) {
			worldinfo = new WorldInfo(worldSettings, worldNameIn);
		} else {
			worldinfo.setWorldName(worldNameIn);
		}

		for (int i = 0; i < worlds.length; ++i) {
			int j = 0;

			if (i == 1) {
				j = -1;
			}

			if (i == 2) {
				j = 1;
			}

			if (i == 0) {
				worlds[i] = (WorldServer) (new WorldServer(this, isavehandler, worldinfo, j, profiler)).init();
				worlds[i].initialize(worldSettings);
			} else {
				worlds[i] = (WorldServer) (new WorldServerMulti(this, isavehandler, j, worlds[0], profiler)).init();
			}

			worlds[i].addEventListener(new ServerWorldEventHandler(this, worlds[i]));
		}

		getPlayerList().setPlayerManager(worlds);

		if (worlds[0].getWorldInfo().getDifficulty() == null) {
			setDifficultyForAllWorlds(mc.gameSettings.difficulty);
		}

		initialWorldChunkLoad();
	}

	/**
	 * Initialises the server and starts it.
	 */
	public boolean init() {

		LOGGER.info("Starting integrated minecraft server version 1.12.2");
		setOnlineMode(true);
		setCanSpawnAnimals(true);
		setCanSpawnNPCs(true);
		setAllowPvp(true);
		setAllowFlight(true);
		LOGGER.info("Generating keypair");
		setKeyPair(CryptManager.generateKeyPair());
		loadAllWorlds(getFolderName(), getWorldName(), worldSettings.getSeed(), worldSettings.getTerrainType(), worldSettings.getGeneratorOptions());
		setMOTD(getServerOwner() + " - " + worlds[0].getWorldInfo().getWorldName());
		return true;
	}

	/**
	 * Main function called by run() every loop.
	 */
	public void tick() {

		boolean flag = isGamePaused;
		isGamePaused = Minecraft.getMinecraft().getConnection() != null && Minecraft.getMinecraft().isGamePaused();

		if (!flag && isGamePaused) {
			LOGGER.info("Saving and pausing game...");
			getPlayerList().saveAllPlayerData();
			saveAllWorlds(false);
		}

		if (isGamePaused) {
			synchronized (futureTaskQueue) {
				while (!futureTaskQueue.isEmpty()) {
					Util.runTask(futureTaskQueue.poll(), LOGGER);
				}
			}
		} else {
			super.tick();

			if (mc.gameSettings.renderDistanceChunks != getPlayerList().getViewDistance()) {
				LOGGER.info("Changing view distance to {}, from {}", mc.gameSettings.renderDistanceChunks, getPlayerList().getViewDistance());
				getPlayerList().setViewDistance(mc.gameSettings.renderDistanceChunks);
			}

			if (mc.world != null) {
				WorldInfo worldinfo1 = worlds[0].getWorldInfo();
				WorldInfo worldinfo = mc.world.getWorldInfo();

				if (!worldinfo1.isDifficultyLocked() && worldinfo.getDifficulty() != worldinfo1.getDifficulty()) {
					LOGGER.info("Changing difficulty to {}, from {}", worldinfo.getDifficulty(), worldinfo1.getDifficulty());
					setDifficultyForAllWorlds(worldinfo.getDifficulty());
				} else if (worldinfo.isDifficultyLocked() && !worldinfo1.isDifficultyLocked()) {
					LOGGER.info("Locking difficulty to {}", worldinfo.getDifficulty());

					for (WorldServer worldserver : worlds) {
						if (worldserver != null) {
							worldserver.getWorldInfo().setDifficultyLocked(true);
						}
					}
				}
			}
		}
	}

	public boolean canStructuresSpawn() {

		return false;
	}

	public GameType getGameType() {

		return worldSettings.getGameType();
	}

	/**
	 * Sets the game type for all worlds.
	 */
	public void setGameType(GameType gameMode) {

		super.setGameType(gameMode);
		getPlayerList().setGameType(gameMode);
	}

	/**
	 * Get the server's difficulty
	 */
	public Difficulty getDifficulty() {

		return mc.world.getWorldInfo().getDifficulty();
	}

	/**
	 * Defaults to false.
	 */
	public boolean isHardcore() {

		return worldSettings.getHardcoreEnabled();
	}

	/**
	 * Get if RCON command events should be broadcast to ops
	 */
	public boolean shouldBroadcastRconToOps() {

		return true;
	}

	/**
	 * Get if console command events should be broadcast to ops
	 */
	public boolean shouldBroadcastConsoleToOps() {

		return true;
	}

	/**
	 * par1 indicates if a log message should be output.
	 */
	public void saveAllWorlds(boolean isSilent) {

		super.saveAllWorlds(isSilent);
	}

	public File getDataDirectory() {

		return mc.mcDataDir;
	}

	public boolean isDedicatedServer() {

		return false;
	}

	/**
	 * Get if native transport should be used. Native transport means linux server performance improvements and
	 * optimized packet sending/receiving on linux
	 */
	public boolean shouldUseNativeTransport() {

		return false;
	}

	/**
	 * Called on exit from the main run() loop.
	 */
	public void finalTick(CrashReport report) {

		mc.crashed(report);
	}

	/**
	 * Adds the server info, including from theWorldServer, to the crash report.
	 */
	public CrashReport addServerInfoToCrashReport(CrashReport report) {

		report = super.addServerInfoToCrashReport(report);
		report.getCategory().addDetail("Type", () -> "Integrated Server (map_client.txt)");
		report.getCategory().addDetail("Is Modded", () -> {

			String s = ClientBrandRetriever.getClientModName();

			if (!s.equals("vanilla")) {
				return "Definitely; Client brand changed to '" + s + "'";
			} else {
				s = getServerModName();

				if (!"vanilla".equals(s)) {
					return "Definitely; Server brand changed to '" + s + "'";
				} else {
					return Minecraft.class.getSigners() == null ? "Very likely; Jar signature invalidated" : "Probably not. Jar signature remains and both client + server brands are untouched.";
				}
			}
		});
		return report;
	}

	public void setDifficultyForAllWorlds(Difficulty difficulty) {

		super.setDifficultyForAllWorlds(difficulty);

		if (mc.world != null) {
			mc.world.getWorldInfo().setDifficulty(difficulty);
		}
	}

	public void addServerStatsToSnooper(Snooper playerSnooper) {

		super.addServerStatsToSnooper(playerSnooper);
		playerSnooper.addClientStat("snooper_partner", mc.getPlayerUsageSnooper().getUniqueID());
	}

	/**
	 * Returns whether snooping is enabled or not.
	 */
	public boolean isSnooperEnabled() {

		return Minecraft.getMinecraft().isSnooperEnabled();
	}

	/**
	 * On dedicated does nothing. On integrated, sets commandsAllowedForAll, gameType and allows external connections.
	 */
	public String shareToLAN(GameType type, boolean allowCheats) {

		try {
			int i = -1;

			try {
				i = HttpUtil.getSuitableLanPort();
			} catch (IOException ignored) {
			}

			if (i <= 0) {
				i = 25564;
			}

			getNetworkSystem().addLanEndpoint(null, i);
			LOGGER.info("Started on {}", i);
			isPublic = true;
			lanServerPing = new ThreadLanServerPing(getMOTD(), i + "");
			lanServerPing.start();
			getPlayerList().setGameType(type);
			getPlayerList().setCommandsAllowedForAll(allowCheats);
			mc.player.setPermissionLevel(allowCheats ? 4 : 0);
			return i + "";
		} catch (IOException var6) {
			return null;
		}
	}

	/**
	 * Saves all necessary data as preparation for stopping the server.
	 */
	public void stopServer() {

		super.stopServer();

		if (lanServerPing != null) {
			lanServerPing.interrupt();
			lanServerPing = null;
		}
	}

	/**
	 * Sets the serverRunning variable to false, in order to get the server to shut down.
	 */
	public void initiateShutdown() {

		Futures.getUnchecked(addScheduledTask(() -> {

			for (EntityPlayerMP entityplayermp : Lists.newArrayList(getPlayerList().getPlayers())) {
				if (!entityplayermp.getUniqueID().equals(mc.player.getUniqueID())) {
					getPlayerList().playerLoggedOut(entityplayermp);
				}
			}
		}));
		super.initiateShutdown();

		if (lanServerPing != null) {
			lanServerPing.interrupt();
			lanServerPing = null;
		}
	}

	/**
	 * Returns true if this integrated server is open to LAN
	 */
	public boolean getPublic() {

		return isPublic;
	}

	/**
	 * Return whether command blocks are enabled.
	 */
	public boolean isCommandBlockEnabled() {

		return true;
	}

	public int getOpPermissionLevel() {

		return 4;
	}

}
