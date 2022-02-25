package org.dream.scheduled.tasks.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {

    private static final String KEY = "3evm0fhbESa1O7PZs35H8CL1g4fGLd20";
    private static final String INIT_VECTOR = "6Hi32s1q950nZ51n";

    private AESUtil(){}
    
    public static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
     
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
     
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return new String(Base64.getEncoder().encode(encrypted));
        } 
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public static String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
     
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            if(Objects.isNull(encrypted)) {
                return "";
            }
            else {
                byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
                return new String(original);
            }
        } 
        catch (Exception ex) {
            ex.printStackTrace();
        }
     
        return null;
    }
    
}
