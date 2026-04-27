package net.minecraft.client.gui;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

import java.io.IOException;

public class GuiVideoSettings extends GuiScreen {

	/**
	 * An array of all of GameSettings.Options's video options.
	 */
	private static final GameSettings.Options[] VIDEO_OPTIONS = new GameSettings.Options[]{GameSettings.Options.GRAPHICS, GameSettings.Options.RENDER_DISTANCE, GameSettings.Options.AMBIENT_OCCLUSION, GameSettings.Options.FRAMERATE_LIMIT, GameSettings.Options.ANAGLYPH, GameSettings.Options.VIEW_BOBBING, GameSettings.Options.GUI_SCALE, GameSettings.Options.ATTACK_INDICATOR, GameSettings.Options.GAMMA, GameSettings.Options.RENDER_CLOUDS, GameSettings.Options.PARTICLES, GameSettings.Options.USE_FULLSCREEN, GameSettings.Options.ENABLE_VSYNC, GameSettings.Options.MIPMAP_LEVELS, GameSettings.Options.USE_VBO, GameSettings.Options.ENTITY_SHADOWS};
	private final GuiScreen parentGuiScreen;
	private final GameSettings guiGameSettings;
	protected String screenTitle = "Video Settings";
	private GuiListExtended optionsRowList;

	public GuiVideoSettings(GuiScreen parentScreenIn, GameSettings gameSettingsIn) {

		parentGuiScreen = parentScreenIn;
		guiGameSettings = gameSettingsIn;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

		screenTitle = I18n.format("options.videoTitle");
		buttonList.clear();
		buttonList.add(new GuiButton(200, width / 2 - 100, height - 27, I18n.format("gui.done")));

		if (OpenGlHelper.vboSupported) {
			optionsRowList = new GuiOptionsRowList(mc, width, height, 32, height - 32, 25, VIDEO_OPTIONS);
		} else {
			GameSettings.Options[] agamesettings$options = new GameSettings.Options[VIDEO_OPTIONS.length - 1];
			int i = 0;

			for (GameSettings.Options gamesettings$options : VIDEO_OPTIONS) {
				if (gamesettings$options == GameSettings.Options.USE_VBO) {
					break;
				}

				agamesettings$options[i] = gamesettings$options;
				++i;
			}

			optionsRowList = new GuiOptionsRowList(mc, width, height, 32, height - 32, 25, agamesettings$options);
		}
	}

	/**
	 * Handles mouse input.
	 */
	public void handleMouseInput() throws IOException {

		super.handleMouseInput();
		optionsRowList.handleMouseInput();
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
			if (button.id == 200) {
				mc.gameSettings.saveOptions();
				mc.displayGuiScreen(parentGuiScreen);
			}
		}
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

		int i = guiGameSettings.guiScale;
		super.mouseClicked(mouseX, mouseY, mouseButton);
		optionsRowList.mouseClicked(mouseX, mouseY, mouseButton);

		if (guiGameSettings.guiScale != i) {
			ScaledResolution scaledresolution = new ScaledResolution(mc);
			int j = scaledresolution.getScaledWidth();
			int k = scaledresolution.getScaledHeight();
			setWorldAndResolution(mc, j, k);
		}
	}

	/**
	 * Called when a mouse button is released.
	 */
	protected void mouseReleased(int mouseX, int mouseY, int state) {

		int i = guiGameSettings.guiScale;
		super.mouseReleased(mouseX, mouseY, state);
		optionsRowList.mouseReleased(mouseX, mouseY, state);

		if (guiGameSettings.guiScale != i) {
			ScaledResolution scaledresolution = new ScaledResolution(mc);
			int j = scaledresolution.getScaledWidth();
			int k = scaledresolution.getScaledHeight();
			setWorldAndResolution(mc, j, k);
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		optionsRowList.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, screenTitle, width / 2, 5, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

}
