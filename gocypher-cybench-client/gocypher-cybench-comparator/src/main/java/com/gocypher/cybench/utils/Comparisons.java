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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gocypher.cybench.CompareBenchmarks;
import com.gocypher.cybench.services.Requests;

public final class Comparisons {
	private static final Logger log = LoggerFactory.getLogger(Comparisons.class);
	public static final DecimalFormat df1 = new DecimalFormat("#.##");

	private Comparisons() {
	}

	public static int validateRange(List<Double> scores, String compareRange) {
		int range;
		int totalScores = scores.size();
		if (compareRange.equals("ALL")) {
			range = totalScores;
		} else {
			range = Integer.parseInt(compareRange);
			if (range > totalScores) {
				log.warn(
						"There are less scores to compare to than the specified range, will compare to as many as possible.");
				range = totalScores;
			}
		}
		return range;
	}

	public static Double compareWithDelta(List<Double> currentVersionScores, List<Double> compareVersionScores,
			Threshold threshold, String rangeString) {

		int currentVersionSize = currentVersionScores.size();
		int compareVersionSize = compareVersionScores.size();
		int range = validateRange(compareVersionScores, rangeString);
		Double newScore = currentVersionScores.get(currentVersionSize - 1);
		Double compareValue = calculateMean(
				compareVersionScores.subList(compareVersionSize - range, compareVersionSize));

		double delta = calculateDelta(newScore, compareValue, threshold);

		if (threshold.equals(Threshold.GREATER)) {
			double deltaPercentChange = calculatePercentChange(newScore, compareValue);

			// round values to two decimals
			newScore = roundTwoDecimal(newScore);
			compareValue = roundTwoDecimal(compareValue);
			delta = roundTwoDecimal(delta);
			deltaPercentChange = roundTwoDecimal(deltaPercentChange);

			log.info("comparison=delta, recentScore={}, range={}, compareMean={}, delta={}, percentChange={}%",
					newScore, rangeString, compareValue, delta, deltaPercentChange);
		} else {
			log.info("Attempting to format Double's... New Style:");
			newScore = roundTwoDecimal(newScore);
			compareValue = roundTwoDecimal(compareValue);
			delta = roundTwoDecimal(delta);
			log.info("comparison=DELTA, recentScore={}, range={}, compareMean={}, percentChange={}%", newScore,
					rangeString, compareValue, delta);
		}

		return delta;
	}

	public static Double compareWithSD(List<Double> currentVersionScores, List<Double> compareVersionScores,
			String rangeString) {
		int currentVersionSize = currentVersionScores.size();
		int compareVersionSize = compareVersionScores.size();
		int range = validateRange(compareVersionScores, rangeString);
		Double newScore = currentVersionScores.get(currentVersionSize - 1);
		Double compareMean = calculateMean(
				compareVersionScores.subList(compareVersionSize - range, compareVersionSize));

		double compareSD = calculateSD(compareVersionScores.subList(compareVersionSize - range, compareVersionSize),
				compareMean);

		double SDfromMean = (Math.abs(newScore) + compareMean) / compareSD;

		if (newScore < compareMean) {
			SDfromMean *= -1;
		}
		newScore = roundTwoDecimal(newScore);
		compareMean = roundTwoDecimal(compareMean);
		compareSD = roundTwoDecimal(compareSD);
		SDfromMean = roundTwoDecimal(SDfromMean);
		log.info("comparison=SD, recentScore={}, range={}, mean={}, SD={}, SDfromMean={}", newScore, rangeString,
				compareMean, compareSD, SDfromMean);

		return SDfromMean;
	}

	public static void logComparison(Map<String, Object> logConfigs, String benchmarkName, String mode) {
		String benchmarkFingerprint = Requests.namesToFingerprints.get(benchmarkName);
		String currentVersion = Requests.getCurrentVersion(benchmarkFingerprint);
		StringBuilder sb = new StringBuilder();
		Method method = (Method) logConfigs.get("method");
		Scope scope = (Scope) logConfigs.get("scope");
		String compareVersion = (String) logConfigs.get("compareVersion");
		if (compareVersion.equals(ConfigHandling.DEFAULT_COMPARE_VERSION)) {
			compareVersion = Requests.getPreviousVersion(benchmarkFingerprint);
		}
		sb.append("COMPARISON - {} : {} - {} running {} current version {}");
		if (scope.equals(Scope.BETWEEN)) {
			sb.append(" and version ").append(compareVersion);
		}
		log.info(sb.toString(), benchmarkName, mode, method, scope, currentVersion);
	}

