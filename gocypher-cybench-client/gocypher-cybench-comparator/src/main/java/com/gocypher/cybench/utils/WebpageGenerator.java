/*
 * Copyright (C) 2020-2021, K2N.IO.
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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gocypher.cybench.CompareBenchmarks;
import com.gocypher.cybench.services.Requests;

public class WebpageGenerator {
	private static final Logger log = LoggerFactory.getLogger(WebpageGenerator.class);

	String passed = String.valueOf(CompareBenchmarks.totalPassedBenchmarks);
	String failed = String.valueOf(CompareBenchmarks.totalFailedBenchmarks);
	String total = String.valueOf(CompareBenchmarks.totalComparedBenchmarks);
	String skipped = String.valueOf(CompareBenchmarks.totalSkippedBenchmarks);

	static String version = "";
	static String name = "";
	static String range = "";
	static String scope = "";
	static String method = "";
	static String compareVersion = "";
	static String threshold = "";
	static String percentChangeAllowed = "";
	static String deviationsAllowed = "";
	static String configType = "";
	static String dateTime = "";
	static String scriptName = "";
	static int packNum = 0;
	static Charset utf8 = StandardCharsets.UTF_8;
	static Map<String, Object> allConfigs;
	static ArrayList<String> packageNames;
	static WebpageGenerator gen = new WebpageGenerator();
	static List<String> skippedFields = Arrays.asList("utf8", "allConfigs", "skippedFields", "gen");

	public WebpageGenerator() {
		// TODO Remove Logs/Prints |
		// Add support/HTML generation for multiple packaged in comparator.yaml
	}

	// for comparator configuration
	public static void generatePage() throws IOException, URISyntaxException {
		WebpageGenerator gen = new WebpageGenerator();
		configType = "comparator.yaml";
		packNum = packageNames.size();
		File tempfile = genTemplateHTML();
		File newHtml = new File("htmlReports/" + packageNames.get(0) + "-" + getDateTimeForFileName() + ".html");
		String htmlTemp;

		htmlTemp = FileUtils.readFileToString(tempfile, utf8);

		// debug
		if (allConfigs == null) {
			log.error("* Unable to grab configurations from comparator.yaml");
		} else {
			changeComparatorConfigs(allConfigs);
			changeVersion();
			changeDateTime();
		}

		Class<?> clazz = gen.getClass();
		for (Field field : clazz.getDeclaredFields()) {

			try {
				if (!skippedFields.contains(field.getName())) {
					htmlTemp = htmlTemp.replace("$" + field.getName(), field.get(gen).toString());
					FileUtils.writeStringToFile(newHtml, htmlTemp, utf8);
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		createPassedTable(newHtml);
		createFailedTable(newHtml);
		createSkippedTable(newHtml);

		FileUtils.write(newHtml, "</body>\n</html>", utf8, true);
		genCSS();
		deleteTempFiles();
		log.info("* Finished creating HTML report.");
		log.info("* Generated HTML report can be found at {}", newHtml.getAbsolutePath());
	}

	// for script configuration || props gets passed all the way from
	// ComparatorScriptEngine
	public static void generatePage(Map<String, Object> props) throws IOException {
		WebpageGenerator gen = new WebpageGenerator();
		configType = scriptName;
		packNum = packageNames.size();
		File tempfile = genTemplateHTML();
		File newHtml = new File("htmlReports/" + packageNames.get(0) + "-" + getDateTimeForFileName() + ".html");
		String htmlTemp;

		htmlTemp = FileUtils.readFileToString(tempfile, utf8);

		changeScriptConfigs(props);
		changeVersion();
		changeDateTime();

		Class<?> clazz = gen.getClass();
		for (Field field : clazz.getDeclaredFields()) {
			try {
				if (!skippedFields.contains(field.getName())) {
					htmlTemp = htmlTemp.replace("$" + field.getName(), field.get(gen).toString());
					FileUtils.writeStringToFile(newHtml, htmlTemp, utf8);
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		createPassedTable(newHtml);
		createFailedTable(newHtml);
		createSkippedTable(newHtml);
		FileUtils.write(newHtml, "</body>\n</html>", utf8, true);
		genCSS();
		deleteTempFiles();
		log.info("* Finished creating HTML report.");
		log.info("* Generated HTML report can be found at {}", newHtml.getAbsolutePath());
	}

	private static void createPassedTable(File file) throws IOException {

		if (CompareBenchmarks.totalPassedBenchmarks == 0) {
			FileUtils.writeStringToFile(file,
					"<tr><td><td><td><td><td style=\"text-align:center\"> No tests passed.</td></td></td></td></td></tr>\n",
					utf8, true);
		} else {

			for (Map.Entry<String, Map<String, Map<String, Map<String, Object>>>> benchmark : CompareBenchmarks.passedBenchmarks
					.entrySet()) {
				String benchmarkName = benchmark.getKey();
				String fingerprint = Requests.namesToFingerprints.get(benchmarkName);

				Map<String, Map<String, Map<String, Object>>> benchVersions = benchmark.getValue();
				for (Map.Entry<String, Map<String, Map<String, Object>>> versionEntry : benchVersions.entrySet()) {
					String benchVersion = versionEntry.getKey();
					Map<String, Map<String, Object>> benchData = versionEntry.getValue();
					for (Map.Entry<String, Map<String, Object>> dataEntry : benchData.entrySet()) {
						String benchMode = dataEntry.getKey();
						Map<String, Object> benchmarkData = dataEntry.getValue();
						Double score = (Double) benchmarkData.get(ConfigHandling.BENCHMARK_SCORE);
						BigDecimal roundScore = BigDecimal.valueOf(score);
						Double compareValue = (Double) benchmarkData.get(Comparisons.CALCULATED_COMPARE_VALUE);
						BigDecimal roundCompValue = BigDecimal.valueOf(compareValue);
						Double delta = (Double) benchmarkData.get(Comparisons.CALCULATED_DELTA);
						Double percentChange = (Double) benchmarkData.get(Comparisons.CALCULATED_PERCENT_CHANGE);
						BigDecimal roundPercentChange = BigDecimal.valueOf(percentChange);
						Double sdFromMean = (Double) benchmarkData.get(Comparisons.CALCULATED_SD_FROM_MEAN);
						String compareVersion = (String) benchmarkData.get(ConfigHandling.COMPARE_VERSION);
						FileUtils.writeStringToFile(file, "<th>" + fingerprint //
								+ "</th><th style='text-align:left'>" + benchmarkName //
								+ "</th><th>" + benchVersion //
								+ "</th><th>" + compareVersion + "</th><th>" + benchMode //
								+ "</th><th style='text-align:right'>" + roundScore //
								+ "</th><th style='text-align:right'>" + roundCompValue //
								+ "</th><th style='text-align:right'>" + delta //
								+ "</th><th style='text-align:right'>" + roundPercentChange //
								+ "% </th><th style='text-align:right'>" + sdFromMean //
								+ "</th></tr>\n", utf8, true);
					}
				}
			}
			FileUtils.writeStringToFile(file, "</tbody></table><br>", utf8, true);
		}
	}

	private static void createFailedTable(File file) throws IOException {
		try {
			FileUtils.write(file,
					"<table id =\"table2\"class=\"display compact anomalyTable\">"
							+ "    <caption class=\"anomalyCaption\">Test Anomalies</caption>" + "    <thead>"
							+ "        <tr class=\"th\">" //
							+ "            <th>Fingerprint</th>" //
							+ "            <th>Name</th>" //
							+ "            <th>Version</th>" //
							+ "            <th>Compare Version</th>" //
							+ "            <th>Mode</th>" //
							+ "            <th>Score</th>" //
							+ "            <th>Compare Value</th>" //
							+ "            <th>Delta</th>" //
							+ "            <th>Percent Change</th>" //
							+ "            <th>SD from Mean</th>" //
							+ "        </tr></thead><tbody>", //
					utf8, true);

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (CompareBenchmarks.totalFailedBenchmarks == 0) {
			FileUtils.writeStringToFile(file,
					"<tr><td><td><td><td><td style=\"text-align:center\">No tests failed.</td></td></td></td></td></tr>\n",
					utf8, true);
		} else {
			for (Map.Entry<String, Map<String, Map<String, Map<String, Object>>>> benchmark : CompareBenchmarks.failedBenchmarks
					.entrySet()) {
				String benchmarkName = benchmark.getKey();
				String fingerprint = Requests.namesToFingerprints.get(benchmarkName);

				Map<String, Map<String, Map<String, Object>>> benchVersions = benchmark.getValue();
				for (Map.Entry<String, Map<String, Map<String, Object>>> versionEntry : benchVersions.entrySet()) {
					String benchVersion = versionEntry.getKey();
					Map<String, Map<String, Object>> benchData = versionEntry.getValue();
					for (Map.Entry<String, Map<String, Object>> dataEntry : benchData.entrySet()) {
						String benchMode = dataEntry.getKey();
						Map<String, Object> benchmarkData = dataEntry.getValue();
						Double score = (Double) benchmarkData.get(ConfigHandling.BENCHMARK_SCORE);
						BigDecimal roundScore = BigDecimal.valueOf(score);
						Double compareValue = (Double) benchmarkData.get(Comparisons.CALCULATED_COMPARE_VALUE);
						BigDecimal roundCompValue = BigDecimal.valueOf(compareValue);
						Double delta = (Double) benchmarkData.get(Comparisons.CALCULATED_DELTA);
						Double percentChange = (Double) benchmarkData.get(Comparisons.CALCULATED_PERCENT_CHANGE);
						BigDecimal roundPercentChange = BigDecimal.valueOf(percentChange);
						Double sdFromMean = (Double) benchmarkData.get(Comparisons.CALCULATED_SD_FROM_MEAN);
						String compareVersion = (String) benchmarkData.get(ConfigHandling.COMPARE_VERSION);
						FileUtils.writeStringToFile(file, "<tr><th>" + fingerprint //
								+ "</th><th style='text-align:left'>" + benchmarkName //
								+ "</th><th>" + benchVersion //
								+ "</th><th>" + compareVersion + "</th><th>" + benchMode //
								+ "</th><th style='text-align:right'>" + roundScore //
								+ "</th><th style='text-align:right'>" + roundCompValue //
								+ "</th><th style='text-align:right'>" + delta //
								+ "</th><th style='text-align:right'>" + roundPercentChange //
								+ "% </th><th style='text-align:right'>" + sdFromMean //
								+ "</th></tr>\n", utf8, true);
					}
				}
			}
			FileUtils.write(file, "\n</tbody></table><br>", utf8, true);
		}
	}

	private static void createSkippedTable(File file) throws IOException {
		try {
			FileUtils.write(file,
					"<table id=\"table3\"class=\"display compact skippedTable\">"
							+ "    <caption class=\"skippedCaption\">Skipped Tests</caption>" + "    <thead>"
							+ "        <tr style=\"border: 1px dotted black;\">" //
							+ "            <th>Fingerprint</th>" //
							+ "            <th>Name</th>" //
							+ "            <th>Version</th>" //
							+ "            <th>Compare Version</th>" //
							+ "            <th>Mode</th>" //
							+ "        </tr></thead><tbody>",

					utf8, true);

		} catch (IOException e) {
			e.printStackTrace();
		}
		if (CompareBenchmarks.totalSkippedBenchmarks == 0) {
			FileUtils.writeStringToFile(file,
					"<tr><td><td><td style=\"text-align:center\">No tests were skipped.<td><td>"
							+ "</td></td></td></td></td></tr>\n</tbody></table>",
					utf8, true);
		} else {
			for (Map.Entry<String, Map<String, Map<String, Map<String, Object>>>> benchmark : CompareBenchmarks.skippedBenchmarks
					.entrySet()) {
				String benchmarkName = benchmark.getKey();
				String fingerprint = Requests.namesToFingerprints.get(benchmarkName);

				Map<String, Map<String, Map<String, Object>>> benchVersions = benchmark.getValue();
				for (Map.Entry<String, Map<String, Map<String, Object>>> versionEntry : benchVersions.entrySet()) {
					String benchVersion = versionEntry.getKey();
					Map<String, Map<String, Object>> benchData = versionEntry.getValue();
					for (Map.Entry<String, Map<String, Object>> dataEntry : benchData.entrySet()) {
						String benchMode = dataEntry.getKey();
						Map<String, Object> benchmarkData = dataEntry.getValue();
						Double score = (Double) benchmarkData.get(ConfigHandling.BENCHMARK_SCORE);
						/*
						 * BigDecimal roundScore = BigDecimal.valueOf(score); Double compareValue =
						 * (Double) benchmarkData.get(Comparisons.CALCULATED_COMPARE_VALUE); Double
						 * delta = (Double) benchmarkData.get(Comparisons.CALCULATED_DELTA); Double
						 * percentChange = (Double)
						 * benchmarkData.get(Comparisons.CALCULATED_PERCENT_CHANGE); Double sdFromMean =
						 * (Double) benchmarkData.get(Comparisons.CALCULATED_SD_FROM_MEAN);
						 */
						String compareVersion = (String) benchmarkData.get(ConfigHandling.COMPARE_VERSION);
						FileUtils.writeStringToFile(file, "<tr><th>" + fingerprint //
								+ "</th><th style='text-align:left'>" + benchmarkName //
								+ "</th><th>" + benchVersion //
								+ "</th><th>" + compareVersion //
								+ "</th><th>" + benchMode //
								+ "</th></tr>\n", utf8, true);
					}
				}
			}
		}
	}

	private static File genTemplateHTML() {

		InputStream in = gen.getClass().getResourceAsStream("/template.html");
		File tempfile = new File("src/main/tmp/temphtml.tmp");
		String result;
		try {
			result = IOUtils.toString(in, utf8);
			FileUtils.writeStringToFile(tempfile, result, utf8);
			in.close();
			return tempfile;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tempfile;

	}

	private static void genCSS() {
		InputStream in = gen.getClass().getResourceAsStream("/dataTables.css");
		InputStream styleIn = gen.getClass().getResourceAsStream("/styles.css");
		File tempFile = new File("htmlReports/styles/dataTables.css");
		File tempStyleFile = new File("htmlReports/styles/styles.css");
		String result;
		String resultStyle;
		try {
			result = IOUtils.toString(in, utf8);
			FileUtils.writeStringToFile(tempFile, result, utf8);
			in.close();
			resultStyle = IOUtils.toString(styleIn, utf8);
			FileUtils.writeStringToFile(tempStyleFile, resultStyle, utf8);
			styleIn.close();
		} catch (IOException e) {
			e.printStackTrace();

		}

	}

	private static void deleteTempFiles() {
		File tempDir = new File("src/main/tmp/");
		try {
			FileUtils.deleteDirectory(tempDir);
			log.info("* Temporary files deleted.");
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Error while deleting temporary files directory.");
		}
	}

	private static void changeComparatorConfigs(Map<String, Object> configs) {
		String tempString = "";
		String longConfigGet = configs.get("compare.default").toString();
		tempString = longConfigGet.substring(1, longConfigGet.length() - 1);
		String[] propsArray = tempString.split(". ");
		setConfig(propsArray);
	}

	private static void changeScriptConfigs(Map<String, Object> compProps) {
		String tempString = "";
		for (Map.Entry<String, Object> entry : compProps.entrySet()) {
			if (entry.getKey().equals("MyScript")) {
				tempString = entry.getValue().toString();
			}
		}
		tempString = tempString.substring(1, tempString.length() - 1);
		String[] propsArray = tempString.split(", ");
		setConfig(propsArray);
	}

	private static void setConfig(String[] propsArray) {
		for (String pName : propsArray) {
			if (pName.contains("method")) {
				method = pName.substring(7);
			} else if (pName.contains("range")) {
				range = pName.substring(6);
			} else if (pName.contains("scope")) {
				scope = pName.substring(6);
			} else if (pName.contains("compareVersion")) {
				compareVersion = pName.substring(15);
			} else if (pName.contains("threshold")) {
				threshold = pName.substring(10);
			} else if (pName.contains("percentChangeAllowed")) {
				percentChangeAllowed = pName.substring(21);
			} else if (pName.contains("deviationsAllowed")) {
				deviationsAllowed = pName.substring(18);
			}
		}
	}

	private static void changeDateTime() {
		dateTime = ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);
	}

	private static void changeVersion() {
		List<String> passedNames = new ArrayList<>();
		for (Entry<String, Map<String, Map<String, Map<String, Object>>>> benchmark : CompareBenchmarks.passedBenchmarks
				.entrySet()) {
			String tempName = benchmark.getKey();
			passedNames.add(tempName);

		}
		for (String pName : passedNames) {
			String tempFingerprint = Requests.namesToFingerprints.get(pName);
			version = Requests.getCurrentVersion(tempFingerprint);
		}
	}

	private static String getDateTimeForFileName() {
		DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
		Date date = new Date();
		String fileNameDateTime = df.format(date);
		return fileNameDateTime;
	}

	public static void grabPackageNames(ArrayList<String> packNames) {
		if (!packNames.isEmpty()) {
			try {
				Set<String> pkgNames = new HashSet<>(packNames);
				packNames.clear();
				packNames.addAll(pkgNames);
				packageNames = packNames;

			} catch (Exception e) {
				log.error("Error grabbing package names from benchmark test");
			}
		} else {
			log.error("List of package names was empty.");
		}

	}

	public static void sendToWebpageGenerator(Map<String, Object> allConfig, Map<String, String> packages) {
		if (!allConfig.isEmpty()) {
			try {
				allConfigs = allConfig;
			} catch (Exception e) {
				log.error("Error passing configuration values");
				e.printStackTrace();
			}
		} else {
			log.error("Passed configuration values were empty.");
		}
	}

	public static void sendToWebpageGenerator(String script) {
		try {
			scriptName = script.substring(script.lastIndexOf("\\") + 1);
		} catch (Exception e) {
			log.error("Error passing script file path.");
			e.printStackTrace();
		}

	}

	public static void logInfo(String msg, Object... args) {
		log.info(msg, args);
	}

	public static void logWarn(String msg, Object... args) {
		log.warn(msg, args);
	}

	public static void logErr(String msg, Object... args) {
		log.error(msg, args);
	}

}