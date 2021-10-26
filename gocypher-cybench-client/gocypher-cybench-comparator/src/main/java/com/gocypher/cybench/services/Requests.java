package com.gocypher.cybench.services;

import java.io.File;
import java.util.*;

import org.apache.commons.io.FileUtils;
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

public class Requests {
    private static final Logger log = LoggerFactory.getLogger(Requests.class);

    private static final String benchmarkViewBenchmarksServiceUrl = "https://www.gocypher.com/gocypher-benchmarks-reports/services/v1/reports/benchmark/view/";
    private static final String localBenchmarkViewBenchmarksServiceUrl = "http://localhost:8080/gocypher-benchmarks-reports-1.0-SNAPSHOT/services/v1/reports/benchmark/view/";

    private static Requests instance;
    private String tagFingerprint = "benchmarkTag";

    // <Benchmark Fingerprint : <Version : <Mode : <List of Scores in Test Order>>>>
    public static Map<String, Map<String, Map<String, List<Double>>>> allBenchmarkTables;
    // keep track of reports preventing duplicates
    public static Set<String> reportIDs;
    
    private static String currentVersion;
    private static String previousVersion;

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private Requests() {
    }

    public static Requests getInstance() {
        if (instance == null) {
            instance = new Requests();
            allBenchmarkTables = new HashMap<>();
            reportIDs = new HashSet<>();
            currentVersion = null;
            previousVersion = null;
        }
        return instance;
    }

    public static Map<String, Map<String, Map<String, List<Double>>>> getBenchmarks() {
        return allBenchmarkTables;
    }

    public static Map<String, Map<String, List<Double>>> getBenchmarks(String benchmarkFingerprint) {
        return allBenchmarkTables.get(benchmarkFingerprint);
    }

    public static Map<String, List<Double>> getBenchmarks(String benchmarkFingerprint, String version) {
        return getBenchmarks(benchmarkFingerprint).get(version);
    }

    public static List<Double> getBenchmarks(String benchmarkFingerprint, String version, String mode) {
        return getBenchmarks(benchmarkFingerprint, version).get(mode);
    }

    public static Set<String> getReports() {
        return reportIDs;
    }

    public boolean fetchBenchmarks(String name, String benchmarkFingerprint, String accessToken) {
        if (!allBenchmarkTables.containsKey(benchmarkFingerprint)) {
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
        if (!allBenchmarkTables.containsKey(benchmarkFingerprint)) {
            Map<String, Map<String, List<Double>>> newBenchTable = new HashMap<>();
            allBenchmarkTables.put(benchmarkFingerprint, newBenchTable);
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

            if (!storeBenchmarkData(benchTable, mode, version, score)) {
                return false;
            }
        }
        return true;
    }

    public static boolean storeBenchmarkData(Map<String, Map<String, List<Double>>> benchTable, String mode,
            String version, Double score) {
        if (benchTable == null) {
            return false;
        }
        if (!benchTable.containsKey(version)) {
            List<Double> testScores = new ArrayList<>();
            HashMap<String, List<Double>> scoresPerMode = new HashMap<>();
            scoresPerMode.put(mode, testScores);
            benchTable.put(version, scoresPerMode);
        }
        if (!benchTable.get(version).containsKey(mode)) {
            List<Double> testScores = new ArrayList<>();
            benchTable.get(version).put(mode, testScores);
        }
        List<Double> testsWithinVersion = benchTable.get(version).get(mode);
        testsWithinVersion.add(score);
        
        if (currentVersion == null) {
        	currentVersion = version;
        	previousVersion = version;
        } else if (isNewerVersion(version, currentVersion)) {
        	previousVersion = currentVersion;
        	currentVersion = version;
        }else if (isNewerVersion(version, previousVersion) && isNewerVersion(currentVersion, version)) {
        	previousVersion = version;
        }
        
        return true;
    }

    public static Map<String, String> getFingerprintsFromReport(String reportPath) {
        Map<String, String> fingerprints = new HashMap<>();
        JSONObject benchmarkReport = null;

        try {
            File report = new File(reportPath);
            String str = FileUtils.readFileToString(report, "UTF-8");
            JSONParser parser = new JSONParser();
            benchmarkReport = (JSONObject) parser.parse(str);
        } catch (Exception e) {
            log.error("* Failed to read report and grab fingerprint data", e);
        }

        if (benchmarkReport != null) {
            JSONObject packages = (JSONObject) benchmarkReport.get("benchmarks");
            for (Object pckg : packages.values()) {
                JSONArray packageBenchmarks = (JSONArray) pckg;
                for (Object packageBenchmark : packageBenchmarks) {
                    JSONObject benchmark = (JSONObject) packageBenchmark;
                    String benchmarkName = (String) benchmark.get("name");
                    String benchmarkFingerprint = (String) benchmark.get("manualFingerprint");
                    fingerprints.put(benchmarkFingerprint, benchmarkName);
                }
            }
        }

        if (fingerprints.isEmpty()) {
            log.info("No fingerprints found in passed report");
        }

        return fingerprints;
    }
    
    public static boolean isNewerVersion(String newVersion, String currentVersion) {
    	if (currentVersion.equals(newVersion))
    		return false;
    	
    	List<String> newVersionDotSplit = Arrays.asList(newVersion.split("."));
    	List<String> currentVersionDotSplit = Arrays.asList(currentVersion.split("."));
    	int currentVersionDotSize = currentVersionDotSplit.size();
    	
    	for (int i = 0; i < newVersionDotSplit.size(); i++) {
    		if (currentVersionDotSize == i) {
    			// newVersion has an additional dot and is therefore newer after all previous subversions have been deemed equivalent
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
    
    public static String getCurrentVersion() {
    	return currentVersion;
    }
    
    public static String getPreviousVersion() {
    	return previousVersion;
    }
}
