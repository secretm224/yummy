package com.cho_co_song_i.yummy.yummy.repository;

import org.junit.jupiter.api.Test;

class BasicUnitTest {

    @Test
    void simpleTest() {
        System.out.println("🔄 기본 단위 테스트 시작");

        // 가장 기본적인 테스트 - assertion 없이
        String testString = "Hello World";
        if (testString == null) {
            throw new RuntimeException("문자열이 null입니다");
        }
        if (!testString.equals("Hello World")) {
            throw new RuntimeException("문자열이 일치하지 않습니다");
        }

        System.out.println("✅ 기본 단위 테스트 성공");
    }

    @Test
    void mathTest() {
        System.out.println("🔄 수학 테스트 시작");

        int a = 5;
        int b = 3;
        int result = a + b;

        if (result != 8) {
            throw new RuntimeException("수학 계산이 틀렸습니다: " + result);
        }

        System.out.println("✅ 수학 테스트 성공: " + result);
    }
}