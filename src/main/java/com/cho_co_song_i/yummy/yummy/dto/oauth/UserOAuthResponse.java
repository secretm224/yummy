package com.cho_co_song_i.yummy.yummy.dto.oauth;

import com.cho_co_song_i.yummy.yummy.dto.oauth.google.GoogleOauthInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.oauth.kakao.KakaoOauthInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.oauth.naver.NaverOauthInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.oauth.telegram.TelegramOauthInfoDto;
import com.cho_co_song_i.yummy.yummy.entity.UserTbl;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserOAuthResponse {
    private OauthChannelStatus loginChannel;
    private PublicStatus publicStatus;
    private KakaoOauthInfoDto kakaoOauthInfoDto;
    private TelegramOauthInfoDto telegramOauthInfoDto;
    private NaverOauthInfoDto naverOauthInfoDto;
    private GoogleOauthInfoDto googleOauthInfoDto;
    private UserTbl userTbl;
}