package com.cho_co_song_i.yummy.yummy.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long userNo;
    private String userId;
//    private String userIdHash;
    private String userPw;
    private String userPwSalt;
    private String userNm;
    private String userBirth;
    private String userGender;
    private Date regDt;
    private String regId;
    private Date chgDt;
    private String chgId;
}
