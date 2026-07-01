package net.minecraft.client.audio;

import com.google.common.collect.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.lwjgl.openal.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.*;
import java.util.Map.Entry;

public class SoundManager {
    private static final Marker LOG_MARKER = MarkerManager.getMarker("SOUNDS");
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Set<ResourceLocation> UNABLE_TO_PLAY = Sets.newHashSet();

    private final SoundHandler sndHandler;
    private final GameSettings options;

    private final BiMap<String, ISound> playingSounds = HashBiMap.create();
    private final BiMap<ISound, String> invPlayingSounds;
    private final Multimap<SoundCategory, String> categorySounds;
    private final List<ITickableSound> tickableSounds;
    private final Map<ISound, Integer> delayedSounds;
    private final Map<String, Integer> playingSoundsStopTime;
    private final List<ISoundEventListener> listeners;
    private final List<String> pausedChannels;

    private final Map<ResourceLocation, Integer> soundBuffers = Maps.newHashMap();
    private final List<SoundSource> sources = Lists.newArrayList();
    private final Map<String, SoundSource> playingSources = Maps.newHashMap();

    private long alcDevice;
    private long alcContext;
    private ALCCapabilities deviceCaps;
    private final ThreadLocal<Boolean> contextCurrent = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private boolean loaded;
    private int playTime;

    public SoundManager(SoundHandler handler, GameSettings options) {
        invPlayingSounds = playingSounds.inverse();
        categorySounds = HashMultimap.create();
        tickableSounds = Lists.newArrayList();
        delayedSounds = Maps.newHashMap();
        playingSoundsStopTime = Maps.newHashMap();
        listeners = Lists.newArrayList();
        pausedChannels = Lists.newArrayList();
        sndHandler = handler;
        this.options = options;
    }

    private void checkContext() {
        if (loaded && !contextCurrent.get()) {
            ALC10.alcMakeContextCurrent(alcContext);
            AL.createCapabilities(deviceCaps);
            contextCurrent.set(Boolean.TRUE);
        }
    }

    public void reloadSoundSystem() {
        UNABLE_TO_PLAY.clear();
        for (SoundEvent soundevent : SoundEvent.REGISTRY) {
            ResourceLocation resourcelocation = soundevent.soundName();
            if (sndHandler.getAccessor(resourcelocation) == null) {
                LOGGER.warn("Missing sound for event: {}", SoundEvent.REGISTRY.getNameForObject(soundevent));
                UNABLE_TO_PLAY.add(resourcelocation);
            }
        }
        unloadSoundSystem();
        loadSoundSystem();
    }

    private synchronized void loadSoundSystem() {
        if (!loaded) {
            try {
                new Thread(() -> {
                    try {
                        alcDevice = ALC10.alcOpenDevice((java.nio.ByteBuffer) null);
                        if (alcDevice == 0) {
                            throw new RuntimeException("Failed to open default OpenAL device.");
                        }
                        deviceCaps = ALC.createCapabilities(alcDevice);
                        alcContext = ALC10.alcCreateContext(alcDevice, (IntBuffer) null);
                        if (alcContext == 0) {
                            throw new RuntimeException("Failed to create OpenAL context.");
                        }
                        
                        ALC10.alcMakeContextCurrent(alcContext);
                        AL.createCapabilities(deviceCaps);

                        // Set distance model
                        AL10.alDistanceModel(AL11.AL_LINEAR_DISTANCE_CLAMPED);

                        // Allocate sources
                        for (int i = 0; i < 256; i++) {
                            SoundSource src = new SoundSource();
                            if (src.getSourceId() == 0) {
                                break;
                            }
                            sources.add(src);
                        }

                        // Set master volume on loader thread
                        AL10.alListenerf(AL10.AL_GAIN, options.getSoundLevel(SoundCategory.MASTER));

                        ALC10.alcMakeContextCurrent(0);

                        loaded = true;
                        LOGGER.info(LOG_MARKER, "OpenAL Sound Engine initialized");
                    } catch (Exception e) {
                        LOGGER.error(LOG_MARKER, "Error starting OpenAL. Turning off sounds & music", e);
                        options.setSoundLevel(SoundCategory.MASTER, 0F);
                        options.saveOptions();
                    }
                }, "Sound Library Loader").start();
            } catch (RuntimeException runtimeexception) {
                LOGGER.error(LOG_MARKER, "Error starting SoundSystem. Turning off sounds & music", runtimeexception);
                options.setSoundLevel(SoundCategory.MASTER, 0F);
                options.saveOptions();
            }
        }
    }

