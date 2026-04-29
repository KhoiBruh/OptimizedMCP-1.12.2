package net.minecraft.entity.passive;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityMule extends AbstractChestHorse {

	public EntityMule(World worldIn) {

		super(worldIn);
	}

	public static void registerFixesMule(DataFixer fixer) {

		AbstractChestHorse.registerFixesAbstractChestHorse(fixer, EntityMule.class);
	}

	
	protected ResourceLocation getLootTable() {

		return LootTableList.ENTITIES_MULE;
	}

	protected SoundEvent getAmbientSound() {

		super.getAmbientSound();
		return SoundEvents.ENTITY_MULE_AMBIENT;
	}

	protected SoundEvent getDeathSound() {

		super.getDeathSound();
		return SoundEvents.ENTITY_MULE_DEATH;
	}

	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {

		super.getHurtSound(damageSourceIn);
		return SoundEvents.ENTITY_MULE_HURT;
	}

	protected void playChestEquipSound() {

		playSound(SoundEvents.ENTITY_MULE_CHEST, 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
	}

}
