function deltaCompareWithinVersion(withinVersionScores, threshold, range) {
	return Comparisons.compareWithDelta(withinVersionScores, threshold, range);
}

function sdCompareWithinVersion(withinVersionScores, range) {
	return Comparisons.compareWithSD(withinVersionScores, range);
}

function deltaCompareBetweenVersions(currentVersionScores, compareVersionScores, threshold, range) {
	return Comparisons.compareWithDelta(currentVersionScores, compareVersionScores, threshold, range);
}

function sdCompareBetweenVersions(currentVersionScores, compareVersionScores, range) {
	return Comparisons.compareWithSD(currentVersionScores, compareVersionScores, range);
}

function calculateDelta(newScore, compareValue, threshold) {
	return Comparisons.calculateDelta(newScore, compareValue, threshold);
}

function calculateMean(scores) {
	return Comparisons.calculateMean(scores);
}

function calculateSD(scores) {
	return Comparisons.calculateSD(scores);
}

function calculatePercentChange(newScore, compareScore) {
	return Comparisons.calculatePercentChange(newScore, compareScore);
}

function getAllBenchmarks() {
	return Requests.getBenchmarks();
}

function getBenchmarksByFingerprint(benchmarkFingerprint) {
	return Requests.getBenchmarks(benchmarkFingerprint);
}

function getBenchmarksByVersion(benchmarkFingerprint, version) {
	return Requests.getBenchmarks(benchmarkFingerprint, version);
}

function getBenchmarksByMode(benchmarkFingerprint, version, mode) {
	return Requests.getBenchmarks(benchmarkFingerprint, version, mode);
}

function fetchBenchmarks(name, benchmarkFingerprint, accessToken) {
	return Requests.getInstance().fetchBenchmarks(name, benchmarkFingerprint, accessToken);
}

function getFingerprintsFromReport(report) {
	return Requests.getFingerprintsFromReport(report);
}

function passAssertionDeviation(deviationsFromMean, deviationsAllowed) {
	return Comparisons.passAssertionDeviation(deviationsFromMean, deviationsAllowed);
}

function passAssertionPercentage(percentChange, percentageAllowed) {
	return Comparisons.passAssertionPercentage(percentChange, percentageAllowed);
}

function passAssertionPositive(val) {
	return Comparisons.passAssertionPositive(val);
}