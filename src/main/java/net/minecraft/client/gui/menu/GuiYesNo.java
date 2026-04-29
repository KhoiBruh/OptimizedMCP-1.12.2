package net.minecraft.client.gui.menu;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.component.GuiButton;
import net.minecraft.client.gui.component.GuiOptionButton;
import net.minecraft.client.resources.I18n;

import java.util.List;

public class GuiYesNo extends GuiScreen {

	private final String messageLine2;
	private final List<String> listLines = Lists.newArrayList();
	/**
	 * A reference to the screen object that created this. Used for navigating between screens.
	 */
	protected GuiYesNoCallback parentScreen;
	protected String messageLine1;
	/**
	 * The text shown for the first button in GuiYesNo
	 */
	protected String confirmButtonText;

	/**
	 * The text shown for the second button in GuiYesNo
	 */
	protected String cancelButtonText;
	protected int parentButtonClickedId;
	private int ticksUntilEnable;

	public GuiYesNo(GuiYesNoCallback parentScreenIn, String messageLine1In, String messageLine2In, int parentButtonClickedIdIn) {

		parentScreen = parentScreenIn;
		messageLine1 = messageLine1In;
		messageLine2 = messageLine2In;
		parentButtonClickedId = parentButtonClickedIdIn;
		confirmButtonText = I18n.format("gui.yes");
		cancelButtonText = I18n.format("gui.no");
	}

	public GuiYesNo(GuiYesNoCallback parentScreenIn, String messageLine1In, String messageLine2In, String confirmButtonTextIn, String cancelButtonTextIn, int parentButtonClickedIdIn) {

		parentScreen = parentScreenIn;
		messageLine1 = messageLine1In;
		messageLine2 = messageLine2In;
		confirmButtonText = confirmButtonTextIn;
		cancelButtonText = cancelButtonTextIn;
		parentButtonClickedId = parentButtonClickedIdIn;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

		buttonList.add(new GuiOptionButton(0, width / 2 - 155, height / 6 + 96, confirmButtonText));
		buttonList.add(new GuiOptionButton(1, width / 2 - 155 + 160, height / 6 + 96, cancelButtonText));
		listLines.clear();
		listLines.addAll(fontRenderer.listFormattedStringToWidth(messageLine2, width - 50));
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void actionPerformed(GuiButton button) {

		parentScreen.confirmClicked(button.id == 0, parentButtonClickedId);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		drawCenteredString(fontRenderer, messageLine1, width / 2, 70, 16777215);
		int i = 90;

		for (String s : listLines) {
			drawCenteredString(fontRenderer, s, width / 2, i, 16777215);
			i += fontRenderer.FONT_HEIGHT;
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	/**
	 * Sets the number of ticks to wait before enabling the buttons.
	 */
	public void setButtonDelay(int ticksUntilEnableIn) {

		ticksUntilEnable = ticksUntilEnableIn;

		for (GuiButton guibutton : buttonList) {
			guibutton.enabled = false;
		}
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {

		super.updateScreen();

		if (--ticksUntilEnable == 0) {
			for (GuiButton guibutton : buttonList) {
				guibutton.enabled = true;
			}
		}
	}

}
