package net.minecraft.entity.ai;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class EntityAIMate extends EntityAIBase {

	private final EntityAnimal animal;
	private final Class<? extends EntityAnimal> mateClass;
	World world;
	/**
	 * Delay preventing a baby from spawning immediately when two mate-able animals find each other.
	 */
	int spawnBabyDelay;
	/**
	 * The speed the creature moves at during mating behavior.
	 */
	double moveSpeed;
	private EntityAnimal targetMate;

	public EntityAIMate(EntityAnimal animal, double speedIn) {

		this(animal, speedIn, animal.getClass());
	}

	public EntityAIMate(EntityAnimal p_i47306_1_, double p_i47306_2_, Class<? extends EntityAnimal> p_i47306_4_) {

		animal = p_i47306_1_;
		world = p_i47306_1_.world;
		mateClass = p_i47306_4_;
		moveSpeed = p_i47306_2_;
		setMutexBits(3);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {

		if (!animal.isInLove()) {
			return false;
		} else {
			targetMate = getNearbyMate();
			return targetMate != null;
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {

		return targetMate.isEntityAlive() && targetMate.isInLove() && spawnBabyDelay < 60;
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {

		targetMate = null;
		spawnBabyDelay = 0;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void updateTask() {

		animal.getLookHelper().setLookPositionWithEntity(targetMate, 10.0F, (float) animal.getVerticalFaceSpeed());
		animal.getNavigator().tryMoveToEntityLiving(targetMate, moveSpeed);
		++spawnBabyDelay;

		if (spawnBabyDelay >= 60 && animal.getDistanceSq(targetMate) < 9.0D) {
			spawnBaby();
		}
	}

	/**
	 * Loops through nearby animals and finds another animal of the same type that can be mated with. Returns the first
	 * valid mate found.
	 */
	private EntityAnimal getNearbyMate() {

		List<EntityAnimal> list = world.getEntitiesWithinAABB(mateClass, animal.getEntityBoundingBox().grow(8.0D));
		double d0 = Double.MAX_VALUE;
		EntityAnimal entityanimal = null;

		for (EntityAnimal entityanimal1 : list) {
			if (animal.canMateWith(entityanimal1) && animal.getDistanceSq(entityanimal1) < d0) {
				entityanimal = entityanimal1;
				d0 = animal.getDistanceSq(entityanimal1);
			}
		}

		return entityanimal;
	}

	/**
	 * Spawns a baby animal of the same type.
	 */
	private void spawnBaby() {

		EntityAgeable entityageable = animal.createChild(targetMate);

		if (entityageable != null) {
			EntityPlayerMP entityplayermp = animal.getLoveCause();

			if (entityplayermp == null && targetMate.getLoveCause() != null) {
				entityplayermp = targetMate.getLoveCause();
			}

			if (entityplayermp != null) {
				entityplayermp.addStat(StatList.ANIMALS_BRED);
				CriteriaTriggers.BRED_ANIMALS.trigger(entityplayermp, animal, targetMate, entityageable);
			}

			animal.setGrowingAge(6000);
			targetMate.setGrowingAge(6000);
			animal.resetInLove();
			targetMate.resetInLove();
			entityageable.setGrowingAge(-24000);
			entityageable.setLocationAndAngles(animal.posX, animal.posY, animal.posZ, 0.0F, 0.0F);
			world.spawnEntity(entityageable);
			Random random = animal.getRNG();

			for (int i = 0; i < 7; ++i) {
				double d0 = random.nextGaussian() * 0.02D;
				double d1 = random.nextGaussian() * 0.02D;
				double d2 = random.nextGaussian() * 0.02D;
				double d3 = random.nextDouble() * (double) animal.width * 2.0D - (double) animal.width;
				double d4 = 0.5D + random.nextDouble() * (double) animal.height;
				double d5 = random.nextDouble() * (double) animal.width * 2.0D - (double) animal.width;
				world.spawnParticle(EnumParticleTypes.HEART, animal.posX + d3, animal.posY + d4, animal.posZ + d5, d0, d1, d2);
			}

			if (world.getGameRules().getBoolean("doMobLoot")) {
				world.spawnEntity(new EntityXPOrb(world, animal.posX, animal.posY, animal.posZ, random.nextInt(7) + 1));
			}
		}
	}

}
