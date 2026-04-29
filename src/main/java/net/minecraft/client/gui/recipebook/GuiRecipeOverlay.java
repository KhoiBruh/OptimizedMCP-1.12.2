package net.minecraft.client.gui.recipebook;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.component.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GuiRecipeOverlay extends Gui {

	private static final ResourceLocation RECIPE_BOOK_TEXTURE = new ResourceLocation("textures/gui/recipe_book.png");
	private final List<GuiRecipeOverlay.Button> buttonList = Lists.newArrayList();
	private boolean visible;
	private int x;
	private int y;
	private Minecraft mc;
	private RecipeList recipeList;
	private IRecipe lastRecipeClicked;
	private float time;

	public void init(Minecraft mcIn, RecipeList recipeListIn, int p_191845_3_, int p_191845_4_, int p_191845_5_, int p_191845_6_, float p_191845_7_, RecipeBook p_191845_8_) {

		mc = mcIn;
		recipeList = recipeListIn;
		boolean flag = p_191845_8_.isFilteringCraftable();
		List<IRecipe> list = recipeListIn.getDisplayRecipes(true);
		List<IRecipe> list1 = flag ? Collections.emptyList() : recipeListIn.getDisplayRecipes(false);
		int i = list.size();
		int j = i + list1.size();
		int k = j <= 16 ? 4 : 5;
		int l = (int) Math.ceil((float) j / (float) k);
		x = p_191845_3_;
		y = p_191845_4_;
		int i1 = 25;
		float f = (float) (x + Math.min(j, k) * 25);
		float f1 = (float) (p_191845_5_ + 50);

		if (f > f1) {
			x = (int) ((float) x - p_191845_7_ * (float) ((int) ((f - f1) / p_191845_7_)));
		}

		float f2 = (float) (y + l * 25);
		float f3 = (float) (p_191845_6_ + 50);

		if (f2 > f3) {
			y = (int) ((float) y - p_191845_7_ * (float) MathHelper.ceil((f2 - f3) / p_191845_7_));
		}

		float f4 = (float) y;
		float f5 = (float) (p_191845_6_ - 100);

		if (f4 < f5) {
			y = (int) ((float) y - p_191845_7_ * (float) MathHelper.ceil((f4 - f5) / p_191845_7_));
		}

		visible = true;
		buttonList.clear();

		for (int j1 = 0; j1 < j; ++j1) {
			boolean flag1 = j1 < i;
			buttonList.add(new GuiRecipeOverlay.Button(x + 4 + 25 * (j1 % k), y + 5 + 25 * (j1 / k), flag1 ? list.get(j1) : list1.get(j1 - i), flag1));
		}

		lastRecipeClicked = null;
	}

	public RecipeList getRecipeList() {

		return recipeList;
	}

	public IRecipe getLastRecipeClicked() {

		return lastRecipeClicked;
	}

	public boolean buttonClicked(int p_193968_1_, int p_193968_2_, int p_193968_3_) {

		if (p_193968_3_ != 0) {
			return false;
		} else {
			for (GuiRecipeOverlay.Button guirecipeoverlay$button : buttonList) {
				if (guirecipeoverlay$button.mousePressed(mc, p_193968_1_, p_193968_2_)) {
					lastRecipeClicked = guirecipeoverlay$button.recipe;
					return true;
				}
			}

			return false;
		}
	}

	public void render(int p_191842_1_, int p_191842_2_, float p_191842_3_) {

		if (visible) {
			time += p_191842_3_;
			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.enableBlend();
			GlStateManager.color(1F, 1F, 1F, 1F);
			mc.getTextureManager().bindTexture(RECIPE_BOOK_TEXTURE);
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, 0F, 170F);
			int i = buttonList.size() <= 16 ? 4 : 5;
			int j = Math.min(buttonList.size(), i);
			int k = MathHelper.ceil((float) buttonList.size() / (float) i);
			int l = 24;
			int i1 = 4;
			int j1 = 82;
			int k1 = 208;
			nineInchSprite(j, k, 24, 4, 82, 208);
			GlStateManager.disableBlend();
			RenderHelper.disableStandardItemLighting();

			for (GuiRecipeOverlay.Button guirecipeoverlay$button : buttonList) {
				guirecipeoverlay$button.drawButton(mc, p_191842_1_, p_191842_2_, p_191842_3_);
			}

			GlStateManager.popMatrix();
		}
	}

	private void nineInchSprite(int p_191846_1_, int p_191846_2_, int p_191846_3_, int p_191846_4_, int p_191846_5_, int p_191846_6_) {

		drawTexturedModalRect(x, y, p_191846_5_, p_191846_6_, p_191846_4_, p_191846_4_);
		drawTexturedModalRect(x + p_191846_4_ * 2 + p_191846_1_ * p_191846_3_, y, p_191846_5_ + p_191846_3_ + p_191846_4_, p_191846_6_, p_191846_4_, p_191846_4_);
		drawTexturedModalRect(x, y + p_191846_4_ * 2 + p_191846_2_ * p_191846_3_, p_191846_5_, p_191846_6_ + p_191846_3_ + p_191846_4_, p_191846_4_, p_191846_4_);
		drawTexturedModalRect(x + p_191846_4_ * 2 + p_191846_1_ * p_191846_3_, y + p_191846_4_ * 2 + p_191846_2_ * p_191846_3_, p_191846_5_ + p_191846_3_ + p_191846_4_, p_191846_6_ + p_191846_3_ + p_191846_4_, p_191846_4_, p_191846_4_);

		for (int i = 0; i < p_191846_1_; ++i) {
			drawTexturedModalRect(x + p_191846_4_ + i * p_191846_3_, y, p_191846_5_ + p_191846_4_, p_191846_6_, p_191846_3_, p_191846_4_);
			drawTexturedModalRect(x + p_191846_4_ + (i + 1) * p_191846_3_, y, p_191846_5_ + p_191846_4_, p_191846_6_, p_191846_4_, p_191846_4_);

			for (int j = 0; j < p_191846_2_; ++j) {
				if (i == 0) {
					drawTexturedModalRect(x, y + p_191846_4_ + j * p_191846_3_, p_191846_5_, p_191846_6_ + p_191846_4_, p_191846_4_, p_191846_3_);
					drawTexturedModalRect(x, y + p_191846_4_ + (j + 1) * p_191846_3_, p_191846_5_, p_191846_6_ + p_191846_4_, p_191846_4_, p_191846_4_);
				}

				drawTexturedModalRect(x + p_191846_4_ + i * p_191846_3_, y + p_191846_4_ + j * p_191846_3_, p_191846_5_ + p_191846_4_, p_191846_6_ + p_191846_4_, p_191846_3_, p_191846_3_);
				drawTexturedModalRect(x + p_191846_4_ + (i + 1) * p_191846_3_, y + p_191846_4_ + j * p_191846_3_, p_191846_5_ + p_191846_4_, p_191846_6_ + p_191846_4_, p_191846_4_, p_191846_3_);
				drawTexturedModalRect(x + p_191846_4_ + i * p_191846_3_, y + p_191846_4_ + (j + 1) * p_191846_3_, p_191846_5_ + p_191846_4_, p_191846_6_ + p_191846_4_, p_191846_3_, p_191846_4_);
				drawTexturedModalRect(x + p_191846_4_ + (i + 1) * p_191846_3_ - 1, y + p_191846_4_ + (j + 1) * p_191846_3_ - 1, p_191846_5_ + p_191846_4_, p_191846_6_ + p_191846_4_, p_191846_4_ + 1, p_191846_4_ + 1);

				if (i == p_191846_1_ - 1) {
					drawTexturedModalRect(x + p_191846_4_ * 2 + p_191846_1_ * p_191846_3_, y + p_191846_4_ + j * p_191846_3_, p_191846_5_ + p_191846_3_ + p_191846_4_, p_191846_6_ + p_191846_4_, p_191846_4_, p_191846_3_);
					drawTexturedModalRect(x + p_191846_4_ * 2 + p_191846_1_ * p_191846_3_, y + p_191846_4_ + (j + 1) * p_191846_3_, p_191846_5_ + p_191846_3_ + p_191846_4_, p_191846_6_ + p_191846_4_, p_191846_4_, p_191846_4_);
				}
			}

			drawTexturedModalRect(x + p_191846_4_ + i * p_191846_3_, y + p_191846_4_ * 2 + p_191846_2_ * p_191846_3_, p_191846_5_ + p_191846_4_, p_191846_6_ + p_191846_3_ + p_191846_4_, p_191846_3_, p_191846_4_);
			drawTexturedModalRect(x + p_191846_4_ + (i + 1) * p_191846_3_, y + p_191846_4_ * 2 + p_191846_2_ * p_191846_3_, p_191846_5_ + p_191846_4_, p_191846_6_ + p_191846_3_ + p_191846_4_, p_191846_4_, p_191846_4_);
		}
	}

	public boolean isVisible() {

		return visible;
	}

	public void setVisible(boolean p_192999_1_) {

		visible = p_192999_1_;
	}

	class Button extends GuiButton {

		private final IRecipe recipe;
		private final boolean isCraftable;

		public Button(int p_i47594_2_, int p_i47594_3_, IRecipe p_i47594_4_, boolean p_i47594_5_) {

			super(0, p_i47594_2_, p_i47594_3_, "");
			width = 24;
			height = 24;
			recipe = p_i47594_4_;
			isCraftable = p_i47594_5_;
		}

		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {

			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.enableAlpha();
			mc.getTextureManager().bindTexture(GuiRecipeOverlay.RECIPE_BOOK_TEXTURE);
			hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			int i = 152;

			if (!isCraftable) {
				i += 26;
			}

			int j = 78;

			if (hovered) {
				j += 26;
			}

			drawTexturedModalRect(x, y, i, j, width, height);
			int k = 3;
			int l = 3;

			if (recipe instanceof ShapedRecipes shapedrecipes) {
				k = shapedrecipes.getWidth();
				l = shapedrecipes.getHeight();
			}

			Iterator<Ingredient> iterator = recipe.getIngredients().iterator();

			for (int i1 = 0; i1 < l; ++i1) {
				int j1 = 3 + i1 * 7;

				for (int k1 = 0; k1 < k; ++k1) {
					if (iterator.hasNext()) {
						ItemStack[] aitemstack = iterator.next().getMatchingStacks();

						if (aitemstack.length != 0) {
							int l1 = 3 + k1 * 7;
							GlStateManager.pushMatrix();
							float f = 0.42F;
							int i2 = (int) ((float) (x + l1) / 0.42F - 3F);
							int j2 = (int) ((float) (y + j1) / 0.42F - 3F);
							GlStateManager.scale(0.42F, 0.42F, 1F);
							GlStateManager.enableLighting();
							mc.getRenderItem().renderItemAndEffectIntoGUI(aitemstack[MathHelper.floor(time / 30F) % aitemstack.length], i2, j2);
							GlStateManager.disableLighting();
							GlStateManager.popMatrix();
						}
					}
				}
			}

			GlStateManager.disableAlpha();
			RenderHelper.disableStandardItemLighting();
		}

	}

}
