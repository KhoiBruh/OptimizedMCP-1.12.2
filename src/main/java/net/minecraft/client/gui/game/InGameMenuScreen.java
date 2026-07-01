package net.minecraft.client.gui.game;

import net.minecraft.client.gui.component.Button;
import net.minecraft.client.gui.option.OptionsScreen;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.option.ShareToLanScreen;
import net.minecraft.client.gui.achievement.StatsScreen;
import net.minecraft.client.gui.advancements.AdvancementsScreen;
import net.minecraft.client.gui.menu.MainMenuScreen;
import net.minecraft.client.gui.menu.MultiplayerScreen;
import net.minecraft.client.resources.I18n;

public class InGameMenuScreen extends Screen {

	private int saveStep;
	private int visibleTime;

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {

		saveStep = 0;
		buttons.clear();
		int i = -16;
		int j = 98;
		buttons.add(new Button(1, width / 2 - 100, height / 4 + 120 - 16, I18n.format("menu.returnToMenu")));

		if (!mc.isIntegratedServerRunning()) {
			(buttons.getFirst()).displayString = I18n.format("menu.disconnect");
		}

		buttons.add(new Button(4, width / 2 - 100, height / 4 + 24 - 16, I18n.format("menu.returnToGame")));
		buttons.add(new Button(0, width / 2 - 100, height / 4 + 96 - 16, 98, 20, I18n.format("menu.options")));
		Button guibutton = addButton(new Button(7, width / 2 + 2, height / 4 + 96 - 16, 98, 20, I18n.format("menu.shareToLan")));
		guibutton.enabled = mc.isSingleplayer() && !mc.getIntegratedServer().getPublic();
		buttons.add(new Button(5, width / 2 - 100, height / 4 + 48 - 16, 98, 20, I18n.format("gui.advancements")));
		buttons.add(new Button(6, width / 2 + 2, height / 4 + 48 - 16, 98, 20, I18n.format("gui.stats")));
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void action(Button button) {

		switch (button.id) {
			case 0:
				mc.displayScreen(new OptionsScreen(this, mc.gameSettings));
				break;

			case 1:
				boolean flag = mc.isIntegratedServerRunning();
				button.enabled = false;
				mc.world.sendQuittingDisconnectingPacket();
				mc.loadWorld(null);

				if (flag) {
					mc.displayScreen(new MainMenuScreen());
				} else {
					mc.displayScreen(new MultiplayerScreen(new MainMenuScreen()));
				}

			case 2:
			case 3:
			default:
				break;

			case 4:
				mc.displayScreen(null);
				mc.setIngameFocus();
				break;

			case 5:
				mc.displayScreen(new AdvancementsScreen(mc.player.connection.getAdvancementManager()));
				break;

			case 6:
				mc.displayScreen(new StatsScreen(this, mc.player.getStatFileWriter()));
				break;

			case 7:
				mc.displayScreen(new ShareToLanScreen(this));
		}
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void update() {

		super.update();
		++visibleTime;
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		drawCenteredString(fontRenderer, I18n.format("menu.game"), width / 2, 40, 16777215);
		super.draw(mouseX, mouseY, partialTicks);
	}

}
