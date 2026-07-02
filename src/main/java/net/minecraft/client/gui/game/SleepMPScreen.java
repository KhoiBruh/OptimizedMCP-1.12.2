package net.minecraft.client.gui.game;

import net.minecraft.client.gui.chat.ChatScreen;
import net.minecraft.client.gui.component.Button;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.play.client.CPacketEntityAction;

import java.io.IOException;

public class SleepMPScreen extends ChatScreen {

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {
		super.init();
		buttons.add(new Button(1, width / 2 - 100, height - 40, I18n.format("multiplayer.stopSleeping")));
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 256) {
			wakeFromSleep();
		} else if (keyCode != 257 && keyCode != 335) {
			super.keyTyped(typedChar, keyCode);
		} else {
			String s = inputField.getText().trim();

			if (!s.isEmpty()) {
				mc.player.sendChatMessage(s);
			}

			inputField.setText("");
			mc.ingameGUI.getChatGUI().resetScroll();
		}
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void action(Button button) throws IOException {
		if (button.id == 1) {
			wakeFromSleep();
		} else {
			super.action(button);
		}
	}

	private void wakeFromSleep() {
		NetHandlerPlayClient nethandlerplayclient = mc.player.connection;
		nethandlerplayclient.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SLEEPING));
	}

}
