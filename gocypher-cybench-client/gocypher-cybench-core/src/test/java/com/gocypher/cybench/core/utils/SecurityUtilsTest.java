package com.gocypher.cybench.core.utils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityUtilsTest  {

    @Test
    public void testComputeClassHashForMethods() {
        Map<String, String> methodHashes = new HashMap<>();
        SecurityUtils.computeClassHashForMethods(TestBenchmarkClass.class, methodHashes);
        System.out.println(methodHashes.toString().replaceAll("\\{", "").replaceAll(", ", "\n"));

        assertEquals(3, methodHashes.size());
        assertEquals("6cf29d752fbaf588cfe9ad26579e4f52", methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark"));
        assertEquals("98f3e718cc2ffe41b02c44e88ef251", methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.someLibraryMethodBenchmark"));
        assertEquals("a3cb9cb15721a54e9d8e34f1c36e542",methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark3"));

        assertNotNull(methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark"));
        assertNotNull(methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.someLibraryMethodBenchmark"));

    }


    @Test
    public void concat(){
        System.out.println(new String(SecurityUtils.concatArrays("ABC".getBytes(), "DEF".getBytes())));
    }

}
