package net.minecraft.client.gui.game;

import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.component.Button;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.component.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.ITabCompleter;
import net.minecraft.util.TabCompleter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.util.Keyboard;
import java.io.IOException;

public class CommandBlockScreen extends Screen implements ITabCompleter {

	private final TileEntityCommandBlock commandBlock;
	/**
	 * Text field containing the command block's command.
	 */
	private GuiTextField commandTextField;
	private GuiTextField previousOutputTextField;
	/**
	 * "Done" button for the GUI.
	 */
	private Button doneBtn;
	private Button cancelBtn;
	private Button outputBtn;
	private Button modeBtn;
	private Button conditionalBtn;
	private Button autoExecBtn;
	private boolean trackOutput;
	private TileEntityCommandBlock.Mode commandBlockMode = TileEntityCommandBlock.Mode.REDSTONE;
	private TabCompleter tabCompleter;
	private boolean conditional;
	private boolean automatic;

	public CommandBlockScreen(TileEntityCommandBlock commandBlockIn) {

		commandBlock = commandBlockIn;
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void update() {

		commandTextField.updateCursorCounter();
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {

		final CommandBlockBaseLogic commandblockbaselogic = commandBlock.getCommandBlockLogic();
		Keyboard.setRepeat(true);
		buttons.clear();
		doneBtn = addButton(new Button(0, width / 2 - 4 - 150, height / 4 + 120 + 12, 150, 20, I18n.format("gui.done")));
		cancelBtn = addButton(new Button(1, width / 2 + 4, height / 4 + 120 + 12, 150, 20, I18n.format("gui.cancel")));
		outputBtn = addButton(new Button(4, width / 2 + 150 - 20, 135, 20, 20, "O"));
		modeBtn = addButton(new Button(5, width / 2 - 50 - 100 - 4, 165, 100, 20, I18n.format("advMode.mode.sequence")));
		conditionalBtn = addButton(new Button(6, width / 2 - 50, 165, 100, 20, I18n.format("advMode.mode.unconditional")));
		autoExecBtn = addButton(new Button(7, width / 2 + 50 + 4, 165, 100, 20, I18n.format("advMode.mode.redstoneTriggered")));
		commandTextField = new GuiTextField(2, fontRenderer, width / 2 - 150, 50, 300, 20);
		commandTextField.setMaxStringLength(32500);
		commandTextField.setFocused(true);
		previousOutputTextField = new GuiTextField(3, fontRenderer, width / 2 - 150, 135, 276, 20);
		previousOutputTextField.setMaxStringLength(32500);
		previousOutputTextField.setEnabled(false);
		previousOutputTextField.setText("-");
		doneBtn.enabled = false;
		outputBtn.enabled = false;
		modeBtn.enabled = false;
		conditionalBtn.enabled = false;
		autoExecBtn.enabled = false;
		tabCompleter = new TabCompleter(commandTextField, true) {
			
			public BlockPos getTargetBlockPos() {

				return commandblockbaselogic.getPosition();
			}
		};
	}

	public void updateGui() {

		CommandBlockBaseLogic commandblockbaselogic = commandBlock.getCommandBlockLogic();
		commandTextField.setText(commandblockbaselogic.getCommand());
		trackOutput = commandblockbaselogic.shouldTrackOutput();
		commandBlockMode = commandBlock.getMode();
		conditional = commandBlock.isConditional();
		automatic = commandBlock.isAuto();
		updateCmdOutput();
		updateMode();
		updateConditional();
		updateAutoExec();
		doneBtn.enabled = true;
		outputBtn.enabled = true;
		modeBtn.enabled = true;
		conditionalBtn.enabled = true;
		autoExecBtn.enabled = true;
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
			CommandBlockBaseLogic commandblockbaselogic = commandBlock.getCommandBlockLogic();

			if (button.id == 1) {
				commandblockbaselogic.setTrackOutput(trackOutput);
				mc.displayScreen(null);
			} else if (button.id == 0) {
				PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
				commandblockbaselogic.fillInInfo(packetbuffer);
				packetbuffer.writeString(commandTextField.getText());
				packetbuffer.writeBoolean(commandblockbaselogic.shouldTrackOutput());
				packetbuffer.writeString(commandBlockMode.name());
				packetbuffer.writeBoolean(conditional);
				packetbuffer.writeBoolean(automatic);
				mc.getConnection().sendPacket(new CPacketCustomPayload("MC|AutoCmd", packetbuffer));

				if (!commandblockbaselogic.shouldTrackOutput()) {
					commandblockbaselogic.setLastOutput(null);
				}

				mc.displayScreen(null);
			} else if (button.id == 4) {
				commandblockbaselogic.setTrackOutput(!commandblockbaselogic.shouldTrackOutput());
				updateCmdOutput();
			} else if (button.id == 5) {
				nextMode();
				updateMode();
			} else if (button.id == 6) {
				conditional = !conditional;
				updateConditional();
			} else if (button.id == 7) {
				automatic = !automatic;
				updateAutoExec();
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

		commandTextField.textboxKeyTyped(typedChar, keyCode);
		previousOutputTextField.textboxKeyTyped(typedChar, keyCode);

		if (keyCode != 257 && keyCode != 335) {
			if (keyCode == 256) {
				action(cancelBtn);
			}
		} else {
			action(doneBtn);
		}
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouse) throws IOException {

		super.mouseClicked(mouseX, mouseY, mouse);
		commandTextField.mouseClicked(mouseX, mouseY, mouse);
		previousOutputTextField.mouseClicked(mouseX, mouseY, mouse);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		drawCenteredString(fontRenderer, I18n.format("advMode.setCommand"), width / 2, 20, 16777215);
		drawString(fontRenderer, I18n.format("advMode.command"), width / 2 - 150, 40, 10526880);
		commandTextField.drawTextBox();
		int i = 75;
		int j = 0;
		drawString(fontRenderer, I18n.format("advMode.nearestPlayer"), width / 2 - 140, i + j++ * fontRenderer.FONT_HEIGHT, 10526880);
		drawString(fontRenderer, I18n.format("advMode.randomPlayer"), width / 2 - 140, i + j++ * fontRenderer.FONT_HEIGHT, 10526880);
		drawString(fontRenderer, I18n.format("advMode.allPlayers"), width / 2 - 140, i + j++ * fontRenderer.FONT_HEIGHT, 10526880);
		drawString(fontRenderer, I18n.format("advMode.allEntities"), width / 2 - 140, i + j++ * fontRenderer.FONT_HEIGHT, 10526880);
		drawString(fontRenderer, I18n.format("advMode.self"), width / 2 - 140, i + j++ * fontRenderer.FONT_HEIGHT, 10526880);

		if (!previousOutputTextField.getText().isEmpty()) {
			i = i + j * fontRenderer.FONT_HEIGHT + 1;
			drawString(fontRenderer, I18n.format("advMode.previousOutput"), width / 2 - 150, i + 4, 10526880);
			previousOutputTextField.drawTextBox();
		}

		super.draw(mouseX, mouseY, partialTicks);
	}

	private void updateCmdOutput() {

		CommandBlockBaseLogic commandblockbaselogic = commandBlock.getCommandBlockLogic();

		if (commandblockbaselogic.shouldTrackOutput()) {
			outputBtn.displayString = "O";

			if (commandblockbaselogic.getLastOutput() != null) {
				previousOutputTextField.setText(commandblockbaselogic.getLastOutput().getUnformattedText());
			}
		} else {
			outputBtn.displayString = "X";
			previousOutputTextField.setText("-");
		}
	}

	private void updateMode() {

		switch (commandBlockMode) {
			case SEQUENCE:
				modeBtn.displayString = I18n.format("advMode.mode.sequence");
				break;

			case AUTO:
				modeBtn.displayString = I18n.format("advMode.mode.auto");
				break;

			case REDSTONE:
				modeBtn.displayString = I18n.format("advMode.mode.redstone");
		}
	}

	private void nextMode() {

		switch (commandBlockMode) {
			case SEQUENCE:
				commandBlockMode = TileEntityCommandBlock.Mode.AUTO;
				break;

			case AUTO:
				commandBlockMode = TileEntityCommandBlock.Mode.REDSTONE;
				break;

			case REDSTONE:
				commandBlockMode = TileEntityCommandBlock.Mode.SEQUENCE;
		}
	}

	private void updateConditional() {

		if (conditional) {
			conditionalBtn.displayString = I18n.format("advMode.mode.conditional");
		} else {
			conditionalBtn.displayString = I18n.format("advMode.mode.unconditional");
		}
	}

	private void updateAutoExec() {

		if (automatic) {
			autoExecBtn.displayString = I18n.format("advMode.mode.autoexec.bat");
		} else {
			autoExecBtn.displayString = I18n.format("advMode.mode.redstoneTriggered");
		}
	}

	/**
	 * Sets the list of tab completions, as long as they were previously requested.
	 */
	public void setCompletions(String... newCompletions) {

		tabCompleter.setCompletions(newCompletions);
	}

}
