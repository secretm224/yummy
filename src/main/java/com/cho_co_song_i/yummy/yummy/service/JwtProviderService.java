package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.JwtValidationResult;
import com.cho_co_song_i.yummy.yummy.enums.JwtValidationStatus;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.utils.CookieUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JwtProviderService {

    private final SecretKey key;

    public JwtProviderService(@Value("${spring.redis.jwt.secret_key}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
    private final long TEMP_TOKEN_EXPIRATION = 1000 * 60 * 10; /* 10분 */
    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60; /* 1시간 */
    private final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; /* 7일 */


    /**
     * Oauth 회원가입을 위한 임시 jwt
     * @param idToken
     * @return
     */
    public String generateOauthTempToken(String idToken, String oauthChannel) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("oauthChannel", oauthChannel);

        return Jwts.builder()
                .claims(claims)
                .subject(idToken)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TEMP_TOKEN_EXPIRATION))
                .signWith(key) /* 알고리즘은 키에서 자동 추론됨 */
                .compact();
    }


    /**
     * Access Token 을 발급해주는 함수
     * @param userNo
     * @param isTempPw
     * @param tokenId
     * @return
     */
    public String generateAccessToken(Long userNo, boolean isTempPw, String tokenId, OauthChannelStatus oauthChannel) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("isTempPw", isTempPw); /* 임시비밀번호 발급 여부 */
        claims.put("tokenId", tokenId); /* Yummy JWT 토큰 아이디 정보 */
        claims.put("oauthChannel", oauthChannel.toString()); /* Oauth 채널 종류 */

        return Jwts.builder()
                .claims(claims)
                .subject(userNo.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key) /* 알고리즘은 키에서 자동 추론됨 */
                .compact();
    }


    /**
     * Refresh Token 을 발급해주는 함수
     * @param userNo
     * @return
     */
    public String generateRefreshToken(Long userNo) {
        return Jwts.builder()
                .subject(userNo.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(key) /* 알고리즘은 키에서 자동 추론됨 */
                .compact();
    }

    /**
     * Token 자체를 검증해주는 함수 - 알고리즘과 key 로 검증
     * @param jwtName
     * @param req
     * @return
     */
    public JwtValidationResult validateAndParseJwt(String jwtName, HttpServletRequest req) {

        String token = CookieUtil.getCookieValue(req, jwtName);

        /* token 이 존재하는지 체크해준다. */
        if (token == null || token.isEmpty()) {
            return new JwtValidationResult(jwtName, JwtValidationStatus.EMPTY, null);
        }

        try {

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new JwtValidationResult(jwtName, JwtValidationStatus.SUCCESS, claims);

        } catch (ExpiredJwtException e) {
            log.warn("JJWT token expired: {}", e.getMessage());
            return new JwtValidationResult(jwtName, JwtValidationStatus.EXPIRED, e.getClaims());
        } catch (UnsupportedJwtException e) {
            log.error("[Error][JwtProviderService->validateAndParseJwt] JWT format not supported: {}", e.getMessage(), e);
            return new JwtValidationResult(jwtName, JwtValidationStatus.INVALID, null);
        } catch (MalformedJwtException e) {
            log.error("[Error][JwtProviderService->validateAndParseJwt] Invalid JWT format: {}", e.getMessage(), e);
            return new JwtValidationResult(jwtName, JwtValidationStatus.INVALID, null);
        } catch (SecurityException e) {
            log.error("[Error][JwtProviderService->validateAndParseJwt] Signature Verification Failed: {}", e.getMessage(), e);
            return new JwtValidationResult(jwtName, JwtValidationStatus.INVALID, null);
        } catch (IllegalArgumentException e) {
            log.error("[Error][JwtProviderService->validateAndParseJwt] Token is empty or null: {}", e.getMessage(), e);
            return new JwtValidationResult(jwtName, JwtValidationStatus.INVALID, null);
        } catch (Exception e) {
            log.error("[Error][JwtProviderService->validateAndParseJwt] Unspecified Exception Occurred: {}", e.getMessage(), e);
            return new JwtValidationResult(jwtName, JwtValidationStatus.INVALID, null);
        }
    }

}
