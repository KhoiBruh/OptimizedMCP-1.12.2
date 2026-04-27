package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;

import java.io.IOException;

public class SPacketOpenWindow implements Packet<INetHandlerPlayClient> {

	private int windowId;
	private String inventoryType;
	private ITextComponent windowTitle;
	private int slotCount;
	private int entityId;

	public SPacketOpenWindow() {

	}

	public SPacketOpenWindow(int windowIdIn, String inventoryTypeIn, ITextComponent windowTitleIn) {

		this(windowIdIn, inventoryTypeIn, windowTitleIn, 0);
	}

	public SPacketOpenWindow(int windowIdIn, String inventoryTypeIn, ITextComponent windowTitleIn, int slotCountIn) {

		windowId = windowIdIn;
		inventoryType = inventoryTypeIn;
		windowTitle = windowTitleIn;
		slotCount = slotCountIn;
	}

	public SPacketOpenWindow(int windowIdIn, String inventoryTypeIn, ITextComponent windowTitleIn, int slotCountIn, int entityIdIn) {

		this(windowIdIn, inventoryTypeIn, windowTitleIn, slotCountIn);
		entityId = entityIdIn;
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handleOpenWindow(this);
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) throws IOException {

		windowId = buf.readUnsignedByte();
		inventoryType = buf.readString(32);
		windowTitle = buf.readTextComponent();
		slotCount = buf.readUnsignedByte();

		if (inventoryType.equals("EntityHorse")) {
			entityId = buf.readInt();
		}
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeByte(windowId);
		buf.writeString(inventoryType);
		buf.writeTextComponent(windowTitle);
		buf.writeByte(slotCount);

		if (inventoryType.equals("EntityHorse")) {
			buf.writeInt(entityId);
		}
	}

	public int getWindowId() {

		return windowId;
	}

	public String getGuiId() {

		return inventoryType;
	}

	public ITextComponent getWindowTitle() {

		return windowTitle;
	}

	public int getSlotCount() {

		return slotCount;
	}

	public int getEntityId() {

		return entityId;
	}

	public boolean hasSlots() {

		return slotCount > 0;
	}

}
