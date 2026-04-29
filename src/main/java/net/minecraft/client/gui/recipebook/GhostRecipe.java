package net.minecraft.client.gui.recipebook;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.math.MathHelper;
import java.util.List;

public class GhostRecipe {

	private final List<GhostRecipe.GhostIngredient> ingredients = Lists.newArrayList();
	private IRecipe recipe;
	private float time;

	public void clear() {

		recipe = null;
		ingredients.clear();
		time = 0F;
	}

	public void addIngredient(Ingredient p_194187_1_, int p_194187_2_, int p_194187_3_) {

		ingredients.add(new GhostRecipe.GhostIngredient(p_194187_1_, p_194187_2_, p_194187_3_));
	}

	public GhostRecipe.GhostIngredient get(int p_192681_1_) {

		return ingredients.get(p_192681_1_);
	}

	public int size() {

		return ingredients.size();
	}

	
	public IRecipe getRecipe() {

		return recipe;
	}

	public void setRecipe(IRecipe p_192685_1_) {

		recipe = p_192685_1_;
	}

	public void render(Minecraft p_194188_1_, int p_194188_2_, int p_194188_3_, boolean p_194188_4_, float p_194188_5_) {

		if (!GuiScreen.isCtrlKeyDown()) {
			time += p_194188_5_;
		}

		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.disableLighting();

		for (int i = 0; i < ingredients.size(); ++i) {
			GhostRecipe.GhostIngredient ghostrecipe$ghostingredient = ingredients.get(i);
			int j = ghostrecipe$ghostingredient.getX() + p_194188_2_;
			int k = ghostrecipe$ghostingredient.getY() + p_194188_3_;

			if (i == 0 && p_194188_4_) {
				Gui.drawRect(j - 4, k - 4, j + 20, k + 20, 822018048);
			} else {
				Gui.drawRect(j, k, j + 16, k + 16, 822018048);
			}

			GlStateManager.disableLighting();
			ItemStack itemstack = ghostrecipe$ghostingredient.getItem();
			RenderItem renderitem = p_194188_1_.getRenderItem();
			renderitem.renderItemAndEffectIntoGUI(p_194188_1_.player, itemstack, j, k);
			GlStateManager.depthFunc(516);
			Gui.drawRect(j, k, j + 16, k + 16, 822083583);
			GlStateManager.depthFunc(515);

			if (i == 0) {
				renderitem.renderItemOverlays(p_194188_1_.fontRenderer, itemstack, j, k);
			}

			GlStateManager.enableLighting();
		}

		RenderHelper.disableStandardItemLighting();
	}

	public class GhostIngredient {

		private final Ingredient ingredient;
		private final int x;
		private final int y;

		public GhostIngredient(Ingredient p_i47604_2_, int p_i47604_3_, int p_i47604_4_) {

			ingredient = p_i47604_2_;
			x = p_i47604_3_;
			y = p_i47604_4_;
		}

		public int getX() {

			return x;
		}

		public int getY() {

			return y;
		}

		public ItemStack getItem() {

			ItemStack[] aitemstack = ingredient.getMatchingStacks();
			return aitemstack[MathHelper.floor(time / 30F) % aitemstack.length];
		}

	}

}
