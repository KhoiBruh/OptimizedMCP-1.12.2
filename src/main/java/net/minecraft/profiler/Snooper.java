package net.minecraft.profiler;

import com.google.common.collect.Maps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.HttpUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

public class Snooper {

	private final Map<String, Object> snooperStats = Maps.newHashMap();
	private final Map<String, Object> clientStats = Maps.newHashMap();
	private final String uniqueID = UUID.randomUUID().toString();

	/**
	 * URL of the server to send the report to
	 */
	private final URL serverUrl;
	private final ISnooperInfo playerStatsCollector;

	/**
	 * set to fire the snooperThread every 15 mins
	 */
	private final Timer threadTrigger = new Timer("Snooper Timer", true);
	private final Object syncLock = new Object();
	private final long minecraftStartTimeMilis;
	private boolean isRunning;

	/**
	 * incremented on every getSelfCounterFor
	 */
	private int selfCounter;

	public Snooper(String side, ISnooperInfo playerStatCollector, long startTime) {

		try {
			serverUrl = new URL("http://snoop.minecraft.net/" + side + "?version=" + 2);
		} catch (MalformedURLException var6) {
			throw new IllegalArgumentException();
		}

		playerStatsCollector = playerStatCollector;
		minecraftStartTimeMilis = startTime;
	}

	/**
	 * Note issuing start multiple times is not an error.
	 */
	public void startSnooper() {

		if (!isRunning) {
			isRunning = true;
			addOSData();
			threadTrigger.schedule(new TimerTask() {
				public void run() {

					if (playerStatsCollector.isSnooperEnabled()) {
						Map<String, Object> map;

						synchronized (syncLock) {
							map = Maps.newHashMap(clientStats);

							if (selfCounter == 0) {
								map.putAll(snooperStats);
							}

							map.put("snooper_count", Integer.valueOf(selfCounter++));
							map.put("snooper_token", uniqueID);
						}

						MinecraftServer minecraftserver = playerStatsCollector instanceof MinecraftServer ? (MinecraftServer) playerStatsCollector : null;
						HttpUtil.postMap(serverUrl, map, true, minecraftserver == null ? null : minecraftserver.getServerProxy());
					}
				}
			}, 0L, 900000L);
		}
	}

	/**
	 * Add OS data into the snooper
	 */
	private void addOSData() {

		addJvmArgsToSnooper();
		addClientStat("snooper_token", uniqueID);
		addStatToSnooper("snooper_token", uniqueID);
		addStatToSnooper("os_name", System.getProperty("os.name"));
		addStatToSnooper("os_version", System.getProperty("os.version"));
		addStatToSnooper("os_architecture", System.getProperty("os.arch"));
		addStatToSnooper("java_version", System.getProperty("java.version"));
		addClientStat("version", "1.12.2");
		playerStatsCollector.addServerTypeToSnooper(this);
	}

	private void addJvmArgsToSnooper() {

		RuntimeMXBean runtimemxbean = ManagementFactory.getRuntimeMXBean();
		List<String> list = runtimemxbean.getInputArguments();
		int i = 0;

		for (String s : list) {
			if (s.startsWith("-X")) {
				addClientStat("jvm_arg[" + i++ + "]", s);
			}
		}

		addClientStat("jvm_args", Integer.valueOf(i));
	}

	public void addMemoryStatsToSnooper() {

		addStatToSnooper("memory_total", Long.valueOf(Runtime.getRuntime().totalMemory()));
		addStatToSnooper("memory_max", Long.valueOf(Runtime.getRuntime().maxMemory()));
		addStatToSnooper("memory_free", Long.valueOf(Runtime.getRuntime().freeMemory()));
		addStatToSnooper("cpu_cores", Integer.valueOf(Runtime.getRuntime().availableProcessors()));
		playerStatsCollector.addServerStatsToSnooper(this);
	}

	public void addClientStat(String statName, Object statValue) {

		synchronized (syncLock) {
			clientStats.put(statName, statValue);
		}
	}

	public void addStatToSnooper(String statName, Object statValue) {

		synchronized (syncLock) {
			snooperStats.put(statName, statValue);
		}
	}

	public Map<String, String> getCurrentStats() {

		Map<String, String> map = Maps.newLinkedHashMap();

		synchronized (syncLock) {
			addMemoryStatsToSnooper();

			for (Entry<String, Object> entry : snooperStats.entrySet()) {
				map.put(entry.getKey(), entry.getValue().toString());
			}

			for (Entry<String, Object> entry1 : clientStats.entrySet()) {
				map.put(entry1.getKey(), entry1.getValue().toString());
			}

			return map;
		}
	}

	public boolean isSnooperRunning() {

		return isRunning;
	}

	public void stopSnooper() {

		threadTrigger.cancel();
	}

	public String getUniqueID() {

		return uniqueID;
	}

	/**
	 * Returns the saved value of System#currentTimeMillis when the game started
	 */
	public long getMinecraftStartTimeMillis() {

		return minecraftStartTimeMilis;
	}

}
