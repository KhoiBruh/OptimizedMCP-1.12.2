package net.minecraft.client.gui.spectator;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.spectator.categories.TeleportToPlayer;
import net.minecraft.client.gui.spectator.categories.TeleportToTeam;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class BaseSpectatorGroup implements ISpectatorMenuView {

	private final List<ISpectatorMenuObject> items = Lists.newArrayList();

	public BaseSpectatorGroup() {

		items.add(new TeleportToPlayer());
		items.add(new TeleportToTeam());
	}

	public List<ISpectatorMenuObject> getItems() {

		return items;
	}

	public ITextComponent getPrompt() {

		return new TextComponentTranslation("spectatorMenu.root.prompt");
	}

}
