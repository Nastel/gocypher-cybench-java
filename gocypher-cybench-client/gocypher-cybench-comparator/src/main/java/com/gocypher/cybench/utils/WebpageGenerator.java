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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.gocypher.cybench.CompareBenchmarks;
import com.gocypher.cybench.services.Requests;

public class WebpageGenerator {

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
	static Charset utf8 = StandardCharsets.UTF_8;
	static Map<String, Object> allConfigs;

	public WebpageGenerator() {
		// TODO Remove Logs/Prints | Change conditional for class variables to be
		// skipped
		// Make sure no artifacts are left in other classes from debugging
		// Add support/HTML generation for multiple packaged in comparator.yaml

	}

	// for comparator configuration
	public static void generatePage() throws IOException, URISyntaxException {
		configType = "comparator.yaml";
		WebpageGenerator gen = new WebpageGenerator();
		File tempfile = genTemplateHTML();
		File newHtml = new File("output/results.html");
		String htmlTemp;

		htmlTemp = FileUtils.readFileToString(tempfile, utf8);

		// debug
		if (allConfigs == null) {
			System.out.println("allConfigs wasn't set correctly....");
		} else {
			changeComparatorConfigs(allConfigs);
			changeVersion();
			changeDateTime();
		}

		Class<?> clazz = gen.getClass();
		for (Field field : clazz.getDeclaredFields()) {
			try {
				if (field.getName() != "utf8" && field.getName() != "allConfigs") {
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
		System.out.println("Done creating page results with updated template");
	}

	// for script configuration || props gets passed all the way from
	// ComparatorScriptEngine
	public static void generatePage(Map<String, Object> props) throws IOException {
		configType = "JavaScript"; // TODO: Add actual script name
		WebpageGenerator gen = new WebpageGenerator();
		File tempfile = genTemplateHTML();
		File newHtml = new File("output/results.html");
		String htmlTemp;

		htmlTemp = FileUtils.readFileToString(tempfile, utf8);

		changeScriptConfigs(props);
		changeVersion();
		changeDateTime();
		Class<?> clazz = gen.getClass();
		for (Field field : clazz.getDeclaredFields()) {
			try {
				if (field.getName() != "utf8" && field.getName() != "allConfigs") {
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
	}

	private static void createPassedTable(File file) throws IOException {

		if (CompareBenchmarks.totalPassedBenchmarks == 0) {
			FileUtils.writeStringToFile(file,
					"<tr><td><td><td><td><td> No tests passed.</td></td></td></td></td></tr>\n", utf8, true);
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
						FileUtils.writeStringToFile(file,
								"<tr><th>" + fingerprint + "</th><th>" + benchmarkName + "</th><th>" + benchVersion
										+ "</th><th>" + benchMode + "</th><th>" + roundScore + "</th><th>"
										+ roundCompValue + "</th><th>" + delta + "</th><th>" + roundPercentChange
										+ "% </th><th>" + sdFromMean + "</th></tr>\n",
								utf8, true);
					}
				}
			}
		}
	}

	private static void createFailedTable(File file) throws IOException {
		try {
			FileUtils.write(file,
					"<table style=\"margin-left:auto;margin-right:auto;border-style:double;background-color:white;\">\r\n"
							+ "    <caption style=\"border-style:double;background-color:#f97c7c;font-weight:bold\">Failed Tests</caption>\r\n"
							+ "        <tr style=\"border: 1px dotted black;\">\r\n" //
							+ "            <th>Fingerprint</th>\r\n" //
							+ "            <th>Name</th>\r\n" //
							+ "            <th>Version</th>\r\n" //
							+ "            <th>Mode</th>\r\n" //
							+ "            <th>Score</th>\r\n" //
							+ "            <th>Compare Value</th>\r\n" //
							+ "            <th>Delta</th>\r\n" //
							+ "            <th>Percent Change</th>\r\n" //
							+ "            <th>SD from Mean</th>\r\n" //
							+ "        </tr>\r\n" //
							+ "    <tr style=\"border: 1px dotted black;font-size:70%\">", //
					utf8, true);

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (CompareBenchmarks.totalFailedBenchmarks == 0) {
			FileUtils.writeStringToFile(file,
					"<tr><td><td><td><td><td> No tests failed.</td></td></td></td></td></tr>\n", utf8, true);
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
						FileUtils.writeStringToFile(file,
								"<tr><th>" + fingerprint + "</th><th>" + benchmarkName + "</th><th>" + benchVersion
										+ "</th><th>" + benchMode + "</th><th>" + roundScore + "</th><th>"
										+ roundCompValue + "</th><th>" + delta + "</th><th>" + roundPercentChange
										+ "% </th><th>" + sdFromMean + "</th></tr>\n",
								utf8, true);
					}

				}
			}
			FileUtils.write(file, "\n</table><br>", utf8, true);

		}
	}

	private static void createSkippedTable(File file) throws IOException {
		try {
			FileUtils.write(file,
					"<table style=\"margin-left:auto;margin-right:auto;border-style:double;background-color:white;\">\r\n"
							+ "    <caption style=\"border-style:double;background-color:goldenrod;font-weight:bold\">Skipped Tests</caption>\r\n"
							+ "        <tr style=\"border: 1px dotted black;\">\r\n" //
							+ "            <th>Fingerprint</th>\r\n" //
							+ "            <th>Name</th>\r\n" //
							+ "            <th>Version</th>\r\n" //
							+ "            <th>Mode</th>\r\n" //
							+ "            <th>Score</th>\r\n" //
							+ "            <th>Compare Value</th>\r\n" //
							+ "            <th>Delta</th>\r\n" //
							+ "            <th>Percent Change</th>\r\n" //
							+ "            <th>SD from Mean</th>\r\n" //
							+ "        </tr>\r\n" //
							+ "    <tr style=\"border: 1px dotted black;font-size:70%\">", //
					utf8, true);

		} catch (IOException e) {
			e.printStackTrace();
		}
		if (CompareBenchmarks.totalSkippedBenchmarks == 0) {
			FileUtils.writeStringToFile(file,
					"<tr><td><td><td><td><td> No tests were skipped.</td></td></td></td></td></tr>\n", utf8, true);
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
						BigDecimal roundScore = BigDecimal.valueOf(score);
						Double compareValue = (Double) benchmarkData.get(Comparisons.CALCULATED_COMPARE_VALUE);
						Double delta = (Double) benchmarkData.get(Comparisons.CALCULATED_DELTA);
						Double percentChange = (Double) benchmarkData.get(Comparisons.CALCULATED_PERCENT_CHANGE);
						Double sdFromMean = (Double) benchmarkData.get(Comparisons.CALCULATED_SD_FROM_MEAN);
						FileUtils.writeStringToFile(file,
								"<tr><th>" + fingerprint + "</th><th>" + benchmarkName + "</th><th>" + benchVersion
										+ "</th><th>" + benchMode + "</th><th>" + roundScore + "</th><th>"
										+ compareValue + "</th><th>" + delta + "</th><th>" + percentChange
										+ "% </th><th>" + sdFromMean + "</th></tr>\n",
								utf8, true);
					}
				}
			}
		}
	}

	private static File genTemplateHTML() {
		WebpageGenerator gen = new WebpageGenerator();
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

	private static void changeComparatorConfigs(Map<String, Object> configs) {
		String tempString = "";

		String longConfigGet = configs.get("compare.default").toString();
		tempString = longConfigGet.substring(1, longConfigGet.length() - 1);

		String[] propsArray = tempString.split(". ");
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

	private static void changeScriptConfigs(Map<String, Object> compProps) {
		String tempString = "";
		System.out.println("Begin loop through compProps");
		for (Map.Entry<String, Object> entry : compProps.entrySet()) {
			if (entry.getKey().equals("MyScript")) {
				tempString = entry.getValue().toString();
			}
		}
		tempString = tempString.substring(1, tempString.length() - 1);
		System.out.println(tempString);
		String[] propsArray = tempString.split(", ");
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
	
	public static void sendToWebpageGenerator(Map<String, Object> allConfig, Map<String, String> packages) {
		allConfigs = allConfig;
		System.out.println("All Configs: \n" + allConfig.toString());
		System.out.println("All Packs: \n" + packages.toString());
	}

}
