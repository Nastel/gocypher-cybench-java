/*
 * Copyright (C) 2020-2022, K2N.IO.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 */

package com.gocypher.cybench.launcher;

import java.io.*;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.SafepointsProfiler;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gocypher.cybench.core.annotation.BenchmarkMetaData;
import com.gocypher.cybench.core.annotation.CyBenchMetadataList;
import com.gocypher.cybench.core.utils.IOUtils;
import com.gocypher.cybench.core.utils.JMHUtils;
import com.gocypher.cybench.core.utils.JSONUtils;
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
import com.gocypher.cybench.launcher.utils.SecurityBuilder;
import com.gocypher.cybench.model.ComparisonConfig;
import com.gocypher.cybench.model.ComparisonConfig.Scope;

public class BenchmarkRunner {
    private static final Logger LOG = LoggerFactory.getLogger(BenchmarkRunner.class);

    private static final String CYB_REPORT_FOLDER = System.getProperty("cybench.report.folder",
            "." + File.separator + "reports" + File.separator);
    public static final String CYB_REPORT_JSON_FILE = CYB_REPORT_FOLDER
            + System.getProperty(Constants.CYB_REPORT_JSON_FILE, "report.cybench");
    public static final String CYB_REPORT_CYB_FILE = CYB_REPORT_FOLDER
            + System.getProperty(Constants.CYB_REPORT_CYB_FILE, "report.cyb");
    static Properties cfg = new Properties();
    static ComparisonConfig automatedComparisonCfg;
    private static String benchSource = "CyBench Launcher";
    private static final String REPORT_NOT_SENT = "You may submit your report '{}' manually at {}";

    private static final Map<String, String> PROJECT_METADATA_MAP = new HashMap<>(5);

