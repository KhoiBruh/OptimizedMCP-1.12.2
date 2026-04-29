package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentBindingCurse extends Enchantment {

	public EnchantmentBindingCurse(Enchantment.Rarity p_i47254_1_, EntityEquipmentSlot... p_i47254_2_) {

		super(p_i47254_1_, EnchantmentType.WEARABLE, p_i47254_2_);
		setName("binding_curse");
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
