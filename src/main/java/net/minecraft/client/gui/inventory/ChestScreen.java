package net.minecraft.client.gui.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLS;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public class ChestScreen extends ContainerScreen {

	/**
	 * The ResourceLocation containing the chest GUI texture.
	 */
	private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
	private final IInventory upperChestInventory;
	private final IInventory lowerChestInventory;

	/**
	 * window height is calculated with these values; the more rows, the heigher
	 */
	private final int inventoryRows;

	public ChestScreen(IInventory upperInv, IInventory lowerInv) {
		super(new ContainerChest(upperInv, lowerInv, Minecraft.getMinecraft().player));
		upperChestInventory = upperInv;
		lowerChestInventory = lowerInv;
		allowInput = false;
		int i = 222;
		int j = 114;
		inventoryRows = lowerInv.getSizeInventory() / 9;
		ySize = 114 + inventoryRows * 18;
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.draw(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRenderer.drawText(lowerChestInventory.displayName().getUnformattedText(), 8, 6, 4210752);
		fontRenderer.drawText(upperChestInventory.displayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GLS.color(1F, 1F, 1F, 1F);
		mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;
		drawTexturedModalRect(i, j, 0, 0, xSize, inventoryRows * 18 + 17);
		drawTexturedModalRect(i, j + inventoryRows * 18 + 17, 0, 126, xSize, 96);
	}

}
