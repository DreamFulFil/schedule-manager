package org.dream.scheduled.tasks.util;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {

    private static final String key = "3evm0fhbESa1O7PZs35H8CL1g4fGLd20";
    private static final String initVector = "6Hi32s1q950nZ51n";
    
    public static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
     
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
     
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return new String(Base64.getEncoder().encode(encrypted));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public static String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
     
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
     
            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
     
        return null;
    }
    
    public static void main(String[] args) {
        String original = "password";
        String encrypted = AESUtil.encrypt("password");
        String decrypted = AESUtil.decrypt(encrypted);
        System.err.println(original);
        System.err.println(encrypted);
        System.err.println(decrypted);
    }
    
}
