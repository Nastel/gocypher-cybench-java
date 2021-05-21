package com.gocypher.cybench.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class SecurityUtilsTest {

    @Test
    public void testComputeClassHashForMethods() throws ClassNotFoundException {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.8")) {
            Map<String, String> methodHashes = new HashMap<>();
            SecurityUtils.computeClassHashForMethods(TestBenchmarkClass.class, methodHashes);
            System.out.println(methodHashes.toString().replaceAll("\\{", "").replaceAll(", ", "\n"));

            assertEquals(4, methodHashes.size());
            assertEquals("dffd7e1b291878c3cde3fb22ab583", methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark"));
            assertEquals("a7494a7f19fa15e1c38812746d527f", methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.someLibraryMethodBenchmark"));
            assertEquals("a61b28d2c39660e8d68faea3c411142", methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark3"));
            assertEquals("28624525d5a4b50fb2897cc135e79e", methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark4"));

            assertNotNull(methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark"));
            assertNotNull(methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.someLibraryMethodBenchmark"));
        }

    }

    @Test
    public void concat() {
        System.out.println(new String(SecurityUtils.concatArrays("ABC".getBytes(), "DEF".getBytes())));
    }

}
