package net.minecraft.client.gui.inventory;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.renderer.GLS;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public class DispenserScreen extends ContainerScreen {

	private static final ResourceLocation DISPENSER_GUI_TEXTURES = new ResourceLocation("textures/gui/container/dispenser.png");

	/**
	 * The player inventory bound to this GUI.
	 */
	private final InventoryPlayer playerInventory;

	/**
	 * The inventory contained within the corresponding Dispenser.
	 */
	public IInventory dispenserInventory;

	public DispenserScreen(InventoryPlayer playerInv, IInventory dispenserInv) {
		super(new ContainerDispenser(playerInv, dispenserInv));
		playerInventory = playerInv;
		dispenserInventory = dispenserInv;
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(DrawContext context, int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.draw(context, mouseX, mouseY, partialTicks);
		renderHoveredToolTip(context, mouseX, mouseY);
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	protected void drawGuiContainerForegroundLayer(DrawContext context, int mouseX, int mouseY) {
		String s = dispenserInventory.displayName().getUnformattedText();
		fontRenderer.drawText(s, xSize / 2 - fontRenderer.getWidth(s) / 2, 6, 4210752);
		fontRenderer.drawText(playerInventory.displayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	protected void drawGuiContainerBackgroundLayer(DrawContext context, int mouseX, int mouseY, float partialTicks) {
		GLS.color(1F, 1F, 1F, 1F);
		mc.getTextureManager().bindTexture(DISPENSER_GUI_TEXTURES);
		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;
		context.blit(i, j, 0, 0, xSize, ySize);
	}

}
