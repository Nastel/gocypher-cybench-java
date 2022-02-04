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

package com.gocypher.cybench.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gocypher.cybench.model.ComparedBenchmark;
import com.gocypher.cybench.model.ComparedBenchmark.CompareState;
import com.gocypher.cybench.model.ComparisonConfig;
import com.gocypher.cybench.model.ComparisonConfig.Scope;
import com.gocypher.cybench.model.ComparisonConfig.Method;
import com.gocypher.cybench.model.ComparisonConfig.Threshold;

public final class Comparisons {
    private static final Logger log = LoggerFactory.getLogger(Comparisons.class);

    private Comparisons() {
    }

    public static Map<String, Object> runComparison(Map<String, Object> configMap,
            Map<String, Map<String, ComparedBenchmark>> projectVersionBenchmarks,
            Map<String, Map<String, List<ComparedBenchmark>>> compareVersionBenchmarks) {
        Map<String, Object> resultMap = new HashMap<>();

        
        Method method = (Method) configMap.get(ConfigHandling.METHOD);
        Threshold threshold = (Threshold) configMap.get(ConfigHandling.THRESHOLD);
        Number deviationsAllowedNum = (Number) configMap.get(ConfigHandling.DEVIATIONS_ALLOWED);
        Number percentChangeAllowedNum = (Number) configMap.get(ConfigHandling.PERCENT_CHANGE_ALLOWED);
        Double deviationsAllowed = null;
        Double percentChangeAllowed = null;

        if (deviationsAllowedNum != null) {
            deviationsAllowed = deviationsAllowedNum.doubleValue();
        }
        if (percentChangeAllowedNum != null) {
            percentChangeAllowed = percentChangeAllowedNum.doubleValue();
        }

        // if (method.equals(Method.DELTA)) {
        //     if (threshold.equals(Threshold.GREATER)) {
        //         resultMap = compareWithDeltaGreater(projectVersionBenchmarks, compareVersionBenchmarks,
        //                 percentChangeAllowed);
        //     } else if (threshold.equals(PERCENT_CHANGE)) {
        //         resultMap = compareWithDeltaPercentChange(projectVersionBenchmarks, compareVersionBenchmarks,
        //                 percentChangeAllowed);
        //     }
        // } else if (method.equals(SD)) {
        //     resultMap = compareWithSD(projectVersionBenchmarks, compareVersionBenchmarks, deviationsAllowed);
        // }

        // resultMap.put("timestampUTC", ZonedDateTime.now(ZoneOffset.UTC).toInstant().toString());
        return resultMap;
    }

    public static CompareState runComparison(ComparedBenchmark benchmarkToCompare, List<ComparedBenchmark> benchmarksToCompareAgainst) {
        
        CompareState state = null;
        ComparisonConfig comparisonConfig = benchmarkToCompare.getComparisonConfig();
        Method method = comparisonConfig.getMethod();
        Threshold threshold = comparisonConfig.getThreshold();
        Double deviationsAllowed = comparisonConfig.getDeviationsAllowed();
        Double percentChangeAllowed = comparisonConfig.getPercentChangeAllowed();

        if (method.equals(Method.DELTA)) {
            if (threshold.equals(Threshold.GREATER)) {
                state = compareWithDeltaGreater(benchmarkToCompare, benchmarksToCompareAgainst);
            } else if (threshold.equals(Threshold.PERCENT_CHANGE)) {
                state = compareWithDeltaPercentChange(benchmarkToCompare, benchmarksToCompareAgainst,
                        percentChangeAllowed);
            }
        } else if (method.equals(Method.SD)) {
            state = compareWithSD(benchmarkToCompare, benchmarksToCompareAgainst, deviationsAllowed);
        }

        return state;
    }

