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
import java.math.BigDecimal;
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

import com.gocypher.cybench.model.ComparedBenchmark;
import com.gocypher.cybench.model.ComparedBenchmark.CompareState;
import com.gocypher.cybench.model.ComparisonConfig;
import com.gocypher.cybench.model.ComparisonConfig.Scope;
import com.gocypher.cybench.model.ComparisonConfig.Method;
import com.gocypher.cybench.model.ComparisonConfig.Threshold;
import com.gocypher.cybench.services.Requests;
import com.gocypher.cybench.utils.ComparatorScriptEngine;
import com.gocypher.cybench.utils.Comparisons;
import com.gocypher.cybench.utils.ConfigHandling;
import com.gocypher.cybench.utils.WebpageGenerator;

public class CompareBenchmarks {
    private static final Logger log = LoggerFactory.getLogger(CompareBenchmarks.class);

    private static String accessToken = null;
        
    public static int totalComparedBenchmarksTest = 0;
    public static int totalPassedBenchmarksTest = 0;
    public static int totalFailedBenchmarksTest = 0;
    public static int totalSkippedBenchmarksTest = 0;
    public static List<ComparedBenchmark> comparedBenchmarksTest = new ArrayList<>();
    public static List<ComparedBenchmark> passedBenchmarksTest = new ArrayList<>();
    public static List<ComparedBenchmark> failedBenchmarksTest = new ArrayList<>();
    public static List<ComparedBenchmark> skippedBenchmarksTest = new ArrayList<>();



