package com.cho_co_song_i.yummy.yummy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 입력받는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardLoginDto {
    private String userId;
    private String userPw;
}