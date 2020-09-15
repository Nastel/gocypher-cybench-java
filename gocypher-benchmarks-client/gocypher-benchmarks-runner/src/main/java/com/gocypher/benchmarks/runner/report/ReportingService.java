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

    public BenchmarkOverviewReport createBenchmarkReport (Collection<RunResult> jmhResults){
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
            if (item.getSecondaryResults() != null){
                //gc.alloc.rate=12.430 MB/sec, ·gc.alloc.rate.norm=20849.130 B/op, ·gc.churn.PS_Eden_Space=12.221 MB/sec, ·gc.churn.PS_Eden_Space.norm=20497.921 B/op, ·gc.count=2.000 counts, gc.time=10.000 ms, ·stack=<delayed till summary>
                //System.out.println("GC:"+item.getSecondaryResults().keySet());
                if (item.getSecondaryResults().get("·gc.count") != null) {
                    report.setGcCalls(item.getSecondaryResults().get("·gc.count").getScore());
                }
                if (item.getSecondaryResults().get("·gc.time") != null) {
                    report.setGcTime(item.getSecondaryResults().get("·gc.time").getScore());
                }
                if (item.getSecondaryResults().get("·gc.alloc.rate") != null) {
                    //report.setGcTime(item.getSecondaryResults().get("·gc.time").getScore());
                }
                if (item.getSecondaryResults().get("·gc.alloc.rate.norm") != null) {

                }
            }
            /*System.out.println("Score:"+result.getPrimaryResult().getScore());
            System.out.println("Stats:"+result.getPrimaryResult().getStatistics());
            System.out.println("getBenchmarkResults:"+result.getBenchmarkResults().size());
            System.out.println("getAggregatedResult:"+result.getAggregatedResult().getBenchmarkResults());
            System.out.println("getSecondaryResults:"+result.getSecondaryResults());
            System.out.println("\n\n");
            */

            //System.out.println("Report class name:"+report.getReportClassName());

            report.setCategory(resolveCategory(report.getReportClassName()));

            overviewReport.addToBenchmarks(report);
        }

        overviewReport.setTimestamp(System.currentTimeMillis());
        overviewReport.setTimestampUTC(ZonedDateTime.now( ZoneOffset.UTC ).toInstant().toEpochMilli());
        overviewReport.computeScores();

        return overviewReport ;
    }

    public Map<String, Object> prepareBenchmarkProperties(String className){
        Map<String, Object> benchmarkProperties = new HashMap<>();
        benchmarkProperties.put("benchVersion", getVersion(className));
        benchmarkProperties.put("benchContext", resolveContext(className));
        return benchmarkProperties;
    }

    private String resolveCategory (String fullClassName){
        try {
            Class clazz = Class.forName(fullClassName);
            BaseBenchmark obj = (BaseBenchmark)clazz.newInstance() ;
            return obj.getCategory() ;

        }catch (Exception e){
            e.printStackTrace();
            LOG.error ("Error on resolving category",e) ;
        }
        return null ;
    }

    private String resolveContext (String fullClassName){
        try {
            Class clazz = Class.forName(fullClassName);
            BaseBenchmark obj = (BaseBenchmark)clazz.newInstance() ;
            return obj.getContext() ;

        }catch (Exception e){
            e.printStackTrace();
            LOG.error ("Error on resolving category",e) ;
        }
        return null ;
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
}
