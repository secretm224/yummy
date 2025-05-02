package com.cho_co_song_i.yummy.yummy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicInfoDto {
    private String userId;
    private String userNm;
    private String userBirth;
    private String userPic;
    private BigDecimal lngX;
    private BigDecimal latY;
}
