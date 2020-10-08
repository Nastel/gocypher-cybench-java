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

package com.gocypher.benchmarks.runner.report;

import com.gocypher.benchmarks.core.model.BaseBenchmark;
import com.gocypher.benchmarks.core.utils.SecurityUtils;
import com.gocypher.benchmarks.runner.model.BenchmarkOverviewReport;
import com.gocypher.benchmarks.runner.model.BenchmarkReport;
import com.gocypher.benchmarks.runner.model.SecuredReport;
import com.gocypher.benchmarks.runner.utils.JSONUtils;
import com.gocypher.benchmarks.runner.utils.SecurityBuilder;
import org.openjdk.jmh.results.RunResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

public class ReportingService {
    private static final Logger LOG = LoggerFactory.getLogger(ReportingService.class);
    private static ReportingService instance ;

    private ReportingService (){

    }
    public static ReportingService getInstance (){
        if (instance == null ){
            instance = new ReportingService () ;
        }
        return instance ;
    }

    public BenchmarkOverviewReport createBenchmarkReport (Collection<RunResult> jmhResults,Map<String,Map<String,String>>customBenchmarksMetadata){
        BenchmarkOverviewReport overviewReport = new BenchmarkOverviewReport() ;

        for (RunResult item :jmhResults){
            BenchmarkReport report = new BenchmarkReport() ;
            if (item.getPrimaryResult() != null) {
                report.setScore(item.getPrimaryResult().getScore());
                report.setUnits(item.getPrimaryResult().getScoreUnit());
                if (item.getPrimaryResult().getStatistics() != null){
                    report.setN(item.getPrimaryResult().getStatistics().getN());
                    report.setMeanScore(item.getPrimaryResult().getStatistics().getMean());
                    report.setMaxScore(item.getPrimaryResult().getStatistics().getMax());
                    report.setMinScore(item.getPrimaryResult().getStatistics().getMin());
                    if (!Double.isNaN(item.getPrimaryResult().getStatistics().getStandardDeviation())) {
                        report.setStdDevScore(item.getPrimaryResult().getStatistics().getStandardDeviation());
                    }
                }

            }
            if (item.getParams() != null) {
                report.setName(item.getParams().getBenchmark());
                report.setMode(item.getParams().getMode().shortLabel());
                //System.out.println("id:"+item.getParams().id());
                //System.out.println("Mode"+item.getParams().getMode().longLabel());
            }

            report.setGcCalls(getScoreFromJMHSecondaryResult(item,"·gc.count"));
            report.setGcTime(getScoreFromJMHSecondaryResult(item,"·gc.time"));
            report.setGcAllocationRate(getScoreFromJMHSecondaryResult(item,"·gc.alloc.rate"));
            report.setGcAllocationRateNorm(getScoreFromJMHSecondaryResult(item,"·gc.alloc.rate.norm"));
            report.setGcChurnPsEdenSpace(getScoreFromJMHSecondaryResult(item,"·gc.churn.PS_Eden_Space"));
            report.setGcChurnPsEdenSpaceNorm(getScoreFromJMHSecondaryResult(item,"·gc.churn.PS_Eden_Space.norm"));
            report.setGcChurnPsSurvivorSpace(getScoreFromJMHSecondaryResult(item,"·gc.churn.PS_Survivor_Space"));
            report.setGcChurnPsSurvivorSpaceNorm(getScoreFromJMHSecondaryResult(item,"·gc.churn.PS_Survivor_Space.norm"));

            report.setThreadsAliveCount(getScoreFromJMHSecondaryResult(item,"·threads.alive"));
            report.setThreadsDaemonCount(getScoreFromJMHSecondaryResult(item,"·threads.daemon"));
            report.setThreadsStartedCount(getScoreFromJMHSecondaryResult(item,"·threads.started"));

            report.setThreadsSafePointSyncTime(getScoreFromJMHSecondaryResult(item,"·rt.safepointSyncTime"));
            report.setThreadsSafePointTime(getScoreFromJMHSecondaryResult(item,"·rt.safepointTime"));
            report.setThreadsSafePointsCount(getScoreFromJMHSecondaryResult(item,"·rt.safepoints"));

            report.setThreadsSyncContendedLockAttemptsCount(getScoreFromJMHSecondaryResult(item,"·rt.sync.contendedLockAttempts"));
            report.setThreadsSyncMonitorFatMonitorsCount(getScoreFromJMHSecondaryResult(item,"·rt.sync.fatMonitors"));
            report.setThreadsSyncMonitorFutileWakeupsCount(getScoreFromJMHSecondaryResult(item,"·rt.sync.futileWakeups"));
            report.setThreadsSyncMonitorDeflations(getScoreFromJMHSecondaryResult(item,"·rt.sync.monitorDeflations"));
            report.setThreadsSyncMonitorInflations(getScoreFromJMHSecondaryResult(item,"·rt.sync.monitorInflations"));
            report.setThreadsSyncNotificationsCount(getScoreFromJMHSecondaryResult(item,"·rt.sync.notifications"));

            report.setThreadsSyncParksCount(getScoreFromJMHSecondaryResult(item,"·rt.sync.parks"));

            /*System.out.println("Score:"+result.getPrimaryResult().getScore());
            System.out.println("Stats:"+result.getPrimaryResult().getStatistics());
            System.out.println("getBenchmarkResults:"+result.getBenchmarkResults().size());
            System.out.println("getAggregatedResult:"+result.getAggregatedResult().getBenchmarkResults());
            System.out.println("getSecondaryResults:"+result.getSecondaryResults());
            System.out.println("\n\n");
            */

            //System.out.println("Report class name:"+report.getReportClassName());

            report.setCategory(resolveCategory(report.getReportClassName(),customBenchmarksMetadata));
            report.recalculateScoresToMatchNewUnits();

            overviewReport.addToBenchmarks(report);
        }

        overviewReport.setTimestamp(System.currentTimeMillis());
        overviewReport.setTimestampUTC(ZonedDateTime.now( ZoneOffset.UTC ).toInstant().toEpochMilli());
        overviewReport.computeScores();

        return overviewReport ;
    }

