package net.minecraft.client.util;

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
    private static Window window;

    private static double x = 0;
    private static double y = 0;
    private static double lastX = 0;
    private static double lastY = 0;

    private static double lastEventX = 0;
    private static double lastEventY = 0;

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

    public static void init(Window window) {
        Mouse.window = window;
        long handle = window.getHandle();
        created = true;

        double[] xPos = new double[1];
        double[] yPos = new double[1];
        glfwGetCursorPos(handle, xPos, yPos);
        x = lastX = lastEventX = xPos[0];
        y = lastY = lastEventY = yPos[0];

        cursorPos = GLFWCursorPosCallback.create((win, cx, cy) -> {
            double dx = cx - lastEventX;
            double dy = lastEventY - cy;
            lastEventX = cx;
            lastEventY = cy;
            Mouse.x = cx;
            Mouse.y = cy;
            eventQueue.addLast(new MouseEvent(-1, false, 0.0, cx, cy, dx, dy));
        });

        mouseButton = GLFWMouseButtonCallback.create((win, btn, action, mods) -> {
            if (btn >= 0 && btn < buttonState.length) {
                boolean pressed = (action != GLFW_RELEASE);
                buttonState[btn] = pressed;
                eventQueue.addLast(new MouseEvent(btn, pressed, 0.0, x, y, 0.0, 0.0));
            }
        });

        scroll = GLFWScrollCallback.create((win, xOff, yOff) -> {
            scrollAccum += yOff;
            eventQueue.addLast(new MouseEvent(-1, false, yOff, x, y, 0.0, 0.0));
        });

        glfwSetCursorPosCallback(handle, cursorPos);
        glfwSetMouseButtonCallback(handle, mouseButton);
        glfwSetScrollCallback(handle, scroll);
    }

    public static void removeCallbacks() {
        long handle = window != null ? window.getHandle() : 0L;
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
        return (window != null ? window.getHeight() : 0) - (int) y;
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
        if (window == null) return;
        long handle = window.getHandle();
        glfwSetInputMode(handle, GLFW_CURSOR,
                         grab ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
    }

    public static void setCursorPosition(int x, int y) {
        if (window == null) return;
        long handle = window.getHandle();
        glfwSetCursorPos(handle, x, y);
        Mouse.x = lastEventX = x;
        Mouse.y = lastEventY = y;
    }

    public static boolean isInsideWindow() {
        if (window == null) return false;
        return glfwGetWindowAttrib(window.getHandle(), GLFW_HOVERED) == GLFW_TRUE;
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
        return (window != null ? window.getHeight() : 0) - (int) currentEvent.y;
    }

    public static int getEventDX() {
        if (currentEvent == null) return 0;
        return (int) currentEvent.dx;
    }

    public static int getEventDY() {
        if (currentEvent == null) return 0;
        return (int) currentEvent.dy;
    }

    public static int getEventDWheel() {
        if (currentEvent == null) return 0;
        return (int) (currentEvent.dWheel * 120.0);
    }

    private record MouseEvent(int button, boolean buttonState, double dWheel, double x, double y, double dx, double dy) { }

}
