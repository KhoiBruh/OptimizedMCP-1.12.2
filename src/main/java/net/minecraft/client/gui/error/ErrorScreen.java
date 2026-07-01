package net.minecraft.client.gui.error;

import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.component.Button;
import net.minecraft.client.resources.I18n;

public class ErrorScreen extends Screen {

	private final String title;
	private final String message;

	public ErrorScreen(String titleIn, String messageIn) {

		title = titleIn;
		message = messageIn;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {

		super.init();
		buttons.add(new Button(0, width / 2 - 100, 140, I18n.format("gui.cancel")));
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(int mouseX, int mouseY, float partialTicks) {

		drawGradientRect(0, 0, width, height, -12574688, -11530224);
		drawCenteredString(fontRenderer, title, width / 2, 90, 16777215);
		drawCenteredString(fontRenderer, message, width / 2, 110, 16777215);
		super.draw(mouseX, mouseY, partialTicks);
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) {

	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void action(Button button) {

		mc.displayScreen(null);
	}

}
