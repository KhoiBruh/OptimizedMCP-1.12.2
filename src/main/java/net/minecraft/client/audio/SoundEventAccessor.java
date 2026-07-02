package net.minecraft.client.audio;

import lombok.Getter;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SoundEventAccessor implements ISoundEventAccessor<Sound> {

	private final List<ISoundEventAccessor<Sound>> accessors = new ArrayList<>();
	private final Random rnd = new Random();

	@Getter
	private final ResourceLocation location;

	@Getter
	private final ITextComponent subtitle;

	public SoundEventAccessor(ResourceLocation locationIn, String subtitleIn) {
		location = locationIn;
		subtitle = subtitleIn == null ? null : new TextComponentTranslation(subtitleIn);
	}

	public int weight() {
		int i = 0;

		for (ISoundEventAccessor<Sound> accessor : accessors) {
			i += accessor.weight();
		}

		return i;
	}

	public Sound cloneEntry() {
		int i = weight();

		if (!accessors.isEmpty() && i != 0) {
			int j = rnd.nextInt(i);

			for (ISoundEventAccessor<Sound> isoundeventaccessor : accessors) {
				j -= isoundeventaccessor.weight();

				if (j < 0) {
					return isoundeventaccessor.cloneEntry();
				}
			}
		}

		return SoundHandler.MISSING_SOUND;
	}

	public void addSound(ISoundEventAccessor<Sound> accessor) {
		accessors.add(accessor);
	}

}
