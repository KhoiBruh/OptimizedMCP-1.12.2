package net.minecraft.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MinecraftError;

public class LoadingScreenRenderer implements IProgressUpdate {

	/**
	 * A reference to the Minecraft object.
	 */
	private final Minecraft mc;
	private final ScaledResolution scaledResolution;
	private final Framebuffer framebuffer;
	private String message = "";
	/**
	 * The text currently displayed (i.e. the argument to the last call to printText or displayString)
	 */
	private String currentlyDisplayedText = "";
	/**
	 * The system's time represented in milliseconds.
	 */
	private long systemTime = Minecraft.getSystemTime();
	/**
	 * True if the loading ended with a success
	 */
	private boolean loadingSuccess;

	public LoadingScreenRenderer(Minecraft mcIn) {

		mc = mcIn;
		scaledResolution = new ScaledResolution(mcIn);
		framebuffer = new Framebuffer(mcIn.displayWidth, mcIn.displayHeight, false);
		framebuffer.setFramebufferFilter(9728);
	}

	/**
	 * this string, followed by "working..." and then the "% complete" are the 3 lines shown. This resets progress to 0,
	 * and the WorkingString to "working...".
	 */
	public void resetProgressAndMessage(String message) {

		loadingSuccess = false;
		displayString(message);
	}

	/**
	 * Shows the 'Saving level' string.
	 */
	public void displaySavingString(String message) {

		loadingSuccess = true;
		displayString(message);
	}

	private void displayString(String message) {

		currentlyDisplayedText = message;

		if (!mc.running) {
			if (!loadingSuccess) {
				throw new MinecraftError();
			}
		} else {
			GlStateManager.clear(256);
			GlStateManager.matrixMode(5889);
			GlStateManager.loadIdentity();

			if (OpenGlHelper.isFramebufferEnabled()) {
				int i = scaledResolution.getScaleFactor();
				GlStateManager.ortho(0.0D, scaledResolution.getScaledWidth() * i, scaledResolution.getScaledHeight() * i, 0.0D, 100.0D, 300.0D);
			} else {
				ScaledResolution scaledresolution = new ScaledResolution(mc);
				GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 100.0D, 300.0D);
			}

			GlStateManager.matrixMode(5888);
			GlStateManager.loadIdentity();
			GlStateManager.translate(0.0F, 0.0F, -200.0F);
		}
	}

	/**
	 * Displays a string on the loading screen supposed to indicate what is being done currently.
	 */
	public void displayLoadingString(String message) {

		if (!mc.running) {
			if (!loadingSuccess) {
				throw new MinecraftError();
			}
		} else {
			systemTime = 0L;
			this.message = message;
			setLoadingProgress(-1);
			systemTime = 0L;
		}
	}

	/**
	 * Updates the progress bar on the loading screen to the specified amount.
	 */
	public void setLoadingProgress(int progress) {

		if (!mc.running) {
			if (!loadingSuccess) {
				throw new MinecraftError();
			}
		} else {
			long i = Minecraft.getSystemTime();

			if (i - systemTime >= 100L) {
				systemTime = i;
				ScaledResolution scaledresolution = new ScaledResolution(mc);
				int j = scaledresolution.getScaleFactor();
				int k = scaledresolution.getScaledWidth();
				int l = scaledresolution.getScaledHeight();

				if (OpenGlHelper.isFramebufferEnabled()) {
					framebuffer.framebufferClear();
				} else {
					GlStateManager.clear(256);
				}

				framebuffer.bindFramebuffer(false);
				GlStateManager.matrixMode(5889);
				GlStateManager.loadIdentity();
				GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 100.0D, 300.0D);
				GlStateManager.matrixMode(5888);
				GlStateManager.loadIdentity();
				GlStateManager.translate(0.0F, 0.0F, -200.0F);

				if (!OpenGlHelper.isFramebufferEnabled()) {
					GlStateManager.clear(16640);
				}

				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuffer();
				mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
				float f = 32.0F;
				bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				bufferbuilder.pos(0.0D, l, 0.0D).tex(0.0D, (float) l / 32.0F).color(64, 64, 64, 255).endVertex();
				bufferbuilder.pos(k, l, 0.0D).tex((float) k / 32.0F, (float) l / 32.0F).color(64, 64, 64, 255).endVertex();
				bufferbuilder.pos(k, 0.0D, 0.0D).tex((float) k / 32.0F, 0.0D).color(64, 64, 64, 255).endVertex();
				bufferbuilder.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.0D).color(64, 64, 64, 255).endVertex();
				tessellator.draw();

				if (progress >= 0) {
					int i1 = 100;
					int j1 = 2;
					int k1 = k / 2 - 50;
					int l1 = l / 2 + 16;
					GlStateManager.disableTexture2D();
					bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
					bufferbuilder.pos(k1, l1, 0.0D).color(128, 128, 128, 255).endVertex();
					bufferbuilder.pos(k1, l1 + 2, 0.0D).color(128, 128, 128, 255).endVertex();
					bufferbuilder.pos(k1 + 100, l1 + 2, 0.0D).color(128, 128, 128, 255).endVertex();
					bufferbuilder.pos(k1 + 100, l1, 0.0D).color(128, 128, 128, 255).endVertex();
					bufferbuilder.pos(k1, l1, 0.0D).color(128, 255, 128, 255).endVertex();
					bufferbuilder.pos(k1, l1 + 2, 0.0D).color(128, 255, 128, 255).endVertex();
					bufferbuilder.pos(k1 + progress, l1 + 2, 0.0D).color(128, 255, 128, 255).endVertex();
					bufferbuilder.pos(k1 + progress, l1, 0.0D).color(128, 255, 128, 255).endVertex();
					tessellator.draw();
					GlStateManager.enableTexture2D();
				}

				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				mc.fontRenderer.drawStringWithShadow(currentlyDisplayedText, (float) ((k - mc.fontRenderer.getStringWidth(currentlyDisplayedText)) / 2), (float) (l / 2 - 4 - 16), 16777215);
				mc.fontRenderer.drawStringWithShadow(message, (float) ((k - mc.fontRenderer.getStringWidth(message)) / 2), (float) (l / 2 - 4 + 8), 16777215);
				framebuffer.unbindFramebuffer();

				if (OpenGlHelper.isFramebufferEnabled()) {
					framebuffer.framebufferRender(k * j, l * j);
				}

				mc.updateDisplay();

				try {
					Thread.yield();
				} catch (Exception var15) {
				}
			}
		}
	}

	public void setDoneWorking() {

	}

}
