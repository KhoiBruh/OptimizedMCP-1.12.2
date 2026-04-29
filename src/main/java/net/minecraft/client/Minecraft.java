package net.minecraft.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.advancements.GuiScreenAdvancements;
import net.minecraft.client.gui.chat.GuiChat;
import net.minecraft.client.gui.chat.GuiNewChat;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.error.GuiMemoryErrorScreen;
import net.minecraft.client.gui.game.*;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.loading.GuiScreenWorking;
import net.minecraft.client.gui.menu.GuiMainMenu;
import net.minecraft.client.gui.menu.GuiMultiplayer;
import net.minecraft.client.gui.option.GuiControls;
import net.minecraft.client.gui.option.ScreenChatOptions;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.main.GameConfiguration;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.*;
import net.minecraft.client.settings.CreativeSettings;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.client.util.*;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.ConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.CPacketLoginStart;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.profiler.Profiler;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.*;
import net.minecraft.util.Timer;
import net.minecraft.util.Util;
import net.minecraft.util.NonNullList;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Facing;
import net.minecraft.util.Hand;
import net.minecraft.world.Difficulty;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.util.glu.GLU;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

public class Minecraft implements IThreadListener, ISnooperInfo {

	// Public static fields
	public static final boolean IS_RUNNING_ON_MAC = Util.getOSType() == Util.OS.OSX;
	public static byte[] memoryReserve = new byte[10485760];

	// Public instance fields
	public CreativeSettings creativeSettings;
	public GuiScreen currentScreen;
	public String debug = "";
	public DebugRenderer debugRenderer;
	public int displayHeight;
	public int displayWidth;
	public ParticleManager effectRenderer;
	public EntityRenderer entityRenderer;
	public FontRenderer fontRenderer;
	public final FrameTimer frameTimer = new FrameTimer();
	public GameSettings gameSettings;
	public GuiIngame ingameGUI;
	public boolean inGameHasFocus;
	public LoadingScreenRenderer loadingScreen;
	public final File mcDataDir;
	public MouseHelper mouseHelper;
	public RayTraceResult objectMouseOver;
	public EntityPlayerSP player;
	public PlayerControllerMP playerController;
	public Entity pointedEntity;
	public final Profiler profiler = new Profiler();
	public boolean renderChunksMany = true;
	public RenderGlobal renderGlobal;
	public ScaledResolution scaledResolution;
	public boolean skipRenderWorld;
	public FontRenderer standardGalacticFontRenderer;
	public WorldClient world;

	// Package-private fields
	long prevFrameTime = -1L;
	volatile boolean running = true;
	long startNanoTime = System.nanoTime();
	long systemTime = getSystemTime();

	// Private static fields
	private static int debugFPS;
	private static Minecraft instance;
	private static final ResourceLocation LOCATION_MOJANG_PNG = new ResourceLocation("textures/gui/title/mojang.png");
	private static final Logger LOGGER = LogManager.getLogger();

	// Private instance fields
	private boolean actionKeyF3;
	private BlockColors blockColors;
	private BlockRendererDispatcher blockRenderDispatcher;
	private CrashReport crashReporter;
	private ServerData currentServerData;
	private final DataFixer dataFixer;
	private long debugCrashKeyPressTime = -1L;
	private String debugProfilerName = "root";
	private long debugUpdateTime = getSystemTime();
	private final List<IResourcePack> defaultResourcePacks = Lists.newArrayList();
	private final File fileAssets;
	private final File fileResourcepacks;
	private Framebuffer framebufferMc;
	private int fpsCounter;
	private boolean fullscreen;
	private boolean hasCrashed;
	private IntegratedServer integratedServer;
	private boolean integratedServerIsRunning;
	private boolean isGamePaused;
	private ItemColors itemColors;
	private ItemRenderer itemRenderer;
	private int joinPlayerCounter;
	private final String launchedVersion;
	private int leftClickCounter;
	private final DefaultResourcePack mcDefaultResourcePack;
	private LanguageManager mcLanguageManager;
	private MusicTicker mcMusicTicker;
	private IReloadableResourceManager mcResourceManager;
	private ResourcePackRepository mcResourcePackRepository;
	private SoundHandler mcSoundHandler;
	private final Thread mcThread = Thread.currentThread();
	private final MetadataSerializer metadataSerializer = new MetadataSerializer();
	private ModelManager modelManager;
	private ResourceLocation mojangLogo;
	private NetworkManager myNetworkManager;
	private final PropertyMap profileProperties;
	private final Proxy proxy;
	private TextureManager renderEngine;
	private RenderItem renderItem;
	private RenderManager renderManager;
	private float renderPartialTicksPaused;
	private Entity renderViewEntity;
	private int rightClickDelayTimer;
	private ISaveFormat saveLoader;
	private final Queue<FutureTask<?>> scheduledTasks = Queues.newArrayDeque();
	private final SearchTreeManager searchTreeManager = new SearchTreeManager();
	private String serverName;
	private int serverPort;
	private final Session session;
	private final MinecraftSessionService sessionService;
	private SkinManager skinManager;
	private final int tempDisplayHeight;
	private final int tempDisplayWidth;
	private TextureMap textureMapBlocks;
	private final Timer timer = new Timer(20F);
	private final GuiToast toastGui;
	private final Tutorial tutorial;
	private final Snooper usageSnooper = new Snooper("client", this, MinecraftServer.getCurrentTimeMillis());
	private final String versionType;

	public Minecraft(GameConfiguration gameConfig) {
		
		instance = this;
		mcDataDir = gameConfig.folderInfo().mcDataDir();
		fileAssets = gameConfig.folderInfo().assetsDir();
		fileResourcepacks = gameConfig.folderInfo().resourcePacksDir();
		launchedVersion = gameConfig.gameInfo().version();
		versionType = gameConfig.gameInfo().versionType();
		profileProperties = gameConfig.userInfo().profileProperties();
		mcDefaultResourcePack = new DefaultResourcePack(gameConfig.folderInfo().getAssetsIndex());
		proxy = gameConfig.userInfo().proxy() == null ? Proxy.NO_PROXY : gameConfig.userInfo().proxy();
		sessionService = (new YggdrasilAuthenticationService(proxy, UUID.randomUUID()
		                                                                .toString())).createMinecraftSessionService();
		session = gameConfig.userInfo().session();
		LOGGER.info("Setting user: {}", session.getUsername());
		LOGGER.debug("(Session ID is {})", session.getSessionID());
		displayWidth = gameConfig.displayInfo().width() > 0 ? gameConfig.displayInfo().width() : 1;
		displayHeight = gameConfig.displayInfo().height() > 0 ? gameConfig.displayInfo().height() : 1;
		tempDisplayWidth = gameConfig.displayInfo().width();
		tempDisplayHeight = gameConfig.displayInfo().height();
		fullscreen = gameConfig.displayInfo().fullscreen();
		integratedServer = null;
		
		if (gameConfig.serverInfo().serverName() != null) {
			serverName = gameConfig.serverInfo().serverName();
			serverPort = gameConfig.serverInfo().serverPort();
		}
		
		ImageIO.setUseCache(false);
		Locale.setDefault(Locale.ROOT);
		Bootstrap.register();
		TextComponentKeybind.displaySupplierFunction = KeyBinding::getDisplayString;
		dataFixer = DataFixesManager.createFixer();
		toastGui = new GuiToast(this);
		tutorial = new Tutorial(this);
	}
	
	public static boolean isGuiEnabled() {
		
		return instance == null || !instance.gameSettings.hideGUI;
	}
	
	public static boolean isFancyGraphicsEnabled() {
		
		return instance != null && instance.gameSettings.fancyGraphics;
	}
	
	/**
	 * Returns if ambient occlusion is enabled
	 */
	public static boolean isAmbientOcclusionEnabled() {
		
		return instance != null && instance.gameSettings.ambientOcclusion != 0;
	}
	
	/**
	 * Return the singleton Minecraft instance for the game
	 */
	public static Minecraft getMinecraft() {
		
		return instance;
	}
	
	/**
	 * Used in the usage snooper.
	 */
	public static int getGLMaximumTextureSize() {
		
		for (int i = 16384; i > 0; i >>= 1) {
			GlStateManager.glTexImage2D(32868, 0, 6408, i, i, 0, 6408, 5121, null);
			int j = GlStateManager.glGetTexLevelParameteri(32868, 0, 4096);
			
			if (j != 0) {
				return i;
			}
		}
		
		return -1;
	}
	
	public static void stopIntegratedServer() {
		
		if (instance != null) {
			IntegratedServer integratedserver = instance.getIntegratedServer();
			
			if (integratedserver != null) {
				integratedserver.stopServer();
			}
		}
	}
	
	/**
	 * Gets the system time in milliseconds.
	 */
	public static long getSystemTime() {
		
		return Sys.getTime() * 1000L / Sys.getTimerResolution();
	}
	
	public static int getDebugFPS() {
		
		return debugFPS;
	}
	
	public void run() {
		
		running = true;
		
		try {
			init();
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Initializing game");
			crashreport.makeCategory("Initialization");
			displayCrashReport(addGraphicsAndWorldToCrashReport(crashreport));
			return;
		}
		
		while (true) {
			try {
				while (running) {
					if (!hasCrashed || crashReporter == null) {
						try {
							runGameLoop();
						} catch (OutOfMemoryError var10) {
							freeMemory();
							displayGuiScreen(new GuiMemoryErrorScreen());
							System.gc();
						}
					} else {
						displayCrashReport(crashReporter);
					}
				}
			} catch (MinecraftError var12) {
				break;
			} catch (ReportedException reportedexception) {
				addGraphicsAndWorldToCrashReport(reportedexception.getCrashReport());
				freeMemory();
				LOGGER.fatal("Reported exception thrown!", reportedexception);
				displayCrashReport(reportedexception.getCrashReport());
				break;
			} catch (Throwable throwable1) {
				CrashReport crashreport1 = addGraphicsAndWorldToCrashReport(new CrashReport("Unexpected error", throwable1));
				freeMemory();
				LOGGER.fatal("Unreported exception thrown!", throwable1);
				displayCrashReport(crashreport1);
				break;
			} finally {
				shutdownMinecraftApplet();
			}
		}
	}
	
