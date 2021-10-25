/*
 * Copyright (C) 2020-2021, K2N.IO.
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

package com.gocypher.cybench.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;

public class SecurityUtilsTest {

    @Test
    public void testComputeClassHashForMethods() throws ClassNotFoundException {
        if (SystemUtils.IS_JAVA_1_8) {
            Map<String, String> methodHashes = new HashMap<>();
            SecurityUtils.computeClassHashForMethods(TestBenchmarkClass.class, methodHashes);
            System.out.println(methodHashes.toString().replaceAll("\\{", "").replaceAll(", ", "\n"));

            assertEquals(4, methodHashes.size());

            if (org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS) {
                assertEquals("dffd7e1b291878c3cde3fb22ab583",
                        methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark"));
                assertEquals("a7494a7f19fa15e1c38812746d527f", methodHashes
                        .get("com.gocypher.cybench.core.utils.TestBenchmarkClass.someLibraryMethodBenchmark"));
                assertEquals("a61b28d2c39660e8d68faea3c411142",
                        methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark3"));
                assertEquals("28624525d5a4b50fb2897cc135e79e",
                        methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark4"));
            } else {
                assertEquals("6cf29d752fbaf588cfe9ad26579e4f52",
                        methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark"));
                assertEquals("98f3e718cc2ffe41b02c44e88ef251", methodHashes
                        .get("com.gocypher.cybench.core.utils.TestBenchmarkClass.someLibraryMethodBenchmark"));
                assertEquals("e58221982bbffa15b1817f74b08a63",
                        methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark3"));
                assertEquals("7d89634e196adfc23391ad3a11fa4e",
                        methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark4"));
            }

            assertNotNull(methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark"));
            assertNotNull(
                    methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.someLibraryMethodBenchmark"));
        }
    }

    @Test
    public void concat() {
        System.out.println(new String(SecurityUtils.concatArrays("ABC".getBytes(), "DEF".getBytes())));
    }

}
