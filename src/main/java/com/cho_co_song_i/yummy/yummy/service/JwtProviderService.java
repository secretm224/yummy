package com.cho_co_song_i.yummy.yummy.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
@Slf4j
public class JwtProviderService {

    private final SecretKey key;

    public JwtProviderService(@Value("${spring.redis.jwt.secret_key}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 2; /* 2시간 */
    private final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; /* 7일 */

    /**
     * Access Token 을 발급해주는 함수
     * @param userId
     * @return
     */
    public String generateAccessToken(String userId) {
        return Jwts.builder()
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key) /* 알고리즘은 키에서 자동 추론됨 */
                .compact();
    }

    /**
     * Refresh Token 을 발급해주는 함수
     * @param userId
     * @return
     */
    public String generateRefreshToken(String userId) {
        return Jwts.builder()
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(key) /* 알고리즘은 키에서 자동 추론됨 */
                .compact();
    }

    /**
     * Token 자체를 검증해주는 함수 - 알고리즘과 key 로 검증
     * @param token
     * @return
     */
    public String validateTokenAndGetSubject(String token) throws Exception {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

//        try {
//
//            return Jwts.parser()
//                    .verifyWith(key)
//                    .build()
//                    .parseSignedClaims(token)
//                    .getPayload()
//                    .getSubject();
//
//        } catch (ExpiredJwtException e) {
//            log.warn("⏰ JWT 토큰 만료됨: {}", e.getMessage());
//            throw new JwtValidationException("만료된 토큰입니다.", e);
//        } catch (UnsupportedJwtException e) {
//            log.warn("❌ 지원하지 않는 JWT 형식: {}", e.getMessage());
//            throw new JwtValidationException("지원되지 않는 토큰입니다.", e);
//        } catch (MalformedJwtException e) {
//            log.warn("❗ 잘못된 JWT 형식: {}", e.getMessage());
//            throw new JwtValidationException("잘못된 형식의 토큰입니다.", e);
//        } catch (SecurityException | SignatureException e) {
//            log.warn("🔐 서명 검증 실패: {}", e.getMessage());
//            throw new JwtValidationException("위조된 토큰입니다.", e);
//        } catch (IllegalArgumentException e) {
//            log.warn("❓ 토큰이 비어 있거나 null임: {}", e.getMessage());
//            throw new JwtValidationException("잘못된 요청입니다.", e);
//        }
    }

}
