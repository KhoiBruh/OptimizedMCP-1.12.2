package net.minecraft.client.renderer.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;

public class DebugRenderer {

	public final DebugRenderer.IDebugRenderer pathfinding;
	public final DebugRenderer.IDebugRenderer water;
	public final DebugRenderer.IDebugRenderer chunkBorder;
	public final DebugRenderer.IDebugRenderer heightMap;
	public final DebugRenderer.IDebugRenderer collisionBox;
	public final DebugRenderer.IDebugRenderer neighborsUpdate;
	public final DebugRenderer.IDebugRenderer solidFace;
	private boolean chunkBorderEnabled;
	private boolean pathfindingEnabled;
	private boolean waterEnabled;
	private boolean heightMapEnabled;
	private boolean collisionBoxEnabled;
	private boolean neighborsUpdateEnabled;
	private boolean solidFaceEnabled;

	public DebugRenderer(Minecraft clientIn) {

		pathfinding = new DebugRendererPathfinding(clientIn);
		water = new DebugRendererWater(clientIn);
		chunkBorder = new DebugRendererChunkBorder(clientIn);
		heightMap = new DebugRendererHeightMap(clientIn);
		collisionBox = new DebugRendererCollisionBox(clientIn);
		neighborsUpdate = new DebugRendererNeighborsUpdate(clientIn);
		solidFace = new DebugRendererSolidFace(clientIn);
	}

	public static void renderDebugText(String str, int x, int y, int z, float partialTicks, int color) {

		renderDebugText(str, (double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, partialTicks, color);
	}

	public static void renderDebugText(String str, double x, double y, double z, float partialTicks, int color) {

		Minecraft minecraft = Minecraft.getMinecraft();

		if (minecraft.player != null && minecraft.getRenderManager() != null && minecraft.getRenderManager().options != null) {
			FontRenderer fontrenderer = minecraft.fontRenderer;
			EntityPlayer entityplayer = minecraft.player;
			double d0 = entityplayer.lastTickPosX + (entityplayer.posX - entityplayer.lastTickPosX) * (double) partialTicks;
			double d1 = entityplayer.lastTickPosY + (entityplayer.posY - entityplayer.lastTickPosY) * (double) partialTicks;
			double d2 = entityplayer.lastTickPosZ + (entityplayer.posZ - entityplayer.lastTickPosZ) * (double) partialTicks;
			GlStateManager.pushMatrix();
			GlStateManager.translate((float) (x - d0), (float) (y - d1) + 0.07F, (float) (z - d2));
			GlStateManager.glNormal3f(0F, 1F, 0F);
			GlStateManager.scale(0.02F, -0.02F, 0.02F);
			RenderManager rendermanager = minecraft.getRenderManager();
			GlStateManager.rotate(-rendermanager.playerViewY, 0F, 1F, 0F);
			GlStateManager.rotate((float) (rendermanager.options.thirdPersonView == 2 ? 1 : -1) * rendermanager.playerViewX, 1F, 0F, 0F);
			GlStateManager.disableLighting();
			GlStateManager.enableTexture2D();
			GlStateManager.enableDepth();
			GlStateManager.depthMask(true);
			GlStateManager.scale(-1F, 1F, 1F);
			fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, 0, color);
			GlStateManager.enableLighting();
			GlStateManager.color(1F, 1F, 1F, 1F);
			GlStateManager.popMatrix();
		}
	}

	public boolean shouldRender() {

		return chunkBorderEnabled || pathfindingEnabled || waterEnabled || heightMapEnabled || collisionBoxEnabled || neighborsUpdateEnabled || solidFaceEnabled;
	}

	/**
	 * Toggles the debug screen's visibility.
	 */
	public boolean toggleChunkBorders() {

		chunkBorderEnabled = !chunkBorderEnabled;
		return chunkBorderEnabled;
	}

	public void renderDebug(float partialTicks, long finishTimeNano) {

		if (pathfindingEnabled) {
			pathfinding.render(partialTicks, finishTimeNano);
		}

		if (chunkBorderEnabled && !Minecraft.getMinecraft().isReducedDebug()) {
			chunkBorder.render(partialTicks, finishTimeNano);
		}

		if (waterEnabled) {
			water.render(partialTicks, finishTimeNano);
		}

		if (heightMapEnabled) {
			heightMap.render(partialTicks, finishTimeNano);
		}

		if (collisionBoxEnabled) {
			collisionBox.render(partialTicks, finishTimeNano);
		}

		if (neighborsUpdateEnabled) {
			neighborsUpdate.render(partialTicks, finishTimeNano);
		}

		if (solidFaceEnabled) {
			solidFace.render(partialTicks, finishTimeNano);
		}
	}

	public interface IDebugRenderer {

		void render(float partialTicks, long finishTimeNano);

	}

}
