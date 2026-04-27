package net.minecraft.client.gui.recipebook;

import com.google.common.collect.Lists;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.RecipeBook;

import java.util.BitSet;
import java.util.List;

public class RecipeList {

	private final List<IRecipe> recipes = Lists.newArrayList();
	private final BitSet craftable = new BitSet();
	private final BitSet canFit = new BitSet();
	private final BitSet inBook = new BitSet();
	private boolean singleResultItem = true;

	/**
	 * Checks if recipebook is not empty
	 */
	public boolean isNotEmpty() {

		return !inBook.isEmpty();
	}

	public void updateKnownRecipes(RecipeBook book) {

		for (int i = 0; i < recipes.size(); ++i) {
			inBook.set(i, book.isUnlocked(recipes.get(i)));
		}
	}

	public void canCraft(RecipeItemHelper handler, int width, int height, RecipeBook book) {

		for (int i = 0; i < recipes.size(); ++i) {
			IRecipe irecipe = recipes.get(i);
			boolean flag = irecipe.canFit(width, height) && book.isUnlocked(irecipe);
			canFit.set(i, flag);
			craftable.set(i, flag && handler.canCraft(irecipe, null));
		}
	}

	public boolean isCraftable(IRecipe recipe) {

		return craftable.get(recipes.indexOf(recipe));
	}

	public boolean containsCraftableRecipes() {

		return !craftable.isEmpty();
	}

	public boolean containsValidRecipes() {

		return !canFit.isEmpty();
	}

	public List<IRecipe> getRecipes() {

		return recipes;
	}

	public List<IRecipe> getRecipes(boolean p_194208_1_) {

		List<IRecipe> list = Lists.newArrayList();

		for (int i = inBook.nextSetBit(0); i >= 0; i = inBook.nextSetBit(i + 1)) {
			if ((p_194208_1_ ? craftable : canFit).get(i)) {
				list.add(recipes.get(i));
			}
		}

		return list;
	}

	public List<IRecipe> getDisplayRecipes(boolean onlyCraftable) {

		List<IRecipe> list = Lists.newArrayList();

		for (int i = inBook.nextSetBit(0); i >= 0; i = inBook.nextSetBit(i + 1)) {
			if (canFit.get(i) && craftable.get(i) == onlyCraftable) {
				list.add(recipes.get(i));
			}
		}

		return list;
	}

	public void add(IRecipe recipe) {

		recipes.add(recipe);

		if (singleResultItem) {
			ItemStack itemstack = recipes.getFirst().getRecipeOutput();
			ItemStack itemstack1 = recipe.getRecipeOutput();
			singleResultItem = ItemStack.areItemsEqual(itemstack, itemstack1) && ItemStack.areItemStackTagsEqual(itemstack, itemstack1);
		}
	}

	public boolean hasSingleResultItem() {

		return singleResultItem;
	}

}
