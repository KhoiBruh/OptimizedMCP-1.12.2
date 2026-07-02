package net.minecraft.client.gui.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLS;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public class HorseInventoryScreen extends ContainerScreen {

	private static final ResourceLocation HORSE_GUI_TEXTURES = new ResourceLocation("textures/gui/container/horse.png");

	/**
	 * The player inventory bound to this GUI.
	 */
	private final IInventory playerInventory;

	/**
	 * The horse inventory bound to this GUI.
	 */
	private final IInventory horseInventory;

	/**
	 * The EntityHorse whose inventory is currently being accessed.
	 */
	private final AbstractHorse horseEntity;

	/**
	 * The mouse x-position recorded during the last rendered frame.
	 */
	private float mousePosx;

	/**
	 * The mouse y-position recorded during the last renderered frame.
	 */
	private float mousePosY;

	public HorseInventoryScreen(IInventory playerInv, IInventory horseInv, AbstractHorse horse) {
		super(new ContainerHorseInventory(playerInv, horseInv, horse, Minecraft.getMinecraft().player));
		playerInventory = playerInv;
		horseInventory = horseInv;
		horseEntity = horse;
		allowInput = false;
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRenderer.drawText(horseInventory.displayName().getUnformattedText(), 8, 6, 4210752);
		fontRenderer.drawText(playerInventory.displayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GLS.color(1F, 1F, 1F, 1F);
		mc.getTextureManager().bindTexture(HORSE_GUI_TEXTURES);
		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;
		drawTexturedModalRect(i, j, 0, 0, xSize, ySize);

		if (horseEntity instanceof AbstractChestHorse abstractchesthorse) {
			if (abstractchesthorse.hasChest()) {
				drawTexturedModalRect(i + 79, j + 17, 0, ySize, abstractchesthorse.getInventoryColumns() * 18, 54);
			}
		}

		if (horseEntity.canBeSaddled()) {
			drawTexturedModalRect(i + 7, j + 35 - 18, 18, ySize + 54, 18, 18);
		}

		if (horseEntity.wearsArmor()) {
			if (horseEntity instanceof EntityLlama) {
				drawTexturedModalRect(i + 7, j + 35, 36, ySize + 54, 18, 18);
			} else {
				drawTexturedModalRect(i + 7, j + 35, 0, ySize + 54, 18, 18);
			}
		}

		InventoryScreen.drawEntityOnScreen(i + 51, j + 60, 17, (float) (i + 51) - mousePosx, (float) (j + 75 - 50) - mousePosY, horseEntity);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		mousePosx = (float) mouseX;
		mousePosY = (float) mouseY;
		super.draw(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}

}
