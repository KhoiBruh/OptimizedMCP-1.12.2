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
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;

public class OpenGlHelper {

	private static final Logger LOGGER = LogManager.getLogger();

	public static int GL_FRAMEBUFFER;
	public static int GL_RENDERBUFFER;
	public static int GL_COLOR_ATTACHMENT0;
	public static int GL_DEPTH_ATTACHMENT;
	public static int GL_FRAMEBUFFER_COMPLETE;
	public static int GL_FB_INCOMPLETE_ATTACHMENT;
	public static int GL_FB_INCOMPLETE_MISS_ATTACH;
	public static int GL_FB_INCOMPLETE_DRAW_BUFFER;
	public static int GL_FB_INCOMPLETE_READ_BUFFER;
	public static int GL_LINK_STATUS;
	public static int GL_COMPILE_STATUS;
	public static int GL_VERTEX_SHADER;
	public static int GL_FRAGMENT_SHADER;

	public static int defaultTexUnit;
	public static int lightmapTexUnit;

	public static int GL_TEXTURE2;
	public static int GL_COMBINE;
	public static int GL_INTERPOLATE;
	public static int GL_PRIMARY_COLOR;
	public static int GL_CONSTANT;
	public static int GL_PREVIOUS;
	public static int GL_COMBINE_RGB;
	public static int GL_SOURCE0_RGB;
	public static int GL_SOURCE1_RGB;
	public static int GL_SOURCE2_RGB;
	public static int GL_OPERAND0_RGB;
	public static int GL_OPERAND1_RGB;
	public static int GL_OPERAND2_RGB;
	public static int GL_COMBINE_ALPHA;
	public static int GL_SOURCE0_ALPHA;
	public static int GL_SOURCE1_ALPHA;
	public static int GL_SOURCE2_ALPHA;
	public static int GL_OPERAND0_ALPHA;
	public static int GL_OPERAND1_ALPHA;
	public static int GL_OPERAND2_ALPHA;
	public static boolean shadersSupported = true;
	public static int GL_ARRAY_BUFFER;
	public static int GL_STATIC_DRAW;

	private static String cpu;

	public static void initializeTextures() {
		defaultTexUnit = 33984;
		lightmapTexUnit = 33985;
		GL_TEXTURE2 = 33986;

		GL_COMBINE = 34160;
		GL_INTERPOLATE = 34165;
		GL_PRIMARY_COLOR = 34167;
		GL_CONSTANT = 34166;
		GL_PREVIOUS = 34168;
		GL_COMBINE_RGB = 34161;
		GL_SOURCE0_RGB = 34176;
		GL_SOURCE1_RGB = 34177;
		GL_SOURCE2_RGB = 34178;
		GL_OPERAND0_RGB = 34192;
		GL_OPERAND1_RGB = 34193;
		GL_OPERAND2_RGB = 34194;
		GL_COMBINE_ALPHA = 34162;
		GL_SOURCE0_ALPHA = 34184;
		GL_SOURCE1_ALPHA = 34185;
		GL_SOURCE2_ALPHA = 34186;
		GL_OPERAND0_ALPHA = 34200;
		GL_OPERAND1_ALPHA = 34201;
		GL_OPERAND2_ALPHA = 34202;

		GL_FRAMEBUFFER = 36160;
		GL_RENDERBUFFER = 36161;
		GL_COLOR_ATTACHMENT0 = 36064;
		GL_DEPTH_ATTACHMENT = 36096;
		GL_FRAMEBUFFER_COMPLETE = 36053;
		GL_FB_INCOMPLETE_ATTACHMENT = 36054;
		GL_FB_INCOMPLETE_MISS_ATTACH = 36055;
		GL_FB_INCOMPLETE_DRAW_BUFFER = 36059;
		GL_FB_INCOMPLETE_READ_BUFFER = 36060;

		GL_LINK_STATUS = 35714;
		GL_COMPILE_STATUS = 35713;
		GL_VERTEX_SHADER = 35633;
		GL_FRAGMENT_SHADER = 35632;
		GL_STATIC_DRAW = 35044;
		GL_ARRAY_BUFFER = 34962;

		CentralProcessor processor = new SystemInfo().getHardware().getProcessor();
		cpu = String.format("%dx %s", processor.getLogicalProcessorCount(), processor.getProcessorIdentifier().getName());
	}

	public static boolean areShadersSupported() {
		return shadersSupported;
	}

	public static int glGetProgrami(int program, int pname) {
		return GL20.glGetProgrami(program, pname);
	}

	public static void glAttachShader(int program, int shaderIn) {
		GL20.glAttachShader(program, shaderIn);
	}

	public static void glDeleteShader(int shaderIn) {
		GL20.glDeleteShader(shaderIn);
	}

	public static int glCreateShader(int type) {
		return GL20.glCreateShader(type);
	}

	public static void glShaderSource(int shaderIn, ByteBuffer string) {
		CharSequence source = StandardCharsets.UTF_8.decode(string);
		GL20.glShaderSource(shaderIn, source);
	}

