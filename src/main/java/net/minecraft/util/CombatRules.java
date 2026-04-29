package net.minecraft.util;

import net.minecraft.util.math.MathHelper;

public class CombatRules {

	public static float getDamageAfterAbsorb(float damage, float totalArmor, float toughnessAttribute) {

		float f = 2F + toughnessAttribute / 4F;
		float f1 = MathHelper.clamp(totalArmor - damage / f, totalArmor * 0.2F, 20F);
		return damage * (1F - f1 / 25F);
	}

	public static float getDamageAfterMagicAbsorb(float damage, float enchantModifiers) {

		float f = MathHelper.clamp(enchantModifiers, 0F, 20F);
		return damage * (1F - f / 25F);
	}

}
