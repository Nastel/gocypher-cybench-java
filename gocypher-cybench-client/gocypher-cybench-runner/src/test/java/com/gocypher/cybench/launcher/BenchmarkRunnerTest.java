package com.gocypher.cybench.launcher;

import com.gocypher.cybench.core.annotation.BenchmarkMetaData;
import com.gocypher.cybench.launcher.model.BenchmarkReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BenchmarkRunnerTest {
    @Test
    public void testMetadataPropogation() {
        BenchmarkReport benchmarkReport = new BenchmarkReport();
        BenchmarkRunner.appendMetadataFromClass(TestClass.class, benchmarkReport);
        benchmarkReport.getMetadata().forEach((k, v) -> System.out.println(k + " : " + v));
        assertEquals(2, benchmarkReport.getMetadata().size());
    }


    @BenchmarkMetaData(key = "A", value = "B")
    public static class TestSuperClass {

    }


    @BenchmarkMetaData(key = "C", value = "D")
    public static class TestClass extends TestSuperClass {

    }
}