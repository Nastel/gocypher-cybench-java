package com.gocypher.benchmarks.jvm.client.tests;

import com.gocypher.benchmarks.core.model.BaseBenchmark;
import com.gocypher.benchmarks.core.utils.IOUtils;
import org.openjdk.jmh.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class IOSyncAPIComparisonBenchmarks extends BaseBenchmark {

    private static Logger LOG = LoggerFactory.getLogger(IOSyncAPIComparisonBenchmarks.class);

    private File srcFile ;
    private File targetFile;
    private long fileSize = 0;

    private FileChannel readFileChannel ;
    private FileChannel writeFileChannel ;

    //private int smallChunk = 4_096 ;
    private int smallChunk = 16_384 ;
    private int hugeChunk = 67_108_864 ;

    private boolean isSyncWrite = true ;

    @Setup(Level.Trial)
    public void setupForFork () throws  Exception {
        LOG.info("\n-->Will generate binary file for tests...");
        srcFile = IOUtils.generateSmallBinaryFileForTests();
        fileSize = srcFile.length();
        LOG.info("\n-->Generated file for processing ,size(B):{}", fileSize);
        readFileChannel = (FileChannel) Files.newByteChannel(srcFile.toPath(), EnumSet.of(StandardOpenOption.READ)) ;

    }
    @Setup(Level.Iteration)
    public void setupForIteration() throws Exception {
        LOG.info("\n Will setup for iteration...");
        targetFile = IOUtils.createSmallOutputFileForTests();
        writeFileChannel = (FileChannel) Files.newByteChannel(targetFile.toPath(), EnumSet.of(
                StandardOpenOption.READ,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) ;
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void copyFileUsingMappedByteBuffer ()throws Exception{
        IOUtils.rwFileUsingMappedByteBuffer(readFileChannel,writeFileChannel,isSyncWrite) ;
    }
    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void copyFileUsingFileStreamAndSmallChunks () throws Exception{
        long bytesCopied = IOUtils.copyFileUsingFileStreams(srcFile, targetFile, smallChunk, isSyncWrite);
        assert bytesCopied == fileSize;
    }
    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void copyFileUsingFileStreamAndHugeChunks () throws Exception{
        long bytesCopied = IOUtils.copyFileUsingFileStreams(srcFile, targetFile, hugeChunk, isSyncWrite);
        assert bytesCopied == fileSize;
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void copyFileUsingBufferedStreamAndSmallChunks () throws Exception{
        long bytesCopied = IOUtils.copyFileUsingBufferedStreams(srcFile, targetFile, smallChunk, isSyncWrite);
        assert bytesCopied == fileSize;
    }
    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void copyFileUsingBufferedStreamAndHugeChunks () throws Exception{
        long bytesCopied = IOUtils.copyFileUsingBufferedStreams(srcFile, targetFile, hugeChunk, isSyncWrite);
        assert bytesCopied == fileSize;
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void copyFileUsingDirectBufferedStreamAndSmallChunks () throws Exception{
        long bytesCopied = IOUtils.copyFileUsingDirectBufferedStreams(srcFile, targetFile, smallChunk, isSyncWrite);
        assert bytesCopied == fileSize;
    }
    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void copyFileUsingDirectBufferedStreamAndHugeChunks () throws Exception{
        long bytesCopied = IOUtils.copyFileUsingDirectBufferedStreams(srcFile, targetFile, hugeChunk, isSyncWrite);
        assert bytesCopied == fileSize;
    }



    @TearDown(Level.Iteration)
    public void cleanUpAfterIteration (){
        try {
            if (writeFileChannel != null){
                writeFileChannel.close();
            }
            IOUtils.removeFile(targetFile);
            LOG.info("\n==>Iteration clean up successful!");
        }catch (Exception e){
            LOG.error("Error on file removal",e);
        }
    }

    @TearDown(Level.Trial)
    public void cleanUpAfterFork (){
        try {
            if (readFileChannel != null){
                readFileChannel.close();
            }

            //IOUtils.removeFile(srcFile);
            LOG.info("\n==>Generated files were closed successfully!");
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