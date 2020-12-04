package com.gocypher.cybench.core.utils;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityUtilsTest  {

    @Test
    public void testComputeClassHashForMethods() {
        Map<String, String> methodHashes = SecurityUtils.computeClassHashForMethods(TestBenchmarkClass.class);
      //  assertEquals(2, methodHashes.size());
      //  assertNotNull(methodHashes.get("mainBenchmark"));
      //  assertNotNull(methodHashes.get("mainBenchmark2"));
      //  assertFalse(methodHashes.get("mainBenchmark") == methodHashes.get("mainBenchmark2"));
        System.out.println(methodHashes.toString().replaceAll("\\{", "").replaceAll(", ", "\n"));

    }


    @Test
    public void concat(){
        System.out.println(new String(SecurityUtils.concatArrays("ABC".getBytes(), "DEF".getBytes())));
    }

}
