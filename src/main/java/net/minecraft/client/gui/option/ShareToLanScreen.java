package net.minecraft.client.gui.option;

import net.minecraft.client.gui.component.Button;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;

public class ShareToLanScreen extends Screen {

	private final Screen lastScreen;
	private Button allowCheatsButton;
	private Button gameModeButton;
	private String gameMode = "survival";
	private boolean allowCheats;

	public ShareToLanScreen(Screen lastScreenIn) {

		lastScreen = lastScreenIn;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {

		buttons.clear();
		buttons.add(new Button(101, width / 2 - 155, height - 28, 150, 20, I18n.format("lanServer.start")));
		buttons.add(new Button(102, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
		gameModeButton = addButton(new Button(104, width / 2 - 155, 100, 150, 20, I18n.format("selectWorld.gameMode")));
		allowCheatsButton = addButton(new Button(103, width / 2 + 5, 100, 150, 20, I18n.format("selectWorld.allowCommands")));
		updateDisplayNames();
	}

	private void updateDisplayNames() {

		gameModeButton.displayString = I18n.format("selectWorld.gameMode") + ": " + I18n.format("selectWorld.gameMode." + gameMode);
		allowCheatsButton.displayString = I18n.format("selectWorld.allowCommands") + " ";

		if (allowCheats) {
			allowCheatsButton.displayString = allowCheatsButton.displayString + I18n.format("options.on");
		} else {
			allowCheatsButton.displayString = allowCheatsButton.displayString + I18n.format("options.off");
		}
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void action(Button button) {

		if (button.id == 102) {
			mc.displayScreen(lastScreen);
		} else if (button.id == 104) {
			switch (gameMode) {
				case "spectator" -> gameMode = "creative";
				case "creative" -> gameMode = "adventure";
				case "adventure" -> gameMode = "survival";
				case null, default -> gameMode = "spectator";
			}

			updateDisplayNames();
		} else if (button.id == 103) {
			allowCheats = !allowCheats;
			updateDisplayNames();
		} else if (button.id == 101) {
			mc.displayScreen(null);
			String s = mc.getIntegratedServer().shareToLAN(GameType.getByName(gameMode), allowCheats);
			ITextComponent itextcomponent;

			if (s != null) {
				itextcomponent = new TextComponentTranslation("commands.publish.started", s);
			} else {
				itextcomponent = new TextComponentString("commands.publish.failed");
			}

			mc.ingameGUI.getChatGUI().printChatMessage(itextcomponent);
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		drawCenteredString(fontRenderer, I18n.format("lanServer.title"), width / 2, 50, 16777215);
		drawCenteredString(fontRenderer, I18n.format("lanServer.otherPlayers"), width / 2, 82, 16777215);
		super.draw(mouseX, mouseY, partialTicks);
	}

}
