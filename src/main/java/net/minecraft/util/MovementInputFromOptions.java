package net.minecraft.util;

import net.minecraft.client.settings.GameSettings;

public class MovementInputFromOptions extends MovementInput {

	private final GameSettings gameSettings;

	public MovementInputFromOptions(GameSettings gameSettingsIn) {

		gameSettings = gameSettingsIn;
	}

	public void updatePlayerMoveState() {

		moveStrafe = 0F;
		moveForward = 0F;

		if (gameSettings.keyForward.isKeyDown()) {
			++moveForward;
			forwardKeyDown = true;
		} else {
			forwardKeyDown = false;
		}

		if (gameSettings.keyBack.isKeyDown()) {
			--moveForward;
			backKeyDown = true;
		} else {
			backKeyDown = false;
		}

		if (gameSettings.keyLeft.isKeyDown()) {
			++moveStrafe;
			leftKeyDown = true;
		} else {
			leftKeyDown = false;
		}

		if (gameSettings.keyRight.isKeyDown()) {
			--moveStrafe;
			rightKeyDown = true;
		} else {
			rightKeyDown = false;
		}

		jump = gameSettings.keyJump.isKeyDown();
		sneak = gameSettings.keySneak.isKeyDown();

		if (sneak) {
			moveStrafe = (float) ((double) moveStrafe * 0.3D);
			moveForward = (float) ((double) moveForward * 0.3D);
		}
	}

}
