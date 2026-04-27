package net.minecraft.client.tutorial;

import net.minecraft.client.gui.toasts.TutorialToast;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;

public class MovementStep implements ITutorialStep {

	private static final ITextComponent MOVE_TITLE = new TextComponentTranslation("tutorial.move.title", Tutorial.createKeybindComponent("forward"), Tutorial.createKeybindComponent("left"), Tutorial.createKeybindComponent("back"), Tutorial.createKeybindComponent("right"));
	private static final ITextComponent MOVE_DESCRIPTION = new TextComponentTranslation("tutorial.move.description", Tutorial.createKeybindComponent("jump"));
	private static final ITextComponent LOOK_TITLE = new TextComponentTranslation("tutorial.look.title");
	private static final ITextComponent LOOK_DESCRIPTION = new TextComponentTranslation("tutorial.look.description");
	private final Tutorial tutorial;
	private TutorialToast moveToast;
	private TutorialToast lookToast;
	private int timeWaiting;
	private int timeMoved;
	private int timeLooked;
	private boolean moved;
	private boolean turned;
	private int moveCompleted = -1;
	private int lookCompleted = -1;

	public MovementStep(Tutorial tutorial) {

		this.tutorial = tutorial;
	}

	public void update() {

		++timeWaiting;

		if (moved) {
			++timeMoved;
			moved = false;
		}

		if (turned) {
			++timeLooked;
			turned = false;
		}

		if (moveCompleted == -1 && timeMoved > 40) {
			if (moveToast != null) {
				moveToast.hide();
				moveToast = null;
			}

			moveCompleted = timeWaiting;
		}

		if (lookCompleted == -1 && timeLooked > 40) {
			if (lookToast != null) {
				lookToast.hide();
				lookToast = null;
			}

			lookCompleted = timeWaiting;
		}

		if (moveCompleted != -1 && lookCompleted != -1) {
			if (tutorial.getGameType() == GameType.SURVIVAL) {
				tutorial.setStep(TutorialSteps.FIND_TREE);
			} else {
				tutorial.setStep(TutorialSteps.NONE);
			}
		}

		if (moveToast != null) {
			moveToast.setProgress((float) timeMoved / 40.0F);
		}

		if (lookToast != null) {
			lookToast.setProgress((float) timeLooked / 40.0F);
		}

		if (timeWaiting >= 100) {
			if (moveCompleted == -1 && moveToast == null) {
				moveToast = new TutorialToast(TutorialToast.Icons.MOVEMENT_KEYS, MOVE_TITLE, MOVE_DESCRIPTION, true);
				tutorial.getMinecraft().getToastGui().add(moveToast);
			} else if (moveCompleted != -1 && timeWaiting - moveCompleted >= 20 && lookCompleted == -1 && lookToast == null) {
				lookToast = new TutorialToast(TutorialToast.Icons.MOUSE, LOOK_TITLE, LOOK_DESCRIPTION, true);
				tutorial.getMinecraft().getToastGui().add(lookToast);
			}
		}
	}

	public void onStop() {

		if (moveToast != null) {
			moveToast.hide();
			moveToast = null;
		}

		if (lookToast != null) {
			lookToast.hide();
			lookToast = null;
		}
	}

	/**
	 * Handles the player movement
	 *
	 * @param input The movement inputs of the player
	 */
	public void handleMovement(MovementInput input) {

		if (input.forwardKeyDown || input.backKeyDown || input.leftKeyDown || input.rightKeyDown || input.jump) {
			moved = true;
		}
	}

	/**
	 * Handles mouse mouvement
	 *
	 * @param mouseHelperIn A MouseHelper providing you informations about the player mouse
	 */
	public void handleMouse(MouseHelper mouseHelperIn) {

		if ((double) MathHelper.abs(mouseHelperIn.deltaX) > 0.01D || (double) MathHelper.abs(mouseHelperIn.deltaY) > 0.01D) {
			turned = true;
		}
	}

}
