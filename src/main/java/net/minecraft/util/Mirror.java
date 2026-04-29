package net.minecraft.util;

public enum Mirror {
	NONE("no_mirror"),
	LEFT_RIGHT("mirror_left_right"),
	FRONT_BACK("mirror_front_back");

	private static final String[] mirrorNames = new String[values().length];

	static {
		int i = 0;

		for (Mirror mirror : values()) {
			mirrorNames[i++] = mirror.name;
		}
	}

	private final String name;

	Mirror(String nameIn) {

		name = nameIn;
	}

	/**
	 * Mirrors the given rotation like specified by this mirror. rotations start at 0 and go up to rotationCount-1. 0 is
	 * front, rotationCount/2 is back.
	 */
	public int mirrorRotation(int rotationIn, int rotationCount) {

		int i = rotationCount / 2;
		int j = rotationIn > i ? rotationIn - rotationCount : rotationIn;

		return switch (this) {
			case FRONT_BACK -> (rotationCount - j) % rotationCount;
			case LEFT_RIGHT -> (i - j + rotationCount) % rotationCount;
			default -> rotationIn;
		};
	}

	/**
	 * Determines the rotation that is equivalent to this mirror if the rotating object faces in the given direction
	 */
	public Rotation toRotation(Facing facing) {

		Facing.Axis enumfacing$axis = facing.getAxis();
		return (this != LEFT_RIGHT || enumfacing$axis != Facing.Axis.Z) && (this != FRONT_BACK || enumfacing$axis != Facing.Axis.X) ? Rotation.NONE : Rotation.CLOCKWISE_180;
	}

	/**
	 * Mirror the given facing according to this mirror
	 */
	public Facing mirror(Facing facing) {

		switch (this) {
			case FRONT_BACK:
				if (facing == Facing.WEST) {
					return Facing.EAST;
				} else {
					if (facing == Facing.EAST) {
						return Facing.WEST;
					}

					return facing;
				}

			case LEFT_RIGHT:
				if (facing == Facing.NORTH) {
					return Facing.SOUTH;
				} else {
					if (facing == Facing.SOUTH) {
						return Facing.NORTH;
					}

					return facing;
				}

			default:
				return facing;
		}
	}
}
