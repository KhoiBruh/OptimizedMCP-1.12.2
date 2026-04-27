package net.minecraft.client.gui.inventory;

import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Set;

public abstract class GuiContainer extends GuiScreen {

	/**
	 * The location of the inventory background texture
	 */
	public static final ResourceLocation INVENTORY_BACKGROUND = new ResourceLocation("textures/gui/container/inventory.png");
	protected final Set<Slot> dragSplittingSlots = Sets.newHashSet();
	/**
	 * A list of the players inventory slots
	 */
	public Container inventorySlots;
	/**
	 * The X size of the inventory window in pixels.
	 */
	protected int xSize = 176;
	/**
	 * The Y size of the inventory window in pixels.
	 */
	protected int ySize = 166;
	/**
	 * Starting X position for the Gui. Inconsistent use for Gui backgrounds.
	 */
	protected int guiLeft;
	/**
	 * Starting Y position for the Gui. Inconsistent use for Gui backgrounds.
	 */
	protected int guiTop;
	protected boolean dragSplitting;
	/**
	 * holds the slot currently hovered
	 */
	private Slot hoveredSlot;
	/**
	 * Used when touchscreen is enabled.
	 */
	private Slot clickedSlot;
	/**
	 * Used when touchscreen is enabled.
	 */
	private boolean isRightMouseClick;
	/**
	 * Used when touchscreen is enabled
	 */
	private ItemStack draggedStack = ItemStack.EMPTY;
	private int touchUpX;
	private int touchUpY;
	private Slot returningStackDestSlot;
	private long returningStackTime;
	/**
	 * Used when touchscreen is enabled
	 */
	private ItemStack returningStack = ItemStack.EMPTY;
	private Slot currentDragTargetSlot;
	private long dragItemDropDelay;
	private int dragSplittingLimit;
	private int dragSplittingButton;
	private boolean ignoreMouseUp;
	private int dragSplittingRemnant;
	private long lastClickTime;
	private Slot lastClickSlot;
	private int lastClickButton;
	private boolean doubleClick;
	private ItemStack shiftClickedSlot = ItemStack.EMPTY;

