package com.gocypher.benchmarks.jvm.client.tests;

import com.gocypher.benchmarks.core.model.BaseBenchmark;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@State(Scope.Benchmark)
public class NumberBenchmarks extends BaseBenchmark {

    @Param({"10000000"})
    public int listSize;

    public List<Integer> testList;

    @Setup
    public void setUp() {
        testList = new Random()
                .ints()
                .limit(listSize)
                .boxed()
                .collect(Collectors.toList());
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void sumNumbersLongStream (Blackhole blackHole){
        long sum = testList.parallelStream().mapToLong(s -> s).sum();
        blackHole.consume(sum);
    }
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void sumNumbersAtomicLongStream (Blackhole blackHole){
        AtomicLong atomicLong = new AtomicLong();
        testList.parallelStream().forEach(atomicLong::addAndGet);
        blackHole.consume(atomicLong.get());
    }

    @TearDown
    public void cleanUp (){
        if (this.testList != null) {
            this.testList.clear();
        }
    }
    @Override
    public String getCategory() {
        return "NUMBERS";
    }

    @Override
    public String getContext() {
        return "JVM";
    }
}
