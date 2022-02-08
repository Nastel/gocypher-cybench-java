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

package com.gocypher.cybench.launcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.gocypher.cybench.core.annotation.BenchmarkMetaData;
import com.gocypher.cybench.launcher.model.BenchmarkReport;

class BenchmarkRunnerTest {
    @Test
    public void testMetadataPropagation() {
        BenchmarkReport benchmarkReport = new BenchmarkReport();
        BenchmarkRunner.appendMetadataFromClass(TestClass.class, benchmarkReport);
        benchmarkReport.getMetadata().forEach((k, v) -> System.out.println(k + " : " + v));
        assertEquals(5, benchmarkReport.getMetadata().size());
    }

    @BenchmarkMetaData(key = "A", value = "B")
    public static class TestSuperClass {

    }

    @BenchmarkMetaData(key = "C", value = "D")
    public static class TestClass extends TestSuperClass implements E, G {

    }

    @BenchmarkMetaData(key = "E", value = "F")
    public interface E extends I {

    }

    @BenchmarkMetaData(key = "G", value = "H")
    public interface G {

    }

    @BenchmarkMetaData(key = "I", value = "J")
    public interface I {

    }
}