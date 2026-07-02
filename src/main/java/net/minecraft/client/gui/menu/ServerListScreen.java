package net.minecraft.client.gui.menu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.component.Button;
import net.minecraft.client.gui.component.GuiTextField;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.Keyboard;

import java.io.IOException;

public class ServerListScreen extends Screen {

	private final Screen lastScreen;
	private final ServerData serverData;
	private GuiTextField ipEdit;

	public ServerListScreen(Screen lastScreenIn, ServerData serverDataIn) {
		lastScreen = lastScreenIn;
		serverData = serverDataIn;
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void update() {
		ipEdit.updateCursorCounter();
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {
		Keyboard.setRepeat(true);
		buttons.clear();
		buttons.add(new Button(0, width / 2 - 100, height / 4 + 96 + 12, I18n.format("selectServer.select")));
		buttons.add(new Button(1, width / 2 - 100, height / 4 + 120 + 12, I18n.format("gui.cancel")));
		ipEdit = new GuiTextField(2, fontRenderer, width / 2 - 100, 116, 200, 20);
		ipEdit.setMaxStringLength(128);
		ipEdit.setFocused(true);
		ipEdit.setText(mc.gameSettings.lastServer);
		(buttons.getFirst()).enabled = !ipEdit.getText().isEmpty() && ipEdit.getText().split(":").length > 0;
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void close() {
		Keyboard.setRepeat(false);
		mc.gameSettings.lastServer = ipEdit.getText();
		mc.gameSettings.saveOptions();
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void action(Button button) {
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
			(buttons.getFirst()).enabled = !ipEdit.getText().isEmpty() && ipEdit.getText().split(":").length > 0;
		} else if (keyCode == 257 || keyCode == 335) {
			action(buttons.getFirst());
		}
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouse) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouse);
		ipEdit.mouseClicked(mouseX, mouseY, mouse);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(DrawContext context, int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, I18n.format("selectServer.direct"), width / 2, 20, 16777215);
		drawString(fontRenderer, I18n.format("addServer.enterIp"), width / 2 - 100, 100, 10526880);
		ipEdit.drawTextBox();
		super.draw(context, mouseX, mouseY, partialTicks);
	}

}
