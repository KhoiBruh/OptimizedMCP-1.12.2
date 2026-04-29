package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.JsonUtils;

public class NBTPredicate {

	/**
	 * The predicate that matches any NBT tag.
	 */
	public static final NBTPredicate ANY = new NBTPredicate(null);

	
	private final NBTTagCompound tag;

	public NBTPredicate(NBTTagCompound tag) {

		this.tag = tag;
	}

	public static NBTPredicate deserialize(JsonElement json) {

		if (json != null && !json.isJsonNull()) {
			NBTTagCompound nbttagcompound;

			try {
				nbttagcompound = JsonToNBT.getTagFromJson(JsonUtils.getString(json, "nbt"));
			} catch (NBTException nbtexception) {
				throw new JsonSyntaxException("Invalid nbt tag: " + nbtexception.getMessage());
			}

			return new NBTPredicate(nbttagcompound);
		} else {
			return ANY;
		}
	}

	public boolean test(ItemStack item) {

		return this == ANY || test(item.getTagCompound());
	}

	public boolean test(Entity entityIn) {

		return this == ANY || test(CommandBase.entityToNBT(entityIn));
	}

	public boolean test(NBTBase nbt) {

		if (nbt == null) {
			return this == ANY;
		} else {
			return tag == null || NBTUtil.areNBTEquals(tag, nbt, true);
		}
	}

}
