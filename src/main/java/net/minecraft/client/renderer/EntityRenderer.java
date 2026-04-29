package net.minecraft.client.renderer;

import com.google.common.base.Predicates;
import com.google.gson.JsonSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.game.MapItemRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.Project;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Random;

public class EntityRenderer implements IResourceManagerReloadListener {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final ResourceLocation RAIN_TEXTURES = new ResourceLocation("textures/environment/rain.png");
	private static final ResourceLocation SNOW_TEXTURES = new ResourceLocation("textures/environment/snow.png");
	private static final ResourceLocation[] SHADERS_TEXTURES = new ResourceLocation[]{new ResourceLocation("shaders/post/notch.json"), new ResourceLocation("shaders/post/fxaa.json"), new ResourceLocation("shaders/post/art.json"), new ResourceLocation("shaders/post/bumpy.json"), new ResourceLocation("shaders/post/blobs2.json"), new ResourceLocation("shaders/post/pencil.json"), new ResourceLocation("shaders/post/color_convolve.json"), new ResourceLocation("shaders/post/deconverge.json"), new ResourceLocation("shaders/post/flip.json"), new ResourceLocation("shaders/post/invert.json"), new ResourceLocation("shaders/post/ntsc.json"), new ResourceLocation("shaders/post/outline.json"), new ResourceLocation("shaders/post/phosphor.json"), new ResourceLocation("shaders/post/scan_pincushion.json"), new ResourceLocation("shaders/post/sobel.json"), new ResourceLocation("shaders/post/bits.json"), new ResourceLocation("shaders/post/desaturate.json"), new ResourceLocation("shaders/post/green.json"), new ResourceLocation("shaders/post/blur.json"), new ResourceLocation("shaders/post/wobble.json"), new ResourceLocation("shaders/post/blobs.json"), new ResourceLocation("shaders/post/antialias.json"), new ResourceLocation("shaders/post/creeper.json"), new ResourceLocation("shaders/post/spider.json")};
	public static final int SHADER_COUNT = SHADERS_TEXTURES.length;
	public static boolean anaglyphEnable;
	/**
	 * Anaglyph field (0=R, 1=GB)
	 */
	public static int anaglyphField;
	public final ItemRenderer itemRenderer;
	/**
	 * A reference to the Minecraft object.
	 */
	private final Minecraft mc;
	private final IResourceManager resourceManager;
	private final Random random = new Random();
	private final MapItemRenderer mapItemRenderer;
	private final MouseFilter mouseFilterXAxis = new MouseFilter();
	private final MouseFilter mouseFilterYAxis = new MouseFilter();
	private final float thirdPersonDistance = 4F;
	private final boolean renderHand = true;
	private final boolean drawBlockOutline = true;
	/**
	 * The texture id of the blocklight/skylight texture used for lighting effects
	 */
	private final DynamicTexture lightmapTexture;
	/**
	 * Colors computed in updateLightmap() and loaded into the lightmap emptyTexture
	 */
	private final int[] lightmapColors;
	private final ResourceLocation locationLightMap;
	private final float[] rainXCoords = new float[1024];
	private final float[] rainYCoords = new float[1024];
	/**
	 * Fog color buffer
	 */
	private final FloatBuffer fogColorBuffer = GLAllocation.createDirectFloatBuffer(16);
	private final double cameraZoom = 1D;
	private float farPlaneDistance;
	/**
	 * Entity renderer update count
	 */
	private int rendererUpdateCount;
	/**
	 * Pointed entity
	 */
	private Entity pointedEntity;
	/**
	 * Previous third person distance
	 */
	private float thirdPersonDistancePrev = 4F;
	/**
	 * Smooth cam yaw
	 */
	private float smoothCamYaw;
	/**
	 * Smooth cam pitch
	 */
	private float smoothCamPitch;
	/**
	 * Smooth cam filter X
	 */
	private float smoothCamFilterX;
	/**
	 * Smooth cam filter Y
	 */
	private float smoothCamFilterY;
	/**
	 * Smooth cam partial ticks
	 */
	private float smoothCamPartialTicks;
	/**
	 * FOV modifier hand
	 */
	private float fovModifierHand;
	/**
	 * FOV modifier hand prev
	 */
	private float fovModifierHandPrev;
	private float bossColorModifier;
	private float bossColorModifierPrev;
	/**
	 * Cloud fog mode
	 */
	private boolean cloudFog;
	private long timeWorldIcon;
	/**
	 * Previous frame time in milliseconds
	 */
	private long prevFrameTime = Minecraft.getSystemTime();
	/**
	 * End time of last render (ns)
	 */
	private long renderEndNanoTime;
	/**
	 * Is set, updateCameraAndRender() calls updateLightmap(); set by updateTorchFlicker()
	 */
	private boolean lightmapUpdateNeeded;
	/**
	 * Torch flicker X
	 */
	private float torchFlickerX;
	private float torchFlickerDX;
	/**
	 * Rain sound counter
	 */
	private int rainSoundCounter;
	private float fogColorRed;
	private float fogColorGreen;
	private float fogColorBlue;
	/**
	 * Fog color 2
	 */
	private float fogColor2;
	/**
	 * Fog color 1
	 */
	private float fogColor1;
	private int debugViewDirection;
	private boolean debugView;
	private double cameraYaw;
	private double cameraPitch;
	private ItemStack itemActivationItem;
	private int itemActivationTicks;
	private float itemActivationOffX;
	private float itemActivationOffY;
	private ShaderGroup shaderGroup;
	private int shaderIndex;
	private boolean useShader;
	private int frameCount;

	public EntityRenderer(Minecraft mcIn, IResourceManager resourceManagerIn) {

		shaderIndex = SHADER_COUNT;
		mc = mcIn;
		resourceManager = resourceManagerIn;
		itemRenderer = mcIn.getItemRenderer();
		mapItemRenderer = new MapItemRenderer(mcIn.getTextureManager());
		lightmapTexture = new DynamicTexture(16, 16);
		locationLightMap = mcIn.getTextureManager().getDynamicTextureLocation("lightMap", lightmapTexture);
		lightmapColors = lightmapTexture.getTextureData();
		shaderGroup = null;

		for (int i = 0; i < 32; ++i) {
			for (int j = 0; j < 32; ++j) {
				float f = (float) (j - 16);
				float f1 = (float) (i - 16);
				float f2 = MathHelper.sqrt(f * f + f1 * f1);
				rainXCoords[i << 5 | j] = -f1 / f2;
				rainYCoords[i << 5 | j] = f / f2;
			}
		}
	}

	public static void drawNameplate(FontRenderer fontRendererIn, String str, float x, float y, float z, int verticalShift, float viewerYaw, float viewerPitch, boolean isThirdPersonFrontal, boolean isSneaking) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.glNormal3f(0F, 1F, 0F);
		GlStateManager.rotate(-viewerYaw, 0F, 1F, 0F);
		GlStateManager.rotate((float) (isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1F, 0F, 0F);
		GlStateManager.scale(-0.025F, -0.025F, 0.025F);
		GlStateManager.disableLighting();
		GlStateManager.depthMask(false);

		if (!isSneaking) {
			GlStateManager.disableDepth();
		}

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		int i = fontRendererIn.getStringWidth(str) / 2;
		GlStateManager.disableTexture2D();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
		bufferbuilder.pos(-i - 1, -1 + verticalShift, 0D).color(0F, 0F, 0F, 0.25F).endVertex();
		bufferbuilder.pos(-i - 1, 8 + verticalShift, 0D).color(0F, 0F, 0F, 0.25F).endVertex();
		bufferbuilder.pos(i + 1, 8 + verticalShift, 0D).color(0F, 0F, 0F, 0.25F).endVertex();
		bufferbuilder.pos(i + 1, -1 + verticalShift, 0D).color(0F, 0F, 0F, 0.25F).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();

		if (!isSneaking) {
			fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, 553648127);
			GlStateManager.enableDepth();
		}

