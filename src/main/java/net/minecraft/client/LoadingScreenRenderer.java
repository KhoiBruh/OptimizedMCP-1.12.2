package net.minecraft.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLS;
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
		framebuffer = new Framebuffer(mcIn.getWindow().getWidth(), mcIn.getWindow().getHeight(), false);
		framebuffer.setFilter(9728);
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
	public void savingMessage(String message) {
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
			GLS.clear(256);
			GLS.matrixMode(5889);
			GLS.loadIdentity();

			int factor = mc.getWindow().getGuiScale();
			GLS.ortho(0D, mc.getWindow().getScaledWidth() * factor, mc.getWindow().getScaledHeight() * factor, 0D, 100D, 300D);

			GLS.matrixMode(5888);
			GLS.loadIdentity();
			GLS.translate(0F, 0F, -200F);
		}
	}

	/**
	 * Displays a string on the loading screen supposed to indicate what is being done currently.
	 */
	public void loadingMessage(String message) {
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
				int j = mc.getWindow().getGuiScale();
				int k = mc.getWindow().getScaledWidth();
				int l = mc.getWindow().getScaledHeight();

				framebuffer.clear();
				framebuffer.bind(false);

				GLS.matrixMode(5889);
				GLS.loadIdentity();
				GLS.ortho(0D, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(), 0D, 100D, 300D);
				GLS.matrixMode(5888);
				GLS.loadIdentity();
				GLS.translate(0F, 0F, -200F);

				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuffer();
				mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
				float f = 32F;
				bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				bufferbuilder.pos(0D, l, 0D).tex(0D, (float) l / 32F).color(64, 64, 64, 255).endVertex();
				bufferbuilder.pos(k, l, 0D).tex((float) k / 32F, (float) l / 32F).color(64, 64, 64, 255).endVertex();
				bufferbuilder.pos(k, 0D, 0D).tex((float) k / 32F, 0D).color(64, 64, 64, 255).endVertex();
				bufferbuilder.pos(0D, 0D, 0D).tex(0D, 0D).color(64, 64, 64, 255).endVertex();
				tessellator.draw();

				if (progress >= 0) {
					int i1 = 100;
					int j1 = 2;
					int k1 = k / 2 - 50;
					int l1 = l / 2 + 16;
					GLS.disableTexture2D();
					bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
					bufferbuilder.pos(k1, l1, 0D).color(128, 128, 128, 255).endVertex();
					bufferbuilder.pos(k1, l1 + 2, 0D).color(128, 128, 128, 255).endVertex();
					bufferbuilder.pos(k1 + 100, l1 + 2, 0D).color(128, 128, 128, 255).endVertex();
					bufferbuilder.pos(k1 + 100, l1, 0D).color(128, 128, 128, 255).endVertex();
					bufferbuilder.pos(k1, l1, 0D).color(128, 255, 128, 255).endVertex();
					bufferbuilder.pos(k1, l1 + 2, 0D).color(128, 255, 128, 255).endVertex();
					bufferbuilder.pos(k1 + progress, l1 + 2, 0D).color(128, 255, 128, 255).endVertex();
					bufferbuilder.pos(k1 + progress, l1, 0D).color(128, 255, 128, 255).endVertex();
					tessellator.draw();
					GLS.enableTexture2D();
				}

				GLS.enableBlend();
				GLS.blendFunc(GLS.SourceFactor.SRC_ALPHA, GLS.DestFactor.ONE_MINUS_SRC_ALPHA, GLS.SourceFactor.ONE, GLS.DestFactor.ZERO);
				mc.fontRenderer.drawShadowText(currentlyDisplayedText, (float) ((k - mc.fontRenderer.getWidth(currentlyDisplayedText)) / 2), (float) (l / 2 - 4 - 16), 16777215);
				mc.fontRenderer.drawShadowText(message, (float) ((k - mc.fontRenderer.getWidth(message)) / 2), (float) (l / 2 - 4 + 8), 16777215);

				framebuffer.unbind();
				framebuffer.render(k * j, l * j);

				mc.updateDisplay();

				try {
					Thread.yield();
				} catch (Exception ignored) {
				}
			}
		}
	}

	public void setDoneWorking() {
	}

}
