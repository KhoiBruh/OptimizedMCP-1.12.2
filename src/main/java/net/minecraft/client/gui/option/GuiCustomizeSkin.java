package net.minecraft.client.gui.option;

import net.minecraft.client.gui.component.GuiButton;
import net.minecraft.client.gui.component.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EnumPlayerModelParts;

import java.io.IOException;

public class GuiCustomizeSkin extends GuiScreen {

	/**
	 * The parent GUI for this GUI
	 */
	private final GuiScreen parentScreen;

	/**
	 * The title of the GUI.
	 */
	private String title;

	public GuiCustomizeSkin(GuiScreen parentScreenIn) {

		parentScreen = parentScreenIn;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

		int i = 0;
		title = I18n.format("options.skinCustomisation.title");

		for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values()) {
			buttonList.add(new GuiCustomizeSkin.ButtonPart(enumplayermodelparts.getPartId(), width / 2 - 155 + i % 2 * 160, height / 6 + 24 * (i >> 1), 150, 20, enumplayermodelparts));
			++i;
		}

		buttonList.add(new GuiOptionButton(199, width / 2 - 155 + i % 2 * 160, height / 6 + 24 * (i >> 1), GameSettings.Options.MAIN_HAND, mc.gameSettings.getKeyBinding(GameSettings.Options.MAIN_HAND)));
		++i;

		if (i % 2 == 1) {
			++i;
		}

		buttonList.add(new GuiButton(200, width / 2 - 100, height / 6 + 24 * (i >> 1), I18n.format("gui.done")));
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
			if (button.id == 200) {
				mc.gameSettings.saveOptions();
				mc.displayGuiScreen(parentScreen);
			} else if (button.id == 199) {
				mc.gameSettings.setOptionValue(GameSettings.Options.MAIN_HAND, 1);
				button.displayString = mc.gameSettings.getKeyBinding(GameSettings.Options.MAIN_HAND);
				mc.gameSettings.sendSettingsToServer();
			} else if (button instanceof GuiCustomizeSkin.ButtonPart) {
				EnumPlayerModelParts enumplayermodelparts = ((GuiCustomizeSkin.ButtonPart) button).playerModelParts;
				mc.gameSettings.switchModelPartEnabled(enumplayermodelparts);
				button.displayString = getMessage(enumplayermodelparts);
			}
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		drawCenteredString(fontRenderer, title, width / 2, 20, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private String getMessage(EnumPlayerModelParts playerModelParts) {

		String s;

		if (mc.gameSettings.getModelParts().contains(playerModelParts)) {
			s = I18n.format("options.on");
		} else {
			s = I18n.format("options.off");
		}

		return playerModelParts.getName().getFormattedText() + ": " + s;
	}

	class ButtonPart extends GuiButton {

		private final EnumPlayerModelParts playerModelParts;

		private ButtonPart(int p_i45514_2_, int p_i45514_3_, int p_i45514_4_, int p_i45514_5_, int p_i45514_6_, EnumPlayerModelParts playerModelParts) {

			super(p_i45514_2_, p_i45514_3_, p_i45514_4_, p_i45514_5_, p_i45514_6_, getMessage(playerModelParts));
			this.playerModelParts = playerModelParts;
		}

	}

}
