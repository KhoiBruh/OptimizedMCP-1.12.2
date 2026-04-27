package net.minecraft.client.util;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.List;

public class RecipeItemHelper {

	/**
	 * Map from {@link #pack} packed ids to counts
	 */
	public final Int2IntMap itemToCount = new Int2IntOpenHashMap();

	public static int pack(ItemStack stack) {

		Item item = stack.getItem();
		int i = item.getHasSubtypes() ? stack.getMetadata() : 0;
		return Item.REGISTRY.getIDForObject(item) << 16 | i & 65535;
	}

	public static ItemStack unpack(int p_194115_0_) {

		return p_194115_0_ == 0 ? ItemStack.EMPTY : new ItemStack(Item.getItemById(p_194115_0_ >> 16 & 65535), 1, p_194115_0_ & 65535);
	}

	public void accountStack(ItemStack stack) {

		if (!stack.isEmpty() && !stack.isItemDamaged() && !stack.isItemEnchanted() && !stack.hasDisplayName()) {
			int i = pack(stack);
			int j = stack.getCount();
			increment(i, j);
		}
	}

	public boolean containsItem(int p_194120_1_) {

		return itemToCount.get(p_194120_1_) > 0;
	}

	public int tryTake(int p_194122_1_, int maximum) {

		int i = itemToCount.get(p_194122_1_);

		if (i >= maximum) {
			itemToCount.put(p_194122_1_, i - maximum);
			return p_194122_1_;
		} else {
			return 0;
		}
	}

	private void increment(int p_194117_1_, int amount) {

		itemToCount.put(p_194117_1_, itemToCount.get(p_194117_1_) + amount);
	}

	public boolean canCraft(IRecipe recipe, @Nullable IntList p_194116_2_) {

		return canCraft(recipe, p_194116_2_, 1);
	}

	public boolean canCraft(IRecipe recipe, @Nullable IntList p_194118_2_, int p_194118_3_) {

		return (new RecipeItemHelper.RecipePicker(recipe)).tryPick(p_194118_3_, p_194118_2_);
	}

	public int getBiggestCraftableStack(IRecipe recipe, @Nullable IntList p_194114_2_) {

		return getBiggestCraftableStack(recipe, Integer.MAX_VALUE, p_194114_2_);
	}

	public int getBiggestCraftableStack(IRecipe recipe, int p_194121_2_, @Nullable IntList p_194121_3_) {

		return (new RecipeItemHelper.RecipePicker(recipe)).tryPickAll(p_194121_2_, p_194121_3_);
	}

	public void clear() {

		itemToCount.clear();
	}

	class RecipePicker {

		private final IRecipe recipe;
		private final List<Ingredient> ingredients = Lists.newArrayList();
		private final int ingredientCount;
		private final int[] possessedIngredientStacks;
		private final int possessedIngredientStackCount;
		private final BitSet data;
		private final IntList path = new IntArrayList();

		public RecipePicker(IRecipe p_i47608_2_) {

			recipe = p_i47608_2_;
			ingredients.addAll(p_i47608_2_.getIngredients());
			ingredients.removeIf((p_194103_0_) ->
			{
				return p_194103_0_ == Ingredient.EMPTY;
			});
			ingredientCount = ingredients.size();
			possessedIngredientStacks = getUniqueAvailIngredientItems();
			possessedIngredientStackCount = possessedIngredientStacks.length;
			data = new BitSet(ingredientCount + possessedIngredientStackCount + ingredientCount + ingredientCount * possessedIngredientStackCount);

			for (int i = 0; i < ingredients.size(); ++i) {
				IntList intlist = ingredients.get(i).getValidItemStacksPacked();

				for (int j = 0; j < possessedIngredientStackCount; ++j) {
					if (intlist.contains(possessedIngredientStacks[j])) {
						data.set(getIndex(true, j, i));
					}
				}
			}
		}

		public boolean tryPick(int p_194092_1_, @Nullable IntList listIn) {

			if (p_194092_1_ <= 0) {
				return true;
			} else {
				int k;

				for (k = 0; dfs(p_194092_1_); ++k) {
					tryTake(possessedIngredientStacks[path.getInt(0)], p_194092_1_);
					int l = path.size() - 1;
					setSatisfied(path.getInt(l));

					for (int i1 = 0; i1 < l; ++i1) {
						toggleResidual((i1 & 1) == 0, path.get(i1).intValue(), path.get(i1 + 1).intValue());
					}

					path.clear();
					data.clear(0, ingredientCount + possessedIngredientStackCount);
				}

				boolean flag = k == ingredientCount;
				boolean flag1 = flag && listIn != null;

				if (flag1) {
					listIn.clear();
				}

				data.clear(0, ingredientCount + possessedIngredientStackCount + ingredientCount);
				int j1 = 0;
				List<Ingredient> list = recipe.getIngredients();

				for (int k1 = 0; k1 < list.size(); ++k1) {
					if (flag1 && list.get(k1) == Ingredient.EMPTY) {
						listIn.add(0);
					} else {
						for (int l1 = 0; l1 < possessedIngredientStackCount; ++l1) {
							if (hasResidual(false, j1, l1)) {
								toggleResidual(true, l1, j1);
								increment(possessedIngredientStacks[l1], p_194092_1_);

								if (flag1) {
									listIn.add(possessedIngredientStacks[l1]);
								}
							}
						}

						++j1;
					}
				}

				return flag;
			}
		}

