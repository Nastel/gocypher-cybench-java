package com.gocypher.cybench.utils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Comparisons {
	private static final Logger log = LoggerFactory.getLogger(Comparisons.class);

	private Comparisons() {
	}

	public static Integer validateRange(List<Double> scores, String compareRange) {
		Integer range = null;
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

	// WITHIN version
	public static Double compareWithDelta(List<Double> withinVersionScores, Threshold threshold, String rangeString) {
		List<Double> compareVersionScores = new ArrayList<>(withinVersionScores);
		// remove new score to have a comparative list
		compareVersionScores.remove(withinVersionScores.size() - 1);
		return compareWithDelta(withinVersionScores, compareVersionScores, threshold, rangeString);
	}

	public static Double compareWithSD(List<Double> withinVersionScores, String rangeString) {
		List<Double> compareVersionScores = new ArrayList<>(withinVersionScores);
		// remove new score to have a comparative list
		compareVersionScores.remove(withinVersionScores.size() - 1);
		return compareWithSD(withinVersionScores, compareVersionScores, rangeString);
	}

	// BETWEEN versions
	public static Double compareWithDelta(List<Double> currentVersionScores, List<Double> compareVersionScores,
			Threshold threshold, String rangeString) {
		int currentVersionSize = currentVersionScores.size();
		int compareVersionSize = compareVersionScores.size();
		Integer range = validateRange(compareVersionScores, rangeString);
		Double newScore = currentVersionScores.get(currentVersionSize - 1);
		Double compareValue = calculateMean(
				compareVersionScores.subList(compareVersionSize - range, compareVersionSize));

		double delta = calculateDelta(newScore, compareValue, threshold);

		log.info("comparison=delta, recentScore={}, range={}, compareMean={}, delta={}", newScore, rangeString,
				compareValue, delta);

		return delta;
	}

	public static Double compareWithSD(List<Double> currentVersionScores, List<Double> compareVersionScores,
			String rangeString) {
		int currentVersionSize = currentVersionScores.size();
		int compareVersionSize = compareVersionScores.size();
		Integer range = validateRange(compareVersionScores, rangeString);
		Double newScore = currentVersionScores.get(currentVersionSize - 1);
		Double compareMean = calculateMean(
				compareVersionScores.subList(compareVersionSize - range, compareVersionSize));

		double compareSD = calculateSD(compareVersionScores.subList(compareVersionSize - range, compareVersionSize),
				compareMean);

		double SDfromMean = (Math.abs(newScore) + compareMean) / compareSD;

		if (newScore < compareMean) {
			SDfromMean *= -1;
		}

		log.info("comparison=SD, recentScore={}, range={}, mean={}, SD={}, SDfromMean={}", newScore, rangeString,
				compareMean, compareSD, SDfromMean);

		return SDfromMean;
	}

	// Compare Methods with Logging
	public static Double compareWithDelta(List<Double> withinVersionScores, Threshold threshold, String rangeString,
			String benchmarkName, String mode, String currentVersion) {
		log.info("{} : {} - Within version {} delta comparison running", benchmarkName, mode, currentVersion);
		Double delta = compareWithDelta(withinVersionScores, threshold, rangeString);
		return delta;
	}

	public static Double compareWithSD(List<Double> withinVersionScores, String rangeString, String benchmarkName,
			String mode, String currentVersion) {
		log.info("{} : {} - Within version {} SD comparison running", benchmarkName, mode, currentVersion);
		Double SDfromMean = compareWithSD(withinVersionScores, rangeString);
		return SDfromMean;
	}

	public static Double compareWithDelta(List<Double> currentVersionScores, List<Double> compareVersionScores,
			Threshold threshold, String rangeString, String benchmarkName, String mode, String currentVersion,
			String previousVersion) {
		log.info("{} : {} - Between current version {} and previous version {} delta comparison running", benchmarkName,
				mode, currentVersion, previousVersion);
		Double delta = compareWithDelta(currentVersionScores, compareVersionScores, threshold, rangeString);
		return delta;
	}

	public static Double compareWithSD(List<Double> currentVersionScores, List<Double> compareVersionScores,
			String rangeString, String benchmarkName, String mode, String currentVersion, String previousVersion) {
		log.info("{} : {} - Between current version {} and previous version {} SD comparison running", benchmarkName,
				mode, currentVersion, previousVersion);
		Double SDfromMean = compareWithSD(currentVersionScores, compareVersionScores, rangeString);
		return SDfromMean;
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
		return Math.abs(deviationsFromMean) < deviationsAllowed;
	}

	public static boolean passAssertionPercentage(Double percentChange, Double percentageAllowed) {
		return Math.abs(percentChange) < percentageAllowed;
	}

	public static boolean passAssertionPositive(Double val) {
		return val >= 0;
	}
}
