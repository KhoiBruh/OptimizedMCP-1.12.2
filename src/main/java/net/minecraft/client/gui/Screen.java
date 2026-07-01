package net.minecraft.client.gui;

import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.component.Button;
import net.minecraft.client.gui.menu.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.menu.GuiPanoramaBackground;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.util.Keyboard;
import net.minecraft.client.util.Mouse;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormat;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

public abstract class Screen extends Gui implements GuiYesNoCallback {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Set<String> PROTOCOLS = Sets.newHashSet("http", "https");

	public int width;
	public int height;

	public boolean allowInput;

	protected Minecraft mc;

	protected RenderItem itemRender;
	protected List<Button> buttons = new ArrayList<>();

	protected FontRenderer fontRenderer;

	protected Button selected;
	private int eventButton;
	private long lastMouseEvent;

	private int touch;
	private URI clickedLink;
	private boolean focused;

	public static String getClipboard() {
		try {
			Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

			if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				return (String) transferable.getTransferData(DataFlavor.stringFlavor);
			}
		} catch (Exception ignored) {
		}

		return "";
	}

	public static void setClipboard(String text) {
		if (text != null && !text.isEmpty()) {
			try {
				StringSelection stringselection = new StringSelection(text);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringselection, null);
			} catch (Exception ignored) {
			}
		}
	}

	public static boolean isCtrlDown() {
		if (Minecraft.IS_RUNNING_ON_MAC) {
			return Keyboard.isKeyDown(343) || Keyboard.isKeyDown(347);
		} else {
			return Keyboard.isKeyDown(341) || Keyboard.isKeyDown(345);
		}
	}

	public static boolean isShiftDown() {
		return Keyboard.isKeyDown(340) || Keyboard.isKeyDown(344);
	}

	public static boolean isAltDown() {
		return Keyboard.isKeyDown(342) || Keyboard.isKeyDown(346);
	}

	public static boolean isCtrlX(int key) {
		return key == 88 && isCtrlDown() && !isShiftDown() && !isAltDown();
	}

	public static boolean isCtrlV(int key) {
		return key == 86 && isCtrlDown() && !isShiftDown() && !isAltDown();
	}

	public static boolean isCtrlC(int key) {
		return key == 67 && isCtrlDown() && !isShiftDown() && !isAltDown();
	}

	public static boolean isCtrlA(int key) {
		return key == 65 && isCtrlDown() && !isShiftDown() && !isAltDown();
	}

	public void draw(int mouseX, int mouseY, float partialTicks) {
		for (Button button : buttons) {
			button.drawButton(mc, mouseX, mouseY, partialTicks);
		}
	}

	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 256) {
			mc.displayScreen(null);

			if (mc.currentScreen == null) mc.setIngameFocus();
		}
	}

	protected <T extends Button> T addButton(T buttonIn) {
		buttons.add(buttonIn);
		return buttonIn;
	}

	protected void renderToolTip(ItemStack stack, int x, int y) {
		drawHoveringText(getItemToolTip(stack), x, y);
	}

	public List<String> getItemToolTip(ItemStack item) {
		List<String> tooltip = item.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);

		for (int i = 0; i < tooltip.size(); ++i) {
			if (i == 0) {
				tooltip.set(i, item.getRarity().rarityColor + tooltip.get(i));
			} else {
				tooltip.set(i, TextFormat.GRAY + tooltip.get(i));
			}
		}

		return tooltip;
	}

	public void drawHoveringText(String text, int x, int y) {
		drawHoveringText(Collections.singletonList(text), x, y);
	}

	public boolean isFocused() {

		return focused;
	}

	public void setFocused(boolean hasFocusedControlIn) {

		focused = hasFocusedControlIn;
	}

	public void drawHoveringText(List<String> textLines, int x, int y) {
		if (!textLines.isEmpty()) {
			GLS.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			GLS.disableLighting();
			GLS.disableDepth();
			int i = 0;

			for (String s : textLines) {
				int j = fontRenderer.getStringWidth(s);

				if (j > i) i = j;
			}

			int l1 = x + 12;
			int i2 = y - 12;
			int k = 8;

			if (textLines.size() > 1) k += 2 + (textLines.size() - 1) * 10;

			if (l1 + i > width) l1 -= 28 + i;

			if (i2 + k + 6 > height) i2 = height - k - 6;

			zLevel = 300F;
			itemRender.zLevel = 300F;
			drawGradientRect(l1 - 3, i2 - 4, l1 + i + 3, i2 - 3, -267386864, -267386864);
			drawGradientRect(l1 - 3, i2 + k + 3, l1 + i + 3, i2 + k + 4, -267386864, -267386864);
			drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 + k + 3, -267386864, -267386864);
			drawGradientRect(l1 - 4, i2 - 3, l1 - 3, i2 + k + 3, -267386864, -267386864);
			drawGradientRect(l1 + i + 3, i2 - 3, l1 + i + 4, i2 + k + 3, -267386864, -267386864);
			drawGradientRect(l1 - 3, i2 - 3 + 1, l1 - 3 + 1, i2 + k + 3 - 1, 1347420415, 1344798847);
			drawGradientRect(l1 + i + 2, i2 - 3 + 1, l1 + i + 3, i2 + k + 3 - 1, 1347420415, 1344798847);
			drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 - 3 + 1, 1347420415, 1347420415);
			drawGradientRect(l1 - 3, i2 + k + 2, l1 + i + 3, i2 + k + 3, 1344798847, 1344798847);

			for (int k1 = 0; k1 < textLines.size(); ++k1) {
				String s1 = textLines.get(k1);
				fontRenderer.drawStringWithShadow(s1, (float) l1, (float) i2, -1);

				if (k1 == 0) i2 += 2;

				i2 += 10;
			}

			zLevel = 0F;
			itemRender.zLevel = 0F;
			GLS.enableLighting();
			GLS.enableDepth();
			RenderHelper.enableStandardItemLighting();
			GLS.enableRescaleNormal();
		}
	}

	protected void handleComponentHover(ITextComponent component, int x, int y) {
		if (component != null && component.getStyle().getHoverEvent() != null) {
			HoverEvent event = component.getStyle().getHoverEvent();

			switch (event.action()) {
				case SHOW_TEXT -> drawHoveringText(mc.fontRenderer.formatToWidth(event.value().getFormattedText(), Math.max(width / 2, 200)), x, y);
				case SHOW_ITEM -> {
					ItemStack item = ItemStack.EMPTY;

					try {
						NBTTagCompound tag = JsonToNBT.getTagFromJson(event.value().getUnformattedText());

						if (tag != null) item = new ItemStack(tag);
					} catch (NBTException ignored) {
					}

					if (item.isEmpty()) drawHoveringText(TextFormat.RED + "Invalid Item!", x, y);
					else renderToolTip(item, x, y);
				}
				case SHOW_ENTITY -> {
					if (mc.gameSettings.advancedItemTooltips) {
						try {
							NBTTagCompound tag = JsonToNBT.getTagFromJson(event.value().getUnformattedText());
							List<String> tags = new ArrayList<>();
							tags.add(tag.getString("name"));

							if (tag.hasKey("type", 8)) {
								String s = tag.getString("type");
								tags.add("Type: " + s);
							}

							tags.add(tag.getString("id"));
							drawHoveringText(tags, x, y);
						} catch (NBTException var8) {
							drawHoveringText(TextFormat.RED + "Invalid Entity!", x, y);
						}
					}
				}
			}

			GLS.disableLighting();
		}
	}

	protected void setText(String newChatText, boolean shouldOverwrite) {

	}

	public boolean handleComponentClick(ITextComponent component) {
		if (component != null) {
			ClickEvent event = component.getStyle().getClickEvent();

			if (isShiftDown()) {
				if (component.getStyle().getInsertion() != null)
					setText(component.getStyle().getInsertion(), false);
			} else if (event != null) {
				switch (event.action()) {
					case RUN_COMMAND -> sendChatMessage(event.value(), false);
					case SUGGEST_COMMAND -> setText(event.value(), true);
					case OPEN_FILE -> {
						URI uri = new File(event.value()).toURI();
						openLink(uri);
					}
					case OPEN_URL -> {
						if (!mc.gameSettings.chatLinks) return false;

						try {
							URI uri = new URI(event.value());
							String s = uri.getScheme();

							if (s == null) throw new URISyntaxException(event.value(), "Missing protocol");

							if (!PROTOCOLS.contains(s.toLowerCase(Locale.ROOT)))
								throw new URISyntaxException(event.value(), "Unsupported protocol: " + s.toLowerCase(Locale.ROOT));

							if (mc.gameSettings.chatLinksPrompt) {
								clickedLink = uri;
								mc.displayScreen(new ConfirmOpenLinkScreen(this, event.value(), 31102009, false));
							} else openLink(uri);
						} catch (URISyntaxException urisyntaxexception) {
							LOGGER.error("Can't open url for {}", event, urisyntaxexception);
						}
					}
					default -> LOGGER.error("Don't know how to handle {}", event);
				}

				return true;
			}

		}

		return false;
	}

	public void sendChatMessage(String msg) {
		sendChatMessage(msg, true);
	}

	public void sendChatMessage(String msg, boolean addToChat) {
		if (addToChat) mc.ingameGUI.getChatGUI().addToSentMessages(msg);

		mc.player.sendChatMessage(msg);
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouse) throws IOException {
		if (mouse == 0) {
			for (Button button : new ArrayList<>(buttons)) {
				if (button.mousePressed(mc, mouseX, mouseY)) {
					selected = button;
					button.playPressSound(mc.getSoundHandler());
					action(button);
					return;
				}
			}
		}
	}

	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (selected != null && state == 0) {
			selected.mouseReleased(mouseX, mouseY);
			selected = null;
		}
	}

	protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLastClick) {

	}

	protected void action(Button button) throws IOException {

	}

	public void setResolution(Minecraft mc, int width, int height) {
		this.mc = mc;
		itemRender = mc.getRenderItem();
		fontRenderer = mc.fontRenderer;
		this.width = width;
		this.height = height;
		buttons.clear();
		init();
	}

	public void init() {

	}

	public void handleInput() throws IOException {
		if (Mouse.isCreated()) while (Mouse.next()) handleMouse();
		if (Keyboard.isCreated()) while (Keyboard.next()) handleKeyboard();
	}

	public void handleMouse() throws IOException {
		int i = Mouse.getX() * width / mc.getWindow().getWidth();

		int j = height - Mouse.getY() * height / mc.getWindow().getHeight() - 1;
		int k = Mouse.getEventButton();

		if (Mouse.getEventButtonState()) {
			if (mc.gameSettings.touchscreen && touch++ > 0) return;

			eventButton = k;
			lastMouseEvent = Minecraft.getSystemTime();
			mouseClicked(i, j, eventButton);
		} else if (k != -1) {
			if (mc.gameSettings.touchscreen && touch-- > 0) return;

			eventButton = -1;
			mouseReleased(i, j, k);
		} else if (eventButton != -1 && lastMouseEvent > 0L) {
			long l = Minecraft.getSystemTime() - lastMouseEvent;
			mouseClickMove(i, j, eventButton, l);
		}
	}

	public void handleKeyboard() throws IOException {
		char c0 = Keyboard.getEventCharacter();

		if (Keyboard.getEventKeyState()) keyTyped(c0, Keyboard.getEventKey());

		mc.dispatchKeypresses();
	}

	public void update() {

	}

	public void close() {

	}

	public void drawDefaultBackground() {
		if (mc.world == null) {
			GLS.disableAlpha();
			GuiPanoramaBackground.render(mc, width, height);
			GLS.enableAlpha();
			drawGradientRect(0, 0, width, height, -2130706433, 16777215);
			drawGradientRect(0, 0, width, height, 0, Integer.MIN_VALUE);
		} else drawWorldBackground(0);
	}

	public void drawWorldBackground(int tint) {
		if (mc.world != null) {
			drawGradientRect(0, 0, width, height, -1072689136, -804253680);
		} else {
			drawBackground(tint);
		}
	}

	public void drawBackground(int tint) {
		GLS.disableLighting();
		GLS.disableFog();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		mc.getTextureManager().bindTexture(OPTIONS_BACKGROUND);
		GLS.color(1F, 1F, 1F, 1F);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.pos(0D, height, 0D).tex(0D, (float) height / 32F + (float) tint).color(64, 64, 64, 255).endVertex();
		bufferbuilder.pos(width, height, 0D).tex((float) width / 32F, (float) height / 32F + (float) tint).color(64, 64, 64, 255).endVertex();
		bufferbuilder.pos(width, 0D, 0D).tex((float) width / 32F, tint).color(64, 64, 64, 255).endVertex();
		bufferbuilder.pos(0D, 0D, 0D).tex(0D, tint).color(64, 64, 64, 255).endVertex();
		tessellator.draw();
	}

	public boolean pauseGame() {
		return true;
	}

	public void confirmClicked(boolean result, int id) {
		if (id == 31102009) {
			if (result) openLink(clickedLink);

			clickedLink = null;
			mc.displayScreen(this);
		}
	}

	private void openLink(URI url) {
		try {
			Desktop.getDesktop().browse(url);
		} catch (Throwable throwable1) {
			Throwable throwable = throwable1.getCause();
			LOGGER.error("Couldn't open link: {}", throwable == null ? "<UNKNOWN>" : throwable.getMessage());
		}
	}

	public void onResize(Minecraft mcIn, int w, int h) {
		setResolution(mcIn, w, h);
	}

}
