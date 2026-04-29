package net.minecraft.client.gui.component;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

import java.util.List;

public class GuiOptionsRowList extends GuiListExtended {

	private final List<GuiOptionsRowList.Row> options = Lists.newArrayList();

	public GuiOptionsRowList(Minecraft mcIn, int p_i45015_2_, int p_i45015_3_, int p_i45015_4_, int p_i45015_5_, int p_i45015_6_, GameSettings.Options... p_i45015_7_) {

		super(mcIn, p_i45015_2_, p_i45015_3_, p_i45015_4_, p_i45015_5_, p_i45015_6_);
		centerListVertically = false;

		for (int i = 0; i < p_i45015_7_.length; i += 2) {
			GameSettings.Options gamesettings$options = p_i45015_7_[i];
			GameSettings.Options gamesettings$options1 = i < p_i45015_7_.length - 1 ? p_i45015_7_[i + 1] : null;
			GuiButton guibutton = createButton(mcIn, p_i45015_2_ / 2 - 155, 0, gamesettings$options);
			GuiButton guibutton1 = createButton(mcIn, p_i45015_2_ / 2 - 155 + 160, 0, gamesettings$options1);
			options.add(new GuiOptionsRowList.Row(guibutton, guibutton1));
		}
	}

	private GuiButton createButton(Minecraft mcIn, int p_148182_2_, int p_148182_3_, GameSettings.Options options) {

		if (options == null) {
			return null;
		} else {
			int i = options.getOrdinal();
			return options.isFloat() ? new GuiOptionSlider(i, p_148182_2_, p_148182_3_, options) : new GuiOptionButton(i, p_148182_2_, p_148182_3_, options, mcIn.gameSettings.getKeyBinding(options));
		}
	}

	/**
	 * Gets the IGuiListEntry object for the given index
	 */
	public GuiOptionsRowList.Row getListEntry(int index) {

		return options.get(index);
	}

	protected int getSize() {

		return options.size();
	}

	/**
	 * Gets the width of the list
	 */
	public int getListWidth() {

		return 400;
	}

	protected int getScrollBarX() {

		return super.getScrollBarX() + 32;
	}

	public static class Row implements GuiListExtended.IGuiListEntry {

		private final Minecraft client = Minecraft.getMinecraft();
		private final GuiButton buttonA;
		private final GuiButton buttonB;

		public Row(GuiButton buttonAIn, GuiButton buttonBIn) {

			buttonA = buttonAIn;
			buttonB = buttonBIn;
		}

		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {

			if (buttonA != null) {
				buttonA.y = y;
				buttonA.drawButton(client, mouseX, mouseY, partialTicks);
			}

			if (buttonB != null) {
				buttonB.y = y;
				buttonB.drawButton(client, mouseX, mouseY, partialTicks);
			}
		}

		public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {

			if (buttonA.mousePressed(client, mouseX, mouseY)) {
				if (buttonA instanceof GuiOptionButton) {
					client.gameSettings.setOptionValue(((GuiOptionButton) buttonA).getOption(), 1);
					buttonA.displayString = client.gameSettings.getKeyBinding(GameSettings.Options.byOrdinal(buttonA.id));
				}

				return true;
			} else if (buttonB != null && buttonB.mousePressed(client, mouseX, mouseY)) {
				if (buttonB instanceof GuiOptionButton) {
					client.gameSettings.setOptionValue(((GuiOptionButton) buttonB).getOption(), 1);
					buttonB.displayString = client.gameSettings.getKeyBinding(GameSettings.Options.byOrdinal(buttonB.id));
				}

				return true;
			} else {
				return false;
			}
		}

		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {

			if (buttonA != null) {
				buttonA.mouseReleased(x, y);
			}

			if (buttonB != null) {
				buttonB.mouseReleased(x, y);
			}
		}

		public void updatePosition(int slotIndex, int x, int y, float partialTicks) {

		}

	}

}
