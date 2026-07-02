package net.minecraft.util.datafix.walkers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;

public abstract class Filtered implements IDataWalker {

	private final ResourceLocation key;

	public Filtered(Class<?> aClass) {
		if (Entity.class.isAssignableFrom(aClass)) {
			key = EntityList.getKey((Class<Entity>) aClass);
		} else if (TileEntity.class.isAssignableFrom(aClass)) {
			key = TileEntity.getKey((Class<TileEntity>) aClass);
		} else {
			key = null;
		}
	}

	public NBTTagCompound process(IDataFixer fixer, NBTTagCompound compound, int versionIn) {
		if ((new ResourceLocation(compound.getString("id"))).equals(key)) {
			compound = filteredProcess(fixer, compound, versionIn);
		}

		return compound;
	}

	abstract NBTTagCompound filteredProcess(IDataFixer fixer, NBTTagCompound compound, int versionIn);

}
