package net.minecraft.util;

public enum Rotation {
	NONE("rotate_0"),
	CLOCKWISE_90("rotate_90"),
	CLOCKWISE_180("rotate_180"),
	COUNTERCLOCKWISE_90("rotate_270");

	private static final String[] rotationNames = new String[values().length];

	static {
		int i = 0;

		for (Rotation rotation : values()) {
			rotationNames[i++] = rotation.name;
		}
	}

	private final String name;

	Rotation(String nameIn) {

		name = nameIn;
	}

	public Rotation add(Rotation rotation) {

		return switch (rotation) {
			case CLOCKWISE_180 -> switch (this) {
				case NONE -> CLOCKWISE_180;
				case CLOCKWISE_90 -> COUNTERCLOCKWISE_90;
				case CLOCKWISE_180 -> NONE;
				case COUNTERCLOCKWISE_90 -> CLOCKWISE_90;
			};
			case COUNTERCLOCKWISE_90 -> switch (this) {
				case NONE -> COUNTERCLOCKWISE_90;
				case CLOCKWISE_90 -> NONE;
				case CLOCKWISE_180 -> CLOCKWISE_90;
				case COUNTERCLOCKWISE_90 -> CLOCKWISE_180;
			};
			case CLOCKWISE_90 -> switch (this) {
				case NONE -> CLOCKWISE_90;
				case CLOCKWISE_90 -> CLOCKWISE_180;
				case CLOCKWISE_180 -> COUNTERCLOCKWISE_90;
				case COUNTERCLOCKWISE_90 -> NONE;
			};
			default -> this;
		};
	}

	public Facing rotate(Facing facing) {

		if (facing.getAxis() == Facing.Axis.Y) {
			return facing;
		} else {
			return switch (this) {
				case CLOCKWISE_90 -> facing.rotateY();
				case CLOCKWISE_180 -> facing.getOpposite();
				case COUNTERCLOCKWISE_90 -> facing.rotateYCCW();
				default -> facing;
			};
		}
	}

	public int rotate(int p_185833_1_, int p_185833_2_) {

		return switch (this) {
			case CLOCKWISE_90 -> (p_185833_1_ + p_185833_2_ / 4) % p_185833_2_;
			case CLOCKWISE_180 -> (p_185833_1_ + p_185833_2_ / 2) % p_185833_2_;
			case COUNTERCLOCKWISE_90 -> (p_185833_1_ + p_185833_2_ * 3 / 4) % p_185833_2_;
			default -> p_185833_1_;
		};
	}
}
