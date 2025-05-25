package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.oauth.OauthLoginDto;
import com.cho_co_song_i.yummy.yummy.dto.oauth.OauthUserSimpleInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.oauth.UserOAuthResponse;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface LoginService {
    /**
     * kakao, naver, google, telegram 구분 용 메서드
     * @return
     */
    OauthChannelStatus getOauthChannel();

    /**
     * Login Code 가 주어졌을 때, 유저의 Oauth에 대한 정보를 반환해주는 함수
     * @param code
     * @return
     * @throws Exception
     */
    UserOAuthResponse getOauthLoginInfo(String code) throws Exception;

    /**
     * 채널별 Oauth2 토큰 정보를 레디스에 저장해주는 함수.
     * @param userNo
     * @param response
     */
    void saveOauthTokenToRedis(Long userNo, UserOAuthResponse response);

    /**
     * Oauth 인증을 통해 기본 정보를 가져와주는 함수
     * @param userNo
     * @return
     */
    OauthUserSimpleInfoDto getUserInfosByOauth(Long userNo);
}
