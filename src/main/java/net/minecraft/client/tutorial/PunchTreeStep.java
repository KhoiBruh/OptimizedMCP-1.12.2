package net.minecraft.client.tutorial;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.toasts.TutorialToast;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;

public class PunchTreeStep implements ITutorialStep
{
    private static final Set<Block> LOG_BLOCKS = Sets.newHashSet(Blocks.LOG, Blocks.LOG2);
    private static final ITextComponent TITLE = new TextComponentTranslation("tutorial.punch_tree.title", new Object[0]);
    private static final ITextComponent DESCRIPTION = new TextComponentTranslation("tutorial.punch_tree.description", new Object[] {Tutorial.createKeybindComponent("attack")});
    private final Tutorial tutorial;
    private TutorialToast toast;
    private int timeWaiting;
    private int resetCount;

    public PunchTreeStep(Tutorial tutorial)
    {
        this.tutorial = tutorial;
    }

    public void update()
    {
        ++timeWaiting;

        if (tutorial.getGameType() != GameType.SURVIVAL)
        {
            tutorial.setStep(TutorialSteps.NONE);
        }
        else
        {
            if (timeWaiting == 1)
            {
                EntityPlayerSP entityplayersp = tutorial.getMinecraft().player;

                if (entityplayersp != null)
                {
                    for (Block block : LOG_BLOCKS)
                    {
                        if (entityplayersp.inventory.hasItemStack(new ItemStack(block)))
                        {
                            tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
                            return;
                        }
                    }

                    if (FindTreeStep.hasPunchedTreesPreviously(entityplayersp))
                    {
                        tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
                        return;
                    }
                }
            }

            if ((timeWaiting >= 600 || resetCount > 3) && toast == null)
            {
                toast = new TutorialToast(TutorialToast.Icons.TREE, TITLE, DESCRIPTION, true);
                tutorial.getMinecraft().getToastGui().add(toast);
            }
        }
    }

    public void onStop()
    {
        if (toast != null)
        {
            toast.hide();
            toast = null;
        }
    }

    /**
     * Called when a player hits block to destroy it.
     *  
     * @param worldIn The world the player is in
     * @param pos The block position
     * @param state The block state
     * @param diggingStage The amount of digging, 1.0 means the block is totally digged, -1.0 means the player stopped
     */
    public void onHitBlock(WorldClient worldIn, BlockPos pos, IBlockState state, float diggingStage)
    {
        boolean flag = LOG_BLOCKS.contains(state.getBlock());

        if (flag && diggingStage > 0.0F)
        {
            if (toast != null)
            {
                toast.setProgress(diggingStage);
            }

            if (diggingStage >= 1.0F)
            {
                tutorial.setStep(TutorialSteps.OPEN_INVENTORY);
            }
        }
        else if (toast != null)
        {
            toast.setProgress(0.0F);
        }
        else if (flag)
        {
            ++resetCount;
        }
    }

    /**
     * Called when the player pick up an ItemStack
     *  
     * @param stack The ItemStack
     */
    public void handleSetSlot(ItemStack stack)
    {
        for (Block block : LOG_BLOCKS)
        {
            if (stack.getItem() == Item.getItemFromBlock(block))
            {
                tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
                return;
            }
        }
    }
}
