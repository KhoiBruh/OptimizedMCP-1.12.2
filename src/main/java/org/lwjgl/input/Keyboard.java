package org.lwjgl.input;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public final class Keyboard {

    public static final int KEY_NONE = 0;
    public static final int KEY_ESCAPE = 1;
    public static final int KEY_1 = 2;
    public static final int KEY_2 = 3;
    public static final int KEY_3 = 4;
    public static final int KEY_4 = 5;
    public static final int KEY_5 = 6;
    public static final int KEY_6 = 7;
    public static final int KEY_7 = 8;
    public static final int KEY_8 = 9;
    public static final int KEY_9 = 10;
    public static final int KEY_0 = 11;
    public static final int KEY_MINUS = 12;
    public static final int KEY_EQUALS = 13;
    public static final int KEY_BACK = 14;
    public static final int KEY_TAB = 15;
    public static final int KEY_Q = 16;
    public static final int KEY_W = 17;
    public static final int KEY_E = 18;
    public static final int KEY_R = 19;
    public static final int KEY_T = 20;
    public static final int KEY_Y = 21;
    public static final int KEY_U = 22;
    public static final int KEY_I = 23;
    public static final int KEY_O = 24;
    public static final int KEY_P = 25;
    public static final int KEY_LBRACKET = 26;
    public static final int KEY_RBRACKET = 27;
    public static final int KEY_RETURN = 28;
    public static final int KEY_LCONTROL = 29;
    public static final int KEY_A = 30;
    public static final int KEY_S = 31;
    public static final int KEY_D = 32;
    public static final int KEY_F = 33;
    public static final int KEY_G = 34;
    public static final int KEY_H = 35;
    public static final int KEY_J = 36;
    public static final int KEY_K = 37;
    public static final int KEY_L = 38;
    public static final int KEY_SEMICOLON = 39;
    public static final int KEY_APOSTROPHE = 40;
    public static final int KEY_GRAVE = 41;
    public static final int KEY_LSHIFT = 42;
    public static final int KEY_BACKSLASH = 43;
    public static final int KEY_Z = 44;
    public static final int KEY_X = 45;
    public static final int KEY_C = 46;
    public static final int KEY_V = 47;
    public static final int KEY_B = 48;
    public static final int KEY_N = 49;
    public static final int KEY_M = 50;
    public static final int KEY_COMMA = 51;
    public static final int KEY_PERIOD = 52;
    public static final int KEY_SLASH = 53;
    public static final int KEY_RSHIFT = 54;
    public static final int KEY_MULTIPLY = 55;
    public static final int KEY_LMENU = 56;
    public static final int KEY_SPACE = 57;
    public static final int KEY_CAPITAL = 58;
    public static final int KEY_F1 = 59;
    public static final int KEY_F2 = 60;
    public static final int KEY_F3 = 61;
    public static final int KEY_F4 = 62;
    public static final int KEY_F5 = 63;
    public static final int KEY_F6 = 64;
    public static final int KEY_F7 = 65;
    public static final int KEY_F8 = 66;
    public static final int KEY_F9 = 67;
    public static final int KEY_F10 = 68;
    public static final int KEY_NUMLOCK = 69;
    public static final int KEY_SCROLL = 70;
    public static final int KEY_NUMPAD7 = 71;
    public static final int KEY_NUMPAD8 = 72;
    public static final int KEY_NUMPAD9 = 73;
    public static final int KEY_SUBTRACT = 74;
    public static final int KEY_NUMPAD4 = 75;
    public static final int KEY_NUMPAD5 = 76;
    public static final int KEY_NUMPAD6 = 77;
    public static final int KEY_ADD = 78;
    public static final int KEY_NUMPAD1 = 79;
    public static final int KEY_NUMPAD2 = 80;
    public static final int KEY_NUMPAD3 = 81;
    public static final int KEY_NUMPAD0 = 82;
    public static final int KEY_DECIMAL = 83;
    public static final int KEY_F11 = 87;
    public static final int KEY_F12 = 88;
    public static final int KEY_F13 = 100;
    public static final int KEY_F14 = 101;
    public static final int KEY_F15 = 102;
    public static final int KEY_KANA = 112;
    public static final int KEY_CONVERT = 121;
    public static final int KEY_NOCONVERT = 123;
    public static final int KEY_YEN = 125;
    public static final int KEY_NUMPADEQUALS = 141;
    public static final int KEY_CIRCUMFLEX = 144;
    public static final int KEY_AT = 145;
    public static final int KEY_COLON = 146;
    public static final int KEY_UNDERLINE = 147;
    public static final int KEY_STOP = 148;
    public static final int KEY_NUMPADENTER = 156;
    public static final int KEY_RCONTROL = 157;
    public static final int KEY_NUMPADCOMMA = 179;
    public static final int KEY_DIVIDE = 181;
    public static final int KEY_SYSRQ = 183;
    public static final int KEY_RMENU = 184;
    public static final int KEY_PAUSE = 197;
    public static final int KEY_HOME = 199;
    public static final int KEY_UP = 200;
    public static final int KEY_PRIOR = 201;
    public static final int KEY_LEFT = 203;
    public static final int KEY_RIGHT = 205;
    public static final int KEY_END = 207;
    public static final int KEY_DOWN = 208;
    public static final int KEY_NEXT = 209;
    public static final int KEY_INSERT = 210;
    public static final int KEY_DELETE = 211;
    public static final int KEY_LMETA = 219;
    public static final int KEY_RMETA = 220;
    public static final int KEY_APPS = 221;

    private static final int MAX_KEY = 256;

    private static final boolean[] keyState = new boolean[MAX_KEY];
    private static final String[] keyNames = new String[MAX_KEY];
    private static final Map<String, Integer> nameToKey = new HashMap<>();
    private static final Map<Integer, Integer> glfwToLwjgl = new HashMap<>();
    private static final Map<Integer, Integer> lwjglToGlfw = new HashMap<>();

    private static final Deque<KeyEvent> eventQueue = new ArrayDeque<>();
    private static KeyEvent currentEvent = null;
	
	@Getter
	@Setter
    private static boolean repeat = false;
	
    @Getter
    private static boolean created = false;
    private static GLFWKeyCallback cbKey;
    private static GLFWCharCallback cbChar;
    private static char pendingChar = '\0';

    private Keyboard() {}

    static {
	    Arrays.fill(keyNames, "UNKNOWN");
        initKeyNames();
        initKeyMapping();
    }

    private static void mapKey(int lwjglCode, int glfwCode, String name) {
        lwjglToGlfw.put(lwjglCode, glfwCode);
        glfwToLwjgl.put(glfwCode, lwjglCode);
        if (lwjglCode >= 0 && lwjglCode < MAX_KEY) {
            keyNames[lwjglCode] = name;
        }
        nameToKey.put(name, lwjglCode);
    }

    private static void initKeyMapping() {
        mapKey(KEY_ESCAPE, GLFW_KEY_ESCAPE, "ESCAPE");
        mapKey(KEY_1, GLFW_KEY_1, "1");
        mapKey(KEY_2, GLFW_KEY_2, "2");
        mapKey(KEY_3, GLFW_KEY_3, "3");
        mapKey(KEY_4, GLFW_KEY_4, "4");
        mapKey(KEY_5, GLFW_KEY_5, "5");
        mapKey(KEY_6, GLFW_KEY_6, "6");
        mapKey(KEY_7, GLFW_KEY_7, "7");
        mapKey(KEY_8, GLFW_KEY_8, "8");
        mapKey(KEY_9, GLFW_KEY_9, "9");
        mapKey(KEY_0, GLFW_KEY_0, "0");
        mapKey(KEY_MINUS, GLFW_KEY_MINUS, "MINUS");
        mapKey(KEY_EQUALS, GLFW_KEY_EQUAL, "EQUALS");
        mapKey(KEY_BACK, GLFW_KEY_BACKSPACE, "BACK");
        mapKey(KEY_TAB, GLFW_KEY_TAB, "TAB");
        mapKey(KEY_Q, GLFW_KEY_Q, "Q");
        mapKey(KEY_W, GLFW_KEY_W, "W");
        mapKey(KEY_E, GLFW_KEY_E, "E");
        mapKey(KEY_R, GLFW_KEY_R, "R");
        mapKey(KEY_T, GLFW_KEY_T, "T");
        mapKey(KEY_Y, GLFW_KEY_Y, "Y");
        mapKey(KEY_U, GLFW_KEY_U, "U");
        mapKey(KEY_I, GLFW_KEY_I, "I");
        mapKey(KEY_O, GLFW_KEY_O, "O");
        mapKey(KEY_P, GLFW_KEY_P, "P");
        mapKey(KEY_LBRACKET, GLFW_KEY_LEFT_BRACKET, "LBRACKET");
        mapKey(KEY_RBRACKET, GLFW_KEY_RIGHT_BRACKET, "RBRACKET");
        mapKey(KEY_RETURN, GLFW_KEY_ENTER, "RETURN");
        mapKey(KEY_LCONTROL, GLFW_KEY_LEFT_CONTROL, "LCONTROL");
        mapKey(KEY_A, GLFW_KEY_A, "A");
        mapKey(KEY_S, GLFW_KEY_S, "S");
        mapKey(KEY_D, GLFW_KEY_D, "D");
        mapKey(KEY_F, GLFW_KEY_F, "F");
        mapKey(KEY_G, GLFW_KEY_G, "G");
        mapKey(KEY_H, GLFW_KEY_H, "H");
        mapKey(KEY_J, GLFW_KEY_J, "J");
        mapKey(KEY_K, GLFW_KEY_K, "K");
        mapKey(KEY_L, GLFW_KEY_L, "L");
        mapKey(KEY_SEMICOLON, GLFW_KEY_SEMICOLON, "SEMICOLON");
        mapKey(KEY_APOSTROPHE, GLFW_KEY_APOSTROPHE, "APOSTROPHE");
        mapKey(KEY_GRAVE, GLFW_KEY_GRAVE_ACCENT, "GRAVE");
        mapKey(KEY_LSHIFT, GLFW_KEY_LEFT_SHIFT, "LSHIFT");
        mapKey(KEY_BACKSLASH, GLFW_KEY_BACKSLASH, "BACKSLASH");
        mapKey(KEY_Z, GLFW_KEY_Z, "Z");
        mapKey(KEY_X, GLFW_KEY_X, "X");
        mapKey(KEY_C, GLFW_KEY_C, "C");
        mapKey(KEY_V, GLFW_KEY_V, "V");
        mapKey(KEY_B, GLFW_KEY_B, "B");
        mapKey(KEY_N, GLFW_KEY_N, "N");
        mapKey(KEY_M, GLFW_KEY_M, "M");
        mapKey(KEY_COMMA, GLFW_KEY_COMMA, "COMMA");
        mapKey(KEY_PERIOD, GLFW_KEY_PERIOD, "PERIOD");
        mapKey(KEY_SLASH, GLFW_KEY_SLASH, "SLASH");
        mapKey(KEY_RSHIFT, GLFW_KEY_RIGHT_SHIFT, "RSHIFT");
        mapKey(KEY_MULTIPLY, GLFW_KEY_KP_MULTIPLY, "MULTIPLY");
        mapKey(KEY_LMENU, GLFW_KEY_LEFT_ALT, "LMENU");
        mapKey(KEY_SPACE, GLFW_KEY_SPACE, "SPACE");
        mapKey(KEY_CAPITAL, GLFW_KEY_CAPS_LOCK, "CAPITAL");
        mapKey(KEY_F1, GLFW_KEY_F1, "F1");
        mapKey(KEY_F2, GLFW_KEY_F2, "F2");
        mapKey(KEY_F3, GLFW_KEY_F3, "F3");
        mapKey(KEY_F4, GLFW_KEY_F4, "F4");
        mapKey(KEY_F5, GLFW_KEY_F5, "F5");
        mapKey(KEY_F6, GLFW_KEY_F6, "F6");
        mapKey(KEY_F7, GLFW_KEY_F7, "F7");
        mapKey(KEY_F8, GLFW_KEY_F8, "F8");
        mapKey(KEY_F9, GLFW_KEY_F9, "F9");
        mapKey(KEY_F10, GLFW_KEY_F10, "F10");
        mapKey(KEY_NUMLOCK, GLFW_KEY_NUM_LOCK, "NUMLOCK");
        mapKey(KEY_SCROLL, GLFW_KEY_SCROLL_LOCK, "SCROLL");
        mapKey(KEY_NUMPAD7, GLFW_KEY_KP_7, "NUMPAD7");
        mapKey(KEY_NUMPAD8, GLFW_KEY_KP_8, "NUMPAD8");
        mapKey(KEY_NUMPAD9, GLFW_KEY_KP_9, "NUMPAD9");
        mapKey(KEY_SUBTRACT, GLFW_KEY_KP_SUBTRACT, "SUBTRACT");
        mapKey(KEY_NUMPAD4, GLFW_KEY_KP_4, "NUMPAD4");
        mapKey(KEY_NUMPAD5, GLFW_KEY_KP_5, "NUMPAD5");
        mapKey(KEY_NUMPAD6, GLFW_KEY_KP_6, "NUMPAD6");
        mapKey(KEY_ADD, GLFW_KEY_KP_ADD, "ADD");
        mapKey(KEY_NUMPAD1, GLFW_KEY_KP_1, "NUMPAD1");
        mapKey(KEY_NUMPAD2, GLFW_KEY_KP_2, "NUMPAD2");
        mapKey(KEY_NUMPAD3, GLFW_KEY_KP_3, "NUMPAD3");
        mapKey(KEY_NUMPAD0, GLFW_KEY_KP_0, "NUMPAD0");
        mapKey(KEY_DECIMAL, GLFW_KEY_KP_DECIMAL, "DECIMAL");
        mapKey(KEY_F11, GLFW_KEY_F11, "F11");
        mapKey(KEY_F12, GLFW_KEY_F12, "F12");
        mapKey(KEY_F13, GLFW_KEY_F13, "F13");
        mapKey(KEY_F14, GLFW_KEY_F14, "F14");
        mapKey(KEY_F15, GLFW_KEY_F15, "F15");
        mapKey(KEY_NUMPADEQUALS, GLFW_KEY_KP_EQUAL, "NUMPADEQUALS");
        mapKey(KEY_NUMPADENTER, GLFW_KEY_KP_ENTER, "NUMPADENTER");
        mapKey(KEY_RCONTROL, GLFW_KEY_RIGHT_CONTROL, "RCONTROL");
        mapKey(KEY_NUMPADCOMMA, GLFW_KEY_KP_DECIMAL, "NUMPADCOMMA");
        mapKey(KEY_DIVIDE, GLFW_KEY_KP_DIVIDE, "DIVIDE");
        mapKey(KEY_SYSRQ, GLFW_KEY_PRINT_SCREEN, "SYSRQ");
        mapKey(KEY_RMENU, GLFW_KEY_RIGHT_ALT, "RMENU");
        mapKey(KEY_PAUSE, GLFW_KEY_PAUSE, "PAUSE");
        mapKey(KEY_HOME, GLFW_KEY_HOME, "HOME");
        mapKey(KEY_UP, GLFW_KEY_UP, "UP");
        mapKey(KEY_PRIOR, GLFW_KEY_PAGE_UP, "PRIOR");
        mapKey(KEY_LEFT, GLFW_KEY_LEFT, "LEFT");
        mapKey(KEY_RIGHT, GLFW_KEY_RIGHT, "RIGHT");
        mapKey(KEY_END, GLFW_KEY_END, "END");
        mapKey(KEY_DOWN, GLFW_KEY_DOWN, "DOWN");
        mapKey(KEY_NEXT, GLFW_KEY_PAGE_DOWN, "NEXT");
        mapKey(KEY_INSERT, GLFW_KEY_INSERT, "INSERT");
        mapKey(KEY_DELETE, GLFW_KEY_DELETE, "DELETE");
        mapKey(KEY_LMETA, GLFW_KEY_LEFT_SUPER, "LMETA");
        mapKey(KEY_RMETA, GLFW_KEY_RIGHT_SUPER, "RMETA");
        mapKey(KEY_APPS, GLFW_KEY_MENU, "APPS");
    }

    private static void initKeyNames() {
        nameToKey.put("NONE", 0);
        keyNames[0] = "NONE";
    }

    public static void create() {
    }

    public static void destroy() {
        created = false;
    }
	
	public static void init(long handle) {
        created = true;

        cbChar = GLFWCharCallback.create((win, codepoint) -> pendingChar = (char) codepoint);

        cbKey = GLFWKeyCallback.create((win, glfwKey, scancode, action, mods) -> {
            if (glfwKey == GLFW_KEY_UNKNOWN) return;

            Integer lwjglCode = glfwToLwjgl.get(glfwKey);
            if (lwjglCode == null || lwjglCode < 0 || lwjglCode >= MAX_KEY) return;

            boolean pressed = (action != GLFW_RELEASE);
            boolean isRepeat = (action == GLFW_REPEAT);

            keyState[lwjglCode] = pressed;

            if (!isRepeat || repeat) {
                char ch = pendingChar;
                pendingChar = '\0';
                eventQueue.addLast(new KeyEvent(lwjglCode, ch, pressed, isRepeat));
            }
        });

        glfwSetKeyCallback(handle, cbKey);
        glfwSetCharCallback(handle, cbChar);
    }

    public static void removeCallbacks(long handle) {
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
        pendingChar = '\0';
    }

    public static void enableRepeatEvents(boolean enable) {
        repeat = enable;
    }

    public static boolean isKeyDown(int key) {
        if (key < 0 || key >= MAX_KEY) return false;
        return keyState[key];
    }

    public static String getKeyName(int key) {
        if (key >= 0 && key < MAX_KEY) return keyNames[key];
        return "UNKNOWN";
    }

    public static int getKeyIndex(String name) {
        Integer code = nameToKey.get(name);
        return code != null ? code : KEY_NONE;
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
        return (currentEvent != null) ? currentEvent.key : KEY_NONE;
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
