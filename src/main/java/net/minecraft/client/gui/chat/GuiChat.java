package net.minecraft.client.gui.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ITabCompleter;
import net.minecraft.util.TabCompleter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import java.io.IOException;

public class GuiChat extends GuiScreen implements ITabCompleter {

	private static final Logger LOGGER = LogManager.getLogger();
	/**
	 * Chat entry field
	 */
	protected GuiTextField inputField;
	private String historyBuffer = "";
	/**
	 * keeps position of which chat message you will select when you press up, (does not increase for duplicated
	 * messages sent immediately after each other)
	 */
	private int sentHistoryCursor = -1;
	private TabCompleter tabCompleter;
	/**
	 * is the text that appears when you press the chat key and the input box appears pre-filled
	 */
	private String defaultInputFieldText = "";

	public GuiChat() {

	}

	public GuiChat(String defaultText) {

		defaultInputFieldText = defaultText;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

		Keyboard.enableRepeatEvents(true);
		sentHistoryCursor = mc.ingameGUI.getChatGUI().getSentMessages().size();
		inputField = new GuiTextField(0, fontRenderer, 4, height - 12, width - 4, 12);
		inputField.setMaxStringLength(256);
		inputField.setEnableBackgroundDrawing(false);
		inputField.setFocused(true);
		inputField.setText(defaultInputFieldText);
		inputField.setCanLoseFocus(false);
		tabCompleter = new GuiChat.ChatTabCompleter(inputField);
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void onGuiClosed() {

		Keyboard.enableRepeatEvents(false);
		mc.ingameGUI.getChatGUI().resetScroll();
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {

		inputField.updateCursorCounter();
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) throws IOException {

		tabCompleter.resetRequested();

		if (keyCode == 15) {
			tabCompleter.complete();
		} else {
			tabCompleter.resetDidComplete();
		}

		if (keyCode == 1) {
			mc.displayGuiScreen(null);
		} else if (keyCode != 28 && keyCode != 156) {
			if (keyCode == 200) {
				getSentHistory(-1);
			} else if (keyCode == 208) {
				getSentHistory(1);
			} else if (keyCode == 201) {
				mc.ingameGUI.getChatGUI().scroll(mc.ingameGUI.getChatGUI().getLineCount() - 1);
			} else if (keyCode == 209) {
				mc.ingameGUI.getChatGUI().scroll(-mc.ingameGUI.getChatGUI().getLineCount() + 1);
			} else {
				inputField.textboxKeyTyped(typedChar, keyCode);
			}
		} else {
			String s = inputField.getText().trim();

			if (!s.isEmpty()) {
				sendChatMessage(s);
			}

			mc.displayGuiScreen(null);
		}
	}

	/**
	 * Handles mouse input.
	 */
	public void handleMouseInput() throws IOException {

		super.handleMouseInput();
		int i = Mouse.getEventDWheel();

		if (i != 0) {
			if (i > 1) {
				i = 1;
			}

			if (i < -1) {
				i = -1;
			}

			if (!isShiftKeyDown()) {
				i *= 7;
			}

			mc.ingameGUI.getChatGUI().scroll(i);
		}
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

		if (mouseButton == 0) {
			ITextComponent itextcomponent = mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

			if (itextcomponent != null && handleComponentClick(itextcomponent)) {
				return;
			}
		}

		inputField.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Sets the text of the chat
	 */
	protected void setText(String newChatText, boolean shouldOverwrite) {

		if (shouldOverwrite) {
			inputField.setText(newChatText);
		} else {
			inputField.writeText(newChatText);
		}
	}

	/**
	 * input is relative and is applied directly to the sentHistoryCursor so -1 is the previous message, 1 is the next
	 * message from the current cursor position
	 */
	public void getSentHistory(int msgPos) {

		int i = sentHistoryCursor + msgPos;
		int j = mc.ingameGUI.getChatGUI().getSentMessages().size();
		i = MathHelper.clamp(i, 0, j);

		if (i != sentHistoryCursor) {
			if (i == j) {
				sentHistoryCursor = j;
				inputField.setText(historyBuffer);
			} else {
				if (sentHistoryCursor == j) {
					historyBuffer = inputField.getText();
				}

				inputField.setText(mc.ingameGUI.getChatGUI().getSentMessages().get(i));
				sentHistoryCursor = i;
			}
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		drawRect(2, height - 14, width - 2, height - 2, Integer.MIN_VALUE);
		inputField.drawTextBox();
		ITextComponent itextcomponent = mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

		if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null) {
			handleComponentHover(itextcomponent, mouseX, mouseY);
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	/**
	 * Returns true if this GUI should pause the game when it is displayed in single-player
	 */
	public boolean doesGuiPauseGame() {

		return false;
	}

	/**
	 * Sets the list of tab completions, as long as they were previously requested.
	 */
	public void setCompletions(String... newCompletions) {

		tabCompleter.setCompletions(newCompletions);
	}

	public static class ChatTabCompleter extends TabCompleter {

		private final Minecraft client = Minecraft.getMinecraft();

		public ChatTabCompleter(GuiTextField p_i46749_1_) {

			super(p_i46749_1_, false);
		}

		public void complete() {

			super.complete();

			if (completions.size() > 1) {
				StringBuilder stringbuilder = new StringBuilder();

				for (String s : completions) {
					if (!stringbuilder.isEmpty()) {
						stringbuilder.append(", ");
					}

					stringbuilder.append(s);
				}

				client.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(stringbuilder.toString()), 1);
			}
		}

		
		public BlockPos getTargetBlockPos() {

			BlockPos blockpos = null;

			if (client.objectMouseOver != null && client.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
				blockpos = client.objectMouseOver.getBlockPos();
			}

			return blockpos;
		}

	}

}
