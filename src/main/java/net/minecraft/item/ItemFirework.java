package net.minecraft.item;

import com.google.common.collect.Lists;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Facing;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import java.util.List;

public class ItemFirework extends Item {

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	public ActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, Hand hand, Facing facing, float hitX, float hitY, float hitZ) {

		if (!worldIn.isRemote) {
			ItemStack itemstack = player.getHeldItem(hand);
			EntityFireworkRocket entityfireworkrocket = new EntityFireworkRocket(worldIn, (float) pos.getX() + hitX, (float) pos.getY() + hitY, (float) pos.getZ() + hitZ, itemstack);
			worldIn.spawnEntity(entityfireworkrocket);

			if (!player.capabilities.isCreativeMode) {
				itemstack.shrink(1);
			}
		}

		return ActionResult.SUCCESS;
	}

	public TypedActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, Hand handIn) {

		if (playerIn.isElytraFlying()) {
			ItemStack itemstack = playerIn.getHeldItem(handIn);

			if (!worldIn.isRemote) {
				EntityFireworkRocket entityfireworkrocket = new EntityFireworkRocket(worldIn, itemstack, playerIn);
				worldIn.spawnEntity(entityfireworkrocket);

				if (!playerIn.capabilities.isCreativeMode) {
					itemstack.shrink(1);
				}
			}

			return new TypedActionResult<>(ActionResult.SUCCESS, playerIn.getHeldItem(handIn));
		} else {
			return new TypedActionResult<>(ActionResult.PASS, playerIn.getHeldItem(handIn));
		}
	}

	/**
	 * allows items to add custom lines of information to the mouseover description
	 */
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

		NBTTagCompound nbttagcompound = stack.getSubCompound("Fireworks");

		if (nbttagcompound != null) {
			if (nbttagcompound.hasKey("Flight", 99)) {
				tooltip.add(I18n.translateToLocal("item.fireworks.flight") + " " + nbttagcompound.getByte("Flight"));
			}

			NBTTagList nbttaglist = nbttagcompound.getTagList("Explosions", 10);

			if (!nbttaglist.hasNoTags()) {
				for (int i = 0; i < nbttaglist.tagCount(); ++i) {
					NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
					List<String> list = Lists.newArrayList();
					ItemFireworkCharge.addExplosionInfo(nbttagcompound1, list);

					if (!list.isEmpty()) {
						for (int j = 1; j < list.size(); ++j) {
							list.set(j, "  " + list.get(j));
						}

						tooltip.addAll(list);
					}
				}
			}
		}
	}

}
