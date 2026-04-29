package net.minecraft.client.gui.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.util.glu.Project;

public final class GuiPanoramaBackground {

	private static final ResourceLocation[] TITLE_PANORAMA_PATHS = new ResourceLocation[]{
		new ResourceLocation("textures/gui/title/background/panorama_0.png"),
		new ResourceLocation("textures/gui/title/background/panorama_1.png"),
		new ResourceLocation("textures/gui/title/background/panorama_2.png"),
		new ResourceLocation("textures/gui/title/background/panorama_3.png"),
		new ResourceLocation("textures/gui/title/background/panorama_4.png"),
		new ResourceLocation("textures/gui/title/background/panorama_5.png")
	};
	private static final DynamicTexture VIEWPORT_TEXTURE = new DynamicTexture(256, 256);
	private static final long START_TIME = Minecraft.getSystemTime();
	private static ResourceLocation backgroundTexture;

	private GuiPanoramaBackground() {

	}

	public static void render(Minecraft mc, int width, int height) {

		ensureBackgroundTexture(mc);
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		mc.getFramebuffer().unbindFramebuffer();
		GlStateManager.viewport(0, 0, 256, 256);
		drawPanorama(mc);
		rotateAndBlurSkybox(mc, width, height);
		rotateAndBlurSkybox(mc, width, height);
		rotateAndBlurSkybox(mc, width, height);
		rotateAndBlurSkybox(mc, width, height);
		rotateAndBlurSkybox(mc, width, height);
		rotateAndBlurSkybox(mc, width, height);
		rotateAndBlurSkybox(mc, width, height);
		mc.getFramebuffer().bindFramebuffer(true);
		GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);
		float f = 120F / (float) Math.max(width, height);
		float f1 = (float) height * f / 256F;
		float f2 = (float) width * f / 256F;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.pos(0D, height, 0D).tex(0.5F - f1, 0.5F + f2).color(1F, 1F, 1F, 1F).endVertex();
		bufferbuilder.pos(width, height, 0D).tex(0.5F - f1, 0.5F - f2).color(1F, 1F, 1F, 1F).endVertex();
		bufferbuilder.pos(width, 0D, 0D).tex(0.5F + f1, 0.5F - f2).color(1F, 1F, 1F, 1F).endVertex();
		bufferbuilder.pos(0D, 0D, 0D).tex(0.5F + f1, 0.5F + f2).color(1F, 1F, 1F, 1F).endVertex();
		tessellator.draw();
	}

	private static void ensureBackgroundTexture(Minecraft mc) {

		if (backgroundTexture == null) {
			backgroundTexture = mc.getTextureManager().getDynamicTextureLocation("shared_menu_panorama", VIEWPORT_TEXTURE);
		}
	}

	private static float getPanoramaTimer() {

		return (Minecraft.getSystemTime() - START_TIME) / 50F;
	}

	private static void drawPanorama(Minecraft mc) {

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.matrixMode(5889);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		Project.gluPerspective(120F, 1F, 0.05F, 10F);
		GlStateManager.matrixMode(5888);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.rotate(180F, 1F, 0F, 0F);
		GlStateManager.rotate(90F, 0F, 0F, 1F);
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.disableCull();
		GlStateManager.depthMask(false);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		float panoramaTimer = getPanoramaTimer();

		for (int j = 0; j < 64; ++j) {
			GlStateManager.pushMatrix();
			float f = ((float) (j % 8) / 8F - 0.5F) / 64F;
			float f1 = ((float) (j / 8) / 8F - 0.5F) / 64F;
			GlStateManager.translate(f, f1, 0F);
			GlStateManager.rotate(MathHelper.sin(panoramaTimer / 400F) * 25F + 20F, 1F, 0F, 0F);
			GlStateManager.rotate(-panoramaTimer * 0.1F, 0F, 1F, 0F);

			for (int k = 0; k < 6; ++k) {
				GlStateManager.pushMatrix();

				if (k == 1) GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
				if (k == 2) GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
				if (k == 3) GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
				if (k == 4) GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
				if (k == 5) GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);

				mc.getTextureManager().bindTexture(TITLE_PANORAMA_PATHS[k]);
				bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				int l = 255 / (j + 1);
				bufferbuilder.pos(-1D, -1D, 1D).tex(0D, 0D).color(255, 255, 255, l).endVertex();
				bufferbuilder.pos(1D, -1D, 1D).tex(1D, 0D).color(255, 255, 255, l).endVertex();
				bufferbuilder.pos(1D, 1D, 1D).tex(1D, 1D).color(255, 255, 255, l).endVertex();
				bufferbuilder.pos(-1D, 1D, 1D).tex(0D, 1D).color(255, 255, 255, l).endVertex();
				tessellator.draw();
				GlStateManager.popMatrix();
			}

			GlStateManager.popMatrix();
			GlStateManager.colorMask(true, true, true, false);
		}

		bufferbuilder.setTranslation(0D, 0D, 0D);
		GlStateManager.colorMask(true, true, true, true);
		GlStateManager.matrixMode(5889);
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(5888);
		GlStateManager.popMatrix();
		GlStateManager.depthMask(true);
		GlStateManager.enableCull();
		GlStateManager.enableDepth();
	}

	private static void rotateAndBlurSkybox(Minecraft mc, int width, int height) {

		mc.getTextureManager().bindTexture(backgroundTexture);
		GlStateManager.glTexParameteri(3553, 10241, 9729);
		GlStateManager.glTexParameteri(3553, 10240, 9729);
		GlStateManager.glCopyTexSubImage2D(3553, 0, 0, 0, 0, 0, 256, 256);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.colorMask(true, true, true, false);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		GlStateManager.disableAlpha();

		for (int j = 0; j < 3; ++j) {
			float f = 1F / (float) (j + 1);
			float f1 = (float) (j - 1) / 256F;
			bufferbuilder.pos(width, height, 0D).tex(0F + f1, 1D).color(1F, 1F, 1F, f).endVertex();
			bufferbuilder.pos(width, 0D, 0D).tex(1F + f1, 1D).color(1F, 1F, 1F, f).endVertex();
			bufferbuilder.pos(0D, 0D, 0D).tex(1F + f1, 0D).color(1F, 1F, 1F, f).endVertex();
			bufferbuilder.pos(0D, height, 0D).tex(0F + f1, 0D).color(1F, 1F, 1F, f).endVertex();
		}

		tessellator.draw();
		GlStateManager.enableAlpha();
		GlStateManager.colorMask(true, true, true, true);
	}
}
