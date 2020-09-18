package com.gocypher.benchmarks.runner;

import com.gocypher.benchmarks.runner.environment.model.HardwareProperties;
import com.gocypher.benchmarks.runner.environment.model.JVMProperties;
import com.gocypher.benchmarks.runner.environment.services.CollectSystemInformation;
import com.gocypher.benchmarks.runner.model.BenchmarkOverviewReport;
import com.gocypher.benchmarks.runner.report.DeliveryService;
import com.gocypher.benchmarks.runner.report.ReportingService;
import com.gocypher.benchmarks.runner.services.ConfigurationHandler;
import com.gocypher.benchmarks.runner.utils.Constants;
import com.gocypher.benchmarks.core.utils.IOUtils;
import com.gocypher.benchmarks.runner.utils.JSONUtils;
import com.gocypher.benchmarks.runner.utils.SecurityBuilder;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class BenchmarkRunner {
    private static final Logger LOG = LoggerFactory.getLogger(BenchmarkRunner.class);
    static Properties cfg = new Properties();

    public static void main (String [] args)throws Exception{
        LOG.info ("-----------------------------------------------------------------------------------------") ;
        LOG.info ("                                 Starting benchmarking                                     ") ;
        LOG.info ("-----------------------------------------------------------------------------------------") ;
        identifyPropertiesFromArguments(args);

        LOG.info ("Will collect hardware, software information...") ;
        HardwareProperties hwProperties = CollectSystemInformation.getEnvironmentProperties() ;
        LOG.info("Will collect JVM properties...") ;
        JVMProperties jvmProperties = CollectSystemInformation.getJavaVirtualMachineProperties() ;
        LOG.info ("Will execute benchmarks...") ;
        //printSystemInformation();
        // Number of separate full executions of a benchmark (warm up+measurement), this is returned still as one primary score item
        int forks = 1 ;
        //Number of measurements per benchmark operation, this is returned still as one primary score item
        int measurementIterations = 5 ;
        // number of iterations executed for warm up
        int warmUpIterations = 1 ;
        // number of seconds dedicated for each warm up iteration
        int warmUpSeconds = 5 ;
        // number of threads for benchmark test execution
        int threads = 1 ;

        /*ClassLoader CLDR = BenchmarkRunner.class.getClassLoader() ;
        URL url = CLDR.getResource("test_picture.png") ;
        System.out.println("URL:"+url);
        File srcFile = new File(url.toURI());
        System.out.println("File for I/O:"+srcFile.exists()+";"+srcFile.length());
        */
        SecurityBuilder securityBuilder = new SecurityBuilder() ;
        Reflections reflections = new Reflections("com.gocypher.benchmarks", new SubTypesScanner(false));
//        Reflections reflections = new Reflections("com.gocypher.benchmarks");
        Map<String, Object> benchmarkSetting =  new HashMap<>();
        Set<Class<? extends Object>> allClasses = reflections.getSubTypesOf(Object.class);

        OptionsBuilder optBuild = new OptionsBuilder();
        LOG.info("_______________________ BENCHMARK TESTS FOUND _________________________________");
        String tempBenchmark = null;
        if(cfg.getProperty(Constants.BENCHMARK_RUN_CLASSES) != null && !cfg.getProperty(Constants.BENCHMARK_RUN_CLASSES).equals("")){
            List<String> benchmarkNames = Arrays.stream( cfg.getProperty(Constants.BENCHMARK_RUN_CLASSES).split(",")).map(String::trim).collect(Collectors.toList());
            for (Class<? extends Object> classObj : allClasses) {
                if (!classObj.getName().isEmpty() && classObj.getSimpleName().contains("Benchmarks") && !classObj.getSimpleName().contains("_")) {
                    if(substringExistsInList(classObj.getName(),benchmarkNames)) {
                        LOG.info(classObj.getName());
                        optBuild.include(classObj.getName());
                        tempBenchmark = classObj.getName();
                        securityBuilder.generateSecurityHashForClasses(classObj);
                    }
                }
            }
        }else {
            for (Class<? extends Object> classObj : allClasses) {
                if (!classObj.getName().isEmpty() && classObj.getSimpleName().contains("Benchmarks") && !classObj.getSimpleName().contains("_")) {
                    LOG.info(classObj.getName());
                    optBuild.include(classObj.getName());
                    tempBenchmark = classObj.getName();
                    securityBuilder.generateSecurityHashForClasses(classObj);
                }
            }
        }
        //LOG.info("Situation of class signatures:{}",securityBuilder.getMapOfHashedParts()) ;
        if(tempBenchmark != null) {
            benchmarkSetting = ReportingService.getInstance().prepareBenchmarkProperties(tempBenchmark);
            benchmarkSetting.put("benchThreadCount", threads);
            if (cfg.getProperty(Constants.BENCHMARK_REPORT_NAME) != null) {
                benchmarkSetting.put("benchReportName", cfg.getProperty(Constants.BENCHMARK_REPORT_NAME));
            }
        }
//       optBuild.include(StringBenchmarks.class.getSimpleName());
//        optBuild.include(IOBenchmarks.class.getSimpleName());
//        optBuild.include(NumberBenchmarks.class.getSimpleName());


        Options opt = optBuild
                .forks(forks)
                .measurementIterations(measurementIterations)
                .warmupIterations(warmUpIterations)
                .warmupTime(TimeValue.seconds(warmUpSeconds))
                .threads(threads)
                .shouldDoGC(true)
                .addProfiler(GCProfiler.class)
                .detectJvmArgs()
                //.addProfiler(StackProfiler.class)
                //.addProfiler(HotspotMemoryProfiler.class)
                //.addProfiler(HotspotRuntimeProfiler.class)
                //.addProfiler(JavaFlightRecorderProfiler.class)

                .build();

        Runner runner = new Runner(opt);
        Collection<RunResult> results = runner.run() ;

        LOG.info ("Tests finished, executed tests count:{}",results.size()) ;

        BenchmarkOverviewReport report = ReportingService.getInstance().createBenchmarkReport(results) ;
        report.getEnvironmentSettings().put("environment",hwProperties) ;
        report.getEnvironmentSettings().put("jvmEnvironment",jvmProperties) ;
        report.getEnvironmentSettings().put("unclassifiedProperties",CollectSystemInformation.getUnclassifiedProperties());
        report.setBenchmarkSettings(benchmarkSetting);

        try {
            String reportJSON = JSONUtils.marshalToPrettyJson(report);
            LOG.info("Tests report:{}",reportJSON);
            LOG.info ("Will store results to file...") ;
            String reportEncrypted = ReportingService.getInstance().prepareReportForDelivery(securityBuilder,report) ;

            String responseWithUrl;
            if (report.isEligibleForStoringExternally() && (cfg.getProperty(Constants.SHOULD_SEND_REPORT_TO_JKOOL) == null || Boolean.parseBoolean(cfg.getProperty(Constants.SHOULD_SEND_REPORT_TO_JKOOL)))) {
                responseWithUrl = DeliveryService.getInstance().sendReportForStoring(reportEncrypted);
                //LOG.info ("responseWithUrl... {}", responseWithUrl) ;
                report.setReportURL(responseWithUrl);
            }
            reportJSON = JSONUtils.marshalToPrettyJson(report);
            IOUtils.storeResultsToFile("report.json",reportJSON);
            IOUtils.storeResultsToFile("report.cyb",reportEncrypted);

        }catch (Exception e){
            e.printStackTrace();
            LOG.error("Error during storing results to file",e);
        }

        /*for (RunResult result :results){
            System.out.println("Score:"+result.getPrimaryResult().getScore());
            System.out.println("Stats:"+result.getPrimaryResult().getStatistics());
            System.out.println("getBenchmarkResults:"+result.getBenchmarkResults().size());
            System.out.println("getAggregatedResult:"+result.getAggregatedResult().getBenchmarkResults());
            System.out.println("getSecondaryResults:"+result.getSecondaryResults());
            System.out.println("\n\n");
        }*/
        LOG.info ("-----------------------------------------------------------------------------------------") ;
        LOG.info ("                                 Finished benchmarking                                     ") ;
        LOG.info ("-----------------------------------------------------------------------------------------") ;
    }
    private static boolean substringExistsInList(String inputStr, List<String> items) {
        return items.stream().parallel().anyMatch(inputStr::contains);
    }
    private static void identifyPropertiesFromArguments(String [] args){
        String configurationFilePath = "";
        for (String property : args) {
            if (property.contains("cfg") || property.contains("config") || property.contains("configuration")) {
                String[] tempConfigPath = property.split("=");
                if(tempConfigPath.length>1) {
                    configurationFilePath = tempConfigPath[1];
                }else{
                    LOG.info("Incorrect format, configuration path syntax: cfg|config|configuration='full-file-path'");
                }
            } else {
                LOG.info("Using default configuration file");
            }
        }
        cfg = ConfigurationHandler.loadConfiguration(configurationFilePath) ;
    }
    private static void printSystemInformation (){
        long kilobytes = 1024;
        long megabytes = kilobytes * 1024;
        long gigabytes = megabytes * 1024;

        String nameOS = "os.name";
        String versionOS = "os.version";
        String architectureOS = "os.arch";
        System.out.println("\n  Info about OS");
        System.out.println("\nName of the OS: " +
                System.getProperty(nameOS));
        System.out.println("Version of the OS: " +
                System.getProperty(versionOS));
        System.out.println("Architecture of The OS: " +
                System.getProperty(architectureOS));
        Map<String, String> env = System.getenv();
        System.out.println("Environment values");
        for(String key : env.keySet()) {
            System.out.println("K: " + key + " \n\tV: " + env.get(key));
        }
        /* Total number of processors or cores available to the JVM */
        System.out.println("\nAvailable processors (cores): " +
                Runtime.getRuntime().availableProcessors());

        /* Total amount of free memory available to the JVM */
        System.out.println("Free memory (megabytes): " +
                Runtime.getRuntime().freeMemory() / (float) megabytes);

        /* This will return Long.MAX_VALUE if there is no preset limit */
        long maxMemory = Runtime.getRuntime().maxMemory();
        /* Maximum amount of memory the JVM will attempt to use */
        System.out.println("Maximum memory (megabytes): " +
                (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory / (float) megabytes));

        /* Total memory currently available to the JVM */
        System.out.println("Total memory available to JVM (megabytes): " +
                Runtime.getRuntime().totalMemory() / (float) megabytes);

        /* Get a list of all filesystem roots on this system */
        File[] roots = File.listRoots();

        /* For each filesystem root, print some info */
        for (File root : roots) {
            System.out.println("\nFile system root: " + root.getAbsolutePath());
            System.out.println("Total space (gigabytes): " + (root.getTotalSpace() / (float) gigabytes));
            System.out.println("Free space (gigabytes): " + (root.getFreeSpace() / (float) gigabytes));
            System.out.println("Usable space (gigabytes): " + (root.getUsableSpace() / (float) gigabytes));

        }
        System.out.println("\n\nProperties:\n------\n");
        System.getProperties().list(System.out);

    }
    public static void printGCStats() {
        long totalGarbageCollections = 0;
        long garbageCollectionTime = 0;

        for(GarbageCollectorMXBean gc :
                ManagementFactory.getGarbageCollectorMXBeans()) {
            System.out.println("GC:"+gc.getName()+";"+gc.getClass());


            long count = gc.getCollectionCount();

            if(count >= 0) {
                totalGarbageCollections += count;
            }

            long time = gc.getCollectionTime();

            if(time >= 0) {
                garbageCollectionTime += time;
            }
        }

        System.out.println("Total Garbage Collections: "
                + totalGarbageCollections);
        System.out.println("Total Garbage Collection Time (ms): "
                + garbageCollectionTime);
    }

}
