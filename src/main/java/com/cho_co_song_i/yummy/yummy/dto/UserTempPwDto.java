package com.cho_co_song_i.yummy.yummy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTempPwDto {
    private Long userNo;
    private String userId;
    private Date regDt;
    private String regId;
    private Date chgDt;
    private String chgId;
}
