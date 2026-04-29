package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ParticleExplosionLarge extends Particle {

	private static final ResourceLocation EXPLOSION_TEXTURE = new ResourceLocation("textures/entity/explosion.png");
	private static final VertexFormat VERTEX_FORMAT = (new VertexFormat()).addElement(DefaultVertexFormats.POSITION_3F).addElement(DefaultVertexFormats.TEX_2F).addElement(DefaultVertexFormats.COLOR_4UB).addElement(DefaultVertexFormats.TEX_2S).addElement(DefaultVertexFormats.NORMAL_3B).addElement(DefaultVertexFormats.PADDING_1B);
	private final int lifeTime;
	/**
	 * The Rendering Engine.
	 */
	private final TextureManager textureManager;
	private final float size;
	private int life;

	protected ParticleExplosionLarge(TextureManager textureManagerIn, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double p_i1213_9_, double p_i1213_11_, double p_i1213_13_) {

		super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0D, 0D, 0D);
		textureManager = textureManagerIn;
		lifeTime = 6 + rand.nextInt(4);
		float f = rand.nextFloat() * 0.6F + 0.4F;
		particleRed = f;
		particleGreen = f;
		particleBlue = f;
		size = 1F - (float) p_i1213_9_ * 0.5F;
	}

	/**
	 * Renders the particle
	 */
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		int i = (int) (((float) life + partialTicks) * 15F / (float) lifeTime);

		if (i <= 15) {
			textureManager.bindTexture(EXPLOSION_TEXTURE);
			float f = (float) (i % 4) / 4F;
			float f1 = f + 0.24975F;
			float f2 = (float) (i / 4) / 4F;
			float f3 = f2 + 0.24975F;
			float f4 = 2F * size;
			float f5 = (float) (prevPosX + (posX - prevPosX) * (double) partialTicks - interpPosX);
			float f6 = (float) (prevPosY + (posY - prevPosY) * (double) partialTicks - interpPosY);
			float f7 = (float) (prevPosZ + (posZ - prevPosZ) * (double) partialTicks - interpPosZ);
			GlStateManager.color(1F, 1F, 1F, 1F);
			GlStateManager.disableLighting();
			RenderHelper.disableStandardItemLighting();
			buffer.begin(7, VERTEX_FORMAT);
			buffer.pos(f5 - rotationX * f4 - rotationXY * f4, f6 - rotationZ * f4, f7 - rotationYZ * f4 - rotationXZ * f4).tex(f1, f3).color(particleRed, particleGreen, particleBlue, 1F).lightmap(0, 240).normal(0F, 1F, 0F).endVertex();
			buffer.pos(f5 - rotationX * f4 + rotationXY * f4, f6 + rotationZ * f4, f7 - rotationYZ * f4 + rotationXZ * f4).tex(f1, f2).color(particleRed, particleGreen, particleBlue, 1F).lightmap(0, 240).normal(0F, 1F, 0F).endVertex();
			buffer.pos(f5 + rotationX * f4 + rotationXY * f4, f6 + rotationZ * f4, f7 + rotationYZ * f4 + rotationXZ * f4).tex(f, f2).color(particleRed, particleGreen, particleBlue, 1F).lightmap(0, 240).normal(0F, 1F, 0F).endVertex();
			buffer.pos(f5 + rotationX * f4 - rotationXY * f4, f6 - rotationZ * f4, f7 + rotationYZ * f4 - rotationXZ * f4).tex(f, f3).color(particleRed, particleGreen, particleBlue, 1F).lightmap(0, 240).normal(0F, 1F, 0F).endVertex();
			Tessellator.getInstance().draw();
			GlStateManager.enableLighting();
		}
	}

	public int getBrightnessForRender(float p_189214_1_) {

		return 61680;
	}

	public void onUpdate() {

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		++life;

		if (life == lifeTime) {
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

			return new ParticleExplosionLarge(Minecraft.getMinecraft().getTextureManager(), worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
		}

	}

}
