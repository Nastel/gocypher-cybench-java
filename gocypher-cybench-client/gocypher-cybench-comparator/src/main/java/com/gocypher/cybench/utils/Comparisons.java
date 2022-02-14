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

package com.gocypher.cybench.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gocypher.cybench.CompareBenchmarks;
import com.gocypher.cybench.model.ComparedBenchmark;
import com.gocypher.cybench.model.ComparedBenchmark.CompareState;
import com.gocypher.cybench.model.ComparisonConfig;
import com.gocypher.cybench.model.ComparisonConfig.Method;
import com.gocypher.cybench.model.ComparisonConfig.Threshold;

public final class Comparisons {
    private static final Logger log = LoggerFactory.getLogger(Comparisons.class);

    private Comparisons() {
    }

    // TODO can maybe simplify by calling runSingleComparison
    public static Map<String, Object> runComparison(ComparisonConfig comparisonConfig,
            Map<String, Map<String, ComparedBenchmark>> benchmarksToCompare,
            Map<String, Map<String, List<ComparedBenchmark>>> benchmarksToCompareAgainst) {
        Map<String, Object> resultMap = new HashMap<>();

        Method method = comparisonConfig.getMethod();
        Threshold threshold = comparisonConfig.getThreshold();
        Double deviationsAllowed = comparisonConfig.getDeviationsAllowed();
        Double percentChangeAllowed = comparisonConfig.getPercentChangeAllowed();

        int totalComparedBenchmarks = 0;
        int totalPassedBenchmarks = 0;
        int totalFailedBenchmarks = 0;
        int totalSkippedBenchmarks = 0;
        List<ComparedBenchmark> comparedBenchmarks = new ArrayList<>();
        List<ComparedBenchmark> passedBenchmarks = new ArrayList<>();
        List<ComparedBenchmark> failedBenchmarks = new ArrayList<>();
        List<ComparedBenchmark> skippedBenchmarks = new ArrayList<>();

        for (Map.Entry<String, Map<String, ComparedBenchmark>> benchmarksToCompareEntry : benchmarksToCompare
                .entrySet()) {
            String fingerprint = benchmarksToCompareEntry.getKey();
            Map<String, ComparedBenchmark> benchmarksByMode = benchmarksToCompareEntry.getValue();
            for (Map.Entry<String, ComparedBenchmark> benchmarksByModeEntry : benchmarksByMode.entrySet()) {
                String mode = benchmarksByModeEntry.getKey();
                ComparedBenchmark benchmarkToCompare = benchmarksByModeEntry.getValue();
                Double score = benchmarkToCompare.getScore();

                if (benchmarksToCompareAgainst.containsKey(fingerprint)) {
                    if (benchmarksToCompareAgainst.get(fingerprint).containsKey(mode)) {
                        List<ComparedBenchmark> cbl = benchmarksToCompareAgainst.get(fingerprint).get(mode);

                        calculatePercentage(cbl, benchmarkToCompare, score);

                        CompareState compareState = findCompareState(method, threshold, benchmarkToCompare,
                                percentChangeAllowed, deviationsAllowed);

                        if (compareState == CompareState.PASS) {
                            totalPassedBenchmarks++;
                            benchmarkToCompare.setCompareState(CompareState.PASS);
                            passedBenchmarks.add(benchmarkToCompare);
                        } else if (compareState == CompareState.FAIL) {
                            totalFailedBenchmarks++;
                            benchmarkToCompare.setCompareState(CompareState.FAIL);
                            failedBenchmarks.add(benchmarkToCompare);
                        }
                    } else {
                        totalSkippedBenchmarks++;
                        benchmarkToCompare.setCompareState(CompareState.SKIP);
                        skippedBenchmarks.add(benchmarkToCompare);
                    }
                } else {
                    totalSkippedBenchmarks++;
                    benchmarkToCompare.setCompareState(CompareState.SKIP);
                    skippedBenchmarks.add(benchmarkToCompare);
                }

                totalComparedBenchmarks++;
                comparedBenchmarks.add(benchmarkToCompare);
            }
        }

        resultMap.put("totalComparedBenchmarks", totalComparedBenchmarks);
        resultMap.put("totalPassedBenchmarks", totalPassedBenchmarks);
        resultMap.put("totalFailedBenchmarks", totalFailedBenchmarks);
        resultMap.put("totalSkippedBenchmarks", totalSkippedBenchmarks);
        resultMap.put("comparedBenchmarks", comparedBenchmarks);
        resultMap.put("passedBenchmarks", passedBenchmarks);
        resultMap.put("failedBenchmarks", failedBenchmarks);
        resultMap.put("skippedBenchmarks", skippedBenchmarks);
        return resultMap;
    }

    private static void calculatePercentage(List<ComparedBenchmark> comparedBenchmarks,
            ComparedBenchmark benchmarkToCompare, Double score) {
        List<Double> compareScores = extractScoresFromComparedBenchmarkList(comparedBenchmarks);
        Double compareMean = calculateMean(compareScores);
        benchmarkToCompare.setCompareMean(compareMean);

        Double delta = score - compareMean;
        benchmarkToCompare.setDelta(delta);
        Double percentChange = calculatePercentChange(score, compareMean);
        benchmarkToCompare.setPercentChange(percentChange);
        Double compareSD = calculateSD(compareScores, compareMean);
        benchmarkToCompare.setCompareSD(compareSD);

        Double deviationsFromMean = calculateDeviationsFromMean(score, compareMean, compareSD);
        benchmarkToCompare.setDeviationsFromMean(deviationsFromMean);
    }

