package net.minecraft.client.tutorial;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.toasts.TutorialToast;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;

public class CraftPlanksStep implements ITutorialStep {

	private static final ITextComponent TITLE = new TextComponentTranslation("tutorial.craft_planks.title");
	private static final ITextComponent DESCRIPTION = new TextComponentTranslation("tutorial.craft_planks.description");
	private final Tutorial tutorial;
	private TutorialToast toast;
	private int timeWaiting;

	public CraftPlanksStep(Tutorial tutorial) {

		this.tutorial = tutorial;
	}

	/**
	 * Indicates if the players crafted at least one time planks.
	 *
	 * @param player The player
	 */
	public static boolean didPlayerCraftedPlanks(EntityPlayerSP player) {

		StatBase statbase = StatList.getCraftStats(Item.getItemFromBlock(Blocks.PLANKS));
		return statbase != null && player.getStatFileWriter().readStat(statbase) > 0;
	}

	public void update() {

		++timeWaiting;

		if (tutorial.getGameType() != GameType.SURVIVAL) {
			tutorial.setStep(TutorialSteps.NONE);
		} else {
			if (timeWaiting == 1) {
				EntityPlayerSP entityplayersp = tutorial.getMinecraft().player;

				if (entityplayersp != null) {
					if (entityplayersp.inventory.hasItemStack(new ItemStack(Blocks.PLANKS))) {
						tutorial.setStep(TutorialSteps.NONE);
						return;
					}

					if (didPlayerCraftedPlanks(entityplayersp)) {
						tutorial.setStep(TutorialSteps.NONE);
						return;
					}
				}
			}

			if (timeWaiting >= 1200 && toast == null) {
				toast = new TutorialToast(TutorialToast.Icons.WOODEN_PLANKS, TITLE, DESCRIPTION, false);
				tutorial.getMinecraft().getToastGui().add(toast);
			}
		}
	}

	public void onStop() {

		if (toast != null) {
			toast.hide();
			toast = null;
		}
	}

	/**
	 * Called when the player pick up an ItemStack
	 *
	 * @param stack The ItemStack
	 */
	public void handleSetSlot(ItemStack stack) {

		if (stack.getItem() == Item.getItemFromBlock(Blocks.PLANKS)) {
			tutorial.setStep(TutorialSteps.NONE);
		}
	}

}
