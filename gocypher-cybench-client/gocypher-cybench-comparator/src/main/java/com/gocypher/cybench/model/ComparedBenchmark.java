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

    private String skipReason;

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
        return comparisonConfig;
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
        roundedScore = new BigDecimal(score).setScale(2, RoundingMode.HALF_UP);
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
        roundedDelta = new BigDecimal(delta).setScale(2, RoundingMode.HALF_UP);
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
        roundedPercentChange = new BigDecimal(percentChange).setScale(2, RoundingMode.HALF_UP);
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
        roundedCompareMean = new BigDecimal(compareMean).setScale(2, RoundingMode.HALF_UP);
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
        roundedCompareSD = new BigDecimal(compareSD).setScale(2, RoundingMode.HALF_UP);
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
        roundedDeviationsfromMean = new BigDecimal(deviationsFromMean).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getRoundedDeviationsFromMean() {
        return roundedDeviationsfromMean;
    }

    public void setSkipReason(String skipReason) {
        this.skipReason = skipReason;
    }

    public String getSkipReason() {
        return skipReason;
    }

    @Override
    public String toString() {
        return displayName + ": " + score;
    }
}
