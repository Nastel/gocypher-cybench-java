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

package com.gocypher.cybench.core.utils;

import static org.junit.jupiter.api.Assertions.*;

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

            assertEquals(5, methodHashes.size());

            if (org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS) {
                assertEquals("dffd7e1b291878c3cde3fb22ab583",
                        methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark"));
                assertEquals("a7494a7f19fa15e1c38812746d527f", methodHashes
                        .get("com.gocypher.cybench.core.utils.TestBenchmarkClass.someLibraryMethodBenchmark"));
                assertEquals("a61b28d2c39660e8d68faea3c411142",
                        methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark3"));
                assertEquals("28624525d5a4b50fb2897cc135e79e",
                        methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark4"));
                assertEquals("9c9bb51e96e98ef193d5a871f3488ef",
                        methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.untaggedBenchmark"));
            } else {
                assertEquals("6cf29d752fbaf588cfe9ad26579e4f52",
                        methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark"));
                assertEquals("98f3e718cc2ffe41b02c44e88ef251", methodHashes
                        .get("com.gocypher.cybench.core.utils.TestBenchmarkClass.someLibraryMethodBenchmark"));
                assertEquals("e58221982bbffa15b1817f74b08a63",
                        methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark3"));
                assertEquals("7d89634e196adfc23391ad3a11fa4e",
                        methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark4"));
                assertEquals("a052b3357a807f16946594aeed8ac1aa",
                        methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.untaggedBenchmark"));
            }

            assertNotNull(methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark"));
            assertNotNull(
                    methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.someLibraryMethodBenchmark"));
        }
    }

    @Test
    public void concat() {
        String catStyring = new String(SecurityUtils.concatArrays("ABC".getBytes(), "DEF".getBytes()));

        assertEquals("ABCDEF", catStyring);
    }

    @Test
    public void testMethodFingerprints() throws Exception {
        Map<String, String> manualFingerprints = new HashMap<>();
        Map<String, String> classFingerprints = new HashMap<>();

        SecurityUtils.generateMethodFingerprints(TestBenchmarkClass.class, manualFingerprints, classFingerprints);

        assertTrue(manualFingerprints.size() == 5);
        assertTrue(classFingerprints.size() == 15);

        assertEquals("6bf07a5b-86c0-4f2b-bc6b-23c6934810d3",
                manualFingerprints.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark"));
        assertEquals("a1ee9ef4-f3c6-4552-b971-aeec7cc77f43", manualFingerprints
                .get("com.gocypher.cybench.core.utils.TestBenchmarkClass.someLibraryMethodBenchmark"));
        assertEquals("7fd319aa-29c3-4371-a0d6-f652b779faf2",
                manualFingerprints.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark3"));
        assertEquals("dea83398-1f94-4162-a73c-e017af3714e8",
                manualFingerprints.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark4"));
        assertEquals("00000000-0000-0000-0000-000000000000",
                manualFingerprints.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.untaggedBenchmark"));

        for (String cf : classFingerprints.values()) {
            assertEquals("b1b58f40a08bd1d689bc22d52ac1086", cf);
        }
    }

}
