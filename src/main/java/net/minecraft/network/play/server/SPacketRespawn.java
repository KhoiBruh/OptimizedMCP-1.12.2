package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldType;

public class SPacketRespawn implements Packet<INetHandlerPlayClient> {

	private int dimensionID;
	private Difficulty difficulty;
	private GameType gameType;
	private WorldType worldType;

	public SPacketRespawn() {

	}

	public SPacketRespawn(int dimensionIdIn, Difficulty difficultyIn, WorldType worldTypeIn, GameType gameModeIn) {

		dimensionID = dimensionIdIn;
		difficulty = difficultyIn;
		gameType = gameModeIn;
		worldType = worldTypeIn;
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handleRespawn(this);
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		dimensionID = buf.readInt();
		difficulty = Difficulty.getDifficultyEnum(buf.readUnsignedByte());
		gameType = GameType.getByID(buf.readUnsignedByte());
		worldType = WorldType.parseWorldType(buf.readString(16));

		if (worldType == null) {
			worldType = WorldType.DEFAULT;
		}
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeInt(dimensionID);
		buf.writeByte(difficulty.getDifficultyId());
		buf.writeByte(gameType.getID());
		buf.writeString(worldType.getName());
	}

	public int getDimensionID() {

		return dimensionID;
	}

	public Difficulty getDifficulty() {

		return difficulty;
	}

	public GameType getGameType() {

		return gameType;
	}

	public WorldType getWorldType() {

		return worldType;
	}

}
