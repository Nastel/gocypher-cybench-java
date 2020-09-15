package com.gocypher.benchmarks.core.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.SecureRandom;

public class IOUtils {
    private static Logger LOG = LoggerFactory.getLogger(IOUtils.class);

    public static File createOutputFileForTests (){
        return createFile("output-test.png") ;
    }

    public static File createFile (String name){
        return new File (name) ;
    }

    public static File openInputPictureForTests () throws Exception{
        return openFile("test_picture.png") ;
    }
    public static File generateBinaryFileForTests () throws Exception{
        //return new File("c:/development/benchmark_tests/binary.txt") ;
        createRandomBinaryFile("binary.txt",300000);
        return new File ("binary.txt") ;
    }

    public static File openFile (String fileName) throws Exception{
        File srcFile = createFile(fileName);
        InputStream is = openFileAsInputStream(fileName) ;
        try {
            long copiedBytes = copyFileUsingBufferedStreams(is, srcFile, 65536);
        } finally {
            is.close();
        }
        return srcFile ;
    }

    private static InputStream openFileAsInputStream(String fileName){
        ClassLoader CLDR = IOUtils.class.getClassLoader() ;
        InputStream in = CLDR.getResourceAsStream(fileName);
        return in ;
    }
    public static void removeFile (File file){
        try {
            if (file != null && file.exists()) {
                file.delete();
            }
        }catch (Exception e){
            e.printStackTrace();
            LOG.error ("Error on removing file",e) ;
        }
    }

    public static long copyFileUsingBufferedStreams(File srcFile, File targetFile, int bufferSize) throws IOException {
        //if (LOGGER.isDebugEnabled())
        //    LOGGER.debug("copyFileUsingBufferedStreams " + srcFile + " -> " + targetFile + ", bufferSize=" + bufferSize);
        long bytesCopied = 0L;
        byte[] buffer = new byte[bufferSize];

        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(srcFile))) {
            try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile))) {
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    bytesCopied += bytesRead;
                }
            }
        }

        return bytesCopied;
    }

    private static long copyFileUsingBufferedStreams(InputStream inputStream, File targetFile, int bufferSize) throws IOException {
        //if (LOGGER.isDebugEnabled())
        //    LOGGER.debug("copyFileUsingBufferedStreams " + srcFile + " -> " + targetFile + ", bufferSize=" + bufferSize);
        long bytesCopied = 0L;
        byte[] buffer = new byte[bufferSize];

            try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile))) {
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    bytesCopied += bytesRead;
                }
            }


        return bytesCopied;
    }

    public static void storeResultsToFile (String fileName, String content){
        FileWriter file = null ;
        try {
            file = new FileWriter(fileName);
            file.write(content);

        } catch (Exception e) {
            e.printStackTrace();
            LOG.error ("Error on storing results to file",e) ;

        } finally {

            try {
                file.flush();
                file.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static void createRandomBinaryFile (String name, long sizePer4KB){
        File file = new File (name) ;
        try (FileOutputStream out = new FileOutputStream(file)) {
            for (int i = 0; i < sizePer4KB;i++) {
                byte[] bytes = new byte[4096];
                new SecureRandom().nextBytes(bytes);
                out.write(bytes);
            }
        }catch (Exception e){
            LOG.error("Error during generation of tmp file",e);
        }
    }


}
