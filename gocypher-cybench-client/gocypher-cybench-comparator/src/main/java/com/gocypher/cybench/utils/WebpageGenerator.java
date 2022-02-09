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

package com.gocypher.cybench.utils;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gocypher.cybench.CompareBenchmarks;
import com.gocypher.cybench.model.ComparedBenchmark;
import com.gocypher.cybench.model.ComparisonConfig;
import com.gocypher.cybench.model.ComparedBenchmark.CompareState;
import com.gocypher.cybench.model.ComparisonConfig.Method;
import com.gocypher.cybench.model.ComparisonConfig.Threshold;
import com.gocypher.cybench.services.Requests;

public class WebpageGenerator {
    private static final Logger log = LoggerFactory.getLogger(WebpageGenerator.class);
    private static Charset utf8 = StandardCharsets.UTF_8;

    public static void generatePage() {
        try {
            InputStream in = WebpageGenerator.class.getResourceAsStream("/template.html");
            File template = new File("src/main/tmp/tempHTML.tmp");
            String result = IOUtils.toString(in, utf8);
            FileUtils.writeStringToFile(template, result, utf8);
            in.close();

            String htmlStr = FileUtils.readFileToString(template, utf8);
            htmlStr = htmlStr.replace("$" + "total", CompareBenchmarks.totalComparedBenchmarks + "");
            htmlStr = htmlStr.replace("$" + "passed", CompareBenchmarks.totalPassedBenchmarks + "");
            htmlStr = htmlStr.replace("$" + "failed", CompareBenchmarks.totalFailedBenchmarks + "");
            htmlStr = htmlStr.replace("$" + "skipped", CompareBenchmarks.totalSkippedBenchmarks + "");
            htmlStr = htmlStr.replace("$" + "projectName", Requests.project);
            htmlStr = htmlStr.replace("$" + "currentVersion", Requests.currentVersion);
            htmlStr = htmlStr.replace("$" + "latestVersion", Requests.latestVersion);
            if (!StringUtils.isBlank(Requests.previousVersion)) {
                htmlStr = htmlStr.replace("$" + "previousVersion", Requests.previousVersion);
            } else {
                htmlStr = htmlStr.replace("$" + "previousVersion", "N/A");
            }

    
            String runTime = getDateTimeForFileName();
            htmlStr = htmlStr.replace("$" + "runTime", runTime);
            File htmlFile = new File(
                    "htmlReports/" + Requests.project + " v" + Requests.currentVersion + " - " + runTime + ".html");
            FileUtils.writeStringToFile(htmlFile, htmlStr, utf8);
    
    
            if (CompareBenchmarks.totalPassedBenchmarks != 0) {
                fillBenchmarkTable(htmlFile, CompareBenchmarks.passedBenchmarks, CompareState.PASS);
            }
            if (CompareBenchmarks.totalFailedBenchmarks != 0) {
                fillBenchmarkTable(htmlFile, CompareBenchmarks.failedBenchmarks, CompareState.FAIL);
            }
            if (CompareBenchmarks.totalSkippedBenchmarks != 0) {
                fillBenchmarkTable(htmlFile, CompareBenchmarks.skippedBenchmarks, CompareState.SKIP);
            }
            FileUtils.write(htmlFile, "</body>\n</html>", utf8, true);
    
            generateStylingFile();
            deleteTempFiles();

            // log.info("* Finished creating HTML report.");
            log.info("* Generated HTML report can be found at {}", htmlFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("* Error generating HTML report!", e);
        }
    }

    private static void fillBenchmarkTable(File htmlFile, List<ComparedBenchmark> benchmarks, CompareState benchmarkState) throws Exception{
        try {
            String table = "";
            String captionClass = "";
            String caption = "";
            switch (benchmarkState) {
                case PASS: {
                    table = "passedTable";
                    captionClass = "passedCaption";
                    caption = "Passed Benchmarks";
                    break;
                }
                case FAIL: {
                    table = "anomalyTable";
                    captionClass = "anomalyCaption";
                    caption = "Anomaly Benchmarks";
                    break;
                }
                case SKIP: {
                    table = "skippedTable";
                    captionClass = "skippedCaption";
                    caption = "Skipped Benchmarks";
                    break;
                }
            }

            FileUtils.write(htmlFile, "<table id=\" \"" + table + "class=\"display compact " + table + "\">"
                    + "<caption class=\"" + captionClass + "\">" + caption + "</caption><thead><tr>"
                    + "<th>Test Type</th>"
                    + "<th>Name</th>"
                    + "<th>Mode</th>"
                    + "<th>Score</th>"
                    + "<th>Compared Against Version</th>"
                    + "<th>Mean</th>"
                    + "<th>SD</th>"
                    + "<th>Delta</th>"
                    + "<th>Percent Change</th>"
                    + "<th>Deviations From Mean</th>"
                    + "</tr></thead>", utf8, true);

            for (ComparedBenchmark benchmark : benchmarks) {
                ComparisonConfig comparisonConfig = benchmark.getComparisonConfig();
                String testType = getTestType(comparisonConfig);
                FileUtils.writeStringToFile(htmlFile, "<tbody><tr>"
                        + "<td>" + testType + "</td>"
                        + "<td style='text-align:left'>" + benchmark.getDisplayName() + "</td>"
                        + "<td>" + benchmark.getMode() + "</td>"
                        + "<td>" + BigDecimal.valueOf(benchmark.getScore()) + "</td>"
                        + "<td>" + comparisonConfig.getCompareVersion() + "</td>"
                        + "<td>" + benchmark.getCompareMean() + "</td>"
                        + "<td>" + benchmark.getCompareSD() + "</td>"
                        + "<td>" + benchmark.getDelta() + "</td>"
                        + "<td>" + benchmark.getPercentChange() + "</td>"
                        + "<td>" + benchmark.getDeviationsFromMean() + "</td>"
                        + "</tr></tbody></table><br>", utf8, true);
            }
        } catch (Exception e) {
            log.error("Error filling HTML table with benchmark data!");
            throw e;
        }
    }

    private static String getTestType(ComparisonConfig comparisonConfig) {
        if (comparisonConfig.getMethod() == Method.SD) {
            return "SD Test: " + comparisonConfig.getDeviationsAllowed() + " deviations allowed";
        } else if (comparisonConfig.getThreshold() == Threshold.GREATER) {
            return "Delta Test";
        } else {
            return "% Change Test: " + comparisonConfig.getPercentChangeAllowed() + "% change allowed";
        }
    }

    
    private static void generateStylingFile() throws Exception{
        InputStream styleIn = WebpageGenerator.class.getResourceAsStream("/styles.css");
        File tempStyleFile = new File("htmlReports/styles/styles.css");
        String resultStyle;
        try {
            resultStyle = IOUtils.toString(styleIn, utf8);
            FileUtils.writeStringToFile(tempStyleFile, resultStyle, utf8);
            styleIn.close();
        } catch (Exception e) {
            log.error("Failed to generate styling for HTML report.");
            throw e;
        }

    }

    private static void deleteTempFiles() throws Exception {
        File tempDir = new File("src/main/tmp/");
        try {
            FileUtils.deleteDirectory(tempDir);
            // log.info("* Temporary files deleted.");
        } catch (Exception e) {
            log.error("Error while deleting temporary files directory.");
            throw e;
        }
    }

    private static String getDateTimeForFileName() {
        DateFormat df = new SimpleDateFormat("EEE, MMM d, yyyy - hh.mm.ss a z");
        Date date = Date.from(CompareBenchmarks.comparisonRunTime);
        String fileNameDateTime = df.format(date);
        return fileNameDateTime;
    }
}