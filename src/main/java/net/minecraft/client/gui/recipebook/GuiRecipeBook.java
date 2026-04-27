package net.minecraft.client.gui.recipebook;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButtonToggle;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.RecipeBookClient;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.client.util.SearchTreeManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.network.play.client.CPacketRecipeInfo;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class GuiRecipeBook extends Gui implements IRecipeUpdateListener {

	protected static final ResourceLocation RECIPE_BOOK = new ResourceLocation("textures/gui/recipe_book.png");
	private final GhostRecipe ghostRecipe = new GhostRecipe();
	private final List<GuiButtonRecipeTab> recipeTabs = Lists.newArrayList(new GuiButtonRecipeTab(0, CreativeTabs.SEARCH), new GuiButtonRecipeTab(0, CreativeTabs.TOOLS), new GuiButtonRecipeTab(0, CreativeTabs.BUILDING_BLOCKS), new GuiButtonRecipeTab(0, CreativeTabs.MISC), new GuiButtonRecipeTab(0, CreativeTabs.REDSTONE));
	private final RecipeBookPage recipeBookPage = new RecipeBookPage();
	private final RecipeItemHelper stackedContents = new RecipeItemHelper();
	private int xOffset;
	private int width;
	private int height;
	private GuiButtonRecipeTab currentTab;
	/**
	 * This button toggles between showing all recipes and showing only craftable recipes
	 */
	private GuiButtonToggle toggleRecipesBtn;
	private InventoryCrafting craftingSlots;
	private Minecraft mc;
	private GuiTextField searchBar;
	private String lastSearch = "";
	private RecipeBook recipeBook;
	private int timesInventoryChanged;

	public void func_194303_a(int p_194303_1_, int p_194303_2_, Minecraft p_194303_3_, boolean p_194303_4_, InventoryCrafting p_194303_5_) {

		mc = p_194303_3_;
		width = p_194303_1_;
		height = p_194303_2_;
		craftingSlots = p_194303_5_;
		recipeBook = p_194303_3_.player.getRecipeBook();
		timesInventoryChanged = p_194303_3_.player.inventory.getTimesChanged();
		currentTab = recipeTabs.get(0);
		currentTab.setStateTriggered(true);

		if (isVisible()) {
			initVisuals(p_194303_4_, p_194303_5_);
		}

		Keyboard.enableRepeatEvents(true);
	}

	public void initVisuals(boolean p_193014_1_, InventoryCrafting p_193014_2_) {

		xOffset = p_193014_1_ ? 0 : 86;
		int i = (width - 147) / 2 - xOffset;
		int j = (height - 166) / 2;
		stackedContents.clear();
		mc.player.inventory.fillStackedContents(stackedContents, false);
		p_193014_2_.fillStackedContents(stackedContents);
		searchBar = new GuiTextField(0, mc.fontRenderer, i + 25, j + 14, 80, mc.fontRenderer.FONT_HEIGHT + 5);
		searchBar.setMaxStringLength(50);
		searchBar.setEnableBackgroundDrawing(false);
		searchBar.setVisible(true);
		searchBar.setTextColor(16777215);
		recipeBookPage.init(mc, i, j);
		recipeBookPage.addListener(this);
		toggleRecipesBtn = new GuiButtonToggle(0, i + 110, j + 12, 26, 16, recipeBook.isFilteringCraftable());
		toggleRecipesBtn.initTextureValues(152, 41, 28, 18, RECIPE_BOOK);
		updateCollections(false);
		updateTabs();
	}

	public void removed() {

		Keyboard.enableRepeatEvents(false);
	}

	public int updateScreenPosition(boolean p_193011_1_, int p_193011_2_, int p_193011_3_) {

		int i;

		if (isVisible() && !p_193011_1_) {
			i = 177 + (p_193011_2_ - p_193011_3_ - 200) / 2;
		} else {
			i = (p_193011_2_ - p_193011_3_) / 2;
		}

		return i;
	}

	public void toggleVisibility() {

		setVisible(!isVisible());
	}

	public boolean isVisible() {

		return recipeBook.isGuiOpen();
	}

	private void setVisible(boolean p_193006_1_) {

		recipeBook.setGuiOpen(p_193006_1_);

		if (!p_193006_1_) {
			recipeBookPage.setInvisible();
		}

		sendUpdateSettings();
	}

	public void slotClicked(@Nullable Slot slotIn) {

		if (slotIn != null && slotIn.slotNumber <= 9) {
			ghostRecipe.clear();

			if (isVisible()) {
				updateStackedContents();
			}
		}
	}

	private void updateCollections(boolean p_193003_1_) {

		List<RecipeList> list = RecipeBookClient.RECIPES_BY_TAB.get(currentTab.getCategory());
		list.forEach((p_193944_1_) ->
		{
			p_193944_1_.canCraft(stackedContents, craftingSlots.getWidth(), craftingSlots.getHeight(), recipeBook);
		});
		List<RecipeList> list1 = Lists.newArrayList(list);
		list1.removeIf((p_193952_0_) ->
		{
			return !p_193952_0_.isNotEmpty();
		});
		list1.removeIf((p_193953_0_) ->
		{
			return !p_193953_0_.containsValidRecipes();
		});
		String s = searchBar.getText();

		if (!s.isEmpty()) {
			ObjectSet<RecipeList> objectset = new ObjectLinkedOpenHashSet<RecipeList>(mc.getSearchTree(SearchTreeManager.RECIPES).search(s.toLowerCase(Locale.ROOT)));
			list1.removeIf((p_193947_1_) ->
			{
				return !objectset.contains(p_193947_1_);
			});
		}

		if (recipeBook.isFilteringCraftable()) {
			list1.removeIf((p_193958_0_) ->
			{
				return !p_193958_0_.containsCraftableRecipes();
			});
		}

		recipeBookPage.updateLists(list1, p_193003_1_);
	}

	private void updateTabs() {

		int i = (width - 147) / 2 - xOffset - 30;
		int j = (height - 166) / 2 + 3;
		int k = 27;
		int l = 0;

		for (GuiButtonRecipeTab guibuttonrecipetab : recipeTabs) {
			CreativeTabs creativetabs = guibuttonrecipetab.getCategory();

			if (creativetabs == CreativeTabs.SEARCH) {
				guibuttonrecipetab.visible = true;
				guibuttonrecipetab.setPosition(i, j + 27 * l++);
			} else if (guibuttonrecipetab.updateVisibility()) {
				guibuttonrecipetab.setPosition(i, j + 27 * l++);
				guibuttonrecipetab.startAnimation(mc);
			}
		}
	}

	public void tick() {

		if (isVisible()) {
			if (timesInventoryChanged != mc.player.inventory.getTimesChanged()) {
				updateStackedContents();
				timesInventoryChanged = mc.player.inventory.getTimesChanged();
			}
		}
	}

	private void updateStackedContents() {

		stackedContents.clear();
		mc.player.inventory.fillStackedContents(stackedContents, false);
		craftingSlots.fillStackedContents(stackedContents);
		updateCollections(false);
	}

	public void render(int mouseX, int mouseY, float partialTicks) {

		if (isVisible()) {
			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.disableLighting();
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0F, 0.0F, 100.0F);
			mc.getTextureManager().bindTexture(RECIPE_BOOK);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			int i = (width - 147) / 2 - xOffset;
			int j = (height - 166) / 2;
			drawTexturedModalRect(i, j, 1, 1, 147, 166);
			searchBar.drawTextBox();
			RenderHelper.disableStandardItemLighting();

			for (GuiButtonRecipeTab guibuttonrecipetab : recipeTabs) {
				guibuttonrecipetab.drawButton(mc, mouseX, mouseY, partialTicks);
			}

			toggleRecipesBtn.drawButton(mc, mouseX, mouseY, partialTicks);
			recipeBookPage.render(i, j, mouseX, mouseY, partialTicks);
			GlStateManager.popMatrix();
		}
	}

	public void renderTooltip(int p_191876_1_, int p_191876_2_, int p_191876_3_, int p_191876_4_) {

		if (isVisible()) {
			recipeBookPage.renderTooltip(p_191876_3_, p_191876_4_);

			if (toggleRecipesBtn.isMouseOver()) {
				String s1 = I18n.format(toggleRecipesBtn.isStateTriggered() ? "gui.recipebook.toggleRecipes.craftable" : "gui.recipebook.toggleRecipes.all");

				if (mc.currentScreen != null) {
					mc.currentScreen.drawHoveringText(s1, p_191876_3_, p_191876_4_);
				}
			}

			renderGhostRecipeTooltip(p_191876_1_, p_191876_2_, p_191876_3_, p_191876_4_);
		}
	}

	private void renderGhostRecipeTooltip(int p_193015_1_, int p_193015_2_, int p_193015_3_, int p_193015_4_) {

		ItemStack itemstack = null;

		for (int i = 0; i < ghostRecipe.size(); ++i) {
			GhostRecipe.GhostIngredient ghostrecipe$ghostingredient = ghostRecipe.get(i);
			int j = ghostrecipe$ghostingredient.getX() + p_193015_1_;
			int k = ghostrecipe$ghostingredient.getY() + p_193015_2_;

			if (p_193015_3_ >= j && p_193015_4_ >= k && p_193015_3_ < j + 16 && p_193015_4_ < k + 16) {
				itemstack = ghostrecipe$ghostingredient.getItem();
			}
		}

		if (itemstack != null && mc.currentScreen != null) {
			mc.currentScreen.drawHoveringText(mc.currentScreen.getItemToolTip(itemstack), p_193015_3_, p_193015_4_);
		}
	}

	public void renderGhostRecipe(int p_191864_1_, int p_191864_2_, boolean p_191864_3_, float p_191864_4_) {

		ghostRecipe.render(mc, p_191864_1_, p_191864_2_, p_191864_3_, p_191864_4_);
	}

	public boolean mouseClicked(int p_191862_1_, int p_191862_2_, int p_191862_3_) {

		if (isVisible() && !mc.player.isSpectator()) {
			if (recipeBookPage.mouseClicked(p_191862_1_, p_191862_2_, p_191862_3_, (width - 147) / 2 - xOffset, (height - 166) / 2, 147, 166)) {
				IRecipe irecipe = recipeBookPage.getLastClickedRecipe();
				RecipeList recipelist = recipeBookPage.getLastClickedRecipeList();

				if (irecipe != null && recipelist != null) {
					if (!recipelist.isCraftable(irecipe) && ghostRecipe.getRecipe() == irecipe) {
						return false;
					}

					ghostRecipe.clear();
					mc.playerController.func_194338_a(mc.player.openContainer.windowId, irecipe, GuiScreen.isShiftKeyDown(), mc.player);

					if (!isOffsetNextToMainGUI() && p_191862_3_ == 0) {
						setVisible(false);
					}
				}

				return true;
			} else if (p_191862_3_ != 0) {
				return false;
			} else if (searchBar.mouseClicked(p_191862_1_, p_191862_2_, p_191862_3_)) {
				return true;
			} else if (toggleRecipesBtn.mousePressed(mc, p_191862_1_, p_191862_2_)) {
				boolean flag = !recipeBook.isFilteringCraftable();
				recipeBook.setFilteringCraftable(flag);
				toggleRecipesBtn.setStateTriggered(flag);
				toggleRecipesBtn.playPressSound(mc.getSoundHandler());
				sendUpdateSettings();
				updateCollections(false);
				return true;
			} else {
				for (GuiButtonRecipeTab guibuttonrecipetab : recipeTabs) {
					if (guibuttonrecipetab.mousePressed(mc, p_191862_1_, p_191862_2_)) {
						if (currentTab != guibuttonrecipetab) {
							guibuttonrecipetab.playPressSound(mc.getSoundHandler());
							currentTab.setStateTriggered(false);
							currentTab = guibuttonrecipetab;
							currentTab.setStateTriggered(true);
							updateCollections(true);
						}

						return true;
					}
				}

				return false;
			}
		} else {
			return false;
		}
	}

	public boolean hasClickedOutside(int p_193955_1_, int p_193955_2_, int p_193955_3_, int p_193955_4_, int p_193955_5_, int p_193955_6_) {

		if (!isVisible()) {
			return true;
		} else {
			boolean flag = p_193955_1_ < p_193955_3_ || p_193955_2_ < p_193955_4_ || p_193955_1_ >= p_193955_3_ + p_193955_5_ || p_193955_2_ >= p_193955_4_ + p_193955_6_;
			boolean flag1 = p_193955_3_ - 147 < p_193955_1_ && p_193955_1_ < p_193955_3_ && p_193955_4_ < p_193955_2_ && p_193955_2_ < p_193955_4_ + p_193955_6_;
			return flag && !flag1 && !currentTab.mousePressed(mc, p_193955_1_, p_193955_2_);
		}
	}

	public boolean keyPressed(char typedChar, int keycode) {

		if (isVisible() && !mc.player.isSpectator()) {
			if (keycode == 1 && !isOffsetNextToMainGUI()) {
				setVisible(false);
				return true;
			} else {
				if (GameSettings.isKeyDown(mc.gameSettings.keyBindChat) && !searchBar.isFocused()) {
					searchBar.setFocused(true);
				} else if (searchBar.textboxKeyTyped(typedChar, keycode)) {
					String s1 = searchBar.getText().toLowerCase(Locale.ROOT);
					pirateRecipe(s1);

					if (!s1.equals(lastSearch)) {
						updateCollections(false);
						lastSearch = s1;
					}

					return true;
				}

				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * "Check if we should activate the pirate speak easter egg"
	 *
	 * @param text 'if equal to "excitedze", activate the easter egg'
	 */
	private void pirateRecipe(String text) {

		if ("excitedze".equals(text)) {
			LanguageManager languagemanager = mc.getLanguageManager();
			Language language = languagemanager.getLanguage("en_pt");

			if (languagemanager.getCurrentLanguage().compareTo(language) == 0) {
				return;
			}

			languagemanager.setCurrentLanguage(language);
			mc.gameSettings.language = language.getLanguageCode();
			mc.refreshResources();
			mc.fontRenderer.setUnicodeFlag(mc.getLanguageManager().isCurrentLocaleUnicode() || mc.gameSettings.forceUnicodeFont);
			mc.fontRenderer.setBidiFlag(languagemanager.isCurrentLanguageBidirectional());
			mc.gameSettings.saveOptions();
		}
	}

	private boolean isOffsetNextToMainGUI() {

		return xOffset == 86;
	}

	public void recipesUpdated() {

		updateTabs();

		if (isVisible()) {
			updateCollections(false);
		}
	}

	public void recipesShown(List<IRecipe> recipes) {

		for (IRecipe irecipe : recipes) {
			mc.player.removeRecipeHighlight(irecipe);
		}
	}

	public void setupGhostRecipe(IRecipe p_193951_1_, List<Slot> p_193951_2_) {

		ItemStack itemstack = p_193951_1_.getRecipeOutput();
		ghostRecipe.setRecipe(p_193951_1_);
		ghostRecipe.addIngredient(Ingredient.fromStacks(itemstack), (p_193951_2_.get(0)).xPos, (p_193951_2_.get(0)).yPos);
		int i = craftingSlots.getWidth();
		int j = craftingSlots.getHeight();
		int k = p_193951_1_ instanceof ShapedRecipes ? ((ShapedRecipes) p_193951_1_).getWidth() : i;
		int l = 1;
		Iterator<Ingredient> iterator = p_193951_1_.getIngredients().iterator();

		for (int i1 = 0; i1 < j; ++i1) {
			for (int j1 = 0; j1 < k; ++j1) {
				if (!iterator.hasNext()) {
					return;
				}

				Ingredient ingredient = iterator.next();

				if (ingredient != Ingredient.EMPTY) {
					Slot slot = p_193951_2_.get(l);
					ghostRecipe.addIngredient(ingredient, slot.xPos, slot.yPos);
				}

				++l;
			}

			if (k < i) {
				l += i - k;
			}
		}
	}

	private void sendUpdateSettings() {

		if (mc.getConnection() != null) {
			mc.getConnection().sendPacket(new CPacketRecipeInfo(isVisible(), recipeBook.isFilteringCraftable()));
		}
	}

}
