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
    private static final Map<String, Map<String, Map<String, Map<String, Object>>>> passedBenchmarks = new HashMap<>();
    private static int totalPassedBenchmarks;
    private static final Map<String, Map<String, Map<String, Map<String, Object>>>> failedBenchmarks = new HashMap<>();
    private static int totalFailedBenchmarks;
    private static final Map<String, String> namesToFingerprints = new HashMap<>();

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

        JSONObject benchmarkReport = null;
        int totalComparedBenchmarks = 0;
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
                    Double benchmarkScore = (Double) benchmark.get("score");
                    String benchmarkMode = (String) benchmark.get("mode");
                    String benchmarkFingerprint = (String) benchmark.get("manualFingerprint");
                    namesToFingerprints.put(benchmarkName, benchmarkFingerprint);
                    // fetch and store data from CyBench UI
                    if (Requests.getInstance().fetchBenchmarks(benchmarkName, benchmarkFingerprint, accessToken)) {
                        // store new data in map if this report hasn't been added already
                        if (reportID != null && !Requests.getReports().contains(reportID)) {
                            Map<String, Map<String, List<Double>>> benchTable = Requests
                                    .getBenchmarks(benchmarkFingerprint);
                            Requests.storeBenchmarkData(benchTable, benchmarkMode, benchmarkVersion, benchmarkScore);
                        }

                        Comparisons.Method compareMethod = (Comparisons.Method) defaultConfigs.get("method");
                        Comparisons.Scope compareScope = (Comparisons.Scope) defaultConfigs.get("scope");
                        String compareRange = (String) defaultConfigs.get("range");
                        Comparisons.Threshold compareThreshold = (Comparisons.Threshold) defaultConfigs
                                .get("threshold");
                        Double percentageAllowed = (Double) defaultConfigs.get("percentageAllowed");
                        String compareVersion = (String) defaultConfigs.get("version");
                        Double deviationsAllowed = (Double) defaultConfigs.get("deviationsAllowed");

                        for (Map.Entry<String, String> entry : configuredPackages.entrySet()) {
                            if (entry.getKey() != null && benchmarkName.startsWith(entry.getKey())) {
                                String identifier = entry.getValue();
                                Map<String, Object> specificConfigs = (Map<String, Object>) allConfigs.get(identifier);
                                compareMethod = (Comparisons.Method) specificConfigs.get("method");
                                compareScope = (Comparisons.Scope) specificConfigs.get("scope");
                                compareRange = (String) specificConfigs.get("range");
                                compareThreshold = (Comparisons.Threshold) specificConfigs.get("threshold");
                                compareVersion = (String) specificConfigs.get("version");
                                if(specificConfigs.containsKey("percentageAllowed")) {
                                	percentageAllowed = Double.parseDouble(specificConfigs.get("percentageAllowed").toString());
                                }
                                if(specificConfigs.containsKey("deviationsAllowed")) {
                                	deviationsAllowed = Double.parseDouble(specificConfigs.get("deviationsAllowed").toString());
                                }
                                break;
                            }
                        }

                        compareBenchmark(benchmarkName, benchmarkFingerprint, benchmarkVersion, benchmarkMode,
                                benchmarkScore, compareMethod, compareScope, compareRange, compareThreshold,
                                compareVersion, percentageAllowed, deviationsAllowed);
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
            Double benchmarkScore, Double COMPARE_VALUE, 
            String benchmarkName, String benchmarkVersion, String benchmarkMode, Comparisons.Method compareMethod,
            Comparisons.Scope compareScope, String compareRange, Comparisons.Threshold compareThreshold,
            Double percentageAllowed, Double deviationsAllowed, String compareVersion) {

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
        data.put("benchmarkScore", benchmarkScore);
        data.put("COMPARE_VALUE", COMPARE_VALUE);
        data.put("compareMethod", compareMethod);
        data.put("compareScope", compareScope);
        data.put("compareRange", compareRange);
        data.put("compareThreshold", compareThreshold);
        data.put("compareVersion", compareVersion);
        data.put("percentageAllowed", percentageAllowed);
        data.put("deviationsAllowed", deviationsAllowed);
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
                    Double benchmarkScore = (Double) e.getValue().get("benchmarkScore");
                    Double COMPARE_VALUE = (Double) e.getValue().get("COMPARE_VALUE");
                    Comparisons.Method compareMethod = (Comparisons.Method) e.getValue().get("compareMethod");
                    Comparisons.Scope compareScope = (Comparisons.Scope) e.getValue().get("compareScope");
                    String compareRange = (String) e.getValue().get("compareRange");
                    String compareVersion = (String) e.getValue().get("compareVersion");
                    
                    
                    String compareStr = "";
                    if (compareMethod == Comparisons.Method.DELTA)
                    	compareStr = "test.delta={}, ";
                    else
                    	compareStr = "test.SDsFromMean={}, ";

                    StringBuilder logReport = new StringBuilder(
                            "   test.name={}, test.version={}, test.mode={}, test.score={}, " + compareStr + "test.compare.method={}, "
                            + "test.compare.scope={}, test.compare.version={}, test.compare.range={}, ");

                    if (compareMethod.equals(Comparisons.Method.DELTA)) {
                    	Comparisons.Threshold compareThreshold = (Comparisons.Threshold) e.getValue()
                                .get("compareThreshold");
                    	logReport.append("test.compare.threshold=").append(compareThreshold).append(", ");
                    	if (compareThreshold.equals(Comparisons.Threshold.PERCENT_CHANGE)) {
                    		Double percentageAllowed = (Double) e.getValue().get("percentageAllowed");
                            logReport.append("test.percentageAllowed=").append(percentageAllowed).append(", ");
                    	}
                    } else {
                    	Double deviationsAllowed = (Double) e.getValue().get("deviationsAllowed");
                    	logReport.append("test.deviationsAllowed=").append(deviationsAllowed).append(", ");
                    }

                    logReport.append("test.id={}");

                    log.info(logReport.toString(), benchmarkName, benchmarkVersion, benchmarkMode, benchmarkScore,
                    		COMPARE_VALUE, compareMethod, compareScope, compareVersion, compareRange,
                            fingerprint);
                }
            }
        }
    }

    private static boolean compareBenchmark(String benchmarkName, String benchmarkFingerprint, String benchmarkVersion,
            String benchmarkMode, Double benchmarkScore, Comparisons.Method compareMethod,
            Comparisons.Scope compareScope, String compareRange, Comparisons.Threshold compareThreshold,
            String compareVersion, Double percentageAllowed, Double deviationsAllowed) {

        Double COMPARE_VALUE = null;

        List<Double> benchmarkVersionScores = Requests.getBenchmarks(benchmarkFingerprint, benchmarkVersion,
                benchmarkMode);

        // default the comparison scores to the current version without the newly added
        // benchmark
        List<Double> compareVersionScores = new ArrayList<>(benchmarkVersionScores);
        compareVersionScores.remove(benchmarkVersionScores.size() - 1);

        if (compareScope.equals(Comparisons.Scope.BETWEEN)) {
            if (benchmarkVersion.equals(compareVersion)) {
                log.warn(
                        "{} - {}: the compare version specified ({}) is the same as the currently benchmarked version ({}), will perform WITHIN VERSION comparisons",
                        benchmarkName, benchmarkMode, compareVersion, benchmarkVersion);
                compareScope = ConfigHandling.DEFAULT_COMPARE_SCOPE;
            } else if (Requests.getBenchmarks(benchmarkFingerprint).containsKey(compareVersion)
                    && Requests.getBenchmarks(benchmarkFingerprint, compareVersion).containsKey(benchmarkMode)) {
                compareVersionScores = Requests.getBenchmarks(benchmarkFingerprint, compareVersion, benchmarkMode);
            } else {
                log.warn(
                        "{} - {}: There are no benchmarks for the specified compare version ({}), will perform WITHIN VERSION comparisons",
                        benchmarkName, benchmarkMode, compareVersion);
                compareScope = ConfigHandling.DEFAULT_COMPARE_SCOPE;
            }
        }

        Integer range = null;
        
        if (compareRange.equals("ALL")) {
        	range = compareVersionScores.size();
        } else {
        	range = Integer.parseInt(compareRange);
        	if (range > compareVersionScores.size()) {
        		log.warn("{} - {}: There are not enough values to compare to within version ({}) with specific range ({}), will compare with as many values as possible", 
        				benchmarkName, benchmarkMode, benchmarkVersion, range);
        		range = compareVersionScores.size();
        		compareRange = range.toString();
        	}
        }
        
        if (compareScope.equals(Comparisons.Scope.WITHIN)) {
            compareVersion = benchmarkVersion;
            if (benchmarkVersionScores.size() <= 1) {
                log.warn("{} - {}: There are no previously tested benchmarks within the version ({})", benchmarkName,
                        benchmarkMode, benchmarkVersion);
                totalPassedBenchmarks++;
                addPassFailBenchData(passedBenchmarks, benchmarkScore, COMPARE_VALUE, benchmarkName, benchmarkVersion, 
                		benchmarkMode, compareMethod, compareScope, compareRange,
                		compareThreshold, percentageAllowed, deviationsAllowed, compareVersion);
                return false;
            }
        }
        
        switch (compareMethod) {
        case DELTA:
        	COMPARE_VALUE = Comparisons.compareWithDelta(benchmarkVersionScores, compareVersionScores, compareThreshold,
        			range);
            break;
        case SD:
        	COMPARE_VALUE = Comparisons.compareWithSD(benchmarkVersionScores, compareVersionScores, compareThreshold,
        			range);
            break;
        }

        boolean pass = passAssertion(COMPARE_VALUE, compareMethod, compareThreshold, percentageAllowed, deviationsAllowed);
        if (pass) {
            totalPassedBenchmarks++;
        } else {
            totalFailedBenchmarks++;
        }

        addPassFailBenchData(pass ? passedBenchmarks : failedBenchmarks, benchmarkScore, COMPARE_VALUE,
                benchmarkName, benchmarkVersion, benchmarkMode, compareMethod, compareScope,
                compareRange, compareThreshold, percentageAllowed, deviationsAllowed, compareVersion);

        return true;
    }

    private static boolean passAssertion(Double COMPARE_VALUE, Comparisons.Method method, 
    		Comparisons.Threshold threshold, Double percentageAllowed, Double deviationsAllowed) {
    	
    	// assert within x SDs from mean
    	if (method.equals(Comparisons.Method.SD)) {
    		return passAssertionDeviation(COMPARE_VALUE, deviationsAllowed);
    	}
    	
    	// assert within x Percentage from COMPARE_VALUE
        if (threshold.equals(Comparisons.Threshold.PERCENT_CHANGE)) {
        	return passAssertionPercentage(COMPARE_VALUE, percentageAllowed);
        }

        // assert higher than COMPARE_VALUE
        return passAssertionPositive(COMPARE_VALUE);
    }
    
    private static boolean passAssertionDeviation(Double deviationsFromMean, Double deviationsAllowed) {
    	return deviationsFromMean < deviationsAllowed;
    }
    
    private static boolean passAssertionPercentage(Double percentChange, Double percentageAllowed) {
    	 return Math.abs(percentChange) < percentageAllowed;
    }
    
    private static boolean passAssertionPositive(Double val) {
    	return val >= 0;
    }
}
