package net.minecraft.client.renderer.color;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.world.ColorizerGrass;

public class ItemColors {

	private final ObjectIntIdentityMap<IItemColor> mapItemColors = new ObjectIntIdentityMap<>(32);

	public static ItemColors init(final BlockColors colors) {

		ItemColors itemcolors = new ItemColors();
		itemcolors.registerItemColorHandler((stack, tintIndex) -> tintIndex > 0 ? -1 : ((ItemArmor) stack.getItem()).getColor(stack), Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
		itemcolors.registerItemColorHandler((stack, tintIndex) -> {

			BlockDoublePlant.PlantType blockdoubleplant$enumplanttype = BlockDoublePlant.PlantType.byMetadata(stack.getMetadata());
			return blockdoubleplant$enumplanttype != BlockDoublePlant.PlantType.GRASS && blockdoubleplant$enumplanttype != BlockDoublePlant.PlantType.FERN ? -1 : ColorizerGrass.getGrassColor(0.5D, 1D);
		}, Blocks.DOUBLE_PLANT);
		itemcolors.registerItemColorHandler((stack, tintIndex) -> {

			if (tintIndex != 1) {
				return -1;
			} else {
				NBTBase nbtbase = ItemFireworkCharge.getExplosionTag(stack, "Colors");

				if (!(nbtbase instanceof NBTTagIntArray)) {
					return 9079434;
				} else {
					int[] aint = ((NBTTagIntArray) nbtbase).getIntArray();

					if (aint.length == 1) {
						return aint[0];
					} else {
						int i = 0;
						int j = 0;
						int k = 0;

						for (int l : aint) {
							i += (l & 16711680) >> 16;
							j += (l & 65280) >> 8;
							k += (l & 255);
						}

						i = i / aint.length;
						j = j / aint.length;
						k = k / aint.length;
						return i << 16 | j << 8 | k;
					}
				}
			}
		}, Items.FIREWORK_CHARGE);
		itemcolors.registerItemColorHandler((stack, tintIndex) -> tintIndex > 0 ? -1 : PotionUtils.getColor(stack), Items.POTIONITEM, Items.SPLASH_POTION, Items.LINGERING_POTION);
		itemcolors.registerItemColorHandler((stack, tintIndex) -> {

			EntityList.EntityEggInfo entitylist$entityegginfo = EntityList.ENTITY_EGGS.get(ItemMonsterPlacer.getNamedIdFrom(stack));

			if (entitylist$entityegginfo == null) {
				return -1;
			} else {
				return tintIndex == 0 ? entitylist$entityegginfo.primaryColor : entitylist$entityegginfo.secondaryColor;
			}
		}, Items.SPAWN_EGG);
		itemcolors.registerItemColorHandler((stack, tintIndex) -> {

			IBlockState iblockstate = ((ItemBlock) stack.getItem()).getBlock().getStateFromMeta(stack.getMetadata());
			return colors.colorMultiplier(iblockstate, null, null, tintIndex);
		}, Blocks.GRASS, Blocks.TALLGRASS, Blocks.VINE, Blocks.LEAVES, Blocks.LEAVES2, Blocks.WATERLILY);
		itemcolors.registerItemColorHandler((stack, tintIndex) -> tintIndex == 0 ? PotionUtils.getColor(stack) : -1, Items.TIPPED_ARROW);
		itemcolors.registerItemColorHandler((stack, tintIndex) -> tintIndex == 0 ? -1 : ItemMap.getColor(stack), Items.FILLED_MAP);
		return itemcolors;
	}

	public int colorMultiplier(ItemStack stack, int tintIndex) {

		IItemColor iitemcolor = mapItemColors.getByValue(Item.REGISTRY.getIDForObject(stack.getItem()));
		return iitemcolor == null ? -1 : iitemcolor.colorMultiplier(stack, tintIndex);
	}

	public void registerItemColorHandler(IItemColor itemColor, Block... blocksIn) {

		for (Block block : blocksIn) {
			mapItemColors.put(itemColor, Item.getIdFromItem(Item.getItemFromBlock(block)));
		}
	}

	public void registerItemColorHandler(IItemColor itemColor, Item... itemsIn) {

		for (Item item : itemsIn) {
			mapItemColors.put(itemColor, Item.getIdFromItem(item));
		}
	}

}
