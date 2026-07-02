package net.minecraft.client.shader;

import com.google.common.collect.Maps;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.util.JsonException;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

public class ShaderLoader {

	private final ShaderLoader.ShaderType shaderType;
	private final String shaderFilename;
	private final int shader;
	private int shaderAttachCount;

	private ShaderLoader(ShaderLoader.ShaderType type, int shaderId, String filename) {
		shaderType = type;
		shader = shaderId;
		shaderFilename = filename;
	}

	public static ShaderLoader loadShader(IResourceManager resourceManager, ShaderLoader.ShaderType type, String filename) throws IOException {
		ShaderLoader shaderloader = type.getLoadedShaders().get(filename);

		if (shaderloader == null) {
			ResourceLocation resourcelocation = new ResourceLocation("shaders/program/" + filename + type.getShaderExtension());
			try (IResource resource = resourceManager.getResource(resourcelocation)) {
				String src = resource.getInputStream().toString();

				int i = GL20.glCreateShader(type.getShaderMode());
				GL20.glShaderSource(i, src);
				GL20.glCompileShader(i);

				if (GL20.glGetShaderi(i, GL20.GL_COMPILE_STATUS) == 0) {
					String s = GL20.glGetShaderInfoLog(i, 32768);
					if (s != null) {
						s = s.trim();
					}
					JsonException jsonexception = new JsonException("Couldn't compile " + type.getShaderName() + " program: " + s);
					jsonexception.setFilenameAndFlush(resourcelocation.getResourcePath());
					throw jsonexception;
				}

				shaderloader = new ShaderLoader(type, i, filename);
				type.getLoadedShaders().put(filename, shaderloader);
			}
		}

		return shaderloader;
	}

	public void attachShader(ShaderManager manager) {
		++shaderAttachCount;
		GL20.glAttachShader(manager.getProgram(), shader);
	}

	public void deleteShader(ShaderManager manager) {
		--shaderAttachCount;

		if (shaderAttachCount <= 0) {
			GL20.glDeleteShader(shader);
			shaderType.getLoadedShaders().remove(shaderFilename);
		}
	}

	public String getShaderFilename() {
		return shaderFilename;
	}

	public enum ShaderType {
		VERTEX("vertex", ".vsh", GL20.GL_VERTEX_SHADER),
		FRAGMENT("fragment", ".fsh", GL20.GL_FRAGMENT_SHADER);

		private final String shaderName;
		private final String shaderExtension;
		private final int shaderMode;
		private final Map<String, ShaderLoader> loadedShaders = Maps.newHashMap();

		ShaderType(String shaderNameIn, String shaderExtensionIn, int shaderModeIn) {
			shaderName = shaderNameIn;
			shaderExtension = shaderExtensionIn;
			shaderMode = shaderModeIn;
		}

		public String getShaderName() {
			return shaderName;
		}

		private String getShaderExtension() {
			return shaderExtension;
		}

		private int getShaderMode() {
			return shaderMode;
		}

		private Map<String, ShaderLoader> getLoadedShaders() {

			return loadedShaders;
		}
	}

}
