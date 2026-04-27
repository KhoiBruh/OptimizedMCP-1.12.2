package net.minecraft.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemFood extends Item {

	/**
	 * Number of ticks to run while 'EnumAction'ing until result.
	 */
	public final int itemUseDuration;

	/**
	 * The amount this food item heals the player.
	 */
	private final int healAmount;
	private final float saturationModifier;

	/**
	 * Whether wolves like this food (true for raw and cooked porkchop).
	 */
	private final boolean isWolfsFavoriteMeat;

	/**
	 * If this field is true, the food can be consumed even if the player don't need to eat.
	 */
	private boolean alwaysEdible;

	/**
	 * represents the potion effect that will occurr upon eating this food. Set by setPotionEffect
	 */
	private PotionEffect potionId;

	/**
	 * probably of the set potion effect occurring
	 */
	private float potionEffectProbability;

	public ItemFood(int amount, float saturation, boolean isWolfFood) {

		itemUseDuration = 32;
		healAmount = amount;
		isWolfsFavoriteMeat = isWolfFood;
		saturationModifier = saturation;
		setCreativeTab(CreativeTabs.FOOD);
	}

	public ItemFood(int amount, boolean isWolfFood) {

		this(amount, 0.6F, isWolfFood);
	}

	/**
	 * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
	 * the Item before the action is complete.
	 */
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {

		if (entityLiving instanceof EntityPlayer entityplayer) {
			entityplayer.getFoodStats().addStats(this, stack);
			worldIn.playSound(null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, worldIn.rand.nextFloat() * 0.1F + 0.9F);
			onFoodEaten(stack, worldIn, entityplayer);
			entityplayer.addStat(StatList.getObjectUseStats(this));

			if (entityplayer instanceof EntityPlayerMP) {
				CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP) entityplayer, stack);
			}
		}

		stack.shrink(1);
		return stack;
	}

	protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {

		if (!worldIn.isRemote && potionId != null && worldIn.rand.nextFloat() < potionEffectProbability) {
			player.addPotionEffect(new PotionEffect(potionId));
		}
	}

	/**
	 * How long it takes to use or consume an item
	 */
	public int getMaxItemUseDuration(ItemStack stack) {

		return 32;
	}

	/**
	 * returns the action that specifies what animation to play when the items is being used
	 */
	public EnumAction getItemUseAction(ItemStack stack) {

		return EnumAction.EAT;
	}

	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {

		ItemStack itemstack = playerIn.getHeldItem(handIn);

		if (playerIn.canEat(alwaysEdible)) {
			playerIn.setActiveHand(handIn);
			return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
		} else {
			return new ActionResult<>(EnumActionResult.FAIL, itemstack);
		}
	}

	public int getHealAmount(ItemStack stack) {

		return healAmount;
	}

	public float getSaturationModifier(ItemStack stack) {

		return saturationModifier;
	}

	/**
	 * Whether wolves like this food (true for raw and cooked porkchop).
	 */
	public boolean isWolfsFavoriteMeat() {

		return isWolfsFavoriteMeat;
	}

	public ItemFood setPotionEffect(PotionEffect effect, float probability) {

		potionId = effect;
		potionEffectProbability = probability;
		return this;
	}

	/**
	 * Set the field 'alwaysEdible' to true, and make the food edible even if the player don't need to eat.
	 */
	public ItemFood setAlwaysEdible() {

		alwaysEdible = true;
		return this;
	}

}
