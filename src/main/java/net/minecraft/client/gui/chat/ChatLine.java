package net.minecraft.client.gui.chat;

import net.minecraft.util.text.ITextComponent;

public class ChatLine {

	/**
	 * GUI Update Counter value this Line was created at
	 */
	private final int updateCounterCreated;
	private final ITextComponent lineString;

	/**
	 * int value to refer to existing Chat Lines, can be 0 which means unreferrable
	 */
	private final int chatLineID;

	public ChatLine(int updateCounterCreatedIn, ITextComponent lineStringIn, int chatLineIDIn) {

		lineString = lineStringIn;
		updateCounterCreated = updateCounterCreatedIn;
		chatLineID = chatLineIDIn;
	}

	public ITextComponent getChatComponent() {

		return lineString;
	}

	public int getUpdatedCounter() {

		return updateCounterCreated;
	}

	public int getChatLineID() {

		return chatLineID;
	}

}
