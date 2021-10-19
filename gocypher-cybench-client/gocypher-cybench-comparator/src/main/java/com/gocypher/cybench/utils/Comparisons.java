package com.gocypher.cybench.utils;

import java.util.List;

public final class Comparisons {

	private Comparisons() {
	}

	public static boolean compareSD(List<Double> scores, int totalScores, double comparePercentage, Range range,
			Threshold threshold) {
		int stopCounter = scores.size();
		List<Double> tempScores = null, tempList = null;
		switch (range) {
		case ALL_VALUES:
			stopCounter = 0;
		case LAST_5:
			stopCounter -= 6;
		case LAST_VALUE:
			stopCounter -= 2;
		}

		switch (threshold) {
		case GREATER:

			if (calculateSD(scores, stopCounter) > (calculateSD(tempList, stopCounter))) {
				return true;
			} else {
				return false;
			}
		case PERCENT_CHANGE_ALLOWED:
			if (calculatePerChange(calculateSD(scores, stopCounter),
					(calculateSD(tempList, stopCounter))) < comparePercentage) {
				return false;
			} else {
				return true;
			}
		}
		return true;
	}

	private static double calculateSD(List<Double> scores, int stopCounter) {
		int numCounter = 0;
		double total = 0, average, sDeviate = 0;
		List<Double> tempScores = null;
		for (int i = scores.size(); --i >= stopCounter; numCounter++) {

			total += scores.get(i);
			tempScores.add(scores.get(i));
		}
		average = total / numCounter;

		for (double score : tempScores) {
			sDeviate = Math.pow(score - average, 2);
		}
		return sDeviate;
	}

	public static boolean compareMean(List<Double> scores, int totalScores, double comparePercentage, Range range,
			Threshold threshold) {
		double average = 0;
		double newestScore = scores.get(scores.size() - 1);
		int stopCounter = scores.size();
		int totalCounter = 0;
		switch (range) {
		case ALL_VALUES:
			stopCounter = 0;
			break;
		case LAST_VALUE:
			stopCounter -= 2;
			break;
		case LAST_5:
			stopCounter -= 6;
			break;
		}

		for (int scoreIndex = scores.size(); scoreIndex >= stopCounter; scoreIndex--, totalCounter++) {
			average += scores.get(scoreIndex);

		}
		average /= (totalCounter);
		switch (threshold) {
		case GREATER:
			if (newestScore > average) {
				return true;
			} else {
				return false;
			}
		case PERCENT_CHANGE_ALLOWED:
			if (calculatePerChange(average, newestScore) < comparePercentage) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	public static boolean compareDelta(List<Double> scores, int totalScores, double comparePercentage, Range range,
			Threshold threshold) {
		int stopCounter = scores.size();
		/* This switch case isn't used yet, as not sure how Delta should be 
		 * compared with 3 or more scores.
		 * 
		 * Possibly: log/print the change in delta that occurred in each test? Compare delta of newest score and an older, specific
		 * score? (e.g. return scores.get(totalScores-1) <= scores.get(stopCounter);)
		 */
		switch (range) {
		case ALL_VALUES:
			stopCounter = 0;
		case LAST_5:
			stopCounter -= 6;
		case LAST_VALUE:
			stopCounter -= 2;
		}

		switch (threshold) {
		case GREATER:
			return scores.get(totalScores - 1) <= scores.get(totalScores - 2);
		case PERCENT_CHANGE_ALLOWED:
			return calculatePerChange(scores.get(totalScores - 1), scores.get(totalScores - 2)) < comparePercentage;
		}

		return scores.get(totalScores - 1) <= scores.get(totalScores - 2);
	}

	/*
	 * NOTE: Not sure if 'calculate5MA' is still necessary, as it can be achieved by
	 * calling compareMean with a LAST_5 range
	 * 
	 * 5-day moving average calculator: public static double
	 * calculate5MA(List<Double> scores, int totalScores) { double average = 0; for
	 * (int scoreIndex = 0; scoreIndex < -5; scoreIndex++) { average +=
	 * scores.get(scoreIndex); } average /= 5; return average; }
	 * 
	 * 5-day moving average comparison (OLD): public static boolean
	 * compare5MA(List<Double> scores, int totalScores) { return
	 * calculate5MA(scores, totalScores = -1) <= calculate5MA(scores, totalScores);
	 * }
	 */

	// percent change grabbing directly from list
	public static double calculatePerChange(List<Double> scores, double newestScore) {
		double latestScore = scores.get(scores.size() - 1);
		return 100 * ((newestScore - latestScore) / latestScore);
	}

	// percent change if you already have the newest and previous score.
	public static double calculatePerChange(double previousScore, double newestScore) {
		return 100 * ((newestScore - previousScore) / previousScore);
	}

	public static enum Method {
		MEAN, DELTA, SD, MOVING_AVERAGE
	}

	public static enum Scope {
		WITHIN, BETWEEN
	}

	public static enum Range {
		ALL_VALUES, LAST_VALUE, LAST_5
	}

	public static enum Threshold {
		PERCENT_CHANGE_ALLOWED, GREATER
	}
}
