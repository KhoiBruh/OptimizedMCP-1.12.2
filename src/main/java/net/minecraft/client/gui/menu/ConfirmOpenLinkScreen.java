package net.minecraft.client.gui.menu;

import net.minecraft.client.gui.component.Button;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;

public class ConfirmOpenLinkScreen extends YesNoScreen {

	/**
	 * Text to warn players from opening unsafe links.
	 */
	private final String openLinkWarning;

	/**
	 * Label for the Copy to Clipboard button.
	 */
	private final String copyLinkButtonText;
	private final String linkText;
	private boolean showSecurityWarning = true;

	public ConfirmOpenLinkScreen(GuiYesNoCallback parentScreenIn, String linkTextIn, int parentButtonClickedIdIn, boolean trusted) {

		super(parentScreenIn, I18n.format(trusted ? "chat.link.confirmTrusted" : "chat.link.confirm"), linkTextIn, parentButtonClickedIdIn);
		confirmButtonText = I18n.format(trusted ? "chat.link.open" : "gui.yes");
		cancelButtonText = I18n.format(trusted ? "gui.cancel" : "gui.no");
		copyLinkButtonText = I18n.format("chat.copy");
		openLinkWarning = I18n.format("chat.link.warning");
		linkText = linkTextIn;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {

		super.init();
		buttons.clear();
		buttons.add(new Button(0, width / 2 - 50 - 105, height / 6 + 96, 100, 20, confirmButtonText));
		buttons.add(new Button(2, width / 2 - 50, height / 6 + 96, 100, 20, copyLinkButtonText));
		buttons.add(new Button(1, width / 2 - 50 + 105, height / 6 + 96, 100, 20, cancelButtonText));
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void action(Button button) {

		if (button.id == 2) {
			copyLinkToClipboard();
		}

		parentScreen.confirmClicked(button.id == 0, parentButtonClickedId);
	}

	/**
	 * Copies the link to the system clipboard.
	 */
	public void copyLinkToClipboard() {

		setClipboard(linkText);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(int mouseX, int mouseY, float partialTicks) {

		super.draw(mouseX, mouseY, partialTicks);

		if (showSecurityWarning) {
			drawCenteredString(fontRenderer, openLinkWarning, width / 2, 110, 16764108);
		}
	}

	public void disableSecurityWarning() {

		showSecurityWarning = false;
	}

}
