package net.minecraft.client.settings;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

public class GameSettings
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private static final Type TYPE_LIST_STRING = new ParameterizedType()
    {
        public Type[] getActualTypeArguments()
        {
            return new Type[] {String.class};
        }
        public Type getRawType()
        {
            return List.class;
        }
        public Type getOwnerType()
        {
            return null;
        }
    };
    public static final Splitter COLON_SPLITTER = Splitter.on(':');

    /** GUI scale values */
    private static final String[] GUISCALES = new String[] {"options.guiScale.auto", "options.guiScale.small", "options.guiScale.normal", "options.guiScale.large"};
    private static final String[] PARTICLES = new String[] {"options.particles.all", "options.particles.decreased", "options.particles.minimal"};
    private static final String[] AMBIENT_OCCLUSIONS = new String[] {"options.ao.off", "options.ao.min", "options.ao.max"};
    private static final String[] CLOUDS_TYPES = new String[] {"options.off", "options.clouds.fast", "options.clouds.fancy"};
    private static final String[] ATTACK_INDICATORS = new String[] {"options.off", "options.attack.crosshair", "options.attack.hotbar"};
    public static final String[] NARRATOR_MODES = new String[] {"options.narrator.off", "options.narrator.all", "options.narrator.chat", "options.narrator.system"};
    public float mouseSensitivity = 0.5F;
    public boolean invertMouse;
    public int renderDistanceChunks = -1;
    public boolean viewBobbing = true;
    public boolean anaglyph;
    public boolean fboEnable = true;
    public int limitFramerate = 120;

    /** Clouds flag */
    public int clouds = 2;
    public boolean fancyGraphics = true;

    /** Smooth Lighting */
    public int ambientOcclusion = 2;
    public List<String> resourcePacks = Lists.<String>newArrayList();
    public List<String> incompatibleResourcePacks = Lists.<String>newArrayList();
    public EntityPlayer.EnumChatVisibility chatVisibility = EntityPlayer.EnumChatVisibility.FULL;
    public boolean chatColours = true;
    public boolean chatLinks = true;
    public boolean chatLinksPrompt = true;
    public float chatOpacity = 1.0F;
    public boolean snooperEnabled = true;
    public boolean fullScreen;
    public boolean enableVsync = true;
    public boolean useVbo = true;
    public boolean reducedDebugInfo;
    public boolean hideServerAddress;

    /**
     * Whether to show advanced information on item tooltips, toggled by F3+H
     */
    public boolean advancedItemTooltips;

    /** Whether to pause when the game loses focus, toggled by F3+P */
    public boolean pauseOnLostFocus = true;
    private final Set<EnumPlayerModelParts> setModelParts = Sets.newHashSet(EnumPlayerModelParts.values());
    public boolean touchscreen;
    public EnumHandSide mainHand = EnumHandSide.RIGHT;
    public int overrideWidth;
    public int overrideHeight;
    public boolean heldItemTooltips = true;
    public float chatScale = 1.0F;
    public float chatWidth = 1.0F;
    public float chatHeightUnfocused = 0.44366196F;
    public float chatHeightFocused = 1.0F;
    public int mipmapLevels = 4;
    private final Map<SoundCategory, Float> soundLevels = Maps.newEnumMap(SoundCategory.class);
    public boolean useNativeTransport = true;
    public boolean entityShadows = true;
    public int attackIndicator = 1;
    public boolean enableWeakAttacks;
    public boolean showSubtitles;
    public boolean realmsNotifications = true;
    public boolean autoJump = true;
    public TutorialSteps tutorialStep = TutorialSteps.MOVEMENT;
    public KeyBinding keyBindForward = new KeyBinding("key.forward", 17, "key.categories.movement");
    public KeyBinding keyBindLeft = new KeyBinding("key.left", 30, "key.categories.movement");
    public KeyBinding keyBindBack = new KeyBinding("key.back", 31, "key.categories.movement");
    public KeyBinding keyBindRight = new KeyBinding("key.right", 32, "key.categories.movement");
    public KeyBinding keyBindJump = new KeyBinding("key.jump", 57, "key.categories.movement");
    public KeyBinding keyBindSneak = new KeyBinding("key.sneak", 42, "key.categories.movement");
    public KeyBinding keyBindSprint = new KeyBinding("key.sprint", 29, "key.categories.movement");
    public KeyBinding keyBindInventory = new KeyBinding("key.inventory", 18, "key.categories.inventory");
    public KeyBinding keyBindSwapHands = new KeyBinding("key.swapHands", 33, "key.categories.inventory");
    public KeyBinding keyBindDrop = new KeyBinding("key.drop", 16, "key.categories.inventory");
    public KeyBinding keyBindUseItem = new KeyBinding("key.use", -99, "key.categories.gameplay");
    public KeyBinding keyBindAttack = new KeyBinding("key.attack", -100, "key.categories.gameplay");
    public KeyBinding keyBindPickBlock = new KeyBinding("key.pickItem", -98, "key.categories.gameplay");
    public KeyBinding keyBindChat = new KeyBinding("key.chat", 20, "key.categories.multiplayer");
    public KeyBinding keyBindPlayerList = new KeyBinding("key.playerlist", 15, "key.categories.multiplayer");
    public KeyBinding keyBindCommand = new KeyBinding("key.command", 53, "key.categories.multiplayer");
    public KeyBinding keyBindScreenshot = new KeyBinding("key.screenshot", 60, "key.categories.misc");
    public KeyBinding keyBindTogglePerspective = new KeyBinding("key.togglePerspective", 63, "key.categories.misc");
    public KeyBinding keyBindSmoothCamera = new KeyBinding("key.smoothCamera", 0, "key.categories.misc");
    public KeyBinding keyBindFullscreen = new KeyBinding("key.fullscreen", 87, "key.categories.misc");
    public KeyBinding keyBindSpectatorOutlines = new KeyBinding("key.spectatorOutlines", 0, "key.categories.misc");
    public KeyBinding keyBindAdvancements = new KeyBinding("key.advancements", 38, "key.categories.misc");
    public KeyBinding[] keyBindsHotbar = new KeyBinding[] {new KeyBinding("key.hotbar.1", 2, "key.categories.inventory"), new KeyBinding("key.hotbar.2", 3, "key.categories.inventory"), new KeyBinding("key.hotbar.3", 4, "key.categories.inventory"), new KeyBinding("key.hotbar.4", 5, "key.categories.inventory"), new KeyBinding("key.hotbar.5", 6, "key.categories.inventory"), new KeyBinding("key.hotbar.6", 7, "key.categories.inventory"), new KeyBinding("key.hotbar.7", 8, "key.categories.inventory"), new KeyBinding("key.hotbar.8", 9, "key.categories.inventory"), new KeyBinding("key.hotbar.9", 10, "key.categories.inventory")};
    public KeyBinding keyBindSaveToolbar = new KeyBinding("key.saveToolbarActivator", 46, "key.categories.creative");
    public KeyBinding keyBindLoadToolbar = new KeyBinding("key.loadToolbarActivator", 45, "key.categories.creative");
    public KeyBinding[] keyBindings;
    protected Minecraft mc;
    private File optionsFile;
    public EnumDifficulty difficulty;
    public boolean hideGUI;
    public int thirdPersonView;

    /** true if debug info should be displayed instead of version */
    public boolean showDebugInfo;
    public boolean showDebugProfilerChart;
    public boolean showLagometer;

    /** The lastServer string. */
    public String lastServer;

    /** Smooth Camera Toggle */
    public boolean smoothCamera;
    public boolean debugCamEnable;
    public float fovSetting;
    public float gammaSetting;
    public float saturation;

    /** GUI scale */
    public int guiScale;

    /** Determines amount of particles. 0 = All, 1 = Decreased, 2 = Minimal */
    public int particleSetting;
    public int narrator;

    /** Game settings language */
    public String language;
    public boolean forceUnicodeFont;

    public GameSettings(Minecraft mcIn, File mcDataDir)
    {
        keyBindings = (KeyBinding[])ArrayUtils.addAll(new KeyBinding[] {keyBindAttack, keyBindUseItem, keyBindForward, keyBindLeft, keyBindBack, keyBindRight, keyBindJump, keyBindSneak, keyBindSprint, keyBindDrop, keyBindInventory, keyBindChat, keyBindPlayerList, keyBindPickBlock, keyBindCommand, keyBindScreenshot, keyBindTogglePerspective, keyBindSmoothCamera, keyBindFullscreen, keyBindSpectatorOutlines, keyBindSwapHands, keyBindSaveToolbar, keyBindLoadToolbar, keyBindAdvancements}, keyBindsHotbar);
        difficulty = EnumDifficulty.NORMAL;
        lastServer = "";
        fovSetting = 70.0F;
        language = "en_us";
        mc = mcIn;
        optionsFile = new File(mcDataDir, "options.txt");

        if (mcIn.isJava64bit() && Runtime.getRuntime().maxMemory() >= 1000000000L)
        {
            GameSettings.Options.RENDER_DISTANCE.setValueMax(32.0F);
        }
        else
        {
            GameSettings.Options.RENDER_DISTANCE.setValueMax(16.0F);
        }

        renderDistanceChunks = mcIn.isJava64bit() ? 12 : 8;
        loadOptions();
    }

    public GameSettings()
    {
        keyBindings = (KeyBinding[])ArrayUtils.addAll(new KeyBinding[] {keyBindAttack, keyBindUseItem, keyBindForward, keyBindLeft, keyBindBack, keyBindRight, keyBindJump, keyBindSneak, keyBindSprint, keyBindDrop, keyBindInventory, keyBindChat, keyBindPlayerList, keyBindPickBlock, keyBindCommand, keyBindScreenshot, keyBindTogglePerspective, keyBindSmoothCamera, keyBindFullscreen, keyBindSpectatorOutlines, keyBindSwapHands, keyBindSaveToolbar, keyBindLoadToolbar, keyBindAdvancements}, keyBindsHotbar);
        difficulty = EnumDifficulty.NORMAL;
        lastServer = "";
        fovSetting = 70.0F;
        language = "en_us";
    }

    /**
     * Gets the display name for a key.
     */
    public static String getKeyDisplayString(int key)
    {
        if (key < 0)
        {
            switch (key)
            {
                case -100:
                    return I18n.format("key.mouse.left");

                case -99:
                    return I18n.format("key.mouse.right");

                case -98:
                    return I18n.format("key.mouse.middle");

                default:
                    return I18n.format("key.mouseButton", key + 101);
            }
        }
        else
        {
            return key < 256 ? Keyboard.getKeyName(key) : String.format("%c", (char)(key - 256)).toUpperCase();
        }
    }

    /**
     * Returns whether the specified key binding is currently being pressed.
     */
    public static boolean isKeyDown(KeyBinding key)
    {
        int i = key.getKeyCode();

        if (i != 0 && i < 256)
        {
            return i < 0 ? Mouse.isButtonDown(i + 100) : Keyboard.isKeyDown(i);
        }
        else
        {
            return false;
        }
    }

    /**
     * Sets a key binding and then saves all settings.
     */
    public void setOptionKeyBinding(KeyBinding key, int keyCode)
    {
        key.setKeyCode(keyCode);
        saveOptions();
    }

    /**
     * If the specified option is controlled by a slider (float value), this will set the float value.
     */
    public void setOptionFloatValue(GameSettings.Options settingsOption, float value)
    {
        if (settingsOption == GameSettings.Options.SENSITIVITY)
        {
            mouseSensitivity = value;
        }

        if (settingsOption == GameSettings.Options.FOV)
        {
            fovSetting = value;
        }

        if (settingsOption == GameSettings.Options.GAMMA)
        {
            gammaSetting = value;
        }

        if (settingsOption == GameSettings.Options.FRAMERATE_LIMIT)
        {
            limitFramerate = (int)value;
        }

        if (settingsOption == GameSettings.Options.CHAT_OPACITY)
        {
            chatOpacity = value;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (settingsOption == GameSettings.Options.CHAT_HEIGHT_FOCUSED)
        {
            chatHeightFocused = value;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (settingsOption == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED)
        {
            chatHeightUnfocused = value;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (settingsOption == GameSettings.Options.CHAT_WIDTH)
        {
            chatWidth = value;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (settingsOption == GameSettings.Options.CHAT_SCALE)
        {
            chatScale = value;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (settingsOption == GameSettings.Options.MIPMAP_LEVELS)
        {
            int i = mipmapLevels;
            mipmapLevels = (int)value;

            if ((float)i != value)
            {
                mc.getTextureMapBlocks().setMipmapLevels(mipmapLevels);
                mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                mc.getTextureMapBlocks().setBlurMipmapDirect(false, mipmapLevels > 0);
                mc.scheduleResourcesRefresh();
            }
        }

        if (settingsOption == GameSettings.Options.RENDER_DISTANCE)
        {
            renderDistanceChunks = (int)value;
            mc.renderGlobal.setDisplayListEntitiesDirty();
        }
    }

    /**
     * For non-float options. Toggles the option on/off, or cycles through the list i.e. render distances.
     */
    public void setOptionValue(GameSettings.Options settingsOption, int value)
    {
        if (settingsOption == GameSettings.Options.RENDER_DISTANCE)
        {
            setOptionFloatValue(settingsOption, MathHelper.clamp((float)(renderDistanceChunks + value), settingsOption.getValueMin(), settingsOption.getValueMax()));
        }

        if (settingsOption == GameSettings.Options.MAIN_HAND)
        {
            mainHand = mainHand.opposite();
        }

        if (settingsOption == GameSettings.Options.INVERT_MOUSE)
        {
            invertMouse = !invertMouse;
        }

        if (settingsOption == GameSettings.Options.GUI_SCALE)
        {
            guiScale = guiScale + value & 3;
        }

        if (settingsOption == GameSettings.Options.PARTICLES)
        {
            particleSetting = (particleSetting + value) % 3;
        }

        if (settingsOption == GameSettings.Options.VIEW_BOBBING)
        {
            viewBobbing = !viewBobbing;
        }

        if (settingsOption == GameSettings.Options.RENDER_CLOUDS)
        {
            clouds = (clouds + value) % 3;
        }

        if (settingsOption == GameSettings.Options.FORCE_UNICODE_FONT)
        {
            forceUnicodeFont = !forceUnicodeFont;
            mc.fontRenderer.setUnicodeFlag(mc.getLanguageManager().isCurrentLocaleUnicode() || forceUnicodeFont);
        }

        if (settingsOption == GameSettings.Options.FBO_ENABLE)
        {
            fboEnable = !fboEnable;
        }

        if (settingsOption == GameSettings.Options.ANAGLYPH)
        {
            anaglyph = !anaglyph;
            mc.refreshResources();
        }

        if (settingsOption == GameSettings.Options.GRAPHICS)
        {
            fancyGraphics = !fancyGraphics;
            mc.renderGlobal.loadRenderers();
        }

        if (settingsOption == GameSettings.Options.AMBIENT_OCCLUSION)
        {
            ambientOcclusion = (ambientOcclusion + value) % 3;
            mc.renderGlobal.loadRenderers();
        }

        if (settingsOption == GameSettings.Options.CHAT_VISIBILITY)
        {
            chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility((chatVisibility.getChatVisibility() + value) % 3);
        }

        if (settingsOption == GameSettings.Options.CHAT_COLOR)
        {
            chatColours = !chatColours;
        }

        if (settingsOption == GameSettings.Options.CHAT_LINKS)
        {
            chatLinks = !chatLinks;
        }

        if (settingsOption == GameSettings.Options.CHAT_LINKS_PROMPT)
        {
            chatLinksPrompt = !chatLinksPrompt;
        }

        if (settingsOption == GameSettings.Options.SNOOPER_ENABLED)
        {
            snooperEnabled = !snooperEnabled;
        }

        if (settingsOption == GameSettings.Options.TOUCHSCREEN)
        {
            touchscreen = !touchscreen;
        }

        if (settingsOption == GameSettings.Options.USE_FULLSCREEN)
        {
            fullScreen = !fullScreen;

            if (mc.isFullScreen() != fullScreen)
            {
                mc.toggleFullscreen();
            }
        }

        if (settingsOption == GameSettings.Options.ENABLE_VSYNC)
        {
            enableVsync = !enableVsync;
            Display.setVSyncEnabled(enableVsync);
        }

        if (settingsOption == GameSettings.Options.USE_VBO)
        {
            useVbo = !useVbo;
            mc.renderGlobal.loadRenderers();
        }

        if (settingsOption == GameSettings.Options.REDUCED_DEBUG_INFO)
        {
            reducedDebugInfo = !reducedDebugInfo;
        }

        if (settingsOption == GameSettings.Options.ENTITY_SHADOWS)
        {
            entityShadows = !entityShadows;
        }

        if (settingsOption == GameSettings.Options.ATTACK_INDICATOR)
        {
            attackIndicator = (attackIndicator + value) % 3;
        }

        if (settingsOption == GameSettings.Options.SHOW_SUBTITLES)
        {
            showSubtitles = !showSubtitles;
        }

        if (settingsOption == GameSettings.Options.REALMS_NOTIFICATIONS)
        {
            realmsNotifications = !realmsNotifications;
        }

        if (settingsOption == GameSettings.Options.AUTO_JUMP)
        {
            autoJump = !autoJump;
        }

        if (settingsOption == GameSettings.Options.NARRATOR)
        {
            if (NarratorChatListener.INSTANCE.isActive())
            {
                narrator = (narrator + value) % NARRATOR_MODES.length;
            }
            else
            {
                narrator = 0;
            }

            NarratorChatListener.INSTANCE.announceMode(narrator);
        }

        saveOptions();
    }

    public float getOptionFloatValue(GameSettings.Options settingOption)
    {
        if (settingOption == GameSettings.Options.FOV)
        {
            return fovSetting;
        }
        else if (settingOption == GameSettings.Options.GAMMA)
        {
            return gammaSetting;
        }
        else if (settingOption == GameSettings.Options.SATURATION)
        {
            return saturation;
        }
        else if (settingOption == GameSettings.Options.SENSITIVITY)
        {
            return mouseSensitivity;
        }
        else if (settingOption == GameSettings.Options.CHAT_OPACITY)
        {
            return chatOpacity;
        }
        else if (settingOption == GameSettings.Options.CHAT_HEIGHT_FOCUSED)
        {
            return chatHeightFocused;
        }
        else if (settingOption == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED)
        {
            return chatHeightUnfocused;
        }
        else if (settingOption == GameSettings.Options.CHAT_SCALE)
        {
            return chatScale;
        }
        else if (settingOption == GameSettings.Options.CHAT_WIDTH)
        {
            return chatWidth;
        }
        else if (settingOption == GameSettings.Options.FRAMERATE_LIMIT)
        {
            return (float) limitFramerate;
        }
        else if (settingOption == GameSettings.Options.MIPMAP_LEVELS)
        {
            return (float) mipmapLevels;
        }
        else
        {
            return settingOption == GameSettings.Options.RENDER_DISTANCE ? (float) renderDistanceChunks : 0.0F;
        }
    }

    public boolean getOptionOrdinalValue(GameSettings.Options settingOption)
    {
        switch (settingOption)
        {
            case INVERT_MOUSE:
                return invertMouse;

            case VIEW_BOBBING:
                return viewBobbing;

            case ANAGLYPH:
                return anaglyph;

            case FBO_ENABLE:
                return fboEnable;

            case CHAT_COLOR:
                return chatColours;

            case CHAT_LINKS:
                return chatLinks;

            case CHAT_LINKS_PROMPT:
                return chatLinksPrompt;

            case SNOOPER_ENABLED:
                return snooperEnabled;

            case USE_FULLSCREEN:
                return fullScreen;

            case ENABLE_VSYNC:
                return enableVsync;

            case USE_VBO:
                return useVbo;

            case TOUCHSCREEN:
                return touchscreen;

            case FORCE_UNICODE_FONT:
                return forceUnicodeFont;

            case REDUCED_DEBUG_INFO:
                return reducedDebugInfo;

            case ENTITY_SHADOWS:
                return entityShadows;

            case SHOW_SUBTITLES:
                return showSubtitles;

            case REALMS_NOTIFICATIONS:
                return realmsNotifications;

            case ENABLE_WEAK_ATTACKS:
                return enableWeakAttacks;

            case AUTO_JUMP:
                return autoJump;

            default:
                return false;
        }
    }

    /**
     * Returns the translation of the given index in the given String array. If the index is smaller than 0 or greater
     * than/equal to the length of the String array, it is changed to 0.
     */
    private static String getTranslation(String[] strArray, int index)
    {
        if (index < 0 || index >= strArray.length)
        {
            index = 0;
        }

        return I18n.format(strArray[index]);
    }

    /**
     * Gets a key binding.
     */
    public String getKeyBinding(GameSettings.Options settingOption)
    {
        String s = I18n.format(settingOption.getTranslation()) + ": ";

        if (settingOption.isFloat())
        {
            float f1 = getOptionFloatValue(settingOption);
            float f = settingOption.normalizeValue(f1);

            if (settingOption == GameSettings.Options.SENSITIVITY)
            {
                if (f == 0.0F)
                {
                    return s + I18n.format("options.sensitivity.min");
                }
                else
                {
                    return f == 1.0F ? s + I18n.format("options.sensitivity.max") : s + (int)(f * 200.0F) + "%";
                }
            }
            else if (settingOption == GameSettings.Options.FOV)
            {
                if (f1 == 70.0F)
                {
                    return s + I18n.format("options.fov.min");
                }
                else
                {
                    return f1 == 110.0F ? s + I18n.format("options.fov.max") : s + (int)f1;
                }
            }
            else if (settingOption == GameSettings.Options.FRAMERATE_LIMIT)
            {
                return f1 == settingOption.valueMax ? s + I18n.format("options.framerateLimit.max") : s + I18n.format("options.framerate", (int)f1);
            }
            else if (settingOption == GameSettings.Options.RENDER_CLOUDS)
            {
                return f1 == settingOption.valueMin ? s + I18n.format("options.cloudHeight.min") : s + ((int)f1 + 128);
            }
            else if (settingOption == GameSettings.Options.GAMMA)
            {
                if (f == 0.0F)
                {
                    return s + I18n.format("options.gamma.min");
                }
                else
                {
                    return f == 1.0F ? s + I18n.format("options.gamma.max") : s + "+" + (int)(f * 100.0F) + "%";
                }
            }
            else if (settingOption == GameSettings.Options.SATURATION)
            {
                return s + (int)(f * 400.0F) + "%";
            }
            else if (settingOption == GameSettings.Options.CHAT_OPACITY)
            {
                return s + (int)(f * 90.0F + 10.0F) + "%";
            }
            else if (settingOption == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED)
            {
                return s + GuiNewChat.calculateChatboxHeight(f) + "px";
            }
            else if (settingOption == GameSettings.Options.CHAT_HEIGHT_FOCUSED)
            {
                return s + GuiNewChat.calculateChatboxHeight(f) + "px";
            }
            else if (settingOption == GameSettings.Options.CHAT_WIDTH)
            {
                return s + GuiNewChat.calculateChatboxWidth(f) + "px";
            }
            else if (settingOption == GameSettings.Options.RENDER_DISTANCE)
            {
                return s + I18n.format("options.chunks", (int)f1);
            }
            else if (settingOption == GameSettings.Options.MIPMAP_LEVELS)
            {
                return f1 == 0.0F ? s + I18n.format("options.off") : s + (int)f1;
            }
            else
            {
                return f == 0.0F ? s + I18n.format("options.off") : s + (int)(f * 100.0F) + "%";
            }
        }
        else if (settingOption.isBoolean())
        {
            boolean flag = getOptionOrdinalValue(settingOption);
            return flag ? s + I18n.format("options.on") : s + I18n.format("options.off");
        }
        else if (settingOption == GameSettings.Options.MAIN_HAND)
        {
            return s + mainHand;
        }
        else if (settingOption == GameSettings.Options.GUI_SCALE)
        {
            return s + getTranslation(GUISCALES, guiScale);
        }
        else if (settingOption == GameSettings.Options.CHAT_VISIBILITY)
        {
            return s + I18n.format(chatVisibility.getResourceKey());
        }
        else if (settingOption == GameSettings.Options.PARTICLES)
        {
            return s + getTranslation(PARTICLES, particleSetting);
        }
        else if (settingOption == GameSettings.Options.AMBIENT_OCCLUSION)
        {
            return s + getTranslation(AMBIENT_OCCLUSIONS, ambientOcclusion);
        }
        else if (settingOption == GameSettings.Options.RENDER_CLOUDS)
        {
            return s + getTranslation(CLOUDS_TYPES, clouds);
        }
        else if (settingOption == GameSettings.Options.GRAPHICS)
        {
            if (fancyGraphics)
            {
                return s + I18n.format("options.graphics.fancy");
            }
            else
            {
                String s1 = "options.graphics.fast";
                return s + I18n.format("options.graphics.fast");
            }
        }
        else if (settingOption == GameSettings.Options.ATTACK_INDICATOR)
        {
            return s + getTranslation(ATTACK_INDICATORS, attackIndicator);
        }
        else if (settingOption == GameSettings.Options.NARRATOR)
        {
            return NarratorChatListener.INSTANCE.isActive() ? s + getTranslation(NARRATOR_MODES, narrator) : s + I18n.format("options.narrator.notavailable");
        }
        else
        {
            return s;
        }
    }

    /**
     * Loads the options from the options file. It appears that this has replaced the previous 'loadOptions'
     */
    public void loadOptions()
    {
        try
        {
            if (!optionsFile.exists())
            {
                return;
            }

            soundLevels.clear();
            List<String> list = IOUtils.readLines(new FileInputStream(optionsFile));
            NBTTagCompound nbttagcompound = new NBTTagCompound();

            for (String s : list)
            {
                try
                {
                    Iterator<String> iterator = COLON_SPLITTER.omitEmptyStrings().limit(2).split(s).iterator();
                    nbttagcompound.setString(iterator.next(), iterator.next());
                }
                catch (Exception var10)
                {
                    LOGGER.warn("Skipping bad option: {}", (Object)s);
                }
            }

            nbttagcompound = dataFix(nbttagcompound);

            for (String s1 : nbttagcompound.getKeySet())
            {
                String s2 = nbttagcompound.getString(s1);

                try
                {
                    if ("mouseSensitivity".equals(s1))
                    {
                        mouseSensitivity = parseFloat(s2);
                    }

                    if ("fov".equals(s1))
                    {
                        fovSetting = parseFloat(s2) * 40.0F + 70.0F;
                    }

                    if ("gamma".equals(s1))
                    {
                        gammaSetting = parseFloat(s2);
                    }

                    if ("saturation".equals(s1))
                    {
                        saturation = parseFloat(s2);
                    }

                    if ("invertYMouse".equals(s1))
                    {
                        invertMouse = "true".equals(s2);
                    }

                    if ("renderDistance".equals(s1))
                    {
                        renderDistanceChunks = Integer.parseInt(s2);
                    }

                    if ("guiScale".equals(s1))
                    {
                        guiScale = Integer.parseInt(s2);
                    }

                    if ("particles".equals(s1))
                    {
                        particleSetting = Integer.parseInt(s2);
                    }

                    if ("bobView".equals(s1))
                    {
                        viewBobbing = "true".equals(s2);
                    }

                    if ("anaglyph3d".equals(s1))
                    {
                        anaglyph = "true".equals(s2);
                    }

                    if ("maxFps".equals(s1))
                    {
                        limitFramerate = Integer.parseInt(s2);
                    }

                    if ("fboEnable".equals(s1))
                    {
                        fboEnable = "true".equals(s2);
                    }

                    if ("difficulty".equals(s1))
                    {
                        difficulty = EnumDifficulty.getDifficultyEnum(Integer.parseInt(s2));
                    }

                    if ("fancyGraphics".equals(s1))
                    {
                        fancyGraphics = "true".equals(s2);
                    }

                    if ("tutorialStep".equals(s1))
                    {
                        tutorialStep = TutorialSteps.getTutorial(s2);
                    }

                    if ("ao".equals(s1))
                    {
                        if ("true".equals(s2))
                        {
                            ambientOcclusion = 2;
                        }
                        else if ("false".equals(s2))
                        {
                            ambientOcclusion = 0;
                        }
                        else
                        {
                            ambientOcclusion = Integer.parseInt(s2);
                        }
                    }

                    if ("renderClouds".equals(s1))
                    {
                        if ("true".equals(s2))
                        {
                            clouds = 2;
                        }
                        else if ("false".equals(s2))
                        {
                            clouds = 0;
                        }
                        else if ("fast".equals(s2))
                        {
                            clouds = 1;
                        }
                    }

                    if ("attackIndicator".equals(s1))
                    {
                        if ("0".equals(s2))
                        {
                            attackIndicator = 0;
                        }
                        else if ("1".equals(s2))
                        {
                            attackIndicator = 1;
                        }
                        else if ("2".equals(s2))
                        {
                            attackIndicator = 2;
                        }
                    }

                    if ("resourcePacks".equals(s1))
                    {
                        resourcePacks = (List)JsonUtils.gsonDeserialize(GSON, s2, TYPE_LIST_STRING);

                        if (resourcePacks == null)
                        {
                            resourcePacks = Lists.<String>newArrayList();
                        }
                    }

                    if ("incompatibleResourcePacks".equals(s1))
                    {
                        incompatibleResourcePacks = (List)JsonUtils.gsonDeserialize(GSON, s2, TYPE_LIST_STRING);

                        if (incompatibleResourcePacks == null)
                        {
                            incompatibleResourcePacks = Lists.<String>newArrayList();
                        }
                    }

                    if ("lastServer".equals(s1))
                    {
                        lastServer = s2;
                    }

                    if ("lang".equals(s1))
                    {
                        language = s2;
                    }

                    if ("chatVisibility".equals(s1))
                    {
                        chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility(Integer.parseInt(s2));
                    }

                    if ("chatColors".equals(s1))
                    {
                        chatColours = "true".equals(s2);
                    }

                    if ("chatLinks".equals(s1))
                    {
                        chatLinks = "true".equals(s2);
                    }

                    if ("chatLinksPrompt".equals(s1))
                    {
                        chatLinksPrompt = "true".equals(s2);
                    }

                    if ("chatOpacity".equals(s1))
                    {
                        chatOpacity = parseFloat(s2);
                    }

                    if ("snooperEnabled".equals(s1))
                    {
                        snooperEnabled = "true".equals(s2);
                    }

                    if ("fullscreen".equals(s1))
                    {
                        fullScreen = "true".equals(s2);
                    }

                    if ("enableVsync".equals(s1))
                    {
                        enableVsync = "true".equals(s2);
                    }

                    if ("useVbo".equals(s1))
                    {
                        useVbo = "true".equals(s2);
                    }

                    if ("hideServerAddress".equals(s1))
                    {
                        hideServerAddress = "true".equals(s2);
                    }

                    if ("advancedItemTooltips".equals(s1))
                    {
                        advancedItemTooltips = "true".equals(s2);
                    }

                    if ("pauseOnLostFocus".equals(s1))
                    {
                        pauseOnLostFocus = "true".equals(s2);
                    }

                    if ("touchscreen".equals(s1))
                    {
                        touchscreen = "true".equals(s2);
                    }

                    if ("overrideHeight".equals(s1))
                    {
                        overrideHeight = Integer.parseInt(s2);
                    }

                    if ("overrideWidth".equals(s1))
                    {
                        overrideWidth = Integer.parseInt(s2);
                    }

                    if ("heldItemTooltips".equals(s1))
                    {
                        heldItemTooltips = "true".equals(s2);
                    }

                    if ("chatHeightFocused".equals(s1))
                    {
                        chatHeightFocused = parseFloat(s2);
                    }

                    if ("chatHeightUnfocused".equals(s1))
                    {
                        chatHeightUnfocused = parseFloat(s2);
                    }

                    if ("chatScale".equals(s1))
                    {
                        chatScale = parseFloat(s2);
                    }

                    if ("chatWidth".equals(s1))
                    {
                        chatWidth = parseFloat(s2);
                    }

                    if ("mipmapLevels".equals(s1))
                    {
                        mipmapLevels = Integer.parseInt(s2);
                    }

                    if ("forceUnicodeFont".equals(s1))
                    {
                        forceUnicodeFont = "true".equals(s2);
                    }

                    if ("reducedDebugInfo".equals(s1))
                    {
                        reducedDebugInfo = "true".equals(s2);
                    }

                    if ("useNativeTransport".equals(s1))
                    {
                        useNativeTransport = "true".equals(s2);
                    }

                    if ("entityShadows".equals(s1))
                    {
                        entityShadows = "true".equals(s2);
                    }

                    if ("mainHand".equals(s1))
                    {
                        mainHand = "left".equals(s2) ? EnumHandSide.LEFT : EnumHandSide.RIGHT;
                    }

                    if ("showSubtitles".equals(s1))
                    {
                        showSubtitles = "true".equals(s2);
                    }

                    if ("realmsNotifications".equals(s1))
                    {
                        realmsNotifications = "true".equals(s2);
                    }

                    if ("enableWeakAttacks".equals(s1))
                    {
                        enableWeakAttacks = "true".equals(s2);
                    }

                    if ("autoJump".equals(s1))
                    {
                        autoJump = "true".equals(s2);
                    }

                    if ("narrator".equals(s1))
                    {
                        narrator = Integer.parseInt(s2);
                    }

                    for (KeyBinding keybinding : keyBindings)
                    {
                        if (s1.equals("key_" + keybinding.getKeyDescription()))
                        {
                            keybinding.setKeyCode(Integer.parseInt(s2));
                        }
                    }

                    for (SoundCategory soundcategory : SoundCategory.values())
                    {
                        if (s1.equals("soundCategory_" + soundcategory.getName()))
                        {
                            soundLevels.put(soundcategory, Float.valueOf(parseFloat(s2)));
                        }
                    }

                    for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values())
                    {
                        if (s1.equals("modelPart_" + enumplayermodelparts.getPartName()))
                        {
                            setModelPartEnabled(enumplayermodelparts, "true".equals(s2));
                        }
                    }
                }
                catch (Exception var11)
                {
                    LOGGER.warn("Skipping bad option: {}:{}", s1, s2);
                }
            }

            KeyBinding.resetKeyBindingArrayAndHash();
        }
        catch (Exception exception)
        {
            LOGGER.error("Failed to load options", (Throwable)exception);
        }
    }

    private NBTTagCompound dataFix(NBTTagCompound p_189988_1_)
    {
        int i = 0;

        try
        {
            i = Integer.parseInt(p_189988_1_.getString("version"));
        }
        catch (RuntimeException var4)
        {
            ;
        }

        return mc.getDataFixer().process(FixTypes.OPTIONS, p_189988_1_, i);
    }

    /**
     * Parses a string into a float.
     */
    private float parseFloat(String str)
    {
        if ("true".equals(str))
        {
            return 1.0F;
        }
        else
        {
            return "false".equals(str) ? 0.0F : Float.parseFloat(str);
        }
    }

    /**
     * Saves the options to the options file.
     */
    public void saveOptions()
    {
        PrintWriter printwriter = null;

        try
        {
            printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(optionsFile), StandardCharsets.UTF_8));
            printwriter.println("version:1343");
            printwriter.println("invertYMouse:" + invertMouse);
            printwriter.println("mouseSensitivity:" + mouseSensitivity);
            printwriter.println("fov:" + (fovSetting - 70.0F) / 40.0F);
            printwriter.println("gamma:" + gammaSetting);
            printwriter.println("saturation:" + saturation);
            printwriter.println("renderDistance:" + renderDistanceChunks);
            printwriter.println("guiScale:" + guiScale);
            printwriter.println("particles:" + particleSetting);
            printwriter.println("bobView:" + viewBobbing);
            printwriter.println("anaglyph3d:" + anaglyph);
            printwriter.println("maxFps:" + limitFramerate);
            printwriter.println("fboEnable:" + fboEnable);
            printwriter.println("difficulty:" + difficulty.getDifficultyId());
            printwriter.println("fancyGraphics:" + fancyGraphics);
            printwriter.println("ao:" + ambientOcclusion);

            switch (clouds)
            {
                case 0:
                    printwriter.println("renderClouds:false");
                    break;

                case 1:
                    printwriter.println("renderClouds:fast");
                    break;

                case 2:
                    printwriter.println("renderClouds:true");
            }

            printwriter.println("resourcePacks:" + GSON.toJson(resourcePacks));
            printwriter.println("incompatibleResourcePacks:" + GSON.toJson(incompatibleResourcePacks));
            printwriter.println("lastServer:" + lastServer);
            printwriter.println("lang:" + language);
            printwriter.println("chatVisibility:" + chatVisibility.getChatVisibility());
            printwriter.println("chatColors:" + chatColours);
            printwriter.println("chatLinks:" + chatLinks);
            printwriter.println("chatLinksPrompt:" + chatLinksPrompt);
            printwriter.println("chatOpacity:" + chatOpacity);
            printwriter.println("snooperEnabled:" + snooperEnabled);
            printwriter.println("fullscreen:" + fullScreen);
            printwriter.println("enableVsync:" + enableVsync);
            printwriter.println("useVbo:" + useVbo);
            printwriter.println("hideServerAddress:" + hideServerAddress);
            printwriter.println("advancedItemTooltips:" + advancedItemTooltips);
            printwriter.println("pauseOnLostFocus:" + pauseOnLostFocus);
            printwriter.println("touchscreen:" + touchscreen);
            printwriter.println("overrideWidth:" + overrideWidth);
            printwriter.println("overrideHeight:" + overrideHeight);
            printwriter.println("heldItemTooltips:" + heldItemTooltips);
            printwriter.println("chatHeightFocused:" + chatHeightFocused);
            printwriter.println("chatHeightUnfocused:" + chatHeightUnfocused);
            printwriter.println("chatScale:" + chatScale);
            printwriter.println("chatWidth:" + chatWidth);
            printwriter.println("mipmapLevels:" + mipmapLevels);
            printwriter.println("forceUnicodeFont:" + forceUnicodeFont);
            printwriter.println("reducedDebugInfo:" + reducedDebugInfo);
            printwriter.println("useNativeTransport:" + useNativeTransport);
            printwriter.println("entityShadows:" + entityShadows);
            printwriter.println("mainHand:" + (mainHand == EnumHandSide.LEFT ? "left" : "right"));
            printwriter.println("attackIndicator:" + attackIndicator);
            printwriter.println("showSubtitles:" + showSubtitles);
            printwriter.println("realmsNotifications:" + realmsNotifications);
            printwriter.println("enableWeakAttacks:" + enableWeakAttacks);
            printwriter.println("autoJump:" + autoJump);
            printwriter.println("narrator:" + narrator);
            printwriter.println("tutorialStep:" + tutorialStep.getName());

            for (KeyBinding keybinding : keyBindings)
            {
                printwriter.println("key_" + keybinding.getKeyDescription() + ":" + keybinding.getKeyCode());
            }

            for (SoundCategory soundcategory : SoundCategory.values())
            {
                printwriter.println("soundCategory_" + soundcategory.getName() + ":" + getSoundLevel(soundcategory));
            }

            for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values())
            {
                printwriter.println("modelPart_" + enumplayermodelparts.getPartName() + ":" + setModelParts.contains(enumplayermodelparts));
            }
        }
        catch (Exception exception)
        {
            LOGGER.error("Failed to save options", (Throwable)exception);
        }
        finally
        {
            IOUtils.closeQuietly((Writer)printwriter);
        }

        sendSettingsToServer();
    }

    public float getSoundLevel(SoundCategory category)
    {
        return soundLevels.containsKey(category) ? ((Float) soundLevels.get(category)).floatValue() : 1.0F;
    }

    public void setSoundLevel(SoundCategory category, float volume)
    {
        mc.getSoundHandler().setSoundLevel(category, volume);
        soundLevels.put(category, Float.valueOf(volume));
    }

    /**
     * Send a client info packet with settings information to the server
     */
    public void sendSettingsToServer()
    {
        if (mc.player != null)
        {
            int i = 0;

            for (EnumPlayerModelParts enumplayermodelparts : setModelParts)
            {
                i |= enumplayermodelparts.getPartMask();
            }

            mc.player.connection.sendPacket(new CPacketClientSettings(language, renderDistanceChunks, chatVisibility, chatColours, i, mainHand));
        }
    }

    public Set<EnumPlayerModelParts> getModelParts()
    {
        return ImmutableSet.copyOf(setModelParts);
    }

    public void setModelPartEnabled(EnumPlayerModelParts modelPart, boolean enable)
    {
        if (enable)
        {
            setModelParts.add(modelPart);
        }
        else
        {
            setModelParts.remove(modelPart);
        }

        sendSettingsToServer();
    }

    public void switchModelPartEnabled(EnumPlayerModelParts modelPart)
    {
        if (getModelParts().contains(modelPart))
        {
            setModelParts.remove(modelPart);
        }
        else
        {
            setModelParts.add(modelPart);
        }

        sendSettingsToServer();
    }

    /**
     * Return true if the clouds should be rendered
     */
    public int shouldRenderClouds()
    {
        return renderDistanceChunks >= 4 ? clouds : 0;
    }

    /**
     * Return true if the client connect to a server using the native transport system
     */
    public boolean isUsingNativeTransport()
    {
        return useNativeTransport;
    }

    public static enum Options
    {
        INVERT_MOUSE("options.invertMouse", false, true),
        SENSITIVITY("options.sensitivity", true, false),
        FOV("options.fov", true, false, 30.0F, 110.0F, 1.0F),
        GAMMA("options.gamma", true, false),
        SATURATION("options.saturation", true, false),
        RENDER_DISTANCE("options.renderDistance", true, false, 2.0F, 16.0F, 1.0F),
        VIEW_BOBBING("options.viewBobbing", false, true),
        ANAGLYPH("options.anaglyph", false, true),
        FRAMERATE_LIMIT("options.framerateLimit", true, false, 10.0F, 260.0F, 10.0F),
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
        SNOOPER_ENABLED("options.snooper", false, true),
        USE_FULLSCREEN("options.fullscreen", false, true),
        ENABLE_VSYNC("options.vsync", false, true),
        USE_VBO("options.vbo", false, true),
        TOUCHSCREEN("options.touchscreen", false, true),
        CHAT_SCALE("options.chat.scale", true, false),
        CHAT_WIDTH("options.chat.width", true, false),
        CHAT_HEIGHT_FOCUSED("options.chat.height.focused", true, false),
        CHAT_HEIGHT_UNFOCUSED("options.chat.height.unfocused", true, false),
        MIPMAP_LEVELS("options.mipmapLevels", true, false, 0.0F, 4.0F, 1.0F),
        FORCE_UNICODE_FONT("options.forceUnicodeFont", false, true),
        REDUCED_DEBUG_INFO("options.reducedDebugInfo", false, true),
        ENTITY_SHADOWS("options.entityShadows", false, true),
        MAIN_HAND("options.mainHand", false, false),
        ATTACK_INDICATOR("options.attackIndicator", false, false),
        ENABLE_WEAK_ATTACKS("options.enableWeakAttacks", false, true),
        SHOW_SUBTITLES("options.showSubtitles", false, true),
        REALMS_NOTIFICATIONS("options.realmsNotifications", false, true),
        AUTO_JUMP("options.autoJump", false, true),
        NARRATOR("options.narrator", false, false);

        private final boolean isFloat;
        private final boolean isBoolean;
        private final String translation;
        private final float valueStep;
        private float valueMin;
        private float valueMax;

        public static GameSettings.Options byOrdinal(int ordinal)
        {
            for (GameSettings.Options gamesettings$options : values())
            {
                if (gamesettings$options.getOrdinal() == ordinal)
                {
                    return gamesettings$options;
                }
            }

            return null;
        }

        private Options(String translation, boolean isFloat, boolean isBoolean)
        {
            this(translation, isFloat, isBoolean, 0.0F, 1.0F, 0.0F);
        }

        private Options(String translation, boolean isFloat, boolean isBoolean, float valMin, float valMax, float valStep)
        {
            this.translation = translation;
            this.isFloat = isFloat;
            this.isBoolean = isBoolean;
            valueMin = valMin;
            valueMax = valMax;
            valueStep = valStep;
        }

        public boolean isFloat()
        {
            return isFloat;
        }

        public boolean isBoolean()
        {
            return isBoolean;
        }

        public int getOrdinal()
        {
            return ordinal();
        }

        public String getTranslation()
        {
            return translation;
        }

        public float getValueMin()
        {
            return valueMin;
        }

        public float getValueMax()
        {
            return valueMax;
        }

        public void setValueMax(float value)
        {
            valueMax = value;
        }

        public float normalizeValue(float value)
        {
            return MathHelper.clamp((snapToStepClamp(value) - valueMin) / (valueMax - valueMin), 0.0F, 1.0F);
        }

        public float denormalizeValue(float value)
        {
            return snapToStepClamp(valueMin + (valueMax - valueMin) * MathHelper.clamp(value, 0.0F, 1.0F));
        }

        public float snapToStepClamp(float value)
        {
            value = snapToStep(value);
            return MathHelper.clamp(value, valueMin, valueMax);
        }

        private float snapToStep(float value)
        {
            if (valueStep > 0.0F)
            {
                value = valueStep * (float)Math.round(value / valueStep);
            }

            return value;
        }
    }
}
