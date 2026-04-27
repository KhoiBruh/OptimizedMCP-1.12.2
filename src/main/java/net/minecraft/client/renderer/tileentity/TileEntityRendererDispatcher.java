package net.minecraft.client.renderer.tileentity;

import com.google.common.collect.Maps;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelShulker;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.*;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Map;

public class TileEntityRendererDispatcher {

	private final Map<Class<? extends TileEntity>, TileEntitySpecialRenderer<? extends TileEntity>> renderers = Maps.newHashMap();
	public static TileEntityRendererDispatcher instance = new TileEntityRendererDispatcher();
	private FontRenderer fontRenderer;

	/**
	 * The player's current X position (same as playerX)
	 */
	public static double staticPlayerX;

	/**
	 * The player's current Y position (same as playerY)
	 */
	public static double staticPlayerY;

	/**
	 * The player's current Z position (same as playerZ)
	 */
	public static double staticPlayerZ;
	public TextureManager renderEngine;
	public World world;
	public Entity entity;
	public float entityYaw;
	public float entityPitch;
	public RayTraceResult cameraHitResult;
	public double entityX;
	public double entityY;
	public double entityZ;

	private TileEntityRendererDispatcher() {

		renderers.put(TileEntitySign.class, new TileEntitySignRenderer());
		renderers.put(TileEntityMobSpawner.class, new TileEntityMobSpawnerRenderer());
		renderers.put(TileEntityPiston.class, new TileEntityPistonRenderer());
		renderers.put(TileEntityChest.class, new TileEntityChestRenderer());
		renderers.put(TileEntityEnderChest.class, new TileEntityEnderChestRenderer());
		renderers.put(TileEntityEnchantmentTable.class, new TileEntityEnchantmentTableRenderer());
		renderers.put(TileEntityEndPortal.class, new TileEntityEndPortalRenderer());
		renderers.put(TileEntityEndGateway.class, new TileEntityEndGatewayRenderer());
		renderers.put(TileEntityBeacon.class, new TileEntityBeaconRenderer());
		renderers.put(TileEntitySkull.class, new TileEntitySkullRenderer());
		renderers.put(TileEntityBanner.class, new TileEntityBannerRenderer());
		renderers.put(TileEntityStructure.class, new TileEntityStructureRenderer());
		renderers.put(TileEntityShulkerBox.class, new TileEntityShulkerBoxRenderer(new ModelShulker()));
		renderers.put(TileEntityBed.class, new TileEntityBedRenderer());

		for (TileEntitySpecialRenderer<?> tileentityspecialrenderer : renderers.values()) {
			tileentityspecialrenderer.setRendererDispatcher(this);
		}
	}

	public <T extends TileEntity> TileEntitySpecialRenderer<T> getRenderer(Class<? extends TileEntity> teClass) {

		TileEntitySpecialRenderer<T> tileentityspecialrenderer = (TileEntitySpecialRenderer) renderers.get(teClass);

		if (tileentityspecialrenderer == null && teClass != TileEntity.class) {
			tileentityspecialrenderer = getRenderer((Class<? extends TileEntity>) teClass.getSuperclass());
			renderers.put(teClass, tileentityspecialrenderer);
		}

		return tileentityspecialrenderer;
	}

	@Nullable
	public <T extends TileEntity> TileEntitySpecialRenderer<T> getRenderer(@Nullable TileEntity tileEntityIn) {

		return tileEntityIn == null ? null : getRenderer(tileEntityIn.getClass());
	}

	public void prepare(World worldIn, TextureManager renderEngineIn, FontRenderer fontRendererIn, Entity entityIn, RayTraceResult cameraHitResultIn, float p_190056_6_) {

		if (world != worldIn) {
			setWorld(worldIn);
		}

		renderEngine = renderEngineIn;
		entity = entityIn;
		fontRenderer = fontRendererIn;
		cameraHitResult = cameraHitResultIn;
		entityYaw = entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * p_190056_6_;
		entityPitch = entityIn.prevRotationPitch + (entityIn.rotationPitch - entityIn.prevRotationPitch) * p_190056_6_;
		entityX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) p_190056_6_;
		entityY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) p_190056_6_;
		entityZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) p_190056_6_;
	}

	public void render(TileEntity tileentityIn, float partialTicks, int destroyStage) {

		if (tileentityIn.getDistanceSq(entityX, entityY, entityZ) < tileentityIn.getMaxRenderDistanceSquared()) {
			RenderHelper.enableStandardItemLighting();
			int i = world.getCombinedLight(tileentityIn.getPos(), 0);
			int j = i % 65536;
			int k = i / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			BlockPos blockpos = tileentityIn.getPos();
			render(tileentityIn, (double) blockpos.getX() - staticPlayerX, (double) blockpos.getY() - staticPlayerY, (double) blockpos.getZ() - staticPlayerZ, partialTicks, destroyStage, 1.0F);
		}
	}

	/**
	 * Render this TileEntity at a given set of coordinates
	 */
	public void render(TileEntity tileEntityIn, double x, double y, double z, float partialTicks) {

		render(tileEntityIn, x, y, z, partialTicks, 1.0F);
	}

	public void render(TileEntity p_192855_1_, double p_192855_2_, double p_192855_4_, double p_192855_6_, float p_192855_8_, float p_192855_9_) {

		render(p_192855_1_, p_192855_2_, p_192855_4_, p_192855_6_, p_192855_8_, -1, p_192855_9_);
	}

	public void render(TileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage, float p_192854_10_) {

		TileEntitySpecialRenderer<TileEntity> tileentityspecialrenderer = this.getRenderer(tileEntityIn);

		if (tileentityspecialrenderer != null) {
			try {
				tileentityspecialrenderer.render(tileEntityIn, x, y, z, partialTicks, destroyStage, p_192854_10_);
			} catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Block Entity");
				CrashReportCategory crashreportcategory = crashreport.makeCategory("Block Entity Details");
				tileEntityIn.addInfoToCrashReport(crashreportcategory);
				throw new ReportedException(crashreport);
			}
		}
	}

	public void setWorld(@Nullable World worldIn) {

		world = worldIn;

		if (worldIn == null) {
			entity = null;
		}
	}

	public FontRenderer getFontRenderer() {

		return fontRenderer;
	}

}
