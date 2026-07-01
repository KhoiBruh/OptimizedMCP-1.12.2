package net.minecraft.client.audio;

public interface ISoundEventAccessor<T> {

	int weight();

	T cloneEntry();

}
