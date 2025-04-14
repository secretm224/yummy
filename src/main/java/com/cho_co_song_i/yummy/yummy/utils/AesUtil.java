package com.cho_co_song_i.yummy.yummy.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.security.SecureRandom;

public class AesUtil {
    private AesUtil() {}
    
    /**
     * AES 암호화
     * @param plainText
     * @return
     * @throws Exception
     */
    public static String encrypt(String initVector, String secretKey, String plainText) throws Exception {
        IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

        byte[] encrypted = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * AES 암호화 - IV 를 랜덤하게 생성해서 조합하는 방식
     * @param secretKey
     * @param plainText
     * @return
     * @throws Exception
     */
    public static String encryptRandom(String secretKey, String plainText) throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] ivBytes = new byte[16]; /* 16 bytes = 128bit */
        random.nextBytes(ivBytes);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        /* IV + 암호문 결합 */
        byte[] result = new byte[ivBytes.length + encrypted.length];
        System.arraycopy(ivBytes, 0, result, 0, ivBytes.length);
        System.arraycopy(encrypted, 0, result, ivBytes.length, encrypted.length);

        return Base64.getEncoder().encodeToString(result);
    }

    /**
     * AES 복호화
     * @param encryptedText
     * @return
     * @throws Exception
     */
    public static String decrypt(String initVector, String secretKey, String encryptedText) throws Exception {
        IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

        byte[] original = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(original);
    }

    /**
     * AES 복호화 - 앞에 16자리 IV 를 사용해서 복호화 하는 방식
     * @param secretKey
     * @param encryptedText
     * @return
     * @throws Exception
     */
    public static String decryptRandom(String secretKey, String encryptedText) throws Exception {

        byte[] input = Base64.getDecoder().decode(encryptedText);

        /* 앞 16바이트 = IV */
        byte[] ivBytes = Arrays.copyOfRange(input, 0, 16);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        /* 나머지 - 암호문 */
        byte[] encryptedBytes = Arrays.copyOfRange(input, 16, input.length);

        SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        byte[] original = cipher.doFinal(encryptedBytes);

        return new String(original, StandardCharsets.UTF_8);
    }

}
