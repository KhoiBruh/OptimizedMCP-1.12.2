package net.minecraft.client.gui.option;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.component.OptionButton;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;

import java.io.IOException;

public class SoundOptionsScreen extends Screen {

	private final Screen parent;

	/**
	 * Reference to the GameSettings object.
	 */
	private final GameSettings game_settings_4;
	protected String title = "Options";
	private String offDisplayString;

	public SoundOptionsScreen(Screen parentIn, GameSettings settingsIn) {
		parent = parentIn;
		game_settings_4 = settingsIn;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {
		title = I18n.format("options.sounds.title");
		offDisplayString = I18n.format("options.off");
		int i = 0;
		buttons.add(new SoundOptionsScreen.Button(SoundCategory.MASTER.ordinal(), width / 2 - 155 + i % 2 * 160, height / 6 - 12 + 24 * (i >> 1), SoundCategory.MASTER, true));
		i = i + 2;

		for (SoundCategory soundcategory : SoundCategory.values()) {
			if (soundcategory != SoundCategory.MASTER) {
				buttons.add(new SoundOptionsScreen.Button(soundcategory.ordinal(), width / 2 - 155 + i % 2 * 160, height / 6 - 12 + 24 * (i >> 1), soundcategory, false));
				++i;
			}
		}

		int j = width / 2 - 75;
		int k = height / 6 - 12;
		++i;
		buttons.add(new OptionButton(201, j, k + 24 * (i >> 1), GameSettings.Options.SHOW_SUBTITLES, game_settings_4.getKeyBinding(GameSettings.Options.SHOW_SUBTITLES)));
		buttons.add(new net.minecraft.client.gui.component.Button(200, width / 2 - 100, height / 6 + 168, I18n.format("gui.done")));
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 256) {
			mc.gameSettings.saveOptions();
		}

		super.keyTyped(typedChar, keyCode);
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void action(net.minecraft.client.gui.component.Button button) {
		if (button.enabled) {
			if (button.id == 200) {
				mc.gameSettings.saveOptions();
				mc.displayScreen(parent);
			} else if (button.id == 201) {
				mc.gameSettings.setOptionValue(GameSettings.Options.SHOW_SUBTITLES, 1);
				button.displayString = mc.gameSettings.getKeyBinding(GameSettings.Options.SHOW_SUBTITLES);
				mc.gameSettings.saveOptions();
			}
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, title, width / 2, 15, 16777215);
		super.draw(mouseX, mouseY, partialTicks);
	}

	protected String getDisplayString(SoundCategory category) {
		float f = game_settings_4.getSoundLevel(category);
		return f == 0F ? offDisplayString : (int) (f * 100F) + "%";
	}

	class Button extends net.minecraft.client.gui.component.Button {

		private final SoundCategory category;
		private final String categoryName;
		public float volume;
		public boolean pressed;

		public Button(int buttonId, int x, int y, SoundCategory categoryIn, boolean master) {
			super(buttonId, x, y, master ? 310 : 150, 20, "");
			category = categoryIn;
			categoryName = I18n.format("soundCategory." + categoryIn.getName());
			displayString = categoryName + ": " + getDisplayString(categoryIn);
			volume = game_settings_4.getSoundLevel(categoryIn);
		}

		protected int getHoverState(boolean mouseOver) {
			return 0;
		}

		protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
			if (visible) {
				if (pressed) {
					volume = (float) (mouseX - (x + 4)) / (float) (width - 8);
					volume = MathHelper.clamp(volume, 0F, 1F);
					mc.gameSettings.setSoundLevel(category, volume);
					mc.gameSettings.saveOptions();
					displayString = categoryName + ": " + getDisplayString(category);
				}

				GLS.color(1F, 1F, 1F, 1F);
				drawTexturedModalRect(x + (int) (volume * (float) (width - 8)), y, 0, 66, 4, 20);
				drawTexturedModalRect(x + (int) (volume * (float) (width - 8)) + 4, y, 196, 66, 4, 20);
			}
		}

		public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
			if (super.mousePressed(mc, mouseX, mouseY)) {
				volume = (float) (mouseX - (x + 4)) / (float) (width - 8);
				volume = MathHelper.clamp(volume, 0F, 1F);
				mc.gameSettings.setSoundLevel(category, volume);
				mc.gameSettings.saveOptions();
				displayString = categoryName + ": " + getDisplayString(category);
				pressed = true;
				return true;
			} else {
				return false;
			}
		}

		public void playPressSound(SoundHandler soundHandlerIn) {
		}

		public void mouseReleased(int mouseX, int mouseY) {
			if (pressed) {
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1F));
			}

			pressed = false;
		}

	}

}
