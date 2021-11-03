/*
 * Copyright (C) 2020-2021, K2N.IO.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 */

// depending on compare method set (DELTA / SD), returns (change in value within/between versions OR deviations from mean within/between versions)
// params: {String, String, List<Double>, List<Double> (optional)}
function compareScores(benchmarkName, currentVersion, benchmarkMode, currentVersionScores, compareVersionScores) {
    if (!compareVersionScores) {
        compareVersionScores = new ArrayList(currentVersionScores);
        // remove new score to have a comparative list
        compareVersionScores.remove(currentVersionScores.size() - 1);
    }
	return Comparisons.compareScores(logConfigs,  benchmarkName, currentVersion, benchmarkMode, currentVersionScores, compareVersionScores);
}

// returns change in value within/between versions
// params: {String, String, Comparisons.Threshold, String, List<Double>, List<Double> (optional)}
function compareDelta(benchmarkName, currentVersion, benchmarkMode, threshold, range, currentVersionScores, compareVersionScores) {
	Comparisons.logComparison(logConfigs, benchmarkName, benchmarkVersion, benchmarkMode, Comparisons.Method.DELTA, range, threshold);
    if (!compareVersionScores) {
        compareVersionScores = new ArrayList(currentVersionScores);
        // remove new score to have a comparative list
        compareVersionScores.remove(currentVersionScores.size() - 1);
    }
    return Comparisons.compareWithDelta(currentVersionScores, compareVersionScores, range, threshold);
}

// returns deviations from mean within/between versions
// params: {String, String, String, List<Double>, List<Double> (optional)}
function compareSD(benchmarkName, currentVersion, benchmarkMode, range, currentVersionScores, compareVersionScores) {
	Comparisons.logComparison(logConfigs, benchmarkName, benchmarkVersion, benchmarkMode, Comparisons.Method.SD, range, null);
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