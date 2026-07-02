package net.minecraft.client.resources;

import net.minecraft.client.gui.menu.ResourcePacksScreen;

public class ResourcePackListEntryFound extends ResourcePackListEntry {

	private final ResourcePackRepository.Entry resourcePackEntry;

	public ResourcePackListEntryFound(ResourcePacksScreen resourcePacksGUIIn, ResourcePackRepository.Entry entry) {
		super(resourcePacksGUIIn);
		resourcePackEntry = entry;
	}

	protected void bindResourcePackIcon() {
		resourcePackEntry.bindTexturePackIcon(mc.getTextureManager());
	}

	protected int getResourcePackFormat() {
		return resourcePackEntry.getPackFormat();
	}

	protected String getResourcePackDescription() {
		return resourcePackEntry.getTexturePackDescription();
	}

	protected String getResourcePackName() {
		return resourcePackEntry.getResourcePackName();
	}

	public ResourcePackRepository.Entry getResourcePackEntry() {

		return resourcePackEntry;
	}

}
