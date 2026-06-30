package org.lwjgl;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.PixelFormat;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.input.Mouse.setWindowSize;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class Window {
	
	@Getter
	private static long handle = NULL;
	
	@Getter
	private static int width = 854;
	
	@Getter
	private static int height = 480;
	
	private static String title = "Game";
	private static boolean vSync = false;
	
	@Setter
	private static boolean resizable = false;
	private static boolean fullscreen = false;
	private static boolean resized = false;
	
	private static long lastFrameTime = 0L;
	
	private Window() {
	}
	
	public static void create() throws Exception {
		create(new PixelFormat());
	}
	
	public static void create(PixelFormat pixelFormat) throws Exception {
		if (!glfwInit()) throw new Exception("Failed to initialise GLFW");
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);
		
		if (pixelFormat.getDepthBits() > 0) {
			glfwWindowHint(GLFW_DEPTH_BITS, pixelFormat.getDepthBits());
		}
		if (pixelFormat.getStencilBits() > 0) {
			glfwWindowHint(GLFW_STENCIL_BITS, pixelFormat.getStencilBits());
		}
		if (pixelFormat.getSamples() > 0) {
			glfwWindowHint(GLFW_SAMPLES, pixelFormat.getSamples());
		}
		
		long monitor = fullscreen ? glfwGetPrimaryMonitor() : NULL;
		
		handle = glfwCreateWindow(width, height, title, monitor, NULL);
		if (handle == NULL) throw new Exception("Failed to create GLFW window");
		
		if (!fullscreen) {
			GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
			if (vidMode != null) glfwSetWindowPos(
					handle,
					(vidMode.width() - width) / 2,
					(vidMode.height() - height) / 2
			);
		}
		
		glfwMakeContextCurrent(handle);
		GL.createCapabilities();
		
		glfwSwapInterval(vSync ? 1 : 0);
		
		glfwSetFramebufferSizeCallback(handle, (win, w, h) -> {
			width = w;
			height = h;
			resized = true;
			glViewport(0, 0, w, h);
			setWindowSize(w, h);
		});
		
		Mouse.init(handle);
		Keyboard.init(handle);
		glfwShowWindow(handle);
		
		lastFrameTime = System.nanoTime();
	}
	
	public static void destroy() {
		if (handle != NULL) {
			org.lwjgl.input.Mouse.removeCallbacks(handle);
			org.lwjgl.input.Keyboard.removeCallbacks(handle);
			glfwDestroyWindow(handle);
			handle = NULL;
		}
		glfwTerminate();
	}
	
	public static void update() {
		resized = false;
		glfwSwapBuffers(handle);
		org.lwjgl.input.Mouse.poll();
		org.lwjgl.input.Keyboard.poll();
		glfwPollEvents();
	}
	
	public static boolean wasResized() {
		return resized;
	}
	
	public static void sync(int fps) {
		if (fps <= 0) return;
		
		long targetNanos = 1_000_000_000L / fps;
		long now = System.nanoTime();
		long elapsed = now - lastFrameTime;
		long sleepNanos = targetNanos - elapsed;
		
		if (sleepNanos > 0) {
			try {
				Thread.sleep(sleepNanos / 1_000_000L, (int) (sleepNanos % 1_000_000L));
			} catch (InterruptedException ignored) {
				Thread.currentThread().interrupt();
			}
		}
		
		lastFrameTime = System.nanoTime();
	}
	
	public static boolean isCloseRequested() {
		return handle != NULL && glfwWindowShouldClose(handle);
	}
	
	public static boolean isActive() {
		if (handle == NULL) return false;
		return glfwGetWindowAttrib(handle, GLFW_FOCUSED) == GLFW_TRUE;
	}
	
	public static boolean isVisible() {
		if (handle == NULL) return false;
		return glfwGetWindowAttrib(handle, GLFW_VISIBLE) == GLFW_TRUE;
	}
	
	public static int getX() {
		if (handle == NULL) return 0;
		int[] x = new int[1];
		int[] y = new int[1];
		glfwGetWindowPos(handle, x, y);
		return x[0];
	}
	
	public static int getY() {
		if (handle == NULL) return 0;
		int[] x = new int[1];
		int[] y = new int[1];
		glfwGetWindowPos(handle, x, y);
		return y[0];
	}
	
	public static DisplayMode getDisplayMode() {
		if (handle != NULL) {
			int[] w = new int[1], h = new int[1];
			glfwGetWindowSize(handle, w, h);
			return new DisplayMode(w[0], h[0], 32, 60);
		}
		return new DisplayMode(width, height, 32, 60);
	}
	
	public static void setDisplayMode(DisplayMode mode) {
		width = mode.width();
		height = mode.height();
		if (handle != NULL) glfwSetWindowSize(handle, width, height);
	}
	
	public static DisplayMode getDesktopDisplayMode() {
		GLFWVidMode vm = glfwGetVideoMode(glfwGetPrimaryMonitor());
		if (vm != null) {
			return new DisplayMode(vm.width(), vm.height(), vm.redBits() + vm.greenBits() + vm.blueBits(), vm.refreshRate());
		}
		return new DisplayMode(width, height, 32, 60);
	}
	
	public static void setFullscreen(boolean fs) {
		fullscreen = fs;
		if (handle == NULL) return;
		
		if (fs) {
			long monitor = glfwGetPrimaryMonitor();
			GLFWVidMode vm = glfwGetVideoMode(monitor);
			if (vm != null) glfwSetWindowMonitor(handle, monitor, 0, 0, vm.width(), vm.height(), vm.refreshRate());
		} else {
			glfwSetWindowMonitor(handle, NULL, 100, 100, width, height, GLFW_DONT_CARE);
		}
	}
	
	public static void setTitle(String title) {
		Window.title = title;
		if (handle != NULL) glfwSetWindowTitle(handle, title);
	}
	
	public static void setVSync(boolean vsync) {
		vSync = vsync;
		if (handle != NULL) glfwSwapInterval(vsync ? 1 : 0);
	}
	
	public static void setIcon(java.nio.ByteBuffer[] icons) {
		if (handle == NULL || icons == null || icons.length == 0) return;
		
		GLFWImage.Buffer buffer = GLFWImage.malloc(icons.length);
		for (int i = 0; i < icons.length; i++) {
			ByteBuffer buf = icons[i];
			buf.rewind();
			int w = buf.getInt();
			int h = buf.getInt();
			GLFWImage image = GLFWImage.malloc();
			image.set(w, h, buf.slice());
			buffer.put(i, image);
			image.free();
		}
		glfwSetWindowIcon(handle, buffer);
		buffer.free();
	}
	
}