    @SuppressWarnings("unchecked")
    public static void main(String... args) throws Exception {
        long start = System.currentTimeMillis();
        LOG.info("-----------------------------------------------------------------------------------------");
        LOG.info("                                 Starting CyBench benchmarks                             ");
        LOG.info("-----------------------------------------------------------------------------------------");
        if (!checkIfConfigurationPropertyIsSet(getProperty(Constants.INTELLIJ_PLUGIN))
                || !Boolean.parseBoolean(getProperty(Constants.INTELLIJ_PLUGIN))) {
            identifyPropertiesFromArguments(args);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.info("Closing delivery service!..");
                DeliveryService.getInstance().close();
            }
        }));

        LOG.info("Collecting hardware, software information...");
        HardwareProperties hwProperties = CollectSystemInformation.getEnvironmentProperties();
        LOG.info("Collecting JVM properties...");
        JVMProperties jvmProperties = CollectSystemInformation.getJavaVirtualMachineProperties();

        Map<String, Map<String, String>> defaultBenchmarksMetadata = ComputationUtils
                .parseBenchmarkMetadata(getProperty(Constants.BENCHMARK_METADATA));

        try {
            checkProjectMetadataExists();

            LOG.info("Executing benchmarks...");

            LOG.info("_______________________ BENCHMARK TESTS FOUND _________________________________");
            OptionsBuilder optBuild = new OptionsBuilder();
            // Number of separate full executions of a benchmark (warm up+measurement), this
            // is returned still as one primary score item
            int forks = setExecutionIntProperty(getProperty(Constants.NUMBER_OF_FORKS));
            // Number of measurements per benchmark operation, this is returned still as one
            // primary score item
            int measurementIterations = setExecutionIntProperty(getProperty(Constants.MEASUREMENT_ITERATIONS));

            int measurementSeconds = setExecutionIntProperty(getProperty(Constants.MEASUREMENT_SECONDS));
            // number of iterations executed for warm up
            int warmUpIterations = setExecutionIntProperty(getProperty(Constants.WARM_UP_ITERATIONS));
            // number of seconds dedicated for each warm up iteration
            int warmUpSeconds = setExecutionIntProperty(getProperty(Constants.WARM_UP_SECONDS));
            // number of threads for benchmark test execution
            int threads = setExecutionIntProperty(getProperty(Constants.RUN_THREAD_COUNT));
            // benchmarks run mode
            Set<Mode> modes = setExecutionModes(getProperty(Constants.BENCHMARK_MODES));
            String jmhArguments = setExecutionStringProperty(getProperty(Constants.JMH_ARGUMENTS));

            String tempBenchmark;
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

                        if (!tempBenchmark.isEmpty()) {
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
                LOG.info("Execute all benchmarks found on the classpath and configure default ones...");
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

                // Reflections reflections = new Reflections("com.gocypher.cybench.", new
                // SubTypesScanner(false));
                // Set<Class<? extends Object>> allDefaultClasses =
                // reflections.getSubTypesOf(Object.class);
                // foundBenchmarks = true;
                // for (Class<? extends Object> classObj : allDefaultClasses) {
                // if (!classObj.getName().isEmpty() &&
                // classObj.getSimpleName().contains("Benchmarks")
                // && !classObj.getSimpleName().contains("_")) {
                // We do not include any class, because then JMH will discover all benchmarks
                // automatically including user defined
                // optBuild.include(classObj.getName());
                // tempBenchmark = classObj.getName();
                // LOG.info("tempBenchmark... {}", tempBenchmark);
                // securityBuilder.generateSecurityHashForClasses(classObj);
                // }
                // }
            }

            ChainedOptionsBuilder optionBuilder = optBuild.shouldDoGC(true) //
                    // .addProfiler(HotspotThreadProfiler.class) // obsolete
                    // .addProfiler(HotspotRuntimeProfiler.class) // obsolete
                    .addProfiler(GCProfiler.class) //
                    .addProfiler(SafepointsProfiler.class) //
                    .detectJvmArgs();

            optionBuilder = setMeasurementProperties(optionBuilder, forks, measurementIterations, measurementSeconds,
                    warmUpIterations, warmUpSeconds, threads, modes);

            if (StringUtils.isNotEmpty(jmhArguments)) {
                CommandLineOptions cliOptions = new CommandLineOptions(jmhArguments.split("\\s"));
                optionBuilder.parent(cliOptions);
            }

            Options opt = optionBuilder.build();

            Runner runner = new Runner(opt);
            Collection<RunResult> results = Collections.emptyList();
            if (foundBenchmarks) {
                results = runner.run();
            }

            LOG.info("Benchmark finished, executed tests count: {}", results.size());

            BenchmarkOverviewReport report = ReportingService.getInstance().createBenchmarkReport(results,
                    defaultBenchmarksMetadata);
            // BenchmarkOverviewReport report =
            // ReportingService.getInstance().createBenchmarkReport(results);
            report.getEnvironmentSettings().put("environment", hwProperties);
            report.getEnvironmentSettings().put("jvmEnvironment", jvmProperties);
            report.getEnvironmentSettings().put("unclassifiedProperties",
                    CollectSystemInformation.getUnclassifiedProperties());
            report.getEnvironmentSettings().put("userDefinedProperties", getUserDefinedProperties());

            if (automatedComparisonCfg != null) {
                if (automatedComparisonCfg.getScope().equals(Scope.WITHIN)) {
                    automatedComparisonCfg.setCompareVersion(PROJECT_METADATA_MAP.get(Constants.PROJECT_VERSION));
                }
                automatedComparisonCfg.setRange(String.valueOf(automatedComparisonCfg.getCompareLatestReports()));
                automatedComparisonCfg.setProjectName(PROJECT_METADATA_MAP.get(Constants.PROJECT_NAME));
                automatedComparisonCfg.setProjectVersion(PROJECT_METADATA_MAP.get(Constants.PROJECT_VERSION));
                report.setAutomatedComparisonConfig(automatedComparisonCfg);
            }

            if (foundBenchmarks) {
                if (System.getProperty(Constants.REPORT_SOURCE) != null) {
                    benchSource = System.getProperty(Constants.REPORT_SOURCE);
                }
                benchmarkSetting.put(Constants.REPORT_SOURCE, benchSource);
            }
            if (getProperty(Constants.BENCHMARK_REPORT_NAME) != null) {
                benchmarkSetting.put("benchReportName", getProperty(Constants.BENCHMARK_REPORT_NAME));
            }

            LOG.info("---> benchmarkSetting: {}", benchmarkSetting);

            for (Map.Entry<String, List<BenchmarkReport>> benchmarksEntry : report.getBenchmarks().entrySet()) {
                List<BenchmarkReport> benchmarks = benchmarksEntry.getValue();
                benchmarks.forEach(benchmarkReport -> {
                    String name = benchmarkReport.getName();
                    benchmarkReport.setClassFingerprint(classFingerprints.get(name));
                    benchmarkReport.setGeneratedFingerprint(generatedFingerprints.get(name));
                    benchmarkReport.setManualFingerprint(manualFingerprints.get(name));
                    try {
                        JMHUtils.ClassAndMethod classAndMethod = new JMHUtils.ClassAndMethod(name).invoke();
                        String clazz = classAndMethod.getClazz();
                        String method = classAndMethod.getMethod();
                        LOG.info("Adding metadata for benchmark: " + clazz + " test: " + method);
                        Class<?> aClass = Class.forName(clazz);
                        Optional<Method> benchmarkMethod = JMHUtils.getBenchmarkMethod(method, aClass);
                        appendMetadataFromClass(aClass, benchmarkReport);
                        appendMetadataFromAnnotated(benchmarkMethod, benchmarkReport);
                        appendMetadataFromJavaDoc(aClass, benchmarkMethod, benchmarkReport);
                        syncReportsMetadata(report, benchmarkReport);
                        benchmarkSetting.put(Constants.REPORT_VERSION, getRunnerVersion());
                    } catch (ClassNotFoundException e) {
                        LOG.error("Failed to load class: {}", name);
                    }
                    report.setBenchmarkSettings(benchmarkSetting);
                });
            }
            if (report.getBenchmarks() != null && !report.getBenchmarks().isEmpty()) {
                List<BenchmarkReport> customBenchmarksCategoryCheck = report.getBenchmarks().get("CUSTOM");
                report.getBenchmarks().remove("CUSTOM");
                if (customBenchmarksCategoryCheck != null) {
                    for (BenchmarkReport benchReport : customBenchmarksCategoryCheck) {
                        report.addToBenchmarks(benchReport);
                    }
                }
                report.computeScores();
                getReportUploadStatus(report);
            }
            try {
                LOG.info("Generating JSON report...");
                String reportJSON;
                String reportEncrypted = ReportingService.getInstance().prepareReportForDelivery(securityBuilder,
                        report);
                String deviceReports = null;
                String resultURL = null;
                Map<?, ?> response = new HashMap<>();
                if (shouldSendReport(report)) {
                    String reportUploadToken = getProperty(Constants.USER_REPORT_TOKEN);
                    String queryToken = getProperty(Constants.USER_QUERY_TOKEN);
                    String emailAddress = getProperty(Constants.USER_EMAIL_ADDRESS);

                    String tokenAndEmail = ComputationUtils.getRequestHeader(reportUploadToken, emailAddress);
                    String responseWithUrl = DeliveryService.getInstance().sendReportForStoring(reportEncrypted,
                            tokenAndEmail, queryToken);
                    if (StringUtils.isNotEmpty(responseWithUrl)) {
                        response = JSONUtils.parseJsonIntoMap(responseWithUrl);
                    }
                    if (!response.isEmpty() && !isErrorResponse(response)) {
                        deviceReports = String.valueOf(response.get(Constants.REPORT_USER_URL));
                        resultURL = String.valueOf(response.get(Constants.REPORT_URL));
                        report.setDeviceReportsURL(deviceReports);
                        report.setReportURL(resultURL);
                    }
                } else {
                    // LOG.info(REPORT_NOT_SENT, CYB_REPORT_CYB_FILE, Constants.CYB_UPLOAD_URL);
                }
                // LOG.info("-----------------------------------------------------------------------------------------");
                // LOG.info("REPORT '{}'", report);
                // LOG.info("-----------------------------------------------------------------------------------------");
                reportJSON = JSONUtils.marshalToPrettyJson(report);
                String cybReportJsonFile = getCybReportFileName(report, CYB_REPORT_JSON_FILE);
                String cybReportFile = getCybReportFileName(report, CYB_REPORT_CYB_FILE);
                if (cybReportJsonFile.equals(CYB_REPORT_JSON_FILE) && cybReportFile.equals(CYB_REPORT_CYB_FILE)) {
                    cybReportJsonFile = IOUtils.getReportsPath("",
                            ComputationUtils.createFileNameForReport("report", start, report.getTotalScore(), false));
                    cybReportFile = IOUtils.getReportsPath("",
                            ComputationUtils.createFileNameForReport("report", start, report.getTotalScore(), true));
                }
                LOG.info("Saving test results to '{}'", cybReportJsonFile);
                IOUtils.storeResultsToFile(cybReportJsonFile, reportJSON);
                LOG.info("Saving encrypted test results to '{}'", cybReportFile);
                IOUtils.storeResultsToFile(cybReportFile, reportEncrypted);

                LOG.info("Removing all temporary auto-generated files...");
                IOUtils.removeTestDataFiles();
                LOG.info("Removed all temporary auto-generated files!!!");
                if (!response.isEmpty() && !isErrorResponse(response)) {
                    LOG.info("Benchmark report submitted successfully to {}", Constants.REPORT_URL);
                    LOG.info("You can find all device benchmarks on {}", deviceReports);
                    LOG.info("Your report is available at {}", resultURL);
                    LOG.info("NOTE: It may take a few minutes for your report to appear online");

                    if (response.containsKey("automatedComparisons")) {
                        List<Map<String, Object>> automatedComparisons = (List<Map<String, Object>>) response
                                .get("automatedComparisons");
                        if (tooManyAnomalies(automatedComparisons)) {
                            System.exit(1);
                        }
                    }
                } else {
                    String errMsg = getErrorResponseMessage(response);
                    if (errMsg != null) {
                        LOG.error("CyBench backend service sent error response: {}", errMsg);
                    }
                    LOG.info(REPORT_NOT_SENT, CYB_REPORT_CYB_FILE, Constants.CYB_UPLOAD_URL);
                }
            } catch (Throwable e) {
                LOG.error("Failed to save test results", e);
                LOG.info(REPORT_NOT_SENT, CYB_REPORT_CYB_FILE, Constants.CYB_UPLOAD_URL);
            }
        } catch (MissingResourceException exc) {
        } finally {
            LOG.info("-----------------------------------------------------------------------------------------");
            LOG.info("                           Finished CyBench benchmarking ({})                            ",
                    ComputationUtils.formatInterval(System.currentTimeMillis() - start));
            LOG.info("-----------------------------------------------------------------------------------------");
        }
    }

    public static boolean isErrorResponse(Map<?, ?> response) {
        return response != null && (response.containsKey("error") || response.containsKey("ERROR"));
    }

    public static String getErrorResponseMessage(Map<?, ?> response) {
        if (response == null) {
            return null;
        }
        String errMsg = (String) response.get("error");
        if (errMsg == null) {
            errMsg = (String) response.get("ERROR");
        }
        return errMsg;
    }

    private static void appendMetadataFromJavaDoc(Class<?> aClass, Optional<Method> benchmarkMethod,
            BenchmarkReport benchmarkReport) {
        String key = aClass.getName() + "." + (benchmarkMethod.map(Method::getName).orElse(""));

        Properties p = new Properties();
        try (InputStream benchJavaDoc = ClassLoader.getSystemResourceAsStream("benchJavaDoc")) {
            if (benchJavaDoc == null) {
                LOG.info("No javadoc descriptions found");
                return;
            }
            p.load(benchJavaDoc);

            String property = p.getProperty(key);
            if (property != null) {
                LOG.info("Appending javadoc {} for {}", key, property);

                benchmarkReport.addMetadata("description", property);
            }
        } catch (IOException e) {
            LOG.error("Cannot load Javadoc", e);
        }
    }

    private static void customBenchmarksCategoryCheck() {
    }

    private static ChainedOptionsBuilder setMeasurementProperties(ChainedOptionsBuilder optionBuilder, int forks,
            int measurementIterations, int measurementSeconds, int warmUpIterations, int warmUpSeconds, int threads,
            Set<Mode> modes) {
        if (forks != Fork.BLANK_FORKS) {
            optionBuilder = optionBuilder.forks(forks);
        }
        if (measurementIterations != Measurement.BLANK_ITERATIONS) {
            optionBuilder = optionBuilder.measurementIterations(measurementIterations);
        }
        if (warmUpIterations != Warmup.BLANK_ITERATIONS) {
            optionBuilder = optionBuilder.warmupIterations(warmUpIterations);
        }
        if (warmUpSeconds != Warmup.BLANK_TIME) {
            optionBuilder = optionBuilder.warmupTime(TimeValue.seconds(warmUpSeconds));
        }
        if (threads != Threads.MAX) {
            optionBuilder = optionBuilder.threads(threads);
        }
        if (measurementSeconds != Measurement.BLANK_TIME) {
            optionBuilder = optionBuilder.measurementTime(TimeValue.seconds(measurementSeconds));
        }
        if (modes != null) {
            for (Mode mode : modes) {
                optionBuilder = optionBuilder.mode(mode);
            }
        }
        return optionBuilder;
    }

    protected static void appendMetadataFromClass(Class<?> aClass, BenchmarkReport benchmarkReport) {
        if (!aClass.isInterface() && !aClass.getSuperclass().equals(Object.class)) {
            appendMetadataFromClass(aClass.getSuperclass(), benchmarkReport);
        }
        for (Class<?> anInterface : aClass.getInterfaces()) {
            appendMetadataFromClass(anInterface, benchmarkReport);
        }

        appendMetadataFromAnnotated(Optional.of(aClass), benchmarkReport);
    }

    /**
     * Resolve and add benchmark annotation to report
     * 
     * @param annotated
     *            benchmark annotated objects
     * @param benchmarkReport
     *            report data object
     */
    public static void appendMetadataFromAnnotated(Optional<? extends AnnotatedElement> annotated,
            BenchmarkReport benchmarkReport) {
        if (annotated.isPresent()) {
            CyBenchMetadataList annotation = annotated.get().getDeclaredAnnotation(CyBenchMetadataList.class);
            if (annotation != null) {
                Arrays.stream(annotation.value()).forEach(annot -> {
                    checkSetOldMetadataProps(annot.key(), annot.value(), benchmarkReport);
                    benchmarkReport.addMetadata(annot.key(), annot.value());
                    LOG.info("added metadata(1) " + annot.key() + "=" + annot.value());
                });
            }
            BenchmarkMetaData singleAnnotation = annotated.get().getDeclaredAnnotation(BenchmarkMetaData.class);
            if (singleAnnotation != null) {
                checkSetOldMetadataProps(singleAnnotation.key(), singleAnnotation.value(), benchmarkReport);
                benchmarkReport.addMetadata(singleAnnotation.key(), singleAnnotation.value());
            }
        }
    }

    /**
     * Synchronizes overview and benchmark reports metadata.
     * 
     * @param report
     *            overview report object
     * @param benchmarkReport
     *            report data object
     */
    public static void syncReportsMetadata(BenchmarkOverviewReport report, BenchmarkReport benchmarkReport) {
        try {
            String projectVersion = PROJECT_METADATA_MAP.get(Constants.PROJECT_VERSION);
            String projectArtifactId = PROJECT_METADATA_MAP.get(Constants.PROJECT_NAME);

            if (StringUtils.isNotEmpty(benchmarkReport.getProject())) {
                report.setProject(benchmarkReport.getProject());
            } else {
                LOG.info("* Project name metadata not defined, grabbing it from build files...");
                report.setProject(projectArtifactId);
                benchmarkReport.setProject(projectArtifactId);
            }

            if (StringUtils.isNotEmpty(benchmarkReport.getProjectVersion())) {
                report.setProjectVersion(benchmarkReport.getProjectVersion());
            } else {
                LOG.info("* Project version metadata not defined, grabbing it from build files...");
                report.setProjectVersion(projectVersion); // default
                benchmarkReport.setProjectVersion(projectVersion);
            }

            if (StringUtils.isEmpty(benchmarkReport.getVersion())) {
                benchmarkReport.setVersion(projectVersion);
            }

            if (StringUtils.isEmpty(report.getBenchmarkSessionId())) {
                Map<String, String> bMetadata = benchmarkReport.getMetadata();
                if (bMetadata != null) {
                    String sessionId = bMetadata.get("benchSession");
                    if (StringUtils.isNotEmpty(sessionId)) {
                        report.setBenchmarkSessionId(sessionId);
                    }
                }
            }

            if (benchmarkReport.getCategory().equals("CUSTOM")) {
                int classIndex = benchmarkReport.getName().lastIndexOf(".");
                if (classIndex > 0) {
                    String pckgAndClass = benchmarkReport.getName().substring(0, classIndex);
                    int pckgIndex = pckgAndClass.lastIndexOf(".");
                    if (pckgIndex > 0) {
                        String pckg = pckgAndClass.substring(0, pckgIndex);
                        benchmarkReport.setCategory(pckg);
                    } else {
                        benchmarkReport.setCategory(pckgAndClass);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error while attempting to synchronize benchmark metadata from runner: ", e);
        }
    }

    /**
     * A method needed in order to support the previous data model. Setting the needed values from annotation to a
     * previously defined data model value
     * 
     * @param key
     *            property key
     * @param value
     *            value to set for the key found
     * @param benchmarkReport
     *            report data object
     */
    public static void checkSetOldMetadataProps(String key, String value, BenchmarkReport benchmarkReport) {
        if ("api".equals(key)) {
            benchmarkReport.setCategory(value);
        }
        if ("context".equals(key)) {
            benchmarkReport.setContext(value);
        }
        if ("version".equals(key)) {
            benchmarkReport.setVersion(value);
        }
        if ("project".equals(key)) {
            benchmarkReport.setProject(value);
        }
        if ("projectVersion".equals(key)) {
            benchmarkReport.setProjectVersion(value);
        }
    }

    private static boolean shouldSendReport(BenchmarkOverviewReport report) {
        if (report.getEnvironmentSettings().get("environment") instanceof HardwareProperties.EmptyHardwareProperties) {
            return false;
        }
        return report.isEligibleForStoringExternally() && (getProperty(Constants.SEND_REPORT) == null
                || Boolean.parseBoolean(getProperty(Constants.SEND_REPORT)));
    }

    private static String getCybReportFileName(BenchmarkOverviewReport report, String nameTemplate) {
        if (Boolean.parseBoolean(getProperty(Constants.APPEND_SCORE_TO_FNAME))) {
            String start = nameTemplate.substring(0, nameTemplate.lastIndexOf('.'));
            String end = nameTemplate.substring(nameTemplate.lastIndexOf('.'));
            return start + "-" + com.gocypher.cybench.core.utils.JSONUtils
                    .convertNumToStringByLength(String.valueOf(report.getTotalScore())) + end;
        } else {
            return nameTemplate;
        }
    }

    public static String getProperty(String key) {
        return System.getProperty(key, cfg.getProperty(key));
    }

    private static String getRunnerVersion() {
        Properties properties = new Properties();
        try (InputStream is = BenchmarkRunner.class.getResourceAsStream("/runner.properties")) {
            properties.load(is);
        } catch (IOException e) {
            LOG.error("Failed to read runner.properties file", e);
        }

        return properties.getProperty("version");
    }

    public static void getReportUploadStatus(BenchmarkOverviewReport report) {
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

    private static int setExecutionIntProperty(String property) {
        if (checkIfConfigurationPropertyIsSet(property)) {
            return Integer.parseInt(property);
        } else {
            return -1;
        }
    }

    private static String setExecutionStringProperty(String property) {
        if (checkIfConfigurationPropertyIsSet(property)) {
            return property;
        } else {
            return null;
        }
    }

    private static Set<Mode> setExecutionModes(String property) {
        if (checkIfConfigurationPropertyIsSet(property)) {
            return Arrays.stream(property.split(",")).map(String::trim).map(Mode::deepValueOf)
                    .collect(Collectors.toSet());
        } else {
            return null;
        }
    }

    private static boolean checkIfConfigurationPropertyIsSet(String property) {
        return StringUtils.isNotEmpty(property);
    }

    private static void identifyPropertiesFromArguments(String[] args) {
        String configurationFilePath = "";
        String automationConfigurationFilePath = "";
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

            if (property.contains("automationCfg")) {
                String[] tempConfigPath = property.split("=");
                if (tempConfigPath.length > 1) {
                    automationConfigurationFilePath = tempConfigPath[1];
                } else {
                    LOG.info("Incorrect format, automation configuration path syntax: automationCfg='full-file-path'");
                }
            } else {
                LOG.info("Will search for automation configuration in default file {}",
                        automationConfigurationFilePath);
            }
        }
        cfg = ConfigurationHandler.loadConfiguration(configurationFilePath, Constants.LAUNCHER_CONFIGURATION);

        Properties automatedComparisonCfgProps = ConfigurationHandler.loadConfiguration(automationConfigurationFilePath,
                Constants.AUTOMATED_COMPARISON_CONFIGURATION);
        if (automatedComparisonCfgProps != null && !automatedComparisonCfgProps.isEmpty()) {
            automatedComparisonCfg = ConfigurationHandler.checkConfigValidity(automatedComparisonCfgProps);
        }
    }

    public static void printSystemInformation() {
        long kilobytes = 1024;
        long megabytes = kilobytes * 1024;
        long gigabytes = megabytes * 1024;

        String nameOS = "os.name";
        String versionOS = "os.version";
        String architectureOS = "os.arch";
        LOG.info("\n  Info about OS");
        LOG.info("\nName of the OS: {}", System.getProperty(nameOS));
        LOG.info("Version of the OS: {}", System.getProperty(versionOS));
        LOG.info("Architecture of The OS: {}", System.getProperty(architectureOS));
        Map<String, String> env = System.getenv();
        LOG.info("Environment values");
        for (Map.Entry<String, String> stringStringEntry : env.entrySet()) {
            LOG.info("K: {} \n\tV: {}", stringStringEntry.getKey(), stringStringEntry.getValue());
        }
        /* Total number of processors or cores available to the JVM */
        LOG.info("\nAvailable processors (cores): {}", Runtime.getRuntime().availableProcessors());

        /* Total amount of free memory available to the JVM */
        LOG.info("Free memory (megabytes): {}", Runtime.getRuntime().freeMemory() / (float) megabytes);

        /* This will return Long.MAX_VALUE if there is no preset limit */
        long maxMemory = Runtime.getRuntime().maxMemory();
        /* Maximum amount of memory the JVM will attempt to use */
        LOG.info("Maximum memory (megabytes): {}",
                (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory / (float) megabytes));

        /* Total memory currently available to the JVM */
        LOG.info("Total memory available to JVM (megabytes): {}",
                Runtime.getRuntime().totalMemory() / (float) megabytes);

        /* Get a list of all filesystem roots on this system */
        File[] roots = File.listRoots();

        /* For each filesystem root, print some info */
        for (File root : roots) {
            LOG.info("\nFile system root: {}", root.getAbsolutePath());
            LOG.info("Total space (gigabytes): {}", (root.getTotalSpace() / (float) gigabytes));
            LOG.info("Free space (gigabytes): {}", (root.getFreeSpace() / (float) gigabytes));
            LOG.info("Usable space (gigabytes): {}", (root.getUsableSpace() / (float) gigabytes));

        }
        LOG.info("\n\nProperties:\n------\n");
        try (PrintWriter pw = new PrintWriter(new StringWriter())) {
            System.getProperties().list(pw);
            LOG.info(pw.toString());
        }
    }

    public static void printGCStats() {
        long totalGarbageCollections = 0;
        long garbageCollectionTime = 0;

        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            LOG.info("GC: {};{}", gc.getName(), gc.getClass());

            long count = gc.getCollectionCount();

            if (count >= 0) {
                totalGarbageCollections += count;
            }

            long time = gc.getCollectionTime();

            if (time >= 0) {
                garbageCollectionTime += time;
            }
        }

        LOG.info("Total Garbage Collections: {}", totalGarbageCollections);
        LOG.info("Total Garbage Collection Time (ms): {}", garbageCollectionTime);
    }

    public static Map<String, String> checkProjectMetadataExists() throws MissingResourceException {
        PROJECT_METADATA_MAP.put(Constants.PROJECT_NAME, getMetadataFromBuildFile(Constants.PROJECT_NAME));
        PROJECT_METADATA_MAP.put(Constants.PROJECT_VERSION, getMetadataFromBuildFile(Constants.PROJECT_VERSION));
        // make sure gradle metadata can be parsed BEFORE benchmarks are run
        String metaProp = PROJECT_METADATA_MAP.get(Constants.PROJECT_NAME);
        if (StringUtils.isEmpty(metaProp)) {
            failBuildFromMissingMetadata("Project");
        } else {
            LOG.info("MetaData - Project name:    {}", metaProp);
        }
        metaProp = PROJECT_METADATA_MAP.get(Constants.PROJECT_VERSION);
        if (StringUtils.isEmpty(metaProp)) {
            failBuildFromMissingMetadata("Version");
        } else {
            LOG.info("MetaData - Project version: {}", metaProp);
        }
        return PROJECT_METADATA_MAP;
    }

    /**
     * Resolved metadata property value from set of project configuration files: pom.xml, build.gradle, etc.
     * 
     * @param prop
     *            metadata property name
     * @return metadata property value
     */
    public static String getMetadataFromBuildFile(String prop) throws MissingResourceException {
        String property = "";
        String userDir = System.getProperty("user.dir");
        File gradle = new File(userDir + "/build.gradle");
        File gradleKTS = new File(userDir + "/build.gradle.kts");
        File pom = new File(userDir + "/pom.xml");
        File projectProps = new File(userDir + "/config/project.properties");

        boolean pomAvailable = pom.exists();
        boolean gradleAvailable = gradle.exists() || gradleKTS.exists();
        boolean propsAvailable = projectProps.exists();

        if (gradleAvailable && pomAvailable) {
            LOG.info("Multiple build instructions detected, resolving to pom.xml...");
            property = getMetadataFromMaven(prop);
        } else if (gradleAvailable) {
            property = getMetadataFromGradle(prop);
        } else if (pomAvailable) {
            property = getMetadataFromMaven(prop);
        } else if (propsAvailable) {
            property = getMetadataFromProjectProperties(prop, projectProps.getPath());
        }
        return property;
    }

    private static String getMetadataFromMaven(String prop) throws MissingResourceException {
        String property = "";
        String userDir = System.getProperty("user.dir");
        File pom = new File(userDir + "/pom.xml");
        LOG.info("* Maven project detected, grabbing missing metadata from pom.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(pom);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("project");

            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    property = eElement.getElementsByTagName(prop).item(0).getTextContent();
                }
            }
        } catch (ParserConfigurationException e) {
            LOG.error("Error creating DocumentBuilder", e);
            failBuildFromMissingMavenMetadata();
        } catch (SAXException e) {
            LOG.error("SAX error", e);
            failBuildFromMissingMavenMetadata();
        } catch (IOException e) {
            LOG.error("Failed to read project file: {}", pom, e);
            failBuildFromMissingMavenMetadata();
        }
        return property;
    }

    private static String getMetadataFromGradle(String prop) throws MissingResourceException {
        // LOG.info("* Gradle project detected, grabbing missing metadata from gradle build files");
        // LOG.info("* Checking for Groovy or Kotlin style build instructions");
        String property = "";
        String dir = System.getProperty("user.dir");
        String switcher;
        File buildFile = new File(dir + "/settings.gradle");

        if (buildFile.exists()) {
            switcher = "groovy";
        } else {
            switcher = "kotlin";
        }

        // LOG.info("Prop is currently: {}", prop);
        switch (switcher) {
        case "groovy":
            // LOG.info("* Regular (groovy) build file detected, looking for possible metadata...");
            property = getGradleProperty(prop, dir, "/config/project.properties", "/settings.gradle",
                    "/version.gradle");
            break;
        case "kotlin":
            // LOG.info("* Kotlin style build file detected, looking for possible metadata...");
            property = getGradleProperty(prop, dir, "/config/project.properties", "/settings.gradle.kts",
                    "/version.gradle.kts");
            break;
        }

        return property;
    }

    private static String getMetadataFromProjectProperties(String prop, String propsFile) {
        if (prop == Constants.PROJECT_NAME) {
            prop = "PROJECT_ARTIFACT";
        } else {
            prop = "PROJECT_VERSION";
        }
        Properties props = loadProperties(propsFile);

        return props.getProperty(prop);
    }

    private static Properties loadProperties(String fileName) {
        Properties props = new Properties();
        File buildFile = new File(fileName);
        try (BufferedReader reader = new BufferedReader(new FileReader(buildFile))) {
            props.load(reader);
        } catch (IOException e) {
            LOG.error("Failed to read project properties file: {}", buildFile, e);
        }
        return props;
    }

    private static String getGradleProperty(String prop, String dir, String... cfgFiles)
            throws MissingResourceException {
        if (prop == Constants.PROJECT_NAME) {
            prop = "PROJECT_ARTIFACT";
        } else {
            prop = "PROJECT_VERSION";
        }
        Properties props = loadProperties(dir + cfgFiles[0]);
        String gradleProp = props.getProperty(prop);
        if (prop == "PROJECT_ARTIFACT" && !isPropUnspecified("PROJECT_ROOT")) { // for subprojects
            String parent = props.getProperty("PROJECT_ROOT");
            parent = parent.replaceAll("\\s", "").split("'")[1];
            if (parent.equals(gradleProp)) {
                return gradleProp;
            } else {
                return parent + "/" + gradleProp;
            }
        }
        if (prop == "PROJECT_ARTIFACT" && isPropUnspecified(gradleProp)) {
            String property = "";
            File buildFile = new File(dir + cfgFiles[1]);
            try (BufferedReader reader = new BufferedReader(new FileReader(buildFile))) {
                String line;
                prop = "rootProject.name";
                while ((line = reader.readLine()) != null) {
                    if (line.contains(prop)) {
                        // LOG.info("Found relevant metadata: {}", line);
                        line = line.replaceAll("\\s", "");
                        property = line.split("'")[1];
                    }
                }
            } catch (IOException e) {
                failBuildFromMissingMetadata("Project");
                LOG.error("Failed to read project file: {}", buildFile, e);
            }
            return property;
        }

        if (prop == "PROJECT_VERSION" && isPropUnspecified(gradleProp)) {
            String property = "";
            File buildFile = new File(dir + cfgFiles[2]);
            try (BufferedReader reader = new BufferedReader(new FileReader(buildFile))) {
                String line;
                prop = "version =";
                while ((line = reader.readLine()) != null) {
                    if (line.contains(prop)) {
                        LOG.info("Found relevant metadata: {}", line);
                        line = line.replaceAll("\\s", "");
                        property = line.split("'")[1];
                    }
                }
            } catch (IOException e) {
                failBuildFromMissingMetadata("Version");
                LOG.error("Failed to read project file: {}", buildFile, e);
            }
            return property;
        }
        return gradleProp;
    }

    private static boolean isPropUnspecified(String prop) {
        return StringUtils.isBlank(prop) || "unspecified".equals(prop);
    }

    public static void failBuildFromMissingMetadata(String metadata) throws MissingResourceException {
        LOG.error("* ===[Build failed from lack of metadata: (" + metadata + ")]===");
        LOG.error("* CyBench runner is unable to continue due to missing crucial metadata.");
        if (metadata.contains("Version")) {
            LOG.error("* Project version metadata was unable to be processed.");
            LOG.warn("* Project version can be set or parsed dynamically a few different ways: \n");
            LOG.warn("*** The quickest and easiest (Gradle) solution is by adding an Ant task to 'build.gradle'"
                    + " to generate 'project.properties' file.");
            LOG.warn("*** This Ant task can be found in the README for CyBench Gradle Plugin"
                    + " (https://github.com/K2NIO/gocypher-cybench-gradle/blob/master/README.md) \n");
            LOG.info("*** For Gradle (groovy) projects, please set 'version = \"<yourProjectVersionNumber>\"' in either "
                            + "'build.gradle' or 'version.gradle'.");
            LOG.info("*** For Gradle (kotlin) projects, please set 'version = \"<yourProjectVersionNumber>\"' in either "
                            + "'build.gradle.kts' or 'version.gradle.kts'.");
            LOG.info("*** For Maven projects, please make sure '<version>' tag is set correctly.\n");
            LOG.info("* If running benchmarks from a class you compiled/generated yourself via IDE plugin (Eclipse, Intellij, etc..),");
            LOG.info("* please set the @BenchmarkMetaData projectVersion tag at the class level");
            LOG.info("* e.g.: '@BenchmarkMetaData(key = \"projectVersion\", value = \"1.6.0\")'");
            LOG.info("* Project version can also be detected from 'metadata.properties' in your project's 'config' folder.");
            LOG.info("* If setting project version via 'metadata.properties', please add the following: ");
            LOG.info("* 'class.version=<yourProjectVersionNumber>'\n");
            LOG.warn("* For more information and instructions on this process, please visit the CyBench wiki at "
                    + "https://github.com/K2NIO/gocypher-cybench-java/wiki/Getting-started-with-CyBench-annotations");

            throw new MissingResourceException("Missing project metadata configuration", null, null);
        } else if (metadata.contains("Project")) {
            LOG.error("* Project name metadata was unable to be processed.");
            LOG.warn("* Project name can be set or parsed dynamically a few different ways: \n");
            LOG.warn("*** The quickest and easiest (Gradle) solution is by adding an Ant task to 'build.gradle'"
                    + " to generate 'project.properties' file.");
            LOG.warn("*** This Ant task can be found in the README for CyBench Gradle Plugin"
                    + " (https://github.com/K2NIO/gocypher-cybench-gradle/blob/master/README.md) \n");
            LOG.info("*** For Gradle (groovy) projects, please set 'rootProject.name = \"<yourProjectName>\"' in 'settings.gradle'.");
            LOG.info("*** For Gradle (kotlin) projects, please set 'rootProject.name = \"<yourProjectName>\"' in 'settings.gradle.kts'.");
            LOG.info("**** Important note regarding Gradle project's name: This value is read-only in 'build.gradle(.kts)'. This value *MUST*"
                            + " be set in 'settings.gradle(.kts)' if the project name isn't able to be dynamically parsed.");
            LOG.info("*** For Maven projects, please make sure '<artifactId>' tag is set correctly.\n");
            LOG.info("*** If running benchmarks from a class you compiled/generated yourself via IDE plugin (Eclipse, Intellij, etc..), "
                            + "please set the @BenchmarkMetaData project tag at the class level");
            LOG.info("**** e.g.: '@BenchmarkMetaData(key = \"project\", value = \"myTestProject\")'");
            LOG.info("*** Project version can also be detected from 'metadata.properties' in your project's 'config' folder.");
            LOG.info("*** If setting project version via 'metadata.properties', please add the following: ");
            LOG.info("*** 'class.project=<yourProjectName>'\n");
            LOG.warn("* For more information and instructions on this process, please visit the CyBench wiki at "
                    + "https://github.com/K2NIO/gocypher-cybench-java/wiki/Getting-started-with-CyBench-annotations");

            throw new MissingResourceException("Missing project metadata configuration", null, null);
        }
    }

    public static void failBuildFromMissingMavenMetadata() throws MissingResourceException {
        LOG.error("* ===[Build failed from lack of metadata]===");
        LOG.error("* CyBench runner is unable to continue due to missing crucial metadata.");
        LOG.error("* Error while parsing Maven project's 'pom.xml' file.");
        LOG.error("* 'artifactId' or 'version' tag was unable to be parsed. ");
        LOG.error("* Refer to the exception thrown for reasons why the .xml file was unable to be parsed.");
        LOG.warn("* For more information on CyBench metadata (setting it, how it is used, etc.), please visit the CyBench wiki at "
                        + "https://github.com/K2NIO/gocypher-cybench-java/wiki/Getting-started-with-CyBench-annotations");

        throw new MissingResourceException("Missing project metadata configuration", null, null);
    }

    @SuppressWarnings("unchecked")
    public static boolean tooManyAnomalies(List<Map<String, Object>> automatedComparisons) {
        for (Map<String, Object> automatedComparison : automatedComparisons) {
            Integer totalFailedBenchmarks = (Integer) automatedComparison.get("totalFailedBenchmarks");
            Map<String, Object> config = (Map<String, Object>) automatedComparison.get("config");
            if (config.containsKey("anomaliesAllowed")) {
                Integer anomaliesAllowed = (Integer) config.get("anomaliesAllowed");
                if (totalFailedBenchmarks != null && totalFailedBenchmarks > anomaliesAllowed) {
                    LOG.error(
                            "There were more anomaly benchmarks than your specified anomalies allowed in one of your automated comparison configurations!");
                    return true;
                }
            }
        }
        return false;
    }
}
