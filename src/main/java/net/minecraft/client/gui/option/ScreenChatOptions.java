package net.minecraft.client.gui.option;

import net.minecraft.client.gui.component.GuiButton;
import net.minecraft.client.gui.component.GuiOptionButton;
import net.minecraft.client.gui.component.GuiOptionSlider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

import java.io.IOException;

public class ScreenChatOptions extends GuiScreen {

	private static final GameSettings.Options[] CHAT_OPTIONS = new GameSettings.Options[]{GameSettings.Options.CHAT_VISIBILITY, GameSettings.Options.CHAT_COLOR, GameSettings.Options.CHAT_LINKS, GameSettings.Options.CHAT_OPACITY, GameSettings.Options.CHAT_LINKS_PROMPT, GameSettings.Options.CHAT_SCALE, GameSettings.Options.CHAT_HEIGHT_FOCUSED, GameSettings.Options.CHAT_HEIGHT_UNFOCUSED, GameSettings.Options.CHAT_WIDTH, GameSettings.Options.REDUCED_DEBUG_INFO, GameSettings.Options.NARRATOR};
	private final GuiScreen parentScreen;
	private final GameSettings game_settings;
	private String chatTitle;
	private GuiOptionButton narratorButton;

	public ScreenChatOptions(GuiScreen parentScreenIn, GameSettings gameSettingsIn) {

		parentScreen = parentScreenIn;
		game_settings = gameSettingsIn;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

		chatTitle = I18n.format("options.chat.title");
		int i = 0;

		for (GameSettings.Options gamesettings$options : CHAT_OPTIONS) {
			if (gamesettings$options.isFloat()) {
				buttonList.add(new GuiOptionSlider(gamesettings$options.getOrdinal(), width / 2 - 155 + i % 2 * 160, height / 6 + 24 * (i >> 1), gamesettings$options));
			} else {
				GuiOptionButton guioptionbutton = new GuiOptionButton(gamesettings$options.getOrdinal(), width / 2 - 155 + i % 2 * 160, height / 6 + 24 * (i >> 1), gamesettings$options, game_settings.getKeyBinding(gamesettings$options));
				buttonList.add(guioptionbutton);

				if (gamesettings$options == GameSettings.Options.NARRATOR) {
					narratorButton = guioptionbutton;
					guioptionbutton.enabled = NarratorChatListener.INSTANCE.isActive();
				}
			}

			++i;
		}

		buttonList.add(new GuiButton(200, width / 2 - 100, height / 6 + 144, I18n.format("gui.done")));
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) throws IOException {

		if (keyCode == 1) {
			mc.gameSettings.saveOptions();
		}

		super.keyTyped(typedChar, keyCode);
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void actionPerformed(GuiButton button) {

		if (button.enabled) {
			if (button.id < 100 && button instanceof GuiOptionButton) {
				game_settings.setOptionValue(((GuiOptionButton) button).getOption(), 1);
				button.displayString = game_settings.getKeyBinding(GameSettings.Options.byOrdinal(button.id));
			}

			if (button.id == 200) {
				mc.gameSettings.saveOptions();
				mc.displayGuiScreen(parentScreen);
			}
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		drawCenteredString(fontRenderer, chatTitle, width / 2, 20, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	public void updateNarratorButton() {

		narratorButton.displayString = game_settings.getKeyBinding(GameSettings.Options.byOrdinal(narratorButton.id));
	}

}
