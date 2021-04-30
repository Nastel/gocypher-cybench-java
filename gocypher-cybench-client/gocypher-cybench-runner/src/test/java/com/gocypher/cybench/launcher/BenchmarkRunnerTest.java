package com.gocypher.cybench.launcher;

import com.gocypher.cybench.core.annotation.BenchmarkMetaData;
import com.gocypher.cybench.launcher.model.BenchmarkReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    public interface E extends I{

    }

    @BenchmarkMetaData(key = "G", value = "H")
    public interface G {

    }

    @BenchmarkMetaData(key = "I", value = "J")
    public interface I {

    }
}