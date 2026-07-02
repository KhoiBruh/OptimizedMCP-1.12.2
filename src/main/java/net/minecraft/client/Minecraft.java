package net.minecraft.client;

import com.google.common.collect.Lists;
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
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.advancements.AdvancementsScreen;
import net.minecraft.client.gui.chat.ChatScreen;
import net.minecraft.client.gui.chat.GuiNewChat;
import net.minecraft.client.gui.error.MemoryErrorScreen;
import net.minecraft.client.gui.game.*;
import net.minecraft.client.gui.inventory.CreativeContainerScreen;
import net.minecraft.client.gui.inventory.InventoryScreen;
import net.minecraft.client.gui.loading.WorkingScreen;
import net.minecraft.client.gui.menu.MainMenuScreen;
import net.minecraft.client.gui.menu.MultiplayerScreen;
import net.minecraft.client.gui.option.ControlsScreen;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.main.GameConfig;
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
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.*;
import net.minecraft.util.Timer;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Maths;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.Version;

import net.minecraft.client.renderer.NativeImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;

public class Minecraft implements IThreadListener {

	// Public static fields
	public static final long START_TIME = System.nanoTime();
	public static final long TIMER_RESOLUTION = 1_000_000_000L;

	public static final boolean IS_RUNNING_ON_MAC = Util.getOSType() == Util.OS.OSX;
	private static final ResourceLocation LOCATION_MOJANG_PNG = new ResourceLocation("textures/gui/title/mojang.png");
	private static final Logger LOGGER = LogManager.getLogger();
	public static byte[] memoryReserve = new byte[10485760];

	// Private static fields
	private static int debugFPS;
	private static Minecraft instance;

	public final FrameTimer frameTimer = new FrameTimer();
	public final File dataDir;
	public final Profiler profiler = new Profiler();
	private final DataFixer dataFixer;
	private final List<IResourcePack> defaultResourcePacks = new ArrayList<>();
	private final File assets;
	private final File resourcepacks;
	private final Window window;
	private final String version;
	private final String versionType;
	private final DefaultResourcePack defaultResourcePack;
	private final Thread mcThread = Thread.currentThread();
	private final MetadataSerializer metadataSerializer = new MetadataSerializer();
	private final PropertyMap profileProperties;
	private final Proxy proxy;
	private final Queue<FutureTask<?>> scheduledTasks = new ArrayDeque<>();
	private final SearchTreeManager searchTreeManager = new SearchTreeManager();
	private final Session session;
	private final MinecraftSessionService sessionService;
	private final int tempDisplayHeight;
	private final int tempDisplayWidth;
	private final Timer timer = new Timer(20F);
	private final GuiToast toastGui;

	// Public instance fields
	public CreativeSettings creativeSettings;
	public Screen currentScreen;
	public String debug = "";
	public DebugRenderer debugRenderer;
	public ParticleManager effectRenderer;
	public EntityRenderer entityRenderer;
	public FontRenderer fontRenderer;
	public GameSettings gameSettings;
	public GuiIngame ingameGUI;
	public boolean inGameHasFocus;
	public LoadingScreenRenderer loadingScreen;
	public RayTraceResult objectMouseOver;
	public EntityPlayerSP player;
	public PlayerControllerMP playerController;
	public Entity pointedEntity;
	public boolean renderChunksMany = true;
	public RenderGlobal renderGlobal;

	// Removed: scaledResolution field (merged into Window)
	public boolean skipRenderWorld;
	public FontRenderer sgaFontRenderer;
	public WorldClient world;

	// Package-private fields
	long prevFrameTime = -1L;
	volatile boolean running = true;
	long startNanoTime = System.nanoTime();
	long systemTime = getSystemTime();

	// Private instance fields

	private SkinManager skinManager;
	private ModelManager modelManager;
	private RenderManager renderManager;
	private LanguageManager languageManager;
	private IReloadableResourceManager resourceManager;

	private boolean actionKeyF3;
	private BlockColors blockColors;
	private BlockRendererDispatcher blockRenderDispatcher;
	private CrashReport crashReport;
	private ServerData currentServerData;
	private long debugCrashKeyPressTime = -1L;
	private String debugProfilerName = "root";
	private long debugUpdateTime = getSystemTime();
	private Framebuffer framebuffer;
	private int fpsCounter;
	private boolean crashed;
	private IntegratedServer integratedServer;
	private boolean integratedServerIsRunning;
	private boolean paused;
	private ItemColors itemColors;
	private ItemRenderer itemRenderer;
	private int joinedPlayers;
	private int leftClicks;
	private int rightClicks;
	private MusicTicker musicTicker;
	private ResourcePackRepository resourcePackRepo;
	private SoundHandler soundHandler;
	private ResourceLocation mojangLogo;
	private NetworkManager networkManager;
	private TextureManager renderEngine;
	private RenderItem renderItem;
	private float partialTicksPaused;
	private Entity renderViewEntity;
	private ISaveFormat saveLoader;
	private TextureMap blockTextures;

	public Minecraft(GameConfig gameConfig) {
		instance = this;
		dataDir = gameConfig.folder().dataDir();
		assets = gameConfig.folder().assetsDir();
		resourcepacks = gameConfig.folder().resourcePacksDir();
		version = gameConfig.game().version();
		versionType = gameConfig.game().type();
		profileProperties = gameConfig.user().profileProperties();
		defaultResourcePack = new DefaultResourcePack(gameConfig.folder().getAssetsIndex());
		proxy = gameConfig.user().proxy() == null ? Proxy.NO_PROXY : gameConfig.user().proxy();
		sessionService = new YggdrasilAuthenticationService(proxy, UUID.randomUUID()
		                                                               .toString()).createMinecraftSessionService();
		session = gameConfig.user().session();
		LOGGER.info("Setting user: {}", session.getUsername());
		LOGGER.debug("(Session ID is {})", session.getSessionID());
		int w = gameConfig.display().width();
		int h = gameConfig.display().height();
		boolean fs = gameConfig.display().fullscreen();
		window = new Window("Minecraft 1.12.2", w, h, fs);
		tempDisplayWidth = w;
		tempDisplayHeight = h;
		integratedServer = null;

				Locale.setDefault(Locale.ROOT);
		Bootstrap.register();
		TextComponentKeybind.displaySupplierFunction = KeyBinding::getDisplayString;
		dataFixer = DataFixesManager.createFixer();
		toastGui = new GuiToast(this);
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

	public static int getGLMaximumTextureSize() {
		for (int i = 16384; i > 0; i >>= 1) {
			GLS.texImage2D(32868, 0, 6408, i, i, 0, 6408, 5121, null);
			int j = GLS.getTexLevelParameteri(32868, 0, 4096);

			if (j != 0) return i;
		}

		return -1;
	}

	public static void stopIntegratedServer() {
		if (instance != null) {
			IntegratedServer server = instance.getIntegratedServer();

			if (server != null) server.stopServer();
		}
	}

	/**
	 * Gets the system time in milliseconds.
	 */
	public static long getSystemTime() {
		return (System.nanoTime() - START_TIME) * 1000L / TIMER_RESOLUTION;
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
			showCrashReport(addGraphicsAndWorldToCrashReport(crashreport));
			return;
		}

		while (true) {
			try {
				while (running) {
					if (!crashed || crashReport == null) {
						try {
							runGameLoop();
						} catch (OutOfMemoryError var10) {
							freeMemory();
							displayScreen(new MemoryErrorScreen());
							System.gc();
						}
					} else showCrashReport(crashReport);
				}
			} catch (MinecraftError var12) {
				break;
			} catch (ReportedException reportedexception) {
				addGraphicsAndWorldToCrashReport(reportedexception.getCrashReport());
				freeMemory();
				LOGGER.fatal("Reported exception thrown!", reportedexception);
				showCrashReport(reportedexception.getCrashReport());
				break;
			} catch (Throwable throwable1) {
				CrashReport crashreport1 = addGraphicsAndWorldToCrashReport(new CrashReport("Unexpected error", throwable1));
				freeMemory();
				LOGGER.fatal("Unreported exception thrown!", throwable1);
				showCrashReport(crashreport1);
				break;
			} finally {
				shutdownMinecraftApplet();
			}
		}
	}

