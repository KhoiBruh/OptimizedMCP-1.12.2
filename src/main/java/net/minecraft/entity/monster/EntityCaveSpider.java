package net.minecraft.entity.monster;

import net.minecraft.entity.*;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityCaveSpider extends EntitySpider {

	public EntityCaveSpider(World worldIn) {

		super(worldIn);
		setSize(0.7F, 0.5F);
	}

	public static void registerFixesCaveSpider(DataFixer fixer) {

		EntityLiving.registerFixesMob(fixer, EntityCaveSpider.class);
	}

	protected void applyEntityAttributes() {

		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12D);
	}

	public boolean attackEntityAsMob(Entity entityIn) {

		if (super.attackEntityAsMob(entityIn)) {
			if (entityIn instanceof EntityLivingBase) {
				int i = 0;

				if (world.getDifficulty() == Difficulty.NORMAL) {
					i = 7;
				} else if (world.getDifficulty() == Difficulty.HARD) {
					i = 15;
				}

				if (i > 0) {
					((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MobEffects.POISON, i * 20, 0));
				}
			}

			return true;
		} else {
			return false;
		}
	}

	

	/**
	 * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
	 * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory.
	 *
	 * The livingdata parameter is used to pass data between all instances during a pack spawn. It will be null on the
	 * first call. Subclasses may check if it's null, and then create a new one and return it if so, initializing all
	 * entities in the pack with the contained data.
	 *
	 * @return The IEntityLivingData to pass to this method for other instances of this entity class within the same
	 * pack
	 *
	 * @param difficulty The current local difficulty
	 * @param livingdata Shared spawn data. Will usually be null. (See return value for more information)
	 */
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {

		return livingdata;
	}

	public float getEyeHeight() {

		return 0.45F;
	}

	
	protected ResourceLocation getLootTable() {

		return LootTableList.ENTITIES_CAVE_SPIDER;
	}

}
