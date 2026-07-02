package net.minecraft.client.renderer;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.*;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class OpenGlHelper {

	private static final Logger LOGGER = LogManager.getLogger();

	public static int defaultTexUnit = 33984;
	public static int lightmapTexUnit = 33985;

	private static String cpu;

	public static void ini() {
		CentralProcessor processor = new SystemInfo().getHardware().getProcessor();
		cpu = String.format("%dx %s", processor.getLogicalProcessorCount(), processor.getProcessorIdentifier().getName());
	}

	public static String getCpu() {
		return cpu == null ? "unknown" : cpu;
	}

	public static void renderDirections(int p_188785_0_) {
		GLS.disableTexture2D();
		GLS.depthMask(false);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GL11.glLineWidth(4F);
		bufferbuilder.begin(1, DefaultVertexFormats.POSITION_COLOR);
		bufferbuilder.pos(0D, 0D, 0D).color(0, 0, 0, 255).endVertex();
		bufferbuilder.pos(p_188785_0_, 0D, 0D).color(0, 0, 0, 255).endVertex();
		bufferbuilder.pos(0D, 0D, 0D).color(0, 0, 0, 255).endVertex();
		bufferbuilder.pos(0D, p_188785_0_, 0D).color(0, 0, 0, 255).endVertex();
		bufferbuilder.pos(0D, 0D, 0D).color(0, 0, 0, 255).endVertex();
		bufferbuilder.pos(0D, 0D, p_188785_0_).color(0, 0, 0, 255).endVertex();
		tessellator.draw();
		GL11.glLineWidth(2F);
		bufferbuilder.begin(1, DefaultVertexFormats.POSITION_COLOR);
		bufferbuilder.pos(0D, 0D, 0D).color(255, 0, 0, 255).endVertex();
		bufferbuilder.pos(p_188785_0_, 0D, 0D).color(255, 0, 0, 255).endVertex();
		bufferbuilder.pos(0D, 0D, 0D).color(0, 255, 0, 255).endVertex();
		bufferbuilder.pos(0D, p_188785_0_, 0D).color(0, 255, 0, 255).endVertex();
		bufferbuilder.pos(0D, 0D, 0D).color(127, 127, 255, 255).endVertex();
		bufferbuilder.pos(0D, 0D, p_188785_0_).color(127, 127, 255, 255).endVertex();
		tessellator.draw();
		GL11.glLineWidth(1F);
		GLS.depthMask(true);
		GLS.enableTexture2D();
	}

	public static void openFile(File file) {
		String s = file.getAbsolutePath();

		if (Util.getOSType() == Util.OS.OSX) {
			try {
				LOGGER.info(s);
				Runtime.getRuntime().exec(new String[]{"/usr/bin/open", s});
				return;
			} catch (IOException ioexception1) {
				LOGGER.error("Couldn't open file", ioexception1);
			}
		} else if (Util.getOSType() == Util.OS.WINDOWS) {
			String[] cmd = {"cmd.exe", "/C", "start", "Open file", s};

			try {
				Runtime.getRuntime().exec(cmd);
				return;
			} catch (IOException ioexception) {
				LOGGER.error("Couldn't open file", ioexception);
			}
		}

		boolean fail = false;

		try {
			Desktop.getDesktop().browse(file.toURI());
		} catch (Throwable throwable) {
			LOGGER.error("Couldn't open link", throwable);
			fail = true;
		}

		if (fail) {
			LOGGER.info("Opening via system class!");
			try {
				Desktop.getDesktop().browse(new URI("file://" + s));
			} catch (IOException | URISyntaxException ignore) {

			}
		}
	}

}
