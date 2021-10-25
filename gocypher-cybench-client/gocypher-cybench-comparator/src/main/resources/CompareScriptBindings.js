function compareWithDelta(currentVersionScores, compareVersionScores, threshold, range) {
	return Comparisons.compareWithDelta(currentVersionScores, compareVersionScores, threshold, range);
}

function compareWithSD(currentVersionScores, compareVersionScores, threshold, range) {
	return Comparisons.compareWithSD(currentVersionScores, compareVersionScores, threshold, range);
}

function calculateDelta(newScore, compareValue, threshold) {
	return Comparisons.calculateDelta(newScore, compareValue, threshold);
}

function calculateMean(scores) {
	return Comparisons.calculateMean(scores);
}

function calculateSD(scores) {
	return Comparisons.calculateSD(scores, mean);
}

function calculatePercentChange(newTrend, compareTrend) {
	return Comparisons.calculatePercentChange(newTrend, compareTrend);
}