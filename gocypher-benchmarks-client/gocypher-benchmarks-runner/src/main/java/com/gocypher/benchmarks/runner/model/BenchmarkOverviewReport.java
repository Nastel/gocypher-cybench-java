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

package com.gocypher.benchmarks.runner.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gocypher.benchmarks.core.model.BaseBenchmark;
import com.gocypher.benchmarks.runner.utils.ComputationUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

public class BenchmarkOverviewReport implements Serializable {
    private long timestamp ;
    private long timestampUTC ;
    private String reportURL ;
    private BigDecimal totalScore ;
    private String uploadStatus ;
    private Map<String,Map<String,Object>>categoriesOverview ;
    private Map<String,Object> environmentSettings  ;
    private Map<String,Object> benchmarkSettings  ;
    private Map<String,List<BenchmarkReport>> benchmarks ;


    public BenchmarkOverviewReport(){
        this.benchmarks = new HashMap<>();
        this.categoriesOverview = new HashMap<>();
        this.environmentSettings = new HashMap<>() ;
    }
    public void addToBenchmarks (BenchmarkReport report){
        this.benchmarks.computeIfAbsent(report.getCategory(), k -> new ArrayList<>());
        this.benchmarks.get(report.getCategory()).add(report) ;
    }
    public void computeScores (){
        Double sumOfCategoryScores = 0.0 ;
        for (String category : this.benchmarks.keySet()) {
            Map<String, Object> categoryOverviewMap = new HashMap<>();
            List<BenchmarkReport> categoryReports = this.benchmarks.get(category);
            //BigDecimal productOfCategoryScores = BigDecimal.ONE;
            long gcCallsCount = 0;
            long gcTime = 0;
            for (BenchmarkReport item : categoryReports) {
                //productOfCategoryScores = productOfCategoryScores.multiply(BigDecimal.valueOf(item.getScore()));
                if (item.getGcCalls() != null) {
                    gcCallsCount += item.getGcCalls().longValue();
                }
                if (item.getGcTime() != null) {
                    gcTime += item.getGcTime().longValue();
                }
            }

            //BigDecimal categoryScore = ComputationUtils.log10(productOfCategoryScores.multiply(BigDecimal.valueOf(10000)));
            Double categoryScore = ComputationUtils.computeCategoryScore(categoryReports);
            sumOfCategoryScores += categoryScore ;

            categoryOverviewMap.put("score", categoryScore);
            categoryOverviewMap.put("gcCallsCount", BigDecimal.valueOf(gcCallsCount));
            categoryOverviewMap.put("gcTime", BigDecimal.valueOf(gcTime));

            categoriesOverview.put(category, categoryOverviewMap);
            //productOfTotals = productOfTotals.multiply(categoryScore);
        }
        //this.totalScore = ComputationUtils.log10(productOfTotals).multiply(BigDecimal.valueOf(10000)) ;
        this.totalScore = BigDecimal.valueOf(sumOfCategoryScores) ;
    }


    @JsonIgnore
    public boolean isEligibleForStoringExternally (){
        if (    this.totalScore != null
                && !this.environmentSettings.isEmpty()
                && !this.categoriesOverview.isEmpty()
                && !this.benchmarks.isEmpty()
                ){
            return true ;
        }
        return false ;
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
