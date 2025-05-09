package com.cho_co_song_i.yummy.yummy.enums;

public enum OauthChannelStatus {
    kakao,
    google,
    naver,
    telegram;

    /**
     * 문자열이 실제로 OauthChannelStatus 에 포함되는지 확인해주는 함수.
     * @param input
     * @return
     */
    public static boolean isValid(String input) {
        try {
            valueOf(input.toLowerCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