		private int[] getUniqueAvailIngredientItems() {

			IntCollection intcollection = new IntAVLTreeSet();

			for (Ingredient ingredient : ingredients) {
				intcollection.addAll(ingredient.getValidItemStacksPacked());
			}

			IntIterator intiterator = intcollection.iterator();

			while (intiterator.hasNext()) {
				if (!containsItem(intiterator.nextInt())) {
					intiterator.remove();
				}
			}

			return intcollection.toIntArray();
		}

		private boolean dfs(int p_194098_1_) {

			int k = possessedIngredientStackCount;

			for (int l = 0; l < k; ++l) {
				if (itemToCount.get(possessedIngredientStacks[l]) >= p_194098_1_) {
					visit(false, l);

					while (!path.isEmpty()) {
						int i1 = path.size();
						boolean flag = (i1 & 1) == 1;
						int j1 = path.getInt(i1 - 1);

						if (!flag && !isSatisfied(j1)) {
							break;
						}

						int k1 = flag ? ingredientCount : k;

						for (int l1 = 0; l1 < k1; ++l1) {
							if (!hasVisited(flag, l1) && hasConnection(flag, j1, l1) && hasResidual(flag, j1, l1)) {
								visit(flag, l1);
								break;
							}
						}

						int i2 = path.size();

						if (i2 == i1) {
							path.removeInt(i2 - 1);
						}
					}

					if (!path.isEmpty()) {
						return true;
					}
				}
			}

			return false;
		}

		private boolean isSatisfied(int p_194091_1_) {

			return data.get(getSatisfiedIndex(p_194091_1_));
		}

		private void setSatisfied(int p_194096_1_) {

			data.set(getSatisfiedIndex(p_194096_1_));
		}

		private int getSatisfiedIndex(int p_194094_1_) {

			return ingredientCount + possessedIngredientStackCount + p_194094_1_;
		}

		private boolean hasConnection(boolean p_194093_1_, int p_194093_2_, int p_194093_3_) {

			return data.get(getIndex(p_194093_1_, p_194093_2_, p_194093_3_));
		}

		private boolean hasResidual(boolean p_194100_1_, int p_194100_2_, int p_194100_3_) {

			return p_194100_1_ != data.get(1 + getIndex(p_194100_1_, p_194100_2_, p_194100_3_));
		}

		private void toggleResidual(boolean p_194089_1_, int p_194089_2_, int p_194089_3_) {

			data.flip(1 + getIndex(p_194089_1_, p_194089_2_, p_194089_3_));
		}

		private int getIndex(boolean p_194095_1_, int p_194095_2_, int p_194095_3_) {

			int k = p_194095_1_ ? p_194095_2_ * ingredientCount + p_194095_3_ : p_194095_3_ * ingredientCount + p_194095_2_;
			return ingredientCount + possessedIngredientStackCount + ingredientCount + 2 * k;
		}

		private void visit(boolean p_194088_1_, int p_194088_2_) {

			data.set(getVisitedIndex(p_194088_1_, p_194088_2_));
			path.add(p_194088_2_);
		}

		private boolean hasVisited(boolean p_194101_1_, int p_194101_2_) {

			return data.get(getVisitedIndex(p_194101_1_, p_194101_2_));
		}

		private int getVisitedIndex(boolean p_194099_1_, int p_194099_2_) {

			return (p_194099_1_ ? 0 : ingredientCount) + p_194099_2_;
		}

		public int tryPickAll(int p_194102_1_, @Nullable IntList list) {

			int k = 0;
			int l = Math.min(p_194102_1_, getMinIngredientCount()) + 1;

			while (true) {
				int i1 = (k + l) / 2;

				if (tryPick(i1, null)) {
					if (l - k <= 1) {
						if (i1 > 0) {
							tryPick(i1, list);
						}

						return i1;
					}

					k = i1;
				} else {
					l = i1;
				}
			}
		}

		private int getMinIngredientCount() {

			int k = Integer.MAX_VALUE;

			for (Ingredient ingredient : ingredients) {
				int l = 0;
				int i1;

				for (IntListIterator intlistiterator = ingredient.getValidItemStacksPacked().iterator(); intlistiterator.hasNext(); l = Math.max(l, itemToCount.get(i1))) {
					i1 = intlistiterator.next().intValue();
				}

				if (k > 0) {
					k = Math.min(k, l);
				}
			}

			return k;
		}

	}

}
