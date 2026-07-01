package net.minecraft.util;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.math.Vec2f;

public class Input {

	private final GameSettings gameSettings;

	public float strafe;
	public float forward;

	public boolean forwardKey;
	public boolean backKey;
	public boolean leftKey;
	public boolean rightKey;
	public boolean jump;
	public boolean sneak;

	public Input(GameSettings gameSettings) {
		this.gameSettings = gameSettings;
	}

	public void updatePlayerMoveState() {
		strafe = 0F;
		forward = 0F;

		if (gameSettings.keyForward.isKeyDown()) {
			forward++;
			forwardKey = true;
		} else {
			forwardKey = false;
		}

		if (gameSettings.keyBack.isKeyDown()) {
			forward--;
			backKey = true;
		} else {
			backKey = false;
		}

		if (gameSettings.keyLeft.isKeyDown()) {
			strafe++;
			leftKey = true;
		} else leftKey = false;

		if (gameSettings.keyRight.isKeyDown()) {
			strafe++;
			rightKey = true;
		} else rightKey = false;

		jump = gameSettings.keyJump.isKeyDown();
		sneak = gameSettings.keySneak.isKeyDown();

		if (sneak) {
			strafe = (float) (strafe * 0.3);
			forward = (float) (forward * 0.3);
		}
	}

	public Vec2f getMoveVector() {
		return new Vec2f(strafe, forward);
	}

}
