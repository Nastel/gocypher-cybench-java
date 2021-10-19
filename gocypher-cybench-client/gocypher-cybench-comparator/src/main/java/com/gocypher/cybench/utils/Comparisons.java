package com.gocypher.cybench.utils;

import java.util.ArrayList;
import java.util.List;

public final class Comparisons {

	private Comparisons() {
	}

	public static Double compareSD(List<Double> compareScores, Double recentScore, Range range, Threshold threshold,
			Double comparePercentage) {

		int stopCounter = compareScores.size();

		switch (range) {
		case ALL_VALUES:
			stopCounter = 0;
		case LAST_5:
			stopCounter -= 5;
		case LAST_VALUE:
			stopCounter = compareScores.size();
		}
		Double previousSD = (calculateSD(compareScores, stopCounter));
		compareScores.add(recentScore);
		Double newSD = (calculateSD(compareScores, stopCounter));

		switch (threshold) {
		case GREATER:
			comparePercentage = (Double) null;
			return newSD - previousSD;
		case PERCENT_CHANGE:
			return calculatePerChange(newSD, previousSD);
		}
		return newSD;
	}

	public static Double compareMean(List<Double> compareScores, Double recentScore, Range range, Threshold threshold,
			Double comparePercentage) {
		Double average = 0.0;
		int stopCounter = compareScores.size();
		int totalCounter = 0;

		switch (range) {
		case ALL_VALUES:
			stopCounter = 0;
			break;
		case LAST_5:
			stopCounter -= 5;
			break;
		case LAST_VALUE:
			stopCounter = compareScores.size();
			break;

		}

		for (int scoreIndex = compareScores.size(); scoreIndex >= stopCounter; scoreIndex--, totalCounter++) {
			average += compareScores.get(scoreIndex);

		}
		average /= (totalCounter);

		switch (threshold) {
		case GREATER:
			comparePercentage = (Double) null;
			return (recentScore - average);
		case PERCENT_CHANGE:
			return (calculatePerChange(average, recentScore));
		}
		return average;
	}

	public static Double compareDelta(List<Double> compareScores, Double recentScore, Range range, Threshold threshold,
			Double comparePercentage) {
		int stopCounter = compareScores.size();
		Double lastScoreFromPrevious = compareScores.get(compareScores.size() - 1); // gets the latest score
		/*
		 * This switch case isn't used yet, as not sure how Delta should be compared
		 * with 3 or more scores.
		 * 
		 * Possibly: log/print the change in delta that occurred in each test? Compare
		 * delta of newest score and an older, specific score? (e.g. return
		 * scores.get(totalScores-1) <= scores.get(stopCounter);)
		 */
		switch (range) {
		case ALL_VALUES:
			stopCounter = 0;
		case LAST_5:
			stopCounter -= 5;
		case LAST_VALUE:
			stopCounter = compareScores.size();
		}

		switch (threshold) {
		case GREATER:
			comparePercentage = (Double) null;
			return (recentScore - lastScoreFromPrevious);
		case PERCENT_CHANGE:
			return calculatePerChange(recentScore, lastScoreFromPrevious);
		}

		return (recentScore - compareScores.get(compareScores.size() - 1));
	}

	/*
	 * NOTE: Not sure if 'calculate5MA' is still necessary, as it can be achieved by
	 * calling compareMean with a LAST_5 range
	 * 
	 * 5-day moving average calculator: public static Double
	 * calculate5MA(List<Double> scores, int totalScores) { Double average = 0; for
	 * (int scoreIndex = 0; scoreIndex < -5; scoreIndex++) { average +=
	 * scores.get(scoreIndex); } average /= 5; return average; }
	 * 
	 * 5-day moving average comparison (OLD): public static boolean
	 * compare5MA(List<Double> scores, int totalScores) { return
	 * calculate5MA(scores, totalScores = -1) <= calculate5MA(scores, totalScores);
	 * }
	 */

	private static Double calculateSD(List<Double> scores, int stopCounter) {
		int numCounter = 0;
		Double total = 0.0, average, sDeviate = 0.0;
		List<Double> tempScores = new ArrayList<>();

		for (int i = scores.size()-1; i >= stopCounter; i--) {
			total += scores.get(i);
			tempScores.add(scores.get(i));
			numCounter++;
		}
		average = total / numCounter;
		
		for (Double score : tempScores) {
			sDeviate += Math.pow(score - average, 2);
		}
		return Math.sqrt(sDeviate/numCounter);
	}

	// percent change grabbing directly from list
	private static Double calculatePerChange(List<Double> scores, Double newestScore) {
		Double latestScore = scores.get(scores.size() - 1);
		return 100 * ((newestScore - latestScore) / latestScore);
	}

	// percent change if you already have the newest and previous score.
	private static Double calculatePerChange(Double previousScore, Double newestScore) {
		return 100 * ((newestScore - previousScore) / previousScore);
	}

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
