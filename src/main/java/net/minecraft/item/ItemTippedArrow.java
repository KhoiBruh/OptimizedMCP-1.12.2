package net.minecraft.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.PotionTypes;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import java.util.List;

public class ItemTippedArrow extends ItemArrow {

	public ItemStack getDefaultInstance() {

		return PotionUtils.addPotionToItemStack(super.getDefaultInstance(), PotionTypes.POISON);
	}

	/**
	 * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
	 */
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {

		if (isInCreativeTab(tab)) {
			for (PotionType potiontype : PotionType.REGISTRY) {
				if (!potiontype.getEffects().isEmpty()) {
					items.add(PotionUtils.addPotionToItemStack(new ItemStack(this), potiontype));
				}
			}
		}
	}

	/**
	 * allows items to add custom lines of information to the mouseover description
	 */
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

		PotionUtils.addPotionTooltip(stack, tooltip, 0.125F);
	}

	public String getItemStackDisplayName(ItemStack stack) {

		return I18n.translateToLocal(PotionUtils.getPotionFromItem(stack).getNamePrefixed("tipped_arrow.effect."));
	}

}
