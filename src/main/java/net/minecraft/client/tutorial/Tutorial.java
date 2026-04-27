package net.minecraft.client.tutorial;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentKeybind;
import net.minecraft.world.GameType;

import javax.annotation.Nullable;

public class Tutorial {

	private final Minecraft minecraft;

	@Nullable
	private ITutorialStep tutorialStep;

	public Tutorial(Minecraft minecraft) {

		this.minecraft = minecraft;
	}

	public static ITextComponent createKeybindComponent(String keybind) {

		TextComponentKeybind textcomponentkeybind = new TextComponentKeybind("key." + keybind);
		textcomponentkeybind.getStyle().setBold(true);
		return textcomponentkeybind;
	}

	public void handleMovement(MovementInput p_193293_1_) {

		if (tutorialStep != null) {
			tutorialStep.handleMovement(p_193293_1_);
		}
	}

	public void handleMouse(MouseHelper p_193299_1_) {

		if (tutorialStep != null) {
			tutorialStep.handleMouse(p_193299_1_);
		}
	}

	public void onMouseHover(@Nullable WorldClient worldIn, @Nullable RayTraceResult result) {

		if (tutorialStep != null && result != null && worldIn != null) {
			tutorialStep.onMouseHover(worldIn, result);
		}
	}

	public void onHitBlock(WorldClient worldIn, BlockPos pos, IBlockState state, float diggingStage) {

		if (tutorialStep != null) {
			tutorialStep.onHitBlock(worldIn, pos, state, diggingStage);
		}
	}

	/**
	 * Called when the player opens his inventory
	 */
	public void openInventory() {

		if (tutorialStep != null) {
			tutorialStep.openInventory();
		}
	}

	/**
	 * Called when the player pick up an ItemStack
	 *
	 * @param stack The ItemStack
	 */
	public void handleSetSlot(ItemStack stack) {

		if (tutorialStep != null) {
			tutorialStep.handleSetSlot(stack);
		}
	}

	public void stop() {

		if (tutorialStep != null) {
			tutorialStep.onStop();
			tutorialStep = null;
		}
	}

	/**
	 * Reloads the tutorial step from the game settings
	 */
	public void reload() {

		if (tutorialStep != null) {
			stop();
		}

		tutorialStep = minecraft.gameSettings.tutorialStep.create(this);
	}

	public void update() {

		if (tutorialStep != null) {
			if (minecraft.world != null) {
				tutorialStep.update();
			} else {
				stop();
			}
		} else if (minecraft.world != null) {
			reload();
		}
	}

	/**
	 * Sets a new step to the tutorial
	 */
	public void setStep(TutorialSteps step) {

		minecraft.gameSettings.tutorialStep = step;
		minecraft.gameSettings.saveOptions();

		if (tutorialStep != null) {
			tutorialStep.onStop();
			tutorialStep = step.create(this);
		}
	}

	public Minecraft getMinecraft() {

		return minecraft;
	}

	public GameType getGameType() {

		return minecraft.playerController == null ? GameType.NOT_SET : minecraft.playerController.getCurrentGameType();
	}

}
