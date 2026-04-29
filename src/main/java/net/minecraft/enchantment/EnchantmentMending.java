package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentMending extends Enchantment {

	public EnchantmentMending(Enchantment.Rarity rarityIn, EntityEquipmentSlot... slots) {

		super(rarityIn, EnchantmentType.BREAKABLE, slots);
		setName("mending");
	}

	/**
	 * Returns the minimal value of enchantability needed on the enchantment level passed.
	 */
	public int getMinEnchantability(int enchantmentLevel) {

		return enchantmentLevel * 25;
	}

	/**
	 * Returns the maximum value of enchantability nedded on the enchantment level passed.
	 */
	public int getMaxEnchantability(int enchantmentLevel) {

		return getMinEnchantability(enchantmentLevel) + 50;
	}

	public boolean isTreasureEnchantment() {

		return true;
	}

}
