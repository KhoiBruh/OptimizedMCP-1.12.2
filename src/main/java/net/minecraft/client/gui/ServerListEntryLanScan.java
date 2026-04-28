package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class ServerListEntryLanScan implements GuiListExtended.IGuiListEntry {

	private final Minecraft mc = Minecraft.getMinecraft();

	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {

		int i = y + slotHeight / 2 - mc.fontRenderer.FONT_HEIGHT / 2;
		mc.fontRenderer.drawString(I18n.format("lanServer.scanning"), mc.currentScreen.width / 2 - mc.fontRenderer.getStringWidth(I18n.format("lanServer.scanning")) / 2, i, 16777215);
		String s = switch ((int) (Minecraft.getSystemTime() / 300L % 4L)) {
			case 1, 3 -> "o O o";
			case 2 -> "o o O";
			default -> "O o o";
		};

		mc.fontRenderer.drawString(s, mc.currentScreen.width / 2 - mc.fontRenderer.getStringWidth(s) / 2, i + mc.fontRenderer.FONT_HEIGHT, 8421504);
	}

	public void updatePosition(int slotIndex, int x, int y, float partialTicks) {

	}

	/**
	 * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
	 * clicked and the list should not be dragged.
	 */
	public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {

		return false;
	}

	/**
	 * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
	 */
	public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {

	}

}
