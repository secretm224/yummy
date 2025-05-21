package com.cho_co_song_i.yummy.yummy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class KakaoUserInfo {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("kakao_account")
    private Map<String, Object> kakaoAccount;

    @JsonProperty("properties")
    private Map<String, Object> properties;

}