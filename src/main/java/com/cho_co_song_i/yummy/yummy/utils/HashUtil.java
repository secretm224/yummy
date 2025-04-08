package com.cho_co_song_i.yummy.yummy.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class HashUtil {
    private static final int SALT_LENGTH = 16; /* 16 bytes */
    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH = 256; /* bits */

    private HashUtil() {} /* 유틸 클래스는 인스턴스화 방지 */

    /**
     * 무작위 salt 생성 (Base64)
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 비밀번호 + 솔트 → PBKDF2 해시 (Base64 인코딩)
     * @param password
     * @param base64Salt
     * @return
     * @throws Exception
     */
    public static String hashWithSalt(String password, String base64Salt) throws Exception {
        String combined = password + base64Salt;
        byte[] saltBytes = base64Salt.getBytes(); /* salt 자체도 key 역할 */

        KeySpec spec = new PBEKeySpec(combined.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hashed = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hashed);
    }

    /**
     * 비밀번호 검증
     * @param inputPassword
     * @param base64Salt
     * @param hashedValue
     * @return
     * @throws Exception
     */
    public static boolean verify(String inputPassword, String base64Salt, String hashedValue) throws Exception {
        String hashedInput = hashWithSalt(inputPassword, base64Salt);
        return hashedInput.equals(hashedValue);
    }
}
