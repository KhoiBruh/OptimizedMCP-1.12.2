package net.minecraft.client.gui;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.Display;

import java.util.List;
import java.util.Map.Entry;

public class GuiOverlayDebug extends Gui {

	private final Minecraft mc;
	private final FontRenderer fontRenderer;

	public GuiOverlayDebug(Minecraft mc) {

		this.mc = mc;
		fontRenderer = mc.fontRenderer;
	}

	public void renderDebugInfo(ScaledResolution scaledResolutionIn) {

		mc.mcProfiler.startSection("debug");
		GlStateManager.pushMatrix();
		renderDebugInfoLeft();
		renderDebugInfoRight(scaledResolutionIn);
		GlStateManager.popMatrix();

		if (mc.gameSettings.showLagometer) {
			renderLagometer();
		}

		mc.mcProfiler.endSection();
	}

	protected void renderDebugInfoLeft() {

		List<String> list = call();
		list.add("");
		list.add("Debug: Pie [shift]: " + (mc.gameSettings.showDebugProfilerChart ? "visible" : "hidden") + " FPS [alt]: " + (mc.gameSettings.showLagometer ? "visible" : "hidden"));
		list.add("For help: press F3 + Q");

		for (int i = 0; i < list.size(); ++i) {
			String s = list.get(i);

			if (!Strings.isNullOrEmpty(s)) {
				int j = fontRenderer.FONT_HEIGHT;
				int k = fontRenderer.getStringWidth(s);
				int l = 2;
				int i1 = 2 + j * i;
				drawRect(1, i1 - 1, 2 + k + 1, i1 + j - 1, -1873784752);
				fontRenderer.drawString(s, 2, i1, 14737632);
			}
		}
	}

	protected void renderDebugInfoRight(ScaledResolution scaledRes) {

		List<String> list = getDebugInfoRight();

		for (int i = 0; i < list.size(); ++i) {
			String s = list.get(i);

			if (!Strings.isNullOrEmpty(s)) {
				int j = fontRenderer.FONT_HEIGHT;
				int k = fontRenderer.getStringWidth(s);
				int l = scaledRes.getScaledWidth() - 2 - k;
				int i1 = 2 + j * i;
				drawRect(l - 1, i1 - 1, l + k + 1, i1 + j - 1, -1873784752);
				fontRenderer.drawString(s, l, i1, 14737632);
			}
		}
	}

