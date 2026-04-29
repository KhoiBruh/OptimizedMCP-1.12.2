package net.minecraft.world.chunk;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BitArray;
import net.minecraft.util.math.MathHelper;

public class BlockStateContainer implements IBlockStatePaletteResizer {

	protected static final IBlockState AIR_BLOCK_STATE = Blocks.AIR.getDefaultState();
	private static final IBlockStatePalette REGISTRY_BASED_PALETTE = new BlockStatePaletteRegistry();
	protected BitArray storage;
	protected IBlockStatePalette palette;
	private int bits;

	public BlockStateContainer() {

		setBits(4);
	}

	private static int getIndex(int x, int y, int z) {

		return y << 8 | z << 4 | x;
	}

	private void setBits(int bitsIn) {

		if (bitsIn != bits) {
			bits = bitsIn;

			if (bits <= 4) {
				bits = 4;
				palette = new BlockStatePaletteLinear(bits, this);
			} else if (bits <= 8) {
				palette = new BlockStatePaletteHashMap(bits, this);
			} else {
				palette = REGISTRY_BASED_PALETTE;
				bits = MathHelper.log2DeBruijn(Block.BLOCK_STATE_IDS.size());
			}

			palette.idFor(AIR_BLOCK_STATE);
			storage = new BitArray(bits, 4096);
		}
	}

	public int onResize(int bits, IBlockState state) {

		BitArray bitarray = storage;
		IBlockStatePalette iblockstatepalette = palette;
		setBits(bits);

		for (int i = 0; i < bitarray.size(); ++i) {
			IBlockState iblockstate = iblockstatepalette.getBlockState(bitarray.getAt(i));

			if (iblockstate != null) {
				set(i, iblockstate);
			}
		}

		return palette.idFor(state);
	}

	public void set(int x, int y, int z, IBlockState state) {

		set(getIndex(x, y, z), state);
	}

	protected void set(int index, IBlockState state) {

		int i = palette.idFor(state);
		storage.setAt(index, i);
	}

	public IBlockState get(int x, int y, int z) {

		return get(getIndex(x, y, z));
	}

	protected IBlockState get(int index) {

		IBlockState iblockstate = palette.getBlockState(storage.getAt(index));
		return iblockstate == null ? AIR_BLOCK_STATE : iblockstate;
	}

	public void read(PacketBuffer buf) {

		int i = buf.readByte();

		if (bits != i) {
			setBits(i);
		}

		palette.read(buf);
		buf.readLongArray(storage.getBackingLongArray());
	}

	public void write(PacketBuffer buf) {

		buf.writeByte(bits);
		palette.write(buf);
		buf.writeLongArray(storage.getBackingLongArray());
	}

	
	public NibbleArray getDataForNBT(byte[] blockIds, NibbleArray data) {

		NibbleArray nibblearray = null;

		for (int i = 0; i < 4096; ++i) {
			int j = Block.BLOCK_STATE_IDS.get(get(i));
			int k = i & 15;
			int l = i >> 8 & 15;
			int i1 = i >> 4 & 15;

			if ((j >> 12 & 15) != 0) {
				if (nibblearray == null) {
					nibblearray = new NibbleArray();
				}

				nibblearray.set(k, l, i1, j >> 12 & 15);
			}

			blockIds[i] = (byte) (j >> 4 & 255);
			data.set(k, l, i1, j & 15);
		}

		return nibblearray;
	}

	public void setDataFromNBT(byte[] blockIds, NibbleArray data, NibbleArray blockIdExtension) {

		for (int i = 0; i < 4096; ++i) {
			int j = i & 15;
			int k = i >> 8 & 15;
			int l = i >> 4 & 15;
			int i1 = blockIdExtension == null ? 0 : blockIdExtension.get(j, k, l);
			int j1 = i1 << 12 | (blockIds[i] & 255) << 4 | data.get(j, k, l);
			set(i, Block.BLOCK_STATE_IDS.getByValue(j1));
		}
	}

	public int getSerializedSize() {

		return 1 + palette.getSerializedSize() + PacketBuffer.getVarIntSize(storage.size()) + storage.getBackingLongArray().length * 8;
	}

}
