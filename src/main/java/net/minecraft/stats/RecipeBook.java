package net.minecraft.stats;

import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

import javax.annotation.Nullable;
import java.util.BitSet;

public class RecipeBook {

	protected final BitSet recipes = new BitSet();

	/**
	 * Recipes the player has not yet seen, so the GUI can play an animation
	 */
	protected final BitSet newRecipes = new BitSet();
	protected boolean isGuiOpen;
	protected boolean isFilteringCraftable;

	protected static int getRecipeId(@Nullable IRecipe recipe) {

		return CraftingManager.REGISTRY.getIDForObject(recipe);
	}

	public void copyFrom(RecipeBook that) {

		recipes.clear();
		newRecipes.clear();
		recipes.or(that.recipes);
		newRecipes.or(that.newRecipes);
	}

	public void unlock(IRecipe recipe) {

		if (!recipe.isDynamic()) {
			recipes.set(getRecipeId(recipe));
		}
	}

	public boolean isUnlocked(@Nullable IRecipe recipe) {

		return recipes.get(getRecipeId(recipe));
	}

	public void lock(IRecipe recipe) {

		int i = getRecipeId(recipe);
		recipes.clear(i);
		newRecipes.clear(i);
	}

	public boolean isNew(IRecipe recipe) {

		return newRecipes.get(getRecipeId(recipe));
	}

	public void markSeen(IRecipe recipe) {

		newRecipes.clear(getRecipeId(recipe));
	}

	public void markNew(IRecipe recipe) {

		newRecipes.set(getRecipeId(recipe));
	}

	public boolean isGuiOpen() {

		return isGuiOpen;
	}

	public void setGuiOpen(boolean open) {

		isGuiOpen = open;
	}

	public boolean isFilteringCraftable() {

		return isFilteringCraftable;
	}

	public void setFilteringCraftable(boolean shouldFilter) {

		isFilteringCraftable = shouldFilter;
	}

}
