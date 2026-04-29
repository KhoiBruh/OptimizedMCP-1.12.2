package net.minecraft.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionUtils;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import java.util.List;

public class ItemLingeringPotion extends ItemPotion {

	public String getItemStackDisplayName(ItemStack stack) {

		return I18n.translateToLocal(PotionUtils.getPotionFromItem(stack).getNamePrefixed("lingering_potion.effect."));
	}

	/**
	 * allows items to add custom lines of information to the mouseover description
	 */
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

		PotionUtils.addPotionTooltip(stack, tooltip, 0.25F);
	}

	public TypedActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, Hand handIn) {

		ItemStack itemstack = playerIn.getHeldItem(handIn);
		ItemStack itemstack1 = playerIn.capabilities.isCreativeMode ? itemstack.copy() : itemstack.splitStack(1);
		worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_LINGERINGPOTION_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

		if (!worldIn.isRemote) {
			EntityPotion entitypotion = new EntityPotion(worldIn, playerIn, itemstack1);
			entitypotion.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, -20F, 0.5F, 1F);
			worldIn.spawnEntity(entitypotion);
		}

		playerIn.addStat(StatList.getObjectUseStats(this));
		return new TypedActionResult<>(ActionResult.SUCCESS, itemstack);
	}

}