    private static CompareState compareWithDeltaGreater(ComparedBenchmark benchmarkToCompare, List<ComparedBenchmark> benchmarksToCompareAgainst) {
        if (!benchmarksToCompareAgainst.isEmpty()) {
            Double score = benchmarkToCompare.getScore();
            Double compareMean = calculateMean(extractScoresFromComparedBenchmarkList(benchmarksToCompareAgainst));
            benchmarkToCompare.setCompareMean(compareMean);
            Double delta = score - compareMean;
            benchmarkToCompare.setDelta(delta);
            if (passAssertionPositive(delta)) {
                return CompareState.PASS;
            } else {
                return CompareState.FAIL;
            }
        } else {
            return CompareState.SKIP;
        }
    }

    private static CompareState compareWithDeltaPercentChange(ComparedBenchmark benchmarkToCompare, List<ComparedBenchmark> benchmarksToCompareAgainst, Double percentChangeAllowed) {
        if (!benchmarksToCompareAgainst.isEmpty()) {
            Double score = benchmarkToCompare.getScore();
            Double compareMean = calculateMean(extractScoresFromComparedBenchmarkList(benchmarksToCompareAgainst));
            benchmarkToCompare.setCompareMean(compareMean);
            Double percentChange = calculatePercentChange(score, compareMean);
            benchmarkToCompare.setPercentChange(percentChange);

            if (passAssertionPercentage(percentChange, percentChangeAllowed)) {
                return CompareState.PASS;
            } else {
                return CompareState.FAIL;
            }
        } else {
            return CompareState.SKIP;
        }
    }

    private static CompareState compareWithSD(ComparedBenchmark benchmarkToCompare, List<ComparedBenchmark> benchmarksToCompareAgainst, Double deviationsAllowed) {
        if (!benchmarksToCompareAgainst.isEmpty()) {
            Double score = benchmarkToCompare.getScore();
            List<Double> compareScores = extractScoresFromComparedBenchmarkList(benchmarksToCompareAgainst);
            Double compareMean = calculateMean(compareScores);
            benchmarkToCompare.setCompareMean(compareMean);
            Double compareSD = calculateSD(compareScores, compareMean);
            benchmarkToCompare.setCompareSD(compareSD);
            Double deviationsFromMean = calculateDeviationsFromMean(score, compareMean, compareSD);
            benchmarkToCompare.setDeviationsFromMean(deviationsFromMean);
            if (passAssertionDeviation(deviationsFromMean, deviationsAllowed)) {
                return CompareState.PASS;
            } else {
                return CompareState.FAIL;
            }
        } else {
            return CompareState.SKIP;
        }
    }

    private static List<Double> extractScoresFromComparedBenchmarkList(List<ComparedBenchmark> comparedBenchmarks) {
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
        Double deviationsFromMean = 0.0;
        if (compareSD != 0) {
            deviationsFromMean = (score - compareMean) / compareSD;
        }
        return deviationsFromMean;
    }

    private static Double calculatePercentChange(Double newScore, Double compareScore) {
        return 100 * ((newScore - compareScore) / compareScore);
    }

    private static boolean passAssertionPositive(Double val) {
        return val >= 0;
    }

    private static boolean passAssertionDeviation(Double deviationsFromMean, Double deviationsAllowed) {
        return Math.abs(deviationsFromMean) < deviationsAllowed;
    }

