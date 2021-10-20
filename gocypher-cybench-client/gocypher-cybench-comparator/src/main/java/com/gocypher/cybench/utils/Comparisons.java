package com.gocypher.cybench.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Comparisons {

	private Comparisons() {
	}

	public static Map<String, Double> compareDelta(List<Double> newScores, List<Double> compareScores, Threshold threshold,
			Trend trend) {
		int newScoresStopCounter = getStopCounter(newScores, trend);
		int compareScoresStopCounter = getStopCounter(compareScores, trend);

		Double newTrend = calculateDeltaTrend(newScores, newScoresStopCounter);
		Double compareTrend = calculateDeltaTrend(compareScores, compareScoresStopCounter);

		Map<String, Double> differenceData = new HashMap<>();
		differenceData.put("benchmarkTrendScore", newTrend);
		differenceData.put("compareTrendScore", compareTrend);
		Double difference = 0.0;
		
		switch (threshold) {
		case GREATER:
			difference = newTrend - compareTrend;
			break;
		case PERCENT_CHANGE:
			difference = calculatePercentChange(newTrend, compareTrend);
			break;
		}
		System.out.println(newTrend+" "+compareTrend+" "+difference +" CALC: "+(newTrend-compareTrend));
		
		differenceData.put("difference", difference);
		return differenceData;
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
	
	public static Map<String, Double> compareMean(List<Double> newScores, List<Double> compareScores, Threshold threshold,
			Trend trend) {
		int newScoresStopCounter = getStopCounter(newScores, trend);
		int compareScoresStopCounter = getStopCounter(compareScores, trend);

		Double newTrend = calculateMeanTrend(newScores, newScoresStopCounter);
		Double compareTrend = calculateMeanTrend(compareScores, compareScoresStopCounter);
		
		Map<String, Double> differenceData = new HashMap<>();
		differenceData.put("benchmarkTrendScore", newTrend);
		differenceData.put("compareTrendScore", compareTrend);
		Double difference = 0.0;
		
		switch (threshold) {
		case GREATER:
			difference = newTrend - compareTrend;
			break;
		case PERCENT_CHANGE:
			difference = calculatePercentChange(newTrend, compareTrend);
			break;
		}
		
		differenceData.put("difference", difference);
		return differenceData;
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
	
	public static Map<String, Double> compareSD(List<Double> newScores, List<Double> compareScores, Threshold threshold,
			Trend trend) {
		int newScoresStopCounter = getStopCounter(newScores, trend);
		int compareScoresStopCounter = getStopCounter(compareScores, trend);

		Double newTrend = calculateSDTrend(newScores, newScoresStopCounter);
		Double compareTrend = calculateSDTrend(compareScores, compareScoresStopCounter);
		
		Map<String, Double> differenceData = new HashMap<>();
		differenceData.put("benchmarkTrendScore", newTrend);
		differenceData.put("compareTrendScore", compareTrend);
		Double difference = 0.0;
		
		switch (threshold) {
		case GREATER:
			difference = newTrend - compareTrend;
			break;
		case PERCENT_CHANGE:
			difference = calculatePercentChange(newTrend, compareTrend);
			break;
		}
		
		differenceData.put("difference", difference);
		return differenceData;
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
		for(Double score : scores) {
			average += score;
		}
		return average/scores.size();
	}
	
	public static Double calculateSD(List<Double> scores) {
		Double mean = calculateMean(scores);
		List<Double> temp = new ArrayList<>();
		
		for(Double score : scores) {
			temp.add(Math.pow(score - mean, 2));
		}
		
		return Math.sqrt(calculateMean(temp));
	}

	private static Double calculatePercentChange(Double newTrend, Double compareTrend) {
		return 100 * ((newTrend - compareTrend) / compareTrend);
	}

	private static int getStopCounter(List<Double> scores, Trend trend) {
		int stopCounter = scores.size();

		switch (trend) {
		case ALL_VALUES:
			stopCounter = 0;
			break;
		case LAST_5:
			stopCounter -= 5;
			break;
		}

		return stopCounter;
	}

//	public static Double compareSD(List<Double> compareScores, Double recentScore, Range range, Threshold threshold) {
//        int stopCounter = getStopCounter(compareScores, range);
//        Double previousSD = (calculateSD(compareScores, stopCounter));
//        compareScores.add(recentScore);
//        Double newSD = (calculateSD(compareScores, stopCounter));
//
//        switch (threshold) {
//        case GREATER:
//            return newSD - previousSD;
//        case PERCENT_CHANGE:
//            return calculatePerChange(newSD, previousSD);
//        }
//        return newSD;
//    }
//
//    private static int getStopCounter(List<Double> compareScores, Range range) {
//        int stopCounter = compareScores.size();
//
//        /*
//         * This switch case isn't used yet, as not sure how Delta should be compared with 3 or more scores.
//         *
//         * Possibly: log/print the change in delta that occurred in each test? Compare delta of the newest score and an
//         * older, specific score? (e.g. return scores.get(totalScores-1) <= scores.get(stopCounter);)
//         */
//        switch (range) {
//        case ALL_VALUES:
//            stopCounter = 0;
//            break;
//        case LAST_5:
//            stopCounter -= 5;
//            break;
//        case LAST_VALUE:
//        default:
//            break;
//        }
//
//        return stopCounter;
//    }
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
//            return (calculatePerChange(average, recentScore));
//        }
//        return average;
//    }
//
//    public static Double compareDelta(List<Double> compareScores, Double recentScore, Range range, Threshold threshold) {
//        int stopCounter = getStopCounter(compareScores, range);
//        Double lastScoreFromPrevious = compareScores.get(compareScores.size() - 1); // gets the latest score
//
//        switch (threshold) {
//        case GREATER:
//            return (recentScore - lastScoreFromPrevious);
//        case PERCENT_CHANGE:
//            return calculatePerChange(recentScore, lastScoreFromPrevious);
//        }
//
//        return (recentScore - compareScores.get(compareScores.size() - 1));
//    }
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
//    private static Double calculatePerChange(List<Double> scores, Double newestScore) {
//        Double latestScore = scores.get(scores.size() - 1);
//        return 100 * ((newestScore - latestScore) / latestScore);
//    }
//
//    // percent change if you already have the newest and previous score.
//    private static Double calculatePerChange(Double previousScore, Double newestScore) {
//        return 100 * ((newestScore - previousScore) / previousScore);
//    }
//	
//	public static enum Range {
//        ALL_VALUES, LAST_VALUE, LAST_5
//    }

	public static enum Method {
		MEAN, DELTA, SD
	}

	public static enum Scope {
		WITHIN, BETWEEN
	}

	public static enum Trend {
		ALL_VALUES, LAST_5, NONE
	}

	public static enum Threshold {
		PERCENT_CHANGE, GREATER
	}
}
