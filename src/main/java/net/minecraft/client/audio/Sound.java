package net.minecraft.client.audio;

import net.minecraft.util.ResourceLocation;

public record Sound(
	ResourceLocation name,
	float volume,
	float pitch,
	int weight,
	Type type,
	boolean streaming
) implements ISoundEventAccessor<Sound> {

	public Sound(String name, float volume, float pitch, int weight, Type type, boolean streaming) {
		this(new ResourceLocation(name), volume, pitch, weight, type, streaming);
	}

	public ResourceLocation getOggLocation() {
		return new ResourceLocation(name.getResourceDomain(), "sounds/" + name.getResourcePath() + ".ogg");
	}

	public Sound cloneEntry() {
		return this;
	}

	public enum Type {
		FILE("file"),
		SOUND_EVENT("event");

		private final String name;

		Type(String name) {
			this.name = name;
		}

		public static Type getByName(String name) {
			for (Type type : values()) {
				if (type.name.equals(name)) return type;
			}

			return null;
		}
	}

}
