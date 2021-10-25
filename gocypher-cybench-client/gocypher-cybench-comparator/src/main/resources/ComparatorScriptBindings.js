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
	return Comparisons.calculateSD(scores);
}

function calculatePercentChange(newTrend, compareTrend) {
	return Comparisons.calculatePercentChange(newTrend, compareTrend);
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