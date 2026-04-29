package net.minecraft.client.audio;

import java.util.List;

public class SoundList {

	private final List<Sound> sounds;

	/**
	 * if true it will override all the sounds from the resourcepacks loaded before
	 */
	private final boolean replaceExisting;
	private final String subtitle;

	public SoundList(List<Sound> soundsIn, boolean replceIn, String subtitleIn) {

		sounds = soundsIn;
		replaceExisting = replceIn;
		subtitle = subtitleIn;
	}

	public List<Sound> getSounds() {

		return sounds;
	}

	public boolean canReplaceExisting() {

		return replaceExisting;
	}

	
	public String getSubtitle() {

		return subtitle;
	}

}
