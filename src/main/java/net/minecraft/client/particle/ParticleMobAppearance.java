package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityElderGuardian;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ParticleMobAppearance extends Particle {

	private EntityLivingBase entity;

	protected ParticleMobAppearance(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn) {

		super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0D, 0D, 0D);
		particleRed = 1F;
		particleGreen = 1F;
		particleBlue = 1F;
		motionX = 0D;
		motionY = 0D;
		motionZ = 0D;
		particleGravity = 0F;
		particleMaxAge = 30;
	}

	/**
	 * Retrieve what effect layer (what texture) the particle should be rendered with. 0 for the particle sprite sheet,
	 * 1 for the main Texture atlas, and 3 for a custom texture
	 */
	public int getFXLayer() {

		return 3;
	}

	public void onUpdate() {

		super.onUpdate();

		if (entity == null) {
			EntityElderGuardian entityelderguardian = new EntityElderGuardian(world);
			entityelderguardian.setGhost();
			entity = entityelderguardian;
		}
	}

	/**
	 * Renders the particle
	 */
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		if (entity != null) {
			RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
			rendermanager.setRenderPosition(Particle.interpPosX, Particle.interpPosY, Particle.interpPosZ);
			float f = 0.42553192F;
			float f1 = ((float) particleAge + partialTicks) / (float) particleMaxAge;
			GlStateManager.depthMask(true);
			GlStateManager.enableBlend();
			GlStateManager.enableDepth();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			float f2 = 240F;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
			GlStateManager.pushMatrix();
			float f3 = 0.05F + 0.5F * MathHelper.sin(f1 * (float) Math.PI);
			GlStateManager.color(1F, 1F, 1F, f3);
			GlStateManager.translate(0F, 1.8F, 0F);
			GlStateManager.rotate(180F - entityIn.rotationYaw, 0F, 1F, 0F);
			GlStateManager.rotate(60F - 150F * f1 - entityIn.rotationPitch, 1F, 0F, 0F);
			GlStateManager.translate(0F, -0.4F, -1.5F);
			GlStateManager.scale(0.42553192F, 0.42553192F, 0.42553192F);
			entity.rotationYaw = 0F;
			entity.rotationYawHead = 0F;
			entity.prevRotationYaw = 0F;
			entity.prevRotationYawHead = 0F;
			rendermanager.renderEntity(entity, 0D, 0D, 0D, 0F, partialTicks, false);
			GlStateManager.popMatrix();
			GlStateManager.enableDepth();
		}
	}

	public static class Factory implements IParticleFactory {

		public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {

			return new ParticleMobAppearance(worldIn, xCoordIn, yCoordIn, zCoordIn);
		}

	}

}
