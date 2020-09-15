package com.gocypher.benchmarks.jvm.client.tests;

import com.gocypher.benchmarks.core.model.BaseBenchmark;
import com.gocypher.benchmarks.core.utils.IOUtils;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class IOBenchmarks extends BaseBenchmark {

    private File srcFile ;
    private File targetFile;
    private long fileSize = 0;

    //@Param({"65536"})
    //@Param({"131072"})
    @Param({"1024","4096"})
    public int bufferSize;

    @Setup
    public void setupEnvironment () throws  Exception{

        srcFile = IOUtils.openInputPictureForTests();
        //srcFile = IOUtils.generateBinaryFileForTests();
        fileSize = srcFile.length();
        //System.out.println("File size for processing:"+fileSize);

        targetFile = IOUtils.createOutputFileForTests();

    }


    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void copyFileUsingBufferedStreams() throws IOException {
        long bytesCopied = IOUtils.copyFileUsingBufferedStreams(srcFile, targetFile, bufferSize);
        assert bytesCopied == fileSize;
    }

    @TearDown
    public void cleanUpEnvironment (){
        IOUtils.removeFile(srcFile);
        IOUtils.removeFile(targetFile);
    }


    @Override
    public String getCategory() {
        return "IO";
    }

    @Override
    public String getContext() {
        return "JVM";
    }
}
