package net.minecraft.client.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumDifficulty;

import java.io.IOException;

public class GuiOptions extends GuiScreen {

	private static final GameSettings.Options[] SCREEN_OPTIONS = new GameSettings.Options[]{GameSettings.Options.FOV};
	private final GuiScreen lastScreen;

	/**
	 * Reference to the GameSettings object.
	 */
	private final GameSettings settings;
	protected String title = "Options";
	private GuiButton difficultyButton;
	private GuiLockIconButton lockButton;

	public GuiOptions(GuiScreen p_i1046_1_, GameSettings p_i1046_2_) {

		lastScreen = p_i1046_1_;
		settings = p_i1046_2_;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

		title = I18n.format("options.title");
		int i = 0;

		for (GameSettings.Options gamesettings$options : SCREEN_OPTIONS) {
			if (gamesettings$options.isFloat()) {
				buttonList.add(new GuiOptionSlider(gamesettings$options.getOrdinal(), width / 2 - 155 + i % 2 * 160, height / 6 - 12 + 24 * (i >> 1), gamesettings$options));
			} else {
				GuiOptionButton guioptionbutton = new GuiOptionButton(gamesettings$options.getOrdinal(), width / 2 - 155 + i % 2 * 160, height / 6 - 12 + 24 * (i >> 1), gamesettings$options, settings.getKeyBinding(gamesettings$options));
				buttonList.add(guioptionbutton);
			}

			++i;
		}

		if (mc.world != null) {
			EnumDifficulty enumdifficulty = mc.world.getDifficulty();
			difficultyButton = new GuiButton(108, width / 2 - 155 + i % 2 * 160, height / 6 - 12 + 24 * (i >> 1), 150, 20, getDifficultyText(enumdifficulty));
			buttonList.add(difficultyButton);

			if (mc.isSingleplayer() && !mc.world.getWorldInfo().isHardcoreModeEnabled()) {
				difficultyButton.setWidth(difficultyButton.getButtonWidth() - 20);
				lockButton = new GuiLockIconButton(109, difficultyButton.x + difficultyButton.getButtonWidth(), difficultyButton.y);
				buttonList.add(lockButton);
				lockButton.setLocked(mc.world.getWorldInfo().isDifficultyLocked());
				lockButton.enabled = !lockButton.isLocked();
				difficultyButton.enabled = !lockButton.isLocked();
			} else {
				difficultyButton.enabled = false;
			}
		} else {
			buttonList.add(new GuiOptionButton(GameSettings.Options.REALMS_NOTIFICATIONS.getOrdinal(), width / 2 - 155 + i % 2 * 160, height / 6 - 12 + 24 * (i >> 1), GameSettings.Options.REALMS_NOTIFICATIONS, settings.getKeyBinding(GameSettings.Options.REALMS_NOTIFICATIONS)));
		}

		buttonList.add(new GuiButton(110, width / 2 - 155, height / 6 + 48 - 6, 150, 20, I18n.format("options.skinCustomisation")));
		buttonList.add(new GuiButton(106, width / 2 + 5, height / 6 + 48 - 6, 150, 20, I18n.format("options.sounds")));
		buttonList.add(new GuiButton(101, width / 2 - 155, height / 6 + 72 - 6, 150, 20, I18n.format("options.video")));
		buttonList.add(new GuiButton(100, width / 2 + 5, height / 6 + 72 - 6, 150, 20, I18n.format("options.controls")));
		buttonList.add(new GuiButton(102, width / 2 - 155, height / 6 + 96 - 6, 150, 20, I18n.format("options.language")));
		buttonList.add(new GuiButton(103, width / 2 + 5, height / 6 + 96 - 6, 150, 20, I18n.format("options.chat.title")));
		buttonList.add(new GuiButton(105, width / 2 - 155, height / 6 + 120 - 6, 150, 20, I18n.format("options.resourcepack")));
		buttonList.add(new GuiButton(104, width / 2 + 5, height / 6 + 120 - 6, 150, 20, I18n.format("options.snooper.view")));
		buttonList.add(new GuiButton(200, width / 2 - 100, height / 6 + 168, I18n.format("gui.done")));
	}

	public String getDifficultyText(EnumDifficulty p_175355_1_) {

		ITextComponent itextcomponent = new TextComponentString("");
		itextcomponent.appendSibling(new TextComponentTranslation("options.difficulty"));
		itextcomponent.appendText(": ");
		itextcomponent.appendSibling(new TextComponentTranslation(p_175355_1_.getDifficultyResourceKey()));
		return itextcomponent.getFormattedText();
	}

	public void confirmClicked(boolean result, int id) {

		mc.displayGuiScreen(this);

		if (id == 109 && result && mc.world != null) {
			mc.world.getWorldInfo().setDifficultyLocked(true);
			lockButton.setLocked(true);
			lockButton.enabled = false;
			difficultyButton.enabled = false;
		}
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
	protected void actionPerformed(GuiButton button) throws IOException {

		if (button.enabled) {
			if (button.id < 100 && button instanceof GuiOptionButton) {
				GameSettings.Options gamesettings$options = ((GuiOptionButton) button).getOption();
				settings.setOptionValue(gamesettings$options, 1);
				button.displayString = settings.getKeyBinding(GameSettings.Options.byOrdinal(button.id));
			}

			if (button.id == 108) {
				mc.world.getWorldInfo().setDifficulty(EnumDifficulty.getDifficultyEnum(mc.world.getDifficulty().getDifficultyId() + 1));
				difficultyButton.displayString = getDifficultyText(mc.world.getDifficulty());
			}

			if (button.id == 109) {
				mc.displayGuiScreen(new GuiYesNo(this, (new TextComponentTranslation("difficulty.lock.title")).getFormattedText(), (new TextComponentTranslation("difficulty.lock.question", new TextComponentTranslation(mc.world.getWorldInfo().getDifficulty().getDifficultyResourceKey()))).getFormattedText(), 109));
			}

			if (button.id == 110) {
				mc.gameSettings.saveOptions();
				mc.displayGuiScreen(new GuiCustomizeSkin(this));
			}

			if (button.id == 101) {
				mc.gameSettings.saveOptions();
				mc.displayGuiScreen(new GuiVideoSettings(this, settings));
			}

			if (button.id == 100) {
				mc.gameSettings.saveOptions();
				mc.displayGuiScreen(new GuiControls(this, settings));
			}

			if (button.id == 102) {
				mc.gameSettings.saveOptions();
				mc.displayGuiScreen(new GuiLanguage(this, settings, mc.getLanguageManager()));
			}

			if (button.id == 103) {
				mc.gameSettings.saveOptions();
				mc.displayGuiScreen(new ScreenChatOptions(this, settings));
			}

			if (button.id == 104) {
				mc.gameSettings.saveOptions();
				mc.displayGuiScreen(new GuiSnooper(this, settings));
			}

			if (button.id == 200) {
				mc.gameSettings.saveOptions();
				mc.displayGuiScreen(lastScreen);
			}

			if (button.id == 105) {
				mc.gameSettings.saveOptions();
				mc.displayGuiScreen(new GuiScreenResourcePacks(this));
			}

			if (button.id == 106) {
				mc.gameSettings.saveOptions();
				mc.displayGuiScreen(new GuiScreenOptionsSounds(this, settings));
			}
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		drawCenteredString(fontRenderer, title, width / 2, 15, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

}
