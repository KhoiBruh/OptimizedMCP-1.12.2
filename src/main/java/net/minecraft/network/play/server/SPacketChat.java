package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;

import java.io.IOException;

public class SPacketChat implements Packet<INetHandlerPlayClient> {

	private ITextComponent chatComponent;
	private ChatType type;

	public SPacketChat() {

	}

	public SPacketChat(ITextComponent componentIn) {

		this(componentIn, ChatType.SYSTEM);
	}

	public SPacketChat(ITextComponent message, ChatType type) {

		chatComponent = message;
		this.type = type;
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) throws IOException {

		chatComponent = buf.readTextComponent();
		type = ChatType.byId(buf.readByte());
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeTextComponent(chatComponent);
		buf.writeByte(type.getId());
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handleChat(this);
	}

	public ITextComponent getChatComponent() {

		return chatComponent;
	}

	/**
	 * This method returns true if the type is SYSTEM or ABOVE_HOTBAR, and false if CHAT
	 */
	public boolean isSystem() {

		return type == ChatType.SYSTEM || type == ChatType.GAME_INFO;
	}

	public ChatType getType() {

		return type;
	}

}
