package net.minecraft.client.gui.option;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.component.Button;
import net.minecraft.client.gui.component.OptionButton;
import net.minecraft.client.gui.component.OptionSlider;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

import java.io.IOException;

public class ControlsScreen extends Screen {

	private static final GameSettings.Options[] OPTIONS_ARR = new GameSettings.Options[]{GameSettings.Options.INVERT_MOUSE, GameSettings.Options.SENSITIVITY, GameSettings.Options.TOUCHSCREEN, GameSettings.Options.AUTO_JUMP};

	/**
	 * A reference to the screen object that created this. Used for navigating between screens.
	 */
	private final Screen parentScreen;
	/**
	 * Reference to the GameSettings object.
	 */
	private final GameSettings options;
	/**
	 * The ID of the button that has been pressed.
	 */
	public KeyBinding buttonId;
	public long time;
	protected String screenTitle = "Controls";
	private GuiKeyBindingList keyBindingList;
	private Button buttonReset;

	public ControlsScreen(Screen screen, GameSettings settings) {

		parentScreen = screen;
		options = settings;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {

		keyBindingList = new GuiKeyBindingList(this, mc);
		buttons.add(new Button(200, width / 2 - 155 + 160, height - 29, 150, 20, I18n.format("gui.done")));
		buttonReset = addButton(new Button(201, width / 2 - 155, height - 29, 150, 20, I18n.format("controls.resetAll")));
		screenTitle = I18n.format("controls.title");
		int i = 0;

		for (GameSettings.Options gamesettings$options : OPTIONS_ARR) {
			if (gamesettings$options.isFloat()) {
				buttons.add(new OptionSlider(gamesettings$options.ordinal(), width / 2 - 155 + i % 2 * 160, 18 + 24 * (i >> 1), gamesettings$options));
			} else {
				buttons.add(new OptionButton(gamesettings$options.ordinal(), width / 2 - 155 + i % 2 * 160, 18 + 24 * (i >> 1), gamesettings$options, options.getKeyBinding(gamesettings$options)));
			}

			++i;
		}
	}

	/**
	 * Handles mouse input.
	 */
	public void handleMouse() throws IOException {

		super.handleMouse();
		keyBindingList.handleMouseInput();
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void action(Button button) {

		if (button.id == 200) {
			mc.displayScreen(parentScreen);
		} else if (button.id == 201) {
			for (KeyBinding keybinding : mc.gameSettings.keyBindings) {
				keybinding.setKeyCode(keybinding.getDefaultKeyCode());
			}

			KeyBinding.resetKeyBindingArrayAndHash();
		} else if (button.id < 100 && button instanceof OptionButton) {
			options.setOptionValue(((OptionButton) button).getOption(), 1);
			button.displayString = options.getKeyBinding(GameSettings.Options.byOrdinal(button.id));
		}
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouse) throws IOException {

		if (buttonId != null) {
			options.setOptionKeyBinding(buttonId, -100 + mouse);
			buttonId = null;
			KeyBinding.resetKeyBindingArrayAndHash();
		} else if (mouse != 0 || !keyBindingList.mouseClicked(mouseX, mouseY, mouse)) {
			super.mouseClicked(mouseX, mouseY, mouse);
		}
	}

	/**
	 * Called when a mouse button is released.
	 */
	protected void mouseReleased(int mouseX, int mouseY, int state) {

		if (state != 0 || !keyBindingList.mouseReleased(mouseX, mouseY, state)) {
			super.mouseReleased(mouseX, mouseY, state);
		}
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) throws IOException {

		if (buttonId != null) {
			if (keyCode == 256) {
				options.setOptionKeyBinding(buttonId, 0);
			} else if (keyCode != 0) {
				options.setOptionKeyBinding(buttonId, keyCode);
			} else if (typedChar > 0) {
				options.setOptionKeyBinding(buttonId, typedChar + 256);
			}

			buttonId = null;
			time = Minecraft.getSystemTime();
			KeyBinding.resetKeyBindingArrayAndHash();
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		keyBindingList.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, screenTitle, width / 2, 8, 16777215);
		boolean flag = false;

		for (KeyBinding keybinding : options.keyBindings) {
			if (keybinding.getKeyCode() != keybinding.getDefaultKeyCode()) {
				flag = true;
				break;
			}
		}

		buttonReset.enabled = flag;
		super.draw(mouseX, mouseY, partialTicks);
	}

}