    private float getVolume(SoundCategory category) {
        return category != null && category != SoundCategory.MASTER ? options.getSoundLevel(category) : 1F;
    }

    public void setVolume(SoundCategory category, float volume) {
        if (loaded) {
            checkContext();
            if (category == SoundCategory.MASTER) {
                setMasterVolume(volume);
            } else {
                for (String s : categorySounds.get(category)) {
                    ISound isound = playingSounds.get(s);
                    float f = getClampedVolume(isound);
                    if (f <= 0F) {
                        stopSound(isound);
                    } else {
                        SoundSource src = playingSources.get(s);
                        if (src != null) {
                            src.setVolume(f);
                        }
                    }
                }
            }
        }
    }

    private void setMasterVolume(float volume) {
        AL10.alListenerf(AL10.AL_GAIN, volume);
    }

    public void unloadSoundSystem() {
        if (loaded) {
            checkContext();
            stopAllSounds();
            for (SoundSource src : sources) {
                src.cleanup();
            }
            sources.clear();
            playingSources.clear();

            for (int bufId : soundBuffers.values()) {
                AL10.alDeleteBuffers(bufId);
            }
            soundBuffers.clear();

            if (alcContext != 0) {
                ALC10.alcMakeContextCurrent(0);
                ALC10.alcDestroyContext(alcContext);
                alcContext = 0;
            }
            if (alcDevice != 0) {
                ALC10.alcCloseDevice(alcDevice);
                alcDevice = 0;
            }
            contextCurrent.remove();
            loaded = false;
        }
    }

    public void stopAllSounds() {
        if (loaded) {
            checkContext();
            for (SoundSource src : playingSources.values()) {
                src.stop();
            }
            playingSources.clear();
            playingSounds.clear();
            delayedSounds.clear();
            tickableSounds.clear();
            categorySounds.clear();
            playingSoundsStopTime.clear();
        }
    }

    public void addListener(ISoundEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ISoundEventListener listener) {
        listeners.remove(listener);
    }

    public void updateAllSounds() {
        ++playTime;

        if (loaded) {
            checkContext();

            for (ITickableSound itickablesound : com.google.common.collect.Lists.newArrayList(tickableSounds)) {
                itickablesound.update();
                if (itickablesound.isDonePlaying()) {
                    stopSound(itickablesound);
                } else {
                    String s = invPlayingSounds.get(itickablesound);
                    SoundSource src = playingSources.get(s);
                    if (src != null) {
                        src.setVolume(getClampedVolume(itickablesound));
                        src.setPitch(getClampedPitch(itickablesound));
                        src.setPosition(itickablesound.getX(), itickablesound.getY(), itickablesound.getZ());
                    }
                }
            }

            List<Entry<String, ISound>> playingCopy = com.google.common.collect.Lists.newArrayList(playingSounds.entrySet());
            for (Entry<String, ISound> entry : playingCopy) {
                String s1 = entry.getKey();
                ISound isound = entry.getValue();
                SoundSource src = playingSources.get(s1);

                if (src == null || !src.isPlaying()) {
                    int j = isound.getRepeatDelay();
                    if (isound.canRepeat() && j > 0) {
                        delayedSounds.put(isound, playTime + j);
                    }
                    playingSounds.remove(s1);
                    LOGGER.debug(LOG_MARKER, "Removed channel {} because it's not playing anymore", s1);
                    if (src != null) {
                        src.stop();
                        playingSources.remove(s1);
                    }
                    playingSoundsStopTime.remove(s1);
                    try {
                        categorySounds.remove(isound.getCategory(), s1);
                    } catch (RuntimeException ignored) {
                    }
                    if (isound instanceof ITickableSound) {
                        tickableSounds.remove(isound);
                    }
                } else {
                    src.updateStream();
                }
            }

            Iterator<Entry<ISound, Integer>> iterator1 = delayedSounds.entrySet().iterator();
            while (iterator1.hasNext()) {
                Entry<ISound, Integer> entry1 = iterator1.next();
                if (playTime >= entry1.getValue()) {
                    ISound isound1 = entry1.getKey();
                    if (isound1 instanceof ITickableSound) {
                        ((ITickableSound) isound1).update();
                    }
                    playSound(isound1);
                    iterator1.remove();
                }
            }
        }
    }

