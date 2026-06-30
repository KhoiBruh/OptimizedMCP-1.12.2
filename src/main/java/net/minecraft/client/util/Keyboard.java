package net.minecraft.client.util;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public final class Keyboard {
	
    private static final int MAX_KEY = GLFW_KEY_LAST + 1;

    private static final boolean[] keyState = new boolean[MAX_KEY];
    private static final Map<Integer, String> keyNames = new HashMap<>();
    private static final Map<String, Integer> nameToKey = new HashMap<>();

    private static final Deque<KeyEvent> eventQueue = new ArrayDeque<>();
    private static KeyEvent currentEvent = null;

    private static final Deque<Character> charQueue = new ArrayDeque<>();

    @Getter @Setter
    private static boolean repeat = false;

    @Getter
    private static boolean created = false;

    private static Window window;
    private static GLFWKeyCallback cbKey;
    private static GLFWCharCallback cbChar;

    private Keyboard() {}

    static {
        initKeyNames();
    }

    private static void initKeyNames() {
        registerKey(GLFW_KEY_SPACE, "SPACE");
        registerKey(GLFW_KEY_APOSTROPHE, "APOSTROPHE");
        registerKey(GLFW_KEY_COMMA, "COMMA");
        registerKey(GLFW_KEY_MINUS, "MINUS");
        registerKey(GLFW_KEY_PERIOD, "PERIOD");
        registerKey(GLFW_KEY_SLASH, "SLASH");
        registerKey(GLFW_KEY_0, "0");
        registerKey(GLFW_KEY_1, "1");
        registerKey(GLFW_KEY_2, "2");
        registerKey(GLFW_KEY_3, "3");
        registerKey(GLFW_KEY_4, "4");
        registerKey(GLFW_KEY_5, "5");
        registerKey(GLFW_KEY_6, "6");
        registerKey(GLFW_KEY_7, "7");
        registerKey(GLFW_KEY_8, "8");
        registerKey(GLFW_KEY_9, "9");
        registerKey(GLFW_KEY_SEMICOLON, "SEMICOLON");
        registerKey(GLFW_KEY_EQUAL, "EQUALS");
        registerKey(GLFW_KEY_A, "A");
        registerKey(GLFW_KEY_B, "B");
        registerKey(GLFW_KEY_C, "C");
        registerKey(GLFW_KEY_D, "D");
        registerKey(GLFW_KEY_E, "E");
        registerKey(GLFW_KEY_F, "F");
        registerKey(GLFW_KEY_G, "G");
        registerKey(GLFW_KEY_H, "H");
        registerKey(GLFW_KEY_I, "I");
        registerKey(GLFW_KEY_J, "J");
        registerKey(GLFW_KEY_K, "K");
        registerKey(GLFW_KEY_L, "L");
        registerKey(GLFW_KEY_M, "M");
        registerKey(GLFW_KEY_N, "N");
        registerKey(GLFW_KEY_O, "O");
        registerKey(GLFW_KEY_P, "P");
        registerKey(GLFW_KEY_Q, "Q");
        registerKey(GLFW_KEY_R, "R");
        registerKey(GLFW_KEY_S, "S");
        registerKey(GLFW_KEY_T, "T");
        registerKey(GLFW_KEY_U, "U");
        registerKey(GLFW_KEY_V, "V");
        registerKey(GLFW_KEY_W, "W");
        registerKey(GLFW_KEY_X, "X");
        registerKey(GLFW_KEY_Y, "Y");
        registerKey(GLFW_KEY_Z, "Z");
        registerKey(GLFW_KEY_LEFT_BRACKET, "LBRACKET");
        registerKey(GLFW_KEY_BACKSLASH, "BACKSLASH");
        registerKey(GLFW_KEY_RIGHT_BRACKET, "RBRACKET");
        registerKey(GLFW_KEY_GRAVE_ACCENT, "GRAVE");
        registerKey(GLFW_KEY_ESCAPE, "ESCAPE");
        registerKey(GLFW_KEY_ENTER, "RETURN");
        registerKey(GLFW_KEY_TAB, "TAB");
        registerKey(GLFW_KEY_BACKSPACE, "BACK");
        registerKey(GLFW_KEY_INSERT, "INSERT");
        registerKey(GLFW_KEY_DELETE, "DELETE");
        registerKey(GLFW_KEY_RIGHT, "RIGHT");
        registerKey(GLFW_KEY_LEFT, "LEFT");
        registerKey(GLFW_KEY_DOWN, "DOWN");
        registerKey(GLFW_KEY_UP, "UP");
        registerKey(GLFW_KEY_PAGE_UP, "PRIOR");
        registerKey(GLFW_KEY_PAGE_DOWN, "NEXT");
        registerKey(GLFW_KEY_HOME, "HOME");
        registerKey(GLFW_KEY_END, "END");
        registerKey(GLFW_KEY_CAPS_LOCK, "CAPITAL");
        registerKey(GLFW_KEY_SCROLL_LOCK, "SCROLL");
        registerKey(GLFW_KEY_NUM_LOCK, "NUMLOCK");
        registerKey(GLFW_KEY_PRINT_SCREEN, "SYSRQ");
        registerKey(GLFW_KEY_PAUSE, "PAUSE");
        registerKey(GLFW_KEY_F1, "F1");
        registerKey(GLFW_KEY_F2, "F2");
        registerKey(GLFW_KEY_F3, "F3");
        registerKey(GLFW_KEY_F4, "F4");
        registerKey(GLFW_KEY_F5, "F5");
        registerKey(GLFW_KEY_F6, "F6");
        registerKey(GLFW_KEY_F7, "F7");
        registerKey(GLFW_KEY_F8, "F8");
        registerKey(GLFW_KEY_F9, "F9");
        registerKey(GLFW_KEY_F10, "F10");
        registerKey(GLFW_KEY_F11, "F11");
        registerKey(GLFW_KEY_F12, "F12");
        registerKey(GLFW_KEY_KP_0, "NUMPAD0");
        registerKey(GLFW_KEY_KP_1, "NUMPAD1");
        registerKey(GLFW_KEY_KP_2, "NUMPAD2");
        registerKey(GLFW_KEY_KP_3, "NUMPAD3");
        registerKey(GLFW_KEY_KP_4, "NUMPAD4");
        registerKey(GLFW_KEY_KP_5, "NUMPAD5");
        registerKey(GLFW_KEY_KP_6, "NUMPAD6");
        registerKey(GLFW_KEY_KP_7, "NUMPAD7");
        registerKey(GLFW_KEY_KP_8, "NUMPAD8");
        registerKey(GLFW_KEY_KP_9, "NUMPAD9");
        registerKey(GLFW_KEY_KP_DECIMAL, "DECIMAL");
        registerKey(GLFW_KEY_KP_DIVIDE, "DIVIDE");
        registerKey(GLFW_KEY_KP_MULTIPLY, "MULTIPLY");
        registerKey(GLFW_KEY_KP_SUBTRACT, "SUBTRACT");
        registerKey(GLFW_KEY_KP_ADD, "ADD");
        registerKey(GLFW_KEY_KP_ENTER, "NUMPADENTER");
        registerKey(GLFW_KEY_KP_EQUAL, "NUMPADEQUALS");
        registerKey(GLFW_KEY_LEFT_SHIFT, "LSHIFT");
        registerKey(GLFW_KEY_LEFT_CONTROL, "LCONTROL");
        registerKey(GLFW_KEY_LEFT_ALT, "LMENU");
        registerKey(GLFW_KEY_LEFT_SUPER, "LMETA");
        registerKey(GLFW_KEY_RIGHT_SHIFT, "RSHIFT");
        registerKey(GLFW_KEY_RIGHT_CONTROL, "RCONTROL");
        registerKey(GLFW_KEY_RIGHT_ALT, "RMENU");
        registerKey(GLFW_KEY_RIGHT_SUPER, "RMETA");
        registerKey(GLFW_KEY_MENU, "APPS");
    }

    private static void registerKey(int key, String name) {
        keyNames.put(key, name);
        nameToKey.put(name, key);
    }

    public static void destroy() {
        created = false;
    }

    public static void init(Window window) {
        Keyboard.window = window;
        long handle = window.getHandle();
        created = true;

        cbChar = GLFWCharCallback.create((win, codepoint) -> {
            synchronized (charQueue) {
                charQueue.addLast((char) codepoint);
            }
        });

        cbKey = GLFWKeyCallback.create((win, glfwKey, scancode, action, mods) -> {
            if (glfwKey < 0 || glfwKey >= MAX_KEY) return;

            boolean pressed = (action != GLFW_RELEASE);
            boolean isRepeat = (action == GLFW_REPEAT);

            keyState[glfwKey] = pressed;

            if (!isRepeat || repeat) {
                char ch;
                synchronized (charQueue) {
                    ch = charQueue.isEmpty() ? '\0' : charQueue.pollFirst();
                }
                eventQueue.addLast(new KeyEvent(glfwKey, ch, pressed, isRepeat));
            }
        });

        glfwSetKeyCallback(handle, cbKey);
        glfwSetCharCallback(handle, cbChar);
    }

    public static void removeCallbacks() {
        long handle = window != null ? window.getHandle() : 0L;
        glfwSetKeyCallback(handle, null);
        glfwSetCharCallback(handle, null);

        if (cbKey != null) {
            cbKey.free();
            cbKey = null;
        }
        if (cbChar != null) {
            cbChar.free();
            cbChar = null;
        }
        created = false;
    }

    public static void poll() {
        currentEvent = null;
    }

    public static void enableRepeatEvents(boolean enable) {
        repeat = enable;
    }

    public static boolean isKeyDown(int key) {
        if (key < 0 || key >= MAX_KEY) return false;
        return keyState[key];
    }

    public static String getKeyName(int key) {
        String name = keyNames.get(key);
        if (name != null) return name;
        String glfwName = glfwGetKeyName(key, 0);
        return glfwName != null ? glfwName.toUpperCase() : "UNKNOWN";
    }

    public static int getKeyIndex(String name) {
        Integer key = nameToKey.get(name);
        return key != null ? key : GLFW_KEY_UNKNOWN;
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
        return currentEvent != null && currentEvent.keyState;
    }

    public static boolean isRepeatEvent() {
        return currentEvent != null && currentEvent.repeat;
    }

    private record KeyEvent(int key, char character, boolean keyState, boolean repeat) { }

}
