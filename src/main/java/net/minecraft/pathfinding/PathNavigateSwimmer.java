package net.minecraft.pathfinding;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PathNavigateSwimmer extends PathNavigate {

	public PathNavigateSwimmer(EntityLiving entitylivingIn, World worldIn) {

		super(entitylivingIn, worldIn);
	}

	protected PathFinder getPathFinder() {

		return new PathFinder(new SwimNodeProcessor());
	}

	/**
	 * If on ground or swimming and can swim
	 */
	protected boolean canNavigate() {

		return isInLiquid();
	}

	protected Vec3d getEntityPosition() {

		return new Vec3d(entity.posX, entity.posY + (double) entity.height * 0.5D, entity.posZ);
	}

	protected void pathFollow() {

		Vec3d vec3d = getEntityPosition();
		float f = entity.width * entity.width;
		int i = 6;

		if (vec3d.squareDistanceTo(currentPath.getVectorFromIndex(entity, currentPath.getCurrentPathIndex())) < (double) f) {
			currentPath.incrementPathIndex();
		}

		for (int j = Math.min(currentPath.getCurrentPathIndex() + 6, currentPath.getCurrentPathLength() - 1); j > currentPath.getCurrentPathIndex(); --j) {
			Vec3d vec3d1 = currentPath.getVectorFromIndex(entity, j);

			if (vec3d1.squareDistanceTo(vec3d) <= 36.0D && isDirectPathBetweenPoints(vec3d, vec3d1, 0, 0, 0)) {
				currentPath.setCurrentPathIndex(j);
				break;
			}
		}

		checkForStuck(vec3d);
	}

	/**
	 * Checks if the specified entity can safely walk to the specified location.
	 */
	protected boolean isDirectPathBetweenPoints(Vec3d posVec31, Vec3d posVec32, int sizeX, int sizeY, int sizeZ) {

		RayTraceResult raytraceresult = world.rayTraceBlocks(posVec31, new Vec3d(posVec32.x(), posVec32.y() + (double) entity.height * 0.5D, posVec32.z()), false, true, false);
		return raytraceresult == null || raytraceresult.typeOfHit == RayTraceResult.Type.MISS;
	}

	public boolean canEntityStandOnPos(BlockPos pos) {

		return !world.getBlockState(pos).isFullBlock();
	}

}