    private static boolean passAssertionPercentage(Double percentChange, Double percentageAllowed) {
        return Math.abs(percentChange) < percentageAllowed;
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













// DEPRECATED METHODS

    
    // public static Double compareScores(Map<String, Object> configMap, String benchmarkName, String benchmarkVersion,
    //         String benchmarkMode, List<Double> benchmarkVersionScores, List<Double> compareVersionScores) {
    //     int benchmarkVersionSize = benchmarkVersionScores.size();
    //     Double benchmarkScore = benchmarkVersionScores.get(benchmarkVersionSize - 1);
    //     Double compareValue = null;
    //     if (validateComparison(configMap, benchmarkName, benchmarkVersion, benchmarkMode, benchmarkScore,
    //             benchmarkVersionScores, compareVersionScores)) {
    //         Method method = (Method) configMap.get(ConfigHandling.METHOD);
    //         Threshold threshold = (Threshold) configMap.get(ConfigHandling.THRESHOLD);
    //         String rangeStr = (String) configMap.get(ConfigHandling.RANGE);
    //         int range = Integer.parseInt(rangeStr);

    //         Double delta = compareWithDelta(benchmarkVersionScores, compareVersionScores, range, Threshold.GREATER);
    //         Double percentChange = compareWithDelta(benchmarkVersionScores, compareVersionScores, range,
    //                 Threshold.PERCENT_CHANGE);
    //         Double sdFromMean = compareWithSD(benchmarkVersionScores, compareVersionScores, range);
    //         if (method.equals(Method.DELTA)) {
    //             if (threshold.equals(Threshold.GREATER)) {
    //                 compareValue = delta;
    //             } else if (threshold.equals(Threshold.PERCENT_CHANGE)) {
    //                 compareValue = percentChange;
    //             }
    //         } else {
    //             compareValue = sdFromMean;
    //         }

    //         Map<String, Double> compareValues = new HashMap<>();
    //         compareValues.put(CALCULATED_COMPARE_VALUE, compareValue);
    //         compareValues.put(CALCULATED_DELTA, delta);
    //         compareValues.put(CALCULATED_PERCENT_CHANGE, percentChange);
    //         compareValues.put(CALCULATED_SD_FROM_MEAN, sdFromMean);

    //         CompareState CompareState = passAssertion(configMap, benchmarkName, benchmarkVersion, benchmarkMode, benchmarkScore,
    //                 compareValues);
    //         CompareBenchmarks.totalComparedBenchmarks++;
    //         if (CompareState.equals(CompareState.PASS)) {
    //             CompareBenchmarks.totalPassedBenchmarks++;
    //         } else if (CompareState.equals(CompareState.FAIL)) {
    //             CompareBenchmarks.totalFailedBenchmarks++;
    //         }

    //         logComparison(CompareState, configMap, benchmarkName, benchmarkVersion, benchmarkMode, method, range, threshold);
    //     }
    //     return compareValue;
    // }

    // public static Double compareWithDelta(List<Double> benchmarkVersionScores, List<Double> compareVersionScores,
    //         int range, Threshold threshold) {
    //     int benchmarkVersionSize = benchmarkVersionScores.size();
    //     int compareVersionSize = compareVersionScores.size();
    //     Double newScore = benchmarkVersionScores.get(benchmarkVersionSize - 1);
    //     Double compareValue = calculateMean(
    //             compareVersionScores.subList(compareVersionSize - range, compareVersionSize));

    //     double delta = calculateDelta(newScore, compareValue, threshold);

    //     return delta;

    // }

    // public static Double compareWithSD(List<Double> benchmarkVersionScores, List<Double> compareVersionScores,
    //         int range) {
    //     int benchmarkVersionSize = benchmarkVersionScores.size();
    //     int compareVersionSize = compareVersionScores.size();
    //     Double newScore = benchmarkVersionScores.get(benchmarkVersionSize - 1);
    //     Double compareMean = calculateMean(
    //             compareVersionScores.subList(compareVersionSize - range, compareVersionSize));

    //     double compareSD = calculateSD(compareVersionScores.subList(compareVersionSize - range, compareVersionSize),
    //             compareMean);
    //     double SDfromMean = 0;
    //     if (compareSD != 0) {
    //         SDfromMean = (newScore - compareMean) / compareSD;
    //     }

    //     return SDfromMean;
    // }

    // public static void logComparison(CompareState CompareState, Map<String, Object> logConfigs, String benchmarkName,
    //         String benchmarkVersion, String benchmarkMode, Method method, int range, Threshold threshold) {
    //     String benchmarkFingerprint = Requests.namesToFingerprints.get(benchmarkName);
    //     StringBuilder sb = new StringBuilder();
    //     Scope scope = (Scope) logConfigs.get(ConfigHandling.SCOPE);
    //     String compareVersion = (String) logConfigs.get(ConfigHandling.COMPARE_VERSION);

    //     sb.append("{} COMPARISON - {} : mode={} - method={} ({} version={}");
    //     if (scope.equals(Scope.BETWEEN)) {
    //         if (compareVersion.equals(ConfigHandling.DEFAULT_COMPARE_VERSION)) {
    //             compareVersion = Requests.getPreviousVersion(benchmarkFingerprint);
    //         }
    //         sb.append(" and version=").append(compareVersion);
    //     }
    //     sb.append("), range={}");
    //     if (threshold != null) {
    //         sb.append(", threshold=").append(threshold);
    //     }

    //     if (CompareState.equals(CompareState.FAIL)) {
    //         log.error(sb.toString(), CompareState, benchmarkName, benchmarkMode, method, scope, benchmarkVersion, range);
    //     } else {
    //         log.info(sb.toString(), CompareState, benchmarkName, benchmarkMode, method, scope, benchmarkVersion, range);
    //     }
    // }

    // public static CompareState passAssertion(Map<String, Object> configMap, String benchmarkName, String benchmarkVersion,
    //         String benchmarkMode, Double benchmarkScore, Map<String, Double> compareValues) {
    //     Method compareMethod = (Method) configMap.get(ConfigHandling.METHOD);
    //     Threshold compareThreshold = (Threshold) configMap.get(ConfigHandling.THRESHOLD);
    //     Double percentChangeAllowed = (Double) configMap.get(ConfigHandling.PERCENT_CHANGE_ALLOWED);
    //     Double deviationsAllowed = null;
    //     // Double deviationsAllowed = (Double)
    //     // configMap.get(ConfigHandling.DEVIATIONS_ALLOWED); ///
    //     if ((configMap.get(ConfigHandling.DEVIATIONS_ALLOWED) != null)) {
    //         if (configMap.get(ConfigHandling.DEVIATIONS_ALLOWED).getClass() != Double.class) {
    //             deviationsAllowed = Double.parseDouble((String) configMap.get(ConfigHandling.DEVIATIONS_ALLOWED));
    //         } else {
    //             deviationsAllowed = (Double) configMap.get(ConfigHandling.DEVIATIONS_ALLOWED);
    //         }
    //     }
    //     Double compareValue = compareValues.get(CALCULATED_COMPARE_VALUE);
    //     boolean pass = false;
    //     if (compareMethod.equals(Method.SD)) {
    //         // assert within x SDs from mean
    //         pass = passAssertionDeviation(compareValue, deviationsAllowed);
    //     } else if (compareThreshold.equals(Threshold.PERCENT_CHANGE)) {
    //         // assert within x Percentage from COMPARE_VALUE
    //         pass = passAssertionPercentage(compareValue, percentChangeAllowed);
    //     } else {
    //         // assert higher than COMPARE_VALUE
    //         pass = passAssertionPositive(compareValue);
    //     }

    //     CompareBenchmarks.addPassFailBenchData(
    //             pass ? CompareBenchmarks.passedBenchmarks : CompareBenchmarks.failedBenchmarks, configMap,
    //             benchmarkName, benchmarkVersion, benchmarkMode, benchmarkScore, compareValues);

    //     if (pass) {
    //         return CompareState.PASS;
    //     }
    //     return CompareState.FAIL;
    // }

    // NO COMPARISON SHOULD BE RUN, PASS TEST
    // public static boolean skipComparison(Double benchmarkScore, String benchmarkName, String benchmarkVersion,
    //         String benchmarkMode) {
    //     CompareBenchmarks.totalComparedBenchmarks++;
    //     CompareBenchmarks.totalSkippedBenchmarks++;
    //     CompareBenchmarks.addSkipBenchData(benchmarkScore, benchmarkName, benchmarkVersion, benchmarkMode);
    //     return false;
    // }
}
