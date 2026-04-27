package net.minecraft.world.chunk;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;

public class BlockStatePaletteLinear implements IBlockStatePalette {

	private final IBlockState[] states;
	private final IBlockStatePaletteResizer resizeHandler;
	private final int bits;
	private int arraySize;

	public BlockStatePaletteLinear(int bitsIn, IBlockStatePaletteResizer resizeHandlerIn) {

		states = new IBlockState[1 << bitsIn];
		bits = bitsIn;
		resizeHandler = resizeHandlerIn;
	}

	public int idFor(IBlockState state) {

		for (int i = 0; i < arraySize; ++i) {
			if (states[i] == state) {
				return i;
			}
		}

		int j = arraySize;

		if (j < states.length) {
			states[j] = state;
			++arraySize;
			return j;
		} else {
			return resizeHandler.onResize(bits + 1, state);
		}
	}

	@Nullable

	/**
	 * Gets the block state by the palette id.
	 */
	public IBlockState getBlockState(int indexKey) {

		return indexKey >= 0 && indexKey < arraySize ? states[indexKey] : null;
	}

	public void read(PacketBuffer buf) {

		arraySize = buf.readVarInt();

		for (int i = 0; i < arraySize; ++i) {
			states[i] = Block.BLOCK_STATE_IDS.getByValue(buf.readVarInt());
		}
	}

	public void write(PacketBuffer buf) {

		buf.writeVarInt(arraySize);

		for (int i = 0; i < arraySize; ++i) {
			buf.writeVarInt(Block.BLOCK_STATE_IDS.get(states[i]));
		}
	}

	public int getSerializedSize() {

		int i = PacketBuffer.getVarIntSize(arraySize);

		for (int j = 0; j < arraySize; ++j) {
			i += PacketBuffer.getVarIntSize(Block.BLOCK_STATE_IDS.get(states[j]));
		}

		return i;
	}

}
