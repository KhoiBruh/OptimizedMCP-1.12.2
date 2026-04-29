package net.minecraft.client.gui.recipebook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.component.GuiButtonToggle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.util.RecipeBookClient;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.RecipeBook;

import java.util.Iterator;
import java.util.List;

public class GuiButtonRecipeTab extends GuiButtonToggle {

	private final CreativeTabs category;
	private float animationTime;

	public GuiButtonRecipeTab(int p_i47588_1_, CreativeTabs p_i47588_2_) {

		super(p_i47588_1_, 0, 0, 35, 27, false);
		category = p_i47588_2_;
		initTextureValues(153, 2, 35, 0, GuiRecipeBook.RECIPE_BOOK);
	}

	public void startAnimation(Minecraft p_193918_1_) {

		RecipeBook recipebook = p_193918_1_.player.getRecipeBook();
		label21:

		for (RecipeList recipelist : RecipeBookClient.RECIPES_BY_TAB.get(category)) {
			Iterator<IRecipe> iterator = recipelist.getRecipes(recipebook.isFilteringCraftable()).iterator();

			while (true) {
				if (!iterator.hasNext()) {
					continue label21;
				}

				IRecipe irecipe = (IRecipe) iterator.next();

				if (recipebook.isNew(irecipe)) {
					break;
				}
			}

			animationTime = 15.0F;
			return;
		}
	}

	/**
	 * Draws this button to the screen.
	 */
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {

		if (visible) {
			if (animationTime > 0.0F) {
				float f = 1.0F + 0.1F * (float) Math.sin(animationTime / 15.0F * (float) Math.PI);
				GlStateManager.pushMatrix();
				GlStateManager.translate((float) (x + 8), (float) (y + 12), 0.0F);
				GlStateManager.scale(1.0F, f, 1.0F);
				GlStateManager.translate((float) (-(x + 8)), (float) (-(y + 12)), 0.0F);
			}

			hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			mc.getTextureManager().bindTexture(resourceLocation);
			GlStateManager.disableDepth();
			int k = xTexStart;
			int i = yTexStart;

			if (stateTriggered) {
				k += xDiffTex;
			}

			if (hovered) {
				i += yDiffTex;
			}

			int j = x;

			if (stateTriggered) {
				j -= 2;
			}

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			drawTexturedModalRect(j, y, k, i, width, height);
			GlStateManager.enableDepth();
			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.disableLighting();
			renderIcon(mc.getRenderItem());
			GlStateManager.enableLighting();
			RenderHelper.disableStandardItemLighting();

			if (animationTime > 0.0F) {
				GlStateManager.popMatrix();
				animationTime -= partialTicks;
			}
		}
	}

	private void renderIcon(RenderItem p_193920_1_) {

		ItemStack itemstack = category.getIconItemStack();

		if (category == CreativeTabs.TOOLS) {
			p_193920_1_.renderItemAndEffectIntoGUI(itemstack, x + 3, y + 5);
			p_193920_1_.renderItemAndEffectIntoGUI(CreativeTabs.COMBAT.getIconItemStack(), x + 14, y + 5);
		} else if (category == CreativeTabs.MISC) {
			p_193920_1_.renderItemAndEffectIntoGUI(itemstack, x + 3, y + 5);
			p_193920_1_.renderItemAndEffectIntoGUI(CreativeTabs.FOOD.getIconItemStack(), x + 14, y + 5);
		} else {
			p_193920_1_.renderItemAndEffectIntoGUI(itemstack, x + 9, y + 5);
		}
	}

	public CreativeTabs getCategory() {

		return category;
	}

	public boolean updateVisibility() {

		List<RecipeList> list = RecipeBookClient.RECIPES_BY_TAB.get(category);
		visible = false;

		for (RecipeList recipelist : list) {
			if (recipelist.isNotEmpty() && recipelist.containsValidRecipes()) {
				visible = true;
				break;
			}
		}

		return visible;
	}

}