	public static void glCompileShader(int shaderIn) {
		GL20.glCompileShader(shaderIn);
	}

	public static int glGetShaderi(int shaderIn, int pname) {
		return GL20.glGetShaderi(shaderIn, pname);
	}

	public static String glGetShaderInfoLog(int shaderIn, int maxLength) {
		return GL20.glGetShaderInfoLog(shaderIn, maxLength);
	}

	public static String glGetProgramInfoLog(int program, int maxLength) {
		return GL20.glGetProgramInfoLog(program, maxLength);
	}

	public static void glUseProgram(int program) {
		GL20.glUseProgram(program);
	}

	public static int glCreateProgram() {
		return GL20.glCreateProgram();
	}

	public static void glDeleteProgram(int program) {
		GL20.glDeleteProgram(program);
	}

	public static void glLinkProgram(int program) {
		GL20.glLinkProgram(program);
	}

	public static int glGetUniformLocation(int programObj, CharSequence name) {
		return GL20.glGetUniformLocation(programObj, name);
	}

	public static void glUniform1(int location, IntBuffer values) {
		GL20.glUniform1iv(location, values);
	}

	public static void glUniform1i(int location, int v0) {
		GL20.glUniform1i(location, v0);
	}

	public static void glUniform1(int location, FloatBuffer values) {
		GL20.glUniform1fv(location, values);
	}

	public static void glUniform2(int location, IntBuffer values) {
		GL20.glUniform2iv(location, values);
	}

	public static void glUniform2(int location, FloatBuffer values) {
		GL20.glUniform2fv(location, values);
	}

	public static void glUniform3(int location, IntBuffer values) {
		GL20.glUniform3iv(location, values);
	}

	public static void glUniform3(int location, FloatBuffer values) {
		GL20.glUniform3fv(location, values);
	}

	public static void glUniform4(int location, IntBuffer values) {
		GL20.glUniform4iv(location, values);
	}

	public static void glUniform4(int location, FloatBuffer values) {
		GL20.glUniform4fv(location, values);
	}

	public static void glUniformMatrix2(int location, boolean transpose, FloatBuffer matrices) {
		GL20.glUniformMatrix2fv(location, transpose, matrices);
	}

	public static void glUniformMatrix3(int location, boolean transpose, FloatBuffer matrices) {
		GL20.glUniformMatrix3fv(location, transpose, matrices);
	}

	public static void glUniformMatrix4(int location, boolean transpose, FloatBuffer matrices) {
		GL20.glUniformMatrix4fv(location, transpose, matrices);
	}

	public static int glGetAttribLocation(int program, CharSequence name) {
		return GL20.glGetAttribLocation(program, name);
	}

	public static int glGenBuffers() {
		return GL15.glGenBuffers();
	}

	public static void glBindBuffer(int target, int buffer) {
		GL15.glBindBuffer(target, buffer);
	}

	public static void glBufferData(int target, ByteBuffer data, int usage) {
		GL15.glBufferData(target, data, usage);
	}

	public static void glDeleteBuffers(int buffer) {
		GL15.glDeleteBuffers(buffer);
	}

	public static void glBindFramebuffer(int target, int framebufferIn) {
		GL30.glBindFramebuffer(target, framebufferIn);
	}

	public static void glBindRenderbuffer(int target, int renderbuffer) {
		GL30.glBindRenderbuffer(target, renderbuffer);
	}

	public static void glDeleteRenderbuffers(int renderbuffer) {
		GL30.glDeleteRenderbuffers(renderbuffer);
	}

	public static void glDeleteFramebuffers(int framebufferIn) {
		GL30.glDeleteFramebuffers(framebufferIn);
	}

	public static int glGenFramebuffers() {
		return GL30.glGenFramebuffers();
	}

	public static int glGenRenderbuffers() {
		return GL30.glGenRenderbuffers();
	}

	public static void glRenderbufferStorage(int target, int internalFormat, int width, int height) {
		GL30.glRenderbufferStorage(target, internalFormat, width, height);
	}

	public static void glFramebufferRenderbuffer(int target, int attachment, int renderBufferTarget, int renderBuffer) {
		GL30.glFramebufferRenderbuffer(target, attachment, renderBufferTarget, renderBuffer);
	}

	public static int glCheckFramebufferStatus(int target) {
		return GL30.glCheckFramebufferStatus(target);
	}

	public static void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
		GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
	}

	public static void setClientActiveTexture(int texture) {
		GL13.glClientActiveTexture(texture);
	}

	public static void setLightmapTextureCoords(int target, float x, float y) {
		GL13.glMultiTexCoord2f(target, x, y);
	}

	public static boolean isFramebufferEnabled() {
		return true;
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

		boolean flag = false;

		try {
			Desktop.getDesktop().browse(file.toURI());
		} catch (Throwable throwable) {
			LOGGER.error("Couldn't open link", throwable);
			flag = true;
		}

		if (flag) {
			LOGGER.info("Opening via system class!");
			try {
				Desktop.getDesktop().browse(new URI("file://" + s));
			} catch (IOException | URISyntaxException ignore) {

			}
		}
	}

}
