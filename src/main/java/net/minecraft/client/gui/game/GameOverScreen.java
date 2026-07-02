package net.minecraft.client.gui.game;

import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.component.Button;
import net.minecraft.client.gui.menu.MainMenuScreen;
import net.minecraft.client.gui.menu.YesNoScreen;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormat;

public class GameOverScreen extends Screen {

	private final ITextComponent causeOfDeath;
	/**
	 * The integer value containing the number of ticks that have passed since the player's death
	 */
	private int enableButtonsTimer;

	public GameOverScreen(ITextComponent causeOfDeathIn) {
		causeOfDeath = causeOfDeathIn;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {
		buttons.clear();
		enableButtonsTimer = 0;

		if (mc.world.getWorldInfo().isHardcoreModeEnabled()) {
			buttons.add(new Button(0, width / 2 - 100, height / 4 + 72, I18n.format("deathScreen.spectate")));
			buttons.add(new Button(1, width / 2 - 100, height / 4 + 96, I18n.format("deathScreen." + (mc.isIntegratedServerRunning() ? "deleteWorld" : "leaveServer"))));
		} else {
			buttons.add(new Button(0, width / 2 - 100, height / 4 + 72, I18n.format("deathScreen.respawn")));
			buttons.add(new Button(1, width / 2 - 100, height / 4 + 96, I18n.format("deathScreen.titleScreen")));

			if (mc.getSession() == null) {
				(buttons.get(1)).enabled = false;
			}
		}

		for (Button guibutton : buttons) {
			guibutton.enabled = false;
		}
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) {
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void action(Button button) {
		switch (button.id) {
			case 0:
				mc.player.respawnPlayer();
				mc.displayScreen(null);
				break;

			case 1:
				if (mc.world.getWorldInfo().isHardcoreModeEnabled()) {
					mc.displayScreen(new MainMenuScreen());
				} else {
					YesNoScreen guiyesno = new YesNoScreen(this, I18n.format("deathScreen.quit.confirm"), "", I18n.format("deathScreen.titleScreen"), I18n.format("deathScreen.respawn"), 0);
					mc.displayScreen(guiyesno);
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
			mc.displayScreen(new MainMenuScreen());
		} else {
			mc.player.respawnPlayer();
			mc.displayScreen(null);
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(int mouseX, int mouseY, float partialTicks) {
		boolean flag = mc.world.getWorldInfo().isHardcoreModeEnabled();
		drawGradientRect(0, 0, width, height, 1615855616, -1602211792);
		GLS.pushMatrix();
		GLS.scale(2F, 2F, 2F);
		drawCenteredString(fontRenderer, I18n.format(flag ? "deathScreen.title.hardcore" : "deathScreen.title"), width / 2 / 2, 30, 16777215);
		GLS.popMatrix();

		if (causeOfDeath != null) {
			drawCenteredString(fontRenderer, causeOfDeath.getFormattedText(), width / 2, 85, 16777215);
		}

		drawCenteredString(fontRenderer, I18n.format("deathScreen.score") + ": " + TextFormat.YELLOW + mc.player.getScore(), width / 2, 100, 16777215);

		if (causeOfDeath != null && mouseY > 85 && mouseY < 85 + fontRenderer.FONT_HEIGHT) {
			ITextComponent itextcomponent = getClickedComponentAt(mouseX);

			if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null) {
				handleComponentHover(itextcomponent, mouseX, mouseY);
			}
		}

		super.draw(mouseX, mouseY, partialTicks);
	}

	public ITextComponent getClickedComponentAt(int p_184870_1_) {
		if (causeOfDeath == null) {
			return null;
		} else {
			int i = mc.fontRenderer.getWidth(causeOfDeath.getFormattedText());
			int j = width / 2 - i / 2;
			int k = width / 2 + i / 2;
			int l = j;

			if (p_184870_1_ >= j && p_184870_1_ <= k) {
				for (ITextComponent itextcomponent : causeOfDeath) {
					l += mc.fontRenderer.getWidth(GuiUtilRenderComponents.removeTextColorsIfConfigured(itextcomponent.getUnformattedComponentText(), false));

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
	public boolean pauseGame() {
		return false;
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void update() {
		super.update();
		++enableButtonsTimer;

		if (enableButtonsTimer == 20) {
			for (Button guibutton : buttons) {
				guibutton.enabled = true;
			}
		}
	}

}
