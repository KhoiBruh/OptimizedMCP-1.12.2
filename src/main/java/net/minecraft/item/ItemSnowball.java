package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.SoundEvents;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemSnowball extends Item {

	public ItemSnowball() {

		maxStackSize = 16;
		setCreativeTab(CreativeTabs.MISC);
	}

	public TypedActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, Hand handIn) {

		ItemStack itemstack = playerIn.getHeldItem(handIn);

		if (!playerIn.capabilities.isCreativeMode) {
			itemstack.shrink(1);
		}

		worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

		if (!worldIn.isRemote) {
			EntitySnowball entitysnowball = new EntitySnowball(worldIn, playerIn);
			entitysnowball.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0F, 1.5F, 1F);
			worldIn.spawnEntity(entitysnowball);
		}

		playerIn.addStat(StatList.getObjectUseStats(this));
		return new TypedActionResult<>(ActionResult.SUCCESS, itemstack);
	}

}
