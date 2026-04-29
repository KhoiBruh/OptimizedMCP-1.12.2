package net.minecraft.entity.ai;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.Facing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityAIFollowOwner extends EntityAIBase {

	private final EntityTameable tameable;
	private final double followSpeed;
	private final PathNavigate petPathfinder;
	World world;
	float maxDist;
	float minDist;
	private EntityLivingBase owner;
	private int timeToRecalcPath;
	private float oldWaterCost;

	public EntityAIFollowOwner(EntityTameable tameableIn, double followSpeedIn, float minDistIn, float maxDistIn) {

		tameable = tameableIn;
		world = tameableIn.world;
		followSpeed = followSpeedIn;
		petPathfinder = tameableIn.getNavigator();
		minDist = minDistIn;
		maxDist = maxDistIn;
		setMutexBits(3);

		if (!(tameableIn.getNavigator() instanceof PathNavigateGround) && !(tameableIn.getNavigator() instanceof PathNavigateFlying)) {
			throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
		}
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {

		EntityLivingBase entitylivingbase = tameable.getOwner();

		if (entitylivingbase == null) {
			return false;
		} else if (entitylivingbase instanceof EntityPlayer && ((EntityPlayer) entitylivingbase).isSpectator()) {
			return false;
		} else if (tameable.isSitting()) {
			return false;
		} else if (tameable.getDistanceSq(entitylivingbase) < (double) (minDist * minDist)) {
			return false;
		} else {
			owner = entitylivingbase;
			return true;
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {

		return !petPathfinder.noPath() && tameable.getDistanceSq(owner) > (double) (maxDist * maxDist) && !tameable.isSitting();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {

		timeToRecalcPath = 0;
		oldWaterCost = tameable.getPathPriority(PathNodeType.WATER);
		tameable.setPathPriority(PathNodeType.WATER, 0F);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {

		owner = null;
		petPathfinder.clearPath();
		tameable.setPathPriority(PathNodeType.WATER, oldWaterCost);
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void updateTask() {

		tameable.getLookHelper().setLookPositionWithEntity(owner, 10F, (float) tameable.getVerticalFaceSpeed());

		if (!tameable.isSitting()) {
			if (--timeToRecalcPath <= 0) {
				timeToRecalcPath = 10;

				if (!petPathfinder.tryMoveToEntityLiving(owner, followSpeed)) {
					if (!tameable.getLeashed() && !tameable.isRiding()) {
						if (tameable.getDistanceSq(owner) >= 144D) {
							int i = MathHelper.floor(owner.posX) - 2;
							int j = MathHelper.floor(owner.posZ) - 2;
							int k = MathHelper.floor(owner.getEntityBoundingBox().minY);

							for (int l = 0; l <= 4; ++l) {
								for (int i1 = 0; i1 <= 4; ++i1) {
									if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && isTeleportFriendlyBlock(i, j, k, l, i1)) {
										tameable.setLocationAndAngles((float) (i + l) + 0.5F, k, (float) (j + i1) + 0.5F, tameable.rotationYaw, tameable.rotationPitch);
										petPathfinder.clearPath();
										return;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	protected boolean isTeleportFriendlyBlock(int x, int z, int y, int xOffset, int zOffset) {

		BlockPos blockpos = new BlockPos(x + xOffset, y - 1, z + zOffset);
		IBlockState iblockstate = world.getBlockState(blockpos);
		return iblockstate.getBlockFaceShape(world, blockpos, Facing.DOWN) == BlockFaceShape.SOLID && iblockstate.canEntitySpawn(tameable) && world.isAirBlock(blockpos.up()) && world.isAirBlock(blockpos.up(2));
	}

}
