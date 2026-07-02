package net.minecraft.client.gui.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.component.Button;
import net.minecraft.client.gui.component.ImageButton;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.renderer.GLS;
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

public class InventoryScreen extends InventoryEffectRenderer implements IRecipeShownListener {

	private final GuiRecipeBook recipeBookGui = new GuiRecipeBook();
	/**
	 * The old x position of the mouse pointer
	 */
	private float oldMouseX;
	/**
	 * The old y position of the mouse pointer
	 */
	private float oldMouseY;
	private ImageButton recipeButton;
	private boolean widthTooNarrow;
	private boolean buttonClicked;

	public InventoryScreen(EntityPlayer player) {
		super(player.inventoryContainer);
		allowInput = true;
	}

	/**
	 * Draws an entity on the screen looking toward the cursor.
	 */
	public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {
		GLS.enableColorMaterial();
		GLS.pushMatrix();
		GLS.translate((float) posX, (float) posY, 50F);
		GLS.scale((float) (-scale), (float) scale, (float) scale);
		GLS.rotate(180F, 0F, 0F, 1F);
		float f = ent.renderYawOffset;
		float f1 = ent.rotationYaw;
		float f2 = ent.rotationPitch;
		float f3 = ent.prevRotationYawHead;
		float f4 = ent.rotationYawHead;
		GLS.rotate(135F, 0F, 1F, 0F);
		RenderHelper.enableStandardItemLighting();
		GLS.rotate(-135F, 0F, 1F, 0F);
		GLS.rotate(-((float) Math.atan(mouseY / 40F)) * 20F, 1F, 0F, 0F);
		ent.renderYawOffset = (float) Math.atan(mouseX / 40F) * 20F;
		ent.rotationYaw = (float) Math.atan(mouseX / 40F) * 40F;
		ent.rotationPitch = -((float) Math.atan(mouseY / 40F)) * 20F;
		ent.rotationYawHead = ent.rotationYaw;
		ent.prevRotationYawHead = ent.rotationYaw;
		GLS.translate(0F, 0F, 0F);
		RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
		rendermanager.setPlayerViewY(180F);
		rendermanager.setRenderShadow(false);
		rendermanager.renderEntity(ent, 0D, 0D, 0D, 0F, 1F, false);
		rendermanager.setRenderShadow(true);
		ent.renderYawOffset = f;
		ent.rotationYaw = f1;
		ent.rotationPitch = f2;
		ent.prevRotationYawHead = f3;
		ent.rotationYawHead = f4;
		GLS.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GLS.disableRescaleNormal();
		GLS.activeTexture(OpenGlHelper.lightmapTexUnit);
		GLS.disableTexture2D();
		GLS.activeTexture(OpenGlHelper.defaultTexUnit);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void update() {
		if (mc.playerController.isInCreativeMode()) {
			mc.displayScreen(new CreativeContainerScreen(mc.player));
		}

		recipeBookGui.tick();
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {
		buttons.clear();

		if (mc.playerController.isInCreativeMode()) {
			mc.displayScreen(new CreativeContainerScreen(mc.player));
		} else {
			super.init();
		}

		widthTooNarrow = width < 379;
		recipeBookGui.func_194303_a(width, height, mc, widthTooNarrow, ((ContainerPlayer) inventorySlots).craftMatrix);
		guiLeft = recipeBookGui.updateScreenPosition(widthTooNarrow, width, xSize);
		recipeButton = new ImageButton(10, guiLeft + 104, height / 2 - 22, 20, 18, 178, 0, 19, INVENTORY_BACKGROUND);
		buttons.add(recipeButton);
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRenderer.drawText(I18n.format("container.crafting"), 97, 8, 4210752);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(DrawContext context, int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		hasActivePotionEffects = !recipeBookGui.isVisible();

		if (recipeBookGui.isVisible() && widthTooNarrow) {
			drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			recipeBookGui.render(mouseX, mouseY, partialTicks);
		} else {
			recipeBookGui.render(mouseX, mouseY, partialTicks);
			super.draw(context, mouseX, mouseY, partialTicks);
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
		GLS.color(1F, 1F, 1F, 1F);
		mc.getTextureManager().bindTexture(INVENTORY_BACKGROUND);
		int i = guiLeft;
		int j = guiTop;
		mc.getDrawContext().blit(i, j, 0, 0, xSize, ySize);
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
	protected void mouseClicked(int mouseX, int mouseY, int mouse) throws IOException {
		if (!recipeBookGui.mouseClicked(mouseX, mouseY, mouse)) {
			if (!widthTooNarrow || !recipeBookGui.isVisible()) {
				super.mouseClicked(mouseX, mouseY, mouse);
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
	protected void action(Button button) {
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
	public void close() {
		recipeBookGui.removed();
		super.close();
	}

	public GuiRecipeBook func_194310_f() {
		return recipeBookGui;
	}

}
