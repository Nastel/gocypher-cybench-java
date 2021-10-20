package com.gocypher.cybench;

import java.io.File;
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
        Map<String, String> configuredPackages = ConfigHandling.identifyAndValidifySpecificConfigs(allConfigs);

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
        // < BenchmarkName, < BenchmarkVersion, < BenchmarkMode, < Data >>>
        Map<String, Map<String, Map<String, Map<String, Object>>>> passedBenchmarks = new HashMap<>();
        Map<String, Map<String, Map<String, Map<String, Object>>>> failedBenchmarks = new HashMap<>();
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
                        Comparisons.Range compareRange = (Comparisons.Range) defaultConfigs.get("range");
                        Comparisons.Threshold compareThreshold = (Comparisons.Threshold) defaultConfigs
                                .get("threshold");
                        Double comparePercentage = (Double) defaultConfigs.get("percentage");
                        String compareVersion = (String) defaultConfigs.get("version");

                        for (Map.Entry<String, String> entry : configuredPackages.entrySet()) {
                            if (benchmarkName.startsWith(entry.getKey())) {
                                String identifier = entry.getValue();
                                Map<String, Object> specificConfigs = (Map<String, Object>) allConfigs.get(identifier);
                                compareMethod = (Comparisons.Method) specificConfigs.get("method");
                                compareScope = (Comparisons.Scope) specificConfigs.get("scope");
                                compareRange = (Comparisons.Range) specificConfigs.get("range");
                                compareThreshold = (Comparisons.Threshold) specificConfigs.get("threshold");
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
                        } else {
                            // Using WITHIN comparison, doesn't matter what the specified compareVersion was, the
                            // version compared to will be the benchmarkVersion
                            compareVersion = benchmarkVersion;
                        }

                        Double scoreDiff = getScoreDifference(benchmarkName, benchmarkFingerprint, benchmarkVersion,
                                benchmarkMode, compareMethod, compareScope, compareRange, compareThreshold, compareVersion);

                        boolean pass = passedBenchmark(scoreDiff, compareThreshold, comparePercentage);
                        if (pass)
                        	totalPassedBenchmarks++;
                        else
                        	totalFailedBenchmarks++;
                        
                        addPassFailBenchData(pass ? passedBenchmarks : failedBenchmarks, scoreDiff,
                                benchmarkName, benchmarkVersion, benchmarkMode, score, compareMethod, compareScope,
                                compareRange, compareThreshold, comparePercentage, compareVersion);
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

    private static void addPassFailBenchData(Map<String, Map<String, Map<String, Map<String, Object>>>> benchmarks,
            Double scoreDiff, String benchmarkName, String benchmarkVersion, String benchmarkMode, Double score,
            Comparisons.Method compareMethod, Comparisons.Scope compareScope, Comparisons.Range compareRange,
            Comparisons.Threshold compareThreshold, Double comparePercentage, String compareVersion) {

        if (!benchmarks.containsKey(benchmarkName)) {
            Map<String, Object> data = new HashMap<>();
            Map<String, Map<String, Object>> scoresPerMode = new HashMap<>();
            Map<String, Map<String, Map<String, Object>>> scoresPerVersion = new HashMap<>();
            scoresPerMode.put(benchmarkMode, data);
            scoresPerVersion.put(benchmarkVersion, scoresPerMode);
            benchmarks.put(benchmarkName, scoresPerVersion);
        }

        Map<String, Map<String, Map<String, Object>>> scoresPerVersion = benchmarks.get(benchmarkName);
        if (!scoresPerVersion.containsKey(benchmarkVersion)) {
            Map<String, Object> data = new HashMap<>();
            Map<String, Map<String, Object>> scoresPerMode = new HashMap<>();
            scoresPerMode.put(benchmarkMode, data);
            scoresPerVersion.put(benchmarkVersion, scoresPerMode);
        }

        Map<String, Map<String, Object>> scoresPerMode = scoresPerVersion.get(benchmarkVersion);
        if (!scoresPerMode.containsKey(benchmarkMode)) {
            Map<String, Object> data = new HashMap<>();
            scoresPerMode.put(benchmarkMode, data);
        }

        Map<String, Object> data = scoresPerMode.get(benchmarkMode);
        data.put("score", score);
        data.put("scoreDiff", scoreDiff);
        data.put("compareMethod", compareMethod);
        data.put("compareScope", compareScope);
        data.put("compareRange", compareRange);
        data.put("compareThreshold", compareThreshold);
        data.put("compareVersion", compareVersion);
        data.put("comparePercentage", comparePercentage);
    }

    private static void printBenchmarkResults(int totalComparedBenchmarks, int totalPassedBenchmarks,
            Map<String, Map<String, Map<String, Map<String, Object>>>> passedBenchmarks, int totalFailedBenchmarks,
            Map<String, Map<String, Map<String, Map<String, Object>>>> failedBenchmarks,
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
            int totalBenchmarksToReport, Map<String, Map<String, Map<String, Map<String, Object>>>> benchmarksToReport,
            Map<String, String> namesToFingerprints) {
        log.info("** {}/{} benchmarks {}:", totalBenchmarksToReport, totalComparedBenchmarks, passfail);
        for (Map.Entry<String, Map<String, Map<String, Map<String, Object>>>> entry : benchmarksToReport.entrySet()) {
            String benchmarkName = entry.getKey();
            String fingerprint = namesToFingerprints.get(benchmarkName);
            for (String benchmarkVersion : entry.getValue().keySet()) {
                Map<String, Map<String, Object>> data = entry.getValue().get(benchmarkVersion);
                for (Map.Entry<String, Map<String, Object>> e : data.entrySet()) {
                    String benchmarkMode = e.getKey();
                    Double score = (Double) e.getValue().get("score");
                    Double scoreDiff = (Double) e.getValue().get("scoreDiff");
                    Comparisons.Method compareMethod = (Comparisons.Method) e.getValue().get("compareMethod");
                    Comparisons.Scope compareScope = (Comparisons.Scope) e.getValue().get("compareScope");
                    Comparisons.Range compareRange = (Comparisons.Range) e.getValue().get("compareRange");
                    Comparisons.Threshold compareThreshold = (Comparisons.Threshold) e.getValue()
                            .get("compareThreshold");
                    String compareVersion = (String) e.getValue().get("compareVersion");

                    if (compareThreshold.equals(Comparisons.Threshold.GREATER)) {
                        log.info(
                                "   test.name={}, test.version={}, test.mode={}, test.score={}, test.score.difference={}, test.compare.method={}, test.compare.scope={}, test.compare.version={}, "
                                        + "test.compare.range={}, test.compare.threshold={}, test.id={}",
                                benchmarkName, benchmarkVersion, benchmarkMode, score, scoreDiff, compareMethod,
                                compareScope, compareVersion, compareRange, compareThreshold, fingerprint);
                    } else {
                        Double comparePercentage = (Double) e.getValue().get("comparePercentage");
                        log.info(
                                "   test.name={}, test.version={}, test.mode={}, test.score={}, test.score.difference={}%, test.compare.method={}, test.compare.scope={}, test.compare.version={}, "
                                        + "test.compare.range={}, test.compare.threshold={}, test.compare.percentage={}, test.id={}",
                                benchmarkName, benchmarkVersion, benchmarkMode, score, scoreDiff, compareMethod,
                                compareScope, compareVersion, compareRange, compareThreshold, comparePercentage,
                                fingerprint);
                    }
                }
            }
        }
    }

    private static Double getScoreDifference(String benchmarkName, String benchmarkFingerprint, String benchmarkVersion,
            String benchmarkMode, Comparisons.Method compareMethod, Comparisons.Scope compareScope,
            Comparisons.Range compareRange, Comparisons.Threshold compareThreshold, String compareVersion) {

        Double scoreDiff = 0.0;

        List<Double> currentVersionScores = Requests.getBenchmarks(benchmarkFingerprint, benchmarkVersion,
                benchmarkMode);
        Double recentScore = currentVersionScores.get(currentVersionScores.size() - 1);
        currentVersionScores.remove(currentVersionScores.size() - 1);

        List<Double> compareVersionScores = currentVersionScores;

        if (compareScope.equals(Comparisons.Scope.BETWEEN)) {
            if (Requests.getBenchmarks(benchmarkFingerprint).containsKey(compareVersion)
                    && Requests.getBenchmarks(benchmarkFingerprint, compareVersion).containsKey(benchmarkMode)) {
                compareVersionScores = Requests.getBenchmarks(benchmarkFingerprint, compareVersion, benchmarkMode);
            } else {
                log.warn("{} - {}: There are no benchmarks for the specified compare version ({})", benchmarkName,
                        benchmarkMode, compareVersion);
                return scoreDiff;
            }
        } else if (compareScope.equals(Comparisons.Scope.WITHIN)) {
            if (currentVersionScores.size() <= 1) {
                log.warn("{} - {}: There are no previously tested benchmarks within the version ({})", benchmarkName,
                        benchmarkMode, benchmarkVersion);
                return scoreDiff;
            }
        }

        switch (compareMethod) {
        case MEAN:
            scoreDiff = Comparisons.compareMean(compareVersionScores, recentScore, compareRange, compareThreshold);
            break;
        case DELTA:
            scoreDiff = Comparisons.compareDelta(compareVersionScores, recentScore, compareRange, compareThreshold);
            break;
        case SD:
            scoreDiff = Comparisons.compareSD(compareVersionScores, recentScore, compareRange, compareThreshold);
            break;
        }
        
        return scoreDiff;
    }
    
    private static boolean passedBenchmark(Double scoreDiff, Comparisons.Threshold compareThreshold, Double comparePercentage) {
    	if(compareThreshold.equals(Comparisons.Threshold.PERCENT_CHANGE) && scoreDiff < 0 && Math.abs(scoreDiff) > comparePercentage) {
        	return false;
        } else if (!compareThreshold.equals(Comparisons.Threshold.PERCENT_CHANGE) && scoreDiff < 0) {
        	return false;
        }
    	
    	return true;
    }

}
