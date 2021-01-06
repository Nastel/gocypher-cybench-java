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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gocypher.cybench.core.model.BaseScoreConverter;
import com.gocypher.cybench.launcher.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BenchmarkReport implements Serializable {
    private static final long serialVersionUID = 2293390306981371292L;
    private static final Logger LOG = LoggerFactory.getLogger(BenchmarkReport.class);
    private String name;
    private Double score;
    private Double operationTime;
    private String units;
    private String mode;
    private String category;
    private String context;
    private String version;
    private Map<String, String> metadata;
    private int benchThreadCount;
    private int benchForkCount;
    private int benchMeasurementIteration;
    private int benchMeasurementSeconds;
    private int benchWarmUpIteration;
    private int benchWarmUpSeconds;
    private String generatedFingerprint;
    private String manualFingerprint;
    private String classFingerprint;
    private Double meanScore;
    private Double minScore;
    private Double maxScore;
    private Double stdDevScore;
    private Long n;
    private Double gcCalls;
    private Double gcTime;
    private Double gcAllocationRate;
    private Double gcAllocationRateNorm;
    private Double gcChurnPsEdenSpace;
    private Double gcChurnPsEdenSpaceNorm;
    private Double gcChurnPsSurvivorSpace;
    private Double gcChurnPsSurvivorSpaceNorm;
    private Double threadsAliveCount;
    private Double threadsDaemonCount;
    private Double threadsStartedCount;
    /*A safepoint is a moment in time when a  thread's data, its internal state and representation in the JVM are, well,safe for observation by other threads in the JVM.
    All JVM's use safepoints to bring all of the application threads to a known state so the JVM can perform certain operations. Safepoints are used during Garbage Collection, during JIT compilation, for Thread Dumps, and many other operations. When a safepoint call is issued all of the application threads should "come to safepoint" as fast as possible. Threads that have come to safepoint block until the JVM releases them. Once all of the threads are at safepoint, the JVM performs the operation -- GC, compile, thread dump, etc. -- and then releases all the threads to run again. But when one or more application threads take a long time to come to safepoint, all of the other threads, which are now blocked, have to wait for the tardy thread(s).
     Time To Safepoint (TTSP).
     https://docs.azul.com/zing/19.02.1.0/Zing_AT_SafePointProfiler.htm
    */
    private Double threadsSafePointsCount;
    private Double threadsSafePointTime;
    private Double threadsSafePointSyncTime;
    private Double threadsSyncContendedLockAttemptsCount;
    private Double threadsSyncMonitorDeflations;
    private Double threadsSyncMonitorInflations;
    private Double threadsSyncMonitorFatMonitorsCount;
    private Double threadsSyncMonitorFutileWakeupsCount;
    private Double threadsSyncNotificationsCount;
    private Double threadsSafePointsInterval;
    private Double threadsSafePointsPause;
    private Double threadsSafePointsPauseAvg;
    private Double threadsSafePointsPauseCount;
    private Double threadsSafePointsPauseTTSP;
    private Double threadsSafePointsPauseTTSPAvg;
    private Double threadsSafePointsPauseTTSPCount;
    /*parked threads are suspended until they are given a permit.*/
    private Double threadsSyncParksCount;

    private Double performanceProcessCpuLoad;
    private Double performanceProcessHeapMemoryUsed  ;
    private Double performanceProcessNonHeapMemoryUsed  ;
    private Double performanceSystemCpuLoad ;

    public BenchmarkReport(){

    }

    @JsonIgnore
    public String getReportClassName() {
        if (this.name != null) {
            int idx = this.name.lastIndexOf(".");
            return this.name.substring(0, idx);
        }
        return null;
    }

    @JsonIgnore
    public void recalculateScoresToMatchNewUnits() {


        // FIXME seek and r/w conversion to MB/s differs, fix it.
        String className = Constants.BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.get(this.name);
        if (className != null) {
            try {
                // LOG.info("Custom scores computation for class found:{}",this.name);
                Class<?> clazz = Class.forName(className);
                BaseScoreConverter converter = (BaseScoreConverter) clazz.getDeclaredConstructor().newInstance();
                Map<String, Object> metaData = new HashMap<>();
                this.score = converter.convertScore(this.score, metaData);
                this.operationTime = converter.getOperationTimeMilliseconds(this.score, metaData);

                Double tmpMin = converter.convertScore(this.minScore, metaData);
                Double tmpMax = converter.convertScore(this.maxScore, metaData);

                if (tmpMin != null && tmpMax != null) {
                    if (tmpMin > tmpMax) {
                        this.minScore = tmpMax;
                        this.maxScore = tmpMin;
                    } else {
                        this.minScore = tmpMin;
                        this.maxScore = tmpMax;
                    }
                } else {
                    this.minScore = null;
                    this.maxScore = null;
                }
                this.meanScore = converter.convertScore(this.meanScore, metaData);
                if (this.stdDevScore != null) {
                    this.stdDevScore = (this.maxScore - this.minScore) / 2;
                }
                this.units = converter.getUnits();
            } catch (Exception e) {
                LOG.error("Error on recalculating score={}", this.name, e);
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

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public Double getThreadsSafePointsInterval() {
        return threadsSafePointsInterval;
    }

    public void setThreadsSafePointsInterval(Double threadsSafePointsInterval) {
        this.threadsSafePointsInterval = threadsSafePointsInterval;
    }

    public Double getThreadsSafePointsPause() {
        return threadsSafePointsPause;
    }

    public void setThreadsSafePointsPause(Double threadsSafePointsPause) {
        this.threadsSafePointsPause = threadsSafePointsPause;
    }

    public Double getThreadsSafePointsPauseAvg() {
        return threadsSafePointsPauseAvg;
    }

    public void setThreadsSafePointsPauseAvg(Double threadsSafePointsPauseAvg) {
        this.threadsSafePointsPauseAvg = threadsSafePointsPauseAvg;
    }

    public Double getThreadsSafePointsPauseCount() {
        return threadsSafePointsPauseCount;
    }

    public void setThreadsSafePointsPauseCount(Double threadsSafePointsPauseCount) {
        this.threadsSafePointsPauseCount = threadsSafePointsPauseCount;
    }

    public Double getThreadsSafePointsPauseTTSP() {
        return threadsSafePointsPauseTTSP;
    }

    public void setThreadsSafePointsPauseTTSP(Double threadsSafePointsPauseTTSP) {
        this.threadsSafePointsPauseTTSP = threadsSafePointsPauseTTSP;
    }

    public Double getThreadsSafePointsPauseTTSPAvg() {
        return threadsSafePointsPauseTTSPAvg;
    }

    public void setThreadsSafePointsPauseTTSPAvg(Double threadsSafePointsPauseTTSPAvg) {
        this.threadsSafePointsPauseTTSPAvg = threadsSafePointsPauseTTSPAvg;
    }

    public Double getThreadsSafePointsPauseTTSPCount() {
        return threadsSafePointsPauseTTSPCount;
    }

    public void setThreadsSafePointsPauseTTSPCount(Double threadsSafePointsPauseTTSPCount) {
        this.threadsSafePointsPauseTTSPCount = threadsSafePointsPauseTTSPCount;
    }


    public String getGeneratedFingerprint() {
        return generatedFingerprint;
    }

    public void setGeneratedFingerprint(String generatedFingerprint) {
        this.generatedFingerprint = generatedFingerprint;
    }

    public String getManualFingerprint() {
        return manualFingerprint;
    }

    public void setManualFingerprint(String manualFingerprint) {
        this.manualFingerprint = manualFingerprint;
    }

    public String getClassFingerprint() {
        return classFingerprint;
    }

    public void setClassFingerprint(String classFingerprint) {
        this.classFingerprint = classFingerprint;
    }

    public int getBenchThreadCount() {
        return benchThreadCount;
    }

    public void setBenchThreadCount(int benchThreadCount) {
        this.benchThreadCount = benchThreadCount;
    }

    public int getBenchForkCount() {
        return benchForkCount;
    }

    public void setBenchForkCount(int benchForkCount) {
        this.benchForkCount = benchForkCount;
    }

    public int getBenchMeasurementIteration() {
        return benchMeasurementIteration;
    }

    public void setBenchMeasurementIteration(int benchMeasurementIteration) {
        this.benchMeasurementIteration = benchMeasurementIteration;
    }

    public int getBenchMeasurementSeconds() {
        return benchMeasurementSeconds;
    }

    public void setBenchMeasurementSeconds(int benchMeasurementSeconds) {
        this.benchMeasurementSeconds = benchMeasurementSeconds;
    }

    public int getBenchWarmUpIteration() {
        return benchWarmUpIteration;
    }

    public void setBenchWarmUpIteration(int benchWarmUpIteration) {
        this.benchWarmUpIteration = benchWarmUpIteration;
    }

    public int getBenchWarmUpSeconds() {
        return benchWarmUpSeconds;
    }

    public void setBenchWarmUpSeconds(int benchWarmUpSeconds) {
        this.benchWarmUpSeconds = benchWarmUpSeconds;
    }

    public Double getPerformanceProcessCpuLoad() {
        return performanceProcessCpuLoad;
    }

    public void setPerformanceProcessCpuLoad(Double performanceProcessCpuLoad) {
        this.performanceProcessCpuLoad = performanceProcessCpuLoad;
    }

    public Double getPerformanceProcessHeapMemoryUsed() {
        return performanceProcessHeapMemoryUsed;
    }

    public void setPerformanceProcessHeapMemoryUsed(Double performanceProcessHeapMemoryUsed) {
        this.performanceProcessHeapMemoryUsed = performanceProcessHeapMemoryUsed;
    }

    public Double getPerformanceProcessNonHeapMemoryUsed() {
        return performanceProcessNonHeapMemoryUsed;
    }

    public void setPerformanceProcessNonHeapMemoryUsed(Double performanceProcessNonHeapMemoryUsed) {
        this.performanceProcessNonHeapMemoryUsed = performanceProcessNonHeapMemoryUsed;
    }

    public Double getPerformanceSystemCpuLoad() {
        return performanceSystemCpuLoad;
    }

    public void setPerformanceSystemCpuLoad(Double performanceSystemCpuLoad) {
        this.performanceSystemCpuLoad = performanceSystemCpuLoad;
    }

    public Double getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(Double operationTime) {
        this.operationTime = operationTime;
    }

    //make flat map https://stackoverflow.com/questions/18043587/why-im-not-able-to-unwrap-and-serialize-a-java-map-using-the-jackson-java-libra/41833934
    @JsonAnyGetter
    public Map<String, String> getMetadata() {
        return metadata;
    }

    //^see above
    @JsonAnySetter
    public void addMetadata(String key, String val) {
        if (getMetadata() == null) {
            this.metadata = new HashMap<>();
        }
        getMetadata().put("metadata" + key, val);
    }

    @Override
    public String toString() {
        return "BenchmarkReport{" +
                "name='" + name + '\'' +
                ", score=" + score +
                ", units='" + units + '\'' +
                ", mode='" + mode + '\'' +
                ", category='" + category + '\'' +
                ", context='" + context + '\'' +
                ", version='" + version + '\'' +
                ", benchThreadCount=" + benchThreadCount +
                ", benchForkCount=" + benchForkCount +
                ", benchMeasurementIteration=" + benchMeasurementIteration +
                ", benchMeasurementSeconds=" + benchMeasurementSeconds +
                ", benchWarmUpIteration=" + benchWarmUpIteration +
                ", benchWarmUpSeconds=" + benchWarmUpSeconds +
                ", generatedFingerprint='" + generatedFingerprint + '\'' +
                ", manualFingerprint='" + manualFingerprint + '\'' +
                ", classFingerprint='" + classFingerprint + '\'' +
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
                ", threadsAliveCount=" + threadsAliveCount +
                ", threadsDaemonCount=" + threadsDaemonCount +
                ", threadsStartedCount=" + threadsStartedCount +
                ", threadsSafePointsCount=" + threadsSafePointsCount +
                ", threadsSafePointTime=" + threadsSafePointTime +
                ", threadsSafePointSyncTime=" + threadsSafePointSyncTime +
                ", threadsSyncContendedLockAttemptsCount=" + threadsSyncContendedLockAttemptsCount +
                ", threadsSyncMonitorDeflations=" + threadsSyncMonitorDeflations +
                ", threadsSyncMonitorInflations=" + threadsSyncMonitorInflations +
                ", threadsSyncMonitorFatMonitorsCount=" + threadsSyncMonitorFatMonitorsCount +
                ", threadsSyncMonitorFutileWakeupsCount=" + threadsSyncMonitorFutileWakeupsCount +
                ", threadsSyncNotificationsCount=" + threadsSyncNotificationsCount +
                ", threadsSafePointsInterval=" + threadsSafePointsInterval +
                ", threadsSafePointsPause=" + threadsSafePointsPause +
                ", threadsSafePointsPauseAvg=" + threadsSafePointsPauseAvg +
                ", threadsSafePointsPauseCount=" + threadsSafePointsPauseCount +
                ", threadsSafePointsPauseTTSP=" + threadsSafePointsPauseTTSP +
                ", threadsSafePointsPauseTTSPAvg=" + threadsSafePointsPauseTTSPAvg +
                ", threadsSafePointsPauseTTSPCount=" + threadsSafePointsPauseTTSPCount +
                ", threadsSyncParksCount=" + threadsSyncParksCount +
                '}';
    }
}
