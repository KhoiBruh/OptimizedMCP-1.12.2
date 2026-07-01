package net.minecraft.client.audio;

import com.google.common.collect.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jspecify.annotations.NonNull;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.AL11;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SoundManager implements IResourceManagerReloadListener, ITickable {

	private static final Marker LOG_MARKER = MarkerManager.getMarker("SOUNDS");
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Set<ResourceLocation> UNABLE_TO_PLAY = ConcurrentHashMap.newKeySet();

	public static final Sound MISSING_SOUND = new Sound("meta:missing_sound", 1F, 1F, 1, Sound.Type.FILE, false);
	private static final Gson GSON = new GsonBuilder()
		.registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer())
		.registerTypeAdapter(SoundList.class, new SoundListSerializer())
		.create();
	private static final ParameterizedType SOUND_LIST_TYPE = new ParameterizedType() {
		public Type @NonNull [] getActualTypeArguments() { return new Type[]{String.class, SoundList.class}; }
		public @NonNull Type getRawType() { return Map.class; }
		public Type getOwnerType() { return null; }
	};

	private final SoundRegistry soundRegistry = new SoundRegistry();
	private final IResourceManager resourceManager;
	private final GameSettings options;
	private final MusicTicker musicTicker;

	private final LinkedBlockingQueue<Runnable> commandQueue = new LinkedBlockingQueue<>();
	private volatile boolean running;
	private Thread soundThread;
	private final AtomicBoolean loaded = new AtomicBoolean(false);
	private final CountDownLatch initLatch = new CountDownLatch(1);

	private final ConcurrentMap<String, ISound> activeSounds = new ConcurrentHashMap<>();

	private volatile float listenerX, listenerY, listenerZ;
	private volatile float listenerYaw, listenerPitch;
	private volatile boolean listenerDirty;

	private final List<ISoundEventListener> listeners = new ArrayList<>();

	private long alcDevice;
	private long alcContext;
	private ALCCapabilities deviceCaps;
	private final List<SoundSource> sources = new ArrayList<>();
	private final Map<String, SoundSource> playingSources = new HashMap<>();
	private final BiMap<String, ISound> playingSounds = HashBiMap.create();
	private final BiMap<ISound, String> invPlayingSounds;
	private final Multimap<SoundCategory, String> categorySounds = HashMultimap.create();
	private final List<ITickableSound> tickableSounds = new ArrayList<>();
	private final Map<ISound, Integer> delayedSounds = new HashMap<>();
	private final Map<String, Integer> playingSoundsStopTime = new HashMap<>();
	private final Map<ResourceLocation, Integer> soundBuffers = new HashMap<>();
	private int playTime;

	public SoundManager(IResourceManager resourceManager, GameSettings options, Minecraft mc) {
		invPlayingSounds = playingSounds.inverse();
		this.resourceManager = resourceManager;
		this.options = options;
		musicTicker = new MusicTicker(mc);
		startSoundThread();
	}

	// ========== SOUND THREAD ==========

	private void startSoundThread() {
		running = true;
		soundThread = new Thread(this::soundThreadMain, "Sound Thread");
		soundThread.setDaemon(true);
		soundThread.start();
	}

	private void soundThreadMain() {
		if (!initOpenAL()) {
			loaded.set(false);
			initLatch.countDown();
			return;
		}
		loaded.set(true);
		initLatch.countDown();

		while (running) {
			Runnable cmd;
			while ((cmd = commandQueue.poll()) != null) {
				try {
					cmd.run();
				} catch (Exception e) {
					LOGGER.error(LOG_MARKER, "Error processing sound command", e);
				}
			}
			soundTick();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}

		cleanupOpenAL();
	}

	private boolean initOpenAL() {
		try {
			alcDevice = ALC10.alcOpenDevice((java.nio.ByteBuffer) null);
			if (alcDevice == 0) {
				LOGGER.error(LOG_MARKER, "Failed to open default OpenAL device.");
				return false;
			}
			deviceCaps = ALC.createCapabilities(alcDevice);
			alcContext = ALC10.alcCreateContext(alcDevice, (IntBuffer) null);
			if (alcContext == 0) {
				LOGGER.error(LOG_MARKER, "Failed to create OpenAL context.");
				ALC10.alcCloseDevice(alcDevice);
				alcDevice = 0;
				return false;
			}
			ALC10.alcMakeContextCurrent(alcContext);
			AL.createCapabilities(deviceCaps);
			AL10.alDistanceModel(AL11.AL_LINEAR_DISTANCE_CLAMPED);

			for (int i = 0; i < 256; i++) {
				SoundSource src = new SoundSource();
				if (src.getSourceId() == 0) break;
				sources.add(src);
			}

			AL10.alListenerf(AL10.AL_GAIN, options.getSoundLevel(SoundCategory.MASTER));
			LOGGER.info(LOG_MARKER, "OpenAL Sound Engine initialized on sound thread");
			return true;
		} catch (Exception e) {
			LOGGER.error(LOG_MARKER, "Error starting OpenAL", e);
			return false;
		}
	}

	private void cleanupOpenAL() {
		stopAllSoundsInternal();
		for (SoundSource src : sources) src.cleanup();
		sources.clear();
		playingSources.clear();
		for (int bufId : soundBuffers.values()) AL10.alDeleteBuffers(bufId);
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
		loaded.set(false);
	}

	// ========== COMMAND DISPATCH ==========

	private void execute(Runnable command) {
		if (!loaded.get()) return;
		commandQueue.offer(command);
	}

	// ========== LISTENER POSITION (main thread writes, sound thread reads) ==========

	public void setListener(EntityPlayer player, float partialTicks) {
		if (!loaded.get() || player == null) return;
		listenerPitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
		listenerYaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;
		listenerX = (float) (player.prevPosX + (player.posX - player.prevPosX) * partialTicks);
		listenerY = (float) (player.prevPosY + (player.posY - player.prevPosY) * partialTicks + player.getEyeHeight());
		listenerZ = (float) (player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks);
		listenerDirty = true;
	}

	// ========== SOUND TICK (sound thread) ==========

	private void soundTick() {
		++playTime;

		if (listenerDirty) {
			updateListenerPosition();
			listenerDirty = false;
		}

		for (ITickableSound tickable : com.google.common.collect.Lists.newArrayList(tickableSounds)) {
			tickable.update();
			if (tickable.isDonePlaying()) {
				stopSoundInternal(tickable);
				continue;
			}
			String channel = invPlayingSounds.get(tickable);
			if (channel != null) {
				SoundSource src = playingSources.get(channel);
				if (src != null) {
					src.setVolume(getClampedVolume(tickable));
					src.setPitch(getClampedPitch(tickable));
					src.setPosition(tickable.getX(), tickable.getY(), tickable.getZ());
				}
			}
		}

		List<Entry<String, ISound>> playingCopy = com.google.common.collect.Lists.newArrayList(playingSounds.entrySet());
		for (Entry<String, ISound> entry : playingCopy) {
			String channelId = entry.getKey();
			ISound sound = entry.getValue();
			SoundSource src = playingSources.get(channelId);

			if (src == null || !src.isPlaying()) {
				int delay = sound.getRepeatDelay();
				if (sound.canRepeat() && delay > 0) {
					delayedSounds.put(sound, playTime + delay);
				}
				removeSound(channelId);
			} else {
				src.updateStream();
			}
		}

		Iterator<Entry<ISound, Integer>> iter = delayedSounds.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<ISound, Integer> entry = iter.next();
			if (playTime >= entry.getValue()) {
				ISound sound = entry.getKey();
				if (sound instanceof ITickableSound) ((ITickableSound) sound).update();
				playSoundInternal(sound);
				iter.remove();
			}
		}
	}

	private void updateListenerPosition() {
		float f2 = MathHelper.cos((listenerYaw + 90F) * 0.017453292F);
		float f3 = MathHelper.sin((listenerYaw + 90F) * 0.017453292F);
		float f4 = MathHelper.cos(-listenerPitch * 0.017453292F);
		float f5 = MathHelper.sin(-listenerPitch * 0.017453292F);
		float f6 = MathHelper.cos((-listenerPitch + 90F) * 0.017453292F);
		float f7 = MathHelper.sin((-listenerPitch + 90F) * 0.017453292F);
		float f8 = f2 * f4;
		float f9 = f3 * f4;
		float f10 = f2 * f6;
		float f11 = f3 * f6;
		AL10.alListener3f(AL10.AL_POSITION, listenerX, listenerY, listenerZ);
		AL10.alListenerfv(AL10.AL_ORIENTATION, new float[]{f8, f5, f9, f10, f7, f11});
	}

	// ========== PLAY / STOP (internal, sound thread) ==========

	private void playSoundInternal(ISound sound) {
		SoundEventAccessor accessor = sound.createAccessor(this);
		ResourceLocation resourcelocation = sound.getSoundLocation();

		if (accessor == null) {
			if (UNABLE_TO_PLAY.add(resourcelocation)) {
				LOGGER.warn(LOG_MARKER, "Unable to play unknown soundEvent: {}", resourcelocation);
			}
			return;
		}

		if (options.getSoundLevel(SoundCategory.MASTER) <= 0F) {
			LOGGER.debug(LOG_MARKER, "Skipped playing soundEvent: {}, master volume was zero", resourcelocation);
			return;
		}

		Sound s = sound.getSound();
		if (s == MISSING_SOUND) {
			if (UNABLE_TO_PLAY.add(resourcelocation)) {
				LOGGER.warn(LOG_MARKER, "Unable to play empty soundEvent: {}", resourcelocation);
			}
			return;
		}

		float f3 = sound.getVolume();
		float f = 16F;
		if (f3 > 1F) f *= f3;

		SoundCategory soundcategory = sound.getCategory();
		float f1 = getClampedVolume(sound);
		float f2 = getClampedPitch(sound);

		if (f1 == 0F) {
			LOGGER.debug(LOG_MARKER, "Skipped playing sound {}, volume was zero.", s.name());
			return;
		}

		boolean loop = sound.canRepeat() && sound.getRepeatDelay() == 0;
		String channelId = UUID.randomUUID().toString();
		ResourceLocation oggLoc = s.getOggLocation();

		SoundSource src = getFreeSource();
		if (src == null) {
			LOGGER.warn("Failed to play sound: no free OpenAL sources.");
			return;
		}

		try {
			InputStream input = Minecraft.getMinecraft().getResourceManager().getResource(oggLoc).getInputStream();
			if (s.streaming()) {
				OggAudioStream stream = new OggAudioStream(input);
				src.playStream(stream, f2, f1, sound.getX(), sound.getY(), sound.getZ(), loop, sound.getAttenuationType().getType(), f);
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
				src.playStatic(bufferId, f2, f1, sound.getX(), sound.getY(), sound.getZ(), loop, sound.getAttenuationType().getType(), f);
			}

			LOGGER.debug(LOG_MARKER, "Playing sound {} for event {} as channel {}", s.name(), resourcelocation, channelId);
			playingSoundsStopTime.put(channelId, playTime + 20);
			playingSounds.put(channelId, sound);
			playingSources.put(channelId, src);
			activeSounds.put(channelId, sound);
			categorySounds.put(soundcategory, channelId);

			if (sound instanceof ITickableSound) {
				tickableSounds.add((ITickableSound) sound);
			}
		} catch (IOException e) {
			LOGGER.error("Failed to load sound resource {}", oggLoc, e);
		}
	}

	private void stopSoundInternal(ISound sound) {
		String channel = invPlayingSounds.get(sound);
		if (channel != null) {
			SoundSource src = playingSources.remove(channel);
			if (src != null) src.stop();
			playingSounds.remove(channel);
			playingSoundsStopTime.remove(channel);
			categorySounds.remove(sound.getCategory(), channel);
			activeSounds.remove(channel);
			if (sound instanceof ITickableSound) {
				tickableSounds.remove(sound);
			}
		}
	}

	private void stopAllSoundsInternal() {
		for (SoundSource src : playingSources.values()) src.stop();
		playingSources.clear();
		playingSounds.clear();
		activeSounds.clear();
		delayedSounds.clear();
		tickableSounds.clear();
		categorySounds.clear();
		playingSoundsStopTime.clear();
	}

	private void removeSound(String channelId) {
		ISound sound = playingSounds.remove(channelId);
		if (sound != null) {
			activeSounds.remove(channelId);
			SoundSource src = playingSources.remove(channelId);
			if (src != null) src.stop();
			playingSoundsStopTime.remove(channelId);
			categorySounds.remove(sound.getCategory(), channelId);
			if (sound instanceof ITickableSound) {
				tickableSounds.remove(sound);
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

	// ========== PUBLIC API (thread-safe, enqueue commands) ==========

	public SoundEventAccessor getAccessor(ResourceLocation location) {
		return soundRegistry.getObject(location);
	}

	public void playSound(ISound sound) {
		if (!loaded.get()) return;
		SoundEventAccessor accessor = sound.createAccessor(this);
		ResourceLocation location = sound.getSoundLocation();

		if (accessor == null) {
			if (UNABLE_TO_PLAY.add(location)) {
				LOGGER.warn(LOG_MARKER, "Unable to play unknown soundEvent: {}", location);
			}
			return;
		}

		if (!listeners.isEmpty()) {
			for (ISoundEventListener listener : listeners) {
				listener.soundPlay(sound, accessor);
			}
		}

		if (options.getSoundLevel(SoundCategory.MASTER) <= 0F) {
			LOGGER.debug(LOG_MARKER, "Skipped playing soundEvent: {}, master volume was zero", location);
			return;
		}

		Sound s = sound.getSound();
		if (s == MISSING_SOUND) {
			if (UNABLE_TO_PLAY.add(location)) {
				LOGGER.warn(LOG_MARKER, "Unable to play empty soundEvent: {}", location);
			}
			return;
		}

		float vol = sound.getVolume();
		float f = 16F;
		if (vol > 1F) f *= vol;

		SoundCategory category = sound.getCategory();
		float clampedVol = getClampedVolume(sound);

		if (clampedVol == 0F) {
			LOGGER.debug(LOG_MARKER, "Skipped playing sound {}, volume was zero.", s.name());
			return;
		}

		boolean loop = sound.canRepeat() && sound.getRepeatDelay() == 0;
		String channelId = UUID.randomUUID().toString();
		ResourceLocation oggLoc = s.getOggLocation();

		final float attenuationDist = f;
		final float effectiveVol = clampedVol;
		final float effectivePitch = getClampedPitch(sound);

		execute(() -> {
			SoundSource src = getFreeSource();
			if (src == null) {
				LOGGER.warn("Failed to play sound: no free OpenAL sources.");
				return;
			}
			try {
				InputStream input = Minecraft.getMinecraft().getResourceManager().getResource(oggLoc).getInputStream();
				if (s.streaming()) {
					OggAudioStream stream = new OggAudioStream(input);
					src.playStream(stream, effectivePitch, effectiveVol, sound.getX(), sound.getY(), sound.getZ(), loop, sound.getAttenuationType().getType(), attenuationDist);
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
					src.playStatic(bufferId, effectivePitch, effectiveVol, sound.getX(), sound.getY(), sound.getZ(), loop, sound.getAttenuationType().getType(), attenuationDist);
				}

				LOGGER.debug(LOG_MARKER, "Playing sound {} for event {} as channel {}", s.name(), location, channelId);
				playingSoundsStopTime.put(channelId, playTime + 20);
				playingSounds.put(channelId, sound);
				playingSources.put(channelId, src);
				activeSounds.put(channelId, sound);
				categorySounds.put(category, channelId);

				if (sound instanceof ITickableSound) {
					tickableSounds.add((ITickableSound) sound);
				}
			} catch (IOException e) {
				LOGGER.error("Failed to load sound resource {}", oggLoc, e);
			}
		});
	}

	public void stopSound(ISound sound) {
		activeSounds.values().remove(sound);
		execute(() -> stopSoundInternal(sound));
	}

	public void stopAllSounds() {
		activeSounds.clear();
		execute(this::stopAllSoundsInternal);
	}

	public void pauseAllSounds() {
		execute(() -> {
			for (Entry<String, ISound> entry : playingSounds.entrySet()) {
				String s = entry.getKey();
				SoundSource src = playingSources.get(s);
				if (src != null && src.isPlaying()) {
					LOGGER.debug(LOG_MARKER, "Pausing channel {}", s);
					AL10.alSourcePause(src.getSourceId());
				}
			}
		});
	}

	public void resumeAllSounds() {
		execute(() -> {
			for (Entry<String, ISound> entry : playingSounds.entrySet()) {
				String s = entry.getKey();
				SoundSource src = playingSources.get(s);
				if (src != null) {
					LOGGER.debug(LOG_MARKER, "Resuming channel {}", s);
					AL10.alSourcePlay(src.getSourceId());
				}
			}
		});
	}

	public boolean isSoundPlaying(ISound sound) {
		return activeSounds.containsValue(sound);
	}

	public void stop(String name, SoundCategory category) {
		execute(() -> {
			if (category != null) {
				for (String s : categorySounds.get(category)) {
					ISound sound = playingSounds.get(s);
					if (name.isEmpty()) {
						stopSoundInternal(sound);
					} else if (sound != null && sound.getSoundLocation().equals(new ResourceLocation(name))) {
						stopSoundInternal(sound);
					}
				}
			} else if (name.isEmpty()) {
				stopAllSoundsInternal();
			} else {
				for (ISound sound : playingSounds.values()) {
					if (sound.getSoundLocation().equals(new ResourceLocation(name))) {
						stopSoundInternal(sound);
					}
				}
			}
		});
	}

	public void playDelayedSound(ISound sound, int delay) {
		execute(() -> delayedSounds.put(sound, playTime + delay));
	}

	public void setVolume(SoundCategory category, float volume) {
		execute(() -> {
			if (category == SoundCategory.MASTER) {
				AL10.alListenerf(AL10.AL_GAIN, volume);
			} else {
				for (String s : categorySounds.get(category)) {
					ISound sound = playingSounds.get(s);
					if (sound == null) continue;
					float clamped = getClampedVolume(sound);
					if (clamped <= 0F) {
						stopSoundInternal(sound);
					} else {
						SoundSource src = playingSources.get(s);
						if (src != null) src.setVolume(clamped);
					}
				}
			}
		});
	}

	public void addListener(ISoundEventListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ISoundEventListener listener) {
		listeners.remove(listener);
	}

	// ========== RESOURCE RELOAD (IResourceManagerReloadListener) ==========

	public void onResourceManagerReload(IResourceManager manager) {
		soundRegistry.clearMap();

		for (String domain : manager.getResourceDomains()) {
			try {
				for (IResource resource : manager.getAllResources(new ResourceLocation(domain, "sounds.json"))) {
					try {
						Map<String, SoundList> map = getSoundMap(resource.getInputStream());
						for (Entry<String, SoundList> entry : map.entrySet()) {
							loadSoundResource(new ResourceLocation(domain, entry.getKey()), entry.getValue());
						}
					} catch (RuntimeException e) {
						LOGGER.warn("Invalid sounds.json", e);
					}
				}
			} catch (IOException ignored) {
			}
		}

		for (ResourceLocation location : soundRegistry.getKeys()) {
			SoundEventAccessor accessor = soundRegistry.getObject(location);
			if (accessor.getSubtitle() instanceof TextComponentTranslation) {
				String key = ((TextComponentTranslation) accessor.getSubtitle()).getKey();
				if (!I18n.hasKey(key)) {
					LOGGER.debug("Missing subtitle {} for event: {}", key, location);
				}
			}
		}

		for (ResourceLocation location : soundRegistry.getKeys()) {
			if (SoundEvent.REGISTRY.getObject(location) == null) {
				LOGGER.debug("Not having sound event for: {}", location);
			}
		}

		reloadSoundSystem();
	}

	private void reloadSoundSystem() {
		UNABLE_TO_PLAY.clear();
		for (SoundEvent soundevent : SoundEvent.REGISTRY) {
			ResourceLocation location = soundevent.soundName();
			if (soundRegistry.getObject(location) == null) {
				LOGGER.warn("Missing sound for event: {}", SoundEvent.REGISTRY.getNameForObject(soundevent));
				UNABLE_TO_PLAY.add(location);
			}
		}
		execute(() -> {
			stopAllSoundsInternal();
			for (int bufId : soundBuffers.values()) AL10.alDeleteBuffers(bufId);
			soundBuffers.clear();
		});
	}

	public void unloadSoundSystem() {
		running = false;
		if (soundThread != null) {
			soundThread.interrupt();
			try {
				soundThread.join(5000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			soundThread = null;
		}
	}

	// ========== MUSIC TICKER (main thread, folded into update) ==========

	public void update() {
		musicTicker.update();
	}

	public MusicTicker getMusicTicker() {
		return musicTicker;
	}

	// ========== SOUND HANDLER COMPAT (main thread helpers) ==========

	public void pauseSounds() {
		pauseAllSounds();
	}

	public void stopSounds() {
		stopAllSounds();
	}

	public void unloadSounds() {
		unloadSoundSystem();
	}

	public void resumeSounds() {
		resumeAllSounds();
	}

	public void setSoundLevel(SoundCategory category, float volume) {
		if (category == SoundCategory.MASTER && volume <= 0F) stopSounds();
		setVolume(category, volume);
	}

	// ========== SOUND REGISTRY (from SoundHandler) ==========

	protected Map<String, SoundList> getSoundMap(InputStream stream) {
		try {
			return JsonUtils.fromJson(GSON, new InputStreamReader(stream, StandardCharsets.UTF_8), SOUND_LIST_TYPE);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	private void loadSoundResource(ResourceLocation location, SoundList sounds) {
		SoundEventAccessor accessor = soundRegistry.getObject(location);
		boolean flag = accessor == null;

		if (flag || sounds.canReplaceExisting()) {
			if (!flag) LOGGER.debug("Replaced sound event location {}", location);
			accessor = new SoundEventAccessor(location, sounds.getSubtitle());
			soundRegistry.add(accessor);
		}

		for (final Sound sound : sounds.getSounds()) {
			final ResourceLocation name = sound.name();
			ISoundEventAccessor<Sound> eventAccessor;

			switch (sound.type()) {
				case FILE -> {
					if (!validateSoundResource(sound, location)) continue;
					eventAccessor = sound;
				}
				case SOUND_EVENT -> eventAccessor = new ISoundEventAccessor<>() {
					public int weight() {
						SoundEventAccessor acc = soundRegistry.getObject(name);
						return acc == null ? 0 : acc.weight();
					}
					public Sound cloneEntry() {
						SoundEventAccessor acc = soundRegistry.getObject(name);
						if (acc == null) return MISSING_SOUND;
						Sound other = acc.cloneEntry();
						return new Sound(other.name().toString(), other.volume() * sound.volume(), other.pitch() * sound.pitch(), sound.weight(), Sound.Type.FILE, other.streaming() || sound.streaming());
					}
				};
				default -> throw new IllegalStateException("Unknown SoundEventRegistration type: " + sound.type());
			}

			accessor.addSound(eventAccessor);
		}
	}

	private boolean validateSoundResource(Sound sound, ResourceLocation location) {
		ResourceLocation oggLocation = sound.getOggLocation();
		IResource resource = null;
		try {
			resource = resourceManager.getResource(oggLocation);
			resource.getInputStream();
			return true;
		} catch (FileNotFoundException e) {
			LOGGER.warn("File {} does not exist, cannot add it to event {}", oggLocation, location);
		} catch (IOException e) {
			LOGGER.warn("Could not load sound file {}, cannot add it to event {}", oggLocation, location, e);
		} finally {
			IOUtils.closeQuietly(resource);
		}
		return false;
	}

	// ========== VOLUME / PITCH HELPERS ==========

	private float getVolume(SoundCategory category) {
		return category != null && category != SoundCategory.MASTER ? options.getSoundLevel(category) : 1F;
	}

	private float getClampedPitch(ISound sound) {
		return MathHelper.clamp(sound.getPitch(), 0.5F, 2F);
	}

	private float getClampedVolume(ISound sound) {
		return MathHelper.clamp(sound.getVolume() * getVolume(sound.getCategory()), 0F, 1F);
	}

}
