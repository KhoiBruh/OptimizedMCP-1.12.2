package net.minecraft.client.audio;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Maths;

public class MovingSoundMinecart extends MovingSound {

	private final EntityMinecart minecart;
	private float distance = 0;

	public MovingSoundMinecart(EntityMinecart minecart) {
		super(SoundEvents.ENTITY_MINECART_RIDING, SoundCategory.NEUTRAL);
		this.minecart = minecart;
		repeat = true;
		repeatDelay = 0;
	}

	/**
	 * Like the old updateEntity(), except more generic.
	 */
	public void update() {
		if (minecart.isDead) {
			donePlaying = true;
		} else {
			x = (float) minecart.posX;
			y = (float) minecart.posY;
			z = (float) minecart.posZ;
			float f = Maths.sqrt(minecart.motionX * minecart.motionX + minecart.motionZ * minecart.motionZ);

			if (f >= 0.01) {
				distance = Maths.clamp(distance + 0.0025F, 0F, 1F);
				volume = Maths.clamp(f, 0F, 0.5F) * 0.7F;
			} else {
				distance = 0;
				volume = 0;
			}
		}
	}

}
