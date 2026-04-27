package net.minecraft.client.resources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public abstract class ResourcePackListEntry implements GuiListExtended.IGuiListEntry {

	private static final ResourceLocation RESOURCE_PACKS_TEXTURE = new ResourceLocation("textures/gui/resource_packs.png");
	private static final ITextComponent INCOMPATIBLE = new TextComponentTranslation("resourcePack.incompatible");
	private static final ITextComponent INCOMPATIBLE_OLD = new TextComponentTranslation("resourcePack.incompatible.old");
	private static final ITextComponent INCOMPATIBLE_NEW = new TextComponentTranslation("resourcePack.incompatible.new");
	protected final Minecraft mc;
	protected final GuiScreenResourcePacks resourcePacksGUI;

	public ResourcePackListEntry(GuiScreenResourcePacks resourcePacksGUIIn) {

		resourcePacksGUI = resourcePacksGUIIn;
		mc = Minecraft.getMinecraft();
	}

	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {

		int i = getResourcePackFormat();

		if (i != 3) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			Gui.drawRect(x - 1, y - 1, x + listWidth - 9, y + slotHeight + 1, -8978432);
		}

		bindResourcePackIcon();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
		String s = getResourcePackName();
		String s1 = getResourcePackDescription();

		if (showHoverOverlay() && (mc.gameSettings.touchscreen || isSelected)) {
			mc.getTextureManager().bindTexture(RESOURCE_PACKS_TEXTURE);
			Gui.drawRect(x, y, x + 32, y + 32, -1601138544);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			int j = mouseX - x;
			int k = mouseY - y;

			if (i < 3) {
				s = INCOMPATIBLE.getFormattedText();
				s1 = INCOMPATIBLE_OLD.getFormattedText();
			} else if (i > 3) {
				s = INCOMPATIBLE.getFormattedText();
				s1 = INCOMPATIBLE_NEW.getFormattedText();
			}

			if (canMoveRight()) {
				if (j < 32) {
					Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 32.0F, 32, 32, 256.0F, 256.0F);
				} else {
					Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 256.0F, 256.0F);
				}
			} else {
				if (canMoveLeft()) {
					if (j < 16) {
						Gui.drawModalRectWithCustomSizedTexture(x, y, 32.0F, 32.0F, 32, 32, 256.0F, 256.0F);
					} else {
						Gui.drawModalRectWithCustomSizedTexture(x, y, 32.0F, 0.0F, 32, 32, 256.0F, 256.0F);
					}
				}

				if (canMoveUp()) {
					if (j < 32 && j > 16 && k < 16) {
						Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 32.0F, 32, 32, 256.0F, 256.0F);
					} else {
						Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 0.0F, 32, 32, 256.0F, 256.0F);
					}
				}

				if (canMoveDown()) {
					if (j < 32 && j > 16 && k > 16) {
						Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 32.0F, 32, 32, 256.0F, 256.0F);
					} else {
						Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 0.0F, 32, 32, 256.0F, 256.0F);
					}
				}
			}
		}

		int i1 = mc.fontRenderer.getStringWidth(s);

		if (i1 > 157) {
			s = mc.fontRenderer.trimStringToWidth(s, 157 - mc.fontRenderer.getStringWidth("...")) + "...";
		}

		mc.fontRenderer.drawStringWithShadow(s, (float) (x + 32 + 2), (float) (y + 1), 16777215);
		List<String> list = mc.fontRenderer.listFormattedStringToWidth(s1, 157);

		for (int l = 0; l < 2 && l < list.size(); ++l) {
			mc.fontRenderer.drawStringWithShadow(list.get(l), (float) (x + 32 + 2), (float) (y + 12 + 10 * l), 8421504);
		}
	}

	protected abstract int getResourcePackFormat();

	protected abstract String getResourcePackDescription();

	protected abstract String getResourcePackName();

	protected abstract void bindResourcePackIcon();

	protected boolean showHoverOverlay() {

		return true;
	}

	protected boolean canMoveRight() {

		return !resourcePacksGUI.hasResourcePackEntry(this);
	}

	protected boolean canMoveLeft() {

		return resourcePacksGUI.hasResourcePackEntry(this);
	}

	protected boolean canMoveUp() {

		List<ResourcePackListEntry> list = resourcePacksGUI.getListContaining(this);
		int i = list.indexOf(this);
		return i > 0 && list.get(i - 1).showHoverOverlay();
	}

	protected boolean canMoveDown() {

		List<ResourcePackListEntry> list = resourcePacksGUI.getListContaining(this);
		int i = list.indexOf(this);
		return i >= 0 && i < list.size() - 1 && list.get(i + 1).showHoverOverlay();
	}

	/**
	 * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
	 * clicked and the list should not be dragged.
	 */
	public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {

		if (showHoverOverlay() && relativeX <= 32) {
			if (canMoveRight()) {
				resourcePacksGUI.markChanged();
				final int j = resourcePacksGUI.getSelectedResourcePacks().getFirst().isServerPack() ? 1 : 0;
				int l = getResourcePackFormat();

				if (l == 3) {
					resourcePacksGUI.getListContaining(this).remove(this);
					resourcePacksGUI.getSelectedResourcePacks().add(j, this);
				} else {
					String s = I18n.format("resourcePack.incompatible.confirm.title");
					String s1 = I18n.format("resourcePack.incompatible.confirm." + (l > 3 ? "new" : "old"));
					mc.displayGuiScreen(new GuiYesNo((result, id) -> {

						List<ResourcePackListEntry> list2 = resourcePacksGUI.getListContaining(ResourcePackListEntry.this);
						mc.displayGuiScreen(resourcePacksGUI);

						if (result) {
							list2.remove(ResourcePackListEntry.this);
							resourcePacksGUI.getSelectedResourcePacks().add(j, ResourcePackListEntry.this);
						}
					}, s, s1, 0));
				}

				return true;
			}

			if (relativeX < 16 && canMoveLeft()) {
				resourcePacksGUI.getListContaining(this).remove(this);
				resourcePacksGUI.getAvailableResourcePacks().addFirst(this);
				resourcePacksGUI.markChanged();
				return true;
			}

			if (relativeX > 16 && relativeY < 16 && canMoveUp()) {
				List<ResourcePackListEntry> list1 = resourcePacksGUI.getListContaining(this);
				int k = list1.indexOf(this);
				list1.remove(this);
				list1.add(k - 1, this);
				resourcePacksGUI.markChanged();
				return true;
			}

			if (relativeX > 16 && relativeY > 16 && canMoveDown()) {
				List<ResourcePackListEntry> list = resourcePacksGUI.getListContaining(this);
				int i = list.indexOf(this);
				list.remove(this);
				list.add(i + 1, this);
				resourcePacksGUI.markChanged();
				return true;
			}
		}

		return false;
	}

	public void updatePosition(int slotIndex, int x, int y, float partialTicks) {

	}

	/**
	 * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
	 */
	public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {

	}

	public boolean isServerPack() {

		return false;
	}

}
