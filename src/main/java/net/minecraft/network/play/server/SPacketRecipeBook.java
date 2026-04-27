package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.io.IOException;
import java.util.List;

public class SPacketRecipeBook implements Packet<INetHandlerPlayClient> {

	private SPacketRecipeBook.State state;
	private List<IRecipe> recipes;
	private List<IRecipe> displayedRecipes;
	private boolean guiOpen;
	private boolean filteringCraftable;

	public SPacketRecipeBook() {

	}

	public SPacketRecipeBook(SPacketRecipeBook.State stateIn, List<IRecipe> recipesIn, List<IRecipe> displayedRecipesIn, boolean isGuiOpen, boolean p_i47597_5_) {

		state = stateIn;
		recipes = recipesIn;
		displayedRecipes = displayedRecipesIn;
		guiOpen = isGuiOpen;
		filteringCraftable = p_i47597_5_;
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handleRecipeBook(this);
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		state = buf.readEnumValue(State.class);
		guiOpen = buf.readBoolean();
		filteringCraftable = buf.readBoolean();
		int i = buf.readVarInt();
		recipes = Lists.newArrayList();

		for (int j = 0; j < i; ++j) {
			recipes.add(CraftingManager.getRecipeById(buf.readVarInt()));
		}

		if (state == SPacketRecipeBook.State.INIT) {
			i = buf.readVarInt();
			displayedRecipes = Lists.newArrayList();

			for (int k = 0; k < i; ++k) {
				displayedRecipes.add(CraftingManager.getRecipeById(buf.readVarInt()));
			}
		}
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeEnumValue(state);
		buf.writeBoolean(guiOpen);
		buf.writeBoolean(filteringCraftable);
		buf.writeVarInt(recipes.size());

		for (IRecipe irecipe : recipes) {
			buf.writeVarInt(CraftingManager.getIDForRecipe(irecipe));
		}

		if (state == SPacketRecipeBook.State.INIT) {
			buf.writeVarInt(displayedRecipes.size());

			for (IRecipe irecipe1 : displayedRecipes) {
				buf.writeVarInt(CraftingManager.getIDForRecipe(irecipe1));
			}
		}
	}

	public List<IRecipe> getRecipes() {

		return recipes;
	}

	public List<IRecipe> getDisplayedRecipes() {

		return displayedRecipes;
	}

	public boolean isGuiOpen() {

		return guiOpen;
	}

	public boolean isFilteringCraftable() {

		return filteringCraftable;
	}

	public SPacketRecipeBook.State getState() {

		return state;
	}

	public enum State {
		INIT,
		ADD,
		REMOVE
	}

}
