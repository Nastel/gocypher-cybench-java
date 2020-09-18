package com.gocypher.benchmarks.jvm.client.tests;

import com.gocypher.benchmarks.core.model.BaseBenchmark;
import com.gocypher.benchmarks.core.utils.IOUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class IOBenchmarks extends BaseBenchmark {
    private static Logger LOG = LoggerFactory.getLogger(IOBenchmarks.class) ;

    private File srcFile ;
    private File targetFile;
    private long fileSize = 0;

    @Param({"65536"})
    public int smallChunk ;

    @Param({"36700160"})
    public int hugeChunk ;


    private RandomAccessFile seekSrc ;
    private RandomAccessFile seekDst ;

    private long [] arrayOfRandomNumbersForSmallChunks ;
    private long [] arrayOfRandomNumbersForHugeChunks ;
    public static int smallSeekChunk = 4_096 ;
    public static int smallSeekIterationsCount = 262_144 ;
    public static int hugeSeekChunk = 16_777_216 ;
    public static int hugeSeekIterationsCount = 64 ;



    @Setup
    public void setupEnvironment () throws  Exception{
        LOG.info ("\n-->Will generate binary file for tests...") ;
        srcFile = IOUtils.generateBinaryFileForTests();
        fileSize = srcFile.length();
        LOG.info("\n-->Generated file for processing ,size(B):{}",fileSize);
        targetFile = IOUtils.createOutputFileForTests();

        seekSrc = new RandomAccessFile(srcFile,"r") ;
        seekDst = new RandomAccessFile(targetFile,"rw") ;

        LOG.info("Will generate an array of random numbers for file positions") ;
        this.arrayOfRandomNumbersForSmallChunks = IOUtils.getArrayOfRandomNumberUsingLongs(0,fileSize-smallSeekChunk-10,smallSeekIterationsCount) ;
        this.arrayOfRandomNumbersForHugeChunks = IOUtils.getArrayOfRandomNumberUsingLongs(0,fileSize-hugeSeekChunk-10,hugeSeekIterationsCount) ;

    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void rwFileUsingFileStreamsSmallChunks(Blackhole blackHole) throws IOException {
        long bytesCopied = IOUtils.copyFileUsingFileStreams(srcFile, targetFile, smallChunk);
        assert bytesCopied == fileSize;
    }
    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void rwFileUsingFileStreamsHugeChunks(Blackhole blackHole) throws IOException {
        long bytesCopied = IOUtils.copyFileUsingFileStreams(srcFile, targetFile, hugeChunk);
        assert bytesCopied == fileSize;
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public int seekAndReadFileSmallChunks () throws  Exception{
        int bytesRead = 0 ;
        for (long position:arrayOfRandomNumbersForSmallChunks) {
            bytesRead += IOUtils.seekAndReadFile(seekSrc, (int) fileSize, smallSeekChunk, position);
        }
        LOG.info("Read bytes:{}",bytesRead);
        return bytesRead;
    }
    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public int seekAndReadFileHugeChunks () throws  Exception{
        int bytesRead = 0 ;
        for (long position:arrayOfRandomNumbersForHugeChunks) {
            bytesRead+= IOUtils.seekAndReadFile(seekSrc, (int) fileSize, hugeSeekChunk, position);
        }
        LOG.info("Read bytes:{}",bytesRead);
        return bytesRead;
    }

    @TearDown
    public void cleanUpEnvironment (){
        try {
            if (seekSrc != null) {
                seekSrc.close();
            }
            if (seekDst != null) {
                seekDst.close();
            }
            IOUtils.removeFile(srcFile);
            IOUtils.removeFile(targetFile);

            LOG.info("\n==>Generated files were removed successfully!");
        }catch (Exception e){
            LOG.error("Error on file removal",e);
        }
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
