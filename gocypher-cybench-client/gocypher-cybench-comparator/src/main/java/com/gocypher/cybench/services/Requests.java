package com.gocypher.cybench.services;

import java.io.File;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gocypher.cybench.utils.ConfigHandling;

public class Requests {
    private static final Logger log = LoggerFactory.getLogger(Requests.class);

    private static final String benchmarkViewBenchmarksServiceUrl = "https://www.gocypher.com/gocypher-benchmarks-reports/services/v1/reports/benchmark/view/";
    private static final String localBenchmarkViewBenchmarksServiceUrl = "http://localhost:8080/gocypher-benchmarks-reports-1.0-SNAPSHOT/services/v1/reports/benchmark/view/";

    private static Requests instance;
    private String tagFingerprint = "benchmarkTag";

    // <Benchmark Fingerprint : <Version : <Mode : <List of Scores in Test Order>>>>
    public static Map<String, Map<String, Map<String, List<Double>>>> allBenchmarks = new HashMap<>();
    // <Benchmark Fingerprint : <Version : <Mode : <Score>>>>
    public static Map<String, Map<String, Map<String, Double>>> recentBenchmarks = new HashMap<>();
    
    // <fingerprint : name>
    public static Map<String, String> fingerprintsToNames = new HashMap<>();
    // <name : fingerprint>
    public static Map<String, String> namesToFingerprints = new HashMap<>();
    // keep track of reports preventing duplicates
    public static Set<String> reportIDs = new HashSet<>();

    // <Fingerprint: (latestVersion, currentVersion, previousVersion)>
    private static Map<String, Map<String, String>> allVersions = new HashMap<>();

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private Requests() {
    }

    public static Requests getInstance() {
        if (instance == null) {
            instance = new Requests();
        }
        return instance;
    }

    public static Map<String, Map<String, Map<String, List<Double>>>> getBenchmarks() {
        Map<String, Map<String, Map<String, List<Double>>>> val = allBenchmarks;
        if (val == null) {
            log.warn("There are no benchmarks!");
        }
        return val;
    }

    public static Map<String, Map<String, List<Double>>> getBenchmarks(String benchmarkFingerprint) {
        Map<String, Map<String, List<Double>>> val = allBenchmarks.get(benchmarkFingerprint);
        if (val == null) {
            log.warn("There are no benchmarks for {}!", fingerprintsToNames.get(benchmarkFingerprint));
        }
        return val;
    }

    public static Map<String, List<Double>> getBenchmarks(String benchmarkFingerprint, String version) {
        Map<String, List<Double>> val = getBenchmarks(benchmarkFingerprint).get(version);
        if (val == null) {
            log.warn("There are no benchmarks for {}, version {}!", fingerprintsToNames.get(benchmarkFingerprint),
                    version);
        }
        return val;
    }

    public static List<Double> getBenchmarks(String benchmarkFingerprint, String version, String mode) {
        List<Double> val = getBenchmarks(benchmarkFingerprint, version).get(mode);
        if (val == null) {
            log.warn("There are no benchmarks for {}, version {}, mode {}!",
                    fingerprintsToNames.get(benchmarkFingerprint), version, mode);
        }
        return val;
    }

    public static Set<String> getReports() {
        return reportIDs;
    }

    public static String getLatestVersion(String fingerprint) {
    	if (allVersions.containsKey(fingerprint))
    		return allVersions.get(fingerprint).get("latestVersion");
    	return null;
    }
    
    public static void setLatestVersion(String fingerprint, String version) {
    	if (!allVersions.containsKey("fingerprint")) {
    		HashMap<String, String> versionData = new HashMap<>();
    		allVersions.put(fingerprint, versionData);
    	}
    	allVersions.get(fingerprint).put("latestVersion", version);
    }

    public static String getCurrentVersion(String fingerprint) {
    	if (allVersions.containsKey(fingerprint))
    		return allVersions.get(fingerprint).get("currentVersion");
    	return null;
    }
    
    public static void setCurrentVersion(String fingerprint, String version) {
    	if (!allVersions.containsKey("fingerprint")) {
    		HashMap<String, String> versionData = new HashMap<>();
    		allVersions.put(fingerprint, versionData);
    	}
    	allVersions.get(fingerprint).put("currentVersion", version);
    }

