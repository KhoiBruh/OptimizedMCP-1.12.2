package net.minecraft.client.particle;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class ParticlePortal extends Particle {

	private final float portalParticleScale;
	private final double portalPosX;
	private final double portalPosY;
	private final double portalPosZ;

	protected ParticlePortal(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {

		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
		motionX = xSpeedIn;
		motionY = ySpeedIn;
		motionZ = zSpeedIn;
		posX = xCoordIn;
		posY = yCoordIn;
		posZ = zCoordIn;
		portalPosX = posX;
		portalPosY = posY;
		portalPosZ = posZ;
		float f = rand.nextFloat() * 0.6F + 0.4F;
		particleScale = rand.nextFloat() * 0.2F + 0.5F;
		portalParticleScale = particleScale;
		particleRed = f * 0.9F;
		particleGreen = f * 0.3F;
		particleBlue = f;
		particleMaxAge = (int) (Math.random() * 10D) + 40;
		setParticleTextureIndex((int) (Math.random() * 8D));
	}

	public void move(double x, double y, double z) {

		setBoundingBox(getBoundingBox().offset(x, y, z));
		resetPositionToBB();
	}

	/**
	 * Renders the particle
	 */
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		float f = ((float) particleAge + partialTicks) / (float) particleMaxAge;
		f = 1F - f;
		f = f * f;
		f = 1F - f;
		particleScale = portalParticleScale * f;
		super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}

	public int getBrightnessForRender(float p_189214_1_) {

		int i = super.getBrightnessForRender(p_189214_1_);
		float f = (float) particleAge / (float) particleMaxAge;
		f = f * f;
		f = f * f;
		int j = i & 255;
		int k = i >> 16 & 255;
		k = k + (int) (f * 15F * 16F);

		if (k > 240) {
			k = 240;
		}

		return j | k << 16;
	}

	public void onUpdate() {

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		float f = (float) particleAge / (float) particleMaxAge;
		float f1 = -f + f * f * 2F;
		float f2 = 1F - f1;
		posX = portalPosX + motionX * (double) f2;
		posY = portalPosY + motionY * (double) f2 + (double) (1F - f);
		posZ = portalPosZ + motionZ * (double) f2;

		if (particleAge++ >= particleMaxAge) {
			setExpired();
		}
	}

	public static class Factory implements IParticleFactory {

		public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {

			return new ParticlePortal(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
		}

	}

}
