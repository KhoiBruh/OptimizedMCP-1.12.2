package net.minecraft.client.particle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.*;

public class ParticleManager {

	private static final ResourceLocation PARTICLE_TEXTURES = new ResourceLocation("textures/particle/particles.png");
	private final ArrayDeque<Particle>[][] fxLayers = new ArrayDeque[4][];
	private final Queue<ParticleEmitter> particleEmitters = Queues.newArrayDeque();
	private final TextureManager renderer;
	/**
	 * RNG.
	 */
	private final Random rand = new Random();
	private final Map<Integer, IParticleFactory> particleTypes = Maps.newHashMap();
	private final Queue<Particle> queue = Queues.newArrayDeque();
	/**
	 * Reference to the World object.
	 */
	protected World world;

	public ParticleManager(World worldIn, TextureManager rendererIn) {

		world = worldIn;
		renderer = rendererIn;

		for (int i = 0; i < 4; ++i) {
			fxLayers[i] = new ArrayDeque[2];

			for (int j = 0; j < 2; ++j) {
				fxLayers[i][j] = Queues.newArrayDeque();
			}
		}

		registerVanillaParticles();
	}

	private void registerVanillaParticles() {

		registerParticle(ParticleTypes.EXPLOSION_NORMAL.getParticleID(), new ParticleExplosion.Factory());
		registerParticle(ParticleTypes.SPIT.getParticleID(), new ParticleSpit.Factory());
		registerParticle(ParticleTypes.WATER_BUBBLE.getParticleID(), new ParticleBubble.Factory());
		registerParticle(ParticleTypes.WATER_SPLASH.getParticleID(), new ParticleSplash.Factory());
		registerParticle(ParticleTypes.WATER_WAKE.getParticleID(), new ParticleWaterWake.Factory());
		registerParticle(ParticleTypes.WATER_DROP.getParticleID(), new ParticleRain.Factory());
		registerParticle(ParticleTypes.SUSPENDED.getParticleID(), new ParticleSuspend.Factory());
		registerParticle(ParticleTypes.SUSPENDED_DEPTH.getParticleID(), new ParticleSuspendedTown.Factory());
		registerParticle(ParticleTypes.CRIT.getParticleID(), new ParticleCrit.Factory());
		registerParticle(ParticleTypes.CRIT_MAGIC.getParticleID(), new ParticleCrit.MagicFactory());
		registerParticle(ParticleTypes.SMOKE_NORMAL.getParticleID(), new ParticleSmokeNormal.Factory());
		registerParticle(ParticleTypes.SMOKE_LARGE.getParticleID(), new ParticleSmokeLarge.Factory());
		registerParticle(ParticleTypes.SPELL.getParticleID(), new ParticleSpell.Factory());
		registerParticle(ParticleTypes.SPELL_INSTANT.getParticleID(), new ParticleSpell.InstantFactory());
		registerParticle(ParticleTypes.SPELL_MOB.getParticleID(), new ParticleSpell.MobFactory());
		registerParticle(ParticleTypes.SPELL_MOB_AMBIENT.getParticleID(), new ParticleSpell.AmbientMobFactory());
		registerParticle(ParticleTypes.SPELL_WITCH.getParticleID(), new ParticleSpell.WitchFactory());
		registerParticle(ParticleTypes.DRIP_WATER.getParticleID(), new ParticleDrip.WaterFactory());
		registerParticle(ParticleTypes.DRIP_LAVA.getParticleID(), new ParticleDrip.LavaFactory());
		registerParticle(ParticleTypes.VILLAGER_ANGRY.getParticleID(), new ParticleHeart.AngryVillagerFactory());
		registerParticle(ParticleTypes.VILLAGER_HAPPY.getParticleID(), new ParticleSuspendedTown.HappyVillagerFactory());
		registerParticle(ParticleTypes.TOWN_AURA.getParticleID(), new ParticleSuspendedTown.Factory());
		registerParticle(ParticleTypes.NOTE.getParticleID(), new ParticleNote.Factory());
		registerParticle(ParticleTypes.PORTAL.getParticleID(), new ParticlePortal.Factory());
		registerParticle(ParticleTypes.ENCHANTMENT_TABLE.getParticleID(), new ParticleEnchantmentTable.EnchantmentTable());
		registerParticle(ParticleTypes.FLAME.getParticleID(), new ParticleFlame.Factory());
		registerParticle(ParticleTypes.LAVA.getParticleID(), new ParticleLava.Factory());
		registerParticle(ParticleTypes.FOOTSTEP.getParticleID(), new ParticleFootStep.Factory());
		registerParticle(ParticleTypes.CLOUD.getParticleID(), new ParticleCloud.Factory());
		registerParticle(ParticleTypes.REDSTONE.getParticleID(), new ParticleRedstone.Factory());
		registerParticle(ParticleTypes.FALLING_DUST.getParticleID(), new ParticleFallingDust.Factory());
		registerParticle(ParticleTypes.SNOWBALL.getParticleID(), new ParticleBreaking.SnowballFactory());
		registerParticle(ParticleTypes.SNOW_SHOVEL.getParticleID(), new ParticleSnowShovel.Factory());
		registerParticle(ParticleTypes.SLIME.getParticleID(), new ParticleBreaking.SlimeFactory());
		registerParticle(ParticleTypes.HEART.getParticleID(), new ParticleHeart.Factory());
		registerParticle(ParticleTypes.BARRIER.getParticleID(), new Barrier.Factory());
		registerParticle(ParticleTypes.ITEM_CRACK.getParticleID(), new ParticleBreaking.Factory());
		registerParticle(ParticleTypes.BLOCK_CRACK.getParticleID(), new ParticleDigging.Factory());
		registerParticle(ParticleTypes.BLOCK_DUST.getParticleID(), new ParticleBlockDust.Factory());
		registerParticle(ParticleTypes.EXPLOSION_HUGE.getParticleID(), new ParticleExplosionHuge.Factory());
		registerParticle(ParticleTypes.EXPLOSION_LARGE.getParticleID(), new ParticleExplosionLarge.Factory());
		registerParticle(ParticleTypes.FIREWORKS_SPARK.getParticleID(), new ParticleFirework.Factory());
		registerParticle(ParticleTypes.MOB_APPEARANCE.getParticleID(), new ParticleMobAppearance.Factory());
		registerParticle(ParticleTypes.DRAGON_BREATH.getParticleID(), new ParticleDragonBreath.Factory());
		registerParticle(ParticleTypes.END_ROD.getParticleID(), new ParticleEndRod.Factory());
		registerParticle(ParticleTypes.DAMAGE_INDICATOR.getParticleID(), new ParticleCrit.DamageIndicatorFactory());
		registerParticle(ParticleTypes.SWEEP_ATTACK.getParticleID(), new ParticleSweepAttack.Factory());
		registerParticle(ParticleTypes.TOTEM.getParticleID(), new ParticleTotem.Factory());
	}