	/**
	 * Starts the game: initializes the canvas, the title, the settings, etcetera.
	 */
	private void init() throws Exception {
		gameSettings = new GameSettings(this, dataDir);
		creativeSettings = new CreativeSettings(this, dataDir);
		defaultResourcePacks.add(defaultResourcePack);

		if (gameSettings.overrideHeight > 0 && gameSettings.overrideWidth > 0) {
			window.setWindowedSize(gameSettings.overrideWidth, gameSettings.overrideHeight);
		}

		LOGGER.info("LWJGL Version: {}", Version.getVersion());
		setWindowIcon();
		setInitialDisplayMode();
		createDisplay();
		OpenGlHelper.ini();
		framebuffer = new Framebuffer(window.getWidth(), window.getHeight(), true);
		framebuffer.setColor(0F, 0F, 0F, 0F);
		registerMetadataSerializers();
		resourcePackRepo = new ResourcePackRepository(resourcepacks, new File(dataDir, "server-resource-packs"), defaultResourcePack, metadataSerializer, gameSettings);
		resourceManager = new SimpleReloadableResourceManager(metadataSerializer);
		languageManager = new LanguageManager(metadataSerializer, gameSettings.language);
		resourceManager.registerReloadListener(languageManager);
		window.setGuiScale(gameSettings.guiScale, isUnicode());
		refreshResources();
		renderEngine = new TextureManager(resourceManager);
		resourceManager.registerReloadListener(renderEngine);
		drawSplashScreen(renderEngine);
		skinManager = new SkinManager(renderEngine, new File(assets, "skins"), sessionService);
		saveLoader = new AnvilSaveConverter(new File(dataDir, "saves"), dataFixer);
		soundHandler = new SoundHandler(resourceManager, gameSettings);
		resourceManager.registerReloadListener(soundHandler);
		musicTicker = new MusicTicker(this);
		fontRenderer = new FontRenderer(new ResourceLocation("textures/font/ascii.png"), renderEngine, false);

		if (gameSettings.language != null) {
			fontRenderer.setUnicode(isUnicode());
			fontRenderer.setBidi(languageManager.isCurrentLanguageBidirectional());
		}

		sgaFontRenderer = new FontRenderer(new ResourceLocation("textures/font/ascii_sga.png"), renderEngine, false);
		resourceManager.registerReloadListener(fontRenderer);
		resourceManager.registerReloadListener(sgaFontRenderer);
		resourceManager.registerReloadListener(new GrassColorReloadListener());
		resourceManager.registerReloadListener(new FoliageColorReloadListener());
		checkGLError("Pre startup");
		GLS.enableTexture2D();
		GLS.shadeModel(7425);
		GLS.clearDepth(1D);
		GLS.enableDepth();
		GLS.depthFunc(515);
		GLS.enableAlpha();
		GLS.alphaFunc(516, 0.1F);
		GLS.cullFace(GLS.CullFace.BACK);
		GLS.matrixMode(5889);
		GLS.loadIdentity();
		GLS.matrixMode(5888);
		checkGLError("Startup");
		blockTextures = new TextureMap("textures");
		blockTextures.setMipmapLevels(gameSettings.mipmapLevels);
		renderEngine.loadTickableTexture(TextureMap.LOCATION_BLOCKS_TEXTURE, blockTextures);
		renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		blockTextures.setBlurMipmapDirect(false, gameSettings.mipmapLevels > 0);
		modelManager = new ModelManager(blockTextures);
		resourceManager.registerReloadListener(modelManager);
		blockColors = BlockColors.init();
		itemColors = ItemColors.init(blockColors);
		renderItem = new RenderItem(renderEngine, modelManager, itemColors);
		renderManager = new RenderManager(renderEngine, renderItem);
		itemRenderer = new ItemRenderer(this);
		resourceManager.registerReloadListener(renderItem);
		entityRenderer = new EntityRenderer(this, resourceManager);
		resourceManager.registerReloadListener(entityRenderer);
		blockRenderDispatcher = new BlockRendererDispatcher(modelManager.getBlockModelShapes(), blockColors);
		resourceManager.registerReloadListener(blockRenderDispatcher);
		renderGlobal = new RenderGlobal(this);
		resourceManager.registerReloadListener(renderGlobal);
		populateSearchTreeManager();
		resourceManager.registerReloadListener(searchTreeManager);
		GLS.viewport(0, 0, window.getWidth(), window.getHeight());
		effectRenderer = new ParticleManager(world, renderEngine);
		checkGLError("Post startup");
		ingameGUI = new GuiIngame(this);

		displayScreen(new MainMenuScreen());

		renderEngine.deleteTexture(mojangLogo);
		mojangLogo = null;
		loadingScreen = new LoadingScreenRenderer(this);
		debugRenderer = new DebugRenderer(this);

		if (gameSettings.fullScreen && !window.isFullscreen()) toggleFullscreen();

		try {
			window.setVSync(gameSettings.enableVsync);
		} catch (RuntimeException var2) {
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
				     .map(TextFormat::getTextWithoutFormattingCodes)
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
			                        .flatMap(
				                        recipe -> recipe.getRecipeOutput()
				                                        .getTooltip(null, ITooltipFlag.TooltipFlags.NORMAL)
				                                        .stream()
			                        )
			                        .map(TextFormat::getTextWithoutFormattingCodes)
			                        .map(String::trim)
			                        .filter(name -> !name.isEmpty()).collect(Collectors.toList()),

			recipeList -> recipeList.getRecipes()
			                        .stream()
			                        .map(recipe -> Item.REGISTRY.getNameForObject(recipe.getRecipeOutput().getItem()))
			                        .collect(Collectors.toList())
		);
		RecipeBookClient.ALL_RECIPES.forEach(recipeListSearchTree::add);
		searchTreeManager.register(SearchTreeManager.ITEMS, itemStackSearchTree);
		searchTreeManager.register(SearchTreeManager.RECIPES, recipeListSearchTree);
	}

	private void registerMetadataSerializers() {
		metadataSerializer.registerMetadataSectionType(new TextureMetadataSectionSerializer(), TextureMetadataSection.class);
		metadataSerializer.registerMetadataSectionType(new AnimationMetadataSectionSerializer(), AnimationMetadataSection.class);
		metadataSerializer.registerMetadataSectionType(new PackMetadataSectionSerializer(), PackMetadataSection.class);
		metadataSerializer.registerMetadataSectionType(new LanguageMetadataSectionSerializer(), LanguageMetadataSection.class);
	}

	private void createDisplay() throws Exception {
		try {
			window.create(24);
		} catch (Exception lwjglexception) {
			LOGGER.error("Couldn't set pixel format", lwjglexception);

			try {
				Thread.sleep(1000L);
			} catch (InterruptedException ignored) {
			}

			if (window.isFullscreen()) updateDisplayMode();
			window.create(24);
		}
	}

	private void setInitialDisplayMode() {
		if (window.isFullscreen()) {
			window.setFullscreen(true);
			window.setWindowedSize(window.getWidth(), window.getHeight());
		} else {
			window.setWindowSize(window.getWidth(), window.getHeight());
		}
	}

