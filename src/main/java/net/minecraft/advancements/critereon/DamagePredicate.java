package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.JsonUtils;

import javax.annotation.Nullable;

public class DamagePredicate {

	public static DamagePredicate ANY = new DamagePredicate();
	private final MinMaxBounds dealt;
	private final MinMaxBounds taken;
	private final EntityPredicate sourceEntity;
	private final Boolean blocked;
	private final DamageSourcePredicate type;

	public DamagePredicate() {

		dealt = MinMaxBounds.UNBOUNDED;
		taken = MinMaxBounds.UNBOUNDED;
		sourceEntity = EntityPredicate.ANY;
		blocked = null;
		type = DamageSourcePredicate.ANY;
	}

	public DamagePredicate(MinMaxBounds dealt, MinMaxBounds taken, EntityPredicate sourceEntity, @Nullable Boolean blocked, DamageSourcePredicate type) {

		this.dealt = dealt;
		this.taken = taken;
		this.sourceEntity = sourceEntity;
		this.blocked = blocked;
		this.type = type;
	}

	public static DamagePredicate deserialize(@Nullable JsonElement element) {

		if (element != null && !element.isJsonNull()) {
			JsonObject jsonobject = JsonUtils.getJsonObject(element, "damage");
			MinMaxBounds minmaxbounds = MinMaxBounds.deserialize(jsonobject.get("dealt"));
			MinMaxBounds minmaxbounds1 = MinMaxBounds.deserialize(jsonobject.get("taken"));
			Boolean obool = jsonobject.has("blocked") ? JsonUtils.getBoolean(jsonobject, "blocked") : null;
			EntityPredicate entitypredicate = EntityPredicate.deserialize(jsonobject.get("source_entity"));
			DamageSourcePredicate damagesourcepredicate = DamageSourcePredicate.deserialize(jsonobject.get("type"));
			return new DamagePredicate(minmaxbounds, minmaxbounds1, entitypredicate, obool, damagesourcepredicate);
		} else {
			return ANY;
		}
	}

	public boolean test(EntityPlayerMP player, DamageSource source, float dealt, float taken, boolean blocked) {

		if (this == ANY) {
			return true;
		} else if (!this.dealt.test(dealt)) {
			return false;
		} else if (!this.taken.test(taken)) {
			return false;
		} else if (!sourceEntity.test(player, source.getTrueSource())) {
			return false;
		} else if (this.blocked != null && this.blocked.booleanValue() != blocked) {
			return false;
		} else {
			return type.test(player, source);
		}
	}

}