	public void registerParticle(int id, IParticleFactory particleFactory) {

		particleTypes.put(id, particleFactory);
	}

	public void emitParticleAtEntity(Entity entityIn, ParticleTypes particleTypes) {

		particleEmitters.add(new ParticleEmitter(world, entityIn, particleTypes));
	}

	public void emitParticleAtEntity(Entity p_191271_1_, ParticleTypes p_191271_2_, int p_191271_3_) {

		particleEmitters.add(new ParticleEmitter(world, p_191271_1_, p_191271_2_, p_191271_3_));
	}

	

	/**
	 * Spawns the relevant particle according to the particle id.
	 */
	public Particle spawnEffectParticle(int particleId, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {

		IParticleFactory iparticlefactory = particleTypes.get(particleId);

		if (iparticlefactory != null) {
			Particle particle = iparticlefactory.createParticle(particleId, world, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);

			if (particle != null) {
				addEffect(particle);
				return particle;
			}
		}

		return null;
	}

	public void addEffect(Particle effect) {

		queue.add(effect);
	}

	public void updateEffects() {

		for (int i = 0; i < 4; ++i) {
			updateEffectLayer(i);
		}

		if (!particleEmitters.isEmpty()) {
			List<ParticleEmitter> list = Lists.newArrayList();

			for (ParticleEmitter particleemitter : particleEmitters) {
				particleemitter.onUpdate();

				if (!particleemitter.isAlive()) {
					list.add(particleemitter);
				}
			}

			particleEmitters.removeAll(list);
		}

		if (!queue.isEmpty()) {
			for (Particle particle = queue.poll(); particle != null; particle = queue.poll()) {
				int j = particle.getFXLayer();
				int k = particle.shouldDisableDepth() ? 0 : 1;

				if (fxLayers[j][k].size() >= 16384) {
					fxLayers[j][k].removeFirst();
				}

				fxLayers[j][k].add(particle);
			}
		}
	}

	private void updateEffectLayer(int layer) {

		world.profiler.startSection(String.valueOf(layer));

		for (int i = 0; i < 2; ++i) {
			world.profiler.startSection(String.valueOf(i));
			tickParticleList(fxLayers[layer][i]);
			world.profiler.endSection();
		}

		world.profiler.endSection();
	}

	private void tickParticleList(Queue<Particle> p_187240_1_) {

		if (!p_187240_1_.isEmpty()) {
			Iterator<Particle> iterator = p_187240_1_.iterator();

			while (iterator.hasNext()) {
				Particle particle = iterator.next();
				tickParticle(particle);

				if (!particle.isAlive()) {
					iterator.remove();
				}
			}
		}
	}

	private void tickParticle(final Particle particle) {

		try {
			particle.onUpdate();
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking Particle");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being ticked");
			final int i = particle.getFXLayer();
			crashreportcategory.addDetail("Particle", particle::toString);
			crashreportcategory.addDetail("Particle Type", () -> {

				if (i == 0) {
					return "MISC_TEXTURE";
				} else if (i == 1) {
					return "TERRAIN_TEXTURE";
				} else {
					return i == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + i;
				}
			});
			throw new ReportedException(crashreport);
		}
	}

