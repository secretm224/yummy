package com.cho_co_song_i.yummy.yummy.dto;
import lombok.Data;

@Data
public class OauthLoginDto {
    private String oauthType;
    private String code;
}
