package net.minecraft.world.storage;

import net.minecraft.util.math.MathHelper;

public record MapDecoration(Type type, byte x, byte y, byte rotation) {

	public byte getImage() {

		return type.getIcon();
	}

	public boolean renderOnFrame() {

		return type.isRenderedOnFrame();
	}

	public boolean equals(Object p_equals_1_) {

		if (this == p_equals_1_) {
			return true;
		} else if (!(p_equals_1_ instanceof MapDecoration(Type type1, byte x1, byte y1, byte rotation1))) {
			return false;
		} else {

			if (type != type1) {
				return false;
			} else if (rotation != rotation1) {
				return false;
			} else if (x != x1) {
				return false;
			} else {
				return y == y1;
			}
		}
	}

	public int hashCode() {

		int i = type.getIcon();
		i = 31 * i + x;
		i = 31 * i + y;
		i = 31 * i + rotation;
		return i;
	}

	public enum Type {
		PLAYER(false),
		FRAME(true),
		RED_MARKER(false),
		BLUE_MARKER(false),
		TARGET_X(true),
		TARGET_POINT(true),
		PLAYER_OFF_MAP(false),
		PLAYER_OFF_LIMITS(false),
		MANSION(true, 5393476),
		MONUMENT(true, 3830373);

		private final byte icon;
		private final boolean renderedOnFrame;
		private final int mapColor;

		Type(boolean p_i47343_3_) {

			this(p_i47343_3_, -1);
		}

		Type(boolean p_i47344_3_, int p_i47344_4_) {

			icon = (byte) ordinal();
			renderedOnFrame = p_i47344_3_;
			mapColor = p_i47344_4_;
		}

		public static Type byIcon(byte p_191159_0_) {

			return values()[MathHelper.clamp(p_191159_0_, 0, values().length - 1)];
		}

		public byte getIcon() {

			return icon;
		}

		public boolean isRenderedOnFrame() {

			return renderedOnFrame;
		}

		public boolean hasMapColor() {

			return mapColor >= 0;
		}

		public int getMapColor() {

			return mapColor;
		}
	}

}