	/**
	 * Renders all current particles. Args player, partialTickTime
	 */
	public void renderParticles(Entity entityIn, float partialTicks) {

		float f = ActiveRenderInfo.getRotationX();
		float f1 = ActiveRenderInfo.getRotationZ();
		float f2 = ActiveRenderInfo.getRotationYZ();
		float f3 = ActiveRenderInfo.getRotationXY();
		float f4 = ActiveRenderInfo.getRotationXZ();
		Particle.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
		Particle.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
		Particle.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
		Particle.cameraViewDir = entityIn.getLook(partialTicks);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.alphaFunc(516, 0.003921569F);

		for (int i_nf = 0; i_nf < 3; ++i_nf) {
			final int i = i_nf;

			for (int j = 0; j < 2; ++j) {
				if (!fxLayers[i][j].isEmpty()) {
					switch (j) {
						case 0:
							GlStateManager.depthMask(false);
							break;

						case 1:
							GlStateManager.depthMask(true);
					}

					switch (i) {
						case 0:
						default:
							renderer.bindTexture(PARTICLE_TEXTURES);
							break;

						case 1:
							renderer.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					}

					GlStateManager.color(1F, 1F, 1F, 1F);
					Tessellator tessellator = Tessellator.getInstance();
					BufferBuilder bufferbuilder = tessellator.getBuffer();
					bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

					for (final Particle particle : fxLayers[i][j]) {
						try {
							particle.renderParticle(bufferbuilder, entityIn, partialTicks, f, f4, f1, f2, f3);
						} catch (Throwable throwable) {
							CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
							CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
							crashreportcategory.addDetail("Particle", () -> particle.toString());
							crashreportcategory.addDetail("Particle Type", () -> {

								if (i == 0) {
									return "MISC_TEXTURE";
								} else if (i == 1) {
									return "TERRAIN_TEXTURE";
								} else {
									return i == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + i;
								}
							});
							throw new ReportedException(crashreport);
						}
					}

					tessellator.draw();
				}
			}
		}

		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
		GlStateManager.alphaFunc(516, 0.1F);
	}

