package net.minecraft.network.datasync;

import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Facing;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Rotations;
import net.minecraft.util.text.ITextComponent;
import java.io.IOException;
import java.util.UUID;

public class DataSerializers {

	public static final DataSerializer<Byte> BYTE = new DataSerializer<>() {
		public void write(PacketBuffer buf, Byte value) {

			buf.writeByte(value);
		}

		public Byte read(PacketBuffer buf) {

			return buf.readByte();
		}

		public DataParameter<Byte> createKey(int id) {

			return new DataParameter<>(id, this);
		}

		public Byte copyValue(Byte value) {

			return value;
		}
	};
	public static final DataSerializer<Integer> VARINT = new DataSerializer<>() {
		public void write(PacketBuffer buf, Integer value) {

			buf.writeVarInt(value);
		}

		public Integer read(PacketBuffer buf) {

			return buf.readVarInt();
		}

		public DataParameter<Integer> createKey(int id) {

			return new DataParameter<>(id, this);
		}

		public Integer copyValue(Integer value) {

			return value;
		}
	};
	public static final DataSerializer<Float> FLOAT = new DataSerializer<>() {
		public void write(PacketBuffer buf, Float value) {

			buf.writeFloat(value);
		}

		public Float read(PacketBuffer buf) {

			return buf.readFloat();
		}

		public DataParameter<Float> createKey(int id) {

			return new DataParameter<>(id, this);
		}

		public Float copyValue(Float value) {

			return value;
		}
	};
	public static final DataSerializer<String> STRING = new DataSerializer<>() {
		public void write(PacketBuffer buf, String value) {

			buf.writeString(value);
		}

		public String read(PacketBuffer buf) {

			return buf.readString(32767);
		}

		public DataParameter<String> createKey(int id) {

			return new DataParameter<>(id, this);
		}

		public String copyValue(String value) {

			return value;
		}
	};
	public static final DataSerializer<ITextComponent> TEXT_COMPONENT = new DataSerializer<>() {
		public void write(PacketBuffer buf, ITextComponent value) {

			buf.writeTextComponent(value);
		}

		public ITextComponent read(PacketBuffer buf) {

			return buf.readTextComponent();
		}

		public DataParameter<ITextComponent> createKey(int id) {

			return new DataParameter<>(id, this);
		}

		public ITextComponent copyValue(ITextComponent value) {

			return value.createCopy();
		}
	};
	public static final DataSerializer<ItemStack> ITEM_STACK = new DataSerializer<>() {
		public void write(PacketBuffer buf, ItemStack value) {

			buf.writeItemStack(value);
		}

		public ItemStack read(PacketBuffer buf) throws IOException {

			return buf.readItemStack();
		}

		public DataParameter<ItemStack> createKey(int id) {

			return new DataParameter<>(id, this);
		}

		public ItemStack copyValue(ItemStack value) {

			return value.copy();
		}
	};
	public static final DataSerializer<Optional<IBlockState>> OPTIONAL_BLOCK_STATE = new DataSerializer<>() {
		public void write(PacketBuffer buf, Optional<IBlockState> value) {

			if (value.isPresent()) {
				buf.writeVarInt(Block.getStateId(value.get()));
			} else {
				buf.writeVarInt(0);
			}
		}

		public Optional<IBlockState> read(PacketBuffer buf) {

			int i = buf.readVarInt();
			return i == 0 ? Optional.absent() : Optional.of(Block.getStateById(i));
		}

		public DataParameter<Optional<IBlockState>> createKey(int id) {

			return new DataParameter<>(id, this);
		}

		public Optional<IBlockState> copyValue(Optional<IBlockState> value) {

			return value;
		}
	};
	public static final DataSerializer<Boolean> BOOLEAN = new DataSerializer<>() {
		public void write(PacketBuffer buf, Boolean value) {

			buf.writeBoolean(value);
		}

		public Boolean read(PacketBuffer buf) {

			return buf.readBoolean();
		}

		public DataParameter<Boolean> createKey(int id) {

			return new DataParameter<>(id, this);
		}

		public Boolean copyValue(Boolean value) {

			return value;
		}
	};
	public static final DataSerializer<Rotations> ROTATIONS = new DataSerializer<>() {
		public void write(PacketBuffer buf, Rotations value) {

			buf.writeFloat(value.x());
			buf.writeFloat(value.y());
			buf.writeFloat(value.z());
		}

		public Rotations read(PacketBuffer buf) {

			return new Rotations(buf.readFloat(), buf.readFloat(), buf.readFloat());
		}

		public DataParameter<Rotations> createKey(int id) {

			return new DataParameter<>(id, this);
		}

		public Rotations copyValue(Rotations value) {

			return value;
		}
	};
	public static final DataSerializer<BlockPos> BLOCK_POS = new DataSerializer<>() {
		public void write(PacketBuffer buf, BlockPos value) {

			buf.writeBlockPos(value);
		}

		public BlockPos read(PacketBuffer buf) {

			return buf.readBlockPos();
		}

		public DataParameter<BlockPos> createKey(int id) {

			return new DataParameter<>(id, this);
		}

		public BlockPos copyValue(BlockPos value) {

			return value;
		}
	};
	public static final DataSerializer<Optional<BlockPos>> OPTIONAL_BLOCK_POS = new DataSerializer<>() {
		public void write(PacketBuffer buf, Optional<BlockPos> value) {

			buf.writeBoolean(value.isPresent());

			if (value.isPresent()) {
				buf.writeBlockPos(value.get());
			}
		}

		public Optional<BlockPos> read(PacketBuffer buf) {

			return !buf.readBoolean() ? Optional.absent() : Optional.of(buf.readBlockPos());
		}

		public DataParameter<Optional<BlockPos>> createKey(int id) {

			return new DataParameter<>(id, this);
		}

		public Optional<BlockPos> copyValue(Optional<BlockPos> value) {

			return value;
		}
	};
	public static final DataSerializer<Facing> FACING = new DataSerializer<>() {
		public void write(PacketBuffer buf, Facing value) {

			buf.writeEnumValue(value);
		}

		public Facing read(PacketBuffer buf) {

			return buf.readEnumValue(Facing.class);
		}

		public DataParameter<Facing> createKey(int id) {

			return new DataParameter<>(id, this);
		}

		public Facing copyValue(Facing value) {

			return value;
		}
	};
	public static final DataSerializer<Optional<UUID>> OPTIONAL_UNIQUE_ID = new DataSerializer<>() {
		public void write(PacketBuffer buf, Optional<UUID> value) {

			buf.writeBoolean(value.isPresent());

			if (value.isPresent()) {
				buf.writeUniqueId(value.get());
			}
		}

		public Optional<UUID> read(PacketBuffer buf) {

			return !buf.readBoolean() ? Optional.absent() : Optional.of(buf.readUniqueId());
		}

		public DataParameter<Optional<UUID>> createKey(int id) {

			return new DataParameter<>(id, this);
		}

		public Optional<UUID> copyValue(Optional<UUID> value) {

			return value;
		}
	};
	public static final DataSerializer<NBTTagCompound> COMPOUND_TAG = new DataSerializer<>() {
		public void write(PacketBuffer buf, NBTTagCompound value) {

			buf.writeCompoundTag(value);
		}

		public NBTTagCompound read(PacketBuffer buf) {

			return buf.readCompoundTag();
		}

		public DataParameter<NBTTagCompound> createKey(int id) {

			return new DataParameter<>(id, this);
		}

		public NBTTagCompound copyValue(NBTTagCompound value) {

			return value.copy();
		}
	};
	private static final IntIdentityHashBiMap<DataSerializer<?>> REGISTRY = new IntIdentityHashBiMap<>(16);

	static {
		registerSerializer(BYTE);
		registerSerializer(VARINT);
		registerSerializer(FLOAT);
		registerSerializer(STRING);
		registerSerializer(TEXT_COMPONENT);
		registerSerializer(ITEM_STACK);
		registerSerializer(BOOLEAN);
		registerSerializer(ROTATIONS);
		registerSerializer(BLOCK_POS);
		registerSerializer(OPTIONAL_BLOCK_POS);
		registerSerializer(FACING);
		registerSerializer(OPTIONAL_UNIQUE_ID);
		registerSerializer(OPTIONAL_BLOCK_STATE);
		registerSerializer(COMPOUND_TAG);
	}

	public static void registerSerializer(DataSerializer<?> serializer) {

		REGISTRY.add(serializer);
	}

	
	public static DataSerializer<?> getSerializer(int id) {

		return REGISTRY.get(id);
	}

	public static int getSerializerId(DataSerializer<?> serializer) {

		return REGISTRY.getId(serializer);
	}
}
