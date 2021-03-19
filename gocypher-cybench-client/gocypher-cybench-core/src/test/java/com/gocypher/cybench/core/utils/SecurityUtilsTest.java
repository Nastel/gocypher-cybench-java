package com.gocypher.cybench.core.utils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityUtilsTest  {

    @Test
    public void testComputeClassHashForMethods() throws ClassNotFoundException {
        Map<String, String> methodHashes = new HashMap<>();
        SecurityUtils.computeClassHashForMethods(TestBenchmarkClass.class, methodHashes);
        System.out.println(methodHashes.toString().replaceAll("\\{", "").replaceAll(", ", "\n"));

        assertEquals(3, methodHashes.size());
        assertEquals("dffd7e1b291878c3cde3fb22ab583", methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark"));
        assertEquals("a7494a7f19fa15e1c38812746d527f", methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.someLibraryMethodBenchmark"));
        assertEquals("ddaaa53cec636ee71b8d4baf9f9f29",methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark3"));

        assertNotNull(methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.mainBenchmark"));
        assertNotNull(methodHashes.get("com.gocypher.cybench.core.utils.TestBenchmarkClass.someLibraryMethodBenchmark"));

    }


    @Test
    public void concat(){
        System.out.println(new String(SecurityUtils.concatArrays("ABC".getBytes(), "DEF".getBytes())));
    }

}
