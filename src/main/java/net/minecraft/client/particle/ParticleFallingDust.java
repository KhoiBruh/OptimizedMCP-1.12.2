package net.minecraft.client.particle;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ParticleFallingDust extends Particle {

	final float rotSpeed;
	float oSize;

	protected ParticleFallingDust(World p_i47135_1_, double p_i47135_2_, double p_i47135_4_, double p_i47135_6_, float p_i47135_8_, float p_i47135_9_, float p_i47135_10_) {

		super(p_i47135_1_, p_i47135_2_, p_i47135_4_, p_i47135_6_, 0D, 0D, 0D);
		motionX = 0D;
		motionY = 0D;
		motionZ = 0D;
		particleRed = p_i47135_8_;
		particleGreen = p_i47135_9_;
		particleBlue = p_i47135_10_;
		float f = 0.9F;
		particleScale *= 0.75F;
		particleScale *= 0.9F;
		oSize = particleScale;
		particleMaxAge = (int) (32D / (Math.random() * 0.8D + 0.2D));
		particleMaxAge = (int) ((float) particleMaxAge * 0.9F);
		rotSpeed = ((float) Math.random() - 0.5F) * 0.1F;
		particleAngle = (float) Math.random() * ((float) Math.PI * 2F);
	}

	/**
	 * Renders the particle
	 */
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		float f = ((float) particleAge + partialTicks) / (float) particleMaxAge * 32F;
		f = MathHelper.clamp(f, 0F, 1F);
		particleScale = oSize * f;
		super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}

	public void onUpdate() {

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		if (particleAge++ >= particleMaxAge) {
			setExpired();
		}

		prevParticleAngle = particleAngle;
		particleAngle += (float) Math.PI * rotSpeed * 2F;

		if (onGround) {
			prevParticleAngle = particleAngle = 0F;
		}

		setParticleTextureIndex(7 - particleAge * 8 / particleMaxAge);
		move(motionX, motionY, motionZ);
		motionY -= 0.003000000026077032D;
		motionY = Math.max(motionY, -0.14000000059604645D);
	}

	public static class Factory implements IParticleFactory {

		
		public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {

			IBlockState iblockstate = Block.getStateById(p_178902_15_[0]);

			if (iblockstate.getBlock() != Blocks.AIR && iblockstate.getRenderType() == EnumBlockRenderType.INVISIBLE) {
				return null;
			} else {
				int i = Minecraft.getMinecraft().getBlockColors().getColor(iblockstate, worldIn, new BlockPos(xCoordIn, yCoordIn, zCoordIn));

				if (iblockstate.getBlock() instanceof BlockFalling) {
					i = ((BlockFalling) iblockstate.getBlock()).getDustColor(iblockstate);
				}

				float f = (float) (i >> 16 & 255) / 255F;
				float f1 = (float) (i >> 8 & 255) / 255F;
				float f2 = (float) (i & 255) / 255F;
				return new ParticleFallingDust(worldIn, xCoordIn, yCoordIn, zCoordIn, f, f1, f2);
			}
		}

	}

}
