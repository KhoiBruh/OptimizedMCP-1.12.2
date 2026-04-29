package net.minecraft.client.gui.chat;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Iterator;
import java.util.List;

public class GuiNewChat extends Gui {

	private static final Logger LOGGER = LogManager.getLogger();
	private final Minecraft mc;
	private final List<String> sentMessages = Lists.newArrayList();
	private final List<ChatLine> chatLines = Lists.newArrayList();
	private final List<ChatLine> drawnChatLines = Lists.newArrayList();
	private int scrollPos;
	private boolean isScrolled;

	public GuiNewChat(Minecraft mcIn) {

		mc = mcIn;
	}

	public static int calculateChatboxWidth(float scale) {

		int i = 320;
		int j = 40;
		return MathHelper.floor(scale * 280.0F + 40.0F);
	}

	public static int calculateChatboxHeight(float scale) {

		int i = 180;
		int j = 20;
		return MathHelper.floor(scale * 160.0F + 20.0F);
	}

	public void drawChat(int updateCounter) {

		if (mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
			int i = getLineCount();
			int j = drawnChatLines.size();
			float f = mc.gameSettings.chatOpacity * 0.9F + 0.1F;

			if (j > 0) {
				boolean flag = getChatOpen();

				float f1 = getChatScale();
				int k = MathHelper.ceil((float) getChatWidth() / f1);
				GlStateManager.pushMatrix();
				GlStateManager.translate(2.0F, 8.0F, 0.0F);
				GlStateManager.scale(f1, f1, 1.0F);
				int l = 0;

				for (int i1 = 0; i1 + scrollPos < drawnChatLines.size() && i1 < i; ++i1) {
					ChatLine chatline = drawnChatLines.get(i1 + scrollPos);

					if (chatline != null) {
						int j1 = updateCounter - chatline.getUpdatedCounter();

						if (j1 < 200 || flag) {
							double d0 = (double) j1 / 200.0D;
							d0 = 1.0D - d0;
							d0 = d0 * 10.0D;
							d0 = MathHelper.clamp(d0, 0.0D, 1.0D);
							d0 = d0 * d0;
							int l1 = (int) (255.0D * d0);

							if (flag) {
								l1 = 255;
							}

							l1 = (int) ((float) l1 * f);
							++l;

							if (l1 > 3) {
								int i2 = 0;
								int j2 = -i1 * 9;
								drawRect(-2, j2 - 9, k + 4, j2, l1 / 2 << 24);
								String s = chatline.getChatComponent().getFormattedText();
								GlStateManager.enableBlend();
								mc.fontRenderer.drawStringWithShadow(s, 0.0F, (float) (j2 - 8), 16777215 + (l1 << 24));
								GlStateManager.disableAlpha();
								GlStateManager.disableBlend();
							}
						}
					}
				}

				if (flag) {
					int k2 = mc.fontRenderer.FONT_HEIGHT;
					GlStateManager.translate(-3.0F, 0.0F, 0.0F);
					int l2 = j * k2 + j;
					int i3 = l * k2 + l;
					int j3 = scrollPos * i3 / j;
					int k1 = i3 * i3 / l2;

					if (l2 != i3) {
						int k3 = j3 > 0 ? 170 : 96;
						int l3 = isScrolled ? 13382451 : 3355562;
						drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
						drawRect(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
					}
				}

				GlStateManager.popMatrix();
			}
		}
	}

	/**
	 * Clears the chat.
	 */
	public void clearChatMessages(boolean p_146231_1_) {

		drawnChatLines.clear();
		chatLines.clear();

		if (p_146231_1_) {
			sentMessages.clear();
		}
	}

	public void printChatMessage(ITextComponent chatComponent) {

		printChatMessageWithOptionalDeletion(chatComponent, 0);
	}

	/**
	 * prints the ChatComponent to Chat. If the ID is not 0, deletes an existing Chat Line of that ID from the GUI
	 */
	public void printChatMessageWithOptionalDeletion(ITextComponent chatComponent, int chatLineId) {

		setChatLine(chatComponent, chatLineId, mc.ingameGUI.getUpdateCounter(), false);
		LOGGER.info("[CHAT] {}", chatComponent.getUnformattedText().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
	}

	private void setChatLine(ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly) {

		if (chatLineId != 0) {
			deleteChatLine(chatLineId);
		}

		int i = MathHelper.floor((float) getChatWidth() / getChatScale());
		List<ITextComponent> list = GuiUtilRenderComponents.splitText(chatComponent, i, mc.fontRenderer, false, false);
		boolean flag = getChatOpen();

		for (ITextComponent itextcomponent : list) {
			if (flag && scrollPos > 0) {
				isScrolled = true;
				scroll(1);
			}

			drawnChatLines.addFirst(new ChatLine(updateCounter, itextcomponent, chatLineId));
		}

		while (drawnChatLines.size() > 100) {
			drawnChatLines.removeLast();
		}

		if (!displayOnly) {
			chatLines.addFirst(new ChatLine(updateCounter, chatComponent, chatLineId));

			while (chatLines.size() > 100) {
				chatLines.removeLast();
			}
		}
	}

	public void refreshChat() {

		drawnChatLines.clear();
		resetScroll();

		for (int i = chatLines.size() - 1; i >= 0; --i) {
			ChatLine chatline = chatLines.get(i);
			setChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true);
		}
	}

	public List<String> getSentMessages() {

		return sentMessages;
	}

	/**
	 * Adds this string to the list of sent messages, for recall using the up/down arrow keys
	 */
	public void addToSentMessages(String message) {

		if (sentMessages.isEmpty() || !sentMessages.getLast().equals(message)) {
			sentMessages.add(message);
		}
	}

	/**
	 * Resets the chat scroll (executed when the GUI is closed, among others)
	 */
	public void resetScroll() {

		scrollPos = 0;
		isScrolled = false;
	}

	/**
	 * Scrolls the chat by the given number of lines.
	 */
	public void scroll(int amount) {

		scrollPos += amount;
		int i = drawnChatLines.size();

		if (scrollPos > i - getLineCount()) {
			scrollPos = i - getLineCount();
		}

		if (scrollPos <= 0) {
			scrollPos = 0;
			isScrolled = false;
		}
	}

	

	/**
	 * Gets the chat component under the mouse
	 */
	public ITextComponent getChatComponent(int mouseX, int mouseY) {
		
		if (getChatOpen()) {
			ScaledResolution scaledResolution = mc.scaledResolution;
			int i = scaledResolution.getScaleFactor();
			float f = getChatScale();
			int j = mouseX / i - 2;
			int k = mouseY / i - 40;
			j = MathHelper.floor((float) j / f);
			k = MathHelper.floor((float) k / f);

			if (j >= 0 && k >= 0) {
				int l = Math.min(getLineCount(), drawnChatLines.size());

				if (j <= MathHelper.floor((float) getChatWidth() / getChatScale()) && k < mc.fontRenderer.FONT_HEIGHT * l + l) {
					int i1 = k / mc.fontRenderer.FONT_HEIGHT + scrollPos;

					if (i1 >= 0 && i1 < drawnChatLines.size()) {
						ChatLine chatline = drawnChatLines.get(i1);
						int j1 = 0;

						for (ITextComponent itextcomponent : chatline.getChatComponent()) {
							if (itextcomponent instanceof TextComponentString) {
								j1 += mc.fontRenderer.getStringWidth(GuiUtilRenderComponents.removeTextColorsIfConfigured(((TextComponentString) itextcomponent).getText(), false));

								if (j1 > j) {
									return itextcomponent;
								}
							}
						}
					}
					
				}
			}
		}
		
		return null;
	}

	/**
	 * Returns true if the chat GUI is open
	 */
	public boolean getChatOpen() {

		return mc.currentScreen instanceof GuiChat;
	}

	/**
	 * finds and deletes a Chat line by ID
	 */
	public void deleteChatLine(int id) {

		Iterator<ChatLine> iterator = drawnChatLines.iterator();

		while (iterator.hasNext()) {
			ChatLine chatline = iterator.next();

			if (chatline.getChatLineID() == id) {
				iterator.remove();
			}
		}

		iterator = chatLines.iterator();

		while (iterator.hasNext()) {
			ChatLine chatline1 = iterator.next();

			if (chatline1.getChatLineID() == id) {
				iterator.remove();
				break;
			}
		}
	}

	public int getChatWidth() {

		return calculateChatboxWidth(mc.gameSettings.chatWidth);
	}

	public int getChatHeight() {

		return calculateChatboxHeight(getChatOpen() ? mc.gameSettings.chatHeightFocused : mc.gameSettings.chatHeightUnfocused);
	}

	/**
	 * Returns the chatscale from mc.gameSettings.chatScale
	 */
	public float getChatScale() {

		return mc.gameSettings.chatScale;
	}

	public int getLineCount() {

		return getChatHeight() / 9;
	}

}
