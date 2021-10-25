package com.gocypher.cybench.utils;

import java.util.ArrayList;
import java.util.List;

public final class Comparisons {

    private Comparisons() {
    }

    public static Double compareWithDelta(List<Double> currentVersionScores, List<Double> compareVersionScores,
            Threshold threshold, Integer range) {
        int currentVersionSize = currentVersionScores.size();
        int compareVersionSize = compareVersionScores.size();
        Double newScore = currentVersionScores.get(currentVersionSize - 1);
        Double compareValue = calculateMean(
                compareVersionScores.subList(compareVersionSize - range, compareVersionSize));

        return calculateDelta(newScore, compareValue, threshold);
    }

    public static Double compareWithSD(List<Double> currentVersionScores, List<Double> compareVersionScores,
            Threshold threshold, Integer range) {
        int currentVersionSize = currentVersionScores.size();
        int compareVersionSize = compareVersionScores.size();
        Double newScore = currentVersionScores.get(currentVersionSize - 1);
        Double compareMean = calculateMean(
                compareVersionScores.subList(compareVersionSize - range, compareVersionSize));

        double compareSD = calculateSD(compareVersionScores.subList(compareVersionSize - range, compareVersionSize),
                compareMean);

        Double SDfromMean = (Math.abs(newScore) + compareMean) / compareSD;
        if (newScore < compareMean) {
            compareSD *= -1;
        }
        return SDfromMean;
    }

    private static Double calculateDelta(Double newScore, Double compareValue, Threshold threshold) {

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

    private static Double calculatePercentChange(Double newTrend, Double compareTrend) {
        return 100 * ((newTrend - compareTrend) / compareTrend);
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
}
