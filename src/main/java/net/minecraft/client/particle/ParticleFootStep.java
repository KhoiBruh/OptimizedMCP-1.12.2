package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ParticleFootStep extends Particle {

	private static final ResourceLocation FOOTPRINT_TEXTURE = new ResourceLocation("textures/particle/footprint.png");
	private final int footstepMaxAge;
	private final TextureManager currentFootSteps;
	private int footstepAge;

	protected ParticleFootStep(TextureManager currentFootStepsIn, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn) {

		super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0D, 0D, 0D);
		currentFootSteps = currentFootStepsIn;
		motionX = 0D;
		motionY = 0D;
		motionZ = 0D;
		footstepMaxAge = 200;
	}

	/**
	 * Renders the particle
	 */
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		float f = ((float) footstepAge + partialTicks) / (float) footstepMaxAge;
		f = f * f;
		float f1 = 2F - f * 2F;

		if (f1 > 1F) {
			f1 = 1F;
		}

		f1 = f1 * 0.2F;
		GlStateManager.disableLighting();
		float f2 = 0.125F;
		float f3 = (float) (posX - interpPosX);
		float f4 = (float) (posY - interpPosY);
		float f5 = (float) (posZ - interpPosZ);
		float f6 = world.getLightBrightness(new BlockPos(posX, posY, posZ));
		currentFootSteps.bindTexture(FOOTPRINT_TEXTURE);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		buffer.pos(f3 - 0.125F, f4, f5 + 0.125F).tex(0D, 1D).color(f6, f6, f6, f1).endVertex();
		buffer.pos(f3 + 0.125F, f4, f5 + 0.125F).tex(1D, 1D).color(f6, f6, f6, f1).endVertex();
		buffer.pos(f3 + 0.125F, f4, f5 - 0.125F).tex(1D, 0D).color(f6, f6, f6, f1).endVertex();
		buffer.pos(f3 - 0.125F, f4, f5 - 0.125F).tex(0D, 0D).color(f6, f6, f6, f1).endVertex();
		Tessellator.getInstance().draw();
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
	}

	public void onUpdate() {

		++footstepAge;

		if (footstepAge == footstepMaxAge) {
			setExpired();
		}
	}

	/**
	 * Retrieve what effect layer (what texture) the particle should be rendered with. 0 for the particle sprite sheet,
	 * 1 for the main Texture atlas, and 3 for a custom texture
	 */
	public int getFXLayer() {

		return 3;
	}

	public static class Factory implements IParticleFactory {

		public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {

			return new ParticleFootStep(Minecraft.getMinecraft().getTextureManager(), worldIn, xCoordIn, yCoordIn, zCoordIn);
		}

	}

}
