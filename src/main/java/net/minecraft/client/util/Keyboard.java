package net.minecraft.client.util;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public final class Keyboard {
	
	private static final int MAX_KEY = GLFW_KEY_LAST + 1;
	
	private static final boolean[] states = new boolean[MAX_KEY];
	private static final Map<Integer, String> keyNames = new HashMap<>();
	
	private static final Deque<KeyEvent> eventQueue = new ArrayDeque<>();
	private static KeyEvent pendingKeyEvent;
	private static KeyEvent currentEvent;

	private static void flushPending() {
		if (pendingKeyEvent != null) {
			eventQueue.addLast(pendingKeyEvent);
			pendingKeyEvent = null;
		}
	}
	
	@Getter
	@Setter
	private static boolean repeat;
	
	@Getter
	private static boolean created;
	
	private static Window window;
	private static GLFWKeyCallback keyCb;
	private static GLFWCharCallback charCb;
	
	static {
		putKey(GLFW_KEY_UNKNOWN, "NONE");
		putKey(GLFW_KEY_SPACE, "SPACE");
		putKey(GLFW_KEY_APOSTROPHE, "APOSTROPHE");
		putKey(GLFW_KEY_COMMA, "COMMA");
		putKey(GLFW_KEY_MINUS, "MINUS");
		putKey(GLFW_KEY_PERIOD, "PERIOD");
		putKey(GLFW_KEY_SLASH, "SLASH");
		putKey(GLFW_KEY_0, "0");
		putKey(GLFW_KEY_1, "1");
		putKey(GLFW_KEY_2, "2");
		putKey(GLFW_KEY_3, "3");
		putKey(GLFW_KEY_4, "4");
		putKey(GLFW_KEY_5, "5");
		putKey(GLFW_KEY_6, "6");
		putKey(GLFW_KEY_7, "7");
		putKey(GLFW_KEY_8, "8");
		putKey(GLFW_KEY_9, "9");
		putKey(GLFW_KEY_SEMICOLON, "SEMICOLON");
		putKey(GLFW_KEY_EQUAL, "EQUALS");
		putKey(GLFW_KEY_A, "A");
		putKey(GLFW_KEY_B, "B");
		putKey(GLFW_KEY_C, "C");
		putKey(GLFW_KEY_D, "D");
		putKey(GLFW_KEY_E, "E");
		putKey(GLFW_KEY_F, "F");
		putKey(GLFW_KEY_G, "G");
		putKey(GLFW_KEY_H, "H");
		putKey(GLFW_KEY_I, "I");
		putKey(GLFW_KEY_J, "J");
		putKey(GLFW_KEY_K, "K");
		putKey(GLFW_KEY_L, "L");
		putKey(GLFW_KEY_M, "M");
		putKey(GLFW_KEY_N, "N");
		putKey(GLFW_KEY_O, "O");
		putKey(GLFW_KEY_P, "P");
		putKey(GLFW_KEY_Q, "Q");
		putKey(GLFW_KEY_R, "R");
		putKey(GLFW_KEY_S, "S");
		putKey(GLFW_KEY_T, "T");
		putKey(GLFW_KEY_U, "U");
		putKey(GLFW_KEY_V, "V");
		putKey(GLFW_KEY_W, "W");
		putKey(GLFW_KEY_X, "X");
		putKey(GLFW_KEY_Y, "Y");
		putKey(GLFW_KEY_Z, "Z");
		putKey(GLFW_KEY_LEFT_BRACKET, "LBRACKET");
		putKey(GLFW_KEY_BACKSLASH, "BACKSLASH");
		putKey(GLFW_KEY_RIGHT_BRACKET, "RBRACKET");
		putKey(GLFW_KEY_GRAVE_ACCENT, "GRAVE");
		putKey(GLFW_KEY_ESCAPE, "ESCAPE");
		putKey(GLFW_KEY_ENTER, "RETURN");
		putKey(GLFW_KEY_TAB, "TAB");
		putKey(GLFW_KEY_BACKSPACE, "BACK");
		putKey(GLFW_KEY_INSERT, "INSERT");
		putKey(GLFW_KEY_DELETE, "DELETE");
		putKey(GLFW_KEY_RIGHT, "RIGHT");
		putKey(GLFW_KEY_LEFT, "LEFT");
		putKey(GLFW_KEY_DOWN, "DOWN");
		putKey(GLFW_KEY_UP, "UP");
		putKey(GLFW_KEY_PAGE_UP, "PRIOR");
		putKey(GLFW_KEY_PAGE_DOWN, "NEXT");
		putKey(GLFW_KEY_HOME, "HOME");
		putKey(GLFW_KEY_END, "END");
		putKey(GLFW_KEY_CAPS_LOCK, "CAPITAL");
		putKey(GLFW_KEY_SCROLL_LOCK, "SCROLL");
		putKey(GLFW_KEY_NUM_LOCK, "NUMLOCK");
		putKey(GLFW_KEY_PRINT_SCREEN, "SYSRQ");
		putKey(GLFW_KEY_PAUSE, "PAUSE");
		putKey(GLFW_KEY_F1, "F1");
		putKey(GLFW_KEY_F2, "F2");
		putKey(GLFW_KEY_F3, "F3");
		putKey(GLFW_KEY_F4, "F4");
		putKey(GLFW_KEY_F5, "F5");
		putKey(GLFW_KEY_F6, "F6");
		putKey(GLFW_KEY_F7, "F7");
		putKey(GLFW_KEY_F8, "F8");
		putKey(GLFW_KEY_F9, "F9");
		putKey(GLFW_KEY_F10, "F10");
		putKey(GLFW_KEY_F11, "F11");
		putKey(GLFW_KEY_F12, "F12");
		putKey(GLFW_KEY_KP_0, "NUMPAD0");
		putKey(GLFW_KEY_KP_1, "NUMPAD1");
		putKey(GLFW_KEY_KP_2, "NUMPAD2");
		putKey(GLFW_KEY_KP_3, "NUMPAD3");
		putKey(GLFW_KEY_KP_4, "NUMPAD4");
		putKey(GLFW_KEY_KP_5, "NUMPAD5");
		putKey(GLFW_KEY_KP_6, "NUMPAD6");
		putKey(GLFW_KEY_KP_7, "NUMPAD7");
		putKey(GLFW_KEY_KP_8, "NUMPAD8");
		putKey(GLFW_KEY_KP_9, "NUMPAD9");
		putKey(GLFW_KEY_KP_DECIMAL, "DECIMAL");
		putKey(GLFW_KEY_KP_DIVIDE, "DIVIDE");
		putKey(GLFW_KEY_KP_MULTIPLY, "MULTIPLY");
		putKey(GLFW_KEY_KP_SUBTRACT, "SUBTRACT");
		putKey(GLFW_KEY_KP_ADD, "ADD");
		putKey(GLFW_KEY_KP_ENTER, "NUMPADENTER");
		putKey(GLFW_KEY_KP_EQUAL, "NUMPADEQUALS");
		putKey(GLFW_KEY_LEFT_SHIFT, "LSHIFT");
		putKey(GLFW_KEY_LEFT_CONTROL, "LCONTROL");
		putKey(GLFW_KEY_LEFT_ALT, "LMENU");
		putKey(GLFW_KEY_LEFT_SUPER, "LMETA");
		putKey(GLFW_KEY_RIGHT_SHIFT, "RSHIFT");
		putKey(GLFW_KEY_RIGHT_CONTROL, "RCONTROL");
		putKey(GLFW_KEY_RIGHT_ALT, "RMENU");
		putKey(GLFW_KEY_RIGHT_SUPER, "RMETA");
		putKey(GLFW_KEY_MENU, "APPS");
	}
	
	private Keyboard() {
	}
	
	private static void putKey(int key, String name) {
		keyNames.put(key, name);
	}
	
	public static void init(Window window) {
		Keyboard.window = window;
		long handle = window.getHandle();
		created = true;
		
		charCb = GLFWCharCallback.create((win, codepoint) -> {
			if (pendingKeyEvent != null) {
				pendingKeyEvent = new KeyEvent(pendingKeyEvent.key, (char) codepoint, pendingKeyEvent.state, pendingKeyEvent.repeat);
				flushPending();
			}
		});
		
		keyCb = GLFWKeyCallback.create((win, key, scancode, action, mods) -> {
			if (key < 0 || key >= MAX_KEY) return;
			
			boolean pressed = (action != GLFW_RELEASE);
			boolean isRepeat = (action == GLFW_REPEAT);
			
			states[key] = pressed;
			
			if (!isRepeat || repeat) {
				flushPending();
				pendingKeyEvent = new KeyEvent(key, '\0', pressed, isRepeat);
				if (!pressed) flushPending();
			}
		});
		
		glfwSetKeyCallback(handle, keyCb);
		glfwSetCharCallback(handle, charCb);
	}
	
	public static void removeCallbacks() {
		long handle = window != null ? window.getHandle() : 0L;
		glfwSetKeyCallback(handle, null);
		glfwSetCharCallback(handle, null);
		
		if (keyCb != null) {
			keyCb.free();
			keyCb = null;
		}
		if (charCb != null) {
			charCb.free();
			charCb = null;
		}
		
		created = false;
	}
	
	public static void poll() {
		flushPending();
		currentEvent = null;
	}
	
	public static boolean isKeyDown(int key) {
		if (key < 0 || key >= MAX_KEY) return false;
		return states[key];
	}
	
	public static String getKeyName(int key) {
		String name = keyNames.get(key);
		if (name != null) return name;
		String glfwName = glfwGetKeyName(key, 0);
		return glfwName != null ? glfwName.toUpperCase() : "UNKNOWN";
	}
	
	public static boolean next() {
		if (eventQueue.isEmpty()) {
			currentEvent = null;
			return false;
		}
		currentEvent = eventQueue.pollFirst();
		return true;
	}
	
	public static int getEventKey() {
		return (currentEvent != null) ? currentEvent.key : GLFW_KEY_UNKNOWN;
	}
	
	public static char getEventCharacter() {
		return (currentEvent != null) ? currentEvent.character : '\0';
	}
	
	public static boolean getEventKeyState() {
		return currentEvent != null && currentEvent.state;
	}
	
	public static boolean isRepeatEvent() {
		return currentEvent != null && currentEvent.repeat;
	}
	
	private record KeyEvent(int key, char character, boolean state, boolean repeat) {
	}
	
}
