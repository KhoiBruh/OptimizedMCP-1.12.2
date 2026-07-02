package net.minecraft.client.renderer;

import net.minecraft.client.Minecraft;
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
import java.util.Locale;

public class OpenGlHelper {

	/**
	 * The logger used by {@link OpenGlHelper} in the event of an error
	 */
	private static final Logger LOGGER = LogManager.getLogger();
	public static boolean nvidia;
	public static int GL_FRAMEBUFFER;
	public static int GL_RENDERBUFFER;
	public static int GL_COLOR_ATTACHMENT0;
	public static int GL_DEPTH_ATTACHMENT;
	public static int GL_FRAMEBUFFER_COMPLETE;
	public static int GL_FB_INCOMPLETE_ATTACHMENT;
	public static int GL_FB_INCOMPLETE_MISS_ATTACH;
	public static int GL_FB_INCOMPLETE_DRAW_BUFFER;
	public static int GL_FB_INCOMPLETE_READ_BUFFER;
	public static boolean framebufferSupported;
	public static int GL_LINK_STATUS;
	public static int GL_COMPILE_STATUS;
	public static int GL_VERTEX_SHADER;
	public static int GL_FRAGMENT_SHADER;
	/**
	 * An OpenGL constant corresponding to GL_TEXTURE0, used when setting data pertaining to auxiliary OpenGL texture
	 * units.
	 */
	public static int defaultTexUnit;
	/**
	 * An OpenGL constant corresponding to GL_TEXTURE1, used when setting data pertaining to auxiliary OpenGL texture
	 * units.
	 */
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
	public static boolean extBlendFuncSeparate;
	public static boolean openGL21;
	public static boolean shadersSupported;
	public static int GL_ARRAY_BUFFER;
	public static int GL_STATIC_DRAW;
	private static OpenGlHelper.FboMode framebufferType;
	private static boolean openGL14;
	private static String logText = "";
	private static String cpu;

	/**
	 * Initializes the texture constants to be used when rendering lightmap values
	 */
	public static void initializeTextures() {
		GLCapabilities caps = GL.getCapabilities();
		logText = logText + "Using GL 1.3 multitexturing.\n";
		defaultTexUnit = 33984;
		lightmapTexUnit = 33985;
		GL_TEXTURE2 = 33986;

		logText = logText + "Using GL 1.3 texture combiners.\n";
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

		extBlendFuncSeparate = caps.GL_EXT_blend_func_separate && !caps.OpenGL14;
		openGL14 = caps.OpenGL14 || caps.GL_EXT_blend_func_separate;
		framebufferSupported = openGL14 && (caps.GL_EXT_framebuffer_object || caps.OpenGL30);

		if (framebufferSupported) {
			logText = logText + "Using framebuffer objects because ";

			if (caps.OpenGL30) {
				logText = logText + "OpenGL 3.0 is supported and separate blending is supported.\n";
				framebufferType = OpenGlHelper.FboMode.BASE;
				GL_FRAMEBUFFER = 36160;
				GL_RENDERBUFFER = 36161;
				GL_COLOR_ATTACHMENT0 = 36064;
				GL_DEPTH_ATTACHMENT = 36096;
				GL_FRAMEBUFFER_COMPLETE = 36053;
				GL_FB_INCOMPLETE_ATTACHMENT = 36054;
				GL_FB_INCOMPLETE_MISS_ATTACH = 36055;
			} else {
				logText = logText + "EXT_framebuffer_object is supported.\n";
				framebufferType = FboMode.EXT;
				GL_FRAMEBUFFER = 36160;
				GL_RENDERBUFFER = 36161;
				GL_COLOR_ATTACHMENT0 = 36064;
				GL_DEPTH_ATTACHMENT = 36096;
				GL_FRAMEBUFFER_COMPLETE = 36053;
				GL_FB_INCOMPLETE_MISS_ATTACH = 36055;
				GL_FB_INCOMPLETE_ATTACHMENT = 36054;
			}

			GL_FB_INCOMPLETE_DRAW_BUFFER = 36059;
			GL_FB_INCOMPLETE_READ_BUFFER = 36060;
		} else {
			logText = logText + "Not using framebuffer objects because ";
			logText = logText + "OpenGL 1.4 is " + (caps.OpenGL14 ? "" : "not ") + "supported, ";
			logText = logText + "EXT_blend_func_separate is " + (caps.GL_EXT_blend_func_separate ? "" : "not ") + "supported, and ";
			logText = logText + "OpenGL 3.0 is " + (caps.OpenGL30 ? "" : "not ") + "supported.\n";
		}

		openGL21 = caps.OpenGL21;
		boolean shadersAvailable = openGL21;
		logText = logText + "Shaders are " + (shadersAvailable ? "" : "not ") + "available because ";

		if (shadersAvailable) {
			logText = logText + "OpenGL 2.1 is supported.\n";
			GL_LINK_STATUS = 35714;
			GL_COMPILE_STATUS = 35713;
			GL_VERTEX_SHADER = 35633;
			GL_FRAGMENT_SHADER = 35632;
		} else {
			logText = logText + "OpenGL 2.1 is not supported.\n";
		}

		shadersSupported = framebufferSupported && shadersAvailable;
		String s = GL11.glGetString(GL11.GL_VENDOR).toLowerCase(Locale.ROOT);
		nvidia = s.contains("nvidia");
		logText = logText + "VBOs are available because ";
		logText = logText + "OpenGL 1.5 is supported.\n";

		GL_STATIC_DRAW = 35044;
		GL_ARRAY_BUFFER = 34962;

		CentralProcessor processor = new SystemInfo().getHardware().getProcessor();
		cpu = String.format("%dx %s", processor.getLogicalProcessorCount(), processor.getProcessorIdentifier()
		                                                                             .getName());
	}

