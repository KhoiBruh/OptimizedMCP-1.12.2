package net.minecraft.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.text.ITextComponent;

public class CombatEntry {

	private final DamageSource damageSrc;
	private final float damage;
	private final String fallSuffix;
	private final float fallDistance;

	public CombatEntry(DamageSource damageSrcIn, float damageAmount, String fallSuffixIn, float fallDistanceIn) {
		damageSrc = damageSrcIn;
		damage = damageAmount;
		fallSuffix = fallSuffixIn;
		fallDistance = fallDistanceIn;
	}

	/**
	 * Get the DamageSource of the CombatEntry instance.
	 */
	public DamageSource getDamageSrc() {
		return damageSrc;
	}

	public float getDamage() {
		return damage;
	}

	/**
	 * Returns true if {@link net.minecraft.util.DamageSource#getEntity() damage source} is a living entity
	 */
	public boolean isLivingDamageSrc() {
		return damageSrc.getTrueSource() instanceof EntityLivingBase;
	}

	
	public String getFallSuffix() {
		return fallSuffix;
	}

	
	public ITextComponent getDamageSrcDisplayName() {
		return getDamageSrc().getTrueSource() == null ? null : getDamageSrc().getTrueSource().getDisplayName();
	}

	public float getDamageAmount() {
		return damageSrc == DamageSource.OUT_OF_WORLD ? Float.MAX_VALUE : fallDistance;
	}

}
