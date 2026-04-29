package net.minecraft.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.EnumDifficulty;

public class FoodStats {

	/**
	 * The player's food level.
	 */
	private int foodLevel = 20;

	/**
	 * The player's food saturation.
	 */
	private float foodSaturationLevel = 5F;

	/**
	 * The player's food exhaustion.
	 */
	private float foodExhaustionLevel;

	/**
	 * The player's food timer value.
	 */
	private int foodTimer;
	private int prevFoodLevel = 20;

	/**
	 * Add food stats.
	 */
	public void addStats(int foodLevelIn, float foodSaturationModifier) {

		foodLevel = Math.min(foodLevelIn + foodLevel, 20);
		foodSaturationLevel = Math.min(foodSaturationLevel + (float) foodLevelIn * foodSaturationModifier * 2F, (float) foodLevel);
	}

	public void addStats(ItemFood foodItem, ItemStack stack) {

		addStats(foodItem.getHealAmount(stack), foodItem.getSaturationModifier(stack));
	}

	/**
	 * Handles the food game logic.
	 */
	public void onUpdate(EntityPlayer player) {

		EnumDifficulty enumdifficulty = player.world.getDifficulty();
		prevFoodLevel = foodLevel;

		if (foodExhaustionLevel > 4F) {
			foodExhaustionLevel -= 4F;

			if (foodSaturationLevel > 0F) {
				foodSaturationLevel = Math.max(foodSaturationLevel - 1F, 0F);
			} else if (enumdifficulty != EnumDifficulty.PEACEFUL) {
				foodLevel = Math.max(foodLevel - 1, 0);
			}
		}

		boolean flag = player.world.getGameRules().getBoolean("naturalRegeneration");

		if (flag && foodSaturationLevel > 0F && player.shouldHeal() && foodLevel >= 20) {
			++foodTimer;

			if (foodTimer >= 10) {
				float f = Math.min(foodSaturationLevel, 6F);
				player.heal(f / 6F);
				addExhaustion(f);
				foodTimer = 0;
			}
		} else if (flag && foodLevel >= 18 && player.shouldHeal()) {
			++foodTimer;

			if (foodTimer >= 80) {
				player.heal(1F);
				addExhaustion(6F);
				foodTimer = 0;
			}
		} else if (foodLevel <= 0) {
			++foodTimer;

			if (foodTimer >= 80) {
				if (player.getHealth() > 10F || enumdifficulty == EnumDifficulty.HARD || player.getHealth() > 1F && enumdifficulty == EnumDifficulty.NORMAL) {
					player.attackEntityFrom(DamageSource.STARVE, 1F);
				}

				foodTimer = 0;
			}
		} else {
			foodTimer = 0;
		}
	}

	/**
	 * Reads the food data for the player.
	 */
	public void readNBT(NBTTagCompound compound) {

		if (compound.hasKey("foodLevel", 99)) {
			foodLevel = compound.getInteger("foodLevel");
			foodTimer = compound.getInteger("foodTickTimer");
			foodSaturationLevel = compound.getFloat("foodSaturationLevel");
			foodExhaustionLevel = compound.getFloat("foodExhaustionLevel");
		}
	}

	/**
	 * Writes the food data for the player.
	 */
	public void writeNBT(NBTTagCompound compound) {

		compound.setInteger("foodLevel", foodLevel);
		compound.setInteger("foodTickTimer", foodTimer);
		compound.setFloat("foodSaturationLevel", foodSaturationLevel);
		compound.setFloat("foodExhaustionLevel", foodExhaustionLevel);
	}

	/**
	 * Get the player's food level.
	 */
	public int getFoodLevel() {

		return foodLevel;
	}

	public void setFoodLevel(int foodLevelIn) {

		foodLevel = foodLevelIn;
	}

	/**
	 * Get whether the player must eat food.
	 */
	public boolean needFood() {

		return foodLevel < 20;
	}

	/**
	 * adds input to foodExhaustionLevel to a max of 40
	 */
	public void addExhaustion(float exhaustion) {

		foodExhaustionLevel = Math.min(foodExhaustionLevel + exhaustion, 40F);
	}

	/**
	 * Get the player's food saturation level.
	 */
	public float getSaturationLevel() {

		return foodSaturationLevel;
	}

	public void setFoodSaturationLevel(float foodSaturationLevelIn) {

		foodSaturationLevel = foodSaturationLevelIn;
	}

}
