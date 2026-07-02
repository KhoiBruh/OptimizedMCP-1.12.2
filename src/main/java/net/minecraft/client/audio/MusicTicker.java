package net.minecraft.client.audio;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Maths;

import java.util.Random;

public class MusicTicker implements ITickable {

	private final Random rand = new Random();
	private final Minecraft mc;
	private ISound currentMusic;
	private int nextMusicTime = 100;

	public MusicTicker(Minecraft mc) {
		this.mc = mc;
	}

	public void update() {
		MusicTicker.MusicType musicType = mc.getAmbientMusicType();

		if (currentMusic != null) {
			if (!musicType.getMusicLocation().soundName().equals(currentMusic.getSoundLocation())) {
				mc.getSoundHandler().stopSound(currentMusic);
				nextMusicTime = Maths.getInt(rand, 0, musicType.getMinDelay() / 2);
			}

			if (!mc.getSoundHandler().isSoundPlaying(currentMusic)) {
				currentMusic = null;
				nextMusicTime = Math.min(Maths.getInt(rand, musicType.getMinDelay(), musicType.getMaxDelay()), nextMusicTime);
			}
		}

		nextMusicTime = Math.min(nextMusicTime, musicType.getMaxDelay());

		if (currentMusic == null && nextMusicTime-- <= 0) playMusic(musicType);
	}

	public void playMusic(MusicTicker.MusicType musicType) {
		currentMusic = PositionedSoundRecord.getMusicRecord(musicType.getMusicLocation());
		mc.getSoundHandler().playSound(currentMusic);
		nextMusicTime = Integer.MAX_VALUE;
	}

	@Getter
	public enum MusicType {
		MENU(SoundEvents.MUSIC_MENU, 20, 600),
		GAME(SoundEvents.MUSIC_GAME, 12000, 24000),
		CREATIVE(SoundEvents.MUSIC_CREATIVE, 1200, 3600),
		CREDITS(SoundEvents.MUSIC_CREDITS, 0, 0),
		NETHER(SoundEvents.MUSIC_NETHER, 1200, 3600),
		END_BOSS(SoundEvents.MUSIC_DRAGON, 0, 0),
		END(SoundEvents.MUSIC_END, 6000, 24000);

		private final SoundEvent musicLocation;
		private final int minDelay;
		private final int maxDelay;

		MusicType(SoundEvent musicLocationIn, int minDelayIn, int maxDelayIn) {
			musicLocation = musicLocationIn;
			minDelay = minDelayIn;
			maxDelay = maxDelayIn;
		}

	}

}
