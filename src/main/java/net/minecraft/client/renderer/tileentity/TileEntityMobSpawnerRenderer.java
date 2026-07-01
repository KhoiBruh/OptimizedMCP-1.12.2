package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLS;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntityMobSpawner;

public class TileEntityMobSpawnerRenderer extends TileEntitySpecialRenderer<TileEntityMobSpawner> {

	/**
	 * Render the mob inside the mob spawner.
	 */
	public static void renderMob(MobSpawnerBaseLogic mobSpawnerLogic, double posX, double posY, double posZ, float partialTicks) {

		Entity entity = mobSpawnerLogic.getCachedEntity();

		if (entity != null) {
			float f = 0.53125F;
			float f1 = Math.max(entity.width, entity.height);

			if ((double) f1 > 1D) {
				f /= f1;
			}

			GLS.translate(0F, 0.4F, 0F);
			GLS.rotate((float) (mobSpawnerLogic.getPrevMobRotation() + (mobSpawnerLogic.getMobRotation() - mobSpawnerLogic.getPrevMobRotation()) * (double) partialTicks) * 10F, 0F, 1F, 0F);
			GLS.translate(0F, -0.2F, 0F);
			GLS.rotate(-30F, 1F, 0F, 0F);
			GLS.scale(f, f, f);
			entity.setLocationAndAngles(posX, posY, posZ, 0F, 0F);
			Minecraft.getMinecraft().getRenderManager().renderEntity(entity, 0D, 0D, 0D, 0F, partialTicks, false);
		}
	}

	public void render(TileEntityMobSpawner te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		GLS.pushMatrix();
		GLS.translate((float) x + 0.5F, (float) y, (float) z + 0.5F);
		renderMob(te.getSpawnerBaseLogic(), x, y, z, partialTicks);
		GLS.popMatrix();
	}

}
