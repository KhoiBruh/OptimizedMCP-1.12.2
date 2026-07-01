package net.minecraft.client.audio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

public class SoundHandler implements IResourceManagerReloadListener, ITickable {

	public static final Sound MISSING_SOUND = new Sound("meta:missing_sound", 1F, 1F, 1, Sound.Type.FILE, false);
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = (new GsonBuilder()).registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer()).registerTypeAdapter(SoundList.class, new SoundListSerializer()).create();
	private static final ParameterizedType TYPE = new ParameterizedType() {
		public Type @NonNull [] getActualTypeArguments() {
			return new Type[]{String.class, SoundList.class};
		}

		public @NonNull Type getRawType() {
			return Map.class;
		}

		public Type getOwnerType() {
			return null;
		}
	};
	private final SoundRegistry soundRegistry = new SoundRegistry();
	private final SoundManager soundManager;
	private final IResourceManager resourceManager;

	public SoundHandler(IResourceManager manager, GameSettings settings) {
		resourceManager = manager;
		soundManager = new SoundManager(this, settings);
	}

	public void onResourceManagerReload(IResourceManager manager) {
		soundRegistry.clearMap();

		for (String s : manager.getResourceDomains()) {
			try {
				for (IResource resource : manager.getAllResources(new ResourceLocation(s, "sounds.json"))) {
					try {
						Map<String, SoundList> map = getSoundMap(resource.getInputStream());

						for (Entry<String, SoundList> entry : map.entrySet()) {
							loadSoundResource(new ResourceLocation(s, entry.getKey()), entry.getValue());
						}
					} catch (RuntimeException runtimeexception) {
						LOGGER.warn("Invalid sounds.json", runtimeexception);
					}
				}
			} catch (IOException ignored) {
			}
		}

		for (ResourceLocation location : soundRegistry.getKeys()) {
			SoundEventAccessor accessor = soundRegistry.getObject(location);

			if (accessor.getSubtitle() instanceof TextComponentTranslation) {
				String s1 = ((TextComponentTranslation) accessor.getSubtitle()).getKey();

				if (!I18n.hasKey(s1)) LOGGER.debug("Missing subtitle {} for event: {}", s1, location);
			}
		}

		for (ResourceLocation resourcelocation1 : soundRegistry.getKeys()) {
			if (SoundEvent.REGISTRY.getObject(resourcelocation1) == null) {
				LOGGER.debug("Not having sound event for: {}", resourcelocation1);
			}
		}

		soundManager.reloadSoundSystem();
	}

	
	protected Map<String, SoundList> getSoundMap(InputStream stream) {

		try (InputStream s = stream) {
			return JsonUtils.fromJson(GSON, new InputStreamReader(s, StandardCharsets.UTF_8), TYPE);
		} catch (IOException ignored) {
			return new java.util.HashMap<>();
		}
	}

	private void loadSoundResource(ResourceLocation location, SoundList sounds) {
		SoundEventAccessor accessor = soundRegistry.getObject(location);
		boolean flag = accessor == null;

		if (flag || sounds.canReplaceExisting()) {
			if (!flag) {
				LOGGER.debug("Replaced sound event location {}", location);
			}

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
						SoundEventAccessor soundeventaccessor1 = soundRegistry.getObject(name);
						return soundeventaccessor1 == null ? 0 : soundeventaccessor1.weight();
					}

					public Sound cloneEntry() {
						SoundEventAccessor accessor = soundRegistry.getObject(name);

						if (accessor == null) {
							return SoundHandler.MISSING_SOUND;
						} else {
							Sound other = accessor.cloneEntry();
							return new Sound(other.name().toString(), other.volume() * sound.volume(), other.pitch() * sound.pitch(), sound.weight(), Sound.Type.FILE, other.streaming() || sound.streaming());
						}
					}
				};

				default -> throw new IllegalStateException("Unknown SoundEventRegistration type: " + sound.type());
			}

			accessor.addSound(eventAccessor);
		}
	}

	private boolean validateSoundResource(Sound sound, ResourceLocation location) {
		ResourceLocation oggLocation = sound.getOggLocation();

		try (IResource resource = resourceManager.getResource(oggLocation)) {
			resource.getInputStream();
			return true;
		} catch (FileNotFoundException var11) {
			LOGGER.warn("File {} does not exist, cannot add it to event {}", oggLocation, location);
		} catch (IOException ioexception) {
			LOGGER.warn("Could not load sound file {}, cannot add it to event {}", oggLocation, location, ioexception);
			return false;
		}

		return false;
	}

	
	public SoundEventAccessor getAccessor(ResourceLocation location) {
		return soundRegistry.getObject(location);
	}

	/**
	 * Play a sound
	 */
	public void playSound(ISound sound) {
		soundManager.playSound(sound);
	}

	/**
	 * Plays the sound in n ticks
	 */
	public void playDelayedSound(ISound sound, int delay) {
		soundManager.playDelayedSound(sound, delay);
	}

	public void setListener(EntityPlayer player, float partialTicks) {
		soundManager.setListener(player, partialTicks);
	}

	public void pauseSounds() {
		soundManager.pauseAllSounds();
	}

	public void stopSounds() {
		soundManager.stopAllSounds();
	}

	public void unloadSounds() {
		soundManager.unloadSoundSystem();
	}

	/**
	 * Like the old updateEntity(), except more generic.
	 */
	public void update() {
		soundManager.updateAllSounds();
	}

	public void resumeSounds() {
		soundManager.resumeAllSounds();
	}

	public void setSoundLevel(SoundCategory category, float volume) {
		if (category == SoundCategory.MASTER && volume <= 0F) stopSounds();

		soundManager.setVolume(category, volume);
	}

	public void stopSound(ISound soundIn) {
		soundManager.stopSound(soundIn);
	}

	public boolean isSoundPlaying(ISound sound) {
		return soundManager.isSoundPlaying(sound);
	}

	public void addListener(ISoundEventListener listener) {
		soundManager.addListener(listener);
	}

	public void removeListener(ISoundEventListener listener) {
		soundManager.removeListener(listener);
	}

	public void stop(String name, SoundCategory category) {
		soundManager.stop(name, category);
	}

}
