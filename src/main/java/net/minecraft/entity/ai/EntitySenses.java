package net.minecraft.entity.ai;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

import java.util.List;

public class EntitySenses {

	EntityLiving entity;
	List<Entity> seenEntities = Lists.newArrayList();
	List<Entity> unseenEntities = Lists.newArrayList();

	public EntitySenses(EntityLiving entityIn) {

		entity = entityIn;
	}

	/**
	 * Clears canSeeCachePositive and canSeeCacheNegative.
	 */
	public void clearSensingCache() {

		seenEntities.clear();
		unseenEntities.clear();
	}

	/**
	 * Checks, whether 'our' entity can see the entity given as argument (true) or not (false), caching the result.
	 */
	public boolean canSee(Entity entityIn) {

		if (seenEntities.contains(entityIn)) {
			return true;
		} else if (unseenEntities.contains(entityIn)) {
			return false;
		} else {
			entity.world.profiler.startSection("canSee");
			boolean flag = entity.canEntityBeSeen(entityIn);
			entity.world.profiler.endSection();

			if (flag) {
				seenEntities.add(entityIn);
			} else {
				unseenEntities.add(entityIn);
			}

			return flag;
		}
	}

}
