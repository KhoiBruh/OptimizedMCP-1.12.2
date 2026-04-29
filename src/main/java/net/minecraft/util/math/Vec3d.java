package net.minecraft.util.math;

/**
 * @param x X coordinate of Vec3D
 * @param y Y coordinate of Vec3D
 * @param z Z coordinate of Vec3D
 */
public record Vec3d(double x, double y, double z) {

	public static final Vec3d ZERO = new Vec3d(0.0D, 0.0D, 0.0D);

	public Vec3d {

		if (x == -0.0D) {
			x = 0.0D;
		}

		if (y == -0.0D) {
			y = 0.0D;
		}

		if (z == -0.0D) {
			z = 0.0D;
		}

	}

	public Vec3d(Vec3i vector) {

		this(vector.getX(), vector.getY(), vector.getZ());
	}

	/**
	 * returns a Vec3d from given pitch and yaw degrees as Vec2f
	 */
	public static Vec3d fromPitchYawVector(Vec2f p_189984_0_) {

		return fromPitchYaw(p_189984_0_.x(), p_189984_0_.y());
	}

	/**
	 * returns a Vec3d from given pitch and yaw degrees
	 */
	public static Vec3d fromPitchYaw(float p_189986_0_, float p_189986_1_) {

		float f = MathHelper.cos(-p_189986_1_ * 0.017453292F - (float) Math.PI);
		float f1 = MathHelper.sin(-p_189986_1_ * 0.017453292F - (float) Math.PI);
		float f2 = -MathHelper.cos(-p_189986_0_ * 0.017453292F);
		float f3 = MathHelper.sin(-p_189986_0_ * 0.017453292F);
		return new Vec3d(f1 * f2, f3, f * f2);
	}

	/**
	 * Returns a new vector with the result of the specified vector minus this.
	 */
	public Vec3d subtractReverse(Vec3d vec) {

		return new Vec3d(vec.x - x, vec.y - y, vec.z - z);
	}

	/**
	 * Normalizes the vector to a length of 1 (except if it is the zero vector)
	 */
	public Vec3d normalize() {

		double d0 = MathHelper.sqrt(x * x + y * y + z * z);
		return d0 < 1.0E-4D ? ZERO : new Vec3d(x / d0, y / d0, z / d0);
	}

	public double dotProduct(Vec3d vec) {

		return x * vec.x + y * vec.y + z * vec.z;
	}

	/**
	 * Returns a new vector with the result of this vector x the specified vector.
	 */
	public Vec3d crossProduct(Vec3d vec) {

		return new Vec3d(y * vec.z - z * vec.y, z * vec.x - x * vec.z, x * vec.y - y * vec.x);
	}

	public Vec3d subtract(Vec3d vec) {

		return subtract(vec.x, vec.y, vec.z);
	}

	public Vec3d subtract(double x, double y, double z) {

		return addVector(-x, -y, -z);
	}

	public Vec3d add(Vec3d vec) {

		return addVector(vec.x, vec.y, vec.z);
	}

	/**
	 * Adds the specified x,y,z vector components to this vector and returns the resulting vector. Does not change this
	 * vector.
	 */
	public Vec3d addVector(double x, double y, double z) {

		return new Vec3d(this.x + x, this.y + y, this.z + z);
	}

	/**
	 * Euclidean distance between this and the specified vector, returned as double.
	 */
	public double distanceTo(Vec3d vec) {

		double d0 = vec.x - x;
		double d1 = vec.y - y;
		double d2 = vec.z - z;
		return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
	}

	/**
	 * The square of the Euclidean distance between this and the specified vector.
	 */
	public double squareDistanceTo(Vec3d vec) {

		double d0 = vec.x - x;
		double d1 = vec.y - y;
		double d2 = vec.z - z;
		return d0 * d0 + d1 * d1 + d2 * d2;
	}

	public double squareDistanceTo(double xIn, double yIn, double zIn) {

		double d0 = xIn - x;
		double d1 = yIn - y;
		double d2 = zIn - z;
		return d0 * d0 + d1 * d1 + d2 * d2;
	}

	public Vec3d scale(double factor) {

		return new Vec3d(x * factor, y * factor, z * factor);
	}

	/**
	 * Returns the length of the vector.
	 */
	public double lengthVector() {

		return MathHelper.sqrt(x * x + y * y + z * z);
	}

	public double lengthSquared() {

		return x * x + y * y + z * z;
	}

	

	/**
	 * Returns a new vector with x value equal to the second parameter, along the line between this vector and the
	 * passed in vector, or null if not possible.
	 */
	public Vec3d getIntermediateWithXValue(Vec3d vec, double x) {

		double d0 = vec.x - this.x;
		double d1 = vec.y - y;
		double d2 = vec.z - z;

		if (d0 * d0 < 1.0000000116860974E-7D) {
			return null;
		} else {
			double d3 = (x - this.x) / d0;
			return d3 >= 0.0D && d3 <= 1.0D ? new Vec3d(this.x + d0 * d3, y + d1 * d3, z + d2 * d3) : null;
		}
	}

	

	/**
	 * Returns a new vector with y value equal to the second parameter, along the line between this vector and the
	 * passed in vector, or null if not possible.
	 */
	public Vec3d getIntermediateWithYValue(Vec3d vec, double y) {

		double d0 = vec.x - x;
		double d1 = vec.y - this.y;
		double d2 = vec.z - z;

		if (d1 * d1 < 1.0000000116860974E-7D) {
			return null;
		} else {
			double d3 = (y - this.y) / d1;
			return d3 >= 0.0D && d3 <= 1.0D ? new Vec3d(x + d0 * d3, this.y + d1 * d3, z + d2 * d3) : null;
		}
	}

	

	/**
	 * Returns a new vector with z value equal to the second parameter, along the line between this vector and the
	 * passed in vector, or null if not possible.
	 */
	public Vec3d getIntermediateWithZValue(Vec3d vec, double z) {

		double d0 = vec.x - x;
		double d1 = vec.y - y;
		double d2 = vec.z - this.z;

		if (d2 * d2 < 1.0000000116860974E-7D) {
			return null;
		} else {
			double d3 = (z - this.z) / d2;
			return d3 >= 0.0D && d3 <= 1.0D ? new Vec3d(x + d0 * d3, y + d1 * d3, this.z + d2 * d3) : null;
		}
	}

	public boolean equals(Object p_equals_1_) {

		if (this == p_equals_1_) {
			return true;
		} else if (!(p_equals_1_ instanceof Vec3d(double x1, double y1, double z1))) {
			return false;
		} else {

			if (Double.compare(x1, x) != 0) {
				return false;
			} else if (Double.compare(y1, y) != 0) {
				return false;
			} else {
				return Double.compare(z1, z) == 0;
			}
		}
	}

	public int hashCode() {

		int i = Double.hashCode(x);
		i = 31 * i + Double.hashCode(y);
		i = 31 * i + Double.hashCode(z);
		return i;
	}

	public String toString() {

		return "(" + x + ", " + y + ", " + z + ")";
	}

	public Vec3d rotatePitch(float pitch) {

		float f = MathHelper.cos(pitch);
		float f1 = MathHelper.sin(pitch);
		double d1 = y * (double) f + z * (double) f1;
		double d2 = z * (double) f - y * (double) f1;
		return new Vec3d(x, d1, d2);
	}

	public Vec3d rotateYaw(float yaw) {

		float f = MathHelper.cos(yaw);
		float f1 = MathHelper.sin(yaw);
		double d0 = x * (double) f + z * (double) f1;
		double d2 = z * (double) f - x * (double) f1;
		return new Vec3d(d0, y, d2);
	}

}
