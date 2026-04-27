package net.minecraft.world.storage.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;

import java.util.Random;

public interface LootCondition {

	boolean testCondition(Random rand, LootContext context);

	abstract class Serializer<T extends LootCondition> {

		private final ResourceLocation lootTableLocation;
		private final Class<T> conditionClass;

		protected Serializer(ResourceLocation location, Class<T> clazz) {

			lootTableLocation = location;
			conditionClass = clazz;
		}

		public ResourceLocation getLootTableLocation() {

			return lootTableLocation;
		}

		public Class<T> getConditionClass() {

			return conditionClass;
		}

		public abstract void serialize(JsonObject json, T value, JsonSerializationContext context);

		public abstract T deserialize(JsonObject json, JsonDeserializationContext context);

	}

}
