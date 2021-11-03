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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gocypher.cybench.CompareBenchmarks;
import com.gocypher.cybench.services.Requests;

public final class Comparisons {
    private static final Logger log = LoggerFactory.getLogger(Comparisons.class);

    private Comparisons() {
    }
    
    private static boolean validateComparison(Map<String, Object> configMap, String benchmarkName, String benchmarkVersion, String benchmarkMode, 
    		Double benchmarkScore, List<Double> benchmarkVersionScores, List<Double> compareVersionScores) {
    	if (compareVersionScores == null) {
    		logWarn(
                    "{} - {}: There are no scores to compare to!", benchmarkName, benchmarkMode);
    		return Comparisons.autoPass(benchmarkScore, benchmarkName, benchmarkVersion, benchmarkMode);
    	}
        String compareVersion = (String) configMap.get(ConfigHandling.COMPARE_VERSION);
    	String compareRange = (String) configMap.get(ConfigHandling.RANGE);
    	Scope compareScope = (Scope) configMap.get(ConfigHandling.SCOPE);
    	int range;
        if (compareRange.equals("ALL")) {
            range = compareVersionScores.size();
        } else {
            range = Integer.parseInt(compareRange);
            if (range > compareVersionScores.size()) {
                logWarn(
                        "{} - {}: There are not enough values to compare to in version ({}) with specific range ({}), no comparison will be run",
                        benchmarkName, benchmarkMode, benchmarkVersion, range);
                return Comparisons.autoPass(benchmarkScore, benchmarkName, benchmarkVersion, benchmarkMode);
            }
        }
        compareRange = String.valueOf(range);
        configMap.put(ConfigHandling.RANGE, compareRange);

        if (compareScope.equals(Comparisons.Scope.WITHIN)) {
        	if (!compareVersion.equals(benchmarkVersion)) {
        		compareVersion = benchmarkVersion;
        		configMap.put(ConfigHandling.COMPARE_VERSION, compareVersion);
        		logWarn("{} -{}: Compare scope set to WITHIN but compareVersion specified as a separate version, will ignore compareVersion", benchmarkName, benchmarkMode);
        	}
            
            if (benchmarkVersionScores.size() <= 1) {
                logWarn(
                        "{} - {}: There are no previously tested benchmarks within the version ({}), no comparison will be run",
                        benchmarkName, benchmarkMode, benchmarkVersion);
                return Comparisons.autoPass(benchmarkScore, benchmarkName, benchmarkVersion, benchmarkMode);
            }
        }
        
        return true;
    }
    
    public static Double compareScores(Map<String, Object> configMap, String benchmarkName, String benchmarkVersion, String benchmarkMode, 
    		List<Double> benchmarkVersionScores, List<Double> compareVersionScores) {
    	int benchmarkVersionSize = benchmarkVersionScores.size();
        Double benchmarkScore = benchmarkVersionScores.get(benchmarkVersionSize - 1);
        Double compareValue = null;
    	if (validateComparison(configMap, benchmarkName, benchmarkVersion, benchmarkMode, 
        		benchmarkScore, benchmarkVersionScores, compareVersionScores) ) {
    		Method method = (Method) configMap.get(ConfigHandling.METHOD);
        	Threshold threshold = (Threshold) configMap.get(ConfigHandling.THRESHOLD);
        	String rangeStr = (String) configMap.get(ConfigHandling.RANGE);
        	int range = Integer.parseInt(rangeStr);
        	
        	if (method.equals(Method.DELTA)) {
        		compareValue = compareWithDelta(benchmarkVersionScores, compareVersionScores, range, threshold);
        	} else {
        		compareValue = compareWithSD(benchmarkVersionScores, compareVersionScores, range);
        	}
        	
        	State state = passAssertion(configMap, benchmarkName, benchmarkVersion, benchmarkMode, benchmarkScore, compareValue);
        	CompareBenchmarks.totalComparedBenchmarks++;
        	if (state.equals(State.PASS))
        		CompareBenchmarks.totalPassedBenchmarks++;
        	else if (state.equals(State.FAIL))
        		CompareBenchmarks.totalFailedBenchmarks++;
        	
        	logComparison(state, configMap, benchmarkName, benchmarkVersion, benchmarkMode, method, range, threshold);
    	}
    	return compareValue;
    }
    
