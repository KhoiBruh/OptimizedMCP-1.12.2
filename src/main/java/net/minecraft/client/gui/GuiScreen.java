package net.minecraft.client.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

public abstract class GuiScreen extends Gui implements GuiYesNoCallback {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Set<String> PROTOCOLS = Sets.newHashSet("http", "https");
	private static final Splitter NEWLINE_SPLITTER = Splitter.on('\n');

	/**
	 * Reference to the Minecraft object.
	 */
	protected Minecraft mc;

	/**
	 * Holds a instance of RenderItem, used to draw the achievement icons on screen (is based on ItemStack)
	 */
	protected RenderItem itemRender;

	/**
	 * The width of the screen object.
	 */
	public int width;

	/**
	 * The height of the screen object.
	 */
	public int height;
	protected List<GuiButton> buttonList = Lists.newArrayList();
	protected List<GuiLabel> labelList = Lists.newArrayList();
	public boolean allowUserInput;

	/**
	 * The FontRenderer used by GuiScreen
	 */
	protected FontRenderer fontRenderer;

	/**
	 * The button that was just pressed.
	 */
	protected GuiButton selectedButton;
	private int eventButton;
	private long lastMouseEvent;

	/**
	 * Tracks the number of fingers currently on the screen. Prevents subsequent fingers registering as clicks.
	 */
	private int touchValue;
	private URI clickedLinkURI;
	private boolean focused;

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		for (int i = 0; i < buttonList.size(); ++i) {
			buttonList.get(i).drawButton(mc, mouseX, mouseY, partialTicks);
		}

