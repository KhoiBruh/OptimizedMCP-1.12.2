package net.minecraft.client.gui.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.game.GuiIngame;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;

public class NormalChatListener implements IChatListener {

	private final Minecraft mc;

	public NormalChatListener(Minecraft p_i47393_1_) {

		mc = p_i47393_1_;
	}

	/**
	 * Called whenever this listener receives a chat message, if this listener is registered to the given type in {@link
	 * GuiIngame#chatListeners chatListeners}
	 *
	 * @param chatTypeIn The type of chat message
	 * @param message    The chat message.
	 */
	public void say(ChatType chatTypeIn, ITextComponent message) {

		mc.ingameGUI.getChatGUI().printChatMessage(message);
	}

}
