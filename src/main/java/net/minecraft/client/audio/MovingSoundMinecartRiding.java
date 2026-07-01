package net.minecraft.client.audio;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;

public class MovingSoundMinecartRiding extends MovingSound {

	private final EntityPlayer player;
	private final EntityMinecart minecart;

	public MovingSoundMinecartRiding(EntityPlayer player, EntityMinecart minecart) {
		super(SoundEvents.ENTITY_MINECART_INSIDE, SoundCategory.NEUTRAL);
		this.player = player;
		this.minecart = minecart;
		attenuationType = ISound.AttenuationType.NONE;
		repeat = true;
		repeatDelay = 0;
	}

	public void update() {
		if (!minecart.isDead && player.isRiding() && player.getRidingEntity() == minecart) {
			float f = MathHelper.sqrt(minecart.motionX * minecart.motionX + minecart.motionZ * minecart.motionZ);

			if (f >= 0.01) {
				volume = MathHelper.clamp(f, 0, 1) * 0.75F;
			} else {
				volume = 0;
			}
		} else {
			donePlaying = true;
		}
	}

}
