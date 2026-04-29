package net.minecraft.pathfinding;

public enum PathNodeType {
	BLOCKED(-1F),
	OPEN(0F),
	WALKABLE(0F),
	TRAPDOOR(0F),
	FENCE(-1F),
	LAVA(-1F),
	WATER(8F),
	RAIL(0F),
	DANGER_FIRE(8F),
	DAMAGE_FIRE(16F),
	DANGER_CACTUS(8F),
	DAMAGE_CACTUS(-1F),
	DANGER_OTHER(8F),
	DAMAGE_OTHER(-1F),
	DOOR_OPEN(0F),
	DOOR_WOOD_CLOSED(-1F),
	DOOR_IRON_CLOSED(-1F);

	private final float priority;

	PathNodeType(float priorityIn) {

		priority = priorityIn;
	}

	public float getPriority() {

		return priority;
	}
}
