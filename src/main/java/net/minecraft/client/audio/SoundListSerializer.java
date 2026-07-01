package net.minecraft.client.audio;

import com.google.common.collect.Lists;
import com.google.gson.*;
import net.minecraft.util.JsonUtils;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Type;
import java.util.List;

public class SoundListSerializer implements JsonDeserializer<SoundList> {

	public SoundList deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = JsonUtils.getJsonObject(json, "entry");
		boolean flag = JsonUtils.getBoolean(object, "replace", false);
		String s = JsonUtils.getString(object, "subtitle", null);
		List<Sound> list = deserializeSounds(object);
		return new SoundList(list, flag, s);
	}

	private List<Sound> deserializeSounds(JsonObject object) {
		List<Sound> list = Lists.newArrayList();

		if (object.has("sounds")) {
			JsonArray jsonarray = JsonUtils.getJsonArray(object, "sounds");

			for (int i = 0; i < jsonarray.size(); ++i) {
				JsonElement jsonelement = jsonarray.get(i);

				if (JsonUtils.isString(jsonelement)) {
					String s = JsonUtils.getString(jsonelement, "sound");
					list.add(new Sound(s, 1F, 1F, 1, Sound.Type.FILE, false));
				} else {
					list.add(deserializeSound(JsonUtils.getJsonObject(jsonelement, "sound")));
				}
			}
		}

		return list;
	}

	private Sound deserializeSound(JsonObject object) {
		String s = JsonUtils.getString(object, "name");
		Sound.Type sound$type = deserializeType(object);
		float f = JsonUtils.getFloat(object, "volume", 1F);
		Validate.isTrue(f > 0F, "Invalid volume");
		float f1 = JsonUtils.getFloat(object, "pitch", 1F);
		Validate.isTrue(f1 > 0F, "Invalid pitch");
		int i = JsonUtils.getInt(object, "weight", 1);
		Validate.isTrue(i > 0, "Invalid weight");
		boolean flag = JsonUtils.getBoolean(object, "stream", false);
		return new Sound(s, f, f1, i, sound$type, flag);
	}

	private Sound.Type deserializeType(JsonObject object) {
		Sound.Type type = Sound.Type.FILE;

		if (object.has("type")) {
			type = Sound.Type.getByName(JsonUtils.getString(object, "type"));
			Validate.notNull(type, "Invalid type");
		}

		return type;
	}

}
