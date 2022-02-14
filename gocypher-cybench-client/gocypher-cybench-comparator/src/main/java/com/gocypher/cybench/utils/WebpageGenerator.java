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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gocypher.cybench.CompareBenchmarks;
import com.gocypher.cybench.model.ComparedBenchmark;
import com.gocypher.cybench.model.ComparedBenchmark.CompareState;
import com.gocypher.cybench.model.ComparisonConfig;
import com.gocypher.cybench.model.ComparisonConfig.Type;
import com.gocypher.cybench.services.Requests;

public class WebpageGenerator {
    private static final Logger log = LoggerFactory.getLogger(WebpageGenerator.class);
    private static final Charset utf8 = StandardCharsets.UTF_8;

    public static void generatePage() {
        String runTime = getDateTimeForFileName();
        String projectName = Requests.project.replace("/", " ");
        File templateDir = new File("htmlReports/tmp/");
        String fileName = "htmlReports/" + projectName + " v" + Requests.currentVersion + " - " + runTime + ".html";
        File htmlFile = new File(fileName);

        try {
            InputStream in = WebpageGenerator.class.getResourceAsStream("/template.html");
            File template = new File("htmlReports/tmp/tempHTML.tmp");
            String result = IOUtils.toString(in, utf8);
            FileUtils.writeStringToFile(template, result, utf8);
            in.close();

            String htmlStr = FileUtils.readFileToString(template, utf8);
            htmlStr = htmlStr.replace("$" + "runTime", runTime);
            htmlStr = htmlStr.replace("$" + "total", String.valueOf(CompareBenchmarks.totalComparedBenchmarks));
            htmlStr = htmlStr.replace("$" + "passed", String.valueOf(CompareBenchmarks.totalPassedBenchmarks));
            htmlStr = htmlStr.replace("$" + "failed", String.valueOf(CompareBenchmarks.totalFailedBenchmarks));
            htmlStr = htmlStr.replace("$" + "skipped", String.valueOf(CompareBenchmarks.totalSkippedBenchmarks));
            htmlStr = htmlStr.replace("$" + "projectName", Requests.project);
            htmlStr = htmlStr.replace("$" + "currentVersion", Requests.currentVersion);
            htmlStr = htmlStr.replace("$" + "latestVersion", Requests.latestVersion);
            if (!StringUtils.isBlank(Requests.previousVersion)) {
                htmlStr = htmlStr.replace("$" + "previousVersion", Requests.previousVersion);
            } else {
                htmlStr = htmlStr.replace("$" + "previousVersion", "N/A");
            }
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
            FileUtils.deleteDirectory(templateDir);

            log.info("* Generated HTML report can be found at {}", htmlFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("* Error generating HTML report!", e);
            try {
                FileUtils.delete(htmlFile);
                FileUtils.deleteDirectory(templateDir);
            } catch (Exception deleteFilesException) {
            }
        }
    }

    private static void fillBenchmarkTable(File htmlFile, List<ComparedBenchmark> benchmarks,
            CompareState benchmarkState) throws Exception {
        try {
            String table = "";
            String captionClass = "";
            String caption = "";
            boolean skippedBenchmarks = false;
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
                skippedBenchmarks = true;
                break;
            }
            }

            FileUtils.write(htmlFile, "<table id=\""+ table + "\" class=\"display compact " + table + "\">"
                    + "<caption class=\"" + captionClass + "\">" + caption + "</caption><thead><tr>", utf8, true);
            if (!skippedBenchmarks) {
                FileUtils.write(htmlFile, "<th>Test Type</th>", utf8, true);
            }
            FileUtils.write(htmlFile, "<th>Name</th>" 
                    + "<th>Mode</th>"
                    + "<th>Score</th>", utf8, true);
            if (!skippedBenchmarks) {
                FileUtils.write(htmlFile, "<th>Compared Against Version</th>"
                    + "<th>Mean</th>"
                    + "<th>SD</th>"
                    + "<th>Delta</th>"
                    + "<th>Percent Change</th>"
                    + "<th>Deviations From Mean</th>", utf8, true);
            }
            FileUtils.write(htmlFile, "</tr></thead><tbody>", utf8, true);

            for (ComparedBenchmark benchmark : benchmarks) {
                ComparisonConfig comparisonConfig = benchmark.getComparisonConfig();
                FileUtils.writeStringToFile(htmlFile, "<tr>", utf8, true);
                if (!skippedBenchmarks) {
                    FileUtils.writeStringToFile(htmlFile, "<td>" + getTestType(comparisonConfig) + "</td>", utf8, true);
                }
                FileUtils.writeStringToFile(htmlFile, "<td style='text-align: left;'>" + benchmark.getDisplayName() + "</td>"
                        + "<td>" + benchmark.getMode() + "</td>"
                        + "<td>" + benchmark.getRoundedScore() + "</td>", utf8, true);
                if (!skippedBenchmarks) {
                    FileUtils.writeStringToFile(htmlFile, "<td>" + comparisonConfig.getCompareVersion() + "</td>"
                            + "<td>" + benchmark.getRoundedCompareMean() + "</td>"
                            + "<td>" + benchmark.getRoundedCompareSD() + "</td>"
                            + "<td>" + benchmark.getRoundedDelta() + "</td>"
                            + "<td>" + benchmark.getRoundedPercentChange() + "%</td>"
                            + "<td>" + benchmark.getRoundedDeviationsFromMean() + "</td>", utf8, true);
                }
                FileUtils.writeStringToFile(htmlFile, "</tr>", utf8, true);
            }
            FileUtils.writeStringToFile(htmlFile, "</tbody></table><br>", utf8, true);
        } catch (Exception e) {
            log.error("Error filling HTML table with benchmark data!");
            throw e;
        }
    }

    private static String getTestType(ComparisonConfig comparisonConfig) {
        if (comparisonConfig.getTestType() == Type.SD) {
            return comparisonConfig.getDeviationsAllowed() + " deviations allowed";
        } else if (comparisonConfig.getTestType() == Type.DELTA) {
            return "Delta Test";
        } else {
            return comparisonConfig.getPercentChangeAllowed() + "% change allowed";
        }
    }

    private static void generateStylingFile() throws Exception {
        InputStream stream = WebpageGenerator.class.getResourceAsStream("/styles.css");
        File styleFile = new File("htmlReports/styles/styles.css");
        try {
            String styles = IOUtils.toString(stream, utf8);
            FileUtils.writeStringToFile(styleFile, styles, utf8);
            stream.close();
        } catch (Exception e) {
            log.error("Failed to generate HTML styling");
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