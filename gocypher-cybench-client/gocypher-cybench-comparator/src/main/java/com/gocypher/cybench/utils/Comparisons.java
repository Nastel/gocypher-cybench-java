package com.gocypher.cybench.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public static Double compareWithDelta(List<Double> currentVersionScores, List<Double> compareVersionScores,
            Threshold threshold, String rangeString) {
        int currentVersionSize = currentVersionScores.size();
        int compareVersionSize = compareVersionScores.size();
        Integer range = validateRange(compareVersionScores, rangeString);
        Double newScore = currentVersionScores.get(currentVersionSize - 1);
        Double compareValue = calculateMean(
                compareVersionScores.subList(compareVersionSize - range, compareVersionSize));

        double delta = calculateDelta(newScore, compareValue, threshold);

        if (threshold.equals(Threshold.GREATER)) {
            double deltaPercentChange = calculatePercentChange(newScore, compareValue);
            log.info("comparison=delta, recentScore={}, range={}, compareMean={}, delta={}, percentChange={}%",
                    newScore, rangeString, compareValue, delta, deltaPercentChange);
        } else {
            log.info("comparison=delta, recentScore={}, range={}, compareMean={}, percentChange={}%", newScore,
                    rangeString, compareValue, delta);
        }

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

    public static void logComparison(Map<String, Object> logConfigs, String benchmarkName, String mode) {
        StringBuilder sb = new StringBuilder();
        Method method = (Method) logConfigs.get("method");
        Scope scope = (Scope) logConfigs.get("scope");
        String currentVersion = (String) logConfigs.get("currentVersion");
        String compareVersion = (String) logConfigs.get("compareVersion");
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
        if (Math.abs(deviationsFromMean) < deviationsAllowed) {
            log.info("Passed test");
            return true;
        } else {
            log.warn("FAILED test");
            return false;
        }
    }

    public static boolean passAssertionPercentage(Double percentChange, Double percentageAllowed) {
        if (Math.abs(percentChange) < percentageAllowed) {
            log.info("Passed test");
            return true;
        } else {
            log.warn("FAILED test");
            return false;
        }
    }

    public static boolean passAssertionPositive(Double val) {
        if (val >= 0) {
            log.info("Passed test");
            return true;
        } else {
            log.warn("FAILED test");
            return false;
        }
    }
}
