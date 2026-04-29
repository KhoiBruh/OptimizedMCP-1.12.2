package net.minecraft.client.gui.game;

import net.minecraft.client.gui.component.GuiButton;
import net.minecraft.client.gui.option.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.option.GuiShareToLan;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.gui.advancements.GuiScreenAdvancements;
import net.minecraft.client.gui.menu.GuiMainMenu;
import net.minecraft.client.gui.menu.GuiMultiplayer;
import net.minecraft.client.resources.I18n;

public class GuiIngameMenu extends GuiScreen {

	private int saveStep;
	private int visibleTime;

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

		saveStep = 0;
		buttonList.clear();
		int i = -16;
		int j = 98;
		buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 120 - 16, I18n.format("menu.returnToMenu")));

		if (!mc.isIntegratedServerRunning()) {
			(buttonList.getFirst()).displayString = I18n.format("menu.disconnect");
		}

		buttonList.add(new GuiButton(4, width / 2 - 100, height / 4 + 24 - 16, I18n.format("menu.returnToGame")));
		buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 96 - 16, 98, 20, I18n.format("menu.options")));
		GuiButton guibutton = addButton(new GuiButton(7, width / 2 + 2, height / 4 + 96 - 16, 98, 20, I18n.format("menu.shareToLan")));
		guibutton.enabled = mc.isSingleplayer() && !mc.getIntegratedServer().getPublic();
		buttonList.add(new GuiButton(5, width / 2 - 100, height / 4 + 48 - 16, 98, 20, I18n.format("gui.advancements")));
		buttonList.add(new GuiButton(6, width / 2 + 2, height / 4 + 48 - 16, 98, 20, I18n.format("gui.stats")));
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void actionPerformed(GuiButton button) {

		switch (button.id) {
			case 0:
				mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
				break;

			case 1:
				boolean flag = mc.isIntegratedServerRunning();
				button.enabled = false;
				mc.world.sendQuittingDisconnectingPacket();
				mc.loadWorld(null);

				if (flag) {
					mc.displayGuiScreen(new GuiMainMenu());
				} else {
					mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
				}

			case 2:
			case 3:
			default:
				break;

			case 4:
				mc.displayGuiScreen(null);
				mc.setIngameFocus();
				break;

			case 5:
				mc.displayGuiScreen(new GuiScreenAdvancements(mc.player.connection.getAdvancementManager()));
				break;

			case 6:
				mc.displayGuiScreen(new GuiStats(this, mc.player.getStatFileWriter()));
				break;

			case 7:
				mc.displayGuiScreen(new GuiShareToLan(this));
		}
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {

		super.updateScreen();
		++visibleTime;
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		drawCenteredString(fontRenderer, I18n.format("menu.game"), width / 2, 40, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

}
