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

package com.gocypher.cybench.services;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

import com.gocypher.cybench.model.ComparedBenchmark;

public class Requests {
    private static final Logger log = LoggerFactory.getLogger(Requests.class);
    private static final String benchmarkBaseUrl = System.getProperty("cybench.benchmark.base.url",
            "https://app.cybench.io/cybench/benchmark/");

    private static final String prodHost = "https://app.cybench.io/";
    private static final String localHost = "http://localhost:8080/";
    private static final String projectSummaryUrl = prodHost + "gocypher-benchmarks-services/services/v1/projects/";
    private static final String benchmarksByReportIDUrl = prodHost
            + "gocypher-benchmarks-services/services/v1/benchmarks/report/compare/";

    public static String project = null;
    public static String currentVersion = null;
    public static String latestVersion = null;
    public static String previousVersion = null;

    // recent report identifiers
    public static String recentReportID = null;
    public static Date recentReportDateTime = null;

    public static ArrayList<String> allProjectVersions = new ArrayList<>();
    // version: list of reports
    public static Map<String, ArrayList<Map<String, Object>>> reportSummaries = new HashMap<>();
    // <ReportID: <Benchmark Fingerprint : <Mode : List<ComparedBenchmark>>>>>
    public static final Map<String, Map<String, Map<String, List<ComparedBenchmark>>>> fetchedBenchmarks = new HashMap<>();

    private static Requests instance;
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private Requests() {
    }

    public static synchronized Requests getInstance() {
        if (instance == null) {
            instance = new Requests();
        }
        return instance;
    }

    public void close() {
        try {
            httpClient.close();
        } catch (Exception e) {
            log.error("Error closing http client", e);
        }
    }

