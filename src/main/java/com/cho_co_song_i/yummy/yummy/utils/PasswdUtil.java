package com.cho_co_song_i.yummy.yummy.utils;

import java.security.SecureRandom;

public class PasswdUtil {

    /**
     * 무작위 비밀번호를 만들어주는 함수
     * @return
     */
    public static String makeTempPw() {
        final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String lower = "abcdefghijklmnopqrstuvwxyz";
        final String special = "!@#$%^&*()-_=+";
        final String all = upper + lower + special;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(all.length());
            password.append(all.charAt(index));
        }

        return password.toString();
    }

}
