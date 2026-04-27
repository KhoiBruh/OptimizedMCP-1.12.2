package net.minecraft.entity.ai;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityAIPanic extends EntityAIBase {

	protected final EntityCreature creature;
	protected double speed;
	protected double randPosX;
	protected double randPosY;
	protected double randPosZ;

	public EntityAIPanic(EntityCreature creature, double speedIn) {

		this.creature = creature;
		speed = speedIn;
		setMutexBits(1);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {

		if (creature.getRevengeTarget() == null && !creature.isBurning()) {
			return false;
		} else {
			if (creature.isBurning()) {
				BlockPos blockpos = getRandPos(creature.world, creature, 5, 4);

				if (blockpos != null) {
					randPosX = blockpos.getX();
					randPosY = blockpos.getY();
					randPosZ = blockpos.getZ();
					return true;
				}
			}

			return findRandomPosition();
		}
	}

	protected boolean findRandomPosition() {

		Vec3d vec3d = RandomPositionGenerator.findRandomTarget(creature, 5, 4);

		if (vec3d == null) {
			return false;
		} else {
			randPosX = vec3d.x();
			randPosY = vec3d.y();
			randPosZ = vec3d.z();
			return true;
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {

		creature.getNavigator().tryMoveToXYZ(randPosX, randPosY, randPosZ, speed);
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {

		return !creature.getNavigator().noPath();
	}

	@Nullable
	private BlockPos getRandPos(World worldIn, Entity entityIn, int horizontalRange, int verticalRange) {

		BlockPos blockpos = new BlockPos(entityIn);
		int i = blockpos.getX();
		int j = blockpos.getY();
		int k = blockpos.getZ();
		float f = (float) (horizontalRange * horizontalRange * verticalRange * 2);
		BlockPos blockpos1 = null;
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

		for (int l = i - horizontalRange; l <= i + horizontalRange; ++l) {
			for (int i1 = j - verticalRange; i1 <= j + verticalRange; ++i1) {
				for (int j1 = k - horizontalRange; j1 <= k + horizontalRange; ++j1) {
					blockpos$mutableblockpos.setPos(l, i1, j1);
					IBlockState iblockstate = worldIn.getBlockState(blockpos$mutableblockpos);

					if (iblockstate.getMaterial() == Material.WATER) {
						float f1 = (float) ((l - i) * (l - i) + (i1 - j) * (i1 - j) + (j1 - k) * (j1 - k));

						if (f1 < f) {
							f = f1;
							blockpos1 = new BlockPos(blockpos$mutableblockpos);
						}
					}
				}
			}
		}

		return blockpos1;
	}

}