    // returns <Benchmark Fingerprint : <Mode : <ComparedBenchmark>>>
    public static Map<String, Map<String, ComparedBenchmark>> parseRecentReport(File recentReport) {
        Map<String, Map<String, ComparedBenchmark>> result = new HashMap<>();
        JSONObject report = null;
        try {
            String str = FileUtils.readFileToString(recentReport, "UTF-8");
            JSONParser parser = new JSONParser();
            report = (JSONObject) parser.parse(str);
        } catch (Exception e) {
            log.error("* Failed to read report", e);
        }

        if (report != null) {
            String reportID = null;
            String reportURL = (String) report.get("reportURL");
            if (reportURL != null) {
                String[] parsedURL = reportURL.split(benchmarkBaseUrl);
                reportID = parsedURL[1].split("/")[0];
            }
            recentReportID = reportID;
            Number timestampNum = (Number) report.get("timestamp");
            recentReportDateTime = new Date(timestampNum.longValue());
            project = (String) report.get("project");
            currentVersion = (String) report.get("projectVersion");
            JSONObject categories = (JSONObject) report.get("benchmarks");

            // loop through categories
            for (Object category : categories.values()) {
                JSONArray categoryBenchmarks = (JSONArray) category;

                // loop through benchmarks in category
                for (Object benchItem : categoryBenchmarks) {
                    JSONObject benchmark = (JSONObject) benchItem;
                    String name = (String) benchmark.get("name");
                    Double score = (Double) benchmark.get("score");
                    String mode = (String) benchmark.get("mode");
                    String fingerprint = (String) benchmark.get("generatedFingerprint");

                    ComparedBenchmark comparedBenchmark = new ComparedBenchmark();
                    comparedBenchmark.setDisplayName(name);
                    comparedBenchmark.setMode(mode);
                    comparedBenchmark.setScore(score);
                    comparedBenchmark.setFingerprint(fingerprint);

                    Map<String, ComparedBenchmark> comparedBenchmarkMap = result.containsKey(fingerprint)
                            ? result.get(fingerprint) : new HashMap<>();
                    comparedBenchmarkMap.put(mode, comparedBenchmark);
                    result.put(fingerprint, comparedBenchmarkMap);
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public boolean getProjectSummary(String projectName, String accessToken) {
        try {
            String serviceUrl = projectSummaryUrl + URLEncoder.encode(projectName, StandardCharsets.UTF_8.toString())
                    + "/providedAccessToken";
            if (serviceUrl != null && projectName != null) {
                log.info("* Fetching project data for {}", projectName);
                JSONObject projectSummary = executeRequest(serviceUrl, accessToken);
                if (projectSummary.isEmpty()) {
                    throw new Exception("No project data found!");
                }

                latestVersion = (String) projectSummary.get("latestVersion");
                previousVersion = (String) projectSummary.get("previousVersion");
                allProjectVersions = (ArrayList<String>) projectSummary.get("versions");
                reportSummaries = (Map<String, ArrayList<Map<String, Object>>>) projectSummary.get("reports");
            }
        } catch (Exception e) {
            log.error("* Failed to fetch project data for " + projectName, e);
            return false;
        }
        return true;
    }

    private JSONObject executeRequest(String serviceUrl, String accessToken) throws Exception {
        URIBuilder uri = new URIBuilder(serviceUrl);

        HttpGet request = new HttpGet(uri.build());
        request.setHeader("x-api-key", accessToken);

        CloseableHttpResponse response = httpClient.execute(request);
        String responseString = EntityUtils.toString(response.getEntity());
        EntityUtils.consume(response.getEntity());

        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(responseString);
    }

    @SuppressWarnings("unchecked")
    public boolean getBenchmarksInReport(String reportID, String accessToken) {
        String serviceUrl = benchmarksByReportIDUrl + reportID + "/providedAccessToken";
        if (serviceUrl != null && reportID != null) {
            try {
                log.info("* Fetching benchmark data for report {}", reportID);
                JSONObject results = executeRequest(serviceUrl, accessToken);
                Map<String, Map<String, List<Map<String, Object>>>> benchmarkObj = (Map<String, Map<String, List<Map<String, Object>>>>) results
                        .get("benchmarks");
                Map<String, Map<String, List<ComparedBenchmark>>> benchmarks = new HashMap<>();
                if (benchmarkObj.isEmpty()) {
                    throw new Exception("No report data found!");
                }

                for (Map.Entry<String, Map<String, List<Map<String, Object>>>> benchmarkObjEntry : benchmarkObj
                        .entrySet()) {
                    String fingerprint = benchmarkObjEntry.getKey();
                    Map<String, List<Map<String, Object>>> fingerprintMap = benchmarkObjEntry.getValue();
                    Map<String, List<ComparedBenchmark>> benchmarksInMode = new HashMap<>();
                    for (Map.Entry<String, List<Map<String, Object>>> modeMap : fingerprintMap.entrySet()) {
                        String mode = modeMap.getKey();
                        List<Map<String, Object>> comparedBenchmarkObjects = modeMap.getValue();
                        List<ComparedBenchmark> comparedBenchmarks = new ArrayList<>();
                        for (Map<String, Object> comparedBenchmarkObj : comparedBenchmarkObjects) {
                            ComparedBenchmark comparedBenchmark = new ComparedBenchmark();
                            comparedBenchmark.setDatasetID((String) comparedBenchmarkObj.get("datasetID"));
                            comparedBenchmark.setBenchProperties(
                                    (Map<String, Object>) comparedBenchmarkObj.get("benchProperties"));
                            comparedBenchmark.setDisplayName((String) comparedBenchmarkObj.get("displayName"));
                            comparedBenchmark.setFingerprint((String) comparedBenchmarkObj.get("fingerprint"));
                            comparedBenchmark.setMode((String) comparedBenchmarkObj.get("mode"));
                            Number score = (Number) comparedBenchmarkObj.get("score");
                            if (score != null) {
                                comparedBenchmark.setScore(score.doubleValue());
                            }

                            comparedBenchmarks.add(comparedBenchmark);
                        }
                        benchmarksInMode.put(mode, comparedBenchmarks);
                    }
                    benchmarks.put(fingerprint, benchmarksInMode);
                }
                fetchedBenchmarks.put(reportID, benchmarks);
            } catch (Exception e) {
                log.error("* Failed to fetch benchmark data for report " + reportID, e);
                return false;
            }
        }
        return true;
    }
}
