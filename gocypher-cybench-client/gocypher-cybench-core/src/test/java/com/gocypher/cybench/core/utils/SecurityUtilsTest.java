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
        assertEquals(3, methodHashes.size());
        assertNotNull(methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark2"));
        assertNotNull(methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark"));
        System.out.println(methodHashes.toString().replaceAll("\\{", "").replaceAll(", ", "\n"));

    }


    @Test
    public void concat(){
        System.out.println(new String(SecurityUtils.concatArrays("ABC".getBytes(), "DEF".getBytes())));
    }

}