	// Calculate Methods
	public static Double calculateDelta(Double newScore, Double compareValue, Threshold threshold) {

		Double difference = null;

		if (compareValue != null) {
			switch (threshold) {
			case GREATER:
				difference = newScore - compareValue;
				break;
			case PERCENT_CHANGE:
				difference = calculatePercentChange(newScore, compareValue);
				break;
			}
		}

		return difference;
	}

	public static Double calculateMean(List<Double> scores) {
		Double average = 0.0;
		for (Double score : scores) {
			average += score;
		}
		return average / scores.size();
	}

	public static Double calculateSD(List<Double> scores) {
		Double mean = calculateMean(scores);
		return calculateSD(scores, mean);
	}

	public static Double calculateSD(List<Double> scores, Double mean) {
		List<Double> temp = new ArrayList<>();

		for (Double score : scores) {
			temp.add(Math.pow(score - mean, 2));
		}

		return Math.sqrt(calculateMean(temp));
	}

	private static Double calculatePercentChange(Double newScore, Double compareScore) {
		return 100 * ((newScore - compareScore) / compareScore);
	}

	private static Double roundTwoDecimal(Double value) {
		// TODO: Handle *BIG* scores in scientific notation
		DecimalFormat df1 = new DecimalFormat("##################.00");
		// DecimalFormat.format() always returns a string, must convert to Double
		String tempStr = df1.format(value);
		Double formatValue = Double.parseDouble(tempStr);
		return formatValue;
	}

	public static enum Method {
		DELTA, SD
	}

	public static enum Scope {
		WITHIN, BETWEEN
	}

	public static enum Threshold {
		PERCENT_CHANGE, GREATER
	}

	public static boolean passAssertion(Double COMPARE_VALUE, Method method, Threshold threshold,
			Double percentageAllowed, Double deviationsAllowed) {
		// assert within x SDs from mean
		if (method.equals(Method.SD)) {
			return passAssertionDeviation(COMPARE_VALUE, deviationsAllowed);
		}

		// assert within x Percentage from COMPARE_VALUE
		if (threshold.equals(Threshold.PERCENT_CHANGE)) {
			return passAssertionPercentage(COMPARE_VALUE, percentageAllowed);
		}

		// assert higher than COMPARE_VALUE
		return passAssertionPositive(COMPARE_VALUE);
	}

	public static boolean passAssertionDeviation(Double deviationsFromMean, Double deviationsAllowed) {
		CompareBenchmarks.totalComparedBenchmarks++;
		if (Math.abs(deviationsFromMean) < deviationsAllowed) {
			log.info("Passed test");
			CompareBenchmarks.totalPassedBenchmarks++;
			return true;
		} else {
			log.warn("FAILED test");
			CompareBenchmarks.totalFailedBenchmarks++;
			return false;
		}
	}

	public static boolean passAssertionPercentage(Double percentChange, Double percentageAllowed) {
		CompareBenchmarks.totalComparedBenchmarks++;
		if (Math.abs(percentChange) < percentageAllowed) {
			log.info("Passed test");
			CompareBenchmarks.totalPassedBenchmarks++;
			return true;
		} else {
			log.warn("FAILED test");
			CompareBenchmarks.totalFailedBenchmarks++;
			return false;
		}
	}

	public static boolean passAssertionPositive(Double val) {
		CompareBenchmarks.totalComparedBenchmarks++;
		if (val >= 0) {
			log.info("Passed test");
			CompareBenchmarks.totalPassedBenchmarks++;
			return true;
		} else {
			log.warn("FAILED test");
			CompareBenchmarks.totalFailedBenchmarks++;
			return false;
		}
	}
}
