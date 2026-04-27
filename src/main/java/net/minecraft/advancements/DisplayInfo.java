package net.minecraft.advancements;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.io.IOException;

public class DisplayInfo {

	private final ITextComponent title;
	private final ITextComponent description;
	private final ItemStack icon;
	private final ResourceLocation background;
	private final FrameType frame;
	private final boolean showToast;
	private final boolean announceToChat;
	private final boolean hidden;
	private float x;
	private float y;

	public DisplayInfo(ItemStack icon, ITextComponent title, ITextComponent description, @Nullable ResourceLocation background, FrameType frame, boolean showToast, boolean announceToChat, boolean hidden) {

		this.title = title;
		this.description = description;
		this.icon = icon;
		this.background = background;
		this.frame = frame;
		this.showToast = showToast;
		this.announceToChat = announceToChat;
		this.hidden = hidden;
	}

	public static DisplayInfo deserialize(JsonObject object, JsonDeserializationContext context) {

		ITextComponent itextcomponent = JsonUtils.deserializeClass(object, "title", context, ITextComponent.class);
		ITextComponent itextcomponent1 = JsonUtils.deserializeClass(object, "description", context, ITextComponent.class);

		if (itextcomponent != null && itextcomponent1 != null) {
			ItemStack itemstack = deserializeIcon(JsonUtils.getJsonObject(object, "icon"));
			ResourceLocation resourcelocation = object.has("background") ? new ResourceLocation(JsonUtils.getString(object, "background")) : null;
			FrameType frametype = object.has("frame") ? FrameType.byName(JsonUtils.getString(object, "frame")) : FrameType.TASK;
			boolean flag = JsonUtils.getBoolean(object, "show_toast", true);
			boolean flag1 = JsonUtils.getBoolean(object, "announce_to_chat", true);
			boolean flag2 = JsonUtils.getBoolean(object, "hidden", false);
			return new DisplayInfo(itemstack, itextcomponent, itextcomponent1, resourcelocation, frametype, flag, flag1, flag2);
		} else {
			throw new JsonSyntaxException("Both title and description must be set");
		}
	}

	private static ItemStack deserializeIcon(JsonObject object) {

		if (!object.has("item")) {
			throw new JsonSyntaxException("Unsupported icon type, currently only items are supported (add 'item' key)");
		} else {
			Item item = JsonUtils.getItem(object, "item");
			int i = JsonUtils.getInt(object, "data", 0);
			return new ItemStack(item, 1, i);
		}
	}

	public static DisplayInfo read(PacketBuffer buf) throws IOException {

		ITextComponent itextcomponent = buf.readTextComponent();
		ITextComponent itextcomponent1 = buf.readTextComponent();
		ItemStack itemstack = buf.readItemStack();
		FrameType frametype = buf.readEnumValue(FrameType.class);
		int i = buf.readInt();
		ResourceLocation resourcelocation = (i & 1) != 0 ? buf.readResourceLocation() : null;
		boolean flag = (i & 2) != 0;
		boolean flag1 = (i & 4) != 0;
		DisplayInfo displayinfo = new DisplayInfo(itemstack, itextcomponent, itextcomponent1, resourcelocation, frametype, flag, false, flag1);
		displayinfo.setPosition(buf.readFloat(), buf.readFloat());
		return displayinfo;
	}

	public void setPosition(float x, float y) {

		this.x = x;
		this.y = y;
	}

	public ITextComponent getTitle() {

		return title;
	}

	public ITextComponent getDescription() {

		return description;
	}

	public ItemStack getIcon() {

		return icon;
	}

	@Nullable
	public ResourceLocation getBackground() {

		return background;
	}

	public FrameType getFrame() {

		return frame;
	}

	public float getX() {

		return x;
	}

	public float getY() {

		return y;
	}

	public boolean shouldShowToast() {

		return showToast;
	}

	public boolean shouldAnnounceToChat() {

		return announceToChat;
	}

	public boolean isHidden() {

		return hidden;
	}

	public void write(PacketBuffer buf) {

		buf.writeTextComponent(title);
		buf.writeTextComponent(description);
		buf.writeItemStack(icon);
		buf.writeEnumValue(frame);
		int i = 0;

		if (background != null) {
			i |= 1;
		}

		if (showToast) {
			i |= 2;
		}

		if (hidden) {
			i |= 4;
		}

		buf.writeInt(i);

		if (background != null) {
			buf.writeResourceLocation(background);
		}

		buf.writeFloat(x);
		buf.writeFloat(y);
	}

}