		GlStateManager.depthMask(true);
		fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, isSneaking ? 553648127 : -1);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.popMatrix();
	}

	public boolean isShaderActive() {

		return OpenGlHelper.shadersSupported && shaderGroup != null;
	}

	public void stopUseShader() {

		if (shaderGroup != null) {
			shaderGroup.deleteShaderGroup();
		}

		shaderGroup = null;
		shaderIndex = SHADER_COUNT;
	}

	public void switchUseShader() {

		useShader = !useShader;
	}

	/**
	 * What shader to use when spectating this entity
	 */
	public void loadEntityShader(Entity entityIn) {

		if (OpenGlHelper.shadersSupported) {
			if (shaderGroup != null) {
				shaderGroup.deleteShaderGroup();
			}

			shaderGroup = null;

			if (entityIn instanceof EntityCreeper) {
				loadShader(new ResourceLocation("shaders/post/creeper.json"));
			} else if (entityIn instanceof EntitySpider) {
				loadShader(new ResourceLocation("shaders/post/spider.json"));
			} else if (entityIn instanceof EntityEnderman) {
				loadShader(new ResourceLocation("shaders/post/invert.json"));
			}
		}
	}

	private void loadShader(ResourceLocation resourceLocationIn) {

		try {
			shaderGroup = new ShaderGroup(mc.getTextureManager(), resourceManager, mc.getFramebuffer(), resourceLocationIn);
			shaderGroup.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
			useShader = true;
		} catch (IOException | JsonSyntaxException ioexception) {
			LOGGER.warn("Failed to load shader: {}", resourceLocationIn, ioexception);
			shaderIndex = SHADER_COUNT;
			useShader = false;
		}
	}

	public void onResourceManagerReload(IResourceManager resourceManager) {

		if (shaderGroup != null) {
			shaderGroup.deleteShaderGroup();
		}

		shaderGroup = null;

		if (shaderIndex == SHADER_COUNT) {
			loadEntityShader(mc.getRenderViewEntity());
		} else {
			loadShader(SHADERS_TEXTURES[shaderIndex]);
		}
	}

	/**
	 * Updates the entity renderer
	 */
	public void updateRenderer() {

		if (OpenGlHelper.shadersSupported && ShaderLinkHelper.getStaticShaderLinkHelper() == null) {
			ShaderLinkHelper.setNewStaticShaderLinkHelper();
		}

		updateFovModifierHand();
		updateTorchFlicker();
		fogColor2 = fogColor1;
		thirdPersonDistancePrev = 4F;

		if (mc.gameSettings.smoothCamera) {
			float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
			float f1 = f * f * f * 8F;
			smoothCamFilterX = mouseFilterXAxis.smooth(smoothCamYaw, 0.05F * f1);
			smoothCamFilterY = mouseFilterYAxis.smooth(smoothCamPitch, 0.05F * f1);
			smoothCamPartialTicks = 0F;
			smoothCamYaw = 0F;
			smoothCamPitch = 0F;
		} else {
			smoothCamFilterX = 0F;
			smoothCamFilterY = 0F;
			mouseFilterXAxis.reset();
			mouseFilterYAxis.reset();
		}

		if (mc.getRenderViewEntity() == null) {
			mc.setRenderViewEntity(mc.player);
		}

		float f3 = mc.world.getLightBrightness(new BlockPos(mc.getRenderViewEntity()));
		float f4 = (float) mc.gameSettings.renderDistanceChunks / 32F;
		float f2 = f3 * (1F - f4) + f4;
		fogColor1 += (f2 - fogColor1) * 0.1F;
		++rendererUpdateCount;
		itemRenderer.updateEquippedItem();
		addRainParticles();
		bossColorModifierPrev = bossColorModifier;

		if (mc.ingameGUI.getBossOverlay().shouldDarkenSky()) {
			bossColorModifier += 0.05F;

			if (bossColorModifier > 1F) {
				bossColorModifier = 1F;
			}
		} else if (bossColorModifier > 0F) {
			bossColorModifier -= 0.0125F;
		}

		if (itemActivationTicks > 0) {
			--itemActivationTicks;

			if (itemActivationTicks == 0) {
				itemActivationItem = null;
			}
		}
	}

	public ShaderGroup getShaderGroup() {

		return shaderGroup;
	}

	public void updateShaderGroupSize(int width, int height) {

		if (OpenGlHelper.shadersSupported) {
			if (shaderGroup != null) {
				shaderGroup.createBindFramebuffers(width, height);
			}

			mc.renderGlobal.createBindEntityOutlineFbs(width, height);
		}
	}

	/**
	 * Gets the block or object that is being moused over.
	 */
	public void getMouseOver(float partialTicks) {

		Entity entity = mc.getRenderViewEntity();

		if (entity != null) {
			if (mc.world != null) {
				mc.profiler.startSection("pick");
				mc.pointedEntity = null;
				double d0 = mc.playerController.getBlockReachDistance();
				mc.objectMouseOver = entity.rayTrace(d0, partialTicks);
				Vec3d vec3d = entity.getPositionEyes(partialTicks);
				boolean flag = false;
				int i = 3;
				double d1 = d0;

				if (mc.playerController.extendedReach()) {
					d1 = 6D;
					d0 = d1;
				} else {
					if (d0 > 3D) {
						flag = true;
					}
				}

				if (mc.objectMouseOver != null) {
					d1 = mc.objectMouseOver.hitVec.distanceTo(vec3d);
				}

				Vec3d vec3d1 = entity.getLook(1F);
				Vec3d vec3d2 = vec3d.addVector(vec3d1.x() * d0, vec3d1.y() * d0, vec3d1.z() * d0);
				pointedEntity = null;
				Vec3d vec3d3 = null;
				float f = 1F;
				List<Entity> list = mc.world.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().expand(vec3d1.x() * d0, vec3d1.y() * d0, vec3d1.z() * d0).grow(1D, 1D, 1D), Predicates.and(EntitySelectors.NOT_SPECTATING, p_apply_1_ -> p_apply_1_ != null && p_apply_1_.canBeCollidedWith()));
				double d2 = d1;

				for (Entity entity1 : list) {
					AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(entity1.getCollisionBorderSize());
					RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);

					if (axisalignedbb.contains(vec3d)) {
						if (d2 >= 0D) {
							pointedEntity = entity1;
							vec3d3 = raytraceresult == null ? vec3d : raytraceresult.hitVec;
							d2 = 0D;
						}
					} else if (raytraceresult != null) {
						double d3 = vec3d.distanceTo(raytraceresult.hitVec);

						if (d3 < d2 || d2 == 0D) {
							if (entity1.getLowestRidingEntity() == entity.getLowestRidingEntity()) {
								if (d2 == 0D) {
									pointedEntity = entity1;
									vec3d3 = raytraceresult.hitVec;
								}
							} else {
								pointedEntity = entity1;
								vec3d3 = raytraceresult.hitVec;
								d2 = d3;
							}
						}
					}
				}

				if (pointedEntity != null && flag && vec3d.distanceTo(vec3d3) > 3D) {
					pointedEntity = null;
					mc.objectMouseOver = new RayTraceResult(RayTraceResult.Type.MISS, vec3d3, null, new BlockPos(vec3d3));
				}

				if (pointedEntity != null && (d2 < d1 || mc.objectMouseOver == null)) {
					mc.objectMouseOver = new RayTraceResult(pointedEntity, vec3d3);

					if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame) {
						mc.pointedEntity = pointedEntity;
					}
				}

				mc.profiler.endSection();
			}
		}
	}

	/**
	 * Update FOV modifier hand
	 */
	private void updateFovModifierHand() {

		float f = 1F;

		if (mc.getRenderViewEntity() instanceof AbstractClientPlayer abstractclientplayer) {
			f = abstractclientplayer.getFovModifier();
		}

		fovModifierHandPrev = fovModifierHand;
		fovModifierHand += (f - fovModifierHand) * 0.5F;

		if (fovModifierHand > 1.5F) {
			fovModifierHand = 1.5F;
		}

		if (fovModifierHand < 0.1F) {
			fovModifierHand = 0.1F;
		}
	}

	/**
	 * Changes the field of view of the player depending on if they are underwater or not
	 */
	private float getFOVModifier(float partialTicks, boolean useFOVSetting) {

		if (debugView) {
			return 90F;
		} else {
			Entity entity = mc.getRenderViewEntity();
			float f = 70F;

			if (useFOVSetting) {
				f = mc.gameSettings.fovSetting;
				f = f * (fovModifierHandPrev + (fovModifierHand - fovModifierHandPrev) * partialTicks);
			}

			if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getHealth() <= 0F) {
				float f1 = (float) ((EntityLivingBase) entity).deathTime + partialTicks;
				f /= (1F - 500F / (f1 + 500F)) * 2F + 1F;
			}

			IBlockState iblockstate = ActiveRenderInfo.getBlockStateAtEntityViewpoint(mc.world, entity, partialTicks);

			if (iblockstate.getMaterial() == Material.WATER) {
				f = f * 60F / 70F;
			}

			return f;
		}
	}

	private void hurtCameraEffect(float partialTicks) {

		if (mc.getRenderViewEntity() instanceof EntityLivingBase entitylivingbase) {
			float f = (float) entitylivingbase.hurtTime - partialTicks;

			if (entitylivingbase.getHealth() <= 0F) {
				float f1 = (float) entitylivingbase.deathTime + partialTicks;
				GlStateManager.rotate(40F - 8000F / (f1 + 200F), 0F, 0F, 1F);
			}

			if (f < 0F) {
				return;
			}

			f = f / (float) entitylivingbase.maxHurtTime;
			f = MathHelper.sin(f * f * f * f * (float) Math.PI);
			float f2 = entitylivingbase.attackedAtYaw;
			GlStateManager.rotate(-f2, 0F, 1F, 0F);
			GlStateManager.rotate(-f * 14F, 0F, 0F, 1F);
			GlStateManager.rotate(f2, 0F, 1F, 0F);
		}
	}

	/**
	 * Updates the bobbing render effect of the player.
	 */
	private void applyBobbing(float partialTicks) {

		if (mc.getRenderViewEntity() instanceof EntityPlayer entityplayer) {
			float f = entityplayer.distanceWalkedModified - entityplayer.prevDistanceWalkedModified;
			float f1 = -(entityplayer.distanceWalkedModified + f * partialTicks);
			float f2 = entityplayer.prevCameraYaw + (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * partialTicks;
			float f3 = entityplayer.prevCameraPitch + (entityplayer.cameraPitch - entityplayer.prevCameraPitch) * partialTicks;
			GlStateManager.translate(MathHelper.sin(f1 * (float) Math.PI) * f2 * 0.5F, -Math.abs(MathHelper.cos(f1 * (float) Math.PI) * f2), 0F);
			GlStateManager.rotate(MathHelper.sin(f1 * (float) Math.PI) * f2 * 3F, 0F, 0F, 1F);
			GlStateManager.rotate(Math.abs(MathHelper.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5F, 1F, 0F, 0F);
			GlStateManager.rotate(f3, 1F, 0F, 0F);
		}
	}

	/**
	 * sets up player's eye (or camera in third person mode)
	 */
	private void orientCamera(float partialTicks) {

		Entity entity = mc.getRenderViewEntity();
		float f = entity.getEyeHeight();
		double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
		double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks + (double) f;
		double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;

		if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()) {
			f = (float) ((double) f + 1D);
			GlStateManager.translate(0F, 0.3F, 0F);

			if (!mc.gameSettings.debugCamEnable) {
				BlockPos blockpos = new BlockPos(entity);
				IBlockState iblockstate = mc.world.getBlockState(blockpos);
				Block block = iblockstate.getBlock();

				if (block == Blocks.BED) {
					int j = iblockstate.getValue(BlockBed.FACING).getHorizontalIndex();
					GlStateManager.rotate((float) (j * 90), 0F, 1F, 0F);
				}

				GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180F, 0F, -1F, 0F);
				GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1F, 0F, 0F);
			}
		} else if (mc.gameSettings.thirdPersonView > 0) {
			double d3 = thirdPersonDistancePrev + (4F - thirdPersonDistancePrev) * partialTicks;

			if (mc.gameSettings.debugCamEnable) {
				GlStateManager.translate(0F, 0F, (float) (-d3));
			} else {
				float f1 = entity.rotationYaw;
				float f2 = entity.rotationPitch;

				if (mc.gameSettings.thirdPersonView == 2) {
					f2 += 180F;
				}

				double d4 = (double) (-MathHelper.sin(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F)) * d3;
				double d5 = (double) (MathHelper.cos(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F)) * d3;
				double d6 = (double) (-MathHelper.sin(f2 * 0.017453292F)) * d3;

				for (int i = 0; i < 8; ++i) {
					float f3 = (float) ((i & 1) * 2 - 1);
					float f4 = (float) ((i >> 1 & 1) * 2 - 1);
					float f5 = (float) ((i >> 2 & 1) * 2 - 1);
					f3 = f3 * 0.1F;
					f4 = f4 * 0.1F;
					f5 = f5 * 0.1F;
					RayTraceResult raytraceresult = mc.world.rayTraceBlocks(new Vec3d(d0 + (double) f3, d1 + (double) f4, d2 + (double) f5), new Vec3d(d0 - d4 + (double) f3 + (double) f5, d1 - d6 + (double) f4, d2 - d5 + (double) f5));

					if (raytraceresult != null) {
						double d7 = raytraceresult.hitVec.distanceTo(new Vec3d(d0, d1, d2));

						if (d7 < d3) {
							d3 = d7;
						}
					}
				}

				if (mc.gameSettings.thirdPersonView == 2) {
					GlStateManager.rotate(180F, 0F, 1F, 0F);
				}

				GlStateManager.rotate(entity.rotationPitch - f2, 1F, 0F, 0F);
				GlStateManager.rotate(entity.rotationYaw - f1, 0F, 1F, 0F);
				GlStateManager.translate(0F, 0F, (float) (-d3));
				GlStateManager.rotate(f1 - entity.rotationYaw, 0F, 1F, 0F);
				GlStateManager.rotate(f2 - entity.rotationPitch, 1F, 0F, 0F);
			}
		} else {
			GlStateManager.translate(0F, 0F, 0.05F);
		}

		if (!mc.gameSettings.debugCamEnable) {
			GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 1F, 0F, 0F);

			if (entity instanceof EntityAnimal entityanimal) {
				GlStateManager.rotate(entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180F, 0F, 1F, 0F);
			} else {
				GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180F, 0F, 1F, 0F);
			}
		}

		GlStateManager.translate(0F, -f, 0F);
		d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
		d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks + (double) f;
		d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;
		cloudFog = mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTicks);
	}

	/**
	 * sets up projection, view effects, camera position/rotation
	 */
	private void setupCameraTransform(float partialTicks, int pass) {

		farPlaneDistance = (float) (mc.gameSettings.renderDistanceChunks * 16);
		GlStateManager.matrixMode(5889);
		GlStateManager.loadIdentity();
		float f = 0.07F;

		if (mc.gameSettings.anaglyph) {
			GlStateManager.translate((float) (-(pass * 2 - 1)) * 0.07F, 0F, 0F);
		}

		if (cameraZoom != 1D) {
			GlStateManager.translate((float) cameraYaw, (float) (-cameraPitch), 0F);
			GlStateManager.scale(cameraZoom, cameraZoom, 1D);
		}

		Project.gluPerspective(getFOVModifier(partialTicks, true), (float) mc.displayWidth / (float) mc.displayHeight, 0.05F, farPlaneDistance * MathHelper.SQRT_2);
		GlStateManager.matrixMode(5888);
		GlStateManager.loadIdentity();

		if (mc.gameSettings.anaglyph) {
			GlStateManager.translate((float) (pass * 2 - 1) * 0.1F, 0F, 0F);
		}

		hurtCameraEffect(partialTicks);

		if (mc.gameSettings.viewBobbing) {
			applyBobbing(partialTicks);
		}

		float f1 = mc.player.prevTimeInPortal + (mc.player.timeInPortal - mc.player.prevTimeInPortal) * partialTicks;

		if (f1 > 0F) {
			int i = 20;

			if (mc.player.isPotionActive(MobEffects.NAUSEA)) {
				i = 7;
			}

			float f2 = 5F / (f1 * f1 + 5F) - f1 * 0.04F;
			f2 = f2 * f2;
			GlStateManager.rotate(((float) rendererUpdateCount + partialTicks) * (float) i, 0F, 1F, 1F);
			GlStateManager.scale(1F / f2, 1F, 1F);
			GlStateManager.rotate(-((float) rendererUpdateCount + partialTicks) * (float) i, 0F, 1F, 1F);
		}

		orientCamera(partialTicks);

		if (debugView) {
			switch (debugViewDirection) {
				case 0:
					GlStateManager.rotate(90F, 0F, 1F, 0F);
					break;

				case 1:
					GlStateManager.rotate(180F, 0F, 1F, 0F);
					break;

				case 2:
					GlStateManager.rotate(-90F, 0F, 1F, 0F);
					break;

				case 3:
					GlStateManager.rotate(90F, 1F, 0F, 0F);
					break;

				case 4:
					GlStateManager.rotate(-90F, 1F, 0F, 0F);
			}
		}
	}

	/**
	 * Render player hand
	 */
	private void renderHand(float partialTicks, int pass) {

		if (!debugView) {
			GlStateManager.matrixMode(5889);
			GlStateManager.loadIdentity();
			float f = 0.07F;

			if (mc.gameSettings.anaglyph) {
				GlStateManager.translate((float) (-(pass * 2 - 1)) * 0.07F, 0F, 0F);
			}

			Project.gluPerspective(getFOVModifier(partialTicks, false), (float) mc.displayWidth / (float) mc.displayHeight, 0.05F, farPlaneDistance * 2F);
			GlStateManager.matrixMode(5888);
			GlStateManager.loadIdentity();

			if (mc.gameSettings.anaglyph) {
				GlStateManager.translate((float) (pass * 2 - 1) * 0.1F, 0F, 0F);
			}

			GlStateManager.pushMatrix();
			hurtCameraEffect(partialTicks);

			if (mc.gameSettings.viewBobbing) {
				applyBobbing(partialTicks);
			}

			boolean flag = mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) mc.getRenderViewEntity()).isPlayerSleeping();

			if (mc.gameSettings.thirdPersonView == 0 && !flag && !mc.gameSettings.hideGUI && !mc.playerController.isSpectator()) {
				enableLightmap();
				itemRenderer.renderItemInFirstPerson(partialTicks);
				disableLightmap();
			}

			GlStateManager.popMatrix();

			if (mc.gameSettings.thirdPersonView == 0 && !flag) {
				itemRenderer.renderOverlays(partialTicks);
				hurtCameraEffect(partialTicks);
			}

			if (mc.gameSettings.viewBobbing) {
				applyBobbing(partialTicks);
			}
		}
	}

	public void disableLightmap() {

		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	public void enableLightmap() {

		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		float f = 0.00390625F;
		GlStateManager.scale(0.00390625F, 0.00390625F, 0.00390625F);
		GlStateManager.translate(8F, 8F, 8F);
		GlStateManager.matrixMode(5888);
		mc.getTextureManager().bindTexture(locationLightMap);
		GlStateManager.glTexParameteri(3553, 10241, 9729);
		GlStateManager.glTexParameteri(3553, 10240, 9729);
		GlStateManager.glTexParameteri(3553, 10242, 10496);
		GlStateManager.glTexParameteri(3553, 10243, 10496);
		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.enableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	/**
	 * Recompute a random value that is applied to block color in updateLightmap()
	 */
	private void updateTorchFlicker() {

		torchFlickerDX = (float) ((double) torchFlickerDX + (Math.random() - Math.random()) * Math.random() * Math.random());
		torchFlickerDX = (float) ((double) torchFlickerDX * 0.9D);
		torchFlickerX += torchFlickerDX - torchFlickerX;
		lightmapUpdateNeeded = true;
	}

	private void updateLightmap(float partialTicks) {

		if (lightmapUpdateNeeded) {
			mc.profiler.startSection("lightTex");
			World world = mc.world;

			if (world != null) {
				float f = world.getSunBrightness(1F);
				float f1 = f * 0.95F + 0.05F;

				for (int i = 0; i < 256; ++i) {
					float f2 = world.provider.getLightBrightnessTable()[i / 16] * f1;
					float f3 = world.provider.getLightBrightnessTable()[i % 16] * (torchFlickerX * 0.1F + 1.5F);

					if (world.getLastLightningBolt() > 0) {
						f2 = world.provider.getLightBrightnessTable()[i / 16];
					}

					float f4 = f2 * (f * 0.65F + 0.35F);
					float f5 = f2 * (f * 0.65F + 0.35F);
					float f6 = f3 * ((f3 * 0.6F + 0.4F) * 0.6F + 0.4F);
					float f7 = f3 * (f3 * f3 * 0.6F + 0.4F);
					float f8 = f4 + f3;
					float f9 = f5 + f6;
					float f10 = f2 + f7;
					f8 = f8 * 0.96F + 0.03F;
					f9 = f9 * 0.96F + 0.03F;
					f10 = f10 * 0.96F + 0.03F;

					if (bossColorModifier > 0F) {
						float f11 = bossColorModifierPrev + (bossColorModifier - bossColorModifierPrev) * partialTicks;
						f8 = f8 * (1F - f11) + f8 * 0.7F * f11;
						f9 = f9 * (1F - f11) + f9 * 0.6F * f11;
						f10 = f10 * (1F - f11) + f10 * 0.6F * f11;
					}

					if (world.provider.getDimensionType().getId() == 1) {
						f8 = 0.22F + f3 * 0.75F;
						f9 = 0.28F + f6 * 0.75F;
						f10 = 0.25F + f7 * 0.75F;
					}

					if (mc.player.isPotionActive(MobEffects.NIGHT_VISION)) {
						float f15 = getNightVisionBrightness(mc.player, partialTicks);
						float f12 = 1F / f8;

						if (f12 > 1F / f9) {
							f12 = 1F / f9;
						}

						if (f12 > 1F / f10) {
							f12 = 1F / f10;
						}

						f8 = f8 * (1F - f15) + f8 * f12 * f15;
						f9 = f9 * (1F - f15) + f9 * f12 * f15;
						f10 = f10 * (1F - f15) + f10 * f12 * f15;
					}

					if (f8 > 1F) {
						f8 = 1F;
					}

					if (f9 > 1F) {
						f9 = 1F;
					}

					if (f10 > 1F) {
						f10 = 1F;
					}

					float f16 = mc.gameSettings.gammaSetting;
					float f17 = 1F - f8;
					float f13 = 1F - f9;
					float f14 = 1F - f10;
					f17 = 1F - f17 * f17 * f17 * f17;
					f13 = 1F - f13 * f13 * f13 * f13;
					f14 = 1F - f14 * f14 * f14 * f14;
					f8 = f8 * (1F - f16) + f17 * f16;
					f9 = f9 * (1F - f16) + f13 * f16;
					f10 = f10 * (1F - f16) + f14 * f16;
					f8 = f8 * 0.96F + 0.03F;
					f9 = f9 * 0.96F + 0.03F;
					f10 = f10 * 0.96F + 0.03F;

					if (f8 > 1F) {
						f8 = 1F;
					}

					if (f9 > 1F) {
						f9 = 1F;
					}

					if (f10 > 1F) {
						f10 = 1F;
					}

					if (f8 < 0F) {
						f8 = 0F;
					}

					if (f9 < 0F) {
						f9 = 0F;
					}

					if (f10 < 0F) {
						f10 = 0F;
					}

					int j = 255;
					int k = (int) (f8 * 255F);
					int l = (int) (f9 * 255F);
					int i1 = (int) (f10 * 255F);
					lightmapColors[i] = -16777216 | k << 16 | l << 8 | i1;
				}

				lightmapTexture.updateDynamicTexture();
				lightmapUpdateNeeded = false;
				mc.profiler.endSection();
			}
		}
	}

	private float getNightVisionBrightness(EntityLivingBase entitylivingbaseIn, float partialTicks) {

		int i = entitylivingbaseIn.getActivePotionEffect(MobEffects.NIGHT_VISION).getDuration();
		return i > 200 ? 1F : 0.7F + MathHelper.sin(((float) i - partialTicks) * (float) Math.PI * 0.2F) * 0.3F;
	}

	public void updateCameraAndRender(float partialTicks, long nanoTime) {

		boolean flag = Display.isActive();

		if (!flag && mc.gameSettings.pauseOnLostFocus && (!mc.gameSettings.touchscreen || !Mouse.isButtonDown(1))) {
			if (Minecraft.getSystemTime() - prevFrameTime > 500L) {
				mc.displayInGameMenu();
			}
		} else {
			prevFrameTime = Minecraft.getSystemTime();
		}

		mc.profiler.startSection("mouse");

		if (flag && Minecraft.IS_RUNNING_ON_MAC && mc.inGameHasFocus && !Mouse.isInsideWindow()) {
			Mouse.setGrabbed(false);
			Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2 - 20);
			Mouse.setGrabbed(true);
		}

		if (mc.inGameHasFocus && flag) {
			mc.mouseHelper.mouseXYChange();
			mc.getTutorial().handleMouse(mc.mouseHelper);
			float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
			float f1 = f * f * f * 8F;
			float f2 = (float) mc.mouseHelper.deltaX * f1;
			float f3 = (float) mc.mouseHelper.deltaY * f1;
			int i = 1;

			if (mc.gameSettings.invertMouse) {
				i = -1;
			}

			if (mc.gameSettings.smoothCamera) {
				smoothCamYaw += f2;
				smoothCamPitch += f3;
				float f4 = partialTicks - smoothCamPartialTicks;
				smoothCamPartialTicks = partialTicks;
				f2 = smoothCamFilterX * f4;
				f3 = smoothCamFilterY * f4;
				mc.player.turn(f2, f3 * (float) i);
			} else {
				smoothCamYaw = 0F;
				smoothCamPitch = 0F;
				mc.player.turn(f2, f3 * (float) i);
			}
		}

		mc.profiler.endSection();

		if (!mc.skipRenderWorld) {
			anaglyphEnable = mc.gameSettings.anaglyph;
			final ScaledResolution scaledresolution = mc.scaledResolution;
			int i1 = scaledresolution.getScaledWidth();
			int j1 = scaledresolution.getScaledHeight();
			final int k1 = Mouse.getX() * i1 / mc.displayWidth;
			final int l1 = j1 - Mouse.getY() * j1 / mc.displayHeight - 1;
			int i2 = mc.gameSettings.limitFramerate;

			if (mc.world != null) {
				mc.profiler.startSection("level");
				int j = Math.min(Minecraft.getDebugFPS(), i2);
				j = Math.max(j, 60);
				long k = System.nanoTime() - nanoTime;
				long l = Math.max((long) (1000000000 / j / 4) - k, 0L);
				renderWorld(partialTicks, System.nanoTime() + l);

				if (mc.isSingleplayer() && timeWorldIcon < Minecraft.getSystemTime() - 1000L) {
					timeWorldIcon = Minecraft.getSystemTime();

					if (!mc.getIntegratedServer().isWorldIconSet()) {
						createWorldIcon();
					}
				}

				if (OpenGlHelper.shadersSupported) {
					mc.renderGlobal.renderEntityOutlineFramebuffer();

					if (shaderGroup != null && useShader) {
						GlStateManager.matrixMode(5890);
						GlStateManager.pushMatrix();
						GlStateManager.loadIdentity();
						shaderGroup.render(partialTicks);
						GlStateManager.popMatrix();
					}

					mc.getFramebuffer().bindFramebuffer(true);
				}

				renderEndNanoTime = System.nanoTime();
				mc.profiler.endStartSection("gui");

				if (!mc.gameSettings.hideGUI || mc.currentScreen != null) {
					GlStateManager.alphaFunc(516, 0.1F);
					setupOverlayRendering();
					renderItemActivation(i1, j1, partialTicks);
					mc.ingameGUI.renderGameOverlay(partialTicks);
				}

				mc.profiler.endSection();
			} else {
				GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);
				GlStateManager.matrixMode(5889);
				GlStateManager.loadIdentity();
				GlStateManager.matrixMode(5888);
				GlStateManager.loadIdentity();
				setupOverlayRendering();
				renderEndNanoTime = System.nanoTime();
			}

			if (mc.currentScreen != null) {
				GlStateManager.clear(256);

				try {
					mc.currentScreen.drawScreen(k1, l1, mc.getTickLength());
				} catch (Throwable throwable) {
					CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering screen");
					CrashReportCategory crashreportcategory = crashreport.makeCategory("Screen render details");
					crashreportcategory.addDetail("Screen name", () -> mc.currentScreen.getClass().getCanonicalName());
					crashreportcategory.addDetail("Mouse location", () -> String.format("Scaled: (%d, %d). Absolute: (%d, %d)", k1, l1, Mouse.getX(), Mouse.getY()));
					crashreportcategory.addDetail("Screen size", () -> String.format("Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %d", scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), mc.displayWidth, mc.displayHeight, scaledresolution.getScaleFactor()));
					throw new ReportedException(crashreport);
				}
			}
		}
	}

	private void createWorldIcon() {

		if (mc.renderGlobal.getRenderedChunks() > 10 && mc.renderGlobal.hasNoChunkUpdates() && !mc.getIntegratedServer().isWorldIconSet()) {
			BufferedImage bufferedimage = ScreenShotHelper.createScreenshot(mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
			int i = bufferedimage.getWidth();
			int j = bufferedimage.getHeight();
			int k = 0;
			int l = 0;

			if (i > j) {
				k = (i - j) / 2;
				i = j;
			} else {
				l = (j - i) / 2;
			}

			try {
				BufferedImage bufferedimage1 = new BufferedImage(64, 64, 1);
				Graphics graphics = bufferedimage1.createGraphics();
				graphics.drawImage(bufferedimage, 0, 0, 64, 64, k, l, k + i, l + i, null);
				graphics.dispose();
				ImageIO.write(bufferedimage1, "png", mc.getIntegratedServer().getWorldIconFile());
			} catch (IOException ioexception) {
				LOGGER.warn("Couldn't save auto screenshot", ioexception);
			}
		}
	}

	public void renderStreamIndicator(float partialTicks) {

		setupOverlayRendering();
	}

	private boolean isDrawBlockOutline() {

		if (!drawBlockOutline) {
			return false;
		} else {
			Entity entity = mc.getRenderViewEntity();
			boolean flag = entity instanceof EntityPlayer && !mc.gameSettings.hideGUI;

			if (flag && !((EntityPlayer) entity).capabilities.allowEdit) {
				ItemStack itemstack = ((EntityPlayer) entity).getHeldItemMainhand();

				if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
					BlockPos blockpos = mc.objectMouseOver.getBlockPos();
					Block block = mc.world.getBlockState(blockpos).getBlock();

					if (mc.playerController.getCurrentGameType() == GameType.SPECTATOR) {
						flag = block.hasTileEntity() && mc.world.getTileEntity(blockpos) instanceof IInventory;
					} else {
						flag = !itemstack.isEmpty() && (itemstack.canDestroy(block) || itemstack.canPlaceOn(block));
					}
				}
			}

			return flag;
		}
	}

	public void renderWorld(float partialTicks, long finishTimeNano) {

		updateLightmap(partialTicks);

		if (mc.getRenderViewEntity() == null) {
			mc.setRenderViewEntity(mc.player);
		}

		getMouseOver(partialTicks);
		GlStateManager.enableDepth();
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.5F);
		mc.profiler.startSection("center");

		if (mc.gameSettings.anaglyph) {
			anaglyphField = 0;
			GlStateManager.colorMask(false, true, true, false);
			renderWorldPass(0, partialTicks, finishTimeNano);
			anaglyphField = 1;
			GlStateManager.colorMask(true, false, false, false);
			renderWorldPass(1, partialTicks, finishTimeNano);
			GlStateManager.colorMask(true, true, true, false);
		} else {
			renderWorldPass(2, partialTicks, finishTimeNano);
		}

		mc.profiler.endSection();
	}

	private void renderWorldPass(int pass, float partialTicks, long finishTimeNano) {

		RenderGlobal renderglobal = mc.renderGlobal;
		ParticleManager particlemanager = mc.effectRenderer;
		boolean flag = isDrawBlockOutline();
		GlStateManager.enableCull();
		mc.profiler.endStartSection("clear");
		GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);
		updateFogColor(partialTicks);
		GlStateManager.clear(16640);
		mc.profiler.endStartSection("camera");
		setupCameraTransform(partialTicks, pass);
		ActiveRenderInfo.updateRenderInfo(mc.player, mc.gameSettings.thirdPersonView == 2);
		mc.profiler.endStartSection("frustum");
		ClippingHelperImpl.getInstance();
		mc.profiler.endStartSection("culling");
		ICamera icamera = new Frustum();
		Entity entity = mc.getRenderViewEntity();
		double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
		double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
		double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
		icamera.setPosition(d0, d1, d2);

		if (mc.gameSettings.renderDistanceChunks >= 4) {
			setupFog(-1, partialTicks);
			mc.profiler.endStartSection("sky");
			GlStateManager.matrixMode(5889);
			GlStateManager.loadIdentity();
			Project.gluPerspective(getFOVModifier(partialTicks, true), (float) mc.displayWidth / (float) mc.displayHeight, 0.05F, farPlaneDistance * 2F);
			GlStateManager.matrixMode(5888);
			renderglobal.renderSky(partialTicks, pass);
			GlStateManager.matrixMode(5889);
			GlStateManager.loadIdentity();
			Project.gluPerspective(getFOVModifier(partialTicks, true), (float) mc.displayWidth / (float) mc.displayHeight, 0.05F, farPlaneDistance * MathHelper.SQRT_2);
			GlStateManager.matrixMode(5888);
		}

		setupFog(0, partialTicks);
		GlStateManager.shadeModel(7425);

		if (entity.posY + (double) entity.getEyeHeight() < 128D) {
			renderCloudsCheck(renderglobal, partialTicks, pass, d0, d1, d2);
		}

		mc.profiler.endStartSection("prepareterrain");
		setupFog(0, partialTicks);
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.disableStandardItemLighting();
		mc.profiler.endStartSection("terrain_setup");
		renderglobal.setupTerrain(entity, partialTicks, icamera, frameCount++, mc.player.isSpectator());

		if (pass == 0 || pass == 2) {
			mc.profiler.endStartSection("updatechunks");
			mc.renderGlobal.updateChunks(finishTimeNano);
		}

		mc.profiler.endStartSection("terrain");
		GlStateManager.matrixMode(5888);
		GlStateManager.pushMatrix();
		GlStateManager.disableAlpha();
		renderglobal.renderBlockLayer(BlockRenderLayer.SOLID, partialTicks, pass, entity);
		GlStateManager.enableAlpha();
		renderglobal.renderBlockLayer(BlockRenderLayer.CUTOUT_MIPPED, partialTicks, pass, entity);
		mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		renderglobal.renderBlockLayer(BlockRenderLayer.CUTOUT, partialTicks, pass, entity);
		mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		GlStateManager.shadeModel(7424);
		GlStateManager.alphaFunc(516, 0.1F);

		if (!debugView) {
			GlStateManager.matrixMode(5888);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			RenderHelper.enableStandardItemLighting();
			mc.profiler.endStartSection("entities");
			renderglobal.renderEntities(entity, icamera, partialTicks);
			RenderHelper.disableStandardItemLighting();
			disableLightmap();
		}

		GlStateManager.matrixMode(5888);
		GlStateManager.popMatrix();

		if (flag && mc.objectMouseOver != null && !entity.isInsideOfMaterial(Material.WATER)) {
			EntityPlayer entityplayer = (EntityPlayer) entity;
			GlStateManager.disableAlpha();
			mc.profiler.endStartSection("outline");
			renderglobal.drawSelectionBox(entityplayer, mc.objectMouseOver, 0, partialTicks);
			GlStateManager.enableAlpha();
		}

		if (mc.debugRenderer.shouldRender()) {
			mc.debugRenderer.renderDebug(partialTicks, finishTimeNano);
		}

		mc.profiler.endStartSection("destroyProgress");
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		renderglobal.drawBlockDamageTexture(Tessellator.getInstance(), Tessellator.getInstance().getBuffer(), entity, partialTicks);
		mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		GlStateManager.disableBlend();

		if (!debugView) {
			enableLightmap();
			mc.profiler.endStartSection("litParticles");
			particlemanager.renderLitParticles(entity, partialTicks);
			RenderHelper.disableStandardItemLighting();
			setupFog(0, partialTicks);
			mc.profiler.endStartSection("particles");
			particlemanager.renderParticles(entity, partialTicks);
			disableLightmap();
		}

		GlStateManager.depthMask(false);
		GlStateManager.enableCull();
		mc.profiler.endStartSection("weather");
		renderRainSnow(partialTicks);
		GlStateManager.depthMask(true);
		renderglobal.renderWorldBorder(entity, partialTicks);
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.alphaFunc(516, 0.1F);
		setupFog(0, partialTicks);
		GlStateManager.enableBlend();
		GlStateManager.depthMask(false);
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.shadeModel(7425);
		mc.profiler.endStartSection("translucent");
		renderglobal.renderBlockLayer(BlockRenderLayer.TRANSLUCENT, partialTicks, pass, entity);
		GlStateManager.shadeModel(7424);
		GlStateManager.depthMask(true);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.disableFog();

		if (entity.posY + (double) entity.getEyeHeight() >= 128D) {
			mc.profiler.endStartSection("aboveClouds");
			renderCloudsCheck(renderglobal, partialTicks, pass, d0, d1, d2);
		}

		mc.profiler.endStartSection("hand");

		if (renderHand) {
			GlStateManager.clear(256);
			renderHand(partialTicks, pass);
		}
	}

	private void renderCloudsCheck(RenderGlobal renderGlobalIn, float partialTicks, int pass, double x, double y, double z) {

		if (mc.gameSettings.shouldRenderClouds() != 0) {
			mc.profiler.endStartSection("clouds");
			GlStateManager.matrixMode(5889);
			GlStateManager.loadIdentity();
			Project.gluPerspective(getFOVModifier(partialTicks, true), (float) mc.displayWidth / (float) mc.displayHeight, 0.05F, farPlaneDistance * 4F);
			GlStateManager.matrixMode(5888);
			GlStateManager.pushMatrix();
			setupFog(0, partialTicks);
			renderGlobalIn.renderClouds(partialTicks, pass, x, y, z);
			GlStateManager.disableFog();
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(5889);
			GlStateManager.loadIdentity();
			Project.gluPerspective(getFOVModifier(partialTicks, true), (float) mc.displayWidth / (float) mc.displayHeight, 0.05F, farPlaneDistance * MathHelper.SQRT_2);
			GlStateManager.matrixMode(5888);
		}
	}

	private void addRainParticles() {

		float f = mc.world.getRainStrength(1F);

		if (!mc.gameSettings.fancyGraphics) {
			f /= 2F;
		}

		if (f != 0F) {
			random.setSeed((long) rendererUpdateCount * 312987231L);
			Entity entity = mc.getRenderViewEntity();
			World world = mc.world;
			BlockPos blockpos = new BlockPos(entity);
			int i = 10;
			double d0 = 0D;
			double d1 = 0D;
			double d2 = 0D;
			int j = 0;
			int k = (int) (100F * f * f);

			if (mc.gameSettings.particleSetting == 1) {
				k >>= 1;
			} else if (mc.gameSettings.particleSetting == 2) {
				k = 0;
			}

			for (int l = 0; l < k; ++l) {
				BlockPos blockpos1 = world.getPrecipitationHeight(blockpos.add(random.nextInt(10) - random.nextInt(10), 0, random.nextInt(10) - random.nextInt(10)));
				Biome biome = world.getBiome(blockpos1);
				BlockPos blockpos2 = blockpos1.down();
				IBlockState iblockstate = world.getBlockState(blockpos2);

				if (blockpos1.getY() <= blockpos.getY() + 10 && blockpos1.getY() >= blockpos.getY() - 10 && biome.canRain() && biome.getTemperature(blockpos1) >= 0.15F) {
					double d3 = random.nextDouble();
					double d4 = random.nextDouble();
					AxisAlignedBB axisalignedbb = iblockstate.getBoundingBox(world, blockpos2);

					if (iblockstate.getMaterial() != Material.LAVA && iblockstate.getBlock() != Blocks.MAGMA) {
						if (iblockstate.getMaterial() != Material.AIR) {
							++j;

							if (random.nextInt(j) == 0) {
								d0 = (double) blockpos2.getX() + d3;
								d1 = (double) ((float) blockpos2.getY() + 0.1F) + axisalignedbb.maxY - 1D;
								d2 = (double) blockpos2.getZ() + d4;
							}

							mc.world.spawnParticle(ParticleTypes.WATER_DROP, (double) blockpos2.getX() + d3, (double) ((float) blockpos2.getY() + 0.1F) + axisalignedbb.maxY, (double) blockpos2.getZ() + d4, 0D, 0D, 0D);
						}
					} else {
						mc.world.spawnParticle(ParticleTypes.SMOKE_NORMAL, (double) blockpos1.getX() + d3, (double) ((float) blockpos1.getY() + 0.1F) - axisalignedbb.minY, (double) blockpos1.getZ() + d4, 0D, 0D, 0D);
					}
				}
			}

			if (j > 0 && random.nextInt(3) < rainSoundCounter++) {
				rainSoundCounter = 0;

				if (d1 > (double) (blockpos.getY() + 1) && world.getPrecipitationHeight(blockpos).getY() > MathHelper.floor((float) blockpos.getY())) {
					mc.world.playSound(d0, d1, d2, SoundEvents.WEATHER_RAIN_ABOVE, SoundCategory.WEATHER, 0.1F, 0.5F, false);
				} else {
					mc.world.playSound(d0, d1, d2, SoundEvents.WEATHER_RAIN, SoundCategory.WEATHER, 0.2F, 1F, false);
				}
			}
		}
	}

	/**
	 * Render rain and snow
	 */
	protected void renderRainSnow(float partialTicks) {

		float f = mc.world.getRainStrength(partialTicks);

		if (f > 0F) {
			enableLightmap();
			Entity entity = mc.getRenderViewEntity();
			World world = mc.world;
			int i = MathHelper.floor(entity.posX);
			int j = MathHelper.floor(entity.posY);
			int k = MathHelper.floor(entity.posZ);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			GlStateManager.disableCull();
			GlStateManager.glNormal3f(0F, 1F, 0F);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.alphaFunc(516, 0.1F);
			double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
			double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
			double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
			int l = MathHelper.floor(d1);
			int i1 = 5;

			if (mc.gameSettings.fancyGraphics) {
				i1 = 10;
			}

			int j1 = -1;
			float f1 = (float) rendererUpdateCount + partialTicks;
			bufferbuilder.setTranslation(-d0, -d1, -d2);
			GlStateManager.color(1F, 1F, 1F, 1F);
			BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

			for (int k1 = k - i1; k1 <= k + i1; ++k1) {
				for (int l1 = i - i1; l1 <= i + i1; ++l1) {
					int i2 = (k1 - k + 16) * 32 + l1 - i + 16;
					double d3 = (double) rainXCoords[i2] * 0.5D;
					double d4 = (double) rainYCoords[i2] * 0.5D;
					blockpos$mutableblockpos.setPos(l1, 0, k1);
					Biome biome = world.getBiome(blockpos$mutableblockpos);

					if (biome.canRain() || biome.getEnableSnow()) {
						int j2 = world.getPrecipitationHeight(blockpos$mutableblockpos).getY();
						int k2 = j - i1;
						int l2 = j + i1;

						if (k2 < j2) {
							k2 = j2;
						}

						if (l2 < j2) {
							l2 = j2;
						}

						int i3 = Math.max(j2, l);

						if (k2 != l2) {
							random.setSeed((long) l1 * l1 * 3121 + l1 * 45238971L ^ (long) k1 * k1 * 418711 + k1 * 13761L);
							blockpos$mutableblockpos.setPos(l1, k2, k1);
							float f2 = biome.getTemperature(blockpos$mutableblockpos);

							if (world.getBiomeProvider().getTemperatureAtHeight(f2, j2) >= 0.15F) {
								if (j1 != 0) {
									if (j1 >= 0) {
										tessellator.draw();
									}

									j1 = 0;
									mc.getTextureManager().bindTexture(RAIN_TEXTURES);
									bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
								}

								double d5 = -((double) (rendererUpdateCount + l1 * l1 * 3121 + l1 * 45238971 + k1 * k1 * 418711 + k1 * 13761 & 31) + (double) partialTicks) / 32D * (3D + random.nextDouble());
								double d6 = (double) ((float) l1 + 0.5F) - entity.posX;
								double d7 = (double) ((float) k1 + 0.5F) - entity.posZ;
								float f3 = MathHelper.sqrt(d6 * d6 + d7 * d7) / (float) i1;
								float f4 = ((1F - f3 * f3) * 0.5F + 0.5F) * f;
								blockpos$mutableblockpos.setPos(l1, i3, k1);
								int j3 = world.getCombinedLight(blockpos$mutableblockpos, 0);
								int k3 = j3 >> 16 & 65535;
								int l3 = j3 & 65535;
								bufferbuilder.pos((double) l1 - d3 + 0.5D, l2, (double) k1 - d4 + 0.5D).tex(0D, (double) k2 * 0.25D + d5).color(1F, 1F, 1F, f4).lightmap(k3, l3).endVertex();
								bufferbuilder.pos((double) l1 + d3 + 0.5D, l2, (double) k1 + d4 + 0.5D).tex(1D, (double) k2 * 0.25D + d5).color(1F, 1F, 1F, f4).lightmap(k3, l3).endVertex();
								bufferbuilder.pos((double) l1 + d3 + 0.5D, k2, (double) k1 + d4 + 0.5D).tex(1D, (double) l2 * 0.25D + d5).color(1F, 1F, 1F, f4).lightmap(k3, l3).endVertex();
								bufferbuilder.pos((double) l1 - d3 + 0.5D, k2, (double) k1 - d4 + 0.5D).tex(0D, (double) l2 * 0.25D + d5).color(1F, 1F, 1F, f4).lightmap(k3, l3).endVertex();
							} else {
								if (j1 != 1) {
									if (j1 == 0) {
										tessellator.draw();
									}

									j1 = 1;
									mc.getTextureManager().bindTexture(SNOW_TEXTURES);
									bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
								}

								double d8 = -((float) (rendererUpdateCount & 511) + partialTicks) / 512F;
								double d9 = random.nextDouble() + (double) f1 * 0.01D * (double) ((float) random.nextGaussian());
								double d10 = random.nextDouble() + (double) (f1 * (float) random.nextGaussian()) * 0.001D;
								double d11 = (double) ((float) l1 + 0.5F) - entity.posX;
								double d12 = (double) ((float) k1 + 0.5F) - entity.posZ;
								float f6 = MathHelper.sqrt(d11 * d11 + d12 * d12) / (float) i1;
								float f5 = ((1F - f6 * f6) * 0.3F + 0.5F) * f;
								blockpos$mutableblockpos.setPos(l1, i3, k1);
								int i4 = (world.getCombinedLight(blockpos$mutableblockpos, 0) * 3 + 15728880) / 4;
								int j4 = i4 >> 16 & 65535;
								int k4 = i4 & 65535;
								bufferbuilder.pos((double) l1 - d3 + 0.5D, l2, (double) k1 - d4 + 0.5D).tex(0D + d9, (double) k2 * 0.25D + d8 + d10).color(1F, 1F, 1F, f5).lightmap(j4, k4).endVertex();
								bufferbuilder.pos((double) l1 + d3 + 0.5D, l2, (double) k1 + d4 + 0.5D).tex(1D + d9, (double) k2 * 0.25D + d8 + d10).color(1F, 1F, 1F, f5).lightmap(j4, k4).endVertex();
								bufferbuilder.pos((double) l1 + d3 + 0.5D, k2, (double) k1 + d4 + 0.5D).tex(1D + d9, (double) l2 * 0.25D + d8 + d10).color(1F, 1F, 1F, f5).lightmap(j4, k4).endVertex();
								bufferbuilder.pos((double) l1 - d3 + 0.5D, k2, (double) k1 - d4 + 0.5D).tex(0D + d9, (double) l2 * 0.25D + d8 + d10).color(1F, 1F, 1F, f5).lightmap(j4, k4).endVertex();
							}
						}
					}
				}
			}

			if (j1 >= 0) {
				tessellator.draw();
			}

			bufferbuilder.setTranslation(0D, 0D, 0D);
			GlStateManager.enableCull();
			GlStateManager.disableBlend();
			GlStateManager.alphaFunc(516, 0.1F);
			disableLightmap();
		}
	}

	/**
	 * Setup orthogonal projection for rendering GUI screen overlays
	 */
	public void setupOverlayRendering() {

		ScaledResolution scaledresolution = mc.scaledResolution;
		GlStateManager.clear(256);
		GlStateManager.matrixMode(5889);
		GlStateManager.loadIdentity();
		GlStateManager.ortho(0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0D, 1000D, 3000D);
		GlStateManager.matrixMode(5888);
		GlStateManager.loadIdentity();
		GlStateManager.translate(0F, 0F, -2000F);
	}

	/**
	 * calculates fog and calls glClearColor
	 */
	private void updateFogColor(float partialTicks) {

		World world = mc.world;
		Entity entity = mc.getRenderViewEntity();
		float f = 0.25F + 0.75F * (float) mc.gameSettings.renderDistanceChunks / 32F;
		f = 1F - (float) Math.pow(f, 0.25D);
		Vec3d vec3d = world.getSkyColor(mc.getRenderViewEntity(), partialTicks);
		float f1 = (float) vec3d.x();
		float f2 = (float) vec3d.y();
		float f3 = (float) vec3d.z();
		Vec3d vec3d1 = world.getFogColor(partialTicks);
		fogColorRed = (float) vec3d1.x();
		fogColorGreen = (float) vec3d1.y();
		fogColorBlue = (float) vec3d1.z();

		if (mc.gameSettings.renderDistanceChunks >= 4) {
			double d0 = MathHelper.sin(world.getCelestialAngleRadians(partialTicks)) > 0F ? -1D : 1D;
			Vec3d vec3d2 = new Vec3d(d0, 0D, 0D);
			float f5 = (float) entity.getLook(partialTicks).dotProduct(vec3d2);

			if (f5 < 0F) {
				f5 = 0F;
			}

			if (f5 > 0F) {
				float[] afloat = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTicks), partialTicks);

				if (afloat != null) {
					f5 = f5 * afloat[3];
					fogColorRed = fogColorRed * (1F - f5) + afloat[0] * f5;
					fogColorGreen = fogColorGreen * (1F - f5) + afloat[1] * f5;
					fogColorBlue = fogColorBlue * (1F - f5) + afloat[2] * f5;
				}
			}
		}

		fogColorRed += (f1 - fogColorRed) * f;
		fogColorGreen += (f2 - fogColorGreen) * f;
		fogColorBlue += (f3 - fogColorBlue) * f;
		float f8 = world.getRainStrength(partialTicks);

		if (f8 > 0F) {
			float f4 = 1F - f8 * 0.5F;
			float f10 = 1F - f8 * 0.4F;
			fogColorRed *= f4;
			fogColorGreen *= f4;
			fogColorBlue *= f10;
		}

		float f9 = world.getThunderStrength(partialTicks);

		if (f9 > 0F) {
			float f11 = 1F - f9 * 0.5F;
			fogColorRed *= f11;
			fogColorGreen *= f11;
			fogColorBlue *= f11;
		}

		IBlockState iblockstate = ActiveRenderInfo.getBlockStateAtEntityViewpoint(mc.world, entity, partialTicks);

		if (cloudFog) {
			Vec3d vec3d3 = world.getCloudColour(partialTicks);
			fogColorRed = (float) vec3d3.x();
			fogColorGreen = (float) vec3d3.y();
			fogColorBlue = (float) vec3d3.z();
		} else if (iblockstate.getMaterial() == Material.WATER) {
			float f12 = 0F;

			if (entity instanceof EntityLivingBase) {
				f12 = (float) EnchantmentHelper.getRespirationModifier((EntityLivingBase) entity) * 0.2F;

				if (((EntityLivingBase) entity).isPotionActive(MobEffects.WATER_BREATHING)) {
					f12 = f12 * 0.3F + 0.6F;
				}
			}

			fogColorRed = 0.02F + f12;
			fogColorGreen = 0.02F + f12;
			fogColorBlue = 0.2F + f12;
		} else if (iblockstate.getMaterial() == Material.LAVA) {
			fogColorRed = 0.6F;
			fogColorGreen = 0.1F;
			fogColorBlue = 0F;
		}

		float f13 = fogColor2 + (fogColor1 - fogColor2) * partialTicks;
		fogColorRed *= f13;
		fogColorGreen *= f13;
		fogColorBlue *= f13;
		double d1 = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks) * world.provider.getVoidFogYFactor();

		if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(MobEffects.BLINDNESS)) {
			int i = ((EntityLivingBase) entity).getActivePotionEffect(MobEffects.BLINDNESS).getDuration();

			if (i < 20) {
				d1 *= 1F - (float) i / 20F;
			} else {
				d1 = 0D;
			}
		}

		if (d1 < 1D) {
			if (d1 < 0D) {
				d1 = 0D;
			}

			d1 = d1 * d1;
			fogColorRed = (float) ((double) fogColorRed * d1);
			fogColorGreen = (float) ((double) fogColorGreen * d1);
			fogColorBlue = (float) ((double) fogColorBlue * d1);
		}

		if (bossColorModifier > 0F) {
			float f14 = bossColorModifierPrev + (bossColorModifier - bossColorModifierPrev) * partialTicks;
			fogColorRed = fogColorRed * (1F - f14) + fogColorRed * 0.7F * f14;
			fogColorGreen = fogColorGreen * (1F - f14) + fogColorGreen * 0.6F * f14;
			fogColorBlue = fogColorBlue * (1F - f14) + fogColorBlue * 0.6F * f14;
		}

		if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(MobEffects.NIGHT_VISION)) {
			float f15 = getNightVisionBrightness((EntityLivingBase) entity, partialTicks);
			float f6 = 1F / fogColorRed;

			if (f6 > 1F / fogColorGreen) {
				f6 = 1F / fogColorGreen;
			}

			if (f6 > 1F / fogColorBlue) {
				f6 = 1F / fogColorBlue;
			}

			fogColorRed = fogColorRed * (1F - f15) + fogColorRed * f6 * f15;
			fogColorGreen = fogColorGreen * (1F - f15) + fogColorGreen * f6 * f15;
			fogColorBlue = fogColorBlue * (1F - f15) + fogColorBlue * f6 * f15;
		}

		if (mc.gameSettings.anaglyph) {
			float f16 = (fogColorRed * 30F + fogColorGreen * 59F + fogColorBlue * 11F) / 100F;
			float f17 = (fogColorRed * 30F + fogColorGreen * 70F) / 100F;
			float f7 = (fogColorRed * 30F + fogColorBlue * 70F) / 100F;
			fogColorRed = f16;
			fogColorGreen = f17;
			fogColorBlue = f7;
		}

		GlStateManager.clearColor(fogColorRed, fogColorGreen, fogColorBlue, 0F);
	}

	/**
	 * Sets up the fog to be rendered. If the arg passed in is -1 the fog starts at 0 and goes to 80% of far plane
	 * distance and is used for sky rendering.
	 */
	private void setupFog(int startCoords, float partialTicks) {

		Entity entity = mc.getRenderViewEntity();
		setupFogColor(false);
		GlStateManager.glNormal3f(0F, -1F, 0F);
		GlStateManager.color(1F, 1F, 1F, 1F);
		IBlockState iblockstate = ActiveRenderInfo.getBlockStateAtEntityViewpoint(mc.world, entity, partialTicks);

		if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(MobEffects.BLINDNESS)) {
			float f1 = 5F;
			int i = ((EntityLivingBase) entity).getActivePotionEffect(MobEffects.BLINDNESS).getDuration();

			if (i < 20) {
				f1 = 5F + (farPlaneDistance - 5F) * (1F - (float) i / 20F);
			}

			GlStateManager.setFog(GlStateManager.FogMode.LINEAR);

			if (startCoords == -1) {
				GlStateManager.setFogStart(0F);
				GlStateManager.setFogEnd(f1 * 0.8F);
			} else {
				GlStateManager.setFogStart(f1 * 0.25F);
				GlStateManager.setFogEnd(f1);
			}

			if (GLContext.getCapabilities().GL_NV_fog_distance) {
				GlStateManager.glFogi(34138, 34139);
			}
		} else if (cloudFog) {
			GlStateManager.setFog(GlStateManager.FogMode.EXP);
			GlStateManager.setFogDensity(0.1F);
		} else if (iblockstate.getMaterial() == Material.WATER) {
			GlStateManager.setFog(GlStateManager.FogMode.EXP);

			if (entity instanceof EntityLivingBase) {
				if (((EntityLivingBase) entity).isPotionActive(MobEffects.WATER_BREATHING)) {
					GlStateManager.setFogDensity(0.01F);
				} else {
					GlStateManager.setFogDensity(0.1F - (float) EnchantmentHelper.getRespirationModifier((EntityLivingBase) entity) * 0.03F);
				}
			} else {
				GlStateManager.setFogDensity(0.1F);
			}
		} else if (iblockstate.getMaterial() == Material.LAVA) {
			GlStateManager.setFog(GlStateManager.FogMode.EXP);
			GlStateManager.setFogDensity(2F);
		} else {
			float f = farPlaneDistance;
			GlStateManager.setFog(GlStateManager.FogMode.LINEAR);

			if (startCoords == -1) {
				GlStateManager.setFogStart(0F);
				GlStateManager.setFogEnd(f);
			} else {
				GlStateManager.setFogStart(f * 0.75F);
				GlStateManager.setFogEnd(f);
			}

			if (GLContext.getCapabilities().GL_NV_fog_distance) {
				GlStateManager.glFogi(34138, 34139);
			}

			if (mc.world.provider.doesXZShowFog((int) entity.posX, (int) entity.posZ) || mc.ingameGUI.getBossOverlay().shouldCreateFog()) {
				GlStateManager.setFogStart(f * 0.05F);
				GlStateManager.setFogEnd(Math.min(f, 192F) * 0.5F);
			}
		}

		GlStateManager.enableColorMaterial();
		GlStateManager.enableFog();
		GlStateManager.colorMaterial(1028, 4608);
	}

	public void setupFogColor(boolean black) {

		if (black) {
			GlStateManager.glFog(2918, setFogColorBuffer(0F, 0F, 0F, 1F));
		} else {
			GlStateManager.glFog(2918, setFogColorBuffer(fogColorRed, fogColorGreen, fogColorBlue, 1F));
		}
	}

	/**
	 * Update and return fogColorBuffer with the RGBA values passed as arguments
	 */
	private FloatBuffer setFogColorBuffer(float red, float green, float blue, float alpha) {

		fogColorBuffer.clear();
		fogColorBuffer.put(red).put(green).put(blue).put(alpha);
		fogColorBuffer.flip();
		return fogColorBuffer;
	}

	public void resetData() {

		itemActivationItem = null;
		mapItemRenderer.clearLoadedMaps();
	}

	public MapItemRenderer getMapItemRenderer() {

		return mapItemRenderer;
	}

	public void displayItemActivation(ItemStack stack) {

		itemActivationItem = stack;
		itemActivationTicks = 40;
		itemActivationOffX = random.nextFloat() * 2F - 1F;
		itemActivationOffY = random.nextFloat() * 2F - 1F;
	}

	private void renderItemActivation(int p_190563_1_, int p_190563_2_, float p_190563_3_) {

		if (itemActivationItem != null && itemActivationTicks > 0) {
			int i = 40 - itemActivationTicks;
			float f = ((float) i + p_190563_3_) / 40F;
			float f1 = f * f;
			float f2 = f * f1;
			float f3 = 10.25F * f2 * f1 + -24.95F * f1 * f1 + 25.5F * f2 + -13.8F * f1 + 4F * f;
			float f4 = f3 * (float) Math.PI;
			float f5 = itemActivationOffX * (float) (p_190563_1_ / 4);
			float f6 = itemActivationOffY * (float) (p_190563_2_ / 4);
			GlStateManager.enableAlpha();
			GlStateManager.pushMatrix();
			GlStateManager.pushAttrib();
			GlStateManager.enableDepth();
			GlStateManager.disableCull();
			RenderHelper.enableStandardItemLighting();
			GlStateManager.translate((float) (p_190563_1_ / 2) + f5 * MathHelper.abs(MathHelper.sin(f4 * 2F)), (float) (p_190563_2_ / 2) + f6 * MathHelper.abs(MathHelper.sin(f4 * 2F)), -50F);
			float f7 = 50F + 175F * MathHelper.sin(f4);
			GlStateManager.scale(f7, -f7, f7);
			GlStateManager.rotate(900F * MathHelper.abs(MathHelper.sin(f4)), 0F, 1F, 0F);
			GlStateManager.rotate(6F * MathHelper.cos(f * 8F), 1F, 0F, 0F);
			GlStateManager.rotate(6F * MathHelper.cos(f * 8F), 0F, 0F, 1F);
			mc.getRenderItem().renderItem(itemActivationItem, ItemCameraTransforms.TransformType.FIXED);
			GlStateManager.popAttrib();
			GlStateManager.popMatrix();
			RenderHelper.disableStandardItemLighting();
			GlStateManager.enableCull();
			GlStateManager.disableDepth();
		}
	}

}
