package net.minecraft.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemCompass extends Item {

	public ItemCompass() {

		addPropertyOverride(new ResourceLocation("angle"), new IItemPropertyGetter() {
			double rotation;
			double rota;
			long lastUpdateTick;

			public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn) {

				if (entityIn == null && !stack.isOnItemFrame()) {
					return 0F;
				} else {
					boolean flag = entityIn != null;
					Entity entity = flag ? entityIn : stack.getItemFrame();

					if (worldIn == null) {
						worldIn = entity.world;
					}

					double d0;

					if (worldIn.provider.isSurfaceWorld()) {
						double d1 = flag ? (double) entity.rotationYaw : getFrameRotation((EntityItemFrame) entity);
						d1 = MathHelper.positiveModulo(d1 / 360D, 1D);
						double d2 = getSpawnToAngle(worldIn, entity) / (Math.PI * 2D);
						d0 = 0.5D - (d1 - 0.25D - d2);
					} else {
						d0 = Math.random();
					}

					if (flag) {
						d0 = wobble(worldIn, d0);
					}

					return MathHelper.positiveModulo((float) d0, 1F);
				}
			}

			private double wobble(World worldIn, double p_185093_2_) {

				if (worldIn.getTotalWorldTime() != lastUpdateTick) {
					lastUpdateTick = worldIn.getTotalWorldTime();
					double d0 = p_185093_2_ - rotation;
					d0 = MathHelper.positiveModulo(d0 + 0.5D, 1D) - 0.5D;
					rota += d0 * 0.1D;
					rota *= 0.8D;
					rotation = MathHelper.positiveModulo(rotation + rota, 1D);
				}

				return rotation;
			}

			private double getFrameRotation(EntityItemFrame p_185094_1_) {

				return MathHelper.wrapDegrees(180 + p_185094_1_.facingDirection.getHorizontalIndex() * 90);
			}

			private double getSpawnToAngle(World p_185092_1_, Entity p_185092_2_) {

				BlockPos blockpos = p_185092_1_.getSpawnPoint();
				return Math.atan2((double) blockpos.getZ() - p_185092_2_.posZ, (double) blockpos.getX() - p_185092_2_.posX);
			}
		});
	}

}
