package net.minecraft.client.gui;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
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
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Map.Entry;

public class GuiOverlayDebug extends Gui {

	private static final int DEBUG_MARGIN = 2;
	private static final int DEBUG_BACKGROUND_COLOR = -1873784752;
	private static final int DEBUG_TEXT_COLOR = 14737632;
	private static final int LAGOMETER_WIDTH = 240;
	private static final int LAGOMETER_HEIGHT = 60;
	private static final int LAGOMETER_MID_HEIGHT = 30;
	private static final int LAGOMETER_BORDER_COLOR = -1;
	private static final int LAGOMETER_LIMIT_COLOR = -16711681;
	private static final int FRAME_COLOR_FAST = -16711936;
	private static final int FRAME_COLOR_MEDIUM = -256;
	private static final int FRAME_COLOR_SLOW = -65536;

	private final Minecraft mc;
	private final FontRenderer fontRenderer;
	private final String vendor;
	private final String renderer;
	private final String version;

	public GuiOverlayDebug(Minecraft mc) {

		this.mc = mc;
		fontRenderer = mc.fontRenderer;
		vendor = GlStateManager.glGetString(GL11.GL_VENDOR);
		renderer = GlStateManager.glGetString(GL11.GL_RENDERER);
		version = GlStateManager.glGetString(GL11.GL_VERSION);
	}

	private static long bytesToMb(long bytes) {

		return bytes / 1024L / 1024L;
	}

	private static String getFacingDescription(EnumFacing facing) {

		return switch (facing) {
			case NORTH -> "Towards negative Z";
			case SOUTH -> "Towards positive Z";
			case WEST -> "Towards negative X";
			case EAST -> "Towards positive X";
			default -> "Invalid";
		};
	}