	/**
	 * Starts the game: initializes the canvas, the title, the settings, etcetera.
	 */
	private void init() throws LWJGLException {
		
		gameSettings = new GameSettings(this, mcDataDir);
		creativeSettings = new CreativeSettings(this, mcDataDir);
		defaultResourcePacks.add(mcDefaultResourcePack);
		startTimerHackThread();
		
		if (gameSettings.overrideHeight > 0 && gameSettings.overrideWidth > 0) {
			displayWidth = gameSettings.overrideWidth;
			displayHeight = gameSettings.overrideHeight;
		}
		
		LOGGER.info("LWJGL Version: {}", Sys.getVersion());
		setWindowIcon();
		setInitialDisplayMode();
		createDisplay();
		OpenGlHelper.initializeTextures();
		framebufferMc = new Framebuffer(displayWidth, displayHeight, true);
		framebufferMc.setFramebufferColor(0F, 0F, 0F, 0F);
		registerMetadataSerializers();
		mcResourcePackRepository = new ResourcePackRepository(fileResourcepacks, new File(mcDataDir, "server-resource-packs"), mcDefaultResourcePack, metadataSerializer, gameSettings);
		mcResourceManager = new SimpleReloadableResourceManager(metadataSerializer);
		mcLanguageManager = new LanguageManager(metadataSerializer, gameSettings.language);
		mcResourceManager.registerReloadListener(mcLanguageManager);
		scaledResolution = new ScaledResolution(this);
		refreshResources();
		renderEngine = new TextureManager(mcResourceManager);
		mcResourceManager.registerReloadListener(renderEngine);
		drawSplashScreen(renderEngine);
		skinManager = new SkinManager(renderEngine, new File(fileAssets, "skins"), sessionService);
		saveLoader = new AnvilSaveConverter(new File(mcDataDir, "saves"), dataFixer);
		mcSoundHandler = new SoundHandler(mcResourceManager, gameSettings);
		mcResourceManager.registerReloadListener(mcSoundHandler);
		mcMusicTicker = new MusicTicker(this);
		fontRenderer = new FontRenderer(gameSettings, new ResourceLocation("textures/font/ascii.png"), renderEngine, false);
		
		if (gameSettings.language != null) {
			fontRenderer.setUnicodeFlag(isUnicode());
			fontRenderer.setBidiFlag(mcLanguageManager.isCurrentLanguageBidirectional());
		}
		
		standardGalacticFontRenderer = new FontRenderer(gameSettings, new ResourceLocation("textures/font/ascii_sga.png"), renderEngine, false);
		mcResourceManager.registerReloadListener(fontRenderer);
		mcResourceManager.registerReloadListener(standardGalacticFontRenderer);
		mcResourceManager.registerReloadListener(new GrassColorReloadListener());
		mcResourceManager.registerReloadListener(new FoliageColorReloadListener());
		mouseHelper = new MouseHelper();
		checkGLError("Pre startup");
		GlStateManager.enableTexture2D();
		GlStateManager.shadeModel(7425);
		GlStateManager.clearDepth(1D);
		GlStateManager.enableDepth();
		GlStateManager.depthFunc(515);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.cullFace(GlStateManager.CullFace.BACK);
		GlStateManager.matrixMode(5889);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(5888);
		checkGLError("Startup");
		textureMapBlocks = new TextureMap("textures");
		textureMapBlocks.setMipmapLevels(gameSettings.mipmapLevels);
		renderEngine.loadTickableTexture(TextureMap.LOCATION_BLOCKS_TEXTURE, textureMapBlocks);
		renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		textureMapBlocks.setBlurMipmapDirect(false, gameSettings.mipmapLevels > 0);
		modelManager = new ModelManager(textureMapBlocks);
		mcResourceManager.registerReloadListener(modelManager);
		blockColors = BlockColors.init();
		itemColors = ItemColors.init(blockColors);
		renderItem = new RenderItem(renderEngine, modelManager, itemColors);
		renderManager = new RenderManager(renderEngine, renderItem);
		itemRenderer = new ItemRenderer(this);
		mcResourceManager.registerReloadListener(renderItem);
		entityRenderer = new EntityRenderer(this, mcResourceManager);
		mcResourceManager.registerReloadListener(entityRenderer);
		blockRenderDispatcher = new BlockRendererDispatcher(modelManager.getBlockModelShapes(), blockColors);
		mcResourceManager.registerReloadListener(blockRenderDispatcher);
		renderGlobal = new RenderGlobal(this);
		mcResourceManager.registerReloadListener(renderGlobal);
		populateSearchTreeManager();
		mcResourceManager.registerReloadListener(searchTreeManager);
		GlStateManager.viewport(0, 0, displayWidth, displayHeight);
		effectRenderer = new ParticleManager(world, renderEngine);
		checkGLError("Post startup");
		ingameGUI = new GuiIngame(this);
		
		if (serverName != null) {
			displayGuiScreen(new GuiConnecting(new GuiMainMenu(), this, serverName, serverPort));
		} else {
			displayGuiScreen(new GuiMainMenu());
		}
		
		renderEngine.deleteTexture(mojangLogo);
		mojangLogo = null;
		loadingScreen = new LoadingScreenRenderer(this);
		debugRenderer = new DebugRenderer(this);
		
		if (gameSettings.fullScreen && !fullscreen) {
			toggleFullscreen();
		}
		
		try {
			Display.setVSyncEnabled(gameSettings.enableVsync);
		} catch (OpenGLException var2) {
			gameSettings.enableVsync = false;
			gameSettings.saveOptions();
		}
		
		renderGlobal.makeEntityOutlineShader();
	}
	
	/**
	 * Fills {@link #searchTreeManager} with the current item and recipe registry contents.
	 */
	private void populateSearchTreeManager() {
		
		SearchTree<ItemStack> itemStackSearchTree = new SearchTree<>(
				stack ->
						stack.getTooltip(null, ITooltipFlag.TooltipFlags.NORMAL)
						     .stream()
						     .map(TextFormatting::getTextWithoutFormattingCodes)
						     .map(String::trim)
						     .filter(name -> !name.isEmpty())
						     .collect(Collectors.toList()),
				stack -> Collections.singleton(Item.REGISTRY.getNameForObject(stack.getItem()))
		);
		
		NonNullList<ItemStack> nonNullItemStacks = NonNullList.create();
		
		for (Item item : Item.REGISTRY) {
			item.getSubItems(CreativeTabs.SEARCH, nonNullItemStacks);
		}
		
		nonNullItemStacks.forEach(itemStackSearchTree::add);
		SearchTree<RecipeList> recipeListSearchTree = new SearchTree<>(
				recipeList -> recipeList.getRecipes()
				                        .stream()
				                        .flatMap(recipe -> recipe.getRecipeOutput()
				                                                 .getTooltip(null, ITooltipFlag.TooltipFlags.NORMAL)
				                                                 .stream())
				                        .map(TextFormatting::getTextWithoutFormattingCodes)
				                        .map(String::trim)
				                        .filter(name -> !name.isEmpty()).collect(Collectors.toList()),
				recipeList -> recipeList.getRecipes()
				                        .stream()
				                        .map(recipe -> Item.REGISTRY.getNameForObject(recipe.getRecipeOutput()
				                                                                            .getItem()))
				                        .collect(Collectors.toList())
		);
		RecipeBookClient.ALL_RECIPES.forEach(recipeListSearchTree::add);
		searchTreeManager.register(SearchTreeManager.ITEMS, itemStackSearchTree);
		searchTreeManager.register(SearchTreeManager.RECIPES, recipeListSearchTree);
	}
	
	private void registerMetadataSerializers() {
		
		metadataSerializer.registerMetadataSectionType(new TextureMetadataSectionSerializer(), TextureMetadataSection.class);
		metadataSerializer.registerMetadataSectionType(new FontMetadataSectionSerializer(), FontMetadataSection.class);
		metadataSerializer.registerMetadataSectionType(new AnimationMetadataSectionSerializer(), AnimationMetadataSection.class);
		metadataSerializer.registerMetadataSectionType(new PackMetadataSectionSerializer(), PackMetadataSection.class);
		metadataSerializer.registerMetadataSectionType(new LanguageMetadataSectionSerializer(), LanguageMetadataSection.class);
	}
	