    private static boolean useScriptConfigForPage = false;
    public static int totalComparedBenchmarks = 0;
    public static final Map<String, Map<String, Map<String, Map<String, Object>>>> passedBenchmarks = new HashMap<>();
    public static int totalPassedBenchmarks = 0;
    public static final Map<String, Map<String, Map<String, Map<String, Object>>>> skippedBenchmarks = new HashMap<>();
    public static int totalSkippedBenchmarks = 0;
    public static final Map<String, Map<String, Map<String, Map<String, Object>>>> failedBenchmarks = new HashMap<>();
    public static int totalFailedBenchmarks = 0;
    public static boolean failBuildFlag = false;

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
            // WebpageGenerator.sendToWebpageGenerator(scriptPath);
            // ComparatorScriptEngine cse = new ComparatorScriptEngine(passedProps, scriptPath);
            useScriptConfigForPage = true;
        } else {
            if (configPath == null) {
                logInfo("No script or config file specified, looking for comparator.yaml in default location");
                configPath = ConfigHandling.DEFAULT_COMPARATOR_CONFIG_PATH;
            }

            logInfo("Attempting to load comparator configurations at {}\n", configPath);
            Map<String, Object> allConfigs = ConfigHandling.loadYaml(configPath);
            Map<String, String> configuredCategories = ConfigHandling.identifyAndValidifySpecificConfigs(allConfigs);
            // WebpageGenerator.sendToWebpageGenerator(allConfigs, configuredCategories);

            File recentReport = ConfigHandling
                    .identifyRecentReport((String) allConfigs.get(ConfigHandling.REPORT_PATH));
            accessToken = (String) allConfigs.get(ConfigHandling.TOKEN);

            if (recentReport != null && accessToken != null) {
                analyzeBenchmarks(recentReport, allConfigs, configuredCategories);

            } else {
                if (recentReport == null) {
                    logErr("* No recent report found to compare!");
                } else if (accessToken == null) {
                    logErr("* Failed to authorize provided access token!");
                }
            }
        }
        if (!useScriptConfigForPage) {
            // WebpageGenerator.generatePage();
        }
    }

    private static void analyzeBenchmarks(File recentReport, Map<String, Object> allConfigs,
            Map<String, String> configuredCategories) throws Exception {
                // System.out.println("\nCONF ALL: "+allConfigs.toString()+"\n");
                // System.out.println("\nCONF CATEGORIIES: "+configuredCategories.toString()+"\n");
                Map<String, Map<String, ComparedBenchmark>> recentBenchmarks = Requests.parseRecentReport(recentReport);
                // System.out.println("\nRECENT BENCHMAKS: "+recentBenchmarks.toString()+"\n");
                if (recentBenchmarks != null && Requests.getInstance().getProjectSummary(Requests.project, accessToken)) {
                    // System.out.println("\nALL PROJECT REPORT SUMMARIES : "+Requests.allProjectReportSummaries.toString()+"\n");
                    // System.out.println("\nALL PROJECT VERSIONS: "+Requests.allProjectVersions+"\n");
                    // System.out.println("\nLATEST V: "+Requests.latestVersion+"\n");
                    // System.out.println("\nPREV V: "+Requests.previousVersion+"\n");

                    for (Map.Entry<String, Map<String, ComparedBenchmark>> benchEntry : recentBenchmarks.entrySet()) {
                        String benchmarkFingerprint = benchEntry.getKey();
                        Map<String, ComparedBenchmark> benchEntryModeMap = benchEntry.getValue();
                        for (Map.Entry<String, ComparedBenchmark> comparedBenchEntry : benchEntryModeMap.entrySet()) {
                            String mode = comparedBenchEntry.getKey();
                            ComparedBenchmark benchmarkToCompare = comparedBenchEntry.getValue();
                            Map<String, Object> configMap = getConfigs(benchmarkToCompare.getDisplayName(), allConfigs, configuredCategories);
                            if (configMap != null) {
                                ComparisonConfig comparisonConfig = new ComparisonConfig(configMap);
                                benchmarkToCompare.setComparisonConfig(comparisonConfig);
                            }
                            CompareState compareState = compareSingleBenchmark(benchmarkToCompare, benchmarkFingerprint);

                            totalComparedBenchmarksTest++;
                            comparedBenchmarksTest.add(benchmarkToCompare);
                            switch (compareState) {
                                case PASS: {
                                    benchmarkToCompare.setCompareState(CompareState.PASS);
                                    totalPassedBenchmarksTest++;
                                    passedBenchmarksTest.add(benchmarkToCompare);
                                    break;
                                }
                                case FAIL: {
                                    benchmarkToCompare.setCompareState(CompareState.FAIL);
                                    totalFailedBenchmarksTest++;
                                    failedBenchmarksTest.add(benchmarkToCompare);
                                    break;
                                }
                                case SKIP: {
                                    benchmarkToCompare.setCompareState(CompareState.SKIP);
                                    totalSkippedBenchmarksTest++;
                                    skippedBenchmarksTest.add(benchmarkToCompare);
                                    break;
                                }
                            }

                        }
                    }
                }
                Requests.getInstance().close();
                logResults();
    }

    private static CompareState compareSingleBenchmark(ComparedBenchmark benchmarkToCompare, String benchmarkFingerprint) {
        ComparisonConfig comparisonConfig = benchmarkToCompare.getComparisonConfig();
        if (comparisonConfig != null) {
            String compareRange = comparisonConfig.getRange();
            Scope compareScope = comparisonConfig.getScope();
            String compareVersion = comparisonConfig.getCompareVersion();

            if (compareScope.equals(Scope.BETWEEN)) {
                if (compareVersion.equals(ConfigHandling.DEFAULT_COMPARE_VERSION)) {
                    compareVersion = Requests.previousVersion;
                    logInfo("{} : mode={} - Compare Version specified as 'PREVIOUS', setting compare version to previous benchmarked version={}",
                            benchmarkToCompare.getDisplayName(), benchmarkToCompare.getMode(), compareVersion);
                }

                if (Requests.currentVersion.equals(compareVersion)) {
                    logWarn("{} : mode={} - the compare version specified ({}) is the same as the currently benchmarked version={}, will perform WITHIN VERSION comparisons",
                        benchmarkToCompare.getDisplayName(), benchmarkToCompare.getMode(), compareVersion, Requests.currentVersion);
                    compareScope = ConfigHandling.DEFAULT_COMPARE_SCOPE;
                }
            } else if (compareScope.equals(Scope.WITHIN)) {
                compareVersion = Requests.currentVersion;
            }
            comparisonConfig.setScope(compareScope);
            comparisonConfig.setCompareVersion(compareVersion);

            
            if (Requests.allProjectVersions.contains(compareVersion)) {
                int range;
                int maxRange = Requests.allProjectReportSummaries.get(compareVersion).size();
                if (compareRange.equals("ALL")) {
                    range = maxRange;
                } else {
                    range = Integer.parseInt(compareRange);
                    if (range > maxRange) {
                        logWarn("SKIP COMPARISON - {} : mode={} - There are not enough values to compare to in version={} with specific range={}",
                                benchmarkToCompare.getDisplayName(), benchmarkToCompare.getMode(), Requests.currentVersion, range);
                        return CompareState.SKIP;
                    }
                }
                compareRange = String.valueOf(range);
                comparisonConfig.setRange(compareRange);


                List<ComparedBenchmark> benchmarksToCompareAgainst = new ArrayList<>();
                for (int i=0; i < range; i++) {
                    String reportID = (String) Requests.allProjectReportSummaries.get(compareVersion).get(i).get("reportId");
                    if (!Requests.fetchedBenchmarks.containsKey(reportID)) {
                        Requests.getInstance().getBenchmarksInReport(reportID, accessToken);
                    }
                    Map<String, List<ComparedBenchmark>> benchmaksInReportWithSameFingerprint = Requests.fetchedBenchmarks.get(reportID).get(benchmarkFingerprint);
                    if (benchmaksInReportWithSameFingerprint.containsKey(benchmarkToCompare.getMode())) {
                        benchmarksToCompareAgainst.addAll(benchmaksInReportWithSameFingerprint.get(benchmarkToCompare.getMode()));
                    }
                    
                }

                return Comparisons.runComparison(benchmarkToCompare, benchmarksToCompareAgainst);

            } else {
                logWarn("SKIP COMPARISON - {} : mode={} - There are no reports for {}, version {}", benchmarkToCompare.getDisplayName(), benchmarkToCompare.getMode(), Requests.project, compareVersion);
                return CompareState.SKIP;
            }
        } else {
            logWarn("SKIP COMPARISON - {} : mode={} - There are no configurations set", benchmarkToCompare.getDisplayName(), benchmarkToCompare.getMode());
            return CompareState.SKIP;
        }
    }

    public static void logResults() throws Exception {
        System.out.print("\n\n");
        logInfo("Comparing {}, version {}", Requests.project, Requests.currentVersion);
        logInfo("compared={}, passed={}, (skipped={}), anomalies={}", totalComparedBenchmarksTest, totalPassedBenchmarksTest,
                totalSkippedBenchmarksTest, totalFailedBenchmarksTest);
        System.out.print("\n");
        logComparison();
        if (totalFailedBenchmarksTest > 0) {
            logWarn("* There are benchmark comparison failures! *");
            if (failBuildFlag) {
                String error = "* Build failed due to benchmark anomalies *";
                logErr(error + "\n");
                // helps logs finish before exception is thrown
                TimeUnit.MILLISECONDS.sleep(500);
                throw new Exception(error);
            }
        }
    }

    private static void logComparison() {
        if (totalPassedBenchmarksTest > 0) {
            logInfo("** {}/{} benchmarks PASSED:", totalPassedBenchmarksTest, totalComparedBenchmarksTest);
            printComparedBenchmarks(CompareState.PASS, passedBenchmarksTest, totalPassedBenchmarksTest);
        }
        System.out.print("\n");
        if (totalSkippedBenchmarksTest > 0) {
            logInfo("** {}/{} benchmarks SKIPPED:", totalSkippedBenchmarksTest, totalComparedBenchmarksTest);
            printComparedBenchmarks(CompareState.SKIP, skippedBenchmarksTest, totalSkippedBenchmarksTest);
        }
        System.out.print("\n");
        if (totalFailedBenchmarksTest > 0) {
            logInfo("** {}/{} benchmark ANOMALIES:", totalFailedBenchmarksTest, totalComparedBenchmarksTest);
            printComparedBenchmarks(CompareState.FAIL, failedBenchmarksTest, totalFailedBenchmarksTest);
        }
        System.out.print("\n");
        logInfo("* Completed benchmark analysis\n");
    }

    private static void printComparedBenchmarks(CompareState compareState, List<ComparedBenchmark> benchmarks, int total) {
        for (ComparedBenchmark benchmark : benchmarks) {
            if (!compareState.equals(CompareState.SKIP)) {
                ComparisonConfig comparisonConfig = benchmark.getComparisonConfig();
                StringBuilder log = new StringBuilder(
                        "   {}: test.name={}, test.mode={}, test.score={}, test.compare.version={}, test.compare.method={}, test.compare.range={}, test.compare.mean={}, ");
    
                if (comparisonConfig.getMethod().equals(Method.DELTA)) {
                    log.append("test.compare.threshold=").append(comparisonConfig.getThreshold()).append(", ");
                    if (comparisonConfig.getThreshold().equals(Threshold.PERCENT_CHANGE)) {
                        log.append("test.percentChangeAllowed=").append(comparisonConfig.getPercentChangeAllowed()).append(", ");
                    }
                } else {
                    log.append("test.deviationsAllowed=").append(comparisonConfig.getDeviationsAllowed()).append(", ");
                }
    
                log.append("test.compare.delta={}, test.compare.percentChange={}%, test.compare.SDsFromMean={}, test.id={}");
    
                if (compareState.equals(CompareState.PASS)) {
                    logInfo(log.toString(), "PASSED", benchmark.getDisplayName(), benchmark.getMode(), benchmark.getScore(), comparisonConfig.getCompareVersion(),
                        comparisonConfig.getMethod(), comparisonConfig.getRange(), benchmark.getCompareMean(), benchmark.getDelta(), 
                        benchmark.getPercentChange(), benchmark.getDeviationsFromMean(), benchmark.getBenchProperties().get("generatedFingerprint"));
                } else {
                    logErr(log.toString(), "ANOMALY", benchmark.getDisplayName(), benchmark.getMode(), benchmark.getScore(), comparisonConfig.getCompareVersion(),
                        comparisonConfig.getMethod(), comparisonConfig.getRange(), benchmark.getCompareMean(), benchmark.getDelta(), 
                        benchmark.getPercentChange(), benchmark.getDeviationsFromMean(), benchmark.getBenchProperties().get("generatedFingerprint"));
                }
            } else {
                logInfo("   NO COMPARISON: test.name={}, test.mode={}, test.score={}", benchmark.getDisplayName(), benchmark.getMode(), benchmark.getScore());
            } 
        }
    }


    // TODO should utilize ComparisonConfig class
    @SuppressWarnings("unchecked")
    private static Map<String, Object> getConfigs(String benchmarkName, Map<String, Object> allConfigs,
            Map<String, String> configuredCategories) {
        Map<String, Object> configs = null;

        Method compareMethod = null;
        Scope compareScope = null;
        String compareRange = null;
        Threshold compareThreshold = null;
        Double percentChangeAllowed = null;
        String compareVersion = null;
        Double deviationsAllowed = null;

        if (allConfigs.containsKey(ConfigHandling.DEFAULT_IDENTIFIER_HEADER)) {
            Map<String, Object> defaultConfigs = (Map<String, Object>) allConfigs
                    .get(ConfigHandling.DEFAULT_IDENTIFIER_HEADER);
            compareMethod = (Method) defaultConfigs.get(ConfigHandling.METHOD);
            compareScope = (Scope) defaultConfigs.get(ConfigHandling.SCOPE);
            compareRange = (String) defaultConfigs.get(ConfigHandling.RANGE);
            compareThreshold = (Threshold) defaultConfigs.get(ConfigHandling.THRESHOLD);
            percentChangeAllowed = (Double) defaultConfigs.get(ConfigHandling.PERCENT_CHANGE_ALLOWED);
            compareVersion = (String) defaultConfigs.get(ConfigHandling.COMPARE_VERSION);
            if ((defaultConfigs.get(ConfigHandling.DEVIATIONS_ALLOWED) != null)) {
                deviationsAllowed = Double.parseDouble((String) defaultConfigs.get(ConfigHandling.DEVIATIONS_ALLOWED));
            }
            // deviationsAllowed = (Double) defaultConfigs.get(ConfigHandling.DEVIATIONS_ALLOWED);

            configs = createConfigMap(compareMethod, compareScope, compareRange, compareThreshold, percentChangeAllowed,
                    compareVersion, deviationsAllowed);
        }

        for (Map.Entry<String, String> categoryEntry : configuredCategories.entrySet()) {
            if (categoryEntry.getKey() != null && benchmarkName.startsWith(categoryEntry.getKey())) {
                String identifier = categoryEntry.getValue();
                Map<String, Object> specificConfigs = (Map<String, Object>) allConfigs.get(identifier);
                compareMethod = (Method) specificConfigs.get(ConfigHandling.METHOD);
                compareScope = (Scope) specificConfigs.get(ConfigHandling.SCOPE);
                compareRange = (String) specificConfigs.get(ConfigHandling.RANGE);
                compareThreshold = (Threshold) specificConfigs.get(ConfigHandling.THRESHOLD);
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

    private static Map<String, Object> createConfigMap(Method compareMethod, Scope compareScope,
            String compareRange, Threshold compareThreshold, Double percentChangeAllowed,
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

    public static void logInfo(String msg, Object... args) {
        log.info(msg, args);
    }

    public static void logWarn(String msg, Object... args) {
        log.warn(msg, args);
    }

    public static void logErr(String msg, Object... args) {
        log.error(msg, args);
    }
























    
// DEPRECATED METHODS

    // public static void addPassFailBenchData(Map<String, Map<String, Map<String, Map<String, Object>>>> benchmarks,
    //         Map<String, Object> configMap, String benchmarkName, String benchmarkVersion, String benchmarkMode,
    //         Double benchmarkScore, Map<String, Double> compareValues) {
    //     Method compareMethod = (Method) configMap.get(ConfigHandling.METHOD);
    //     Scope compareScope = (Scope) configMap.get(ConfigHandling.SCOPE);
    //     String compareRange = (String) configMap.get(ConfigHandling.RANGE);
    //     Threshold compareThreshold = (Threshold) configMap.get(ConfigHandling.THRESHOLD);
    //     String compareVersion = (String) configMap.get(ConfigHandling.COMPARE_VERSION);
    //     Double percentChangeAllowed = (Double) configMap.get(ConfigHandling.PERCENT_CHANGE_ALLOWED);
    //     Double deviationsAllowed = null;
    //     if ((configMap.get(ConfigHandling.DEVIATIONS_ALLOWED) != null)) {
    //         if (configMap.get(ConfigHandling.DEVIATIONS_ALLOWED).getClass() != Double.class) {
    //             deviationsAllowed = Double.parseDouble((String) configMap.get(ConfigHandling.DEVIATIONS_ALLOWED));
    //         } else {
    //             deviationsAllowed = (Double) configMap.get(ConfigHandling.DEVIATIONS_ALLOWED);
    //         }
    //     }
    //     // Double deviationsAllowed = (Double) configMap.get(ConfigHandling.DEVIATIONS_ALLOWED);

    //     Map<String, Object> data = prepareCompareDataMap(benchmarks, benchmarkName, benchmarkVersion, benchmarkMode);
    //     benchmarkScore = Comparisons.roundHandling(benchmarkScore);
    //     Double compareValue = compareValues.get(Comparisons.CALCULATED_COMPARE_VALUE);
    //     compareValue = Comparisons.roundHandling(compareValue);
    //     Double delta = compareValues.get(Comparisons.CALCULATED_DELTA);
    //     delta = Comparisons.roundHandling(delta);
    //     Double percentChange = compareValues.get(Comparisons.CALCULATED_PERCENT_CHANGE);
    //     percentChange = Comparisons.roundHandling(percentChange);
    //     Double sdFromMean = compareValues.get(Comparisons.CALCULATED_SD_FROM_MEAN);
    //     sdFromMean = Comparisons.roundHandling(sdFromMean);
    //     data.put(ConfigHandling.BENCHMARK_SCORE, benchmarkScore);
    //     data.put(Comparisons.CALCULATED_COMPARE_VALUE, compareValue);
    //     data.put(Comparisons.CALCULATED_DELTA, delta);
    //     data.put(Comparisons.CALCULATED_PERCENT_CHANGE, percentChange);
    //     data.put(Comparisons.CALCULATED_SD_FROM_MEAN, sdFromMean);
    //     data.put(ConfigHandling.METHOD, compareMethod);
    //     data.put(ConfigHandling.SCOPE, compareScope);
    //     data.put(ConfigHandling.RANGE, compareRange);
    //     data.put(ConfigHandling.THRESHOLD, compareThreshold);
    //     data.put(ConfigHandling.COMPARE_VERSION, compareVersion);
    //     data.put(ConfigHandling.PERCENT_CHANGE_ALLOWED, percentChangeAllowed);
    //     data.put(ConfigHandling.DEVIATIONS_ALLOWED, deviationsAllowed);
    // }

    // private static Map<String, Object> prepareCompareDataMap(
    //         Map<String, Map<String, Map<String, Map<String, Object>>>> benchmarks, String benchmarkName,
    //         String benchmarkVersion, String benchmarkMode) {
    //     if (!benchmarks.containsKey(benchmarkName)) {
    //         Map<String, Object> data = new HashMap<>();
    //         Map<String, Map<String, Object>> dataPerMode = new HashMap<>();
    //         Map<String, Map<String, Map<String, Object>>> dataPerVersion = new HashMap<>();
    //         dataPerMode.put(benchmarkMode, data);
    //         dataPerVersion.put(benchmarkVersion, dataPerMode);
    //         benchmarks.put(benchmarkName, dataPerVersion);
    //     }

    //     Map<String, Map<String, Map<String, Object>>> dataPerVersion = benchmarks.get(benchmarkName);
    //     if (!dataPerVersion.containsKey(benchmarkVersion)) {
    //         Map<String, Object> data = new HashMap<>();
    //         Map<String, Map<String, Object>> dataPerMode = new HashMap<>();
    //         dataPerMode.put(benchmarkMode, data);
    //         dataPerVersion.put(benchmarkVersion, dataPerMode);
    //     }

    //     Map<String, Map<String, Object>> dataPerMode = dataPerVersion.get(benchmarkVersion);
    //     if (!dataPerMode.containsKey(benchmarkMode)) {
    //         Map<String, Object> data = new HashMap<>();
    //         dataPerMode.put(benchmarkMode, data);
    //     }

    //     return dataPerMode.get(benchmarkMode);
    // }

    // public static void addSkipBenchData(Double benchmarkScore, String benchmarkName, String benchmarkVersion,
    //         String benchmarkMode) {
    //     Map<String, Object> data = prepareCompareDataMap(skippedBenchmarks, benchmarkName, benchmarkVersion,
    //             benchmarkMode);
    //     benchmarkScore = Comparisons.roundHandling(benchmarkScore);
    //     data.put(ConfigHandling.BENCHMARK_SCORE, benchmarkScore);
    // }

    // private static void printBenchmarkResults(Map<String, String> namesToFingerprints) {
    //     if (totalPassedBenchmarks > 0) {
    //         printBenchmarkResultsHelper(CompareState.PASS, totalPassedBenchmarks, passedBenchmarks,
    //                 namesToFingerprints);
    //     }
    //     System.out.print("\n");
    //     if (totalSkippedBenchmarks > 0) {
    //         printBenchmarkResultsHelper(CompareState.SKIP, totalSkippedBenchmarks, skippedBenchmarks,
    //                 namesToFingerprints);
    //     }
    //     System.out.print("\n");
    //     if (totalFailedBenchmarks > 0) {
    //         printBenchmarkResultsHelper(CompareState.FAIL, totalFailedBenchmarks, failedBenchmarks,
    //                 namesToFingerprints);
    //     }
    //     System.out.print("\n");
    //     logInfo("* Completed benchmark analysis\n");
    // }

    // private static void printBenchmarkResultsHelper(CompareState passfail, int totalBenchmarksToReport,
    //         Map<String, Map<String, Map<String, Map<String, Object>>>> benchmarksToReport,
    //         Map<String, String> namesToFingerprints) {
    //     String passfailStr;
    //     if (passfail.equals(CompareState.PASS)) {
    //         passfailStr = "PASSED";
    //     } else if (passfail.equals(CompareState.FAIL)) {
    //         passfailStr = "FAILED";
    //     } else {
    //         passfailStr = "SKIPPED";
    //     }

    //     logInfo("** {}/{} benchmarks {}:", totalBenchmarksToReport, totalComparedBenchmarks, passfailStr);
    //     for (Map.Entry<String, Map<String, Map<String, Map<String, Object>>>> brEntry : benchmarksToReport.entrySet()) {
    //         String benchmarkName = brEntry.getKey();
    //         String fingerprint = namesToFingerprints.get(benchmarkName);
    //         Map<String, Map<String, Map<String, Object>>> benchmarkVersions = brEntry.getValue();
    //         for (Map.Entry<String, Map<String, Map<String, Object>>> bvEntry : benchmarkVersions.entrySet()) {
    //             String benchmarkVersion = bvEntry.getKey();
    //             Map<String, Map<String, Object>> benchmarksData = bvEntry.getValue();
    //             for (Map.Entry<String, Map<String, Object>> bdEntry : benchmarksData.entrySet()) {
    //                 String benchmarkMode = bdEntry.getKey();
    //                 Map<String, Object> benchmarkData = bdEntry.getValue();
    //                 Double benchmarkScore = (Double) benchmarkData.get(ConfigHandling.BENCHMARK_SCORE);
    //                 BigDecimal roundedBenchmarkScore = BigDecimal.valueOf(benchmarkScore);
    //                 if (!passfail.equals(CompareState.SKIP)) {
    //                     Double compareValue = (Double) benchmarkData.get(Comparisons.CALCULATED_COMPARE_VALUE);
    //                     BigDecimal roundedCompareValue = BigDecimal.valueOf(compareValue);
    //                     Double delta = (Double) benchmarkData.get(Comparisons.CALCULATED_DELTA);
    //                     BigDecimal roundedDelta = BigDecimal.valueOf(delta);
    //                     Double percentChange = (Double) benchmarkData.get(Comparisons.CALCULATED_PERCENT_CHANGE);
    //                     BigDecimal roundedPercentChange = BigDecimal.valueOf(percentChange);
    //                     Double sdFromMean = (Double) benchmarkData.get(Comparisons.CALCULATED_SD_FROM_MEAN);
    //                     BigDecimal roundedSDFromMean = BigDecimal.valueOf(sdFromMean);
    //                     Method compareMethod = (Method) benchmarkData
    //                             .get(ConfigHandling.METHOD);
    //                     Scope compareScope = (Scope) benchmarkData.get(ConfigHandling.SCOPE);
    //                     String compareRange = (String) benchmarkData.get(ConfigHandling.RANGE);
    //                     String compareVersion = (String) benchmarkData.get(ConfigHandling.COMPARE_VERSION);
    //                     Threshold compareThreshold = (Threshold) benchmarkData
    //                             .get(ConfigHandling.THRESHOLD);

    //                     StringBuilder logReport = new StringBuilder(
    //                             "   {} COMPARISON: test.name={}, test.version={}, test.mode={}, test.score={}, "
    //                                     + "test.compare.method={}, test.COMPARE.VALUE={}, test.delta={}, test.percentChange={}%, test.SDsFromMean={}, "
    //                                     + "test.compare.scope={}, test.compare.version={}, test.compare.range={}, ");

    //                     if (compareMethod.equals(Method.DELTA)) {

    //                         logReport.append("test.compare.threshold=").append(compareThreshold).append(", ");
    //                         if (compareThreshold.equals(Threshold.PERCENT_CHANGE)) {
    //                             Double percentChangeAllowed = (Double) benchmarkData
    //                                     .get(ConfigHandling.PERCENT_CHANGE_ALLOWED);
    //                             logReport.append("test.percentChangeAllowed=").append(percentChangeAllowed)
    //                                     .append(", ");
    //                         }
    //                     } else {
    //                         Double deviationsAllowed = (Double) benchmarkData.get(ConfigHandling.DEVIATIONS_ALLOWED);
    //                         logReport.append("test.deviationsAllowed=").append(deviationsAllowed).append(", ");
    //                     }

    //                     logReport.append("test.id={}");

    //                     if (passfail.equals(CompareState.PASS)) {
    //                         logInfo(logReport.toString(), passfail, benchmarkName, benchmarkVersion, benchmarkMode,
    //                                 roundedBenchmarkScore, compareMethod, roundedCompareValue, roundedDelta,
    //                                 roundedPercentChange, roundedSDFromMean, compareScope, compareVersion, compareRange,
    //                                 fingerprint);
    //                     } else {

    //                         logErr(logReport.toString(), passfail, benchmarkName, benchmarkVersion, benchmarkMode,
    //                                 roundedBenchmarkScore, compareMethod, roundedCompareValue, roundedDelta,
    //                                 roundedPercentChange, roundedSDFromMean, compareScope, compareVersion, compareRange,
    //                                 fingerprint);
    //                     }
    //                 } else {
    //                     logInfo("   NO COMPARISON: test.name={}, test.version={}, test.mode={}, test.score={}",
    //                             benchmarkName, benchmarkVersion, benchmarkMode, roundedBenchmarkScore);
    //                 }
    //             }
    //         }
    //     }
    // }


    // private static boolean compareBenchmark(String benchmarkName, String benchmarkFingerprint, String benchmarkVersion,
    //         String benchmarkMode, Double benchmarkScore, Map<String, Object> allConfigs,
    //         Map<String, String> configuredPackages) {

    //     Map<String, Object> configMap = getConfigs(benchmarkName, allConfigs, configuredPackages);

    //     if (configMap != null) {
    //         Scope compareScope = (Scope) configMap.get(ConfigHandling.SCOPE);
    //         String compareRange = (String) configMap.get(ConfigHandling.RANGE);
    //         String compareVersion = (String) configMap.get(ConfigHandling.COMPARE_VERSION);

    //         List<Double> benchmarkVersionScores = Requests.getBenchmarks(benchmarkFingerprint, benchmarkVersion,
    //                 benchmarkMode);

    //         // default the comparison scores to the current version without the newly added
    //         // benchmark
    //         List<Double> compareVersionScores = new ArrayList<>(benchmarkVersionScores);
    //         compareVersionScores.remove(benchmarkVersionScores.size() - 1);

    //         if (compareScope.equals(Scope.BETWEEN)) {
    //             if (compareVersion.equals(ConfigHandling.DEFAULT_COMPARE_VERSION)) {
    //                 compareVersion = Requests.getPreviousVersion(benchmarkFingerprint);
    //                 logInfo("{} : mode={} - Compare Version specified as 'PREVIOUS', setting compare version to previous benchmarked version={}",
    //                         benchmarkName, benchmarkMode, compareVersion);
    //             }

    //             if (benchmarkVersion.equals(compareVersion)) {
    //                 logWarn("{} : mode={} - the compare version specified ({}) is the same as the currently benchmarked version={}, will perform WITHIN VERSION comparisons",
    //                         benchmarkName, benchmarkMode, compareVersion, benchmarkVersion);
    //                 compareScope = ConfigHandling.DEFAULT_COMPARE_SCOPE;
    //             } else if (Requests.getBenchmarks(benchmarkFingerprint).containsKey(compareVersion)
    //                     && Requests.getBenchmarks(benchmarkFingerprint, compareVersion).containsKey(benchmarkMode)) {
    //                 compareVersionScores = Requests.getBenchmarks(benchmarkFingerprint, compareVersion, benchmarkMode);
    //             } else {
    //                 logWarn("SKIP COMPARISON - {} : mode={} - There are no benchmarks for the specified compare version={}",
    //                         benchmarkName, benchmarkMode, compareVersion);
    //                 return Comparisons.skipComparison(benchmarkScore, benchmarkName, benchmarkVersion, benchmarkMode);
    //             }
    //         }
    //         configMap.put(ConfigHandling.SCOPE, compareScope);
    //         configMap.put(ConfigHandling.COMPARE_VERSION, compareVersion);

    //         Comparisons.compareScores(configMap, benchmarkName, benchmarkVersion, benchmarkMode, benchmarkVersionScores,
    //                 compareVersionScores);

    //         return true;
    //     } else {
    //         logWarn("SKIP COMPARISON - {} : mode={} - There are no configurations set", benchmarkName, benchmarkMode);
    //         return Comparisons.skipComparison(benchmarkScore, benchmarkName, benchmarkVersion, benchmarkMode);
    //     }
    // }

    // public static void finalizeComparisonLogs(Map<String, Object> props) throws Exception {
    //     System.out.print("\n\n");
    //     logInfo("compared={}, passed={}, (skipped={}), failed={}", totalComparedBenchmarks, totalPassedBenchmarks,
    //             totalSkippedBenchmarks, totalFailedBenchmarks);
    //     System.out.print("\n");
    //     printBenchmarkResults(Requests.namesToFingerprints);
    //     WebpageGenerator.generatePage(props);
    //     if (totalFailedBenchmarks > 0) {
    //         logWarn("* There are benchmark comparison failures! *");
    //         if (failBuildFlag) {
    //             String error = "* Build failed due to scores being too low *";
    //             logErr(error + "\n");
    //             // helps logs finish before exception is thrown
    //             TimeUnit.MILLISECONDS.sleep(500);
    //             throw new Exception(error);
    //         }
    //     }
    // }

}