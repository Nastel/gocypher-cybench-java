package com.gocypher.benchmarks.jvm.test;

import com.gocypher.benchmarks.jvm.client.tests.IOBenchmarks;
import com.gocypher.benchmarks.jvm.client.tests.NumberBenchmarks;
import com.gocypher.benchmarks.jvm.client.tests.StringBenchmarks;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class BenchmarksTest {
    private static final Logger LOG = LoggerFactory.getLogger(BenchmarksTest.class) ;

    public static void main (String []args)throws Exception{

        // Number of separate full executions of a benchmark (warm up+measurement), this is returned still as one primary score item
        int forks = 1 ;
        //Number of measurements per benchmark operation, this is returned still as one primary score item
        int measurementIterations = 1 ;
        // number of iterations executed for warm up
        int warmUpIterations = 1 ;
        // number of seconds dedicated for each warm up iteration
        int warmUpSeconds = 5 ;
        // number of threads for benchmark test execution
        int threads = 1 ;


        OptionsBuilder optBuild = new OptionsBuilder();

        //optBuild.include(StringBenchmarks.class.getSimpleName());
        optBuild.include(IOBenchmarks.class.getSimpleName());
        //optBuild.include(NumberBenchmarks.class.getSimpleName());

        Options opt = optBuild
                .forks(forks)
                .measurementIterations(measurementIterations)
                .warmupIterations(warmUpIterations)
                .warmupTime(TimeValue.seconds(warmUpSeconds))
                .threads(threads)
                .shouldDoGC(true)
                //.addProfiler(GCProfiler.class)
                //.addProfiler(StackProfiler.class)
                //.addProfiler(HotspotMemoryProfiler.class)
                //.addProfiler(HotspotRuntimeProfiler.class)
                //.addProfiler(JavaFlightRecorderProfiler.class)

                .build();

        Runner runner = new Runner(opt);
        Collection<RunResult> results = runner.run() ;

        LOG.info ("Tests finished, executed tests count:{}",results.size()) ;

    }
}
