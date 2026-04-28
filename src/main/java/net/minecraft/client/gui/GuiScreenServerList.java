package net.minecraft.client.gui;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiScreenServerList extends GuiScreen {

	private final GuiScreen lastScreen;
	private final ServerData serverData;
	private GuiTextField ipEdit;

	public GuiScreenServerList(GuiScreen lastScreenIn, ServerData serverDataIn) {

		lastScreen = lastScreenIn;
		serverData = serverDataIn;
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {

		ipEdit.updateCursorCounter();
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

		Keyboard.enableRepeatEvents(true);
		buttonList.clear();
		buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 96 + 12, I18n.format("selectServer.select")));
		buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 120 + 12, I18n.format("gui.cancel")));
		ipEdit = new GuiTextField(2, fontRenderer, width / 2 - 100, 116, 200, 20);
		ipEdit.setMaxStringLength(128);
		ipEdit.setFocused(true);
		ipEdit.setText(mc.gameSettings.lastServer);
		(buttonList.getFirst()).enabled = !ipEdit.getText().isEmpty() && ipEdit.getText().split(":").length > 0;
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void onGuiClosed() {

		Keyboard.enableRepeatEvents(false);
		mc.gameSettings.lastServer = ipEdit.getText();
		mc.gameSettings.saveOptions();
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void actionPerformed(GuiButton button) {

		if (button.enabled) {
			if (button.id == 1) {
				lastScreen.confirmClicked(false, 0);
			} else if (button.id == 0) {
				serverData.serverIP = ipEdit.getText();
				lastScreen.confirmClicked(true, 0);
			}
		}
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) {

		if (ipEdit.textboxKeyTyped(typedChar, keyCode)) {
			(buttonList.getFirst()).enabled = !ipEdit.getText().isEmpty() && ipEdit.getText().split(":").length > 0;
		} else if (keyCode == 28 || keyCode == 156) {
			actionPerformed(buttonList.getFirst());
		}
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

		super.mouseClicked(mouseX, mouseY, mouseButton);
		ipEdit.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		drawCenteredString(fontRenderer, I18n.format("selectServer.direct"), width / 2, 20, 16777215);
		drawString(fontRenderer, I18n.format("addServer.enterIp"), width / 2 - 100, 100, 10526880);
		ipEdit.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

}
