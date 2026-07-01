package net.minecraft.client.audio;

import lombok.Getter;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

public interface ISound {

	ResourceLocation getSoundLocation();

	SoundEventAccessor createAccessor(SoundHandler handler);

	Sound getSound();

	SoundCategory getCategory();

	boolean canRepeat();

	int getRepeatDelay();

	float getVolume();

	float getPitch();

	float getX();

	float getY();

	float getZ();

	ISound.AttenuationType getAttenuationType();

	@Getter
	enum AttenuationType {
		NONE(0),
		LINEAR(2);

		private final int type;

		AttenuationType(int type) {
			this.type = type;
		}

	}

}
