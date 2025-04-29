package com.cho_co_song_i.yummy.yummy.dto;

import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserOAuthResponse {
    private PublicStatus publicStatus;
    private String idToken;
    private Long userNum;
}