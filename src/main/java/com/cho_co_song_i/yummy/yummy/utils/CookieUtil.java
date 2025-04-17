package com.cho_co_song_i.yummy.yummy.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

public class CookieUtil {

    /* 인스턴스 생성 방지 */
    private CookieUtil() {}

    /* 쿠키 조회 */
    public static String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /* 쿠키 삭제 */
    public static void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /* 여러개의 쿠키를 삭제해주는 함수*/
    public static void clearCookies(HttpServletResponse response, String... cookieName) {

        for (String name : cookieName) {
            clearCookie(response, name);
        }

    }

    /* 쿠키 생성 */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAgeInSeconds) {

        String cookieHeader = name + "=" + value +
                "; Max-Age=" + maxAgeInSeconds +
                "; Path=/" +
                "; HttpOnly" +
                "; SameSite=Lax";

        response.addHeader("Set-Cookie", cookieHeader);
    }
}