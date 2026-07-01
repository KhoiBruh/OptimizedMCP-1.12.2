package net.minecraft.client.audio;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistrySimple;

import java.util.HashMap;
import java.util.Map;

public class SoundRegistry extends RegistrySimple<ResourceLocation, SoundEventAccessor> {

	private Map<ResourceLocation, SoundEventAccessor> sounds;

	protected Map<ResourceLocation, SoundEventAccessor> createUnderlyingMap() {
		sounds = new HashMap<>();
		return sounds;
	}

	public void add(SoundEventAccessor accessor) {
		putObject(accessor.getLocation(), accessor);
	}

	public void clearMap() {
		sounds.clear();
	}

}
