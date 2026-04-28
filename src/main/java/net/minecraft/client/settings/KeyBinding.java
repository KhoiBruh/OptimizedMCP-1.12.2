package net.minecraft.client.settings;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IntHashMap;
import org.lwjgl.input.Keyboard;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class KeyBinding implements Comparable<KeyBinding> {

	private static final Map<String, KeyBinding> KEYBIND_ARRAY = Maps.newHashMap();
	private static final IntHashMap<KeyBinding> HASH = new IntHashMap<>();
	private static final Set<String> KEYBIND_SET = Sets.newHashSet();
	private static final Map<String, Integer> CATEGORY_ORDER = Maps.newHashMap();

	static {
		CATEGORY_ORDER.put("key.categories.movement", 1);
		CATEGORY_ORDER.put("key.categories.gameplay", 2);
		CATEGORY_ORDER.put("key.categories.inventory", 3);
		CATEGORY_ORDER.put("key.categories.creative", 4);
		CATEGORY_ORDER.put("key.categories.multiplayer", 5);
		CATEGORY_ORDER.put("key.categories.ui", 6);
		CATEGORY_ORDER.put("key.categories.misc", 7);
	}

	private final String keyDescription;
	private final int keyCodeDefault;
	private final String keyCategory;
	private int keyCode;
	/**
	 * Is the key held down?
	 */
	private boolean pressed;
	private int pressTime;

	public KeyBinding(String description, int keyCode, String category) {

		keyDescription = description;
		this.keyCode = keyCode;
		keyCodeDefault = keyCode;
		keyCategory = category;
		KEYBIND_ARRAY.put(description, this);
		HASH.addKey(keyCode, this);
		KEYBIND_SET.add(category);
	}

	public static void onTick(int keyCode) {

		if (keyCode != 0) {
			KeyBinding keybinding = HASH.lookup(keyCode);

			if (keybinding != null) {
				++keybinding.pressTime;
			}
		}
	}

	public static void setKeyBindState(int keyCode, boolean pressed) {

		if (keyCode != 0) {
			KeyBinding keybinding = HASH.lookup(keyCode);

			if (keybinding != null) {
				keybinding.pressed = pressed;
			}
		}
	}

	/**
	 * Completely recalculates whether any keybinds are held, from scratch.
	 */
	public static void updateKeyBindState() {

		for (KeyBinding keybinding : KEYBIND_ARRAY.values()) {
			try {
				setKeyBindState(keybinding.keyCode, keybinding.keyCode < 256 && Keyboard.isKeyDown(keybinding.keyCode));
			} catch (IndexOutOfBoundsException ignored) {
			}
		}
	}

	public static void unPressAllKeys() {

		for (KeyBinding keybinding : KEYBIND_ARRAY.values()) {
			keybinding.unpressKey();
		}
	}

	public static void resetKeyBindingArrayAndHash() {

		HASH.clearMap();

		for (KeyBinding keybinding : KEYBIND_ARRAY.values()) {
			HASH.addKey(keybinding.keyCode, keybinding);
		}
	}

	public static Set<String> getKeybinds() {

		return KEYBIND_SET;
	}

	public static Supplier<String> getDisplayString(String key) {

		KeyBinding keybinding = KEYBIND_ARRAY.get(key);
		return keybinding == null ? () ->
				key : () ->
				GameSettings.getKeyDisplayString(keybinding.getKeyCode());
	}

	/**
	 * Returns true if the key is pressed (used for continuous querying). Should be used in tickers.
	 */
	public boolean isKeyDown() {

		return pressed;
	}

	public String getKeyCategory() {

		return keyCategory;
	}

	/**
	 * Returns true on the initial key press. For continuous querying use {@link isKeyDown()}. Should be used in key
	 * events.
	 */
	public boolean isPressed() {

		if (pressTime == 0) {
			return false;
		} else {
			--pressTime;
			return true;
		}
	}

	private void unpressKey() {

		pressTime = 0;
		pressed = false;
	}

	public String getKeyDescription() {

		return keyDescription;
	}

	public int getKeyCodeDefault() {

		return keyCodeDefault;
	}

	public int getKeyCode() {

		return keyCode;
	}

	public void setKeyCode(int keyCode) {

		this.keyCode = keyCode;
	}

	public int compareTo(KeyBinding p_compareTo_1_) {

		return keyCategory.equals(p_compareTo_1_.keyCategory) ? I18n.format(keyDescription).compareTo(I18n.format(p_compareTo_1_.keyDescription)) : CATEGORY_ORDER.get(keyCategory).compareTo(CATEGORY_ORDER.get(p_compareTo_1_.keyCategory));
	}
}
