package net.minecraft.client.audio;

import lombok.Getter;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

@Getter
public abstract class MovingSound extends PositionedSound implements ITickableSound {

	protected boolean donePlaying;

	protected MovingSound(SoundEvent event, SoundCategory category) {
		super(event, category);
	}

}