    public static String getPreviousVersion(String fingerprint) {
    	if (allVersions.containsKey(fingerprint))
    		return allVersions.get(fingerprint).get("previousVersion");
    	return null;
    }
    
    public static void setPreviousVersion(String fingerprint, String version) {
    	if (!allVersions.containsKey("fingerprint")) {
    		HashMap<String, String> versionData = new HashMap<>();
    		allVersions.put(fingerprint, versionData);
    	}
    	allVersions.get(fingerprint).put("previousVersion", version);
    }

    public boolean fetchBenchmarks(String name, String benchmarkFingerprint, String accessToken) {
        if (!allBenchmarks.containsKey(benchmarkFingerprint)) {
            String serviceUrl = localBenchmarkViewBenchmarksServiceUrl + benchmarkFingerprint + "?typeOfFingerprint="
                    + tagFingerprint;
            log.info("* Fetching benchmark data for {}", name);
            try {
                URIBuilder uri = new URIBuilder(serviceUrl);
                uri.setParameter("typeOfFingerprint", tagFingerprint);

                HttpGet request = new HttpGet(uri.build());
                request.setHeader("x-api-key", accessToken);

                CloseableHttpResponse response = httpClient.execute(request);
                String responseString = EntityUtils.toString(response.getEntity());
                EntityUtils.consume(response.getEntity());

                JSONParser parser = new JSONParser();
                JSONArray responseJSON = (JSONArray) parser.parse(responseString);

                return storeFetchedBenchmarkData(benchmarkFingerprint, responseJSON);
            } catch (Exception e) {
                log.error("* Failed to fetch benchmark data for " + name, e);
                return false;
            }
        }
        return true;
    }

    public void close() {
        try {
            httpClient.close();
        } catch (Exception e) {
            log.error("Error closing http client", e);
        }
    }

    private static boolean storeFetchedBenchmarkData(String benchmarkFingerprint, JSONArray benchmarkList) {
        if (!allBenchmarks.containsKey(benchmarkFingerprint)) {
            Map<String, Map<String, List<Double>>> newBenchTable = new HashMap<>();
            allBenchmarks.put(benchmarkFingerprint, newBenchTable);
        }

        Map<String, Map<String, List<Double>>> benchTable = getBenchmarks(benchmarkFingerprint);

        // benchmarkList is in reverse order of timestamp
        for (int i = benchmarkList.size() - 1; i >= 0; i--) {
            JSONObject benchmark = (JSONObject) benchmarkList.get(i);
            JSONObject baseValues = (JSONObject) benchmark.get("baseValues");

            String reportID = (String) baseValues.get("reportId");
            reportIDs.add(reportID);

            String version = (String) baseValues.get("benchVersion");
            Double score = (Double) baseValues.get("totalScore");
            JSONObject benchmarksValues = (JSONObject) benchmark.get("benchmarksValues");
            String mode = (String) benchmarksValues.get("mode");

            if (!storeFetchedBenchmarkHelper(benchmarkFingerprint, benchTable, mode, version, score)) {
                return false;
            }
        }
        return true;
    }

    public static boolean storeFetchedBenchmarkHelper(String fingerprint, 
    		Map<String, Map<String, List<Double>>> benchTable, String mode, String version, Double score) {
        if (benchTable == null) {
            return false;
        }
        if (!benchTable.containsKey(version)) {
            List<Double> testScores = new ArrayList<>();
            HashMap<String, List<Double>> scoresPerMode = new HashMap<>();
            scoresPerMode.put(mode, testScores);
            benchTable.put(version, scoresPerMode);

            if (getLatestVersion(fingerprint) == null || isNewerVersion(version, getLatestVersion(fingerprint))) {
                setLatestVersion(fingerprint, version);
            }
        }
        if (!benchTable.get(version).containsKey(mode)) {
            List<Double> testScores = new ArrayList<>();
            benchTable.get(version).put(mode, testScores);
        }
        List<Double> testsWithinVersion = benchTable.get(version).get(mode);
        testsWithinVersion.add(score);

        return true;
    }
    
