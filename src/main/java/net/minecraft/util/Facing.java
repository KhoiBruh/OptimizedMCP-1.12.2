package net.minecraft.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public enum Facing implements IStringSerializable {
	DOWN(0, 1, -1, "down", Facing.AxisDirection.NEGATIVE, Facing.Axis.Y, new Vec3i(0, -1, 0)),
	UP(1, 0, -1, "up", Facing.AxisDirection.POSITIVE, Facing.Axis.Y, new Vec3i(0, 1, 0)),
	NORTH(2, 3, 2, "north", Facing.AxisDirection.NEGATIVE, Facing.Axis.Z, new Vec3i(0, 0, -1)),
	SOUTH(3, 2, 0, "south", Facing.AxisDirection.POSITIVE, Facing.Axis.Z, new Vec3i(0, 0, 1)),
	WEST(4, 5, 1, "west", Facing.AxisDirection.NEGATIVE, Facing.Axis.X, new Vec3i(-1, 0, 0)),
	EAST(5, 4, 3, "east", Facing.AxisDirection.POSITIVE, Facing.Axis.X, new Vec3i(1, 0, 0));

	/**
	 * All facings in D-U-N-S-W-E order
	 */
	private static final Facing[] VALUES = new Facing[6];
	/**
	 * All Facings with horizontal axis in order S-W-N-E
	 */
	private static final Facing[] HORIZONTALS = new Facing[4];
	private static final Map<String, Facing> NAME_LOOKUP = Maps.newHashMap();

	static {
		for (Facing enumfacing : values()) {
			VALUES[enumfacing.index] = enumfacing;

			if (enumfacing.getAxis().isHorizontal()) {
				HORIZONTALS[enumfacing.horizontalIndex] = enumfacing;
			}

			NAME_LOOKUP.put(enumfacing.getName2().toLowerCase(Locale.ROOT), enumfacing);
		}
	}

	/**
	 * Ordering index for D-U-N-S-W-E
	 */
	private final int index;
	/**
	 * Index of the opposite Facing in the VALUES array
	 */
	private final int opposite;
	/**
	 * Ordering index for the HORIZONTALS field (S-W-N-E)
	 */
	private final int horizontalIndex;
	private final String name;
	private final Facing.Axis axis;
	private final Facing.AxisDirection axisDirection;
	/**
	 * Normalized Vector that points in the direction of this Facing
	 */
	private final Vec3i directionVec;

	Facing(int indexIn, int oppositeIn, int horizontalIndexIn, String nameIn, Facing.AxisDirection axisDirectionIn, Facing.Axis axisIn, Vec3i directionVecIn) {

		index = indexIn;
		horizontalIndex = horizontalIndexIn;
		opposite = oppositeIn;
		name = nameIn;
		axis = axisIn;
		axisDirection = axisDirectionIn;
		directionVec = directionVecIn;
	}

	

	/**
	 * Get the facing specified by the given name
	 */
	public static Facing byName(String name) {

		return name == null ? null : NAME_LOOKUP.get(name.toLowerCase(Locale.ROOT));
	}

	/**
	 * Get a Facing by it's index (0-5). The order is D-U-N-S-W-E. Named getFront for legacy reasons.
	 */
	public static Facing getFront(int index) {

		return VALUES[MathHelper.abs(index % VALUES.length)];
	}

	/**
	 * Get a Facing by it's horizontal index (0-3). The order is S-W-N-E.
	 */
	public static Facing getHorizontal(int horizontalIndexIn) {

		return HORIZONTALS[MathHelper.abs(horizontalIndexIn % HORIZONTALS.length)];
	}

	/**
	 * Get the Facing corresponding to the given angle (0-360). An angle of 0 is SOUTH, an angle of 90 would be WEST.
	 */
	public static Facing fromAngle(double angle) {

		return getHorizontal(MathHelper.floor(angle / 90D + 0.5D) & 3);
	}

	/**
	 * Choose a random Facing using the given Random
	 */
	public static Facing random(Random rand) {

		return values()[rand.nextInt(values().length)];
	}

	public static Facing getFacingFromVector(float x, float y, float z) {

		Facing enumfacing = NORTH;
		float f = Float.MIN_VALUE;

		for (Facing enumfacing1 : values()) {
			float f1 = x * (float) enumfacing1.directionVec.getX() + y * (float) enumfacing1.directionVec.getY() + z * (float) enumfacing1.directionVec.getZ();

			if (f1 > f) {
				f = f1;
				enumfacing = enumfacing1;
			}
		}

		return enumfacing;
	}

	public static Facing getFacingFromAxis(Facing.AxisDirection axisDirectionIn, Facing.Axis axisIn) {

		for (Facing enumfacing : values()) {
			if (enumfacing.getAxisDirection() == axisDirectionIn && enumfacing.getAxis() == axisIn) {
				return enumfacing;
			}
		}

		throw new IllegalArgumentException("No such direction: " + axisDirectionIn + " " + axisIn);
	}

	public static Facing getDirectionFromEntityLiving(BlockPos pos, EntityLivingBase placer) {

		if (Math.abs(placer.posX - (double) ((float) pos.getX() + 0.5F)) < 2D && Math.abs(placer.posZ - (double) ((float) pos.getZ() + 0.5F)) < 2D) {
			double d0 = placer.posY + (double) placer.getEyeHeight();

			if (d0 - (double) pos.getY() > 2D) {
				return UP;
			}

			if ((double) pos.getY() - d0 > 0D) {
				return DOWN;
			}
		}

		return placer.getHorizontalFacing().getOpposite();
	}

	/**
	 * Get the Index of this Facing (0-5). The order is D-U-N-S-W-E
	 */
	public int getIndex() {

		return index;
	}

	/**
	 * Get the index of this horizontal facing (0-3). The order is S-W-N-E
	 */
	public int getHorizontalIndex() {

		return horizontalIndex;
	}

	/**
	 * Get the AxisDirection of this Facing.
	 */
	public Facing.AxisDirection getAxisDirection() {

		return axisDirection;
	}

	/**
	 * Get the opposite Facing (e.g. DOWN => UP)
	 */
	public Facing getOpposite() {

		return getFront(opposite);
	}

	/**
	 * Rotate this Facing around the given axis clockwise. If this facing cannot be rotated around the given axis,
	 * returns this facing without rotating.
	 */
	public Facing rotateAround(Facing.Axis axis) {

		return switch (axis) {
			case X -> {
				if (this != WEST && this != EAST) {
					yield rotateX();
				}

				yield this;
			}
			case Y -> {
				if (this != UP && this != DOWN) {
					yield rotateY();
				}

				yield this;
			}
			case Z -> {
				if (this != NORTH && this != SOUTH) {
					yield rotateZ();
				}

				yield this;
			}
		};
	}

	/**
	 * Rotate this Facing around the Y axis clockwise (NORTH => EAST => SOUTH => WEST => NORTH)
	 */
	public Facing rotateY() {

		return switch (this) {
			case NORTH -> EAST;
			case EAST -> SOUTH;
			case SOUTH -> WEST;
			case WEST -> NORTH;
			default -> throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
		};
	}

	/**
	 * Rotate this Facing around the X axis (NORTH => DOWN => SOUTH => UP => NORTH)
	 */
	private Facing rotateX() {

		return switch (this) {
			case NORTH -> DOWN;
			case SOUTH -> UP;
			case UP -> NORTH;
			case DOWN -> SOUTH;
			default -> throw new IllegalStateException("Unable to get X-rotated facing of " + this);
		};
	}

	/**
	 * Rotate this Facing around the Z axis (EAST => DOWN => WEST => UP => EAST)
	 */
	private Facing rotateZ() {

		return switch (this) {
			case EAST -> DOWN;
			case WEST -> UP;
			case UP -> EAST;
			case DOWN -> WEST;
			default -> throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
		};
	}

	/**
	 * Rotate this Facing around the Y axis counter-clockwise (NORTH => WEST => SOUTH => EAST => NORTH)
	 */
	public Facing rotateYCCW() {

		return switch (this) {
			case NORTH -> WEST;
			case EAST -> NORTH;
			case SOUTH -> EAST;
			case WEST -> SOUTH;
			default -> throw new IllegalStateException("Unable to get CCW facing of " + this);
		};
	}

	/**
	 * Returns a offset that addresses the block in front of this facing.
	 */
	public int getFrontOffsetX() {

		return axis == Facing.Axis.X ? axisDirection.getOffset() : 0;
	}

	public int getFrontOffsetY() {

		return axis == Facing.Axis.Y ? axisDirection.getOffset() : 0;
	}

	/**
	 * Returns a offset that addresses the block in front of this facing.
	 */
	public int getFrontOffsetZ() {

		return axis == Facing.Axis.Z ? axisDirection.getOffset() : 0;
	}

	/**
	 * Same as getName, but does not override the method from Enum.
	 */
	public String getName2() {

		return name;
	}

	public Facing.Axis getAxis() {

		return axis;
	}

	public float getHorizontalAngle() {

		return (float) ((horizontalIndex & 3) * 90);
	}

	public String toString() {

		return name;
	}

	public String getName() {

		return name;
	}

	/**
	 * Get a normalized Vector that points in the direction of this Facing.
	 */
	public Vec3i getDirectionVec() {

		return directionVec;
	}

	public enum Axis implements Predicate<Facing>, IStringSerializable {
		X("x", Facing.Plane.HORIZONTAL),
		Y("y", Facing.Plane.VERTICAL),
		Z("z", Facing.Plane.HORIZONTAL);

		private static final Map<String, Facing.Axis> NAME_LOOKUP = Maps.newHashMap();

		static {
			for (Facing.Axis enumfacing$axis : values()) {
				NAME_LOOKUP.put(enumfacing$axis.getName2().toLowerCase(Locale.ROOT), enumfacing$axis);
			}
		}

		private final String name;
		private final Facing.Plane plane;

		Axis(String name, Facing.Plane plane) {

			this.name = name;
			this.plane = plane;
		}

		
		public static Facing.Axis byName(String name) {

			return name == null ? null : NAME_LOOKUP.get(name.toLowerCase(Locale.ROOT));
		}

		public String getName2() {

			return name;
		}

		public boolean isVertical() {

			return plane == Facing.Plane.VERTICAL;
		}

		public boolean isHorizontal() {

			return plane == Facing.Plane.HORIZONTAL;
		}

		public String toString() {

			return name;
		}

		public boolean apply(Facing p_apply_1_) {

			return p_apply_1_ != null && p_apply_1_.getAxis() == this;
		}

		public Facing.Plane getPlane() {

			return plane;
		}

		public String getName() {

			return name;
		}
	}

	public enum AxisDirection {
		POSITIVE(1, "Towards positive"),
		NEGATIVE(-1, "Towards negative");

		private final int offset;
		private final String description;

		AxisDirection(int offset, String description) {

			this.offset = offset;
			this.description = description;
		}

		public int getOffset() {

			return offset;
		}

		public String toString() {

			return description;
		}
	}

	public enum Plane implements Predicate<Facing>, Iterable<Facing> {
		HORIZONTAL,
		VERTICAL;

		public Facing[] facings() {

			return switch (this) {
				case HORIZONTAL ->
						new Facing[]{Facing.NORTH, Facing.EAST, Facing.SOUTH, Facing.WEST};
				case VERTICAL -> new Facing[]{Facing.UP, Facing.DOWN};
			};
		}

		public Facing random(Random rand) {

			Facing[] aenumfacing = facings();
			return aenumfacing[rand.nextInt(aenumfacing.length)];
		}

		public boolean apply(Facing p_apply_1_) {

			return p_apply_1_ != null && p_apply_1_.getAxis().getPlane() == this;
		}

		public Iterator<Facing> iterator() {

			return Iterators.forArray(facings());
		}
	}
}
