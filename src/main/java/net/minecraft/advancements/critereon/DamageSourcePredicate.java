package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.JsonUtils;

public class DamageSourcePredicate {

	public static DamageSourcePredicate ANY = new DamageSourcePredicate();
	private final Boolean isProjectile;
	private final Boolean isExplosion;
	private final Boolean bypassesArmor;
	private final Boolean bypassesInvulnerability;
	private final Boolean bypassesMagic;
	private final Boolean isFire;
	private final Boolean isMagic;
	private final EntityPredicate directEntity;
	private final EntityPredicate sourceEntity;

	public DamageSourcePredicate() {

		isProjectile = null;
		isExplosion = null;
		bypassesArmor = null;
		bypassesInvulnerability = null;
		bypassesMagic = null;
		isFire = null;
		isMagic = null;
		directEntity = EntityPredicate.ANY;
		sourceEntity = EntityPredicate.ANY;
	}

	public DamageSourcePredicate(Boolean isProjectile, Boolean isExplosion, Boolean bypassesArmor, Boolean bypassesInvulnerability, Boolean bypassesMagic, Boolean isFire, Boolean isMagic, EntityPredicate directEntity, EntityPredicate sourceEntity) {

		this.isProjectile = isProjectile;
		this.isExplosion = isExplosion;
		this.bypassesArmor = bypassesArmor;
		this.bypassesInvulnerability = bypassesInvulnerability;
		this.bypassesMagic = bypassesMagic;
		this.isFire = isFire;
		this.isMagic = isMagic;
		this.directEntity = directEntity;
		this.sourceEntity = sourceEntity;
	}

	public static DamageSourcePredicate deserialize(JsonElement element) {

		if (element != null && !element.isJsonNull()) {
			JsonObject jsonobject = JsonUtils.getJsonObject(element, "damage type");
			Boolean obool = optionalBoolean(jsonobject, "is_projectile");
			Boolean obool1 = optionalBoolean(jsonobject, "is_explosion");
			Boolean obool2 = optionalBoolean(jsonobject, "bypasses_armor");
			Boolean obool3 = optionalBoolean(jsonobject, "bypasses_invulnerability");
			Boolean obool4 = optionalBoolean(jsonobject, "bypasses_magic");
			Boolean obool5 = optionalBoolean(jsonobject, "is_fire");
			Boolean obool6 = optionalBoolean(jsonobject, "is_magic");
			EntityPredicate entitypredicate = EntityPredicate.deserialize(jsonobject.get("direct_entity"));
			EntityPredicate entitypredicate1 = EntityPredicate.deserialize(jsonobject.get("source_entity"));
			return new DamageSourcePredicate(obool, obool1, obool2, obool3, obool4, obool5, obool6, entitypredicate, entitypredicate1);
		} else {
			return ANY;
		}
	}

	
	private static Boolean optionalBoolean(JsonObject object, String memberName) {

		return object.has(memberName) ? JsonUtils.getBoolean(object, memberName) : null;
	}

	public boolean test(EntityPlayerMP player, DamageSource source) {

		if (this == ANY) {
			return true;
		} else if (isProjectile != null && isProjectile != source.isProjectile()) {
			return false;
		} else if (isExplosion != null && isExplosion != source.isExplosion()) {
			return false;
		} else if (bypassesArmor != null && bypassesArmor != source.isUnblockable()) {
			return false;
		} else if (bypassesInvulnerability != null && bypassesInvulnerability != source.canHarmInCreative()) {
			return false;
		} else if (bypassesMagic != null && bypassesMagic != source.isDamageAbsolute()) {
			return false;
		} else if (isFire != null && isFire != source.isFireDamage()) {
			return false;
		} else if (isMagic != null && isMagic != source.isMagicDamage()) {
			return false;
		} else if (!directEntity.test(player, source.getImmediateSource())) {
			return false;
		} else {
			return sourceEntity.test(player, source.getTrueSource());
		}
	}

}
