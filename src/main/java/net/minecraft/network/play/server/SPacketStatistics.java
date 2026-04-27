package net.minecraft.network.play.server;

import com.google.common.collect.Maps;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;

import java.util.Map;
import java.util.Map.Entry;

public class SPacketStatistics implements Packet<INetHandlerPlayClient> {

	private Map<StatBase, Integer> statisticMap;

	public SPacketStatistics() {

	}

	public SPacketStatistics(Map<StatBase, Integer> statisticMapIn) {

		statisticMap = statisticMapIn;
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerPlayClient handler) {

		handler.handleStatistics(this);
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {

		int i = buf.readVarInt();
		statisticMap = Maps.newHashMap();

		for (int j = 0; j < i; ++j) {
			StatBase statbase = StatList.getOneShotStat(buf.readString(32767));
			int k = buf.readVarInt();

			if (statbase != null) {
				statisticMap.put(statbase, k);
			}
		}
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {

		buf.writeVarInt(statisticMap.size());

		for (Entry<StatBase, Integer> entry : statisticMap.entrySet()) {
			buf.writeString((entry.getKey()).statId);
			buf.writeVarInt(entry.getValue());
		}
	}

	public Map<StatBase, Integer> getStatisticMap() {

		return statisticMap;
	}

}
