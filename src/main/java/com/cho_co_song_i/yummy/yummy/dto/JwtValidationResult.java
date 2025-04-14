package com.cho_co_song_i.yummy.yummy.dto;

import com.cho_co_song_i.yummy.yummy.enums.JwtValidationStatus;

/**
 * JWT 확인결과 관련 DTO
 */
public class JwtValidationResult {
    private final JwtValidationStatus status;
    private final String subject;

    public JwtValidationResult(JwtValidationStatus status, String subject) {
        this.status = status;
        this.subject = subject;
    }

    public JwtValidationStatus getStatus() { return status; }
    public String getSubject() { return subject; }
}