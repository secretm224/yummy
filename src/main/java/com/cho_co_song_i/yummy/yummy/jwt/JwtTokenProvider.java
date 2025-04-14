package com.cho_co_song_i.yummy.yummy.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/*
    Swagger 에서 특정 함수나 controller 에 대해서 인증 후 함수를 호출 할 수 있도록 설정 하기 위해서

 */
@Component
public class JwtTokenProvider {

    @Value("${spring.redis.jwt.secret_key}")
    private String secretKey;
//
    public boolean validateToken(String token) {
        try {
                  Jwts.parser()
                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER")); // 혹은 claims에서 roles 추출

        return new UsernamePasswordAuthenticationToken(username, "", authorities);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String createToken(String username, String role) {
        Date now = new Date();
       // Date validity = new Date(now.getTime() + 1000 * 60 * 60); // 1시간

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                //.expiration(validity)
                .claims(Map.of("auth", role))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }
}
