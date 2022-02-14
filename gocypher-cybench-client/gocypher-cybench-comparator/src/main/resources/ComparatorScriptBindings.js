/*
 * Copyright (C) 2020-2022, K2N.IO.
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


// returns List<ComparedBenchmark>
// params {ComparedBenchmark, String (optional), String (optional)}
function getBenchmarksToCompareAgainst(benchmarkToCompare, compareVersion, range) {
    comparisonConfig = new ComparisonConfig(configMap);
    benchmarkToCompare.setComparisonConfig(comparisonConfig);
    if (!compareVersion) {
        compareVersion = configMap.get(ConfigHandling.COMPARE_VERSION);
    } else {
        comparisonConfig.getComparisonConfig().setCompareVersion(compareVersion);
    }
    if (!range) {
        range = configMap.get(ConfigHandling.RANGE);
    } else {
        comparisonConfig.setRange(range);
    }
    return CompareBenchmarks.getBenchmarksToCompareAgainst(benchmarkToCompare);
}


// returns List<Double> list of scores
// params: {List<ComparedBenchmark>}
function getBenchmarkScores(benchmarks) {
    return Comparisons.extractScoresFromComparedBenchmarkList(benchmarks);
}

// returns CompareState (ENUM representing PASS, FAIL, SKIP)
// params: {ComparedBenchmark, List<ComparedBenchmark>}
function runComparison(benchmarkToCompare, benchmarksToCompareAgainst) {
    return Comparisons.runSingleComparison(benchmarkToCompare, benchmarksToCompareAgainst)
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

// returns: deviations from a mean
// params: {Double, Double, Double}
function calculateDeviationsFromMean(score, compareMean, compareStandardDeviation) {
    return Comparisons.calculateDeviationsFromMean(score, compareMean, compareStandardDeviation);
}

// returns percent change
// params: {Double, Double}
function calculatePercentChange(newScore, compareScore) {
    return Comparisons.calculatePercentChange(newScore, compareScore);
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