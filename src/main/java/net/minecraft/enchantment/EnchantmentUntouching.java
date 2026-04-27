package net.minecraft.enchantment;

import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentUntouching extends Enchantment {

	protected EnchantmentUntouching(Enchantment.Rarity rarityIn, EntityEquipmentSlot... slots) {

		super(rarityIn, EnumEnchantmentType.DIGGER, slots);
		setName("untouching");
	}

	/**
	 * Returns the minimal value of enchantability needed on the enchantment level passed.
	 */
	public int getMinEnchantability(int enchantmentLevel) {

		return 15;
	}

	/**
	 * Returns the maximum value of enchantability nedded on the enchantment level passed.
	 */
	public int getMaxEnchantability(int enchantmentLevel) {

		return super.getMinEnchantability(enchantmentLevel) + 50;
	}

	/**
	 * Determines if the enchantment passed can be applyied together with this enchantment.
	 */
	public boolean canApplyTogether(Enchantment ench) {

		return super.canApplyTogether(ench) && ench != Enchantments.FORTUNE;
	}

}
