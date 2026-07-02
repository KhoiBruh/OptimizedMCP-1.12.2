package net.minecraft.client.gui.option;

import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.component.Button;
import net.minecraft.client.gui.component.GuiListExtended;
import net.minecraft.client.gui.component.GuiOptionsRowList;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

import java.io.IOException;

public class VideoOptionsScreen extends Screen {

	/**
	 * An array of all of GameSettings.Options's video options.
	 */
	private static final GameSettings.Options[] VIDEO_OPTIONS = new GameSettings.Options[]{
		GameSettings.Options.GRAPHICS, GameSettings.Options.RENDER_DISTANCE,
		GameSettings.Options.AMBIENT_OCCLUSION, GameSettings.Options.FRAMERATE_LIMIT,
		GameSettings.Options.VIEW_BOBBING,
		GameSettings.Options.GUI_SCALE, GameSettings.Options.ATTACK_INDICATOR,
		GameSettings.Options.GAMMA, GameSettings.Options.RENDER_CLOUDS,
		GameSettings.Options.PARTICLES, GameSettings.Options.USE_FULLSCREEN,
		GameSettings.Options.ENABLE_VSYNC, GameSettings.Options.MIPMAP_LEVELS,
		GameSettings.Options.ENTITY_SHADOWS
	};
	private final Screen parentScreen;
	private final GameSettings guiGameSettings;
	protected String screenTitle = "Video Settings";
	private GuiListExtended optionsRowList;

	public VideoOptionsScreen(Screen parentScreenIn, GameSettings gameSettingsIn) {
		parentScreen = parentScreenIn;
		guiGameSettings = gameSettingsIn;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {
		screenTitle = I18n.format("options.videoTitle");
		buttons.clear();
		buttons.add(new Button(200, width / 2 - 100, height - 27, I18n.format("gui.done")));

		optionsRowList = new GuiOptionsRowList(mc, width, height, 32, height - 32, 25, VIDEO_OPTIONS);
	}

	/**
	 * Handles mouse input.
	 */
	public void handleMouse() throws IOException {
		super.handleMouse();
		optionsRowList.handleMouseInput();
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
			if (button.id == 200) {
				mc.gameSettings.saveOptions();
				mc.displayScreen(parentScreen);
			}
		}
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouse) throws IOException {
		int i = guiGameSettings.guiScale;
		super.mouseClicked(mouseX, mouseY, mouse);
		optionsRowList.mouseClicked(mouseX, mouseY, mouse);

		if (guiGameSettings.guiScale != i) {
			int j = mc.getWindow().getScaledWidth();
			int k = mc.getWindow().getScaledHeight();
			setResolution(mc, j, k);
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
			int j = mc.getWindow().getScaledWidth();
			int k = mc.getWindow().getScaledHeight();
			setResolution(mc, j, k);
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		optionsRowList.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, screenTitle, width / 2, 5, 16777215);
		super.draw(mouseX, mouseY, partialTicks);
	}

}
