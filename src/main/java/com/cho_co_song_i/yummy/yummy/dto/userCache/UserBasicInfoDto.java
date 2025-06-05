package com.cho_co_song_i.yummy.yummy.dto.userCache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 기본 유저의 정보 -> Redis 에 태워서 계속 호출될 것.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicInfoDto {
    private String userId;
    private String userNm;
    private String userBirth;
    private String userPic;
    private BigDecimal lng;
    private BigDecimal lat;
}
