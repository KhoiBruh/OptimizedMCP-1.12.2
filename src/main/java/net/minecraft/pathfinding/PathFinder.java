package net.minecraft.pathfinding;

import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import java.util.Set;

public class PathFinder {

	/**
	 * The path being generated
	 */
	private final PathHeap path = new PathHeap();
	private final Set<PathPoint> closedSet = Sets.newHashSet();

	/**
	 * Selection of path points to add to the path
	 */
	private final PathPoint[] pathOptions = new PathPoint[32];
	private final NodeProcessor nodeProcessor;

	public PathFinder(NodeProcessor processor) {

		nodeProcessor = processor;
	}

	
	public Path findPath(IBlockAccess worldIn, EntityLiving entitylivingIn, Entity targetEntity, float maxDistance) {

		return findPath(worldIn, entitylivingIn, targetEntity.posX, targetEntity.getEntityBoundingBox().minY, targetEntity.posZ, maxDistance);
	}

	
	public Path findPath(IBlockAccess worldIn, EntityLiving entitylivingIn, BlockPos targetPos, float maxDistance) {

		return findPath(worldIn, entitylivingIn, (float) targetPos.getX() + 0.5F, (float) targetPos.getY() + 0.5F, (float) targetPos.getZ() + 0.5F, maxDistance);
	}

	
	private Path findPath(IBlockAccess worldIn, EntityLiving entitylivingIn, double x, double y, double z, float maxDistance) {

		path.clearPath();
		nodeProcessor.init(worldIn, entitylivingIn);
		PathPoint pathpoint = nodeProcessor.getStart();
		PathPoint pathpoint1 = nodeProcessor.getPathPointToCoords(x, y, z);
		Path path = findPath(pathpoint, pathpoint1, maxDistance);
		nodeProcessor.postProcess();
		return path;
	}

	
	private Path findPath(PathPoint pathFrom, PathPoint pathTo, float maxDistance) {

		pathFrom.totalPathDistance = 0.0F;
		pathFrom.distanceToNext = pathFrom.distanceManhattan(pathTo);
		pathFrom.distanceToTarget = pathFrom.distanceToNext;
		path.clearPath();
		closedSet.clear();
		path.addPoint(pathFrom);
		PathPoint pathpoint = pathFrom;
		int i = 0;

		while (!path.isPathEmpty()) {
			++i;

			if (i >= 200) {
				break;
			}

			PathPoint pathpoint1 = path.dequeue();

			if (pathpoint1.equals(pathTo)) {
				pathpoint = pathTo;
				break;
			}

			if (pathpoint1.distanceManhattan(pathTo) < pathpoint.distanceManhattan(pathTo)) {
				pathpoint = pathpoint1;
			}

			pathpoint1.visited = true;
			int j = nodeProcessor.findPathOptions(pathOptions, pathpoint1, pathTo, maxDistance);

			for (int k = 0; k < j; ++k) {
				PathPoint pathpoint2 = pathOptions[k];
				float f = pathpoint1.distanceManhattan(pathpoint2);
				pathpoint2.distanceFromOrigin = pathpoint1.distanceFromOrigin + f;
				pathpoint2.cost = f + pathpoint2.costMalus;
				float f1 = pathpoint1.totalPathDistance + pathpoint2.cost;

				if (pathpoint2.distanceFromOrigin < maxDistance && (!pathpoint2.isAssigned() || f1 < pathpoint2.totalPathDistance)) {
					pathpoint2.previous = pathpoint1;
					pathpoint2.totalPathDistance = f1;
					pathpoint2.distanceToNext = pathpoint2.distanceManhattan(pathTo) + pathpoint2.costMalus;

					if (pathpoint2.isAssigned()) {
						path.changeDistance(pathpoint2, pathpoint2.totalPathDistance + pathpoint2.distanceToNext);
					} else {
						pathpoint2.distanceToTarget = pathpoint2.totalPathDistance + pathpoint2.distanceToNext;
						path.addPoint(pathpoint2);
					}
				}
			}
		}

		if (pathpoint == pathFrom) {
			return null;
		} else {
			return createPath(pathFrom, pathpoint);
		}
	}

	/**
	 * Returns a new PathEntity for a given start and end point
	 */
	private Path createPath(PathPoint start, PathPoint end) {

		int i = 1;

		for (PathPoint pathpoint = end; pathpoint.previous != null; pathpoint = pathpoint.previous) {
			++i;
		}

		PathPoint[] apathpoint = new PathPoint[i];
		PathPoint pathpoint1 = end;
		--i;

		for (apathpoint[i] = end; pathpoint1.previous != null; apathpoint[i] = pathpoint1) {
			pathpoint1 = pathpoint1.previous;
			--i;
		}

		return new Path(apathpoint);
	}

}