	private void createDisplay() throws LWJGLException {
		
		Display.setResizable(true);
		Display.setTitle("Minecraft 1.12.2");
		
		try {
			Display.create((new PixelFormat()).withDepthBits(24));
		} catch (LWJGLException lwjglexception) {
			LOGGER.error("Couldn't set pixel format", lwjglexception);
			
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException ignored) {
			}
			
			if (fullscreen) {
				updateDisplayMode();
			}
			
			Display.create();
		}
	}
	
	private void setInitialDisplayMode() throws LWJGLException {
		
		if (fullscreen) {
			Display.setFullscreen(true);
			DisplayMode displaymode = Display.getDisplayMode();
			displayWidth = Math.max(1, displaymode.getWidth());
			displayHeight = Math.max(1, displaymode.getHeight());
		} else {
			Display.setDisplayMode(new DisplayMode(displayWidth, displayHeight));
		}
	}
	
	private void setWindowIcon() {
		
		Util.OS util$enumos = Util.getOSType();
		
		if (util$enumos != Util.OS.OSX) {
			InputStream inputstream = null;
			InputStream inputstream1 = null;
			
			try {
				inputstream = mcDefaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_16x16.png"));
				inputstream1 = mcDefaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_32x32.png"));
				
				if (inputstream != null && inputstream1 != null) {
					Display.setIcon(new ByteBuffer[]{readImageToBuffer(inputstream), readImageToBuffer(inputstream1)});
				}
			} catch (IOException ioexception) {
				LOGGER.error("Couldn't set icon", ioexception);
			} finally {
				IOUtils.closeQuietly(inputstream);
				IOUtils.closeQuietly(inputstream1);
			}
		}
	}
	
	public Framebuffer getFramebuffer() {
		
		return framebufferMc;
	}
	
	/**
	 * Gets the version that Minecraft was launched under (the name of a version JSON). Specified via the
	 * <code>--version</code> flag.
	 */
	public String getVersion() {
		
		return launchedVersion;
	}
	
	/**
	 * Gets the type of version that Minecraft was launched under (as specified in the version JSON). Specified via the
	 * <code>--versionType</code> flag.
	 */
	public String getVersionType() {
		
		return versionType;
	}
	
	private void startTimerHackThread() {
		
		Thread thread = new Thread("Timer hack thread") {
			public void run() {
				
				while (running) {
					try {
						Thread.sleep(2147483647L);
					} catch (InterruptedException ignored) {
					}
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
	}
	
	public void crashed(CrashReport crash) {
		
		hasCrashed = true;
		crashReporter = crash;
	}
	
	/**
	 * Wrapper around displayCrashReportInternal
	 */
	public void displayCrashReport(CrashReport crashReportIn) {
		
		File file1 = new File(getMinecraft().mcDataDir, "crash-reports");
		File file2 = new File(file1, "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-client.txt");
		Bootstrap.printToSYSOUT(crashReportIn.getCompleteReport());
		
		if (crashReportIn.getFile() != null) {
			Bootstrap.printToSYSOUT("#@!@# Game crashed! Crash report saved to: #@!@# " + crashReportIn.getFile());
			System.exit(-1);
		} else if (crashReportIn.saveToFile(file2)) {
			Bootstrap.printToSYSOUT("#@!@# Game crashed! Crash report saved to: #@!@# " + file2.getAbsolutePath());
			System.exit(-1);
		} else {
			Bootstrap.printToSYSOUT("#@?@# Game crashed! Crash report could not be saved. #@?@#");
			System.exit(-2);
		}
	}
	
	public boolean isUnicode() {
		
		return mcLanguageManager.isCurrentLocaleUnicode() || gameSettings.forceUnicodeFont;
	}
	
	public void refreshResources() {
		
		List<IResourcePack> list = Lists.newArrayList(defaultResourcePacks);
		
		if (integratedServer != null) {
			integratedServer.reload();
		}
		
		for (ResourcePackRepository.Entry resourcepackrepository$entry : mcResourcePackRepository.getRepositoryEntries()) {
			list.add(resourcepackrepository$entry.getResourcePack());
		}
		
		if (mcResourcePackRepository.getServerResourcePack() != null) {
			list.add(mcResourcePackRepository.getServerResourcePack());
		}
		
		try {
			mcResourceManager.reloadResources(list);
		} catch (RuntimeException runtimeexception) {
			LOGGER.info("Caught error stitching, removing all assigned resourcepacks", runtimeexception);
			list.clear();
			list.addAll(defaultResourcePacks);
			mcResourcePackRepository.setRepositories(Collections.emptyList());
			mcResourceManager.reloadResources(list);
			gameSettings.resourcePacks.clear();
			gameSettings.incompatibleResourcePacks.clear();
			gameSettings.saveOptions();
		}
		
		mcLanguageManager.parseLanguageMetadata(list);
		
		if (renderGlobal != null) {
			renderGlobal.loadRenderers();
		}
	}
	
	private ByteBuffer readImageToBuffer(InputStream imageStream) throws IOException {
		
		BufferedImage bufferedimage = ImageIO.read(imageStream);
		int[] aint = bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), null, 0, bufferedimage.getWidth());
		ByteBuffer bytebuffer = ByteBuffer.allocate(4 * aint.length);
		
		for (int i : aint) {
			bytebuffer.putInt(i << 8 | i >> 24 & 255);
		}
		
		bytebuffer.flip();
		return bytebuffer;
	}
	
	private void updateDisplayMode() throws LWJGLException {
		
		DisplayMode displayMode = Display.getDesktopDisplayMode();
		
		Display.setDisplayMode(displayMode);
		displayWidth = displayMode.getWidth();
		displayHeight = displayMode.getHeight();
	}
	
	private void drawSplashScreen(TextureManager textureManagerInstance) {
		
		int i = scaledResolution.getScaleFactor();
		Framebuffer framebuffer = new Framebuffer(scaledResolution.getScaledWidth() * i, scaledResolution.getScaledHeight() * i, true);
		framebuffer.bindFramebuffer(false);
		GlStateManager.matrixMode(5889);
		GlStateManager.loadIdentity();
		GlStateManager.ortho(0D, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), 0D, 1000D, 3000D);
		GlStateManager.matrixMode(5888);
		GlStateManager.loadIdentity();
		GlStateManager.translate(0F, 0F, -2000F);
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		GlStateManager.disableDepth();
		GlStateManager.enableTexture2D();
		InputStream inputstream = null;
		
		try {
			inputstream = mcDefaultResourcePack.getInputStream(LOCATION_MOJANG_PNG);
			mojangLogo = textureManagerInstance.getDynamicTextureLocation("logo", new DynamicTexture(ImageIO.read(inputstream)));
			textureManagerInstance.bindTexture(mojangLogo);
		} catch (IOException ioexception) {
			LOGGER.error("Unable to load logo: {}", LOCATION_MOJANG_PNG, ioexception);
		} finally {
			IOUtils.closeQuietly(inputstream);
		}
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.pos(0D, displayHeight, 0D).tex(0D, 0D).color(255, 255, 255, 255).endVertex();
		bufferbuilder.pos(displayWidth, displayHeight, 0D).tex(0D, 0D).color(255, 255, 255, 255).endVertex();
		bufferbuilder.pos(displayWidth, 0D, 0D).tex(0D, 0D).color(255, 255, 255, 255).endVertex();
		bufferbuilder.pos(0D, 0D, 0D).tex(0D, 0D).color(255, 255, 255, 255).endVertex();
		tessellator.draw();
		GlStateManager.color(1F, 1F, 1F, 1F);
		draw((scaledResolution.getScaledWidth() - 256) / 2, (scaledResolution.getScaledHeight() - 256) / 2, 0, 0, 256, 256, 255, 255, 255, 255);
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		framebuffer.unbindFramebuffer();
		framebuffer.framebufferRender(scaledResolution.getScaledWidth() * i, scaledResolution.getScaledHeight() * i);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);
		updateDisplay();
	}
	
	/**
	 * Draw with the WorldRenderer
	 */
	public void draw(int posX, int posY, int texU, int texV, int width, int height, int red, int green, int blue, int alpha) {
		
		BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.pos(posX, posY + height, 0D)
		             .tex((float) texU * 0.00390625F, (float) (texV + height) * 0.00390625F)
		             .color(red, green, blue, alpha)
		             .endVertex();
		bufferbuilder.pos(posX + width, posY + height, 0D)
		             .tex((float) (texU + width) * 0.00390625F, (float) (texV + height) * 0.00390625F)
		             .color(red, green, blue, alpha)
		             .endVertex();
		bufferbuilder.pos(posX + width, posY, 0D)
		             .tex((float) (texU + width) * 0.00390625F, (float) texV * 0.00390625F)
		             .color(red, green, blue, alpha)
		             .endVertex();
		bufferbuilder.pos(posX, posY, 0D)
		             .tex((float) texU * 0.00390625F, (float) texV * 0.00390625F)
		             .color(red, green, blue, alpha)
		             .endVertex();
		Tessellator.getInstance().draw();
	}
	
	/**
	 * Returns the save loader that is currently being used
	 */
	public ISaveFormat getSaveLoader() {
		
		return saveLoader;
	}
	
	/**
	 * Sets the argument GuiScreen as the main (topmost visible) screen.
	 *
	 * <p><strong>WARNING</strong>: This method is not thread-safe. Opening GUIs from a thread other than the main
	 * thread may cause many different issues, including the GUI being rendered before it has initialized (leading to
	 * unusual crashes). If on a thread other than the main thread, use {@link #addScheduledTask}:
	 *
	 * <pre>
	 * minecraft.addScheduledTask(() -> minecraft.displayGuiScreen(gui));
	 * </pre>
	 *
	 * @param guiScreenIn The {@link GuiScreen} to display. If it is {@code null}, any open GUI will be closed.
	 */
	public void displayGuiScreen(GuiScreen guiScreenIn) {
		
		if (currentScreen != null) {
			currentScreen.onGuiClosed();
		}
		
		if (guiScreenIn == null && world == null) {
			guiScreenIn = new GuiMainMenu();
		} else if (guiScreenIn == null && player.getHealth() <= 0F) {
			guiScreenIn = new GuiGameOver(null);
		}
		
		if (guiScreenIn instanceof GuiMainMenu || guiScreenIn instanceof GuiMultiplayer) {
			gameSettings.showDebugInfo = false;
			ingameGUI.getChatGUI().clearChatMessages(true);
		}
		
		currentScreen = guiScreenIn;
		
		if (guiScreenIn != null) {
			setIngameNotInFocus();
			KeyBinding.unPressAllKeys();
			
			while (Mouse.next()) {
			}
			
			while (Keyboard.next()) {
			}
			
			int i = scaledResolution.getScaledWidth();
			int j = scaledResolution.getScaledHeight();
			guiScreenIn.setWorldAndResolution(this, i, j);
			skipRenderWorld = false;
		} else {
			mcSoundHandler.resumeSounds();
			setIngameFocus();
		}
	}
	
	/**
	 * Checks for an OpenGL error. If there is one, prints the error ID and error string.
	 */
	private void checkGLError(String message) {
		
		int i = GlStateManager.glGetError();
		
		if (i != 0) {
			String s = GLU.gluErrorString(i);
			LOGGER.error("########## GL ERROR ##########");
			LOGGER.error("@ {}", message);
			LOGGER.error("{}: {}", i, s);
		}
	}
	
	/**
	 * Shuts down the minecraft applet by stopping the resource downloads, and clearing up GL stuff; called when the
	 * application (or web page) is exited.
	 */
	public void shutdownMinecraftApplet() {
		
		try {
			LOGGER.info("Stopping!");
			
			try {
				loadWorld(null);
			} catch (Throwable ignored) {
			}
			
			mcSoundHandler.unloadSounds();
		} finally {
			Display.destroy();
			
			if (!hasCrashed) {
				System.exit(0);
			}
		}
		
		System.gc();
	}
	
	/**
	 * Called repeatedly from run()
	 */
	private void runGameLoop() throws IOException {
		
		long i = System.nanoTime();
		profiler.startSection("root");
		
		if (Display.isCreated() && Display.isCloseRequested()) {
			shutdown();
		}
		
		timer.updateTimer();
		profiler.startSection("scheduledExecutables");
		
		synchronized (scheduledTasks) {
			while (!scheduledTasks.isEmpty()) {
				Util.runTask(scheduledTasks.poll(), LOGGER);
			}
		}
		
		profiler.endSection();
		long l = System.nanoTime();
		profiler.startSection("tick");
		
		for (int j = 0; j < Math.min(10, timer.elapsedTicks); ++j) {
			runTick();
		}
		
		profiler.endStartSection("preRenderErrors");
		long i1 = System.nanoTime() - l;
		checkGLError("Pre render");
		profiler.endStartSection("sound");
		mcSoundHandler.setListener(player, timer.renderPartialTicks);
		profiler.endSection();
		profiler.startSection("render");
		GlStateManager.pushMatrix();
		GlStateManager.clear(16640);
		framebufferMc.bindFramebuffer(true);
		profiler.startSection("display");
		GlStateManager.enableTexture2D();
		profiler.endSection();
		
		if (!skipRenderWorld) {
			profiler.endStartSection("gameRenderer");
			entityRenderer.updateCameraAndRender(isGamePaused ? renderPartialTicksPaused : timer.renderPartialTicks, i);
			profiler.endStartSection("toasts");
			toastGui.drawToast(scaledResolution);
			profiler.endSection();
		}
		
		profiler.endSection();
		
		if (gameSettings.showDebugInfo && gameSettings.showDebugProfilerChart && !gameSettings.hideGUI) {
			if (!profiler.profilingEnabled) {
				profiler.clearProfiling();
			}
			
			profiler.profilingEnabled = true;
			displayDebugInfo(i1);
		} else {
			profiler.profilingEnabled = false;
			prevFrameTime = System.nanoTime();
		}
		
		framebufferMc.unbindFramebuffer();
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		framebufferMc.framebufferRender(displayWidth, displayHeight);
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		entityRenderer.renderStreamIndicator(timer.renderPartialTicks);
		GlStateManager.popMatrix();
		profiler.startSection("root");
		updateDisplay();
		Thread.yield();
		checkGLError("Post render");
		++fpsCounter;
		boolean flag = isSingleplayer() && currentScreen != null && currentScreen.doesGuiPauseGame() && !integratedServer.getPublic();
		
		if (isGamePaused != flag) {
			if (isGamePaused) {
				renderPartialTicksPaused = timer.renderPartialTicks;
			} else {
				timer.renderPartialTicks = renderPartialTicksPaused;
			}
			
			isGamePaused = flag;
		}
		
		long k = System.nanoTime();
		frameTimer.addFrame(k - startNanoTime);
		startNanoTime = k;
		
		while (getSystemTime() >= debugUpdateTime + 1000L) {
			debugFPS = fpsCounter;
			debug = String.format("%d fps (%d chunk update%s) T: %s%s%s%s%s", debugFPS, RenderChunk.renderChunksUpdated, RenderChunk.renderChunksUpdated == 1 ? "" : "s", (float) gameSettings.limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() ? "inf" : gameSettings.limitFramerate, gameSettings.enableVsync ? " vsync" : "", gameSettings.fancyGraphics ? "" : " fast", gameSettings.clouds == 0 ? "" : (gameSettings.clouds == 1 ? " fast-clouds" : " fancy-clouds"), OpenGlHelper.useVbo() ? " vbo" : "");
			RenderChunk.renderChunksUpdated = 0;
			debugUpdateTime += 1000L;
			fpsCounter = 0;
			usageSnooper.addMemoryStatsToSnooper();
			
			if (!usageSnooper.isSnooperRunning()) {
				usageSnooper.startSnooper();
			}
		}
		
		if (isFramerateLimitBelowMax()) {
			profiler.startSection("fpslimit_wait");
			Display.sync(getLimitFramerate());
			profiler.endSection();
		}
		
		profiler.endSection();
	}
	
	public void updateDisplay() {
		
		profiler.startSection("display_update");
		Display.update();
		profiler.endSection();
		checkWindowResize();
	}
	
	protected void checkWindowResize() {
		
		if (!fullscreen && Display.wasResized()) {
			int i = displayWidth;
			int j = displayHeight;
			displayWidth = Display.getWidth();
			displayHeight = Display.getHeight();
			
			if (displayWidth != i || displayHeight != j) {
				if (displayWidth <= 0) {
					displayWidth = 1;
				}
				
				if (displayHeight <= 0) {
					displayHeight = 1;
				}
				
				resize(displayWidth, displayHeight);
			}
		}
	}
	
	public int getLimitFramerate() {
		
		return world == null && currentScreen != null ? 30 : gameSettings.limitFramerate;
	}
	
	public boolean isFramerateLimitBelowMax() {
		
		return (float) getLimitFramerate() < GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
	}
	
	/**
	 * Attempts to free as much memory as possible, including leaving the world and running the garbage collector.
	 */
	public void freeMemory() {
		
		try {
			memoryReserve = new byte[0];
			renderGlobal.deleteAllDisplayLists();
		} catch (Throwable ignored) {
		}
		
		try {
			System.gc();
			loadWorld(null);
		} catch (Throwable ignored) {
		}
		
		System.gc();
	}
	
	/**
	 * Update debugProfilerName in response to number keys in debug screen
	 */
	private void updateDebugProfilerName(int keyCount) {
		
		List<Profiler.Result> list = profiler.getProfilingData(debugProfilerName);
		
		if (!list.isEmpty()) {
			Profiler.Result profiler$result = list.removeFirst();
			
			if (keyCount == 0) {
				if (!profiler$result.profilerName.isEmpty()) {
					int i = debugProfilerName.lastIndexOf(46);
					
					if (i >= 0) {
						debugProfilerName = debugProfilerName.substring(0, i);
					}
				}
			} else {
				--keyCount;
				
				if (keyCount < list.size() && !"unspecified".equals((list.get(keyCount)).profilerName)) {
					if (!debugProfilerName.isEmpty()) {
						debugProfilerName = debugProfilerName + ".";
					}
					
					debugProfilerName = debugProfilerName + (list.get(keyCount)).profilerName;
				}
			}
		}
	}
	
	/**
	 * Parameter appears to be unused
	 */
	private void displayDebugInfo(long ignoredElapsedTicksTime) {
		
		if (profiler.profilingEnabled) {
			List<Profiler.Result> list = profiler.getProfilingData(debugProfilerName);
			Profiler.Result profiler$result = list.removeFirst();
			GlStateManager.clear(256);
			GlStateManager.matrixMode(5889);
			GlStateManager.enableColorMaterial();
			GlStateManager.loadIdentity();
			GlStateManager.ortho(0D, displayWidth, displayHeight, 0D, 1000D, 3000D);
			GlStateManager.matrixMode(5888);
			GlStateManager.loadIdentity();
			GlStateManager.translate(0F, 0F, -2000F);
			GlStateManager.glLineWidth(1F);
			GlStateManager.disableTexture2D();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			int i = 160;
			int j = displayWidth - 160 - 10;
			int k = displayHeight - 320;
			GlStateManager.enableBlend();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
			bufferbuilder.pos((float) j - 176F, (float) k - 96F - 16F, 0D).color(200, 0, 0, 0).endVertex();
			bufferbuilder.pos((float) j - 176F, k + 320, 0D).color(200, 0, 0, 0).endVertex();
			bufferbuilder.pos((float) j + 176F, k + 320, 0D).color(200, 0, 0, 0).endVertex();
			bufferbuilder.pos((float) j + 176F, (float) k - 96F - 16F, 0D).color(200, 0, 0, 0).endVertex();
			tessellator.draw();
			GlStateManager.disableBlend();
			double d0 = 0D;
			
			for (Profiler.Result profiler$result1 : list) {
				int i1 = MathHelper.floor(profiler$result1.usePercentage / 4D) + 1;
				bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
				int j1 = profiler$result1.getColor();
				int k1 = j1 >> 16 & 255;
				int l1 = j1 >> 8 & 255;
				int i2 = j1 & 255;
				bufferbuilder.pos(j, k, 0D).color(k1, l1, i2, 255).endVertex();
				
				for (int j2 = i1; j2 >= 0; --j2) {
					float f = (float) ((d0 + profiler$result1.usePercentage * (double) j2 / (double) i1) * (Math.PI * 2D) / 100D);
					float f1 = MathHelper.sin(f) * 160F;
					float f2 = MathHelper.cos(f) * 160F * 0.5F;
					bufferbuilder.pos((float) j + f1, (float) k - f2, 0D).color(k1, l1, i2, 255).endVertex();
				}
				
				tessellator.draw();
				bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);
				
				for (int i3 = i1; i3 >= 0; --i3) {
					float f3 = (float) ((d0 + profiler$result1.usePercentage * (double) i3 / (double) i1) * (Math.PI * 2D) / 100D);
					float f4 = MathHelper.sin(f3) * 160F;
					float f5 = MathHelper.cos(f3) * 160F * 0.5F;
					bufferbuilder.pos((float) j + f4, (float) k - f5, 0D)
					             .color(k1 >> 1, l1 >> 1, i2 >> 1, 255)
					             .endVertex();
					bufferbuilder.pos((float) j + f4, (float) k - f5 + 10F, 0D)
					             .color(k1 >> 1, l1 >> 1, i2 >> 1, 255)
					             .endVertex();
				}
				
				tessellator.draw();
				d0 += profiler$result1.usePercentage;
			}
			
			DecimalFormat decimalformat = new DecimalFormat("##0.00");
			GlStateManager.enableTexture2D();
			String s = "";
			
			if (!"unspecified".equals(profiler$result.profilerName)) {
				s = s + "[0] ";
			}
			
			if (profiler$result.profilerName.isEmpty()) {
				s = s + "ROOT ";
			} else {
				s = s + profiler$result.profilerName + ' ';
			}
			
			fontRenderer.drawStringWithShadow(s, (float) (j - 160), (float) (k - 80 - 16), 16777215);
			s = decimalformat.format(profiler$result.totalUsePercentage) + "%";
			fontRenderer.drawStringWithShadow(s, (float) (j + 160 - fontRenderer.getStringWidth(s)), (float) (k - 80 - 16), 16777215);
			
			for (int k2 = 0; k2 < list.size(); ++k2) {
				Profiler.Result profiler$result2 = list.get(k2);
				StringBuilder stringbuilder = new StringBuilder();
				
				if ("unspecified".equals(profiler$result2.profilerName)) {
					stringbuilder.append("[?] ");
				} else {
					stringbuilder.append("[").append(k2 + 1).append("] ");
				}
				
				String s1 = stringbuilder.append(profiler$result2.profilerName).toString();
				fontRenderer.drawStringWithShadow(s1, (float) (j - 160), (float) (k + 80 + k2 * 8 + 20), profiler$result2.getColor());
				s1 = decimalformat.format(profiler$result2.usePercentage) + "%";
				fontRenderer.drawStringWithShadow(s1, (float) (j + 160 - 50 - fontRenderer.getStringWidth(s1)), (float) (k + 80 + k2 * 8 + 20), profiler$result2.getColor());
				s1 = decimalformat.format(profiler$result2.totalUsePercentage) + "%";
				fontRenderer.drawStringWithShadow(s1, (float) (j + 160 - fontRenderer.getStringWidth(s1)), (float) (k + 80 + k2 * 8 + 20), profiler$result2.getColor());
			}
		}
	}
	
	/**
	 * Called when the window is closing. Sets 'running' to false which allows the game loop to exit cleanly.
	 */
	public void shutdown() {
		
		running = false;
	}
	
	/**
	 * Will set the focus to ingame if the Minecraft window is the active with focus. Also clears any GUI screen
	 * currently displayed
	 */
	public void setIngameFocus() {
		
		if (Display.isActive()) {
			if (!inGameHasFocus) {
				if (!IS_RUNNING_ON_MAC) {
					KeyBinding.updateKeyBindState();
				}
				
				inGameHasFocus = true;
				mouseHelper.grabMouseCursor();
				displayGuiScreen(null);
				leftClickCounter = 10000;
			}
		}
	}
	
	/**
	 * Resets the player keystate, disables the ingame focus, and ungrabs the mouse cursor.
	 */
	public void setIngameNotInFocus() {
		
		if (inGameHasFocus) {
			inGameHasFocus = false;
			mouseHelper.ungrabMouseCursor();
		}
	}
	
	/**
	 * Displays the ingame menu
	 */
	public void displayInGameMenu() {
		
		if (currentScreen == null) {
			displayGuiScreen(new GuiIngameMenu());
			
			if (isSingleplayer() && !integratedServer.getPublic()) {
				mcSoundHandler.pauseSounds();
			}
		}
	}
	
	private void sendClickBlockToController(boolean leftClick) {
		
		if (!leftClick) {
			leftClickCounter = 0;
		}
		
		if (leftClickCounter <= 0 && !player.isHandActive()) {
			if (leftClick && objectMouseOver != null && objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
				BlockPos blockpos = objectMouseOver.getBlockPos();
				
				if (world.getBlockState(blockpos)
				         .getMaterial() != Material.AIR && playerController.onPlayerDamageBlock(blockpos, objectMouseOver.sideHit)) {
					effectRenderer.addBlockHitEffects(blockpos, objectMouseOver.sideHit);
					player.swingArm(Hand.MAIN_HAND);
				}
			} else {
				playerController.resetBlockRemoving();
			}
		}
	}
	
	private void clickMouse() {
		
		if (leftClickCounter <= 0) {
			if (objectMouseOver == null) {
				LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
				
				if (playerController.isNotCreative()) {
					leftClickCounter = 10;
				}
			} else if (!player.isRowingBoat()) {
				switch (objectMouseOver.typeOfHit) {
					case ENTITY:
						playerController.attackEntity(player, objectMouseOver.entityHit);
						break;
					
					case BLOCK:
						BlockPos blockpos = objectMouseOver.getBlockPos();
						
						if (world.getBlockState(blockpos).getMaterial() != Material.AIR) {
							playerController.clickBlock(blockpos, objectMouseOver.sideHit);
							break;
						}
					
					case MISS:
						if (playerController.isNotCreative()) {
							leftClickCounter = 10;
						}
						
						player.resetCooldown();
				}
				
				player.swingArm(Hand.MAIN_HAND);
			}
		}
	}
	
	/*
	  Called when user clicked he's mouse right button (place)
	 */
	private void rightClickMouse() {
		
		if (!playerController.getIsHittingBlock()) {
			rightClickDelayTimer = 4;
			
			if (!player.isRowingBoat()) {
				if (objectMouseOver == null) {
					LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
				}
				
				for (Hand enumhand : Hand.values()) {
					ItemStack itemstack = player.getHeldItem(enumhand);
					
					if (objectMouseOver != null) {
						switch (objectMouseOver.typeOfHit) {
							case ENTITY:
								if (playerController.interactWithEntity(player, objectMouseOver.entityHit, objectMouseOver, enumhand) == ActionResult.SUCCESS) {
									return;
								}
								
								if (playerController.interactWithEntity(player, objectMouseOver.entityHit, enumhand) == ActionResult.SUCCESS) {
									return;
								}
								
								break;
							
							case BLOCK:
								BlockPos blockpos = objectMouseOver.getBlockPos();
								
								if (world.getBlockState(blockpos).getMaterial() != Material.AIR) {
									int i = itemstack.getCount();
									ActionResult enumactionresult = playerController.processRightClickBlock(player, world, blockpos, objectMouseOver.sideHit, objectMouseOver.hitVec, enumhand);
									
									if (enumactionresult == ActionResult.SUCCESS) {
										player.swingArm(enumhand);
										
										if (!itemstack.isEmpty() && (itemstack.getCount() != i || playerController.isInCreativeMode())) {
											entityRenderer.itemRenderer.resetEquippedProgress(enumhand);
										}
										
										return;
									}
								}
						}
					}
					
					if (!itemstack.isEmpty() && playerController.processRightClick(player, world, enumhand) == ActionResult.SUCCESS) {
						entityRenderer.itemRenderer.resetEquippedProgress(enumhand);
						return;
					}
				}
			}
		}
	}
	
	/**
	 * Toggles fullscreen mode.
	 */
	public void toggleFullscreen() {
		
		try {
			fullscreen = !fullscreen;
			gameSettings.fullScreen = fullscreen;
			
			if (fullscreen) {
				updateDisplayMode();
				displayWidth = Display.getDisplayMode().getWidth();
				displayHeight = Display.getDisplayMode().getHeight();
				
			} else {
				Display.setDisplayMode(new DisplayMode(tempDisplayWidth, tempDisplayHeight));
				displayWidth = tempDisplayWidth;
				displayHeight = tempDisplayHeight;
				
			}
			if (displayWidth <= 0) {
				displayWidth = 1;
			}
			if (displayHeight <= 0) {
				displayHeight = 1;
			}
			
			if (currentScreen != null) {
				resize(displayWidth, displayHeight);
			} else {
				updateFramebufferSize();
			}
			
			Display.setFullscreen(fullscreen);
			Display.setVSyncEnabled(gameSettings.enableVsync);
			updateDisplay();
		} catch (Exception exception) {
			LOGGER.error("Couldn't toggle fullscreen", exception);
		}
	}
	
	/**
	 * Called to resize the current screen.
	 */
	private void resize(int width, int height) {
		
		displayWidth = Math.max(1, width);
		displayHeight = Math.max(1, height);
		scaledResolution = new ScaledResolution(this);
		
		if (currentScreen != null) currentScreen.onResize(
				this,
				scaledResolution.getScaledWidth(),
				scaledResolution.getScaledHeight()
		);
		
		loadingScreen = new LoadingScreenRenderer(this);
		updateFramebufferSize();
	}
	
	private void updateFramebufferSize() {
		
		framebufferMc.createBindFramebuffer(displayWidth, displayHeight);
		
		if (entityRenderer != null) {
			entityRenderer.updateShaderGroupSize(displayWidth, displayHeight);
		}
	}
	
	/**
	 * Return the musicTicker's instance
	 */
	public MusicTicker getMusicTicker() {
		
		return mcMusicTicker;
	}
	
	/**
	 * Runs the current tick.
	 */
	public void runTick() throws IOException {
		
		if (rightClickDelayTimer > 0) {
			--rightClickDelayTimer;
		}
		
		profiler.startSection("gui");
		
		if (!isGamePaused) {
			ingameGUI.updateTick();
		}
		
		profiler.endSection();
		entityRenderer.getMouseOver(1F);
		tutorial.onMouseHover(world, objectMouseOver);
		profiler.startSection("gameMode");
		
		if (!isGamePaused && world != null) {
			playerController.updateController();
		}
		
		profiler.endStartSection("textures");
		
		if (world != null) {
			renderEngine.tick();
		}
		
		if (currentScreen == null && player != null) {
			if (player.getHealth() <= 0F && !(currentScreen instanceof GuiGameOver)) {
				displayGuiScreen(null);
			} else if (player.isPlayerSleeping() && world != null) {
				displayGuiScreen(new GuiSleepMP());
			}
		} else if (currentScreen != null && currentScreen instanceof GuiSleepMP && !player.isPlayerSleeping()) {
			displayGuiScreen(null);
		}
		
		if (currentScreen != null) {
			leftClickCounter = 10000;
		}
		
		if (currentScreen != null) {
			try {
				currentScreen.handleInput();
			} catch (Throwable throwable1) {
				CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Updating screen events");
				CrashReportCategory crashreportcategory = crashreport.makeCategory("Affected screen");
				crashreportcategory.addDetail("Screen name", () -> currentScreen.getClass().getCanonicalName());
				throw new ReportedException(crashreport);
			}
			
			if (currentScreen != null) {
				try {
					currentScreen.updateScreen();
				} catch (Throwable throwable) {
					CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Ticking screen");
					CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Affected screen");
					crashreportcategory1.addDetail("Screen name", () -> currentScreen.getClass().getCanonicalName());
					throw new ReportedException(crashreport1);
				}
			}
		}
		
		if (currentScreen == null || currentScreen.allowUserInput) {
			profiler.endStartSection("mouse");
			runTickMouse();
			
			if (leftClickCounter > 0) {
				--leftClickCounter;
			}
			
			profiler.endStartSection("keyboard");
			runTickKeyboard();
		}
		
		if (world != null) {
			if (player != null) {
				++joinPlayerCounter;
				
				if (joinPlayerCounter == 30) {
					joinPlayerCounter = 0;
					world.joinEntityInSurroundings(player);
				}
			}
			
			profiler.endStartSection("gameRenderer");
			
			if (!isGamePaused) {
				entityRenderer.updateRenderer();
			}
			
			profiler.endStartSection("levelRenderer");
			
			if (!isGamePaused) {
				renderGlobal.updateClouds();
			}
			
			profiler.endStartSection("level");
			
			if (!isGamePaused) {
				if (world.getLastLightningBolt() > 0) {
					world.setLastLightningBolt(world.getLastLightningBolt() - 1);
				}
				
				world.updateEntities();
			}
		} else if (entityRenderer.isShaderActive()) {
			entityRenderer.stopUseShader();
		}
		
		if (!isGamePaused) {
			mcMusicTicker.update();
			mcSoundHandler.update();
		}
		
		if (world != null) {
			if (!isGamePaused) {
				world.setAllowedSpawnTypes(world.getDifficulty() != Difficulty.PEACEFUL, true);
				tutorial.update();
				
				try {
					world.tick();
				} catch (Throwable throwable2) {
					CrashReport crashreport2 = CrashReport.makeCrashReport(throwable2, "Exception in world tick");
					
					if (world == null) {
						CrashReportCategory crashreportcategory2 = crashreport2.makeCategory("Affected level");
						crashreportcategory2.addCrashSection("Problem", "Level is null!");
					} else {
						world.addWorldInfoToCrashReport(crashreport2);
					}
					
					throw new ReportedException(crashreport2);
				}
			}
			
			profiler.endStartSection("animateTick");
			
			if (!isGamePaused && world != null) {
				world.doVoidFogParticles(MathHelper.floor(player.posX), MathHelper.floor(player.posY), MathHelper.floor(player.posZ));
			}
			
			profiler.endStartSection("particles");
			
			if (!isGamePaused) {
				effectRenderer.updateEffects();
			}
		} else if (myNetworkManager != null) {
			profiler.endStartSection("pendingConnection");
			myNetworkManager.processReceivedPackets();
		}
		
		profiler.endSection();
		systemTime = getSystemTime();
	}
	
	private void runTickKeyboard() throws IOException {
		
		while (Keyboard.next()) {
			int i = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
			
			if (debugCrashKeyPressTime > 0L) {
				if (getSystemTime() - debugCrashKeyPressTime >= 6000L) {
					throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
				}
				
				if (!Keyboard.isKeyDown(46) || !Keyboard.isKeyDown(61)) {
					debugCrashKeyPressTime = -1L;
				}
			} else if (Keyboard.isKeyDown(46) && Keyboard.isKeyDown(61)) {
				actionKeyF3 = true;
				debugCrashKeyPressTime = getSystemTime();
			}
			
			dispatchKeypresses();
			
			if (currentScreen != null) {
				currentScreen.handleKeyboardInput();
			}
			
			boolean flag = Keyboard.getEventKeyState();
			
			if (flag) {
				if (i == 62 && entityRenderer != null) {
					entityRenderer.switchUseShader();
				}
				
				boolean flag1 = false;
				
				if (currentScreen == null) {
					if (i == 1) {
						displayInGameMenu();
					}
					
					flag1 = Keyboard.isKeyDown(61) && processKeyF3(i);
					actionKeyF3 |= flag1;
					
					if (i == 59) {
						gameSettings.hideGUI = !gameSettings.hideGUI;
					}
				}
				
				if (flag1) {
					KeyBinding.setKeyBindState(i, false);
				} else {
					KeyBinding.setKeyBindState(i, true);
					KeyBinding.onTick(i);
				}
				
				if (gameSettings.showDebugProfilerChart) {
					if (i == 11) {
						updateDebugProfilerName(0);
					}
					
					for (int j = 0; j < 9; ++j) {
						if (i == 2 + j) {
							updateDebugProfilerName(j + 1);
						}
					}
				}
			} else {
				KeyBinding.setKeyBindState(i, false);
				
				if (i == 61) {
					if (actionKeyF3) {
						actionKeyF3 = false;
					} else {
						gameSettings.showDebugInfo = !gameSettings.showDebugInfo;
						gameSettings.showDebugProfilerChart = gameSettings.showDebugInfo && GuiScreen.isShiftKeyDown();
						gameSettings.showLagometer = gameSettings.showDebugInfo && GuiScreen.isAltKeyDown();
					}
				}
			}
		}
		
		processKeyBinds();
	}
	
	private boolean processKeyF3(int auxKey) {
		
		if (auxKey == 30) {
			renderGlobal.loadRenderers();
			debugFeedbackTranslated("debug.reload_chunks.message");
			return true;
		} else if (auxKey == 48) {
			boolean flag1 = !renderManager.isDebugBoundingBox();
			renderManager.setDebugBoundingBox(flag1);
			debugFeedbackTranslated(flag1 ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off");
			return true;
		} else if (auxKey == 32) {
			if (ingameGUI != null) {
				ingameGUI.getChatGUI().clearChatMessages(false);
			}
			
			return true;
		} else if (auxKey == 33) {
			gameSettings.setOptionValue(GameSettings.Options.RENDER_DISTANCE, GuiScreen.isShiftKeyDown() ? -1 : 1);
			debugFeedbackTranslated("debug.cycle_renderdistance.message", gameSettings.renderDistanceChunks);
			return true;
		} else if (auxKey == 34) {
			boolean flag = debugRenderer.toggleChunkBorders();
			debugFeedbackTranslated(flag ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
			return true;
		} else if (auxKey == 35) {
			gameSettings.advancedItemTooltips = !gameSettings.advancedItemTooltips;
			debugFeedbackTranslated(gameSettings.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
			gameSettings.saveOptions();
			return true;
		} else if (auxKey == 49) {
			if (!player.canUseCommand(2, "")) {
				debugFeedbackTranslated("debug.creative_spectator.error");
			} else if (player.isCreative()) {
				player.sendChatMessage("/gamemode spectator");
			} else if (player.isSpectator()) {
				player.sendChatMessage("/gamemode creative");
			}
			
			return true;
		} else if (auxKey == 25) {
			gameSettings.pauseOnLostFocus = !gameSettings.pauseOnLostFocus;
			gameSettings.saveOptions();
			debugFeedbackTranslated(gameSettings.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off");
			return true;
		} else if (auxKey == 16) {
			debugFeedbackTranslated("debug.help.message");
			GuiNewChat guinewchat = ingameGUI.getChatGUI();
			guinewchat.printChatMessage(new TextComponentTranslation("debug.reload_chunks.help"));
			guinewchat.printChatMessage(new TextComponentTranslation("debug.show_hitboxes.help"));
			guinewchat.printChatMessage(new TextComponentTranslation("debug.clear_chat.help"));
			guinewchat.printChatMessage(new TextComponentTranslation("debug.cycle_renderdistance.help"));
			guinewchat.printChatMessage(new TextComponentTranslation("debug.chunk_boundaries.help"));
			guinewchat.printChatMessage(new TextComponentTranslation("debug.advanced_tooltips.help"));
			guinewchat.printChatMessage(new TextComponentTranslation("debug.creative_spectator.help"));
			guinewchat.printChatMessage(new TextComponentTranslation("debug.pause_focus.help"));
			guinewchat.printChatMessage(new TextComponentTranslation("debug.help.help"));
			guinewchat.printChatMessage(new TextComponentTranslation("debug.reload_resourcepacks.help"));
			return true;
		} else if (auxKey == 20) {
			debugFeedbackTranslated("debug.reload_resourcepacks.message");
			refreshResources();
			return true;
		} else {
			return false;
		}
	}
	
	private void processKeyBinds() {
		
		for (; gameSettings.keyBindTogglePerspective.isPressed(); renderGlobal.setDisplayListEntitiesDirty()) {
			++gameSettings.thirdPersonView;
			
			if (gameSettings.thirdPersonView > 2) {
				gameSettings.thirdPersonView = 0;
			}
			
			if (gameSettings.thirdPersonView == 0) {
				entityRenderer.loadEntityShader(getRenderViewEntity());
			} else if (gameSettings.thirdPersonView == 1) {
				entityRenderer.loadEntityShader(null);
			}
		}
		
		while (gameSettings.keyBindSmoothCamera.isPressed()) {
			gameSettings.smoothCamera = !gameSettings.smoothCamera;
		}
		
		for (int i = 0; i < 9; ++i) {
			boolean flag = gameSettings.keyBindSaveToolbar.isKeyDown();
			boolean flag1 = gameSettings.keyBindLoadToolbar.isKeyDown();
			
			if (gameSettings.keyBindsHotbar[i].isPressed()) {
				if (player.isSpectator()) {
					ingameGUI.getSpectatorGui().onHotbarSelected(i);
				} else if (!player.isCreative() || currentScreen != null || !flag1 && !flag) {
					player.inventory.currentItem = i;
				} else {
					GuiContainerCreative.handleHotbarSnapshots(this, i, flag1, flag);
				}
			}
		}
		
		while (gameSettings.keyBindInventory.isPressed()) {
			if (playerController.isRidingHorse()) {
				player.sendHorseInventory();
			} else {
				tutorial.openInventory();
				displayGuiScreen(new GuiInventory(player));
			}
		}
		
		while (gameSettings.keyBindAdvancements.isPressed()) {
			displayGuiScreen(new GuiScreenAdvancements(player.connection.getAdvancementManager()));
		}
		
		while (gameSettings.keyBindSwapHands.isPressed()) {
			if (!player.isSpectator()) {
				getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, BlockPos.ORIGIN, Facing.DOWN));
			}
		}
		
		while (gameSettings.keyBindDrop.isPressed()) {
			if (!player.isSpectator()) {
				player.dropItem(GuiScreen.isCtrlKeyDown());
			}
		}
		
		boolean flag2 = gameSettings.chatVisibility != EntityPlayer.ChatVisibility.HIDDEN;
		
		if (flag2) {
			while (gameSettings.keyBindChat.isPressed()) {
				displayGuiScreen(new GuiChat());
			}
			
			if (currentScreen == null && gameSettings.keyBindCommand.isPressed()) {
				displayGuiScreen(new GuiChat("/"));
			}
		}
		
		if (player.isHandActive()) {
			if (!gameSettings.keyBindUseItem.isKeyDown()) {
				playerController.onStoppedUsingItem(player);
			}
			
			label109:
			
			while (true) {
				if (!gameSettings.keyBindAttack.isPressed()) {
					while (gameSettings.keyBindUseItem.isPressed()) {
					}
					
					while (true) {
						if (gameSettings.keyBindPickBlock.isPressed()) continue;
						
						break label109;
					}
				}
			}
		} else {
			while (gameSettings.keyBindAttack.isPressed()) {
				clickMouse();
			}
			
			while (gameSettings.keyBindUseItem.isPressed()) {
				rightClickMouse();
			}
			
			while (gameSettings.keyBindPickBlock.isPressed()) {
				middleClickMouse();
			}
		}
		
		if (gameSettings.keyBindUseItem.isKeyDown() && rightClickDelayTimer == 0 && !player.isHandActive()) {
			rightClickMouse();
		}
		
		sendClickBlockToController(currentScreen == null && gameSettings.keyBindAttack.isKeyDown() && inGameHasFocus);
	}
	
	private void runTickMouse() throws IOException {
		
		while (Mouse.next()) {
			int i = Mouse.getEventButton();
			KeyBinding.setKeyBindState(i - 100, Mouse.getEventButtonState());
			
			if (Mouse.getEventButtonState()) {
				if (player.isSpectator() && i == 2) {
					ingameGUI.getSpectatorGui().onMiddleClick();
				} else {
					KeyBinding.onTick(i - 100);
				}
			}
			
			long j = getSystemTime() - systemTime;
			
			if (j <= 200L) {
				int k = Mouse.getEventDWheel();
				
				if (k != 0) {
					if (player.isSpectator()) {
						k = k < 0 ? -1 : 1;
						
						if (ingameGUI.getSpectatorGui().isMenuActive()) {
							ingameGUI.getSpectatorGui().onMouseScroll(-k);
						} else {
							float f = MathHelper.clamp(player.capabilities.getFlySpeed() + (float) k * 0.005F, 0F, 0.2F);
							player.capabilities.setFlySpeed(f);
						}
					} else {
						player.inventory.changeCurrentItem(k);
					}
				}
				
				if (currentScreen == null) {
					if (!inGameHasFocus && Mouse.getEventButtonState()) {
						setIngameFocus();
					}
				} else if (currentScreen != null) {
					currentScreen.handleMouseInput();
				}
			}
		}
	}
	
	private void debugFeedbackTranslated(String untranslatedTemplate, Object... objs) {
		
		ingameGUI.getChatGUI()
		         .printChatMessage((new TextComponentString("")).appendSibling((new TextComponentTranslation("debug.prefix")).setStyle((new Style()).setColor(TextFormatting.YELLOW)
		                                                                                                                                            .setBold(true)))
		                                                        .appendText(" ")
		                                                        .appendSibling(new TextComponentTranslation(untranslatedTemplate, objs)));
	}
	
	/**
	 * Arguments: World foldername,  World ingame name, WorldSettings
	 */
	public void launchIntegratedServer(String folderName, String worldName, WorldSettings worldSettingsIn) {
		
		loadWorld(null);
		System.gc();
		ISaveHandler saveHandler = saveLoader.getSaveLoader(folderName, false);
		WorldInfo worldinfo = saveHandler.loadWorldInfo();
		
		if (worldinfo == null && worldSettingsIn != null) {
			worldinfo = new WorldInfo(worldSettingsIn, folderName);
			saveHandler.saveWorldInfo(worldinfo);
		}
		
		if (worldSettingsIn == null) worldSettingsIn = new WorldSettings(worldinfo);
		
		try {
			YggdrasilAuthenticationService yggdrasilauthenticationservice = new YggdrasilAuthenticationService(proxy, UUID.randomUUID()
			                                                                                                              .toString());
			MinecraftSessionService minecraftsessionservice = yggdrasilauthenticationservice.createMinecraftSessionService();
			GameProfileRepository gameprofilerepository = yggdrasilauthenticationservice.createProfileRepository();
			PlayerProfileCache playerprofilecache = new PlayerProfileCache(gameprofilerepository, new File(mcDataDir, MinecraftServer.USER_CACHE_FILE.getName()));
			TileEntitySkull.setProfileCache(playerprofilecache);
			TileEntitySkull.setSessionService(minecraftsessionservice);
			PlayerProfileCache.setOnlineMode(false);
			integratedServer = new IntegratedServer(this, folderName, worldName, worldSettingsIn, yggdrasilauthenticationservice, minecraftsessionservice, gameprofilerepository, playerprofilecache);
			integratedServer.startServerThread();
			integratedServerIsRunning = true;
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Starting integrated server");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Starting integrated server");
			crashreportcategory.addCrashSection("Level ID", folderName);
			crashreportcategory.addCrashSection("Level Name", worldName);
			throw new ReportedException(crashreport);
		}
		
		loadingScreen.displaySavingString(I18n.format("menu.loadingLevel"));
		
		while (!integratedServer.serverIsInRunLoop()) {
			String s = integratedServer.getUserMessage();
			
			if (s != null) {
				loadingScreen.displayLoadingString(I18n.format(s));
			} else {
				loadingScreen.displayLoadingString("");
			}
		}
		
		displayGuiScreen(new GuiScreenWorking());
		SocketAddress socketaddress = integratedServer.getNetworkSystem().addLocalEndpoint();
		NetworkManager networkmanager = NetworkManager.provideLocalClient(socketaddress);
		networkmanager.setNetHandler(new NetHandlerLoginClient(networkmanager, this, null));
		networkmanager.sendPacket(new C00Handshake(socketaddress.toString(), 0, ConnectionState.LOGIN));
		networkmanager.sendPacket(new CPacketLoginStart(getSession().getProfile()));
		myNetworkManager = networkmanager;
	}
	
	/**
	 * unloads the current world first
	 */
	public void loadWorld(WorldClient worldClientIn) {
		
		loadWorld(worldClientIn, "");
	}
	
	/**
	 * par2Str is displayed on the loading screen to the user unloads the current world first
	 */
	public void loadWorld(WorldClient worldClientIn, String loadingMessage) {
		
		if (worldClientIn == null) {
			NetHandlerPlayClient nethandlerplayclient = getConnection();
			
			if (nethandlerplayclient != null) {
				nethandlerplayclient.cleanup();
			}
			
			if (integratedServer != null && integratedServer.isAnvilFileSet()) {
				integratedServer.initiateShutdown();
			}
			
			integratedServer = null;
			entityRenderer.resetData();
			playerController = null;
			NarratorChatListener.INSTANCE.clear();
		}
		
		renderViewEntity = null;
		myNetworkManager = null;
		
		if (loadingScreen != null) {
			loadingScreen.resetProgressAndMessage(loadingMessage);
			loadingScreen.displayLoadingString("");
		}
		
		if (worldClientIn == null && world != null) {
			mcResourcePackRepository.clearResourcePack();
			ingameGUI.resetPlayersOverlayFooterHeader();
			setServerData(null);
			integratedServerIsRunning = false;
		}
		
		mcSoundHandler.stopSounds();
		world = worldClientIn;
		
		if (renderGlobal != null) {
			renderGlobal.setWorldAndLoadRenderers(worldClientIn);
		}
		
		if (effectRenderer != null) {
			effectRenderer.clearEffects(worldClientIn);
		}
		
		TileEntityRendererDispatcher.instance.setWorld(worldClientIn);
		
		if (worldClientIn != null) {
			if (!integratedServerIsRunning) {
				AuthenticationService authenticationservice = new YggdrasilAuthenticationService(proxy, UUID.randomUUID()
				                                                                                            .toString());
				MinecraftSessionService minecraftsessionservice = authenticationservice.createMinecraftSessionService();
				GameProfileRepository gameprofilerepository = authenticationservice.createProfileRepository();
				PlayerProfileCache playerprofilecache = new PlayerProfileCache(gameprofilerepository, new File(mcDataDir, MinecraftServer.USER_CACHE_FILE.getName()));
				TileEntitySkull.setProfileCache(playerprofilecache);
				TileEntitySkull.setSessionService(minecraftsessionservice);
				PlayerProfileCache.setOnlineMode(false);
			}
			
			if (player == null) {
				player = playerController.createPlayer(worldClientIn, new StatisticsManager(), new RecipeBookClient());
				playerController.flipPlayer(player);
			}
			
			player.preparePlayerToSpawn();
			worldClientIn.spawnEntity(player);
			player.movementInput = new MovementInputFromOptions(gameSettings);
			playerController.setPlayerCapabilities(player);
			renderViewEntity = player;
		} else {
			saveLoader.flushCache();
			player = null;
		}
		
		System.gc();
		systemTime = 0L;
	}
	
	public void setDimensionAndSpawnPlayer(int dimension) {
		
		world.setInitialSpawnLocation();
		world.removeAllEntities();
		int i = 0;
		String s = null;
		
		if (player != null) {
			i = player.getEntityId();
			world.removeEntity(player);
			s = player.getServerBrand();
		}
		
		renderViewEntity = null;
		EntityPlayerSP entityplayersp = player;
		player = playerController.createPlayer(world, player == null ? new StatisticsManager() : player.getStatFileWriter(), player == null ? new RecipeBook() : player.getRecipeBook());
		player.getDataManager().setEntryValues(entityplayersp.getDataManager().getAll());
		player.dimension = dimension;
		renderViewEntity = player;
		player.preparePlayerToSpawn();
		player.setServerBrand(s);
		world.spawnEntity(player);
		playerController.flipPlayer(player);
		player.movementInput = new MovementInputFromOptions(gameSettings);
		player.setEntityId(i);
		playerController.setPlayerCapabilities(player);
		player.setReducedDebug(entityplayersp.hasReducedDebug());
		
		if (currentScreen instanceof GuiGameOver) displayGuiScreen(null);
	}
	
	
	public NetHandlerPlayClient getConnection() {
		
		return player == null ? null : player.connection;
	}
	
	/**
	 * Called when user clicked he's mouse middle button (pick block)
	 */
	private void middleClickMouse() {
		
		if (objectMouseOver != null && objectMouseOver.typeOfHit != RayTraceResult.Type.MISS) {
			boolean flag = player.capabilities.isCreativeMode;
			TileEntity tileentity = null;
			ItemStack itemstack;
			
			if (objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
				BlockPos blockpos = objectMouseOver.getBlockPos();
				IBlockState iblockstate = world.getBlockState(blockpos);
				Block block = iblockstate.getBlock();
				
				if (iblockstate.getMaterial() == Material.AIR) {
					return;
				}
				
				itemstack = block.getItem(world, blockpos, iblockstate);
				
				if (itemstack.isEmpty()) {
					return;
				}
				
				if (flag && GuiScreen.isCtrlKeyDown() && block.hasTileEntity()) {
					tileentity = world.getTileEntity(blockpos);
				}
			} else {
				if (objectMouseOver.typeOfHit != RayTraceResult.Type.ENTITY || objectMouseOver.entityHit == null || !flag) {
					return;
				}
				
				switch (objectMouseOver.entityHit) {
					case EntityPainting ignored -> itemstack = new ItemStack(Items.PAINTING);
					case EntityLeashKnot ignored -> itemstack = new ItemStack(Items.LEAD);
					case EntityItemFrame entityitemframe -> {
						ItemStack itemstack1 = entityitemframe.getDisplayedItem();
						
						if (itemstack1.isEmpty()) {
							itemstack = new ItemStack(Items.ITEM_FRAME);
						} else {
							itemstack = itemstack1.copy();
						}
					}
					case EntityMinecart entityminecart -> {
						Item item1 = switch (entityminecart.getType()) {
							case FURNACE -> Items.FURNACE_MINECART;
							case CHEST -> Items.CHEST_MINECART;
							case TNT -> Items.TNT_MINECART;
							case HOPPER -> Items.HOPPER_MINECART;
							case COMMAND_BLOCK -> Items.COMMAND_BLOCK_MINECART;
							default -> Items.MINECART;
						};
						
						itemstack = new ItemStack(item1);
					}
					case EntityBoat entityBoat -> itemstack = new ItemStack(entityBoat.getItemBoat());
					case EntityArmorStand ignored -> itemstack = new ItemStack(Items.ARMOR_STAND);
					case EntityEnderCrystal ignored -> itemstack = new ItemStack(Items.END_CRYSTAL);
					default -> {
						ResourceLocation resourcelocation = EntityList.getKey(objectMouseOver.entityHit);
						
						if (resourcelocation == null || !EntityList.ENTITY_EGGS.containsKey(resourcelocation)) {
							return;
						}
						
						itemstack = new ItemStack(Items.SPAWN_EGG);
						ItemMonsterPlacer.applyEntityIdToItemStack(itemstack, resourcelocation);
					}
				}
			}
			
			if (itemstack.isEmpty()) {
				String s = "";
				
				if (objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
					s = Block.REGISTRY.getNameForObject(world.getBlockState(objectMouseOver.getBlockPos()).getBlock())
					                  .toString();
				} else if (objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY) {
					s = EntityList.getKey(objectMouseOver.entityHit).toString();
				}
				
				LOGGER.warn("Picking on: [{}] {} gave null item", objectMouseOver.typeOfHit, s);
			} else {
				InventoryPlayer inventoryplayer = player.inventory;
				
				if (tileentity != null) storeTEInStack(itemstack, tileentity);
				
				int i = inventoryplayer.getSlotFor(itemstack);
				
				if (flag) {
					inventoryplayer.setPickedItemStack(itemstack);
					playerController.sendSlotPacket(player.getHeldItem(Hand.MAIN_HAND), 36 + inventoryplayer.currentItem);
				} else if (i != -1) {
					if (InventoryPlayer.isHotbar(i)) {
						inventoryplayer.currentItem = i;
					} else {
						playerController.pickItem(i);
					}
				}
			}
		}
	}
	
	private void storeTEInStack(ItemStack stack, TileEntity te) {
		
		NBTTagCompound nbttagcompound = te.writeToNBT(new NBTTagCompound());
		
		if (stack.getItem() == Items.SKULL && nbttagcompound.hasKey("Owner")) {
			NBTTagCompound nbttagcompound2 = nbttagcompound.getCompoundTag("Owner");
			NBTTagCompound nbttagcompound3 = new NBTTagCompound();
			nbttagcompound3.setTag("SkullOwner", nbttagcompound2);
			stack.setTagCompound(nbttagcompound3);
		} else {
			stack.setTagInfo("BlockEntityTag", nbttagcompound);
			NBTTagCompound nbttagcompound1 = new NBTTagCompound();
			NBTTagList nbttaglist = new NBTTagList();
			nbttaglist.appendTag(new NBTTagString("(+NBT)"));
			nbttagcompound1.setTag("Lore", nbttaglist);
			stack.setTagInfo("display", nbttagcompound1);
		}
	}
	
	/**
	 * adds core server Info (GL version , Texture pack, isModded, type), and the worldInfo to the crash report
	 */
	public CrashReport addGraphicsAndWorldToCrashReport(CrashReport theCrash) {
		
		theCrash.getCategory().addDetail("Launched Version", () -> launchedVersion);
		theCrash.getCategory().addDetail("LWJGL", Sys::getVersion);
		theCrash.getCategory()
		        .addDetail("OpenGL", () -> GlStateManager.glGetString(7937) + " GL version " + GlStateManager.glGetString(7938) + ", " + GlStateManager.glGetString(7936));
		theCrash.getCategory().addDetail("GL Caps", OpenGlHelper::getLogText);
		theCrash.getCategory().addDetail("Using VBOs", () -> gameSettings.useVbo ? "Yes" : "No");
		theCrash.getCategory().addDetail("Is Modded", () -> {
			
			String s = ClientBrandRetriever.getClientModName();
			
			if (!"vanilla".equals(s)) {
				return "Definitely; Client brand changed to '" + s + "'";
			} else {
				return Minecraft.class.getSigners() == null ? "Very likely; Jar signature invalidated" : "Probably not. Jar signature remains and client brand is untouched.";
			}
		});
		theCrash.getCategory().addDetail("Type", () -> "Client (map_client.txt)");
		theCrash.getCategory().addDetail("Resource Packs", () -> {
			
			StringBuilder stringbuilder = new StringBuilder();
			
			for (String s : gameSettings.resourcePacks) {
				if (!stringbuilder.isEmpty()) {
					stringbuilder.append(", ");
				}
				
				stringbuilder.append(s);
				
				if (gameSettings.incompatibleResourcePacks.contains(s)) {
					stringbuilder.append(" (incompatible)");
				}
			}
			
			return stringbuilder.toString();
		});
		theCrash.getCategory().addDetail("Current Language", () -> mcLanguageManager.getCurrentLanguage().toString());
		theCrash.getCategory()
		        .addDetail("Profiler Position", () -> profiler.profilingEnabled ? profiler.getNameOfLastSection() : "N/A (disabled)");
		theCrash.getCategory().addDetail("CPU", OpenGlHelper::getCpu);
		
		if (world != null) {
			world.addWorldInfoToCrashReport(theCrash);
		}
		
		return theCrash;
	}
	
	public ListenableFuture<Object> scheduleResourcesRefresh() {
		
		return addScheduledTask(this::refreshResources);
	}
	
	public void addServerStatsToSnooper(Snooper playerSnooper) {
		
		playerSnooper.addClientStat("fps", debugFPS);
		playerSnooper.addClientStat("vsync_enabled", gameSettings.enableVsync);
		playerSnooper.addClientStat("display_frequency", Display.getDisplayMode().getFrequency());
		playerSnooper.addClientStat("display_type", fullscreen ? "fullscreen" : "windowed");
		playerSnooper.addClientStat("run_time", (MinecraftServer.getCurrentTimeMillis() - playerSnooper.getMinecraftStartTimeMillis()) / 60L * 1000L);
		playerSnooper.addClientStat("current_action", getCurrentAction());
		playerSnooper.addClientStat("language", gameSettings.language == null ? "en_us" : gameSettings.language);
		String s = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? "little" : "big";
		playerSnooper.addClientStat("endianness", s);
		playerSnooper.addClientStat("subtitles", gameSettings.showSubtitles);
		playerSnooper.addClientStat("touch", gameSettings.touchscreen ? "touch" : "mouse");
		playerSnooper.addClientStat("resource_packs", mcResourcePackRepository.getRepositoryEntries().size());
		int i = 0;
		
		for (ResourcePackRepository.Entry resourcepackrepository$entry : mcResourcePackRepository.getRepositoryEntries()) {
			playerSnooper.addClientStat("resource_pack[" + i++ + "]", resourcepackrepository$entry.getResourcePackName());
		}
		
		if (integratedServer != null && integratedServer.getPlayerUsageSnooper() != null) {
			playerSnooper.addClientStat("snooper_partner", integratedServer.getPlayerUsageSnooper().getUniqueID());
		}
	}
	
	/**
	 * Return the current action's name
	 */
	private String getCurrentAction() {
		
		if (integratedServer != null) {
			return integratedServer.getPublic() ? "hosting_lan" : "singleplayer";
		} else if (currentServerData != null) {
			return currentServerData.isOnLAN() ? "playing_lan" : "multiplayer";
		} else {
			return "out_of_game";
		}
	}
	
	public void addServerTypeToSnooper(Snooper playerSnooper) {
		
		playerSnooper.addStatToSnooper("opengl_version", GlStateManager.glGetString(7938));
		playerSnooper.addStatToSnooper("opengl_vendor", GlStateManager.glGetString(7936));
		playerSnooper.addStatToSnooper("client_brand", ClientBrandRetriever.getClientModName());
		playerSnooper.addStatToSnooper("launched_version", launchedVersion);
		ContextCapabilities contextcapabilities = GLContext.getCapabilities();
		playerSnooper.addStatToSnooper("gl_caps[ARB_arrays_of_arrays]", contextcapabilities.GL_ARB_arrays_of_arrays);
		playerSnooper.addStatToSnooper("gl_caps[ARB_base_instance]", contextcapabilities.GL_ARB_base_instance);
		playerSnooper.addStatToSnooper("gl_caps[ARB_blend_func_extended]", contextcapabilities.GL_ARB_blend_func_extended);
		playerSnooper.addStatToSnooper("gl_caps[ARB_clear_buffer_object]", contextcapabilities.GL_ARB_clear_buffer_object);
		playerSnooper.addStatToSnooper("gl_caps[ARB_color_buffer_float]", contextcapabilities.GL_ARB_color_buffer_float);
		playerSnooper.addStatToSnooper("gl_caps[ARB_compatibility]", contextcapabilities.GL_ARB_compatibility);
		playerSnooper.addStatToSnooper("gl_caps[ARB_compressed_texture_pixel_storage]", contextcapabilities.GL_ARB_compressed_texture_pixel_storage);
		playerSnooper.addStatToSnooper("gl_caps[ARB_compute_shader]", contextcapabilities.GL_ARB_compute_shader);
		playerSnooper.addStatToSnooper("gl_caps[ARB_copy_buffer]", contextcapabilities.GL_ARB_copy_buffer);
		playerSnooper.addStatToSnooper("gl_caps[ARB_copy_image]", contextcapabilities.GL_ARB_copy_image);
		playerSnooper.addStatToSnooper("gl_caps[ARB_depth_buffer_float]", contextcapabilities.GL_ARB_depth_buffer_float);
		playerSnooper.addStatToSnooper("gl_caps[ARB_compute_shader]", contextcapabilities.GL_ARB_compute_shader);
		playerSnooper.addStatToSnooper("gl_caps[ARB_copy_buffer]", contextcapabilities.GL_ARB_copy_buffer);
		playerSnooper.addStatToSnooper("gl_caps[ARB_copy_image]", contextcapabilities.GL_ARB_copy_image);
		playerSnooper.addStatToSnooper("gl_caps[ARB_depth_buffer_float]", contextcapabilities.GL_ARB_depth_buffer_float);
		playerSnooper.addStatToSnooper("gl_caps[ARB_depth_clamp]", contextcapabilities.GL_ARB_depth_clamp);
		playerSnooper.addStatToSnooper("gl_caps[ARB_depth_texture]", contextcapabilities.GL_ARB_depth_texture);
		playerSnooper.addStatToSnooper("gl_caps[ARB_draw_buffers]", contextcapabilities.GL_ARB_draw_buffers);
		playerSnooper.addStatToSnooper("gl_caps[ARB_draw_buffers_blend]", contextcapabilities.GL_ARB_draw_buffers_blend);
		playerSnooper.addStatToSnooper("gl_caps[ARB_draw_elements_base_vertex]", contextcapabilities.GL_ARB_draw_elements_base_vertex);
		playerSnooper.addStatToSnooper("gl_caps[ARB_draw_indirect]", contextcapabilities.GL_ARB_draw_indirect);
		playerSnooper.addStatToSnooper("gl_caps[ARB_draw_instanced]", contextcapabilities.GL_ARB_draw_instanced);
		playerSnooper.addStatToSnooper("gl_caps[ARB_explicit_attrib_location]", contextcapabilities.GL_ARB_explicit_attrib_location);
		playerSnooper.addStatToSnooper("gl_caps[ARB_explicit_uniform_location]", contextcapabilities.GL_ARB_explicit_uniform_location);
		playerSnooper.addStatToSnooper("gl_caps[ARB_fragment_layer_viewport]", contextcapabilities.GL_ARB_fragment_layer_viewport);
		playerSnooper.addStatToSnooper("gl_caps[ARB_fragment_program]", contextcapabilities.GL_ARB_fragment_program);
		playerSnooper.addStatToSnooper("gl_caps[ARB_fragment_shader]", contextcapabilities.GL_ARB_fragment_shader);
		playerSnooper.addStatToSnooper("gl_caps[ARB_fragment_program_shadow]", contextcapabilities.GL_ARB_fragment_program_shadow);
		playerSnooper.addStatToSnooper("gl_caps[ARB_framebuffer_object]", contextcapabilities.GL_ARB_framebuffer_object);
		playerSnooper.addStatToSnooper("gl_caps[ARB_framebuffer_sRGB]", contextcapabilities.GL_ARB_framebuffer_sRGB);
		playerSnooper.addStatToSnooper("gl_caps[ARB_geometry_shader4]", contextcapabilities.GL_ARB_geometry_shader4);
		playerSnooper.addStatToSnooper("gl_caps[ARB_gpu_shader5]", contextcapabilities.GL_ARB_gpu_shader5);
		playerSnooper.addStatToSnooper("gl_caps[ARB_half_float_pixel]", contextcapabilities.GL_ARB_half_float_pixel);
		playerSnooper.addStatToSnooper("gl_caps[ARB_half_float_vertex]", contextcapabilities.GL_ARB_half_float_vertex);
		playerSnooper.addStatToSnooper("gl_caps[ARB_instanced_arrays]", contextcapabilities.GL_ARB_instanced_arrays);
		playerSnooper.addStatToSnooper("gl_caps[ARB_map_buffer_alignment]", contextcapabilities.GL_ARB_map_buffer_alignment);
		playerSnooper.addStatToSnooper("gl_caps[ARB_map_buffer_range]", contextcapabilities.GL_ARB_map_buffer_range);
		playerSnooper.addStatToSnooper("gl_caps[ARB_multisample]", contextcapabilities.GL_ARB_multisample);
		playerSnooper.addStatToSnooper("gl_caps[ARB_multitexture]", contextcapabilities.GL_ARB_multitexture);
		playerSnooper.addStatToSnooper("gl_caps[ARB_occlusion_query2]", contextcapabilities.GL_ARB_occlusion_query2);
		playerSnooper.addStatToSnooper("gl_caps[ARB_pixel_buffer_object]", contextcapabilities.GL_ARB_pixel_buffer_object);
		playerSnooper.addStatToSnooper("gl_caps[ARB_seamless_cube_map]", contextcapabilities.GL_ARB_seamless_cube_map);
		playerSnooper.addStatToSnooper("gl_caps[ARB_shader_objects]", contextcapabilities.GL_ARB_shader_objects);
		playerSnooper.addStatToSnooper("gl_caps[ARB_shader_stencil_export]", contextcapabilities.GL_ARB_shader_stencil_export);
		playerSnooper.addStatToSnooper("gl_caps[ARB_shader_texture_lod]", contextcapabilities.GL_ARB_shader_texture_lod);
		playerSnooper.addStatToSnooper("gl_caps[ARB_shadow]", contextcapabilities.GL_ARB_shadow);
		playerSnooper.addStatToSnooper("gl_caps[ARB_shadow_ambient]", contextcapabilities.GL_ARB_shadow_ambient);
		playerSnooper.addStatToSnooper("gl_caps[ARB_stencil_texturing]", contextcapabilities.GL_ARB_stencil_texturing);
		playerSnooper.addStatToSnooper("gl_caps[ARB_sync]", contextcapabilities.GL_ARB_sync);
		playerSnooper.addStatToSnooper("gl_caps[ARB_tessellation_shader]", contextcapabilities.GL_ARB_tessellation_shader);
		playerSnooper.addStatToSnooper("gl_caps[ARB_texture_border_clamp]", contextcapabilities.GL_ARB_texture_border_clamp);
		playerSnooper.addStatToSnooper("gl_caps[ARB_texture_buffer_object]", contextcapabilities.GL_ARB_texture_buffer_object);
		playerSnooper.addStatToSnooper("gl_caps[ARB_texture_cube_map]", contextcapabilities.GL_ARB_texture_cube_map);
		playerSnooper.addStatToSnooper("gl_caps[ARB_texture_cube_map_array]", contextcapabilities.GL_ARB_texture_cube_map_array);
		playerSnooper.addStatToSnooper("gl_caps[ARB_texture_non_power_of_two]", contextcapabilities.GL_ARB_texture_non_power_of_two);
		playerSnooper.addStatToSnooper("gl_caps[ARB_uniform_buffer_object]", contextcapabilities.GL_ARB_uniform_buffer_object);
		playerSnooper.addStatToSnooper("gl_caps[ARB_vertex_blend]", contextcapabilities.GL_ARB_vertex_blend);
		playerSnooper.addStatToSnooper("gl_caps[ARB_vertex_buffer_object]", contextcapabilities.GL_ARB_vertex_buffer_object);
		playerSnooper.addStatToSnooper("gl_caps[ARB_vertex_program]", contextcapabilities.GL_ARB_vertex_program);
		playerSnooper.addStatToSnooper("gl_caps[ARB_vertex_shader]", contextcapabilities.GL_ARB_vertex_shader);
		playerSnooper.addStatToSnooper("gl_caps[EXT_bindable_uniform]", contextcapabilities.GL_EXT_bindable_uniform);
		playerSnooper.addStatToSnooper("gl_caps[EXT_blend_equation_separate]", contextcapabilities.GL_EXT_blend_equation_separate);
		playerSnooper.addStatToSnooper("gl_caps[EXT_blend_func_separate]", contextcapabilities.GL_EXT_blend_func_separate);
		playerSnooper.addStatToSnooper("gl_caps[EXT_blend_minmax]", contextcapabilities.GL_EXT_blend_minmax);
		playerSnooper.addStatToSnooper("gl_caps[EXT_blend_subtract]", contextcapabilities.GL_EXT_blend_subtract);
		playerSnooper.addStatToSnooper("gl_caps[EXT_draw_instanced]", contextcapabilities.GL_EXT_draw_instanced);
		playerSnooper.addStatToSnooper("gl_caps[EXT_framebuffer_multisample]", contextcapabilities.GL_EXT_framebuffer_multisample);
		playerSnooper.addStatToSnooper("gl_caps[EXT_framebuffer_object]", contextcapabilities.GL_EXT_framebuffer_object);
		playerSnooper.addStatToSnooper("gl_caps[EXT_framebuffer_sRGB]", contextcapabilities.GL_EXT_framebuffer_sRGB);
		playerSnooper.addStatToSnooper("gl_caps[EXT_geometry_shader4]", contextcapabilities.GL_EXT_geometry_shader4);
		playerSnooper.addStatToSnooper("gl_caps[EXT_gpu_program_parameters]", contextcapabilities.GL_EXT_gpu_program_parameters);
		playerSnooper.addStatToSnooper("gl_caps[EXT_gpu_shader4]", contextcapabilities.GL_EXT_gpu_shader4);
		playerSnooper.addStatToSnooper("gl_caps[EXT_multi_draw_arrays]", contextcapabilities.GL_EXT_multi_draw_arrays);
		playerSnooper.addStatToSnooper("gl_caps[EXT_packed_depth_stencil]", contextcapabilities.GL_EXT_packed_depth_stencil);
		playerSnooper.addStatToSnooper("gl_caps[EXT_paletted_texture]", contextcapabilities.GL_EXT_paletted_texture);
		playerSnooper.addStatToSnooper("gl_caps[EXT_rescale_normal]", contextcapabilities.GL_EXT_rescale_normal);
		playerSnooper.addStatToSnooper("gl_caps[EXT_separate_shader_objects]", contextcapabilities.GL_EXT_separate_shader_objects);
		playerSnooper.addStatToSnooper("gl_caps[EXT_shader_image_load_store]", contextcapabilities.GL_EXT_shader_image_load_store);
		playerSnooper.addStatToSnooper("gl_caps[EXT_shadow_funcs]", contextcapabilities.GL_EXT_shadow_funcs);
		playerSnooper.addStatToSnooper("gl_caps[EXT_shared_texture_palette]", contextcapabilities.GL_EXT_shared_texture_palette);
		playerSnooper.addStatToSnooper("gl_caps[EXT_stencil_clear_tag]", contextcapabilities.GL_EXT_stencil_clear_tag);
		playerSnooper.addStatToSnooper("gl_caps[EXT_stencil_two_side]", contextcapabilities.GL_EXT_stencil_two_side);
		playerSnooper.addStatToSnooper("gl_caps[EXT_stencil_wrap]", contextcapabilities.GL_EXT_stencil_wrap);
		playerSnooper.addStatToSnooper("gl_caps[EXT_texture_3d]", contextcapabilities.GL_EXT_texture_3d);
		playerSnooper.addStatToSnooper("gl_caps[EXT_texture_array]", contextcapabilities.GL_EXT_texture_array);
		playerSnooper.addStatToSnooper("gl_caps[EXT_texture_buffer_object]", contextcapabilities.GL_EXT_texture_buffer_object);
		playerSnooper.addStatToSnooper("gl_caps[EXT_texture_integer]", contextcapabilities.GL_EXT_texture_integer);
		playerSnooper.addStatToSnooper("gl_caps[EXT_texture_lod_bias]", contextcapabilities.GL_EXT_texture_lod_bias);
		playerSnooper.addStatToSnooper("gl_caps[EXT_texture_sRGB]", contextcapabilities.GL_EXT_texture_sRGB);
		playerSnooper.addStatToSnooper("gl_caps[EXT_vertex_shader]", contextcapabilities.GL_EXT_vertex_shader);
		playerSnooper.addStatToSnooper("gl_caps[EXT_vertex_weighting]", contextcapabilities.GL_EXT_vertex_weighting);
		playerSnooper.addStatToSnooper("gl_caps[gl_max_vertex_uniforms]", GlStateManager.glGetInteger(35658));
		GlStateManager.glGetError();
		playerSnooper.addStatToSnooper("gl_caps[gl_max_fragment_uniforms]", GlStateManager.glGetInteger(35657));
		GlStateManager.glGetError();
		playerSnooper.addStatToSnooper("gl_caps[gl_max_vertex_attribs]", GlStateManager.glGetInteger(34921));
		GlStateManager.glGetError();
		playerSnooper.addStatToSnooper("gl_caps[gl_max_vertex_texture_image_units]", GlStateManager.glGetInteger(35660));
		GlStateManager.glGetError();
		playerSnooper.addStatToSnooper("gl_caps[gl_max_texture_image_units]", GlStateManager.glGetInteger(34930));
		GlStateManager.glGetError();
		playerSnooper.addStatToSnooper("gl_caps[gl_max_array_texture_layers]", GlStateManager.glGetInteger(35071));
		GlStateManager.glGetError();
		playerSnooper.addStatToSnooper("gl_max_texture_size", getGLMaximumTextureSize());
		GameProfile gameprofile = session.getProfile();
		
		if (gameprofile != null && gameprofile.getId() != null) {
			playerSnooper.addStatToSnooper("uuid", Hashing.sha256()
			                                              .hashBytes(gameprofile.getId()
			                                                                    .toString()
			                                                                    .getBytes(StandardCharsets.ISO_8859_1))
			                                              .toString());
		}
	}
	
	/**
	 * Returns whether snooping is enabled or not.
	 */
	public boolean isSnooperEnabled() {
		
		return gameSettings.snooperEnabled;
	}
	
	/**
	 * Set the current ServerData instance.
	 */
	public void setServerData(ServerData serverDataIn) {
		
		currentServerData = serverDataIn;
	}
	
	
	public ServerData getCurrentServerData() {
		
		return currentServerData;
	}
	
	public boolean isIntegratedServerRunning() {
		
		return integratedServerIsRunning;
	}
	
	/**
	 * Returns true if there is only one player playing, and the current server is the integrated one.
	 */
	public boolean isSingleplayer() {
		
		return integratedServerIsRunning && integratedServer != null;
	}
	
	

	/*
	  Returns the currently running integrated server
	 */
	public IntegratedServer getIntegratedServer() {
		
		return integratedServer;
	}
	
	/**
	 * Returns the PlayerUsageSnooper instance.
	 */
	public Snooper getPlayerUsageSnooper() {
		
		return usageSnooper;
	}
	
	/**
	 * Returns whether we're in full screen or not.
	 */
	public boolean isFullScreen() {
		
		return fullscreen;
	}
	
	public Session getSession() {
		
		return session;
	}
	
	/**
	 * Return the player's GameProfile properties
	 */
	public PropertyMap getProfileProperties() {
		
		if (profileProperties.isEmpty()) {
			GameProfile gameprofile = getSessionService().fillProfileProperties(session.getProfile(), false);
			profileProperties.putAll(gameprofile.getProperties());
		}
		
		return profileProperties;
	}
	
	public Proxy getProxy() {
		
		return proxy;
	}
	
	public TextureManager getTextureManager() {
		
		return renderEngine;
	}
	
	public IResourceManager getResourceManager() {
		
		return mcResourceManager;
	}
	
	public ResourcePackRepository getResourcePackRepository() {
		
		return mcResourcePackRepository;
	}
	
	public LanguageManager getLanguageManager() {
		
		return mcLanguageManager;
	}
	
	public TextureMap getTextureMapBlocks() {
		
		return textureMapBlocks;
	}
	
	public boolean isGamePaused() {
		
		return isGamePaused;
	}
	
	public SoundHandler getSoundHandler() {
		
		return mcSoundHandler;
	}
	
	public MusicTicker.MusicType getAmbientMusicType() {
		
		if (currentScreen instanceof GuiWinGame) {
			return MusicTicker.MusicType.CREDITS;
		} else if (player != null) {
			if (player.world.provider instanceof WorldProviderHell) {
				return MusicTicker.MusicType.NETHER;
			} else if (player.world.provider instanceof WorldProviderEnd) {
				return ingameGUI.getBossOverlay()
				                .shouldPlayEndBossMusic() ? MusicTicker.MusicType.END_BOSS : MusicTicker.MusicType.END;
			} else {
				return player.capabilities.isCreativeMode && player.capabilities.allowFlying ? MusicTicker.MusicType.CREATIVE : MusicTicker.MusicType.GAME;
			}
		} else {
			return MusicTicker.MusicType.MENU;
		}
	}
	
	public void dispatchKeypresses() {
		
		int i = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
		
		if (i != 0 && !Keyboard.isRepeatEvent()) {
			if (!(currentScreen instanceof GuiControls) || ((GuiControls) currentScreen).time <= getSystemTime() - 20L) {
				if (Keyboard.getEventKeyState()) {
					if (i == gameSettings.keyBindFullscreen.getKeyCode()) {
						toggleFullscreen();
					} else if (i == gameSettings.keyBindScreenshot.getKeyCode()) {
						ingameGUI.getChatGUI()
						         .printChatMessage(ScreenShotHelper.saveScreenshot(mcDataDir, displayWidth, displayHeight, framebufferMc));
					} else if (i == 48 && GuiScreen.isCtrlKeyDown() && (currentScreen == null || currentScreen != null && !currentScreen.isFocused())) {
						gameSettings.setOptionValue(GameSettings.Options.NARRATOR, 1);
						
						if (currentScreen instanceof ScreenChatOptions) {
							((ScreenChatOptions) currentScreen).updateNarratorButton();
						}
					}
				}
			}
		}
	}
	
	public MinecraftSessionService getSessionService() {
		
		return sessionService;
	}
	
	public SkinManager getSkinManager() {
		
		return skinManager;
	}
	
	
	public Entity getRenderViewEntity() {
		
		return renderViewEntity;
	}
	
	public void setRenderViewEntity(Entity viewingEntity) {
		
		renderViewEntity = viewingEntity;
		entityRenderer.loadEntityShader(viewingEntity);
	}
	
	public <V> ListenableFuture<V> addScheduledTask(Callable<V> callable) {
		
		Objects.requireNonNull(callable);
		
		if (isCallingFromMinecraftThread()) {
			try {
				return Futures.immediateFuture(callable.call());
			} catch (Exception exception) {
				return Futures.immediateFailedFuture(exception);
			}
		} else {
			ListenableFutureTask<V> listenablefuturetask = ListenableFutureTask.create(callable);
			
			synchronized (scheduledTasks) {
				scheduledTasks.add(listenablefuturetask);
				return listenablefuturetask;
			}
		}
	}
	
	public ListenableFuture<Object> addScheduledTask(Runnable runnable) {
		
		Objects.requireNonNull(runnable);
		return addScheduledTask(Executors.callable(runnable));
	}
	
	public boolean isCallingFromMinecraftThread() {
		
		return Thread.currentThread() == mcThread;
	}
	
	public BlockRendererDispatcher getBlockRendererDispatcher() {
		
		return blockRenderDispatcher;
	}
	
	public RenderManager getRenderManager() {
		
		return renderManager;
	}
	
	public RenderItem getRenderItem() {
		
		return renderItem;
	}
	
	public ItemRenderer getItemRenderer() {
		
		return itemRenderer;
	}
	
	public <T> ISearchTree<T> getSearchTree(SearchTreeManager.Key<T> key) {
		
		return searchTreeManager.get(key);
	}
	
	/**
	 * Return the FrameTimer's instance
	 */
	public FrameTimer getFrameTimer() {
		
		return frameTimer;
	}
	
	public DataFixer getDataFixer() {
		
		return dataFixer;
	}
	
	public float getRenderPartialTicks() {
		
		return timer.renderPartialTicks;
	}
	
	public float getTickLength() {
		
		return timer.elapsedPartialTicks;
	}
	
	public BlockColors getBlockColors() {
		
		return blockColors;
	}
	
	/**
	 * Whether to use reduced debug info
	 */
	public boolean isReducedDebug() {
		
		return player != null && player.hasReducedDebug() || gameSettings.reducedDebugInfo;
	}
	
	public GuiToast getToastGui() {
		
		return toastGui;
	}
	
	public Tutorial getTutorial() {
		
		return tutorial;
	}
	
}
