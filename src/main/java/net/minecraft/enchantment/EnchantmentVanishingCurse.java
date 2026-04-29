package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentVanishingCurse extends Enchantment {

	public EnchantmentVanishingCurse(Enchantment.Rarity p_i47252_1_, EntityEquipmentSlot... p_i47252_2_) {

		super(p_i47252_1_, EnchantmentType.ALL, p_i47252_2_);
		setName("vanishing_curse");
	}

	/**
	 * Returns the minimal value of enchantability needed on the enchantment level passed.
	 */
	public int getMinEnchantability(int enchantmentLevel) {

		return 25;
	}

	/**
	 * Returns the maximum value of enchantability nedded on the enchantment level passed.
	 */
	public int getMaxEnchantability(int enchantmentLevel) {

		return 50;
	}

	public boolean isTreasureEnchantment() {

		return true;
	}

	public boolean isCurse() {

		return true;
	}

}