    public static void storeRecentBenchmark(String fingerprint, String version, String mode, Double score) {
    	if (!recentBenchmarks.containsKey(fingerprint)) {
        	Map<String, Map<String, Double>> versionsTested = new HashMap<>();
        	recentBenchmarks.put(fingerprint, versionsTested);
        }
    	Map<String, Map<String, Double>> versionsTested = recentBenchmarks.get(fingerprint);
        if (!versionsTested.containsKey(version)) {
        	Map<String, Double> modesTested = new HashMap<>();
        	versionsTested.put(version, modesTested);
        }
        Map<String, Double> modesTested = versionsTested.get(version);
        
        modesTested.put(mode, score);
        
        if (getCurrentVersion(fingerprint) == null || isNewerVersion(version, getCurrentVersion(fingerprint))) {
            setCurrentVersion(fingerprint, version);
        }
    }
    

    public static Map<String, Map<String, Map<String, Double>>> getBenchmarksFromReport(String accessToken, File recentReport) {
        if (StringUtils.isNotEmpty(accessToken) && !accessToken.equals("undefined")) {
            JSONObject benchmarkReport = null;
            try {
                String str = FileUtils.readFileToString(recentReport, "UTF-8");
                JSONParser parser = new JSONParser();
                benchmarkReport = (JSONObject) parser.parse(str);
            } catch (Exception e) {
                log.error("* Failed to read report and grab fingerprint data", e);
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
                    
                    // loop through benchmarks in package (com.x.bench1, com.x.bench2, ...)
                    for (Object packageBenchmark : packageBenchmarks) {
                        JSONObject benchmark = (JSONObject) packageBenchmark;
                        String benchmarkName = (String) benchmark.get("name");
                        String benchmarkVersion = (String) benchmark.get("version");
                        Double benchmarkScore = (Double) benchmark.get("score");
                        String benchmarkMode = (String) benchmark.get("mode");
                        String benchmarkFingerprint = (String) benchmark.get("manualFingerprint");
                        fingerprintsToNames.put(benchmarkFingerprint, benchmarkName);
                        namesToFingerprints.put(benchmarkName, benchmarkFingerprint);
                        
                        // report data handling
                        storeRecentBenchmark(benchmarkFingerprint, benchmarkVersion, benchmarkMode, benchmarkScore);
                        
                        // fetch CyBench data handling
                        if (getInstance().fetchBenchmarks(benchmarkName, benchmarkFingerprint, accessToken)) {
                            // store new data in map if this report hasn't been added already
                            if (reportID != null && !reportIDs.contains(reportID)) {
                                Map<String, Map<String, List<Double>>> benchTable = getBenchmarks(benchmarkFingerprint);
                                storeFetchedBenchmarkHelper(benchmarkFingerprint, benchTable, benchmarkMode, benchmarkVersion, benchmarkScore);
                            }
                        } else {
                        	log.error("* Fetching and storing benchmark data for analysis failed *");
                        	return null;
                        }
                    }
                }
            }
            if (recentBenchmarks.isEmpty()) {
                log.warn("No benchmarks found in passed report");
            }
        } else {
            log.warn("No access token provided!");
        }
        getInstance().close();

        return recentBenchmarks;
    }

    public static boolean isNewerVersion(String newVersion, String currentVersion) {
        if (currentVersion.equals(newVersion)) {
            return false;
        }

        List<String> newVersionDotSplit = Arrays.asList(newVersion.split("\\."));
        List<String> currentVersionDotSplit = Arrays.asList(currentVersion.split("\\."));
        int currentVersionDotSize = currentVersionDotSplit.size();

        for (int i = 0; i < newVersionDotSplit.size(); i++) {
            if (currentVersionDotSize == i) {
                // newVersion has an additional dot and is therefore newer after all previous subversions have been
                // deemed equivalent
                return true;
            } else {
                int comparedValue = newVersionDotSplit.get(i).compareTo(currentVersionDotSplit.get(i));
                // if this is positive, the subversion is newer at that dot
                // if this is negative, the subversion is older at that dot
                // if this is 0, the subversion is the same at that dot
                if (comparedValue > 0) {
                    return true;
                } else if (comparedValue < 0) {
                    return false;
                }
            }
        }
        return false;
    }
}
