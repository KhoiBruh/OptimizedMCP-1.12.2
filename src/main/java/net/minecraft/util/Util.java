package net.minecraft.util;

import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Util {

	public static Util.OS getOSType() {

		String s = System.getProperty("os.name").toLowerCase(Locale.ROOT);

		if (s.contains("win")) {
			return Util.OS.WINDOWS;
		} else if (s.contains("mac")) {
			return Util.OS.OSX;
		} else if (s.contains("solaris")) {
			return Util.OS.SOLARIS;
		} else if (s.contains("sunos")) {
			return Util.OS.SOLARIS;
		} else if (s.contains("linux")) {
			return Util.OS.LINUX;
		} else {
			return s.contains("unix") ? Util.OS.LINUX : Util.OS.UNKNOWN;
		}
	}

	public static <V> void runTask(FutureTask<V> task, Logger logger) {

		try {
			task.run();
			task.get();
		} catch (ExecutionException | InterruptedException exception) {
			logger.fatal("Error executing task", exception);
		}
	}

	public static <T> T getLastElement(List<T> list) {

		return list.getLast();
	}

	public enum OS {
		LINUX,
		SOLARIS,
		WINDOWS,
		OSX,
		UNKNOWN
	}

}