	public void renderLitParticles(Entity entityIn, float partialTick) {

		float f = 0.017453292F;
		float f1 = MathHelper.cos(entityIn.rotationYaw * 0.017453292F);
		float f2 = MathHelper.sin(entityIn.rotationYaw * 0.017453292F);
		float f3 = -f2 * MathHelper.sin(entityIn.rotationPitch * 0.017453292F);
		float f4 = f1 * MathHelper.sin(entityIn.rotationPitch * 0.017453292F);
		float f5 = MathHelper.cos(entityIn.rotationPitch * 0.017453292F);

		for (int i = 0; i < 2; ++i) {
			Queue<Particle> queue = fxLayers[3][i];

			if (!queue.isEmpty()) {
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuffer();

				for (Particle particle : queue) {
					particle.renderParticle(bufferbuilder, entityIn, partialTick, f1, f5, f2, f3, f4);
				}
			}
		}
	}

	public void clearEffects(World worldIn) {

		world = worldIn;

		for (int i = 0; i < 4; ++i) {
			for (int j = 0; j < 2; ++j) {
				fxLayers[i][j].clear();
			}
		}

		particleEmitters.clear();
	}

	public void addBlockDestroyEffects(BlockPos pos, IBlockState state) {

		if (state.getMaterial() != Material.AIR) {
			state = state.getActualState(world, pos);
			int i = 4;

			for (int j = 0; j < 4; ++j) {
				for (int k = 0; k < 4; ++k) {
					for (int l = 0; l < 4; ++l) {
						double d0 = ((double) j + 0.5D) / 4D;
						double d1 = ((double) k + 0.5D) / 4D;
						double d2 = ((double) l + 0.5D) / 4D;
						addEffect((new ParticleDigging(world, (double) pos.getX() + d0, (double) pos.getY() + d1, (double) pos.getZ() + d2, d0 - 0.5D, d1 - 0.5D, d2 - 0.5D, state)).setBlockPos(pos));
					}
				}
			}
		}
	}

	/**
	 * Adds block hit particles for the specified block
	 */
	public void addBlockHitEffects(BlockPos pos, Facing side) {

		IBlockState iblockstate = world.getBlockState(pos);

		if (iblockstate.getRenderType() != BlockRenderType.INVISIBLE) {
			int i = pos.getX();
			int j = pos.getY();
			int k = pos.getZ();
			float f = 0.1F;
			AxisAlignedBB axisalignedbb = iblockstate.getBoundingBox(world, pos);
			double d0 = (double) i + rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minX;
			double d1 = (double) j + rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minY;
			double d2 = (double) k + rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minZ;

			if (side == Facing.DOWN) {
				d1 = (double) j + axisalignedbb.minY - 0.10000000149011612D;
			}

			if (side == Facing.UP) {
				d1 = (double) j + axisalignedbb.maxY + 0.10000000149011612D;
			}

			if (side == Facing.NORTH) {
				d2 = (double) k + axisalignedbb.minZ - 0.10000000149011612D;
			}

			if (side == Facing.SOUTH) {
				d2 = (double) k + axisalignedbb.maxZ + 0.10000000149011612D;
			}

			if (side == Facing.WEST) {
				d0 = (double) i + axisalignedbb.minX - 0.10000000149011612D;
			}

			if (side == Facing.EAST) {
				d0 = (double) i + axisalignedbb.maxX + 0.10000000149011612D;
			}

			addEffect((new ParticleDigging(world, d0, d1, d2, 0D, 0D, 0D, iblockstate)).setBlockPos(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
		}
	}

	public String getStatistics() {

		int i = 0;

		for (int j = 0; j < 4; ++j) {
			for (int k = 0; k < 2; ++k) {
				i += fxLayers[j][k].size();
			}
		}

		return "" + i;
	}

}
