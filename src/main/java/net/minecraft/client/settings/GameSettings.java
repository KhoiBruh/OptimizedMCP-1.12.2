package net.minecraft.client.settings;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.GuiNewChat;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.Keyboard;
import net.minecraft.client.util.Mouse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerModelParts;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.util.HandSide;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class GameSettings {
	
	public static final Splitter COLON_SPLITTER = Splitter.on(':');
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new Gson();
	private static final Type TYPE_LIST_STRING = new ParameterizedType() {
		public Type @NonNull [] getActualTypeArguments() {
			return new Type[]{String.class};
		}
		
		public @NonNull Type getRawType() {
			return List.class;
		}
		
		public Type getOwnerType() {
			return null;
		}
	};
	/**
	 * GUI scale values
	 */
	private static final String[] GUISCALES = new String[]{"options.guiScale.auto", "options.guiScale.small", "options.guiScale.normal", "options.guiScale.large"};
	private static final String[] PARTICLES = new String[]{"options.particles.all", "options.particles.decreased", "options.particles.minimal"};
	private static final String[] AMBIENT_OCCLUSIONS = new String[]{"options.ao.off", "options.ao.min", "options.ao.max"};
	private static final String[] CLOUDS_TYPES = new String[]{"options.off", "options.clouds.fast", "options.clouds.fancy"};
	private static final String[] ATTACK_INDICATORS = new String[]{"options.off", "options.attack.crosshair", "options.attack.hotbar"};
	
	private final Set<PlayerModelParts> modelParts = new HashSet<>(List.of(PlayerModelParts.values()));
	private final Map<SoundCategory, Float> soundLevels = new EnumMap<>(SoundCategory.class);
	
	public float mouseSensitivity = 0.5F;
	public boolean invertMouse;
	public int renderDistanceChunks = 8;
	public boolean viewBobbing = true;
	public boolean fboEnable = true;
	public int limitFramerate = 120;
	/**
	 * Clouds flag
	 */
	public int clouds = 2;
	public boolean fancyGraphics = true;
	/**
	 * Smooth Lighting
	 */
	public int ambientOcclusion = 2;
	public List<String> resourcePacks = new ArrayList<>();
	public List<String> incompatibleResourcePacks = new ArrayList<>();
	public EntityPlayer.ChatVisibility chatVisibility = EntityPlayer.ChatVisibility.FULL;
	public boolean chatColours = true;
	public boolean chatLinks = true;
	public boolean chatLinksPrompt = true;
	public float chatOpacity = 1F;
	public boolean fullScreen;
	public boolean enableVsync = true;
	public boolean useVbo = true;
	public boolean reducedDebugInfo;
	public boolean hideServerAddress;
	/**
	 * Whether to show advanced information on item tooltips, toggled by F3+H
	 */
	public boolean advancedItemTooltips;
	/**
	 * Whether to pause when the game loses focus, toggled by F3+P
	 */
	public boolean pauseOnLostFocus = true;
	public boolean touchscreen;
	public HandSide mainHand = HandSide.RIGHT;
	public int overrideWidth;
	public int overrideHeight;
	public boolean heldItemTooltips = true;
	public float chatScale = 1F;
	public float chatWidth = 1F;
	public float chatHeightUnfocused = 0.44366196F;
	public float chatHeightFocused = 1F;
	public int mipmapLevels = 4;
	public boolean useNativeTransport = true;
	public boolean entityShadows = true;
	public int attackIndicator = 1;
	public boolean enableWeakAttacks;
	public boolean showSubtitles;
	public boolean autoJump = true;
	
	public KeyBinding keyForward = new KeyBinding("key.forward", GLFW_KEY_W, "key.categories.movement");
	public KeyBinding keyLeft = new KeyBinding("key.left", GLFW_KEY_A, "key.categories.movement");
	public KeyBinding keyBack = new KeyBinding("key.back", GLFW_KEY_S, "key.categories.movement");
	public KeyBinding keyRight = new KeyBinding("key.right", GLFW_KEY_D, "key.categories.movement");
	public KeyBinding keyJump = new KeyBinding("key.jump", GLFW_KEY_SPACE, "key.categories.movement");
	public KeyBinding keySneak = new KeyBinding("key.sneak", GLFW_KEY_LEFT_SHIFT, "key.categories.movement");
	public KeyBinding keySprint = new KeyBinding("key.sprint", GLFW_KEY_LEFT_CONTROL, "key.categories.movement");
	public KeyBinding keyInventory = new KeyBinding("key.inventory", GLFW_KEY_E, "key.categories.inventory");
	public KeyBinding keySwapHands = new KeyBinding("key.swapHands", GLFW_KEY_F, "key.categories.inventory");
	public KeyBinding keyDrop = new KeyBinding("key.drop", GLFW_KEY_Q, "key.categories.inventory");
	public KeyBinding keyUseItem = new KeyBinding("key.use", -99, "key.categories.gameplay");
	public KeyBinding keyAttack = new KeyBinding("key.attack", -100, "key.categories.gameplay");
	public KeyBinding keyPickBlock = new KeyBinding("key.pickItem", -98, "key.categories.gameplay");
	public KeyBinding keyChat = new KeyBinding("key.chat", GLFW_KEY_T, "key.categories.multiplayer");
	public KeyBinding keyPlayerList = new KeyBinding("key.playerlist", GLFW_KEY_TAB, "key.categories.multiplayer");
	public KeyBinding keyCommand = new KeyBinding("key.command", GLFW_KEY_SLASH, "key.categories.multiplayer");
	public KeyBinding keyScreenshot = new KeyBinding("key.screenshot", GLFW_KEY_F2, "key.categories.misc");
	public KeyBinding keyTogglePerspective = new KeyBinding("key.togglePerspective", GLFW_KEY_F5, "key.categories.misc");
	public KeyBinding keySmoothCamera = new KeyBinding("key.smoothCamera", GLFW_KEY_UNKNOWN, "key.categories.misc");
	public KeyBinding keyFullscreen = new KeyBinding("key.fullscreen", GLFW_KEY_F11, "key.categories.misc");
	public KeyBinding keySpectatorOutlines = new KeyBinding("key.spectatorOutlines", GLFW_KEY_UNKNOWN, "key.categories.misc");
	public KeyBinding keyAdvancements = new KeyBinding("key.advancements", GLFW_KEY_L, "key.categories.misc");
	
	public KeyBinding[] keyHotbar = new KeyBinding[]{
			new KeyBinding("key.hotbar.1", GLFW_KEY_1, "key.categories.inventory"),
			new KeyBinding("key.hotbar.2", GLFW_KEY_2, "key.categories.inventory"),
			new KeyBinding("key.hotbar.3", GLFW_KEY_3, "key.categories.inventory"),
			new KeyBinding("key.hotbar.4", GLFW_KEY_4, "key.categories.inventory"),
			new KeyBinding("key.hotbar.5", GLFW_KEY_5, "key.categories.inventory"),
			new KeyBinding("key.hotbar.6", GLFW_KEY_6, "key.categories.inventory"),
			new KeyBinding("key.hotbar.7", GLFW_KEY_7, "key.categories.inventory"),
			new KeyBinding("key.hotbar.8", GLFW_KEY_8, "key.categories.inventory"),
			new KeyBinding("key.hotbar.9", GLFW_KEY_9, "key.categories.inventory")
	};
	
	public KeyBinding keySaveToolbar = new KeyBinding("key.saveToolbarActivator", GLFW_KEY_UNKNOWN, "key.categories.creative");
	public KeyBinding keyLoadToolbar = new KeyBinding("key.loadToolbarActivator", GLFW_KEY_UNKNOWN, "key.categories.creative");
	
	public KeyBinding[] keyBindings;
	public Difficulty difficulty;
	public boolean hideGUI;
	public int thirdPersonView;
	/**
	 * true if debug info should be displayed instead of version
	 */
	public boolean showDebugInfo;
	public boolean showDebugProfilerChart;
	public boolean showLagometer;
	/**
	 * The lastServer string.
	 */
	public String lastServer;
	/**
	 * Smooth Camera Toggle
	 */
	public boolean smoothCamera;
	public boolean debugCamEnable;
	public float fovSetting;
	public float gammaSetting;
	public float saturation;
	/**
	 * GUI scale
	 */
	public int guiScale;
	/**
	 * Determines amount of particles. 0 = All, 1 = Decreased, 2 = Minimal
	 */
	public int particleSetting;
	/**
	 * Game settings language
	 */
	public String language;
	public boolean forceUnicodeFont;
	protected Minecraft mc;
	private File optionsFile;
	
	public GameSettings(Minecraft mcIn, File mcDataDir) {
		KeyBinding[] akeybinding = new KeyBinding[]{keyAttack, keyUseItem, keyForward, keyLeft, keyBack, keyRight, keyJump, keySneak, keySprint, keyDrop, keyInventory, keyChat, keyPlayerList, keyPickBlock, keyCommand, keyScreenshot, keyTogglePerspective, keySmoothCamera, keyFullscreen, keySpectatorOutlines, keySwapHands, keySaveToolbar, keyLoadToolbar, keyAdvancements};
		keyBindings = new KeyBinding[akeybinding.length + keyHotbar.length];
		System.arraycopy(akeybinding, 0, keyBindings, 0, akeybinding.length);
		System.arraycopy(keyHotbar, 0, keyBindings, akeybinding.length, keyHotbar.length);
		difficulty = Difficulty.NORMAL;
		lastServer = "";
		fovSetting = 70F;
		language = "en_us";
		mc = mcIn;
		optionsFile = new File(mcDataDir, "options.txt");
		
		loadOptions();
	}
	
	public GameSettings() {
		KeyBinding[] akeybinding = new KeyBinding[]{keyAttack, keyUseItem, keyForward, keyLeft, keyBack, keyRight, keyJump, keySneak, keySprint, keyDrop, keyInventory, keyChat, keyPlayerList, keyPickBlock, keyCommand, keyScreenshot, keyTogglePerspective, keySmoothCamera, keyFullscreen, keySpectatorOutlines, keySwapHands, keySaveToolbar, keyLoadToolbar, keyAdvancements};
		keyBindings = new KeyBinding[akeybinding.length + keyHotbar.length];
		System.arraycopy(akeybinding, 0, keyBindings, 0, akeybinding.length);
		System.arraycopy(keyHotbar, 0, keyBindings, akeybinding.length, keyHotbar.length);
		difficulty = Difficulty.NORMAL;
		lastServer = "";
		fovSetting = 70F;
		language = "en_us";
	}
	
	/**
	 * Gets the display name for a key.
	 */
	public static String getKeyDisplayString(int key) {
		if (key < 0) {
			int button = key + 100;
			if (button >= 0 && button <= GLFW_MOUSE_BUTTON_LAST) {
				return switch (button) {
					case 0 -> I18n.format("key.mouse.left");
					case 1 -> I18n.format("key.mouse.right");
					case 2 -> I18n.format("key.mouse.middle");
					default -> I18n.format("key.mouseButton", button + 1);
				};
			}
			String name = Keyboard.getKeyName(key);
			if (!name.equals("UNKNOWN")) return name;
			return String.format("%c", (char) (key - 256)).toUpperCase();
		} else {
			String name = Keyboard.getKeyName(key);
			if (!name.equals("UNKNOWN")) return name;
			return String.format("%c", (char) (key - 256)).toUpperCase();
		}
	}
	
	/**
	 * Returns whether the specified key binding is currently being pressed.
	 */
	public static boolean isKeyDown(KeyBinding key) {
		int i = key.getKeyCode();
		if (i == GLFW_KEY_UNKNOWN) return false;
		if (i < 0) return Mouse.isButtonDown(i + 100);
		return Keyboard.isKeyDown(i);
	}
	
	/**
	 * Returns the translation of the given index in the given String array. If the index is smaller than 0 or greater
	 * than/equal to the length of the String array, it is changed to 0.
	 */
	private static String getTranslation(String[] strArray, int index) {
		if (index < 0 || index >= strArray.length) index = 0;
		
		return I18n.format(strArray[index]);
	}
	
	/**
	 * Sets a key binding and then saves all settings.
	 */
	public void setOptionKeyBinding(KeyBinding key, int keyCode) {
		key.setKeyCode(keyCode);
		saveOptions();
	}
	
	/**
	 * If the specified option is controlled by a slider (float value), this will set the float value.
	 */
	public void setOptionFloatValue(GameSettings.Options options, float value) {
		switch (options) {
			case SENSITIVITY -> mouseSensitivity = value;
			case FOV -> fovSetting = value;
			case GAMMA -> gammaSetting = value;
			case FRAMERATE_LIMIT -> limitFramerate = (int) value;
			case CHAT_OPACITY -> {
				chatOpacity = value;
				mc.ingameGUI.getChatGUI().refreshChat();
			}
			case CHAT_HEIGHT_FOCUSED -> {
				chatHeightFocused = value;
				mc.ingameGUI.getChatGUI().refreshChat();
			}
			case CHAT_HEIGHT_UNFOCUSED -> {
				chatHeightUnfocused = value;
				mc.ingameGUI.getChatGUI().refreshChat();
			}
			case CHAT_WIDTH -> {
				chatWidth = value;
				mc.ingameGUI.getChatGUI().refreshChat();
			}
			case CHAT_SCALE -> {
				chatScale = value;
				mc.ingameGUI.getChatGUI().refreshChat();
			}
			case MIPMAP_LEVELS -> {
				int i = mipmapLevels;
				mipmapLevels = (int) value;
				if ((float) i != value) {
					mc.getBlockTextures().setMipmapLevels(mipmapLevels);
					mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					mc.getBlockTextures().setBlurMipmapDirect(false, mipmapLevels > 0);
					mc.scheduleResourcesRefresh();
				}
			}
			case RENDER_DISTANCE -> {
				renderDistanceChunks = (int) value;
				mc.renderGlobal.setDisplayListEntitiesDirty();
			}
			default -> {
			}
		}
	}
	
	/**
	 * For non-float options. Toggles the option on/off, or cycles through the list i.e. render distances.
	 */
	public void setOptionValue(GameSettings.Options options, int value) {
		switch (options) {
			case RENDER_DISTANCE ->
					setOptionFloatValue(options, MathHelper.clamp((float) (renderDistanceChunks + value), options.min, options.max));
			case MAIN_HAND -> mainHand = mainHand.opposite();
			case INVERT_MOUSE -> invertMouse = !invertMouse;
			case GUI_SCALE -> {
				guiScale = guiScale + value & 3;
				mc.getWindow().setGuiScale(guiScale, mc.isUnicode());
			}
			case PARTICLES -> particleSetting = (particleSetting + value) % 3;
			case VIEW_BOBBING -> viewBobbing = !viewBobbing;
			case RENDER_CLOUDS -> clouds = (clouds + value) % 3;
			case FORCE_UNICODE_FONT -> {
				forceUnicodeFont = !forceUnicodeFont;
				mc.fontRenderer.setUnicodeFlag(mc.getLanguageManager().isCurrentLocaleUnicode() || forceUnicodeFont);
			}
			case FBO_ENABLE -> fboEnable = !fboEnable;
			case GRAPHICS -> {
				fancyGraphics = !fancyGraphics;
				mc.renderGlobal.loadRenderers();
			}
			case AMBIENT_OCCLUSION -> {
				ambientOcclusion = (ambientOcclusion + value) % 3;
				mc.renderGlobal.loadRenderers();
			}
			case CHAT_VISIBILITY ->
					chatVisibility = EntityPlayer.ChatVisibility.getEnumChatVisibility((chatVisibility.getChatVisibility() + value) % 3);
			case CHAT_COLOR -> chatColours = !chatColours;
			case CHAT_LINKS -> chatLinks = !chatLinks;
			case CHAT_LINKS_PROMPT -> chatLinksPrompt = !chatLinksPrompt;
			case TOUCHSCREEN -> touchscreen = !touchscreen;
			case USE_FULLSCREEN -> {
				fullScreen = !fullScreen;
				if (mc.isFullScreen() != fullScreen) mc.toggleFullscreen();
			}
			case ENABLE_VSYNC -> {
				enableVsync = !enableVsync;
				mc.getWindow().setVSync(enableVsync);
			}
			case USE_VBO -> {
				useVbo = !useVbo;
				mc.renderGlobal.loadRenderers();
			}
			case REDUCED_DEBUG_INFO -> reducedDebugInfo = !reducedDebugInfo;
			case ENTITY_SHADOWS -> entityShadows = !entityShadows;
			case ATTACK_INDICATOR -> attackIndicator = (attackIndicator + value) % 3;
			case SHOW_SUBTITLES -> showSubtitles = !showSubtitles;
			case AUTO_JUMP -> autoJump = !autoJump;
			default -> {}
		}
		
		saveOptions();
	}
	
	public float getOptionFloatValue(GameSettings.Options options) {
		return switch (options) {
			case FOV -> fovSetting;
			case GAMMA -> gammaSetting;
			case SATURATION -> saturation;
			case SENSITIVITY -> mouseSensitivity;
			case CHAT_OPACITY -> chatOpacity;
			case CHAT_HEIGHT_FOCUSED -> chatHeightFocused;
			case CHAT_HEIGHT_UNFOCUSED -> chatHeightUnfocused;
			case CHAT_SCALE -> chatScale;
			case CHAT_WIDTH -> chatWidth;
			case FRAMERATE_LIMIT -> limitFramerate;
			case MIPMAP_LEVELS -> mipmapLevels;
			case RENDER_DISTANCE -> renderDistanceChunks;
			default -> 0;
		};
	}
	
	public boolean getOptionOrdinalValue(GameSettings.Options options) {
		return switch (options) {
			case INVERT_MOUSE -> invertMouse;
			case VIEW_BOBBING -> viewBobbing;
			case FBO_ENABLE -> fboEnable;
			case CHAT_COLOR -> chatColours;
			case CHAT_LINKS -> chatLinks;
			case CHAT_LINKS_PROMPT -> chatLinksPrompt;
			case USE_FULLSCREEN -> fullScreen;
			case ENABLE_VSYNC -> enableVsync;
			case USE_VBO -> useVbo;
			case TOUCHSCREEN -> touchscreen;
			case FORCE_UNICODE_FONT -> forceUnicodeFont;
			case REDUCED_DEBUG_INFO -> reducedDebugInfo;
			case ENTITY_SHADOWS -> entityShadows;
			case SHOW_SUBTITLES -> showSubtitles;
			case ENABLE_WEAK_ATTACKS -> enableWeakAttacks;
			case AUTO_JUMP -> autoJump;
			default -> false;
		};
	}
	
	/**
	 * Gets a key binding.
	 */
	public String getKeyBinding(GameSettings.Options options) {
		String s = I18n.format(options.getTranslation()) + ": ";
		
		if (options.isFloat()) {
			float f1 = getOptionFloatValue(options);
			float f = options.normalize(f1);
			
			return switch (options) {
				case SENSITIVITY -> {
					if (f == 0F) yield s + I18n.format("options.sensitivity.min");
					else if (f == 1F) yield s + I18n.format("options.sensitivity.max");
					else yield s + (int) (f * 200F) + "%";
				}
				case FOV -> {
					if (f1 == 70F) yield s + I18n.format("options.fov.min");
					else if (f1 == 110F) yield s + I18n.format("options.fov.max");
					else yield s + (int) f1;
				}
				case FRAMERATE_LIMIT ->
						f1 == options.max ? s + I18n.format("options.framerateLimit.max") : s + I18n.format("options.framerate", (int) f1);
				case RENDER_CLOUDS ->
						f1 == options.min ? s + I18n.format("options.cloudHeight.min") : s + ((int) f1 + 128);
				case GAMMA -> {
					if (f == 0F) yield s + I18n.format("options.gamma.min");
					else if (f == 1F) yield s + I18n.format("options.gamma.max");
					else yield s + "+" + (int) (f * 100F) + "%";
				}
				case SATURATION -> s + (int) (f * 400F) + "%";
				case CHAT_OPACITY -> s + (int) (f * 90F + 10F) + "%";
				case CHAT_HEIGHT_UNFOCUSED, CHAT_HEIGHT_FOCUSED -> s + GuiNewChat.calculateChatboxHeight(f) + "px";
				case CHAT_WIDTH -> s + GuiNewChat.calculateChatboxWidth(f) + "px";
				case RENDER_DISTANCE -> s + I18n.format("options.chunks", (int) f1);
				case MIPMAP_LEVELS -> f1 == 0F ? s + I18n.format("options.off") : s + (int) f1;
				default -> f == 0F ? s + I18n.format("options.off") : s + (int) (f * 100F) + "%";
			};
		} else if (options.isBoolean()) {
			boolean flag = getOptionOrdinalValue(options);
			return flag ? s + I18n.format("options.on") : s + I18n.format("options.off");
		} else {
			return switch (options) {
				case MAIN_HAND -> s + mainHand;
				case GUI_SCALE -> s + getTranslation(GUISCALES, guiScale);
				case CHAT_VISIBILITY -> s + I18n.format(chatVisibility.getResourceKey());
				case PARTICLES -> s + getTranslation(PARTICLES, particleSetting);
				case AMBIENT_OCCLUSION -> s + getTranslation(AMBIENT_OCCLUSIONS, ambientOcclusion);
				case RENDER_CLOUDS -> s + getTranslation(CLOUDS_TYPES, clouds);
				case GRAPHICS -> fancyGraphics ? s + I18n.format("options.graphics.fancy") : s + I18n.format("options.graphics.fast");
				case ATTACK_INDICATOR -> s + getTranslation(ATTACK_INDICATORS, attackIndicator);
				default -> s;
			};
		}
	}
	
	/**
	 * Loads the options from the options file. It appears that this has replaced the previous 'loadOptions'
	 */
	public void loadOptions() {
		try {
			if (!optionsFile.exists()) return;
			
			soundLevels.clear();
			List<String> options = IOUtils.readLines(new FileInputStream(optionsFile), StandardCharsets.UTF_8);
			NBTTagCompound compound = new NBTTagCompound();
			
			for (String s : options) {
				try {
					Iterator<String> iterator = COLON_SPLITTER.omitEmptyStrings().limit(2).split(s).iterator();
					compound.setString(iterator.next(), iterator.next());
				} catch (Exception var10) {
					LOGGER.warn("Skipping bad option: {}", s);
				}
			}
			
			compound = dataFix(compound);
			
			for (String s1 : compound.getKeySet()) {
				String s2 = compound.getString(s1);
				
				try {
					switch (s1) {
						case "mouseSensitivity" -> mouseSensitivity = parseFloat(s2);
						case "fov" -> fovSetting = parseFloat(s2) * 40F + 70F;
						case "gamma" -> gammaSetting = parseFloat(s2);
						case "saturation" -> saturation = parseFloat(s2);
						case "invertYMouse" -> invertMouse = "true".equals(s2);
						case "renderDistance" -> renderDistanceChunks = Integer.parseInt(s2);
						case "guiScale" -> guiScale = Integer.parseInt(s2);
						case "particles" -> particleSetting = Integer.parseInt(s2);
						case "bobView" -> viewBobbing = "true".equals(s2);
						case "maxFps" -> limitFramerate = Integer.parseInt(s2);
						case "fboEnable" -> fboEnable = "true".equals(s2);
						case "difficulty" -> difficulty = Difficulty.getDifficultyEnum(Integer.parseInt(s2));
						case "fancyGraphics" -> fancyGraphics = "true".equals(s2);
						case "ao" -> {
							if ("true".equals(s2)) ambientOcclusion = 2;
							else if ("false".equals(s2)) ambientOcclusion = 0;
							else ambientOcclusion = Integer.parseInt(s2);
						}
						case "renderClouds" -> {
							if ("true".equals(s2)) clouds = 2;
							else if ("false".equals(s2)) clouds = 0;
							else if ("fast".equals(s2)) clouds = 1;
						}
						case "attackIndicator" -> attackIndicator = Integer.parseInt(s2);
						case "resourcePacks" -> {
							resourcePacks = JsonUtils.gsonDeserialize(GSON, s2, TYPE_LIST_STRING);
							if (resourcePacks == null) resourcePacks = new ArrayList<>();
						}
						case "incompatibleResourcePacks" -> {
							incompatibleResourcePacks = JsonUtils.gsonDeserialize(GSON, s2, TYPE_LIST_STRING);
							if (incompatibleResourcePacks == null) incompatibleResourcePacks = new ArrayList<>();
						}
						case "lastServer" -> lastServer = s2;
						case "lang" -> language = s2;
						case "chatVisibility" ->
								chatVisibility = EntityPlayer.ChatVisibility.getEnumChatVisibility(Integer.parseInt(s2));
						case "chatColors" -> chatColours = "true".equals(s2);
						case "chatLinks" -> chatLinks = "true".equals(s2);
						case "chatLinksPrompt" -> chatLinksPrompt = "true".equals(s2);
						case "chatOpacity" -> chatOpacity = parseFloat(s2);
						case "fullscreen" -> fullScreen = "true".equals(s2);
						case "enableVsync" -> enableVsync = "true".equals(s2);
						case "useVbo" -> useVbo = "true".equals(s2);
						case "hideServerAddress" -> hideServerAddress = "true".equals(s2);
						case "advancedItemTooltips" -> advancedItemTooltips = "true".equals(s2);
						case "pauseOnLostFocus" -> pauseOnLostFocus = "true".equals(s2);
						case "touchscreen" -> touchscreen = "true".equals(s2);
						case "overrideHeight" -> overrideHeight = Integer.parseInt(s2);
						case "overrideWidth" -> overrideWidth = Integer.parseInt(s2);
						case "heldItemTooltips" -> heldItemTooltips = "true".equals(s2);
						case "chatHeightFocused" -> chatHeightFocused = parseFloat(s2);
						case "chatHeightUnfocused" -> chatHeightUnfocused = parseFloat(s2);
						case "chatScale" -> chatScale = parseFloat(s2);
						case "chatWidth" -> chatWidth = parseFloat(s2);
						case "mipmapLevels" -> mipmapLevels = Integer.parseInt(s2);
						case "forceUnicodeFont" -> forceUnicodeFont = "true".equals(s2);
						case "reducedDebugInfo" -> reducedDebugInfo = "true".equals(s2);
						case "useNativeTransport" -> useNativeTransport = "true".equals(s2);
						case "entityShadows" -> entityShadows = "true".equals(s2);
						case "mainHand" -> mainHand = "left".equals(s2) ? HandSide.LEFT : HandSide.RIGHT;
						case "showSubtitles" -> showSubtitles = "true".equals(s2);
						case "enableWeakAttacks" -> enableWeakAttacks = "true".equals(s2);
						case "autoJump" -> autoJump = "true".equals(s2);
						default -> {
						}
					}
					
					for (KeyBinding keybinding : keyBindings) {
						if (s1.equals("key_" + keybinding.getDescription())) {
							keybinding.setKeyCode(Integer.parseInt(s2));
						}
					}
					
					for (SoundCategory soundcategory : SoundCategory.values()) {
						if (s1.equals("soundCategory_" + soundcategory.getName())) {
							soundLevels.put(soundcategory, parseFloat(s2));
						}
					}
					
					for (PlayerModelParts enumplayermodelparts : PlayerModelParts.values()) {
						if (s1.equals("modelPart_" + enumplayermodelparts.getPartName())) {
							setModelPartEnabled(enumplayermodelparts, "true".equals(s2));
						}
					}
				} catch (Exception e) {
					LOGGER.warn("Skipping bad option: {}:{}", s1, s2);
				}
			}
			
			KeyBinding.resetKeyBindingArrayAndHash();
		} catch (Exception e) {
			LOGGER.error("Failed to load options", e);
		}
	}
	
	private NBTTagCompound dataFix(NBTTagCompound compound) {
		int i = 0;
		
		try {
			i = Integer.parseInt(compound.getString("version"));
		} catch (RuntimeException ignored) {
		}
		
		return mc.getDataFixer().process(FixTypes.OPTIONS, compound, i);
	}
	
	/**
	 * Parses a string into a float.
	 */
	private float parseFloat(String str) {
		if (str.equals("true")) {
			return 1F;
		} else {
			return str.equals("false") ? 0F : Float.parseFloat(str);
		}
	}
	
	/**
	 * Saves the options to the options file.
	 */
	public void saveOptions() {
		try (var writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(optionsFile), StandardCharsets.UTF_8))) {
			writer.println("version:1343");
			writer.println("invertYMouse:" + invertMouse);
			writer.println("mouseSensitivity:" + mouseSensitivity);
			writer.println("fov:" + (fovSetting - 70F) / 40F);
			writer.println("gamma:" + gammaSetting);
			writer.println("saturation:" + saturation);
			writer.println("renderDistance:" + renderDistanceChunks);
			writer.println("guiScale:" + guiScale);
			writer.println("particles:" + particleSetting);
			writer.println("bobView:" + viewBobbing);
			writer.println("maxFps:" + limitFramerate);
			writer.println("fboEnable:" + fboEnable);
			writer.println("difficulty:" + difficulty.getDifficultyId());
			writer.println("fancyGraphics:" + fancyGraphics);
			writer.println("ao:" + ambientOcclusion);
			
			switch (clouds) {
				case 0 -> writer.println("renderClouds:false");
				case 1 -> writer.println("renderClouds:fast");
				case 2 -> writer.println("renderClouds:true");
			}
			
			writer.println("resourcePacks:" + GSON.toJson(resourcePacks));
			writer.println("incompatibleResourcePacks:" + GSON.toJson(incompatibleResourcePacks));
			writer.println("lastServer:" + lastServer);
			writer.println("lang:" + language);
			writer.println("chatVisibility:" + chatVisibility.getChatVisibility());
			writer.println("chatColors:" + chatColours);
			writer.println("chatLinks:" + chatLinks);
			writer.println("chatLinksPrompt:" + chatLinksPrompt);
			writer.println("chatOpacity:" + chatOpacity);
			writer.println("fullscreen:" + fullScreen);
			writer.println("enableVsync:" + enableVsync);
			writer.println("useVbo:" + useVbo);
			writer.println("hideServerAddress:" + hideServerAddress);
			writer.println("advancedItemTooltips:" + advancedItemTooltips);
			writer.println("pauseOnLostFocus:" + pauseOnLostFocus);
			writer.println("touchscreen:" + touchscreen);
			writer.println("overrideWidth:" + overrideWidth);
			writer.println("overrideHeight:" + overrideHeight);
			writer.println("heldItemTooltips:" + heldItemTooltips);
			writer.println("chatHeightFocused:" + chatHeightFocused);
			writer.println("chatHeightUnfocused:" + chatHeightUnfocused);
			writer.println("chatScale:" + chatScale);
			writer.println("chatWidth:" + chatWidth);
			writer.println("mipmapLevels:" + mipmapLevels);
			writer.println("forceUnicodeFont:" + forceUnicodeFont);
			writer.println("reducedDebugInfo:" + reducedDebugInfo);
			writer.println("useNativeTransport:" + useNativeTransport);
			writer.println("entityShadows:" + entityShadows);
			writer.println("mainHand:" + (mainHand == HandSide.LEFT ? "left" : "right"));
			writer.println("attackIndicator:" + attackIndicator);
			writer.println("showSubtitles:" + showSubtitles);
			writer.println("enableWeakAttacks:" + enableWeakAttacks);
			writer.println("autoJump:" + autoJump);
			
			for (KeyBinding key : keyBindings) {
				writer.println("key_" + key.getDescription() + ":" + key.getKeyCode());
			}
			
			for (SoundCategory sound : SoundCategory.values()) {
				writer.println("soundCategory_" + sound.getName() + ":" + getSoundLevel(sound));
			}
			
			for (PlayerModelParts parts : PlayerModelParts.values()) {
				writer.println("modelPart_" + parts.getPartName() + ":" + modelParts.contains(parts));
			}
		} catch (Exception exception) {
			LOGGER.error("Failed to save options", exception);
		}
		
		sendSettingsToServer();
	}
	
	public float getSoundLevel(SoundCategory category) {
		return soundLevels.getOrDefault(category, 1F);
	}
	
	public void setSoundLevel(SoundCategory category, float volume) {
		mc.getSoundHandler().setSoundLevel(category, volume);
		soundLevels.put(category, volume);
	}
	
	/**
	 * Send a client info packet with settings information to the server
	 */
	public void sendSettingsToServer() {
		if (mc.player != null) {
			int i = 0;
			
			for (PlayerModelParts parts : modelParts) {
				i |= parts.getPartMask();
			}
			
			mc.player.connection.sendPacket(new CPacketClientSettings(language, renderDistanceChunks, chatVisibility, chatColours, i, mainHand));
		}
	}
	
	public Set<PlayerModelParts> getModelParts() {
		return ImmutableSet.copyOf(modelParts);
	}
	
	public void setModelPartEnabled(PlayerModelParts parts, boolean enable) {
		if (enable) {
			modelParts.add(parts);
		} else {
			modelParts.remove(parts);
		}
		
		sendSettingsToServer();
	}
	
	public void switchModelPartEnabled(PlayerModelParts parts) {
		if (getModelParts().contains(parts)) {
			modelParts.remove(parts);
		} else {
			modelParts.add(parts);
		}
		
		sendSettingsToServer();
	}
	
	/**
	 * Return true if the clouds should be rendered
	 */
	public int shouldRenderClouds() {
		return renderDistanceChunks >= 4 ? clouds : 0;
	}
	
	/**
	 * Return true if the client connect to a server using the native transport system
	 */
	public boolean isUsingNativeTransport() {
		return useNativeTransport;
	}
	
	@Getter
	public enum Options {
		INVERT_MOUSE("options.invertMouse", false, true),
		SENSITIVITY("options.sensitivity", true, false),
		FOV("options.fov", true, false, 30F, 110F, 1F),
		GAMMA("options.gamma", true, false),
		SATURATION("options.saturation", true, false),
		RENDER_DISTANCE("options.renderDistance", true, false, 2F, 32F, 1F),
		VIEW_BOBBING("options.viewBobbing", false, true),
		FRAMERATE_LIMIT("options.framerateLimit", true, false, 10F, 260F, 10F),
		FBO_ENABLE("options.fboEnable", false, true),
		RENDER_CLOUDS("options.renderClouds", false, false),
		GRAPHICS("options.graphics", false, false),
		AMBIENT_OCCLUSION("options.ao", false, false),
		GUI_SCALE("options.guiScale", false, false),
		PARTICLES("options.particles", false, false),
		CHAT_VISIBILITY("options.chat.visibility", false, false),
		CHAT_COLOR("options.chat.color", false, true),
		CHAT_LINKS("options.chat.links", false, true),
		CHAT_OPACITY("options.chat.opacity", true, false),
		CHAT_LINKS_PROMPT("options.chat.links.prompt", false, true),
		USE_FULLSCREEN("options.fullscreen", false, true),
		ENABLE_VSYNC("options.vsync", false, true),
		USE_VBO("options.vbo", false, true),
		TOUCHSCREEN("options.touchscreen", false, true),
		CHAT_SCALE("options.chat.scale", true, false),
		CHAT_WIDTH("options.chat.width", true, false),
		CHAT_HEIGHT_FOCUSED("options.chat.height.focused", true, false),
		CHAT_HEIGHT_UNFOCUSED("options.chat.height.unfocused", true, false),
		MIPMAP_LEVELS("options.mipmapLevels", true, false, 0F, 4F, 1F),
		FORCE_UNICODE_FONT("options.forceUnicodeFont", false, true),
		REDUCED_DEBUG_INFO("options.reducedDebugInfo", false, true),
		ENTITY_SHADOWS("options.entityShadows", false, true),
		MAIN_HAND("options.mainHand", false, false),
		ATTACK_INDICATOR("options.attackIndicator", false, false),
		ENABLE_WEAK_ATTACKS("options.enableWeakAttacks", false, true),
		SHOW_SUBTITLES("options.showSubtitles", false, true),
		AUTO_JUMP("options.autoJump", false, true);
		
		private final boolean isFloat;
		private final boolean isBoolean;
		private final String translation;
		private final float step;
		private final float min;
		private final float max;
		
		Options(String translation, boolean isFloat, boolean isBoolean) {
			this(translation, isFloat, isBoolean, 0F, 1F, 0F);
		}
		
		Options(String translation, boolean isFloat, boolean isBoolean, float min, float max, float step) {
			this.translation = translation;
			this.isFloat = isFloat;
			this.isBoolean = isBoolean;
			this.min = min;
			this.max = max;
			this.step = step;
		}
		
		public static GameSettings.Options byOrdinal(int ordinal) {
			for (GameSettings.Options options : values()) {
				if (options.ordinal() == ordinal) return options;
			}
			
			return null;
		}
		
		public float normalize(float value) {
			return MathHelper.clamp((stepClamp(value) - min) / (max - min), 0F, 1F);
		}
		
		public float denormalize(float value) {
			return stepClamp(min + (max - min) * MathHelper.clamp(value, 0F, 1F));
		}
		
		public float stepClamp(float value) {
			value = snapStep(value);
			return MathHelper.clamp(value, min, max);
		}
		
		private float snapStep(float value) {
			if (step > 0F) value = step * Math.round(value / step);
			return value;
		}
	}
	
}
