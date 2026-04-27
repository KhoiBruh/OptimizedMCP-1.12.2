package net.minecraft.client.gui.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;

import java.io.IOException;

public class GuiInventory extends InventoryEffectRenderer implements IRecipeShownListener {

	private final GuiRecipeBook recipeBookGui = new GuiRecipeBook();
	/**
	 * The old x position of the mouse pointer
	 */
	private float oldMouseX;
	/**
	 * The old y position of the mouse pointer
	 */
	private float oldMouseY;
	private GuiButtonImage recipeButton;
	private boolean widthTooNarrow;
	private boolean buttonClicked;

	public GuiInventory(EntityPlayer player) {

		super(player.inventoryContainer);
		allowUserInput = true;
	}

	/**
	 * Draws an entity on the screen looking toward the cursor.
	 */
	public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {

		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) posX, (float) posY, 50.0F);
		GlStateManager.scale((float) (-scale), (float) scale, (float) scale);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		float f = ent.renderYawOffset;
		float f1 = ent.rotationYaw;
		float f2 = ent.rotationPitch;
		float f3 = ent.prevRotationYawHead;
		float f4 = ent.rotationYawHead;
		GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-((float) Math.atan(mouseY / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
		ent.renderYawOffset = (float) Math.atan(mouseX / 40.0F) * 20.0F;
		ent.rotationYaw = (float) Math.atan(mouseX / 40.0F) * 40.0F;
		ent.rotationPitch = -((float) Math.atan(mouseY / 40.0F)) * 20.0F;
		ent.rotationYawHead = ent.rotationYaw;
		ent.prevRotationYawHead = ent.rotationYaw;
		GlStateManager.translate(0.0F, 0.0F, 0.0F);
		RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
		rendermanager.setPlayerViewY(180.0F);
		rendermanager.setRenderShadow(false);
		rendermanager.renderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
		rendermanager.setRenderShadow(true);
		ent.renderYawOffset = f;
		ent.rotationYaw = f1;
		ent.rotationPitch = f2;
		ent.prevRotationYawHead = f3;
		ent.rotationYawHead = f4;
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {

		if (mc.playerController.isInCreativeMode()) {
			mc.displayGuiScreen(new GuiContainerCreative(mc.player));
		}

		recipeBookGui.tick();
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

		buttonList.clear();

		if (mc.playerController.isInCreativeMode()) {
			mc.displayGuiScreen(new GuiContainerCreative(mc.player));
		} else {
			super.initGui();
		}

		widthTooNarrow = width < 379;
		recipeBookGui.func_194303_a(width, height, mc, widthTooNarrow, ((ContainerPlayer) inventorySlots).craftMatrix);
		guiLeft = recipeBookGui.updateScreenPosition(widthTooNarrow, width, xSize);
		recipeButton = new GuiButtonImage(10, guiLeft + 104, height / 2 - 22, 20, 18, 178, 0, 19, INVENTORY_BACKGROUND);
		buttonList.add(recipeButton);
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

		fontRenderer.drawString(I18n.format("container.crafting"), 97, 8, 4210752);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		hasActivePotionEffects = !recipeBookGui.isVisible();

		if (recipeBookGui.isVisible() && widthTooNarrow) {
			drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			recipeBookGui.render(mouseX, mouseY, partialTicks);
		} else {
			recipeBookGui.render(mouseX, mouseY, partialTicks);
			super.drawScreen(mouseX, mouseY, partialTicks);
			recipeBookGui.renderGhostRecipe(guiLeft, guiTop, false, partialTicks);
		}

		renderHoveredToolTip(mouseX, mouseY);
		recipeBookGui.renderTooltip(guiLeft, guiTop, mouseX, mouseY);
		oldMouseX = (float) mouseX;
		oldMouseY = (float) mouseY;
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(INVENTORY_BACKGROUND);
		int i = guiLeft;
		int j = guiTop;
		drawTexturedModalRect(i, j, 0, 0, xSize, ySize);
		drawEntityOnScreen(i + 51, j + 75, 30, (float) (i + 51) - oldMouseX, (float) (j + 75 - 50) - oldMouseY, mc.player);
	}

	/**
	 * Test if the 2D point is in a rectangle (relative to the GUI). Args : rectX, rectY, rectWidth, rectHeight, pointX,
	 * pointY
	 */
	protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {

		return (!widthTooNarrow || !recipeBookGui.isVisible()) && super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

		if (!recipeBookGui.mouseClicked(mouseX, mouseY, mouseButton)) {
			if (!widthTooNarrow || !recipeBookGui.isVisible()) {
				super.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
	}

	/**
	 * Called when a mouse button is released.
	 */
	protected void mouseReleased(int mouseX, int mouseY, int state) {

		if (buttonClicked) {
			buttonClicked = false;
		} else {
			super.mouseReleased(mouseX, mouseY, state);
		}
	}

	protected boolean hasClickedOutside(int p_193983_1_, int p_193983_2_, int p_193983_3_, int p_193983_4_) {

		boolean flag = p_193983_1_ < p_193983_3_ || p_193983_2_ < p_193983_4_ || p_193983_1_ >= p_193983_3_ + xSize || p_193983_2_ >= p_193983_4_ + ySize;
		return recipeBookGui.hasClickedOutside(p_193983_1_, p_193983_2_, guiLeft, guiTop, xSize, ySize) && flag;
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void actionPerformed(GuiButton button) {

		if (button.id == 10) {
			recipeBookGui.initVisuals(widthTooNarrow, ((ContainerPlayer) inventorySlots).craftMatrix);
			recipeBookGui.toggleVisibility();
			guiLeft = recipeBookGui.updateScreenPosition(widthTooNarrow, width, xSize);
			recipeButton.setPosition(guiLeft + 104, height / 2 - 22);
			buttonClicked = true;
		}
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) throws IOException {

		if (!recipeBookGui.keyPressed(typedChar, keyCode)) {
			super.keyTyped(typedChar, keyCode);
		}
	}

	/**
	 * Called when the mouse is clicked over a slot or outside the gui.
	 */
	protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {

		super.handleMouseClick(slotIn, slotId, mouseButton, type);
		recipeBookGui.slotClicked(slotIn);
	}

	public void recipesUpdated() {

		recipeBookGui.recipesUpdated();
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void onGuiClosed() {

		recipeBookGui.removed();
		super.onGuiClosed();
	}

	public GuiRecipeBook func_194310_f() {

		return recipeBookGui;
	}

}
