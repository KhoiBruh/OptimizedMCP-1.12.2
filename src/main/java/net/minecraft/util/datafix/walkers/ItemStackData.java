package net.minecraft.util.datafix.walkers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.IDataFixer;

public class ItemStackData extends Filtered {

	private final String[] matchingTags;

	public ItemStackData(Class<?> p_i47311_1_, String... matchingTagsIn) {

		super(p_i47311_1_);
		matchingTags = matchingTagsIn;
	}

	NBTTagCompound filteredProcess(IDataFixer fixer, NBTTagCompound compound, int versionIn) {

		for (String s : matchingTags) {
			compound = DataFixesManager.processItemStack(fixer, compound, versionIn, s);
		}

		return compound;
	}

}
