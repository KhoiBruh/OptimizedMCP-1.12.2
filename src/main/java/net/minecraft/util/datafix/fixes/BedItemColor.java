package net.minecraft.util.datafix.fixes;

import net.minecraft.item.DyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class BedItemColor implements IFixableData {

	public int getFixVersion() {

		return 1125;
	}

	public NBTTagCompound fixTagCompound(NBTTagCompound compound) {

		if ("minecraft:bed".equals(compound.getString("id")) && compound.getShort("Damage") == 0) {
			compound.setShort("Damage", (short) DyeColor.RED.getMetadata());
		}

		return compound;
	}

}