    public boolean isSoundPlaying(ISound sound) {
        if (!loaded) {
            return false;
        }
        checkContext();
        String s = invPlayingSounds.get(sound);
        if (s == null) {
            return false;
        }
        SoundSource src = playingSources.get(s);
        return src != null && src.isPlaying();
    }

    public void stopSound(ISound sound) {
        if (loaded) {
            checkContext();
            String s = invPlayingSounds.get(sound);
            if (s != null) {
                SoundSource src = playingSources.remove(s);
                if (src != null) {
                    src.stop();
                }
                playingSounds.remove(s);
                playingSoundsStopTime.remove(s);
                categorySounds.remove(sound.getCategory(), s);
                if (sound instanceof ITickableSound) {
                    tickableSounds.remove(sound);
                }
            }
        }
    }

    public void playSound(ISound p_sound) {
        if (loaded) {
            checkContext();
            SoundEventAccessor soundeventaccessor = p_sound.createAccessor(sndHandler);
            ResourceLocation resourcelocation = p_sound.getSoundLocation();

            if (soundeventaccessor == null) {
                if (UNABLE_TO_PLAY.add(resourcelocation)) {
                    LOGGER.warn(LOG_MARKER, "Unable to play unknown soundEvent: {}", resourcelocation);
                }
            } else {
                if (!listeners.isEmpty()) {
                    for (ISoundEventListener isoundeventlistener : listeners) {
                        isoundeventlistener.soundPlay(p_sound, soundeventaccessor);
                    }
                }

                if (options.getSoundLevel(SoundCategory.MASTER) <= 0F) {
                    LOGGER.debug(LOG_MARKER, "Skipped playing soundEvent: {}, master volume was zero", resourcelocation);
                } else {
                    Sound sound = p_sound.getSound();
                    if (sound == SoundHandler.MISSING_SOUND) {
                        if (UNABLE_TO_PLAY.add(resourcelocation)) {
                            LOGGER.warn(LOG_MARKER, "Unable to play empty soundEvent: {}", resourcelocation);
                        }
                    } else {
                        float f3 = p_sound.getVolume();
                        float f = 16F;
                        if (f3 > 1F) {
                            f *= f3;
                        }

                        SoundCategory soundcategory = p_sound.getCategory();
                        float f1 = getClampedVolume(p_sound);
                        float f2 = getClampedPitch(p_sound);

                        if (f1 == 0F) {
                            LOGGER.debug(LOG_MARKER, "Skipped playing sound {}, volume was zero.", sound.name());
                        } else {
                            boolean loop = p_sound.canRepeat() && p_sound.getRepeatDelay() == 0;
                            String channelId = UUID.randomUUID().toString();
                            ResourceLocation oggLoc = sound.getOggLocation();

                            SoundSource src = getFreeSource();
                            if (src == null) {
                                LOGGER.warn("Failed to play sound: no free OpenAL sources.");
                                return;
                            }

                            try {
                                InputStream input = Minecraft.getMinecraft().getResourceManager().getResource(oggLoc).getInputStream();
                                if (sound.streaming()) {
                                    OggAudioStream stream = new OggAudioStream(input);
                                    src.playStream(stream, f2, f1, p_sound.getX(), p_sound.getY(), p_sound.getZ(), loop, p_sound.getAttenuationType().getType(), f);
                                } else {
                                    int bufferId;
                                    if (soundBuffers.containsKey(oggLoc)) {
                                        bufferId = soundBuffers.get(oggLoc);
                                    } else {
                                        try (OggAudioStream stream = new OggAudioStream(input)) {
                                            ShortBuffer data = stream.readAll();
                                            bufferId = AL10.alGenBuffers();
                                            AL10.alBufferData(bufferId, stream.getFormat(), data, stream.getSampleRate());
                                            soundBuffers.put(oggLoc, bufferId);
                                        }
                                    }
                                    src.playStatic(bufferId, f2, f1, p_sound.getX(), p_sound.getY(), p_sound.getZ(), loop, p_sound.getAttenuationType().getType(), f);
                                }

                                LOGGER.debug(LOG_MARKER, "Playing sound {} for event {} as channel {}", sound.name(), resourcelocation, channelId);
                                playingSoundsStopTime.put(channelId, playTime + 20);
                                playingSounds.put(channelId, p_sound);
                                playingSources.put(channelId, src);
                                categorySounds.put(soundcategory, channelId);

                                if (p_sound instanceof ITickableSound) {
                                    tickableSounds.add((ITickableSound) p_sound);
                                }
                            } catch (IOException e) {
                                LOGGER.error("Failed to load sound resource {}", oggLoc, e);
                            }
                        }
                    }
                }
            }
        }
    }

