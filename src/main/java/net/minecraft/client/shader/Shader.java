package net.minecraft.client.shader;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import org.lwjgl.util.vector.Matrix4f;

import java.io.IOException;
import java.util.List;

public class Shader {

	public final Framebuffer framebufferIn;
	public final Framebuffer framebufferOut;
	private final ShaderManager manager;
	private final List<Object> listAuxFramebuffers = Lists.newArrayList();
	private final List<String> listAuxNames = Lists.newArrayList();
	private final List<Integer> listAuxWidths = Lists.newArrayList();
	private final List<Integer> listAuxHeights = Lists.newArrayList();
	private Matrix4f projectionMatrix;

	public Shader(IResourceManager resourceManager, String programName, Framebuffer framebufferInIn, Framebuffer framebufferOutIn) throws IOException {

		manager = new ShaderManager(resourceManager, programName);
		framebufferIn = framebufferInIn;
		framebufferOut = framebufferOutIn;
	}

	public void deleteShader() {

		manager.deleteShader();
	}

	public void addAuxFramebuffer(String auxName, Object auxFramebufferIn, int width, int height) {

		listAuxNames.add(listAuxNames.size(), auxName);
		listAuxFramebuffers.add(listAuxFramebuffers.size(), auxFramebufferIn);
		listAuxWidths.add(listAuxWidths.size(), width);
		listAuxHeights.add(listAuxHeights.size(), height);
	}

	private void preRender() {

		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.disableBlend();
		GlStateManager.disableDepth();
		GlStateManager.disableAlpha();
		GlStateManager.disableFog();
		GlStateManager.disableLighting();
		GlStateManager.disableColorMaterial();
		GlStateManager.enableTexture2D();
		GlStateManager.bindTexture(0);
	}

	public void setProjectionMatrix(Matrix4f projectionMatrixIn) {

		projectionMatrix = projectionMatrixIn;
	}

	public void render(float partialTicks) {

		preRender();
		framebufferIn.unbindFramebuffer();
		float f = (float) framebufferOut.framebufferTextureWidth;
		float f1 = (float) framebufferOut.framebufferTextureHeight;
		GlStateManager.viewport(0, 0, (int) f, (int) f1);
		manager.addSamplerTexture("DiffuseSampler", framebufferIn);

		for (int i = 0; i < listAuxFramebuffers.size(); ++i) {
			manager.addSamplerTexture(listAuxNames.get(i), listAuxFramebuffers.get(i));
			manager.getShaderUniformOrDefault("AuxSize" + i).set((float) listAuxWidths.get(i), (float) listAuxHeights.get(i));
		}

		manager.getShaderUniformOrDefault("ProjMat").set(projectionMatrix);
		manager.getShaderUniformOrDefault("InSize").set((float) framebufferIn.framebufferTextureWidth, (float) framebufferIn.framebufferTextureHeight);
		manager.getShaderUniformOrDefault("OutSize").set(f, f1);
		manager.getShaderUniformOrDefault("Time").set(partialTicks);
		Minecraft minecraft = Minecraft.getMinecraft();
		manager.getShaderUniformOrDefault("ScreenSize").set((float) minecraft.displayWidth, (float) minecraft.displayHeight);
		manager.useShader();
		framebufferOut.framebufferClear();
		framebufferOut.bindFramebuffer(false);
		GlStateManager.depthMask(false);
		GlStateManager.colorMask(true, true, true, true);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
		bufferbuilder.pos(0D, f1, 500D).color(255, 255, 255, 255).endVertex();
		bufferbuilder.pos(f, f1, 500D).color(255, 255, 255, 255).endVertex();
		bufferbuilder.pos(f, 0D, 500D).color(255, 255, 255, 255).endVertex();
		bufferbuilder.pos(0D, 0D, 500D).color(255, 255, 255, 255).endVertex();
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.colorMask(true, true, true, true);
		manager.endShader();
		framebufferOut.unbindFramebuffer();
		framebufferIn.unbindFramebufferTexture();

		for (Object object : listAuxFramebuffers) {
			if (object instanceof Framebuffer) {
				((Framebuffer) object).unbindFramebufferTexture();
			}
		}
	}

	public ShaderManager getShaderManager() {

		return manager;
	}

}
