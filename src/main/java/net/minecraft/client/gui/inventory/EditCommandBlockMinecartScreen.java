package net.minecraft.client.gui.inventory;

import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.component.Button;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.component.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.ITabCompleter;
import net.minecraft.util.TabCompleter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.util.Keyboard;
import java.io.IOException;

public class EditCommandBlockMinecartScreen extends Screen implements ITabCompleter {

	private final CommandBlockBaseLogic commandBlockLogic;
	private GuiTextField commandField;
	private GuiTextField previousEdit;
	private Button doneButton;
	private Button cancelButton;
	private Button outputButton;
	private boolean trackOutput;
	private TabCompleter tabCompleter;

	public EditCommandBlockMinecartScreen(CommandBlockBaseLogic p_i46595_1_) {

		commandBlockLogic = p_i46595_1_;
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void update() {

		commandField.updateCursorCounter();
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {

		Keyboard.setRepeat(true);
		buttons.clear();
		doneButton = addButton(new Button(0, width / 2 - 4 - 150, height / 4 + 120 + 12, 150, 20, I18n.format("gui.done")));
		cancelButton = addButton(new Button(1, width / 2 + 4, height / 4 + 120 + 12, 150, 20, I18n.format("gui.cancel")));
		outputButton = addButton(new Button(4, width / 2 + 150 - 20, 150, 20, 20, "O"));
		commandField = new GuiTextField(2, fontRenderer, width / 2 - 150, 50, 300, 20);
		commandField.setMaxStringLength(32500);
		commandField.setFocused(true);
		commandField.setText(commandBlockLogic.getCommand());
		previousEdit = new GuiTextField(3, fontRenderer, width / 2 - 150, 150, 276, 20);
		previousEdit.setMaxStringLength(32500);
		previousEdit.setEnabled(false);
		previousEdit.setText("-");
		trackOutput = commandBlockLogic.shouldTrackOutput();
		updateCommandOutput();
		doneButton.enabled = !commandField.getText().trim().isEmpty();
		tabCompleter = new TabCompleter(commandField, true) {
			
			public BlockPos getTargetBlockPos() {

				return commandBlockLogic.getPosition();
			}
		};
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void close() {

		Keyboard.setRepeat(false);
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void action(Button button) {

		if (button.enabled) {
			if (button.id == 1) {
				commandBlockLogic.setTrackOutput(trackOutput);
				mc.displayScreen(null);
			} else if (button.id == 0) {
				PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
				packetbuffer.writeByte(commandBlockLogic.getCommandBlockType());
				commandBlockLogic.fillInInfo(packetbuffer);
				packetbuffer.writeString(commandField.getText());
				packetbuffer.writeBoolean(commandBlockLogic.shouldTrackOutput());
				mc.getConnection().sendPacket(new CPacketCustomPayload("MC|AdvCmd", packetbuffer));

				if (!commandBlockLogic.shouldTrackOutput()) {
					commandBlockLogic.setLastOutput(null);
				}

				mc.displayScreen(null);
			} else if (button.id == 4) {
				commandBlockLogic.setTrackOutput(!commandBlockLogic.shouldTrackOutput());
				updateCommandOutput();
			}
		}
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) {

		tabCompleter.resetRequested();

		if (keyCode == 258) {
			tabCompleter.complete();
		} else {
			tabCompleter.resetDidComplete();
		}

		commandField.textboxKeyTyped(typedChar, keyCode);
		previousEdit.textboxKeyTyped(typedChar, keyCode);
		doneButton.enabled = !commandField.getText().trim().isEmpty();

		if (keyCode != 257 && keyCode != 335) {
			if (keyCode == 256) {
				action(cancelButton);
			}
		} else {
			action(doneButton);
		}
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

		super.mouseClicked(mouseX, mouseY, mouseButton);
		commandField.mouseClicked(mouseX, mouseY, mouseButton);
		previousEdit.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		drawCenteredString(fontRenderer, I18n.format("advMode.setCommand"), width / 2, 20, 16777215);
		drawString(fontRenderer, I18n.format("advMode.command"), width / 2 - 150, 40, 10526880);
		commandField.drawTextBox();
		int i = 75;
		int j = 0;
		drawString(fontRenderer, I18n.format("advMode.nearestPlayer"), width / 2 - 140, i + j++ * fontRenderer.FONT_HEIGHT, 10526880);
		drawString(fontRenderer, I18n.format("advMode.randomPlayer"), width / 2 - 140, i + j++ * fontRenderer.FONT_HEIGHT, 10526880);
		drawString(fontRenderer, I18n.format("advMode.allPlayers"), width / 2 - 140, i + j++ * fontRenderer.FONT_HEIGHT, 10526880);
		drawString(fontRenderer, I18n.format("advMode.allEntities"), width / 2 - 140, i + j++ * fontRenderer.FONT_HEIGHT, 10526880);
		drawString(fontRenderer, I18n.format("advMode.self"), width / 2 - 140, i + j++ * fontRenderer.FONT_HEIGHT, 10526880);

		if (!previousEdit.getText().isEmpty()) {
			i = i + j * fontRenderer.FONT_HEIGHT + 20;
			drawString(fontRenderer, I18n.format("advMode.previousOutput"), width / 2 - 150, i, 10526880);
			previousEdit.drawTextBox();
		}

		super.draw(mouseX, mouseY, partialTicks);
	}

	private void updateCommandOutput() {

		if (commandBlockLogic.shouldTrackOutput()) {
			outputButton.displayString = "O";

			if (commandBlockLogic.getLastOutput() != null) {
				previousEdit.setText(commandBlockLogic.getLastOutput().getUnformattedText());
			}
		} else {
			outputButton.displayString = "X";
			previousEdit.setText("-");
		}
	}

	/**
	 * Sets the list of tab completions, as long as they were previously requested.
	 */
	public void setCompletions(String... newCompletions) {

		tabCompleter.setCompletions(newCompletions);
	}

}
