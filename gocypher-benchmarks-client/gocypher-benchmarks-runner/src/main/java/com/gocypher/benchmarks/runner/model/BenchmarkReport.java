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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gocypher.benchmarks.core.model.BaseScoreConverter;
import com.gocypher.benchmarks.runner.utils.Constants;

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
    private Double gcAllocationRate ;
    private Double gcAllocationRateNorm ;
    private Double gcChurnPsEdenSpace ;
    private Double gcChurnPsEdenSpaceNorm ;
    private Double gcChurnPsSurvivorSpace ;
    private Double gcChurnPsSurvivorSpaceNorm ;

    private Double threadsAliveCount ;
    private Double threadsDaemonCount ;
    private Double threadsStartedCount ;

    /*A safepoint is a moment in time when a  thread's data, its internal state and representation in the JVM are, well,safe for observation by other threads in the JVM.*/
    private Double threadsSafePointsCount ;
    private Double threadsSafePointTime ;
    private Double threadsSafePointSyncTime ;
    private Double threadsSyncContendedLockAttemptsCount ;
    private Double threadsSyncMonitorDeflations ;
    private Double threadsSyncMonitorInflations ;
    private Double threadsSyncMonitorFatMonitorsCount ;
    private Double threadsSyncMonitorFutileWakeupsCount ;
    private Double threadsSyncNotificationsCount ;

    /*parked threads are suspended until they are given a permit.*/
    private Double threadsSyncParksCount ;

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
    @JsonIgnore
    public void recalculateScoresToMatchNewUnits (){

            //FIXME seek and r/w conversion to MB/s differs, fix it.
            if (Constants.BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.get(this.name) != null){
                try{
                    //LOG.info("Custom scores computation for class found:{}",this.name);
                    Class<?> clazz = Class.forName(Constants.BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.get(this.name)) ;
                    BaseScoreConverter converter = (BaseScoreConverter)clazz.newInstance() ;
                    Map<String,Object>metaData = new HashMap<>() ;
                    this.score = converter.convertScore(this.score,metaData) ;

                    Double tmpMin = converter.convertScore(this.minScore,metaData) ;
                    Double tmpMax = converter.convertScore(this.maxScore,metaData) ;

                    if (tmpMin != null && tmpMax != null){
                        if (tmpMin>tmpMax){
                            this.minScore = tmpMax ;
                            this.maxScore = tmpMin ;
                        }
                        else {
                            this.minScore = tmpMin ;
                            this.maxScore = tmpMax ;
                        }
                    }
                    else {
                        this.minScore =null ;
                        this.maxScore = null;
                    }
                    this.meanScore = converter.convertScore(this.meanScore,metaData) ;

                    this.units = converter.getUnits() ;
                }catch(Exception e){

                }
            }

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

    public Double getGcAllocationRate() {
        return gcAllocationRate;
    }

    public void setGcAllocationRate(Double gcAllocationRate) {
        this.gcAllocationRate = gcAllocationRate;
    }

    public Double getGcAllocationRateNorm() {
        return gcAllocationRateNorm;
    }

    public void setGcAllocationRateNorm(Double gcAllocationRateNorm) {
        this.gcAllocationRateNorm = gcAllocationRateNorm;
    }

    public Double getGcChurnPsEdenSpace() {
        return gcChurnPsEdenSpace;
    }

    public void setGcChurnPsEdenSpace(Double gcChurnPsEdenSpace) {
        this.gcChurnPsEdenSpace = gcChurnPsEdenSpace;
    }

    public Double getGcChurnPsEdenSpaceNorm() {
        return gcChurnPsEdenSpaceNorm;
    }

    public void setGcChurnPsEdenSpaceNorm(Double gcChurnPsEdenSpaceNorm) {
        this.gcChurnPsEdenSpaceNorm = gcChurnPsEdenSpaceNorm;
    }

    public Double getGcChurnPsSurvivorSpace() {
        return gcChurnPsSurvivorSpace;
    }

    public void setGcChurnPsSurvivorSpace(Double gcChurnPsSurvivorSpace) {
        this.gcChurnPsSurvivorSpace = gcChurnPsSurvivorSpace;
    }

    public Double getGcChurnPsSurvivorSpaceNorm() {
        return gcChurnPsSurvivorSpaceNorm;
    }

    public void setGcChurnPsSurvivorSpaceNorm(Double gcChurnPsSurvivorSpaceNorm) {
        this.gcChurnPsSurvivorSpaceNorm = gcChurnPsSurvivorSpaceNorm;
    }

    public Double getThreadsAliveCount() {
        return threadsAliveCount;
    }

    public void setThreadsAliveCount(Double threadsAliveCount) {
        this.threadsAliveCount = threadsAliveCount;
    }

    public Double getThreadsDaemonCount() {
        return threadsDaemonCount;
    }

    public void setThreadsDaemonCount(Double threadsDaemonCount) {
        this.threadsDaemonCount = threadsDaemonCount;
    }

    public Double getThreadsStartedCount() {
        return threadsStartedCount;
    }

    public void setThreadsStartedCount(Double threadsStartedCount) {
        this.threadsStartedCount = threadsStartedCount;
    }

    public Double getThreadsSafePointsCount() {
        return threadsSafePointsCount;
    }

    public void setThreadsSafePointsCount(Double threadsSafePointsCount) {
        this.threadsSafePointsCount = threadsSafePointsCount;
    }

    public Double getThreadsSafePointTime() {
        return threadsSafePointTime;
    }

    public void setThreadsSafePointTime(Double threadsSafePointTime) {
        this.threadsSafePointTime = threadsSafePointTime;
    }

    public Double getThreadsSafePointSyncTime() {
        return threadsSafePointSyncTime;
    }

    public void setThreadsSafePointSyncTime(Double threadsSafePointSyncTime) {
        this.threadsSafePointSyncTime = threadsSafePointSyncTime;
    }

    public Double getThreadsSyncContendedLockAttemptsCount() {
        return threadsSyncContendedLockAttemptsCount;
    }

    public void setThreadsSyncContendedLockAttemptsCount(Double threadsSyncContendedLockAttemptsCount) {
        this.threadsSyncContendedLockAttemptsCount = threadsSyncContendedLockAttemptsCount;
    }

    public Double getThreadsSyncMonitorDeflations() {
        return threadsSyncMonitorDeflations;
    }

    public void setThreadsSyncMonitorDeflations(Double threadsSyncMonitorDeflations) {
        this.threadsSyncMonitorDeflations = threadsSyncMonitorDeflations;
    }

    public Double getThreadsSyncMonitorInflations() {
        return threadsSyncMonitorInflations;
    }

    public void setThreadsSyncMonitorInflations(Double threadsSyncMonitorInflations) {
        this.threadsSyncMonitorInflations = threadsSyncMonitorInflations;
    }

    public Double getThreadsSyncMonitorFatMonitorsCount() {
        return threadsSyncMonitorFatMonitorsCount;
    }

    public void setThreadsSyncMonitorFatMonitorsCount(Double threadsSyncMonitorFatMonitorsCount) {
        this.threadsSyncMonitorFatMonitorsCount = threadsSyncMonitorFatMonitorsCount;
    }

    public Double getThreadsSyncMonitorFutileWakeupsCount() {
        return threadsSyncMonitorFutileWakeupsCount;
    }

    public void setThreadsSyncMonitorFutileWakeupsCount(Double threadsSyncMonitorFutileWakeupsCount) {
        this.threadsSyncMonitorFutileWakeupsCount = threadsSyncMonitorFutileWakeupsCount;
    }

    public Double getThreadsSyncNotificationsCount() {
        return threadsSyncNotificationsCount;
    }

    public void setThreadsSyncNotificationsCount(Double threadsSyncNotificationsCount) {
        this.threadsSyncNotificationsCount = threadsSyncNotificationsCount;
    }

    public Double getThreadsSyncParksCount() {
        return threadsSyncParksCount;
    }

    public void setThreadsSyncParksCount(Double threadsSyncParksCount) {
        this.threadsSyncParksCount = threadsSyncParksCount;
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
                ", gcCalls=" + gcCalls +
                ", gcTime=" + gcTime +
                ", gcAllocationRate=" + gcAllocationRate +
                ", gcAllocationRateNorm=" + gcAllocationRateNorm +
                ", gcChurnPsEdenSpace=" + gcChurnPsEdenSpace +
                ", gcChurnPsEdenSpaceNorm=" + gcChurnPsEdenSpaceNorm +
                ", gcChurnPsSurvivorSpace=" + gcChurnPsSurvivorSpace +
                ", gcChurnPsSurvivorSpaceNorm=" + gcChurnPsSurvivorSpaceNorm +
                '}';
    }
}
