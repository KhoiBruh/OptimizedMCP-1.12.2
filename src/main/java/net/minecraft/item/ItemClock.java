package net.minecraft.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemClock extends Item {

	public ItemClock() {

		addPropertyOverride(new ResourceLocation("time"), new IItemPropertyGetter() {
			double rotation;
			double rota;
			long lastUpdateTick;

			public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn) {

				boolean flag = entityIn != null;
				Entity entity = flag ? entityIn : stack.getItemFrame();

				if (worldIn == null && entity != null) {
					worldIn = entity.world;
				}

				if (worldIn == null) {
					return 0F;
				} else {
					double d0;

					if (worldIn.provider.isSurfaceWorld()) {
						d0 = worldIn.getCelestialAngle(1F);
					} else {
						d0 = Math.random();
					}

					d0 = wobble(worldIn, d0);
					return (float) d0;
				}
			}

			private double wobble(World p_185087_1_, double p_185087_2_) {

				if (p_185087_1_.getTotalWorldTime() != lastUpdateTick) {
					lastUpdateTick = p_185087_1_.getTotalWorldTime();
					double d0 = p_185087_2_ - rotation;
					d0 = MathHelper.positiveModulo(d0 + 0.5D, 1D) - 0.5D;
					rota += d0 * 0.1D;
					rota *= 0.9D;
					rotation = MathHelper.positiveModulo(rotation + rota, 1D);
				}

				return rotation;
			}
		});
	}

}