	@SuppressWarnings("incomplete-switch")
	protected List<String> call() {

		BlockPos blockpos = new BlockPos(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().getEntityBoundingBox().minY, mc.getRenderViewEntity().posZ);

		if (mc.isReducedDebug()) {
			return Lists.newArrayList("Minecraft 1.12.2 (" + mc.getVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", mc.debug, mc.renderGlobal.getDebugInfoRenders(), mc.renderGlobal.getDebugInfoEntities(), "P: " + mc.effectRenderer.getStatistics() + ". T: " + mc.world.getDebugLoadedEntities(), mc.world.getProviderName(), "", String.format("Chunk-relative: %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15));
		} else {
			Entity entity = mc.getRenderViewEntity();
			EnumFacing enumfacing = entity.getHorizontalFacing();
			String s = "Invalid";

			switch (enumfacing) {
				case NORTH:
					s = "Towards negative Z";
					break;

				case SOUTH:
					s = "Towards positive Z";
					break;

				case WEST:
					s = "Towards negative X";
					break;

				case EAST:
					s = "Towards positive X";
			}

			List<String> list = Lists.newArrayList("Minecraft 1.12.2 (" + mc.getVersion() + "/" + ClientBrandRetriever.getClientModName() + ("release".equalsIgnoreCase(mc.getVersionType()) ? "" : "/" + mc.getVersionType()) + ")", mc.debug, mc.renderGlobal.getDebugInfoRenders(), mc.renderGlobal.getDebugInfoEntities(), "P: " + mc.effectRenderer.getStatistics() + ". T: " + mc.world.getDebugLoadedEntities(), mc.world.getProviderName(), "", String.format("XYZ: %.3f / %.5f / %.3f", mc.getRenderViewEntity().posX, mc.getRenderViewEntity().getEntityBoundingBox().minY, mc.getRenderViewEntity().posZ), String.format("Block: %d %d %d", blockpos.getX(), blockpos.getY(), blockpos.getZ()), String.format("Chunk: %d %d %d in %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15, blockpos.getX() >> 4, blockpos.getY() >> 4, blockpos.getZ() >> 4), String.format("Facing: %s (%s) (%.1f / %.1f)", enumfacing, s, MathHelper.wrapDegrees(entity.rotationYaw), MathHelper.wrapDegrees(entity.rotationPitch)));

			if (mc.world != null) {
				Chunk chunk = mc.world.getChunkFromBlockCoords(blockpos);

				if (mc.world.isBlockLoaded(blockpos) && blockpos.getY() >= 0 && blockpos.getY() < 256) {
					if (!chunk.isEmpty()) {
						list.add("Biome: " + chunk.getBiome(blockpos, mc.world.getBiomeProvider()).getBiomeName());
						list.add("Light: " + chunk.getLightSubtracted(blockpos, 0) + " (" + chunk.getLightFor(EnumSkyBlock.SKY, blockpos) + " sky, " + chunk.getLightFor(EnumSkyBlock.BLOCK, blockpos) + " block)");
						DifficultyInstance difficultyinstance = mc.world.getDifficultyForLocation(blockpos);

						if (mc.isIntegratedServerRunning() && mc.getIntegratedServer() != null) {
							EntityPlayerMP entityplayermp = mc.getIntegratedServer().getPlayerList().getPlayerByUUID(mc.player.getUniqueID());

							if (entityplayermp != null) {
								difficultyinstance = entityplayermp.world.getDifficultyForLocation(new BlockPos(entityplayermp));
							}
						}

						list.add(String.format("Local Difficulty: %.2f // %.2f (Day %d)", difficultyinstance.getAdditionalDifficulty(), difficultyinstance.getClampedAdditionalDifficulty(), mc.world.getWorldTime() / 24000L));
					} else {
						list.add("Waiting for chunk...");
					}
				} else {
					list.add("Outside of world...");
				}
			}

			if (mc.entityRenderer != null && mc.entityRenderer.isShaderActive()) {
				list.add("Shader: " + mc.entityRenderer.getShaderGroup().getShaderGroupName());
			}

			if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK && mc.objectMouseOver.getBlockPos() != null) {
				BlockPos blockpos1 = mc.objectMouseOver.getBlockPos();
				list.add(String.format("Looking at: %d %d %d", blockpos1.getX(), blockpos1.getY(), blockpos1.getZ()));
			}

			return list;
		}
	}

