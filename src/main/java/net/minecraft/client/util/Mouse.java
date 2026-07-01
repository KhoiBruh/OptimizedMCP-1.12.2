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
	
	private static double x;
	private static double y;
	private static double lastX;
	private static double lastY;
	private static double lastEventX;
	private static double lastEventY;
	
	@Getter
	private static boolean grabbed;
	
	@Getter
	private static boolean created;
	
	private static MouseEvent currentEvent;
	
	private static GLFWCursorPosCallback posCb;
	private static GLFWMouseButtonCallback buttonCb;
	private static GLFWScrollCallback scrollCb;
	
	private Mouse() {
	}
	
	public static void init(Window window) {
		Mouse.window = window;
		long handle = window.getHandle();
		created = true;
		
		double[] xPos = new double[1];
		double[] yPos = new double[1];
		glfwGetCursorPos(handle, xPos, yPos);
		x = lastX = lastEventX = xPos[0];
		y = lastY = lastEventY = yPos[0];
		
		posCb = GLFWCursorPosCallback.create((win, cx, cy) -> {
			double dx = cx - lastEventX;
			double dy = lastEventY - cy;
			lastEventX = cx;
			lastEventY = cy;
			Mouse.x = cx;
			Mouse.y = cy;
			eventQueue.addLast(new MouseEvent(-1, false, 0, cx, cy, dx, dy));
		});
		
		buttonCb = GLFWMouseButtonCallback.create((win, btn, action, mods) -> {
			if (btn >= 0 && btn < buttonState.length) {
				boolean pressed = (action != GLFW_RELEASE);
				buttonState[btn] = pressed;
				eventQueue.addLast(new MouseEvent(btn, pressed, 0, x, y, 0, 0));
			}
		});
		
		scrollCb = GLFWScrollCallback.create((win, xOff, yOff) -> eventQueue.addLast(
				new MouseEvent(-1, false, yOff, x, y, 0, 0)
		));
		
		glfwSetCursorPosCallback(handle, posCb);
		glfwSetMouseButtonCallback(handle, buttonCb);
		glfwSetScrollCallback(handle, scrollCb);
	}
	
	public static void removeCallbacks() {
		long handle = window.getHandle();
		glfwSetCursorPosCallback(handle, null);
		glfwSetMouseButtonCallback(handle, null);
		glfwSetScrollCallback(handle, null);
		
		if (posCb != null) {
			posCb.free();
			posCb = null;
		}
		if (buttonCb != null) {
			buttonCb.free();
			buttonCb = null;
		}
		if (scrollCb != null) {
			scrollCb.free();
			scrollCb = null;
		}
		
		created = false;
	}
	
	public static void poll() {
		lastX = x;
		lastY = y;
		currentEvent = null;
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
	
	public static boolean isButtonDown(int button) {
		if (button < 0 || button >= buttonState.length) return false;
		return buttonState[button];
	}
	
	public static void setGrabbed(boolean grab) {
		grabbed = grab;
		if (window == null) return;
		long handle = window.getHandle();
		glfwSetInputMode(handle, GLFW_CURSOR, grab ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
	}

	public static void grabCursor() {
		setGrabbed(true);
		lastX = x;
		lastY = y;
	}

	public static void ungrabCursor() {
		if (window != null) setCursorPos(window.getWidth() / 2, window.getHeight() / 2);
		setGrabbed(false);
	}
	
	public static void setCursorPos(int x, int y) {
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
	
	public static int getEventDWheel() {
		if (currentEvent == null) return 0;
		return (int) (currentEvent.dWheel * 120);
	}
	
	private record MouseEvent(int button, boolean buttonState, double dWheel, double x, double y, double dx, double dy) {
	
	}
	
}
