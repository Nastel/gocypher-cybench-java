package com.gocypher.cybench.utils;

import java.util.ArrayList;
import java.util.List;

public final class Comparisons {

	private Comparisons() {
	}

	public static Double compareDelta(List<Double> newScores, List<Double> compareScores, Range range,
			Threshold threshold) {
		int newScoresStopCounter = getStopCounter(newScores, range);
		int compareScoresStopCounter = getStopCounter(compareScores, range);

		Double newTrend = calculateDeltaTrend(newScores, newScoresStopCounter);
		Double compareTrend = calculateDeltaTrend(compareScores, compareScoresStopCounter);

		switch (threshold) {
		case GREATER:
			return newTrend - compareTrend;
		case PERCENT_CHANGE:
			return calculatePercentChange(newTrend, compareTrend);
		default:
			return null;
		}
	}

	public static Double calculateDeltaTrend(List<Double> scores, int stopCounter) {
		int size = scores.size();
		if (size < 2) {
			return 0.0;
		} else {
			if (stopCounter == size) {
				return scores.get(size - 1) - scores.get(size - 2);
			} else {
				Double average = 0.0;
				int count = 0;
				for (int i = size - 1; i > stopCounter; i--) {
					average += (scores.get(i) - scores.get(i - 1));
					count++;
				}
				return average / count;
			}
		}
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
		case LAST_5:
			stopCounter -= 5;
			break;
		}

		return stopCounter;
	}

//    public static Double compareSD(List<Double> compareScores, Double recentScore, Range range, Threshold threshold) {
//        int stopCounter = getStopCounter(compareScores, range);
//        Double previousSD = (calculateSD(compareScores, stopCounter));
//        compareScores.add(recentScore);
//        Double newSD = (calculateSD(compareScores, stopCounter));
//
//        switch (threshold) {
//        case GREATER:
//            return newSD - previousSD;
//        case PERCENT_CHANGE:
//            return calculatePercentChange(newSD, previousSD);
//        }
//        return newSD;
//    }
//
//    
//
//    public static Double compareMean(List<Double> compareScores, Double recentScore, Range range, Threshold threshold) {
//        Double average = 0.0;
//        int stopCounter = getStopCounter(compareScores, range);
//        int totalCounter = 0;
//
//        for (int scoreIndex = compareScores.size()-1; scoreIndex >= stopCounter; scoreIndex--, totalCounter++) {
//            average += compareScores.get(scoreIndex);
//        }
//        if (totalCounter != 0) {
//            average /= (totalCounter);
//        }
//        switch (threshold) {
//        case GREATER:
//            return (recentScore - average);
//        case PERCENT_CHANGE:
//            return (calculatePercentChange(average, recentScore));
//        }
//        return average;
//    }
//
//    
//
//    /**
//     * NOTE: Not sure if 'calculate5MA' is still necessary, as it can be achieved by calling compareMean with a LAST_5
//     * range
//     * 
//     * 5-day moving average calculator:
//     * 
//     * <pre>
//     * public static double calculate5MA(List&lt;Double&gt; scores, int totalScores) {
//     *     double average = 0;
//     *     for (int scoreIndex = 0; scoreIndex < -5; scoreIndex++) {
//     *         average += scores.get(scoreIndex);
//     *     }
//     *     average /= 5;
//     *     return average;
//     * }
//     * </pre>
//     * 
//     * 5-day moving average comparison (OLD):
//     * 
//     * <pre>
//     * public static boolean compare5MA(List&lt;Double&gt; scores, int totalScores) {
//     *     return calculate5MA(scores, totalScores = -1) <= calculate5MA(scores, totalScores);
//     * }
//     * </pre>
//     */
//
//    private static Double calculateSD(List<Double> scores, int stopCounter) {
//        int numCounter = 0;
//        double total = 0.0, average, sDeviate = 0.0;
//        List<Double> tempScores = new ArrayList<>();
//
//        for (int i = scores.size() - 1; i >= stopCounter; i--) {
//            total += scores.get(i);
//            tempScores.add(scores.get(i));
//            numCounter++;
//        }
//        average = total / numCounter;
//
//        for (Double score : tempScores) {
//            sDeviate += Math.pow(score - average, 2);
//        }
//        return Math.sqrt(sDeviate / numCounter);
//    }
//
//    // percent change grabbing directly from list
//    private static Double calculatePercentChange(List<Double> scores, Double newestScore) {
//        Double latestScore = scores.get(scores.size() - 1);
//        return 100 * ((newestScore - latestScore) / latestScore);
//    }
//

	public static enum Method {
		MEAN, DELTA, SD
	}

	public static enum Scope {
		WITHIN, BETWEEN
	}

	public static enum Range {
		ALL_VALUES, LAST_VALUE, LAST_5
	}

	public static enum Threshold {
		PERCENT_CHANGE, GREATER
	}
}
