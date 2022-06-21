/*
 * Copyright (C) 2020-2022, K2N.IO.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 */

// 
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
// 

package com.gocypher.cybench.core.utils;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
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
    public void mainBenchmark4(Blackhole bh, BenchmarkParams a) {
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

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @BenchmarkTag(tag = "00000000-0000-0000-0000-000000000000")
    public void untaggedBenchmark(Blackhole bh, BenchmarkParams prms) {
        // TODO fill up benchmark method with logic
    }
}
