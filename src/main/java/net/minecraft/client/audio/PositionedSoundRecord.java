package net.minecraft.client.audio;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public class PositionedSoundRecord extends PositionedSound {

	public PositionedSoundRecord(SoundEvent soundIn, SoundCategory categoryIn, float volumeIn, float pitchIn, BlockPos pos) {

		this(soundIn, categoryIn, volumeIn, pitchIn, (float) pos.getX() + 0.5F, (float) pos.getY() + 0.5F, (float) pos.getZ() + 0.5F);
	}

	public PositionedSoundRecord(SoundEvent soundIn, SoundCategory categoryIn, float volumeIn, float pitchIn, float xIn, float yIn, float zIn) {

		this(soundIn, categoryIn, volumeIn, pitchIn, false, 0, ISound.AttenuationType.LINEAR, xIn, yIn, zIn);
	}

	private PositionedSoundRecord(SoundEvent soundIn, SoundCategory categoryIn, float volumeIn, float pitchIn, boolean repeatIn, int repeatDelayIn, ISound.AttenuationType attenuationTypeIn, float xIn, float yIn, float zIn) {

		this(soundIn.soundName(), categoryIn, volumeIn, pitchIn, repeatIn, repeatDelayIn, attenuationTypeIn, xIn, yIn, zIn);
	}

	public PositionedSoundRecord(ResourceLocation soundId, SoundCategory categoryIn, float volumeIn, float pitchIn, boolean repeatIn, int repeatDelayIn, ISound.AttenuationType attenuationTypeIn, float xIn, float yIn, float zIn) {

		super(soundId, categoryIn);
		volume = volumeIn;
		pitch = pitchIn;
		xPosF = xIn;
		yPosF = yIn;
		zPosF = zIn;
		repeat = repeatIn;
		repeatDelay = repeatDelayIn;
		attenuationType = attenuationTypeIn;
	}

	public static PositionedSoundRecord getMasterRecord(SoundEvent soundIn, float pitchIn) {

		return getRecord(soundIn, pitchIn, 0.25F);
	}

	public static PositionedSoundRecord getRecord(SoundEvent soundIn, float pitchIn, float volumeIn) {

		return new PositionedSoundRecord(soundIn, SoundCategory.MASTER, volumeIn, pitchIn, false, 0, ISound.AttenuationType.NONE, 0F, 0F, 0F);
	}

	public static PositionedSoundRecord getMusicRecord(SoundEvent soundIn) {

		return new PositionedSoundRecord(soundIn, SoundCategory.MUSIC, 1F, 1F, false, 0, ISound.AttenuationType.NONE, 0F, 0F, 0F);
	}

	public static PositionedSoundRecord getRecordSoundRecord(SoundEvent soundIn, float xIn, float yIn, float zIn) {

		return new PositionedSoundRecord(soundIn, SoundCategory.RECORDS, 4F, 1F, false, 0, ISound.AttenuationType.LINEAR, xIn, yIn, zIn);
	}

}
