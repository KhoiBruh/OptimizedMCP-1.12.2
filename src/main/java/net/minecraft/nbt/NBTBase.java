package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class NBTBase {

	public static final String[] NBT_TYPES = new String[]{"END", "BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BYTE[]", "STRING", "LIST", "COMPOUND", "INT[]", "LONG[]"};

	/**
	 * Creates a new NBTBase object that corresponds with the passed in id.
	 */
	protected static NBTBase createNewByType(byte id) {

		return switch (id) {
			case 0 -> new NBTTagEnd();
			case 1 -> new NBTTagByte();
			case 2 -> new NBTTagShort();
			case 3 -> new NBTTagInt();
			case 4 -> new NBTTagLong();
			case 5 -> new NBTTagFloat();
			case 6 -> new NBTTagDouble();
			case 7 -> new NBTTagByteArray();
			case 8 -> new NBTTagString();
			case 9 -> new NBTTagList();
			case 10 -> new NBTTagCompound();
			case 11 -> new NBTTagIntArray();
			case 12 -> new NBTTagLongArray();
			default -> null;
		};
	}

	public static String getTagTypeName(int p_193581_0_) {

		return switch (p_193581_0_) {
			case 0 -> "TAG_End";
			case 1 -> "TAG_Byte";
			case 2 -> "TAG_Short";
			case 3 -> "TAG_Int";
			case 4 -> "TAG_Long";
			case 5 -> "TAG_Float";
			case 6 -> "TAG_Double";
			case 7 -> "TAG_Byte_Array";
			case 8 -> "TAG_String";
			case 9 -> "TAG_List";
			case 10 -> "TAG_Compound";
			case 11 -> "TAG_Int_Array";
			case 12 -> "TAG_Long_Array";
			case 99 -> "Any Numeric Tag";
			default -> "UNKNOWN";
		};
	}

	/**
	 * Write the actual data contents of the tag, implemented in NBT extension classes
	 */
	abstract void write(DataOutput output) throws IOException;

	abstract void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException;

	public abstract String toString();

	/**
	 * Gets the type byte for the tag.
	 */
	public abstract byte getId();

	/**
	 * Creates a clone of the tag.
	 */
	public abstract NBTBase copy();

	/**
	 * Return whether this compound has no tags.
	 */
	public boolean hasNoTags() {

		return false;
	}

	public boolean equals(Object p_equals_1_) {

		return p_equals_1_ instanceof NBTBase && getId() == ((NBTBase) p_equals_1_).getId();
	}

	public int hashCode() {

		return getId();
	}

	protected String getString() {

		return toString();
	}

}
