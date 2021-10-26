// return change in value within a version
// params: {List<Double>, Comparisons.Threshold, String}
function deltaCompareWithinVersion(withinVersionScores, threshold, range) {
	return Comparisons.compareWithDelta(withinVersionScores, threshold, range);
}

// same as above but with extra logging
// params: {List<Double>, Comparisons.Threshold, String, String, String, String}
function deltaCompareWithinVersionWithLogging(withinVersionScores, threshold, range, benchmarkName, mode, currentVersion) {
	return Comparisons.compareWithDelta(withinVersionScores, threshold, range, benchmarkName, mode, currentVersion);
}


// returns deviations from mean within a version
// params: {List<Double>, String}
function sdCompareWithinVersion(withinVersionScores, range) {
	return Comparisons.compareWithSD(withinVersionScores, range);
}

// same as above but with extra logging
// params: {List<Double>, String, String, String, String}
function sdCompareWithinVersionWithLogging(withinVersionScores, range, benchmarkName, mode, currentVersion) {
	return Comparisons.compareWithSD(withinVersionScores, range, benchmarkName, mode, currentVersion);
}


// returns change in value between versions
// params: {List<Double>, List<Double>, Comparisons.Threshold, String}
function deltaCompareBetweenVersions(currentVersionScores, compareVersionScores, threshold, range) {
	return Comparisons.compareWithDelta(currentVersionScores, compareVersionScores, threshold, range);
}

// returns change in value between versions
// params: {List<Double>, List<Double>, Comparisons.Threshold, String, String, String, String, String}
function deltaCompareBetweenVersionsWithLogging(currentVersionScores, compareVersionScores, threshold, range, benchmarkName, mode, currentVersion, previousVersion) {
	return Comparisons.compareWithDelta(currentVersionScores, compareVersionScores, threshold, range, benchmarkName, mode, currentVersion, previousVersion);
}


// returns deviations from mean of compareVersion
// params: {List<Double>, List<Double>, String}
function sdCompareBetweenVersions(currentVersionScores, compareVersionScores, range) {
	return Comparisons.compareWithSD(currentVersionScores, compareVersionScores, range);
}

// returns deviations from mean of compareVersion
// params: {List<Double>, List<Double>, String, String, String, String, String}
function sdCompareBetweenVersionsWithLogging(currentVersionScores, compareVersionScores, range, benchmarkName, mode, currentVersion, previousVersion) {
	return Comparisons.compareWithSD(currentVersionScores, compareVersionScores, range, benchmarkName, mode, currentVersion, previousVersion);
}


// return change in value
// params: {Double, Double, Comparisons.Threshold}
function calculateDelta(newScore, compareValue, threshold) {
	return Comparisons.calculateDelta(newScore, compareValue, threshold);
}

// returns mean
// params: {List<Double>}
function calculateMean(scores) {
	return Comparisons.calculateMean(scores);
}

// return standard deviation
// params: {List<Double>}
function calculateSD(scores) {
	return Comparisons.calculateSD(scores);
}

// returns percent change
// params: {Double, Double}
function calculatePercentChange(newScore, compareScore) {
	return Comparisons.calculatePercentChange(newScore, compareScore);
}


// return Map<String, String> that maps the method CyBench fingerprints in the report to the method names
// params: {String reportPath}
function getFingerprintsFromReport(report, accessToken) {
	return Requests.getFingerprintsFromReport(report, accessToken);
}

// runs a fetch command in the background to prepare benchmark data to be accesed with the get methods below
// params: {String, String, String}
function fetchBenchmarks(name, benchmarkFingerprint, accessToken) {
	return Requests.getInstance().fetchBenchmarks(name, benchmarkFingerprint, accessToken);
}

// returns Map<String, Map<String, Map<String, List<Double>>>> 
// Map represents <Benchmark Fingerprint : <Version : <Mode : <List of Scores in Test Order>>>>
function getAllBenchmarks() {
	return Requests.getBenchmarks();
}

// returns Map<String, Map<String, List<Double>>>
// Map represents <Version : <Mode : <List of Scores in Test Order>>>
// params: {String}
function getBenchmarksByFingerprint(benchmarkFingerprint) {
	return Requests.getBenchmarks(benchmarkFingerprint);
}

// returns Map<String, List<Double>>
// Map represents <Mode : <List of Scores in Test Order>>
// params: {String}
function getBenchmarksByVersion(benchmarkFingerprint, version) {
	return Requests.getBenchmarks(benchmarkFingerprint, version);
}

// returns List<Double>
// List represents <List of Scores in Test Order>
// params: {String, String, String}
function getBenchmarksByMode(benchmarkFingerprint, version, mode) {
	return Requests.getBenchmarks(benchmarkFingerprint, version, mode);
}

// returns the most current version benchmarked with
function getCurrentVersion() {
	return Requests.getCurrentVersion();
}

// returns the previous version to the current version found in your benchmarks
function getPreviousVersion() {
	return Requests.getPreviousVersion();
}

// returns boolean that represents whether or not deviationsFromMean is within deviationsAllowed
// params: {Double, Double}
function passAssertionDeviation(deviationsFromMean, deviationsAllowed) {
	return Comparisons.passAssertionDeviation(deviationsFromMean, deviationsAllowed);
}

// returns boolean that represents whether or not percentChange is within percentageAllowed
// params: {Double, Double}
function passAssertionPercentage(percentChange, percentageAllowed) {
	return Comparisons.passAssertionPercentage(percentChange, percentageAllowed);
}

// returns boolean that represents whether or not val is greater than 0
// params: {Double}
function passAssertionPositive(val) {
	return Comparisons.passAssertionPositive(val);
}