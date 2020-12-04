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
    @BenchmarkTag(tag = "74d4fc4b-210e-4d41-b532-cc904a1edd7c")
    public // )
    void mainBenchmark(Blackhole bh) {
        bh.consume(Runtime.getRuntime().availableProcessors());
    }

    @OutputTimeUnit(TimeUnit.SECONDS)
    @BenchmarkMode({ Mode.Throughput })
    @Benchmark
    @BenchmarkTag(tag = "b41b7a3c-79a0-4190-a8e6-85c4bdb504d6")
    public void mainBenchmark2(Blackhole bh) {
        bh.consume(System.getProperties().getProperty("ABS"));
    }

    @OutputTimeUnit(TimeUnit.SECONDS)
    @BenchmarkMode({ Mode.Throughput })
    @Benchmark
    @BenchmarkTag(tag = "7fd319aa-29c3-4371-a0d6-f652b779faf2")
    public void mainBenchmark3(Blackhole bh) {
        bh.consume(System.getProperties().getProperty("DEF"));
    }

    @Benchmark
    @BenchmarkTag(tag = "dea83398-1f94-4162-a73c-e017af3714e8")
    public void mainBenchmark3(Blackhole bh, String a) {
        bh.consume(System.getProperties().getProperty("DEF"));
    }

    public void notBenchmark(Blackhole bh) {
    }
}
