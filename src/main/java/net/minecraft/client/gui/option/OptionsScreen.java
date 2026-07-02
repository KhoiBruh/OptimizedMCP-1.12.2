package net.minecraft.client.gui.option;

import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.component.Button;
import net.minecraft.client.gui.component.LockIconButton;
import net.minecraft.client.gui.component.OptionButton;
import net.minecraft.client.gui.component.OptionSlider;
import net.minecraft.client.gui.menu.ResourcePacksScreen;
import net.minecraft.client.gui.menu.YesNoScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.Difficulty;

import java.io.IOException;

public class OptionsScreen extends Screen {

	private static final GameSettings.Options[] SCREEN_OPTIONS = new GameSettings.Options[]{GameSettings.Options.FOV};
	private final Screen lastScreen;

	/**
	 * Reference to the GameSettings object.
	 */
	private final GameSettings settings;
	protected String title = "Options";
	private Button difficultyButton;
	private LockIconButton lockButton;

	public OptionsScreen(Screen screen, GameSettings settings) {
		lastScreen = screen;
		this.settings = settings;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {
		title = I18n.format("options.title");
		int i = 0;

		for (GameSettings.Options options : SCREEN_OPTIONS) {
			if (options.isFloat()) {
				buttons.add(new OptionSlider(options.ordinal(), width / 2 - 155 + i % 2 * 160, height / 6 - 12 + 24 * (i >> 1), options));
			} else {
				OptionButton button = new OptionButton(options.ordinal(), width / 2 - 155 + i % 2 * 160, height / 6 - 12 + 24 * (i >> 1), options, settings.getKeyBinding(options));
				buttons.add(button);
			}

			++i;
		}

		if (mc.world != null) {
			Difficulty enumdifficulty = mc.world.getDifficulty();
			difficultyButton = new Button(108, width / 2 - 155 + i % 2 * 160, height / 6 - 12 + 24 * (i >> 1), 150, 20, getDifficultyText(enumdifficulty));
			buttons.add(difficultyButton);

			if (mc.isSingleplayer() && !mc.world.getWorldInfo().isHardcoreModeEnabled()) {
				difficultyButton.setWidth(difficultyButton.getButtonWidth() - 20);
				lockButton = new LockIconButton(109, difficultyButton.x + difficultyButton.getButtonWidth(), difficultyButton.y);
				buttons.add(lockButton);
				lockButton.setLocked(mc.world.getWorldInfo().isDifficultyLocked());
				lockButton.enabled = !lockButton.isLocked();
				difficultyButton.enabled = !lockButton.isLocked();
			} else {
				difficultyButton.enabled = false;
			}
		}

		buttons.add(new Button(110, width / 2 - 155, height / 6 + 48 - 6, 150, 20, I18n.format("options.skinCustomisation")));
		buttons.add(new Button(106, width / 2 + 5, height / 6 + 48 - 6, 150, 20, I18n.format("options.sounds")));
		buttons.add(new Button(101, width / 2 - 155, height / 6 + 72 - 6, 150, 20, I18n.format("options.video")));
		buttons.add(new Button(100, width / 2 + 5, height / 6 + 72 - 6, 150, 20, I18n.format("options.controls")));
		buttons.add(new Button(102, width / 2 - 155, height / 6 + 96 - 6, 150, 20, I18n.format("options.language")));
		buttons.add(new Button(103, width / 2 + 5, height / 6 + 96 - 6, 150, 20, I18n.format("options.chat.title")));
		buttons.add(new Button(105, width / 2 - 155, height / 6 + 120 - 6, 150, 20, I18n.format("options.resourcepack")));
		buttons.add(new Button(200, width / 2 - 100, height / 6 + 168, I18n.format("gui.done")));
	}

	public String getDifficultyText(Difficulty p_175355_1_) {
		ITextComponent itextcomponent = new TextComponentString("");
		itextcomponent.appendSibling(new TextComponentTranslation("options.difficulty"));
		itextcomponent.appendText(": ");
		itextcomponent.appendSibling(new TextComponentTranslation(p_175355_1_.getDifficultyResourceKey()));
		return itextcomponent.getFormattedText();
	}

	public void confirmClicked(boolean result, int id) {
		mc.displayScreen(this);

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
				GameSettings.Options gamesettings$options = ((OptionButton) button).getOption();
				settings.setOptionValue(gamesettings$options, 1);
				button.displayString = settings.getKeyBinding(GameSettings.Options.byOrdinal(button.id));
			}

			if (button.id == 108) {
				mc.world.getWorldInfo().setDifficulty(Difficulty.getDifficultyEnum(mc.world.getDifficulty().getDifficultyId() + 1));
				difficultyButton.displayString = getDifficultyText(mc.world.getDifficulty());
			}

			if (button.id == 109) {
				mc.displayScreen(new YesNoScreen(this, (new TextComponentTranslation("difficulty.lock.title")).getFormattedText(), (new TextComponentTranslation("difficulty.lock.question", new TextComponentTranslation(mc.world.getWorldInfo().getDifficulty().getDifficultyResourceKey()))).getFormattedText(), 109));
			}

			if (button.id == 110) {
				mc.gameSettings.saveOptions();
				mc.displayScreen(new CustomizeSkinScreen(this));
			}

			if (button.id == 101) {
				mc.gameSettings.saveOptions();
				mc.displayScreen(new VideoOptionsScreen(this, settings));
			}

			if (button.id == 100) {
				mc.gameSettings.saveOptions();
				mc.displayScreen(new ControlsScreen(this, settings));
			}

			if (button.id == 102) {
				mc.gameSettings.saveOptions();
				mc.displayScreen(new LanguageScreen(this, settings, mc.getLanguageManager()));
			}

			if (button.id == 103) {
				mc.gameSettings.saveOptions();
				mc.displayScreen(new ScreenChatOptions(this, settings));
			}

			if (button.id == 200) {
				mc.gameSettings.saveOptions();
				mc.displayScreen(lastScreen);
			}

			if (button.id == 105) {
				mc.gameSettings.saveOptions();
				mc.displayScreen(new ResourcePacksScreen(this));
			}

			if (button.id == 106) {
				mc.gameSettings.saveOptions();
				mc.displayScreen(new SoundOptionsScreen(this, settings));
			}
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, title, width / 2, 15, 16777215);
		super.draw(mouseX, mouseY, partialTicks);
	}

}
