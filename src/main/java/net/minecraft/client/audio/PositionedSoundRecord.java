package net.minecraft.client.audio;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public class PositionedSoundRecord extends PositionedSound {

	public PositionedSoundRecord(SoundEvent event, SoundCategory category, float volume, float pitch, BlockPos pos) {
		this(event, category, volume, pitch, (float) pos.getX() + 0.5F, (float) pos.getY() + 0.5F, (float) pos.getZ() + 0.5F);
	}

	public PositionedSoundRecord(SoundEvent event, SoundCategory category, float volume, float pitch, float x, float y, float z) {
		this(event, category, volume, pitch, false, 0, ISound.AttenuationType.LINEAR, x, y, z);
	}

	private PositionedSoundRecord(SoundEvent event, SoundCategory category, float volume, float pitch, boolean repeat, int repeatDelay, ISound.AttenuationType attenuationType, float x, float y, float z) {
		this(event.soundName(), category, volume, pitch, repeat, repeatDelay, attenuationType, x, y, z);
	}

	public PositionedSoundRecord(ResourceLocation soundId, SoundCategory category, float volume, float pitch, boolean repeat, int repeatDelay, ISound.AttenuationType attenuationType, float x, float y, float z) {
		super(soundId, category);
		this.volume = volume;
		this.pitch = pitch;
		this.x = x;
		this.y = y;
		this.z = z;
		this.repeat = repeat;
		this.repeatDelay = repeatDelay;
		this.attenuationType = attenuationType;
	}

	public static PositionedSoundRecord getMasterRecord(SoundEvent event, float pitch) {
		return getRecord(event, pitch, 0.25F);
	}

	public static PositionedSoundRecord getRecord(SoundEvent event, float pitch, float volume) {
		return new PositionedSoundRecord(event, SoundCategory.MASTER, volume, pitch, false, 0, ISound.AttenuationType.NONE, 0F, 0F, 0F);
	}

	public static PositionedSoundRecord getMusicRecord(SoundEvent event) {
		return new PositionedSoundRecord(event, SoundCategory.MUSIC, 1F, 1F, false, 0, ISound.AttenuationType.NONE, 0F, 0F, 0F);
	}

	public static PositionedSoundRecord getRecordSoundRecord(SoundEvent event, float x, float y, float z) {
		return new PositionedSoundRecord(event, SoundCategory.RECORDS, 4F, 1F, false, 0, ISound.AttenuationType.LINEAR, x, y, z);
	}

}