	public static boolean areShadersSupported() {
		return shadersSupported;
	}

	public static String getLogText() {
		return logText;
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

	/**
	 * creates a shader with the given mode and returns the GL id. params: mode
	 */
	public static int glCreateShader(int type) {
		return GL20.glCreateShader(type);
	}

	public static void glShaderSource(int shaderIn, ByteBuffer string) {
		CharSequence source = java.nio.charset.StandardCharsets.UTF_8.decode(string);
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
		if (framebufferSupported) {
			switch (framebufferType) {
				case BASE:
					GL30.glBindFramebuffer(target, framebufferIn);
					break;

				case EXT:
					EXTFramebufferObject.glBindFramebufferEXT(target, framebufferIn);
			}
		}
	}

	public static void glBindRenderbuffer(int target, int renderbuffer) {
		if (framebufferSupported) {
			switch (framebufferType) {
				case BASE:
					GL30.glBindRenderbuffer(target, renderbuffer);
					break;

				case EXT:
					EXTFramebufferObject.glBindRenderbufferEXT(target, renderbuffer);
			}
		}
	}

	public static void glDeleteRenderbuffers(int renderbuffer) {
		if (framebufferSupported) {
			switch (framebufferType) {
				case BASE:
					GL30.glDeleteRenderbuffers(renderbuffer);
					break;

				case EXT:
					EXTFramebufferObject.glDeleteRenderbuffersEXT(renderbuffer);
			}
		}
	}

	public static void glDeleteFramebuffers(int framebufferIn) {
		if (framebufferSupported) {
			switch (framebufferType) {
				case BASE:
					GL30.glDeleteFramebuffers(framebufferIn);
					break;

				case EXT:
					EXTFramebufferObject.glDeleteFramebuffersEXT(framebufferIn);
			}
		}
	}

	/**
	 * Calls the appropriate glGenFramebuffers method and returns the newly created fbo, or returns -1 if not supported.
	 */
	public static int glGenFramebuffers() {
		if (!framebufferSupported) {
			return -1;
		} else {
			return switch (framebufferType) {
				case BASE -> GL30.glGenFramebuffers();
				case EXT -> EXTFramebufferObject.glGenFramebuffersEXT();
			};
		}
	}

	public static int glGenRenderbuffers() {
		if (!framebufferSupported) {
			return -1;
		} else {
			return switch (framebufferType) {
				case BASE -> GL30.glGenRenderbuffers();
				case EXT -> EXTFramebufferObject.glGenRenderbuffersEXT();
			};
		}
	}

	public static void glRenderbufferStorage(int target, int internalFormat, int width, int height) {
		if (framebufferSupported) {
			switch (framebufferType) {
				case BASE:
					GL30.glRenderbufferStorage(target, internalFormat, width, height);
					break;

				case EXT:
					EXTFramebufferObject.glRenderbufferStorageEXT(target, internalFormat, width, height);
			}
		}
	}

	public static void glFramebufferRenderbuffer(int target, int attachment, int renderBufferTarget, int renderBuffer) {
		if (framebufferSupported) {
			switch (framebufferType) {
				case BASE:
					GL30.glFramebufferRenderbuffer(target, attachment, renderBufferTarget, renderBuffer);
					break;

				case EXT:
					EXTFramebufferObject.glFramebufferRenderbufferEXT(target, attachment, renderBufferTarget, renderBuffer);
			}
		}
	}

	public static int glCheckFramebufferStatus(int target) {
		if (!framebufferSupported) {
			return -1;
		} else {
			return switch (framebufferType) {
				case BASE -> GL30.glCheckFramebufferStatus(target);
				case EXT -> EXTFramebufferObject.glCheckFramebufferStatusEXT(target);
			};
		}
	}

	public static void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
		if (framebufferSupported) {
			switch (framebufferType) {
				case BASE:
					GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
					break;

				case EXT:
					EXTFramebufferObject.glFramebufferTexture2DEXT(target, attachment, textarget, texture, level);
			}
		}
	}