	private void setWindowIcon() {
		Util.OS util$enumos = Util.getOSType();

		if (util$enumos != Util.OS.OSX) {
			try (InputStream inputstream = defaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_16x16.png"));
			     InputStream inputstream1 = defaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_32x32.png"))) {

				if (inputstream != null && inputstream1 != null) {
					window.setIcon(new ByteBuffer[]{readImageToBuffer(inputstream), readImageToBuffer(inputstream1)});
				}
			} catch (IOException ioexception) {
				LOGGER.error("Couldn't set icon", ioexception);
			}
		}
	}

	public Framebuffer getFramebuffer() {
		return framebuffer;
	}

	public Window getWindow() {
		return window;
	}

	/**
	 * Gets the version that Minecraft was launched under (the name of a version JSON). Specified via the
	 * <code>--version</code> flag.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Gets the type of version that Minecraft was launched under (as specified in the version JSON). Specified via the
	 * <code>--type</code> flag.
	 */
	public String getVersionType() {
		return versionType;
	}

	public void crashed(CrashReport crash) {
		crashed = true;
		crashReport = crash;
	}

	/**
	 * Wrapper around displayCrashReportInternal
	 */
	public void showCrashReport(CrashReport crash) {
		File report = new File(getMinecraft().dataDir, "crash-reports");
		File client = new File(report, "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-client.txt");
		Bootstrap.printToSYSOUT(crash.getCompleteReport());

		if (crash.getFile() != null) {
			Bootstrap.printToSYSOUT("#@!@# Game crashed! Crash report saved to: #@!@# " + crash.getFile());
			System.exit(-1);
		} else if (crash.saveToFile(client)) {
			Bootstrap.printToSYSOUT("#@!@# Game crashed! Crash report saved to: #@!@# " + client.getAbsolutePath());
			System.exit(-1);
		} else {
			Bootstrap.printToSYSOUT("#@?@# Game crashed! Crash report could not be saved. #@?@#");
			System.exit(-2);
		}
	}

	public boolean isUnicode() {
		return languageManager.isCurrentLocaleUnicode() || gameSettings.forceUnicodeFont;
	}

	public void refreshResources() {
		List<IResourcePack> list = Lists.newArrayList(defaultResourcePacks);

		if (integratedServer != null) integratedServer.reload();

		for (ResourcePackRepository.Entry entry : resourcePackRepo.getRepositoryEntries()) {
			list.add(entry.getResourcePack());
		}

		if (resourcePackRepo.getServerResourcePack() != null) {
			list.add(resourcePackRepo.getServerResourcePack());
		}

		try {
			resourceManager.reloadResources(list);
		} catch (RuntimeException runtimeexception) {
			LOGGER.info("Caught error stitching, removing all assigned resourcepacks", runtimeexception);
			list.clear();
			list.addAll(defaultResourcePacks);
			resourcePackRepo.setRepositories(Collections.emptyList());
			resourceManager.reloadResources(list);
			gameSettings.resourcePacks.clear();
			gameSettings.incompatibleResourcePacks.clear();
			gameSettings.saveOptions();
		}

		languageManager.parseLanguageMetadata(list);

		if (renderGlobal != null) renderGlobal.loadRenderers();
	}

	private ByteBuffer readImageToBuffer(InputStream imageStream) throws IOException {
		NativeImage bufferedimage = NativeImage.read(imageStream);
		ByteBuffer bytebuffer = ByteBuffer.allocate(bufferedimage.getBuffer().capacity());
		bytebuffer.put(bufferedimage.getBuffer());
		bytebuffer.flip();
		bufferedimage.close();
		return bytebuffer;
	}

	private void updateDisplayMode() {
		window.applyDesktopSize();
	}

