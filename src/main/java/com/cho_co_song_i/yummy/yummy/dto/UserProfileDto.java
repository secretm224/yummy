package com.cho_co_song_i.yummy.yummy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
public class UserProfileDto {
    private Long userNo;
    private String userNm;
    private String loginChannel;
    private String tokenId;
    private String addrType;
    private String addr;
    private BigDecimal lngx;
    private BigDecimal laty;
    private Date regDt;
}