	/**
	 * Sets the current lightmap texture to the specified OpenGL constant
	 */
	public static void setActiveTexture(int texture) {
		GL13.glActiveTexture(texture);
	}

	/**
	 * Sets the current lightmap texture to the specified OpenGL constant
	 */
	public static void setClientActiveTexture(int texture) {
		GL13.glClientActiveTexture(texture);
	}

	/**
	 * Sets the current coordinates of the given lightmap texture
	 */
	public static void setLightmapTextureCoords(int target, float x, float y) {
		GL13.glMultiTexCoord2f(target, x, y);
	}

	public static void glBlendFunc(int sFactorRGB, int dFactorRGB, int sfactorAlpha, int dfactorAlpha) {
		if (openGL14) {
			if (extBlendFuncSeparate) {
				EXTBlendFuncSeparate.glBlendFuncSeparateEXT(sFactorRGB, dFactorRGB, sfactorAlpha, dfactorAlpha);
			} else {
				GL14.glBlendFuncSeparate(sFactorRGB, dFactorRGB, sfactorAlpha, dfactorAlpha);
			}
		} else {
			GL11.glBlendFunc(sFactorRGB, dFactorRGB);
		}
	}

	public static boolean isFramebufferEnabled() {
		return framebufferSupported && Minecraft.getMinecraft().gameSettings.fboEnable;
	}

	public static String getCpu() {
		return cpu == null ? "<unknown>" : cpu;
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

	public static void openFile(File fileIn) {
		String s = fileIn.getAbsolutePath();

		if (Util.getOSType() == Util.OS.OSX) {
			try {
				LOGGER.info(s);
				Runtime.getRuntime().exec(new String[]{"/usr/bin/open", s});
				return;
			} catch (IOException ioexception1) {
				LOGGER.error("Couldn't open file", ioexception1);
			}
		} else if (Util.getOSType() == Util.OS.WINDOWS) {
			String[] cmd = new String[]{"cmd.exe", "/C", "start", "Open file", s};

			try {
				Runtime.getRuntime().exec(cmd);
				return;
			} catch (IOException ioexception) {
				LOGGER.error("Couldn't open file", ioexception);
			}
		}

		boolean flag = false;

		try {
			Desktop.getDesktop().browse(fileIn.toURI());
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

	enum FboMode {
		BASE,
		EXT
	}

}
