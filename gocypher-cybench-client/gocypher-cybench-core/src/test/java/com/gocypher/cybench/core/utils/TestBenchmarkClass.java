// 
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
// 
package com.gocypher.cybench.core.utils;

// import com.gocypher.cybench.core.annotation.BenchmarkTag;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;
import com.gocypher.cybench.core.annotation.BenchmarkTag;

@State(Scope.Benchmark)
public class TestBenchmarkClass {

    public TestBenchmarkClass() {
    }

    @Setup
    public void setup() {
    }

    @TearDown
    public void teardown() {
    }

    @OutputTimeUnit(TimeUnit.SECONDS)
    @BenchmarkMode({ Mode.Throughput })
    @Benchmark
    @BenchmarkTag(tag = "7fd319aa-29c3-4371-a0d6-f652b779faf2")
    public void mainBenchmark3(Blackhole bh) {
    }

    @Benchmark
    @BenchmarkTag(tag = "dea83398-1f94-4162-a73c-e017af3714e8")
    public void mainBenchmark3(Blackhole bh, String a) {
        bh.consume(System.getProperties().getProperty("DEF"));
    }

    public void notBenchmark(Blackhole bh) {
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @BenchmarkTag(tag = "a1ee9ef4-f3c6-4552-b971-aeec7cc77f43")
    public void someLibraryMethodBenchmark(Blackhole bh) {
        // TODO fill up benchmark method with logic
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @BenchmarkTag(tag = "6bf07a5b-86c0-4f2b-bc6b-23c6934810d3")
    public void mainBenchmark(Blackhole bh) {
        // TODO fill up benchmark method with logic
    }
}
