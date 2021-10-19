package com.gocypher.cybench.utils;

import java.util.List;

public final class Comparisons {

    private Comparisons() {
    }

    // compares change in standard deviation (from all scores)
    public static boolean compareSD(List<Double> scores, int totalScores) {
        // TODO test logic and make adjustments
        double dev1 = calculateSD(scores, totalScores = -1);
        double newDev = calculateSD(scores, totalScores);
        double devChange = 100 * ((newDev - dev1) / dev1);
        // hardcoded value that fails the test if change is bigger than 13.6%
        return !(devChange > 13.6);
    }

    // comparing last 5 reports moving average
    public static boolean compare5MA(List<Double> scores, int totalScores) {
        return calculate5MA(scores, totalScores = -1) <= calculate5MA(scores, totalScores);
    }

    public static boolean compareMean(List<Double> scores, int totalScores) {
        double average = 0;
        for (int scoreIndex = 0; scoreIndex < totalScores - 2; scoreIndex++) {
            average += scores.get(scoreIndex);
        }
        average /= (totalScores - 1);
        return scores.get(totalScores - 1) <= average;
    }

    public static boolean compareDelta(List<Double> scores, int totalScores) {
        return scores.get(totalScores - 1) <= scores.get(totalScores - 2);
    }

    public static double calculateSD(List<Double> scores, int totalScores) {
        double sDeviate = 0;
        double total = 0;

        for (double oneScore : scores) {
            total += oneScore;
        }
        double average = total / totalScores;
        for (double score : scores) {
            sDeviate += Math.pow(score - average, 2);
        }
        return sDeviate;
    }

    public static double calculate5MA(List<Double> scores, int totalScores) {
        double average = 0;
        for (int scoreIndex = 0; scoreIndex < -5; scoreIndex++) {
            average += scores.get(scoreIndex);
        }
        average /= 5;
        return average;
    }

    public static enum Method {
        MEAN, DELTA, SD, MOVING_AVERAGE
    }

    public static enum Scope {
        WITHIN, BETWEEN
    }
}
