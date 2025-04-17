package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.ErrorResponse;
import com.cho_co_song_i.yummy.yummy.dto.JwtValidationResult;
import com.cho_co_song_i.yummy.yummy.enums.JwtValidationStatus;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class JwtProviderService {

    private final SecretKey key;

    public JwtProviderService(@Value("${spring.redis.jwt.secret_key}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 3; /* 3시간 */
    private final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; /* 7일 */

    /**
     * Access Token 을 발급해주는 함수
     * @param userHashedId
     * @return
     */
    public String generateAccessToken(String userHashedId) {
        return Jwts.builder()
                .subject(userHashedId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key) /* 알고리즘은 키에서 자동 추론됨 */
                .compact();
    }

    /**
     * Refresh Token 을 발급해주는 함수
     * @param userHashedId
     * @return
     */
    public String generateRefreshToken(String userHashedId) {
        return Jwts.builder()
                .subject(userHashedId)
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
    public JwtValidationResult validateTokenAndGetSubject(String token) {

        try {

            String jwtParser = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

            return new JwtValidationResult(JwtValidationStatus.SUCCESS, jwtParser);

        } catch (ExpiredJwtException e) {
            log.warn("JJWT token expired: {}", e.getMessage());
            return new JwtValidationResult(JwtValidationStatus.EXPIRED, null);
        } catch (UnsupportedJwtException e) {
            log.error("JWT format not supported: {}", e.getMessage());
            return new JwtValidationResult(JwtValidationStatus.INVALID, null);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT format: {}", e.getMessage());
            return new JwtValidationResult(JwtValidationStatus.INVALID, null);
        } catch (SecurityException e) {
            log.error("Signature Verification Failed: {}", e.getMessage());
            return new JwtValidationResult(JwtValidationStatus.INVALID, null);
        } catch (IllegalArgumentException e) {
            log.error("Token is empty or null: {}", e.getMessage());
            return new JwtValidationResult(JwtValidationStatus.INVALID, null);
        }
    }

}
