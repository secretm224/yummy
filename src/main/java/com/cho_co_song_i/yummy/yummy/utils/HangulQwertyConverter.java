package com.cho_co_song_i.yummy.yummy.utils;

import java.util.*;

public class HangulQwertyConverter {

    private static final char[] CHOSEONG = {
            'ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ','ㅅ',
            'ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'
    };

    private static final char[] JUNGSEONG = {
            'ㅏ','ㅐ','ㅑ','ㅒ','ㅓ','ㅔ','ㅕ','ㅖ',
            'ㅗ','ㅘ','ㅙ','ㅚ','ㅛ','ㅜ','ㅝ','ㅞ','ㅟ','ㅠ','ㅡ','ㅢ','ㅣ'
    };

    private static final char[] JONGSEONG = {
            '\0','ㄱ','ㄲ','ㄳ','ㄴ','ㄵ','ㄶ','ㄷ','ㄹ','ㄺ','ㄻ','ㄼ','ㄽ','ㄾ',
            'ㄿ','ㅀ','ㅁ','ㅂ','ㅄ','ㅅ','ㅆ','ㅇ','ㅈ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'
    };

    // QWERTY 키 → 자모 매핑
    private static final Map<Character, Character> QWERTY_TO_JAMO = new HashMap<>();
    static {
        String eng  = "rRseEfaqQtTdwWczxvgkoiOjpuPhynbml";
        String jamo = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ"
                + "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅛㅜㅠㅡㅣ";
        for (int i = 0; i < eng.length(); i++) {
            QWERTY_TO_JAMO.put(eng.charAt(i), jamo.charAt(i));
        }
    }

    // 복합 모음 조합 맵
    private static final Map<String, Character> JUNGSEONG_COMBOS = Map.of(
            "ㅗㅏ", 'ㅘ', "ㅗㅐ", 'ㅙ', "ㅗㅣ", 'ㅚ',
            "ㅜㅓ", 'ㅝ', "ㅜㅔ", 'ㅞ', "ㅜㅣ", 'ㅟ',
            "ㅡㅣ", 'ㅢ'
    );

    // 복합 종성 조합 맵
    private static final Map<String, Character> JONGSEONG_COMBOS = Map.ofEntries(
            Map.entry("ㄱㅅ", 'ㄳ'),
            Map.entry("ㄴㅈ", 'ㄵ'),
            Map.entry("ㄴㅎ", 'ㄶ'),
            Map.entry("ㄹㄱ", 'ㄺ'),
            Map.entry("ㄹㅁ", 'ㄻ'),
            Map.entry("ㄹㅂ", 'ㄼ'),
            Map.entry("ㄹㅅ", 'ㄽ'),
            Map.entry("ㄹㅌ", 'ㄾ'),
            Map.entry("ㄹㅍ", 'ㄿ'),
            Map.entry("ㄹㅎ", 'ㅀ'),
            Map.entry("ㅂㅅ", 'ㅄ')
    );

    public static String convertQwertyToHangul(String input) {
        // 1) QWERTY → 자모 리스트
        List<Character> jamos = new ArrayList<>(input.length());
        for (char c : input.toCharArray()) {
            Character jm = QWERTY_TO_JAMO.get(c);
            if (jm != null) jamos.add(jm);
        }

        StringBuilder sb = new StringBuilder();
        int i = 0, n = jamos.size();
        while (i < n) {
            // --- 1. 초성 ---
            int cho = indexOf(CHOSEONG, jamos.get(i));
            if (cho < 0) {
                sb.append(jamos.get(i++));
                continue;
            }
            i++;

            // --- 2. 중성 (복합 포함) ---
            int jung = -1;
            if (i < n) {
                char first = jamos.get(i);
                char combined = first;
                if (i + 1 < n) {
                    String key = "" + first + jamos.get(i + 1);
                    Character combo = JUNGSEONG_COMBOS.get(key);
                    if (combo != null) {
                        combined = combo;
                        i += 2;
                    } else {
                        i++;
                    }
                } else {
                    i++;
                }
                jung = indexOf(JUNGSEONG, combined);
                if (jung < 0) {
                    // 중성 매핑 실패 시 초성만 출력
                    sb.append(CHOSEONG[cho]).append(combined);
                    continue;
                }
            } else {
                sb.append(CHOSEONG[cho]);
                break;
            }

            // --- 3. 종성 (복합 종성 먼저 시도) ---
            int jong = 0;
            if (i < n) {
                // 3-1) 복합 종성
                if (i + 1 < n) {
                    String key = "" + jamos.get(i) + jamos.get(i + 1);
                    Character combo = JONGSEONG_COMBOS.get(key);
                    if (combo != null) {
                        jong = indexOf(JONGSEONG, combo);
                        if (jong > 0) {
                            i += 2;
                        }
                    }
                }
                // 3-2) 단일 종성
                if (jong == 0) {
                    char cand = jamos.get(i);
                    int idx = indexOf(JONGSEONG, cand);
                    boolean nextIsJung = (i + 1 < n && indexOf(JUNGSEONG, jamos.get(i + 1)) >= 0);
                    if (idx > 0 && !nextIsJung) {
                        jong = idx;
                        i++;
                    }
                }
            }

            // --- 4. 완성형 한글 조합 및 append ---
            char syllable = (char)(0xAC00 + (cho * 21 * 28) + (jung * 28) + jong);
            sb.append(syllable);
        }

        return sb.toString();
    }

    private static int indexOf(char[] arr, char target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) return i;
        }
        return -1;
    }
}