		for (int j = 0; j < labelList.size(); ++j) {
			labelList.get(j).drawLabel(mc, mouseX, mouseY);
		}
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) throws IOException {

		if (keyCode == 1) {
			mc.displayGuiScreen(null);

			if (mc.currentScreen == null) {
				mc.setIngameFocus();
			}
		}
	}

	protected <T extends GuiButton> T addButton(T buttonIn) {

		buttonList.add(buttonIn);
		return buttonIn;
	}

	/**
	 * Returns a string stored in the system clipboard.
	 */
	public static String getClipboardString() {

		try {
			Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

			if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				return (String) transferable.getTransferData(DataFlavor.stringFlavor);
			}
		} catch (Exception var1) {
		}

		return "";
	}

	/**
	 * Stores the given string in the system clipboard
	 */
	public static void setClipboardString(String copyText) {

		if (!StringUtils.isEmpty(copyText)) {
			try {
				StringSelection stringselection = new StringSelection(copyText);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringselection, null);
			} catch (Exception var2) {
			}
		}
	}

	protected void renderToolTip(ItemStack stack, int x, int y) {

		drawHoveringText(getItemToolTip(stack), x, y);
	}

	public List<String> getItemToolTip(ItemStack p_191927_1_) {

		List<String> list = p_191927_1_.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);

		for (int i = 0; i < list.size(); ++i) {
			if (i == 0) {
				list.set(i, p_191927_1_.getRarity().rarityColor + list.get(i));
			} else {
				list.set(i, TextFormatting.GRAY + list.get(i));
			}
		}

		return list;
	}

	/**
	 * Draws the given text as a tooltip.
	 */
	public void drawHoveringText(String text, int x, int y) {

		drawHoveringText(Collections.singletonList(text), x, y);
	}

	public void setFocused(boolean hasFocusedControlIn) {

		focused = hasFocusedControlIn;
	}

	public boolean isFocused() {

		return focused;
	}

	/**
	 * Draws a List of strings as a tooltip. Every entry is drawn on a seperate line.
	 */
	public void drawHoveringText(List<String> textLines, int x, int y) {

		if (!textLines.isEmpty()) {
			GlStateManager.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			int i = 0;

			for (String s : textLines) {
				int j = fontRenderer.getStringWidth(s);

				if (j > i) {
					i = j;
				}
			}

			int l1 = x + 12;
			int i2 = y - 12;
			int k = 8;

			if (textLines.size() > 1) {
				k += 2 + (textLines.size() - 1) * 10;
			}

			if (l1 + i > width) {
				l1 -= 28 + i;
			}

			if (i2 + k + 6 > height) {
				i2 = height - k - 6;
			}

			zLevel = 300.0F;
			itemRender.zLevel = 300.0F;
			int l = -267386864;
			drawGradientRect(l1 - 3, i2 - 4, l1 + i + 3, i2 - 3, -267386864, -267386864);
			drawGradientRect(l1 - 3, i2 + k + 3, l1 + i + 3, i2 + k + 4, -267386864, -267386864);
			drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 + k + 3, -267386864, -267386864);
			drawGradientRect(l1 - 4, i2 - 3, l1 - 3, i2 + k + 3, -267386864, -267386864);
			drawGradientRect(l1 + i + 3, i2 - 3, l1 + i + 4, i2 + k + 3, -267386864, -267386864);
			int i1 = 1347420415;
			int j1 = 1344798847;
			drawGradientRect(l1 - 3, i2 - 3 + 1, l1 - 3 + 1, i2 + k + 3 - 1, 1347420415, 1344798847);
			drawGradientRect(l1 + i + 2, i2 - 3 + 1, l1 + i + 3, i2 + k + 3 - 1, 1347420415, 1344798847);
			drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 - 3 + 1, 1347420415, 1347420415);
			drawGradientRect(l1 - 3, i2 + k + 2, l1 + i + 3, i2 + k + 3, 1344798847, 1344798847);

			for (int k1 = 0; k1 < textLines.size(); ++k1) {
				String s1 = textLines.get(k1);
				fontRenderer.drawStringWithShadow(s1, (float) l1, (float) i2, -1);

				if (k1 == 0) {
					i2 += 2;
				}

				i2 += 10;
			}

			zLevel = 0.0F;
			itemRender.zLevel = 0.0F;
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
			RenderHelper.enableStandardItemLighting();
			GlStateManager.enableRescaleNormal();
		}
	}

	/**
	 * Draws the hover event specified by the given chat component
	 */
	protected void handleComponentHover(ITextComponent component, int x, int y) {

		if (component != null && component.getStyle().getHoverEvent() != null) {
			HoverEvent hoverevent = component.getStyle().getHoverEvent();

			if (hoverevent.action() == HoverEvent.Action.SHOW_ITEM) {
				ItemStack itemstack = ItemStack.EMPTY;

				try {
					NBTBase nbtbase = JsonToNBT.getTagFromJson(hoverevent.value().getUnformattedText());

					if (nbtbase instanceof NBTTagCompound) {
						itemstack = new ItemStack((NBTTagCompound) nbtbase);
					}
				} catch (NBTException var9) {
				}

				if (itemstack.isEmpty()) {
					drawHoveringText(TextFormatting.RED + "Invalid Item!", x, y);
				} else {
					renderToolTip(itemstack, x, y);
				}
			} else if (hoverevent.action() == HoverEvent.Action.SHOW_ENTITY) {
				if (mc.gameSettings.advancedItemTooltips) {
					try {
						NBTTagCompound nbttagcompound = JsonToNBT.getTagFromJson(hoverevent.value().getUnformattedText());
						List<String> list = Lists.newArrayList();
						list.add(nbttagcompound.getString("name"));

						if (nbttagcompound.hasKey("type", 8)) {
							String s = nbttagcompound.getString("type");
							list.add("Type: " + s);
						}

						list.add(nbttagcompound.getString("id"));
						drawHoveringText(list, x, y);
					} catch (NBTException var8) {
						drawHoveringText(TextFormatting.RED + "Invalid Entity!", x, y);
					}
				}
			} else if (hoverevent.action() == HoverEvent.Action.SHOW_TEXT) {
				drawHoveringText(mc.fontRenderer.listFormattedStringToWidth(hoverevent.value().getFormattedText(), Math.max(width / 2, 200)), x, y);
			}

			GlStateManager.disableLighting();
		}
	}

	/**
	 * Sets the text of the chat
	 */
	protected void setText(String newChatText, boolean shouldOverwrite) {

	}

	/**
	 * Executes the click event specified by the given chat component
	 */
	public boolean handleComponentClick(ITextComponent component) {

		if (component == null) {
			return false;
		} else {
			ClickEvent clickevent = component.getStyle().getClickEvent();

			if (isShiftKeyDown()) {
				if (component.getStyle().getInsertion() != null) {
					setText(component.getStyle().getInsertion(), false);
				}
			} else if (clickevent != null) {
				if (clickevent.action() == ClickEvent.Action.OPEN_URL) {
					if (!mc.gameSettings.chatLinks) {
						return false;
					}

					try {
						URI uri = new URI(clickevent.value());
						String s = uri.getScheme();

						if (s == null) {
							throw new URISyntaxException(clickevent.value(), "Missing protocol");
						}

						if (!PROTOCOLS.contains(s.toLowerCase(Locale.ROOT))) {
							throw new URISyntaxException(clickevent.value(), "Unsupported protocol: " + s.toLowerCase(Locale.ROOT));
						}

						if (mc.gameSettings.chatLinksPrompt) {
							clickedLinkURI = uri;
							mc.displayGuiScreen(new GuiConfirmOpenLink(this, clickevent.value(), 31102009, false));
						} else {
							openWebLink(uri);
						}
					} catch (URISyntaxException urisyntaxexception) {
						LOGGER.error("Can't open url for {}", clickevent, urisyntaxexception);
					}
				} else if (clickevent.action() == ClickEvent.Action.OPEN_FILE) {
					URI uri1 = (new File(clickevent.value())).toURI();
					openWebLink(uri1);
				} else if (clickevent.action() == ClickEvent.Action.SUGGEST_COMMAND) {
					setText(clickevent.value(), true);
				} else if (clickevent.action() == ClickEvent.Action.RUN_COMMAND) {
					sendChatMessage(clickevent.value(), false);
				} else {
					LOGGER.error("Don't know how to handle {}", clickevent);
				}

				return true;
			}

			return false;
		}
	}

	/**
	 * Used to add chat messages to the client's GuiChat.
	 */
	public void sendChatMessage(String msg) {

		sendChatMessage(msg, true);
	}

	public void sendChatMessage(String msg, boolean addToChat) {

		if (addToChat) {
			mc.ingameGUI.getChatGUI().addToSentMessages(msg);
		}

		mc.player.sendChatMessage(msg);
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

		if (mouseButton == 0) {
			for (int i = 0; i < buttonList.size(); ++i) {
				GuiButton guibutton = buttonList.get(i);

				if (guibutton.mousePressed(mc, mouseX, mouseY)) {
					selectedButton = guibutton;
					guibutton.playPressSound(mc.getSoundHandler());
					actionPerformed(guibutton);
				}
			}
		}
	}

	/**
	 * Called when a mouse button is released.
	 */
	protected void mouseReleased(int mouseX, int mouseY, int state) {

		if (selectedButton != null && state == 0) {
			selectedButton.mouseReleased(mouseX, mouseY);
			selectedButton = null;
		}
	}

	/**
	 * Called when a mouse button is pressed and the mouse is moved around. Parameters are : mouseX, mouseY,
	 * lastButtonClicked & timeSinceMouseClick.
	 */
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {

	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void actionPerformed(GuiButton button) throws IOException {

	}

	/**
	 * Causes the screen to lay out its subcomponents again. This is the equivalent of the Java call
	 * Container.validate()
	 */
	public void setWorldAndResolution(Minecraft mc, int width, int height) {

		this.mc = mc;
		itemRender = mc.getRenderItem();
		fontRenderer = mc.fontRenderer;
		this.width = width;
		this.height = height;
		buttonList.clear();
		initGui();
	}

	/**
	 * Set the gui to the specified width and height
	 */
	public void setGuiSize(int w, int h) {

		width = w;
		height = h;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

	}

	/**
	 * Delegates mouse and keyboard input.
	 */
	public void handleInput() throws IOException {

		if (Mouse.isCreated()) {
			while (Mouse.next()) {
				handleMouseInput();
			}
		}

		if (Keyboard.isCreated()) {
			while (Keyboard.next()) {
				handleKeyboardInput();
			}
		}
	}

	/**
	 * Handles mouse input.
	 */
	public void handleMouseInput() throws IOException {

		int i = Mouse.getEventX() * width / mc.displayWidth;
		int j = height - Mouse.getEventY() * height / mc.displayHeight - 1;
		int k = Mouse.getEventButton();

		if (Mouse.getEventButtonState()) {
			if (mc.gameSettings.touchscreen && touchValue++ > 0) {
				return;
			}

			eventButton = k;
			lastMouseEvent = Minecraft.getSystemTime();
			mouseClicked(i, j, eventButton);
		} else if (k != -1) {
			if (mc.gameSettings.touchscreen && --touchValue > 0) {
				return;
			}

			eventButton = -1;
			mouseReleased(i, j, k);
		} else if (eventButton != -1 && lastMouseEvent > 0L) {
			long l = Minecraft.getSystemTime() - lastMouseEvent;
			mouseClickMove(i, j, eventButton, l);
		}
	}

	/**
	 * Handles keyboard input.
	 */
	public void handleKeyboardInput() throws IOException {

		char c0 = Keyboard.getEventCharacter();

		if (Keyboard.getEventKey() == 0 && c0 >= ' ' || Keyboard.getEventKeyState()) {
			keyTyped(c0, Keyboard.getEventKey());
		}

		mc.dispatchKeypresses();
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {

	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void onGuiClosed() {

	}

	/**
	 * Draws either a gradient over the background screen (when it exists) or a flat gradient over background.png
	 */
	public void drawDefaultBackground() {

		drawWorldBackground(0);
	}

	public void drawWorldBackground(int tint) {

		if (mc.world != null) {
			drawGradientRect(0, 0, width, height, -1072689136, -804253680);
		} else {
			drawBackground(tint);
		}
	}

	/**
	 * Draws the background (i is always 0 as of 1.2.2)
	 */
	public void drawBackground(int tint) {

		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		mc.getTextureManager().bindTexture(OPTIONS_BACKGROUND);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		float f = 32.0F;
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.pos(0.0D, height, 0.0D).tex(0.0D, (float) height / 32.0F + (float) tint).color(64, 64, 64, 255).endVertex();
		bufferbuilder.pos(width, height, 0.0D).tex((float) width / 32.0F, (float) height / 32.0F + (float) tint).color(64, 64, 64, 255).endVertex();
		bufferbuilder.pos(width, 0.0D, 0.0D).tex((float) width / 32.0F, tint).color(64, 64, 64, 255).endVertex();
		bufferbuilder.pos(0.0D, 0.0D, 0.0D).tex(0.0D, tint).color(64, 64, 64, 255).endVertex();
		tessellator.draw();
	}

	/**
	 * Returns true if this GUI should pause the game when it is displayed in single-player
	 */
	public boolean doesGuiPauseGame() {

		return true;
	}

	public void confirmClicked(boolean result, int id) {

		if (id == 31102009) {
			if (result) {
				openWebLink(clickedLinkURI);
			}

			clickedLinkURI = null;
			mc.displayGuiScreen(this);
		}
	}

	private void openWebLink(URI url) {

		try {
			Class<?> oclass = Class.forName("java.awt.Desktop");
			Object object = oclass.getMethod("getDesktop").invoke(null);
			oclass.getMethod("browse", URI.class).invoke(object, url);
		} catch (Throwable throwable1) {
			Throwable throwable = throwable1.getCause();
			LOGGER.error("Couldn't open link: {}", throwable == null ? "<UNKNOWN>" : throwable.getMessage());
		}
	}

	/**
	 * Returns true if either windows ctrl key is down or if either mac meta key is down
	 */
	public static boolean isCtrlKeyDown() {

		if (Minecraft.IS_RUNNING_ON_MAC) {
			return Keyboard.isKeyDown(219) || Keyboard.isKeyDown(220);
		} else {
			return Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157);
		}
	}

	/**
	 * Returns true if either shift key is down
	 */
	public static boolean isShiftKeyDown() {

		return Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54);
	}

	/**
	 * Returns true if either alt key is down
	 */
	public static boolean isAltKeyDown() {

		return Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184);
	}

	public static boolean isKeyComboCtrlX(int keyID) {

		return keyID == 45 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
	}

	public static boolean isKeyComboCtrlV(int keyID) {

		return keyID == 47 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
	}

	public static boolean isKeyComboCtrlC(int keyID) {

		return keyID == 46 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
	}

	public static boolean isKeyComboCtrlA(int keyID) {

		return keyID == 30 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
	}

	/**
	 * Called when the GUI is resized in order to update the world and the resolution
	 */
	public void onResize(Minecraft mcIn, int w, int h) {

		setWorldAndResolution(mcIn, w, h);
	}

}
