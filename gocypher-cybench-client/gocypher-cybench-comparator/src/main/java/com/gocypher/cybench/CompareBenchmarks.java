package com.gocypher.cybench;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gocypher.cybench.services.Requests;
import com.gocypher.cybench.utils.Comparisons;
import com.gocypher.cybench.utils.ConfigHandling;

public class CompareBenchmarks {
    private static final Logger log = LoggerFactory.getLogger(CompareBenchmarks.class);

    @SuppressWarnings("unchecked")
    public static void main(String... args) throws Exception {
        log.info("* Analyzing benchmark performance...");

        Map<String, Object> allConfigs = ConfigHandling.loadYaml(args);

        Map<String, Object> defaultConfigs = (Map<String, Object>) allConfigs
                .get(ConfigHandling.DEFAULT_IDENTIFIER_HEADER);
        Map<String, String> configuredPackages = ConfigHandling
                .identifyAndValidifySpecificComparisonConfigs(allConfigs);

        File recentReport = ConfigHandling.identifyRecentReport((String) allConfigs.get("reports"));
        String accessToken = (String) allConfigs.get("token");

        if (recentReport != null && accessToken != null) {
            analyzeBenchmarks(recentReport, accessToken, defaultConfigs, configuredPackages, allConfigs);
        } else {
            if (recentReport == null) {
                log.error("* No recent report found to compare!");
            } else if (accessToken == null) {
                log.error("* Failed to authorize provided access token!");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void analyzeBenchmarks(File recentReport, String accessToken, Map<String, Object> defaultConfigs,
            Map<String, String> configuredPackages, Map<String, Object> allConfigs) throws Exception {
        // < BenchmarkName, < BenchmarkVersion, < BenchmarkMode, < BenchmarkScore,
        // CompareMethod, CompareScope, CompareVersion >>>
        Map<String, Map<String, Map<String, List<Object>>>> passedBenchmarks = new HashMap<>();
        Map<String, Map<String, Map<String, List<Object>>>> failedBenchmarks = new HashMap<>();
        Map<String, String> namesToFingerprints = new HashMap<>();
        JSONObject benchmarkReport = null;
        int totalComparedBenchmarks = 0;
        int totalPassedBenchmarks = 0;
        int totalFailedBenchmarks = 0;
        boolean failFetch = false;
        try {
            String str = FileUtils.readFileToString(recentReport, "UTF-8");
            JSONParser parser = new JSONParser();
            benchmarkReport = (JSONObject) parser.parse(str);
        } catch (Exception e) {
            log.error("* Failed to fetch benchmark data from recent report for analysis", e);
        }

        if (benchmarkReport != null) {
            String reportID = null;
            String reportURL = (String) benchmarkReport.get("reportURL");
            if (reportURL != null) {
                String[] parsedURL = reportURL.split("https://app.cybench.io/cybench/benchmark/");
                reportID = parsedURL[1].split("/")[0];
            }
            JSONObject packages = (JSONObject) benchmarkReport.get("benchmarks");

            // loop through packages (com.x, com.x2, ...)
            for (Object pckg : packages.values()) {
                JSONArray packageBenchmarks = (JSONArray) pckg;
                if (failFetch) {
                    break;
                }
                // loop through benchmarks in package (com.x.bench1, com.x.bench2, ...)
                for (Object packageBenchmark : packageBenchmarks) {
                    totalComparedBenchmarks++;
                    JSONObject benchmark = (JSONObject) packageBenchmark;
                    String benchmarkName = (String) benchmark.get("name");
                    String benchmarkVersion = (String) benchmark.get("version");
                    Double score = (Double) benchmark.get("score");
                    String benchmarkMode = (String) benchmark.get("mode");
                    String benchmarkFingerprint = (String) benchmark.get("manualFingerprint");
                    namesToFingerprints.put(benchmarkName, benchmarkFingerprint);
                    // fetch and store data from CyBench UI
                    if (Requests.getInstance().fetchBenchmarks(benchmarkName, benchmarkFingerprint, accessToken)) {
                        // store new data in map if this report hasn't been added already
                        if (reportID != null && !Requests.getReports().contains(reportID)) {
                            Map<String, Map<String, List<Double>>> benchTable = Requests
                                    .getBenchmarks(benchmarkFingerprint);
                            Requests.storeBenchmarkData(benchTable, benchmarkMode, benchmarkVersion, score);
                        }

                        Comparisons.Method compareMethod = (Comparisons.Method) defaultConfigs.get("method");
                        Comparisons.Scope compareScope = (Comparisons.Scope) defaultConfigs.get("scope");
                        Double comparePercentage = (Double) defaultConfigs.get("percentage");
                        String compareVersion = (String) defaultConfigs.get("version");

                        for (Map.Entry<String, String> entry : configuredPackages.entrySet()) {
                            if (benchmarkName.startsWith(entry.getKey())) {
                                String identifier = entry.getValue();
                                Map<String, Object> specificConfigs = (Map<String, Object>) allConfigs.get(identifier);
                                compareMethod = (Comparisons.Method) specificConfigs.get("method");
                                compareScope = (Comparisons.Scope) specificConfigs.get("scope");
                                comparePercentage = (Double) specificConfigs.get("percentage");
                                compareVersion = (String) specificConfigs.get("version");
                                break;
                            }
                        }

                        if (compareScope.equals(Comparisons.Scope.BETWEEN)) {
                            if (benchmarkVersion.equals(compareVersion)) {
                                log.warn(
                                        "{} - {}: the compare version specified ({}) is the same as the currently benchmarked version ({}), will perform WITHIN VERSION comparisons",
                                        benchmarkName, benchmarkMode, compareVersion, benchmarkVersion);
                                compareScope = ConfigHandling.DEFAULT_COMPARE_SCOPE;
                            }
                        }

                        if (passedBenchmark(benchmarkName, benchmarkFingerprint, benchmarkVersion, benchmarkMode,
                                compareMethod, comparePercentage, compareScope, compareVersion)) {
                            totalPassedBenchmarks++;
                            addPassFailBenchData(passedBenchmarks, benchmarkName, benchmarkVersion, benchmarkMode,
                                    score, compareMethod, comparePercentage, compareScope, compareVersion);
                        } else {
                            totalFailedBenchmarks++;
                            addPassFailBenchData(failedBenchmarks, benchmarkName, benchmarkVersion, benchmarkMode,
                                    score, compareMethod, comparePercentage, compareScope, compareVersion);
                        }
                    } else {
                        failFetch = true;
                        break;
                    }
                }
            }
            if (!failFetch) {
                Requests.getInstance().close();
                System.out.print("\n\n");
                log.info("compared={}, passed={}, failed={}", totalComparedBenchmarks, totalPassedBenchmarks,
                        totalFailedBenchmarks);
                System.out.print("\n");
                printBenchmarkResults(totalComparedBenchmarks, totalPassedBenchmarks, passedBenchmarks,
                        totalFailedBenchmarks, failedBenchmarks, namesToFingerprints);
                if (totalFailedBenchmarks > 0) {
                    String error = "* Build failed due to scores being too low *";
                    log.error(error + "\n");

                    // helps logs finish before exception is thrown
                    TimeUnit.MILLISECONDS.sleep(500);
                    throw new Exception(error);
                }
            } else {
                log.error("* Fetching and storing benchmark data for analysis failed *");
            }
        }
    }

    private static void addPassFailBenchData(Map<String, Map<String, Map<String, List<Object>>>> benchmarks,
            String benchmarkName, String benchmarkVersion, String benchmarkMode, Double score,
            Comparisons.Method compareMethod, Double comparePercentage, Comparisons.Scope compareScope,
            String compareVersion) {

        if (!benchmarks.containsKey(benchmarkName)) {
            List<Object> data = new ArrayList<>();
            Map<String, List<Object>> scoresPerMode = new HashMap<>();
            Map<String, Map<String, List<Object>>> scoresPerVersion = new HashMap<>();
            scoresPerMode.put(benchmarkMode, data);
            scoresPerVersion.put(benchmarkVersion, scoresPerMode);
            benchmarks.put(benchmarkName, scoresPerVersion);
        }

        Map<String, Map<String, List<Object>>> scoresPerVersion = benchmarks.get(benchmarkName);
        if (!scoresPerVersion.containsKey(benchmarkVersion)) {
            List<Object> data = new ArrayList<>();
            Map<String, List<Object>> scoresPerMode = new HashMap<>();
            scoresPerMode.put(benchmarkMode, data);
            scoresPerVersion.put(benchmarkVersion, scoresPerMode);
        }

        Map<String, List<Object>> scoresPerMode = scoresPerVersion.get(benchmarkVersion);
        if (!scoresPerMode.containsKey(benchmarkMode)) {
            List<Object> data = new ArrayList<>();
            scoresPerMode.put(benchmarkMode, data);
        }

        List<Object> data = scoresPerMode.get(benchmarkMode);
        data.add(score);
        data.add(compareMethod);
        data.add(compareScope);
        data.add(compareVersion);
    }

    private static void printBenchmarkResults(int totalComparedBenchmarks, int totalPassedBenchmarks,
            Map<String, Map<String, Map<String, List<Object>>>> passedBenchmarks, int totalFailedBenchmarks,
            Map<String, Map<String, Map<String, List<Object>>>> failedBenchmarks,
            Map<String, String> namesToFingerprints) {
        if (totalPassedBenchmarks > 0) {
            printBenchmarkResultsHelper("PASSED", totalComparedBenchmarks, totalPassedBenchmarks, passedBenchmarks,
                    namesToFingerprints);
        }
        System.out.print("\n");
        if (totalFailedBenchmarks > 0) {
            printBenchmarkResultsHelper("FAILED", totalComparedBenchmarks, totalFailedBenchmarks, failedBenchmarks,
                    namesToFingerprints);
        }
        System.out.print("\n");
        log.info("* Completed benchmark analysis\n");
    }

    private static void printBenchmarkResultsHelper(String passfail, int totalComparedBenchmarks,
            int totalBenchmarksToReport, Map<String, Map<String, Map<String, List<Object>>>> benchmarksToReport,
            Map<String, String> namesToFingerprints) {
        log.info("** {}/{} benchmarks {}:", totalBenchmarksToReport, totalComparedBenchmarks, passfail);
        for (Map.Entry<String, Map<String, Map<String, List<Object>>>> entry : benchmarksToReport.entrySet()) {
            String benchmarkName = entry.getKey();
            String fingerprint = namesToFingerprints.get(benchmarkName);
            for (String benchmarkVersion : entry.getValue().keySet()) {
                Map<String, List<Object>> data = entry.getValue().get(benchmarkVersion);
                for (Map.Entry<String, List<Object>> e : data.entrySet()) {
                    String benchmarkMode = e.getKey();
                    Double score = (Double) e.getValue().get(0);
                    Comparisons.Method compareMethod = (Comparisons.Method) e.getValue().get(1);
                    Comparisons.Scope compareScope = (Comparisons.Scope) e.getValue().get(2);
                    if (compareScope.equals(Comparisons.Scope.BETWEEN)) {
                        String compareVersion = (String) e.getValue().get(3);
                        log.info(
                                "   test.name={}, test.version={}, test.mode={}, test.score={}, test.compare.method={}, test.compare.scope={}, test.compare.version={}, test.id={}",
                                benchmarkName, benchmarkVersion, benchmarkMode, score, compareMethod, compareScope,
                                compareVersion, fingerprint);
                    } else {
                        log.info(
                                "   test.name={}, test.version={}, test.mode={}, test.score={}, test.compare.method={}, test.compare.scope={}, test.id={}",
                                benchmarkName, benchmarkVersion, benchmarkMode, score, compareMethod, compareScope,
                                fingerprint);
                    }
                }
                // TODO log how much it passed by
            }
        }
    }

    private static boolean passedBenchmark(String benchmarkName, String benchmarkFingerprint, String benchmarkVersion,
            String benchmarkMode, Comparisons.Method compareMethod, Double comparePercentage,
            Comparisons.Scope compareScope, String compareVersion) {

        List<Double> currentVersionScores = Requests.getBenchmarks(benchmarkFingerprint, benchmarkVersion,
                benchmarkMode);

        if (compareScope.equals(Comparisons.Scope.BETWEEN)) {
            if (Requests.getBenchmarks(benchmarkFingerprint).containsKey(compareVersion)
                    && Requests.getBenchmarks(benchmarkFingerprint, compareVersion).containsKey(benchmarkMode)) {
                List<Double> compareVersionScores = Requests.getBenchmarks(benchmarkFingerprint, compareVersion,
                        benchmarkMode);
            } else {
                log.warn("There are no previous benchmarks for the specified compare version ({}) for {}, mode: {}!",
                        compareVersion, benchmarkName, benchmarkMode);
                return true;
            }
        }
        if (compareScope.equals(Comparisons.Scope.WITHIN)) {
            if (currentVersionScores.size() <= 1) {
                log.info(
                        "The new benchmark for {}, mode: {} has no previously tested benchmarks to compare to within the version {}",
                        benchmarkName, benchmarkMode, benchmarkVersion);
                return true;
            }
        }

        // switch (compareMethod) {
        // case MEAN:
        // return Comparisons.compareMean(currentVersionScores, totalScores);
        // case DELTA:
        // return Comparisons.compareDelta(currentVersionScores, totalScores);
        // case SD:
        // return Comparisons.compareSD(currentVersionScores, totalScores);
        // case MOVING_AVERAGE:
        // return Comparisons.compare5MA(currentVersionScores, totalScores);
        // default:
        // return false;
        // }

        // TODO full implementation
        return false;
    }

}
