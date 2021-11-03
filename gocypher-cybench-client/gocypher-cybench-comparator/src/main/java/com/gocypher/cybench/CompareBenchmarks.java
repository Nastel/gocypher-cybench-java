/*
 * Copyright (C) 2020-2021, K2N.IO.
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

package com.gocypher.cybench;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gocypher.cybench.services.Requests;
import com.gocypher.cybench.utils.ComparatorScriptEngine;
import com.gocypher.cybench.utils.Comparisons;
import com.gocypher.cybench.utils.Comparisons.Scope;
import com.gocypher.cybench.utils.Comparisons.Threshold;
import com.gocypher.cybench.utils.ConfigHandling;

public class CompareBenchmarks {
    private static final Logger log = LoggerFactory.getLogger(CompareBenchmarks.class);
    public static int totalComparedBenchmarks = 0;
    private static final Map<String, Map<String, Map<String, Map<String, Object>>>> passedBenchmarks = new HashMap<>();
    public static int totalPassedBenchmarks = 0;
    private static final Map<String, Map<String, Map<String, Map<String, Object>>>> failedBenchmarks = new HashMap<>();
    public static int totalFailedBenchmarks = 0;
    public static boolean failBuildFlag = false;

    private static final String AUTO_PASS_KEY = "autoPass";

    public static void main(String... args) throws Exception {
        logInfo("* Analyzing benchmark performance...");

        Options options = new Options();
        options.addOption("F", ConfigHandling.FAIL_BUILD_FLAG, false, "Fail build on failed comparisons");
        options.addOption("S", ConfigHandling.SCRIPT_PATH, true, "User JS script");
        options.addOption("C", ConfigHandling.CONFIG_PATH, true, "YAML config file");
        options.addOption("T", ConfigHandling.TOKEN, true, "CyBench access token");
        options.addOption("R", ConfigHandling.REPORT_PATH, true, "Report file/Report directory");
        options.addOption("m", ConfigHandling.METHOD, true, "Comparison method");
        options.addOption("r", ConfigHandling.RANGE, true, "Comparison range");
        options.addOption("s", ConfigHandling.SCOPE, true, "Comparison scope");
        options.addOption("v", ConfigHandling.COMPARE_VERSION, true, "Comparison version");
        options.addOption("t", ConfigHandling.THRESHOLD, true, "Comparison threshold");
        options.addOption("p", ConfigHandling.PERCENT_CHANGE_ALLOWED, true, "Comparison percent change allowed");
        options.addOption("d", ConfigHandling.DEVIATIONS_ALLOWED, true, "Comparison deviations allowed");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("F")) {
            logWarn("Build will fail if any benchmark comparison assertions fail\n");
            failBuildFlag = true;
        }

        Map<String, String> passedProps = new HashMap<>();

        passedProps.put(ConfigHandling.SCRIPT_PATH, cmd.getOptionValue("S"));
        passedProps.put(ConfigHandling.CONFIG_PATH, cmd.getOptionValue("C"));
        passedProps.put(ConfigHandling.TOKEN, cmd.getOptionValue("T"));
        passedProps.put(ConfigHandling.REPORT_PATH, cmd.getOptionValue("R"));
        passedProps.put(ConfigHandling.METHOD, cmd.getOptionValue("m"));
        passedProps.put(ConfigHandling.RANGE, cmd.getOptionValue("r"));
        passedProps.put(ConfigHandling.SCOPE, cmd.getOptionValue("s"));
        passedProps.put(ConfigHandling.COMPARE_VERSION, cmd.getOptionValue("v"));
        passedProps.put(ConfigHandling.THRESHOLD, cmd.getOptionValue("t"));
        passedProps.put(ConfigHandling.PERCENT_CHANGE_ALLOWED, cmd.getOptionValue("p"));
        passedProps.put(ConfigHandling.DEVIATIONS_ALLOWED, cmd.getOptionValue("d"));

        for (Map.Entry<String, String> pEntry : passedProps.entrySet()) {
            String prop = pEntry.getValue();
            if (prop != null) {
                prop = prop.replaceAll("\\s+", "");
                passedProps.put(pEntry.getKey(), prop);
            }
        }

        String scriptPath = passedProps.get(ConfigHandling.SCRIPT_PATH);
        String configPath = passedProps.get(ConfigHandling.CONFIG_PATH);

        if (scriptPath != null) {
            logInfo("Attempting to evaluate custom defined script at {}\n", scriptPath);

            ComparatorScriptEngine cse = new ComparatorScriptEngine(passedProps, scriptPath);
        } else {
            if (configPath == null) {
                logInfo("No script or config file specified, looking for comparator.yaml in default location");
                configPath = ConfigHandling.DEFAULT_COMPARATOR_CONFIG_PATH;
            }

            logInfo("Attempting to load comparator configurations at {}\n", configPath);
            Map<String, Object> allConfigs = ConfigHandling.loadYaml(configPath);
            Map<String, String> configuredPackages = ConfigHandling.identifyAndValidifySpecificConfigs(allConfigs);

            File recentReport = ConfigHandling
                    .identifyRecentReport((String) allConfigs.get(ConfigHandling.REPORT_PATH));
            String accessToken = (String) allConfigs.get(ConfigHandling.TOKEN);

            if (recentReport != null && accessToken != null) {
                analyzeBenchmarks(accessToken, recentReport, allConfigs, configuredPackages);
            } else {
                if (recentReport == null) {
                    logErr("* No recent report found to compare!");
                } else if (accessToken == null) {
                    logErr("* Failed to authorize provided access token!");
                }
            }
        }
    }

    private static void analyzeBenchmarks(String accessToken, File recentReport, Map<String, Object> allConfigs,
            Map<String, String> configuredPackages) throws Exception {
        Map<String, Map<String, Map<String, Double>>> recentReports = Requests.getBenchmarksFromReport(accessToken,
                recentReport);
        if (recentReports != null) {
            for (Map.Entry<String, Map<String, Map<String, Double>>> repEntry : recentReports.entrySet()) {
                String benchmarkFingerprint = repEntry.getKey();
                Map<String, Map<String, Double>> versionsTested = repEntry.getValue();
                for (Map.Entry<String, Map<String, Double>> vEntry : versionsTested.entrySet()) {
                    Map<String, Double> modesTested = vEntry.getValue();
                    for (Map.Entry<String, Double> stringDoubleEntry : modesTested.entrySet()) {
                        Double benchmarkScore = stringDoubleEntry.getValue();
                        String benchmarkName = Requests.fingerprintsToNames.get(benchmarkFingerprint);

                        compareBenchmark(benchmarkName, benchmarkFingerprint, vEntry.getKey(),
                                stringDoubleEntry.getKey(), benchmarkScore, allConfigs, configuredPackages);
                    }
                }
            }

            System.out.print("\n\n");
            logInfo("compared={}, passed={}, failed={}", totalComparedBenchmarks, totalPassedBenchmarks,
                    totalFailedBenchmarks);
            System.out.print("\n");
            printBenchmarkResults(Requests.namesToFingerprints);
            buildFailureCheck();
        }
    }

    private static Map<String, Object> prepareCompareDataMap(
            Map<String, Map<String, Map<String, Map<String, Object>>>> benchmarks, String benchmarkName,
            String benchmarkVersion, String benchmarkMode) {
        if (!benchmarks.containsKey(benchmarkName)) {
            Map<String, Object> data = new HashMap<>();
            Map<String, Map<String, Object>> dataPerMode = new HashMap<>();
            Map<String, Map<String, Map<String, Object>>> dataPerVersion = new HashMap<>();
            dataPerMode.put(benchmarkMode, data);
            dataPerVersion.put(benchmarkVersion, dataPerMode);
            benchmarks.put(benchmarkName, dataPerVersion);
        }

        Map<String, Map<String, Map<String, Object>>> dataPerVersion = benchmarks.get(benchmarkName);
        if (!dataPerVersion.containsKey(benchmarkVersion)) {
            Map<String, Object> data = new HashMap<>();
            Map<String, Map<String, Object>> dataPerMode = new HashMap<>();
            dataPerMode.put(benchmarkMode, data);
            dataPerVersion.put(benchmarkVersion, dataPerMode);
        }

        Map<String, Map<String, Object>> dataPerMode = dataPerVersion.get(benchmarkVersion);
        if (!dataPerMode.containsKey(benchmarkMode)) {
            Map<String, Object> data = new HashMap<>();
            dataPerMode.put(benchmarkMode, data);
        }

        return dataPerMode.get(benchmarkMode);
    }

    private static void addPassFailBenchData(Map<String, Map<String, Map<String, Map<String, Object>>>> benchmarks,
            Double benchmarkScore, Double COMPARE_VALUE, String benchmarkName, String benchmarkVersion,
            String benchmarkMode, Comparisons.Method compareMethod, Comparisons.Scope compareScope, String compareRange,
            Comparisons.Threshold compareThreshold, Double percentChangeAllowed, Double deviationsAllowed,
            String compareVersion) {
        Map<String, Object> data = prepareCompareDataMap(benchmarks, benchmarkName, benchmarkVersion, benchmarkMode);
        data.put("benchmarkScore", benchmarkScore);
        data.put("COMPARE_VALUE", COMPARE_VALUE);
        data.put(ConfigHandling.METHOD, compareMethod);
        data.put(ConfigHandling.SCOPE, compareScope);
        data.put(ConfigHandling.RANGE, compareRange);
        data.put(ConfigHandling.THRESHOLD, compareThreshold);
        data.put(ConfigHandling.COMPARE_VERSION, compareVersion);
        data.put(ConfigHandling.PERCENT_CHANGE_ALLOWED, percentChangeAllowed);
        data.put(ConfigHandling.DEVIATIONS_ALLOWED, deviationsAllowed);
    }

    private static void addAutoPassBenchData(Double benchmarkScore, String benchmarkName, String benchmarkVersion,
            String benchmarkMode) {
        Map<String, Object> data = prepareCompareDataMap(passedBenchmarks, benchmarkName, benchmarkVersion,
                benchmarkMode);
        data.put("benchmarkScore", benchmarkScore);
        data.put(AUTO_PASS_KEY, "true");
    }

    private static void printBenchmarkResults(Map<String, String> namesToFingerprints) {
        if (totalPassedBenchmarks > 0) {
            printBenchmarkResultsHelper(true, totalPassedBenchmarks, passedBenchmarks, namesToFingerprints);
        }
        System.out.print("\n");
        if (totalFailedBenchmarks > 0) {
            printBenchmarkResultsHelper(false, totalFailedBenchmarks, failedBenchmarks, namesToFingerprints);
        }
        System.out.print("\n");
        logInfo("* Completed benchmark analysis\n");
    }

    private static void printBenchmarkResultsHelper(boolean passfail, int totalBenchmarksToReport,
            Map<String, Map<String, Map<String, Map<String, Object>>>> benchmarksToReport,
            Map<String, String> namesToFingerprints) {
    	String passfailStr = passfail ? "PASSED" : "FAILED";
        logInfo("** {}/{} benchmarks {}:", totalBenchmarksToReport, totalComparedBenchmarks, passfailStr);
        for (Map.Entry<String, Map<String, Map<String, Map<String, Object>>>> brEntry : benchmarksToReport.entrySet()) {
            String benchmarkName = brEntry.getKey();
            String fingerprint = namesToFingerprints.get(benchmarkName);
            Map<String, Map<String, Map<String, Object>>> benchmarkVersions = brEntry.getValue();
            for (Map.Entry<String, Map<String, Map<String, Object>>> bvEntry : benchmarkVersions.entrySet()) {
                String benchmarkVersion = bvEntry.getKey();
                Map<String, Map<String, Object>> benchmarksData = bvEntry.getValue();
                for (Map.Entry<String, Map<String, Object>> bdEntry : benchmarksData.entrySet()) {
                    String benchmarkMode = bdEntry.getKey();
                    Map<String, Object> benchmarkData = bdEntry.getValue();
                    Double benchmarkScore = (Double) benchmarkData.get("benchmarkScore");
                    if (!benchmarkData.containsKey(AUTO_PASS_KEY)) {
                        Double COMPARE_VALUE = (Double) benchmarkData.get("COMPARE_VALUE");
                        Comparisons.Method compareMethod = (Comparisons.Method) benchmarkData
                                .get(ConfigHandling.METHOD);
                        Comparisons.Scope compareScope = (Comparisons.Scope) benchmarkData.get(ConfigHandling.SCOPE);
                        String compareRange = (String) benchmarkData.get(ConfigHandling.RANGE);
                        String compareVersion = (String) benchmarkData.get(ConfigHandling.COMPARE_VERSION);

                        String compareStr;
                        if (compareMethod == Comparisons.Method.DELTA) {
                            compareStr = "test.delta={}, ";
                        } else {
                            compareStr = "test.SDsFromMean={}, ";
                        }

                        StringBuilder logReport = new StringBuilder(
                                "   test.name={}, test.version={}, test.mode={}, test.score={}, " + compareStr
                                        + "test.compare.method={}, "
                                        + "test.compare.scope={}, test.compare.version={}, test.compare.range={}, ");

                        if (compareMethod.equals(Comparisons.Method.DELTA)) {
                            Comparisons.Threshold compareThreshold = (Comparisons.Threshold) benchmarkData
                                    .get(ConfigHandling.THRESHOLD);
                            logReport.append("test.compare.threshold=").append(compareThreshold).append(", ");
                            if (compareThreshold.equals(Comparisons.Threshold.PERCENT_CHANGE)) {
                                Double percentChangeAllowed = (Double) benchmarkData
                                        .get(ConfigHandling.PERCENT_CHANGE_ALLOWED);
                                logReport.append("test.percentChangeAllowed=").append(percentChangeAllowed)
                                        .append(", ");
                            }
                        } else {
                            Double deviationsAllowed = (Double) benchmarkData.get(ConfigHandling.DEVIATIONS_ALLOWED);
                            logReport.append("test.deviationsAllowed=").append(deviationsAllowed).append(", ");
                        }

                        logReport.append("test.id={}");

                        if (passfail)
                        	logInfo(logReport.toString(), benchmarkName, benchmarkVersion, benchmarkMode, benchmarkScore,
                                COMPARE_VALUE, compareMethod, compareScope, compareVersion, compareRange, fingerprint);
                        else 
                        	logErr(logReport.toString(), benchmarkName, benchmarkVersion, benchmarkMode, benchmarkScore,
                                    COMPARE_VALUE, compareMethod, compareScope, compareVersion, compareRange, fingerprint);
                    } else {
                    	if (passfail)
                    		logInfo("   NO COMPARISON: test.name={}, test.version={}, test.mode={}, test.score={}",
                                benchmarkName, benchmarkVersion, benchmarkMode, benchmarkScore);
                    	else
                    		logErr("   NO COMPARISON: test.name={}, test.version={}, test.mode={}, test.score={}",
                                    benchmarkName, benchmarkVersion, benchmarkMode, benchmarkScore);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getConfigs(String benchmarkName, Map<String, Object> allConfigs,
            Map<String, String> configuredPackages) {
        Map<String, Object> configs = null;

        Comparisons.Method compareMethod = null;
        Comparisons.Scope compareScope = null;
        String compareRange = null;
        Comparisons.Threshold compareThreshold = null;
        Double percentChangeAllowed = null;
        String compareVersion = null;
        Double deviationsAllowed = null;

        if (allConfigs.containsKey(ConfigHandling.DEFAULT_IDENTIFIER_HEADER)) {
            Map<String, Object> defaultConfigs = (Map<String, Object>) allConfigs
                    .get(ConfigHandling.DEFAULT_IDENTIFIER_HEADER);
            compareMethod = (Comparisons.Method) defaultConfigs.get(ConfigHandling.METHOD);
            compareScope = (Comparisons.Scope) defaultConfigs.get(ConfigHandling.SCOPE);
            compareRange = (String) defaultConfigs.get(ConfigHandling.RANGE);
            compareThreshold = (Comparisons.Threshold) defaultConfigs.get(ConfigHandling.THRESHOLD);
            percentChangeAllowed = (Double) defaultConfigs.get(ConfigHandling.PERCENT_CHANGE_ALLOWED);
            compareVersion = (String) defaultConfigs.get(ConfigHandling.COMPARE_VERSION);
            deviationsAllowed = (Double) defaultConfigs.get(ConfigHandling.DEVIATIONS_ALLOWED);
            configs = createConfigMap(compareMethod, compareScope, compareRange, compareThreshold, percentChangeAllowed,
                    compareVersion, deviationsAllowed);
        }

        for (Map.Entry<String, String> pkgEntry : configuredPackages.entrySet()) {
            if (pkgEntry.getKey() != null && benchmarkName.startsWith(pkgEntry.getKey())) {
                String identifier = pkgEntry.getValue();
                Map<String, Object> specificConfigs = (Map<String, Object>) allConfigs.get(identifier);
                compareMethod = (Comparisons.Method) specificConfigs.get(ConfigHandling.METHOD);
                compareScope = (Comparisons.Scope) specificConfigs.get(ConfigHandling.SCOPE);
                compareRange = (String) specificConfigs.get(ConfigHandling.RANGE);
                compareThreshold = (Comparisons.Threshold) specificConfigs.get(ConfigHandling.THRESHOLD);
                compareVersion = (String) specificConfigs.get(ConfigHandling.COMPARE_VERSION);
                if (specificConfigs.containsKey(ConfigHandling.PERCENT_CHANGE_ALLOWED)) {
                    percentChangeAllowed = Double
                            .parseDouble(specificConfigs.get(ConfigHandling.PERCENT_CHANGE_ALLOWED).toString());
                }
                if (specificConfigs.containsKey(ConfigHandling.DEVIATIONS_ALLOWED)) {
                    deviationsAllowed = Double
                            .parseDouble(specificConfigs.get(ConfigHandling.DEVIATIONS_ALLOWED).toString());
                }
                configs = createConfigMap(compareMethod, compareScope, compareRange, compareThreshold,
                        percentChangeAllowed, compareVersion, deviationsAllowed);
                break;
            }
        }
        return configs;
    }

    private static Map<String, Object> createConfigMap(Comparisons.Method compareMethod, Comparisons.Scope compareScope,
            String compareRange, Comparisons.Threshold compareThreshold, Double percentChangeAllowed,
            String compareVersion, Double deviationsAllowed) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConfigHandling.METHOD, compareMethod);
        configs.put(ConfigHandling.SCOPE, compareScope);
        configs.put(ConfigHandling.RANGE, compareRange);
        configs.put(ConfigHandling.THRESHOLD, compareThreshold);
        configs.put(ConfigHandling.PERCENT_CHANGE_ALLOWED, percentChangeAllowed);
        configs.put(ConfigHandling.COMPARE_VERSION, compareVersion);
        configs.put(ConfigHandling.DEVIATIONS_ALLOWED, deviationsAllowed);
        return configs;
    }

    private static boolean compareBenchmark(String benchmarkName, String benchmarkFingerprint, String benchmarkVersion,
            String benchmarkMode, Double benchmarkScore, Map<String, Object> allConfigs,
            Map<String, String> configuredPackages) {

        Map<String, Object> configMap = getConfigs(benchmarkName, allConfigs, configuredPackages);

        if (configMap != null) {
            Comparisons.Method compareMethod = (Comparisons.Method) configMap.get(ConfigHandling.METHOD);
            Comparisons.Scope compareScope = (Scope) configMap.get(ConfigHandling.SCOPE);
            String compareRange = (String) configMap.get(ConfigHandling.RANGE);
            Comparisons.Threshold compareThreshold = (Threshold) configMap.get(ConfigHandling.THRESHOLD);
            Double percentChangeAllowed = (Double) configMap.get(ConfigHandling.PERCENT_CHANGE_ALLOWED);
            String compareVersion = (String) configMap.get(ConfigHandling.COMPARE_VERSION);
            Double deviationsAllowed = (Double) configMap.get(ConfigHandling.DEVIATIONS_ALLOWED);

            Double COMPARE_VALUE = null;

            List<Double> benchmarkVersionScores = Requests.getBenchmarks(benchmarkFingerprint, benchmarkVersion,
                    benchmarkMode);

            // default the comparison scores to the current version without the newly added
            // benchmark
            List<Double> compareVersionScores = new ArrayList<>(benchmarkVersionScores);
            compareVersionScores.remove(benchmarkVersionScores.size() - 1);

            if (compareScope.equals(Comparisons.Scope.BETWEEN)) {
                if (compareVersion.equals(ConfigHandling.DEFAULT_COMPARE_VERSION)) {
                    compareVersion = Requests.getPreviousVersion(benchmarkFingerprint);
                    logInfo(
                            "{} - {}: Compare Version specified as 'PREVIOUS', setting compare version to previous benchmarked version {}",
                            benchmarkName, benchmarkMode, compareVersion);
                }

                if (benchmarkVersion.equals(compareVersion)) {
                    logWarn(
                            "{} - {}: the compare version specified ({}) is the same as the currently benchmarked version ({}), will perform WITHIN VERSION comparisons",
                            benchmarkName, benchmarkMode, compareVersion, benchmarkVersion);
                    compareScope = ConfigHandling.DEFAULT_COMPARE_SCOPE;
                } else if (Requests.getBenchmarks(benchmarkFingerprint).containsKey(compareVersion)
                        && Requests.getBenchmarks(benchmarkFingerprint, compareVersion).containsKey(benchmarkMode)) {
                    compareVersionScores = Requests.getBenchmarks(benchmarkFingerprint, compareVersion, benchmarkMode);
                } else {
                    logWarn(
                            "{} - {}: There are no benchmarks for the specified compare version ({}), no comparison will be run",
                            benchmarkName, benchmarkMode, compareVersion);
                    return autoPass(benchmarkScore, benchmarkName, benchmarkVersion, benchmarkMode);
                }
            }

            // range validation
            int range;
            if (compareRange.equals("ALL")) {
                range = compareVersionScores.size();
            } else {
                range = Integer.parseInt(compareRange);
                if (range > compareVersionScores.size()) {
                    logWarn(
                            "{} - {}: There are not enough values to compare to in version ({}) with specific range ({}), no comparison will be run",
                            benchmarkName, benchmarkMode, benchmarkVersion, range);
                    return autoPass(benchmarkScore, benchmarkName, benchmarkVersion, benchmarkMode);
                }
            }
            compareRange = String.valueOf(range);

            if (compareScope.equals(Comparisons.Scope.WITHIN)) {
                compareVersion = benchmarkVersion;
                if (benchmarkVersionScores.size() <= 1) {
                    logWarn(
                            "{} - {}: There are no previously tested benchmarks within the version ({}), no comparison will be run",
                            benchmarkName, benchmarkMode, benchmarkVersion);
                    return autoPass(benchmarkScore, benchmarkName, benchmarkVersion, benchmarkMode);
                }
            }

            switch (compareMethod) {
            case DELTA:
                logInfo("COMPARISON: {} : {} - Between versions {} and {} delta running", benchmarkName, benchmarkMode,
                        benchmarkVersion, compareVersion);
                COMPARE_VALUE = Comparisons.compareWithDelta(benchmarkVersionScores, compareVersionScores,
                        compareThreshold, compareRange);
                break;
            case SD:
                logInfo("COMPARISON: {} : {} - Between versions {} and {} SD running", benchmarkName, benchmarkMode,
                        benchmarkVersion, compareVersion);
                COMPARE_VALUE = Comparisons.compareWithSD(benchmarkVersionScores, compareVersionScores, compareRange);
                break;
            }

            boolean pass = Comparisons.passAssertion(COMPARE_VALUE, compareMethod, compareThreshold,
                    percentChangeAllowed, deviationsAllowed);

            addPassFailBenchData(pass ? passedBenchmarks : failedBenchmarks, benchmarkScore, COMPARE_VALUE,
                    benchmarkName, benchmarkVersion, benchmarkMode, compareMethod, compareScope, compareRange,
                    compareThreshold, percentChangeAllowed, deviationsAllowed, compareVersion);

            return true;
        } else {
            logWarn("{} - {}: There are no configurations set, no comparison will be run", benchmarkName,
                    benchmarkMode);
            return autoPass(benchmarkScore, benchmarkName, benchmarkVersion, benchmarkMode);
        }
    }

    // NO COMPARISON SHOULD BE RUN, PASS TEST
    private static boolean autoPass(Double benchmarkScore, String benchmarkName, String benchmarkVersion,
            String benchmarkMode) {
        totalComparedBenchmarks++;
        totalPassedBenchmarks++;
        addAutoPassBenchData(benchmarkScore, benchmarkName, benchmarkVersion, benchmarkMode);
        return false;
    }

    public static void buildFailureCheck() throws Exception {
        if (totalFailedBenchmarks > 0) {
            logWarn("* There are benchmark comparison failures! *");
            if (failBuildFlag) {
                String error = "* Build failed due to scores being too low *";
                logErr(error + "\n");
                // helps logs finish before exception is thrown
                TimeUnit.MILLISECONDS.sleep(500);
                throw new Exception(error);
            }
        }
    }
    
    public static void logInfo(String msg, Object... args) {
    	log.info(msg, args);
    }
    
    public static void logWarn(String msg, Object... args) {
    	log.warn(msg, args);
    }
    
    public static void logErr(String msg, Object... args) {
    	log.error(msg, args);
    }
}