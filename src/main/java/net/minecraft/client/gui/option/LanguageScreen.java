package net.minecraft.client.gui.option;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.component.Button;
import net.minecraft.client.gui.component.GuiSlot;
import net.minecraft.client.gui.component.OptionButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;

import java.io.IOException;
import java.util.Map;

public class LanguageScreen extends Screen {

	/**
	 * Reference to the GameSettings object.
	 */
	private final GameSettings game_settings_3;
	/**
	 * Reference to the LanguageManager object.
	 */
	private final LanguageManager languageManager;
	/**
	 * The parent Gui screen
	 */
	protected Screen parentScreen;
	/**
	 * The List GuiSlot object reference.
	 */
	private LanguageScreen.List list;
	/**
	 * A button which allows the user to determine if the Unicode font should be forced.
	 */
	private OptionButton forceUnicodeFontBtn;

	/**
	 * The button to confirm the current settings.
	 */
	private OptionButton confirmSettingsBtn;

	public LanguageScreen(Screen screen, GameSettings gameSettingsObj, LanguageManager manager) {
		parentScreen = screen;
		game_settings_3 = gameSettingsObj;
		languageManager = manager;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {
		forceUnicodeFontBtn = addButton(new OptionButton(100, width / 2 - 155, height - 38, GameSettings.Options.FORCE_UNICODE_FONT, game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT)));
		confirmSettingsBtn = addButton(new OptionButton(6, width / 2 - 155 + 160, height - 38, I18n.format("gui.done")));
		list = new LanguageScreen.List(mc);
		list.registerScrollButtons(7, 8);
	}

	/**
	 * Handles mouse input.
	 */
	public void handleMouse() throws IOException {
		super.handleMouse();
		list.handleMouseInput();
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void action(Button button) {
		if (button.enabled) {
			switch (button.id) {
				case 5:
					break;

				case 6:
					mc.displayScreen(parentScreen);
					break;

				case 100:
					if (button instanceof OptionButton) {
						game_settings_3.setOptionValue(((OptionButton) button).getOption(), 1);
						button.displayString = game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT);
						int i = mc.getWindow().getScaledWidth();
						int j = mc.getWindow().getScaledHeight();
						setResolution(mc, i, j);
					}

					break;

				default:
					list.actionPerformed(button);
			}
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(int mouseX, int mouseY, float partialTicks) {
		list.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, I18n.format("options.language"), width / 2, 16, 16777215);
		drawCenteredString(fontRenderer, "(" + I18n.format("options.languageWarning") + ")", width / 2, height - 56, 8421504);
		super.draw(mouseX, mouseY, partialTicks);
	}

	class List extends GuiSlot {

		private final java.util.List<String> langCodeList = Lists.newArrayList();
		private final Map<String, Language> languageMap = Maps.newHashMap();

		public List(Minecraft mcIn) {
			super(mcIn, LanguageScreen.this.width, LanguageScreen.this.height, 32, LanguageScreen.this.height - 65 + 4, 18);

			for (Language language : languageManager.getLanguages()) {
				languageMap.put(language.getLanguageCode(), language);
				langCodeList.add(language.getLanguageCode());
			}
		}

		protected int getSize() {
			return langCodeList.size();
		}

		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
			Language language = languageMap.get(langCodeList.get(slotIndex));
			languageManager.setCurrentLanguage(language);
			game_settings_3.language = language.getLanguageCode();
			mc.refreshResources();
			fontRenderer.setUnicode(languageManager.isCurrentLocaleUnicode() || game_settings_3.forceUnicodeFont);
			fontRenderer.setBidi(languageManager.isCurrentLanguageBidirectional());
			confirmSettingsBtn.displayString = I18n.format("gui.done");
			forceUnicodeFontBtn.displayString = game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT);
			game_settings_3.saveOptions();
		}

		protected boolean isSelected(int slotIndex) {
			return langCodeList.get(slotIndex).equals(languageManager.getCurrentLanguage().getLanguageCode());
		}

		protected int getContentHeight() {
			return getSize() * 18;
		}

		protected void drawBackground() {
			drawDefaultBackground();
		}

		protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {
			fontRenderer.setBidi(true);
			drawCenteredString(fontRenderer, languageMap.get(langCodeList.get(slotIndex))
			                                            .toString(), width / 2, yPos + 1, 16777215);
			fontRenderer.setBidi(languageManager.getCurrentLanguage().isBidirectional());
		}

	}

}
