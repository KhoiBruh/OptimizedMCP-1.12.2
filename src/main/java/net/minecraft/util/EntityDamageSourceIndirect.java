package net.minecraft.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;

import javax.annotation.Nullable;

public class EntityDamageSourceIndirect extends EntityDamageSource {

	/**
	 * The entity who created the direct source, e.g. the shooter of an arrow
	 */
	private final Entity indirectEntity;

	public EntityDamageSourceIndirect(String damageTypeIn, Entity source, @Nullable Entity indirectEntityIn) {

		super(damageTypeIn, source);
		indirectEntity = indirectEntityIn;
	}

	@Nullable

	/**
	 * Retrieves the immediate causer of the damage, e.g. the arrow entity, not its shooter
	 */
	public Entity getImmediateSource() {

		return damageSourceEntity;
	}

	@Nullable

	/**
	 * Retrieves the true causer of the damage, e.g. the player who fired an arrow, the shulker who fired the bullet,
	 * etc.
	 */
	public Entity getTrueSource() {

		return indirectEntity;
	}

	/**
	 * Gets the death message that is displayed when the player dies
	 */
	public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {

		ITextComponent itextcomponent = indirectEntity == null ? damageSourceEntity.getDisplayName() : indirectEntity.getDisplayName();
		ItemStack itemstack = indirectEntity instanceof EntityLivingBase ? ((EntityLivingBase) indirectEntity).getHeldItemMainhand() : ItemStack.EMPTY;
		String s = "death.attack." + damageType;
		String s1 = s + ".item";
		return !itemstack.isEmpty() && itemstack.hasDisplayName() && I18n.canTranslate(s1) ? new TextComponentTranslation(s1, entityLivingBaseIn.getDisplayName(), itextcomponent, itemstack.getTextComponent()) : new TextComponentTranslation(s, entityLivingBaseIn.getDisplayName(), itextcomponent);
	}

}
