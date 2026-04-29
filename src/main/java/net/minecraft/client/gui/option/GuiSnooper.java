package net.minecraft.client.gui.option;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.component.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.component.GuiSlot;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.TreeMap;

public class GuiSnooper extends GuiScreen {

	private final GuiScreen lastScreen;

	/**
	 * Reference to the GameSettings object.
	 */
	private final GameSettings game_settings_2;
	private final java.util.List<String> keys = Lists.newArrayList();
	private final java.util.List<String> values = Lists.newArrayList();
	private String title;
	private String[] desc;
	private GuiSnooper.List list;
	private GuiButton toggleButton;

	public GuiSnooper(GuiScreen p_i1061_1_, GameSettings p_i1061_2_) {

		lastScreen = p_i1061_1_;
		game_settings_2 = p_i1061_2_;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

		title = I18n.format("options.snooper.title");
		String s = I18n.format("options.snooper.desc");
		java.util.List<String> list = Lists.newArrayList();

		list.addAll(fontRenderer.listFormattedStringToWidth(s, width - 30));

		desc = list.toArray(new String[0]);
		keys.clear();
		values.clear();
		toggleButton = addButton(new GuiButton(1, width / 2 - 152, height - 30, 150, 20, game_settings_2.getKeyBinding(GameSettings.Options.SNOOPER_ENABLED)));
		buttonList.add(new GuiButton(2, width / 2 + 2, height - 30, 150, 20, I18n.format("gui.done")));
		boolean flag = mc.getIntegratedServer() != null && mc.getIntegratedServer().getPlayerUsageSnooper() != null;

		for (Entry<String, String> entry : (new TreeMap<>(mc.getPlayerUsageSnooper().getCurrentStats())).entrySet()) {
			keys.add((flag ? "C " : "") + entry.getKey());
			values.add(fontRenderer.trimStringToWidth(entry.getValue(), width - 220));
		}

		if (flag) {
			for (Entry<String, String> entry1 : (new TreeMap<>(mc.getIntegratedServer().getPlayerUsageSnooper().getCurrentStats())).entrySet()) {
				keys.add("S " + entry1.getKey());
				values.add(fontRenderer.trimStringToWidth(entry1.getValue(), width - 220));
			}
		}

		this.list = new GuiSnooper.List();
	}

	/**
	 * Handles mouse input.
	 */
	public void handleMouseInput() throws IOException {

		super.handleMouseInput();
		list.handleMouseInput();
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void actionPerformed(GuiButton button) {

		if (button.enabled) {
			if (button.id == 2) {
				game_settings_2.saveOptions();
				game_settings_2.saveOptions();
				mc.displayGuiScreen(lastScreen);
			}

			if (button.id == 1) {
				game_settings_2.setOptionValue(GameSettings.Options.SNOOPER_ENABLED, 1);
				toggleButton.displayString = game_settings_2.getKeyBinding(GameSettings.Options.SNOOPER_ENABLED);
			}
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		list.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, title, width / 2, 8, 16777215);
		int i = 22;

		for (String s : desc) {
			drawCenteredString(fontRenderer, s, width / 2, i, 8421504);
			i += fontRenderer.FONT_HEIGHT;
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	class List extends GuiSlot {

		public List() {

			super(GuiSnooper.this.mc, GuiSnooper.this.width, GuiSnooper.this.height, 80, GuiSnooper.this.height - 40, fontRenderer.FONT_HEIGHT + 1);
		}

		protected int getSize() {

			return keys.size();
		}

		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {

		}

		protected boolean isSelected(int slotIndex) {

			return false;
		}

		protected void drawBackground() {

		}

		protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {

			fontRenderer.drawString(keys.get(slotIndex), 10, yPos, 16777215);
			fontRenderer.drawString(values.get(slotIndex), 230, yPos, 16777215);
		}

		protected int getScrollBarX() {

			return width - 10;
		}

	}

}
