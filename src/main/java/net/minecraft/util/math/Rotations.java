package net.minecraft.util.math;

import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;

/**
 * @param x Rotation on the X axis
 * @param y Rotation on the Y axis
 * @param z Rotation on the Z axis
 */
public record Rotations(float x, float y, float z) {

	public Rotations(float x, float y, float z) {

		this.x = !Float.isInfinite(x) && !Float.isNaN(x) ? x % 360.0F : 0.0F;
		this.y = !Float.isInfinite(y) && !Float.isNaN(y) ? y % 360.0F : 0.0F;
		this.z = !Float.isInfinite(z) && !Float.isNaN(z) ? z % 360.0F : 0.0F;
	}

	public Rotations(NBTTagList nbt) {

		this(nbt.getFloatAt(0), nbt.getFloatAt(1), nbt.getFloatAt(2));
	}

	public NBTTagList writeToNBT() {

		NBTTagList nbttaglist = new NBTTagList();
		nbttaglist.appendTag(new NBTTagFloat(x));
		nbttaglist.appendTag(new NBTTagFloat(y));
		nbttaglist.appendTag(new NBTTagFloat(z));
		return nbttaglist;
	}

	public boolean equals(Object p_equals_1_) {

		if (!(p_equals_1_ instanceof Rotations(float x1, float y1, float z1))) {
			return false;
		} else {
			return x == x1 && y == y1 && z == z1;
		}
	}

	/**
	 * Gets the X axis rotation
	 */
	@Override
	public float x() {

		return x;
	}

	/**
	 * Gets the Y axis rotation
	 */
	@Override
	public float y() {

		return y;
	}

	/**
	 * Gets the Z axis rotation
	 */
	@Override
	public float z() {

		return z;
	}

}
