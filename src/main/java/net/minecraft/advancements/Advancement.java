package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class Advancement {

	private final Advancement parent;
	private final DisplayInfo display;
	private final AdvancementRewards rewards;
	private final ResourceLocation id;
	private final Map<String, Criterion> criteria;
	private final String[][] requirements;
	private final Set<Advancement> children = Sets.newLinkedHashSet();
	private final ITextComponent displayText;

	public Advancement(ResourceLocation id, @Nullable Advancement parentIn, @Nullable DisplayInfo displayIn, AdvancementRewards rewardsIn, Map<String, Criterion> criteriaIn, String[][] requirementsIn) {

		this.id = id;
		display = displayIn;
		criteria = ImmutableMap.copyOf(criteriaIn);
		parent = parentIn;
		rewards = rewardsIn;
		requirements = requirementsIn;

		if (parentIn != null) {
			parentIn.addChild(this);
		}

		if (displayIn == null) {
			displayText = new TextComponentString(id.toString());
		} else {
			displayText = new TextComponentString("[");
			displayText.getStyle().setColor(displayIn.getFrame().getFormat());
			ITextComponent itextcomponent = displayIn.getTitle().createCopy();
			ITextComponent itextcomponent1 = new TextComponentString("");
			ITextComponent itextcomponent2 = itextcomponent.createCopy();
			itextcomponent2.getStyle().setColor(displayIn.getFrame().getFormat());
			itextcomponent1.appendSibling(itextcomponent2);
			itextcomponent1.appendText("\n");
			itextcomponent1.appendSibling(displayIn.getDescription());
			itextcomponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, itextcomponent1));
			displayText.appendSibling(itextcomponent);
			displayText.appendText("]");
		}
	}

	/**
	 * Creates a new advancement builder with the data from this advancement
	 */
	public Advancement.Builder copy() {

		return new Advancement.Builder(parent == null ? null : parent.getId(), display, rewards, criteria, requirements);
	}

	@Nullable

	/**
	 * Get the {@code Advancement} that is this {@code Advancement}'s parent. This determines the tree structure that
	 * appears in the {@linkplain GuiScreenAdvancements GUI}.
	 * @return the parent {@code Advancement} of this {@code Advancement}, or {@code null} to signify that this {@code
	 * Advancement} is a root with no parent.
	 */
	public Advancement getParent() {

		return parent;
	}

	@Nullable

	/**
	 * Get information that defines this {@code Advancement}'s appearance in GUIs.
	 * @return information that defines this {@code Advancement}'s appearance in GUIs. If {@code null}, signifies an
	 * invisible {@code Advancement}.
	 */
	public DisplayInfo getDisplay() {

		return display;
	}

	public AdvancementRewards getRewards() {

		return rewards;
	}

	public String toString() {

		return "SimpleAdvancement{id=" + getId() + ", parent=" + (parent == null ? "null" : parent.getId()) + ", display=" + display + ", rewards=" + rewards + ", criteria=" + criteria + ", requirements=" + Arrays.deepToString(requirements) + '}';
	}

	public Iterable<Advancement> getChildren() {

		return children;
	}

	public Map<String, Criterion> getCriteria() {

		return criteria;
	}

	/**
	 * Get how many requirements this {@code Advancement} has.
	 *
	 * @return {@code this.getRequirements().length}
	 * @see #getRequirements()
	 */
	public int getRequirementCount() {

		return requirements.length;
	}

	/**
	 * Add the given {@code Advancement} as a child of this {@code Advancement}.
	 *
	 * @see #getParent()
	 */
	public void addChild(Advancement advancementIn) {

		children.add(advancementIn);
	}

	/**
	 * Get this {@code Advancement}'s unique identifier.
	 *
	 * @return this {@code Advancement}'s unique identifier
	 */
	public ResourceLocation getId() {

		return id;
	}

	public boolean equals(Object p_equals_1_) {

		if (this == p_equals_1_) {
			return true;
		} else if (!(p_equals_1_ instanceof Advancement advancement)) {
			return false;
		} else {
			return id.equals(advancement.id);
		}
	}

	public int hashCode() {

		return id.hashCode();
	}

	public String[][] getRequirements() {

		return requirements;
	}

	/**
	 * Returns the {@code ITextComponent} that is shown in the chat message sent after this {@code Advancement} is
	 * completed.
	 *
	 * @return the {@code ITextComponent} that is shown in the chat message sent after this {@code Advancement} is
	 * completed. If this {@code Advancement} is {@linkplain #getDisplay() invisible}, then it consists simply of {@link
	 * #getId()}. Otherwise, it is the {@linkplain DisplayInfo#getTitle() title} inside square brackets, colored by the
	 * {@linkplain net.minecraft.advancements.FrameType#getFormat frame type}, and hovering over it shows the
	 * {@linkplain DisplayInfo#getDescription() description}.
	 */
	public ITextComponent getDisplayText() {

		return displayText;
	}

	public static class Builder {

		private final ResourceLocation parentId;
		private final DisplayInfo display;
		private final AdvancementRewards rewards;
		private final Map<String, Criterion> criteria;
		private final String[][] requirements;
		private Advancement parent;

		Builder(@Nullable ResourceLocation p_i47414_1_, @Nullable DisplayInfo p_i47414_2_, AdvancementRewards p_i47414_3_, Map<String, Criterion> p_i47414_4_, String[][] p_i47414_5_) {

			parentId = p_i47414_1_;
			display = p_i47414_2_;
			rewards = p_i47414_3_;
			criteria = p_i47414_4_;
			requirements = p_i47414_5_;
		}

		public static Advancement.Builder deserialize(JsonObject json, JsonDeserializationContext context) {

			ResourceLocation resourcelocation = json.has("parent") ? new ResourceLocation(JsonUtils.getString(json, "parent")) : null;
			DisplayInfo displayinfo = json.has("display") ? DisplayInfo.deserialize(JsonUtils.getJsonObject(json, "display"), context) : null;
			AdvancementRewards advancementrewards = JsonUtils.deserializeClass(json, "rewards", AdvancementRewards.EMPTY, context, AdvancementRewards.class);
			Map<String, Criterion> map = Criterion.criteriaFromJson(JsonUtils.getJsonObject(json, "criteria"), context);

			if (map.isEmpty()) {
				throw new JsonSyntaxException("Advancement criteria cannot be empty");
			} else {
				JsonArray jsonarray = JsonUtils.getJsonArray(json, "requirements", new JsonArray());
				String[][] astring = new String[jsonarray.size()][];

				for (int i = 0; i < jsonarray.size(); ++i) {
					JsonArray jsonarray1 = JsonUtils.getJsonArray(jsonarray.get(i), "requirements[" + i + "]");
					astring[i] = new String[jsonarray1.size()];

					for (int j = 0; j < jsonarray1.size(); ++j) {
						astring[i][j] = JsonUtils.getString(jsonarray1.get(j), "requirements[" + i + "][" + j + "]");
					}
				}

				if (astring.length == 0) {
					astring = new String[map.size()][];
					int k = 0;

					for (String s2 : map.keySet()) {
						astring[k++] = new String[]{s2};
					}
				}

				for (String[] astring1 : astring) {
					if (astring1.length == 0 && map.isEmpty()) {
						throw new JsonSyntaxException("Requirement entry cannot be empty");
					}

					for (String s : astring1) {
						if (!map.containsKey(s)) {
							throw new JsonSyntaxException("Unknown required criterion '" + s + "'");
						}
					}
				}

				for (String s1 : map.keySet()) {
					boolean flag = false;

					for (String[] astring2 : astring) {
						if (ArrayUtils.contains(astring2, s1)) {
							flag = true;
							break;
						}
					}

					if (!flag) {
						throw new JsonSyntaxException("Criterion '" + s1 + "' isn't a requirement for completion. This isn't supported behaviour, all criteria must be required.");
					}
				}

				return new Advancement.Builder(resourcelocation, displayinfo, advancementrewards, map, astring);
			}
		}

		public static Advancement.Builder readFrom(PacketBuffer buf) throws IOException {

			ResourceLocation resourcelocation = buf.readBoolean() ? buf.readResourceLocation() : null;
			DisplayInfo displayinfo = buf.readBoolean() ? DisplayInfo.read(buf) : null;
			Map<String, Criterion> map = Criterion.criteriaFromNetwork(buf);
			String[][] astring = new String[buf.readVarInt()][];

			for (int i = 0; i < astring.length; ++i) {
				astring[i] = new String[buf.readVarInt()];

				for (int j = 0; j < astring[i].length; ++j) {
					astring[i][j] = buf.readString(32767);
				}
			}

			return new Advancement.Builder(resourcelocation, displayinfo, AdvancementRewards.EMPTY, map, astring);
		}

		public boolean resolveParent(Function<ResourceLocation, Advancement> lookup) {

			if (parentId == null) {
				return true;
			} else {
				parent = lookup.apply(parentId);
				return parent != null;
			}
		}

		public Advancement build(ResourceLocation id) {

			return new Advancement(id, parent, display, rewards, criteria, requirements);
		}

		public void writeTo(PacketBuffer buf) {

			if (parentId == null) {
				buf.writeBoolean(false);
			} else {
				buf.writeBoolean(true);
				buf.writeResourceLocation(parentId);
			}

			if (display == null) {
				buf.writeBoolean(false);
			} else {
				buf.writeBoolean(true);
				display.write(buf);
			}

			Criterion.serializeToNetwork(criteria, buf);
			buf.writeVarInt(requirements.length);

			for (String[] astring : requirements) {
				buf.writeVarInt(astring.length);

				for (String s : astring) {
					buf.writeString(s);
				}
			}
		}

		public String toString() {

			return "Task Advancement{parentId=" + parentId + ", display=" + display + ", rewards=" + rewards + ", criteria=" + criteria + ", requirements=" + Arrays.deepToString(requirements) + '}';
		}

	}

}
