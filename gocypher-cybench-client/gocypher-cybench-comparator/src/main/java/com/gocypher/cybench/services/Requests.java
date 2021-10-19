package com.gocypher.cybench.services;

import java.util.*;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Requests {
    private static final Logger log = LoggerFactory.getLogger(Requests.class);

    private static final String benchmarkViewBenchmarksServiceUrl = "https://www.gocypher.com/gocypher-benchmarks-services/services/v1/benchmarks/benchmark/view/";
    private static final String authFinalizeLoginServiceURL = "https://www.gocypher.com/gocypher-benchmarks-services/services/v1/auth/finalizeSignIn/token";
    private static final String localBenchmarkViewBenchmarksServiceUrl = "http://localhost:8080/gocypher-benchmarks-services-1.0-SNAPSHOT/services/v1/benchmarks/benchmark/view/";
    private static final String localAuthFinalizeLoginServiceURL = "http://localhost:8080/gocypher-benchmarks-services-1.0-SNAPSHOT/services/v1/auth/finalizeSignIn/token";

    private static Requests instance;
    private String tagFingerprint = "benchmarkTag";

    // <Benchmark Fingerprint : <Version : <Mode : <List of Scores in Test Order>>>>
    public static Map<String, Map<String, Map<String, List<Double>>>> allBenchmarkTables;
    // keep track of reports to prevent duplicates
    public static Set<String> reportIDs;

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private Requests() {
    }

    public static Requests getInstance() {
        if (instance == null) {
            instance = new Requests();
            allBenchmarkTables = new HashMap<>();
            reportIDs = new HashSet<>();
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

    public boolean fetchBenchmarks(String name, String benchmarkFingerprint, String sessionToken,
            String benchmarkWorkspace) {
        if (!allBenchmarkTables.containsKey(benchmarkFingerprint)) {
            String serviceUrl = localBenchmarkViewBenchmarksServiceUrl + benchmarkFingerprint + "?typeOfFingerprint="
                    + tagFingerprint;
            log.info("* Fetching benchmark data for {}", name);
            try {
                URIBuilder uri = new URIBuilder(serviceUrl);
                uri.setParameter("typeOfFingerprint", tagFingerprint);

                HttpGet request = new HttpGet(uri.build());
                request.setHeader("x-benchmark-workspace", benchmarkWorkspace);
                request.setHeader("Authorization", sessionToken);

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
        return true;
    }

    public String getSessionToken(String personalAccessToken) {
        if (personalAccessToken != null && !personalAccessToken.isEmpty()) {
            log.info("Authorizing GitHub Token...");
            try {
                URIBuilder uri = new URIBuilder(localAuthFinalizeLoginServiceURL);
                HttpPost request = new HttpPost(uri.build());
                List<NameValuePair> nvps = new ArrayList<>();
                nvps.add(new BasicNameValuePair("personalToken", personalAccessToken));
                request.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

                CloseableHttpResponse response = httpClient.execute(request);
                String responseString = EntityUtils.toString(response.getEntity());
                EntityUtils.consume(response.getEntity());

                JSONParser parser = new JSONParser();
                JSONObject responseJSON = (JSONObject) parser.parse(responseString);
                if (responseJSON.containsKey("error")) {
                    log.error("{}, the provided token may be incorrect...", responseJSON.get("error"));
                    return null;
                }
                String sessionToken = (String) responseJSON.get("token");
                return sessionToken;
            } catch (Exception e) {
                log.error("Failed to get CyBench session token", e);
                return null;
            }
        }
        return "";
    }

    public void close() {
        try {
            httpClient.close();
        } catch (Exception e) {
            log.error("Error closing http client", e);
        }
    }
}
