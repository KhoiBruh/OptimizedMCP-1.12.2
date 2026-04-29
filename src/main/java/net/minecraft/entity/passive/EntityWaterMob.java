package net.minecraft.entity.passive;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public abstract class EntityWaterMob extends EntityLiving implements IAnimals {

	public EntityWaterMob(World worldIn) {

		super(worldIn);
	}

	public boolean canBreatheUnderwater() {

		return true;
	}

	/**
	 * Checks if the entity's current position is a valid location to spawn this entity.
	 */
	public boolean getCanSpawnHere() {

		return true;
	}

	/**
	 * Checks that the entity is not colliding with any blocks / liquids
	 */
	public boolean isNotColliding() {

		return world.checkNoEntityCollision(getEntityBoundingBox(), this);
	}

	/**
	 * Get number of ticks, at least during which the living entity will be silent.
	 */
	public int getTalkInterval() {

		return 120;
	}

	/**
	 * Get the experience points the entity currently has.
	 */
	protected int getExperiencePoints(EntityPlayer player) {

		return 1 + world.rand.nextInt(3);
	}

	/**
	 * Gets called every tick from main Entity class
	 */
	public void onEntityUpdate() {

		int i = getAir();
		super.onEntityUpdate();

		if (isEntityAlive() && !isInWater()) {
			--i;
			setAir(i);

			if (getAir() == -20) {
				setAir(0);
				attackEntityFrom(DamageSource.DROWN, 2F);
			}
		} else {
			setAir(300);
		}
	}

	public boolean isPushedByWater() {

		return false;
	}

}
