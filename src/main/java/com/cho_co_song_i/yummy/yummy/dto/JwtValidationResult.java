package com.cho_co_song_i.yummy.yummy.dto;

import com.cho_co_song_i.yummy.yummy.enums.JwtValidationStatus;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JWT 확인결과 관련 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtValidationResult {
    private String jwtName;
    private JwtValidationStatus status;
    private Claims claims;
}