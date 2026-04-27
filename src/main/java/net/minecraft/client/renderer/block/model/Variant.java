package net.minecraft.client.renderer.block.model;

import com.google.gson.*;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;

public record Variant(ResourceLocation modelLocation, ModelRotation rotation, boolean uvLock, int weight) {

	public String toString() {

		return "Variant{modelLocation=" + modelLocation + ", rotation=" + rotation + ", uvLock=" + uvLock + ", weight=" + weight + '}';
	}

	public boolean equals(Object p_equals_1_) {

		if (this == p_equals_1_) {
			return true;
		} else if (!(p_equals_1_ instanceof Variant variant)) {
			return false;
		} else {
			return modelLocation.equals(variant.modelLocation) && rotation == variant.rotation && uvLock == variant.uvLock && weight == variant.weight;
		}
	}

	public int hashCode() {

		int i = modelLocation.hashCode();
		i = 31 * i + rotation.hashCode();
		i = 31 * i + Boolean.valueOf(uvLock).hashCode();
		i = 31 * i + weight;
		return i;
	}

	public static class Deserializer implements JsonDeserializer<Variant> {

		public Variant deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {

			JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
			String s = getStringModel(jsonobject);
			ModelRotation modelrotation = parseModelRotation(jsonobject);
			boolean flag = parseUvLock(jsonobject);
			int i = parseWeight(jsonobject);
			return new Variant(getResourceLocationBlock(s), modelrotation, flag, i);
		}

		private ResourceLocation getResourceLocationBlock(String p_188041_1_) {

			ResourceLocation resourcelocation = new ResourceLocation(p_188041_1_);
			resourcelocation = new ResourceLocation(resourcelocation.getResourceDomain(), "block/" + resourcelocation.getResourcePath());
			return resourcelocation;
		}

		private boolean parseUvLock(JsonObject json) {

			return JsonUtils.getBoolean(json, "uvlock", false);
		}

		protected ModelRotation parseModelRotation(JsonObject json) {

			int i = JsonUtils.getInt(json, "x", 0);
			int j = JsonUtils.getInt(json, "y", 0);
			ModelRotation modelrotation = ModelRotation.getModelRotation(i, j);

			if (modelrotation == null) {
				throw new JsonParseException("Invalid BlockModelRotation x: " + i + ", y: " + j);
			} else {
				return modelrotation;
			}
		}

		protected String getStringModel(JsonObject json) {

			return JsonUtils.getString(json, "model");
		}

		protected int parseWeight(JsonObject json) {

			int i = JsonUtils.getInt(json, "weight", 1);

			if (i < 1) {
				throw new JsonParseException("Invalid weight " + i + " found, expected integer >= 1");
			} else {
				return i;
			}
		}

	}

}
