package net.minecraft.client.resources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.menu.ResourcePacksScreen;

public class ResourcePackListEntryDefault extends ResourcePackListEntryServer {

	public ResourcePackListEntryDefault(ResourcePacksScreen resourcePacksGUIIn) {

		super(resourcePacksGUIIn, Minecraft.getMinecraft().getResourcePackRepository().rprDefaultResourcePack);
	}

	protected String getResourcePackName() {

		return "Default";
	}

	public boolean isServerPack() {

		return false;
	}

}
