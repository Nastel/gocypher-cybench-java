/*
 * Copyright (C) 2020, K2N.IO.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package com.gocypher.cybench.launcher.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gocypher.cybench.launcher.utils.ComputationUtils;

public class BenchmarkOverviewReport implements Serializable {
    private static final long serialVersionUID = 4919010767589263480L;

    private long timestamp;
    private long timestampUTC;
    private String reportURL;
    private String deviceReportsURL;
    private BigDecimal totalScore;
    private String uploadStatus;
    private Map<String, Map<String, Object>> categoriesOverview;
    private Map<String, Object> environmentSettings;
    private Map<String, Object> benchmarkSettings;
    private Map<String, List<BenchmarkReport>> benchmarks;

    public BenchmarkOverviewReport() {
        benchmarks = new HashMap<>();
        categoriesOverview = new HashMap<>();
        environmentSettings = new HashMap<>();
    }

    public void addToBenchmarks(BenchmarkReport report) {
        benchmarks.computeIfAbsent(report.getCategory(), k -> new ArrayList<>());
        benchmarks.get(report.getCategory()).add(report);
    }

    public void updateUploadStatus(String uploadStatus) {
        if ("private".equalsIgnoreCase(uploadStatus)) {
            this.uploadStatus = uploadStatus;
        } else {
            this.uploadStatus = "public";
        }
    }

    public void computeScores() {
        Double sumOfCategoryScores = 0.0;
        for (Map.Entry<String, List<BenchmarkReport>> stringListEntry : benchmarks.entrySet()) {
            Map<String, Object> categoryOverviewMap = new HashMap<>();
            List<BenchmarkReport> categoryReports = stringListEntry.getValue();
            // BigDecimal productOfCategoryScores = BigDecimal.ONE;
            long gcCallsCount = 0;
            long gcTime = 0;
            for (BenchmarkReport item : categoryReports) {
                // productOfCategoryScores = productOfCategoryScores.multiply(BigDecimal.valueOf(item.getScore()));
                if (item.getGcCalls() != null) {
                    gcCallsCount += item.getGcCalls().longValue();
                }
                if (item.getGcTime() != null) {
                    gcTime += item.getGcTime().longValue();
                }
            }

            // BigDecimal categoryScore =
            // ComputationUtils.log10(productOfCategoryScores.multiply(BigDecimal.valueOf(10000)));
            Double categoryScore = ComputationUtils.computeCategoryScore(categoryReports);
            sumOfCategoryScores += categoryScore;

            categoryOverviewMap.put("score", categoryScore);
            categoryOverviewMap.put("gcCallsCount", BigDecimal.valueOf(gcCallsCount));
            categoryOverviewMap.put("gcTime", BigDecimal.valueOf(gcTime));

            categoriesOverview.put(stringListEntry.getKey(), categoryOverviewMap);
            // productOfTotals = productOfTotals.multiply(categoryScore);
        }
        // this.totalScore = ComputationUtils.log10(productOfTotals).multiply(BigDecimal.valueOf(10000)) ;
        totalScore = BigDecimal.valueOf(sumOfCategoryScores);
    }

    @JsonIgnore
    public boolean isEligibleForStoringExternally() {
        return totalScore != null && !environmentSettings.isEmpty() && !categoriesOverview.isEmpty()
                && !benchmarks.isEmpty();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestampUTC() {
        return timestampUTC;
    }

    public void setTimestampUTC(long timestampUTC) {
        this.timestampUTC = timestampUTC;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public Map<String, List<BenchmarkReport>> getBenchmarks() {
        return benchmarks;
    }

    public void setBenchmarks(Map<String, List<BenchmarkReport>> benchmarks) {
        this.benchmarks = benchmarks;
    }

    public Map<String, Map<String, Object>> getCategoriesOverview() {
        return categoriesOverview;
    }

    public void setCategoriesOverview(Map<String, Map<String, Object>> categoriesOverview) {
        this.categoriesOverview = categoriesOverview;
    }

    public Map<String, Object> getEnvironmentSettings() {
        return environmentSettings;
    }

    public void setEnvironmentSettings(Map<String, Object> environmentSettings) {
        this.environmentSettings = environmentSettings;
    }

    public Map<String, Object> getBenchmarkSettings() {
        return benchmarkSettings;
    }

    public void setBenchmarkSettings(Map<String, Object> benchmarkSettings) {
        this.benchmarkSettings = benchmarkSettings;
    }

    public String getReportURL() {
        return reportURL;
    }

    public void setReportURL(String reportURL) {
        this.reportURL = reportURL;
    }

    public String getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(String uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public String getDeviceReportsURL() {
        return deviceReportsURL;
    }

    public void setDeviceReportsURL(String deviceReports) {
        deviceReportsURL = deviceReports;
    }

    @Override
    public String toString() {
        return "BenchmarkOverviewReport{" +
                "timestamp=" + timestamp +
                ", timestampUTC=" + timestampUTC +
                ", totalScore=" + totalScore +
                ", categoriesOverview=" + categoriesOverview +
                ", environmentSettings=" + environmentSettings +
                ", benchmarks=" + benchmarks +
                ", reportURL=" + reportURL +
                '}';
    }

}
