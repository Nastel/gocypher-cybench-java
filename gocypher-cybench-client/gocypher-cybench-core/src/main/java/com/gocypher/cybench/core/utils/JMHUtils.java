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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301  USA
 */

package com.gocypher.cybench.core.utils;

import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.BenchmarkList;
import org.openjdk.jmh.runner.BenchmarkListEntry;
import org.openjdk.jmh.runner.format.OutputFormat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class JMHUtils {


    public static List<String> getAllBenchmarkClasses() {
        Set<BenchmarkListEntry> all = BenchmarkList.defaultList().getAll(new SilentOutputFormat(), Collections.EMPTY_LIST);
        return all.stream().map(entry -> entry.getUserClassQName()).collect(Collectors.toList());
    }

    public static Optional<Method> getBenchmarkMethod(String method, Class<?> aClass) {
        return Arrays.stream(aClass.getMethods()).filter(benchmarks -> benchmarks.getName().equals(method)).findFirst();
    }

    public static class SilentOutputFormat implements OutputFormat {
        @Override
        public void iteration(BenchmarkParams benchParams, IterationParams params, int iteration) {

        }

        @Override
        public void iterationResult(BenchmarkParams benchParams, IterationParams params, int iteration, IterationResult data) {

        }

        @Override
        public void startBenchmark(BenchmarkParams benchParams) {

        }

        @Override
        public void endBenchmark(BenchmarkResult result) {

        }

        @Override
        public void startRun() {

        }

        @Override
        public void endRun(Collection<RunResult> result) {

        }

        @Override
        public void print(String s) {

        }

        @Override
        public void println(String s) {

        }

        @Override
        public void flush() {

        }

        @Override
        public void close() {

        }

        @Override
        public void verbosePrintln(String s) {

        }

        @Override
        public void write(int b) {

        }

        @Override
        public void write(byte[] b) throws IOException {

        }
    }

    public static class ClassAndMethod {
        private String name;
        private String clazz;
        private String method;

        public ClassAndMethod(String name) {
            this.name = name;
        }

        public String getClazz() {
            return clazz;
        }

        public String getMethod() {
            return method;
        }

        public ClassAndMethod invoke() {
            clazz = name.substring(0, name.lastIndexOf('.'));
            method = name.substring(name.lastIndexOf('.') + 1);
            return this;
        }
    }
}
