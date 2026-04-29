package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.util.math.Vec3d;

public class EntityAIWanderAvoidWater extends EntityAIWander {

	protected final float probability;

	public EntityAIWanderAvoidWater(EntityCreature p_i47301_1_, double p_i47301_2_) {

		this(p_i47301_1_, p_i47301_2_, 0.001F);
	}

	public EntityAIWanderAvoidWater(EntityCreature p_i47302_1_, double p_i47302_2_, float p_i47302_4_) {

		super(p_i47302_1_, p_i47302_2_);
		probability = p_i47302_4_;
	}

	
	protected Vec3d getPosition() {

		if (entity.isInWater()) {
			Vec3d vec3d = RandomPositionGenerator.getLandPos(entity, 15, 7);
			return vec3d == null ? super.getPosition() : vec3d;
		} else {
			return entity.getRNG().nextFloat() >= probability ? RandomPositionGenerator.getLandPos(entity, 10, 7) : super.getPosition();
		}
	}

}
