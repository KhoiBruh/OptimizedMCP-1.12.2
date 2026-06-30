package net.minecraft.client.util;

import lombok.Getter;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class Window {

    @Getter
    private long handle = NULL;

    @Getter
    private int width;

    @Getter
    private int height;
	
	@Getter
	private int scaledWidth;
	
	@Getter
	private int scaledHeight;
	
	@Getter
	private int guiScale;
	
	@Getter
	private boolean fullscreen;
	
	@Getter
	private boolean resized = false;

    private String title;

    private long lastFrameTime = 0L;

    public Window(String title, int width, int height, boolean fullscreen) {
        this.title = title;
        this.width = Math.max(width, 1);
        this.height = Math.max(height, 1);
        this.fullscreen = fullscreen;
    }
	
    public void setGuiScale(int setting, boolean unicode) {
        int scale = 1;
        int maxScale = setting == 0 ? 100 : setting;

        while (scale < maxScale && width >= (scale + 1) * 320 && height >= (scale + 1) * 240) scale++;

        if (unicode && scale > 1 && (scale & 1) == 1) scale--;

        guiScale = scale;
        scaledWidth = width / scale;
        scaledHeight = height / scale;
    }
	
	public void create(int depthBits) throws Exception {
        if (!glfwInit()) throw new Exception("Failed to initialise GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        if (depthBits > 0) glfwWindowHint(GLFW_DEPTH_BITS, depthBits);

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

        glfwSetFramebufferSizeCallback(handle, (win, w, h) -> {
            width = w;
            height = h;
            resized = true;
            glViewport(0, 0, w, h);
            Mouse.setWindowSize(w, h);
        });

        Mouse.init(handle);
        Keyboard.init(handle);
        glfwShowWindow(handle);

        lastFrameTime = System.nanoTime();
    }

    public void destroy() {
        if (handle != NULL) {
            Mouse.removeCallbacks(handle);
            Keyboard.removeCallbacks(handle);
            glfwDestroyWindow(handle);
            handle = NULL;
        }
        glfwTerminate();
    }

    public void update() {
        resized = false;
        glfwSwapBuffers(handle);
        Mouse.poll();
        Keyboard.poll();
        glfwPollEvents();
    }
	
	public void sync(int fps) {
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

    public boolean isCloseRequested() {
        return handle != NULL && glfwWindowShouldClose(handle);
    }

    public boolean isActive() {
        if (handle == NULL) return false;
        return glfwGetWindowAttrib(handle, GLFW_FOCUSED) == GLFW_TRUE;
    }

    public boolean isVisible() {
        if (handle == NULL) return false;
        return glfwGetWindowAttrib(handle, GLFW_VISIBLE) == GLFW_TRUE;
    }
	
	public int getX() {
        if (handle == NULL) return 0;
        int[] x = new int[1];
        int[] y = new int[1];
        glfwGetWindowPos(handle, x, y);
        return x[0];
    }

    public int getY() {
        if (handle == NULL) return 0;
        int[] x = new int[1];
        int[] y = new int[1];
        glfwGetWindowPos(handle, x, y);
        return y[0];
    }

    public DisplayMode getDisplayMode() {
        if (handle != NULL) {
            int[] w = new int[1], h = new int[1];
            glfwGetWindowSize(handle, w, h);
            return new DisplayMode(w[0], h[0], 32, 60);
        }
        return new DisplayMode(width, height, 32, 60);
    }

    public void setDisplayMode(DisplayMode mode) {
        width = mode.width();
        height = mode.height();
        if (handle != NULL) glfwSetWindowSize(handle, width, height);
    }

    public void setWindowedSize(int width, int height) {
        this.width = Math.max(width, 1);
        this.height = Math.max(height, 1);
    }

    public DisplayMode getDesktopDisplayMode() {
        GLFWVidMode vm = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vm != null) return new DisplayMode(vm.width(), vm.height(), vm.redBits() + vm.greenBits() + vm.blueBits(), vm.refreshRate());
        return new DisplayMode(width, height, 32, 60);
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        if (handle == NULL) return;

        if (fullscreen) {
            long monitor = glfwGetPrimaryMonitor();
            GLFWVidMode vm = glfwGetVideoMode(monitor);
            if (vm != null) glfwSetWindowMonitor(handle, monitor, 0, 0, vm.width(), vm.height(), vm.refreshRate());
        } else {
            glfwSetWindowMonitor(handle, NULL, 100, 100, width, height, GLFW_DONT_CARE);
        }
    }

    public void setTitle(String title) {
        this.title = title;
        if (handle != NULL) glfwSetWindowTitle(handle, title);
    }

    public void setVSync(boolean vsync) {
        if (handle != NULL) glfwSwapInterval(vsync ? 1 : 0);
    }
	
	public void setIcon(ByteBuffer[] icons) {
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
