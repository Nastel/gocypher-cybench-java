package com.gocypher.benchmarks.core.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.SecureRandom;
import java.util.Random;

public class IOUtils {
    private static Logger LOG = LoggerFactory.getLogger(IOUtils.class);
    private static int randomFileChunkSize = 65536 ;
    private static long fileSizeMultiplierPerChunkSize = 16384 ;
    //private static long fileSizeMultiplierPerChunkSize = 1024 ;
    private static String FILE_NAME_AS_SRC = "binary.txt";
    private static String FILE_NAME_AS_DST = "output-binary-test.txt";

    public static File createOutputFileForTests (){
        return createFile(FILE_NAME_AS_DST) ;
    }

    public static File createFile (String name){
        return new File (name) ;
    }

    public static File generateBinaryFileForTests () throws Exception{
        createRandomBinaryFile(FILE_NAME_AS_SRC,fileSizeMultiplierPerChunkSize);
        File f = new File (FILE_NAME_AS_SRC) ;
        return f;
    }


    /*public static File openFile (String fileName) throws Exception{
        File srcFile = createFile(fileName);
        InputStream is = openFileAsInputStream(fileName) ;
        try {
            long copiedBytes = copyFileUsingBufferedStreams(is, srcFile, 65536);
        } finally {
            is.close();
        }
        return srcFile ;
    }
*/
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

    public static long copyFileUsingFileStreams(File srcFile, File targetFile, int bufferSize) throws IOException {
        long bytesCopied = 0L;
        byte[] buffer = new byte[bufferSize];

        try (InputStream in = new FileInputStream(srcFile)) {
            try (OutputStream out = new FileOutputStream(targetFile)) {
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    out.flush();
                    bytesCopied += bytesRead;
                }
            }
        }

        return bytesCopied;
    }

    private static long copyFileUsingBufferedStreams(InputStream inputStream, File targetFile, int bufferSize) throws IOException {
        long bytesCopied = 0L;
        byte[] buffer = new byte[bufferSize];

            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile))) {
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
    private static void createRandomBinaryFile (String name, long sizePer65KB){
        File file = new File (name) ;
        try (FileOutputStream out = new FileOutputStream(file)) {
            for (int i = 0; i < sizePer65KB;i++) {
                byte[] bytes = new byte[randomFileChunkSize];
                new SecureRandom().nextBytes(bytes);
                out.write(bytes);
            }
            out.flush();
        }catch (Exception e){
            LOG.error("Error during generation of tmp file",e);
        }
    }
    public static long getRandomBinaryFileSizeInBytes (){
        return randomFileChunkSize*fileSizeMultiplierPerChunkSize ;
    }

    public static int seekAndReadFile (RandomAccessFile file , long fileSize, int pageSize) throws Exception{
        //int pageSize = 4096 ;
        //int pageSize = 1048576 ;
        long position = getRandomNumberUsingIntegers (0,fileSize-pageSize-10) ;
        byte [] pageBytes = new byte[pageSize] ;
        int offset = 0 ;
        file.seek(position);
        int bytesRead = file.read(pageBytes, offset, pageSize);
        file.seek(0);
        return bytesRead ;
    }
    public static int seekAndReadFile (RandomAccessFile file , long fileSize, int pageSize ,long position) throws Exception{
        //int pageSize = 4096 ;
        //int pageSize = 1048576 ;
        //long position = getRandomNumberUsingIntegers (0,fileSize-pageSize-10) ;
        byte [] pageBytes = new byte[pageSize] ;
        int offset = 0 ;
        file.seek(position);
        int bytesRead = file.read(pageBytes, offset, pageSize);
        file.seek(0);
        return bytesRead ;
    }

    public static long getRandomNumberUsingIntegers(long min, long max) {
        Random random = new Random();
        return random.longs(min, max)
                .findFirst()
                .getAsLong();
    }
    public static long[] getArrayOfRandomNumberUsingLongs(long min, long max, int amount) {
        Random random = new Random();
        return random.longs(min, max)
                .limit(amount).toArray();
    }


}
