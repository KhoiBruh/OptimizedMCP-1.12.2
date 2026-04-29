package net.minecraft.profiler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Profiler {

	private static final Logger LOGGER = LogManager.getLogger();
	private final List<String> sectionList = Lists.newArrayList();
	private final List<Long> timestampList = Lists.newArrayList();
	private final Map<String, Long> profilingMap = Maps.newHashMap();
	/**
	 * Flag profiling enabled
	 */
	public boolean profilingEnabled;
	/**
	 * Current profiling section
	 */
	private String profilingSection = "";

	/**
	 * Clear profiling.
	 */
	public void clearProfiling() {

		profilingMap.clear();
		profilingSection = "";
		sectionList.clear();
	}

	/**
	 * Start section
	 */
	public void startSection(String name) {

		if (profilingEnabled) {
			if (!profilingSection.isEmpty()) {
				profilingSection = profilingSection + ".";
			}

			profilingSection = profilingSection + name;
			sectionList.add(profilingSection);
			timestampList.add(System.nanoTime());
		}
	}

	public void func_194340_a(Supplier<String> p_194340_1_) {

		if (profilingEnabled) {
			startSection(p_194340_1_.get());
		}
	}

	/**
	 * End section
	 */
	public void endSection() {

		if (profilingEnabled) {
			long i = System.nanoTime();
			long j = timestampList.removeLast();
			sectionList.removeLast();
			long k = i - j;

			if (profilingMap.containsKey(profilingSection)) {
				profilingMap.put(profilingSection, profilingMap.get(profilingSection) + k);
			} else {
				profilingMap.put(profilingSection, k);
			}

			if (k > 100000000L) {
				LOGGER.warn("Something's taking too long! '{}' took aprox {} ms", profilingSection, (double) k / 1000000D);
			}

			profilingSection = sectionList.isEmpty() ? "" : sectionList.getLast();
		}
	}

	public List<Profiler.Result> getProfilingData(String profilerName) {

		if (!profilingEnabled) {
			return Collections.emptyList();
		} else {
			long i = profilingMap.getOrDefault("root", 0L);
			long j = profilingMap.getOrDefault(profilerName, -1L);
			List<Profiler.Result> list = Lists.newArrayList();

			if (!profilerName.isEmpty()) {
				profilerName = profilerName + ".";
			}

			long k = 0L;

			for (String s : profilingMap.keySet()) {
				if (s.length() > profilerName.length() && s.startsWith(profilerName) && s.indexOf(".", profilerName.length() + 1) < 0) {
					k += profilingMap.get(s);
				}
			}

			float f = (float) k;

			if (k < j) {
				k = j;
			}

			if (i < k) {
				i = k;
			}

			for (String s1 : profilingMap.keySet()) {
				if (s1.length() > profilerName.length() && s1.startsWith(profilerName) && s1.indexOf(".", profilerName.length() + 1) < 0) {
					long l = profilingMap.get(s1);
					double d0 = (double) l * 100D / (double) k;
					double d1 = (double) l * 100D / (double) i;
					String s2 = s1.substring(profilerName.length());
					list.add(new Profiler.Result(s2, d0, d1));
				}
			}

			profilingMap.replaceAll((s, v) -> profilingMap.get(s) * 999L / 1000L);

			if ((float) k > f) {
				list.add(new Profiler.Result("unspecified", (double) ((float) k - f) * 100D / (double) k, (double) ((float) k - f) * 100D / (double) i));
			}

			Collections.sort(list);
			list.addFirst(new Profiler.Result(profilerName, 100D, (double) k * 100D / (double) i));
			return list;
		}
	}

	/**
	 * End current section and start a new section
	 */
	public void endStartSection(String name) {

		endSection();
		startSection(name);
	}

	public void func_194339_b(Supplier<String> p_194339_1_) {

		endSection();
		func_194340_a(p_194339_1_);
	}

	public String getNameOfLastSection() {

		return sectionList.isEmpty() ? "[UNKNOWN]" : sectionList.getLast();
	}

	public static final class Result implements Comparable<Profiler.Result> {

		public double usePercentage;
		public double totalUsePercentage;
		public String profilerName;

		public Result(String profilerName, double usePercentage, double totalUsePercentage) {

			this.profilerName = profilerName;
			this.usePercentage = usePercentage;
			this.totalUsePercentage = totalUsePercentage;
		}

		public int compareTo(Profiler.Result p_compareTo_1_) {

			if (p_compareTo_1_.usePercentage < usePercentage) {
				return -1;
			} else {
				return p_compareTo_1_.usePercentage > usePercentage ? 1 : p_compareTo_1_.profilerName.compareTo(profilerName);
			}
		}

		public int getColor() {

			return (profilerName.hashCode() & 11184810) + 4473924;
		}

	}

}
