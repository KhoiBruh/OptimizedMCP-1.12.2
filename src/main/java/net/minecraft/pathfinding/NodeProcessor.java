package net.minecraft.pathfinding;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

public abstract class NodeProcessor {

	protected final IntHashMap<PathPoint> pointMap = new IntHashMap<>();
	protected IBlockAccess blockaccess;
	protected EntityLiving entity;
	protected int entitySizeX;
	protected int entitySizeY;
	protected int entitySizeZ;
	protected boolean canEnterDoors;
	protected boolean canOpenDoors;
	protected boolean canSwim;

	public void init(IBlockAccess sourceIn, EntityLiving mob) {

		blockaccess = sourceIn;
		entity = mob;
		pointMap.clearMap();
		entitySizeX = MathHelper.floor(mob.width + 1F);
		entitySizeY = MathHelper.floor(mob.height + 1F);
		entitySizeZ = MathHelper.floor(mob.width + 1F);
	}

	/**
	 * This method is called when all nodes have been processed and PathEntity is created.
	 * {@link net.minecraft.world.pathfinder.WalkNodeProcessor WalkNodeProcessor} uses this to change its field {@link
	 * net.minecraft.world.pathfinder.WalkNodeProcessor#avoidsWater avoidsWater}
	 */
	public void postProcess() {

		blockaccess = null;
		entity = null;
	}

	/**
	 * Returns a mapped point or creates and adds one
	 */
	protected PathPoint openPoint(int x, int y, int z) {

		int i = PathPoint.makeHash(x, y, z);
		PathPoint pathpoint = pointMap.lookup(i);

		if (pathpoint == null) {
			pathpoint = new PathPoint(x, y, z);
			pointMap.addKey(i, pathpoint);
		}

		return pathpoint;
	}

	public abstract PathPoint getStart();

	/**
	 * Returns PathPoint for given coordinates
	 */
	public abstract PathPoint getPathPointToCoords(double x, double y, double z);

	public abstract int findPathOptions(PathPoint[] pathOptions, PathPoint currentPoint, PathPoint targetPoint, float maxDistance);

	public abstract PathNodeType getPathNodeType(IBlockAccess blockaccessIn, int x, int y, int z, EntityLiving entitylivingIn, int xSize, int ySize, int zSize, boolean canBreakDoorsIn, boolean canEnterDoorsIn);

	public abstract PathNodeType getPathNodeType(IBlockAccess blockaccessIn, int x, int y, int z);

	public boolean getCanEnterDoors() {

		return canEnterDoors;
	}

	public void setCanEnterDoors(boolean canEnterDoorsIn) {

		canEnterDoors = canEnterDoorsIn;
	}

	public boolean getCanOpenDoors() {

		return canOpenDoors;
	}

	public void setCanOpenDoors(boolean canOpenDoorsIn) {

		canOpenDoors = canOpenDoorsIn;
	}

	public boolean getCanSwim() {

		return canSwim;
	}

	public void setCanSwim(boolean canSwimIn) {

		canSwim = canSwimIn;
	}

}
