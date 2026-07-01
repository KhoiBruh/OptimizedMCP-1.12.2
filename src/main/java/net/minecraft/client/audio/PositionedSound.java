package net.minecraft.client.audio;

import lombok.Getter;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public abstract class PositionedSound implements ISound {

	@Getter
	protected Sound sound;

	@Getter
	protected SoundCategory category;
	protected ResourceLocation location;

	protected float volume;
	protected float pitch;

	@Getter
	protected float x;

	@Getter
	protected float y;

	@Getter
	protected float z;

	protected boolean repeat;

	@Getter
	protected int repeatDelay;

	@Getter
	protected ISound.AttenuationType attenuationType;

	protected PositionedSound(SoundEvent soundIn, SoundCategory categoryIn) {
		this(soundIn.soundName(), categoryIn);
	}

	protected PositionedSound(ResourceLocation soundId, SoundCategory categoryIn) {
		volume = 1F;
		pitch = 1F;
		attenuationType = ISound.AttenuationType.LINEAR;
		location = soundId;
		category = categoryIn;
	}

	public ResourceLocation getSoundLocation() {
		return location;
	}

	public SoundEventAccessor createAccessor(SoundHandler handler) {
		SoundEventAccessor soundEvent = handler.getAccessor(location);

		if (soundEvent == null) {
			sound = SoundHandler.MISSING_SOUND;
		} else {
			sound = soundEvent.cloneEntry();
		}

		return soundEvent;
	}

	public boolean canRepeat() {
		return repeat;
	}

	public float getVolume() {
		return volume * sound.volume();
	}

	public float getPitch() {
		return pitch * sound.pitch();
	}

}
