package net.minecraft.world;

import net.minecraft.util.math.MathHelper;

public class DifficultyInstance {

	private final EnumDifficulty worldDifficulty;
	private final float additionalDifficulty;

	public DifficultyInstance(EnumDifficulty worldDifficulty, long worldTime, long chunkInhabitedTime, float moonPhaseFactor) {

		this.worldDifficulty = worldDifficulty;
		additionalDifficulty = calculateAdditionalDifficulty(worldDifficulty, worldTime, chunkInhabitedTime, moonPhaseFactor);
	}

	public float getAdditionalDifficulty() {

		return additionalDifficulty;
	}

	public boolean isHarderThan(float p_193845_1_) {

		return additionalDifficulty > p_193845_1_;
	}

	public float getClampedAdditionalDifficulty() {

		if (additionalDifficulty < 2F) {
			return 0F;
		} else {
			return additionalDifficulty > 4F ? 1F : (additionalDifficulty - 2F) / 2F;
		}
	}

	private float calculateAdditionalDifficulty(EnumDifficulty difficulty, long worldTime, long chunkInhabitedTime, float moonPhaseFactor) {

		if (difficulty == EnumDifficulty.PEACEFUL) {
			return 0F;
		} else {
			boolean flag = difficulty == EnumDifficulty.HARD;
			float f = 0.75F;
			float f1 = MathHelper.clamp(((float) worldTime - 72000F) / 1440000F, 0F, 1F) * 0.25F;
			f = f + f1;
			float f2 = 0F;
			f2 = f2 + MathHelper.clamp((float) chunkInhabitedTime / 3600000F, 0F, 1F) * (flag ? 1F : 0.75F);
			f2 = f2 + MathHelper.clamp(moonPhaseFactor * 0.25F, 0F, f1);

			if (difficulty == EnumDifficulty.EASY) {
				f2 *= 0.5F;
			}

			f = f + f2;
			return (float) difficulty.getDifficultyId() * f;
		}
	}

}
