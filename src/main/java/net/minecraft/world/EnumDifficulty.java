package net.minecraft.world;

public enum EnumDifficulty {
	PEACEFUL(0, "options.difficulty.peaceful"),
	EASY(1, "options.difficulty.easy"),
	NORMAL(2, "options.difficulty.normal"),
	HARD(3, "options.difficulty.hard");

	private static final EnumDifficulty[] ID_MAPPING = new EnumDifficulty[values().length];

	static {
		for (EnumDifficulty enumdifficulty : values()) {
			ID_MAPPING[enumdifficulty.difficultyId] = enumdifficulty;
		}
	}

	private final int difficultyId;
	private final String difficultyResourceKey;

	EnumDifficulty(int difficultyIdIn, String difficultyResourceKeyIn) {

		difficultyId = difficultyIdIn;
		difficultyResourceKey = difficultyResourceKeyIn;
	}

	public static EnumDifficulty getDifficultyEnum(int id) {

		return ID_MAPPING[id % ID_MAPPING.length];
	}

	public int getDifficultyId() {

		return difficultyId;
	}

	public String getDifficultyResourceKey() {

		return difficultyResourceKey;
	}
}
