package com.gocypher.cybench.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.gocypher.cybench.CompareBenchmarks;
import com.gocypher.cybench.services.Requests;
import com.sun.tools.javac.jvm.Gen;

public class WebpageGenerator {

	String passed = "" + CompareBenchmarks.totalPassedBenchmarks;
	String failed = "" + CompareBenchmarks.totalFailedBenchmarks;
	String total = "" + CompareBenchmarks.totalComparedBenchmarks;
	String skipped = "" + CompareBenchmarks.totalSkippedBenchmarks;

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
	static Charset utf8 = Charset.forName("UTF-8");
	static Map<String, Object> allConfigs;

	public WebpageGenerator() {
		//TODO Remove Logs/Prints | Change conditional for class variables to be skipped
		//     Make sure no artifacts are left in other classes from debugging
		//     Add support/HTML generation for multple packaged in comparator.yaml
		
		
	}

	// for comparator configuration
	public static void generatePage() throws IOException {
		File templateHTML = new File("config/template.html");
		File newHtml = new File("output/results.html");
		String htmlTemp;
		configType = "comparator.yaml";
		WebpageGenerator gen = new WebpageGenerator();
		htmlTemp = FileUtils.readFileToString(templateHTML, utf8);

		
		//debug
		System.out.println("Comparator Config detected.........");
		if (allConfigs == null) {
			System.out.println("allConfigs wasn't set correctly....");
		} else {
			changeComparatorConfigs(allConfigs);
			changeVersion();
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
		System.out.println("Done creating page results");
	}

	// for script configuration || props gets passed all the way from ComparatorScriptEngine
	public static void generatePage(Map<String, Object> props) throws IOException {
		File templateHTML = new File("config/template.html");
		File newHtml = new File("output/results.html");
		String htmlTemp;
		configType = "JavaScript"; // TODO: Add actual script name
		WebpageGenerator gen = new WebpageGenerator();
		htmlTemp = FileUtils.readFileToString(templateHTML, utf8);

		changeScriptConfigs(props);
		changeVersion();

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
		System.out.println("Done creating page results");
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
							+ "	<caption style=\"border-style:double;background-color:#f97c7c;font-weight:bold\">Failed Tests</caption>\r\n"
							+ "			<tr style=\"border: 1px dotted black;\">\r\n"
							+ "			<th>Fingerprint</th>\r\n" + "			<th>Name</th>\r\n"
							+ "			<th>Version</th>\r\n" + "			<th>Mode</th>\r\n"
							+ "			<th>Score</th>\r\n" + "			<th>Compare Value</th>\r\n"
							+ "			<th>Delta</th>\r\n" + "			<th>Percent Change</th>\r\n"
							+ "			<th>SD from Mean</th>\r\n" + "		</tr>\r\n"
							+ "	<tr style=\"border: 1px dotted black;font-size:70%\">",
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
							+ "	<caption style=\"border-style:double;background-color:goldenrod;font-weight:bold\">Skipped Tests</caption>\r\n"
							+ "			<tr style=\"border: 1px dotted black;\">\r\n"
							+ "			<th>Fingerprint</th>\r\n" + "			<th>Name</th>\r\n"
							+ "			<th>Version</th>\r\n" + "			<th>Mode</th>\r\n"
							+ "			<th>Score</th>\r\n" + "			<th>Compare Value</th>\r\n"
							+ "			<th>Delta</th>\r\n" + "			<th>Percent Change</th>\r\n"
							+ "			<th>SD from Mean</th>\r\n" + "		</tr>\r\n"
							+ "	<tr style=\"border: 1px dotted black;font-size:70%\">",
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

	private static void changeVersion() {
		List<String> passedNames = new ArrayList();
		for (Entry<String, Map<String, Map<String, Map<String, Object>>>> benchmark : CompareBenchmarks.passedBenchmarks
				.entrySet()) {
			String tempName = benchmark.getKey().toString();
			passedNames.add(tempName);

		}
		for (String name : passedNames) {
			String tempFingerprint = Requests.namesToFingerprints.get(name);
			version = Requests.getCurrentVersion(tempFingerprint);
		}
	}

	private static void changeComparatorConfigs(Map<String, Object> configs) {
		String tempString = "";

		String longConfigGet = (String) configs.get("compare.default").toString();
		tempString = longConfigGet.substring(1, longConfigGet.length()-1);
		
		String[] propsArray = tempString.split(". ");
		for (String name : propsArray) {
			if (name.contains("method")) {
				method = name.substring(7);
			} else if (name.contains("range")) {
				range = name.substring(6);
			} else if (name.contains("scope")) {
				scope = name.substring(6);
			} else if (name.contains("compareVersion")) {
				compareVersion = name.substring(15);
			} else if (name.contains("threshold")) {
				threshold = name.substring(10);
			} else if (name.contains("percentChangeAllowed")) {
				percentChangeAllowed = name.substring(21);
			} else if (name.contains("deviationsAllowed")) {
				deviationsAllowed = name.substring(18);
			}
		}
		
	}
	
	private static void changeScriptConfigs(Map<String, Object> compProps) {
		String tempString = "";
		int size = compProps.size();
		System.out.println("Begin loop through compProps");
		for (Map.Entry<String, Object> entry : compProps.entrySet()) {
			if (entry.getKey().equals("MyScript")) {
				tempString = entry.getValue().toString();
				System.out.println(range);
			}
		}
		tempString = tempString.substring(1, tempString.length() - 1);
		System.out.println(tempString);
		String[] propsArray = tempString.split(", ");
		for (String name : propsArray) {
			if (name.contains("method")) {
				method = name.substring(7);
			} else if (name.contains("range")) {
				range = name.substring(6);
			} else if (name.contains("scope")) {
				scope = name.substring(6);
			} else if (name.contains("compareVersion")) {
				compareVersion = name.substring(15);
			} else if (name.contains("threshold")) {
				threshold = name.substring(10);
			} else if (name.contains("percentChangeAllowed")) {
				percentChangeAllowed = name.substring(21);
			} else if (name.contains("deviationsAllowed")) {
				deviationsAllowed = name.substring(18);
			}
		}

	}

	public static void sendToWebpageGenerator(Map<String, Object> allConfig, Map<String, String> packages) {
		allConfigs = allConfig;
		System.out.println("All Configs: \n" + allConfig.toString());
		System.out.println("All Packs: \n" + packages.toString());
	}

}
