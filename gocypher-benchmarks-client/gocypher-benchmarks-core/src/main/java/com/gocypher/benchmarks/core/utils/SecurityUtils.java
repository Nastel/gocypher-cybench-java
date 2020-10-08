/*
 * Copyright (C) 2020, K2N.IO.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package com.gocypher.benchmarks.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SecurityUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityUtils.class);
    private static final String BEGIN_PRIVATE_KEY="-----BEGIN RSA PRIVATE KEY-----\n";
    private static final String END_PRIVATE_KEY="\n-----END RSA PRIVATE KEY-----\n";

    private static final String BEGIN_PUBLIC_KEY="-----BEGIN RSA PUBLIC KEY-----\n";
    private static final String END_PUBLIC_KEY="\n-----END RSA PUBLIC KEY-----\n";

    public static String computeStringHash (String string){
        if (string != null) {
            try {
                return hashByteArray(string.getBytes());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null ;
    }
    public static String computeClassHash (Object object){
        if (object != null){
            return computeClassHash(object.getClass()) ;
        }
        return null ;
    }
    public static String computeClassHash (Class<?> clazz){
        if (clazz != null){
            String name = clazz.getName();
            //String fileName = "/" + name.replaceAll("\\.", "/") + ".class";
            String fileName = "" + name.replaceAll("\\.", "/") + ".class";
            LOG.info("Will access class bytes:{}",fileName) ;
            //ClassLoader cldr = clazz.getClassLoader() ;
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try (InputStream in = loader.getResourceAsStream(fileName)){
                byte[] classBytes = file2ByteArray(in);
                String classMD5Hash = hashByteArray(classBytes);
                return classMD5Hash ;
            }catch (Exception e){
                LOG.error ("Error on class hash computing",e) ;
            }


        }
        return null ;
    }
    private static byte[] file2ByteArray (InputStream inputStream){
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            byte[] byteArray = buffer.toByteArray();
            return byteArray ;
        }catch (Exception e){
            LOG.error ("Error on read file",e) ;
            return null ;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }catch (Throwable t){
                }
            }
        }
    }
    private static String hashByteArray (byte[] classBytes) throws Exception{
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.reset();
        byte [] digested = md.digest(classBytes) ;
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<digested.length;i++){
            sb.append(Integer.toHexString(0xff & digested[i]));
        }
        return sb.toString();
    }

    public static void generateAndStoreKeyPair (String fileName){
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(4096);
            KeyPair kp = kpg.generateKeyPair();
            Key pub = kp.getPublic();
            Key pvt = kp.getPrivate();

            Base64.Encoder encoder = Base64.getEncoder();
            Writer out = new FileWriter(fileName + ".key");
            out.write(BEGIN_PRIVATE_KEY);
            out.write(encoder.encodeToString(pvt.getEncoded()));
            out.write(END_PRIVATE_KEY);
            out.close();

            out = new FileWriter(fileName + ".pub");
            out.write(BEGIN_PUBLIC_KEY);
            out.write(encoder.encodeToString(pub.getEncoded()));
            out.write(END_PUBLIC_KEY);
            out.close();


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static RSAPublicKey loadPublicKey (){
        try {

            byte[] keyBytes = loadBase64EncodedKey("cybench_key.pub", true);
            X509EncodedKeySpec ks = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(ks);
            return pubKey ;
        }catch (Exception e){
            LOG.error ("Error on loading key",e) ;
        }
        return null ;
    }
    public static PrivateKey loadPrivateKey (){
        try {
            byte[] keyBytes = loadBase64EncodedKey("cybench_key.key", false);
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey pvt = kf.generatePrivate(ks);
            return pvt ;
        }catch (Exception e){
            LOG.error ("Error on loading key",e) ;
        }
        return  null ;
    }

    private static byte[] loadBase64EncodedKey (String fileName, boolean isPublicKey ){
        ClassLoader CLDR = SecurityUtils.class.getClassLoader() ;
        InputStream in = CLDR.getResourceAsStream(fileName);
        byte[] keyBytes1 = file2ByteArray(in) ;
        String keyStr = new String (keyBytes1) ;
        String publicKeyPEM = keyStr
                .replace(isPublicKey?BEGIN_PUBLIC_KEY:BEGIN_PRIVATE_KEY, "")
                //.replaceAll(System.lineSeparator(), "")
                .replace(isPublicKey?END_PUBLIC_KEY:END_PRIVATE_KEY, "");

        return Base64.getDecoder().decode(publicKeyPEM);
    }

    private static byte[] encryptRSA(PublicKey key, byte[] plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(plaintext);
    }
    public static String encryptReport (String report){

        try {
            if (report != null && !report.isEmpty()) {
                RSAPublicKey key = loadPublicKey();
                SecretKey secKey = generateKey() ;
                byte[] encryptedReport =  encryptAES(secKey,report) ;
                byte[] encryptedKey = encryptRSA(key, secKey.getEncoded());

                Base64.Encoder encoder = Base64.getEncoder();
                Map<String,String>map = new HashMap<>() ;
                map.put ("encryptedKey",encoder.encodeToString(encryptedKey)) ;
                map.put ("encryptedReport",encoder.encodeToString(encryptedReport)) ;
                return encoder.encodeToString(JSONUtils.marshalToJson(map).getBytes()) ;
            }


        }catch (Exception e){
            e.printStackTrace();
        }
        return null ;
    }

    private static byte[] encryptAES (SecretKey secKey, String text) throws Exception{

        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, secKey);
        byte[] byteCipherText = aesCipher.doFinal(text.getBytes("UTF-8"));
        return byteCipherText ;
    }
    private static SecretKey generateKey () throws Exception{
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128); // The AES key size in number of bits
        SecretKey secKey = generator.generateKey();
        return secKey ;
    }



}
