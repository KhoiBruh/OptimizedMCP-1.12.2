package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.List;

public record VariantList(List<Variant> variantList) {

	public boolean equals(Object p_equals_1_) {

		if (this == p_equals_1_) {
			return true;
		} else if (p_equals_1_ instanceof VariantList variantlist) {
			return variantList.equals(variantlist.variantList);
		} else {
			return false;
		}
	}

	public static class Deserializer implements JsonDeserializer<VariantList> {

		public VariantList deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {

			List<Variant> list = Lists.newArrayList();

			if (p_deserialize_1_.isJsonArray()) {
				JsonArray jsonarray = p_deserialize_1_.getAsJsonArray();

				if (jsonarray.size() == 0) {
					throw new JsonParseException("Empty variant array");
				}

				for (JsonElement jsonelement : jsonarray) {
					list.add(p_deserialize_3_.deserialize(jsonelement, Variant.class));
				}
			} else {
				list.add(p_deserialize_3_.deserialize(p_deserialize_1_, Variant.class));
			}

			return new VariantList(list);
		}

	}

}
