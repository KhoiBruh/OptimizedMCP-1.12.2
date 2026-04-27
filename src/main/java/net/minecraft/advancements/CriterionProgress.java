package net.minecraft.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.PacketBuffer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CriterionProgress {

	private static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
	private final AdvancementProgress advancementProgress;
	private Date obtained;

	public CriterionProgress(AdvancementProgress advancementProgressIn) {

		advancementProgress = advancementProgressIn;
	}

	public boolean isObtained() {

		return obtained != null;
	}

	public void obtain() {

		obtained = new Date();
	}

	public void reset() {

		obtained = null;
	}

	public Date getObtained() {

		return obtained;
	}

	public String toString() {

		return "CriterionProgress{obtained=" + (obtained == null ? "false" : obtained) + '}';
	}

	public void write(PacketBuffer buf) {

		buf.writeBoolean(obtained != null);

		if (obtained != null) {
			buf.writeTime(obtained);
		}
	}

	public JsonElement serialize() {

		return obtained != null ? new JsonPrimitive(DATE_TIME_FORMATTER.format(obtained)) : JsonNull.INSTANCE;
	}

	public static CriterionProgress read(PacketBuffer buf, AdvancementProgress advancementProgressIn) {

		CriterionProgress criterionprogress = new CriterionProgress(advancementProgressIn);

		if (buf.readBoolean()) {
			criterionprogress.obtained = buf.readTime();
		}

		return criterionprogress;
	}

	public static CriterionProgress fromDateTime(AdvancementProgress advancementProgressIn, String dateTime) {

		CriterionProgress criterionprogress = new CriterionProgress(advancementProgressIn);

		try {
			criterionprogress.obtained = DATE_TIME_FORMATTER.parse(dateTime);
			return criterionprogress;
		} catch (ParseException parseexception) {
			throw new JsonSyntaxException("Invalid datetime: " + dateTime, parseexception);
		}
	}

}
