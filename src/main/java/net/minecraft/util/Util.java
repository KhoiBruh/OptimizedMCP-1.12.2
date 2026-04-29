package net.minecraft.util;

import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Util {

	public static Util.EnumOS getOSType() {

		String s = System.getProperty("os.name").toLowerCase(Locale.ROOT);

		if (s.contains("win")) {
			return Util.EnumOS.WINDOWS;
		} else if (s.contains("mac")) {
			return Util.EnumOS.OSX;
		} else if (s.contains("solaris")) {
			return Util.EnumOS.SOLARIS;
		} else if (s.contains("sunos")) {
			return Util.EnumOS.SOLARIS;
		} else if (s.contains("linux")) {
			return Util.EnumOS.LINUX;
		} else {
			return s.contains("unix") ? Util.EnumOS.LINUX : Util.EnumOS.UNKNOWN;
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

	public enum EnumOS {
		LINUX,
		SOLARIS,
		WINDOWS,
		OSX,
		UNKNOWN
	}

}
