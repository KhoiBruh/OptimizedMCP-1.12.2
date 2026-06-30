package org.lwjgl.input;

import lombok.Getter;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.lwjgl.glfw.GLFW.*;

public final class Mouse {

    private static final boolean[] buttonState = new boolean[16];
    private static final Deque<MouseEvent> eventQueue = new ArrayDeque<>();
    private static long windowHandle = 0L;

    private static double x = 0;
    private static double y = 0;
    private static double lastX = 0;
    private static double lastY = 0;
	
    private static int windowWidth = 854;
    private static int windowHeight = 480;

    private static double scrollAccum = 0.0;
	
    @Getter
    private static boolean grabbed = false;
	
    @Getter
    private static boolean created = false;
	
    private static MouseEvent currentEvent = null;

    private static GLFWCursorPosCallback cursorPos;
    private static GLFWMouseButtonCallback mouseButton;
    private static GLFWScrollCallback scroll;

    private Mouse() {}

    public static void init(long handle) {
        windowHandle = handle;
        created = true;

        int[] w = new int[1], h = new int[1];
        glfwGetWindowSize(handle, w, h);
        windowWidth = w[0];
        windowHeight = h[0];

        double[] xPos = new double[1], yPos = new double[1];
        glfwGetCursorPos(handle, xPos, yPos);
        x = lastX = xPos[0];
        y = lastY = yPos[0];

        cursorPos = GLFWCursorPosCallback.create((win, x, y) -> {
            Mouse.x = x;
            Mouse.y = y;
        });

        mouseButton = GLFWMouseButtonCallback.create((win, btn, action, mods) -> {
            if (btn >= 0 && btn < buttonState.length) {
                boolean pressed = (action != GLFW_RELEASE);
                buttonState[btn] = pressed;
                eventQueue.addLast(new MouseEvent(btn, pressed, 0.0, x, y));
            }
        });

        scroll = GLFWScrollCallback.create((win, xOff, yOff) -> {
            scrollAccum += yOff;
            eventQueue.addLast(new MouseEvent(-1, false, yOff, x, y));
        });

        glfwSetCursorPosCallback(handle, cursorPos);
        glfwSetMouseButtonCallback(handle, mouseButton);
        glfwSetScrollCallback(handle, scroll);
    }

    public static void removeCallbacks(long handle) {
        glfwSetCursorPosCallback(handle, null);
        glfwSetMouseButtonCallback(handle, null);
        glfwSetScrollCallback(handle, null);

        if (cursorPos != null) { cursorPos.free(); cursorPos = null; }
        if (mouseButton != null) { mouseButton.free(); mouseButton = null; }
        if (scroll != null) { scroll.free(); scroll = null; }

        created = false;
    }

    public static void poll() {
        lastX = x;
        lastY = y;
        scrollAccum = 0.0;
        currentEvent = null;
    }

    public static void destroy() {
        created = false;
    }
	
	public static int getDX() {
        return (int) (x - lastX);
    }

    public static int getDY() {
        return -(int) (y - lastY);
    }

    public static int getX() {
        return (int) x;
    }

    public static int getY() {
        return windowHeight - (int) y;
    }

    public static int getDWheel() {
        return (int) (scrollAccum * 120.0);
    }

    public static boolean isButtonDown(int button) {
        if (button < 0 || button >= buttonState.length) return false;
        return buttonState[button];
    }
	
	public static void setGrabbed(boolean grab) {
        grabbed = grab;
        if (windowHandle == 0L) return;
        glfwSetInputMode(windowHandle, GLFW_CURSOR,
                grab ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
    }

    public static void setCursorPosition(int x, int y) {
        if (windowHandle == 0L) return;
        glfwSetCursorPos(windowHandle, x, y);
        Mouse.x = x;
        Mouse.y = y;
    }

    public static boolean isInsideWindow() {
        if (windowHandle == 0L) return false;
        return glfwGetWindowAttrib(windowHandle, GLFW_HOVERED) == GLFW_TRUE;
    }

    public static boolean next() {
        if (eventQueue.isEmpty()) {
            currentEvent = null;
            return false;
        }
        currentEvent = eventQueue.pollFirst();
        return true;
    }

    public static int getEventButton() {
        return (currentEvent != null) ? currentEvent.button : -1;
    }

    public static boolean getEventButtonState() {
        return currentEvent != null && currentEvent.buttonState;
    }

    public static int getEventX() {
        if (currentEvent == null) return 0;
        return (int) currentEvent.x;
    }

    public static int getEventY() {
        if (currentEvent == null) return 0;
        return windowHeight - (int) currentEvent.y;
    }

    public static int getEventDWheel() {
        if (currentEvent == null) return 0;
        return (int) (currentEvent.dWheel * 120.0);
    }

    public static void setWindowSize(int width, int height) {
        windowWidth = width;
        windowHeight = height;
    }
	
	private record MouseEvent(int button, boolean buttonState, double dWheel, double x, double y) { }

}
