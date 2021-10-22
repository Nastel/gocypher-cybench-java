package com.gocypher.cybench.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Comparisons {

    private Comparisons() {
    }

    public static Double compareDelta(List<Double> newScores, List<Double> compareScores,
            Threshold threshold, Range range) {
        int newScoresStopCounter = getStopCounter(newScores, range);
        int compareScoresStopCounter = getStopCounter(compareScores, range);

        Double newTrend = calculateDeltaTrend(newScores, newScoresStopCounter);
        Double compareTrend = calculateDeltaTrend(compareScores, compareScoresStopCounter);

        return getDifference(newTrend, compareTrend, threshold);
    }

    private static Double getDifference(Double newTrend, Double compareTrend, Threshold threshold) {

        double difference = 0.0;

        switch (threshold) {
        case GREATER:
            difference = newTrend - compareTrend;
            break;
        case PERCENT_CHANGE:
            difference = calculatePercentChange(newTrend, compareTrend);
            break;
        }

        return difference;
    }

    // returns average delta after calculating delta at each point in the list
    // if trend = NONE : returns recentScore
    public static Double calculateDeltaTrend(List<Double> scores, int stopCounter) {
        int size = scores.size();
        if (size < 2) {
            return 0.0;
        } else {
            if (stopCounter == size) {
                return scores.get(size - 1);
            } else {
                List<Double> deltas = new ArrayList<>();
                for (int i = size - 1; i > stopCounter; i--) {
                    deltas.add(scores.get(i) - scores.get(i - 1));
                }
                return calculateMean(deltas);
            }
        }
    }

    public static Double compareMean(List<Double> newScores, List<Double> compareScores,
            Threshold threshold, Range range) {
        int newScoresStopCounter = getStopCounter(newScores, range);
        int compareScoresStopCounter = getStopCounter(compareScores, range);

        Double newTrend = calculateMeanTrend(newScores, newScoresStopCounter);
        Double compareTrend = calculateMeanTrend(compareScores, compareScoresStopCounter);

        return getDifference(newTrend, compareTrend, threshold);
    }

    // returns average mean after calculating mean at each point in the list
    // if trend = NONE : returns mean of list
    public static Double calculateMeanTrend(List<Double> scores, int stopCounter) {
        int size = scores.size();
        if (size < 2) {
            return 0.0;
        } else {
            if (stopCounter == size) {
                return calculateMean(scores.subList(0, size));
            } else {
                List<Double> means = new ArrayList<>();
                for (int i = size; i > stopCounter; i--) {
                    means.add(calculateMean(scores.subList(stopCounter, i)));
                }
                return calculateMean(means);
            }
        }
    }

    public static Double compareSD(List<Double> newScores, List<Double> compareScores, Threshold threshold,
            Range range) {
        int newScoresStopCounter = getStopCounter(newScores, range);
        int compareScoresStopCounter = getStopCounter(compareScores, range);

        Double newTrend = calculateSDTrend(newScores, newScoresStopCounter);
        Double compareTrend = calculateSDTrend(compareScores, compareScoresStopCounter);

        return getDifference(newTrend, compareTrend, threshold);
    }

    // returns average deviation after calculating SD at each point in list
    // if trend = NONE : returns SD of list
    public static Double calculateSDTrend(List<Double> scores, int stopCounter) {
        int size = scores.size();
        if (size < 2) {
            return 0.0;
        } else {
            if (stopCounter == size) {
                return calculateSD(scores.subList(0, size));
            } else {
                List<Double> deviations = new ArrayList<>();
                for (int i = size; i > stopCounter; i--) {
                    deviations.add(calculateSD(scores.subList(stopCounter, i)));
                }
                return calculateMean(deviations);
            }
        }
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
        List<Double> temp = new ArrayList<>();

        for (Double score : scores) {
            temp.add(Math.pow(score - mean, 2));
        }

        return Math.sqrt(calculateMean(temp));
    }

    private static Double calculatePercentChange(Double newTrend, Double compareTrend) {
        return 100 * ((newTrend - compareTrend) / compareTrend);
    }

    private static int getStopCounter(List<Double> scores, Range range) {
        int stopCounter = scores.size();

        switch (range) {
        case ALL_VALUES:
            stopCounter = 0;
            break;
        }

        return stopCounter;
    }

    public static enum Method {
        MEAN, DELTA, SD
    }

    public static enum Scope {
        WITHIN, BETWEEN
    }

    public static enum Range {
        ALL_VALUES, LAST
    }

    public static enum Threshold {
        PERCENT_CHANGE, GREATER
    }
}
