
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.gocypher.cybench.core.utils;

//import com.gocypher.cybench.core.annotation.BenchmarkTag;
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
    @BenchmarkMode({Mode.Throughput})
    @Benchmark
//    @BenchmarkTag(
//            tag = "a5a0881c-5481-4dac-a640-f1fe6d3c913c"
//    )
    public void mainBenchmark(Blackhole bh) {
        bh.consume(Runtime.getRuntime().availableProcessors());
    }

    @OutputTimeUnit(TimeUnit.SECONDS)
    @BenchmarkMode({Mode.Throughput})
    @Benchmark
    public void mainBenchmark2(Blackhole bh) {
        bh.consume(System.getProperties().getProperty("ABS"));
    }

    @OutputTimeUnit(TimeUnit.SECONDS)
    @BenchmarkMode({Mode.Throughput})
    @Benchmark
    public void mainBenchmark3(Blackhole bh) {
        bh.consume(System.getProperties().getProperty("DEF"));
    }

    @Benchmark
    public void mainBenchmark3(Blackhole bh, String a) {
        bh.consume(System.getProperties().getProperty("DEF"));
    }


    public void notBenchmark(Blackhole bh) {

    }
}
