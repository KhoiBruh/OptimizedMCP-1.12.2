package net.minecraft.client.gui.loading;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IProgressUpdate;

public class GuiScreenWorking extends GuiScreen implements IProgressUpdate {

	private String title = "";
	private String stage = "";
	private int progress;
	private boolean doneWorking;

	/**
	 * Shows the 'Saving level' string.
	 */
	public void displaySavingString(String message) {

		resetProgressAndMessage(message);
	}

	/**
	 * this string, followed by "working..." and then the "% complete" are the 3 lines shown. This resets progress to 0,
	 * and the WorkingString to "working...".
	 */
	public void resetProgressAndMessage(String message) {

		title = message;
		displayLoadingString("Working...");
	}

	/**
	 * Displays a string on the loading screen supposed to indicate what is being done currently.
	 */
	public void displayLoadingString(String message) {

		stage = message;
		setLoadingProgress(0);
	}

	/**
	 * Updates the progress bar on the loading screen to the specified amount.
	 */
	public void setLoadingProgress(int progress) {

		this.progress = progress;
	}

	public void setDoneWorking() {

		doneWorking = true;
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		if (doneWorking) {
			mc.displayGuiScreen(null);
		} else {
			drawDefaultBackground();
			drawCenteredString(fontRenderer, title, width / 2, 70, 16777215);
			drawCenteredString(fontRenderer, stage + " " + progress + "%", width / 2, 90, 16777215);
			super.drawScreen(mouseX, mouseY, partialTicks);
		}
	}

}