	private void drawSplashScreen(TextureManager textureManagerInstance) {
		int i = window.getGuiScale();
		Framebuffer framebuffer = new Framebuffer(window.getScaledWidth() * i, window.getScaledHeight() * i, true);
		framebuffer.bind(false);
		GLS.matrixMode(5889);
		GLS.loadIdentity();
		GLS.ortho(0D, window.getScaledWidth(), window.getScaledHeight(), 0D, 1000D, 3000D);
		GLS.matrixMode(5888);
		GLS.loadIdentity();
		GLS.translate(0F, 0F, -2000F);
		GLS.disableLighting();
		GLS.disableFog();
		GLS.disableDepth();
		GLS.enableTexture2D();
		try (InputStream inputstream = defaultResourcePack.getInputStream(LOCATION_MOJANG_PNG)) {
			mojangLogo = textureManagerInstance.getDynamicTextureLocation("logo", new DynamicTexture(NativeImage.read(inputstream)));
			textureManagerInstance.bindTexture(mojangLogo);
		} catch (IOException ioexception) {
			LOGGER.error("Unable to load logo: {}", LOCATION_MOJANG_PNG, ioexception);
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.pos(0D, window.getHeight(), 0D).tex(0D, 0D).color(255, 255, 255, 255).endVertex();
		bufferbuilder.pos(window.getWidth(), window.getHeight(), 0D).tex(0D, 0D).color(255, 255, 255, 255).endVertex();
		bufferbuilder.pos(window.getWidth(), 0D, 0D).tex(0D, 0D).color(255, 255, 255, 255).endVertex();
		bufferbuilder.pos(0D, 0D, 0D).tex(0D, 0D).color(255, 255, 255, 255).endVertex();
		tessellator.draw();
		GLS.color(1F, 1F, 1F, 1F);
		draw((window.getScaledWidth() - 256) / 2, (window.getScaledHeight() - 256) / 2, 0, 0, 256, 256, 255, 255, 255, 255);
		GLS.disableLighting();
		GLS.disableFog();
		framebuffer.unbind();
		framebuffer.render(window.getScaledWidth() * i, window.getScaledHeight() * i);
		GLS.enableAlpha();
		GLS.alphaFunc(516, 0.1F);
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
	 * @param screen The {@link Screen} to display. If it is {@code null}, any open GUI will be closed.
	 */
	public void displayScreen(Screen screen) {
		if (currentScreen != null) currentScreen.close();

		if (screen == null && world == null) {
			screen = new MainMenuScreen();
		} else if (screen == null && player.getHealth() <= 0F) {
			screen = new GameOverScreen(null);
		}

		if (screen instanceof MainMenuScreen || screen instanceof MultiplayerScreen) {
			gameSettings.showDebugInfo = false;
			ingameGUI.getChatGUI().clearChatMessages(true);
		}

		currentScreen = screen;

		if (screen != null) {
			setIngameNotInFocus();
			KeyBinding.unPressAllKeys();

			while (Mouse.next()) ;
			while (Keyboard.next()) ;

			int i = window.getScaledWidth();
			int j = window.getScaledHeight();
			screen.setResolution(this, i, j);
			skipRenderWorld = false;
		} else {
			soundHandler.resumeSounds();
			setIngameFocus();
		}
	}

	/**
	 * Checks for an OpenGL error. If there is one, prints the error ID and error string.
	 */
	private void checkGLError(String message) {
		int i = GLS.getError();

		if (i != 0) {
			String s = Projection.getErrorString(i);
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

			soundHandler.unloadSounds();
		} finally {
			window.destroy();

			if (!crashed) {
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

		if (window.isCloseRequested()) shutdown();

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

		for (int j = 0; j < Math.min(10, timer.elapsedTicks); j++) tick();

		profiler.endStartSection("preRenderErrors");
		long i1 = System.nanoTime() - l;
		checkGLError("Pre render");
		profiler.endStartSection("sound");
		soundHandler.setListener(player, timer.renderPartialTicks);
		profiler.endSection();
		profiler.startSection("render");
		GLS.pushMatrix();
		GLS.clear(16640);
		framebuffer.bind(true);
		profiler.startSection("display");
		GLS.enableTexture2D();
		profiler.endSection();

		if (!skipRenderWorld) {
			profiler.endStartSection("gameRenderer");
			entityRenderer.updateCameraAndRender(paused ? partialTicksPaused : timer.renderPartialTicks, i);
			profiler.endStartSection("toasts");
			toastGui.drawToast();
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

		framebuffer.unbind();
		GLS.popMatrix();
		GLS.pushMatrix();
		framebuffer.render(window.getWidth(), window.getHeight());
		GLS.popMatrix();
		GLS.pushMatrix();
		entityRenderer.renderStreamIndicator(timer.renderPartialTicks);
		GLS.popMatrix();
		profiler.startSection("root");
		updateDisplay();
		Thread.yield();
		checkGLError("Post render");
		fpsCounter++;
		boolean flag = isSingleplayer() && currentScreen != null && currentScreen.pauseGame() && !integratedServer.getPublic();

		if (paused != flag) {
			if (paused) {
				partialTicksPaused = timer.renderPartialTicks;
			} else {
				timer.renderPartialTicks = partialTicksPaused;
			}

			paused = flag;
		}

		long k = System.nanoTime();
		frameTimer.addFrame(k - startNanoTime);
		startNanoTime = k;

		while (getSystemTime() >= debugUpdateTime + 1000L) {
			debugFPS = fpsCounter;
			debug = String.format("%d fps (%d chunk update%s) T: %s%s%s%s", debugFPS, RenderChunk.renderChunksUpdated, RenderChunk.renderChunksUpdated == 1 ? "" : "s", (float) gameSettings.limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getMax() ? "inf" : gameSettings.limitFramerate, gameSettings.enableVsync ? " vsync" : "", gameSettings.fancyGraphics ? "" : " fast", gameSettings.clouds == 0 ? "" : (gameSettings.clouds == 1 ? " fast-clouds" : " fancy-clouds"));
			RenderChunk.renderChunksUpdated = 0;
			debugUpdateTime += 1000L;
			fpsCounter = 0;
		}

		if (isFramerateLimitBelowMax()) {
			profiler.startSection("fpslimit_wait");
			window.sync(getLimitFramerate());
			profiler.endSection();
		}

		profiler.endSection();
	}

	public void updateDisplay() {
		profiler.startSection("display_update");
		window.update();
		profiler.endSection();
		checkWindowResize();
	}

	protected void checkWindowResize() {
		if (!window.isFullscreen() && window.isResized()) {
			resize(window.getWidth(), window.getHeight());
		}
	}

	public int getLimitFramerate() {
		return world == null && currentScreen != null ? 30 : gameSettings.limitFramerate;
	}

	public boolean isFramerateLimitBelowMax() {
		return getLimitFramerate() < GameSettings.Options.FRAMERATE_LIMIT.getMax();
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

					if (i >= 0) debugProfilerName = debugProfilerName.substring(0, i);
				}
			} else {
				keyCount--;

				if (keyCount < list.size() && !"unspecified".equals((list.get(keyCount)).profilerName)) {
					if (!debugProfilerName.isEmpty()) debugProfilerName = debugProfilerName + ".";

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
			GLS.clear(256);
			GLS.matrixMode(5889);
			GLS.enableColorMaterial();
			GLS.loadIdentity();
			GLS.ortho(0D, window.getWidth(), window.getHeight(), 0D, 1000D, 3000D);
			GLS.matrixMode(5888);
			GLS.loadIdentity();
			GLS.translate(0F, 0F, -2000F);
			GLS.lineWidth(1F);
			GLS.disableTexture2D();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			int j = window.getWidth() - 160 - 10;
			int k = window.getHeight() - 320;
			GLS.enableBlend();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
			bufferbuilder.pos((float) j - 176F, (float) k - 96F - 16F, 0D).color(200, 0, 0, 0).endVertex();
			bufferbuilder.pos((float) j - 176F, k + 320, 0D).color(200, 0, 0, 0).endVertex();
			bufferbuilder.pos((float) j + 176F, k + 320, 0D).color(200, 0, 0, 0).endVertex();
			bufferbuilder.pos((float) j + 176F, (float) k - 96F - 16F, 0D).color(200, 0, 0, 0).endVertex();
			tessellator.draw();
			GLS.disableBlend();
			double d0 = 0D;

			for (Profiler.Result profiler$result1 : list) {
				int i1 = Maths.floor(profiler$result1.usePercentage / 4D) + 1;
				bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
				int j1 = profiler$result1.getColor();
				int k1 = j1 >> 16 & 255;
				int l1 = j1 >> 8 & 255;
				int i2 = j1 & 255;
				bufferbuilder.pos(j, k, 0D).color(k1, l1, i2, 255).endVertex();

				for (int j2 = i1; j2 >= 0; j2--) {
					float f = (float) ((d0 + profiler$result1.usePercentage * (double) j2 / (double) i1) * (Math.PI * 2D) / 100D);
					float f1 = Maths.sin(f) * 160F;
					float f2 = Maths.cos(f) * 160F * 0.5F;
					bufferbuilder.pos((float) j + f1, (float) k - f2, 0D).color(k1, l1, i2, 255).endVertex();
				}

				tessellator.draw();
				bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);

				for (int i3 = i1; i3 >= 0; i3--) {
					float f3 = (float) ((d0 + profiler$result1.usePercentage * (double) i3 / (double) i1) * (Math.PI * 2D) / 100D);
					float f4 = Maths.sin(f3) * 160F;
					float f5 = Maths.cos(f3) * 160F * 0.5F;
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
			GLS.enableTexture2D();
			String s = "";

			if (!"unspecified".equals(profiler$result.profilerName)) s = s + "[0] ";

			if (profiler$result.profilerName.isEmpty()) {
				s = s + "ROOT ";
			} else {
				s = s + profiler$result.profilerName + ' ';
			}

			fontRenderer.drawShadowText(s, (float) (j - 160), (float) (k - 80 - 16), 16777215);
			s = decimalformat.format(profiler$result.totalUsePercentage) + "%";
			fontRenderer.drawShadowText(s, (float) (j + 160 - fontRenderer.getWidth(s)), (float) (k - 80 - 16), 16777215);

			for (int k2 = 0; k2 < list.size(); k2++) {
				Profiler.Result profiler$result2 = list.get(k2);
				StringBuilder stringbuilder = new StringBuilder();

				if ("unspecified".equals(profiler$result2.profilerName)) {
					stringbuilder.append("[?] ");
				} else {
					stringbuilder.append("[").append(k2 + 1).append("] ");
				}

				String s1 = stringbuilder.append(profiler$result2.profilerName).toString();
				fontRenderer.drawShadowText(s1, (float) (j - 160), (float) (k + 80 + k2 * 8 + 20), profiler$result2.getColor());
				s1 = decimalformat.format(profiler$result2.usePercentage) + "%";
				fontRenderer.drawShadowText(s1, (float) (j + 160 - 50 - fontRenderer.getWidth(s1)), (float) (k + 80 + k2 * 8 + 20), profiler$result2.getColor());
				s1 = decimalformat.format(profiler$result2.totalUsePercentage) + "%";
				fontRenderer.drawShadowText(s1, (float) (j + 160 - fontRenderer.getWidth(s1)), (float) (k + 80 + k2 * 8 + 20), profiler$result2.getColor());
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
		if (window.isActive() && !inGameHasFocus) {
			if (!IS_RUNNING_ON_MAC) KeyBinding.updateKeyBindState();

			inGameHasFocus = true;
			Mouse.grabCursor();
			displayScreen(null);
			leftClicks = 10000;
		}
	}

	/**
	 * Resets the player keystate, disables the ingame focus, and ungrabs the mouse cursor.
	 */
	public void setIngameNotInFocus() {
		if (inGameHasFocus) {
			inGameHasFocus = false;
			Mouse.ungrabCursor();
		}
	}

	/**
	 * Displays the ingame menu
	 */
	public void displayInGameMenu() {
		if (currentScreen == null) {
			displayScreen(new InGameMenuScreen());

			if (isSingleplayer() && !integratedServer.getPublic()) soundHandler.pauseSounds();
		}
	}

	private void sendClickBlockToController(boolean leftClick) {
		if (!leftClick) leftClicks = 0;

		if (leftClicks <= 0 && !player.isHandActive()) {
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

	private void leftClick() {
		if (leftClicks <= 0) {
			if (objectMouseOver == null) {
				LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");

				if (playerController.isNotCreative()) leftClicks = 10;
			} else if (!player.isRowingBoat()) {
				switch (objectMouseOver.typeOfHit) {
					case ENTITY -> playerController.attackEntity(player, objectMouseOver.entityHit);
					case BLOCK -> {
						BlockPos blockpos = objectMouseOver.getBlockPos();
						if (world.getBlockState(blockpos).getMaterial() != Material.AIR)
							playerController.clickBlock(blockpos, objectMouseOver.sideHit);
					}
					case MISS -> {
						if (playerController.isNotCreative()) leftClicks = 10;
						player.resetCooldown();
					}
				}

				player.swingArm(Hand.MAIN_HAND);
			}
		}
	}

	/*
	  Called when user clicked he's mouse right button (place)
	 */
	private void rightClick() {
		if (!playerController.getIsHittingBlock()) {
			rightClicks = 4;

			if (!player.isRowingBoat()) {
				if (objectMouseOver == null) LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");

				for (Hand hand : Hand.values()) {
					ItemStack item = player.getHeldItem(hand);

					if (objectMouseOver != null) switch (objectMouseOver.typeOfHit) {
						case ENTITY -> {
							if (playerController.interactWithEntity(player, objectMouseOver.entityHit, objectMouseOver, hand) == ActionResult.SUCCESS) {
								return;
							}

							if (playerController.interactWithEntity(player, objectMouseOver.entityHit, hand) == ActionResult.SUCCESS) {
								return;
							}
						}
						case BLOCK -> {
							BlockPos block = objectMouseOver.getBlockPos();

							if (world.getBlockState(block).getMaterial() != Material.AIR) {
								int i = item.getCount();
								ActionResult result = playerController.processRightClickBlock(player, world, block, objectMouseOver.sideHit, objectMouseOver.hitVec, hand);

								if (result == ActionResult.SUCCESS) {
									player.swingArm(hand);

									if (!item.isEmpty() && (item.getCount() != i || playerController.isInCreativeMode()))
										entityRenderer.itemRenderer.resetEquippedProgress(hand);

									return;
								}
							}
						}
					}

					if (!item.isEmpty() && playerController.processRightClick(player, world, hand) == ActionResult.SUCCESS) {
						entityRenderer.itemRenderer.resetEquippedProgress(hand);
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
			boolean newFullscreen = !window.isFullscreen();
			gameSettings.fullScreen = newFullscreen;

			if (newFullscreen) {
				updateDisplayMode();
				window.setWindowedSize(window.getWidth(), window.getHeight());
			} else {
				window.setWindowSize(tempDisplayWidth, tempDisplayHeight);
				window.setWindowedSize(Math.max(tempDisplayWidth, 1), Math.max(tempDisplayHeight, 1));
			}

			if (currentScreen != null) {
				resize(window.getWidth(), window.getHeight());
			} else {
				updateFramebufferSize();
			}

			window.setFullscreen(newFullscreen);
			window.setVSync(gameSettings.enableVsync);
			updateDisplay();
		} catch (Exception exception) {
			LOGGER.error("Couldn't toggle fullscreen", exception);
		}
	}

	/**
	 * Called to resize the current screen.
	 */
	private void resize(int width, int height) {
		window.setGuiScale(gameSettings.guiScale, isUnicode());

		if (currentScreen != null) currentScreen.onResize(
			this,
			window.getScaledWidth(),
			window.getScaledHeight()
		);

		loadingScreen = new LoadingScreenRenderer(this);
		updateFramebufferSize();
	}

	private void updateFramebufferSize() {
		framebuffer.create(window.getWidth(), window.getHeight());

		if (entityRenderer != null) entityRenderer.updateShaderGroupSize(window.getWidth(), window.getHeight());
	}

	/**
	 * Return the musicTicker's instance
	 */
	public MusicTicker getMusicTicker() {
		return musicTicker;
	}

	/**
	 * Runs the current tick.
	 */
	public void tick() throws IOException {
		if (rightClicks > 0) rightClicks--;

		profiler.startSection("gui");

		if (!paused) ingameGUI.updateTick();

		profiler.endSection();
		entityRenderer.getMouseOver(1F);
		profiler.startSection("gameMode");

		if (!paused && world != null) playerController.updateController();

		profiler.endStartSection("textures");

		if (world != null) renderEngine.tick();

		if (currentScreen == null && player != null) {
			if (player.getHealth() <= 0F && !(currentScreen instanceof GameOverScreen)) {
				displayScreen(null);
			} else if (player.isPlayerSleeping() && world != null) {
				displayScreen(new SleepMPScreen());
			}
		} else if (currentScreen != null && currentScreen instanceof SleepMPScreen && !player.isPlayerSleeping()) {
			displayScreen(null);
		}

		if (currentScreen != null) leftClicks = 10000;

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
					currentScreen.update();
				} catch (Throwable throwable) {
					CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Ticking screen");
					CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Affected screen");
					crashreportcategory1.addDetail("Screen name", () -> currentScreen.getClass().getCanonicalName());
					throw new ReportedException(crashreport1);
				}
			}
		}

		if (currentScreen == null || currentScreen.allowInput) {
			profiler.endStartSection("mouse");
			tickMouse();

			if (leftClicks > 0) {
				leftClicks--;
			}

			profiler.endStartSection("keyboard");
			tickKeyboard();
		}

		if (world != null) {
			if (player != null) {
				joinedPlayers++;

				if (joinedPlayers == 30) {
					joinedPlayers = 0;
					world.joinEntityInSurroundings(player);
				}
			}

			profiler.endStartSection("gameRenderer");

			if (!paused) {
				entityRenderer.updateRenderer();
			}

			profiler.endStartSection("levelRenderer");

			if (!paused) {
				renderGlobal.updateClouds();
			}

			profiler.endStartSection("level");

			if (!paused) {
				if (world.getLastLightningBolt() > 0) {
					world.setLastLightningBolt(world.getLastLightningBolt() - 1);
				}

				world.updateEntities();
			}
		} else if (entityRenderer.isShaderActive()) {
			entityRenderer.stopUseShader();
		}

		if (!paused) {
			musicTicker.update();
			soundHandler.update();
		}

		if (world != null) {
			if (!paused) {
				world.setAllowedSpawnTypes(world.getDifficulty() != Difficulty.PEACEFUL, true);

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

			if (!paused && world != null)
				world.doVoidFogParticles(Maths.floor(player.posX), Maths.floor(player.posY), Maths.floor(player.posZ));

			profiler.endStartSection("particles");

			if (!paused) effectRenderer.updateEffects();
		} else if (networkManager != null) {
			profiler.endStartSection("pendingConnection");
			networkManager.processReceivedPackets();
		}

		profiler.endSection();
		systemTime = getSystemTime();
	}

	private void tickKeyboard() throws IOException {
		while (Keyboard.next()) {
			int i = Keyboard.getEventKey();

			if (debugCrashKeyPressTime > 0L) {
				if (getSystemTime() - debugCrashKeyPressTime >= 6000L) {
					throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
				}

				if (!Keyboard.isKeyDown(GLFW_KEY_C) || !Keyboard.isKeyDown(GLFW_KEY_F3)) {
					debugCrashKeyPressTime = -1L;
				}
			} else if (Keyboard.isKeyDown(GLFW_KEY_C) && Keyboard.isKeyDown(GLFW_KEY_F3)) {
				actionKeyF3 = true;
				debugCrashKeyPressTime = getSystemTime();
			}

			dispatchKeypresses();

			if (currentScreen != null) currentScreen.handleKeyboard();

			boolean flag = Keyboard.getEventKeyState();

			if (flag) {
				if (i == GLFW_KEY_F6 && entityRenderer != null) entityRenderer.switchUseShader();

				boolean flag1 = false;

				if (currentScreen == null) {
					if (i == GLFW_KEY_ESCAPE) displayInGameMenu();

					flag1 = Keyboard.isKeyDown(GLFW_KEY_F3) && processKeyF3(i);
					actionKeyF3 |= flag1;

					if (i == GLFW_KEY_F1) gameSettings.hideGUI = !gameSettings.hideGUI;
				}

				if (flag1) {
					KeyBinding.setKeyBindState(i, false);
				} else {
					KeyBinding.setKeyBindState(i, true);
					KeyBinding.onTick(i);
				}

				if (gameSettings.showDebugProfilerChart) {
					if (i == GLFW_KEY_0) {
						updateDebugProfilerName(0);
					} else if (i >= GLFW_KEY_1 && i <= GLFW_KEY_9) {
						updateDebugProfilerName(i - GLFW_KEY_1 + 1);
					}
				}
			} else {
				KeyBinding.setKeyBindState(i, false);

				if (i == GLFW_KEY_F3) {
					if (actionKeyF3) {
						actionKeyF3 = false;
					} else {
						gameSettings.showDebugInfo = !gameSettings.showDebugInfo;
						gameSettings.showDebugProfilerChart = gameSettings.showDebugInfo && Screen.isShiftDown();
						gameSettings.showLagometer = gameSettings.showDebugInfo && Screen.isAltDown();
					}
				}
			}
		}

		processKeyBinds();
	}

	private boolean processKeyF3(int auxKey) {
		return switch (auxKey) {
			case GLFW_KEY_A -> {
				renderGlobal.loadRenderers();
				debugFeedbackTranslated("debug.reload_chunks.message");
				yield true;
			}
			case GLFW_KEY_B -> {
				boolean flag1 = !renderManager.isDebugBoundingBox();
				renderManager.setDebugBoundingBox(flag1);
				debugFeedbackTranslated(flag1 ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off");
				yield true;
			}
			case GLFW_KEY_D -> {
				if (ingameGUI != null) ingameGUI.getChatGUI().clearChatMessages(false);
				yield true;
			}
			case GLFW_KEY_F -> {
				gameSettings.setOptionValue(GameSettings.Options.RENDER_DISTANCE, Screen.isShiftDown() ? -1 : 1);
				debugFeedbackTranslated("debug.cycle_renderdistance.message", gameSettings.renderDistanceChunks);
				yield true;
			}
			case GLFW_KEY_G -> {
				boolean flag = debugRenderer.toggleChunkBorders();
				debugFeedbackTranslated(flag ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
				yield true;
			}
			case GLFW_KEY_H -> {
				gameSettings.advancedItemTooltips = !gameSettings.advancedItemTooltips;
				debugFeedbackTranslated(gameSettings.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
				gameSettings.saveOptions();
				yield true;
			}
			case GLFW_KEY_N -> {
				if (!player.canUseCommand(2, "")) {
					debugFeedbackTranslated("debug.creative_spectator.error");
				} else if (player.isCreative()) {
					player.sendChatMessage("/gamemode spectator");
				} else if (player.isSpectator()) {
					player.sendChatMessage("/gamemode creative");
				}
				yield true;
			}
			case GLFW_KEY_P -> {
				gameSettings.pauseOnLostFocus = !gameSettings.pauseOnLostFocus;
				gameSettings.saveOptions();
				debugFeedbackTranslated(gameSettings.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off");
				yield true;
			}
			case GLFW_KEY_Q -> {
				debugFeedbackTranslated("debug.help.message");
				GuiNewChat guinewchat = ingameGUI.getChatGUI();
				guinewchat.printChatMessage(new TextComponentTranslation("debug.reload_chunks.help"));
				guinewchat.printChatMessage(new TextComponentTranslation("debug.show_hitboxes.help"));
				guinewchat.printChatMessage(new TextComponentTranslation("debug.clear_chat.help"));
				guinewchat.printChatMessage(new TextComponentTranslation("debug.cycle_renderdistance.help"));
				guinewchat.printChatMessage(new TextComponentTranslation("debug.chunk_boundaries.help"));
				guinewchat.printChatMessage(new TextComponentTranslation("debug.advanced_tooltips.help"));
				guinewchat.printChatMessage(new TextComponentTranslation("debug.creative_spectator.help"));
				guinewchat.printChatMessage(new TextComponentTranslation("debug.creative_spectator.help"));
				guinewchat.printChatMessage(new TextComponentTranslation("debug.pause_focus.help"));
				guinewchat.printChatMessage(new TextComponentTranslation("debug.help.help"));
				guinewchat.printChatMessage(new TextComponentTranslation("debug.reload_resourcepacks.help"));
				yield true;
			}
			case GLFW_KEY_T -> {
				debugFeedbackTranslated("debug.reload_resourcepacks.message");
				refreshResources();
				yield true;
			}
			default -> false;
		};
	}

	private void processKeyBinds() {
		while (gameSettings.keyTogglePerspective.isPressed()) {
			gameSettings.thirdPersonView = (gameSettings.thirdPersonView + 1) % 3;

			if (gameSettings.thirdPersonView == 0) {
				entityRenderer.loadEntityShader(getRenderViewEntity());
			} else if (gameSettings.thirdPersonView == 1) {
				entityRenderer.loadEntityShader(null);
			}

			renderGlobal.setDisplayListEntitiesDirty();
		}

		while (gameSettings.keySmoothCamera.isPressed()) {
			gameSettings.smoothCamera = !gameSettings.smoothCamera;
		}

		boolean saveToolbar = gameSettings.keySaveToolbar.isDown();
		boolean loadToolbar = gameSettings.keyLoadToolbar.isDown();

		for (int i = 0; i < 9; i++) {
			if (gameSettings.keyHotbar[i].isPressed()) {
				if (player.isSpectator()) {
					ingameGUI.getSpectatorGui().onHotbarSelected(i);
				} else if (!player.isCreative() || currentScreen != null || (!loadToolbar && !saveToolbar)) {
					player.inventory.currentItem = i;
				} else {
					CreativeContainerScreen.handleHotbarSnapshots(this, i, loadToolbar, saveToolbar);
				}
			}
		}

		while (gameSettings.keyInventory.isPressed()) {
			if (playerController.isRidingHorse()) {
				player.sendHorseInventory();
			} else {
				displayScreen(new InventoryScreen(player));
			}
		}

		while (gameSettings.keyAdvancements.isPressed()) {
			displayScreen(new AdvancementsScreen(player.connection.getAdvancementManager()));
		}

		while (gameSettings.keySwapHands.isPressed()) {
			if (!player.isSpectator()) {
				getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, BlockPos.ORIGIN, Facing.DOWN));
			}
		}

		while (gameSettings.keyDrop.isPressed()) {
			if (!player.isSpectator()) {
				player.dropItem(Screen.isCtrlDown());
			}
		}

		boolean chatEnabled = gameSettings.chatVisibility != EntityPlayer.ChatVisibility.HIDDEN;

		if (chatEnabled) {
			while (gameSettings.keyChat.isPressed()) {
				displayScreen(new ChatScreen());
			}

			if (currentScreen == null && gameSettings.keyCommand.isPressed()) {
				displayScreen(new ChatScreen("/"));
			}
		}

		if (player.isHandActive()) {
			if (!gameSettings.keyUseItem.isDown()) playerController.onStoppedUsingItem(player);

			while (gameSettings.keyAttack.isPressed()) ;
			while (gameSettings.keyUseItem.isPressed()) ;
			while (gameSettings.keyPickBlock.isPressed()) ;
		} else {
			while (gameSettings.keyAttack.isPressed()) leftClick();
			while (gameSettings.keyUseItem.isPressed()) rightClick();
			while (gameSettings.keyPickBlock.isPressed()) midClick();
		}

		if (gameSettings.keyUseItem.isDown() && rightClicks == 0 && !player.isHandActive()) rightClick();

		sendClickBlockToController(currentScreen == null && gameSettings.keyAttack.isDown() && inGameHasFocus);
	}

	private void tickMouse() throws IOException {
		while (Mouse.next()) {
			int i = Mouse.getEventButton();
			if (i != -1) {
				KeyBinding.setKeyBindState(i - 100, Mouse.getEventButtonState());
			}

			if (Mouse.getEventButtonState()) {
				if (player.isSpectator() && i == 2) {
					ingameGUI.getSpectatorGui().onMiddleClick();
				} else if (i != -1) {
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
							float f = Maths.clamp(player.capabilities.getFlySpeed() + (float) k * 0.005F, 0F, 0.2F);
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
					currentScreen.handleMouse();
				}
			}
		}
	}

	private void debugFeedbackTranslated(String template, Object... objs) {
		ingameGUI.getChatGUI()
		         .printChatMessage(
			         new TextComponentString("")
				         .appendSibling(
					         new TextComponentTranslation("debug.prefix")
						         .setStyle(new Style().setColor(TextFormat.YELLOW).setBold(true))
				         ).appendText(" ")
				         .appendSibling(new TextComponentTranslation(template, objs))
		         );
	}

	/**
	 * Arguments: World foldername,  World ingame name, WorldSettings
	 */
	public void launchIntegratedServer(String folderName, String worldName, WorldSettings worldSettings) {
		loadWorld(null);
		System.gc();
		ISaveHandler saveHandler = saveLoader.getSaveLoader(folderName, false);
		WorldInfo worldinfo = saveHandler.loadWorldInfo();

		if (worldinfo == null && worldSettings != null) {
			worldinfo = new WorldInfo(worldSettings, folderName);
			saveHandler.saveWorldInfo(worldinfo);
		}

		if (worldSettings == null) worldSettings = new WorldSettings(worldinfo);

		try {
			YggdrasilAuthenticationService yggdrasilService = new YggdrasilAuthenticationService(proxy, UUID.randomUUID()
			                                                                                                .toString());
			MinecraftSessionService sessionService = yggdrasilService.createMinecraftSessionService();
			GameProfileRepository profileRepo = yggdrasilService.createProfileRepository();
			PlayerProfileCache profileCache = new PlayerProfileCache(profileRepo, new File(dataDir, MinecraftServer.USER_CACHE_FILE.getName()));
			TileEntitySkull.setProfileCache(profileCache);
			TileEntitySkull.setSessionService(sessionService);
			PlayerProfileCache.setOnlineMode(false);
			integratedServer = new IntegratedServer(this, folderName, worldName, worldSettings, yggdrasilService, sessionService, profileRepo, profileCache);
			integratedServer.startServerThread();
			integratedServerIsRunning = true;
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Starting integrated server");
			CrashReportCategory category = crashreport.makeCategory("Starting integrated server");
			category.addCrashSection("Level ID", folderName);
			category.addCrashSection("Level Name", worldName);
			throw new ReportedException(crashreport);
		}

		loadingScreen.savingMessage(I18n.format("menu.loadingLevel"));

		while (!integratedServer.serverIsInRunLoop()) {
			String s = integratedServer.getUserMessage();

			if (s != null) loadingScreen.loadingMessage(I18n.format(s));
			else loadingScreen.loadingMessage("");
		}

		displayScreen(new WorkingScreen());
		SocketAddress socketaddress = integratedServer.getNetworkSystem().addLocalEndpoint();
		NetworkManager networkmanager = NetworkManager.provideLocalClient(socketaddress);
		networkmanager.setNetHandler(new NetHandlerLoginClient(networkmanager, this, null));
		networkmanager.sendPacket(new C00Handshake(socketaddress.toString(), 0, ConnectionState.LOGIN));
		networkmanager.sendPacket(new CPacketLoginStart(getSession().getProfile()));
		networkManager = networkmanager;
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
	public void loadWorld(WorldClient client, String loadingMessage) {
		if (client == null) {
			NetHandlerPlayClient connection = getConnection();

			if (connection != null) connection.cleanup();

			if (integratedServer != null && integratedServer.isAnvilFileSet()) integratedServer.initiateShutdown();

			integratedServer = null;
			entityRenderer.resetData();
			playerController = null;
		}

		renderViewEntity = null;
		networkManager = null;

		if (loadingScreen != null) {
			loadingScreen.resetProgressAndMessage(loadingMessage);
			loadingScreen.loadingMessage("");
		}

		if (client == null && world != null) {
			resourcePackRepo.clearResourcePack();
			ingameGUI.resetPlayersOverlayFooterHeader();
			setServerData(null);
			integratedServerIsRunning = false;
		}

		soundHandler.stopSounds();
		world = client;

		if (renderGlobal != null) {
			renderGlobal.setWorldAndLoadRenderers(client);
		}

		if (effectRenderer != null) {
			effectRenderer.clearEffects(client);
		}

		TileEntityRendererDispatcher.instance.setWorld(client);

		if (client != null) {
			if (!integratedServerIsRunning) {
				AuthenticationService auth = new YggdrasilAuthenticationService(proxy, UUID.randomUUID().toString());
				MinecraftSessionService session = auth.createMinecraftSessionService();
				GameProfileRepository repo = auth.createProfileRepository();
				PlayerProfileCache cache = new PlayerProfileCache(repo, new File(dataDir, MinecraftServer.USER_CACHE_FILE.getName()));
				TileEntitySkull.setProfileCache(cache);
				TileEntitySkull.setSessionService(session);
				PlayerProfileCache.setOnlineMode(false);
			}

			if (player == null) {
				player = playerController.createPlayer(client, new StatisticsManager(), new RecipeBookClient());
				playerController.flipPlayer(player);
			}

			player.preparePlayerToSpawn();
			client.spawnEntity(player);
			player.input = new Input(gameSettings);
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
		player = playerController.createPlayer(world, player == null ? new StatisticsManager() : player.getStatFileWriter(), player == null ? new RecipeBook() : player.getRecipeBook());
		player.getDataManager().setEntryValues(player.getDataManager().getAll());
		player.dimension = dimension;
		renderViewEntity = player;
		player.preparePlayerToSpawn();
		player.setServerBrand(s);
		world.spawnEntity(player);
		playerController.flipPlayer(player);
		player.input = new Input(gameSettings);
		player.setEntityId(i);
		playerController.setPlayerCapabilities(player);
		player.setReducedDebug(player.hasReducedDebug());

		if (currentScreen instanceof GameOverScreen) displayScreen(null);
	}

	public NetHandlerPlayClient getConnection() {
		return player == null ? null : player.connection;
	}

	/**
	 * Called when user clicked he's mouse middle button (pick block)
	 */
	private void midClick() {
		if (objectMouseOver != null && objectMouseOver.typeOfHit != RayTraceResult.Type.MISS) {
			boolean creative = player.capabilities.isCreativeMode;
			TileEntity tileEntity = null;
			ItemStack item;

			if (objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
				BlockPos pos = objectMouseOver.getBlockPos();
				IBlockState state = world.getBlockState(pos);
				Block block = state.getBlock();

				if (state.getMaterial() == Material.AIR) return;

				item = block.getItem(world, pos, state);

				if (item.isEmpty()) return;

				if (creative && Screen.isCtrlDown() && block.hasTileEntity())
					tileEntity = world.getTileEntity(pos);
			} else {
				if (objectMouseOver.typeOfHit != RayTraceResult.Type.ENTITY || objectMouseOver.entityHit == null || !creative)
					return;

				switch (objectMouseOver.entityHit) {
					case EntityPainting ignored -> item = new ItemStack(Items.PAINTING);
					case EntityLeashKnot ignored -> item = new ItemStack(Items.LEAD);
					case EntityItemFrame frame -> {
						ItemStack frameItem = frame.getDisplayedItem();

						if (frameItem.isEmpty()) {
							item = new ItemStack(Items.ITEM_FRAME);
						} else {
							item = frameItem.copy();
						}
					}
					case EntityMinecart minecart -> {
						Item minecartItem = switch (minecart.getType()) {
							case FURNACE -> Items.FURNACE_MINECART;
							case CHEST -> Items.CHEST_MINECART;
							case TNT -> Items.TNT_MINECART;
							case HOPPER -> Items.HOPPER_MINECART;
							case COMMAND_BLOCK -> Items.COMMAND_BLOCK_MINECART;
							default -> Items.MINECART;
						};

						item = new ItemStack(minecartItem);
					}
					case EntityBoat entityBoat -> item = new ItemStack(entityBoat.getItemBoat());
					case EntityArmorStand ignored -> item = new ItemStack(Items.ARMOR_STAND);
					case EntityEnderCrystal ignored -> item = new ItemStack(Items.END_CRYSTAL);
					default -> {
						ResourceLocation location = EntityList.getKey(objectMouseOver.entityHit);
						if (location == null || !EntityList.ENTITY_EGGS.containsKey(location)) return;

						item = new ItemStack(Items.SPAWN_EGG);
						ItemMonsterPlacer.applyEntityIdToItemStack(item, location);
					}
				}
			}

			if (item.isEmpty()) {
				String s = switch (objectMouseOver.typeOfHit) {
					case BLOCK ->
						Block.REGISTRY.getNameForObject(world.getBlockState(objectMouseOver.getBlockPos()).getBlock())
						              .toString();
					case ENTITY -> EntityList.getKey(objectMouseOver.entityHit).toString();
					default -> "";
				};

				LOGGER.warn("Picking on: [{}] {} gave null item", objectMouseOver.typeOfHit, s);
			} else {
				InventoryPlayer inventory = player.inventory;

				if (tileEntity != null) storeTEInStack(item, tileEntity);

				int i = inventory.getSlotFor(item);

				if (creative) {
					inventory.setPickedItemStack(item);
					playerController.sendSlotPacket(player.getHeldItem(Hand.MAIN_HAND), 36 + inventory.currentItem);
				} else if (i != -1) {
					if (InventoryPlayer.isHotbar(i)) inventory.currentItem = i;
					else playerController.pickItem(i);
				}
			}
		}
	}

	private void storeTEInStack(ItemStack stack, TileEntity te) {
		NBTTagCompound nbt = te.writeToNBT(new NBTTagCompound());

		if (stack.getItem() == Items.SKULL && nbt.hasKey("Owner")) {
			NBTTagCompound owner = nbt.getCompoundTag("Owner");
			NBTTagCompound tag = new NBTTagCompound();
			tag.setTag("SkullOwner", owner);
			stack.setTagCompound(tag);
		} else {
			stack.setTagInfo("BlockEntityTag", nbt);
			NBTTagCompound tag = new NBTTagCompound();
			NBTTagList nbts = new NBTTagList();
			nbts.appendTag(new NBTTagString("(+NBT)"));
			tag.setTag("Lore", nbts);
			stack.setTagInfo("display", tag);
		}
	}

	/**
	 * adds core server Info (GL version , Texture pack, isModded, type), and the worldInfo to the crash report
	 */
	public CrashReport addGraphicsAndWorldToCrashReport(CrashReport report) {
		report.getCategory().addDetail("Launched Version", () -> version);
		report.getCategory().addDetail("LWJGL", Version::getVersion);
		report.getCategory().addDetail("OpenGL",
		                               () -> GLS.getString(7937) +
			                               " GL version " + GLS.getString(7938) +
			                               ", " + GLS.getString(7936)
		);
		report.getCategory().addDetail("Type", () -> "Client (map_client.txt)");
		report.getCategory().addDetail("Resource Packs", () -> {
			StringBuilder builder = new StringBuilder();

			for (String s : gameSettings.resourcePacks) {
				if (!builder.isEmpty()) builder.append(", ");

				builder.append(s);

				if (gameSettings.incompatibleResourcePacks.contains(s)) {
					builder.append(" (incompatible)");
				}
			}

			return builder.toString();
		});
		report.getCategory().addDetail("Current Language", () -> languageManager.getCurrentLanguage().toString());
		report.getCategory()
		      .addDetail("Profiler Position", () -> profiler.profilingEnabled ? profiler.getNameOfLastSection() : "N/A (disabled)");
		report.getCategory().addDetail("CPU", OpenGlHelper::getCpu);

		if (world != null) world.addWorldInfoToCrashReport(report);

		return report;
	}

	public ListenableFuture<Object> scheduleResourcesRefresh() {
		return addScheduledTask(this::refreshResources);
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
	 * Returns whether we're in full screen or not.
	 */
	public boolean isFullScreen() {
		return window.isFullscreen();
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
		return resourceManager;
	}

	public ResourcePackRepository getResourcePackRepository() {
		return resourcePackRepo;
	}

	public LanguageManager getLanguageManager() {
		return languageManager;
	}

	public TextureMap getBlockTextures() {
		return blockTextures;
	}

	public boolean isPaused() {
		return paused;
	}

	public SoundHandler getSoundHandler() {
		return soundHandler;
	}

	public MusicTicker.MusicType getAmbientMusicType() {
		if (currentScreen instanceof WinGameScreen) return MusicTicker.MusicType.CREDITS;
		else if (player != null) {
			switch (player.world.provider) {
				case WorldProviderHell ignored -> {
					return MusicTicker.MusicType.NETHER;
				}
				case WorldProviderEnd ignored -> {
					return ingameGUI
						.getBossOverlay()
						.shouldPlayEndBossMusic() ? MusicTicker.MusicType.END_BOSS : MusicTicker.MusicType.END;
				}
				default -> {
					return player.capabilities.isCreativeMode &&
						player.capabilities.allowFlying ?
						MusicTicker.MusicType.CREATIVE :
						MusicTicker.MusicType.GAME;
				}
			}
		}

		return MusicTicker.MusicType.MENU;
	}

	public void dispatchKeypresses() {
		int i = Keyboard.getEventKey();

		if (i != 0 && !Keyboard.isRepeatEvent()) {
			if (!(currentScreen instanceof ControlsScreen) || ((ControlsScreen) currentScreen).time <= getSystemTime() - 20L) {
				if (Keyboard.getEventKeyState()) {
					if (i == gameSettings.keyFullscreen.getKeyCode()) {
						toggleFullscreen();
					} else if (i == gameSettings.keyScreenshot.getKeyCode()) {
						ingameGUI.getChatGUI()
						         .printChatMessage(ScreenShotHelper.saveScreenshot(dataDir, window.getWidth(), window.getHeight(), framebuffer));
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

}
