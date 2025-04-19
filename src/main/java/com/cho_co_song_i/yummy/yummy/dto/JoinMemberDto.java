package com.cho_co_song_i.yummy.yummy.dto;

import lombok.Data;

@Data
public class JoinMemberDto {
    private String userId;
    private String password;
    private String email;
    private String name;
    private String birthDate;
    private String telecom;
    private String gender;
    private String phoneNumber;
}