	public GuiContainer(Container inventorySlotsIn) {

		inventorySlots = inventorySlotsIn;
		ignoreMouseUp = true;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

		super.initGui();
		mc.player.openContainer = inventorySlots;
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		int i = guiLeft;
		int j = guiTop;
		drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		super.drawScreen(mouseX, mouseY, partialTicks);
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) i, (float) j, 0.0F);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableRescaleNormal();
		hoveredSlot = null;
		int k = 240;
		int l = 240;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		for (int i1 = 0; i1 < inventorySlots.inventorySlots.size(); ++i1) {
			Slot slot = inventorySlots.inventorySlots.get(i1);

			if (slot.isEnabled()) {
				drawSlot(slot);
			}

			if (isMouseOverSlot(slot, mouseX, mouseY) && slot.isEnabled()) {
				hoveredSlot = slot;
				GlStateManager.disableLighting();
				GlStateManager.disableDepth();
				int j1 = slot.xPos;
				int k1 = slot.yPos;
				GlStateManager.colorMask(true, true, true, false);
				drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
				GlStateManager.colorMask(true, true, true, true);
				GlStateManager.enableLighting();
				GlStateManager.enableDepth();
			}
		}

		RenderHelper.disableStandardItemLighting();
		drawGuiContainerForegroundLayer(mouseX, mouseY);
		RenderHelper.enableGUIStandardItemLighting();
		InventoryPlayer inventoryplayer = mc.player.inventory;
		ItemStack itemstack = draggedStack.isEmpty() ? inventoryplayer.getItemStack() : draggedStack;

		if (!itemstack.isEmpty()) {
			int j2 = 8;
			int k2 = draggedStack.isEmpty() ? 8 : 16;
			String s = null;

			if (!draggedStack.isEmpty() && isRightMouseClick) {
				itemstack = itemstack.copy();
				itemstack.setCount(MathHelper.ceil((float) itemstack.getCount() / 2.0F));
			} else if (dragSplitting && dragSplittingSlots.size() > 1) {
				itemstack = itemstack.copy();
				itemstack.setCount(dragSplittingRemnant);

				if (itemstack.isEmpty()) {
					s = TextFormatting.YELLOW + "0";
				}
			}

			drawItemStack(itemstack, mouseX - i - 8, mouseY - j - k2, s);
		}

		if (!returningStack.isEmpty()) {
			float f = (float) (Minecraft.getSystemTime() - returningStackTime) / 100.0F;

			if (f >= 1.0F) {
				f = 1.0F;
				returningStack = ItemStack.EMPTY;
			}

			int l2 = returningStackDestSlot.xPos - touchUpX;
			int i3 = returningStackDestSlot.yPos - touchUpY;
			int l1 = touchUpX + (int) ((float) l2 * f);
			int i2 = touchUpY + (int) ((float) i3 * f);
			drawItemStack(returningStack, l1, i2, null);
		}

		GlStateManager.popMatrix();
		GlStateManager.enableLighting();
		GlStateManager.enableDepth();
		RenderHelper.enableStandardItemLighting();
	}

	protected void renderHoveredToolTip(int p_191948_1_, int p_191948_2_) {

		if (mc.player.inventory.getItemStack().isEmpty() && hoveredSlot != null && hoveredSlot.getHasStack()) {
			renderToolTip(hoveredSlot.getStack(), p_191948_1_, p_191948_2_);
		}
	}

	/**
	 * Draws an ItemStack.
	 * <p>
	 * The z index is increased by 32 (and not decreased afterwards), and the item is then rendered at z=200.
	 */
	private void drawItemStack(ItemStack stack, int x, int y, String altText) {

		GlStateManager.translate(0.0F, 0.0F, 32.0F);
		zLevel = 200.0F;
		itemRender.zLevel = 200.0F;
		itemRender.renderItemAndEffectIntoGUI(stack, x, y);
		itemRender.renderItemOverlayIntoGUI(fontRenderer, stack, x, y - (draggedStack.isEmpty() ? 0 : 8), altText);
		zLevel = 0.0F;
		itemRender.zLevel = 0.0F;
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	protected abstract void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY);

	/**
	 * Draws the given slot: any item in it, the slot's background, the hovered highlight, etc.
	 */
	private void drawSlot(Slot slotIn) {

		int i = slotIn.xPos;
		int j = slotIn.yPos;
		ItemStack itemstack = slotIn.getStack();
		boolean flag = false;
		boolean flag1 = slotIn == clickedSlot && !draggedStack.isEmpty() && !isRightMouseClick;
		ItemStack itemstack1 = mc.player.inventory.getItemStack();
		String s = null;

		if (slotIn == clickedSlot && !draggedStack.isEmpty() && isRightMouseClick && !itemstack.isEmpty()) {
			itemstack = itemstack.copy();
			itemstack.setCount(itemstack.getCount() / 2);
		} else if (dragSplitting && dragSplittingSlots.contains(slotIn) && !itemstack1.isEmpty()) {
			if (dragSplittingSlots.size() == 1) {
				return;
			}

			if (Container.canAddItemToSlot(slotIn, itemstack1, true) && inventorySlots.canDragIntoSlot(slotIn)) {
				itemstack = itemstack1.copy();
				flag = true;
				Container.computeStackSize(dragSplittingSlots, dragSplittingLimit, itemstack, slotIn.getStack().isEmpty() ? 0 : slotIn.getStack().getCount());
				int k = Math.min(itemstack.getMaxStackSize(), slotIn.getItemStackLimit(itemstack));

				if (itemstack.getCount() > k) {
					s = TextFormatting.YELLOW.toString() + k;
					itemstack.setCount(k);
				}
			} else {
				dragSplittingSlots.remove(slotIn);
				updateDragSplitting();
			}
		}

		zLevel = 100.0F;
		itemRender.zLevel = 100.0F;

		if (itemstack.isEmpty() && slotIn.isEnabled()) {
			String s1 = slotIn.getSlotTexture();

			if (s1 != null) {
				TextureAtlasSprite textureatlassprite = mc.getTextureMapBlocks().getAtlasSprite(s1);
				GlStateManager.disableLighting();
				mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				drawTexturedModalRect(i, j, textureatlassprite, 16, 16);
				GlStateManager.enableLighting();
				flag1 = true;
			}
		}

		if (!flag1) {
			if (flag) {
				drawRect(i, j, i + 16, j + 16, -2130706433);
			}

			GlStateManager.enableDepth();
			itemRender.renderItemAndEffectIntoGUI(mc.player, itemstack, i, j);
			itemRender.renderItemOverlayIntoGUI(fontRenderer, itemstack, i, j, s);
		}

		itemRender.zLevel = 0.0F;
		zLevel = 0.0F;
	}

	private void updateDragSplitting() {

		ItemStack itemstack = mc.player.inventory.getItemStack();

		if (!itemstack.isEmpty() && dragSplitting) {
			if (dragSplittingLimit == 2) {
				dragSplittingRemnant = itemstack.getMaxStackSize();
			} else {
				dragSplittingRemnant = itemstack.getCount();

				for (Slot slot : dragSplittingSlots) {
					ItemStack itemstack1 = itemstack.copy();
					ItemStack itemstack2 = slot.getStack();
					int i = itemstack2.isEmpty() ? 0 : itemstack2.getCount();
					Container.computeStackSize(dragSplittingSlots, dragSplittingLimit, itemstack1, i);
					int j = Math.min(itemstack1.getMaxStackSize(), slot.getItemStackLimit(itemstack1));

					if (itemstack1.getCount() > j) {
						itemstack1.setCount(j);
					}

					dragSplittingRemnant -= itemstack1.getCount() - i;
				}
			}
		}
	}

	/**
	 * Returns the slot at the given coordinates or null if there is none.
	 */
	private Slot getSlotAtPosition(int x, int y) {

		for (int i = 0; i < inventorySlots.inventorySlots.size(); ++i) {
			Slot slot = inventorySlots.inventorySlots.get(i);

			if (isMouseOverSlot(slot, x, y) && slot.isEnabled()) {
				return slot;
			}
		}

		return null;
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

		super.mouseClicked(mouseX, mouseY, mouseButton);
		boolean flag = mouseButton == mc.gameSettings.keyBindPickBlock.getKeyCode() + 100;
		Slot slot = getSlotAtPosition(mouseX, mouseY);
		long i = Minecraft.getSystemTime();
		doubleClick = lastClickSlot == slot && i - lastClickTime < 250L && lastClickButton == mouseButton;
		ignoreMouseUp = false;

		if (mouseButton == 0 || mouseButton == 1 || flag) {
			int j = guiLeft;
			int k = guiTop;
			boolean flag1 = hasClickedOutside(mouseX, mouseY, j, k);
			int l = -1;

			if (slot != null) {
				l = slot.slotNumber;
			}

			if (flag1) {
				l = -999;
			}

			if (mc.gameSettings.touchscreen && flag1 && mc.player.inventory.getItemStack().isEmpty()) {
				mc.displayGuiScreen(null);
				return;
			}

			if (l != -1) {
				if (mc.gameSettings.touchscreen) {
					if (slot != null && slot.getHasStack()) {
						clickedSlot = slot;
						draggedStack = ItemStack.EMPTY;
						isRightMouseClick = mouseButton == 1;
					} else {
						clickedSlot = null;
					}
				} else if (!dragSplitting) {
					if (mc.player.inventory.getItemStack().isEmpty()) {
						if (mouseButton == mc.gameSettings.keyBindPickBlock.getKeyCode() + 100) {
							handleMouseClick(slot, l, mouseButton, ClickType.CLONE);
						} else {
							boolean flag2 = l != -999 && (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54));
							ClickType clicktype = ClickType.PICKUP;

							if (flag2) {
								shiftClickedSlot = slot != null && slot.getHasStack() ? slot.getStack().copy() : ItemStack.EMPTY;
								clicktype = ClickType.QUICK_MOVE;
							} else if (l == -999) {
								clicktype = ClickType.THROW;
							}

							handleMouseClick(slot, l, mouseButton, clicktype);
						}

						ignoreMouseUp = true;
					} else {
						dragSplitting = true;
						dragSplittingButton = mouseButton;
						dragSplittingSlots.clear();

						if (mouseButton == 0) {
							dragSplittingLimit = 0;
						} else if (mouseButton == 1) {
							dragSplittingLimit = 1;
						} else if (mouseButton == mc.gameSettings.keyBindPickBlock.getKeyCode() + 100) {
							dragSplittingLimit = 2;
						}
					}
				}
			}
		}

		lastClickSlot = slot;
		lastClickTime = i;
		lastClickButton = mouseButton;
	}

	protected boolean hasClickedOutside(int p_193983_1_, int p_193983_2_, int p_193983_3_, int p_193983_4_) {

		return p_193983_1_ < p_193983_3_ || p_193983_2_ < p_193983_4_ || p_193983_1_ >= p_193983_3_ + xSize || p_193983_2_ >= p_193983_4_ + ySize;
	}

	/**
	 * Called when a mouse button is pressed and the mouse is moved around. Parameters are : mouseX, mouseY,
	 * lastButtonClicked & timeSinceMouseClick.
	 */
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {

		Slot slot = getSlotAtPosition(mouseX, mouseY);
		ItemStack itemstack = mc.player.inventory.getItemStack();

		if (clickedSlot != null && mc.gameSettings.touchscreen) {
			if (clickedMouseButton == 0 || clickedMouseButton == 1) {
				if (draggedStack.isEmpty()) {
					if (slot != clickedSlot && !clickedSlot.getStack().isEmpty()) {
						draggedStack = clickedSlot.getStack().copy();
					}
				} else if (draggedStack.getCount() > 1 && slot != null && Container.canAddItemToSlot(slot, draggedStack, false)) {
					long i = Minecraft.getSystemTime();

					if (currentDragTargetSlot == slot) {
						if (i - dragItemDropDelay > 500L) {
							handleMouseClick(clickedSlot, clickedSlot.slotNumber, 0, ClickType.PICKUP);
							handleMouseClick(slot, slot.slotNumber, 1, ClickType.PICKUP);
							handleMouseClick(clickedSlot, clickedSlot.slotNumber, 0, ClickType.PICKUP);
							dragItemDropDelay = i + 750L;
							draggedStack.shrink(1);
						}
					} else {
						currentDragTargetSlot = slot;
						dragItemDropDelay = i;
					}
				}
			}
		} else if (dragSplitting && slot != null && !itemstack.isEmpty() && (itemstack.getCount() > dragSplittingSlots.size() || dragSplittingLimit == 2) && Container.canAddItemToSlot(slot, itemstack, true) && slot.isItemValid(itemstack) && inventorySlots.canDragIntoSlot(slot)) {
			dragSplittingSlots.add(slot);
			updateDragSplitting();
		}
	}

	/**
	 * Called when a mouse button is released.
	 */
	protected void mouseReleased(int mouseX, int mouseY, int state) {

		Slot slot = getSlotAtPosition(mouseX, mouseY);
		int i = guiLeft;
		int j = guiTop;
		boolean flag = hasClickedOutside(mouseX, mouseY, i, j);
		int k = -1;

		if (slot != null) {
			k = slot.slotNumber;
		}

		if (flag) {
			k = -999;
		}

		if (doubleClick && slot != null && state == 0 && inventorySlots.canMergeSlot(ItemStack.EMPTY, slot)) {
			if (isShiftKeyDown()) {
				if (!shiftClickedSlot.isEmpty()) {
					for (Slot slot2 : inventorySlots.inventorySlots) {
						if (slot2 != null && slot2.canTakeStack(mc.player) && slot2.getHasStack() && slot2.inventory == slot.inventory && Container.canAddItemToSlot(slot2, shiftClickedSlot, true)) {
							handleMouseClick(slot2, slot2.slotNumber, state, ClickType.QUICK_MOVE);
						}
					}
				}
			} else {
				handleMouseClick(slot, k, state, ClickType.PICKUP_ALL);
			}

			doubleClick = false;
			lastClickTime = 0L;
		} else {
			if (dragSplitting && dragSplittingButton != state) {
				dragSplitting = false;
				dragSplittingSlots.clear();
				ignoreMouseUp = true;
				return;
			}

			if (ignoreMouseUp) {
				ignoreMouseUp = false;
				return;
			}

			if (clickedSlot != null && mc.gameSettings.touchscreen) {
				if (state == 0 || state == 1) {
					if (draggedStack.isEmpty() && slot != clickedSlot) {
						draggedStack = clickedSlot.getStack();
					}

					boolean flag2 = Container.canAddItemToSlot(slot, draggedStack, false);

					if (k != -1 && !draggedStack.isEmpty() && flag2) {
						handleMouseClick(clickedSlot, clickedSlot.slotNumber, state, ClickType.PICKUP);
						handleMouseClick(slot, k, 0, ClickType.PICKUP);

						if (mc.player.inventory.getItemStack().isEmpty()) {
							returningStack = ItemStack.EMPTY;
						} else {
							handleMouseClick(clickedSlot, clickedSlot.slotNumber, state, ClickType.PICKUP);
							touchUpX = mouseX - i;
							touchUpY = mouseY - j;
							returningStackDestSlot = clickedSlot;
							returningStack = draggedStack;
							returningStackTime = Minecraft.getSystemTime();
						}
					} else if (!draggedStack.isEmpty()) {
						touchUpX = mouseX - i;
						touchUpY = mouseY - j;
						returningStackDestSlot = clickedSlot;
						returningStack = draggedStack;
						returningStackTime = Minecraft.getSystemTime();
					}

					draggedStack = ItemStack.EMPTY;
					clickedSlot = null;
				}
			} else if (dragSplitting && !dragSplittingSlots.isEmpty()) {
				handleMouseClick(null, -999, Container.getQuickcraftMask(0, dragSplittingLimit), ClickType.QUICK_CRAFT);

				for (Slot slot1 : dragSplittingSlots) {
					handleMouseClick(slot1, slot1.slotNumber, Container.getQuickcraftMask(1, dragSplittingLimit), ClickType.QUICK_CRAFT);
				}

				handleMouseClick(null, -999, Container.getQuickcraftMask(2, dragSplittingLimit), ClickType.QUICK_CRAFT);
			} else if (!mc.player.inventory.getItemStack().isEmpty()) {
				if (state == mc.gameSettings.keyBindPickBlock.getKeyCode() + 100) {
					handleMouseClick(slot, k, state, ClickType.CLONE);
				} else {
					boolean flag1 = k != -999 && (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54));

					if (flag1) {
						shiftClickedSlot = slot != null && slot.getHasStack() ? slot.getStack().copy() : ItemStack.EMPTY;
					}

					handleMouseClick(slot, k, state, flag1 ? ClickType.QUICK_MOVE : ClickType.PICKUP);
				}
			}
		}

		if (mc.player.inventory.getItemStack().isEmpty()) {
			lastClickTime = 0L;
		}

		dragSplitting = false;
	}

	/**
	 * Returns whether the mouse is over the given slot.
	 */
	private boolean isMouseOverSlot(Slot slotIn, int mouseX, int mouseY) {

		return isPointInRegion(slotIn.xPos, slotIn.yPos, 16, 16, mouseX, mouseY);
	}

	/**
	 * Test if the 2D point is in a rectangle (relative to the GUI). Args : rectX, rectY, rectWidth, rectHeight, pointX,
	 * pointY
	 */
	protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {

		int i = guiLeft;
		int j = guiTop;
		pointX = pointX - i;
		pointY = pointY - j;
		return pointX >= rectX - 1 && pointX < rectX + rectWidth + 1 && pointY >= rectY - 1 && pointY < rectY + rectHeight + 1;
	}

	/**
	 * Called when the mouse is clicked over a slot or outside the gui.
	 */
	protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {

		if (slotIn != null) {
			slotId = slotIn.slotNumber;
		}

		mc.playerController.windowClick(inventorySlots.windowId, slotId, mouseButton, type, mc.player);
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) throws IOException {

		if (keyCode == 1 || keyCode == mc.gameSettings.keyBindInventory.getKeyCode()) {
			mc.player.closeScreen();
		}

		checkHotbarKeys(keyCode);

		if (hoveredSlot != null && hoveredSlot.getHasStack()) {
			if (keyCode == mc.gameSettings.keyBindPickBlock.getKeyCode()) {
				handleMouseClick(hoveredSlot, hoveredSlot.slotNumber, 0, ClickType.CLONE);
			} else if (keyCode == mc.gameSettings.keyBindDrop.getKeyCode()) {
				handleMouseClick(hoveredSlot, hoveredSlot.slotNumber, isCtrlKeyDown() ? 1 : 0, ClickType.THROW);
			}
		}
	}

	/**
	 * Checks whether a hotbar key (to swap the hovered item with an item in the hotbar) has been pressed. If so, it
	 * swaps the given items.
	 * Returns true if a hotbar key was pressed.
	 */
	protected boolean checkHotbarKeys(int keyCode) {

		if (mc.player.inventory.getItemStack().isEmpty() && hoveredSlot != null) {
			for (int i = 0; i < 9; ++i) {
				if (keyCode == mc.gameSettings.keyBindsHotbar[i].getKeyCode()) {
					handleMouseClick(hoveredSlot, hoveredSlot.slotNumber, i, ClickType.SWAP);
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void onGuiClosed() {

		if (mc.player != null) {
			inventorySlots.onContainerClosed(mc.player);
		}
	}

	/**
	 * Returns true if this GUI should pause the game when it is displayed in single-player
	 */
	public boolean doesGuiPauseGame() {

		return false;
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {

		super.updateScreen();

		if (!mc.player.isEntityAlive() || mc.player.isDead) {
			mc.player.closeScreen();
		}
	}

}