    public static Double compareWithDelta(List<Double> benchmarkVersionScores, List<Double> compareVersionScores,
    		int range, Threshold threshold) {
        int benchmarkVersionSize = benchmarkVersionScores.size();
        int compareVersionSize = compareVersionScores.size();
        Double newScore = benchmarkVersionScores.get(benchmarkVersionSize - 1);
        Double compareValue = calculateMean(
                compareVersionScores.subList(compareVersionSize - range, compareVersionSize));

		double delta = calculateDelta(newScore, compareValue, threshold);

		if (threshold.equals(Threshold.GREATER)) {
			double deltaPercentChange = calculatePercentChange(newScore, compareValue);

			// round values to two decimals
			newScore = roundTwoDecimal(newScore);
			compareValue = roundTwoDecimal(compareValue);
			delta = roundTwoDecimal(delta);
			deltaPercentChange = roundTwoDecimal(deltaPercentChange);
		} else {
			newScore = roundTwoDecimal(newScore);
			compareValue = roundTwoDecimal(compareValue);
			delta = roundTwoDecimal(delta);
		}

		return delta;
    }

    public static Double compareWithSD(List<Double> benchmarkVersionScores, List<Double> compareVersionScores,
            int range) {
        int benchmarkVersionSize = benchmarkVersionScores.size();
        int compareVersionSize = compareVersionScores.size();
        Double newScore = benchmarkVersionScores.get(benchmarkVersionSize - 1);
        Double compareMean = calculateMean(
                compareVersionScores.subList(compareVersionSize - range, compareVersionSize));

        double compareSD = calculateSD(compareVersionScores.subList(compareVersionSize - range, compareVersionSize),
                compareMean);

        double SDfromMean = (Math.abs(newScore) + compareMean) / compareSD;

        if (newScore < compareMean) {
            SDfromMean *= -1;
        }
        
		newScore = roundTwoDecimal(newScore);
		compareMean = roundTwoDecimal(compareMean);
		compareSD = roundTwoDecimal(compareSD);
		if (Double.isFinite(SDfromMean)) {
			SDfromMean = roundTwoDecimal(SDfromMean);
		}	

		return SDfromMean;
    }
    
    public static void logComparison(State state, Map<String, Object> logConfigs, String benchmarkName, String benchmarkVersion, String benchmarkMode,
    		Method method, int range, Threshold threshold) {
    	String benchmarkFingerprint = Requests.namesToFingerprints.get(benchmarkName);
    	StringBuilder sb = new StringBuilder();
        Scope scope = (Scope) logConfigs.get(ConfigHandling.SCOPE);
        String compareVersion = (String) logConfigs.get(ConfigHandling.COMPARE_VERSION);
        if (compareVersion.equals(ConfigHandling.DEFAULT_COMPARE_VERSION)) 
            compareVersion = Requests.getPreviousVersion(benchmarkFingerprint);
        sb.append("COMPARISON {} - {} : {} - method={} ({} version {}");
        if (scope.equals(Scope.BETWEEN)) 
            sb.append(" and version ").append(compareVersion);
        sb.append("), range={}");
        if (threshold != null)
        	sb.append(", threshold=").append(threshold);
        
        if (state.equals(State.FAIL))
        	log.error(sb.toString(), state, benchmarkName, benchmarkMode, method, scope, benchmarkVersion, range);
        else
        	log.info(sb.toString(), state, benchmarkName, benchmarkMode, method, scope, benchmarkVersion, range);
    }

    // Calculate Methods
    public static Double calculateDelta(Double newScore, Double compareValue, Threshold threshold) {

        Double difference = null;

        if (compareValue != null) {
            switch (threshold) {
            case GREATER:
                difference = newScore - compareValue;
                break;
            case PERCENT_CHANGE:
                difference = calculatePercentChange(newScore, compareValue);
                break;
            }
        }

        return difference;
    }

