package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class GuiScreenResourcePacks extends GuiScreen {

	private final GuiScreen parentScreen;
	private List<ResourcePackListEntry> availableResourcePacks;
	private List<ResourcePackListEntry> selectedResourcePacks;

	/**
	 * List component that contains the available resource packs
	 */
	private GuiResourcePackAvailable availableResourcePacksList;

	/**
	 * List component that contains the selected resource packs
	 */
	private GuiResourcePackSelected selectedResourcePacksList;
	private boolean changed;

	public GuiScreenResourcePacks(GuiScreen parentScreenIn) {

		parentScreen = parentScreenIn;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

		buttonList.add(new GuiOptionButton(2, width / 2 - 154, height - 48, I18n.format("resourcePack.openFolder")));
		buttonList.add(new GuiOptionButton(1, width / 2 + 4, height - 48, I18n.format("gui.done")));

		if (!changed) {
			availableResourcePacks = Lists.newArrayList();
			selectedResourcePacks = Lists.newArrayList();
			ResourcePackRepository resourcepackrepository = mc.getResourcePackRepository();
			resourcepackrepository.updateRepositoryEntriesAll();
			List<ResourcePackRepository.Entry> list = Lists.newArrayList(resourcepackrepository.getRepositoryEntriesAll());
			list.removeAll(resourcepackrepository.getRepositoryEntries());

			for (ResourcePackRepository.Entry resourcepackrepository$entry : list) {
				availableResourcePacks.add(new ResourcePackListEntryFound(this, resourcepackrepository$entry));
			}

			ResourcePackRepository.Entry resourcepackrepository$entry2 = resourcepackrepository.getResourcePackEntry();

			if (resourcepackrepository$entry2 != null) {
				selectedResourcePacks.add(new ResourcePackListEntryServer(this, resourcepackrepository.getServerResourcePack()));
			}

			for (ResourcePackRepository.Entry resourcepackrepository$entry1 : Lists.reverse(resourcepackrepository.getRepositoryEntries())) {
				selectedResourcePacks.add(new ResourcePackListEntryFound(this, resourcepackrepository$entry1));
			}

			selectedResourcePacks.add(new ResourcePackListEntryDefault(this));
		}

		availableResourcePacksList = new GuiResourcePackAvailable(mc, 200, height, availableResourcePacks);
		availableResourcePacksList.setSlotXBoundsFromLeft(width / 2 - 4 - 200);
		availableResourcePacksList.registerScrollButtons(7, 8);
		selectedResourcePacksList = new GuiResourcePackSelected(mc, 200, height, selectedResourcePacks);
		selectedResourcePacksList.setSlotXBoundsFromLeft(width / 2 + 4);
		selectedResourcePacksList.registerScrollButtons(7, 8);
	}

	/**
	 * Handles mouse input.
	 */
	public void handleMouseInput() throws IOException {

		super.handleMouseInput();
		selectedResourcePacksList.handleMouseInput();
		availableResourcePacksList.handleMouseInput();
	}

	public boolean hasResourcePackEntry(ResourcePackListEntry resourcePackEntry) {

		return selectedResourcePacks.contains(resourcePackEntry);
	}

	public List<ResourcePackListEntry> getListContaining(ResourcePackListEntry resourcePackEntry) {

		return hasResourcePackEntry(resourcePackEntry) ? selectedResourcePacks : availableResourcePacks;
	}

	public List<ResourcePackListEntry> getAvailableResourcePacks() {

		return availableResourcePacks;
	}

	public List<ResourcePackListEntry> getSelectedResourcePacks() {

		return selectedResourcePacks;
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void actionPerformed(GuiButton button) throws IOException {

		if (button.enabled) {
			if (button.id == 2) {
				File file1 = mc.getResourcePackRepository().getDirResourcepacks();
				OpenGlHelper.openFile(file1);
			} else if (button.id == 1) {
				if (changed) {
					List<ResourcePackRepository.Entry> list = Lists.newArrayList();

					for (ResourcePackListEntry resourcepacklistentry : selectedResourcePacks) {
						if (resourcepacklistentry instanceof ResourcePackListEntryFound) {
							list.add(((ResourcePackListEntryFound) resourcepacklistentry).getResourcePackEntry());
						}
					}

					Collections.reverse(list);
					mc.getResourcePackRepository().setRepositories(list);
					mc.gameSettings.resourcePacks.clear();
					mc.gameSettings.incompatibleResourcePacks.clear();

					for (ResourcePackRepository.Entry resourcepackrepository$entry : list) {
						mc.gameSettings.resourcePacks.add(resourcepackrepository$entry.getResourcePackName());

						if (resourcepackrepository$entry.getPackFormat() != 3) {
							mc.gameSettings.incompatibleResourcePacks.add(resourcepackrepository$entry.getResourcePackName());
						}
					}

					mc.gameSettings.saveOptions();
					mc.refreshResources();
				}

				mc.displayGuiScreen(parentScreen);
			}
		}
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

		super.mouseClicked(mouseX, mouseY, mouseButton);
		availableResourcePacksList.mouseClicked(mouseX, mouseY, mouseButton);
		selectedResourcePacksList.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Called when a mouse button is released.
	 */
	protected void mouseReleased(int mouseX, int mouseY, int state) {

		super.mouseReleased(mouseX, mouseY, state);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		drawBackground(0);
		availableResourcePacksList.drawScreen(mouseX, mouseY, partialTicks);
		selectedResourcePacksList.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, I18n.format("resourcePack.title"), width / 2, 16, 16777215);
		drawCenteredString(fontRenderer, I18n.format("resourcePack.folderInfo"), width / 2 - 77, height - 26, 8421504);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	/**
	 * Marks the selected resource packs list as changed to trigger a resource reload when the screen is closed
	 */
	public void markChanged() {

		changed = true;
	}

}
