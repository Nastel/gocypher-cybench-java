package com.gocypher.cybench;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptEngine;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
    private static final Map<String, Map<String, Map<String, Map<String, Object>>>> passedBenchmarks = new HashMap<>();
    private static int totalPassedBenchmarks;
    private static final Map<String, Map<String, Map<String, Map<String, Object>>>> failedBenchmarks = new HashMap<>();
    private static int totalFailedBenchmarks;

    public static void main(String... args) throws Exception {
        log.info("* Analyzing benchmark performance...");

        Options options = new Options();
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
            log.info("Attempting to evaluate custom defined script at {}\n", scriptPath);

            ComparatorScriptEngine cse = new ComparatorScriptEngine(passedProps);
            File userScript = cse.loadUserScript(scriptPath);
            ScriptEngine engine = cse.prepareScriptEngine();
            cse.runUserScript(engine, userScript);
        } else {
            if (configPath == null) {
                log.info("No script or config file specified, looking for comparator.yaml in default location");
                configPath = ConfigHandling.DEFAULT_COMPARATOR_CONFIG_PATH;
            }

            log.info("Attempting to load comparator configurations at {}\n", configPath);
            Map<String, Object> allConfigs = ConfigHandling.loadYaml(configPath);
            Map<String, String> configuredPackages = ConfigHandling.identifyAndValidifySpecificConfigs(allConfigs);

            File recentReport = ConfigHandling.identifyRecentReport((String) allConfigs.get("reports"));
            String accessToken = (String) allConfigs.get(ConfigHandling.TOKEN);
            
            if (recentReport != null && accessToken != null) {
                analyzeBenchmarks(accessToken, recentReport, allConfigs, configuredPackages);
            } else {
                if (recentReport == null) {
                    log.error("* No recent report found to compare!");
                } else if (accessToken == null) {
                    log.error("* Failed to authorize provided access token!");
                }
            }
        }
    }

    private static void analyzeBenchmarks(String accessToken, File recentReport, Map<String, Object> allConfigs,
            Map<String, String> configuredPackages) throws Exception {
    	Map<String, Map<String, Map<String, Double>>> recentReports = Requests.getBenchmarksFromReport(accessToken, recentReport);
        if (recentReports != null) {
        	int totalComparedBenchmarks = 0;
        	for(String benchmarkFingerprint : recentReports.keySet()) {
        		Map<String, Map<String, Double>> versionsTested = recentReports.get(benchmarkFingerprint);
        		for(String benchmarkVersion : versionsTested.keySet()) {
        			Map<String, Double> modesTested = versionsTested.get(benchmarkVersion);
        			for(String benchmarkMode : modesTested.keySet()) {
                    	totalComparedBenchmarks++;
        				Double benchmarkScore = modesTested.get(benchmarkMode);
        				String benchmarkName = Requests.fingerprintsToNames.get(benchmarkFingerprint);
        				
        				compareBenchmark(benchmarkName, benchmarkFingerprint, benchmarkVersion, benchmarkMode,
                                benchmarkScore, allConfigs, configuredPackages);
        			}
        		}
        	}
        	
            System.out.print("\n\n");
            log.info("compared={}, passed={}, failed={}", totalComparedBenchmarks, totalPassedBenchmarks,
                    totalFailedBenchmarks);
            System.out.print("\n");
            printBenchmarkResults(totalComparedBenchmarks, totalPassedBenchmarks, passedBenchmarks,
                    totalFailedBenchmarks, failedBenchmarks, Requests.namesToFingerprints);
            if (totalFailedBenchmarks > 0) {
                String error = "* Build failed due to scores being too low *";
                log.error(error + "\n");

                // helps logs finish before exception is thrown
                TimeUnit.MILLISECONDS.sleep(500);
                throw new Exception(error);
            }
        }
    }

    private static void addPassFailBenchData(Map<String, Map<String, Map<String, Map<String, Object>>>> benchmarks,
            Double benchmarkScore, Double COMPARE_VALUE, String benchmarkName, String benchmarkVersion,
            String benchmarkMode, Comparisons.Method compareMethod, Comparisons.Scope compareScope, String compareRange,
            Comparisons.Threshold compareThreshold, Double percentChangeAllowed, Double deviationsAllowed,
            String compareVersion) {

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
        data.put(ConfigHandling.METHOD, compareMethod);
        data.put(ConfigHandling.SCOPE, compareScope);
        data.put(ConfigHandling.RANGE, compareRange);
        data.put(ConfigHandling.THRESHOLD, compareThreshold);
        data.put(ConfigHandling.COMPARE_VERSION, compareVersion);
        data.put(ConfigHandling.PERCENT_CHANGE_ALLOWED, percentChangeAllowed);
        data.put(ConfigHandling.DEVIATIONS_ALLOWED, deviationsAllowed);
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
                    Comparisons.Method compareMethod = (Comparisons.Method) e.getValue().get(ConfigHandling.METHOD);
                    Comparisons.Scope compareScope = (Comparisons.Scope) e.getValue().get(ConfigHandling.SCOPE);
                    String compareRange = (String) e.getValue().get(ConfigHandling.RANGE);
                    String compareVersion = (String) e.getValue().get(ConfigHandling.COMPARE_VERSION);

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
                        Comparisons.Threshold compareThreshold = (Comparisons.Threshold) e.getValue()
                                .get(ConfigHandling.THRESHOLD);
                        logReport.append("test.compare.threshold=").append(compareThreshold).append(", ");
                        if (compareThreshold.equals(Comparisons.Threshold.PERCENT_CHANGE)) {
                            Double percentChangeAllowed = (Double) e.getValue()
                                    .get(ConfigHandling.PERCENT_CHANGE_ALLOWED);
                            logReport.append("test.percentChangeAllowed=").append(percentChangeAllowed).append(", ");
                        }
                    } else {
                        Double deviationsAllowed = (Double) e.getValue().get(ConfigHandling.DEVIATIONS_ALLOWED);
                        logReport.append("test.deviationsAllowed=").append(deviationsAllowed).append(", ");
                    }

                    logReport.append("test.id={}");

                    log.info(logReport.toString(), benchmarkName, benchmarkVersion, benchmarkMode, benchmarkScore,
                            COMPARE_VALUE, compareMethod, compareScope, compareVersion, compareRange, fingerprint);
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
            configs = createConfigMap(compareMethod, compareScope, compareRange, 
            		compareThreshold, percentChangeAllowed, compareVersion, deviationsAllowed);
    	}
    	
        for (Map.Entry<String, String> entry : configuredPackages.entrySet()) {
            if (entry.getKey() != null && benchmarkName.startsWith(entry.getKey())) {
                String identifier = entry.getValue();
                Map<String, Object> specificConfigs = (Map<String, Object>) allConfigs.get(identifier);
                compareMethod = (Comparisons.Method) specificConfigs.get(ConfigHandling.METHOD);
                compareScope = (Comparisons.Scope) specificConfigs.get(ConfigHandling.SCOPE);
                compareRange = (String) specificConfigs.get(ConfigHandling.RANGE);
                compareThreshold = (Comparisons.Threshold) specificConfigs
                        .get(ConfigHandling.THRESHOLD);
                compareVersion = (String) specificConfigs.get(ConfigHandling.COMPARE_VERSION);
                if (specificConfigs.containsKey(ConfigHandling.PERCENT_CHANGE_ALLOWED)) {
                    percentChangeAllowed = Double.parseDouble(
                            specificConfigs.get(ConfigHandling.PERCENT_CHANGE_ALLOWED).toString());
                }
                if (specificConfigs.containsKey(ConfigHandling.DEVIATIONS_ALLOWED)) {
                    deviationsAllowed = Double.parseDouble(
                            specificConfigs.get(ConfigHandling.DEVIATIONS_ALLOWED).toString());
                }
                configs = createConfigMap(compareMethod, compareScope, compareRange, 
                		compareThreshold, percentChangeAllowed, compareVersion, deviationsAllowed);
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

            // range validation
            int range;
            if (compareRange.equals("ALL")) {
                range = compareVersionScores.size();
            } else {
                range = Integer.parseInt(compareRange);
                if (range > compareVersionScores.size()) {
                    log.warn(
                            "{} - {}: There are not enough values to compare to in version ({}) with specific range ({}), will compare with as many values as possible",
                            benchmarkName, benchmarkMode, benchmarkVersion, range);
                    range = compareVersionScores.size();

                }
            }
            compareRange = String.valueOf(range);

            if (compareScope.equals(Comparisons.Scope.WITHIN)) {
                compareVersion = benchmarkVersion;
                if (benchmarkVersionScores.size() <= 1) {
                    log.warn("{} - {}: There are no previously tested benchmarks within the version ({})", benchmarkName,
                            benchmarkMode, benchmarkVersion);
                    totalPassedBenchmarks++;
                    addPassFailBenchData(passedBenchmarks, benchmarkScore, COMPARE_VALUE, benchmarkName, benchmarkVersion,
                            benchmarkMode, compareMethod, compareScope, compareRange, compareThreshold,
                            percentChangeAllowed, deviationsAllowed, compareVersion);
                    return false;
                }
            }

            switch (compareMethod) {
            case DELTA:
                log.info("COMPARISON: {} : {} - Between versions {} and {} delta running", benchmarkName, benchmarkMode,
                        benchmarkVersion, compareVersion);
                COMPARE_VALUE = Comparisons.compareWithDelta(benchmarkVersionScores, compareVersionScores, compareThreshold,
                        compareRange);
                break;
            case SD:
                log.info("COMPARISON: {} : {} - Between versions {} and {} SD running", benchmarkName, benchmarkMode,
                        benchmarkVersion, compareVersion);
                COMPARE_VALUE = Comparisons.compareWithSD(benchmarkVersionScores, compareVersionScores, compareRange);
                break;
            }

            boolean pass = Comparisons.passAssertion(COMPARE_VALUE, compareMethod, compareThreshold, percentChangeAllowed,
                    deviationsAllowed);
            if (pass) {
                totalPassedBenchmarks++;
            } else {
                totalFailedBenchmarks++;
            }

            addPassFailBenchData(pass ? passedBenchmarks : failedBenchmarks, benchmarkScore, COMPARE_VALUE, benchmarkName,
                    benchmarkVersion, benchmarkMode, compareMethod, compareScope, compareRange, compareThreshold,
                    percentChangeAllowed, deviationsAllowed, compareVersion);

            return true;
    	} else {
    		// NO COMPARISON SHOULD BE DONE
    		// TODO PASS TEST AND LOGGING
    	}
    		return false;
    }
}
