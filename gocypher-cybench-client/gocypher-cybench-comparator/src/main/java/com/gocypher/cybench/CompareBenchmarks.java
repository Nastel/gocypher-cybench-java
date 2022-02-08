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

package com.gocypher.cybench;

import java.io.File;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
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
    private static boolean useScriptConfigForPage = false;
    public static boolean failBuildFlag = false;
        
    public static int totalComparedBenchmarks = 0;
    public static int totalPassedBenchmarks = 0;
    public static int totalFailedBenchmarks = 0;
    public static int totalSkippedBenchmarks = 0;
    public static List<ComparedBenchmark> comparedBenchmarks = new ArrayList<>();
    public static List<ComparedBenchmark> passedBenchmarks = new ArrayList<>();
    public static List<ComparedBenchmark> failedBenchmarks = new ArrayList<>();
    public static List<ComparedBenchmark> skippedBenchmarks = new ArrayList<>();

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
            ComparatorScriptEngine cse = new ComparatorScriptEngine(passedProps, scriptPath);
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
            setAccessToken((String) allConfigs.get(ConfigHandling.TOKEN));

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

    public static void setAccessToken(String token) {
        accessToken = token;
    }

    private static void analyzeBenchmarks(File recentReport, Map<String, Object> allConfigs,
            Map<String, String> configuredCategories) throws Exception {
        Map<String, Map<String, ComparedBenchmark>> recentBenchmarks = Requests.parseRecentReport(recentReport);
        if (recentBenchmarks != null && Requests.getInstance().getProjectSummary(Requests.project, accessToken)) {
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
                    CompareState compareState = compareSingleBenchmark(benchmarkToCompare);
                }
            }
        }
        Requests.getInstance().close();
        logResults();
    }

    private static CompareState compareSingleBenchmark(ComparedBenchmark benchmarkToCompare) {
        ComparisonConfig comparisonConfig = benchmarkToCompare.getComparisonConfig();
        if (comparisonConfig != null) {
            List<ComparedBenchmark> benchmarksToCompareAgainst = getBenchmarksToCompareAgainst(benchmarkToCompare);

            if (benchmarksToCompareAgainst.size() == 0) {
                logWarn("SKIP COMPARISON - {} : mode={} - There are not enough benchmarks to compare to in version={} with specific range={}",
                            benchmarkToCompare.getDisplayName(), benchmarkToCompare.getMode(), Requests.currentVersion, comparisonConfig.getRange());
                if (benchmarkToCompare.getCompareState() == null) {
                    // handling in case skip flag wasn't raised in getBenchmarksToCompareAgainst method
                    skipBenchmark(benchmarkToCompare);
                }
                return CompareState.SKIP;
            }

            return Comparisons.runSingleComparison(benchmarkToCompare, benchmarksToCompareAgainst);            
        } else {
            logWarn("SKIP COMPARISON - {} : mode={} - There are no configurations set", benchmarkToCompare.getDisplayName(), benchmarkToCompare.getMode());
            skipBenchmark(benchmarkToCompare);
            return CompareState.SKIP;
        }
    }

    public static boolean thereExistsReportsToCompareAgainst(ComparedBenchmark benchmarkToCompare) {
        ComparisonConfig comparisonConfig = benchmarkToCompare.getComparisonConfig();
        Scope compareScope = comparisonConfig.getScope();
        String compareVersion = comparisonConfig.getCompareVersion();
        String compareRange = comparisonConfig.getRange();

        if (compareScope.equals(Scope.BETWEEN)) {
            if (compareVersion.equals(ConfigHandling.DEFAULT_COMPARE_VERSION)) {
                logInfo("{} : mode={} - Compare Version specified as 'PREVIOUS', setting compare version to previous benchmarked version={}",
                    benchmarkToCompare.getDisplayName(), benchmarkToCompare.getMode(), compareVersion);
                compareVersion = Requests.previousVersion;
            }

            if (Requests.currentVersion.equals(compareVersion)) {
                logWarn("{} : mode={} - the Compare Version specified ({}) is the same as the currently benchmarked version={}, will perform WITHIN VERSION comparisons",
                    benchmarkToCompare.getDisplayName(), benchmarkToCompare.getMode(), compareVersion, Requests.currentVersion);
                compareScope = ConfigHandling.DEFAULT_COMPARE_SCOPE;
            }
        } else if (compareScope.equals(Scope.WITHIN)) {
            if (compareVersion == null) {
                compareVersion = Requests.currentVersion;
            } else {
                logWarn("{} : mode={} - Compare Version was specified as {}, but scope was set to WITHIN, will perform BETWEEN VERSION comparisons",
                    benchmarkToCompare.getDisplayName(), benchmarkToCompare.getMode(), compareVersion);
                compareScope = Scope.BETWEEN;
            }
        }
        comparisonConfig.setScope(compareScope);
        comparisonConfig.setCompareVersion(compareVersion);
        
        if (Requests.allProjectVersions.contains(compareVersion)) {
            int range;
            int maxRange = Requests.reportSummaries.get(compareVersion).size();
            if (compareRange.equals("ALL")) {
                range = maxRange;
            } else {
                range = Integer.parseInt(compareRange);
                if (range > maxRange) {
                    logWarn("SKIP COMPARISON - {} : mode={} - There are not enough reports to compare to in version={} with specific range={}",
                        benchmarkToCompare.getDisplayName(), benchmarkToCompare.getMode(), Requests.currentVersion, range);
                    skipBenchmark(benchmarkToCompare);
                    return false;
                }
            }    
            compareRange = String.valueOf(range);
            comparisonConfig.setRange(compareRange);
            return true;
        } else {
            logWarn("SKIP COMPARISON - {} : mode={} - There are no reports for {}, version {}", benchmarkToCompare.getDisplayName(), benchmarkToCompare.getMode(), Requests.project, compareVersion);
            skipBenchmark(benchmarkToCompare);
            return false;
        }
    }

    public static List<ComparedBenchmark> getBenchmarksToCompareAgainst(ComparedBenchmark benchmarkToCompare) {
        List<ComparedBenchmark> benchmarksToCompareAgainst = new ArrayList<>();
        if (thereExistsReportsToCompareAgainst(benchmarkToCompare)) {
            String compareVersion = benchmarkToCompare.getComparisonConfig().getCompareVersion();
            int range = Integer.parseInt(benchmarkToCompare.getComparisonConfig().getRange());
            int added = 0;
            for (int i=0; i < Requests.reportSummaries.get(compareVersion).size() && added < range; i++) {
                String reportID = (String) Requests.reportSummaries.get(compareVersion).get(i).get("reportId");
                Number timestampNum = (Number) Requests.reportSummaries.get(compareVersion).get(i).get("timestamp");
                Date reportDateTime = new Date(timestampNum.longValue() / 1000);
                if (reportID != Requests.recentReportID && Requests.recentReportDateTime.compareTo(reportDateTime) != 0) {
                    boolean fetchSuccess = true;
                    if (!Requests.fetchedBenchmarks.containsKey(reportID)) {
                        fetchSuccess = Requests.getInstance().getBenchmarksInReport(reportID, accessToken);
                    }

                    if (fetchSuccess) {
                        Map<String, List<ComparedBenchmark>> benchmaksInReportWithSameFingerprint = Requests.fetchedBenchmarks.get(reportID).get(benchmarkToCompare.getFingerprint());
                        if (benchmaksInReportWithSameFingerprint.containsKey(benchmarkToCompare.getMode())) {
                            benchmarksToCompareAgainst.addAll(benchmaksInReportWithSameFingerprint.get(benchmarkToCompare.getMode()));
                            added++;
                        }
                    }
                }
            }
        }
        return benchmarksToCompareAgainst;
    }

    public static Map<String, Object> compareAllBenchmarks(ComparisonConfig comparisonConfig,
            Map<String, Map<String, ComparedBenchmark>> benchmarksToCompare,
            Map<String, Map<String, List<ComparedBenchmark>>> benchmarksToCompareAgainst) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap = Comparisons.runComparison(comparisonConfig, benchmarksToCompare, benchmarksToCompareAgainst);
        resultMap.put("timestampUTC", ZonedDateTime.now(ZoneOffset.UTC).toInstant().toString());
        return resultMap;
    }

    public static void passBenchmark(ComparedBenchmark benchmarkToCompare) {
        totalComparedBenchmarks++;
        comparedBenchmarks.add(benchmarkToCompare);
        benchmarkToCompare.setCompareState(CompareState.PASS);
        totalPassedBenchmarks++;
        passedBenchmarks.add(benchmarkToCompare);
    }

    public static void failBenchmark(ComparedBenchmark benchmarkToCompare) {
        totalComparedBenchmarks++;
        comparedBenchmarks.add(benchmarkToCompare);
        benchmarkToCompare.setCompareState(CompareState.FAIL);
        totalFailedBenchmarks++;
        failedBenchmarks.add(benchmarkToCompare);
    }

    public static void skipBenchmark(ComparedBenchmark benchmarkToCompare) {
        totalComparedBenchmarks++;
        comparedBenchmarks.add(benchmarkToCompare);
        benchmarkToCompare.setCompareState(CompareState.SKIP);
        totalSkippedBenchmarks++;
        skippedBenchmarks.add(benchmarkToCompare);
    }

    public static void logResults() throws Exception {
        System.out.print("\n\n");
        logInfo("Comparing {}, version {}", Requests.project, Requests.currentVersion);
        logInfo("compared={}, passed={}, (skipped={}), anomalies={}", totalComparedBenchmarks, totalPassedBenchmarks,
                totalSkippedBenchmarks, totalFailedBenchmarks);
        System.out.print("\n");
        logComparison();
        if (totalFailedBenchmarks > 0) {
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
        if (totalPassedBenchmarks > 0) {
            logInfo("** {}/{} benchmarks PASSED:", totalPassedBenchmarks, totalComparedBenchmarks);
            printComparedBenchmarks(CompareState.PASS, passedBenchmarks, totalPassedBenchmarks);
        }
        System.out.print("\n");
        if (totalSkippedBenchmarks > 0) {
            logInfo("** {}/{} benchmarks SKIPPED:", totalSkippedBenchmarks, totalComparedBenchmarks);
            printComparedBenchmarks(CompareState.SKIP, skippedBenchmarks, totalSkippedBenchmarks);
        }
        System.out.print("\n");
        if (totalFailedBenchmarks > 0) {
            logInfo("** {}/{} benchmark ANOMALIES:", totalFailedBenchmarks, totalComparedBenchmarks);
            printComparedBenchmarks(CompareState.FAIL, failedBenchmarks, totalFailedBenchmarks);
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
                        benchmark.getPercentChange(), benchmark.getDeviationsFromMean(), benchmark.getFingerprint());
                } else {
                    logErr(log.toString(), "ANOMALY", benchmark.getDisplayName(), benchmark.getMode(), benchmark.getScore(), comparisonConfig.getCompareVersion(),
                        comparisonConfig.getMethod(), comparisonConfig.getRange(), benchmark.getCompareMean(), benchmark.getDelta(), 
                        benchmark.getPercentChange(), benchmark.getDeviationsFromMean(), benchmark.getFingerprint());
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
}