package net.minecraft.client.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class GuiDisconnected extends GuiScreen {

	private final String reason;
	private final ITextComponent message;
	private final GuiScreen parentScreen;
	private List<String> multilineMessage;
	private int textHeight;

	public GuiDisconnected(GuiScreen screen, String reasonLocalizationKey, ITextComponent chatComp) {

		parentScreen = screen;
		reason = I18n.format(reasonLocalizationKey);
		message = chatComp;
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) {

	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

		buttonList.clear();
		multilineMessage = fontRenderer.listFormattedStringToWidth(message.getFormattedText(), width - 50);
		textHeight = multilineMessage.size() * fontRenderer.FONT_HEIGHT;
		buttonList.add(new GuiButton(0, width / 2 - 100, Math.min(height / 2 + textHeight / 2 + fontRenderer.FONT_HEIGHT, height - 30), I18n.format("gui.toMenu")));
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void actionPerformed(GuiButton button) {

		if (button.id == 0) {
			mc.displayGuiScreen(parentScreen);
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		drawCenteredString(fontRenderer, reason, width / 2, height / 2 - textHeight / 2 - fontRenderer.FONT_HEIGHT * 2, 11184810);
		int i = height / 2 - textHeight / 2;

		if (multilineMessage != null) {
			for (String s : multilineMessage) {
				drawCenteredString(fontRenderer, s, width / 2, i, 16777215);
				i += fontRenderer.FONT_HEIGHT;
			}
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

}
