package com.cho_co_song_i.yummy.yummy.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoToken {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("expires_in")
    private Long expiresIn;
    @JsonProperty("scope")
    private String scope;
    @JsonProperty("id_token")
    private String idToken;
}