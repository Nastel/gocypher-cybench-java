package com.gocypher.benchmarks.jvm.client.tests;

import com.gocypher.benchmarks.core.model.BaseBenchmark;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class StringBenchmarks extends BaseBenchmark {

    private static final String PART1 = "for a real test, make this big";
    private static final String PART2 = "so that we can see GC effects";
    private static final String REGEX = "XXX";
    private static final String REPL = " -- ";
    private static final String TEMPLATE = PART1 + REGEX + PART2;

    @Param({"1000000"})
    public int iterations;

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public int replaceBenchmark (){
        return executeReplace(iterations, TEMPLATE, REGEX, REPL);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void concatBenchmark (){
        executeConcat(iterations, TEMPLATE, REGEX, REPL);
    }

    private static int executeConcat(int reps, String part1, String part2, String repl){
        int concatCount = 0 ;
        for (int ii = 0 ; ii < reps ; ii++){
            String s = part1 + repl + part2;
            concatCount++ ;
        }
        return concatCount ;
    }

    private static int executeReplace(int reps, String template, String regex, String repl) {
        int replaceCount = 0 ;
        for (int ii = 0 ; ii < reps ; ii++){
            String s = template.replaceAll(regex, repl);
            replaceCount++ ;
        }
        return replaceCount ;
    }

    @Override
    public String getCategory() {
        return "STRINGS";
    }

    @Override
    public String getContext() {
        return "JVM";
    }
}
