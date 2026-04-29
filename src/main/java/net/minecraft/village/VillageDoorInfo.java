package net.minecraft.village;

import net.minecraft.util.Facing;
import net.minecraft.util.math.BlockPos;

public class VillageDoorInfo {

	/**
	 * a block representing the door. Could be either upper or lower part
	 */
	private final BlockPos doorBlockPos;
	private final BlockPos insideBlock;

	/**
	 * the inside direction is where can see less sky
	 */
	private final Facing insideDirection;
	private int lastActivityTimestamp;
	private boolean isDetachedFromVillageFlag;
	private int doorOpeningRestrictionCounter;

	public VillageDoorInfo(BlockPos pos, int deltaX, int deltaZ, int timestamp) {

		this(pos, getFaceDirection(deltaX, deltaZ), timestamp);
	}

	public VillageDoorInfo(BlockPos pos, Facing facing, int timestamp) {

		doorBlockPos = pos;
		insideDirection = facing;
		insideBlock = pos.offset(facing, 2);
		lastActivityTimestamp = timestamp;
	}

	private static Facing getFaceDirection(int deltaX, int deltaZ) {

		if (deltaX < 0) {
			return Facing.WEST;
		} else if (deltaX > 0) {
			return Facing.EAST;
		} else {
			return deltaZ < 0 ? Facing.NORTH : Facing.SOUTH;
		}
	}

	/**
	 * Returns the squared distance between this door and the given coordinate.
	 */
	public int getDistanceSquared(int x, int y, int z) {

		return (int) doorBlockPos.distanceSq(x, y, z);
	}

	public int getDistanceToDoorBlockSq(BlockPos pos) {

		return (int) pos.distanceSq(getDoorBlockPos());
	}

	public int getDistanceToInsideBlockSq(BlockPos pos) {

		return (int) insideBlock.distanceSq(pos);
	}

	public boolean isInsideSide(BlockPos pos) {

		int i = pos.getX() - doorBlockPos.getX();
		int j = pos.getZ() - doorBlockPos.getY();
		return i * insideDirection.getFrontOffsetX() + j * insideDirection.getFrontOffsetZ() >= 0;
	}

	public void resetDoorOpeningRestrictionCounter() {

		doorOpeningRestrictionCounter = 0;
	}

	public void incrementDoorOpeningRestrictionCounter() {

		++doorOpeningRestrictionCounter;
	}

	public int getDoorOpeningRestrictionCounter() {

		return doorOpeningRestrictionCounter;
	}

	public BlockPos getDoorBlockPos() {

		return doorBlockPos;
	}

	public BlockPos getInsideBlockPos() {

		return insideBlock;
	}

	public int getInsideOffsetX() {

		return insideDirection.getFrontOffsetX() * 2;
	}

	public int getInsideOffsetZ() {

		return insideDirection.getFrontOffsetZ() * 2;
	}

	public int getLastActivityTimestamp() {

		return lastActivityTimestamp;
	}

	public void setLastActivityTimestamp(int timestamp) {

		lastActivityTimestamp = timestamp;
	}

	public boolean getIsDetachedFromVillageFlag() {

		return isDetachedFromVillageFlag;
	}

	public void setIsDetachedFromVillageFlag(boolean detached) {

		isDetachedFromVillageFlag = detached;
	}

	public Facing getInsideDirection() {

		return insideDirection;
	}

}
