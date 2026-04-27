package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.FunctionManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.profiler.Profiler;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.*;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.*;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public abstract class MinecraftServer implements ICommandSender, Runnable, IThreadListener, ISnooperInfo {

	public static final File USER_CACHE_FILE = new File("usercache.json");
	private static final Logger LOGGER = LogManager.getLogger();
	public final ICommandManager commandManager;
	public final Profiler profiler = new Profiler();
	public final long[] tickTimeArray = new long[100];
	public final Queue<FutureTask<?>> futureTaskQueue = Queues.newArrayDeque();
	protected final Proxy serverProxy;
	private final ISaveFormat anvilConverterForAnvilFile;
	/**
	 * The PlayerUsageSnooper instance.
	 */
	private final Snooper usageSnooper = new Snooper("server", this, getCurrentTimeMillis());
	private final File anvilFile;
	private final List<ITickable> tickables = Lists.newArrayList();
	private final NetworkSystem networkSystem;
	private final ServerStatusResponse statusResponse = new ServerStatusResponse();
	private final Random random = new Random();
	private final DataFixer dataFixer;
	/**
	 * The server's port.
	 */
	private final int serverPort = -1;
	private final YggdrasilAuthenticationService authService;
	private final MinecraftSessionService sessionService;
	private final GameProfileRepository profileRepo;
	private final PlayerProfileCache profileCache;
	/**
	 * The server world instances.
	 */
	public WorldServer[] worlds;
	/**
	 * The task the server is currently working on(and will output on outputPercentRemaining).
	 */
	public String currentTask;
	/**
	 * The percentage of the current task finished so far.
	 */
	public int percentDone;
	/**
	 * Stats are [dimension][tick%100] system.nanoTime is stored.
	 */
	public long[][] timeOfLastDimensionTick;
	/**
	 * The player list for this server
	 */
	private PlayerList playerList;
	/**
	 * Indicates whether the server is running or not. Set to false to initiate a shutdown.
	 */
	private boolean serverRunning = true;
	/**
	 * Indicates to other classes that the server is safely stopped.
	 */
	private boolean serverStopped;
	/**
	 * Incremented every tick.
	 */
	private int tickCounter;
	/**
	 * True if the server is in online mode.
	 */
	private boolean onlineMode;
	private boolean preventProxyConnections;
	/**
	 * True if the server has animals turned on.
	 */
	private boolean canSpawnAnimals;
	private boolean canSpawnNPCs;
	/**
	 * Indicates whether PvP is active on the server or not.
	 */
	private boolean pvpEnabled;
	/**
	 * Determines if flight is allowed or not.
	 */
	private boolean allowFlight;
	/**
	 * The server MOTD string.
	 */
	private String motd;
	/**
	 * Maximum build height.
	 */
	private int buildLimit;
	private int maxPlayerIdleMinutes;
	private KeyPair serverKeyPair;
	/**
	 * Username of the server owner (for integrated servers)
	 */
	private String serverOwner;
	private String folderName;
	private String worldName;
	private boolean isDemo;
	private boolean enableBonusChest;
	/**
	 * The texture pack for the server
	 */
	private String resourcePackUrl = "";
	private String resourcePackHash = "";
	private boolean serverIsRunning;
	/**
	 * Set when warned for "Can't keep up", which triggers again after 15 seconds.
	 */
	private long timeOfLastWarning;
	private String userMessage;
	private boolean startProfiling;
	private boolean isGamemodeForced;
	private long nanoTimeSinceStatusRefresh;
	private Thread serverThread;
	private long currentTime = getCurrentTimeMillis();
	private boolean worldIconSet;

	public MinecraftServer(File anvilFileIn, Proxy proxyIn, DataFixer dataFixerIn, YggdrasilAuthenticationService authServiceIn, MinecraftSessionService sessionServiceIn, GameProfileRepository profileRepoIn, PlayerProfileCache profileCacheIn) {

		serverProxy = proxyIn;
		authService = authServiceIn;
		sessionService = sessionServiceIn;
		profileRepo = profileRepoIn;
		profileCache = profileCacheIn;
		anvilFile = anvilFileIn;
		networkSystem = new NetworkSystem(this);
		commandManager = createCommandManager();
		anvilConverterForAnvilFile = new AnvilSaveConverter(anvilFileIn, dataFixerIn);
		dataFixer = dataFixerIn;
	}

	public static long getCurrentTimeMillis() {

		return System.currentTimeMillis();
	}

	public ServerCommandManager createCommandManager() {

		return new ServerCommandManager(this);
	}

	/**
	 * Initialises the server and starts it.
	 */
	public abstract boolean init();

	public void convertMapIfNeeded(String worldNameIn) {

		if (getActiveAnvilConverter().isOldMapFormat(worldNameIn)) {
			LOGGER.info("Converting map!");
			setUserMessage("menu.convertingLevel");
			getActiveAnvilConverter().convertMapFormat(worldNameIn, new IProgressUpdate() {
				private long startTime = System.currentTimeMillis();

				public void displaySavingString(String message) {

				}

				public void resetProgressAndMessage(String message) {

				}

				public void setLoadingProgress(int progress) {

					if (System.currentTimeMillis() - startTime >= 1000L) {
						startTime = System.currentTimeMillis();
						MinecraftServer.LOGGER.info("Converting... {}%", progress);
					}
				}

				public void setDoneWorking() {

				}

				public void displayLoadingString(String message) {

				}
			});
		}
	}

	@Nullable

	public synchronized String getUserMessage() {

		return userMessage;
	}

	/**
	 * Typically "menu.convertingLevel", "menu.loadingLevel" or others.
	 */
	protected synchronized void setUserMessage(String message) {

		userMessage = message;
	}

	public void loadAllWorlds(String saveName, String worldNameIn, long seed, WorldType type, String generatorOptions) {

		convertMapIfNeeded(saveName);
		setUserMessage("menu.loadingLevel");
		worlds = new WorldServer[3];
		timeOfLastDimensionTick = new long[worlds.length][100];
		ISaveHandler isavehandler = anvilConverterForAnvilFile.getSaveLoader(saveName, true);
		setResourcePackFromWorld(getFolderName(), isavehandler);
		WorldInfo worldinfo = isavehandler.loadWorldInfo();
		WorldSettings worldsettings;

		if (worldinfo == null) {
			if (isDemo()) {
				worldsettings = WorldServerDemo.DEMO_WORLD_SETTINGS;
			} else {
				worldsettings = new WorldSettings(seed, getGameType(), canStructuresSpawn(), isHardcore(), type);
				worldsettings.setGeneratorOptions(generatorOptions);

				if (enableBonusChest) {
					worldsettings.enableBonusChest();
				}
			}

			worldinfo = new WorldInfo(worldsettings, worldNameIn);
		} else {
			worldinfo.setWorldName(worldNameIn);
			worldsettings = new WorldSettings(worldinfo);
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
				if (isDemo()) {
					worlds[i] = (WorldServer) (new WorldServerDemo(this, isavehandler, worldinfo, j, profiler)).init();
				} else {
					worlds[i] = (WorldServer) (new WorldServer(this, isavehandler, worldinfo, j, profiler)).init();
				}

				worlds[i].initialize(worldsettings);
			} else {
				worlds[i] = (WorldServer) (new WorldServerMulti(this, isavehandler, j, worlds[0], profiler)).init();
			}

			worlds[i].addEventListener(new ServerWorldEventHandler(this, worlds[i]));

			if (!isSinglePlayer()) {
				worlds[i].getWorldInfo().setGameType(getGameType());
			}
		}

		playerList.setPlayerManager(worlds);
		setDifficultyForAllWorlds(getDifficulty());
		initialWorldChunkLoad();
	}

	public void initialWorldChunkLoad() {

		int i = 16;
		int j = 4;
		int k = 192;
		int l = 625;
		int i1 = 0;
		setUserMessage("menu.generatingTerrain");
		int j1 = 0;
		LOGGER.info("Preparing start region for level 0");
		WorldServer worldserver = worlds[0];
		BlockPos blockpos = worldserver.getSpawnPoint();
		long k1 = getCurrentTimeMillis();

		for (int l1 = -192; l1 <= 192 && isServerRunning(); l1 += 16) {
			for (int i2 = -192; i2 <= 192 && isServerRunning(); i2 += 16) {
				long j2 = getCurrentTimeMillis();

				if (j2 - k1 > 1000L) {
					outputPercentRemaining("Preparing spawn area", i1 * 100 / 625);
					k1 = j2;
				}

				++i1;
				worldserver.getChunkProvider().provideChunk(blockpos.getX() + l1 >> 4, blockpos.getZ() + i2 >> 4);
			}
		}

		clearCurrentTask();
	}

	public void setResourcePackFromWorld(String worldNameIn, ISaveHandler saveHandlerIn) {

		File file1 = new File(saveHandlerIn.getWorldDirectory(), "resources.zip");

		if (file1.isFile()) {
			setResourcePack("level://" + URLEncoder.encode(worldNameIn, StandardCharsets.UTF_8) + "/" + "resources.zip", "");
		}
	}

	public abstract boolean canStructuresSpawn();

	public abstract GameType getGameType();

	/**
	 * Sets the game type for all worlds.
	 */
	public void setGameType(GameType gameMode) {

		for (WorldServer worldserver1 : worlds) {
			worldserver1.getWorldInfo().setGameType(gameMode);
		}
	}

	/**
	 * Get the server's difficulty
	 */
	public abstract EnumDifficulty getDifficulty();

	/**
	 * Defaults to false.
	 */
	public abstract boolean isHardcore();

	public abstract int getOpPermissionLevel();

	/**
	 * Get if RCON command events should be broadcast to ops
	 */
	public abstract boolean shouldBroadcastRconToOps();

	/**
	 * Get if console command events should be broadcast to ops
	 */
	public abstract boolean shouldBroadcastConsoleToOps();

	/**
	 * Used to display a percent remaining given text and the percentage.
	 */
	protected void outputPercentRemaining(String message, int percent) {

		currentTask = message;
		percentDone = percent;
		LOGGER.info("{}: {}%", message, percent);
	}

	/**
	 * Set current task to null and set its percentage to 0.
	 */
	protected void clearCurrentTask() {

		currentTask = null;
		percentDone = 0;
	}

	/**
	 * par1 indicates if a log message should be output.
	 */
	public void saveAllWorlds(boolean isSilent) {

		for (WorldServer worldserver : worlds) {
			if (worldserver != null) {
				if (!isSilent) {
					LOGGER.info("Saving chunks for level '{}'/{}", worldserver.getWorldInfo().getWorldName(), worldserver.provider.getDimensionType().getName());
				}

				try {
					worldserver.saveAllChunks(true, (IProgressUpdate) null);
				} catch (MinecraftException minecraftexception) {
					LOGGER.warn(minecraftexception.getMessage());
				}
			}
		}
	}

	/**
	 * Saves all necessary data as preparation for stopping the server.
	 */
	public void stopServer() {

		LOGGER.info("Stopping server");

		if (getNetworkSystem() != null) {
			getNetworkSystem().terminateEndpoints();
		}

		if (playerList != null) {
			LOGGER.info("Saving players");
			playerList.saveAllPlayerData();
			playerList.removeAllPlayers();
		}

		if (worlds != null) {
			LOGGER.info("Saving worlds");

			for (WorldServer worldserver : worlds) {
				if (worldserver != null) {
					worldserver.disableLevelSaving = false;
				}
			}

			saveAllWorlds(false);

			for (WorldServer worldserver1 : worlds) {
				if (worldserver1 != null) {
					worldserver1.flush();
				}
			}
		}

		if (usageSnooper.isSnooperRunning()) {
			usageSnooper.stopSnooper();
		}
	}

	public boolean isServerRunning() {

		return serverRunning;
	}

	/**
	 * Sets the serverRunning variable to false, in order to get the server to shut down.
	 */
	public void initiateShutdown() {

		serverRunning = false;
	}

	public void run() {

		try {
			if (init()) {
				currentTime = getCurrentTimeMillis();
				long i = 0L;
				statusResponse.setServerDescription(new TextComponentString(motd));
				statusResponse.setVersion(new ServerStatusResponse.Version("1.12.2", 340));
				applyServerIconToResponse(statusResponse);

				while (serverRunning) {
					long k = getCurrentTimeMillis();
					long j = k - currentTime;

					if (j > 2000L && currentTime - timeOfLastWarning >= 15000L) {
						LOGGER.warn("Can't keep up! Did the system time change, or is the server overloaded? Running {}ms behind, skipping {} tick(s)", j, j / 50L);
						j = 2000L;
						timeOfLastWarning = currentTime;
					}

					if (j < 0L) {
						LOGGER.warn("Time ran backwards! Did the system time change?");
						j = 0L;
					}

					i += j;
					currentTime = k;

					if (worlds[0].areAllPlayersAsleep()) {
						tick();
						i = 0L;
					} else {
						while (i > 50L) {
							i -= 50L;
							tick();
						}
					}

					Thread.sleep(Math.max(1L, 50L - i));
					serverIsRunning = true;
				}
			} else {
				finalTick(null);
			}
		} catch (Throwable throwable1) {
			LOGGER.error("Encountered an unexpected exception", throwable1);
			CrashReport crashreport;

			if (throwable1 instanceof ReportedException) {
				crashreport = addServerInfoToCrashReport(((ReportedException) throwable1).getCrashReport());
			} else {
				crashreport = addServerInfoToCrashReport(new CrashReport("Exception in server tick loop", throwable1));
			}

			File file1 = new File(new File(getDataDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");

			if (crashreport.saveToFile(file1)) {
				LOGGER.error("This crash report has been saved to: {}", file1.getAbsolutePath());
			} else {
				LOGGER.error("We were unable to save this crash report to disk.");
			}

			finalTick(crashreport);
		} finally {
			try {
				serverStopped = true;
				stopServer();
			} catch (Throwable throwable) {
				LOGGER.error("Exception stopping the server", throwable);
			} finally {
				systemExitNow();
			}
		}
	}

	public void applyServerIconToResponse(ServerStatusResponse response) {

		File file1 = getFile("server-icon.png");

		if (!file1.exists()) {
			file1 = getActiveAnvilConverter().getFile(getFolderName(), "icon.png");
		}

		if (file1.isFile()) {
			ByteBuf bytebuf = Unpooled.buffer();

			try {
				BufferedImage bufferedimage = ImageIO.read(file1);
				Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
				Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
				ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));
				ByteBuf bytebuf1 = Base64.encode(bytebuf);
				response.setFavicon("data:image/png;base64," + bytebuf1.toString(StandardCharsets.UTF_8));
			} catch (Exception exception) {
				LOGGER.error("Couldn't load server icon", exception);
			} finally {
				bytebuf.release();
			}
		}
	}

	public boolean isWorldIconSet() {

		worldIconSet = worldIconSet || getWorldIconFile().isFile();
		return worldIconSet;
	}

	public File getWorldIconFile() {

		return getActiveAnvilConverter().getFile(getFolderName(), "icon.png");
	}

	public File getDataDirectory() {

		return new File(".");
	}

	/**
	 * Called on exit from the main run() loop.
	 */
	public void finalTick(CrashReport report) {

	}

	/**
	 * Directly calls System.exit(0), instantly killing the program.
	 */
	public void systemExitNow() {

	}

	/**
	 * Main function called by run() every loop.
	 */
	public void tick() {

		long i = System.nanoTime();
		++tickCounter;

		if (startProfiling) {
			startProfiling = false;
			profiler.profilingEnabled = true;
			profiler.clearProfiling();
		}

		profiler.startSection("root");
		updateTimeLightAndEntities();

		if (i - nanoTimeSinceStatusRefresh >= 5000000000L) {
			nanoTimeSinceStatusRefresh = i;
			statusResponse.setPlayers(new ServerStatusResponse.Players(getMaxPlayers(), getCurrentPlayerCount()));
			GameProfile[] agameprofile = new GameProfile[Math.min(getCurrentPlayerCount(), 12)];
			int j = MathHelper.getInt(random, 0, getCurrentPlayerCount() - agameprofile.length);

			for (int k = 0; k < agameprofile.length; ++k) {
				agameprofile[k] = playerList.getPlayers().get(j + k).getGameProfile();
			}

			Collections.shuffle(Arrays.asList(agameprofile));
			statusResponse.getPlayers().setPlayers(agameprofile);
		}

		if (tickCounter % 900 == 0) {
			profiler.startSection("save");
			playerList.saveAllPlayerData();
			saveAllWorlds(true);
			profiler.endSection();
		}

		profiler.startSection("tallying");
		tickTimeArray[tickCounter % 100] = System.nanoTime() - i;
		profiler.endSection();
		profiler.startSection("snooper");

		if (!usageSnooper.isSnooperRunning() && tickCounter > 100) {
			usageSnooper.startSnooper();
		}

		if (tickCounter % 6000 == 0) {
			usageSnooper.addMemoryStatsToSnooper();
		}

		profiler.endSection();
		profiler.endSection();
	}

	public void updateTimeLightAndEntities() {

		profiler.startSection("jobs");

		synchronized (futureTaskQueue) {
			while (!futureTaskQueue.isEmpty()) {
				Util.runTask(futureTaskQueue.poll(), LOGGER);
			}
		}

		profiler.endStartSection("levels");

		for (int j = 0; j < worlds.length; ++j) {
			long i = System.nanoTime();

			if (j == 0 || getAllowNether()) {
				WorldServer worldserver = worlds[j];
				profiler.func_194340_a(() ->
						worldserver.getWorldInfo().getWorldName());

				if (tickCounter % 20 == 0) {
					profiler.startSection("timeSync");
					playerList.sendPacketToAllPlayersInDimension(new SPacketTimeUpdate(worldserver.getTotalWorldTime(), worldserver.getWorldTime(), worldserver.getGameRules().getBoolean("doDaylightCycle")), worldserver.provider.getDimensionType().getId());
					profiler.endSection();
				}

				profiler.startSection("tick");

				try {
					worldserver.tick();
				} catch (Throwable throwable1) {
					CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Exception ticking world");
					worldserver.addWorldInfoToCrashReport(crashreport);
					throw new ReportedException(crashreport);
				}

				try {
					worldserver.updateEntities();
				} catch (Throwable throwable) {
					CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Exception ticking world entities");
					worldserver.addWorldInfoToCrashReport(crashreport1);
					throw new ReportedException(crashreport1);
				}

				profiler.endSection();
				profiler.startSection("tracker");
				worldserver.getEntityTracker().tick();
				profiler.endSection();
				profiler.endSection();
			}

			timeOfLastDimensionTick[j][tickCounter % 100] = System.nanoTime() - i;
		}

		profiler.endStartSection("connection");
		getNetworkSystem().networkTick();
		profiler.endStartSection("players");
		playerList.onTick();
		profiler.endStartSection("commandFunctions");
		getFunctionManager().update();
		profiler.endStartSection("tickables");

		for (ITickable tickable : tickables) {
			tickable.update();
		}

		profiler.endSection();
	}

	public boolean getAllowNether() {

		return true;
	}

	public void startServerThread() {

		serverThread = new Thread(this, "Server thread");
		serverThread.start();
	}

	/**
	 * Returns a File object from the specified string.
	 */
	public File getFile(String fileName) {

		return new File(getDataDirectory(), fileName);
	}

	/**
	 * Logs the message with a level of WARN.
	 */
	public void logWarning(String msg) {

		LOGGER.warn(msg);
	}

	/**
	 * Gets the worldServer by the given dimension.
	 */
	public WorldServer getWorld(int dimension) {

		if (dimension == -1) {
			return worlds[1];
		} else {
			return dimension == 1 ? worlds[2] : worlds[0];
		}
	}

	/**
	 * Returns the server's Minecraft version as string.
	 */
	public String getMinecraftVersion() {

		return "1.12.2";
	}

	/**
	 * Returns the number of players currently on the server.
	 */
	public int getCurrentPlayerCount() {

		return playerList.getCurrentPlayerCount();
	}

	/**
	 * Returns the maximum number of players allowed on the server.
	 */
	public int getMaxPlayers() {

		return playerList.getMaxPlayers();
	}

	/**
	 * Returns an array of the usernames of all the connected players.
	 */
	public String[] getOnlinePlayerNames() {

		return playerList.getOnlinePlayerNames();
	}

	/**
	 * Returns an array of the GameProfiles of all the connected players
	 */
	public GameProfile[] getOnlinePlayerProfiles() {

		return playerList.getOnlinePlayerProfiles();
	}

	public String getServerModName() {

		return "vanilla";
	}

	/**
	 * Adds the server info, including from theWorldServer, to the crash report.
	 */
	public CrashReport addServerInfoToCrashReport(CrashReport report) {

		report.getCategory().addDetail("Profiler Position", () -> profiler.profilingEnabled ? profiler.getNameOfLastSection() : "N/A (disabled)");

		if (playerList != null) {
			report.getCategory().addDetail("Player Count", () -> playerList.getCurrentPlayerCount() + " / " + playerList.getMaxPlayers() + "; " + playerList.getPlayers());
		}

		return report;
	}

	public List<String> getTabCompletions(ICommandSender sender, String input, @Nullable BlockPos pos, boolean hasTargetBlock) {

		List<String> list = Lists.newArrayList();
		boolean flag = input.startsWith("/");

		if (flag) {
			input = input.substring(1);
		}

		if (!flag && !hasTargetBlock) {
			String[] astring = input.split(" ", -1);
			String s2 = astring[astring.length - 1];

			for (String s1 : playerList.getOnlinePlayerNames()) {
				if (CommandBase.doesStringStartWith(s2, s1)) {
					list.add(s1);
				}
			}

			return list;
		} else {
			boolean flag1 = !input.contains(" ");
			List<String> list1 = commandManager.getTabCompletions(sender, input, pos);

			if (!list1.isEmpty()) {
				for (String s : list1) {
					if (flag1 && !hasTargetBlock) {
						list.add("/" + s);
					} else {
						list.add(s);
					}
				}
			}

			return list;
		}
	}

	public boolean isAnvilFileSet() {

		return anvilFile != null;
	}

	/**
	 * Get the name of this object. For players this returns their username
	 */
	public String getName() {

		return "Server";
	}

	/**
	 * Send a chat message to the CommandSender
	 */
	public void sendMessage(ITextComponent component) {

		LOGGER.info(component.getUnformattedText());
	}

	/**
	 * Returns {@code true} if the CommandSender is allowed to execute the command, {@code false} if not
	 */
	public boolean canUseCommand(int permLevel, String commandName) {

		return true;
	}

	public ICommandManager getCommandManager() {

		return commandManager;
	}

	/**
	 * Gets KeyPair instanced in MinecraftServer.
	 */
	public KeyPair getKeyPair() {

		return serverKeyPair;
	}

	public void setKeyPair(KeyPair keyPair) {

		serverKeyPair = keyPair;
	}

	/**
	 * Returns the username of the server owner (for integrated servers)
	 */
	public String getServerOwner() {

		return serverOwner;
	}

	/**
	 * Sets the username of the owner of this server (in the case of an integrated server)
	 */
	public void setServerOwner(String owner) {

		serverOwner = owner;
	}

	public boolean isSinglePlayer() {

		return serverOwner != null;
	}

	public String getFolderName() {

		return folderName;
	}

	public void setFolderName(String name) {

		folderName = name;
	}

	public String getWorldName() {

		return worldName;
	}

	public void setWorldName(String worldNameIn) {

		worldName = worldNameIn;
	}

	public void setDifficultyForAllWorlds(EnumDifficulty difficulty) {

		for (WorldServer worldserver1 : worlds) {
			if (worldserver1 != null) {
				if (worldserver1.getWorldInfo().isHardcoreModeEnabled()) {
					worldserver1.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
					worldserver1.setAllowedSpawnTypes(true, true);
				} else if (isSinglePlayer()) {
					worldserver1.getWorldInfo().setDifficulty(difficulty);
					worldserver1.setAllowedSpawnTypes(worldserver1.getDifficulty() != EnumDifficulty.PEACEFUL, true);
				} else {
					worldserver1.getWorldInfo().setDifficulty(difficulty);
					worldserver1.setAllowedSpawnTypes(allowSpawnMonsters(), canSpawnAnimals);
				}
			}
		}
	}

	public boolean allowSpawnMonsters() {

		return true;
	}

	/**
	 * Gets whether this is a demo or not.
	 */
	public boolean isDemo() {

		return isDemo;
	}

	/**
	 * Sets whether this is a demo or not.
	 */
	public void setDemo(boolean demo) {

		isDemo = demo;
	}

	public void canCreateBonusChest(boolean enable) {

		enableBonusChest = enable;
	}

	public ISaveFormat getActiveAnvilConverter() {

		return anvilConverterForAnvilFile;
	}

	public String getResourcePackUrl() {

		return resourcePackUrl;
	}

	public String getResourcePackHash() {

		return resourcePackHash;
	}

	public void setResourcePack(String url, String hash) {

		resourcePackUrl = url;
		resourcePackHash = hash;
	}

	public void addServerStatsToSnooper(Snooper playerSnooper) {

		playerSnooper.addClientStat("whitelist_enabled", false);
		playerSnooper.addClientStat("whitelist_count", 0);

		if (playerList != null) {
			playerSnooper.addClientStat("players_current", getCurrentPlayerCount());
			playerSnooper.addClientStat("players_max", getMaxPlayers());
			playerSnooper.addClientStat("players_seen", playerList.getAvailablePlayerDat().length);
		}

		playerSnooper.addClientStat("uses_auth", onlineMode);
		playerSnooper.addClientStat("gui_state", getGuiEnabled() ? "enabled" : "disabled");
		playerSnooper.addClientStat("run_time", (getCurrentTimeMillis() - playerSnooper.getMinecraftStartTimeMillis()) / 60L * 1000L);
		playerSnooper.addClientStat("avg_tick_ms", (int) (MathHelper.average(tickTimeArray) * 1.0E-6D));
		int l = 0;

		if (worlds != null) {
			for (WorldServer worldserver1 : worlds) {
				if (worldserver1 != null) {
					WorldInfo worldinfo = worldserver1.getWorldInfo();
					playerSnooper.addClientStat("world[" + l + "][dimension]", worldserver1.provider.getDimensionType().getId());
					playerSnooper.addClientStat("world[" + l + "][mode]", worldinfo.getGameType());
					playerSnooper.addClientStat("world[" + l + "][difficulty]", worldserver1.getDifficulty());
					playerSnooper.addClientStat("world[" + l + "][hardcore]", worldinfo.isHardcoreModeEnabled());
					playerSnooper.addClientStat("world[" + l + "][generator_name]", worldinfo.getTerrainType().getName());
					playerSnooper.addClientStat("world[" + l + "][generator_version]", worldinfo.getTerrainType().getVersion());
					playerSnooper.addClientStat("world[" + l + "][height]", buildLimit);
					playerSnooper.addClientStat("world[" + l + "][chunks_loaded]", worldserver1.getChunkProvider().getLoadedChunkCount());
					++l;
				}
			}
		}

		playerSnooper.addClientStat("worlds", l);
	}

	public void addServerTypeToSnooper(Snooper playerSnooper) {

		playerSnooper.addStatToSnooper("singleplayer", isSinglePlayer());
		playerSnooper.addStatToSnooper("server_brand", getServerModName());
		playerSnooper.addStatToSnooper("gui_supported", GraphicsEnvironment.isHeadless() ? "headless" : "supported");
		playerSnooper.addStatToSnooper("dedicated", isDedicatedServer());
	}

	/**
	 * Returns whether snooping is enabled or not.
	 */
	public boolean isSnooperEnabled() {

		return true;
	}

	public abstract boolean isDedicatedServer();

	public boolean isServerInOnlineMode() {

		return onlineMode;
	}

	public void setOnlineMode(boolean online) {

		onlineMode = online;
	}

	public boolean getPreventProxyConnections() {

		return preventProxyConnections;
	}

	public boolean getCanSpawnAnimals() {

		return canSpawnAnimals;
	}

	public void setCanSpawnAnimals(boolean spawnAnimals) {

		canSpawnAnimals = spawnAnimals;
	}

	public boolean getCanSpawnNPCs() {

		return canSpawnNPCs;
	}

	public void setCanSpawnNPCs(boolean spawnNpcs) {

		canSpawnNPCs = spawnNpcs;
	}

	/**
	 * Get if native transport should be used. Native transport means linux server performance improvements and
	 * optimized packet sending/receiving on linux
	 */
	public abstract boolean shouldUseNativeTransport();

	public boolean isPVPEnabled() {

		return pvpEnabled;
	}

	public void setAllowPvp(boolean allowPvp) {

		pvpEnabled = allowPvp;
	}

	public boolean isFlightAllowed() {

		return allowFlight;
	}

	public void setAllowFlight(boolean allow) {

		allowFlight = allow;
	}

	/**
	 * Return whether command blocks are enabled.
	 */
	public abstract boolean isCommandBlockEnabled();

	public String getMOTD() {

		return motd;
	}

	public void setMOTD(String motdIn) {

		motd = motdIn;
	}

	public int getBuildLimit() {

		return buildLimit;
	}

	public void setBuildLimit(int maxBuildHeight) {

		buildLimit = maxBuildHeight;
	}

	public boolean isServerStopped() {

		return serverStopped;
	}

	public PlayerList getPlayerList() {

		return playerList;
	}

	public void setPlayerList(PlayerList list) {

		playerList = list;
	}

	public NetworkSystem getNetworkSystem() {

		return networkSystem;
	}

	public boolean serverIsInRunLoop() {

		return serverIsRunning;
	}

	public boolean getGuiEnabled() {

		return false;
	}

	/**
	 * On dedicated does nothing. On integrated, sets commandsAllowedForAll, gameType and allows external connections.
	 */
	public abstract String shareToLAN(GameType type, boolean allowCheats);

	public int getTickCounter() {

		return tickCounter;
	}

	public void enableProfiling() {

		startProfiling = true;
	}

	public Snooper getPlayerUsageSnooper() {

		return usageSnooper;
	}

	/**
	 * Get the world, if available. <b>{@code null} is not allowed!</b> If you are not an entity in the world, return
	 * the overworld
	 */
	public World getEntityWorld() {

		return worlds[0];
	}

	public boolean isBlockProtected(World worldIn, BlockPos pos, EntityPlayer playerIn) {

		return false;
	}

	/**
	 * Get the forceGamemode field (whether joining players will be put in their old gamemode or the default one)
	 */
	public boolean getForceGamemode() {

		return isGamemodeForced;
	}

	public Proxy getServerProxy() {

		return serverProxy;
	}

	public int getMaxPlayerIdleMinutes() {

		return maxPlayerIdleMinutes;
	}

	public void setPlayerIdleTimeout(int idleTimeout) {

		maxPlayerIdleMinutes = idleTimeout;
	}

	public MinecraftSessionService getMinecraftSessionService() {

		return sessionService;
	}

	public GameProfileRepository getGameProfileRepository() {

		return profileRepo;
	}

	public PlayerProfileCache getPlayerProfileCache() {

		return profileCache;
	}

	public ServerStatusResponse getServerStatusResponse() {

		return statusResponse;
	}

	public void refreshStatusNextTick() {

		nanoTimeSinceStatusRefresh = 0L;
	}

	@Nullable
	public Entity getEntityFromUuid(UUID uuid) {

		for (WorldServer worldserver1 : worlds) {
			if (worldserver1 != null) {
				Entity entity = worldserver1.getEntityFromUuid(uuid);

				if (entity != null) {
					return entity;
				}
			}
		}

		return null;
	}

	/**
	 * Returns true if the command sender should be sent feedback about executed commands
	 */
	public boolean sendCommandFeedback() {

		return worlds[0].getGameRules().getBoolean("sendCommandFeedback");
	}

	/**
	 * Get the Minecraft server instance
	 */
	public MinecraftServer getServer() {

		return this;
	}

	public int getMaxWorldSize() {

		return 29999984;
	}

	public <V> ListenableFuture<V> callFromMainThread(Callable<V> callable) {

		Validate.notNull(callable);

		if (!isCallingFromMinecraftThread() && !isServerStopped()) {
			ListenableFutureTask<V> listenablefuturetask = ListenableFutureTask.create(callable);

			synchronized (futureTaskQueue) {
				futureTaskQueue.add(listenablefuturetask);
				return listenablefuturetask;
			}
		} else {
			try {
				return Futures.immediateFuture(callable.call());
			} catch (Exception exception) {
				return Futures.immediateFailedFuture(exception);
			}
		}
	}

	public ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule) {

		Validate.notNull(runnableToSchedule);
		return callFromMainThread(Executors.callable(runnableToSchedule));
	}

	public boolean isCallingFromMinecraftThread() {

		return Thread.currentThread() == serverThread;
	}

	/**
	 * The compression treshold. If the packet is larger than the specified amount of bytes, it will be compressed
	 */
	public int getNetworkCompressionThreshold() {

		return 256;
	}

	public int getSpawnRadius(@Nullable WorldServer worldIn) {

		return worldIn != null ? worldIn.getGameRules().getInt("spawnRadius") : 10;
	}

	public AdvancementManager getAdvancementManager() {

		return worlds[0].getAdvancementManager();
	}

	public FunctionManager getFunctionManager() {

		return worlds[0].getFunctionManager();
	}

	public void reload() {

		if (isCallingFromMinecraftThread()) {
			getPlayerList().saveAllPlayerData();
			worlds[0].getLootTableManager().reloadLootTables();
			getAdvancementManager().reload();
			getFunctionManager().reload();
			getPlayerList().reloadResources();
		} else {
			addScheduledTask(this::reload);
		}
	}

}
