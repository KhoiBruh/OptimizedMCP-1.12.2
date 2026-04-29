package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Facing;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Rotations;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class ItemArmorStand extends Item {

	public ItemArmorStand() {

		setCreativeTab(CreativeTabs.DECORATIONS);
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	public ActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, Hand hand, Facing facing, float hitX, float hitY, float hitZ) {

		if (facing == Facing.DOWN) {
			return ActionResult.FAIL;
		} else {
			boolean flag = worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
			BlockPos blockpos = flag ? pos : pos.offset(facing);
			ItemStack itemstack = player.getHeldItem(hand);

			if (!player.canPlayerEdit(blockpos, facing, itemstack)) {
				return ActionResult.FAIL;
			} else {
				BlockPos blockpos1 = blockpos.up();
				boolean flag1 = !worldIn.isAirBlock(blockpos) && !worldIn.getBlockState(blockpos).getBlock().isReplaceable(worldIn, blockpos);
				flag1 = flag1 | (!worldIn.isAirBlock(blockpos1) && !worldIn.getBlockState(blockpos1).getBlock().isReplaceable(worldIn, blockpos1));

				if (flag1) {
					return ActionResult.FAIL;
				} else {
					double d0 = blockpos.getX();
					double d1 = blockpos.getY();
					double d2 = blockpos.getZ();
					List<Entity> list = worldIn.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(d0, d1, d2, d0 + 1D, d1 + 2D, d2 + 1D));

					if (!list.isEmpty()) {
						return ActionResult.FAIL;
					} else {
						if (!worldIn.isRemote) {
							worldIn.setBlockToAir(blockpos);
							worldIn.setBlockToAir(blockpos1);
							EntityArmorStand entityarmorstand = new EntityArmorStand(worldIn, d0 + 0.5D, d1, d2 + 0.5D);
							float f = (float) MathHelper.floor((MathHelper.wrapDegrees(player.rotationYaw - 180F) + 22.5F) / 45F) * 45F;
							entityarmorstand.setLocationAndAngles(d0 + 0.5D, d1, d2 + 0.5D, f, 0F);
							applyRandomRotations(entityarmorstand, worldIn.rand);
							ItemMonsterPlacer.applyItemEntityDataToEntity(worldIn, player, itemstack, entityarmorstand);
							worldIn.spawnEntity(entityarmorstand);
							worldIn.playSound(null, entityarmorstand.posX, entityarmorstand.posY, entityarmorstand.posZ, SoundEvents.ENTITY_ARMORSTAND_PLACE, SoundCategory.BLOCKS, 0.75F, 0.8F);
						}

						itemstack.shrink(1);
						return ActionResult.SUCCESS;
					}
				}
			}
		}
	}

	private void applyRandomRotations(EntityArmorStand armorStand, Random rand) {

		Rotations rotations = armorStand.getHeadRotation();
		float f = rand.nextFloat() * 5F;
		float f1 = rand.nextFloat() * 20F - 10F;
		Rotations rotations1 = new Rotations(rotations.x() + f, rotations.y() + f1, rotations.z());
		armorStand.setHeadRotation(rotations1);
		rotations = armorStand.getBodyRotation();
		f = rand.nextFloat() * 10F - 5F;
		rotations1 = new Rotations(rotations.x(), rotations.y() + f, rotations.z());
		armorStand.setBodyRotation(rotations1);
	}

}
