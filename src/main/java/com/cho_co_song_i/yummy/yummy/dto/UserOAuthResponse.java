package com.cho_co_song_i.yummy.yummy.dto;

import com.cho_co_song_i.yummy.yummy.entity.UserTbl;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.model.KakaoToken;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserOAuthResponse {
    private PublicStatus publicStatus;
    private KakaoToken kakaoToken;
    private UserOAuthInfoDto userOAuthInfoDto;
    private OauthChannelStatus loginChannel;
    private UserTbl userTbl;
}