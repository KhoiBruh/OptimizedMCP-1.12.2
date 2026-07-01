package net.minecraft.client.gui.game;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.ContainerScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public class HopperScreen extends ContainerScreen {

	/**
	 * The ResourceLocation containing the gui texture for the hopper
	 */
	private static final ResourceLocation HOPPER_GUI_TEXTURE = new ResourceLocation("textures/gui/container/hopper.png");

	/**
	 * The player inventory currently bound to this GUI instance
	 */
	private final IInventory playerInventory;

	/**
	 * The hopper inventory bound to this GUI instance
	 */
	private final IInventory hopperInventory;

	public HopperScreen(InventoryPlayer playerInv, IInventory hopperInv) {

		super(new ContainerHopper(playerInv, hopperInv, Minecraft.getMinecraft().player));
		playerInventory = playerInv;
		hopperInventory = hopperInv;
		allowUserInput = false;
		ySize = 133;
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

		fontRenderer.drawString(hopperInventory.displayName().getUnformattedText(), 8, 6, 4210752);
		fontRenderer.drawString(playerInventory.displayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

		GlStateManager.color(1F, 1F, 1F, 1F);
		mc.getTextureManager().bindTexture(HOPPER_GUI_TEXTURE);
		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;
		drawTexturedModalRect(i, j, 0, 0, xSize, ySize);
	}

}
