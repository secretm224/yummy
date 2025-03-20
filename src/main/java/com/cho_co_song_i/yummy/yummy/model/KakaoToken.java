package com.cho_co_song_i.yummy.yummy.model;
import lombok.Data;

@Data
public class KakaoToken {
    private String access_token;
    private String token_type;
    private String refresh_token;
    private Long expires_in;
    private String scope;
    private  String id_token;

}
