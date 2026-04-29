package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import java.util.Map;

public class EnchantmentPredicate {

	/**
	 * The predicate that matches any set of enchantments.
	 */
	public static final EnchantmentPredicate ANY = new EnchantmentPredicate();
	private final Enchantment enchantment;
	private final MinMaxBounds levels;

	public EnchantmentPredicate() {

		enchantment = null;
		levels = MinMaxBounds.UNBOUNDED;
	}

	public EnchantmentPredicate(Enchantment enchantment, MinMaxBounds levels) {

		this.enchantment = enchantment;
		this.levels = levels;
	}

	public static EnchantmentPredicate deserialize(JsonElement element) {

		if (element != null && !element.isJsonNull()) {
			JsonObject jsonobject = JsonUtils.getJsonObject(element, "enchantment");
			Enchantment enchantment = null;

			if (jsonobject.has("enchantment")) {
				ResourceLocation resourcelocation = new ResourceLocation(JsonUtils.getString(jsonobject, "enchantment"));
				enchantment = Enchantment.REGISTRY.getObject(resourcelocation);

				if (enchantment == null) {
					throw new JsonSyntaxException("Unknown enchantment '" + resourcelocation + "'");
				}
			}

			MinMaxBounds minmaxbounds = MinMaxBounds.deserialize(jsonobject.get("levels"));
			return new EnchantmentPredicate(enchantment, minmaxbounds);
		} else {
			return ANY;
		}
	}

	public static EnchantmentPredicate[] deserializeArray(JsonElement element) {

		if (element != null && !element.isJsonNull()) {
			JsonArray jsonarray = JsonUtils.getJsonArray(element, "enchantments");
			EnchantmentPredicate[] aenchantmentpredicate = new EnchantmentPredicate[jsonarray.size()];

			for (int i = 0; i < aenchantmentpredicate.length; ++i) {
				aenchantmentpredicate[i] = deserialize(jsonarray.get(i));
			}

			return aenchantmentpredicate;
		} else {
			return new EnchantmentPredicate[0];
		}
	}

	public boolean test(Map<Enchantment, Integer> enchantmentsIn) {

		if (enchantment != null) {
			if (!enchantmentsIn.containsKey(enchantment)) {
				return false;
			}

			int i = enchantmentsIn.get(enchantment);

			return levels == null || levels.test((float) i);
		} else if (levels != null) {
			for (Integer integer : enchantmentsIn.values()) {
				if (levels.test((float) integer)) {
					return true;
				}
			}

			return false;
		}

		return true;
	}

}
