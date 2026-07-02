package net.minecraft.client.gui.option;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.component.Button;
import net.minecraft.client.gui.component.OptionButton;
import net.minecraft.client.gui.component.OptionSlider;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

import java.io.IOException;

public class ScreenChatOptions extends Screen {

	private static final GameSettings.Options[] CHAT_OPTIONS = new GameSettings.Options[]{GameSettings.Options.CHAT_VISIBILITY, GameSettings.Options.CHAT_COLOR, GameSettings.Options.CHAT_LINKS, GameSettings.Options.CHAT_OPACITY, GameSettings.Options.CHAT_LINKS_PROMPT, GameSettings.Options.CHAT_SCALE, GameSettings.Options.CHAT_HEIGHT_FOCUSED, GameSettings.Options.CHAT_HEIGHT_UNFOCUSED, GameSettings.Options.CHAT_WIDTH, GameSettings.Options.REDUCED_DEBUG_INFO};
	private final Screen parentScreen;
	private final GameSettings game_settings;
	private String chatTitle;

	public ScreenChatOptions(Screen parentScreenIn, GameSettings gameSettingsIn) {
		parentScreen = parentScreenIn;
		game_settings = gameSettingsIn;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {
		chatTitle = I18n.format("options.chat.title");
		int i = 0;

		for (GameSettings.Options gamesettings$options : CHAT_OPTIONS) {
			if (gamesettings$options.isFloat()) {
				buttons.add(new OptionSlider(gamesettings$options.ordinal(), width / 2 - 155 + i % 2 * 160, height / 6 + 24 * (i >> 1), gamesettings$options));
			} else {
				OptionButton guioptionbutton = new OptionButton(gamesettings$options.ordinal(), width / 2 - 155 + i % 2 * 160, height / 6 + 24 * (i >> 1), gamesettings$options, game_settings.getKeyBinding(gamesettings$options));
				buttons.add(guioptionbutton);
			}

			++i;
		}

		buttons.add(new Button(200, width / 2 - 100, height / 6 + 144, I18n.format("gui.done")));
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 256) {
			mc.gameSettings.saveOptions();
		}

		super.keyTyped(typedChar, keyCode);
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void action(Button button) {
		if (button.enabled) {
			if (button.id < 100 && button instanceof OptionButton) {
				game_settings.setOptionValue(((OptionButton) button).getOption(), 1);
				button.displayString = game_settings.getKeyBinding(GameSettings.Options.byOrdinal(button.id));
			}

			if (button.id == 200) {
				mc.gameSettings.saveOptions();
				mc.displayScreen(parentScreen);
			}
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(DrawContext context, int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		context.drawCenteredString(fontRenderer, chatTitle, width / 2, 20, 16777215);
		super.draw(context, mouseX, mouseY, partialTicks);
	}

}
