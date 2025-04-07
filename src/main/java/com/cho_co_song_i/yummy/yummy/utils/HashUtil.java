package com.cho_co_song_i.yummy.yummy.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class HashUtil {
    private HashUtil() {}

    private static final int SALT_LENGTH = 16; // 16바이트
    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH = 256; // bits

    /**
     * 무작위 salt 생성
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 단방향 해시 생성 (입력 + salt)
     */
    public static String hash(String input, String base64Salt) throws Exception {
        byte[] salt = Base64.getDecoder().decode(base64Salt);

        KeySpec spec = new PBEKeySpec(input.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] hashed = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hashed);
    }

    /**
     * 해시 검증 (입력값과 저장된 해시가 일치하는지)
     */
    public static boolean verify(String input, String base64Salt, String hashedValue) throws Exception {
        String newHash = hash(input, base64Salt);
        return newHash.equals(hashedValue);
    }

}