    private static CompareState findCompareState(Method method, Threshold threshold,
            ComparedBenchmark benchmarkToCompare, Double percentChangeAllowed, Double deviationsAllowed) {
        CompareState compareState = null;
        if (method.equals(Method.DELTA)) {
            if (threshold.equals(Threshold.GREATER)) {
                compareState = passAssertionPositive(benchmarkToCompare.getDelta());
            } else if (threshold.equals(Threshold.PERCENT_CHANGE)) {
                compareState = passAssertionPercentage(benchmarkToCompare.getPercentChange(), percentChangeAllowed);
            }
        } else if (method.equals(Method.SD)) {
            compareState = passAssertionDeviation(benchmarkToCompare.getDeviationsFromMean(), deviationsAllowed);
        }

        return compareState;
    }

    public static CompareState runSingleComparison(ComparedBenchmark benchmarkToCompare,
            List<ComparedBenchmark> benchmarksToCompareAgainst) {

        CompareState state = null;
        ComparisonConfig comparisonConfig = benchmarkToCompare.getComparisonConfig();
        Method method = comparisonConfig.getMethod();
        Threshold threshold = comparisonConfig.getThreshold();
        Double deviationsAllowed = comparisonConfig.getDeviationsAllowed();
        Double percentChangeAllowed = comparisonConfig.getPercentChangeAllowed();

        if (!benchmarksToCompareAgainst.isEmpty()) {
            Double score = benchmarkToCompare.getScore();

            calculatePercentage(benchmarksToCompareAgainst, benchmarkToCompare, score);

            state = findCompareState(method, threshold, benchmarkToCompare, percentChangeAllowed, deviationsAllowed);
        } else {
            state = CompareState.SKIP;
        }

        switch (state) {
        case PASS: {
            CompareBenchmarks.passBenchmark(benchmarkToCompare);
            break;
        }
        case FAIL: {
            CompareBenchmarks.failBenchmark(benchmarkToCompare);
            break;
        }
        case SKIP: {
            CompareBenchmarks.skipBenchmark(benchmarkToCompare, "Benchmark not found in compared to reports");
            break;
        }
        }

        return state;
    }

    public static List<Double> extractScoresFromComparedBenchmarkList(List<ComparedBenchmark> comparedBenchmarks) {
        List<Double> scores = new ArrayList<>();
        for (ComparedBenchmark comparedBenchmark : comparedBenchmarks) {
            scores.add(comparedBenchmark.getScore());
        }
        return scores;
    }

    // Calculate Methods
    private static Double calculateMean(List<Double> scores) {
        Double average = 0.0;
        for (Double score : scores) {
            average += score;
        }
        return average / scores.size();
    }

    private static Double calculateSD(List<Double> scores) {
        Double mean = calculateMean(scores);
        return calculateSD(scores, mean);
    }

    private static Double calculateSD(List<Double> scores, Double mean) {
        double sumOfSquares = 0.0;

        for (Double score : scores) {
            sumOfSquares += Math.pow(score - mean, 2);
        }
        return Math.sqrt(sumOfSquares / scores.size());
    }

    private static Double calculateDeviationsFromMean(Double score, Double compareMean, Double compareSD) {
        double deviationsFromMean = 0.0;
        if (compareSD != 0) {
            deviationsFromMean = (score - compareMean) / compareSD;
        }
        return deviationsFromMean;
    }

    private static Double calculatePercentChange(Double newScore, Double compareScore) {
        return 100 * ((newScore - compareScore) / compareScore);
    }

    private static CompareState passAssertionPositive(Double val) {
        if (val >= 0) {
            return CompareState.PASS;
        } else {
            return CompareState.FAIL;
        }
    }

    private static CompareState passAssertionDeviation(Double deviationsFromMean, Double deviationsAllowed) {
        if (Math.abs(deviationsFromMean) < deviationsAllowed) {
            return CompareState.PASS;
        } else {
            return CompareState.FAIL;
        }
    }

    private static CompareState passAssertionPercentage(Double percentChange, Double percentageAllowed) {
        if (Math.abs(percentChange) < percentageAllowed) {
            return CompareState.PASS;
        } else {
            return CompareState.FAIL;
        }
    }

    public static Double roundHandling(Double value) {

        DecimalFormat df1 = new DecimalFormat("#.00");
        DecimalFormat df2 = new DecimalFormat("#.00000");

        if (value >= 1 || value < 0) {
            String tempStr = df1.format(value);
            Double formatValue = Double.parseDouble(tempStr);
            return formatValue;

        } else {
            String tempStr = df2.format(value);
            Double formatValue = Double.parseDouble(tempStr);
            return formatValue;
        }
    }

    public static void logInfo(String msg, Object... args) {
        log.info(msg, args);
    }

    public static void logWarn(String msg, Object... args) {
        log.warn(msg, args);
    }

    public static void logErr(String msg, Object... args) {
        log.error(msg, args);
    }
}
