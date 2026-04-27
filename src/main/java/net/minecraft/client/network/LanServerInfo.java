package net.minecraft.client.network;

import net.minecraft.client.Minecraft;

public class LanServerInfo {

	private final String lanServerMotd;
	private final String lanServerIpPort;

	/**
	 * Last time this LanServer was seen.
	 */
	private long timeLastSeen;

	public LanServerInfo(String p_i47130_1_, String p_i47130_2_) {

		lanServerMotd = p_i47130_1_;
		lanServerIpPort = p_i47130_2_;
		timeLastSeen = Minecraft.getSystemTime();
	}

	public String getServerMotd() {

		return lanServerMotd;
	}

	public String getServerIpPort() {

		return lanServerIpPort;
	}

	/**
	 * Updates the time this LanServer was last seen.
	 */
	public void updateLastSeen() {

		timeLastSeen = Minecraft.getSystemTime();
	}

}
