package net.minecraft.client.gui.loading;

import net.minecraft.client.gui.Screen;
import net.minecraft.client.resources.I18n;

public class DownloadTerrainScreen extends Screen {

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {
		buttons.clear();
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, I18n.format("multiplayer.downloadingTerrain"), width / 2, height / 2 - 50, 16777215);
		super.draw(mouseX, mouseY, partialTicks);
	}

	/**
	 * Returns true if this GUI should pause the game when it is displayed in single-player
	 */
	public boolean pauseGame() {
		return false;
	}

}
