package net.minecraft.client.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.io.IOException;

public class GuiGameOver extends GuiScreen {

	private final ITextComponent causeOfDeath;
	/**
	 * The integer value containing the number of ticks that have passed since the player's death
	 */
	private int enableButtonsTimer;

	public GuiGameOver(@Nullable ITextComponent causeOfDeathIn) {

		causeOfDeath = causeOfDeathIn;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

		buttonList.clear();
		enableButtonsTimer = 0;

		if (mc.world.getWorldInfo().isHardcoreModeEnabled()) {
			buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 72, I18n.format("deathScreen.spectate")));
			buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 96, I18n.format("deathScreen." + (mc.isIntegratedServerRunning() ? "deleteWorld" : "leaveServer"))));
		} else {
			buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 72, I18n.format("deathScreen.respawn")));
			buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 96, I18n.format("deathScreen.titleScreen")));

			if (mc.getSession() == null) {
				(buttonList.get(1)).enabled = false;
			}
		}

		for (GuiButton guibutton : buttonList) {
			guibutton.enabled = false;
		}
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) throws IOException {

	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void actionPerformed(GuiButton button) throws IOException {

		switch (button.id) {
			case 0:
				mc.player.respawnPlayer();
				mc.displayGuiScreen(null);
				break;

			case 1:
				if (mc.world.getWorldInfo().isHardcoreModeEnabled()) {
					mc.displayGuiScreen(new GuiMainMenu());
				} else {
					GuiYesNo guiyesno = new GuiYesNo(this, I18n.format("deathScreen.quit.confirm"), "", I18n.format("deathScreen.titleScreen"), I18n.format("deathScreen.respawn"), 0);
					mc.displayGuiScreen(guiyesno);
					guiyesno.setButtonDelay(20);
				}
		}
	}

	public void confirmClicked(boolean result, int id) {

		if (result) {
			if (mc.world != null) {
				mc.world.sendQuittingDisconnectingPacket();
			}

			mc.loadWorld(null);
			mc.displayGuiScreen(new GuiMainMenu());
		} else {
			mc.player.respawnPlayer();
			mc.displayGuiScreen(null);
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		boolean flag = mc.world.getWorldInfo().isHardcoreModeEnabled();
		drawGradientRect(0, 0, width, height, 1615855616, -1602211792);
		GlStateManager.pushMatrix();
		GlStateManager.scale(2.0F, 2.0F, 2.0F);
		drawCenteredString(fontRenderer, I18n.format(flag ? "deathScreen.title.hardcore" : "deathScreen.title"), width / 2 / 2, 30, 16777215);
		GlStateManager.popMatrix();

		if (causeOfDeath != null) {
			drawCenteredString(fontRenderer, causeOfDeath.getFormattedText(), width / 2, 85, 16777215);
		}

		drawCenteredString(fontRenderer, I18n.format("deathScreen.score") + ": " + TextFormatting.YELLOW + mc.player.getScore(), width / 2, 100, 16777215);

		if (causeOfDeath != null && mouseY > 85 && mouseY < 85 + fontRenderer.FONT_HEIGHT) {
			ITextComponent itextcomponent = getClickedComponentAt(mouseX);

			if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null) {
				handleComponentHover(itextcomponent, mouseX, mouseY);
			}
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Nullable
	public ITextComponent getClickedComponentAt(int p_184870_1_) {

		if (causeOfDeath == null) {
			return null;
		} else {
			int i = mc.fontRenderer.getStringWidth(causeOfDeath.getFormattedText());
			int j = width / 2 - i / 2;
			int k = width / 2 + i / 2;
			int l = j;

			if (p_184870_1_ >= j && p_184870_1_ <= k) {
				for (ITextComponent itextcomponent : causeOfDeath) {
					l += mc.fontRenderer.getStringWidth(GuiUtilRenderComponents.removeTextColorsIfConfigured(itextcomponent.getUnformattedComponentText(), false));

					if (l > p_184870_1_) {
						return itextcomponent;
					}
				}

				return null;
			} else {
				return null;
			}
		}
	}

	/**
	 * Returns true if this GUI should pause the game when it is displayed in single-player
	 */
	public boolean doesGuiPauseGame() {

		return false;
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {

		super.updateScreen();
		++enableButtonsTimer;

		if (enableButtonsTimer == 20) {
			for (GuiButton guibutton : buttonList) {
				guibutton.enabled = true;
			}
		}
	}

}
