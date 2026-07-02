package net.minecraft.client.gui.recipebook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.component.Button;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Maths;

import java.util.List;

public class RecipeButton extends Button {

	private static final ResourceLocation RECIPE_BOOK = new ResourceLocation("textures/gui/recipe_book.png");
	private RecipeBook book;
	private RecipeList list;
	private float time;
	private float animationTime;
	private int currentIndex;

	public RecipeButton() {
		super(0, 0, 0, 25, 25, "");
	}

	public void init(RecipeList p_193928_1_, RecipeBookPage p_193928_2_, RecipeBook p_193928_3_) {
		list = p_193928_1_;
		book = p_193928_3_;
		List<IRecipe> list = p_193928_1_.getRecipes(p_193928_3_.isFilteringCraftable());

		for (IRecipe irecipe : list) {
			if (p_193928_3_.isNew(irecipe)) {
				p_193928_2_.recipesShown(list);
				animationTime = 15F;
				break;
			}
		}
	}

	public RecipeList getList() {
		return list;
	}

	public void setPosition(int p_191770_1_, int p_191770_2_) {
		x = p_191770_1_;
		y = p_191770_2_;
	}

	/**
	 * Draws this button to the screen.
	 */
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			if (!Screen.isCtrlDown()) {
				time += partialTicks;
			}

			hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			RenderHelper.enableGUIStandardItemLighting();
			mc.getTextureManager().bindTexture(RECIPE_BOOK);
			GLS.disableLighting();
			int i = 29;

			if (!list.containsCraftableRecipes()) {
				i += 25;
			}

			int j = 206;

			if (list.getRecipes(book.isFilteringCraftable()).size() > 1) {
				j += 25;
			}

			boolean flag = animationTime > 0F;

			if (flag) {
				float f = 1F + 0.1F * (float) Math.sin(animationTime / 15F * (float) Math.PI);
				GLS.pushMatrix();
				GLS.translate((float) (x + 8), (float) (y + 12), 0F);
				GLS.scale(f, f, 1F);
				GLS.translate((float) (-(x + 8)), (float) (-(y + 12)), 0F);
				animationTime -= partialTicks;
			}

			drawTexturedModalRect(x, y, i, j, width, height);
			List<IRecipe> list = getOrderedRecipes();
			currentIndex = Maths.floor(time / 30F) % list.size();
			ItemStack itemstack = list.get(currentIndex).getRecipeOutput();
			int k = 4;

			if (this.list.hasSingleResultItem() && getOrderedRecipes().size() > 1) {
				mc.getRenderItem().renderItemAndEffectIntoGUI(mc.getDrawContext(), itemstack, x + k + 1, y + k + 1);
				--k;
			}

			mc.getRenderItem().renderItemAndEffectIntoGUI(mc.getDrawContext(), itemstack, x + k, y + k);

			if (flag) {
				GLS.popMatrix();
			}

			GLS.enableLighting();
			RenderHelper.disableStandardItemLighting();
		}
	}

	private List<IRecipe> getOrderedRecipes() {
		List<IRecipe> list = this.list.getDisplayRecipes(true);

		if (!book.isFilteringCraftable()) {
			list.addAll(this.list.getDisplayRecipes(false));
		}

		return list;
	}

	public boolean isOnlyOption() {
		return getOrderedRecipes().size() == 1;
	}

	public IRecipe getRecipe() {
		List<IRecipe> list = getOrderedRecipes();
		return list.get(currentIndex);
	}

	public List<String> getToolTipText(Screen p_191772_1_) {
		ItemStack itemstack = getOrderedRecipes().get(currentIndex).getRecipeOutput();
		List<String> list = p_191772_1_.getItemToolTip(itemstack);

		if (this.list.getRecipes(book.isFilteringCraftable()).size() > 1) {
			list.add(I18n.format("gui.recipebook.moreRecipes"));
		}

		return list;
	}

	public int getButtonWidth() {
		return 25;
	}

}
