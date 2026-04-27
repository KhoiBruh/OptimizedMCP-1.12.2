package net.minecraft.util.text.event;

import com.google.common.collect.Maps;
import net.minecraft.util.text.ITextComponent;

import java.util.Map;

public record HoverEvent(Action action, ITextComponent value) {

	/**
	 * Gets the action to perform when this event is raised.
	 */
	@Override
	public Action action() {

		return action;
	}

	/**
	 * Gets the value to perform the action on when this event is raised.  For example, if the action is "show item",
	 * this would be the item to show.
	 */
	@Override
	public ITextComponent value() {

		return value;
	}

	public boolean equals(Object p_equals_1_) {

		if (this == p_equals_1_) {
			return true;
		} else if (p_equals_1_ != null && getClass() == p_equals_1_.getClass()) {
			HoverEvent hoverevent = (HoverEvent) p_equals_1_;

			if (action != hoverevent.action) {
				return false;
			} else {
				if (value != null) {
					return value.equals(hoverevent.value);
				} else return hoverevent.value == null;
			}
		} else {
			return false;
		}
	}

	public String toString() {

		return "HoverEvent{action=" + action + ", value='" + value + '\'' + '}';
	}

	public enum Action {
		SHOW_TEXT("show_text", true),
		SHOW_ITEM("show_item", true),
		SHOW_ENTITY("show_entity", true);

		private static final Map<String, Action> NAME_MAPPING = Maps.newHashMap();

		static {
			for (Action hoverevent$action : values()) {
				NAME_MAPPING.put(hoverevent$action.getCanonicalName(), hoverevent$action);
			}
		}

		private final boolean allowedInChat;
		private final String canonicalName;

		Action(String canonicalNameIn, boolean allowedInChatIn) {

			canonicalName = canonicalNameIn;
			allowedInChat = allowedInChatIn;
		}

		public static Action getValueByCanonicalName(String canonicalNameIn) {

			return NAME_MAPPING.get(canonicalNameIn);
		}

		public boolean shouldAllowInChat() {

			return allowedInChat;
		}

		public String getCanonicalName() {

			return canonicalName;
		}
	}

}
