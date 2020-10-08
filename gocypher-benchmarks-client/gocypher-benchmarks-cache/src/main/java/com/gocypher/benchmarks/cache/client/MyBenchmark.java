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

package com.gocypher.benchmarks.cache.client;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MyBenchmark {

    @Benchmark
    public int testStringConcatenation() {
        List<String>list = new ArrayList<>() ;
        String s1 = "Test " ;
        try {
            for (int i = 0; i < 100000;i++){
                list.add(s1+"-"+i) ;
            }
        }catch (Exception e){

        }
        return list.size() ;
    }

    @Benchmark
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    @BenchmarkMode(Mode.Throughput)
    public int testRandomGenerationAndSort() {
        // This is a demo/sample template for building your JMH benchmarks. Edit as needed.
        // Put your benchmark code here.
        List<Integer> list = new ArrayList<>() ;
        try {
            for (int i = 0; i < 1000000;i++){
                Random r = new Random() ;
                list.add(r.nextInt()*1000) ;
            }
            Collections.sort(list);
        }catch (Exception e){

        }
        return list.size() ;
    }
    /*@Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void testMethod3() {
        Blackhole.consumeCPU(1);
    }
    */
   /* @State(Scope.Benchmark)
    public static class BenchmarkState {
        volatile double x = Math.PI;
    }

    @State(Scope.Thread)
    public static class ThreadState {
        volatile double x = Math.PI;
    }
    @Benchmark
    public void measureUnshared(ThreadState state) {
        // All benchmark threads will call in this method.
        //
        // However, since ThreadState is the Scope.Thread, each thread
        // will have it's own copy of the state, and this benchmark
        // will measure unshared case.
        state.x++;
    }
    @Benchmark
    public void measureShared(BenchmarkState state) {
        // All benchmark threads will call in this method.
        //
        // Since BenchmarkState is the Scope.Benchmark, all threads
        // will share the state instance, and we will end up measuring
        // shared case.
        state.x++;
    }
    */

}
