package net.minecraft.client.renderer.block.model;

import com.google.gson.*;
import net.minecraft.util.Facing;
import net.minecraft.util.JsonUtils;
import java.lang.reflect.Type;

public record BlockPartFace(Facing cullFace, int tintIndex, String texture, BlockFaceUV blockFaceUV) {

	public static final Facing FACING_DEFAULT = null;

	public BlockPartFace(Facing cullFace, int tintIndex, String texture, BlockFaceUV blockFaceUV) {

		this.cullFace = cullFace;
		this.tintIndex = tintIndex;
		this.texture = texture;
		this.blockFaceUV = blockFaceUV;
	}

	static class Deserializer implements JsonDeserializer<BlockPartFace> {

		public BlockPartFace deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {

			JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
			Facing enumfacing = parseCullFace(jsonobject);
			int i = parseTintIndex(jsonobject);
			String s = parseTexture(jsonobject);
			BlockFaceUV blockfaceuv = p_deserialize_3_.deserialize(jsonobject, BlockFaceUV.class);
			return new BlockPartFace(enumfacing, i, s, blockfaceuv);
		}

		protected int parseTintIndex(JsonObject object) {

			return JsonUtils.getInt(object, "tintindex", -1);
		}

		private String parseTexture(JsonObject object) {

			return JsonUtils.getString(object, "texture");
		}

		
		private Facing parseCullFace(JsonObject object) {

			String s = JsonUtils.getString(object, "cullface", "");
			return Facing.byName(s);
		}

	}

}