    private SoundSource getFreeSource() {
        for (SoundSource src : sources) {
            if (!playingSources.containsValue(src)) {
                src.stop();
                return src;
            }
        }
        return null;
    }

    private float getClampedPitch(ISound soundIn) {
        return MathHelper.clamp(soundIn.getPitch(), 0.5F, 2F);
    }

    private float getClampedVolume(ISound soundIn) {
        return MathHelper.clamp(soundIn.getVolume() * getVolume(soundIn.getCategory()), 0F, 1F);
    }

    public void pauseAllSounds() {
        if (loaded) {
            checkContext();
            for (Entry<String, ISound> entry : playingSounds.entrySet()) {
                String s = entry.getKey();
                SoundSource src = playingSources.get(s);
                if (src != null && src.isPlaying()) {
                    LOGGER.debug(LOG_MARKER, "Pausing channel {}", s);
                    AL10.alSourcePause(src.getSourceId());
                    pausedChannels.add(s);
                }
            }
        }
    }

    public void resumeAllSounds() {
        if (loaded) {
            checkContext();
            for (String s : pausedChannels) {
                SoundSource src = playingSources.get(s);
                if (src != null) {
                    LOGGER.debug(LOG_MARKER, "Resuming channel {}", s);
                    AL10.alSourcePlay(src.getSourceId());
                }
            }
            pausedChannels.clear();
        }
    }

    public void playDelayedSound(ISound sound, int delay) {
        delayedSounds.put(sound, playTime + delay);
    }

    public void setListener(EntityPlayer player, float partialTicks) {
        if (loaded && player != null) {
            checkContext();
            float f = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
            float f1 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;
            double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks;
            double d1 = player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks + (double) player.getEyeHeight();
            double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks;

            float f2 = MathHelper.cos((f1 + 90F) * 0.017453292F);
            float f3 = MathHelper.sin((f1 + 90F) * 0.017453292F);
            float f4 = MathHelper.cos(-f * 0.017453292F);
            float f5 = MathHelper.sin(-f * 0.017453292F);
            float f6 = MathHelper.cos((-f + 90F) * 0.017453292F);
            float f7 = MathHelper.sin((-f + 90F) * 0.017453292F);

            float f8 = f2 * f4;
            float f9 = f3 * f4;
            float f10 = f2 * f6;
            float f11 = f3 * f6;

            AL10.alListener3f(AL10.AL_POSITION, (float) d0, (float) d1, (float) d2);
            AL10.alListenerfv(AL10.AL_ORIENTATION, new float[]{f8, f5, f9, f10, f7, f11});
        }
    }

    public void stop(String p_189567_1_, SoundCategory p_189567_2_) {
        if (loaded) {
            checkContext();
            if (p_189567_2_ != null) {
                for (String s : categorySounds.get(p_189567_2_)) {
                    ISound isound = playingSounds.get(s);
                    if (p_189567_1_.isEmpty()) {
                        stopSound(isound);
                    } else if (isound.getSoundLocation().equals(new ResourceLocation(p_189567_1_))) {
                        stopSound(isound);
                    }
                }
            } else if (p_189567_1_.isEmpty()) {
                stopAllSounds();
            } else {
                for (ISound isound1 : playingSounds.values()) {
                    if (isound1.getSoundLocation().equals(new ResourceLocation(p_189567_1_))) {
                        stopSound(isound1);
                    }
                }
            }
        }
    }
}
