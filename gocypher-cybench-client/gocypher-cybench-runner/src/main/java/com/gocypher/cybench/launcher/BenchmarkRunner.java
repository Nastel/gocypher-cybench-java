/*
 * Copyright (C) 2020, K2N.IO.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package com.gocypher.cybench.launcher;

import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.gocypher.cybench.launcher.utils.ComputationUtils;
import com.gocypher.cybench.launcher.model.BenchmarkOverviewReport;
import com.gocypher.cybench.launcher.report.DeliveryService;
import com.gocypher.cybench.launcher.report.ReportingService;
import com.gocypher.cybench.launcher.services.ConfigurationHandler;
import com.gocypher.cybench.launcher.utils.Constants;
import com.gocypher.cybench.launcher.utils.JSONUtils;
import com.gocypher.cybench.launcher.utils.SecurityBuilder;
import com.jcabi.manifests.Manifests;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.HotspotRuntimeProfiler;
import org.openjdk.jmh.profile.HotspotThreadProfiler;
import org.openjdk.jmh.profile.SafepointsProfiler;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gocypher.cybench.core.utils.IOUtils;
import com.gocypher.cybench.launcher.environment.model.HardwareProperties;
import com.gocypher.cybench.launcher.environment.model.JVMProperties;
import com.gocypher.cybench.launcher.environment.services.CollectSystemInformation;

public class BenchmarkRunner {
	private static final Logger LOG = LoggerFactory.getLogger(BenchmarkRunner.class);
	
	private static final String CYB_REPORT_FOLDER = System.getProperty("cybench.report.folder", 
			"." + File.separator + "reports" + File.separator);
	
	public static final String CYB_REPORT_JSON_FILE = CYB_REPORT_FOLDER + "report.json";
	public static final String CYB_REPORT_CYB_FILE = CYB_REPORT_FOLDER + "report.cyb";
	
	public static final String CYB_UPLOAD_URL = System.getProperty("cybench.manual.upload.url",	"https://www.gocypher.com/cybench/upload");

	static Properties cfg = new Properties();

	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		LOG.info("-----------------------------------------------------------------------------------------");
		LOG.info("                                 Starting CyBench benchmarks                             ");
		LOG.info("-----------------------------------------------------------------------------------------");
		identifyPropertiesFromArguments(args);

		LOG.info("Collecting hardware, software information...");
		HardwareProperties hwProperties = CollectSystemInformation.getEnvironmentProperties();
		LOG.info("Collecting JVM properties...");
		JVMProperties jvmProperties = CollectSystemInformation.getJavaVirtualMachineProperties();
		LOG.info("Executing benchmarks...");
		// printSystemInformation();

		// Number of separate full executions of a benchmark (warm up+measurement), this
		// is returned still as one primary score item
		int forks = setExecutionProperty(getProperty(Constants.NUMBER_OF_FORKS), 1);
		// Number of measurements per benchmark operation, this is returned still as one
		// primary score item
		int measurementIterations = setExecutionProperty(getProperty(Constants.MEASUREMENT_ITERATIONS), 5);
		// number of iterations executed for warm up
		int warmUpIterations = setExecutionProperty(getProperty(Constants.WARM_UP_ITERATIONS), 1);
		// number of seconds dedicated for each warm up iteration
		int warmUpSeconds = setExecutionProperty(getProperty(Constants.WARM_UP_SECONDS), 5);
		// number of threads for benchmark test execution
		int threads = setExecutionProperty(getProperty(Constants.BENCHMARK_RUN_THREAD_COUNT), 1);

		Map<String, Map<String, String>> defaultBenchmarksMetadata = ComputationUtils.parseBenchmarkMetadata(getProperty(Constants.BENCHMARK_METADATA));

		LOG.info("_______________________ BENCHMARK TESTS FOUND _________________________________");
		OptionsBuilder optBuild = new OptionsBuilder();
		String tempBenchmark = null;
		SecurityBuilder securityBuilder = new SecurityBuilder();

		Map<String, Object> benchmarkSetting = new HashMap<>();

		int includedClassesForCustomRun = 0;

		if (checkIfConfigurationPropertyIsSet(getProperty(Constants.BENCHMARK_RUN_CLASSES))) {
			LOG.info("Execute benchmarks found in configuration {}", getProperty(Constants.BENCHMARK_RUN_CLASSES));
			List<String> benchmarkNames = Arrays.stream(getProperty(Constants.BENCHMARK_RUN_CLASSES).split(","))
					.map(String::trim).collect(Collectors.toList());
			for (String benchmarkClass : benchmarkNames) {
				try {
					LOG.info("benchmarkClass name {}",benchmarkClass);
					Class<?> classObj = Class.forName(benchmarkClass);
					if (!classObj.getName().isEmpty()) {
						optBuild.include(classObj.getName());
						includedClassesForCustomRun++;
						if (classObj.getName().startsWith("com.gocypher.cybench.")) {
							tempBenchmark = classObj.getName();
							securityBuilder.generateSecurityHashForClasses(classObj);
						}
					}
				} catch (ClassNotFoundException exc) {
					LOG.error("Class not found in the classpath for execution", exc);
				}
			}
			LOG.info("Custom classes found and registered for execution: {}", includedClassesForCustomRun);
		} else {
			LOG.info("Execute all benchmarks found on the classpath and configure default ones....");
			Reflections reflections = new Reflections("com.gocypher.cybench.", new SubTypesScanner(false));
			Set<Class<? extends Object>> allDefaultClasses = reflections.getSubTypesOf(Object.class);
			includedClassesForCustomRun++;
			for (Class<? extends Object> classObj : allDefaultClasses) {
				if (!classObj.getName().isEmpty() && classObj.getSimpleName().contains("Benchmarks")
						&& !classObj.getSimpleName().contains("_")) {
					// LOG.info("==>Default found:{}",classObj.getName());
					// We do not include any class, because then JMH will discover all benchmarks
					// automatically including user defined
					// optBuild.include(classObj.getName());
					tempBenchmark = classObj.getName();
					securityBuilder.generateSecurityHashForClasses(classObj);
				}
			}
		}
		// LOG.info("Situation of class
		// signatures:{}",securityBuilder.getMapOfHashedParts()) ;
		if (tempBenchmark != null) {
			String manifestData = null ;
			if (Manifests.exists(Constants.BENCHMARK_METADATA)) {
				manifestData = Manifests.read(Constants.BENCHMARK_METADATA);
			}
			Map<String,Map<String,String>> benchmarksMetadata =  ComputationUtils.parseBenchmarkMetadata(manifestData);
			Map<String, String> benchProps;
			if(manifestData != null) {
				benchProps = ReportingService.getInstance().prepareBenchmarkSettings(tempBenchmark, benchmarksMetadata);
			}else{
				benchProps = ReportingService.getInstance().prepareBenchmarkSettings(tempBenchmark, defaultBenchmarksMetadata);
			}
			benchmarkSetting.putAll(benchProps);
			benchmarkSetting.put("benchThreadCount", threads);
			if (getProperty(Constants.BENCHMARK_REPORT_NAME) != null) {
				benchmarkSetting.put("benchReportName", getProperty(Constants.BENCHMARK_REPORT_NAME));
			}
		}

		Options opt = optBuild.forks(forks).measurementIterations(measurementIterations)
				.warmupIterations(warmUpIterations).warmupTime(TimeValue.seconds(warmUpSeconds)).threads(threads)
				.shouldDoGC(true).addProfiler(GCProfiler.class).addProfiler(HotspotThreadProfiler.class)
				.addProfiler(HotspotRuntimeProfiler.class).addProfiler(SafepointsProfiler.class).detectJvmArgs()
				// .addProfiler(StackProfiler.class)
				// .addProfiler(HotspotMemoryProfiler.class)
				// .addProfiler(HotspotRuntimeProfiler.class)
				// .addProfiler(JavaFlightRecorderProfiler.class)
				.build();

		Runner runner = new Runner(opt);
		Collection<RunResult> results = Collections.emptyList();
		if (includedClassesForCustomRun > 0) {
			results = runner.run();
		}

		LOG.info("Benchmark finished, executed tests count:{}", results.size());

		BenchmarkOverviewReport report = ReportingService.getInstance().createBenchmarkReport(results, defaultBenchmarksMetadata);
		report.getEnvironmentSettings().put("environment", hwProperties);
		report.getEnvironmentSettings().put("jvmEnvironment", jvmProperties);
		report.getEnvironmentSettings().put("unclassifiedProperties", CollectSystemInformation.getUnclassifiedProperties());
		report.getEnvironmentSettings().put("userDefinedProperties", getUserDefinedProperties());
		report.setBenchmarkSettings(benchmarkSetting);
		getReportUploadStatus(report);
		try {
			LOG.info("Generating JSON report...");
			String reportJSON;
			String reportEncrypted = ReportingService.getInstance().prepareReportForDelivery(securityBuilder, report);
			String responseWithUrl;
			if (report.isEligibleForStoringExternally() && (getProperty(Constants.SHOULD_SEND_REPORT) == null
					|| Boolean.parseBoolean(getProperty(Constants.SHOULD_SEND_REPORT)))) {
				responseWithUrl = DeliveryService.getInstance().sendReportForStoring(reportEncrypted);
				report.setReportURL(responseWithUrl);
			} else {
				LOG.info("You may submit your report '{}' manually at {}", CYB_REPORT_CYB_FILE, CYB_UPLOAD_URL);
			}
//			LOG.info("-----------------------------------------------------------------------------------------");
//			LOG.info("REPORT '{}'", report);
//			LOG.info("-----------------------------------------------------------------------------------------");
			reportJSON = JSONUtils.marshalToPrettyJson(report);
			LOG.info("Saving test results to '{}'", CYB_REPORT_JSON_FILE);
			IOUtils.storeResultsToFile(CYB_REPORT_JSON_FILE, reportJSON);
			LOG.info("Saving ecnrypted test results to '{}'", CYB_REPORT_CYB_FILE);
			IOUtils.storeResultsToFile(CYB_REPORT_CYB_FILE, reportEncrypted);

			LOG.info("Removing all temporary auto-generated files....");
			IOUtils.removeTestDataFiles();
			LOG.info("Removed all temporary auto-generated files!!!");
		} catch (Exception e) {
			LOG.error("Failed to save test results", e);
			LOG.info("You may submit your report '{}' manually at {}", CYB_REPORT_CYB_FILE, CYB_UPLOAD_URL);
		}
		LOG.info("-----------------------------------------------------------------------------------------");
		LOG.info("                                 Finished CyBench benchmarking ({})                      ", formatInterval(System.currentTimeMillis() - start));
		LOG.info("-----------------------------------------------------------------------------------------");
	}

    private static String getProperty(String key) {
	    return System.getProperty(key,cfg.getProperty(key) );
    }

    private static String formatInterval(final long l) {
		final long hr = TimeUnit.MILLISECONDS.toHours(l);
		final long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
		final long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
		final long ms = TimeUnit.MILLISECONDS.toMillis(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
		return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
	}

    private static void getReportUploadStatus(BenchmarkOverviewReport report) {
		String reportUploadStatus = getProperty(Constants.REPORT_UPLOAD_STATUS);
		if (Constants.REPORT_PUBLIC.equals(reportUploadStatus)) {
			report.setUploadStatus(reportUploadStatus);
		} else if (Constants.REPORT_PRIVATE.equals(reportUploadStatus)) {
			report.setUploadStatus(reportUploadStatus);
		} else {
			report.setUploadStatus(Constants.REPORT_PUBLIC);
		}
	}

	private static Map<String, Object> getUserDefinedProperties() {
		Map<String, Object> userProperties = new HashMap<>();
		Set<String> keys = cfg.stringPropertyNames();
		for (String key : keys) {
			if (key.startsWith(Constants.USER_PROPERTY_PREFIX)) {
				userProperties.put(key, getProperty(key));
			}
		}
		return userProperties;
	}

	private static int setExecutionProperty(String property, int value) {
		if (checkIfConfigurationPropertyIsSet(property)) {
			return Integer.parseInt(property);
		} else {
			return value;
		}
	}

	private static boolean checkIfConfigurationPropertyIsSet(String property) {
		return property != null && !property.equals("");
	}

	private static void identifyPropertiesFromArguments(String[] args) {
		String configurationFilePath = "";
		for (String property : args) {
			if (property.contains("cfg") || property.contains("config") || property.contains("configuration")) {
				String[] tempConfigPath = property.split("=");
				if (tempConfigPath.length > 1) {
					configurationFilePath = tempConfigPath[1];
				} else {
					LOG.info("Incorrect format, configuration path syntax: cfg|config|configuration='full-file-path'");
				}
			} else {
				LOG.info("Using default configuration file {}", configurationFilePath);
			}
		}
		cfg = ConfigurationHandler.loadConfiguration(configurationFilePath);
	}

	static void printSystemInformation() {
		long kilobytes = 1024;
		long megabytes = kilobytes * 1024;
		long gigabytes = megabytes * 1024;

		String nameOS = "os.name";
		String versionOS = "os.version";
		String architectureOS = "os.arch";
		System.out.println("\n  Info about OS");
		System.out.println("\nName of the OS: " + System.getProperty(nameOS));
		System.out.println("Version of the OS: " + System.getProperty(versionOS));
		System.out.println("Architecture of The OS: " + System.getProperty(architectureOS));
		Map<String, String> env = System.getenv();
		System.out.println("Environment values");
		for (String key : env.keySet()) {
			System.out.println("K: " + key + " \n\tV: " + env.get(key));
		}
		/* Total number of processors or cores available to the JVM */
		System.out.println("\nAvailable processors (cores): " + Runtime.getRuntime().availableProcessors());

		/* Total amount of free memory available to the JVM */
		System.out.println("Free memory (megabytes): " + Runtime.getRuntime().freeMemory() / (float) megabytes);

		/* This will return Long.MAX_VALUE if there is no preset limit */
		long maxMemory = Runtime.getRuntime().maxMemory();
		/* Maximum amount of memory the JVM will attempt to use */
		System.out.println("Maximum memory (megabytes): "
				+ (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory / (float) megabytes));

		/* Total memory currently available to the JVM */
		System.out.println(
				"Total memory available to JVM (megabytes): " + Runtime.getRuntime().totalMemory() / (float) megabytes);

		/* Get a list of all filesystem roots on this system */
		File[] roots = File.listRoots();

		/* For each filesystem root, print some info */
		for (File root : roots) {
			System.out.println("\nFile system root: " + root.getAbsolutePath());
			System.out.println("Total space (gigabytes): " + (root.getTotalSpace() / (float) gigabytes));
			System.out.println("Free space (gigabytes): " + (root.getFreeSpace() / (float) gigabytes));
			System.out.println("Usable space (gigabytes): " + (root.getUsableSpace() / (float) gigabytes));

		}
		System.out.println("\n\nProperties:\n------\n");
		System.getProperties().list(System.out);

	}

	public static void printGCStats() {
		long totalGarbageCollections = 0;
		long garbageCollectionTime = 0;

		for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
			System.out.println("GC:" + gc.getName() + ";" + gc.getClass());

			long count = gc.getCollectionCount();

			if (count >= 0) {
				totalGarbageCollections += count;
			}

			long time = gc.getCollectionTime();

			if (time >= 0) {
				garbageCollectionTime += time;
			}
		}

		System.out.println("Total Garbage Collections: " + totalGarbageCollections);
		System.out.println("Total Garbage Collection Time (ms): " + garbageCollectionTime);
	}
}
