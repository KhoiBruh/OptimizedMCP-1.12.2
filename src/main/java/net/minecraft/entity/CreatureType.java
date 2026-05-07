package net.minecraft.entity;

import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.passive.IAnimals;

public enum CreatureType {
	MONSTER(IMob.class, 70, false, false),
	CREATURE(EntityAnimal.class, 10, true, true),
	AMBIENT(EntityAmbientCreature.class, 15, true, false),
	WATER_CREATURE(EntityWaterMob.class, 5, true, false);

	private final Class<? extends IAnimals> creatureClass;
	private final int maxNumberOfCreature;
	
	/**
	 * A flag indicating whether this creature type is peaceful.
	 */
	private final boolean isPeacefulCreature;

	/**
	 * Whether this creature type is an animal.
	 */
	private final boolean isAnimal;

	CreatureType(Class<? extends IAnimals> creatureClassIn, int maxNumberOfCreatureIn, boolean isPeacefulCreatureIn, boolean isAnimalIn) {

		creatureClass = creatureClassIn;
		maxNumberOfCreature = maxNumberOfCreatureIn;
		isPeacefulCreature = isPeacefulCreatureIn;
		isAnimal = isAnimalIn;
	}

	public Class<? extends IAnimals> getCreatureClass() {

		return creatureClass;
	}

	public int getMaxNumberOfCreature() {

		return maxNumberOfCreature;
	}

	/**
	 * Gets whether or not this creature type is peaceful.
	 */
	public boolean getPeacefulCreature() {

		return isPeacefulCreature;
	}

	/**
	 * Return whether this creature type is an animal.
	 */
	public boolean getAnimal() {

		return isAnimal;
	}
}