    public static Double calculateMean(List<Double> scores) {
        Double average = 0.0;
        for (Double score : scores) {
            average += score;
        }
        return average / scores.size();
    }

    public static Double calculateSD(List<Double> scores) {
        Double mean = calculateMean(scores);
        return calculateSD(scores, mean);
    }

    public static Double calculateSD(List<Double> scores, Double mean) {
        List<Double> temp = new ArrayList<>();

        for (Double score : scores) {
            temp.add(Math.pow(score - mean, 2));
        }

        return Math.sqrt(calculateMean(temp));
    }

    private static Double calculatePercentChange(Double newScore, Double compareScore) {
        return 100 * ((newScore - compareScore) / compareScore);
    }

    public static enum Method {
        DELTA, SD
    }

    public static enum Scope {
        WITHIN, BETWEEN
    }

    public static enum Threshold {
        PERCENT_CHANGE, GREATER
    }
    
    public static enum State {
    	RUNNING, PASS, FAIL
    }
    
    private static Double roundTwoDecimal(Double value) {
		// TODO: Handle *BIG* scores in scientific notation
		DecimalFormat df1 = new DecimalFormat("##################.00");
		// DecimalFormat.format() always returns a string, must convert to Double
		String tempStr = df1.format(value);
		Double formatValue = Double.parseDouble(tempStr);
		return formatValue;
	}

    public static State passAssertion(Map<String, Object> configMap, String benchmarkName, String benchmarkVersion, String benchmarkMode,
    		Double benchmarkScore, Double compareValue) {
    	Comparisons.Method compareMethod = (Comparisons.Method) configMap.get(ConfigHandling.METHOD);
    	Comparisons.Threshold compareThreshold = (Comparisons.Threshold) configMap.get(ConfigHandling.THRESHOLD);
    	Double percentChangeAllowed = (Double) configMap.get(ConfigHandling.PERCENT_CHANGE_ALLOWED);
    	Double deviationsAllowed = (Double) configMap.get(ConfigHandling.DEVIATIONS_ALLOWED);
    	boolean pass = false;
        if (compareMethod.equals(Method.SD)) {
        	// assert within x SDs from mean
            pass = passAssertionDeviation(compareValue, deviationsAllowed);
        } else if (compareThreshold.equals(Threshold.PERCENT_CHANGE)) {
        	// assert within x Percentage from COMPARE_VALUE
            pass = passAssertionPercentage(compareValue, percentChangeAllowed);
        } else {
        	// assert higher than COMPARE_VALUE
        	pass = passAssertionPositive(compareValue);
        }
        
        CompareBenchmarks.addPassFailBenchData(pass ? CompareBenchmarks.passedBenchmarks : CompareBenchmarks.failedBenchmarks, configMap, benchmarkName, benchmarkVersion, 
        		benchmarkMode, benchmarkScore, compareValue);
        
        if (pass)
        	return State.PASS;
        return State.FAIL;
    }

    public static boolean passAssertionDeviation(Double deviationsFromMean, Double deviationsAllowed) {
        if (Math.abs(deviationsFromMean) < deviationsAllowed) {
            log.info("Passed assertion");
            return true;
        } else {
            log.error("FAILED assertion");
            return false;
        }
    }

    public static boolean passAssertionPercentage(Double percentChange, Double percentageAllowed) {
        if (Math.abs(percentChange) < percentageAllowed) {
            log.info("Passed assertion");
            return true;
        } else {
            log.error("FAILED assertion");
            return false;
        }
    }

    public static boolean passAssertionPositive(Double val) {
        if (val >= 0) {
            log.info("Passed assertion");
            return true;
        } else {
            log.error("FAILED assertion");
            return false;
        }
    }
    


    // NO COMPARISON SHOULD BE RUN, PASS TEST
    public static boolean autoPass(Double benchmarkScore, String benchmarkName, String benchmarkVersion,
            String benchmarkMode) {
        CompareBenchmarks.totalComparedBenchmarks++;
        CompareBenchmarks.totalPassedBenchmarks++;
        CompareBenchmarks.addAutoPassBenchData(benchmarkScore, benchmarkName, benchmarkVersion, benchmarkMode);
        return false;
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
