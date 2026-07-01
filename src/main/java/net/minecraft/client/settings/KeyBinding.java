package net.minecraft.client.settings;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IntHashMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

public class KeyBinding implements Comparable<KeyBinding> {

	private static final Map<String, KeyBinding> KEYBIND_ARRAY = new HashMap<>();
	private static final IntHashMap<KeyBinding> HASH = new IntHashMap<>();
	private static final Set<String> KEYBIND_SET = new HashSet<>();
	private static final Map<String, Integer> CATEGORY_ORDER = new HashMap<>();

	static {
		CATEGORY_ORDER.put("key.categories.movement", 1);
		CATEGORY_ORDER.put("key.categories.gameplay", 2);
		CATEGORY_ORDER.put("key.categories.inventory", 3);
		CATEGORY_ORDER.put("key.categories.creative", 4);
		CATEGORY_ORDER.put("key.categories.multiplayer", 5);
		CATEGORY_ORDER.put("key.categories.ui", 6);
		CATEGORY_ORDER.put("key.categories.misc", 7);
	}

	@Getter
	private final String description;
	
	@Getter
	private final int defaultKeyCode;
	
	@Getter
	private final String category;
	
	@Setter
	@Getter
	private int keyCode;
	/**
	 * Is the key held down?
	 */
	private boolean pressed;
	private int pressTime;

	public KeyBinding(String description, int keyCode, String category) {
		this.description = description;
		this.keyCode = keyCode;
		defaultKeyCode = keyCode;
		this.category = category;
		KEYBIND_ARRAY.put(description, this);
		HASH.addKey(keyCode, this);
		KEYBIND_SET.add(category);
	}

	public static void onTick(int keyCode) {
		if (keyCode != GLFW_KEY_UNKNOWN) {
			KeyBinding key = HASH.lookup(keyCode);

			if (key != null) key.pressTime++;
		}
	}

	public static void setKeyBindState(int keyCode, boolean pressed) {
		if (keyCode != GLFW_KEY_UNKNOWN) {
			KeyBinding key = HASH.lookup(keyCode);

			if (key != null) key.pressed = pressed;
		}
	}

	/**
	 * Completely recalculates whether any keybinds are held, from scratch.
	 */
	public static void updateKeyBindState() {
		for (KeyBinding key : KEYBIND_ARRAY.values()) {
			try {
				setKeyBindState(key.keyCode, GameSettings.isKeyDown(key));
			} catch (IndexOutOfBoundsException ignored) {
			}
		}
	}

	public static void unPressAllKeys() {
		for (KeyBinding key : KEYBIND_ARRAY.values()) {
			key.unpressKey();
		}
	}

	public static void resetKeyBindingArrayAndHash() {
		HASH.clearMap();

		for (KeyBinding key : KEYBIND_ARRAY.values()) {
			HASH.addKey(key.keyCode, key);
		}
	}

	public static Set<String> getKeybinds() {
		return KEYBIND_SET;
	}

	public static Supplier<String> getDisplayString(String key) {
		KeyBinding keybinding = KEYBIND_ARRAY.get(key);
		return keybinding == null ? () -> key : () -> GameSettings.getKeyDisplayString(keybinding.getKeyCode());
	}

	/**
	 * Returns true if the key is pressed (used for continuous querying). Should be used in tickers.
	 */
	public boolean isKeyDown() {
		return pressed;
	}
	
	public boolean isPressed() {
		if (pressTime == 0) {
			return false;
		} else {
			pressTime--;
			return true;
		}
	}

	private void unpressKey() {
		pressTime = 0;
		pressed = false;
	}
	
	public int compareTo(KeyBinding other) {
		return category.equals(other.category) ?
				I18n.format(description).compareTo(I18n.format(other.description)) :
				CATEGORY_ORDER.get(category).compareTo(CATEGORY_ORDER.get(other.category));
	}
}
