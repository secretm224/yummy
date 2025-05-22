package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.OauthLoginDto;
import com.cho_co_song_i.yummy.yummy.dto.oauth.OauthUserSimpleInfoDto;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface LoginService {

    /**
     * Oauth2 를 사용해서 로그인 처리를 하는 함수
     * @param loginDto
     * @param res
     * @param req
     * @return
     * @throws Exception
     */
    PublicStatus handleOAuthLogin(OauthLoginDto loginDto, HttpServletResponse res, HttpServletRequest req) throws Exception;

    /**
     * 회원이 oauth2 를 통해 기존아이디 통합 또는 회원가입을 위해 임시 jwt 쿠키를 발급해준다.
     * @param idToken
     * @param res
     */
    void generateTempOauthJwtCookie(String idToken, HttpServletResponse res);

    /**
     * 유저의 기본적인 정보를 Oauth 통신을 통해서 가져와주는 함수
     * @param userNo
     * @return
     */
    OauthUserSimpleInfoDto getUserInfosByOauth(Long userNo);
}
