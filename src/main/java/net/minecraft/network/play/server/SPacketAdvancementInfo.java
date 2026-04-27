package net.minecraft.network.play.server;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SPacketAdvancementInfo implements Packet<INetHandlerPlayClient> {

	private boolean firstSync;
	private Map<ResourceLocation, Advancement.Builder> advancementsToAdd;
	private Set<ResourceLocation> advancementsToRemove;
	private Map<ResourceLocation, AdvancementProgress> progressUpdates;

	public SPacketAdvancementInfo() {

	}

	public SPacketAdvancementInfo(boolean p_i47519_1_, Collection<Advancement> p_i47519_2_, Set<ResourceLocation> p_i47519_3_, Map<ResourceLocation, AdvancementProgress> p_i47519_4_) {

		firstSync = p_i47519_1_;
		advancementsToAdd = Maps.newHashMap();

		for (Advancement advancement : p_i47519_2_) {
			advancementsToAdd.put(advancement.getId(), advancement.copy());
		}

		advancementsToRemove = p_i47519_3_;
		progressUpdates = Maps.newHashMap(p_i47519_4_);
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handleAdvancementInfo(this);
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) throws IOException {

		firstSync = buf.readBoolean();
		advancementsToAdd = Maps.newHashMap();
		advancementsToRemove = Sets.newLinkedHashSet();
		progressUpdates = Maps.newHashMap();
		int i = buf.readVarInt();

		for (int j = 0; j < i; ++j) {
			ResourceLocation resourcelocation = buf.readResourceLocation();
			Advancement.Builder advancement$builder = Advancement.Builder.readFrom(buf);
			advancementsToAdd.put(resourcelocation, advancement$builder);
		}

		i = buf.readVarInt();

		for (int k = 0; k < i; ++k) {
			ResourceLocation resourcelocation1 = buf.readResourceLocation();
			advancementsToRemove.add(resourcelocation1);
		}

		i = buf.readVarInt();

		for (int l = 0; l < i; ++l) {
			ResourceLocation resourcelocation2 = buf.readResourceLocation();
			progressUpdates.put(resourcelocation2, AdvancementProgress.fromNetwork(buf));
		}
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) throws IOException {

		buf.writeBoolean(firstSync);
		buf.writeVarInt(advancementsToAdd.size());

		for (Entry<ResourceLocation, Advancement.Builder> entry : advancementsToAdd.entrySet()) {
			ResourceLocation resourcelocation = entry.getKey();
			Advancement.Builder advancement$builder = entry.getValue();
			buf.writeResourceLocation(resourcelocation);
			advancement$builder.writeTo(buf);
		}

		buf.writeVarInt(advancementsToRemove.size());

		for (ResourceLocation resourcelocation1 : advancementsToRemove) {
			buf.writeResourceLocation(resourcelocation1);
		}

		buf.writeVarInt(progressUpdates.size());

		for (Entry<ResourceLocation, AdvancementProgress> entry1 : progressUpdates.entrySet()) {
			buf.writeResourceLocation(entry1.getKey());
			entry1.getValue().serializeToNetwork(buf);
		}
	}

	public Map<ResourceLocation, Advancement.Builder> getAdvancementsToAdd() {

		return advancementsToAdd;
	}

	public Set<ResourceLocation> getAdvancementsToRemove() {

		return advancementsToRemove;
	}

	public Map<ResourceLocation, AdvancementProgress> getProgressUpdates() {

		return progressUpdates;
	}

	public boolean isFirstSync() {

		return firstSync;
	}

}
