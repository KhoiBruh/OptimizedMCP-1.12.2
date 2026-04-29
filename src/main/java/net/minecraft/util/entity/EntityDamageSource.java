package net.minecraft.util.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;

public class EntityDamageSource extends DamageSource {

	
	protected Entity damageSourceEntity;

	/**
	 * Whether this EntityDamageSource is from an entity wearing Thorns-enchanted armor.
	 */
	private boolean isThornsDamage;

	public EntityDamageSource(String damageTypeIn, Entity damageSourceEntityIn) {

		super(damageTypeIn);
		damageSourceEntity = damageSourceEntityIn;
	}

	/**
	 * Sets this EntityDamageSource as originating from Thorns armor
	 */
	public EntityDamageSource setIsThornsDamage() {

		isThornsDamage = true;
		return this;
	}

	public boolean getIsThornsDamage() {

		return isThornsDamage;
	}

	

	/**
	 * Retrieves the true causer of the damage, e.g. the player who fired an arrow, the shulker who fired the bullet,
	 * etc.
	 */
	public Entity getTrueSource() {

		return damageSourceEntity;
	}

	/**
	 * Gets the death message that is displayed when the player dies
	 */
	public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {

		ItemStack itemstack = damageSourceEntity instanceof EntityLivingBase ? ((EntityLivingBase) damageSourceEntity).getHeldItemMainhand() : ItemStack.EMPTY;
		String s = "death.attack." + damageType;
		String s1 = s + ".item";
		return !itemstack.isEmpty() && itemstack.hasDisplayName() && I18n.canTranslate(s1) ? new TextComponentTranslation(s1, entityLivingBaseIn.getDisplayName(), damageSourceEntity.getDisplayName(), itemstack.getTextComponent()) : new TextComponentTranslation(s, entityLivingBaseIn.getDisplayName(), damageSourceEntity.getDisplayName());
	}

	/**
	 * Return whether this damage source will have its damage amount scaled based on the current difficulty.
	 */
	public boolean isDifficultyScaled() {

		return damageSourceEntity != null && damageSourceEntity instanceof EntityLivingBase && !(damageSourceEntity instanceof EntityPlayer);
	}

	

	/**
	 * Gets the location from which the damage originates.
	 */
	public Vec3d getDamageLocation() {

		return new Vec3d(damageSourceEntity.posX, damageSourceEntity.posY, damageSourceEntity.posZ);
	}

}
