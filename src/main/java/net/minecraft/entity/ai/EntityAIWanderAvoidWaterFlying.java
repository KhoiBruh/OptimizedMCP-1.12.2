package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.entity.EntityCreature;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Iterator;

public class EntityAIWanderAvoidWaterFlying extends EntityAIWanderAvoidWater {

	public EntityAIWanderAvoidWaterFlying(EntityCreature p_i47413_1_, double p_i47413_2_) {

		super(p_i47413_1_, p_i47413_2_);
	}

	@Nullable
	protected Vec3d getPosition() {

		Vec3d vec3d = null;

		if (entity.isInWater() || entity.isOverWater()) {
			vec3d = RandomPositionGenerator.getLandPos(entity, 15, 15);
		}

		if (entity.getRNG().nextFloat() >= probability) {
			vec3d = getTreePos();
		}

		return vec3d == null ? super.getPosition() : vec3d;
	}

	@Nullable
	private Vec3d getTreePos() {

		BlockPos blockpos = new BlockPos(entity);
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos blockpos$mutableblockpos1 = new BlockPos.MutableBlockPos();
		Iterable<BlockPos.MutableBlockPos> iterable = BlockPos.MutableBlockPos.getAllInBoxMutable(MathHelper.floor(entity.posX - 3.0D), MathHelper.floor(entity.posY - 6.0D), MathHelper.floor(entity.posZ - 3.0D), MathHelper.floor(entity.posX + 3.0D), MathHelper.floor(entity.posY + 6.0D), MathHelper.floor(entity.posZ + 3.0D));
		Iterator<BlockPos.MutableBlockPos> iterator = iterable.iterator();
		BlockPos blockpos1;

		while (true) {
			if (!iterator.hasNext()) {
				return null;
			}

			blockpos1 = (BlockPos) iterator.next();

			if (!blockpos.equals(blockpos1)) {
				Block block = entity.world.getBlockState(blockpos$mutableblockpos1.setPos(blockpos1).move(EnumFacing.DOWN)).getBlock();
				boolean flag = block instanceof BlockLeaves || block == Blocks.LOG || block == Blocks.LOG2;

				if (flag && entity.world.isAirBlock(blockpos1) && entity.world.isAirBlock(blockpos$mutableblockpos.setPos(blockpos1).move(EnumFacing.UP))) {
					break;
				}
			}
		}

		return new Vec3d(blockpos1.getX(), blockpos1.getY(), blockpos1.getZ());
	}

}