    public Map<String, Object> prepareBenchmarkProperties(String className,Map<String,Map<String,String>>customBenchmarksMetadata){
        Map<String, Object> benchmarkProperties = new HashMap<>();
        if (benchmarkProperties.get("benchVersion") == null) {
            benchmarkProperties.put("benchVersion", getVersion(className));
        }
        if (benchmarkProperties.get("benchContext") == null) {
            benchmarkProperties.put("benchContext", resolveContext(className, customBenchmarksMetadata));
        }
        return benchmarkProperties;
    }

    private String resolveCategory (String fullClassName, Map<String,Map<String,String>>customBenchmarksMetadata){
        try {
            Class clazz = Class.forName(fullClassName);
            Object obj = clazz.newInstance() ;
            if (obj instanceof BaseBenchmark) {
                return ((BaseBenchmark)obj).getCategory() ;
            }
            else {
                if (customBenchmarksMetadata.get(fullClassName)!= null){
                    if (customBenchmarksMetadata.get(fullClassName).get("category") != null) {
                        return customBenchmarksMetadata.get(fullClassName).get("category") ;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            LOG.error ("Error on resolving category",e) ;
        }
        return "Custom" ;
    }

    private String resolveContext (String fullClassName, Map<String,Map<String,String>>customBenchmarksMetadata){
        try {
            Class clazz = Class.forName(fullClassName);
            Object obj = clazz.newInstance() ;
            if (obj instanceof BaseBenchmark) {
                return ((BaseBenchmark)obj).getContext() ;
            }
            /*else {
                if (customBenchmarksMetadata.get(fullClassName)!= null){
                    if (customBenchmarksMetadata.get(fullClassName).get("context") != null) {
                        return customBenchmarksMetadata.get(fullClassName).get("context") ;
                    }
                }
            }*/
        }catch (Exception e){
            e.printStackTrace();
            LOG.error ("Error on resolving category",e) ;
        }
        return "Other" ;
    }

    private String getVersion(String fullClassName) {
        try {
            Class clazz = Class.forName(fullClassName);
            //URLClassLoader cl = (URLClassLoader) ReportingService.class.getClassLoader();
            //URL url = cl.findResource("META-INF/MANIFEST.MF");
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            //Manifest manifest = new Manifest(url.openStream());
            Manifest manifest = new Manifest(loader.getResourceAsStream("META-INF/MANIFEST.MF"));
            String benchmarkPackageString = clazz.getPackage().getName().replace(".", "-")+"-version";
            return manifest.getMainAttributes().getValue(benchmarkPackageString);
        } catch (Exception e) {
            LOG.info("Could not locate the benchmark version",e);
        }
        return null;
    }
    public String prepareReportForDelivery (SecurityBuilder securityBuilder,BenchmarkOverviewReport report){
        LOG.info("Will encrypt and sign...") ;
        SecuredReport securedReport = createSecuredReport(report) ;

        securityBuilder.generateSecurityHashForReport(securedReport.getReport());
        //LOG.info("Report for hashing:{}",securedReport.getReport());
        securedReport.setSignatures(securityBuilder.buildSignatures());

        String plainReport = JSONUtils.marshalToJson(securedReport) ;
        return SecurityUtils.encryptReport(plainReport) ;


    }
    private SecuredReport createSecuredReport (BenchmarkOverviewReport report){
        SecuredReport securedReport = new SecuredReport() ;
        securedReport.setReport(JSONUtils.marshalToJson(report));
        /*securedReport.setReportURL(report.getReportURL());
        securedReport.setBenchmarkSettings(report.getBenchmarkSettings());
        securedReport.setTimestamp(report.getTimestamp());
        securedReport.setTimestampUTC(report.getTimestampUTC());
        securedReport.setBenchmarks(report.getBenchmarks());
        securedReport.setCategoriesOverview(report.getCategoriesOverview());
        securedReport.setEnvironmentSettings(report.getEnvironmentSettings());
        securedReport.setTotalScore(report.getTotalScore());
        */

        return securedReport ;
    }
    private Double getScoreFromJMHSecondaryResult (RunResult result,String key){
        if (result != null && result.getSecondaryResults() != null){
            if (result.getSecondaryResults().get(key)!= null){
                return result.getSecondaryResults().get(key).getScore() ;
            }
        }
        return null ;
    }

}
