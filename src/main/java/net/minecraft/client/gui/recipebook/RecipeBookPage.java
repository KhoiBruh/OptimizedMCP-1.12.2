package net.minecraft.client.gui.recipebook;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButtonToggle;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.RecipeBook;

public class RecipeBookPage
{
    private List<GuiButtonRecipe> buttons = Lists.<GuiButtonRecipe>newArrayListWithCapacity(20);
    private GuiButtonRecipe hoveredButton;
    private GuiRecipeOverlay overlay = new GuiRecipeOverlay();
    private Minecraft minecraft;
    private List<IRecipeUpdateListener> listeners = Lists.<IRecipeUpdateListener>newArrayList();
    private List<RecipeList> recipeLists;
    private GuiButtonToggle forwardButton;
    private GuiButtonToggle backButton;
    private int totalPages;
    private int currentPage;
    private RecipeBook recipeBook;
    private IRecipe lastClickedRecipe;
    private RecipeList lastClickedRecipeList;

    public RecipeBookPage()
    {
        for (int i = 0; i < 20; ++i)
        {
            buttons.add(new GuiButtonRecipe());
        }
    }

    public void init(Minecraft p_194194_1_, int p_194194_2_, int p_194194_3_)
    {
        minecraft = p_194194_1_;
        recipeBook = p_194194_1_.player.getRecipeBook();

        for (int i = 0; i < buttons.size(); ++i)
        {
            ((GuiButtonRecipe) buttons.get(i)).setPosition(p_194194_2_ + 11 + 25 * (i % 5), p_194194_3_ + 31 + 25 * (i / 5));
        }

        forwardButton = new GuiButtonToggle(0, p_194194_2_ + 93, p_194194_3_ + 137, 12, 17, false);
        forwardButton.initTextureValues(1, 208, 13, 18, GuiRecipeBook.RECIPE_BOOK);
        backButton = new GuiButtonToggle(0, p_194194_2_ + 38, p_194194_3_ + 137, 12, 17, true);
        backButton.initTextureValues(1, 208, 13, 18, GuiRecipeBook.RECIPE_BOOK);
    }

    public void addListener(GuiRecipeBook p_193732_1_)
    {
        listeners.remove(p_193732_1_);
        listeners.add(p_193732_1_);
    }

    public void updateLists(List<RecipeList> p_194192_1_, boolean p_194192_2_)
    {
        recipeLists = p_194192_1_;
        totalPages = (int)Math.ceil((double)p_194192_1_.size() / 20.0D);

        if (totalPages <= currentPage || p_194192_2_)
        {
            currentPage = 0;
        }

        updateButtonsForPage();
    }

    private void updateButtonsForPage()
    {
        int i = 20 * currentPage;

        for (int j = 0; j < buttons.size(); ++j)
        {
            GuiButtonRecipe guibuttonrecipe = buttons.get(j);

            if (i + j < recipeLists.size())
            {
                RecipeList recipelist = recipeLists.get(i + j);
                guibuttonrecipe.init(recipelist, this, recipeBook);
                guibuttonrecipe.visible = true;
            }
            else
            {
                guibuttonrecipe.visible = false;
            }
        }

        updateArrowButtons();
    }

    private void updateArrowButtons()
    {
        forwardButton.visible = totalPages > 1 && currentPage < totalPages - 1;
        backButton.visible = totalPages > 1 && currentPage > 0;
    }

    public void render(int p_194191_1_, int p_194191_2_, int p_194191_3_, int p_194191_4_, float p_194191_5_)
    {
        if (totalPages > 1)
        {
            String s = currentPage + 1 + "/" + totalPages;
            int i = minecraft.fontRenderer.getStringWidth(s);
            minecraft.fontRenderer.drawString(s, p_194191_1_ - i / 2 + 73, p_194191_2_ + 141, -1);
        }

        RenderHelper.disableStandardItemLighting();
        hoveredButton = null;

        for (GuiButtonRecipe guibuttonrecipe : buttons)
        {
            guibuttonrecipe.drawButton(minecraft, p_194191_3_, p_194191_4_, p_194191_5_);

            if (guibuttonrecipe.visible && guibuttonrecipe.isMouseOver())
            {
                hoveredButton = guibuttonrecipe;
            }
        }

        backButton.drawButton(minecraft, p_194191_3_, p_194191_4_, p_194191_5_);
        forwardButton.drawButton(minecraft, p_194191_3_, p_194191_4_, p_194191_5_);
        overlay.render(p_194191_3_, p_194191_4_, p_194191_5_);
    }

    public void renderTooltip(int p_193721_1_, int p_193721_2_)
    {
        if (minecraft.currentScreen != null && hoveredButton != null && !overlay.isVisible())
        {
            minecraft.currentScreen.drawHoveringText(hoveredButton.getToolTipText(minecraft.currentScreen), p_193721_1_, p_193721_2_);
        }
    }

    @Nullable
    public IRecipe getLastClickedRecipe()
    {
        return lastClickedRecipe;
    }

    @Nullable
    public RecipeList getLastClickedRecipeList()
    {
        return lastClickedRecipeList;
    }

    public void setInvisible()
    {
        overlay.setVisible(false);
    }

    public boolean mouseClicked(int p_194196_1_, int p_194196_2_, int p_194196_3_, int p_194196_4_, int p_194196_5_, int p_194196_6_, int p_194196_7_)
    {
        lastClickedRecipe = null;
        lastClickedRecipeList = null;

        if (overlay.isVisible())
        {
            if (overlay.buttonClicked(p_194196_1_, p_194196_2_, p_194196_3_))
            {
                lastClickedRecipe = overlay.getLastRecipeClicked();
                lastClickedRecipeList = overlay.getRecipeList();
            }
            else
            {
                overlay.setVisible(false);
            }

            return true;
        }
        else if (forwardButton.mousePressed(minecraft, p_194196_1_, p_194196_2_) && p_194196_3_ == 0)
        {
            forwardButton.playPressSound(minecraft.getSoundHandler());
            ++currentPage;
            updateButtonsForPage();
            return true;
        }
        else if (backButton.mousePressed(minecraft, p_194196_1_, p_194196_2_) && p_194196_3_ == 0)
        {
            backButton.playPressSound(minecraft.getSoundHandler());
            --currentPage;
            updateButtonsForPage();
            return true;
        }
        else
        {
            for (GuiButtonRecipe guibuttonrecipe : buttons)
            {
                if (guibuttonrecipe.mousePressed(minecraft, p_194196_1_, p_194196_2_))
                {
                    guibuttonrecipe.playPressSound(minecraft.getSoundHandler());

                    if (p_194196_3_ == 0)
                    {
                        lastClickedRecipe = guibuttonrecipe.getRecipe();
                        lastClickedRecipeList = guibuttonrecipe.getList();
                    }
                    else if (!overlay.isVisible() && !guibuttonrecipe.isOnlyOption())
                    {
                        overlay.init(minecraft, guibuttonrecipe.getList(), guibuttonrecipe.x, guibuttonrecipe.y, p_194196_4_ + p_194196_6_ / 2, p_194196_5_ + 13 + p_194196_7_ / 2, (float)guibuttonrecipe.getButtonWidth(), recipeBook);
                    }

                    return true;
                }
            }

            return false;
        }
    }

    public void recipesShown(List<IRecipe> p_194195_1_)
    {
        for (IRecipeUpdateListener irecipeupdatelistener : listeners)
        {
            irecipeupdatelistener.recipesShown(p_194195_1_);
        }
    }
}
