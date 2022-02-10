/*
 * Copyright (C) 2020-2022, K2N.IO. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * K2N.IO. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with K2N.IO.
 *
 * K2N.IO MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. K2N.IO SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 */
package com.gocypher.cybench.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;


public class ComparedBenchmark {
    public static enum CompareState {
        PASS, FAIL, SKIP
    }

    private String datasetID;
    private Map<String, Object> benchProperties = new HashMap<>();
    private ComparisonConfig comparisonConfig;

    private String fingerprint;
    private String displayName;
    private Double score;
    private BigDecimal roundedScore;
    private String mode;

    private CompareState compareState;
    private Double delta;
    private BigDecimal roundedDelta;
    private Double percentChange;
    private BigDecimal roundedPercentChange;
    private Double compareMean;
    private BigDecimal roundedCompareMean;
    private Double compareSD;
    private BigDecimal roundedCompareSD; 
    private Double deviationsFromMean;
    private BigDecimal roundedDeviationsfromMean;


    public ComparedBenchmark() {

    }
    
    public ComparedBenchmark(String datasetID, Map<String, Object> benchProperties) {
        setDatasetID(datasetID);
        setBenchProperties(benchProperties);
    }

    public void setDatasetID(String datasetID) {
        this.datasetID = datasetID;
    }

    public String getDatasetID() {
        return datasetID;
    }

    public void setBenchProperties(Map<String, Object> benchProperties) {
        this.benchProperties = benchProperties;
    }

    public Map<String, Object> getBenchProperties() {
        return benchProperties;
    }

    public void setComparisonConfig(ComparisonConfig comparisonConfig) {
        this.comparisonConfig = comparisonConfig;
    }

    public ComparisonConfig getComparisonConfig() {
        return this.comparisonConfig;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setScore(Double score) {
        this.score = score;
        setRoundedScore(score);
    }

    public Double getScore() {
        return score;
    }

    public void setRoundedScore(Double score) {
        this.roundedScore = new BigDecimal(score).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getRoundedScore() {
        return roundedScore;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

    public void setCompareState(CompareState compareState) {
        this.compareState = compareState;
    }

    public CompareState getCompareState() {
        return compareState;
    }

    public void setDelta(Double delta) {
        this.delta = delta;
        setRoundedDelta(delta);
    }

    public Double getDelta() {
        return delta;
    }

    public void setRoundedDelta(Double delta) {
        this.roundedDelta = new BigDecimal(delta).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getRoundedDelta() {
        return roundedDelta;
    }

    public void setPercentChange(Double percentChange) {
        this.percentChange = percentChange;
        setRoundedPercentChange(percentChange);
    }

    public Double getPercentChange() {
        return percentChange;
    }

    public void setRoundedPercentChange(Double percentChange) {
        this.roundedPercentChange = new BigDecimal(percentChange).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getRoundedPercentChange() {
        return roundedPercentChange;
    }

    public void setCompareMean(Double compareMean) {
        this.compareMean = compareMean;
        setRoundedCompareMean(compareMean);
    }

    public Double getCompareMean() {
        return compareMean;
    }

    public void setRoundedCompareMean(Double compareMean) {
        this.roundedCompareMean = new BigDecimal(compareMean).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getRoundedCompareMean() {
        return roundedCompareMean;
    }

    public void setCompareSD(Double compareSD) {
        this.compareSD = compareSD;
        setRoundedCompareSD(compareSD);
    }

    public Double getCompareSD() {
        return compareSD;
    }

    public void setRoundedCompareSD(Double compareSD) {
        this.roundedCompareSD = new BigDecimal(compareSD).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getRoundedCompareSD() {
        return roundedCompareSD;
    }

    public void setDeviationsFromMean(Double deviationsFromMean) {
        this.deviationsFromMean = deviationsFromMean;
        setRoundedDeviationsFromMean(deviationsFromMean);
    }

    public Double getDeviationsFromMean() {
        return deviationsFromMean;
    }

    public void setRoundedDeviationsFromMean(Double deviationsFromMean) {
        this.roundedDeviationsfromMean = new BigDecimal(deviationsFromMean).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getRoundedDeviationsFromMean() {
        return roundedDeviationsfromMean;
    }

    public String toString() {
        return displayName + ": " + score;
    }
}
