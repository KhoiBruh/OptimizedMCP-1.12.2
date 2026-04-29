package net.minecraft.entity;

import net.minecraft.entity.ai.attributes.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Collection;
import java.util.UUID;

public class SharedMonsterAttributes {

	public static final IAttribute MAX_HEALTH = (new RangedAttribute(null, "generic.maxHealth", 20D, 0D, 1024D)).setDescription("Max Health").setShouldWatch(true);
	public static final IAttribute FOLLOW_RANGE = (new RangedAttribute(null, "generic.followRange", 32D, 0D, 2048D)).setDescription("Follow Range");
	public static final IAttribute KNOCKBACK_RESISTANCE = (new RangedAttribute(null, "generic.knockbackResistance", 0D, 0D, 1D)).setDescription("Knockback Resistance");
	public static final IAttribute MOVEMENT_SPEED = (new RangedAttribute(null, "generic.movementSpeed", 0.699999988079071D, 0D, 1024D)).setDescription("Movement Speed").setShouldWatch(true);
	public static final IAttribute FLYING_SPEED = (new RangedAttribute(null, "generic.flyingSpeed", 0.4000000059604645D, 0D, 1024D)).setDescription("Flying Speed").setShouldWatch(true);
	public static final IAttribute ATTACK_DAMAGE = new RangedAttribute(null, "generic.attackDamage", 2D, 0D, 2048D);
	public static final IAttribute ATTACK_SPEED = (new RangedAttribute(null, "generic.attackSpeed", 4D, 0D, 1024D)).setShouldWatch(true);
	public static final IAttribute ARMOR = (new RangedAttribute(null, "generic.armor", 0D, 0D, 30D)).setShouldWatch(true);
	public static final IAttribute ARMOR_TOUGHNESS = (new RangedAttribute(null, "generic.armorToughness", 0D, 0D, 20D)).setShouldWatch(true);
	public static final IAttribute LUCK = (new RangedAttribute(null, "generic.luck", 0D, -1024D, 1024D)).setShouldWatch(true);
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Creates an NBTTagList from a BaseAttributeMap, including all its AttributeInstances
	 */
	public static NBTTagList writeBaseAttributeMapToNBT(AbstractAttributeMap map) {

		NBTTagList nbttaglist = new NBTTagList();

		for (IAttributeInstance iattributeinstance : map.getAllAttributes()) {
			nbttaglist.appendTag(writeAttributeInstanceToNBT(iattributeinstance));
		}

		return nbttaglist;
	}

	/**
	 * Creates an NBTTagCompound from an AttributeInstance, including its AttributeModifiers
	 */
	private static NBTTagCompound writeAttributeInstanceToNBT(IAttributeInstance instance) {

		NBTTagCompound nbttagcompound = new NBTTagCompound();
		IAttribute iattribute = instance.getAttribute();
		nbttagcompound.setString("Name", iattribute.getName());
		nbttagcompound.setDouble("Base", instance.getBaseValue());
		Collection<AttributeModifier> collection = instance.getModifiers();

		if (collection != null && !collection.isEmpty()) {
			NBTTagList nbttaglist = new NBTTagList();

			for (AttributeModifier attributemodifier : collection) {
				if (attributemodifier.isSaved()) {
					nbttaglist.appendTag(writeAttributeModifierToNBT(attributemodifier));
				}
			}

			nbttagcompound.setTag("Modifiers", nbttaglist);
		}

		return nbttagcompound;
	}

	/**
	 * Creates an NBTTagCompound from an AttributeModifier
	 */
	public static NBTTagCompound writeAttributeModifierToNBT(AttributeModifier modifier) {

		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setString("Name", modifier.getName());
		nbttagcompound.setDouble("Amount", modifier.getAmount());
		nbttagcompound.setInteger("Operation", modifier.getOperation());
		nbttagcompound.setUniqueId("UUID", modifier.getID());
		return nbttagcompound;
	}

	public static void setAttributeModifiers(AbstractAttributeMap map, NBTTagList list) {

		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
			IAttributeInstance iattributeinstance = map.getAttributeInstanceByName(nbttagcompound.getString("Name"));

			if (iattributeinstance == null) {
				LOGGER.warn("Ignoring unknown attribute '{}'", nbttagcompound.getString("Name"));
			} else {
				applyModifiersToAttributeInstance(iattributeinstance, nbttagcompound);
			}
		}
	}

	private static void applyModifiersToAttributeInstance(IAttributeInstance instance, NBTTagCompound compound) {

		instance.setBaseValue(compound.getDouble("Base"));

		if (compound.hasKey("Modifiers", 9)) {
			NBTTagList nbttaglist = compound.getTagList("Modifiers", 10);

			for (int i = 0; i < nbttaglist.tagCount(); ++i) {
				AttributeModifier attributemodifier = readAttributeModifierFromNBT(nbttaglist.getCompoundTagAt(i));

				if (attributemodifier != null) {
					AttributeModifier attributemodifier1 = instance.getModifier(attributemodifier.getID());

					if (attributemodifier1 != null) {
						instance.removeModifier(attributemodifier1);
					}

					instance.applyModifier(attributemodifier);
				}
			}
		}
	}

	

	/**
	 * Creates an AttributeModifier from an NBTTagCompound
	 */
	public static AttributeModifier readAttributeModifierFromNBT(NBTTagCompound compound) {

		UUID uuid = compound.getUniqueId("UUID");

		try {
			return new AttributeModifier(uuid, compound.getString("Name"), compound.getDouble("Amount"), compound.getInteger("Operation"));
		} catch (Exception exception) {
			LOGGER.warn("Unable to create attribute: {}", exception.getMessage());
			return null;
		}
	}

}
