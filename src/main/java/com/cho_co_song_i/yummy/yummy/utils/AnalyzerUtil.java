package com.cho_co_song_i.yummy.yummy.utils;

import org.apache.commons.text.similarity.LevenshteinDistance;

public class AnalyzerUtil {

    /**
     * Levenstein 알고리즘을 사용하여 단어간의 유사도를 계산
     * @param query
     * @param candidate
     * @return
     */
    public static double similarityByLevenstein(String query, String candidate) {
        LevenshteinDistance dist = LevenshteinDistance.getDefaultInstance();
        int edits = dist.apply(query, candidate);
        int maxLen = Math.max(query.length(), candidate.length());

        if (maxLen == 0) return 1.0; /* 둘 다 빈 문자열일 경우 */
        double base = 1.0 - ((double) edits / maxLen);

        /* 보정: 접두사, 부분 문자열 일치 시 보너스 */
        double bonus = 0.0;

        if (candidate.startsWith(query)) {
            bonus += 0.3;
        } else if (candidate.contains(query)) {
            bonus += 0.15;
        }

        return Math.min(1.0, base + bonus);
    }

    /**
     * 단어가 모두 영어로 되어있는지 확인해주는 함수
     * @param input
     * @return
     */
    public static boolean isAllEnglish(String input) {
        String cleaned = input.replaceAll("\\s+", "");
        return cleaned.matches("[a-zA-Z]+");
    }
}
