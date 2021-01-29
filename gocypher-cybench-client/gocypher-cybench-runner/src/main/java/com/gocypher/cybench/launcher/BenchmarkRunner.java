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

import com.gocypher.cybench.core.annotation.BenchmarkMetaData;
import com.gocypher.cybench.core.annotation.CyBenchMetadataList;
import com.gocypher.cybench.core.utils.IOUtils;
import com.gocypher.cybench.core.utils.JMHUtils;
import com.gocypher.cybench.core.utils.SecurityUtils;
import com.gocypher.cybench.launcher.environment.model.HardwareProperties;
import com.gocypher.cybench.launcher.environment.model.JVMProperties;
import com.gocypher.cybench.launcher.environment.services.CollectSystemInformation;
import com.gocypher.cybench.launcher.model.BenchmarkOverviewReport;
import com.gocypher.cybench.launcher.model.BenchmarkReport;
import com.gocypher.cybench.launcher.report.DeliveryService;
import com.gocypher.cybench.launcher.report.ReportingService;
import com.gocypher.cybench.launcher.services.ConfigurationHandler;
import com.gocypher.cybench.launcher.utils.ComputationUtils;
import com.gocypher.cybench.launcher.utils.Constants;
import com.gocypher.cybench.launcher.utils.JSONUtils;
import com.gocypher.cybench.launcher.utils.SecurityBuilder;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.HotspotRuntimeProfiler;
import org.openjdk.jmh.profile.HotspotThreadProfiler;
import org.openjdk.jmh.profile.SafepointsProfiler;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BenchmarkRunner {
    public static final String CYB_UPLOAD_URL = System.getProperty("cybench.manual.upload.url", "https://app.cybench.io/cybench/upload");
    private static final Logger LOG = LoggerFactory.getLogger(BenchmarkRunner.class);
    private static final String CYB_REPORT_FOLDER = System.getProperty("cybench.report.folder",
            "." + File.separator + "reports" + File.separator);
    public static final String CYB_REPORT_JSON_FILE = CYB_REPORT_FOLDER + System.getProperty(Constants.CYB_REPORT_JSON_FILE, "report.cybench");
    public static final String CYB_REPORT_CYB_FILE = CYB_REPORT_FOLDER + System.getProperty(Constants.CYB_REPORT_CYB_FILE, "report.cyb");
    static Properties cfg = new Properties();
    private static String benchSource = "CyBench Launcher";

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

        Map<String, Map<String, String>> defaultBenchmarksMetadata = ComputationUtils.parseBenchmarkMetadata(getProperty(Constants.BENCHMARK_METADATA));

        LOG.info("_______________________ BENCHMARK TESTS FOUND _________________________________");
        OptionsBuilder optBuild = new OptionsBuilder();
        // Number of separate full executions of a benchmark (warm up+measurement), this
        // is returned still as one primary score item
        int forks = setExecutionProperty(getProperty(Constants.NUMBER_OF_FORKS));
        // Number of measurements per benchmark operation, this is returned still as one
        // primary score item
        int measurementIterations = setExecutionProperty(getProperty(Constants.MEASUREMENT_ITERATIONS));

        int measurementSeconds = setExecutionProperty(getProperty(Constants.MEASUREMENT_SECONDS));
        // number of iterations executed for warm up
        int warmUpIterations = setExecutionProperty(getProperty(Constants.WARM_UP_ITERATIONS));
        // number of seconds dedicated for each warm up iteration
        int warmUpSeconds = setExecutionProperty(getProperty(Constants.WARM_UP_SECONDS));
        // number of threads for benchmark test execution
        int threads = setExecutionProperty(getProperty(Constants.RUN_THREAD_COUNT));

        String tempBenchmark = null;
        SecurityBuilder securityBuilder = new SecurityBuilder();

        Map<String, Object> benchmarkSetting = new HashMap<>();

        Map<String, String> generatedFingerprints = new HashMap<>();
        Map<String, String> manualFingerprints = new HashMap<>();
        Map<String, String> classFingerprints = new HashMap<>();

        boolean foundBenchmarks = false;


        if (checkIfConfigurationPropertyIsSet(getProperty(Constants.BENCHMARK_RUN_CLASSES))) {
            LOG.info("Execute benchmarks found in configuration {}", getProperty(Constants.BENCHMARK_RUN_CLASSES));
            List<String> benchmarkNames = Arrays.stream(getProperty(Constants.BENCHMARK_RUN_CLASSES).split(","))
                    .map(String::trim).collect(Collectors.toList());

            // *********************************************************

            for (String benchmarkClass : benchmarkNames) {
                try {
                    Class<?> classObj = Class.forName(benchmarkClass);
                    SecurityUtils.generateMethodFingerprints(classObj, manualFingerprints, classFingerprints);
                    SecurityUtils.computeClassHashForMethods(classObj, generatedFingerprints);
                    tempBenchmark = classObj.getName();

                    if (!classObj.getName().isEmpty()) {
                        optBuild.include(classObj.getName());
                        foundBenchmarks = true;
                        if (classObj.getName().startsWith("com.gocypher.cybench.")) {
                            securityBuilder.generateSecurityHashForClasses(classObj);
                        }
                    }
                } catch (ClassNotFoundException exc) {
                    LOG.error("Class not found in the classpath for execution", exc);
                }
            }

            // *********************************************************

            LOG.info("Custom classes found and registered for execution: {}", foundBenchmarks);
        } else {
            LOG.info("Execute all benchmarks found on the classpath and configure default ones....");
            List<String> benchmarkClasses = JMHUtils.getAllBenchmarkClasses();

            for (String benchmarkClass : benchmarkClasses) {
                try {
                    Class<?> classObj = Class.forName(benchmarkClass);
                    SecurityUtils.generateMethodFingerprints(classObj, manualFingerprints, classFingerprints);
                    SecurityUtils.computeClassHashForMethods(classObj, generatedFingerprints);
                    foundBenchmarks = true;
                    tempBenchmark = classObj.getName();
                    securityBuilder.generateSecurityHashForClasses(classObj);
                } catch (ClassNotFoundException exc) {
                    LOG.error("Class not found in the classpath for execution", exc);
                }
            }

//            Reflections reflections = new Reflections("com.gocypher.cybench.", new SubTypesScanner(false));
//            Set<Class<? extends Object>> allDefaultClasses = reflections.getSubTypesOf(Object.class);
//            foundBenchmarks = true;
//            for (Class<? extends Object> classObj : allDefaultClasses) {
//                if (!classObj.getName().isEmpty() && classObj.getSimpleName().contains("Benchmarks")
//                        && !classObj.getSimpleName().contains("_")) {
                    // We do not include any class, because then JMH will discover all benchmarks
                    // automatically including user defined
                    // optBuild.include(classObj.getName());
//                    tempBenchmark = classObj.getName();
//                    LOG.info("tempBenchmark... {}", tempBenchmark);
//                    securityBuilder.generateSecurityHashForClasses(classObj);
//                }
//            }
        }

        if (foundBenchmarks) {
            if (System.getProperty(Constants.REPORT_SOURCE) != null) {
                benchSource = System.getProperty(Constants.REPORT_SOURCE);
            }
            benchmarkSetting.put(Constants.REPORT_SOURCE, benchSource);
            benchmarkSetting.put(Constants.REPORT_VERSION, "1.0.0");
        }
        if (getProperty(Constants.BENCHMARK_REPORT_NAME) != null) {
            benchmarkSetting.put("benchReportName", getProperty(Constants.BENCHMARK_REPORT_NAME));
        }
        LOG.info("--->benchmarkSetting:{}", benchmarkSetting);

        ChainedOptionsBuilder optionBuilder = optBuild.shouldDoGC(true).addProfiler(GCProfiler.class).addProfiler(HotspotThreadProfiler.class)
                .addProfiler(HotspotRuntimeProfiler.class).addProfiler(SafepointsProfiler.class).detectJvmArgs();

        optionBuilder = setMeasurementProperties(optionBuilder, forks, measurementIterations, measurementSeconds, warmUpIterations, warmUpSeconds, threads);
        Options opt = optionBuilder.build();

        Runner runner = new Runner(opt);
        Collection<RunResult> results = Collections.emptyList();
        if (foundBenchmarks) {
            results = runner.run();
        }

        LOG.info("Benchmark finished, executed tests count:{}", results.size());

        BenchmarkOverviewReport report = ReportingService.getInstance().createBenchmarkReport(results, defaultBenchmarksMetadata);
//        BenchmarkOverviewReport report = ReportingService.getInstance().createBenchmarkReport(results);
        report.getEnvironmentSettings().put("environment", hwProperties);
        report.getEnvironmentSettings().put("jvmEnvironment", jvmProperties);
        report.getEnvironmentSettings().put("unclassifiedProperties", CollectSystemInformation.getUnclassifiedProperties());
        report.getEnvironmentSettings().put("userDefinedProperties", getUserDefinedProperties());
        report.setBenchmarkSettings(benchmarkSetting);

        Iterator<String> it = report.getBenchmarks().keySet().iterator();

        while (it.hasNext()) {
            List<BenchmarkReport> custom = report.getBenchmarks().get(it.next()).stream().collect(Collectors.toList());
            custom.stream().forEach(benchmarkReport -> {
                String name = benchmarkReport.getName();
                benchmarkReport.setClassFingerprint(classFingerprints.get(name));
                benchmarkReport.setGeneratedFingerprint(generatedFingerprints.get(name));
                benchmarkReport.setManualFingerprint(manualFingerprints.get(name));
                try {
                    JMHUtils.ClassAndMethod classAndMethod = new JMHUtils.ClassAndMethod(name).invoke();
                    String clazz = classAndMethod.getClazz();
                    String method = classAndMethod.getMethod();
                    LOG.info("Adding metadata for benchamrk: " + clazz + " test: " + method);
                    Class<?> aClass = Class.forName(clazz);
                    Optional<Method> benchmarkMethod = JMHUtils.getBenchmarkMethod(method, aClass);
                    appendMetadataFromMethod(benchmarkMethod, benchmarkReport);
                    appendMetadataFromClass(aClass, benchmarkReport);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
        }
        if(report.getBenchmarks() != null) {
            List<BenchmarkReport> customBenchmarksCategoryCheck = report.getBenchmarks().get("CUSTOM");
            report.getBenchmarks().remove("CUSTOM");
            for (BenchmarkReport benchReport : customBenchmarksCategoryCheck) {
                report.addToBenchmarks(benchReport);
            }
            report.computeScores();
            getReportUploadStatus(report);
        }
        try {
            LOG.info("Generating JSON report...");
            String reportJSON;
            String reportEncrypted = ReportingService.getInstance().prepareReportForDelivery(securityBuilder, report);
            String responseWithUrl;
            if (shouldSendReport(report)) {
                responseWithUrl = DeliveryService.getInstance().sendReportForStoring(reportEncrypted);
                report.setReportURL(responseWithUrl);
            } else {
                LOG.info("You may submit your report '{}' manually at {}", CYB_REPORT_CYB_FILE, CYB_UPLOAD_URL);
            }
//			LOG.info("-----------------------------------------------------------------------------------------");
//			LOG.info("REPORT '{}'", report);
//			LOG.info("-----------------------------------------------------------------------------------------");
            reportJSON = JSONUtils.marshalToPrettyJson(report);
            String cybReportJsonFile = getCybReportFileName(report, CYB_REPORT_JSON_FILE);
            String cybReportFile = getCybReportFileName(report, CYB_REPORT_CYB_FILE);
            if(cybReportJsonFile.equals(CYB_REPORT_JSON_FILE) && cybReportFile.equals(CYB_REPORT_CYB_FILE)){
                cybReportJsonFile = IOUtils.getReportsPath("", ComputationUtils.createFileNameForReport("report", start, report.getTotalScore(), false));
                cybReportFile = IOUtils.getReportsPath("", ComputationUtils.createFileNameForReport("report", start, report.getTotalScore(), true));
            }
            LOG.info("Saving test results to '{}'", cybReportJsonFile);
            IOUtils.storeResultsToFile(cybReportJsonFile, reportJSON);
            LOG.info("Saving ecnrypted test results to '{}'", cybReportFile);
            IOUtils.storeResultsToFile(cybReportFile, reportEncrypted);

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
    private static void customBenchmarksCategoryCheck(){

    }
    private static ChainedOptionsBuilder setMeasurementProperties(ChainedOptionsBuilder optionBuilder, int forks, int measurementIterations, int measurementSeconds, int warmUpIterations, int warmUpSeconds, int threads){
        if(forks!=-1){
            optionBuilder = optionBuilder.forks(forks);
        }
        if(measurementIterations!=-1){
            optionBuilder = optionBuilder.measurementIterations(measurementIterations);
        }
        if(warmUpIterations!=-1){
            optionBuilder = optionBuilder.warmupIterations(warmUpIterations);
        }
        if(warmUpSeconds!=-1){
            optionBuilder = optionBuilder.warmupTime(TimeValue.seconds(warmUpSeconds));
        }
        if(threads!=-1){
            optionBuilder = optionBuilder.threads(threads);
        }
        if(measurementSeconds!=-1){
            optionBuilder = optionBuilder.measurementTime(TimeValue.seconds(measurementSeconds));
        }
        return optionBuilder;
    }

    protected static void appendMetadataFromClass(Class<?> aClass, BenchmarkReport benchmarkReport) {
        CyBenchMetadataList annotation = aClass.getDeclaredAnnotation(CyBenchMetadataList.class);
        if (annotation != null) {
            Arrays.stream(annotation.value()).forEach(annot -> {
                checkSetOldMetadataProps(annot.key(), annot.value(), benchmarkReport);
                benchmarkReport.addMetadata(annot.key(), annot.value());
//                LOG.info("added metadata " + annot.key() + "=" + annot.value());
            });
        }
        BenchmarkMetaData singleAnnotation = aClass.getDeclaredAnnotation(BenchmarkMetaData.class);
        if (singleAnnotation != null) {
            checkSetOldMetadataProps(singleAnnotation.key(), singleAnnotation.value(), benchmarkReport);
            benchmarkReport.addMetadata(singleAnnotation.key(), singleAnnotation.value());
//            LOG.info("added metadata " + singleAnnotation.key() + "=" + singleAnnotation.value());
        }

        if (!aClass.getSuperclass().equals(Object.class) ) {
            appendMetadataFromClass(aClass.getSuperclass(), benchmarkReport);
        }
    }

    protected static void appendMetadataFromMethod(Optional<Method> benchmarkMethod, BenchmarkReport benchmarkReport) {
        CyBenchMetadataList annotation = benchmarkMethod.get().getDeclaredAnnotation(CyBenchMetadataList.class);
        if (annotation != null) {
            Arrays.stream(annotation.value()).forEach(annot -> {
                checkSetOldMetadataProps(annot.key(), annot.value(), benchmarkReport);
                benchmarkReport.addMetadata(annot.key(), annot.value());
//                LOG.info("added metadata " + annot.key() + "=" + annot.value());
            });
        }
        BenchmarkMetaData singleAnnotation = benchmarkMethod.get().getDeclaredAnnotation(BenchmarkMetaData.class);
        if (singleAnnotation != null) {
            checkSetOldMetadataProps(singleAnnotation.key(), singleAnnotation.value(), benchmarkReport);
            benchmarkReport.addMetadata(singleAnnotation.key(), singleAnnotation.value());
//            LOG.info("added metadata " + singleAnnotation.key() + "=" + singleAnnotation.value());

        }

    }

    private static void checkSetOldMetadataProps(String key,String value, BenchmarkReport benchmarkReport){
        if(key.equals("api")){
            benchmarkReport.setCategory(value);
        }
        if(key.equals("context")){
            benchmarkReport.setContext(value);
        }
        if(key.equals("version")){
            benchmarkReport.setVersion(value);
        }
    }

    private static boolean shouldSendReport(BenchmarkOverviewReport report) {
        if (report.getEnvironmentSettings().get("environment") instanceof HardwareProperties.EmptyHardwareProperties) return false;
        return report.isEligibleForStoringExternally() && (getProperty(Constants.SEND_REPORT) == null
                || Boolean.parseBoolean(getProperty(Constants.SEND_REPORT)));
    }

    private static String getCybReportFileName(BenchmarkOverviewReport report, String nameTemplate) {
        if (Boolean.parseBoolean(getProperty(Constants.APPEND_SCORE_TO_FNAME))) {
            String start = nameTemplate.substring(0, nameTemplate.lastIndexOf('.'));
            String end = nameTemplate.substring(nameTemplate.lastIndexOf('.'));
            return start + "-" + com.gocypher.cybench.core.utils.JSONUtils.convertNumToStringByLength(String.valueOf(report.getTotalScore())) + end;
        } else {
            return nameTemplate;

        }
    }

    public static String getProperty(String key) {
        return System.getProperty(key, cfg.getProperty(key));
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

    private static int setExecutionProperty(String property) {
        if (checkIfConfigurationPropertyIsSet(property)) {
            return Integer.parseInt(property);
        } else {
            return -1;
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
