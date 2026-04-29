package net.minecraft.client.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class GuiLockIconButton extends GuiButton {

	private boolean locked;

	public GuiLockIconButton(int buttonId, int x, int y) {

		super(buttonId, x, y, 20, 20, "");
	}

	public boolean isLocked() {

		return locked;
	}

	public void setLocked(boolean lockedIn) {

		locked = lockedIn;
	}

	/**
	 * Draws this button to the screen.
	 */
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {

		if (visible) {
			mc.getTextureManager().bindTexture(GuiButton.BUTTON_TEXTURES);
			GlStateManager.color(1F, 1F, 1F, 1F);
			boolean flag = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			GuiLockIconButton.Icon guilockiconbutton$icon;

			if (locked) {
				if (!enabled) {
					guilockiconbutton$icon = GuiLockIconButton.Icon.LOCKED_DISABLED;
				} else if (flag) {
					guilockiconbutton$icon = GuiLockIconButton.Icon.LOCKED_HOVER;
				} else {
					guilockiconbutton$icon = GuiLockIconButton.Icon.LOCKED;
				}
			} else if (!enabled) {
				guilockiconbutton$icon = GuiLockIconButton.Icon.UNLOCKED_DISABLED;
			} else if (flag) {
				guilockiconbutton$icon = GuiLockIconButton.Icon.UNLOCKED_HOVER;
			} else {
				guilockiconbutton$icon = GuiLockIconButton.Icon.UNLOCKED;
			}

			drawTexturedModalRect(x, y, guilockiconbutton$icon.getX(), guilockiconbutton$icon.getY(), width, height);
		}
	}

	enum Icon {
		LOCKED(0, 146),
		LOCKED_HOVER(0, 166),
		LOCKED_DISABLED(0, 186),
		UNLOCKED(20, 146),
		UNLOCKED_HOVER(20, 166),
		UNLOCKED_DISABLED(20, 186);

		private final int x;
		private final int y;

		Icon(int xIn, int yIn) {

			x = xIn;
			y = yIn;
		}

		public int getX() {

			return x;
		}

		public int getY() {

			return y;
		}
	}

}
