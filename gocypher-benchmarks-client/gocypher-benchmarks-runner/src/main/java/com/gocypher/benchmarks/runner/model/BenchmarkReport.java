package com.gocypher.benchmarks.runner.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

public class BenchmarkReport implements Serializable {
    private String name ;
    private Double score;
    private String units ;
    private String mode ;
    private String category ;

    private Double meanScore ;
    private Double minScore ;
    private Double maxScore ;
    private Double stdDevScore ;
    private Long n ;
    private Double gcCalls ;
    private Double gcTime ;


    public BenchmarkReport(){

    }
    @JsonIgnore
    public String getReportClassName (){
        if (this.name != null){
            int idx = this.name.lastIndexOf(".") ;
            return this.name.substring(0,idx) ;
        }
        return null ;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getMeanScore() {
        return meanScore;
    }

    public void setMeanScore(Double meanScore) {
        this.meanScore = meanScore;
    }

    public Double getMinScore() {
        return minScore;
    }

    public void setMinScore(Double minScore) {
        this.minScore = minScore;
    }

    public Double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Double maxScore) {
        this.maxScore = maxScore;
    }

    public Double getStdDevScore() {
        return stdDevScore;
    }

    public void setStdDevScore(Double stdDevScore) {
        this.stdDevScore = stdDevScore;
    }

    public Long getN() {
        return n;
    }

    public void setN(Long n) {
        this.n = n;
    }

    public Double getGcCalls() {
        return gcCalls;
    }

    public void setGcCalls(Double gcCalls) {
        this.gcCalls = gcCalls;
    }

    public Double getGcTime() {
        return gcTime;
    }

    public void setGcTime(Double gcTime) {
        this.gcTime = gcTime;
    }

    @Override
    public String toString() {
        return "BenchmarkReport{" +
                "name='" + name + '\'' +
                ", score=" + score +
                ", units='" + units + '\'' +
                ", mode='" + mode + '\'' +
                ", category='" + category + '\'' +
                ", meanScore=" + meanScore +
                ", minScore=" + minScore +
                ", maxScore=" + maxScore +
                ", stdDevScore=" + stdDevScore +
                ", n=" + n +
                '}';
    }
}
