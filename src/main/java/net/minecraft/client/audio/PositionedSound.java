package net.minecraft.client.audio;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public abstract class PositionedSound implements ISound {

	protected Sound sound;
	protected SoundCategory category;
	protected ResourceLocation positionedSoundLocation;
	protected float volume;
	protected float pitch;
	protected float xPosF;
	protected float yPosF;
	protected float zPosF;
	protected boolean repeat;
	/**
	 * The number of ticks between repeating the sound
	 */
	protected int repeatDelay;
	protected ISound.AttenuationType attenuationType;

	
	private SoundEventAccessor soundEvent;

	protected PositionedSound(SoundEvent soundIn, SoundCategory categoryIn) {

		this(soundIn.soundName(), categoryIn);
	}

	protected PositionedSound(ResourceLocation soundId, SoundCategory categoryIn) {

		volume = 1F;
		pitch = 1F;
		attenuationType = ISound.AttenuationType.LINEAR;
		positionedSoundLocation = soundId;
		category = categoryIn;
	}

	public ResourceLocation getSoundLocation() {

		return positionedSoundLocation;
	}

	public SoundEventAccessor createAccessor(SoundHandler handler) {

		soundEvent = handler.getAccessor(positionedSoundLocation);

		if (soundEvent == null) {
			sound = SoundHandler.MISSING_SOUND;
		} else {
			sound = soundEvent.cloneEntry();
		}

		return soundEvent;
	}

	public Sound getSound() {

		return sound;
	}

	public SoundCategory getCategory() {

		return category;
	}

	public boolean canRepeat() {

		return repeat;
	}

	public int getRepeatDelay() {

		return repeatDelay;
	}

	public float getVolume() {

		return volume * sound.getVolume();
	}

	public float getPitch() {

		return pitch * sound.getPitch();
	}

	public float getXPosF() {

		return xPosF;
	}

	public float getYPosF() {

		return yPosF;
	}

	public float getZPosF() {

		return zPosF;
	}

	public ISound.AttenuationType getAttenuationType() {

		return attenuationType;
	}

}
