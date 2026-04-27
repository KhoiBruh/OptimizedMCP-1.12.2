package net.minecraft.util;

import com.google.common.collect.Maps;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class CooldownTracker {

	private final Map<Item, CooldownTracker.Cooldown> cooldowns = Maps.newHashMap();
	private int ticks;

	public boolean hasCooldown(Item itemIn) {

		return getCooldown(itemIn, 0.0F) > 0.0F;
	}

	public float getCooldown(Item itemIn, float partialTicks) {

		CooldownTracker.Cooldown cooldowntracker$cooldown = cooldowns.get(itemIn);

		if (cooldowntracker$cooldown != null) {
			float f = (float) (cooldowntracker$cooldown.expireTicks - cooldowntracker$cooldown.createTicks);
			float f1 = (float) cooldowntracker$cooldown.expireTicks - ((float) ticks + partialTicks);
			return MathHelper.clamp(f1 / f, 0.0F, 1.0F);
		} else {
			return 0.0F;
		}
	}

	public void tick() {

		++ticks;

		if (!cooldowns.isEmpty()) {
			Iterator<Entry<Item, CooldownTracker.Cooldown>> iterator = cooldowns.entrySet().iterator();

			while (iterator.hasNext()) {
				Entry<Item, CooldownTracker.Cooldown> entry = iterator.next();

				if ((entry.getValue()).expireTicks <= ticks) {
					iterator.remove();
					notifyOnRemove(entry.getKey());
				}
			}
		}
	}

	public void setCooldown(Item itemIn, int ticksIn) {

		cooldowns.put(itemIn, new CooldownTracker.Cooldown(ticks, ticks + ticksIn));
		notifyOnSet(itemIn, ticksIn);
	}

	public void removeCooldown(Item itemIn) {

		cooldowns.remove(itemIn);
		notifyOnRemove(itemIn);
	}

	protected void notifyOnSet(Item itemIn, int ticksIn) {

	}

	protected void notifyOnRemove(Item itemIn) {

	}

	class Cooldown {

		final int createTicks;
		final int expireTicks;

		private Cooldown(int createTicksIn, int expireTicksIn) {

			createTicks = createTicksIn;
			expireTicks = expireTicksIn;
		}

	}

}