	protected <T extends Comparable<T>> List<String> getDebugInfoRight() {

		long i = Runtime.getRuntime().maxMemory();
		long j = Runtime.getRuntime().totalMemory();
		long k = Runtime.getRuntime().freeMemory();
		long l = j - k;
		List<String> list = Lists.newArrayList(String.format("Java: %s %dbit", System.getProperty("java.version"), mc.isJava64bit() ? 64 : 32), String.format("Mem: % 2d%% %03d/%03dMB", l * 100L / i, bytesToMb(l), bytesToMb(i)), String.format("Allocated: % 2d%% %03dMB", j * 100L / i, bytesToMb(j)), "", String.format("CPU: %s", OpenGlHelper.getCpu()), "", String.format("Display: %dx%d (%s)", Display.getWidth(), Display.getHeight(), GlStateManager.glGetString(7936)), GlStateManager.glGetString(7937), GlStateManager.glGetString(7938));

		if (mc.isReducedDebug()) {
			return list;
		} else {
			if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK && mc.objectMouseOver.getBlockPos() != null) {
				BlockPos blockpos = mc.objectMouseOver.getBlockPos();
				IBlockState iblockstate = mc.world.getBlockState(blockpos);

				if (mc.world.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
					iblockstate = iblockstate.getActualState(mc.world, blockpos);
				}

				list.add("");
				list.add(String.valueOf(Block.REGISTRY.getNameForObject(iblockstate.getBlock())));
				IProperty<T> iproperty;
				String s;

				for (UnmodifiableIterator unmodifiableiterator = iblockstate.getProperties().entrySet().iterator(); unmodifiableiterator.hasNext(); list.add(iproperty.getName() + ": " + s)) {
					Entry<IProperty<?>, Comparable<?>> entry = (Entry) unmodifiableiterator.next();
					iproperty = (IProperty) entry.getKey();
					T t = (T) entry.getValue();
					s = iproperty.getName(t);

					if (Boolean.TRUE.equals(t)) {
						s = TextFormatting.GREEN + s;
					} else if (Boolean.FALSE.equals(t)) {
						s = TextFormatting.RED + s;
					}
				}
			}

			return list;
		}
	}

	private void renderLagometer() {

		GlStateManager.disableDepth();
		FrameTimer frametimer = mc.getFrameTimer();
		int i = frametimer.getLastIndex();
		int j = frametimer.getIndex();
		long[] along = frametimer.getFrames();
		ScaledResolution scaledresolution = new ScaledResolution(mc);
		int k = i;
		int l = 0;
		drawRect(0, scaledresolution.getScaledHeight() - 60, 240, scaledresolution.getScaledHeight(), -1873784752);

		while (k != j) {
			int i1 = frametimer.getLagometerValue(along[k], 30);
			int j1 = getFrameColor(MathHelper.clamp(i1, 0, 60), 0, 30, 60);
			drawVerticalLine(l, scaledresolution.getScaledHeight(), scaledresolution.getScaledHeight() - i1, j1);
			++l;
			k = frametimer.parseIndex(k + 1);
		}

		drawRect(1, scaledresolution.getScaledHeight() - 30 + 1, 14, scaledresolution.getScaledHeight() - 30 + 10, -1873784752);
		fontRenderer.drawString("60", 2, scaledresolution.getScaledHeight() - 30 + 2, 14737632);
		drawHorizontalLine(0, 239, scaledresolution.getScaledHeight() - 30, -1);
		drawRect(1, scaledresolution.getScaledHeight() - 60 + 1, 14, scaledresolution.getScaledHeight() - 60 + 10, -1873784752);
		fontRenderer.drawString("30", 2, scaledresolution.getScaledHeight() - 60 + 2, 14737632);
		drawHorizontalLine(0, 239, scaledresolution.getScaledHeight() - 60, -1);
		drawHorizontalLine(0, 239, scaledresolution.getScaledHeight() - 1, -1);
		drawVerticalLine(0, scaledresolution.getScaledHeight() - 60, scaledresolution.getScaledHeight(), -1);
		drawVerticalLine(239, scaledresolution.getScaledHeight() - 60, scaledresolution.getScaledHeight(), -1);

		if (mc.gameSettings.limitFramerate <= 120) {
			drawHorizontalLine(0, 239, scaledresolution.getScaledHeight() - 60 + mc.gameSettings.limitFramerate / 2, -16711681);
		}

		GlStateManager.enableDepth();
	}

	private int getFrameColor(int p_181552_1_, int p_181552_2_, int p_181552_3_, int p_181552_4_) {

		return p_181552_1_ < p_181552_3_ ? blendColors(-16711936, -256, (float) p_181552_1_ / (float) p_181552_3_) : blendColors(-256, -65536, (float) (p_181552_1_ - p_181552_3_) / (float) (p_181552_4_ - p_181552_3_));
	}

	private int blendColors(int p_181553_1_, int p_181553_2_, float p_181553_3_) {

		int i = p_181553_1_ >> 24 & 255;
		int j = p_181553_1_ >> 16 & 255;
		int k = p_181553_1_ >> 8 & 255;
		int l = p_181553_1_ & 255;
		int i1 = p_181553_2_ >> 24 & 255;
		int j1 = p_181553_2_ >> 16 & 255;
		int k1 = p_181553_2_ >> 8 & 255;
		int l1 = p_181553_2_ & 255;
		int i2 = MathHelper.clamp((int) ((float) i + (float) (i1 - i) * p_181553_3_), 0, 255);
		int j2 = MathHelper.clamp((int) ((float) j + (float) (j1 - j) * p_181553_3_), 0, 255);
		int k2 = MathHelper.clamp((int) ((float) k + (float) (k1 - k) * p_181553_3_), 0, 255);
		int l2 = MathHelper.clamp((int) ((float) l + (float) (l1 - l) * p_181553_3_), 0, 255);
		return i2 << 24 | j2 << 16 | k2 << 8 | l2;
	}

	private static long bytesToMb(long bytes) {

		return bytes / 1024L / 1024L;
	}

}
