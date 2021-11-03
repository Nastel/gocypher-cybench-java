// help log the comparison being ran
// params: {String, String}
function logComparison(benchmarkName, mode) {
    return Comparisons.logComparison(logConfigs, benchmarkName, mode);
}

// depending on compare method set (DELTA / SD), returns (change in value within/between versions OR deviations from mean within/between versions)
// params: {List<Double>, List<Double> (optional)}
function compareScores(currentVersionScores, compareVersionScores) {
    if (!compareVersionScores) {
        compareVersionScores = new ArrayList(currentVersionScores);
        // remove new score to have a comparative list
        compareVersionScores.remove(currentVersionScores.size() - 1);
    }
    if (method === Comparisons.Method.DELTA) {
        return Comparisons.compareWithDelta(currentVersionScores, compareVersionScores, threshold, range);
    } else {
        return Comparisons.compareWithSD(currentVersionScores, compareVersionScores, range);
    }
}

// returns change in value within/between versions
// params: {Comparisons.Threshold, String, List<Double>, List<Double> (optional)}
function compareDelta(threshold, range, currentVersionScores, compareVersionScores) {
    if (!compareVersionScores) {
        compareVersionScores = new ArrayList(currentVersionScores);
        // remove new score to have a comparative list
        compareVersionScores.remove(currentVersionScores.size() - 1);
    }
    return Comparisons.compareWithDelta(currentVersionScores, compareVersionScores, threshold, range);
}

// returns deviations from mean within/between versions
// params: {String, List<Double>, List<Double> (optional)}
function compareSD(range, currentVersionScores, compareVersionScores) {
    if (!compareVersionScores) {
        compareVersionScores = new ArrayList(currentVersionScores);
        // remove new score to have a comparative list
        compareVersionScores.remove(currentVersionScores.size() - 1);
    }
    return Comparisons.compareWithSD(currentVersionScores, compareVersionScores, range);
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

// returns all the recently benchmarked modes within the current report by the passed fingerprint
// List represents list of modes 
// params: {String, String}
function getRecentlyBenchmarkedModes(benchmarkFingerprint, currentVersion) {
    return new ArrayList(myBenchmarks.get(benchmarkFingerprint).get(currentVersion).keySet());
}

// depending on deviationsAllowed test, percentChangeAllowed test, or GREATER test, returns boolean representing a passed test
// params: {Double}
function passAssertion(val) {
    if (method === Comparisons.Method.SD) {
        // val = deviationsFromMean
        return Comparisons.passAssertionDeviation(val, deviationsAllowed);
    } else if (threshold === Comparisons.Threshold.PERCENT_CHANGE) {
        // val = percentChange
        return Comparisons.passAssertionPercentage(percentChange, percentageAllowed);
    } else {
        // val = simple delta value
        return Comparisons.passAssertionPositive(val);
    }
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


// returns current version of the fingerprint
// params: {String}
function getCurrentVersion(fingerprint) {
    return Requests.getCurrentVersion(fingerprint);
}

// returns previous version of the fingerprint
// params: {String}
function getPreviousVersion(fingerprint) {
    return Requests.getPreviousVersion(fingerprint);
}