	private static String formatProperty(IProperty<?> property, Comparable<?> value) {

		String valueName = getPropertyValueName(property, value);

		if (Boolean.TRUE.equals(value)) {
			valueName = TextFormatting.GREEN + valueName;
		} else if (Boolean.FALSE.equals(value)) {
			valueName = TextFormatting.RED + valueName;
		}

		return property.getName() + ": " + valueName;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static String getPropertyValueName(IProperty property, Comparable value) {

		return property.getName(value);
	}

	private static int getFrameColor(int value, int warningValue, int maxValue) {

		return value < warningValue ? blendColors(FRAME_COLOR_FAST, FRAME_COLOR_MEDIUM, (float) value / (float) warningValue) : blendColors(FRAME_COLOR_MEDIUM, FRAME_COLOR_SLOW, (float) (value - warningValue) / (float) (maxValue - warningValue));
	}

	private static int blendColors(int firstColor, int secondColor, float ratio) {

		int firstAlpha = firstColor >> 24 & 255;
		int firstRed = firstColor >> 16 & 255;
		int firstGreen = firstColor >> 8 & 255;
		int firstBlue = firstColor & 255;
		int secondAlpha = secondColor >> 24 & 255;
		int secondRed = secondColor >> 16 & 255;
		int secondGreen = secondColor >> 8 & 255;
		int secondBlue = secondColor & 255;
		int alpha = MathHelper.clamp((int) ((float) firstAlpha + (float) (secondAlpha - firstAlpha) * ratio), 0, 255);
		int red = MathHelper.clamp((int) ((float) firstRed + (float) (secondRed - firstRed) * ratio), 0, 255);
		int green = MathHelper.clamp((int) ((float) firstGreen + (float) (secondGreen - firstGreen) * ratio), 0, 255);
		int blue = MathHelper.clamp((int) ((float) firstBlue + (float) (secondBlue - firstBlue) * ratio), 0, 255);
		return alpha << 24 | red << 16 | green << 8 | blue;
	}

	public void renderDebugInfo(ScaledResolution scaledResolutionIn) {

		mc.mcProfiler.startSection("debug");
		try {
			GlStateManager.pushMatrix();
			try {
				renderDebugInfoLeft();
				renderDebugInfoRight(scaledResolutionIn);
			} finally {
				GlStateManager.popMatrix();
			}

			if (mc.gameSettings.showLagometer) {
				renderLagometer();
			}
		} finally {
			mc.mcProfiler.endSection();
		}
	}

	protected void renderDebugInfoLeft() {

		List<String> list = call();
		list.add("");
		list.add("Debug: Pie [shift]: " + (mc.gameSettings.showDebugProfilerChart ? "visible" : "hidden") + " FPS [alt]: " + (mc.gameSettings.showLagometer ? "visible" : "hidden"));
		list.add("For help: press F3 + Q");
		drawDebugText(list, DEBUG_MARGIN, false);
	}

	protected void renderDebugInfoRight(ScaledResolution scaledRes) {

		drawDebugText(getDebugInfoRight(), scaledRes.getScaledWidth() - DEBUG_MARGIN, true);
	}

	private void drawDebugText(List<String> lines, int xAnchor, boolean alignRight) {

		for (int i = 0; i < lines.size(); ++i) {
			String line = lines.get(i);

			if (!Strings.isNullOrEmpty(line)) {
				int lineHeight = fontRenderer.FONT_HEIGHT;
				int width = fontRenderer.getStringWidth(line);
				int x = alignRight ? xAnchor - width : xAnchor;
				int y = DEBUG_MARGIN + lineHeight * i;
				drawRect(x - 1, y - 1, x + width + 1, y + lineHeight - 1, DEBUG_BACKGROUND_COLOR);
				fontRenderer.drawString(line, x, y, DEBUG_TEXT_COLOR);
			}
		}
	}

	protected List<String> call() {

		return getDebugInfoLeft();
	}

	protected List<String> getDebugInfoLeft() {

		Entity entity = mc.getRenderViewEntity();
		BlockPos blockpos = new BlockPos(entity.posX, entity.getEntityBoundingBox().minY, entity.posZ);

		if (mc.isReducedDebug()) {
			List<String> list = getDebugInfoHeader(false);
			list.add("");
			list.add(String.format("Chunk-relative: %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15));
			return list;
		} else {
			EnumFacing enumfacing = entity.getHorizontalFacing();
			List<String> list = getDebugInfoHeader(true);
			list.add("");
			list.add(String.format("XYZ: %.3f / %.5f / %.3f", entity.posX, entity.getEntityBoundingBox().minY, entity.posZ));
			list.add(String.format("Block: %d %d %d", blockpos.getX(), blockpos.getY(), blockpos.getZ()));
			list.add(String.format("Chunk: %d %d %d in %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15, blockpos.getX() >> 4, blockpos.getY() >> 4, blockpos.getZ() >> 4));
			list.add(String.format("Facing: %s (%s) (%.1f / %.1f)", enumfacing, getFacingDescription(enumfacing), MathHelper.wrapDegrees(entity.rotationYaw), MathHelper.wrapDegrees(entity.rotationPitch)));

			if (mc.world != null) {
				Chunk chunk = mc.world.getChunkFromBlockCoords(blockpos);

				if (mc.world.isBlockLoaded(blockpos) && blockpos.getY() >= 0 && blockpos.getY() < 256) {
					if (!chunk.isEmpty()) {
						list.add("Biome: " + chunk.getBiome(blockpos, mc.world.getBiomeProvider()).getBiomeName());
						list.add("Light: " + chunk.getLightSubtracted(blockpos, 0) + " (" + chunk.getLightFor(EnumSkyBlock.SKY, blockpos) + " sky, " + chunk.getLightFor(EnumSkyBlock.BLOCK, blockpos) + " block)");
						DifficultyInstance difficultyinstance = mc.world.getDifficultyForLocation(blockpos);

						if (mc.isIntegratedServerRunning() && mc.getIntegratedServer() != null) {
							EntityPlayerMP entityplayermp = mc.getIntegratedServer()
							                                  .getPlayerList()
							                                  .getPlayerByUUID(mc.player.getUniqueID());

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

	private List<String> getDebugInfoHeader(boolean includeVersionType) {

		return Lists.newArrayList(
				getVersionLine(includeVersionType),
				mc.debug,
				mc.renderGlobal.getDebugInfoRenders(),
				mc.renderGlobal.getDebugInfoEntities(),
				"P: " + mc.effectRenderer.getStatistics() + ". T: " + mc.world.getDebugLoadedEntities(),
				mc.world.getProviderName()
		);
	}

	private String getVersionLine(boolean includeVersionType) {

		String versionType = includeVersionType && !"release".equalsIgnoreCase(mc.getVersionType()) ? "/" + mc.getVersionType() : "";
		return "Minecraft 1.12.2 (" + mc.getVersion() + "/" + ClientBrandRetriever.getClientModName() + versionType + ")";
	}

	protected List<String> getDebugInfoRight() {

		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory();
		long totalMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		long usedMemory = totalMemory - freeMemory;
		List<String> list = Lists.newArrayList(
				String.format("Java: %s %dbit", System.getProperty("java.version"), mc.isJava64bit() ? 64 : 32),
				String.format("Mem: % 2d%% %03d/%03dMB", usedMemory * 100L / maxMemory, bytesToMb(usedMemory), bytesToMb(maxMemory)),
				String.format("Allocated: % 2d%% %03dMB", totalMemory * 100L / maxMemory, bytesToMb(totalMemory)), "",
				String.format("CPU: %s", OpenGlHelper.getCpu()), "",
				String.format("Display: %dx%d (%s)", Display.getWidth(), Display.getHeight(), vendor),
				renderer, version
		);

		if (!mc.isReducedDebug()) {
			if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK && mc.objectMouseOver.getBlockPos() != null) {
				addBlockStateDebugInfo(list, mc.objectMouseOver.getBlockPos());
			}
		}

		return list;
	}

	private void addBlockStateDebugInfo(List<String> list, BlockPos blockpos) {

		IBlockState iblockstate = mc.world.getBlockState(blockpos);

		if (mc.world.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
			iblockstate = iblockstate.getActualState(mc.world, blockpos);
		}

		list.add("");
		list.add(String.valueOf(Block.REGISTRY.getNameForObject(iblockstate.getBlock())));

		for (Entry<IProperty<?>, Comparable<?>> entry : iblockstate.getProperties().entrySet()) {
			list.add(formatProperty(entry.getKey(), entry.getValue()));
		}
	}

	private void renderLagometer() {

		GlStateManager.disableDepth();
		FrameTimer frametimer = mc.getFrameTimer();
		int i = frametimer.getLastIndex();
		int j = frametimer.getIndex();
		long[] along = frametimer.getFrames();
		ScaledResolution scaledresolution = new ScaledResolution(mc);
		int scaledHeight = scaledresolution.getScaledHeight();
		int top = scaledHeight - LAGOMETER_HEIGHT;
		int right = LAGOMETER_WIDTH - 1;
		int k = i;
		int l = 0;
		drawRect(0, top, LAGOMETER_WIDTH, scaledHeight, DEBUG_BACKGROUND_COLOR);

		while (k != j) {
			int i1 = frametimer.getLagometerValue(along[k], 30);
			int j1 = getFrameColor(MathHelper.clamp(i1, 0, LAGOMETER_HEIGHT), LAGOMETER_MID_HEIGHT, LAGOMETER_HEIGHT);
			drawVerticalLine(l, scaledHeight, scaledHeight - i1, j1);
			++l;
			k = frametimer.parseIndex(k + 1);
		}

		drawLagometerLabel("60", scaledHeight - LAGOMETER_MID_HEIGHT);
		drawLagometerLabel("30", top);
		drawHorizontalLine(0, right, scaledHeight - 1, LAGOMETER_BORDER_COLOR);
		drawVerticalLine(0, top, scaledHeight, LAGOMETER_BORDER_COLOR);
		drawVerticalLine(right, top, scaledHeight, LAGOMETER_BORDER_COLOR);

		if (mc.gameSettings.limitFramerate <= 120) {
			drawHorizontalLine(0, right, top + mc.gameSettings.limitFramerate / 2, LAGOMETER_LIMIT_COLOR);
		}

		GlStateManager.enableDepth();
	}

	private void drawLagometerLabel(String text, int y) {

		drawRect(1, y + 1, 14, y + 10, DEBUG_BACKGROUND_COLOR);
		fontRenderer.drawString(text, 2, y + 2, DEBUG_TEXT_COLOR);
		drawHorizontalLine(0, LAGOMETER_WIDTH - 1, y, LAGOMETER_BORDER_COLOR);
	}

}
