package com.gocypher.benchmarks.jvm.client.tests;

import com.gocypher.benchmarks.core.model.BaseBenchmark;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@State(Scope.Benchmark)
public class NumberBenchmarks extends BaseBenchmark {

    @Param({"1000000"})
    public int listSize;

    public List<Integer> testList;

    Random randomGenerator;
    double rangeMax = 100000 ;
    double rangeMin = 0 ;

    @Setup
    public void setUp() {
        /*testList = new Random()
                .ints()
                .limit(listSize)
                .boxed()
                .collect(Collectors.toList());
                */
        randomGenerator = new Random();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void generateAndAddDoubleNumbers (Blackhole blackHole){
        Double sum = 0.0 ;
        sum += Double.valueOf(rangeMin + (rangeMax - rangeMin) * this.randomGenerator.nextDouble()) ;
        sum += Double.valueOf(rangeMin + (rangeMax - rangeMin) *this.randomGenerator.nextDouble() );
        blackHole.consume(sum);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void generateAndAddAtomicNumbers (Blackhole blackHole){
        int num = (int)(rangeMin + (int) (new Random().nextFloat() * (rangeMax - rangeMin)));
        AtomicLong atomicLong = new AtomicLong() ;
        atomicLong.addAndGet(Long.valueOf(num));
        int num2 = (int)(rangeMin + (int) (new Random().nextFloat() * (rangeMax - rangeMin)));
        atomicLong.addAndGet(Long.valueOf(num2));
        Long result = atomicLong.get() ;
        blackHole.consume(result);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void generateAndAddBigDecimalNumbers (Blackhole blackHole){
        int num = (int)(rangeMin + (int) (new Random().nextFloat() * (rangeMax - rangeMin)));
        BigDecimal sum = BigDecimal.ZERO ;
        sum = sum.add(BigDecimal.valueOf(num)) ;
        int num2 = (int)(rangeMin + (int) (new Random().nextFloat() * (rangeMax - rangeMin)));
        sum = sum.add(BigDecimal.valueOf(num2)) ;
        blackHole.consume(sum);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void generateAndLogarithmDoubleNumbers (Blackhole blackHole){
        Double number= Double.valueOf(Math.log10(rangeMin + (rangeMax - rangeMin) * this.randomGenerator.nextDouble())) ;
        blackHole.consume(number);
    }
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void generateAndPowerDoubleNumbers (Blackhole blackHole){
        Double number= Double.valueOf(Math.pow(rangeMin + (rangeMax - rangeMin) * this.randomGenerator.nextDouble(),10)) ;
        blackHole.consume(number);
    }
    /*@Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void sumNumbersAtomicLongStream (Blackhole blackHole){
        AtomicLong atomicLong = new AtomicLong();
        testList.parallelStream().forEach(atomicLong::addAndGet);
        blackHole.consume(atomicLong.get());
    }
    */

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
