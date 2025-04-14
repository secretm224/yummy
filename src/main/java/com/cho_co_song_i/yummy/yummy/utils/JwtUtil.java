package com.cho_co_song_i.yummy.yummy.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtUtil {
    /* 인스턴스 생성 방지 */
    private JwtUtil() {}

    /**
     * jwt 페이로드를 해석(디코드)해주는 함수
     * @param idToken
     * @return
     */
    public static Map<String, Object> decodeJwtPayload(String idToken) {
        if (idToken == null || idToken.isEmpty()) return Collections.emptyMap();

        DecodedJWT decoded = JWT.decode(idToken);
        return decoded.getClaims().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().as(Object.class)
                ));
    }
}
