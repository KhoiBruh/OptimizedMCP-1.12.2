package net.minecraft.client.gui.menu;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.io.FileUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiWorldEdit extends GuiScreen {

	private final GuiScreen lastScreen;
	private final String worldId;
	private GuiTextField nameEdit;

	public GuiWorldEdit(GuiScreen p_i46593_1_, String p_i46593_2_) {

		lastScreen = p_i46593_1_;
		worldId = p_i46593_2_;
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {

		nameEdit.updateCursorCounter();
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

		Keyboard.enableRepeatEvents(true);
		buttonList.clear();
		GuiButton guibutton = addButton(new GuiButton(3, width / 2 - 100, height / 4 + 24 + 12, I18n.format("selectWorld.edit.resetIcon")));
		buttonList.add(new GuiButton(4, width / 2 - 100, height / 4 + 48 + 12, I18n.format("selectWorld.edit.openFolder")));
		buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 96 + 12, I18n.format("selectWorld.edit.save")));
		buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 120 + 12, I18n.format("gui.cancel")));
		guibutton.enabled = mc.getSaveLoader().getFile(worldId, "icon.png").isFile();
		ISaveFormat isaveformat = mc.getSaveLoader();
		WorldInfo worldinfo = isaveformat.getWorldInfo(worldId);
		String s = worldinfo == null ? "" : worldinfo.getWorldName();
		nameEdit = new GuiTextField(2, fontRenderer, width / 2 - 100, 60, 200, 20);
		nameEdit.setFocused(true);
		nameEdit.setText(s);
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void onGuiClosed() {

		Keyboard.enableRepeatEvents(false);
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void actionPerformed(GuiButton button) {

		if (button.enabled) {
			if (button.id == 1) {
				mc.displayGuiScreen(lastScreen);
			} else if (button.id == 0) {
				ISaveFormat isaveformat = mc.getSaveLoader();
				isaveformat.renameWorld(worldId, nameEdit.getText().trim());
				mc.displayGuiScreen(lastScreen);
			} else if (button.id == 3) {
				ISaveFormat isaveformat1 = mc.getSaveLoader();
				FileUtils.deleteQuietly(isaveformat1.getFile(worldId, "icon.png"));
				button.enabled = false;
			} else if (button.id == 4) {
				ISaveFormat isaveformat2 = mc.getSaveLoader();
				OpenGlHelper.openFile(isaveformat2.getFile(worldId, "icon.png").getParentFile());
			}
		}
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) {

		nameEdit.textboxKeyTyped(typedChar, keyCode);
		(buttonList.get(2)).enabled = !nameEdit.getText().trim().isEmpty();

		if (keyCode == 28 || keyCode == 156) {
			actionPerformed(buttonList.get(2));
		}
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

		super.mouseClicked(mouseX, mouseY, mouseButton);
		nameEdit.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		drawCenteredString(fontRenderer, I18n.format("selectWorld.edit.title"), width / 2, 20, 16777215);
		drawString(fontRenderer, I18n.format("selectWorld.enterName"), width / 2 - 100, 47, 10526880);
		nameEdit.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

}
